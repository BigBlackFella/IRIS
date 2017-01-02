package com.temenos.useragent.generic.mediatype;

/*
 * #%L
 * useragent-generic-java
 * %%
 * Copyright (C) 2012 - 2016 Temenos Holdings N.V.
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


import static org.junit.Assert.*;

import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.temenos.useragent.generic.Entity;
import com.temenos.useragent.generic.Link;
import com.temenos.useragent.generic.internal.EntityWrapper;
import com.temenos.useragent.generic.mediatype.AtomPayloadHandler;

public class AtomPayloadHandlerTest {

	private AtomPayloadHandler handler = new AtomPayloadHandler();

	@Test
	public void testIsCollectionForTrue() throws Exception {
		handler.setPayload(AtomPayloadHandler.class
				.getResourceAsStream("/atom_feed_with_single_entry.txt"));
		assertTrue(handler.isCollection());
	}

	@Test
	public void testIsCollectionForFalse() throws Exception {
		handler.setPayload(AtomPayloadHandler.class
				.getResourceAsStream("/atom_entry_with_xml_content.txt"));
		assertFalse(handler.isCollection());
	}

	@Test
	public void testSetPayloadForNull() {
		try {
			handler.setPayload(null);
			fail("Should have thrown IllegalArgumentException");
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}
	}

	@Test
	public void testSetPayloadForInvalidXmlContent() {
		try {
			handler
					.setPayload(IOUtils.toInputStream("<some><valid><xml><but><invalid><atom-xml>foo</atom-xml></invalid></but></xml></valid></some>"));
			fail("Should have thrown IllegalArgumentException");
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}
	}

	@Test
	public void testSetPayloadForInvalidTextContent() {
		try {
			handler.setPayload(IOUtils.toInputStream("foo"));
			fail("Should have thrown IllegalArgumentException");
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}
	}

	@Test
	public void testSetPayloadForValidFeed() throws Exception {
		handler.setPayload(AtomPayloadHandler.class
				.getResourceAsStream("/atom_feed_with_single_entry.txt"));
		assertTrue(handler.isCollection());
	}

	@Test
	public void testGetLinksForCollection() throws Exception {
		handler.setPayload(AtomPayloadHandler.class
				.getResourceAsStream("/atom_feed_with_single_entry.txt"));
		List<Link> links = handler.links();
		assertEquals(2, links.size());

		// first 'self' link
		Link firstLink = links.get(0);
		assertEquals("self", firstLink.rel());
		assertEquals("Customers()", firstLink.href());
		assertEquals("Customers", firstLink.title());
		assertEquals("", firstLink.description());
		assertFalse(firstLink.hasEmbeddedPayload());

		// second 'new' link
		Link secondLink = links.get(1);
		assertEquals("http://mybank/rels/new", secondLink.rel());
		assertEquals("Customers()/new", secondLink.href());
		assertEquals("create new deal", secondLink.title());
		assertEquals("http://schemas.microsoft.com/ado/2007/08/dataservices/related/Customer", secondLink.description());
		assertFalse(secondLink.hasEmbeddedPayload());
	}

	@Test
	public void testGetLinksForEntity() throws Exception {
		handler.setPayload(AtomPayloadHandler.class
				.getResourceAsStream("/atom_entry_with_xml_content.txt"));
		List<Link> links = handler.links();
		assertEquals(4, links.size());

		// first 'self' link
		Link firstLink = links.get(0);
		assertEquals("self", firstLink.rel());
		assertEquals("Customers('100974')", firstLink.href());
		assertEquals("Customer", firstLink.title());
		assertEquals("", firstLink.description());
		assertFalse(firstLink.hasEmbeddedPayload());

		// second 'see' link
		Link secondLink = links.get(1);
		assertEquals("http://mybank/rels/see", secondLink.rel());
		assertEquals("Customers('100974')/see", secondLink.href());
		assertEquals("see record", secondLink.title());
		assertEquals("http://schemas.microsoft.com/ado/2007/08/dataservices/related/Customer", secondLink.description());
		assertFalse(secondLink.hasEmbeddedPayload());

		// third 'input' link
		Link thirdLink = links.get(2);
		assertEquals("http://mybank/rels/input", thirdLink.rel());
		assertEquals("Customers('100974')/input", thirdLink.href());
		assertEquals("input deal", thirdLink.title());
		assertEquals("", thirdLink.description());
		assertFalse(thirdLink.hasEmbeddedPayload());

		// fourth 'review' link
		Link fourthLink = links.get(3);
		assertEquals("http://mybank/rels/review", fourthLink.rel());
		assertEquals("Customers('100974')/review", fourthLink.href());
		assertEquals("audit deal", fourthLink.title());
		assertEquals("", fourthLink.description());
		assertFalse(fourthLink.hasEmbeddedPayload());
	}

	@Test
	public void testEntities() throws Exception {
		handler.setPayload(AtomPayloadHandler.class
				.getResourceAsStream("/atom_feed_with_single_entry.txt"));
		List<EntityWrapper> entities = handler.entities();
		assertEquals(1, entities.size());
		assertNotNull(entities.get(0));
	}
}
