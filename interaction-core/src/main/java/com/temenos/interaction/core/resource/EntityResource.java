package com.temenos.interaction.core.resource;

/*
 * #%L
 * interaction-core
 * %%
 * Copyright (C) 2012 - 2013 Temenos Holdings N.V.
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


import java.util.Collection;
import java.util.Map;

import javax.ws.rs.core.GenericEntity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.temenos.interaction.core.entity.Entity;
import com.temenos.interaction.core.hypermedia.Link;
import com.temenos.interaction.core.hypermedia.Transition;
import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;

/**
 * An EntityResource is the RESTful representation of a 'thing' within our
 * system.  A 'thing' is addressable by a globally unique key, it has a set of
 * simple & complex named properties, and a set of links to find other resources
 * linked to this resource.
 * @author aphethean
 */
@XmlRootElement(name = "resource")
@XmlAccessorType(XmlAccessType.FIELD)
public class EntityResource<T> implements RESTResource {
	@XmlAnyElement(lax=true)
	private T entity;
	
	/* injected by during build response phase */
	@XmlTransient
    private String entityName;
	@XmlTransient
    private Collection<Link> links;
	@XmlTransient
    private Map<Transition, RESTResource> embedded;
	@XmlTransient
    private String entityTag = null;

	public EntityResource() {
	}
	
	public EntityResource(String entityName, T entity) {
		this.entityName = entityName;
		this.entity = entity;
	}

	public EntityResource(T entity) {
		this.entity = entity;
	}
	
	public T getEntity() {
		return entity;
	}

	@Override
	public GenericEntity<EntityResource<T>> getGenericEntity() {
		return new GenericEntity<EntityResource<T>>(this, this.getClass().getGenericSuperclass());
	}

	@Override
    public Collection<Link> getLinks() {
    	return this.links;
    }

    /**
     * Called during resource building phase to set the links for
     * serialization by the provider.
     * @param links
     */
	@Override
    public void setLinks(Collection<Link> links) {
    	this.links = links;
    }

	@Override
    public Map<Transition, RESTResource> getEmbedded() {
    	return this.embedded;
    }

    /**
     * Called during resource building phase to set the embedded
     * resources for serialization by the provider.
     * @param embedded
     */
	@Override
    public void setEmbedded(Map<Transition, RESTResource> embedded) {
    	this.embedded = embedded;
    }

	@Override
	public String getEntityName() {
		return entityName;
	}

	@Override
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	@Override
	public String getEntityTag() {
		return entityTag;
	}

	@Override
	public void setEntityTag(String entityTag) {
		this.entityTag = entityTag;
	}

	/**
	 * Copy this EntityResource instance with a deep clone of its entity.
	 * @return
	 */
	public EntityResource<?> cloneWithDeepCopyOfEntities() {
		if(entity instanceof OEntity) {
			OEntity originalEntity = (OEntity)entity;
			OEntity oEntity = OEntities.create(originalEntity.getEntitySet(), originalEntity.getEntityKey(),
					originalEntity.getProperties(), originalEntity.getLinks());
			EntityResource<OEntity> newCopy = this.createNewEntityResource(new String(getEntityName()), oEntity);
			shallowCopyFields(newCopy);
			return newCopy;
		}else if(entity instanceof Entity){
			Entity originalEntity = (Entity)entity;
			Entity entity = new Entity(originalEntity.getName(), originalEntity.getProperties());
			EntityResource<Entity> newCopy = this.createNewEntityResource(new String(getEntityName()), entity);
			shallowCopyFields(newCopy);
			return newCopy;
		}else{
			return this;
		}
	}
	
	/**
	 * Create a new EntityResource from the given entity name and entity.
	 * @param entityName
	 * @param entityResource
	 * @return
	 */
	protected <E> EntityResource<E> createNewEntityResource(String entityName, E entityResource){
		return new EntityResource<E>(entityName, entityResource);
	}

	/**
	 * Shallow copy embedded links, links and entity tag from this instance
	 * to the new EntityResource instance.
	 * @param newCopy
	 */
	private void shallowCopyFields(EntityResource<?> newCopy) {
		newCopy.setEmbedded(this.embedded);
		newCopy.setLinks(this.links);
		newCopy.setEntityTag(this.entityTag);
	}
}
