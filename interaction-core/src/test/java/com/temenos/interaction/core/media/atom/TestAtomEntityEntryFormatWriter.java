package com.temenos.interaction.core.media.atom;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.temenos.interaction.core.entity.Entity;
import com.temenos.interaction.core.entity.EntityMetadata;
import com.temenos.interaction.core.entity.EntityProperties;
import com.temenos.interaction.core.entity.EntityProperty;
import com.temenos.interaction.core.entity.Metadata;
import com.temenos.interaction.core.entity.MetadataParser;
import com.temenos.interaction.core.entity.vocabulary.Term;
import com.temenos.interaction.core.entity.vocabulary.TermFactory;
import com.temenos.interaction.core.hypermedia.Link;

public class TestAtomEntityEntryFormatWriter {

	public final static String METADATA_XML_FILE = "TestMetadataParser.xml";
	private static Entity simpleEntity;
	private static Entity simpleEntityWithComplexTypes;
	private static Entity complexEntity;
	private static Entity complexEntity2;
	
	private static EntityMetadata entityMetadata;
	private static EntityMetadata complexEntityMetadata;
	private static EntityMetadata complexEntityMetadata2;
	private static String modelName;
			
	@BeforeClass
	public static void setup() {
		
		// Just adding as we do not want to add more metadata files
		//Read the metadata file
		TermFactory termFactory = new TermFactory() {
			public Term createTerm(String name, String value) throws Exception {
				if(name.equals("TEST_ENTITY_ALIAS")) {
					Term mockTerm = mock(Term.class);
					when(mockTerm.getValue()).thenReturn(value);
					when(mockTerm.getName()).thenReturn(name);
					return mockTerm;
				}
				else {
					return super.createTerm(name, value);
				}
			}			
		};
		
		// Initailise
		MetadataParser parser = new MetadataParser(termFactory);
		InputStream is = parser.getClass().getClassLoader().getResourceAsStream(METADATA_XML_FILE);
		Metadata metadata = parser.parse(is);
		Assert.assertNotNull(metadata);
	
		modelName = metadata.getModelName();
		
		// Simple Metadata and Entity
		entityMetadata = metadata.getEntityMetadata("Customer");
		simpleEntity = getSimpleEntity("Customer");
		simpleEntityWithComplexTypes = getComplexEntity("Customer");
		
		// Complex Metadata and Entity
		complexEntityMetadata = metadata.getEntityMetadata("CustomerWithTermList");
		complexEntity = getComplexEntity("CustomerWithTermList");
		
		// Second Complex Metadata and Entity
		complexEntityMetadata2 = metadata.getEntityMetadata("CustomerAllTermList");
		complexEntity2 = getComplexEntity("CustomerAllTermList");
	}
	
	@AfterClass
	public static void tearDown() {
		simpleEntity = null;
		complexEntity = null;
		entityMetadata = null;
		complexEntityMetadata = null;
	}
	
	@Test
	public void testWriteSimpleEntry() {
		// Get UriInfo and Links
		UriInfo uriInfo = mock(UriInfo.class);
		try {
			when(uriInfo.getBaseUri()).thenReturn(new URI("http", "//www.temenos.com/iris/test", "simple"));
		} catch (Exception e) {
			fail(e.getMessage());
		}
		List<Link> links = new ArrayList<Link>();
				
		AtomEntityEntryFormatWriter writer = new AtomEntityEntryFormatWriter();
		StringWriter strWriter = new StringWriter();
		writer.write(uriInfo, strWriter, simpleEntity, entityMetadata, links, modelName);
		
		String output = strWriter.toString();
		//System.out.println(strWriter);
		
		// We should not have List or infact any complex type representation here
		Assert.assertFalse(output.contains("<d:CustomerWithTermList_address m:type=\"Bag(CustomerServiceTestModel.CustomerWithTermList_address)\">"));
		Assert.assertFalse(output.contains("<d:CustomerWithTermList_street m:type=\"CustomerServiceTestModel.CustomerWithTermList_street\">"));
	}
	
	@Test
	public void testWriteSimpleEntryWithComplexType() {
		// Get UriInfo and Links
		UriInfo uriInfo = mock(UriInfo.class);
		try {
			when(uriInfo.getBaseUri()).thenReturn(new URI("http", "//www.temenos.com/iris/test", "simpleWithComplexType"));
		} catch (Exception e) {
			fail(e.getMessage());
		}
		List<Link> links = new ArrayList<Link>();
				
		AtomEntityEntryFormatWriter writer = new AtomEntityEntryFormatWriter();
		StringWriter strWriter = new StringWriter();
		writer.write(uriInfo, strWriter, simpleEntityWithComplexTypes, entityMetadata, links, modelName);
		
		String output = strWriter.toString();
		//System.out.println(strWriter);
		
		// We should not have List or infact any complex type representation here
		Assert.assertFalse(output.contains("<d:CustomerWithTermList_address m:type=\"CustomerServiceTestModel.CustomerWithTermList_address\">"));
		Assert.assertFalse(output.contains("<d:CustomerWithTermList_street m:type=\"CustomerServiceTestModel.CustomerWithTermList_street\">"));
	}
	
