package com.netifera.platform.net.http.internal.ui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.net.http.web.model.HTTPBasicAuthenticationEntity;
import com.netifera.platform.net.http.web.model.HTTPRequestEntity;
import com.netifera.platform.net.http.web.model.HTTPResponseEntity;
import com.netifera.platform.net.http.web.model.WebApplicationEntity;
import com.netifera.platform.net.http.web.model.WebFormAuthenticationEntity;
import com.netifera.platform.net.http.web.model.WebPageEntity;
import com.netifera.platform.net.http.web.model.WebSiteEntity;
import com.netifera.platform.ui.api.model.IEntityLabelProvider;
import com.netifera.platform.ui.images.ImageCache;

public class EntityLabelProvider implements IEntityLabelProvider {
	private final static String PLUGIN_ID = "com.netifera.platform.net.http.ui";

	private ImageCache images = new ImageCache(PLUGIN_ID);

	private static final String WEBSITE = "icons/world.png";
	private static final String WEBAPP = "icons/webapp.png";
	
	private static final String HTTP_ERROR = "icons/http_error.png";
	private static final String HTTP_REDIRECT = "icons/http_redirect.png";
	
	private static final String AUTH = "icons/lock_16x16.png";
	private static final String AUTH_REQUIRED = "icons/page_lock.png";
	
	private static final String ARCHIVE = "icons/mime-types/archive.png";
	private static final String AUDIO = "icons/mime-types/audio.png";
	private static final String BINARY = "icons/mime-types/binary.png";
	private static final String EXECUTABLE = "icons/mime-types/executable.png.png";
	private static final String FLASH = "icons/mime-types/flash.png";
	private static final String FONT = "icons/mime-types/font.png";
	private static final String HTML = "icons/mime-types/html.png";
	private static final String IMAGE = "icons/mime-types/image.png";
	private static final String PDF = "icons/mime-types/pdf.png";
	private static final String POSTSCRIPT = "icons/mime-types/postscript.png";
	private static final String RSS = "icons/mime-types/rss.png";
	private static final String SCRIPT = "icons/mime-types/script.png";
	private static final String TEXT = "icons/mime-types/text.png";
	private static final String VIDEO = "icons/mime-types/video.png";
	private static final String XML = "icons/mime-types/xml.png";
	private static final String MSWORD = "icons/mime-types/msword.png";
	private static final String PRESENTATION = "icons/mime-types/presentation.png";
	private static final String SPREADSHEET = "icons/mime-types/spreadsheet.png";

