package com.netifera.platform.net.tools.portscanning;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.netifera.platform.api.tools.ToolException;
import com.netifera.platform.net.internal.tools.portscanning.Activator;
import com.netifera.platform.net.sockets.CompletionHandler;
import com.netifera.platform.net.sockets.TCPChannel;
import com.netifera.platform.util.PortSet;
import com.netifera.platform.util.addresses.inet.InternetAddress;
import com.netifera.platform.util.locators.TCPSocketLocator;

public class TCPConnectScanner extends AbstractPortscanner {	
	private int errorCount = 0;
	private int errorThreshold = 30;
	private BitSet badHostSet;
	private AtomicInteger outstandingConnects;
	private Set<ActiveServiceDetector> detectors = Collections.synchronizedSet(new HashSet<ActiveServiceDetector>());
	private int firstPort;

	class ActiveServiceDetector {
		final int index;
		final TCPSocketLocator locator;
		TCPChannel channel;
		final ByteBuffer readBuffer = ByteBuffer.allocate(1024 * 8);
		final ByteBuffer writeBuffer = ByteBuffer.allocate(1024 * 4);
		Map<String,String> serviceInfo = null;
		volatile Future<?> future;

		ActiveServiceDetector(TCPSocketLocator locator, int index) {
			this.locator = locator;
			this.index = index;
		}
		
		synchronized void connect() throws IOException, InterruptedException {
			detectors.add(this);
			try {
				channel = Activator.getInstance().getSocketEngine().openTCP();
				outstandingConnects.incrementAndGet();
				future = channel.connect(locator, 5000, TimeUnit.MILLISECONDS, null, new CompletionHandler<Void,Void>() {
					public void cancelled(Void attachment) {
						done();
					}
					public void completed(Void result, Void attachment) {
//						context.debug("Connected to "+locator);

						PortSet ports = new PortSet();
						ports.addPort(locator.getPort());
						Activator.getInstance().getNetworkEntityFactory().addOpenTCPPorts(realm, context.getSpaceId(), locator.getAddress(), ports);

						readBanner();
					}
					public void failed(final Throwable e, Void attachment) {
						if ((e instanceof ConnectException)
								|| (e instanceof SocketTimeoutException)) {
							/*
							 * ConnectException = closed or rejected
							 * SocketTimeoutException = filtered or no such host
							 */
						} else if (e instanceof NoRouteToHostException || e instanceof SocketException) {
							markTargetBad(index, new Runnable() {
								public void run() {
									context.worked(targetPorts.itemCount()-1); //XXX is this number ok? might not be the first port we scan
//									context.debug("Skipping unreachable host "+locator.getAddress()+", "+e.getMessage());
								}
							});
						} else {
							context.error("Unexpected exception "+e);
						}
						done();
					}
				});
			} catch (IOException e) {
				done();
				throw e;
			} catch (InterruptedException e) {
				done();
				throw e;
			}
		}
		
		synchronized void readBanner() {
			// if we wont send a trigger, wait longer for a banner
			byte[] trigger = Activator.getInstance().getServerDetector().getTrigger("tcp",locator.getPort());
			int timeout = trigger.length > 0 ? 100 : 4000;
			future = channel.read(readBuffer, timeout, TimeUnit.MILLISECONDS, null, new CompletionHandler<Integer,Void>() {
				public void cancelled(Void attachment) {
					checkUnrecognized();
					done();
				}
				public void completed(Integer result, Void attachment) {
					if (result > 0) {
						ByteBuffer tempBuffer = readBuffer.duplicate();
						tempBuffer.flip();
						serviceInfo = Activator.getInstance().getServerDetector().detect("tcp", locator.getPort(), null, tempBuffer);
						if (serviceInfo != null) {
							Activator.getInstance().getNetworkEntityFactory().createService(realm, context.getSpaceId(), locator, serviceInfo.get("serviceType"), serviceInfo);
							done();
							return;
						}
					} else {
						if (result == -1) {
//							context.debug(locator + " disconnected before trigger");
							done();
							return;
						}

//						context.debug(locator + " zero read (no banner)");
					}

					writeTrigger();
				}
				public void failed(Throwable e, Void attachment) {
					if (e instanceof SocketTimeoutException) {
//						context.debug(locator + " timeout");
						writeTrigger();
					} else {
						if (! (e instanceof ClosedChannelException)) {
							context.exception("Unexpected error reading banner " + locator, e);
						}
						done();
					}
				}
			});
		}
		
