package com.temenos.interaction.core.media.atom;


import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.IgnoreTextAndAttributeValuesDifferenceListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odata4j.core.ImmutableList;
import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OLink;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntityContainer;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmSchema;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.format.Entry;
import org.odata4j.format.xml.AtomEntryFormatParser;
import org.odata4j.internal.FeedCustomizationMapping;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.temenos.interaction.core.dynaresource.HTTPDynaRIM;
import com.temenos.interaction.core.link.ResourceRegistry;
import com.temenos.interaction.core.link.ResourceState;
import com.temenos.interaction.core.resource.EntityResource;
import com.temenos.interaction.core.resource.MetaDataResource;
import com.temenos.interaction.core.resource.RESTResource;
import com.temenos.interaction.core.state.ResourceInteractionModel;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OEntityKey.class, AtomXMLProvider.class})
public class TestAtomXMLProvider {
	
	private final static String EXPECTED_XML = "<?xml version=\"1.0\" encoding=\"utf-8\"?><entry xmlns=\"http://www.w3.org/2005/Atom\" xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" xml:base=\"http://localhost:8080/responder/rest\"><id>http://localhost:8080/responder/restFlight('123')</id><title type=\"text\"></title><updated>2012-03-14T11:29:19Z</updated><author><name></name></author><link rel=\"edit\" title=\"Flight\" href=\"Flight('123')\"></link><category term=\"InteractionTest.Flight\" scheme=\"http://schemas.microsoft.com/ado/2007/08/dataservices/scheme\"></category><content type=\"application/xml\"><m:properties><d:id>1</d:id><d:flight>EI218</d:flight></m:properties></content></entry>";
	
	public class MockAtomXMLProvider extends AtomXMLProvider {
		public MockAtomXMLProvider(EdmDataServices edmDataServices) {
			super(edmDataServices, new ResourceRegistry(edmDataServices, new HashSet<HTTPDynaRIM>()));
		}
		public void setUriInfo(UriInfo uriInfo) {
			super.setUriInfo(uriInfo);
		}
	};

	@Test
	public void testWriteEntityResourceOEntity_XML() throws Exception {
		EdmEntitySet ees = createMockEdmEntitySet();
		EdmDataServices mockEDS = createMockFlightEdmDataServices();
		EntityResource<OEntity> er = createMockEntityResourceOEntity(ees);
		
		when(mockEDS.getEdmEntitySet(anyString())).thenReturn(ees);
		
        //Wrap entity resource into a JAX-RS GenericEntity instance
		GenericEntity<EntityResource<OEntity>> ge = new GenericEntity<EntityResource<OEntity>>(er) {};

		//Create provider
		MockAtomXMLProvider p = new MockAtomXMLProvider(mockEDS);
		UriInfo uriInfo = mock(UriInfo.class);
		URI uri = new URI("http://localhost:8080/responder/rest");
		when(uriInfo.getBaseUri()).thenReturn(uri);
		p.setUriInfo(uriInfo);
		
		//Serialize resource
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		p.writeTo(ge.getEntity(), ge.getRawType(), ge.getType(), null, MediaType.APPLICATION_XML_TYPE, null, bos);
		String responseString = new String(bos.toByteArray(), "UTF-8");

		//Assert xml string but ignore text and attribute values
	    DifferenceListener myDifferenceListener = new IgnoreTextAndAttributeValuesDifferenceListener();
	    Diff myDiff = new Diff(responseString, EXPECTED_XML);
	    myDiff.overrideDifferenceListener(myDifferenceListener);
	    assertTrue(myDiff.similar());		
	}

