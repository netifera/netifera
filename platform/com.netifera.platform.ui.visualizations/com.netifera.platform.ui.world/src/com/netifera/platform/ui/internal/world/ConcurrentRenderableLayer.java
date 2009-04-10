package com.netifera.platform.ui.internal.world;

import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;

public class ConcurrentRenderableLayer extends RenderableLayer {

    @Override
    protected synchronized void doPick(DrawContext dc, java.awt.Point pickPoint) {
    	super.doPick(dc, pickPoint);
    }

    @Override
    protected synchronized void doRender(DrawContext dc) {
    	super.doRender(dc);
    }

    @Override
    public synchronized void addRenderable(Renderable renderable) {
    	super.addRenderable(renderable);
    }
    
    @Override
    public synchronized void removeAllRenderables() {
    	super.removeAllRenderables();
    }
}
