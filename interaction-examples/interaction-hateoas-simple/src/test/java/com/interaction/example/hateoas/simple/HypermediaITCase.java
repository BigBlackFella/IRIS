package com.interaction.example.hateoas.simple;

/*
 * #%L
 * interaction-example-hateoas-simple
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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.test.framework.JerseyTest;
import com.temenos.interaction.media.hal.MediaType;
import com.theoryinpractise.halbuilder.RepresentationFactory;
import com.theoryinpractise.halbuilder.spi.Link;
import com.theoryinpractise.halbuilder.spi.ReadableRepresentation;

/**
 * This test ensures that we can navigate from one application state
 * to another using hypermedia (links).
 * 
 * @author aphethean
 */
public class HypermediaITCase extends JerseyTest {

	public HypermediaITCase() throws Exception {
		super();
	}
	
	@Before
	public void initTest() {
		// TODO make this configurable
		// test with external server 
    	webResource = Client.create().resource(Configuration.TEST_ENDPOINT_URI); 
	}

	@After
	public void tearDown() {}


	@Test
	public void testGetEntryPointLinks() {
		ClientResponse response = webResource.path("/").accept(MediaType.APPLICATION_HAL_JSON).get(ClientResponse.class);
        assertEquals(Response.Status.Family.SUCCESSFUL, Response.Status.fromStatusCode(response.getStatus()).getFamily());

		RepresentationFactory representationFactory = new RepresentationFactory();
		ReadableRepresentation resource = representationFactory.readRepresentation(new InputStreamReader(response.getEntityInputStream()));

		List<Link> links = resource.getLinks();
		assertEquals(4, links.size());
		for (Link link : links) {
			if (link.getRel().equals("self") && link.getName().get().equals("HOME.home>GET>HOME.home")) {
				assertEquals(Configuration.TEST_ENDPOINT_URI + "/", link.getHref());
			} else if (link.getName().get().equals("HOME.home>GET>Preferences.preferences")) {
				assertEquals(Configuration.TEST_ENDPOINT_URI + "/preferences", link.getHref());
			} else if (link.getName().get().equals("HOME.home>GET>Profile.profile")) {
				assertEquals(Configuration.TEST_ENDPOINT_URI + "/profile", link.getHref());
			} else if (link.getName().get().equals("HOME.home>GET>Note.notes")) {
				assertEquals(Configuration.TEST_ENDPOINT_URI + "/notes", link.getHref());
			} else {
				fail("unexpected link [" + link.getName().get() + "]");
			}
		}
	}
	
	@Test
	public void testCollectionLinks() {
		ClientResponse response = webResource.path("/notes").accept(MediaType.APPLICATION_HAL_JSON).get(ClientResponse.class);
        assertEquals(Response.Status.Family.SUCCESSFUL, Response.Status.fromStatusCode(response.getStatus()).getFamily());

		RepresentationFactory representationFactory = new RepresentationFactory();
		ReadableRepresentation resource = representationFactory.readRepresentation(new InputStreamReader(response.getEntityInputStream()));

		// the links from the collection
		List<Link> links = resource.getLinks();
		assertEquals(2, links.size());
		for (Link link : links) {
			if (link.getRel().equals("self")) {
				assertEquals(Configuration.TEST_ENDPOINT_URI + "/notes", link.getHref());
			} else if (link.getName().get().equals("Note.notes>POST>ID.newNote")) {
				assertEquals("POST " + Configuration.TEST_ENDPOINT_URI + "/notes/new", link.getHref());
			} else {
				fail("unexpected link [" + link.getName().get() + "]");
			}
		}
		
		// the items, and links on each item
		Collection<ReadableRepresentation> subresources = resource.getResources().values();
		assertNotNull(subresources);
		/*
		 * Test that there are actually some subresource returned.  If the 'self' link rel in
		 * the HALProvider is broken then we won't get any subresources here.
		 */
		assertTrue(subresources.size() > 0);
		for (ReadableRepresentation item : subresources) {
			List<Link> itemLinks = item.getLinks();
			assertEquals(2, itemLinks.size());
			for (Link link : itemLinks) {
				if (link.getRel().contains("self")) {
					assertEquals(Configuration.TEST_ENDPOINT_URI + "/notes/" + item.getProperties().get("noteID").get(), link.getHref());
				} else if (link.getName().get().contains("Note.deletedNote")) {
					assertEquals("DELETE " + Configuration.TEST_ENDPOINT_URI + "/notes/" + item.getProperties().get("noteID").get(), link.getHref());
				} else {
					fail("unexpected link [" + link.getName().get() + "]");
				}
			}
		}
	}