	@Test
	public void testWriteComplexEntry() {
		// Get UriInfo and Links
		UriInfo uriInfo = mock(UriInfo.class);
		try {
			when(uriInfo.getBaseUri()).thenReturn(new URI("http", "//www.temenos.com/iris/test", "complex"));
		} catch (Exception e) {
			fail(e.getMessage());
		}
		List<Link> links = new ArrayList<Link>();
				
		AtomEntityEntryFormatWriter writer = new AtomEntityEntryFormatWriter();
		StringWriter strWriter = new StringWriter();
		writer.write(uriInfo, strWriter, complexEntity, complexEntityMetadata, links, modelName);
		
		String output = strWriter.toString();
		//System.out.println(strWriter);
		
		// Lets check if we have represented the Entry successfully 
		Assert.assertTrue(output.contains("<d:CustomerWithTermList_address m:type=\"Bag(CustomerServiceTestModel.CustomerWithTermList_address)\">"));
		Assert.assertTrue(output.contains("<d:CustomerWithTermList_street m:type=\"CustomerServiceTestModel.CustomerWithTermList_street\">"));
	}
	
	@Test
	public void testWriteComplexEntryWithAllList() {
		// Get UriInfo and Links
		UriInfo uriInfo = mock(UriInfo.class);
		try {
			when(uriInfo.getBaseUri()).thenReturn(new URI("http", "//www.temenos.com/iris/test", "complex2"));
		} catch (Exception e) {
			fail(e.getMessage());
		}
		List<Link> links = new ArrayList<Link>();
				
		AtomEntityEntryFormatWriter writer = new AtomEntityEntryFormatWriter();
		StringWriter strWriter = new StringWriter();
		writer.write(uriInfo, strWriter, complexEntity2, complexEntityMetadata2, links, modelName);
		
		String output = strWriter.toString();
		//System.out.println(strWriter);
		
		// Lets check if we have represented the Entry successfully 
		Assert.assertTrue(output.contains("<d:CustomerAllTermList_address m:type=\"Bag(CustomerServiceTestModel.CustomerAllTermList_address)\">"));
		Assert.assertTrue(output.contains("<d:CustomerAllTermList_street m:type=\"Bag(CustomerServiceTestModel.CustomerAllTermList_street)\">"));
	}
		
	private static Entity getSimpleEntity(String entityName) {
		EntityProperties props = new EntityProperties();
		props.setProperty(new EntityProperty("name", "SomeName"));
		props.setProperty(new EntityProperty("dateOfBirth", new Date()));
		props.setProperty(new EntityProperty("sector", "Finance"));
		props.setProperty(new EntityProperty("industry", "Banking"));
		props.setProperty(new EntityProperty("loyal", "true"));
		props.setProperty(new EntityProperty("loyalty_rating", 10));
		return new Entity(entityName, props);
	}
	
	private static Entity getComplexEntity(String entityName) {
		EntityProperties props = new EntityProperties();
		props.setProperty(new EntityProperty("name", "SomeName"));
		
		// Addressess
		List<EntityProperties> addGroup = new ArrayList<EntityProperties>();
		
		// Address 1
		EntityProperties addGroup1 = new EntityProperties();
		addGroup1.setProperty(new EntityProperty("number", 2));
		List<EntityProperties> add1StreetGroup = new ArrayList<EntityProperties>();
		EntityProperties addGroup1Street1 = new EntityProperties();
		addGroup1Street1.setProperty(new EntityProperty("streetType", "Peoples Building"));
		add1StreetGroup.add(addGroup1Street1);
		EntityProperties addGroup1Street2 = new EntityProperties();
		addGroup1Street2.setProperty(new EntityProperty("streetType", "Mayland's Avenue"));
		add1StreetGroup.add(addGroup1Street2);
		addGroup1.setProperty(new EntityProperty("street", add1StreetGroup));
		addGroup1.setProperty(new EntityProperty("town", "Hemel Hempstead"));
		addGroup1.setProperty(new EntityProperty("postCode", "HP2 4NW"));
		addGroup.add(addGroup1);
		
		// Address2
		EntityProperties addGroup2 = new EntityProperties();
		addGroup2.setProperty(new EntityProperty("number", 2));
		List<EntityProperties> add2StreetGroup = new ArrayList<EntityProperties>();
		EntityProperties addGroup2Street1 = new EntityProperties();
		addGroup2Street1.setProperty(new EntityProperty("streetType", "Mayland's Avenue"));
		add2StreetGroup.add(addGroup2Street1);
		addGroup2.setProperty(new EntityProperty("street", add2StreetGroup));
		addGroup2.setProperty(new EntityProperty("town", "Hemel Hempstead"));
		addGroup2.setProperty(new EntityProperty("postCode", "HP2 4NW"));
		addGroup.add(addGroup2);
		
		props.setProperty(new EntityProperty("address", addGroup));
		
		props.setProperty(new EntityProperty("dateOfBirth",  new Date()));
		props.setProperty(new EntityProperty("sector", "Finance"));
		props.setProperty(new EntityProperty("industry", "Banking"));
		props.setProperty(new EntityProperty("loyal", "true"));
		props.setProperty(new EntityProperty("loyalty_rating", 10));
		return new Entity(entityName, props);
	}
}