	@Test
	public void testWriteEntityResourceOEntity_AtomXML() throws Exception {
		EdmEntitySet ees = createMockEdmEntitySet();
		EdmDataServices mockEDS = createMockFlightEdmDataServices();
		EntityResource<OEntity> er = createMockEntityResourceOEntity(ees);
		
		when(mockEDS.getEdmEntitySet(anyString())).thenReturn(ees);

        //Wrap entity resource into a JAX-RS GenericEntity instance
		GenericEntity<EntityResource<OEntity>> ge = new GenericEntity<EntityResource<OEntity>>(er) {};

		//Create provider
		MockAtomXMLProvider p = new MockAtomXMLProvider(mockEDS);
		UriInfo uriInfo = mock(UriInfo.class);
		URI uri = new URI("http://localhost:8080/responder/rest");
		when(uriInfo.getBaseUri()).thenReturn(uri);
		p.setUriInfo(uriInfo);
		
		//Serialize resource
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		p.writeTo(ge.getEntity(), ge.getRawType(), ge.getType(), null, MediaType.APPLICATION_ATOM_XML_TYPE, null, bos);
		String responseString = new String(bos.toByteArray(), "UTF-8");

		//Assert xml string but ignore text and attribute values
	    DifferenceListener myDifferenceListener = new IgnoreTextAndAttributeValuesDifferenceListener();
	    Diff myDiff = new Diff(responseString, EXPECTED_XML);
	    myDiff.overrideDifferenceListener(myDifferenceListener);
	    assertTrue(myDiff.similar());		
	}
	
	private EdmEntitySet createMockEdmEntitySet() {
		// Create an entity set
		List<EdmProperty.Builder> eprops = new ArrayList<EdmProperty.Builder>();
		EdmProperty.Builder ep = EdmProperty.newBuilder("id").setType(EdmSimpleType.STRING);
		eprops.add(ep);
		EdmEntityType.Builder eet = EdmEntityType.newBuilder().setNamespace("InteractionTest").setName("Flight").addKeys(Arrays.asList("id")).addProperties(eprops);
		EdmEntitySet.Builder eesb = EdmEntitySet.newBuilder().setName("Flight").setEntityType(eet);
		return eesb.build();
	}
	
	private EdmDataServices createMockFlightEdmDataServices() {
		EdmDataServices mockEDS = mock(EdmDataServices.class);

		//Mock EdmDataServices
		List<String> keys = new ArrayList<String>();
		keys.add("MyId");
		List<EdmProperty.Builder> properties = new ArrayList<EdmProperty.Builder>();
		EdmProperty.Builder ep = EdmProperty.newBuilder("MyId").setType(EdmSimpleType.STRING);
		properties.add(ep);
		EdmEntityType.Builder eet = EdmEntityType.newBuilder().setNamespace("MyNamespace").setAlias("MyAlias").setName("Flight").addKeys(keys).addProperties(properties);
		EdmEntitySet.Builder ees = EdmEntitySet.newBuilder().setName("Flight").setEntityType(eet);
		List<EdmEntityType.Builder> mockEntityTypes = new ArrayList<EdmEntityType.Builder>();
		mockEntityTypes.add(eet);
		List<EdmEntitySet.Builder> mockEntitySets = new ArrayList<EdmEntitySet.Builder>();
		mockEntitySets.add(ees);
		EdmEntityContainer.Builder eec = EdmEntityContainer.newBuilder().setName("MyEntityContainer").addEntitySets(mockEntitySets);
		List<EdmEntityContainer.Builder> mockEntityContainers = new ArrayList<EdmEntityContainer.Builder>();
		mockEntityContainers.add(eec);
		EdmSchema.Builder es = EdmSchema.newBuilder().setNamespace("MyNamespace").setAlias("MyAlias").addEntityTypes(mockEntityTypes).addEntityContainers(mockEntityContainers);
		List<EdmSchema> mockSchemas = new ArrayList<EdmSchema>();
		mockSchemas.add(es.build());
		when(mockEDS.getSchemas()).thenReturn(ImmutableList.copyOf(mockSchemas));

		return mockEDS;
	}
	
	@SuppressWarnings("unchecked")
	private EntityResource<OEntity> createMockEntityResourceOEntity(EdmEntitySet ees) {
		EntityResource<OEntity> er = mock(EntityResource.class);

		//Create an OEntity
		OEntityKey entityKey = OEntityKey.create("123");
		List<OProperty<?>> properties = new ArrayList<OProperty<?>>();
		properties.add(OProperties.string("id", "1"));
		properties.add(OProperties.string("flight", "EI218"));
		OEntity entity = OEntities.create(ees, entityKey, properties, new ArrayList<OLink>());
		when(er.getEntity()).thenReturn(entity);
		return er;
	}
	
