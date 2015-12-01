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


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import javax.ws.rs.core.GenericEntity;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

import com.temenos.interaction.core.NestedObject;
import com.temenos.interaction.core.entity.Entity;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;

public class TestResourceType {

	@SuppressWarnings("unchecked")
	@Test
	public void testResourceTypeJAXB() throws JAXBException {
		//Create entity resource for NestedObject
		JAXBContext jc = JAXBContext.newInstance(EntityResource.class, NestedObject.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
		String testXMLString = "<resource><Test/></resource>";
        EntityResource<NestedObject> er = (EntityResource<NestedObject>) unmarshaller.unmarshal(new ByteArrayInputStream(testXMLString.getBytes()));
 
        //Wrap entity resource into a JAX-RS GenericEntity instance
		GenericEntity<EntityResource<NestedObject>> ge = new GenericEntity<EntityResource<NestedObject>>(er) {};
		
		assertTrue(ResourceTypeHelper.isType(ge.getRawType(), ge.getType(), EntityResource.class));
		assertTrue(ResourceTypeHelper.isType(ge.getRawType(), ge.getType(), EntityResource.class, NestedObject.class));
	}

	@Test
	public void testResourceTypeOEntity() {
		//Create entity resource for a mock Entity
		Entity oe = mock(Entity.class);
        EntityResource<Entity> er = new EntityResource<Entity>(oe);  
 
        //Wrap entity resource into a JAX-RS GenericEntity instance
		GenericEntity<EntityResource<Entity>> ge = new GenericEntity<EntityResource<Entity>>(er) {};
		
		assertTrue(ResourceTypeHelper.isType(ge.getRawType(), ge.getType(), EntityResource.class));
		assertTrue(ResourceTypeHelper.isType(ge.getRawType(), ge.getType(), EntityResource.class, Entity.class));
	}

	@Test
	public void testResourceTypeWithoutGenericEntity() {
		//Create entity resource for a mock OEntity
        EntityResource<Entity> er = new EntityResource<Entity>(mock(Entity.class));  
 
		assertTrue(ResourceTypeHelper.isType(er.getClass(), null, EntityResource.class));
		assertTrue(ResourceTypeHelper.isType(er.getClass(), er.getEntity().getClass(), EntityResource.class, mock(Entity.class).getClass()));
	}
	
	@Test
	public void testResourceTypeNotOEntity() {
		//Create entity resource for a mock NestedObject
		NestedObject no = mock(NestedObject.class);
        EntityResource<NestedObject> er = new EntityResource<NestedObject>(no);  
 
        //Wrap entity resource into a JAX-RS GenericEntity instance
		GenericEntity<EntityResource<NestedObject>> ge = new GenericEntity<EntityResource<NestedObject>>(er) {};
		
		assertTrue(ResourceTypeHelper.isType(ge.getRawType(), ge.getType(), EntityResource.class));
		assertFalse(ResourceTypeHelper.isType(ge.getRawType(), ge.getType(), EntityResource.class, Entity.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public<E> void testResourceTypeEntityWithTypeVariableAsGenericType() {
		Entity me = mock(Entity.class);
		
		//Wrap entity resource into a JAX-RS GenericEntity instance
		GenericEntity<E> ge = new GenericEntity<E>((E)me) {};
		Type t = getTestTypeVariable("Entity");
		
		assertTrue(ResourceTypeHelper.isType(ge.getRawType(), t, Entity.class, Entity.class));
	}
	
	@Test
	public void testResourceTypeNotCollectionResourceNotOEntityWithoutGenericEntity() {
		//Create entity resource for a mock NestedObject
        EntityResource<NestedObject> er = new EntityResource<NestedObject>(mock(NestedObject.class));  
 
		assertFalse(ResourceTypeHelper.isType(er.getClass(), null, CollectionResource.class));
		assertFalse(ResourceTypeHelper.isType(er.getClass(), er.getEntity().getClass(), EntityResource.class, mock(Entity.class).getClass()));
	}
	
	@SuppressWarnings("rawtypes")
	private<E> Type getTestTypeVariable(String typeName) {
		final String fTypeName = typeName;
		Type t = new TypeVariable() {
			@Override
			public Type[] getBounds() {
				return null;
			}

			@Override
			public GenericDeclaration getGenericDeclaration() {
				return null;
			}

			@Override
			public String getName() {
				return fTypeName;
			}

                    @Override
                    public AnnotatedType[] getAnnotatedBounds() {
                        return new AnnotatedType[]{};
                    }

                    @Override
                    public <T extends Annotation> T getAnnotation(Class<T> type) {
                        return null;
                    }

                    @Override
                    public Annotation[] getAnnotations() {
                        return new Annotation[]{};
                    }

                    @Override
                    public Annotation[] getDeclaredAnnotations() {
                        return new Annotation[]{};
                    }
			
		};
		return t;
	}
}
