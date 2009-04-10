package com.netifera.platform.ui.forms;


import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ILayoutExtension;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.SizeCache;
import org.eclipse.ui.internal.forms.widgets.*;

/**
 * Netifera custom ExpandableComposite with optional image label and some other
 * minor modifications. 
 */
public class NetiferaExpand extends Canvas {
	/**
	 * If this style is used, a twistie will be used to render the expansion
	 * toggle.
	 */
	public static final int TWISTIE = 1 << 1;

	/**
	 * If this style is used, the title text will be rendered as a hyperlink
	 * that can individually accept focus. Otherwise, it will still act like a
	 * hyperlink, but only the toggle control will accept focus.
	 */
	public static final int FOCUS_TITLE = 1 << 3;

	/**
	 * If this style is used, the client origin will be vertically aligned with
	 * the title text. Otherwise, it will start at x = 0.
	 */
	public static final int CLIENT_INDENT = 1 << 4;

	/**
	 * If this style is used, computed size of the composite will take the
	 * client width into consideration only in the expanded state. Otherwise,
	 * client width will always be taken into acount.
	 */
	public static final int COMPACT = 1 << 5;

	/**
	 * If this style is used, the control will be created in the expanded state.
	 * This state can later be changed programmatically or by the user if
	 * TWISTIE or TREE_NODE style is used.
	 */
	public static final int EXPANDED = 1 << 6;

	/**
	 * If this style is used, title bar decoration will be painted behind the
	 * text.
	 */
	public static final int TITLE_BAR = 1 << 8;

	/**
	 * If this style is used, a short version of the title bar decoration will
	 * be painted behind the text. This style is useful when a more descrete
	 * option is needed for the title bar.
	 * 
	 * @since 3.1
	 */
	public static final int SHORT_TITLE_BAR = 1 << 9;

	/**
	 * If this style is used, title will not be rendered.
	 */
	public static final int NO_TITLE = 1 << 12;

	/**
	 * By default, text client is right-aligned. If this style is used, it will
	 * be positioned after the text control and vertically centered with it.
	 */
	public static final int LEFT_TEXT_CLIENT_ALIGNMENT = 1 << 13;

	/**
	 * Width of the margin that will be added around the control (default is 0).
	 */
	public int marginWidth = 0;

	/**
	 * Height of the margin that will be added around the control (default is
	 * 0).
	 */
	public int marginHeight = 0;

	/**
	 * Vertical spacing between the title area and the composite client control
	 * (default is 3).
	 */
	public int clientVerticalSpacing = 3;

	/**
	 * Vertical spacing between the title area and the description control
	 * (default is 0). The description control is normally placed at the new
	 * line as defined in the font used to render it. This value will be added
	 * to it.
	 * 
	 * @since 3.3
	 */
	public int descriptionVerticalSpacing = 0;

	/**
	 * Horizontal margin around the inside of the title bar area when TITLE_BAR
	 * or SHORT_TITLE_BAR style is used. This variable is not used otherwise.
	 * 
	 * @since 3.3
	 */
	public int titleBarTextMarginWidth = 6;

	/**
	 * The toggle widget used to expand the composite.
	 */
	protected NetiferaTwistie toggle;

	/**
	 * The text label for the title.
	 */
	protected Control textLabel;
	
	/**
	 * The image label for the title.
	 */
	protected Control imageLabel;

	/**
	 * @deprecated this variable was left as protected by mistake. It will be
	 *             turned into static and hidden in the future versions. Do not
	 *             use them and do not change its value.
	 */
	protected int VGAP = 3;
	/**
	 * @deprecated this variable was left as protected by mistake. It will be
	 *             turned into static and hidden in the future versions. Do not
	 *             use it and do not change its value.
	 */
	protected int GAP = 4;

	static final int IGAP = 4;
	static final int IVGAP = 3;

	private static final Point NULL_SIZE = new Point(0, 0);

	private static final int VSPACE = 3;

	private static final int SEPARATOR_HEIGHT = 2;

	private int expansionStyle = TWISTIE | FOCUS_TITLE | EXPANDED;

	private boolean expanded;

	private Control textClient;

	private Control client;

	private ListenerList listeners = new ListenerList();

