package com.netifera.platform.ui.util;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Used to to display information control hovers. Tracks mouse events to show
 * and hide the hover control.
 */

public abstract class MouseTracker extends ShellAdapter implements MouseTrackListener,
		MouseMoveListener, DisposeListener , KeyListener {
	/**
	 * Margin around the original hover event location for computing the hover
	 * area.
	 */
	protected final static int EPSILON = 3;

	/** The area in which the original hover event occurred. */

	private Rectangle hoverArea;

	/**
	 * subjectArea the area relative to the subject control inside which the
	 * presented information is valid
	 */
	private Rectangle subjectArea;

	/**
	 * keepUpArea if the mouse if moved inside the keepUpArea the hover is not
	 * closed
	 */
	private Rectangle keepUpArea;

	private Control subjectControl;

	/* isComputing the information to be displayed is being computed */
	private boolean isComputing;

	/* ignoreShowEvents if set the hover events are ignored */
	private boolean ignoreShowEvents;

	/* the mouse moved outside the subject area while computing */
	private boolean mouseLostWhileComputing;

	/* the subject control shell was deactivated while computing */
	private boolean shellDeactivatedWhileComputing;

	private MouseEvent lastHoverEvent;

	private boolean expandAreaToIncludePointer;

	public MouseTracker(Control subjectControl) {
		start(subjectControl);
	}

	/**
	 * Starts tracking mouse events in the given control
	 * 
	 * @param subjectControl
	 *            the control where the hover will be active
	 */
	private void start(Control subjectControl) {

		this.subjectControl = subjectControl;

		if (subjectControl != null && !subjectControl.isDisposed()) {
			subjectControl.addMouseTrackListener(this);
			subjectControl.addDisposeListener(this);
			subjectControl.addKeyListener(this);
		}

		ignoreShowEvents = false;
		isComputing = false;
		mouseLostWhileComputing = false;
		shellDeactivatedWhileComputing = false;
	}

	public void stop() {
		if (subjectControl != null && !subjectControl.isDisposed()) {
			subjectControl.removeMouseTrackListener(this);
			subjectControl.removeMouseMoveListener(this);
			subjectControl.getShell().removeShellListener(this);
			subjectControl = null;
		}
	}

	/**
	 * @param subjectArea
	 *            rectangle containing the item for which the information being
	 *            shown has relevance
	 */
	private void setSubjectArea(Rectangle subjectArea) {
		this.subjectArea = subjectArea;
	}

	/**
	 * @param hoverArea
	 *            the rectangle containing the information control
	 */
	protected void setHoverArea(Rectangle hoverArea) {
		this.hoverArea = hoverArea;
		keepUpArea = new Rectangle(Math.max(hoverArea.x - EPSILON * 3, 0), Math
				.max(hoverArea.y - EPSILON * 3, 0), hoverArea.width + EPSILON
				* 3 * 2, hoverArea.height + EPSILON * 3);
	}

	/**
	 * @return lastHoverEvent the event object of the last handled hover event
	 */
	private MouseEvent getLastHoverEvent() {
		return lastHoverEvent;
	}

	private boolean canMoveOverHoverControl() {
		return true;
	}

	/**
	 * Determines whether the computed information is still useful for
	 * presentation. This is not the case, if the shell of the subject control
	 * has been deactivated, the mouse left the subject control, or the mouse
	 * moved on, so that it is no longer in the subject area.
	 * 
	 * @return <code>true</code> if information is still useful for
	 *         presentation, <code>false</code> otherwise
	 */
	private boolean isComputedInformationStillValid() {

		if (mouseLostWhileComputing || shellDeactivatedWhileComputing) {
			return true;
		}

		if (subjectControl != null && !subjectControl.isDisposed()) {
			Point cursorLocation = subjectControl.getDisplay()
					.getCursorLocation();
			cursorLocation = subjectControl.toControl(cursorLocation);

			if (!subjectArea.contains(cursorLocation)
					&& !hoverArea.contains(cursorLocation)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Tests whether a given mouse location is within the keep-up area The hover
	 * should not be hidden as long as the mouse stays inside this area.
	 */
	private boolean inKeepUpArea(int x, int y) {
		Rectangle informationControlArea = getInformationControlArea();

		if (informationControlArea != null) {
			setHoverArea(informationControlArea);
		}
		Point pointInScreen = subjectControl.toDisplay(x, y);
		if (keepUpArea != null && keepUpArea.contains(pointInScreen)) {
			return true;
		}
		return false;
	}

	/**
	 * hides the information control and disables the mouse tracking
	 */
	protected void deactivate() {

		if (isComputing) {
			return;
		}
		hideInformationControl();

		ignoreShowEvents = false;

		if (subjectControl != null && !subjectControl.isDisposed()) {
			subjectControl.removeMouseMoveListener(this);
			subjectControl.getShell().removeShellListener(this);
		}
	}

	/**
	 * the information to be displayed has been computed
	 */
	protected void computationCompleted() {
		isComputing = false;
		mouseLostWhileComputing = false;
		shellDeactivatedWhileComputing = false;
	}

	/** UI event handlers */

	public void mouseEnter(MouseEvent e) {
	}

	/**
	 * Executes when the mouse pointer moves over the subject control but
	 * without hovering.
	 * 
	 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events
	 *      .MouseEvent)
	 */
	public void mouseMove(MouseEvent e) {
		if (!(subjectArea.contains(e.x, e.y) || inKeepUpArea(e.x, e.y))) {
			deactivate();
		}
	}

	public void mouseExit(MouseEvent e) {
		// TODO
	}

	/**
	 * Executes when the mouse pointer hovers (stops moving for an operating
	 * system specific period of time) over the subject control
	 * 
	 * @see org.eclipse.swt.events.MouseTrackListener#mouseHover(org.eclipse.swt.
	 *      events.MouseEvent)
	 */
	public void mouseHover(MouseEvent e) {
		lastHoverEvent = e;
		showEventAt(new Point(e.x,e.y));
	}
	
	protected void showEventAt(final Point eventPointInControl) {

		if (isComputing || ignoreShowEvents) {
			return;
		}

		if (subjectControl == null || subjectControl.isDisposed()) {
			return;
		}

		Shell activeShell = subjectControl.getShell().getDisplay()
				.getActiveShell();

		if (subjectControl.getShell() != activeShell) {
			return;
		}

		Point eventPointScreen = subjectControl.toDisplay(eventPointInControl);

		calculateAreas(eventPointInControl);

		/*
		 * if after the areas are calculated the triggering point is outside the
		 * subjectArea then the hover is not shown
		 */
		if (!subjectArea.contains(eventPointInControl)) {
			return;
		}

		isComputing = true;
		ignoreShowEvents = true;

		mouseLostWhileComputing = false;
		shellDeactivatedWhileComputing = false;

		/*
		 * add listeners to the subject control to know when to hide the hover
		 * control
		 */

		subjectControl.addMouseMoveListener(this);
		activeShell.addShellListener(this);

		/* ready to show the information control */
		Object item = getItemAt(eventPointInControl);
		if (item != null) {
			try {
				showInformationControl(activeShell, eventPointScreen,
						getInput(), item);
			} catch (IllegalArgumentException exception) {
				ignoreShowEvents = false;
			}
		} else {
			ignoreShowEvents = false;
		}
		computationCompleted();
	}

	/**
	 * Calculates the hover and subject area
	 * 
	 * @param pointInControl
	 *            coordinates relative to the subject control where the hover
	 *            event happened.
	 */
	private void calculateAreas(Point pointInControl) {

		/* very small default hover area */
		Rectangle hoverArea = new Rectangle(pointInControl.x - EPSILON,
				pointInControl.y - EPSILON, 2 * EPSILON, 2 * EPSILON);

		hoverArea.y = Math.max(hoverArea.y, 0);
		hoverArea.x = Math.max(hoverArea.x, 0);

		Rectangle itemArea = getAreaOfItemAt(pointInControl);

		if (itemArea == null) {
			return;
		}

		/*
		 * if for some reason the point that triggered the hover is not inside
		 * the rectangle, the rectangle is expanded.
		 */

		if (!itemArea.contains(pointInControl) && expandAreaToIncludePointer) {
			itemArea.add(hoverArea);
		}

		setSubjectArea(itemArea);
		setHoverArea(hoverArea);

	}

	/** ShellListener methods */

	public void shellIconified(ShellEvent e) {
		shellDeactivatedWhileComputing = true;
		deactivate();
	}

	public void shellActivated(ShellEvent e) {
		if (ignoreShowEvents) {
			e.doit = false;
		}
	}

	public void shellDeactivated(ShellEvent e) {
		shellDeactivatedWhileComputing = true;
		ignoreShowEvents = false;
		/* we could remove the previous line and call deactivate after checking that
		 * the hover is not the the shell that got focus */
		// deactivate();
	}

	public void widgetDisposed(DisposeEvent e) {
		stop();
	}
	
	/** KeyListener methods */
	public void keyPressed(KeyEvent e) {
		if (e.character == ' ') {
			Rectangle itemArea = getAreaOfSelectedItem();
			if(itemArea != null) {
				Point eventPoint = new Point(itemArea.x,itemArea.y);
				showEventAt(eventPoint);
			}
		}
		else if (e.character == SWT.ESC) {
			hideInformationControl();
			ignoreShowEvents = false;
		}
		else {
			if(!isComputing) {
				e.doit = !focusInformationControl();
			}
		}
	}
	
	public void keyReleased(KeyEvent e) {
		
	}
	
	/** the following methods belong to the subject control interface */

	/**
	 * @param point
	 *            relative to the subject control
	 * @return Object whose data is represented by the item at the specified
	 *         point
	 */
	protected abstract Object getItemAt(Point point);

	/**
	 * @param point
	 *            relative to the subject control
	 * @return subject area rectangle relative to the subject control
	 */
	protected Rectangle getAreaOfItemAt(Point point) {
		return new Rectangle(point.x - EPSILON, point.y - EPSILON, EPSILON * 2,
				EPSILON * 2);
	}

	/**
	 * @return subject area rectangle relative to the subject control if any
	 *         item is selected, null otherwise.
	 */
	protected Rectangle getAreaOfSelectedItem() {
		return null;
	}

	/**
	 * @return input Object to which the subject item belongs
	 */
	public Object getInput() {
		return subjectControl.getData();
	}

	/** to show information controls the following methods should be implemented */

	protected abstract void showInformationControl(Shell parent, Point location,
			Object input, Object item);

	protected abstract void hideInformationControl();
	
	/**
	 * @return true if the control got focus, and false if was unable to
	 */
	protected abstract boolean focusInformationControl();
	
	protected abstract Rectangle getInformationControlArea();
}