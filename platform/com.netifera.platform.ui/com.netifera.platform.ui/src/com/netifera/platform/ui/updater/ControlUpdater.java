/**
 * 
 */
package com.netifera.platform.ui.updater;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * @author kevin
 * 
 */
public abstract class ControlUpdater implements Runnable {
	/* default 128 millisecond default delay between updates */
	private static final int DEFAULT_DELAY = 128;
	private final ScheduledTask scheduledTask;
	private final Control control;
	private volatile boolean disposed;
	private Queue<Runnable> extraTasks = new ConcurrentLinkedQueue<Runnable>();

	protected static Map<Control, ControlUpdater> controlToUpdater = new HashMap<Control, ControlUpdater>();

	protected ControlUpdater(Control control) {
		this.control = control;
		scheduledTask = new ScheduledTask(this, 0, DEFAULT_DELAY);
		/* register this new updater in the global map */
		ControlUpdater.put(this);
	}

	protected static ControlUpdater get(Control control) {
		synchronized (controlToUpdater) {
			return controlToUpdater.get(control);
		}
	}

	private static void put(final ControlUpdater controlUpdater) {
		final Control control = controlUpdater.getControl();
		/* add dispose listener to the wrapped control */
		controlUpdater.addDisposeListener(new Listener() {

			public void handleEvent(Event event) {
				/* dispose the wrapper when the control is disposed */
				controlUpdater.dispose();
			}
		});
		synchronized (controlToUpdater) {
			controlToUpdater.put(control, controlUpdater);
		}
	}

	public void addDisposeListener(final Listener listener) {
		if(!control.isDisposed()) {
			control.addListener(SWT.Dispose, listener);
		}
	}

	private static void remove(Control control) {
		synchronized (controlToUpdater) {
			if (controlToUpdater.containsKey(control)) {
				controlToUpdater.remove(control);
			}
		}
	}

	/**
	 * If not already disposed, removes the updater from the scheduler and the
	 * control to updater map. The disposed flag is set.
	 */
	public void dispose() {
		/* check if already disposed, it is only optimization */
		if (disposed) {
			return;
		}
		disposed = true;
		/* remove from the viewer to updater global map */
		remove(getControl());
		/* remove the updater task from the scheduler */
		scheduledTask.cancel();
	}

	protected boolean checkDisposed() {
		if (getControl().isDisposed()) {
			dispose();
		}
		return disposed;
	}

	protected void scheduleUpdate() {
		scheduledTask.schedule();
	}

	public boolean asyncExec(Runnable task) {
		return extraTasks.offer(task);
	}

	public void run() {
		if (checkDisposed()) {
			return;
		}

		if (Display.getCurrent() == null) {
			getControl().getDisplay().syncExec(new Runnable() {
				public void run() {
					updateControl();
					while (!extraTasks.isEmpty()) {
						Runnable task = extraTasks.poll();
						task.run();
					}
				}
			});
		} else {
			updateControl();
		}
	}
	
	public final Control getControl() {
		return control;
	}
	
	public void setPeriod(int period) {
		scheduledTask.setPeriod(period);
	}

	protected abstract void updateControl();
}
