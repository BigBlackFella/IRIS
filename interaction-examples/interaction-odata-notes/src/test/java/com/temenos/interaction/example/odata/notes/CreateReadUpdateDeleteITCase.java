package com.temenos.interaction.example.odata.notes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperties;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.test.framework.JerseyTest;

public class CreateReadUpdateDeleteITCase extends JerseyTest {

	public final static String PERSONS_RESOURCE = "/Person";
	public final static String NOTES_RESOURCE = "/Note";
	
	private final static String NOTE_ENTITYSET_NAME = "Note";
	private final static String PERSON_ENTITYSET_NAME = "Person";

	@Before
	public void initTest() {
		// TODO make this configurable
		// test with external server 
    	webResource = Client.create().resource(Configuration.TEST_ENDPOINT_URI); 

    	// Create note 3, linked to person 2 if it doesn't exist
		ODataConsumer consumer = ODataJerseyConsumer.newBuilder(Configuration.TEST_ENDPOINT_URI).build();
		OEntity person = consumer.getEntity(PERSON_ENTITYSET_NAME, 2).execute();
		if (person == null) {
			person = consumer
						.createEntity(PERSON_ENTITYSET_NAME)
						.properties(OProperties.string("name", "Ron"))
						.execute();
		}
		OEntity note = consumer.getEntity(NOTE_ENTITYSET_NAME, 3).execute();
		if (note == null) {
			note = consumer
					.createEntity(NOTE_ENTITYSET_NAME)
					.properties(OProperties.string("body", "test"))
					.link("NotePerson", person)
					.execute();
		}		
	}
	
	@After
	public void tearDown() {}

    public CreateReadUpdateDeleteITCase() throws Exception {
    	/* Allows standalone Jersey Test
    	super("example", "rest", "com.temenos.interaction.example");
		*/
        // enable logging on base web resource
    	System.setProperty("enableLogging", "ya");
    }
    
    @Test
	public void testOptions() {
        String noteUri = NOTES_RESOURCE + "(1)";
        ClientResponse response = webResource.path(noteUri).options(ClientResponse.class);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        assertEquals(5, response.getAllow().size());
        assertTrue(response.getAllow().contains("GET"));
        assertTrue(response.getAllow().contains("PUT"));
        assertTrue(response.getAllow().contains("DELETE"));
        assertTrue(response.getAllow().contains("OPTIONS"));
        assertTrue(response.getAllow().contains("HEAD"));
	}

    @Test
	public void testDeleteNote() {
		String noteUri = NOTES_RESOURCE + "(3)";

        // delete Note number 3 (which should now exists see initTest)
		ClientResponse response = webResource.path(noteUri).delete(ClientResponse.class);
        assertEquals(204, response.getStatus());

		// make sure Note number 3 is really gone
		ClientResponse deletedResponse = webResource.path(noteUri).get(ClientResponse.class);
        assertEquals(404, deletedResponse.getStatus());

		// delete Note number 56 (which does not exist)
		String notFoundNoteUri = NOTES_RESOURCE + "(56)";
		ClientResponse nresponse = webResource.path(notFoundNoteUri).delete(ClientResponse.class);
        assertEquals(204, nresponse.getStatus());
    }

    @Test
	public void testDeletePerson() {
		// delete Person number 1 (which exists), but we have bound a NoopDELETECommand
		String noteUri = PERSONS_RESOURCE + "(1)";
		ClientResponse response = webResource.path(noteUri).delete(ClientResponse.class);
        assertEquals(405, response.getStatus());
    }

    @Test
    public void testCreate() {
		ODataConsumer consumer = ODataJerseyConsumer.newBuilder(Configuration.TEST_ENDPOINT_URI).build();
		OEntity person = consumer.createEntity(PERSON_ENTITYSET_NAME)
				.properties(OProperties.string("name", "Ron"))
				.execute();

		assertTrue(person != null);
    }
    
    @Test
	public void testDelete() {
		ODataConsumer consumer = ODataJerseyConsumer.newBuilder(Configuration.TEST_ENDPOINT_URI).build();
		
		// find a person
		OEntity person = consumer.getEntity(PERSON_ENTITYSET_NAME, 2).execute();
		if (person == null) {
			person = consumer
						.createEntity(PERSON_ENTITYSET_NAME)
						.properties(OProperties.string("name", "Ron"))
						.execute();
		}
		
		// create a note
		OEntity note = consumer.getEntity(NOTE_ENTITYSET_NAME, 6).execute();
		if (note == null) {
			note = consumer
					.createEntity(NOTE_ENTITYSET_NAME)
					.properties(OProperties.string("body", "test"))
					.link("NotePerson", person)
					.execute();
		}		
		
		// delete one note
		consumer.deleteEntity(note).execute();

		// check its deleted
		OEntity afterDelete = consumer.getEntity(note).execute();
		assertEquals(null, afterDelete);
    }

    // TODO AtomXMLProvider needs better support for matching of URIs to resources
    @Test
    public void testUpdate() {
    	// Create note for person 1
		ODataConsumer consumer = ODataJerseyConsumer.newBuilder(Configuration.TEST_ENDPOINT_URI).build();
		OEntity person = consumer.getEntity(PERSON_ENTITYSET_NAME, 1).execute();
		OEntity note = consumer
					.createEntity(NOTE_ENTITYSET_NAME)
					.properties(OProperties.string("body", "test"))
					.link("NotePerson", person)
					.execute();
		// update the note text
		consumer.updateEntity(note)
				.properties(OProperties.string("body", "new text for note"))
				.execute();

		// read the note again, check text
		OEntity afterUpdate = consumer.getEntity(note).execute();
		assertEquals("new text for note", afterUpdate.getProperty("body").getValue());
		
    }
}