		synchronized void writeTrigger() {
			byte[] trigger = Activator.getInstance().getServerDetector().getTrigger("tcp",locator.getPort());
			writeBuffer.clear();
			writeBuffer.put(trigger);
			writeBuffer.flip();

			future = channel.write(writeBuffer, 5000, TimeUnit.MILLISECONDS, null, new CompletionHandler<Integer,Void>() {
				public void cancelled(Void attachment) {
					done();
				}
				public void completed(Integer result, Void attachment) {
					readResponse();
				}
				public void failed(Throwable e, Void attachment) {
					if (! (e instanceof ClosedChannelException)) {
						context.exception("Unexpected error writting trigger " + locator, e);
					}
					checkUnrecognized();
					done();
				}
			});
		}
		
		synchronized void readResponse() {
			future = channel.read(readBuffer, 4000, TimeUnit.MILLISECONDS, null, new CompletionHandler<Integer,Void>() {
				public void cancelled(Void attachment) {
					checkUnrecognized();
					done();
				}
				public void completed(final Integer result, Void attachment) {
					if (result > 0) {
						// read once more, timeout very short
						// sometimes there's some more data right after, comming in another packet
						future = channel.read(readBuffer, 100, TimeUnit.MILLISECONDS, null, new CompletionHandler<Integer,Void>() {
							public void cancelled(Void attachment) {
								detect();
							}
							public void completed(Integer result,
									Void attachment) {
								detect();
							}
							public void failed(Throwable exc, Void attachment) {
								detect();
							}
							private void detect() {
								readBuffer.flip();
								writeBuffer.rewind();
								serviceInfo = Activator.getInstance().getServerDetector().detect("tcp", locator.getPort(), writeBuffer, readBuffer);
								if (serviceInfo != null) {
									Activator.getInstance().getNetworkEntityFactory().createService(realm, context.getSpaceId(), locator, serviceInfo.get("serviceType"), serviceInfo);
								}
								checkUnrecognized();
								done();
							}
						});
					} else {
						if (result == -1) {
							context.debug(locator + " disconnected");
						} else {
							context.debug(locator + " 0 read after trigger");
						}
						checkUnrecognized();
						done();
					}
				}
				public void failed(Throwable e, Void attachment) {
					if (e instanceof SocketTimeoutException) {
						context.debug(locator + " trigger timeout");
					} else if (! (e instanceof ClosedChannelException)) {
						context.exception("Unexpected error when reading response: " + locator, e);
					}
					checkUnrecognized();
					done();
				}
			});
		}
		
		private synchronized void done() {
			try {
				channel.close();
			} catch (IOException e) {
			}
			
			outstandingConnects.decrementAndGet();
			detectors.remove(this);
			context.worked(1);
			future = null;
			
			if (serviceInfo != null)
				context.info(serviceInfo.get("serviceType")+" @ "+locator);
		}
		
		private void checkUnrecognized() {
			// if the service was not recognized
			if (serviceInfo == null)
				context.warning("Unknown service @ " + locator);
		}
		
		synchronized void cancel(boolean mayInterruptIfRunning) {
			Future<?> future = this.future;
			if (future != null)
				future.cancel(mayInterruptIfRunning);
			context.debug("Cancelled connection to "+locator);
		}
	}
	
