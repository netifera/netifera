package com.netifera.platform.net.http.internal.web.applications;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.ComponentContext;

import com.netifera.platform.net.http.web.applications.IWebApplicationDetector;
import com.netifera.platform.util.patternmatching.ISessionPattern;
import com.netifera.platform.util.patternmatching.Regex;
import com.netifera.platform.util.patternmatching.SessionPattern;

public class WebApplicationDetector implements IWebApplicationDetector {
	private final ArrayList<String> triggers = new ArrayList<String>();
	private final ArrayList<ISessionPattern> detectors = new ArrayList<ISessionPattern>();
	
	protected void activate(ComponentContext context) {
		//TODO add relative triggers
		addTrigger("/");
		addTrigger("/index.php");
		addTrigger("/phpMyAdmin");
		
		// Tomcat
		addTrigger("/admin/login.jsp");
		addTrigger("/manager/html");
		
		addTrigger("/info.php");
		addTrigger("/phpinfo.php");
//		addTrigger("zboard.php");
		addTrigger("/index.asp");
		addTrigger("/cgi-bin/webif/"); // OpenWRT
		
		// Joomla!
		addTrigger("/administrator");
		addTrigger("/content/administrator");
		
		// WordPress
		addTrigger("/wp/wp-login.php");
		addTrigger("/wp-login.php");
		addTrigger("/wordpress/wp-login.php");

		// Citrix
		addTrigger("/Citrix/MetaframeXP/default/login.asp"); // Citrix
//		addTrigger("/Citrix/MetaFrame/"); // or /Citrix/MetaFrame/auth/login.aspx ???
		addTrigger("/Citrix/MetaFrame/auth/login.aspx");
		addTrigger("/Citrix/MetaFrame/login.asp"); // this kind of citrix needs cookies set
		addTrigger("/Citrix/NFuse17/login.asp");
		
		
		// Cisco web administration interface
		Regex response = new Regex("HTTP/1.0 401.*WWW-Authenticate: Basic realm=\"level.15 access\".*");
//		regex = new Regex("HTTP/1.0 401.*WWW-Authenticate: Basic realm=\"access\".*"); // XXX false positives
		response.add("serviceType", "Cisco Web Manager");
		response.add("path", "/");
		addDetector(new SessionPattern(null, response));

		// OpenWrt
		response = new Regex("HTTP/1.0 401.*WWW-Authenticate: Basic realm=\"OpenWrt\".*");
		response.add("serviceType", "OpenWrt");
		response.add("path", "/");
		addDetector(new SessionPattern(null, response));

		response = new Regex(".*<meta http-equiv=\"refresh\" content=\"0; URL=/cgi-bin/webif\\.sh\" />.*");
		response.add("serviceType", "OpenWrt");
		response.add("path", "/");
		addDetector(new SessionPattern(null, response));

		// DD-WRT
		response = new Regex(".*WWW-Authenticate: Basic realm=\"DD-WRT\".*");
		response.add("serviceType", "DD-WRT");
		response.add("path", "/index.asp");
		addDetector(new SessionPattern(null, response));

		// Linksys
		response = new Regex(".*WWW-Authenticate: Basic realm=\"(?:Linksys )?(\\p{Upper}+\\p{Digit}+[\\p{Upper}\\p{Digit}]*)\".*");
		response.add("serviceType", "Linksys");
		response.add(1, "version");
		response.add("path", "/");
		addDetector(new SessionPattern(null, response));

		// TP-Link
		response = new Regex(".*WWW-Authenticate: Basic realm=\"TP-LINK Wireless Router (.*)\".*");
		response.add("serviceType", "TP-LINK");
		response.add(1, "version");
		response.add("path", "/");
		addDetector(new SessionPattern(null, response));

		// phpMyAdmin
		response = new Regex(".*<title>phpMyAdmin (.*)</title>.*");
		response.add("serviceType", "phpMyAdmin");
		response.add(1, "version");
		response.add("path", "/phpMyAdmin");
		addDetector(new SessionPattern(null, response));

		// Tomcat
		response = new Regex(".*<title>Tomcat Server Administration</title>.*<form method=\"POST\" action='j_security_check.*");
		response.add("serviceType", "Tomcat Admin");
		response.add("path", "/admin/login.jsp");
		addDetector(new SessionPattern(null, response));

		response = new Regex(".*WWW-Authenticate: Basic realm=\"Tomcat Manager Application\".*Apache Tomcat/([\\w.]+) .*");
		response.add("serviceType", "Tomcat Manager");
		response.add(1, "version");
		response.add("path", "/manager/html");
		addDetector(new SessionPattern(null, response));

		// generic php request, bellow are responses that match web services
		Regex request = new Regex(".* (.*)/.*\\.php.*");
		request.add(1, "path");

		// vBulletin
//		response = new Regex(".*Do not remove this copyright notice -->Powered by vBulletin&reg; Version ([\\w.]+)<br.*");
		response = new Regex(".*Powered by vBulletin.*Version ([\\w.]+)<br.*");
		response.add("serviceType", "vBulletin");
		response.add(1, "version");
		addDetector(new SessionPattern(request, response));

		// phpBB
		request = new Regex(".* (.*)/.*\\.php .*");
		request.add(1, "path");
		response = new Regex(".*Powered by <a href=\"http://www\\.phpbb\\.com/\" target=\"_phpbb\" class=\"copyright\">phpBB</a>\\s+&copy;.*phpBB Group<br.*");
		response.add("serviceType", "phpBB");
		addDetector(new SessionPattern(request, response));

		response = new Regex(".*<div id=\"footer\">.*Powered by <a href=\"http://www\\.phpbb\\.com/\">phpBB</a>.*");
		response.add("serviceType", "phpBB");
		addDetector(new SessionPattern(request, response));

		response = new Regex(".*Powered by <a href=\"http://www\\.phpbb\\.com/\"( target=\"_blank\")?>phpBB</a>.*");
		response.add("serviceType", "phpBB");
		addDetector(new SessionPattern(request, response));

/*		response = new Regex(".*var style_cookie = 'phpBBstyle';.*");
		response.add("serviceType", "phpBB");
		addDetector(new SessionPattern(request, response));
*/
		// Zeroboard
		response = new Regex(".* : Zeroboard\\n.* : (.*)\\n.* : zero.*\\n.*Homepage : http://zeroboard\\.com.*");
		response.add("serviceType", "Zeroboard");
		response.add(1, "version");
		addDetector(new SessionPattern(request, response));
		
		response = new Regex(".*function ZB_layerAction\\(name,status\\).*");
		response.add("serviceType", "Zeroboard");
		addDetector(new SessionPattern(request, response));

		// Simple Machines Forum
		response = new Regex(".*<a href=\"http://www\\.simplemachines\\.org/\" title=\"Simple Machines Forum\" target=\"_blank\">Powered by SMF ([\\w.]+)</a>.*");
		response.add("serviceType", "SMF");
		response.add(1, "version");
		addDetector(new SessionPattern(request, response));
		
		// MyBB
		response = new Regex(".*<div id=\"copyright\">.*Powered By <a href=\"http://www\\.mybboard\\.(net|com)\" target=\"_blank\">MyBB</a> <br.*");
		response.add("serviceType", "MyBB");
		addDetector(new SessionPattern(request, response));
		
		// PHP-Nuke
		response = Regex.caseInsensitive(".*<META NAME=\"GENERATOR\" CONTENT=\"PHP-Nuke Copyright \\(c\\) ([\\w.]+) by Francisco.*");
		response.add("serviceType", "PHP-Nuke");
		response.add(1, "version");
		addDetector(new SessionPattern(request, response));
		
		response = Regex.caseInsensitive(".*<META HTTP-EQUIV=\"EXPIRES\".*<META NAME=\"AUTHOR\" CONTENT=\".*");
		response.add("serviceType", "PHP-Nuke");
		addDetector(new SessionPattern(request, response));
		
		// Invision Power Board
		response = new Regex(".*<a href='http://www\\.invisionboard\\.com' style='text-decoration:none' target='_blank'>.*Board</a>[\\s\\n]*v?([\\w.]+) &copy;.*IPS, Inc.*");
		response.add("serviceType", "Invision Power Board");
		response.add(1, "version");
		addDetector(new SessionPattern(request, response));
		
		// WordPress
		response = new Regex(".*wp-admin.css\\?version=([\\w.]+)'.*<div id=\"login\"><h1><a href=\"http://wordpress\\.org/\" title=\"Powered by WordPress\">.*");
		response.add("serviceType", "WordPress");
		response.add(1, "version");
		addDetector(new SessionPattern(request, response));

		response = new Regex(".*document\\.getElementById\\('user_login'\\).focus\\(\\).*");
		response.add("serviceType", "WordPress");
		addDetector(new SessionPattern(request, response));
		
		// Discuz!
		response = new Regex(".*Powered by <a href=\"http://www\\.discuz\\.net\" target=\"_blank\" style=\"color: blue\"><b>Discuz\\!</b></a> <b style=\"color:.*\">([\\w.]+)</b>.*&copy;.*Comsenz.*");
		response.add("serviceType", "Discuz");
		response.add(1, "version");
		addDetector(new SessionPattern(request, response));

		// PHP-Fusion
		// XXX this works?
		response = new Regex(".*Powered by <a href='http://www\\.php-fusion\\.co\\.uk' target='_blank'><img.*></a> v?([\\w.]+) &copy;.*");
		response.add("serviceType", "PHP-Fusion");
		response.add(1, "version");
		addDetector(new SessionPattern(request, response));

		// phpinfo()
		response = new Regex(".*<title>phpinfo\\(\\)</title>.*<a href=\"http://www\\.php\\.net/\"><img border=\"0\" src=\".*\" alt=\"PHP Logo\" /></a>.*");
//		response = new Regex(".*<title>phpinfo\\(\\)</title>.*<a href=\"http://www\\.php\\.net/\"><img border=\"0\" src=\"/phpinfo\\.php\\?=.*\" alt=\"PHP Logo\" /></a>.*");
		response.add("serviceType", "phpinfo()");
		addDetector(new SessionPattern(null, response));
		
		// SquirrelMail
		response = new Regex(".*function squirrelmail_loginpage_onload.*<small>SquirrelMail version ([\\w.-]+)<br />.*");
		response.add("serviceType", "SquirellMail");
		response.add(1, "version");
		addDetector(new SessionPattern(request, response));
		
		// Joomla!
		response = new Regex(".*Joomla.*<div (id|class)=\"footer\".*<a href=\"http://www.joomla.org\".*GNU.*");
		response.add("serviceType", "Joomla!");
		addDetector(new SessionPattern(null, response));
		
		// Citrix
		// GH inurl:citrix/metaframexp/default/login.asp
		response = new Regex(".*<script src=\"clientscripts/login.js\".*<script src=\"clientscripts/clientdetect.js\".*clientdetection\\(.*");
		response.add("serviceType", "Citrix Metaframe XP");
		addDetector(new SessionPattern(null, response));

		// GH inurl:Citrix/MetaFrame/auth/login.aspx
		response = new Regex(".*Copyright.*Citrix Systems.*Web Interface \\((Build [\\d]*)\\).*");
		response.add("serviceType", "Citrix Metaframe");
		response.add(1, "version");
		addDetector(new SessionPattern(null, response));
		
/*		// GH inurl:citrix/metaframe inurl:login.asp
		// note: this kind of citrix needs cookies set
		response = new Regex(".*function focus_UPD\\(loginForm\\).*");
		response.add("serviceType", "Citrix Metaframe");
		addDetector(new SessionPattern(null, response));
*/
		// GH inurl:NFuse
		response = new Regex(".*<form method=\"POST\" action=\"login.asp\" name=\"NFuseForm\">.*");
		response.add("serviceType", "Citrix NFuse Classic");
		addDetector(new SessionPattern(null, response));
	}
	
	public void addDetector(ISessionPattern detector) {
		detectors.add(detector);
	}
	
	public void addTrigger(String request) {
		triggers.add(request);
	}
	
	public List<String> getTriggers() {
		return triggers;
	}

	public Map<String,String> detect(String request, String response) {
		for (ISessionPattern detector: detectors) {
			Map<String,String> result = detector.match(request, response);
			if (result != null)
				return result;
		}
		return null;
	}
}
