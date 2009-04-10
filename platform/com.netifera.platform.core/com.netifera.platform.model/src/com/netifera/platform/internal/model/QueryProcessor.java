package com.netifera.platform.internal.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import com.db4o.ObjectContainer;
import com.db4o.query.Predicate;
import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IModelPredicate;

public class QueryProcessor {
	private final static boolean QUERY_DEBUG = false;
	private final ObjectContainer database;
	private final ModelService model;
	
	
	QueryProcessor(ObjectContainer database, ModelService model) {
		this.database = database;
		this.model = model;
	}
	
	public <T extends IEntity> List<T> findAll(Class<T> klass) {
		if(QUERY_DEBUG) {
			model.getLogger().info("findAll " + klass);
		}
		return database.query(klass);
	}
	
	@SuppressWarnings("serial")
	AbstractEntity findById(final long id) {
		if(QUERY_DEBUG) {
			model.getLogger().info("findById(" + id + ")");
		}
		if(id <= 0) {
			throw new IllegalArgumentException();
		}
		List<AbstractEntity> results = database.query(new Predicate<AbstractEntity>() {
	
			public boolean match(AbstractEntity candidate) {
				return candidate.privateGetId() == id;
			}
		
		});

		if(results.size() == 1) {
			return results.get(0);
		} else if(results.isEmpty()) {
			model.getLogger().debug("Failed to lookup entity with id = " + id);
			return null;
		} else {
			model.getLogger().error("Database corrupted, duplicate entity found for id = " + id);
			throw new IllegalStateException();
		}
		
	}
	
	@SuppressWarnings("serial")
	List<IEntity> findByRealm(final long realm) {
		if(QUERY_DEBUG) {
			model.getLogger().info("findByRealm(" + realm + ")");
		}
		
		List<AbstractEntity> entities = database.query(new Predicate<AbstractEntity>() {
			public boolean match(AbstractEntity candidate) {
				return candidate.getRealmId() == realm;
			}
		});

		entities = new ArrayList<AbstractEntity>(entities);
		Collections.sort(entities, new Comparator<AbstractEntity>() {
			public int compare(AbstractEntity o1, AbstractEntity o2) {
				return o1.privateGetId() < o2.privateGetId() ? -1 : (o1.privateGetId() == o2.privateGetId() ? 0 : 1);
			}
		});

		return new ArrayList<IEntity>(entities);
	}
	
	@SuppressWarnings("serial")
	public <T extends IEntity> List<T> findByPredicate(final IModelPredicate<T> predicate) {
		if(QUERY_DEBUG) {
			model.getLogger().info("findByPredicate " + predicate);
		}
		return database.query(new Predicate<T>() {
			public boolean match(T candidate) {
				return predicate.match(candidate);
			}
		});
	}

	@SuppressWarnings("serial")
	public <T extends IEntity> List<T> findByPredicate(final IModelPredicate<T> predicate, Comparator<T> comparator) {
		if(QUERY_DEBUG) {
			model.getLogger().info("findByPredicateComp " + predicate);
		}

		return database.query(new Predicate<T>() {
			public boolean match(T candidate) {
				return predicate.match(candidate);
			}
		}, comparator);
	}

	@SuppressWarnings("serial")
	public <T extends IEntity> List<T> findByPredicate(Class<T> klass, final IModelPredicate<T> predicate) {
		if(QUERY_DEBUG) {
			model.getLogger().info("findByPredicate " + klass + " " + predicate);
		}
		return database.query(new Predicate<T>(klass) {
			public boolean match(T candidate) {
				return predicate.match(candidate);
			}
		});

	}
	

}
