package com.temenos.interaction.core.media.hal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.UriInfo;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;
import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OLink;
import org.odata4j.core.OLinks;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmSimpleType;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.jayway.jaxrs.hateoas.HateoasLink;
import com.jayway.jaxrs.hateoas.HateoasVerbosity;
import com.temenos.interaction.core.resource.EntityResource;
import com.temenos.interaction.core.resource.MetaDataResource;
import com.temenos.interaction.core.resource.RESTResource;
import com.temenos.interaction.core.media.hal.HALProvider;
import com.temenos.interaction.core.media.hal.MediaType;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.anyString;

public class TestHALProvider {

	/*
	 * Test the getSize operation of GET with this provider
	 */
	@Test
	public void testSize() {
		HALProvider hp = new HALProvider(mock(EdmDataServices.class));
		assertEquals(-1, hp.getSize(null, null, null, null, null));
	}

	/*
	 * Test the getSize operation of GET with this provider
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testDeserialise() throws IOException {
		EdmDataServices edmDS = mock(EdmDataServices.class);
		EdmEntitySet.Builder ees = EdmEntitySet.newBuilder().setName("mockChild").setEntityType(mock(EdmEntityType.Builder.class));
		EdmEntitySet entitySet = ees.build();
		when(edmDS.getEdmEntitySet(anyString())).thenReturn(entitySet);
		HALProvider hp = new HALProvider(edmDS);
		
		String strEntityStream = "<resource><Child><name>noah</name><age>2</age></Child><links></links></resource>";
		InputStream entityStream = new ByteArrayInputStream(strEntityStream.getBytes());
		GenericEntity<EntityResource<OEntity>> ge = new GenericEntity<EntityResource<OEntity>>(new EntityResource<OEntity>()) {}; 
		EntityResource<OEntity> er = (EntityResource<OEntity>) hp.readFrom(RESTResource.class, ge.getType(), null, MediaType.APPLICATION_HAL_XML_TYPE, null, entityStream);
		assertNotNull(er.getEntity());
		OEntity entity = er.getEntity();
		assertEquals("mockChild", entity.getEntitySetName());
		assertNotNull(entity.getProperties());
		// string type
		assertEquals(EdmSimpleType.STRING, entity.getProperty("name").getType());
		assertEquals("noah", entity.getProperty("name").getValue());
		// int type
		// TODO handle non string entity properties
//		assertEquals(EdmSimpleType.INT32, entity.getProperty("age").getType());
//		assertEquals(2, entity.getProperty("age").getValue());
	}

	@Test(expected = WebApplicationException.class)
	public void testAttemptToSerialiseNonEntityResource() throws IOException {
		EntityResource<?> mdr = mock(EntityResource.class);

		HALProvider hp = new HALProvider(mock(EdmDataServices.class));
		hp.writeTo(mdr, MetaDataResource.class, null, null, MediaType.APPLICATION_HAL_XML_TYPE, null, new ByteArrayOutputStream());
	}
	
	@Test
	public void testSerialiseSimpleResource() throws Exception {
		// mock a simple entity (Children entity set)
		List<EdmProperty.Builder> eprops = new ArrayList<EdmProperty.Builder>();
		EdmProperty.Builder ep = EdmProperty.newBuilder("ID").setType(EdmSimpleType.STRING);
		eprops.add(ep);
		EdmEntityType.Builder eet = EdmEntityType.newBuilder().setNamespace("InteractionTest").setName("Children").addKeys(Arrays.asList("ID")).addProperties(eprops);
		EdmEntitySet.Builder ees = EdmEntitySet.newBuilder().setName("Children").setEntityType(eet);

		// the test key
		OEntityKey entityKey = OEntityKey.create("123");
		// the test properties
		List<OProperty<?>> properties = new ArrayList<OProperty<?>>();
		properties.add(OProperties.string("name", "noah"));
		properties.add(OProperties.string("age", "2"));

		OEntity entity = OEntities.create(ees.build(), entityKey, properties, new ArrayList<OLink>());
		EntityResource<OEntity> er = new EntityResource<OEntity>(entity);
		
		HALProvider hp = new HALProvider(mock(EdmDataServices.class));
		UriInfo mockUriInfo = mock(UriInfo.class);
		when(mockUriInfo.getBaseUri()).thenReturn(new URI("http://www.temenos.com/children"));
		hp.setUriInfo(mockUriInfo);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		hp.writeTo(er, EntityResource.class, OEntity.class, null, MediaType.APPLICATION_HAL_XML_TYPE, null, bos);

		String expectedXML = "<resource href=\"http://www.temenos.com/children\"><name>noah</name><age>2</age></resource>";
		String responseString = createFlatXML(bos);
		
		Diff diff = new Diff(expectedXML, responseString);
		// don't worry about the order of the elements in the xml
		assertTrue(diff.similar());
	}

	private String createFlatXML(ByteArrayOutputStream bos) throws Exception {
		String responseString = new String(bos.toByteArray(), "UTF-8");
		responseString = responseString.replaceAll(System.getProperty("line.separator"), "");
		responseString = responseString.replaceAll(">\\s+<", "><");
		return responseString;
	}
	
	private Collection<Map<String,Object>> mockLinks() {
		Collection<HateoasLink> links = new ArrayList<HateoasLink>();
        Collection<Map<String,Object>> linkMaps = Collections2.transform(links,
                new Function<HateoasLink, Map<String, Object>>() {
                    @Override
                    public Map<String, Object> apply(HateoasLink from) {
                        return from.toMap(HateoasVerbosity.GENERIC_CLIENT);
                    }
                });
        return linkMaps;
	}
	
	@Test
	public void testSerialiseResourceNoEntity() throws Exception {
		EntityResource<?> er = mock(EntityResource.class);
		when(er.getEntity()).thenReturn(null);
		
		HALProvider hp = new HALProvider(mock(EdmDataServices.class));
		UriInfo mockUriInfo = mock(UriInfo.class);
		when(mockUriInfo.getBaseUri()).thenReturn(new URI("http://www.temenos.com"));
		hp.setUriInfo(mockUriInfo);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		hp.writeTo(er, EntityResource.class, null, null, MediaType.APPLICATION_HAL_XML_TYPE, null, bos);

		String expectedXML = "<resource href=\"http://www.temenos.com\"></resource>";
		String responseString = createFlatXML(bos);
		XMLAssert.assertXMLEqual(expectedXML, responseString);		
	}

//	@Test
	public void testSerialiseResourceWithLinks() throws Exception {
		// mock a simple entity (Children entity set)
		List<EdmProperty.Builder> eprops = new ArrayList<EdmProperty.Builder>();
		EdmProperty.Builder ep = EdmProperty.newBuilder("ID").setType(EdmSimpleType.STRING);
		eprops.add(ep);
		EdmEntityType.Builder eet = EdmEntityType.newBuilder().setNamespace("InteractionTest").setName("Children").addKeys(Arrays.asList("ID")).addProperties(eprops);
		EdmEntitySet.Builder ees = EdmEntitySet.newBuilder().setName("Children").setEntityType(eet);

		// the test key
		OEntityKey entityKey = OEntityKey.create("123");
		// the test properties
		List<OProperty<?>> properties = new ArrayList<OProperty<?>>();
		properties.add(OProperties.string("name", "noah"));
		properties.add(OProperties.string("age", "2"));
		// the test links
		List<OLink> links = new ArrayList<OLink>();
		links.add(OLinks.relatedEntity("_person", "father", "/humans/31"));
		links.add(OLinks.relatedEntity("_person", "mother", "/humans/32"));
		
		OEntity entity = OEntities.create(ees.build(), entityKey, properties, links);
		EntityResource<OEntity> er = new EntityResource<OEntity>(entity);
		er.setLinks(mockLinks());
		
		HALProvider hp = new HALProvider(mock(EdmDataServices.class));
		UriInfo mockUriInfo = mock(UriInfo.class);
		when(mockUriInfo.getBaseUri()).thenReturn(new URI("http://www.temenos.com"));
		hp.setUriInfo(mockUriInfo);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		hp.writeTo(er, EntityResource.class, null, null, MediaType.APPLICATION_HAL_XML_TYPE, null, bos);

		String expectedXML = "<resource href=\"http://www.temenos.com\"><Children><name>noah</name><age>2</age></Children><links><link href=\"/humans/31\" rel=\"_person\" title=\"father\"/><link href=\"/humans/32\" rel=\"_person\" title=\"mother\"/></links></resource>";
		String responseString = createFlatXML(bos);
		XMLAssert.assertXMLEqual(expectedXML, responseString);
	}

//	@Test
	public void testSerialiseResourceWithRelatedLinks() throws Exception {
		// mock a simple entity (Children entity set)
		List<EdmProperty.Builder> eprops = new ArrayList<EdmProperty.Builder>();
		EdmProperty.Builder ep = EdmProperty.newBuilder("ID").setType(EdmSimpleType.STRING);
		eprops.add(ep);
		EdmEntityType.Builder eet = EdmEntityType.newBuilder().setNamespace("InteractionTest").setName("Children").addKeys(Arrays.asList("ID")).addProperties(eprops);
		EdmEntitySet.Builder ees = EdmEntitySet.newBuilder().setName("Children").setEntityType(eet);

		// the test key
		OEntityKey entityKey = OEntityKey.create("123");
		// the test properties
		List<OProperty<?>> properties = new ArrayList<OProperty<?>>();
		properties.add(OProperties.string("name", "noah"));
		properties.add(OProperties.string("age", "2"));
		// the test links
		/*
		 * Not sure, but I think relatedEntity and link are the same thing.
		 * However, a relatedEntity also has the relatedEntityInline capability.
		 */
		List<OLink> links = new ArrayList<OLink>();
		links.add(OLinks.relatedEntity("_person", "father", "/humans/31"));
		links.add(OLinks.relatedEntity("_person", "mother", "/humans/32"));
		OLinks.relatedEntities("_family", "siblings", "/humans/phetheans");
		
