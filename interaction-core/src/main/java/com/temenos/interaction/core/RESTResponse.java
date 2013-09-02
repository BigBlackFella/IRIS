package com.temenos.interaction.core;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Response.StatusType;

import com.temenos.interaction.core.hypermedia.Link;
import com.temenos.interaction.core.resource.RESTResource;

public class RESTResponse {

	private final StatusType status;
	private final RESTResource resource;
	private final Set<Link> transitions = new HashSet<Link>();
	private final Set<String> validMethods = new HashSet<String>();
	
	public RESTResponse(StatusType status, RESTResource resource) {
		this(status, resource, null, null);
	}
	
	public RESTResponse(StatusType status, RESTResource resource, Set<Link> transitions, Set<String> validMethods) {
		this.status = status;
		this.resource = resource;
		if (transitions != null)
			this.transitions.addAll(transitions);
		if (validMethods != null)
			this.validMethods.addAll(validMethods);
	}
	
	public StatusType getStatus() {
		return status;
	}
	
	public RESTResource getResource() {
		return resource;
	}
	
	public Set<Link> getTransitions() {
		return transitions;
	}

	public Set<String> getValidMethods() {
		return validMethods;
	}
	
}
