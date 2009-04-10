package com.netifera.platform.net.internal.services.detection.basic;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.netifera.platform.net.services.detection.INetworkServiceDetector;
import com.netifera.platform.net.services.detection.INetworkServiceDetectorProvider;
import com.netifera.platform.net.services.detection.INetworkServiceTrigger;
import com.netifera.platform.util.HexaEncoding;
import com.netifera.platform.util.PortSet;
import com.netifera.platform.util.patternmatching.ISessionPattern;
import com.netifera.platform.util.patternmatching.Regex;
import com.netifera.platform.util.patternmatching.SessionPattern;

// one class, one dream
public class BasicDetectorProvider implements INetworkServiceDetectorProvider {
	
	private INetworkServiceDetector newDetector(final String protocol, final PortSet ports, final ISessionPattern pattern) {
		
		return new INetworkServiceDetector() {
			public Map<String, String> detect(String clientData,
					String serverData) {
				return pattern.match(clientData, serverData);
			}
			public PortSet getPorts() {
				return ports;
			}
			public String getProtocol() {
				return protocol;
			}
			@Override
			public String toString() {
				return ports.toString() + "/" + protocol + "\n" + pattern.toString();
			}
		};
	}

	private INetworkServiceDetector newDetector(String protocol, ISessionPattern detector) {
		return newDetector(protocol, new PortSet("0-65535"), detector);
	}

	private INetworkServiceDetector newDetector(String protocol, PortSet ports, String triggerPattern, String responsePattern, String serviceType, String product, Object versionGroup, Object os) {
		Regex triggerRegex = new Regex(triggerPattern);
		Regex responseRegex = new Regex(responsePattern);
		responseRegex.add("serviceType", serviceType);
		responseRegex.add("product", product);
		if (os instanceof String) {
			responseRegex.add("os", (String)os);
			if (((String) os).matches(".*indow.*"))
					responseRegex.add("arch", "i386"); // FIXME x86_64
		} else if (os instanceof Integer) {
			responseRegex.add((Integer)os, "os");
		}
		if (versionGroup != null) {
			if (versionGroup instanceof String) {
				responseRegex.add("version", (String)versionGroup);
			} else if (versionGroup instanceof Integer) {
				responseRegex.add((Integer)versionGroup, "version");
			}
		}
		return newDetector(protocol,ports,new SessionPattern(triggerRegex, responseRegex));
	}

	private INetworkServiceDetector newDetector(String protocol, PortSet ports, Pattern triggerPattern, Pattern responsePattern, String serviceType, String product, Object versionGroup, Object os) {
		Regex triggerRegex = new Regex(triggerPattern);
		Regex responseRegex = new Regex(responsePattern);
		responseRegex.add("serviceType", serviceType);
		responseRegex.add("product", product);
		if (os instanceof String) {
			responseRegex.add("os", (String)os);
			if (((String) os).matches(".*indow.*"))
				responseRegex.add("arch", "i386");
		} else if (os instanceof Integer) {
			responseRegex.add((Integer)os, "os");
		}
		if (versionGroup != null) {
			if (versionGroup instanceof String) {
				responseRegex.add("version", (String)versionGroup);
			} else if (versionGroup instanceof Integer) {
				responseRegex.add((Integer)versionGroup, "version");
			}
		}
		return newDetector(protocol,ports,new SessionPattern(triggerRegex, responseRegex));
	}

	private INetworkServiceDetector newDetector(String protocol, String triggerPattern, String responsePattern, String serviceType, String product, Object versionGroup, Object os) {
		return newDetector(protocol, new PortSet("0-65535"), triggerPattern, responsePattern, serviceType, product, versionGroup, os);
	}

	private INetworkServiceDetector newDetector(String protocol, Pattern triggerPattern, Pattern responsePattern, String serviceType, String product, Integer versionGroup, Object os) {
		return newDetector(protocol, new PortSet("0-65535"), triggerPattern, responsePattern, serviceType, product, versionGroup, os);
	}

	private INetworkServiceTrigger newTrigger(final String protocol, final PortSet ports, final byte[] bytes) {
		return new INetworkServiceTrigger() {
			public byte[] getBytes() {
				return bytes;
			}
			public PortSet getPorts() {
				return ports;
			}
			public String getProtocol() {
				return protocol;
			}
			@Override
			public String toString() {
				return ports.toString() + "/" + protocol + " " + HexaEncoding.bytes2hex(bytes);
			}
		};
	}

	public List<INetworkServiceDetector> getClientDetectors() {
		List<INetworkServiceDetector> answer = new ArrayList<INetworkServiceDetector>();

		// HTTP proxy
		Regex regex = Regex.caseInsensitive("^GET http[s]?://.* HTTP/1\\.[01].*User-Agent: ([^\\r\\n]+)[\\r\\n].*");
		regex.add("serviceType", "HTTP Proxy");
		regex.add(1, "product");
		answer.add(newDetector("tcp", new PortSet("80-89,8000-8999,1080-1089,3128"), new SessionPattern(regex, null)));

		// HTTP
		regex = Regex.caseInsensitive("^GET .* HTTP/1\\.[01].*User-Agent: ([^\\r\\n]+)[\\r\\n].*");
		regex.add("serviceType", "HTTP");
		regex.add(1, "product");
		answer.add(newDetector("tcp", new PortSet("80"), new SessionPattern(regex, null)));

		// FTP
		regex = Regex.caseInsensitive("^USER .*");
		regex.add("serviceType", "FTP");
		answer.add(newDetector("tcp", new PortSet("21"), new SessionPattern(regex, null)));

		// SSH
		regex = Regex.caseInsensitive("^SSH-.*OpenSSH_([\\w.]+)\\s+([^\\x00\\r\\n]*).*");
		regex.add("serviceType", "SSH");
		regex.add("product", "OpenSSH");
		regex.add(1, "version");
		regex.add(2, "os");
		answer.add(newDetector("tcp", new SessionPattern(regex, null)));

		regex = Regex.caseInsensitive("^SSH-\\d\\.\\d-([^\\r\\n\\x00]*)");
		regex.add("serviceType", "SSH");
		regex.add(1, "product");
		answer.add(newDetector("tcp", new SessionPattern(regex, null)));

		// POP3
		regex = Regex.caseInsensitive("^USER .*");
		regex.add("serviceType", "POP3");
		answer.add(newDetector("tcp", new PortSet("110"), new SessionPattern(regex, null)));

		// MSN
		regex = new Regex("^VER \\d+ MSN.*CVR \\d+ [^ ]* (.*) (MSN[^ ]*) ([\\d.]*) .*USR \\d+ [^ ]* . ([^\\r\\n]+)[\\r\\n].*");
		regex.add("serviceType", "MSN");
		regex.add(1, "os");
		regex.add(2, "product");
		regex.add(3, "version");
		regex.add(4, "username");
		answer.add(newDetector("tcp", new PortSet("1863"), new SessionPattern(regex, null)));

		// Jabber (Google Talk)
		regex = new Regex("^<\\?xml version='1\\.0' \\?><stream:stream to\\='gmail.com' .*");
		regex.add("serviceType", "Jabber");
		regex.add("product", "Google Talk");
		answer.add(newDetector("tcp", new PortSet("5222"), new SessionPattern(regex, null)));

		// IRC
		regex = new Regex("^^NICK ([^\\r\\n]+)[\\r\\n].*");
		regex.add("serviceType", "IRC");
		regex.add(1, "username");
		answer.add(newDetector("tcp", new SessionPattern(regex, null)));

		// Telnet
		regex = Regex.caseInsensitive("^(\\xff[\\xfb\\xfc\\xfd].)+.*"); // IAC + (WILL|WONT|DO) // TODO 'SB' option?
		regex.add("serviceType", "Telnet");
		answer.add(newDetector("tcp", new SessionPattern(regex, null)));
		
		// Sun RPC Call
		// FIXME these detectors are too generic, they match netbios
/*		answer.add(newDetector("udp", ".*",
				"^.{4}"						// XID
				+ "\\x00\\x00\\x00\\x00"	// Call
				+ "\\x00\\x00\\x00."		// version
				+ ".{4}"					// program
				+ "\\x00\\x00\\x00."		// program version
				+ "\\x00\\x00...*",			// procedure
				"RPC", null, null, null));
		answer.add(newDetector("tcp", ".*",
				"^(.{4})?"					// fragment?
				+ ".{4}"					// XID
				+ "\\x00\\x00\\x00\\x00"	// Call
				+ "\\x00\\x00\\x00."		// version
				+ ".{4}"					// program
				+ "\\x00\\x00\\x00."		// program version
				+ "\\x00\\x00...*",			// procedure
				"RPC", null, null, null));
*/		
		// SSL
		answer.add(newDetector("tcp", ".*", "^\\x16" + // Handshake
				"\\x03\\x01" + // TLS 1.0
				".." + // len
				// Handshake
				"\\x01" + // Client Hello
				"..." + // len
				"\\x03\\x01" + // TLS 1.0
				".*", "SSL", null, "TLSv1", null));
		answer.add(newDetector("tcp", ".*", "^\\x16" + // Handshake
				"\\x03\\x00" + // SSL 3.0
				".." + // len
				// Handshake
				"\\x01" + // Client Hello
				"..." + // len
				"\\x03\\x00" + // SSL 3.0
				".*", "SSL", null, "SSLv3", null));

		// DNS
		// FIXME
/*		answer.add(newDetector("tcp", ".*",
				"^.." + // len
				".." + // id
				"\\x00." + // flags (Query) // FIXME auth?
				".." + // q
				"\\x00\\x00...." + //RRs
				"(\\x03\\w+)+\\x00" + // name
				"\\x00\\xfc" + // AXFR // XXX
				"\\x00\\x01" + // Internet
				".*", // FIXME
				"DNS", null, null, null));
*/		
		// Real Time Streaming Protocol
		regex = Regex.caseInsensitive("^\\p{Upper}+ rtsp:\\/\\/ RTSP/1\\.0.*User-Agent: ([^\\r\\n]+)[\\r\\n].*");
		regex.add("serviceType", "RTSP");
		regex.add(1, "product");
		answer.add(newDetector("tcp", new SessionPattern(regex, null)));

		/*
		// Radius
		answer.add(newDetector("udp", new PortSet("1812"), ".*",
				"^\\x01" +			// AccessRequest
				"." +				// ID
				"\\x00." +			// len
				".{16}.*",			// authentificator, ...
				"Radius", null, null, null)); // TODO version

		// ISAKMP
		answer.add(newDetector("udp", new PortSet("500"), ".*",
				"^.{16}"			// 2 cookies
				+ ".\\x10.{10}.*",	// payload, version (0x10 = 1.0), type + flags + id + len
				"ISAKMP", null, null, null)); // TODO version
		*/
		
		return answer;
	}