	private Color titleBarForeground;

	@SuppressWarnings("restriction")
	private class ExpandableLayout extends Layout implements ILayoutExtension {

		private SizeCache toggleCache = new SizeCache();

		private SizeCache textClientCache = new SizeCache();

		private SizeCache textLabelCache = new SizeCache();
		
		private SizeCache imageLabelCache = new SizeCache();

		private SizeCache descriptionCache = new SizeCache();

		private SizeCache clientCache = new SizeCache();

		private void initCache(boolean shouldFlush) {
			toggleCache.setControl(toggle);
			textClientCache.setControl(textClient);
			textLabelCache.setControl(textLabel);
			imageLabelCache.setControl(imageLabel);
			descriptionCache.setControl(getDescriptionControl());
			clientCache.setControl(client);

			if (shouldFlush) {
				toggleCache.flush();
				textClientCache.flush();
				textLabelCache.flush();
				imageLabelCache.flush();
				descriptionCache.flush();
				clientCache.flush();
			}
		}

		protected void layout(Composite parent, boolean changed) {
			initCache(changed);

			Rectangle clientArea = parent.getClientArea();
			int thmargin = 0;
			int tvmargin = 0;

			if (hasTitleBar()) {
				thmargin = titleBarTextMarginWidth;
				tvmargin = IVGAP;
			}
			int x = marginWidth + thmargin;
			int y = marginHeight + tvmargin;
			Point toggleSize = NULL_SIZE;
			Point textClientSize = NULL_SIZE;
			if (toggle != null)
				toggleSize = toggleCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			int totalWidth = clientArea.width - marginWidth - marginWidth
					- thmargin - thmargin;
			if (toggleSize.x > 0)
				totalWidth -= toggleSize.x + IGAP;
			if (textClient != null)
				textClientSize = textClientCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			if (textClientSize.x > 0)
				totalWidth -= textClientSize.x + IGAP;
			Point textLabelSize = NULL_SIZE;
			/* compute size of textLabel */
			if (textLabel != null)
				textLabelSize = textLabelCache.computeSize(totalWidth, SWT.DEFAULT);
			if (textLabel instanceof Label) {
				Point defSize = textLabelCache.computeSize(SWT.DEFAULT,
						SWT.DEFAULT);
				if (defSize.y == textLabelSize.y) {
					// One line - pick the smaller of the two widths
					textLabelSize.x = Math.min(defSize.x, textLabelSize.x);
				}
			}
			Point imageLabelSize = NULL_SIZE;
			if(imageLabel != null)
				imageLabelSize = imageLabelCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			if (toggle != null) {
				GC gc = new GC(NetiferaExpand.this);
				gc.setFont(getFont());
				FontMetrics fm = gc.getFontMetrics();
				int textHeight = fm.getHeight();
				gc.dispose();
				if (textClient != null
						&& (expansionStyle & LEFT_TEXT_CLIENT_ALIGNMENT) != 0) {
					textHeight = Math.max(textHeight, textClientSize.y);
				}
				int toggleY = textHeight / 2 - toggleSize.y / 2 + 1;
				toggleY = Math.max(toggleY, 0);
				toggleY += marginHeight + tvmargin;
				toggle.setLocation(x, toggleY);
				toggle.setSize(toggleSize);
				x += toggleSize.x + IGAP;
			}
			if (textLabel != null) {
				int ty = y;
				
				if (textClient != null
						&& (expansionStyle & LEFT_TEXT_CLIENT_ALIGNMENT) != 0) {
					/*if textClient is set and aligned to the left. textLabel is center aligned */
					if (textLabelSize.y < textClientSize.y)
						ty = textClientSize.y / 2 - textLabelSize.y / 2 + marginHeight
								+ tvmargin;
				}
				/* set the bounds of the textLabel */
				textLabelCache.setBounds(x+imageLabelSize.x + IGAP, ty, textLabelSize.x, textLabelSize.y);
				/* set imageLabel bounds here */
				if(imageLabel != null) {
					imageLabelCache.setBounds(x,ty,imageLabelSize.x,imageLabelSize.y);
				}
			}
			if (textClient != null) {
				int tcx;
				if ((expansionStyle & LEFT_TEXT_CLIENT_ALIGNMENT) != 0) {
					/* to the left of the text and image */
					tcx = x + imageLabelSize.x + textLabelSize.x + IGAP + GAP;
				} else {
					/* if the textClient goes to the right end of the title*/
					tcx = clientArea.width - textClientSize.x - marginWidth - thmargin;
				}
				/*set the bound of the textClient */
				textClientCache.setBounds(tcx, y, textClientSize.x, textClientSize.y);
			}
			int tbarHeight = 0;
			if (textLabelSize.y > 0)
				tbarHeight = textLabelSize.y;
			if (textClientSize.y > 0)
				tbarHeight = Math.max(tbarHeight, textClientSize.y);
			y += tbarHeight;
			if (hasTitleBar())
				y += tvmargin;
			if (getSeparatorControl() != null) {
				y += VSPACE;
				getSeparatorControl().setBounds(marginWidth, y,
						clientArea.width - marginWidth - marginWidth,
						SEPARATOR_HEIGHT);
				y += SEPARATOR_HEIGHT;
				if (expanded)
					y += VSPACE;
			}
			if (expanded) {
				int areaWidth = clientArea.width - marginWidth - marginWidth
						- thmargin - thmargin;
				int cx = marginWidth + thmargin;
				if ((expansionStyle & CLIENT_INDENT) != 0) {
					cx = x;
					areaWidth -= x;
				}
				if (client != null) {
					Point dsize = null;
					Control desc = getDescriptionControl();
					if (desc != null) {
						dsize = descriptionCache.computeSize(areaWidth,
								SWT.DEFAULT);
						y += descriptionVerticalSpacing;
						descriptionCache.setBounds(cx, y, areaWidth, dsize.y);
						y += dsize.y + clientVerticalSpacing;
					} else {
						y += clientVerticalSpacing;
						if (getSeparatorControl() != null)
							y -= VSPACE;
					}
					int cwidth = areaWidth;
					int cheight = clientArea.height - marginHeight
							- marginHeight - y;
					clientCache.setBounds(cx, y, cwidth, cheight);
				}
			}
		}

