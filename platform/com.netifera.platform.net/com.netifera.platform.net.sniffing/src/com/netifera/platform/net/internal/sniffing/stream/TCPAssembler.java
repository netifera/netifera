package com.netifera.platform.net.internal.sniffing.stream;

import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.packets.PacketPayload;
import com.netifera.platform.net.packets.tcpip.TCP;
import com.netifera.platform.net.packets.tcpip.TCPSequenceNumber;

/*
 * Assembles one side of a TCP session.
 */
public class TCPAssembler {
	
	private final static ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0).asReadOnlyBuffer();
	
	/* next expected sequence number */
	private TCPSequenceNumber nextSequence;
	
	final private SortedSet<TCP> reassemblyTree = 
		new TreeSet<TCP>(new Comparator<TCP>() {
			public int compare(TCP t1, TCP t2) {
				return (t1.sequence().compareTo(t2.sequence()));
			}
		});
	private final TCPReassemblyConfig config;
	private long lastReassemblyTimestamp = 0;
	
	private final static int OUTPUT_QUEUE_SIZE = 16;
	final private BlockingQueue<ByteBuffer> outputQueue = new ArrayBlockingQueue<ByteBuffer>(OUTPUT_QUEUE_SIZE);
	
	private final static int DEFAULT_OUTPUT_BUFFER_SIZE = 8 * 1024;
	private final ByteBuffer defaultOutputBuffer = ByteBuffer.allocate(DEFAULT_OUTPUT_BUFFER_SIZE);
	private ILogger logger;
	private boolean closed = false;
	private boolean reset = false;
	private long assembledByteCount = 0;
	
	public TCPAssembler(TCP syn, TCPReassemblyConfig config, ILogger logger) {
		if(!syn.getSYN()) {
			throw new IllegalArgumentException("Must pass a SYN segment to the constructor");
		}
		nextSequence = syn.nextSequence();
		this.config = config;
		this.logger = logger;
	}
	
	public long getAssembledByteCount() {
		return assembledByteCount;
	}
	
	public boolean hasReassemblyExpired(long timestamp) {
		if(lastReassemblyTimestamp == 0 || reassemblyTree.isEmpty())
			return false;
		
		return (timestamp - lastReassemblyTimestamp) > config.getReassemblyTimeout();
	}
	
	/**
	 * 
	 * This must always be called in the following way:
	 * 
	 *   assembler.addSegment(segment)
	 *   while(assembler.isDataAvailable()) {
	 *     ByteBuffer data = assembler.getAvailableData();
	 *     // Do something with data
	 *   }
	 *   
	 * Adding a segment may create output data which can be 
	 * consumed and in some situations it could fill a sequence
	 * number hole and cause more data to be placed in the output
	 * queue than can be delivered at one time.  In this case, the
	 * call to getAvailableData() will free up space in the queue
	 * which will be consumed causing isDataAvailable() to continue
	 * returning true until all data has been delivered.
	 * 
	 */
	public void addSegment(TCP tcp, long currentTimestamp) {
		if(tcp.getSYN()) {
			return;
		}
				
		final TCPSequenceNumber seq = tcp.sequence();
		
		/*
		 * Case A:
		 * 
		 * S --> Segment sequence number (seq)
		 * N --> Next expected byte (nextSequence)
		 * 
		 * The common case where this segment is the next
		 * one which is expected (S == N).
		 * 
		 *  [S        DATA         ]
		 *   ^
		 *   N
		 *   
		 */
		if(nextSequence.equals(seq)) {
			processSegment(tcp);
			return;
		}
		
		/*
		 * Case B:
		 * 
		 * Overlap with some duplication of already received data
		 * 
		 *  [S        DATA        ]
		 *       ^
		 *       N
		 */
		if(nextSequence.greater(seq) && nextSequence.less(tcp.nextSequence())) {
			processSegment(tcp);
			return;
		}
		
		/* 
		 * Case C:
		 * 
		 * This is a hole, into the reassembly tree!
		 * 
		 *      ...    [S        DATA        ]
		 *   ^
		 *   N
		 */
		
		if(config.canProcessFinOutsideReassembly() && checkFinAndRst(tcp)) {
			return;
		}
		
		if(nextSequence.less(seq)) {
			lastReassemblyTimestamp = currentTimestamp;
			tcp.persist();
			reassemblyTree.add(tcp);
			reassemble();
			if(isOverReassemblyByteLimit()) {
				logger.debug("Reassembly limit exceeded adding segment " + tcp);
				doClose();
				return;
			}
			return;
		}
		
		/*
		 * Case D:
		 * 
		 * If we are here then the next pointer must after the data.  Old news, retransmission, ignore.
		 * 
		 *  [S      DATA      ]  ...
		 *                           ^
		 *                           N
		 */
		
	}
	private boolean isOverReassemblyByteLimit() {
		final long limit = config.getMaximumPendingReassemblyBytes();
		if(limit == TCPReassemblyConfig.NO_LIMIT)
			return false;
		
		long sum = 0;
		for(TCP segment : reassemblyTree) {
			sum += segment.getLength();
		}
		return sum >= limit;
	}
	/**
	 * Return a ByteBuffer containing assembled stream data.
	 */
	public ByteBuffer getAvailableData() {
		int count = 0;
		for(ByteBuffer buffer : outputQueue) {
			count += buffer.remaining();
		}
		
		if(count == 0) {
			outputQueue.clear();
			return EMPTY_BUFFER;
		}
		
		ByteBuffer output;
		
		if(count > DEFAULT_OUTPUT_BUFFER_SIZE) {
			output = ByteBuffer.allocate(count);
		} else {
			output = defaultOutputBuffer;
			output.clear();
		}

		while(!outputQueue.isEmpty()) {
			output.put( outputQueue.remove() ); 
		}
		
		if(!reassemblyTree.isEmpty()) {
			reassemble();
		}
		
		output.flip();
		
		return output.asReadOnlyBuffer();
	}
	
	/**
	 * Return true if data is available to read with getAvailableData()
	 * 
	 * @return True if data is available to read with getAvailableData();
	 */
	public boolean isDataAvailable() {
		return !outputQueue.isEmpty();
	}
	
	/**
	 * Has this side of the connection seen a valid FIN segment?
	 * @return True if this side of the connection is closed.
	 */
	public boolean isClosed() {
		return closed;
	}
	
	/**
	 * Has this side of the connetion seen a valid RST segment?
	 * @return True if the connection is reset.
	 */
	public boolean isReset() {
		return reset;
	}
	
	/**
	 * Process a TCP segment which has already be validated to have a correct sequence number.
	 * The data in this segment may overlap with data that has already been received.  In this
	 * case, the overlapping data in the new segment is ignored, and the old data is used.
	 * 
	 * If the segment has either the FIN or RST flag set, this side of the connection is closed.
	 * 
	 * If this segment contains data, it is placed in the output queue after processing any overlap
	 * and the current sequence number is updated to record the number of bytes received.
	 * 
	 * @param tcp TCP segement to process.
	 */
	private void processSegment(TCP tcp) {
		if(closed || checkFinAndRst(tcp)) {
			return;
		}
		    
		// XXX URG processing?
	
		final IPacketHeader next = tcp.getNextHeader();
		
		if(next == null) {
			return;
		}
		
		if(!(next instanceof PacketPayload)) {
			throw new IllegalStateException("Unexpected packet type encapsulated in TCP: " + next);
		}
		
		final ByteBuffer payload = ((PacketPayload)next).toByteBuffer();
		payload.rewind();
		
		/* In case of overlap, move the buffer 'position' pointer to where the overlap ends */
		if(tcp.sequence().less(nextSequence)) {
			int offset = tcp.sequence().distanceTo(nextSequence);
			if(payload.remaining() <= offset) {
				return;
			}
			payload.position(offset);
		}
		
		if(!outputQueue.offer(payload.slice())) {
			throw new IllegalStateException("TCP output queue overflow");
		}
		
		nextSequence = nextSequence.add(payload.remaining());
		assembledByteCount += payload.remaining();
	}
	
	private boolean checkFinAndRst(TCP tcp) {
		if(tcp.getRST()) {
			reset = true;
			doClose();
			return true;
		} else if(tcp.getFIN()) {
			doClose();
			return true;
		} else {
			return false;
		}
		
		
	}
	
	private void doClose() {
		closed = true;
		lastReassemblyTimestamp = 0;
		reassemblyTree.clear();
	}
	
	/**
	 * Examine the reassembly tree to see if it contains any TCP segments which
	 * are ready to deliver.
	 */
	private void reassemble() {
		
		while(!reassemblyTree.isEmpty()) {
			
			if(outputQueue.remainingCapacity() == 0) {
				return;
			}
			
			TCP t = reassemblyTree.first();
			
			final TCPSequenceNumber seq = t.sequence();

			// See comments in addSegment() for explanation + ascii art for each of these cases
			
			// Case A: Contiguous?
			if(nextSequence.equals(seq)) {
				reassemblyTree.remove(t);
				processSegment(t);
				continue;
			}
			
			// Case B: Overlap?
			if(nextSequence.greater(seq) && nextSequence.less(t.nextSequence())) {
				reassemblyTree.remove(t);
				processSegment(t);
				continue;
			}
			
			// Case C: hole?
			if(nextSequence.less(seq)) {
				return;
			}
				
			// Case D: otherwise retransmission
			reassemblyTree.remove(t);
			
		}
		
		// reassemblyTree is empty
		lastReassemblyTimestamp = 0;
	}
	
}