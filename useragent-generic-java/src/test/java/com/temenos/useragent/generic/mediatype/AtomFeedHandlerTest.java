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

public class AtomFeedHandlerTest {

	private AtomPayloadHandler transformer = new AtomPayloadHandler();

	@Test
	public void testIsCollectionForTrue() throws Exception {
		transformer.setPayload(IOUtils.toString(AtomPayloadHandler.class
				.getResourceAsStream("/atom_feed_with_single_entry.txt")));
		assertTrue(transformer.isCollection());
	}

	@Test
	public void testIsCollectionForFalse() throws Exception {
		transformer.setPayload(IOUtils.toString(AtomPayloadHandler.class
				.getResourceAsStream("/atom_entry_with_xml_content.txt")));
		assertFalse(transformer.isCollection());
	}

	@Test
	public void testSetPayloadForNull() {
		try {
			transformer.setPayload(null);
			fail("Should have thrown IllegalArgumentException");
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}
	}

	@Test
	public void testSetPayloadForInvalidXmlContent() {
		try {
			transformer
					.setPayload("<some><valid><xml><but><invalid><atom-xml>foo</atom-xml></invalid></but></xml></valid></some>");
			fail("Should have thrown IllegalArgumentException");
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}
	}

	@Test
	public void testSetPayloadForInvalidTextContent() {
		try {
			transformer.setPayload("foo");
			fail("Should have thrown IllegalArgumentException");
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}
	}

	@Test
	public void testSetPayloadForValidFeed() throws Exception {
		transformer.setPayload(IOUtils.toString(AtomPayloadHandler.class
				.getResourceAsStream("/atom_feed_with_single_entry.txt")));
		assertTrue(transformer.isCollection());
	}

	@Test
	public void testGetLinks() throws Exception {
		transformer.setPayload(IOUtils.toString(AtomPayloadHandler.class
				.getResourceAsStream("/atom_feed_with_single_entry.txt")));
		List<Link> links = transformer.links();
		assertEquals(2, links.size());

		// first 'self' link
		Link firstLink = links.get(0);
		assertEquals("self", firstLink.rel());
		assertEquals("Customers()", firstLink.href());
		assertFalse(firstLink.hasEmbeddedPayload());

		// second 'new' link
		Link secondLink = links.get(1);
		assertEquals("http://mybank/rels/new", secondLink.rel());
		assertEquals("Customers()/new", secondLink.href());
		assertFalse(secondLink.hasEmbeddedPayload());
	}

	@Test
	public void testEntities() throws Exception {
		transformer.setPayload(IOUtils.toString(AtomPayloadHandler.class
				.getResourceAsStream("/atom_feed_with_single_entry.txt")));
		List<EntityWrapper> entities = transformer.entities();
		assertEquals(1, entities.size());
		Entity entity = entities.get(0);
//		assertEquals(4, entity.links().all().size());
	}
}
