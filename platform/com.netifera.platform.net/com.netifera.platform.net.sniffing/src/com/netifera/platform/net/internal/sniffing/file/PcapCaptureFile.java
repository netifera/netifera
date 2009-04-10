package com.netifera.platform.net.internal.sniffing.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import com.netifera.platform.net.pcap.Datalink;

public class PcapCaptureFile implements IPcapCaptureFile {
	/** Maximum possible packet size */
	private final static int MAX_PACKET_SIZE = (64 * 1024); // MaxSizeOf(IPv4)
	private final static boolean debug = false;
	
	private final File captureFile;
	private final String captureFilePath;
	private final long captureFileLength;
	
	
	private boolean opened;
	
	private FileChannel inputChannel;
	private boolean bigEndian;
	private ByteBuffer headerBuffer;
	private Datalink linkType;

	private ByteBuffer packetBuffer;
	
	private int packetCount;
	private boolean foundTruncated;

	
	class CaptureRecord implements ICaptureFileRecord {
		
		private final long seconds;
		private final int useconds;
		private final int captureLength;
		private final int originalLength;
		private final ByteBuffer recordBytes;
		
		CaptureRecord(long seconds, int useconds, int captureLen, int originalLength, ByteBuffer data) {
			this.seconds = seconds;
			this.useconds = useconds;
			this.captureLength = captureLen;
			this.originalLength = originalLength;
			this.recordBytes = data;
		}
		
		public int getCaptureLength() {
			return captureLength;
		}

		public int getOriginalLength() {
			return originalLength;
		}

		public ByteBuffer getRecordBytes() {
			return recordBytes;
		}

		public int getCaplen() {
			return captureLength;
		}

		public int getDatalen() {
			return originalLength;
		}

		public int getMicroseconds() {
			return useconds;
		}

		public long getSeconds() {
			return seconds;
		}
	}
	
	/**
	 * Create a new <code>PcapCaptureFile</code> for the specified file.
	 * @param path  Path to the capture file.
	 */
	public PcapCaptureFile(String path) {
		captureFile = new File(path);
		captureFilePath = lookupPath();
		captureFileLength = captureFile.length();
	}
	
	private String lookupPath() {
		try {
			return captureFile.getCanonicalPath();
		} catch (IOException e) {
			return "PATH LOOKUP FAILED: " + e.getMessage();
		}
	}
	
	public String getPath() {
		return captureFilePath;
	}

	
	public int getProgress() {
		if(!opened) {
			return 0;
		}
		
		try {
			return (int) ((inputChannel.position() * 100) / captureFileLength);
		} catch (IOException e) {
			return 0;
		}
	}
	
	public int getPacketCount() {
		return packetCount;
	}
	/*
	 * (non-Javadoc)
	 * @see com.netifera.platform.net.internal.sniffing.file.IPcapCaptureFile#open()
	 */
	public void open() throws CaptureFileException {
		
		if(opened) {
			throw new CaptureFileException("Cannot 'reopen' a capture file object");
		}
		
		try {
			inputChannel = new FileInputStream(captureFile).getChannel();

			readCaptureHeader();
			
			if(debug) {
				System.out.println("Tcpdump capture file version 2.4");
				System.out.println("snaplen: " + packetBuffer.capacity());
				System.out.println("linktype: " + linkType);
			}
			 
		} catch (FileNotFoundException e) {
			throw new CaptureFileException("Unable to open '" + captureFile.getName() + "' for reading.", e);
		} catch (IOException e) {
			try {
				if(inputChannel != null) {
					inputChannel.close();
				} }
			catch (IOException e1) {}
			throw new CaptureFileException("I/O error reading capture file header", e);
		}
		
		
		opened = true;
		packetCount = 0;
	}

	/**
	 * Read the main header from the capture file sets up the correct state for reading the rest of
	 * the file.  A packet decoder is selected depending on the link layer type and a packet buffer
	 * is allocated depending on the size of the snaplen.
	 * 
	 * @throws IOException An I/O error has occurred while reading the header data.
	 * @throws CaptureFileException The header could not be read and parsed.
	 */
	private void readCaptureHeader() throws IOException, CaptureFileException {
		ByteBuffer magicBuffer = ByteBuffer.allocate(8);

		while(magicBuffer.hasRemaining()) {
			if(inputChannel.read(magicBuffer) == -1) {
				inputChannel.close();
				throw new CaptureFileException("EOF reading magic value from capture file header");
			}
		}
		magicBuffer.flip();
	
		
		if(!validMagic(magicBuffer)) {
			inputChannel.close();
			throw new CaptureFileException("File is not a valid libpcap capture file");
		}
		
		headerBuffer = ByteBuffer.allocate(16);
		if(!bigEndian)
			headerBuffer.order(ByteOrder.LITTLE_ENDIAN);
		
		if(!readHeaderBuffer()) {
			throw new CaptureFileException("Unexpected EOF reading capture file header.");
		}
					
		headerBuffer.getInt(); /* discard thiszone field */
		headerBuffer.getInt(); /* discard sigfigs field */
		
		final int snaplen = headerBuffer.getInt();
		final int dlt = headerBuffer.getInt();
		
		linkType = dltLookup(dlt);
		
		allocatePacketBuffer(snaplen);
	}
	