	/**
	 * Found a small issue where a GET to a non-existent resource still generated the links and this
	 * resulted in a server side error (500)
	 */
	@Test
	public void testGET404() {
		ClientResponse response = webResource.path("/notes/666").accept(MediaType.APPLICATION_HAL_JSON).get(ClientResponse.class);
        assertEquals(404, response.getStatus());
	}

	@Test
	public void testFollowDeleteItemLink() {
		ClientResponse response = webResource.path("/notes").accept(MediaType.APPLICATION_HAL_JSON).get(ClientResponse.class);
        assertEquals(Response.Status.Family.SUCCESSFUL, Response.Status.fromStatusCode(response.getStatus()).getFamily());

		RepresentationFactory representationFactory = new RepresentationFactory();
		ReadableRepresentation resource = representationFactory.readRepresentation(new InputStreamReader(response.getEntityInputStream()));
		
		// the items in the collection
		Collection<ReadableRepresentation> subresources = resource.getResources().values();
		assertNotNull(subresources);
		
		// follow the link to delete the first in the collection
		if (subresources.size() == 0) {
			// we might have run the integration tests more times than we have rows in our table
		} else {
			ReadableRepresentation item = subresources.iterator().next();
			List<Link> itemLinks = item.getLinks();
			assertEquals(2, itemLinks.size());
			Link deleteLink = null;
			for (Link link : itemLinks) {
				if (link.getName().isPresent() && link.getName().get().contains("Note.notes>DELETE>Note.deletedNote")) {
					deleteLink = link;
				}
			}
			assertNotNull(deleteLink);
			String[] hrefElements = deleteLink.getHref().split(" ");
			String method = hrefElements[0];
			assertEquals("DELETE", method);
			String uri = hrefElements[1];

			// create http client
			Client client = Client.create();
			// do not follow the Location redirect
			client.setFollowRedirects(false);
			// execute delete without custom link relation, will find the only DELETE transition from entity
			ClientResponse deleteResponse = client.resource(uri).accept(MediaType.APPLICATION_HAL_JSON)
					.delete(ClientResponse.class);
	        // 303 "See Other" instructs user agent to fetch another resource as specified by the 'Location' header
	        assertEquals(303, deleteResponse.getStatus());
	        assertEquals(Configuration.TEST_ENDPOINT_URI + "/notes", deleteResponse.getHeaders().getFirst("Location"));
		}
	}

	@Test
	public void testFollowDeleteItemLinkRelation() {
		ClientResponse response = webResource.path("/notes").accept(MediaType.APPLICATION_HAL_JSON).get(ClientResponse.class);
        assertEquals(Response.Status.Family.SUCCESSFUL, Response.Status.fromStatusCode(response.getStatus()).getFamily());

		RepresentationFactory representationFactory = new RepresentationFactory();
		ReadableRepresentation resource = representationFactory.readRepresentation(new InputStreamReader(response.getEntityInputStream()));
		
		// the items in the collection
		Collection<ReadableRepresentation> subresources = resource.getResources().values();
		assertNotNull(subresources);
		
		// follow the link to delete the first in the collection
		if (subresources.size() == 0) {
			// we might have run the integration tests more times than we have rows in our table
		} else {
			ReadableRepresentation item = subresources.iterator().next();
			List<Link> itemLinks = item.getLinks();
			assertEquals(2, itemLinks.size());
			Link deleteLink = null;
			for (Link link : itemLinks) {
				if (link.getName().isPresent() && link.getName().get().contains("Note.notes>DELETE>Note.deletedNote")) {
					deleteLink = link;
				}
			}
			assertNotNull(deleteLink);
			String[] hrefElements = deleteLink.getHref().split(" ");
			String method = hrefElements[0];
			assertEquals("DELETE", method);
			String uri = hrefElements[1];

			// execute delete with custom link relation (see rfc5988)
			ClientResponse deleteResponse = Client.create().resource(uri)
					.header("Link", "<" + uri + ">; rel=\"" + deleteLink.getName().get() + "\"")
					.accept(MediaType.APPLICATION_HAL_JSON)
					.delete(ClientResponse.class);
	        // 205 "Reset Content" instructs user agent to reload the resource that contained this link
	        assertEquals(205, deleteResponse.getStatus());
		}
	}
	
