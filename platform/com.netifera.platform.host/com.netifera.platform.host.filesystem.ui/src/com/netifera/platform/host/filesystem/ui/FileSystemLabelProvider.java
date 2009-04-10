package com.netifera.platform.host.filesystem.ui;

import java.text.DateFormat;
import java.util.Locale;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.netifera.platform.host.filesystem.File;
import com.netifera.platform.host.internal.filesystem.ui.Activator;

public class FileSystemLabelProvider extends ColumnLabelProvider {

	private final Image directoryImage = Activator.getInstance().getImageCache().get("icons/folder.png");
	private final Image archiveImage = Activator.getInstance().getImageCache().get("icons/file-types/archive.png");
	private final Image audioImage = Activator.getInstance().getImageCache().get("icons/file-types/audio.png");
	private final Image binaryImage = Activator.getInstance().getImageCache().get("icons/file-types/binery.png");
	private final Image executableImage = Activator.getInstance().getImageCache().get("icons/file-types/executable.png");
	private final Image flashImage = Activator.getInstance().getImageCache().get("icons/file-types/flash.png");
	private final Image htmlImage = Activator.getInstance().getImageCache().get("icons/file-types/html.png");
	private final Image imageImage = Activator.getInstance().getImageCache().get("icons/file-types/image.png");
	private final Image mswordImage = Activator.getInstance().getImageCache().get("icons/file-types/msword.png");
	private final Image pdfImage = Activator.getInstance().getImageCache().get("icons/file-types/pdf.png");
	private final Image scriptImage = Activator.getInstance().getImageCache().get("icons/file-types/script.png");
	private final Image textImage = Activator.getInstance().getImageCache().get("icons/file-types/text.png");
	private final Image videoImage = Activator.getInstance().getImageCache().get("icons/file-types/video.png");
	private final Image xmlImage = Activator.getInstance().getImageCache().get("icons/file-types/xml.png");
	private final Image configImage = Activator.getInstance().getImageCache().get("icons/file-types/config.png");
	private final Image logImage = Activator.getInstance().getImageCache().get("icons/file-types/log.png");

	private int column;
	
	public FileSystemLabelProvider(int column) {
		this.column = column;
	}

	@Override
	public Image getImage(Object element) {
		/* Only return images in column 0 */
		if(column != 0) return null;
		
		if (!(element instanceof File))
			return null;
		
		File file = (File) element;
		
		if (file.isDirectory())
			return directoryImage;

		String extension = file.getName().substring(file.getName().lastIndexOf(".")+1);
		extension = extension.toLowerCase(Locale.ENGLISH);
		
		if (extension.matches("htm.*"))
 			return htmlImage;
		if (extension.matches("sh|py|pl|rb|js|vb|bat"))
 			return scriptImage;
		if (extension.matches("xml"))
 			return xmlImage;
/*		if (extension.matches("text|txt|me|faq|info|notes"))
 			return textImage;
*/		if (extension.matches("jpg|jpeg|gif|bmp|tif|tiff|png|ico|icon"))
 			return imageImage;
		if (extension.matches("mp3|wav|ra"))
 			return audioImage;
		if (extension.matches("mpg|mpeg|mov|rm|divx|xvid|avi"))
 			return videoImage;
		if (extension.matches("arj|.?zip|jar|lha|lhz|rar|rpm|deb|sit|tar|gz|gz2"))
 			return archiveImage;
		if (extension.matches("exe"))
 			return executableImage;
		if (extension.matches("swf|flv|fla"))
 			return flashImage;
		if (extension.matches("pdf"))
 			return pdfImage;
		if (extension.matches("doc"))
 			return mswordImage;
		if (extension.matches("dll|so|lib|o|obj"))
 			return binaryImage;
		if (extension.matches("init|rc|cfg|conf.*"))
			return configImage;
		if (extension.matches("log"))
			return logImage;
		
//		System.err.println("Unknown file type \""+extension+"\"");

		return textImage;
	}
	
	@Override
	public String getText(Object element) {
		if (!(element instanceof File))
			return column == 0 ? element.toString() : null;

		File file = (File) element;
		switch (column) {
			case 0: return file.getName();
			case 1: return file.isFile() ? getLengthText(file.length()) : null;
			case 2:
				if (file.lastModified() == 0) {
					return null;
				}
				/*
				 * KLUDGE: thread-safe DateFormat
				 * 
				 * creates a new instance of a Format object for each invocation
				 * (performance hit)
				 */
				return DateFormat.getInstance().format(file.lastModified());
		}
		return null;
	}
	
	private String getLengthText(long length) {
		if (length < 1024) return length+" bytes";
		if (length < 1024*1024) return (length/1024)+" KB";
		if (length < 1024*1024*1024) return (length/1024/1024)+" MB";
		if (length < 1024*1024*1024*1024) return (length/1024/1024/1024)+" GB";
		return length+" bytes";
	}

	@Override
	public String getToolTipText(Object element) {
		if (element instanceof File)
			return ((File)element).getAbsolutePath();
		return null;
	}

	@Override
	public void dispose() {
		super.dispose();
//		images.dispose();
	}
}