		OEntity entity = OEntities.create(ees.build(), entityKey, properties, links);
		EntityResource<OEntity> er = new EntityResource<OEntity>(entity);
		er.setLinks(mockLinks());
		
		HALProvider hp = new HALProvider(mock(EdmDataServices.class));
		UriInfo mockUriInfo = mock(UriInfo.class);
		when(mockUriInfo.getBaseUri()).thenReturn(new URI("http://www.temenos.com"));
		hp.setUriInfo(mockUriInfo);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		hp.writeTo(er, EntityResource.class, null, null, MediaType.APPLICATION_HAL_XML_TYPE, null, bos);

		String expectedXML = "<resource href=\"http://www.temenos.com\"><Children><name>noah</name><age>2</age></Children><links><link href=\"/humans/31\" rel=\"_person\" title=\"father\"/><link href=\"/humans/32\" rel=\"_person\" title=\"mother\"/></links></resource>";
		String responseString = createFlatXML(bos);
		XMLAssert.assertXMLEqual(expectedXML, responseString);		
	}

	
	@Test
	public void testSerialiseResourceWithForm() {
		// don't know how to deal with forms yet, possibly embed an xform
	}

	@Test
	public void testSerialiseStreamingResource() {
		// cannot decorate a streaming resource so should fail
	}
	
}
