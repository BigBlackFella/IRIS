package com.temenos.interaction.core.link;

import java.util.Map;

public class CollectionResourceState extends ResourceState {

	public CollectionResourceState(String entityName, String name, String path) {
		super(entityName, name, path);
	}

	public void addTransitionForEachItem(String httpMethod, ResourceState targetState, Map<String, String> uriLinkageMap) {
		
	}

}