		protected Point computeSize(Composite parent, int wHint, int hHint,
				boolean changed) {
			initCache(changed);

			int width = 0, height = 0;
			Point tsize = NULL_SIZE;
			int twidth = 0;
			if (toggle != null) {
				tsize = toggleCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				twidth = tsize.x + IGAP;
			}
			int thmargin = 0;
			int tvmargin = 0;

			if (hasTitleBar()) {
				thmargin = titleBarTextMarginWidth;
				tvmargin = IVGAP;
			}
			int innerwHint = wHint;
			if (innerwHint != SWT.DEFAULT)
				innerwHint -= twidth + marginWidth + marginWidth + thmargin
						+ thmargin;

			int innertHint = innerwHint;

			Point tcsize = NULL_SIZE;
			if (textClient != null) {
				tcsize = textClientCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				if (innertHint != SWT.DEFAULT)
					innertHint -= IGAP + tcsize.x;
			}
			Point size = NULL_SIZE;

			if (textLabel != null)
				size = textLabelCache.computeSize(innertHint, SWT.DEFAULT);
			if (textLabel instanceof Label) {
				Point defSize = textLabelCache.computeSize(SWT.DEFAULT,
						SWT.DEFAULT);
				if (defSize.y == size.y) {
					// One line - pick the smaller of the two widths
					size.x = Math.min(defSize.x, size.x);
				}
			}
			/* add the width of the image */
			if(imageLabel != null) {
				Point defSize = imageLabelCache.computeSize(SWT.DEFAULT,SWT.DEFAULT);
				if(defSize.x > 0)
					width += defSize.x + IGAP;
			}
			
			if (size.x > 0)
				width = size.x;
			if (tcsize.x > 0)
				width += IGAP + tcsize.x;
			height = tcsize.y > 0 ? Math.max(tcsize.y, size.y) : size.y;
			if (getSeparatorControl() != null) {
				height += VSPACE + SEPARATOR_HEIGHT;
				if (expanded && client != null)
					height += VSPACE;
			}
			// if (hasTitleBar())
			// height += VSPACE;
			if ((expanded || (expansionStyle & COMPACT) == 0) && client != null) {
				int cwHint = wHint;

				if (cwHint != SWT.DEFAULT) {
					cwHint -= marginWidth + marginWidth + thmargin + thmargin;
					if ((expansionStyle & CLIENT_INDENT) != 0)
						if (tcsize.x > 0)
							cwHint -= twidth;
				}
				Point dsize = null;
				Point csize = clientCache.computeSize(FormUtil.getWidthHint(
						cwHint, client), SWT.DEFAULT);
				if (getDescriptionControl() != null) {
					int dwHint = cwHint;
					if (dwHint == SWT.DEFAULT) {
						dwHint = csize.x;
						if ((expansionStyle & CLIENT_INDENT) != 0)
							dwHint -= twidth;
					}
					dsize = descriptionCache.computeSize(dwHint, SWT.DEFAULT);
				}
				if (dsize != null) {
					width = Math.max(width, dsize.x);
					if (expanded)
						height += descriptionVerticalSpacing + dsize.y
								+ clientVerticalSpacing;
				} else {
					height += clientVerticalSpacing;
					if (getSeparatorControl() != null)
						height -= VSPACE;
				}
				width = Math.max(width, csize.x);
				if (expanded)
					height += csize.y;
			}
			if (toggle != null) {
				height = height - size.y + Math.max(size.y, tsize.y);
				width += twidth;
			}

			Point result = new Point(width + marginWidth + marginWidth
					+ thmargin + thmargin, height + marginHeight + marginHeight
					+ tvmargin + tvmargin);
			return result;
		}