	@SuppressWarnings("boxing")
	public List<INetworkServiceDetector> getServerDetectors() {
		List<INetworkServiceDetector> answer = new ArrayList<INetworkServiceDetector>();
		
		// SSH
		// FIXME james: regex matching major.minor version?
		answer.add(newDetector("tcp", ".*", "^SSH-.*dropbear_([.\\w]+).*", "SSH", "Dropbear", 1, null));
		answer.add(newDetector("tcp", ".*", "^SSH-.*Sun_SSH.*", "SSH", null, null, "SunOS"));
		answer.add(newDetector("tcp", ".*", "^SSH-.*OSU.*", "SSH", null, null, "OpenVMS")); // TODO get proc type? // james
		answer.add(newDetector("tcp", ".*", "^SSH-.*OpenSSH_([.\\w]+).*", "SSH", "OpenSSH", 1, null));
		answer.add(newDetector("tcp", ".*", "^SSH-.*Cisco-(\\d+\\.\\d+).*", "SSH", "Cisco SSH", 1, "Cisco IOS"));
		answer.add(newDetector("tcp", ".*", "^SSH-.*", "SSH", null, null, null));
		
		// FTP
		// see http://en.wikipedia.org/wiki/List_of_FTP_servers
		answer.add(newDetector("tcp", ".*", "^220.*FileZilla Server version (\\d[-.\\w ]+)\\r\\n.*", "FTP", "FileZilla", 1, "Windows"));
		answer.add(newDetector("tcp", ".*", "^220.*Microsoft FTP Service \\(Version (\\d[^)]+).*", "FTP", "Microsoft FTPD", 1, "Windows"));
		answer.add(newDetector("tcp", ".*", "^220[ -]Microsoft FTP Service\\r\\n.*", "FTP", "Microsoft FTPD", null, "Windows"));
		answer.add(newDetector("tcp", ".*", "^220[ -]Serv-U FTP[ -]Server v(\\d\\S+) ... WinSock .*", "FTP", "Serv-U FTPD", 1, "Windows"));
		answer.add(newDetector("tcp", ".*", "^220-Serv-U FTP Server for Winsock\\r\\n.*", "FTP", "Serv-U FTPD", null, "Windows"));
		answer.add(newDetector("tcp", ".*", "^220-SECURE FTP SERVER VERSION ([\\d.]+).*", "FTP", "Serv-U FTPD", null, "Windows"));
		answer.add(newDetector("tcp", ".*", "^220[- ].*FTP server \\(Version (wu-[-.\\w]+).*", "FTP", "WU-FTPD", 1, "Unix"));
		answer.add(newDetector("tcp", ".*", "^220-\\r\\n220 .* FTP server \\(Version ([-.+\\w()]+)\\) ready\\.\\r\\n.*", "FTP", "WU-FTPD", 1, "Unix"));
		answer.add(newDetector("tcp", ".*", "^220 ProFTPD (\\d\\S+) Server.*", "FTP", "ProFTPD", 1, "Unix"));
		answer.add(newDetector("tcp", ".*", "^220 .*ProFTP.*", "FTP", "ProFTPD", null, "Unix"));
		answer.add(newDetector("tcp", ".*", "^220 \\(vsFTPd ([-.\\w]+)\\)\\r\\n.*", "FTP", "vsftpd", 1, "Unix"));
		answer.add(newDetector("tcp", ".*", "^220 .* FTP server \\(GNU inetutils ([\\d.]+)\\) ready\\.\\r\\n.*", "FTP", "GNU inetutils", 1, "Unix"));
		answer.add(newDetector("tcp", ".*", "^220 Cisco \\(([\\d.]+)\\) FTP server ready\\r\\n.*", "FTP", "Cisco FTPD", 1, "Cisco IOS"));
		answer.add(newDetector("tcp", ".*", "^220 .* Server \\(vftpd ([\\d.]+)\\) ready\\.\\r\\n.*", "FTP", "VFTPD", 1, "Windows"));
		answer.add(newDetector("tcp", ".*", "^220 .*\\(Version (\\d.\\d)/OpenBSD.*", "FTP", "OpenBSD FTPD", 1, "OpenBSD")); // XXX or can it run in other OS too?
		answer.add(newDetector("tcp", ".*", "^220---------- Welcome to Pure-FTPd (\\[\\p{Alpha}+\\] )*----------\\r\\n.*", "FTP", "Pure-FTPd", null, null));
		answer.add(newDetector("tcp", ".*", "^220 .* FTP server \\(Compaq Tru64 UNIX Version ([\\d.]+)\\) ready\\.\\r\\n.*", "FTP", "Compaq FTPD", 1, "Tru64")); // FIXME isn't it OS version?
		answer.add(newDetector("tcp", ".*", "^220-[A-Z0-9]*FTP[A-Z0-9]*1 IBM FTP CS(:?\\/\\d+)? (V\\d+R[\\d+\\.]+) at .*, [\\d:]+ on [\\d-]+\\.\\r\\n.*", "FTP", "IBM FTP", 1, "z/OS")); // TODO use R(elease) to detect AS/400, OS/390, z/OS // james
		answer.add(newDetector("tcp", ".*", "^220-[A-Z0-9]*FTP[A-Z0-9]*1 IBM VM Level (\\d+) at .*, [\\d:]+ .*\\r\\n.*", "FTP", "IBM FTP", 1, "z/VM"));
		answer.add(newDetector("tcp", ".*", "^220 HP ARPA FTP Server \\[.{8}\\] \\(C\\) Hewlett-Packard .*\\r\\n.*", "FTP", "HP ARPA FTP", null, "HP-UX")); // TODO OS version? // james
		answer.add(newDetector("tcp", ".*", "^220 MPE\\/iX File Transfer Protocol Server \\[.{8}\\] \\(C\\) Hewlett-Packard .*\\r\\n.*", "FTP", "HP MPE/iX FTP", null, "HP-UX")); // TODO OS version? // james
		answer.add(newDetector("tcp", ".*", "^220 JD FTP Server Ready.?\\r\\n.*", "FTP", "Hewlett-Packard FTP Print Server", null, "HP Jetdirect Printer"));
		answer.add(newDetector("tcp", ".*", "^220 .* FTP server \\((?:Revision [\\d.]+ )?Version wuftpd-([^ ]+).*\\) ready\\.\\r\\n.*", "FTP", "WU-FTPD", 1, null)); // TODO fingerprint OS by build version
		answer.add(newDetector("tcp", ".*", "^220-QTCP at .*", "FTP", "FTP", null, "AS/400")); // TODO get hostname
		
		answer.add(newDetector("tcp",
				Pattern.compile("^USER .*\n.*", Pattern.MULTILINE|Pattern.DOTALL|Pattern.CASE_INSENSITIVE),
				Pattern.compile("^220.*\n(331|530).*", Pattern.MULTILINE|Pattern.DOTALL),
				"FTP", null, null, null));
		answer.add(newDetector("tcp",
				Pattern.compile(".*"),
				Pattern.compile("^220.*FTP.*", Pattern.MULTILINE|Pattern.DOTALL|Pattern.CASE_INSENSITIVE),
				"FTP", null, null, null));

		answer.add(newDetector("tcp", new PortSet("21"), ".*", "^220.*", "FTP", null, null, null));

		// IMAP
		answer.add(newDetector("tcp", ".*", "^\\* OK [-.\\w]+ IMAP4rev1 MDaemon (\\d[-.\\w]+) .*", "IMAP", "Alt-N MDaemon", 1, "Windows"));
		answer.add(newDetector("tcp", ".*", "^\\* OK Domino IMAP4 Server Release (\\d[-.\\w]+) +ready.*", "IMAP", "Lotus Domino", 1, null));
		answer.add(newDetector("tcp", ".*", "^\\* OK Domino IMAP4 Server Build V([\\w_]+ Beta \\w+) ready.*", "IMAP", "Lotus Domino", 1, null));
		answer.add(newDetector("tcp", ".*", "^\\* OK Microsoft Exchange IMAP4rev1 server version ([-.\\w]+) .*", "IMAP", "Microsoft Exchange IMAP4rev1 server", 1, "Windows"));
		answer.add(newDetector("tcp", ".*", "^\\* OK Microsoft Exchange 2000 IMAP4rev1 server version (\\d[-.\\w]+) \\([-.\\w]+\\) ready\\.\\r\\n.*", "IMAP", "Microsoft Exchange 2000 IMAP4rev1 server", 1, "Windows")); // XXX second group is OS? what format?
		answer.add(newDetector("tcp", ".*", "^\\* OK Microsoft Exchange .*", "IMAP", "Microsoft Exchange IMAP server", null, "Windows"));
		answer.add(newDetector("tcp", ".*", "^\\* OK IMAP4rev1 server ready at \\d\\d/\\d\\d/\\d\\d \\d\\d:\\d\\d:\\d\\d \\r\\n.*", "IMAP", "MailEnable Professional", null, "Windows"));
		answer.add(newDetector("tcp", ".*", "^\\* OK.* Courier.*IMAP.*", "IMAP", "Courier", null, null));
		answer.add(newDetector("tcp", ".*", "^\\* OK.*[Dd]ovecot.*", "IMAP", "Dovecot", null, "Unix"));
		answer.add(newDetector("tcp", ".*", "^\\* OK CommuniGate Pro IMAP Server ([\\w.]+) .*ready.*", "IMAP", "CommuniGate Pro", 1, null));
		answer.add(newDetector("tcp", ".*", "^\\* OK.*", "IMAP", null, null, null));

		// TOR
		answer.add(newDetector("tcp", "^GET /tor/(server|status)/.*\\r\\nHost: .*\\r\\n\\r\\n$", "^HTTP/1\\.0 200 OK.*application/octet[_\\-]stream.*", "TOR", null, null, null));
		answer.add(newDetector("tcp", ".*", "^HTTP/1\\.0 503 Directory busy, try again later.*", "TOR", null, null, null));
		answer.add(newDetector("tcp", ".*", "^HTTP/1\\.0 200 OK.*signed-directory.*published.*nrecommended-software.*", "TOR", null, null, null));
		
		// HTTP Proxy
		answer.add(newDetector("tcp", "^CONNECT .*", "^HTTP/1\\.[01](200|503).*", "HTTP Proxy", null, null, null));
		
		Regex regex = Regex.caseInsensitive("^HTTP/1\\.[01].*Proxy-Agent: ([^\\r\\n]*)[\\r\\n].*");
		regex.add("serviceType", "HTTP Proxy");
		regex.add(1, "product");
		answer.add(newDetector("tcp", new SessionPattern(null, regex)));

		// HTTP
		answer.add(newDetector("tcp", ".*Host: ([^\\r\\n]+)[\\r\\n].*", "^HTTP/1\\.0 \\d\\d\\d .*\\r\\nServer: Tomcat Web Server/(\\d[-.\\w ]+) \\( ([^)]+) \\)\\r\\n.*", "HTTP", "Apache Tomcat", 1, 2));
		answer.add(newDetector("tcp", ".*", "^HTTP/1\\.0 \\d\\d\\d .*\\r\\nServer: Tomcat Web Server/(\\d[-.\\w ]+)\\r\\n\\r\\n.*", "HTTP", "Apache Tomcat", 1, null)); // FIXME ' '
		answer.add(newDetector("tcp", ".*", "^HTTP/1\\.0 \\d\\d\\d .*\\r\\nServlet-Engine: Tomcat Web Server/(\\d[-.\\w]+) .*", "HTTP", "Apache Tomcat", 1, null));
		answer.add(newDetector("tcp", ".*", "^HTTP/1\\.[01].*Server: Apache\\/([-.\\w]+) +HP-UX_Apache-based_Web_Server.*\\r\\n.*", "HTTP", "HP-UX Apache", 1, "HP-UX"));
		answer.add(newDetector("tcp", ".*", "^HTTP/1\\.[01].*Server: HP-UX_Apache-based_Web_Server\\/([-.\\w]+) .*\\r\\n.*", "HTTP", "HP-UX Apache", 1, "HP-UX"));
		answer.add(newDetector("tcp", ".*", "^HTTP/1\\.[01].*[Ss]erver: IBM_HTTP_Server.* Apache\\/([^ \\t\\r\\n]+).*\\r\\n.*", "HTTP", "IBM Apache", 1, null)); // FIXME IBM_HTTP_Server version?
		answer.add(newDetector("tcp", ".*", "^HTTP/1\\.[01].*[Ss]erver: IBM_HTTP_Server.*", "HTTP", "IBM Apache", null, null));
		answer.add(newDetector("tcp", ".*", "^HTTP/1\\.[01].*Server: Rapidsite\\/Apa\\/([^ \\t\\r\\n]+).*", "HTTP", "Rapidsite Apache", 1, "HP-UX")); // HP-UX 11i Apache-based Web Server
		answer.add(newDetector("tcp", ".*", "^HTTP/1\\.[01].*Server: MIT Web Server Apache\\/([^ \\t\\r\\n]+).*\\r\\n.*", "HTTP", "MIT Apache", 1, null)); // FIXME IBM_HTTP_Server version?
		answer.add(newDetector("tcp", ".*", "^HTTP/1\\.[01].*Server: Apache\\/([^ \\t\\r\\n]+).*\\r\\n.*", "HTTP", "Apache", 1, null)); // FIXME IBM_HTTP_Server version?
		answer.add(newDetector("tcp", ".*", "^HTTP/1\\.[01].*Server: Apache.*", "HTTP", "Apache", null, null));
		answer.add(newDetector("tcp", ".*", "^HTTP/1\\.[01].*\\r\\nServer: Microsoft-IIS/([-.\\w]+)\\r\\n.*", "HTTP", "Microsoft IIS", 1, "Windows"));
		answer.add(newDetector("tcp", ".*", "^HTTP/1\\.[01].*\\r\\nServer: mini_httpd/(\\d+\\.\\d+).*", "HTTP", "Acme mini_httpd", 1, null));
		answer.add(newDetector("tcp", ".*", "^HTTP/1\\.[01].*level.15.*", "HTTP", "Cisco IOS", null, "Cisco IOS"));
		answer.add(newDetector("tcp", ".*", "^HTTP/1\\.[01].*\\r\\nServer: cisco-IOS.*", "HTTP", "Cisco IOS", null, "Cisco IOS"));
		answer.add(newDetector("tcp", ".*", "^HTTP/1\\.0 \\d+ [\\w ]+\\r\\n.*\\r\\nExpires: Thu, 16 Feb 1989 00:00:00 GMT(\\r\\n){2}.*", "HTTP", "Cisco IOS", null, "Cisco IOS"));
		answer.add(newDetector("tcp", ".*", "^HTTP/1\\.[01].*Server: lighttpd/([-.\\w]+)\\r\\n.*", "HTTP", "lighttpd", 1, null));
		answer.add(newDetector("tcp", ".*", "^HTTP/1\\.[01].*Server: nginx/([-.\\w]+)\\r\\n.*", "HTTP", "nginx", 1, null));
		answer.add(newDetector("tcp", ".*", "^HTTP/1\\.[01].*Server: Mongrel ([-.\\w]+)\\r\\n.*", "HTTP", "Mongrel", 1, null));
		answer.add(newDetector("tcp", ".*", "^HTTP/1\\.[01].*Server: Lotus-Domino\\r\\n.*", "HTTP", "Lotus Domino", null, null));
		answer.add(newDetector("tcp", ".*", "^HTTP/1\\.[01].*Server: Cerberian Service\\r\\n.*", "HTTP", "Cerberian", null, null));
		//answer.add(newDetector("tcp", ".*", "^HTTP/1\\.[01].*Server: ENI-Web/([^ \\t\\r\\n]+).*\\r\\n.*", "HTTP", "ENI", 1, null));
		answer.add(newDetector("tcp", ".*", "^HTTP/1\\.[01].*Server: AV-TECH (.*) Video Web Server\\n.*", "HTTP", "AV-TECH Video", null, 1)); // FIXME OS
		answer.add(newDetector("tcp", ".*", "^HTTP/1\\.[01].*Server: JRun Web Server\\/([\\d.]+)\\r\\n.*", "HTTP", "Adobe JRun", 1, null)); // J2EE application server
		answer.add(newDetector("tcp", ".*", "^HTTP/1\\.[01].*Server: NetPort Software ([\\d.]+)\\r\\n.*", "HTTP", "Intel NetPort", 1, "Windows"));
		answer.add(newDetector("tcp", ".*", "^HTTP/1\\.1 403 Forbidden \\( The server denied the specified Uniform Resource Locator \\(URL\\). Contact the server administrator.  \\)\\r\\n.*", "HTTP", "Microsoft IIS with ISA Server", null, "Windows"));
		answer.add(newDetector("tcp", ".*", "^HTTP/1\\.[01].*\\r\\nServer: HTTPd-WASD\\/(\\d\\.\\d\\.\\d) [\\w\\/]+( \\w+)?\\r\\n.*", "HTTP", "WASD", 1, "OpenVMS")); // TODO proc
		
		regex = Regex.caseInsensitive("^HTTP/1\\.[01].*\\r\\nServer: (HP.Chai\\w+)\\/([\\d.]+)\\r\\n.*"); // FIXME not only Jetdirect?
		regex.add("serviceType", "HTTP");
		regex.add(1, "product");
		regex.add(2, "version");
		regex.add("os", "HP Printer");
		answer.add(newDetector("tcp", new SessionPattern(null, regex)));
		
		answer.add(newDetector("tcp", new SessionPattern(null, regex)));
		//regex = Regex.caseInsensitive("^HTTP/1\\.[01].*\\r?\\n[Ss]erver: ([^\\/ \\t\\r\\n]+)(?i:-?Web)? *(?i:Server)?\\/([^ \\/\\t\\r\\n]+).*\\r?\\n.*");
		regex = Regex.caseInsensitive("^HTTP/1\\.[01].*\\r?\\n[Ss]erver: ([^\\/ \\t\\r\\n]+)\\/([^ \\/\\t\\r\\n]+).*\\r?\\n.*");
		regex.add("serviceType", "HTTP");
		regex.add(1, "product");
		regex.add(2, "version");
		answer.add(newDetector("tcp", new SessionPattern(null, regex)));
		
		// FIXME
		//regex = Regex.caseInsensitive("^HTTP/1\\.[01].*\\r?\\n[Ss]erver: +([\\p{Graph} ]+)(:?\\/.*)?\\r?\\n.*"); // FIXME space ' '?
		regex = Regex.caseInsensitive("^HTTP/1\\.[01].*\\r?\\n[Ss]erver: +([^\\/\\t\\r\\n ][^\\/\\t\\r\\n]*)(:?\\/.*)?\\r?\\n.*");
		regex.add("serviceType", "HTTP");
		regex.add(1, "product");
		answer.add(newDetector("tcp", new SessionPattern(null, regex)));

/*		Regex clientRegex = Regex.caseInsensitive(".*(Host: ([^\\r\\n]+)[\\r\\n])?.*");
		clientRegex.add(2, "hostname");
		regex = Regex.caseInsensitive("^HTTP/1\\.[01].*(Server: ([^\\r\\n]+)[\\r\\n])?.*");
		regex.add("serviceType", "HTTP");
		regex.add(2, "product");
		answer.add(newDetector("tcp", new SessionPattern(null, regex)));
*/
		answer.add(newDetector("tcp", ".*", "^HTTP/1\\.[01].*", "HTTP", null, null, null));

		// HTTPS // FIXME has sens? not better HTTP + TLSv1|SSL tag?
		answer.add(newDetector("tcp", ".*", ".*<title>400 Bad Request<\\/title>.*Your browser sent a request that this server could not understand\\..*", "HTTPS", "Apache", null, null));

		// Real Time Streaming Protocol
		regex = Regex.caseInsensitive("^RTSP/1\\.0.*\\r\\nServer: QTSS\\/([\\d.]+) \\(.*Build\\/([^;]+);.*Platform\\/([^;]+).*\\)\\r\\n.*");
		regex.add("serviceType", "RTSP");
		regex.add("product", "QuickTime Streaming Server"); // Apple's commercial streaming server delivered as part of Mac OS X Server
		regex.add(1, "version");
		regex.add(2, "build");
		regex.add(3, "os"); // MacOSX
		answer.add(newDetector("tcp", new SessionPattern(null, regex)));
		regex = Regex.caseInsensitive("^RTSP/1\\.0.*\\r\\nServer: DSS\\/([\\d.]+) \\(.*Build\\/([^;]+);.*Platform\\/([^;]+).*\\)\\r\\n.*");
		regex.add("serviceType", "RTSP");
		regex.add("product", "Darwin Streaming Server");
		regex.add(1, "version");
		regex.add(2, "build");
		regex.add(3, "os"); // Windows, Linux, and Solaris
		answer.add(newDetector("tcp", new SessionPattern(null, regex)));
		
		// POP3
		
		regex = new Regex("^\\+OK <.*@([-.\\w]+)>.*XMail ([\\d-.]+) .*");
		regex.add("serviceType", "POP3");
		regex.add("product", "XMail");
		regex.add(2, "version");
		regex.add(1, "hostname");
		regex.add("os", "Windows");
		answer.add(newDetector("tcp", new SessionPattern(null, regex)));

		answer.add(newDetector("tcp", ".*", "^\\+OK Welcome to MailEnable POP3 Server.*", "POP3", "MailEnable", null, "Windows"));
		answer.add(newDetector("tcp", ".*", "^\\+OK.*MailEnable.*POP3.*", "POP3", "MailEnable", null, "Windows"));
		answer.add(newDetector("tcp", ".*", "^\\+OK ready  <\\d{1,5}\\.10\\d{8}@([-.\\w]+)>\\r\\n.*", "POP3", "Qualcomm Qpopper pop3d", null, null));
		answer.add(newDetector("tcp", ".*", "^\\+OK Lotus Notes POP3 server version ([-.\\w]+) ready.*", "POP3", "Lotus Domino", 1, null));
		answer.add(newDetector("tcp", ".*", "^\\+OK Lotus Notes POP3 server version Release ([-.\\w]+) ready.*", "POP3", "Lotus Domino", 1, null));
		answer.add(newDetector("tcp", ".*", "^\\+OK.*Lotus Notes POP3.*", "POP3", "Lotus Domino", null, null));
		answer.add(newDetector("tcp", ".*", "^\\+OK Microsoft Exchange Server 2003 POP3 server version ([\\d.]+) \\(([\\w-_.]+)\\) ready\\.\\r\\n.*", "POP3", "Microsoft Exchange 2003 POP3D", 1, "Windows")); // XXX 2nd group is OS?what format?
		answer.add(newDetector("tcp", ".*", "^\\+OK Microsoft Exchange 2000 POP3 server version (\\S+).* ready\\.\\r\\n.*", "POP3", "Microsoft Exchange 2000 POP3", 1, "Windows"));
		answer.add(newDetector("tcp", ".*", "^\\+OK Microsoft Exchange POP3 server version (\\S+) ready\\r\\n.*", "POP3","Microsoft Exchange POP3D", 1, "Windows"));
		answer.add(newDetector("tcp", ".*", "^\\+OK.*Exchange.*POP3.*", "POP3", "Microsoft Exchange POP3", null, "Windows")); // XXX 2nd group is OS?what format?
		answer.add(newDetector("tcp", ".*", "^\\+OK ([-.\\w]+) POP MDaemon (\\S+) ready <MDAEMON.*", "POP3", "MDaemon", 2, "Windows"));
		answer.add(newDetector("tcp", ".*", "^\\+OK ([-.\\w]+) POP MDaemon ready using UNREGISTERED SOFTWARE ([\\d.]+) <MDAEMON.*", "POP3", "MDaemon", 2, "Windows"));
		answer.add(newDetector("tcp", ".*", "^\\+OK.*POP MDaemon.*", "POP3", "MDaemon", null, "Windows"));
		answer.add(newDetector("tcp", ".*", "^\\+OK GroupWise POP3 server ready\\r\\n.*", "POP3", "Novell GroupWise", null,  null));
		answer.add(newDetector("tcp", ".*", "^\\+OK Qpopper \\(version ([\\d.]+)\\) at .* starting\\..*", "POP3", "Qpopper", 1, null));
		answer.add(newDetector("tcp", ".*", "^\\+OK.*[Dd]ovecot.*", "POP3", "Dovecot", null, "Unix"));
		
		regex = new Regex("^\\+OK <.*@([-.\\w]+)>.*");
		regex.add("serviceType", "POP3");
		regex.add(1, "hostname");
		answer.add(newDetector("tcp", new PortSet("110"), new SessionPattern(null, regex)));
		
		answer.add(newDetector("tcp", new PortSet("110"), ".*", "^\\+OK.*", "POP3", null, null, null));

		// SMTP
		regex = new Regex("^220[ -]([-.\\w]+) .*Sendmail ([-.\\w]+)/.*");
		regex.add("serviceType", "SMTP");
		regex.add("product", "Sendmail");
		regex.add(2, "version");
		regex.add(1, "hostname");
		answer.add(newDetector("tcp", new SessionPattern(null, regex)));

		regex = new Regex("^220.*Sendmail ([-.\\w]+)/.*"); // can this happen? no hostname
		regex.add("serviceType", "SMTP");
		regex.add("product", "Sendmail");
		regex.add(1, "version");
		answer.add(newDetector("tcp", new SessionPattern(null, regex)));

		answer.add(newDetector("tcp", ".*", "^220.*Sendmail.*", "SMTP", "Sendmail", null, null));
		answer.add(newDetector("tcp", ".*", "^220.*MailGate.*", "SMTP", "MailGate", null, "Windows"));

		regex = new Regex("^220 <.*@([-.\\w]+)>.*XMail ([\\d-.]+) .*");
		regex.add("serviceType", "SMTP");
		regex.add("product", "XMail");
		regex.add(2, "version");
		regex.add(1, "hostname");
		regex.add("os", "Windows");
		answer.add(newDetector("tcp", new SessionPattern(null, regex)));

		answer.add(newDetector("tcp", ".*", "^220.*XMail.*", "SMTP", "XMail", null, "Windows"));
		
		regex = new Regex("^220[ -]([-.\\w]+) ESMTP MDaemon (\\d[-.\\w]+);.*");
		regex.add("serviceType", "SMTP");
		regex.add("product", "MDaemon");
		regex.add(2, "version");
		regex.add(1, "hostname");
		regex.add("os", "Windows");
		answer.add(newDetector("tcp", new SessionPattern(null, regex)));
		
		regex = new Regex("^220[ -]([-.\\w]+) Kerio MailServer ([-.\\w]+) ESMTP.*");
		regex.add("serviceType", "SMTP");
		regex.add("product", "Kerio");
		regex.add(2, "version");
		regex.add(1, "hostname");
//		regex.add("os", "Windows");
		answer.add(newDetector("tcp", new SessionPattern(null, regex)));

		regex = new Regex("^220[ -]([-.\\w]+) Microsoft E?SMTP MAIL Service, Version: ([-.\\w]+) ready.*");
		regex.add("serviceType", "SMTP");
		regex.add("product", "Microsoft Exchange");
		regex.add(2, "version");
		regex.add(1, "hostname");
		regex.add("os", "Windows");
		answer.add(newDetector("tcp", new SessionPattern(null, regex)));

		regex = new Regex("^220[ -]([-.\\w]+) Mercury/32 v([-.\\w]+) ESMTP server ready.*");
		regex.add("serviceType", "SMTP");
		regex.add("product", "Mercury/32");
		regex.add(2, "version");
		regex.add(1, "hostname");
		regex.add("os", "Windows");
		answer.add(newDetector("tcp", new SessionPattern(null, regex)));

		regex = new Regex("^220[ -]([-.\\w]+) .*[Pp]ost[Ff]ix.*");
		regex.add("serviceType", "SMTP");
		regex.add("product", "Postfix");
		regex.add(1, "hostname");
		answer.add(newDetector("tcp", new SessionPattern(null, regex)));

		regex = new Regex("^220[ -]([-.\\w]+) E?SMTP \\(spam is not appreciated\\)\\r\\n.*");
		regex.add("serviceType", "SMTP");
		regex.add(1, "hostname");
		regex.add("product", "Postfix");
		answer.add(newDetector("tcp", new SessionPattern(null, regex)));

		answer.add(newDetector("tcp", ".*", "^220.*IMail.*", "SMTP", "IMail", null, "Windows"));
		answer.add(newDetector("tcp", ".*", "^220.*Exchange.*", "SMTP", "Microsoft Exchange", null, "Windows"));
		answer.add(newDetector("tcp", ".*", "^220.*MailEnable.*", "SMTP", "MailEnable", null, "Windows"));
		answer.add(newDetector("tcp", ".*", "^220.*Lotus Domino.*", "SMTP", "Lotus Domino", null, null));
		answer.add(newDetector("tcp", ".*", "^220[ -].* ESMTP Exim ([\\d.]+) .*\\r\\n.*", "SMTP", "Exim", 1, null)); // TODO save compilation date in a tag
		answer.add(newDetector("tcp", ".*", "^220.*[Pp]ost[Ff]ix.*", "SMTP", "Postfix", null, null));

		answer.add(newDetector("tcp", ".*", "^220 .* ESMTP \\(spam is not appreciated\\)\\r\\n.*", "SMTP", "Postfix", null, null));
		
		answer.add(newDetector("tcp", ".*", "^220 ([^\\s]+) V([-.\\w]+), (OpenVMS V[-.\\w]+(?: VAX)?) ready.*", "SMTP", "VMS SMTP", 2, 3)); //TODO: add hostname [and arch?]
		answer.add(newDetector("tcp", ".*", "^220 ([^\\s]+) Symantec Mail Security .*\\r\\n.*", "SMTP", "Symantec Mail Security for Microsoft Exchange", null, "Windows"));

		regex = new Regex("^220[ -]([-.\\w]+) E?SMTP.*");
		regex.add("serviceType", "SMTP");
		regex.add(1, "hostname");
		answer.add(newDetector("tcp", new SessionPattern(null, regex)));

		answer.add(newDetector("tcp",
				Pattern.compile(".*"),
				Pattern.compile("^(220.*\n250|220.*SMTP|412 .*smtp|554.*mail|554.*smtp|220 .*mail).*", Pattern.MULTILINE|Pattern.DOTALL|Pattern.CASE_INSENSITIVE),
				"SMTP", null, null, null));

		answer.add(newDetector("tcp", new PortSet("25"), ".*", "^220.*", "SMTP", null, null, null));
		
		// MySQL
		answer.add(newDetector("tcp", ".*", "^.\\x00\\x00\\x00\\x0a([-\\w._]*)\\x00.*", "MySQL", "MySQL", 1, null));
		// (access not allowed from this host, should tag)
		answer.add(newDetector("tcp", ".*", "^.\\x00\\x00\\x00\\xff\\x6a\\x04.*", "MySQL", "MySQL", null, null));

		// MISC
		answer.add(new OracleDetector());
		answer.add(new MSSQLDetector());
		
		// Remote Desktop (3389)
		answer.add(newDetector("tcp", ".*", "^\\x03\\x00\\x00[\\x0b\\x11].*", "RDP", null, null, "Windows")); // FIXME no all 3389 are windows!
		/*
		Remote Desktop Protocol:
		- Aqua Connect became the first company to license and implement RDP server for the Mac OS X platform, thus allowing users to connect to the Mac OS X server with Microsoft's RDP.
		- xrdp is an open source implementation of the RDP server available for Unix-like operating systems.
		*/
		
		// VNC
		answer.add(newDetector("tcp", "", "^RFB.*", "VNC", null, null, null)); // FIXME ""
		
		// DNS version response
		answer.add(newDetector("udp", ".*", ".*\\x07version\\x04bind.*\\x00\\x10\\x00\\x03\\x00\\x00\\x00\\x00\\x00[\\x04-\\x15][\\x03-\\x14]([-\\w._ ]{3,20}).*", "DNS", "ISC Bind", 1, null));
		answer.add(newDetector("udp", ".*", ".*\\x07version\\x04bind.*[\\x03-\\x14]([-\\w._ ]{3,20})$", "DNS", "ISC Bind", 1, null));
		answer.add(newDetector("udp", ".*", ".*\\x07version\\x04bind.*[\\x03-\\x14]BIND ([-\\w._]{3,20})$", "DNS", "ISC Bind", 1, null));
		answer.add(newDetector("udp", ".*", ".*\\x07version\\x04bind.*", "DNS", null, null, null));
		answer.add(newDetector("tcp", ".*",
				"^.." + // len
				".." + // id
				"\\x80." + // flags (Response)
				".{8}" + // q + RRs
				".*(\\x00.\\x00\\x01)+.*", // any Internet Type
				"DNS", null, null, null));
		
		// DNS response
		answer.add(newDetector("udp", ".*", ".*\\x81\\x80\\x00.\\x00.\\x00.\\x00.*\\x00\\x01\\x00\\x01.*", "DNS", null, null, null));

		// DNS response refused (restrict to port 53 because this rule is too generic)
		answer.add(newDetector("udp", new PortSet("53"), ".*",
				"^.." + // id
				"\\x81\\x85" + // flags (Response Refused + Recursion)
				"\\x00{8}.*", // q + RRs (should be all 0 if refused, right?)
				"DNS", null, null, null));

		// mDNS
		answer.add(newDetector("udp", new PortSet("5353"), ".*",
				"^.{4}" +
				"[\\x00\\x84].{8}.*(i[pP]hone|i[tT]ouch).*",
				"mDNS", null, null, 1));
		answer.add(newDetector("udp", new PortSet("5353"), ".*",
				"^.{4}" +
				"[\\x00\\x84].{8}.*",
				"mDNS", null, null, null));
		
		// TODO review snmp // james
		// SNMP
		regex = new Regex(".*public.*\\x2b\\x06\\x01\\x02\\x01\\x01\\x01...(.*)");
		regex.add("serviceType", "SNMPv1");
		regex.add("password", "public");
		regex.add(1, "banner");
		answer.add(newDetector("udp", new SessionPattern(null, regex)));
		
		regex = new Regex("^0.\\x02\\x01\\x00\\x04\\x06public\\xa2.*");
		regex.add("serviceType", "SNMPv1");
		regex.add("password", "public");
		answer.add(newDetector("udp", new SessionPattern(null, regex)));
		
		// XDMCP
		answer.add(newDetector("udp", ".*", "^\\x00\\x01\\x00\\x05..\\x00\\x00..(\\w+)..(\\d+) user.*", "XDMCP", "dtlogin", 2, "Unix"));
		answer.add(newDetector("udp", ".*", "^\\x00\\x01\\x00[\\x08\\x0c].{8}.+", "XDMCP", null, null, "Unix"));
		//answer.add(newDetector("udp", ".*", "^\\x00\\x01\\x00\\x0e.*", "XDMCP", null, null, "Unix"));
		
		// UPnP
		regex = new Regex(".*\\r\\nLocation:\\s*http://(.+):(\\d+)/.*\\r\\nServer:\\s*([^\r]+)\\r\\n.*");
		regex.add("serviceType", "UPnP");
		regex.add(3, "product");
		answer.add(newDetector("udp", new SessionPattern(null, regex)));
		
		// Radius 'Access-Reject' packet
		answer.add(newDetector("udp", new PortSet("1812"), ".*", "^\\x03.*", "Radius", null, null, null));

		// SIP
		answer.add(newDetector("udp", new PortSet("5060"), ".*", "^SIP/2.0 200 OK[\\r\\n].*Server: (.*)[\\r\\n]", "SIP", null, null, null)); // FIXME review regex // james
		answer.add(newDetector("udp", new PortSet("5060"), ".*", "^SIP/.*", "SIP", null, null, null));

		// PCAnywhere
		answer.add(newDetector("tcp", ".*", "^\\x00X\\x08\\x00\\}\\x08\\r\\n\\x00\\.\\x08.*\\.\\.\\.\\r\\n", "PCAnywhere", "PCAnywhere", null, "Windows"));

		// IRC
		answer.add(newDetector("tcp", "^NICK.*", "^NOTICE AUTH.*", "IRC", null, null, null));
		answer.add(newDetector("tcp", "^NICK.*", "^:.* \\d\\d\\d .*", "IRC", null, null, null));
		answer.add(newDetector("tcp", ".*", "^:Welcome!.+@.+ NOTICE \\* :psyBNC([\\d.-]+)\\r\\n.*", "IRC", "psyBNC", 1, null));

		// NetBIOS
		//FIXME could also be tcp?
		//FIXME should set OS to Windows?
		//FIXME we're matching the transaction id 7908 that we send in the trigger, this wont work for passive detection
		regex = new Regex("^\\x79\\x08.\\x00\\x00\\x00\\x00.*\\x00\\x21\\x00\\x01.{7}([^\\x00]+)\\x00.*");
		regex.add("serviceType", "NetBIOS-NS");
		regex.add(1, "hostname");
		answer.add(newDetector("udp", new SessionPattern(null, regex)));
		
		answer.add(newDetector("udp", ".*", "^\\x79\\x08.*BROWSE.*", "NetBIOS-NS", null, null, null));
		answer.add(newDetector("udp", ".*", "^\\x79\\x08.\\x00\\x00\\x00\\x00.*", "NetBIOS-NS", null, null, null));
		answer.add(newDetector("udp", ".*", "^\\x05\\x00\\x0d\\x03.*", "NetBIOS-SSN", null, null, null));
		answer.add(newDetector("udp", ".*", "^\\x83\\x00.*", "NetBIOS-SSN", null, null, null));
		answer.add(newDetector("tcp", ".*", "^\\x82\\x00\\x00\\x00.*", "NetBIOS-SSN", null, null, null));

		// Microsoft-DS (SMB)
		//FIXME should set OS to Windows?
/*		answer.add(newDetector("tcp", ".*", "^\\x00\\x00\\x00.\\xffSMBr\\x00\\x00\\x00\\x00\\x88\\x01@\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00@\\x06\\x00\\x00\\x01\\x00\\x11\\x07\\x00.\n\\x00\\x01\\x00\\x04\\x11\\x00\\x00\\x00\\x00\\x01\\x00\\x00\\x00\\x00\\x00\\xfd\\xe3\\x03\\x00.*", "Microsoft-DS", null, null, "Microsoft Windows Longhorn"));
		answer.add(newDetector("tcp", ".*", "^\\x00\\x00\\x00.\\xffSMBr\\x00\\x00\\x00\\x00\\x88\\x01@\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00@\\x06\\x00\\x00\\x01\\x00\\x11\\x07\\x00.\n\\x00\\x01\\x00\\x04\\x11\\x00\\x00\\x00\\x00\\x01\\x00\\x00\\x00\\x00\\x00\\xfd\\xe3\\x00\\x00.*", "Microsoft-DS", null, null, "Microsoft Windows XP"));
		answer.add(newDetector("tcp", ".*", "^\\x00\\x00\\x00.\\xffSMBr\\x00\\x00\\x00\\x00\\x88\\x01@\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00@\\x06\\x00\\x00\\x01\\x00\\x11\\x07\\x00.2\\x00\\x01\\x00\\x04A\\x00\\x00\\x00\\x00\\x01\\x00\\x00\\x00\\x00\\x00\\xfd\\xf3\\x00\\x00.*", "Microsoft-DS", null, null, "Microsoft Windows 2000"));
		answer.add(newDetector("tcp", ".*", "^\\x00\\x00\\x00.\\xffSMBr\\x00\\x00\\x00\\x00\\x88\\x01@\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00@\\x06\\x00\\x00\\x01\\x00\\x11\\x07\\x00.2\\x00\\x01\\x00\\x04.\\x00\\x00\\x00\\x00\\x01\\x00\\x00\\x00\\x00\\x00\\xfd\\xf3\\x01\\x00.*", "Microsoft-DS", null, null, "Microsoft Windows 2003"));
		answer.add(newDetector("tcp", ".*", "^\\x00\\x00\\x00.\\xffSMBr\\x00\\x00\\x00\\x00\\x88\\x01@\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00@\\x06\\x00\\x00\\x01\\x00\\x11\\x07\\x00.[}2]\\x00\\x01\\x00\\x04A\\x00\\x00\\x00\\x00\\x01\\x00\\x00\\x00\\x00\\x00\\xfd[\\xe3\\xf3]\\x00\\x00.*", "Microsoft-DS", null, null, "Microsoft Windows 2000"));
*/		answer.add(newDetector("tcp", ".*", "\\x00\\x00\\x00\\x55\\xff\\x53\\x4d\\x42\\x72\\x00.*", "Microsoft-DS", null, null, null));
		answer.add(newDetector("tcp", ".*", "^.....SMB.*", "Microsoft-DS", null, null, null));
		answer.add(newDetector("tcp", ".*", "^\\x00\\x00\\x00\\x65.*", "Microsoft-DS", null, null, null));

		// RPC Reply
		//FIXME these detectors are too generic
		//FIXME we're matching the transaction id 12345678 that we send in the trigger, this wont work for passive detection
		answer.add(newDetector("udp", ".*",
				"\\x12\\x34\\x56\\x78" // "^.{4}"							// XID
				+ "\\x00\\x00\\x00\\x01"		// Reply
				+ "\\x00\\x00\\x00[\\x00\\x01]"	// replyState
				+ ".{12}.*",					// verifier + acceptState (12x'0' most of the time)
				"RPC", null, null, null));
		answer.add(newDetector("tcp", ".*",
				"^(.{4})?"						// fragment (optional)
				+ "\\x12\\x34\\x56\\x78" // ".{4}"						// XID
				+ "\\x00\\x00\\x00\\x01"		// Reply
				+ "\\x00\\x00\\x00[\\x00\\x01]"	// replyState
				+ ".{12}.*",					// verifier + acceptState (12x'0' most of the time)
				"RPC", null, null, null));

		// Telnet
		// TODO suboption (ff fa .* -> ff f0)?
		// FIXME is "^(\\xff.*)|$" really useful?
		answer.add(newDetector("tcp", "^(\\xff.*)|$", "^(?:\\xff[\\xfb\\xfd\\xfe].)+.*(\\r\\n){2}User Access Verification(\\r\\n){2}(Username|Password): .*", "Telnet", null, null, "Cisco"));
		answer.add(newDetector("tcp", "^(\\xff.*)|$", "^(?:\\xff[\\xfb\\xfd\\xfe].)+.*", "Telnet", null, null, null)); // IAC + (WILL|DO|DON'T)
		answer.add(newDetector("tcp", "^(\\xff.*)|$", ".*Telnet is disabled now.*", "Telnet", null, null, null));
		answer.add(newDetector("tcp", new PortSet("23,2000-2063"), "^(\\xff.*)|$", "^(?:[\\p{Print}\\p{Blank}]*\\r\\n)+[\\p{Print}\\p{Blank}]*", "Telnet", null, null, null)); // 2000-2063: 64 serial lines on a Cisco terminal servers // FIXME not limit to only that range?
		// TODO "^(?:\\xff[\\xfb\\xfd\\xfe].)+.*(\\r\\n){2}Password required, but none set\\r\\n.*"
		// TODO "^(?:\\xff[\\xfb\\xfd\\xfe].)+.*(\\r\\n){2}SunOS 5.\\d+\\r\\n.*"
		// TODO "^(?:\\xff[\\xfb\\xfd\\xfe].)+.*\\r\\nHP-UX\ [^ ]+ (.*)(\\r\\n){2}.*"


		// Whois
		answer.add(newDetector("tcp", new PortSet("43"), ".*", ".*Whois.*(?i:server|data).*\\n.*", "Whois", null, null, null));
		answer.add(newDetector("tcp", new PortSet("43"), ".*", "^NOTICE AND TERMS OF USE: You are not authorized to access or query our WHOIS\\ndatabase.*", "Whois", null, null, null));
		answer.add(newDetector("tcp", new PortSet("43"), ".*", "^\\nquery: .*\\n\\n# KOREAN\\n\\n.*", "Whois", null, null, null));
		
		// SSL
		answer.add(newDetector("tcp", ".*", "^\\x16" + // Handshake
				"\\x03\\x01" + // TLS 1.0
				".." + // len
				// Handshake
				"\\x02" +  // Server Hello
				"..." +
				"\\x03\\x01" + // TLS 1.0
				".*", "SSL", null, "TLSv1", null)); // TODO v1.0
		answer.add(newDetector("tcp", ".*", "\\x16" +// Handshake
				"\\x03\\x00" + // SSL 3.0
				".." +	// len
				// Handshake
				"\\x02" +  // Server Hello
				"..." +
				"\\x03\\x00" + // SSL 3.0
				".*", "SSL", null, "SSLv3", null));
		
		// LDAP
		answer.add(newDetector("tcp", new PortSet("389"), ".*",
				"^0.\\x02\\x01.\\w.*", "LDAP", null, null, null));
		
		// Kerberos v5
		answer.add(newDetector("udp", new PortSet("88"), ".*",
				"^.{10}\\x05.*", "Kerberos", null, "5", null));

		// Radius
		answer.add(newDetector("udp", new PortSet("1812"), ".*",
				"^[\\x02\\x0b]" +	// AccessAccept|AccessChallenge
				"." +				// ID
				"\\x00." +			// len
				".{16}.*",			// authentificator, ...
				"Radius", null, null, null)); // TODO version
		
		// ISAKMP
		answer.add(newDetector("udp", new PortSet("500"), ".*",
				"^.{16}" +		// 2 cookies
				"." +			// payload
				"\\x10" +		// version (0x10 = 1.0)
				".{10}.*",		// type + flags + id + len
				"ISAKMP", null, null, null));

		return answer;
	}

