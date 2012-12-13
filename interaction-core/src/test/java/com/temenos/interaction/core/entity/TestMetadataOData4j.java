package com.temenos.interaction.core.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.edm.EdmAssociation;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.edm.EdmType;

import com.temenos.interaction.core.hypermedia.Action;
import com.temenos.interaction.core.hypermedia.CollectionResourceState;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.hypermedia.ResourceStateMachine;

public class TestMetadataOData4j {
	public final static String METADATA_XML_FILE = "TestMetadataParser.xml";
	public final static String METADATA_AIRLINE_XML_FILE = "AirlinesMetadata.xml";

	private static String AIRLINE_NAMESPACE = "FlightResponderModel";
	private static Metadata metadataAirline;
	private static MetadataOData4j metadataOdata4j;
	private static MetadataOData4j metadataAirlineOdata4j;
	
	@BeforeClass
	public static void setup()
	{
		//Read the metadata file
		MetadataParser parser = new MetadataParser();
		InputStream is = parser.getClass().getClassLoader().getResourceAsStream(METADATA_XML_FILE);
		Metadata metadata = parser.parse(is);
		Assert.assertNotNull(metadata);
		
		//Convert metadata to odata4j metadata
		metadataOdata4j = new MetadataOData4j(metadata, new ResourceStateMachine(new ResourceState("SD", "initial", new ArrayList<Action>(), "/")));

		// Create mock state machine with entity sets
		ResourceState serviceRoot = new ResourceState("SD", "initial", new ArrayList<Action>(), "/");
		serviceRoot.addTransition(new CollectionResourceState("FlightSchedule", "FlightSchedule", new ArrayList<Action>(), "/FlightSchedule"));
		serviceRoot.addTransition(new CollectionResourceState("Flight", "Flight", new ArrayList<Action>(), "/Flight"));
		serviceRoot.addTransition(new CollectionResourceState("Airport", "Airport", new ArrayList<Action>(), "/Airline"));
		ResourceStateMachine hypermediaEngine = new ResourceStateMachine(serviceRoot);

		//Read the airline metadata file
		MetadataParser parserAirline = new MetadataParser();
		InputStream isAirline = parserAirline.getClass().getClassLoader().getResourceAsStream(METADATA_AIRLINE_XML_FILE);
		metadataAirline = parserAirline.parse(isAirline);
		Assert.assertNotNull(metadataAirline);
		
		//Convert metadata to odata4j metadata
		metadataAirlineOdata4j = new MetadataOData4j(metadataAirline, hypermediaEngine);
	}
	
	@Test(expected = AssertionError.class)
	public void testAssertIndividualInitialState() {
		CollectionResourceState serviceRoot = new CollectionResourceState("SD", "initial", new ArrayList<Action>(), "/");
		ResourceStateMachine hypermediaEngine = new ResourceStateMachine(serviceRoot);
		new MetadataOData4j(metadataAirline, hypermediaEngine);
	}
	
	@Test
	public void testCustomerEntity()
	{	
		EdmDataServices edmDataServices = metadataOdata4j.getMetadata();
		EdmType type = edmDataServices.findEdmEntityType("CustomerServiceTestModel.Customer");
		Assert.assertNotNull(type);
		Assert.assertTrue(type.getFullyQualifiedTypeName().equals("CustomerServiceTestModel.Customer"));
		Assert.assertTrue(type instanceof EdmEntityType);
		EdmEntityType entityType = (EdmEntityType) type;
		Assert.assertEquals("Customer", entityType.getName());
		Assert.assertEquals(false, entityType.findProperty("dateOfBirth").isNullable());
		Assert.assertEquals(true, entityType.findProperty("postCode").isNullable());
	}

	@Test
	public void testAirlineSchemaCount()
	{	
		EdmDataServices edmDataServices = metadataAirlineOdata4j.getMetadata();
		Assert.assertEquals(1, edmDataServices.getSchemas().size());
	}
	
	@Test
	public void testAirlineEntityTypes()
	{	
		EdmDataServices edmDataServices = metadataAirlineOdata4j.getMetadata();
		EdmType type = edmDataServices.findEdmEntityType(AIRLINE_NAMESPACE + ".FlightSchedule");
		Assert.assertNotNull(type);
		Assert.assertTrue(type.getFullyQualifiedTypeName().equals("FlightResponderModel.FlightSchedule"));
		Assert.assertTrue(type instanceof EdmEntityType);
		EdmEntityType entityType = (EdmEntityType) type;
		Assert.assertEquals("FlightSchedule", entityType.getName());
		Assert.assertEquals(false, entityType.findProperty("flightScheduleID").isNullable());
	}

	@Test
	public void testAirlineEntitySets()
	{	
		EdmDataServices edmDataServices = metadataAirlineOdata4j.getMetadata();
		Assert.assertEquals(1, edmDataServices.getSchemas().size());
		Assert.assertEquals(1, edmDataServices.getSchemas().get(0).getEntityContainers().size());
		Assert.assertEquals(3, edmDataServices.getSchemas().get(0).getEntityContainers().get(0).getEntitySets().size());
		EdmEntitySet entitySetFlightSchedule = edmDataServices.findEdmEntitySet("FlightSchedule");
		Assert.assertEquals("FlightSchedule", entitySetFlightSchedule.getName());
	}
	
