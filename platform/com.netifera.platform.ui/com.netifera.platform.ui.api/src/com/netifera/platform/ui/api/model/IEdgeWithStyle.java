package com.netifera.platform.ui.api.model;


import java.awt.Color;

import com.netifera.platform.api.model.layers.IEdge;

public interface IEdgeWithStyle extends IEdge {
	final public int STYLE_NONE = 0;
	final public int STYLE_DASHED = 1;
	
	Color getColor();
	int getLineWidth();
	short getLineStyle();
}