		public int computeMinimumWidth(Composite parent, boolean changed) {
			return computeSize(parent, 0, SWT.DEFAULT, changed).x;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.forms.parts.ILayoutExtension#computeMinimumWidth(org.eclipse.swt.widgets.Composite,
		 *      boolean)
		 */
		public int computeMaximumWidth(Composite parent, boolean changed) {
			return computeSize(parent, SWT.DEFAULT, SWT.DEFAULT, changed).x;
		}
	}

	/**
	 * Creates an expandable composite using a TWISTIE toggle.
	 * 
	 * @param parent
	 *            the parent composite
	 * @param style
	 *            SWT style bits
	 */
	public NetiferaExpand(Composite parent, int style) {
		this(parent, style, TWISTIE);
	}

	/**
	 * Creates the expandable composite in the provided parent.
	 * 
	 * @param parent
	 *            the parent
	 * @param style
	 *            the control style (as expected by SWT subclass)
	 * @param expansionStyle
	 *            the style of the expansion widget (TREE_NODE, TWISTIE,
	 *            CLIENT_INDENT, COMPACT, FOCUS_TITLE,
	 *            LEFT_TEXT_CLIENT_ALIGNMENT, NO_TITLE)
	 */
	@SuppressWarnings("restriction")
	public NetiferaExpand(Composite parent, int style, int expansionStyle) {
		super(parent, style);
		this.expansionStyle = expansionStyle;
		if ((expansionStyle & TITLE_BAR) != 0)
			setBackgroundMode(SWT.INHERIT_DEFAULT);
		super.setLayout(new ExpandableLayout());
		if (hasTitleBar()) {
			this.addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) {
					onPaint(e);
				}
			});
		}
		if ((expansionStyle & TWISTIE) != 0)
			toggle = new NetiferaTwistie(this, SWT.NULL);
		else
			expanded = true;
		if ((expansionStyle & EXPANDED) != 0)
			expanded = true;
		if (toggle != null) {
			toggle.setExpanded(expanded);
			toggle.addHyperlinkListener(new HyperlinkAdapter() {
				public void linkActivated(HyperlinkEvent e) {
					toggleState();
				}
			});
			toggle.addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) {
					if (textLabel instanceof Label && !isFixedStyle())
						textLabel.setForeground(toggle.isHover() ? toggle
								.getHoverDecorationColor()
								: getTitleBarForeground());
				}
			});
			toggle.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if (e.keyCode == SWT.ARROW_UP) {
						verticalMove(false);
						e.doit = false;
					} else if (e.keyCode == SWT.ARROW_DOWN) {
						verticalMove(true);
						e.doit = false;
					}
				}
			});
			if ((getExpansionStyle()&FOCUS_TITLE)==0) {
				//XXX cant access paintFocus :(
				//toggle.paintFocus=false;
				toggle.addFocusListener(new FocusListener() {

					public void focusGained(FocusEvent e) {
						//Zexpand forwards the focus to the parent
						toggle.getParent().notifyListeners(SWT.FocusIn, new Event());
						textLabel.redraw();
						if(imageLabel != null)
							imageLabel.redraw();
					}

					public void focusLost(FocusEvent e) {
						toggle.getParent().notifyListeners(SWT.FocusOut, new Event());
						textLabel.redraw();
						if(imageLabel != null)
							imageLabel.redraw();
					}
				});
			}
		}
		if ((expansionStyle & FOCUS_TITLE) != 0) {
			Hyperlink link = new Hyperlink(this, SWT.WRAP);
			link.addHyperlinkListener(new HyperlinkAdapter() {
				public void linkActivated(HyperlinkEvent e) {
					programmaticToggleState();
				}
			});
			textLabel = link;
		} else if ((expansionStyle & NO_TITLE) == 0) {
			/* create the image label control */
			final Label imagelabel = new Label(this,SWT.WRAP);
			final Label label = new Label(this, SWT.WRAP);
			if (!isFixedStyle()) {
				label.setCursor(FormsResources.getHandCursor());
				imagelabel.setCursor(FormsResources.getHandCursor());
				Listener listener = new Listener() {
					public void handleEvent(Event e) {
						switch (e.type) {
						case SWT.MouseDown:
							if (toggle != null)
								toggle.setFocus();
							break;
						case SWT.MouseUp:
							label.setCursor(FormsResources.getBusyCursor());
							programmaticToggleState();
							label.setCursor(FormsResources.getHandCursor());
							break;
						case SWT.MouseEnter:
							if (toggle != null) {
								label.setForeground(toggle.getHoverDecorationColor());
								toggle.setHover(true);
								toggle.redraw();
							}
							break;
						case SWT.MouseExit:
							if (toggle != null) {
								label.setForeground(getTitleBarForeground());
								toggle.setHover(false);
								toggle.redraw();
							}
							break;
						case SWT.Paint:
							if (toggle != null && e.widget == textLabel) {
								paintTitleFocus(e.gc);
							}
							break;
						}
					}
				};
				label.addListener(SWT.MouseDown, listener);
				label.addListener(SWT.MouseUp, listener);
				label.addListener(SWT.MouseEnter, listener);
				label.addListener(SWT.MouseExit, listener);
				label.addListener(SWT.Paint, listener);
				imagelabel.addListener(SWT.MouseDown, listener);
				imagelabel.addListener(SWT.MouseUp, listener);
				imagelabel.addListener(SWT.MouseEnter, listener);
				imagelabel.addListener(SWT.MouseExit, listener);
				imagelabel.addListener(SWT.Paint, listener);
			}
			textLabel = label;
			imageLabel = imagelabel;
			
		}
		if (textLabel != null) {
			textLabel.setMenu(getMenu());
			textLabel.addTraverseListener(new TraverseListener() {
				public void keyTraversed(TraverseEvent e) {
					if (e.detail == SWT.TRAVERSE_MNEMONIC) {
						// steal the mnemonic
						if (!isVisible() || !isEnabled())
							return;
						if (FormUtil.mnemonicMatch(getText(), e.character)) {
							e.doit = false;
							programmaticToggleState();
							setFocus();
						}
					}
				}
			});
		}
		if (imageLabel != null) {
			imageLabel.setMenu(getMenu());
		}
	}

	/**
	 * Overrides 'super' to pass the menu to the text label.
	 * 
	 * @param menu
	 *            the menu from the parent to attach to this control.
	 */

	public void setMenu(Menu menu) {
		if (textLabel != null)
			textLabel.setMenu(menu);
		super.setMenu(menu);
	}

	/**
	 * Prevents assignment of the layout manager - expandable composite uses its
	 * own layout.
	 */
	public final void setLayout(Layout layout) {
	}

	/**
	 * Sets the background of all the custom controls in the expandable.
	 */
	public void setBackground(Color bg) {
		super.setBackground(bg);
		if ((getExpansionStyle() & TITLE_BAR) == 0) {
			if (textLabel != null)
				textLabel.setBackground(bg);
			if (toggle != null)
				toggle.setBackground(bg);
		}
	}

	/**
	 * Sets the foreground of all the custom controls in the expandable.
	 */
	public void setForeground(Color fg) {
		super.setForeground(fg);
		if (textLabel != null)
			textLabel.setForeground(fg);
		if (toggle != null)
			toggle.setForeground(fg);
	}

	/**
	 * Sets the color of the toggle control.
	 * 
	 * @param c
	 *            the color object
	 */
	public void setToggleColor(Color c) {
		if (toggle != null)
			toggle.setDecorationColor(c);
	}

	/**
	 * Sets the active color of the toggle control (when the mouse enters the
	 * toggle area).
	 * 
	 * @param c
	 *            the active color object
	 */
	public void setActiveToggleColor(Color c) {
		if (toggle != null)
			toggle.setHoverDecorationColor(c);
	}

	/**
	 * Sets the fonts of all the custom controls in the expandable.
	 */
	public void setFont(Font font) {
		super.setFont(font);
		if (textLabel != null)
			textLabel.setFont(font);
		if(imageLabel != null)
			imageLabel.setFont(font);
		if (toggle != null)
			toggle.setFont(font);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */

	public void setEnabled(boolean enabled) {
		if (textLabel != null)
			textLabel.setEnabled(enabled);
		if(imageLabel != null) {
			imageLabel.setEnabled(enabled);
		}
		if (toggle != null)
			toggle.setEnabled(enabled);
		super.setEnabled(enabled);
	}

	/**
	 * Sets the client of this expandable composite. The client must not be
	 * <samp>null </samp> and must be a direct child of this container.
	 * 
	 * @param client
	 *            the client that will be expanded or collapsed
	 */
	public void setClient(Control client) {
		Assert.isTrue(client != null && client.getParent().equals(this));
		this.client = client;
		/* forward focus events to this parent composite */
		final Composite parent = this;
		FocusListener forwardFocusListener = new FocusListener() {
			final Event event = new Event();
			public void focusGained(FocusEvent e) {
				event.widget = parent;				
				parent.notifyListeners(SWT.FocusIn, event);
			}

			public void focusLost(FocusEvent e) {
				event.widget = parent;
				parent.notifyListeners(SWT.FocusOut, event);
			}
		};
		client.addFocusListener(forwardFocusListener);
		if(client instanceof Composite) {
			Composite clientComposite = (Composite)client;
		
		for(Control child : clientComposite.getChildren()) {
			child.addFocusListener(forwardFocusListener);
		}
		}
	}

	/**
	 * Returns the current expandable client.
	 * 
	 * @return the client control
	 */
	public Control getClient() {
		return client;
	}

	/**
	 * Sets the title of the expandable composite. The title will act as a
	 * hyperlink and activating it will toggle the client between expanded and
	 * collapsed state.
	 * 
	 * @param title
	 *            the new title string
	 * @see #getText()
	 */
	public void setText(String title) {
		if (textLabel instanceof Label)
			((Label) textLabel).setText(title);
		else if (textLabel instanceof Hyperlink)
			((Hyperlink) textLabel).setText(title);
	}
	/*Zexpand setImage*/
	public void setImage(Image image) {
		((Label) imageLabel).setImage(image);
	}
	/**
	 * Returns the title string.
	 * 
	 * @return the title string
	 * @see #setText(String)
	 */
	public String getText() {
		if (textLabel instanceof Label)
			return ((Label) textLabel).getText();
		else if (textLabel instanceof Hyperlink)
			return ((Hyperlink) textLabel).getText();
		else
			return ""; //$NON-NLS-1$
	}

	/**
	 * Tests the expanded state of the composite.
	 * 
	 * @return <samp>true </samp> if expanded, <samp>false </samp> if collapsed.
	 */
	public boolean isExpanded() {
		return expanded;
	}

	/**
	 * Returns the bitwise-ORed style bits for the expansion control.
	 * 
	 * @return the bitwise-ORed style bits for the expansion control
	 */
	public int getExpansionStyle() {
		return expansionStyle;
	}

	/**
	 * Programmatically changes expanded state.
	 * 
	 * @param expanded
	 *            the new expanded state
	 */
	public void setExpanded(boolean expanded) {
		internalSetExpanded(expanded);
		if (toggle != null)
			toggle.setExpanded(expanded);
	}

	/**
	 * Performs the expansion state change for the expandable control.
	 * 
	 * @param expanded
	 *            the expansion state
	 */
	protected void internalSetExpanded(boolean expanded) {
		if (this.expanded != expanded) {
			this.expanded = expanded;
			if (getDescriptionControl() != null)
				getDescriptionControl().setVisible(expanded);
			if (client != null)
				client.setVisible(expanded);
			layout();
		}
		reflow();
	}

	/**
	 * Adds the listener that will be notified when the expansion state changes.
	 * 
	 * @param listener
	 *            the listener to add
	 */
	public void addExpansionListener(IExpansionListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes the expansion listener.
	 * 
	 * @param listener
	 *            the listner to remove
	 */
	public void removeExpansionListener(IExpansionListener listener) {
		listeners.remove(listener);
	}

	/**
	 * If TITLE_BAR or SHORT_TITLE_BAR style is used, title bar decoration will
	 * be painted behind the text in this method. The default implementation
	 * does nothing - subclasses are responsible for rendering the title area.
	 * 
	 * @param e
	 *            the paint event
	 */
	protected void onPaint(PaintEvent e) {
	}

	/**
	 * Returns description control that will be placed under the title if
	 * present.
	 * 
	 * @return the description control or <samp>null </samp> if not used.
	 */
	protected Control getDescriptionControl() {
		return null;
	}

	/**
	 * Returns the separator control that will be placed between the title and
	 * the description if present.
	 * 
	 * @return the separator control or <samp>null </samp> if not used.
	 */
	protected Control getSeparatorControl() {
		return null;
	}

	/**
	 * Computes the size of the expandable composite.
	 * 
	 * @see org.eclipse.swt.widgets.Composite#computeSize
	 */
	public Point computeSize(int wHint, int hHint, boolean changed) {
		checkWidget();
		Point size;
		ExpandableLayout layout = (ExpandableLayout) getLayout();
		if (wHint == SWT.DEFAULT || hHint == SWT.DEFAULT) {
			size = layout.computeSize(this, wHint, hHint, changed);
		} else {
			size = new Point(wHint, hHint);
		}
		Rectangle trim = computeTrim(0, 0, size.x, size.y);
		return new Point(trim.width, trim.height);
	}

	/**
	 * Returns <samp>true </samp> if the composite is fixed i.e. cannot be
	 * expanded or collapsed. Fixed control will still contain the title,
	 * separator and description (if present) as well as the client, but will be
	 * in the permanent expanded state and the toggle affordance will not be
	 * shown.
	 * 
	 * @return <samp>true </samp> if the control is fixed in the expanded state,
	 *         <samp>false </samp> if it can be collapsed.
	 */
	protected boolean isFixedStyle() {
		return ((expansionStyle & TWISTIE) == 0);
	}

	/**
	 * Returns the text client control.
	 * 
	 * @return Returns the text client control if specified, or
	 *         <code>null</code> if not.
	 */
	public Control getTextClient() {
		return textClient;
	}

	/**
	 * Sets the text client control. Text client is a control that is a child of
	 * the expandable composite and is placed to the right of the text. It can
	 * be used to place small image hyperlinks. If more than one control is
	 * needed, use Composite to hold them. Care should be taken that the height
	 * of the control is comparable to the height of the text.
	 * 
	 * @param textClient
	 *            the textClient to set or <code>null</code> if not needed any
	 *            more.
	 */
	public void setTextClient(Control textClient) {
		if (this.textClient != null)
			this.textClient.dispose();
		this.textClient = textClient;
	}

	/**
	 * Returns the difference in height between the text and the text client (if
	 * set). This difference can cause vertical alignment problems when two
	 * expandable composites are placed side by side, one with and one without
	 * the text client. Use this method obtain the value to add to either
	 * <code>descriptionVerticalSpacing</code> (if you have description) or
	 * <code>clientVerticalSpacing</code> to correct the alignment of the
	 * expandable without the text client.
	 * 
	 * @return the difference in height between the text and the text client or
	 *         0 if no corrective action is needed.
	 * @since 3.3
	 */
	public int getTextClientHeightDifference() {
		if (textClient == null || textLabel == null)
			return 0;
		int theight = textLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		int tcheight = textClient.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		return Math.max(tcheight - theight, 0);
	}

	/**
	 * Tests if this expandable composite renders a title bar around the text.
	 * 
	 * @return <code>true</code> for <code>TITLE_BAR</code> or
	 *         <code>SHORT_TITLE_BAR</code> styles, <code>false</code>
	 *         otherwise.
	 */
	protected boolean hasTitleBar() {
		return (getExpansionStyle() & TITLE_BAR) != 0
				|| (getExpansionStyle() & SHORT_TITLE_BAR) != 0;
	}

	/**
	 * Sets the color of the title bar foreground when TITLE_BAR style is used.
	 * 
	 * @param color
	 *            the title bar foreground
	 */
	public void setTitleBarForeground(Color color) {
		titleBarForeground = color;
		textLabel.setForeground(color);
		if(imageLabel != null) 
			imageLabel.setForeground(color);
	}

	/**
	 * Returns the title bar foreground when TITLE_BAR style is used.
	 * 
	 * @return the title bar foreground
	 */
	public Color getTitleBarForeground() {
		return titleBarForeground;
	}

	// end of APIs

	@SuppressWarnings("restriction")
	private void toggleState() {
		boolean newState = !isExpanded();
		fireExpanding(newState, true);
		internalSetExpanded(newState);
		fireExpanding(newState, false);
		if (newState)
			FormUtil.ensureVisible(this);
	}

	private void fireExpanding(boolean state, boolean before) {
		int size = listeners.size();
		if (size == 0)
			return;
		ExpansionEvent e = new ExpansionEvent(this, state);
		Object [] listenerList = listeners.getListeners();
		for (int i = 0; i < size; i++) {
			IExpansionListener listener = (IExpansionListener) listenerList[i];
			if (before)
				listener.expansionStateChanging(e);
			else
				listener.expansionStateChanged(e);
		}
	}

	private void verticalMove(boolean down) {
		Composite parent = getParent();
		Control[] children = parent.getChildren();
		for (int i = 0; i < children.length; i++) {
			Control child = children[i];
			if (child == this) {
				NetiferaExpand sibling = getSibling(children, i, down);
				if (sibling != null && sibling.toggle != null) {
					sibling.setFocus();
				}
				break;
			}
		}
	}

	private NetiferaExpand getSibling(Control[] children, int index,
			boolean down) {
		int loc = down ? index + 1 : index - 1;
		while (loc >= 0 && loc < children.length) {
			Control c = children[loc];
			if (c instanceof NetiferaExpand && c.isVisible())
				return (NetiferaExpand) c;
			loc = down ? loc + 1 : loc - 1;
		}
		return null;
	}

	private void programmaticToggleState() {
		if (toggle != null)
			toggle.setExpanded(!toggle.isExpanded());
		toggleState();
	}
	
	/**
	 * Draws a rectangle around the controls to represent focus
	 * @param gc
	 */
	private void paintTitleFocus(GC gc) {
		Point size = textLabel.getSize();
		gc.setBackground(textLabel.getBackground());
		gc.setForeground(textLabel.getForeground());
		if (toggle.isFocusControl())
			gc.drawFocus(0, 0, size.x, size.y);
	}
	
	/**
	 * Reflows this section and all the parents up the hierarchy until a
	 * ScrolledForm is reached.
	 */
	protected void reflow() {
		Composite c = this;
		while (c != null) {
			c.setRedraw(false);
			c = c.getParent();
			if (c instanceof ScrolledForm) {
				break;
			}
		}
		c = this;
		while (c != null) {
			c.layout(true);
			c = c.getParent();
			if (c instanceof ScrolledForm) {
				((ScrolledForm) c).reflow(true);
				break;
			}
		}
		c = this;
		while (c != null) {
			c.setRedraw(true);
			c = c.getParent();
			if (c instanceof ScrolledForm) {
				break;
			}
		}
	}

}