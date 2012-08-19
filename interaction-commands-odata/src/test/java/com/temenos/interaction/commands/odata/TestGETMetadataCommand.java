package com.temenos.interaction.commands.odata;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.odata4j.core.ImmutableList;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntityContainer;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmSchema;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.producer.ODataProducer;

import com.temenos.interaction.core.resource.EntityResource;
import com.temenos.interaction.core.resource.MetaDataResource;
import com.temenos.interaction.core.RESTResponse;

public class TestGETMetadataCommand {

	@Test
	public void testMetadataResource() {
		ODataProducer mockProducer = createMockODataProducer("A");
		
		GETMetadataCommand command = new GETMetadataCommand("Metadata", mockProducer.getMetadata());
		RESTResponse rr = command.get("1", null);
		assertNotNull(rr);
		assertTrue(rr.getResource() instanceof MetaDataResource);
	}

	@Test
	public void testServiceDocumentResource() {
		ODataProducer mockProducer = createMockODataProducer("A");
		
		GETMetadataCommand command = new GETMetadataCommand("ServiceDocument", mockProducer.getMetadata());
		RESTResponse rr = command.get("1", null);
		assertNotNull(rr);
		assertTrue(rr.getResource() instanceof EntityResource);
	}

	@Test
	public void testGETMetadataODataMetadata() {
		GETMetadataCommand command = new GETMetadataCommand("Metadata", createMockODataProducer("A").getMetadata());
		RESTResponse rr = command.get("1", null);
		assertNotNull(rr);
		assertTrue(rr.getResource() instanceof MetaDataResource);
	}
	
	private ODataProducer createMockODataProducer(String suffix) {
		ODataProducer mockProducer = mock(ODataProducer.class);
		EdmDataServices mockEDS = createMetadata(suffix);
		when(mockProducer.getMetadata()).thenReturn(mockEDS);
	
		return mockProducer;
	}			

	private EdmDataServices createMetadata(String suffix) {
		EdmDataServices mockEDS = mock(EdmDataServices.class);

		//Mock EdmDataServices
		List<String> keys = new ArrayList<String>();
		keys.add("MyId" + suffix);
		List<EdmProperty.Builder> properties = new ArrayList<EdmProperty.Builder>();
		EdmProperty.Builder ep = EdmProperty.newBuilder("MyId" + suffix).setType(EdmSimpleType.STRING);
		properties.add(ep);
		EdmEntityType.Builder eet = EdmEntityType.newBuilder().setNamespace("MyNamespace" + suffix).setAlias("MyAlias" + suffix).setName("Flight" + suffix).addKeys(keys).addProperties(properties);
		EdmEntitySet.Builder ees = EdmEntitySet.newBuilder().setName("Flight" + suffix).setEntityType(eet);
		List<EdmEntityType.Builder> mockEntityTypes = new ArrayList<EdmEntityType.Builder>();
		mockEntityTypes.add(eet);
		List<EdmEntitySet.Builder> mockEntitySets = new ArrayList<EdmEntitySet.Builder>();
		mockEntitySets.add(ees);
		EdmEntityContainer.Builder eec = EdmEntityContainer.newBuilder().setName("MyEntityContainer" + suffix).addEntitySets(mockEntitySets);
		List<EdmEntityContainer.Builder> mockEntityContainers = new ArrayList<EdmEntityContainer.Builder>();
		mockEntityContainers.add(eec);
		EdmSchema.Builder es = EdmSchema.newBuilder().setNamespace("MyNamespace" + suffix).setAlias("MyAlias" + suffix).addEntityTypes(mockEntityTypes).addEntityContainers(mockEntityContainers);
		List<EdmSchema> mockSchemas = new ArrayList<EdmSchema>();
		mockSchemas.add(es.build());
		when(mockEDS.getSchemas()).thenReturn(ImmutableList.copyOf(mockSchemas));

		return mockEDS;
	}
}