	@Test
	public void testManyToOneMandatoryNavProperty() {
		// create mock resource interaction (which should result in creation of mandatory Navigation Property in EdmDataService metadata)
		ResourceState initial = new ResourceState("ROOT", "initial", new ArrayList<Action>(), "/");
		
		// flights and airports
		CollectionResourceState flights = new CollectionResourceState("Flight", "Flights", new ArrayList<Action>(), "/Flights");
		ResourceState flight = new ResourceState("Flight", "flight", new ArrayList<Action>(), "/Flights({id})");
		CollectionResourceState airports = new CollectionResourceState("Airport", "Airports", new ArrayList<Action>(), "/Airports");
		ResourceState airport = new ResourceState("Airport", "airport", new ArrayList<Action>(), "/Airports({id})");
		// a flight must have a departure airport
		ResourceState flightDepartureAirport = new ResourceState("Airport", "departureAirport", new ArrayList<Action>(), "/Flights({id})/departureAirport");
		
		
		initial.addTransition(flights);
		flights.addTransitionForEachItem("GET", flight, new HashMap<String, String>());
		initial.addTransition(airports);
		airports.addTransitionForEachItem("GET", airport, new HashMap<String, String>());
		flight.addTransition(flightDepartureAirport);
		flights.addTransitionForEachItem("GET", flightDepartureAirport, new HashMap<String, String>());
		ResourceStateMachine rsm = new ResourceStateMachine(initial);
		
		MetadataOData4j metadataOData4j = new MetadataOData4j(metadataAirline, rsm);
		EdmDataServices edmMetadata = metadataOData4j.getMetadata();
		
		assertNotNull(edmMetadata);
		// entity types (one with the mandatory nav property)
		EdmEntityType entityType = (EdmEntityType) edmMetadata.findEdmEntityType(AIRLINE_NAMESPACE + ".Flight");
		EdmNavigationProperty flightNavProperty = entityType.findNavigationProperty("departureAirport");
		assertNotNull(flightNavProperty);
		assertEquals("Flight_Airport", flightNavProperty.getRelationship().getName());
		assertEquals("Flight_Airport_Source", flightNavProperty.getFromRole().getRole());
		assertEquals("Flight_Airport_Target", flightNavProperty.getToRole().getRole());
		// check association
		assertEquals("*", flightNavProperty.getFromRole().getMultiplicity().getSymbolString());
		assertEquals("1", flightNavProperty.getToRole().getMultiplicity().getSymbolString());
		
		// associations
		assertNotNull(edmMetadata.getAssociations());
		int noAssociations = 0;
		for (EdmAssociation association : edmMetadata.getAssociations()) {
			noAssociations++;
			if ("Flight_Airport".equals(association.getName())) {
			} else {
				fail("Unexpected association");
			}
		}
		assertEquals(1, noAssociations);
		
		// entity sets (from ResourceStateMachine)
		assertNotNull(edmMetadata.getEntitySets());
		int noEntitySets = 0;
		for (EdmEntitySet entitySet : edmMetadata.getEntitySets()) {
			noEntitySets++;
			if ("Flights".equals(entitySet.getName())) {
			} else if ("Airports".equals(entitySet.getName())) {
			} else {
				fail("Unexpected entity set");
			}
		}
		assertEquals(2, noEntitySets);
	}
	
	@Test
	public void testManyToManyNavProperty() throws Exception {
		// create mock resource interaction (which should result in creation of Navigation Property in EdmDataService metadata)
		ResourceState initial = new ResourceState("ROOT", "initial", new ArrayList<Action>(), "/");
		CollectionResourceState flights = new CollectionResourceState("Flight", "Flights", new ArrayList<Action>(), "/Flights");
		CollectionResourceState airports = new CollectionResourceState("Airport", "Airports", new ArrayList<Action>(), "/Airports");
		CollectionResourceState flightSchedules = new CollectionResourceState("FlightSchedule", "FlightSchedules", new ArrayList<Action>(), "/FlightSchedules");
		ResourceState flight = new ResourceState("Flight", "flight", new ArrayList<Action>(), "/Flights({id})");
		CollectionResourceState airportFlights = new CollectionResourceState("Airport", "AirportFlights", new ArrayList<Action>(), "/Airports({id})/Flights");
		initial.addTransition(flights);
		initial.addTransition(airports);
		initial.addTransition(flightSchedules);
		flight.addTransition("GET", airportFlights, new HashMap<String, String>());
		flights.addTransitionForEachItem("GET", airportFlights, new HashMap<String, String>());
		ResourceStateMachine rsm = new ResourceStateMachine(initial);
		
		MetadataOData4j metadataOData4j = new MetadataOData4j(metadataAirline, rsm);
		EdmDataServices edmMetadata = metadataOData4j.getMetadata();
		
		assertNotNull(edmMetadata);
		
		// entities (from Metadata)
		assertNotNull(edmMetadata.getEntitySets());
		int noEntities = 0;
		for (EdmEntityType entityType : edmMetadata.getEntityTypes()) {
			noEntities++;
			if ("Flight".equals(entityType.getName())) {
			} else if ("Airport".equals(entityType.getName())) {
			} else if ("FlightSchedule".equals(entityType.getName())) {
			} else {
				fail("Unexpected entity");
			}
		}
		assertEquals(3, noEntities);

		// entity sets (from ResourceStateMachine)
		assertNotNull(edmMetadata.getEntitySets());
		int noEntitySets = 0;
		for (EdmEntitySet entitySet : edmMetadata.getEntitySets()) {
			noEntitySets++;
			if ("Flights".equals(entitySet.getName())) {
			} else if ("Airports".equals(entitySet.getName())) {
			} else if ("FlightSchedules".equals(entitySet.getName())) {
			} else {
				fail("Unexpected entity set");
			}
		}
		assertEquals(3, noEntitySets);

		// function imports (from ResourceStateMachine where transition to Collection not from initial state)
		assertNotNull(edmMetadata.findEdmFunctionImport("AirportFlights"));
	}

}