	@Override
	protected void scannerRun() {
		outstandingConnects = new AtomicInteger(0);

		int hostCount = targetNetwork.itemCount();
		badHostSet = new BitSet(hostCount);
		
		firstPort = targetPorts.contains(80) ? 80 : targetPorts.itemAt(0);
		context.setTitle("TCP connect scan "+targetNetwork);
		context.setStatus("Scanning port "+firstPort);
		context.setTotalWork(targetNetwork.itemCount()*targetPorts.itemCount());
		
		try {
			context.info("Scanning port "+firstPort);
			if (scanFirstPort(firstPort) == false)
				return;

			if (targetPorts.itemCount() > 1) {
				context.info("Scanning the rest of the ports");
				for (int i = 0; i < targetNetwork.itemCount(); i++)
					if (scanHost(i) == false)
						return;
			}
		} catch (InterruptedException e) {
			context.setStatus("Cancelling "+outstandingConnects.get()+" connections");
			synchronized(detectors) {
				for (ActiveServiceDetector detector: detectors.toArray(new ActiveServiceDetector[detectors.size()]))
					detector.cancel(false);
			}
			context.warning("Interrupted");
//			Thread.currentThread().interrupt();
//			return;
		} catch (Exception e) {
			context.exception("Exception", e);
		}
		while (outstandingConnects.get() != 0) {
			context.debug("Outstanding connects "+outstandingConnects.get());
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				context.warning("Interrupted");
				synchronized(detectors) {
					for (ActiveServiceDetector detector: detectors.toArray(new ActiveServiceDetector[detectors.size()]))
						detector.cancel(false);
				}
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	private boolean scanFirstPort(final int port) throws InterruptedException {
		for (int i = 0; i < targetNetwork.itemCount(); i++) {
			final TCPSocketLocator locator = new TCPSocketLocator(targetNetwork.itemAt(i),port);
			try {
				ActiveServiceDetector detector = new ActiveServiceDetector(locator, i);
				detector.connect();
			} catch (PortUnreachableException e) {
				continue;
			} catch (SocketException e) {
				markTargetBad(i, new Runnable() {
					public void run() {
						context.worked(targetPorts.itemCount()-1); // remaining ports
//						context.debug("Skipping unreachable host " + locator.getAddress());
					}
				});
			} catch (IOException e) {
				context.debug("Connecting to " + locator + " failed with error "
						+ e);
				errorCount++;
				if (errorCount >= errorThreshold) {
					context.error("Too many errors, aborting.");
					return false;
				}
			}
		}
		return true;
	}

	private boolean scanHost(int index) throws InterruptedException {
		final InternetAddress target = targetNetwork.itemAt(index);
		context.setStatus("Scanning host "+target);
		
		for (int i = 0; i < targetPorts.itemCount(); i++) {
			if (isTargetBad(index)) {
				return true;
			}

			int port = targetPorts.itemAt(i);
			if (port == firstPort)
				continue; // already scanned before
			
			final TCPSocketLocator locator = new TCPSocketLocator(target, port);

			try {
				ActiveServiceDetector detector = new ActiveServiceDetector(locator, index);
				detector.connect();
			} catch (PortUnreachableException e) {
				continue;
			} catch (final SocketException e) {
				final int remainingPorts = targetPorts.itemCount()-i-1;
				markTargetBad(index, new Runnable() {
					public void run() {
						context.worked(remainingPorts);
//						context.debug("Skipping unreachable host "+target+", "+e.getMessage());
					}
				});
				return true;
			} catch (IOException e) {
				context.debug("Connecting to " + locator + " failed, "+e.getMessage());
				errorCount++;
				if (errorCount >= errorThreshold) {
					context.error("Too many errors, aborting.");
					return false;
				}
			}
		}
		return true;
	}

	private void markTargetBad(int index, Runnable runnable) {
		synchronized (badHostSet) {
			if (!badHostSet.get(index)) {
				badHostSet.set(index);
				runnable.run();
			}
		}
	}

	private boolean isTargetBad(int index) {
		synchronized (badHostSet) {
			return badHostSet.get(index);
		}
	}
	
	protected void setupToolOptions() throws ToolException {
		context.setTitle("TCP Connect Scan");
		super.setupToolOptions();
	}
}