	@Test (expected = WebApplicationException.class)
	public void testUnhandledRawType() throws IOException {
		EdmDataServices metadata = mock(EdmDataServices.class);
		ResourceRegistry registry = mock(ResourceRegistry.class);

		AtomXMLProvider ap = new AtomXMLProvider(metadata, registry);
        // Wrap an unsupported resource into a JAX-RS GenericEntity instance
		GenericEntity<MetaDataResource<String>> ge = new GenericEntity<MetaDataResource<String>>(new MetaDataResource<String>("")) {};
		// will throw exception if we check the class properly
		Annotation[] annotations = null;
		MediaType mediaType = null;
		MultivaluedMap<String, String> headers = null;
		InputStream content = null;
		ap.readFrom(RESTResource.class, ge.getType(), annotations, mediaType, headers, content);
	}

	/*
	 * Wink does not seem to supply us with a Generic type so we must accept everything and hope for the best
	@Test (expected = WebApplicationException.class)
	public void testUnhandledGenericType() throws IOException {
		EdmDataServices metadata = mock(EdmDataServices.class);
		ResourceRegistry registry = mock(ResourceRegistry.class);

		AtomXMLProvider ap = new AtomXMLProvider(metadata, registry);
        // Wrap an unsupported entity resource into a JAX-RS GenericEntity instance
		GenericEntity<EntityResource<String>> ge = new GenericEntity<EntityResource<String>>(new EntityResource<String>(null)) {};
		// will throw exception if we check the class properly
		Annotation[] annotations = null;
		MediaType mediaType = null;
		MultivaluedMap<String, String> headers = null;
		InputStream content = null;
		ap.readFrom(RESTResource.class, ge.getType(), annotations, mediaType, headers, content);
	}
	 */

	@Test
	public void testReadPath() throws Exception {
		EdmDataServices metadata = mock(EdmDataServices.class);
		ResourceRegistry registry = mock(ResourceRegistry.class);
		ResourceInteractionModel rim = mock(ResourceInteractionModel.class);
		when(rim.getCurrentState()).thenReturn(mock(ResourceState.class));
		when(registry.getResourceInteractionModel(anyString())).thenReturn(rim);
		GenericEntity<EntityResource<OEntity>> ge = new GenericEntity<EntityResource<OEntity>>(new EntityResource<OEntity>(null)) {};
		// don't do anything when trying to read context
		AtomEntryFormatParser mockParser = mock(AtomEntryFormatParser.class);
		Entry mockEntry = mock(Entry.class);
		OEntity mockOEntity = mock(OEntity.class);
		when(mockEntry.getEntity()).thenReturn(mockOEntity);
		when(mockParser.parse(any(Reader.class))).thenReturn(mockEntry);
		whenNew(AtomEntryFormatParser.class).withArguments(any(EdmDataServices.class), anyString(), any(OEntityKey.class), any(FeedCustomizationMapping.class)).thenReturn(mockParser);
		
		AtomXMLProvider ap = new AtomXMLProvider(metadata, registry);
		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getPath()).thenReturn("/test/someresource/2");
		ap.setUriInfo(uriInfo);
		
		EntityResource<OEntity> result = ap.readFrom(RESTResource.class, ge.getType(), null, null, null, new ByteArrayInputStream(new byte[0]));
		assertNotNull(result);
		assertEquals(mockOEntity, result.getEntity());
		
