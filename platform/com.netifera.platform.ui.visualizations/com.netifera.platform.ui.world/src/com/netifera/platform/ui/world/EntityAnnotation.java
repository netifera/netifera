package com.netifera.platform.ui.world;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.FrameFactory;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.MultiLineTextRenderer;
import gov.nasa.worldwind.render.PatternFactory;
import gov.nasa.worldwind.render.Polyline;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.util.Random;

import com.netifera.platform.api.model.IEntity;

public class EntityAnnotation extends GlobeAnnotation {

	final private IEntity entity;
	
	public EntityAnnotation(IEntity entity, String text, Position position, boolean randomizePosition) {
		super(text, position, createAttributes(randomizePosition));
		this.entity = entity;
	}

	static private Random random = new Random();
	
	static private AnnotationAttributes createAttributes(boolean randomizePosition) {
		AnnotationAttributes attributes = new AnnotationAttributes();
		attributes.setFrameShape(FrameFactory.SHAPE_RECTANGLE);
		attributes.setLeader(FrameFactory.LEADER_TRIANGLE);
		attributes.setBorderWidth(0);
//		attributes.setBorderColor(Color.GREEN);
		attributes.setFont(Font.decode("Arial-Bold-14"));
		attributes.setTextColor(Color.WHITE);
		attributes.setTextAlign(MultiLineTextRenderer.ALIGN_CENTER);
//		attributes.setAdjustWidthToText("www.google.com asdfasdf qwer");
		if (randomizePosition)
			attributes.setDrawOffset(new Point(random.nextInt(64)-32, random.nextInt(64)-32));
		else
			attributes.setDrawOffset(new Point(-5, 10)); // centered just above
		attributes.setEffect(MultiLineTextRenderer.EFFECT_OUTLINE);
//		attributes.setBackgroundColor(Color.);
		
//		attributes.setImageSource(PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLE, 10.8f, Color.GREEN));
        return attributes;
	}
	
	public IEntity getEntity() {
		return entity;
	}
}
