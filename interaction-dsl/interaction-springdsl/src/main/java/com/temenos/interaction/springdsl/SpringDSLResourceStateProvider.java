package com.temenos.interaction.springdsl;

/*
 * #%L
 * interaction-springdsl
 * %%
 * Copyright (C) 2012 - 2014 Temenos Holdings N.V.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */


import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.temenos.interaction.core.hypermedia.Event;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.hypermedia.ResourceStateProvider;

public class SpringDSLResourceStateProvider implements ResourceStateProvider, DynamicRegistrationResourceStateProvider {
	private final Logger logger = LoggerFactory.getLogger(SpringDSLResourceStateProvider.class);

	private ConcurrentMap<String, ResourceState> resources = new ConcurrentHashMap<String, ResourceState>();
	
	private StateRegisteration stateRegisteration;	

    /**
     * Map of ResourceState bean names, to paths.
     */
	private Properties beanMap;
	
	private boolean initialised = false;
	/**
	 * Map of paths to state names
	 */
	private Map<String, Set<String>> resourceStatesByPath = new HashMap<String, Set<String>>();
	/**
	 * Map of request to state names
	 */
	private Map<String, String> resourceStatesByRequest = new HashMap<String, String>();
	/**
	 * Map of resource methods where state name is the key
	 */
	private Map<String, Set<String>> resourceMethodsByState = new HashMap<String, Set<String>>();
	/**
	 * Map to a resource path where the state name is the key
	 */
	private Map<String, String> resourcePathsByState = new HashMap<String, String>();
	
	public SpringDSLResourceStateProvider() {}
	public SpringDSLResourceStateProvider(Properties beanMap) {
		this.beanMap = beanMap;
	}
	
	public void setResourceMap(Properties beanMap) {
		this.beanMap = beanMap;
	}
	
	private void initialise() {
		if (initialised)
			return;
		for (Object stateObj : beanMap.keySet()) {
			String stateName = stateObj.toString();
			// binding is [GET,PUT /thePath]
			String binding = beanMap.getProperty(stateName.toString());
			// split into methods and path
			String[] strs = binding.split(" ");
			String methodPart = strs[0];
			String path = strs[1];
			// methods
			String[] methodsStrs = methodPart.split(",");
			// path
			resourcePathsByState.put(stateName, path);
			// methods
			Set<String> methods = resourceMethodsByState.get(stateName);
			if (methods == null) {
				methods = new HashSet<String>();
			}
			for (String method : methodsStrs) {
				methods.add(method);
			}
			resourceMethodsByState.put(stateName, methods);
			for (String method : methods) {
				String request = method + " " + path;
				logger.debug("Binding ["+stateName+"] to ["+request+"]");
				String found = resourceStatesByRequest.get(request);
				if (found != null) {
					logger.error("Multiple states bound to the same request ["+request+"], overriding ["+found+"] with ["+stateName+"]");
				}
				resourceStatesByRequest.put(request, stateName);
			}
			
			Set<String> stateNames = resourceStatesByPath.get(path);
			if (stateNames == null) {
				stateNames = new HashSet<String>();
			}
			stateNames.add(stateName.toString());
			resourceStatesByPath.put(path, stateNames);
		}
		initialised = true;
	}
	
	void addState(String stateObj, Properties properties) {
		if (initialised) {
			String stateName = stateObj.toString();

			// binding is [GET,PUT /thePath]
			String binding = properties.getProperty(stateName);

			// split into methods and path
			String[] strs = binding.split(" ");
			String methodPart = strs[0];
			String path = strs[1];

			// methods
			String[] methods = methodPart.split(",");

			logger.info("Attempting to register state: " + stateName + " methods: " + methods + " path: " + path
					+ " using: " + stateRegisteration);

			this.stateRegisteration.register(stateName, path, new HashSet<String>(Arrays.asList(methods)));
		}
	}
	
	protected void unload(String name) {
		resources.remove(name);
	}
	
	@Override
	public boolean isLoaded(String name) {
		return resources.containsKey(name);
	}

	@Override
	public ResourceState getResourceState(String name) {
		try {
			if (name != null) {				
				ResourceState resource = resources.get(name);
				if (resource == null) {
					String beanXml = "IRIS-"+name+"-PRD.xml";
					if (this.getClass().getClassLoader().getResource(beanXml) != null) {
						ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] {beanXml});
						resource = (ResourceState) context.getBean(name);
						resources.putIfAbsent(name, resource);
					} else {
						logger.error("Unable to load resource ["+beanXml+"] not found");
					}
				}
				return resource;
			}
		} catch (BeansException e) {
			logger.error("Failed to load ["+name+"]", e);
		}
		return null;
	}

	@Override
	public ResourceState determineState(Event event, String resourcePath) {
		initialise();
		String request = event.getMethod() + " " + resourcePath;
		String stateName = resourceStatesByRequest.get(request);
		logger.debug("Found state ["+stateName+"] for ["+request+"]");
		return getResourceState(stateName);
	}

	@Override
	public Map<String, Set<String>> getResourceStatesByPath() {
		initialise();
		return resourceStatesByPath;
	}

	public Map<String, Set<String>> getResourceMethodsByState() {
		initialise();
		return resourceMethodsByState;
	}

	public Map<String, String> getResourcePathsByState() {
		initialise();
		return resourcePathsByState;
	}

	protected Map<String, Set<String>> getResourceStatesByPath(Properties beanMap) {
		initialise();
		return resourceStatesByPath;
	}
		
	@Override
	public void setStateRegisteration(StateRegisteration registerState) {
		this.stateRegisteration = registerState;		
	}
	
	@Override
	public ResourceState getInitialResourceState() {
		throw new UnsupportedOperationException();
	}	
}
