package com.temenos.interaction.core.resource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.odata4j.core.OEntity;

import java.io.ByteArrayInputStream;

import javax.ws.rs.core.GenericEntity;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;


import com.temenos.interaction.core.NestedObject;

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
		//Create entity resource for a mock OEntity
		OEntity oe = mock(OEntity.class);
        EntityResource<OEntity> er = new EntityResource<OEntity>(oe);  
 
        //Wrap entity resource into a JAX-RS GenericEntity instance
		GenericEntity<EntityResource<OEntity>> ge = new GenericEntity<EntityResource<OEntity>>(er) {};
		
		assertTrue(ResourceTypeHelper.isType(ge.getRawType(), ge.getType(), EntityResource.class));
		assertTrue(ResourceTypeHelper.isType(ge.getRawType(), ge.getType(), EntityResource.class, OEntity.class));
	}

	@Test
	public void testResourceTypeWithoutGenericEntity() {
		//Create entity resource for a mock OEntity
        EntityResource<OEntity> er = new EntityResource<OEntity>(mock(OEntity.class));  
 
		assertTrue(ResourceTypeHelper.isType(er.getClass(), null, EntityResource.class));
		assertTrue(ResourceTypeHelper.isType(er.getClass(), er.getEntity().getClass(), EntityResource.class, mock(OEntity.class).getClass()));
	}
	
	@Test
	public void testResourceTypeNotOEntity() {
		//Create entity resource for a mock NestedObject
		NestedObject no = mock(NestedObject.class);
        EntityResource<NestedObject> er = new EntityResource<NestedObject>(no);  
 
        //Wrap entity resource into a JAX-RS GenericEntity instance
		GenericEntity<EntityResource<NestedObject>> ge = new GenericEntity<EntityResource<NestedObject>>(er) {};
		
		assertTrue(ResourceTypeHelper.isType(ge.getRawType(), ge.getType(), EntityResource.class));
		assertFalse(ResourceTypeHelper.isType(ge.getRawType(), ge.getType(), EntityResource.class, OEntity.class));
	}

	@Test
	public void testResourceTypeNotCollectionResourceNotOEntityWithoutGenericEntity() {
		//Create entity resource for a mock NestedObject
        EntityResource<NestedObject> er = new EntityResource<NestedObject>(mock(NestedObject.class));  
 
		assertFalse(ResourceTypeHelper.isType(er.getClass(), null, CollectionResource.class));
		assertFalse(ResourceTypeHelper.isType(er.getClass(), er.getEntity().getClass(), EntityResource.class, mock(OEntity.class).getClass()));
	}
}
