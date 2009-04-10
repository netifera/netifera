package com.netifera.platform.api.model;

/*
 * We use this interface for queries to avoid
 * dragging the db4o interfaces outside of the 
 * model component
 */
public interface IModelPredicate<T> {
	boolean match(T candidate);
}
