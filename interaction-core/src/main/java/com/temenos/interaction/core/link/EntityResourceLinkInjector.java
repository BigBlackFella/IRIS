package com.temenos.interaction.core.link;


import javax.ws.rs.core.GenericEntity;

import com.jayway.jaxrs.hateoas.HateoasLinkInjector;
import com.jayway.jaxrs.hateoas.HateoasVerbosity;
import com.jayway.jaxrs.hateoas.LinkProducer;
import com.temenos.interaction.core.resource.EntityResource;
import com.temenos.interaction.core.resource.ResourceTypeHelper;

/**
 * This link injector doesn't try very hard to inject the links.  If the GenericEntity
 * does not wrap an entity that is an instance of {@link EntityResource} it just returns 
 * the entity without injecting any links.
 * @author aphethean
 */
public class EntityResourceLinkInjector implements HateoasLinkInjector<Object> {

	/**
	 * Test whether the entity being returned in the HateoasResponse can
	 * carry the links we've evaluated for this resource, these links will
	 * then be serialized to the client if the MediaType (see Provider) can
	 * support links. 
	 * @invariant supplied entity must be of type GenericEntity<?>
	 */
    @Override
    public boolean canInject(Object entity) {
    	assert(entity instanceof GenericEntity<?>);
    	GenericEntity<?> ge = (GenericEntity<?>) entity;
    	return ResourceTypeHelper.isType(ge.getRawType(), ge.getType(), EntityResource.class);
    }

    @Override
    public Object injectLinks(Object entity, LinkProducer<Object> linkProducer, final HateoasVerbosity verbosity) {
    	assert(entity instanceof GenericEntity<?>);
    	GenericEntity<?> ge = (GenericEntity<?>) entity;
    	// must be instance of EntityResource, see canInject
    	EntityResource<?> er = (EntityResource<?>) ge.getEntity();
    	er.setLinks(linkProducer.getLinks(entity));
    	
        return entity;
    }

}
