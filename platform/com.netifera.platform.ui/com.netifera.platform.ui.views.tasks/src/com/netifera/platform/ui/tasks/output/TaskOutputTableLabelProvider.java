package com.netifera.platform.ui.tasks.output;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.netifera.platform.api.tasks.ITaskOutput;
import com.netifera.platform.tasks.TaskLogOutput;
import com.netifera.platform.ui.internal.tasks.TasksPlugin;

/**
 * Creates text and icons for entries in the task list.
 * 
 */
public class TaskOutputTableLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	private final static Image modelChangeImage;
	private final static Image unknownMessageImage;
	
	private final static String IMAGE_MODEL_CHANGE = "icons/logmsg.png";
	private final static String IMAGE_LOG_INFO = "icons/lognfo.png";
	private final static String IMAGE_LOG_WARNING = "icons/logwrn.png";
	private final static String IMAGE_LOG_ERROR = "icons/logerr.png";
	private final static String IMAGE_LOG_DEBUG = "icons/logdbg.png";
	private final static String IMAGE_UNKNOWN_MESSAGE = "icons/unknown.png";
	
	private final static Map<Integer, Image> imageMap;
	
	static {
		modelChangeImage = loadImage(IMAGE_MODEL_CHANGE);
		unknownMessageImage = loadImage(IMAGE_UNKNOWN_MESSAGE);
		
		imageMap = new HashMap<Integer, Image>();
        imageMap.put(TaskLogOutput.INFO,    loadImage(IMAGE_LOG_INFO));
        imageMap.put(TaskLogOutput.WARNING, loadImage(IMAGE_LOG_WARNING));
        imageMap.put(TaskLogOutput.ERROR,   loadImage(IMAGE_LOG_ERROR));
		imageMap.put(TaskLogOutput.DEBUG,   loadImage(IMAGE_LOG_DEBUG));
	}
	
	private static Image loadImage(String key) {
		return TasksPlugin.getPlugin().getImageCache().get(key);
	}
	
	/** to use to format date/time columns if any */
	// DateFormat dateFormat = DateFormat.getInstance();
//	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	/**
	 * Return a icon image (if any) for the indicated column in the table.
	 */
	public Image getColumnImage(Object element, int columnIndex) {

		if (columnIndex != 1) {
			return null;
		}

		if (element instanceof TaskLogOutput) {
		    int level = ((TaskLogOutput) element).getLogLevel();
		    if (imageMap.containsKey(level)) {
		        return imageMap.get(level);
		    }
		    return unknownMessageImage;
		}
		return modelChangeImage;
	}

	/**
	 * Return text string to display for given element in the indicated column
	 */
	public String getColumnText(Object element, int columnIndex) {

		if (!(element instanceof ITaskOutput)) {
			return "??";
		}

		/** columns: time , (i) message*/
		switch (columnIndex) {
		case 0:
			return dateFormat.format(((ITaskOutput) element).getTime());
		case 1:
			return element.toString();

		default:
			return "";
		}
	}
		
	@Override
    public void dispose() {
		super.dispose();
	}
};