	@Test
	public void testFollowDeleteLinkWithLinkRel() {
		ClientResponse response = webResource.path("/notes").accept(MediaType.APPLICATION_HAL_JSON).get(ClientResponse.class);
        assertEquals(Response.Status.Family.SUCCESSFUL, Response.Status.fromStatusCode(response.getStatus()).getFamily());

		RepresentationFactory representationFactory = new RepresentationFactory();
		ReadableRepresentation resource = representationFactory.readRepresentation(new InputStreamReader(response.getEntityInputStream()));
		
		// the items in the collection
		Collection<ReadableRepresentation> subresources = resource.getResources().values();
		assertNotNull(subresources);
		
		// follow the link to delete the first in the collection
		if (subresources.size() == 0) {
			// we might have run the integration tests more times than we have rows in our table
		} else {
			ReadableRepresentation item = subresources.iterator().next();
			List<Link> itemLinks = item.getLinks();
			assertEquals(2, itemLinks.size());
			
			// GET item link (Note.notes->Note.note)
			Link getLink = null;
			for (Link link : itemLinks) {
				if (link.getRel().contains("self")) {
					getLink = link;
				}
			}
			// follow GET item link
			assertNotNull(getLink);
			ClientResponse getResponse = Client.create().resource(getLink.getHref()).accept(MediaType.APPLICATION_HAL_JSON).get(ClientResponse.class);
	        assertEquals(Response.Status.Family.SUCCESSFUL, Response.Status.fromStatusCode(getResponse.getStatus()).getFamily());
			// the item
			ReadableRepresentation itemResource = representationFactory.readRepresentation(new InputStreamReader(getResponse.getEntityInputStream()));
			List<Link> links = itemResource.getLinks();
			assertNotNull(links);
			/*
			 * 2 links.  
			 * One to 'self'
			 * One to 'item' which contains DELETE in href (this should be changed to 'edit' rel) 
			 */
			assertEquals(2, links.size());
			
			// DELETE item link (Note.note>Note.deletedNote, Note.deletedNote is an auto transition to Note.notes)
			Link deleteLink = null;
			for (Link link : links) {
				if (link.getName().get().contains("Note.note>DELETE>Note.deletedNote")) {
					deleteLink = link;
				}
			}
			assertNotNull(deleteLink);
			String[] hrefElements = deleteLink.getHref().split(" ");
			String method = hrefElements[0];
			assertEquals("DELETE", method);
			String uri = hrefElements[1];

			// create http client
			Client client = Client.create();
			// do not follow the Location redirect
			client.setFollowRedirects(false);
			// execute delete with custom link relation (see rfc5988)
			ClientResponse deleteResponse = client.resource(uri)
					.header("Link", "<" + uri + ">; rel=\"" + deleteLink.getName().get() + "\"")
					.accept(MediaType.APPLICATION_HAL_JSON)
					.delete(ClientResponse.class);
	        // 303 "See Other" instructs user agent to fetch another resource as specified by the 'Location' header
	        assertEquals(303, deleteResponse.getStatus());
	        assertEquals(Configuration.TEST_ENDPOINT_URI + "/notes", deleteResponse.getHeaders().getFirst("Location"));
		}
	}

	/**
	 * Attempt a DELETE to the notes resource (a collection resource)
	 */
	@Test
	public void deletePersonMethodNotAllowed() throws Exception {
		// attempt to delete the Person root, rather than an individual
		ClientResponse response = webResource.path("/notes").delete(ClientResponse.class);
        assertEquals(405, response.getStatus());

        assertEquals(3, response.getAllow().size());
        assertTrue(response.getAllow().contains("GET"));
        assertTrue(response.getAllow().contains("OPTIONS"));
        assertTrue(response.getAllow().contains("HEAD"));
	}

	/**
	 * Attempt a PUT an invalid notes resource (a collection resource)
	 */
	@Test
	public void putPersonBadRequest() throws Exception {
		String halRequest = "{}";
		// attempt to put to the notes collection, rather than an individual
		ClientResponse response = webResource.path("/notes").type(MediaType.APPLICATION_HAL_JSON).put(ClientResponse.class, halRequest);
        assertEquals(400, response.getStatus());
	}

}
