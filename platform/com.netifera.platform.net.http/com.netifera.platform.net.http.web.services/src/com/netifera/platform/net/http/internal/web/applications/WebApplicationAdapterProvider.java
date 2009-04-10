package com.netifera.platform.net.http.internal.web.applications;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import com.netifera.platform.api.iterables.IndexedIterable;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityAdapterProvider;
import com.netifera.platform.net.http.service.HTTP;
import com.netifera.platform.net.http.web.applications.IWebApplicationProvider;
import com.netifera.platform.net.http.web.model.WebApplicationEntity;
import com.netifera.platform.net.http.web.model.WebPageEntity;
import com.netifera.platform.net.http.web.model.WebSiteEntity;

public class WebApplicationAdapterProvider implements IEntityAdapterProvider {
	private final Map<String,IWebApplicationProvider> providers = new HashMap<String,IWebApplicationProvider>();
	
	public Object getAdapter(IEntity entity, Class<?> adapterType) {
		if (adapterType.isAssignableFrom(URI.class)) {
			try {
				if (entity instanceof WebSiteEntity)
					return new URI(((WebSiteEntity)entity).getRootURL());
				if (entity instanceof WebPageEntity) {
					WebPageEntity page = (WebPageEntity)entity;
					return ((URI)page.getAdapter(URI.class)).resolve(page.getPath());
				}
				if (entity instanceof WebApplicationEntity) {
					return new URI(((WebApplicationEntity)entity).getURL());
				}
			} catch (URISyntaxException e) {
				return null;
			}
		}
		
		if (!(entity instanceof WebApplicationEntity))
			return null;
		
		WebApplicationEntity serviceEntity = (WebApplicationEntity) entity;
		String serviceType = serviceEntity.getServiceType();
		IWebApplicationProvider provider = providers.get(serviceType);
		if (provider != null && adapterType.isAssignableFrom(provider.getServiceClass())) {
			HTTP http = (HTTP) serviceEntity.getHTTP().getAdapter(HTTP.class);
			URI url;
			try {
				url = new URI(serviceEntity.getURL());
			} catch (URISyntaxException e) {
				return null;
			}
			return provider.create(http,url);
		}
		
		return null;
	}
	
	public IndexedIterable<?> getIterableAdapter(IEntity entity, Class<?> iterableType) {
		// TODO Auto-generated method stub
		return null;
	}
	

	protected void registerProvider(IWebApplicationProvider provider) {
		providers.put(provider.getServiceName(), provider);
	}
	
	protected void unregisterProvider(IWebApplicationProvider provider) {
		providers.remove(provider.getServiceName()); // FIXME what if two providers with the same service name? should not happen
	}
}
