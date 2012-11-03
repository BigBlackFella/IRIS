package com.temenos.interaction.core.media;

import java.util.Collection;
import java.util.Map;

import com.temenos.interaction.core.hypermedia.Link;
import com.temenos.interaction.core.hypermedia.Transition;
import com.temenos.interaction.core.resource.EntityResource;

/**
 * Entity resource wrapper classes which exposes a link to itself. 
 */
public class EntityResourceWrapper {
	private EntityResource<Map<String, Object>> entityResource;
	private Link entityResourceLink;
	
	public EntityResourceWrapper(EntityResource<Map<String, Object>> entityResource) {
		this.entityResource = entityResource;
		entityResourceLink = findEntityResourceLink(entityResource.getLinks());
	}
	
	public EntityResource<Map<String, Object>> getResource() {
		return entityResource;
	}

	public Link getEntityResourceLink() {
		return entityResourceLink;
	}
	
	protected Link findEntityResourceLink(Collection<Link> links) {
		Link selfLink = null;
		if (links != null) {
			for (Link l : links) {
				Transition t = l.getTransition();
				if (l.getRel().equals("self") || t != null &&
						t.getSource().getEntityName().equals(t.getTarget().getEntityName()) &&
						t.getSource().getRel().equals("collection") &&
						t.getTarget().getRel().equals("item")) {
					selfLink = l;
					break;
				}
			}
		}
		return selfLink;
	}
}
