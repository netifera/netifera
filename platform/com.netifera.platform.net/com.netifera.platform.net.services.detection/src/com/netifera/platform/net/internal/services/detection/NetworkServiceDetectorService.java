package com.netifera.platform.net.internal.services.detection;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.netifera.platform.net.services.detection.INetworkServiceDetector;
import com.netifera.platform.net.services.detection.INetworkServiceDetectorService;
import com.netifera.platform.util.patternmatching.IPattern;
import com.netifera.platform.util.patternmatching.Regex;

abstract class NetworkServiceDetectorService implements INetworkServiceDetectorService {
	protected final Map<String, ArrayList<INetworkServiceDetector>> detectors = new HashMap<String, ArrayList<INetworkServiceDetector>>();
	
	protected static List<IPattern> genericOSDetectors;
	protected static List<IPattern> genericArchDetectors;
	
	static {
		// operating systems
		genericOSDetectors = new ArrayList<IPattern>();

		Regex regex = Regex.caseInsensitive(".*((sunos|solaris|linux|macosx|osx|darwin|(net|free|open)bsd) [\\w.]+).*");
		regex.add(1, "os");
		genericOSDetectors.add(regex);

		regex = Regex.caseInsensitive(".*(sunos|solaris|macosx|darwin|(net|free|open)bsd).*");
		regex.add(1, "os");
		genericOSDetectors.add(regex);

		regex = Regex.caseInsensitive(".*(red hat|redhat|centos|fedora|debian|ubuntu|gentoo|mandriva|knopixx|slackware|suse).*");
		regex.add("os", "Linux");
		regex.add(1, "distribution");
		genericOSDetectors.add(regex);

		regex = Regex.caseInsensitive(".*((windows.nt|windows.200.|windows |win(32|2k|nt|200.)|cygwin) (version )?[\\w.]+).*");
		regex.add(1, "os");
		regex.add("arch", "i386"); // FIXME some NT run alpha
		genericOSDetectors.add(regex);

		regex = Regex.caseInsensitive(".*(windows.nt|windows.200.|windows |for windows|win(32|2k|nt|200.)|cygwin).*");
		regex.add(1, "os");
		regex.add("arch", "i386"); // FIXME some NT run alpha
		genericOSDetectors.add(regex);

		// architectures:
		genericArchDetectors = new ArrayList<IPattern>();
		
		//regex = Regex.caseInsensitive(".*x86_64.*"); // + (amd64|em64t)
		//regex.add("arch", "x86_64");
		//genericArchDetectors.add(regex);
		
		regex = Regex.caseInsensitive(".*(i386|x86|i86(pc|xen|xpv)).*"); // FIXME mingw32?
		regex.add("arch", "i386"); // FIXME x86_64
		genericArchDetectors.add(regex);

		regex = Regex.caseInsensitive(".*(amd64|em64t).*");
		regex.add("arch", "AMD64"); 
		genericArchDetectors.add(regex);
		
		regex = Regex.caseInsensitive(".*(powerpc|ppc(32|64)).*");
		regex.add("arch", "PowerPC");
		genericArchDetectors.add(regex);

		regex = Regex.caseInsensitive(".*(sparc|sun4[muv]).*");
		regex.add("arch", "SPARC");
		genericArchDetectors.add(regex);

		regex = Regex.caseInsensitive(".*(pa.?risc|hppa).*");
		regex.add("arch", "HPPA");
		genericArchDetectors.add(regex);
		
		// TODO very common arm/mips on embedded devices
	}
	
	protected void addDetector(INetworkServiceDetector detector) {
		ArrayList<INetworkServiceDetector> list;
		if (detectors.containsKey(detector.getProtocol())) {
			list = detectors.get(detector.getProtocol());
		} else {
			list = new ArrayList<INetworkServiceDetector>();
			detectors.put(detector.getProtocol(), list);
		}
		list.add(detector);
	}

	public Map<String,String> detect(String protocol, int port,
			ByteBuffer triggerBuffer, ByteBuffer responseBuffer) {
		String triggerString = "";
		if (triggerBuffer != null)
			triggerString = stringFromByteBuffer(triggerBuffer);
		String responseString = stringFromByteBuffer(responseBuffer);
		return detect(protocol, port, triggerString, responseString);
	}

	private String stringFromByteBuffer(ByteBuffer buffer) {
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		try {
			return new String(bytes, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException("Unsupported encoding: ISO-8859-1");
		}
	}

	protected boolean isPrintable(String s) {
		return !s.matches("[\\x00-\\x09\\x0b\\x0c\\x0e-\\x1f]");
	}
}