	public int getSnaplen() {
		if(packetBuffer != null) {
			return packetBuffer.capacity();
		} else {
			return 0;
		}
	}
	
	public Datalink getLinkType() {
		return linkType;
	}

	/**
	 * Allocate a buffer for receiving raw packet data.
	 * @param size The requested size for the buffer.
	 * @throws CaptureFileException Thrown if an illegal size is requested.
	 */
	private void allocatePacketBuffer(int size) throws CaptureFileException {
		if(size < 0 || size > MAX_PACKET_SIZE) {
			throw new CaptureFileException("Packet buffer size " + size + " is too large");
		}
		packetBuffer = ByteBuffer.allocate(size);
	}
	
	/**
	 * Fill the header buffer with a 16 byte record header.
	 * @return False on EOF.  True otherwise.
	 * @throws CaptureFileException Thrown if reading the header causes an I/O error.
	 */
	private boolean readHeaderBuffer() throws CaptureFileException {
		headerBuffer.clear();
		try {
			while(headerBuffer.hasRemaining()) {
				if(inputChannel.read(headerBuffer) <= 0) {
					return false;
				}
			}
		} catch (IOException e) {
			try { inputChannel.close(); } catch (IOException e1) { }
			throw new CaptureFileException("I/O error reading capture file header", e);
		}
		headerBuffer.flip();
		return true;
	}

	private Datalink dltLookup(int n) {
		for(Datalink dlt : Datalink.values()) {
			if(dlt.getConstant() == n) {
				return dlt;
			}
		}
		return Datalink.DLT_INVALID;
	}

	/**
	 * Read and return a single capture file record from the packet capture file.
	 * 
	 * @return The next capture file record or <code>null</code> if EOF is reached.
	 * @throws CaptureFileException Thrown for any error encountered while proccesing the capture file.
	 */
	public ICaptureFileRecord readRecord() throws CaptureFileException {
		
	
		if(!readHeaderBuffer()) {
			return null;
		}
		
		final int timestampSeconds = headerBuffer.getInt();
		final int timestampMicroseconds = headerBuffer.getInt();
		final int captureLength = headerBuffer.getInt();
		final int originalLength = headerBuffer.getInt();
		
		if(captureLength > MAX_PACKET_SIZE) {
			throw new CaptureFileException("Capture length of " + captureLength +
					" decoded from record header exceeds the maximum allowed packet size of " + MAX_PACKET_SIZE);
		}
		
		if(captureLength > packetBuffer.capacity()) {
			if(debug) {
				System.out.println("Warning: Record length exceeds snaplen.  Extending read buffer...");
			}
			
			allocatePacketBuffer(captureLength);
		}
		
		if(originalLength > captureLength) {
			if(!foundTruncated) {
				if(debug) {
					System.out.println("Capture file contains truncated packets.");
				}
				foundTruncated = true;
			}
		}
		
		packetBuffer.clear();
		packetBuffer.limit(captureLength);
		while(packetBuffer.hasRemaining()) {
			try {
				if(inputChannel.read(packetBuffer) == -1) {
					inputChannel.close();
					throw new CaptureFileException("Premature EOF reading capture file");
				}
			} catch (IOException e) {
				try {
					inputChannel.close();
				} catch (IOException e1) {}
				throw new CaptureFileException("I/O error reading capture file", e);
			}
		}

		packetCount++;
		packetBuffer.flip();
		return new CaptureRecord(
				timestampSeconds,
				timestampMicroseconds,
				captureLength,
				originalLength,
				packetBuffer.asReadOnlyBuffer());
	}
	



	/**
	 * Process the first 8 bytes of the capture file to determine if the 'magic' and 'version' fields
	 * have legal values.  The endianess  of the capture file is inferred from the byte order of the
	 * magic and version fields.  The member <code>bigEndian</code> is set appropriately depending on
	 * the observed byte ordering.
	 * 
	 * Only version value 2.4 is recognized as legal.
	 * 
	 * @param magicBuffer This buffer is the first 8 bytes from the capture file header and contains both the magic and version fields.
	 * @return Return true if the header is valid, false otherwise.
	 */
	private boolean validMagic(ByteBuffer magicBuffer) {
		
		int magic = magicBuffer.getInt();
		int version = magicBuffer.getInt();
	
		
		if(debug) {
			System.out.println("magic is " + Integer.toHexString(magic));
			System.out.println("version is " + Integer.toHexString(version));
		}
		
		if( (magic == 0xa1b2c3d4) && (version == 0x00020004) ) {
			bigEndian = true;
			return true;
		} else if( (magic == 0xd4c3b2a1) && (version == 0x02000400) ) {
			bigEndian = false;
			return true;
		} else {
			return false;
		}
	}



}