		// verify get rim with /test/someresource
		verify(registry).getResourceInteractionModel("/test/someresource");
		// verify static with entity key "2"
		verifyStatic();
		OEntityKey.parse("2");
	}

	@Test
	public void testReadPathNoEntityKey() throws Exception {
		EdmDataServices metadata = mock(EdmDataServices.class);
		ResourceRegistry registry = mock(ResourceRegistry.class);
		ResourceInteractionModel rim = mock(ResourceInteractionModel.class);
		when(rim.getCurrentState()).thenReturn(mock(ResourceState.class));
		when(registry.getResourceInteractionModel("/test/someresource")).thenReturn(rim);
		GenericEntity<EntityResource<OEntity>> ge = new GenericEntity<EntityResource<OEntity>>(new EntityResource<OEntity>(null)) {};
		// don't do anything when trying to read context
		AtomEntryFormatParser mockParser = mock(AtomEntryFormatParser.class);
		Entry mockEntry = mock(Entry.class);
		OEntity mockOEntity = mock(OEntity.class);
		when(mockEntry.getEntity()).thenReturn(mockOEntity);
		when(mockParser.parse(any(Reader.class))).thenReturn(mockEntry);
		whenNew(AtomEntryFormatParser.class).withArguments(any(EdmDataServices.class), anyString(), any(OEntityKey.class), any(FeedCustomizationMapping.class)).thenReturn(mockParser);
		
		AtomXMLProvider ap = new AtomXMLProvider(metadata, registry);
		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getPath()).thenReturn("/test/someresource");
		ap.setUriInfo(uriInfo);
		
		EntityResource<OEntity> result = ap.readFrom(RESTResource.class, ge.getType(), null, null, null, new ByteArrayInputStream(new byte[0]));
		assertNotNull(result);
		assertEquals(mockOEntity, result.getEntity());
		
		// verify get rim with /test/someresource
		verify(registry).getResourceInteractionModel("/test/someresource");
		// verify static with entity key "2"
		verifyStatic();
		OEntityKey.parse("2");
	}

	@Test
	public void testReadPath404() throws Exception {
		EdmDataServices metadata = mock(EdmDataServices.class);
		ResourceRegistry registry = mock(ResourceRegistry.class);
		// never find any resources
		when(registry.getResourceInteractionModel(anyString())).thenReturn(null);
		GenericEntity<EntityResource<OEntity>> ge = new GenericEntity<EntityResource<OEntity>>(new EntityResource<OEntity>(null)) {};
		// don't do anything when trying to read context
		AtomEntryFormatParser mockParser = mock(AtomEntryFormatParser.class);
		Entry mockEntry = mock(Entry.class);
		OEntity mockOEntity = mock(OEntity.class);
		when(mockEntry.getEntity()).thenReturn(mockOEntity);
		when(mockParser.parse(any(Reader.class))).thenReturn(mockEntry);
		whenNew(AtomEntryFormatParser.class).withArguments(any(EdmDataServices.class), anyString(), any(OEntityKey.class), any(FeedCustomizationMapping.class)).thenReturn(mockParser);
		
		AtomXMLProvider ap = new AtomXMLProvider(metadata, registry);
		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getPath()).thenReturn("/test/someresource");
		ap.setUriInfo(uriInfo);
		
		int status = -1;
		try {
			ap.readFrom(RESTResource.class, ge.getType(), null, null, null, new ByteArrayInputStream(new byte[0]));
		} catch (WebApplicationException wae) {
			status = wae.getResponse().getStatus();
		}
		assertEquals(404, status);
	}

	@Test
	public void testReadEntityResourceOEntity() throws Exception {
		EdmDataServices metadata = mock(EdmDataServices.class);
		ResourceRegistry registry = mock(ResourceRegistry.class);
		ResourceInteractionModel rim = mock(ResourceInteractionModel.class);
		when(rim.getCurrentState()).thenReturn(mock(ResourceState.class));
		when(registry.getResourceInteractionModel(anyString())).thenReturn(rim);
		GenericEntity<EntityResource<OEntity>> ge = new GenericEntity<EntityResource<OEntity>>(new EntityResource<OEntity>(null)) {};
		// don't do anything when trying to read context
		AtomEntryFormatParser mockParser = mock(AtomEntryFormatParser.class);
		Entry mockEntry = mock(Entry.class);
		OEntity mockOEntity = mock(OEntity.class);
		when(mockEntry.getEntity()).thenReturn(mockOEntity);
		when(mockParser.parse(any(Reader.class))).thenReturn(mockEntry);
		whenNew(AtomEntryFormatParser.class).withArguments(any(EdmDataServices.class), anyString(), any(OEntityKey.class), any(FeedCustomizationMapping.class)).thenReturn(mockParser);
		
		AtomXMLProvider ap = new AtomXMLProvider(metadata, registry);
		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getPath()).thenReturn("/test/someresource/2");
		ap.setUriInfo(uriInfo);
		
		Annotation[] annotations = null;
		MediaType mediaType = null;
		MultivaluedMap<String, String> headers = null;
		InputStream content = new ByteArrayInputStream(new byte[0]);
		EntityResource<OEntity> result = ap.readFrom(RESTResource.class, ge.getType(), annotations, mediaType, headers, content);
		assertNotNull(result);
		assertEquals(mockOEntity, result.getEntity());

		// verify parse was called
		verify(mockParser).parse(any(Reader.class));
	}
}
