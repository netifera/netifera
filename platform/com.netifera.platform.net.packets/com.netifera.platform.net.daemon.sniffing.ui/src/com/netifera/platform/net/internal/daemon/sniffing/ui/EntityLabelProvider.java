package com.netifera.platform.net.internal.daemon.sniffing.ui;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.swt.graphics.Image;

import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.net.daemon.sniffing.model.CaptureFileEntity;
import com.netifera.platform.net.daemon.sniffing.model.NetworkInterfaceEntity;
import com.netifera.platform.net.daemon.sniffing.model.SniffingSessionEntity;
import com.netifera.platform.ui.api.model.IEntityLabelProvider;
import com.netifera.platform.ui.images.ImageCache;

public class EntityLabelProvider implements IEntityLabelProvider {
	private final static String PLUGIN_ID = "com.netifera.platform.net.daemon.sniffing.ui";
	
	private ImageCache images = new ImageCache(PLUGIN_ID);

	private static final String CAPTUREFILE = "icons/capfile.png";
	private static final String SNIFFING = "icons/sniffing.png";
	private static final String INTERFACE = "icons/interface.png";
	
	public void dispose() {
		images.dispose();
	}

	public Image getImage(IShadowEntity e) {
		if(e instanceof CaptureFileEntity) {
			return images.get(CAPTUREFILE);
		} else if(e instanceof SniffingSessionEntity) {
			return images.get(SNIFFING);
		} else if(e instanceof NetworkInterfaceEntity) {
			return images.get(INTERFACE);
		} else {
			return null;
		}
	}
	
	public Image decorateImage(Image image, IShadowEntity e) {
		return null;
	}
	
	private String dateString(long timestamp) {
		Date d = new Date(timestamp);
		return DateFormat.getInstance().format(d);
	}
	
	public String getText(IShadowEntity e) {
		if(e instanceof CaptureFileEntity) {
			final CaptureFileEntity entity = (CaptureFileEntity) e;
			return "PCAP Capture File '" + entity.getPath() + "'";
		} else if(e instanceof SniffingSessionEntity) {
			final SniffingSessionEntity entity = (SniffingSessionEntity) e;
			return "Live Sniffing Session (" + dateString(entity.getTimestamp()) + ")";
		} else if(e instanceof NetworkInterfaceEntity) {
			return ((NetworkInterfaceEntity)e).getName();
		} else {
			return null;
		}
	}
	
	public String getFullText(IShadowEntity e) {
		return getText(e);
	}

	public Integer getSortingCategory(IShadowEntity e) {
		return null;
	}

	public Integer compare(IShadowEntity e1, IShadowEntity e2) {
		if(e1 instanceof NetworkInterfaceEntity && e2 instanceof NetworkInterfaceEntity) {
			return ((NetworkInterfaceEntity)e1).getName().compareToIgnoreCase(((NetworkInterfaceEntity)e2).getName());
		}
		if(e1 instanceof SniffingSessionEntity && e2 instanceof SniffingSessionEntity) {
			return compareSniffingSessionEntities((SniffingSessionEntity)e1, (SniffingSessionEntity)e2);
		}
		if(e1 instanceof CaptureFileEntity && e2 instanceof CaptureFileEntity) {
			return compareCaptureFileEntities((CaptureFileEntity)e1, (CaptureFileEntity)e2);
		}
		return null;
	}

	private int compareCaptureFileEntities(CaptureFileEntity e1,
			CaptureFileEntity e2) {
		int res = e1.getPath().compareTo(e2.getPath());
		if (res < 0) {
			return -1;
		} else if (res > 0) {
			return 1;
		}
		res = (int) (e1.getTimestamp() - e2.getTimestamp());
		if (res < 0) {
			return -1;
		} else if (res > 0) {
			return 1;
		}
		return 0;
	}

	private int compareSniffingSessionEntities(SniffingSessionEntity e1,
			SniffingSessionEntity e2) {
		long res = e1.getTimestamp() - e2.getTimestamp();
		if (res < 0) {
			return -1;
		} else if (res > 0) {
			return 1;
		}
		return 0;
	}
}