	public String getText(IShadowEntity e) {
		if (e instanceof WebSiteEntity) {
			return ((WebSiteEntity) e).getRootURL();
		} else if (e instanceof WebPageEntity) {
			WebPageEntity page = (WebPageEntity)e;
			if (page.getAuthentication() instanceof HTTPBasicAuthenticationEntity)
				return page.getPath()+" ["+((HTTPBasicAuthenticationEntity)page.getAuthentication()).getAuthenticationRealm()+"]";
			return page.getPath();
		} else if (e instanceof HTTPBasicAuthenticationEntity) {
			HTTPBasicAuthenticationEntity auth = (HTTPBasicAuthenticationEntity)e;
			return "WWW-Authenticate: Basic realm=\""+auth.getAuthenticationRealm()+"\"";
		} else if (e instanceof WebFormAuthenticationEntity) {
			WebFormAuthenticationEntity auth = (WebFormAuthenticationEntity)e;
			return "web forms authentication"; // FIXME caps?
		} else if (e instanceof WebApplicationEntity) {
			WebApplicationEntity app = (WebApplicationEntity) e;
			String answer = app.getServiceType();
			try {
				URI url = new URI(app.getURL());
				answer = url.getPath()+" "+answer;
			} catch (URISyntaxException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
			if (app.getVersion() != null) answer += " ["+app.getVersion()+"]";
			return answer;
		} else if (e instanceof HTTPRequestEntity) {
			HTTPRequestEntity request = (HTTPRequestEntity) e;
			HTTPResponseEntity response = request.getResponse();
			if (response != null && response.getStatusCode() >= 300)
				return request.getMethod()+" "+request.getURL()+" -> "+response.getStatusLine();
			else
				return request.getMethod()+" "+request.getURL();
		}
		return null;
	}
	
	public String getFullText(IShadowEntity e) {
		return getText(e);
	}
	
	public Image getImage(IShadowEntity e) {
		if (e instanceof WebSiteEntity) {
			return webSiteImage((WebSiteEntity)e);
		} else if (e instanceof WebPageEntity) {
			WebPageEntity page = (WebPageEntity)e;
			if (page.getAuthentication() != null)
				return images.get(AUTH_REQUIRED);
			return getMIMEImage(page.getContentType());
		} else if (e instanceof WebApplicationEntity) {
			return images.get(WEBAPP);
		} else if (e instanceof HTTPBasicAuthenticationEntity) {
			return images.get(AUTH);
		} else if (e instanceof WebFormAuthenticationEntity) {
			return images.get(AUTH);
		} else if (e instanceof HTTPRequestEntity) {
			return httpRequestImage((HTTPRequestEntity)e);
		}
		return null;
	}
	
	public Image decorateImage(Image image, IShadowEntity e) {
		return null;
	}

	private Image webSiteImage(WebSiteEntity e) {
		byte[] bytes = e.getFaviconBytes();
		if (bytes == null || bytes.length == 0)
			return images.get(WEBSITE);
		InputStream in = new ByteArrayInputStream(bytes);
		Display display = Display.getCurrent();
		ImageData data = new ImageData(in);
		if (data.transparentPixel > 0)
			return new Image(display, data, data.getTransparencyMask());
		return new Image(display, data);
	}
	
	private Image httpRequestImage(HTTPRequestEntity e) {
		final HTTPResponseEntity response = e.getResponse();
		if(response == null) {
			return null;
		}
		int status = response.getStatusCode();
		if (status < 200 || status >= 400)
			return images.get(HTTP_ERROR);
		if (status > 300)
			return images.get(HTTP_REDIRECT);
		return getMIMEImage(response.getNamedAttribute("Content-Type"));
	}
	
	private Image getMIMEImage(String contentType) {
		if (contentType == null)
			return null;
		if (contentType.matches("text/html.*"))
 			return images.get(HTML);
		if (contentType.matches("text/(javascript|vbscript|tcl)|application/(x-)?(javascript|perl|tcl|c?sh)"))
 			return images.get(SCRIPT);
		if (contentType.matches("((text|application)/xml|application/x-(xhtml|xml)).*"))
 			return images.get(XML);
		if (contentType.matches("text/.*"))
 			return images.get(TEXT);
		if (contentType.matches("image/.*"))
 			return images.get(IMAGE);
		if (contentType.matches("audio/.*"))
 			return images.get(AUDIO);
		if (contentType.matches("video/.*"))
 			return images.get(VIDEO);
		if (contentType.matches("application/x-(archive|arj|.?zip(-compressed)?|compress|cpio|jar|lha|lhz|rar|rpm|deb|stuffit|g?tar|shar).*" /* FIXME trailing ".*" ? */) || contentType.matches("application/java-archive"))
 			return images.get(ARCHIVE);
		if (contentType.matches("application/.*zip.*")) // FIXME duplicated?
 			return images.get(ARCHIVE);
		if (contentType.matches("application.*executable.*"))
 			return images.get(EXECUTABLE);
		if (contentType.matches("application/x-shockwave.*"))
 			return images.get(FLASH);
		if (contentType.matches("application/pdf"))
 			return images.get(PDF);
		if (contentType.matches("application/postscript"))
			return images.get(POSTSCRIPT);
		if (contentType.matches("application/msword"))
 			return images.get(MSWORD);
		if (contentType.matches("application/.*excel"))
			return images.get(SPREADSHEET);
		if (contentType.matches("application/.*powerpoint"))
			return images.get(PRESENTATION);
		if (contentType.matches("application/.*font.*"))
 			return images.get(FONT);
		if (contentType.matches("application/(rss|atom)\\+xml.*"))
 			return images.get(RSS);
		if (contentType.matches("application/octet-stream")) // application/mac-binhex\\d* ?
 			return images.get(BINARY);
		
		logger.debug("Unknown mime type \""+contentType+"\"");
		
		return null;
	}

	public void dispose() {
		images.dispose();
	}

	public Integer getSortingCategory(IShadowEntity e) {
		return null;
	}

	public Integer compare(IShadowEntity e1, IShadowEntity e2) {
		return null;
	}
	
	private ILogger logger;

	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("HTTP LabelProvider");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		logger = null;
	}
}