	public List<INetworkServiceTrigger> getTriggers() {
		List<INetworkServiceTrigger> answer = new ArrayList<INetworkServiceTrigger>();
		
		answer.add(newTrigger("tcp", new PortSet("21,990"), "USER ftp\r\n".getBytes()));
		
		answer.add(newTrigger("tcp", new PortSet("80,81,88,1080,8000,8080-8081,8118,8888,443"), "GET / HTTP/1.0\r\n\r\n".getBytes()));
		
//		answer.add(newTrigger("tcp", new PortSet("1433"), HexaEncoding.hex2bytes("1201003400000000000015000601001b000102001c000c0300280004ff080000c20000004d5353514c53657276657200ac070000")));
		
		answer.add(newTrigger("tcp", new PortSet("3306"), "\n\n\n\n\n".getBytes()));
		
		ByteBuffer buffer = ByteBuffer.allocate(512);
		buffer.put(HexaEncoding.hex2bytes(
				// TNS Header
				"005a" + // packet len
				"0000" +
				"01" + // packet type CONNECT
				"00" + "0000" +
				// Connect Packet
				"0136 012c 0000 0800 7fff a30a 0000 0100" +
				"0020" + // len of data
				"003a" + // offset of data
				"00000000 00 00 00000000 00000000" +
				"000006fc00000002" +
				"0000000000000000" // padding for alignment?
			));
		buffer.put("(CONNECT_DATA=(COMMAND=version))".getBytes());
		buffer.flip();
		byte[] trigger = new byte[buffer.remaining()];
		buffer.get(trigger);
		answer.add(newTrigger("tcp", new PortSet("1521-1529"), trigger));
		
//		answer.add(newTrigger("tcp", new PortSet("1521"), HexaEncoding.hex2bytes("00 A6 00 00 01 00 00 00 01 34 01 2C 00 00 08 00 7F FF 4F 98 00 00 00 01 00 84 00 22 00 00 00 00 01 01 28 44 45 53 43 52 49 50 54 49 4F 4E 3D 28 43 4F 4E 4E 45 43 54 5F 44 41 54 41 3D 28 53 49 44 3D 74 65 73 74 29 28 43 49 44 3D 28 50 52 4F 47 52 41 4D 3D 29 28 48 4F 53 54 3D 5F 5F 6A 64 62 63 5F 5F 29 28 55 53 45 52 3D 29 29 29 28 41 44 44 52 45 53 53 3D 28 50 52 4F 54 4F 43 4F 4C 3D 74 63 70 29 28 48 4F 53 54 3d 31 36 32 2e 32 37 2e 35 39 2e 31 33 35 29 28 50 4F 52 54 3D 31 35 32 31 29 29 29")));

		answer.add(newTrigger("tcp", new PortSet("3389"), HexaEncoding.hex2bytes("0300000b06e00000000000")));

		answer.add(newTrigger("tcp", new PortSet("25,465"), "HELO mail\r\n".getBytes()));

		answer.add(newTrigger("tcp", new PortSet("110,143,995"), "\r\n".getBytes())); // just to get some response

		// MSSQL
		answer.add(newTrigger("tcp", new PortSet("1433"), new byte[] {
					0x12, 0x01, 0x00, 0x34, 0x00, 0x00, 0x00, 0x00,
					0x00, 0x00, 0x15, 0x00, 0x06, 0x01, 0x00, 0x1b,
					0x00, 0x01, 0x02, 0x00, 0x1c, 0x00, 0x0c, 0x03,
					0x00, 0x28, 0x00, 0x04, (byte) 0xff, 0x08, 0x00,
					0x01, 0x55, 0x00, 0x00, 0x00, 0x4d, 0x53, 0x53,
					0x51, 0x4c, 0x53, 0x65, 0x72, 0x76, 0x65, 0x72,
					0x00, 0x4e, 0x53, 0x46, 0x4f }));

		// DNS version
		answer.add(newTrigger("udp", new PortSet("53"), HexaEncoding.hex2bytes("0006010000010000000000000776657273696f6e0462696e640000100003")));
		
		// RPC proc-0 Call
		
		/* 111:rpcbind, 2049:nfs, 4045:nlockmgr
		 * some services are binded below 1024: rquotad, mountd, status (not always respected on Solaris)
		 * repartition is between 600-1000 but closer to mod[100] i.e.: 796,799,809,812,995,998
		 * Linux repartition is pretty random (up to 65535...)
		 * by default in 32770-32850 on Solaris < 10
		 */
		PortSet rpcPortSet = new PortSet("111,2049,4045,32771-32850"); // TODO add HPUX default portrange
		
		answer.add(newTrigger("udp", rpcPortSet, HexaEncoding.hex2bytes(
				"12345678 00000000 00000002" +
					//"000186a0 00000002" + // recognized by wireshark
					"00000000 00000000" +	// protocol correct but not analyzed by wireshark
				"00000000 0000000000000000 0000000000000000")));
		answer.add(newTrigger("tcp", rpcPortSet, HexaEncoding.hex2bytes(
				"80000028" + // fragment header: last frame
				"12345678 00000000 00000002" +
					//"000186a0 00000002" +
					"00000000 00000000" +
				"00000000 0000000000000000 0000000000000000")));
		
		// SNMPv1 public
		answer.add(newTrigger("udp", new PortSet("161"), HexaEncoding.hex2bytes(
				"302602010004067075626c6963a119" +
				"02041f3c1d65" +
				"020100020100300b300906052b060102010500")));
	//			"3082002f02010004067075626c6963a082002002044c33a756020100020100308200103082000c06082b060102010105000500"));
				//"0082002f"
				//"0201000406"+"7075626c6963"+"a082002002044c33a756020100020100308200103082000c06082b060102010105000500"));
		
		// XDMCP
		answer.add(newTrigger("udp", new PortSet("177"), HexaEncoding.hex2bytes("0001000200090100065a6172646f5a"))); // OP_QUERY
		//answer.add(newTrigger("udp", new PortSet("177"), HexaEncoding.hex2bytes("0001000d00000000000000")); // OP_KEEPALIVE
		
		// Radius
		// NOTE:  after receiving that Access-Request packet, the radius server will wait 4seconds before reply)
		answer.add(newTrigger("udp", new PortSet("1812"), HexaEncoding.hex2bytes("0100001400000000000000000000000000000000")));
		
		// UPnP (Universal Plug and Play) version
		// FIXME only LAN
		answer.add(newTrigger("udp", new PortSet("1900"), "M-SEARCH * HTTP/1.1\r\nST: ssdp:all\r\nMX: 4000\r\nMAN: \"ssdp:discover\"\r\n\r\n".getBytes()));

		// SIP
		answer.add(newTrigger("udp", new PortSet("5060"), "".getBytes()));

		// NetBIOS
		answer.add(newTrigger("udp", new PortSet("135-139"), HexaEncoding.hex2bytes("79 08 00 00 00 01 00 00 00 00 00 00 20 43 4b 41 41 41 41 41 41 41 41 41 41 41 41 41 41 41 41 41 41 41 41 41 41 41 41 41 41 41 41 41 41 00 00 21 00 01")));

		// Microsft-DS (SMB)
		answer.add(newTrigger("tcp", new PortSet("445"), HexaEncoding.hex2bytes("00 00 00 85 FF 53 4D 42 72 00 00 00 00 18 53 C8 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FE 00 00 00 00 00 62 00 02 50 43 20 4E 45 54 57 4F 52 4B 20 50 52 4F 47 52 41 4D 20 31 2E 30 00 02 4C 41 4E 4D 41 4E 31 2E 30 00 02 57 69 6E 64 6F 77 73 20 66 6F 72 20 57 6F 72 6B 67 72 6F 75 70 73 20 33 2E 31 61 00 02 4C 4D 31 2E 32 58 30 30 32 00 02 4C 41 4E 4D 41 4E 32 2E 31 00 02 4E 54 20 4C 4D 20 30 2E 31 32 00")));

		// SSH
		answer.add(newTrigger("tcp", new PortSet("22"), "SSH-2.0-OpenSSH".getBytes()));
		
		// Telnet
		answer.add(newTrigger("tcp", new PortSet("23"),
			HexaEncoding.hex2bytes(
					"ff fb 03" // Will Suppress Go Ahead
					//"ff fc 25" +
					//"ff fc 26" +
					//"ff fc 01" +
					//"ff fc 21" +
					//"ff fc 24"
			)));

		// to detect services with banner when we don't need any trigger
		// Finger, NNTP
		answer.add(newTrigger("tcp", new PortSet("79,119"), new byte[0])); // dont send anything, just have this here so it will connect to ports 22,23

		return answer;
	}
}
