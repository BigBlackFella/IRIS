package com.temenos.useragent.generic.mediatype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.temenos.useragent.generic.Entity;
import com.temenos.useragent.generic.Link;
import com.temenos.useragent.generic.internal.EntityWrapper;
import com.temenos.useragent.generic.internal.NullEntityWrapper;

public class HalJsonPayloadHandlerTest {

	private HalJsonPayloadHandler payloadHandler;

	@Before
	public void setUp() {
		initPayloadHandler("/haljson_collection_with_two_items.json");
	}

	@Test
	public void testIsCollectionCollection() {
		assertTrue(payloadHandler.isCollection());
	}

	@Test
	public void testIsCollectionForItem() {
		initPayloadHandler("/haljson_item_with_all_properties.json");
		assertFalse(payloadHandler.isCollection());
	}

	@Test
	public void testLinks() {
		List<Link> links = payloadHandler.links();
		assertEquals(2, links.size());

		Link selfLink = links.get(0);
		assertEquals("self", selfLink.rel());
		assertEquals("http://mybank/restservice/BankService/Customers()",
				selfLink.href());
		assertEquals("", selfLink.title());

		Link newLink = links.get(1);
		assertEquals("http://temenostech.temenos.com/rels/new", newLink.rel());
		assertEquals("http://mybank/restservice/BankService/Customers()/new",
				newLink.href());
		assertEquals("create new deal", newLink.title());
	}

	@Test
	public void testEntities() {
		List<EntityWrapper> entities = payloadHandler.entities();
		assertEquals(2, entities.size());
		Entity firstEntity = entities.get(0);
		assertEquals("2002", firstEntity.get("AccountOfficer"));
		assertEquals(2, firstEntity.count("OverrideGroup"));
		assertEquals("FORM XTP NOT RECEIVED",
				firstEntity.get("OverrideGroup(0)/Override"));
		assertEquals("MEMORANDUM NOT RECEIVED",
				firstEntity.get("OverrideGroup(1)/Override"));

		Entity secondEntity = entities.get(1);
		assertEquals("2001", secondEntity.get("AccountOfficer"));
		assertEquals(3, secondEntity.count("OverrideGroup"));
		assertEquals("PASSPORT NOT VERIFIED",
				secondEntity.get("OverrideGroup(0)/Override"));
		assertEquals("FORM NOT RECEIVED",
				secondEntity.get("OverrideGroup(1)/Override"));
		assertEquals("MEMORANDUM NOT RECEIVED",
				secondEntity.get("OverrideGroup(2)/Override"));
	}

	@Test
	public void testEntity() {
		Entity nullEntity = payloadHandler.entity();
		assertTrue(nullEntity instanceof NullEntityWrapper);

		initPayloadHandler("/haljson_item_with_all_properties.json");
		assertNotNull(payloadHandler.entity());
		assertEquals("2002", payloadHandler.entity().get("AccountOfficer"));
	}

	@Test
	public void testSetPayload() {
		try {
			payloadHandler.setPayload(null);
			fail("IllegalArgumentException should be thrown");
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}
	}

	private void initPayloadHandler(String jsonFileName) {
		payloadHandler = new HalJsonPayloadHandler();
		try {
			payloadHandler.setPayload(IOUtils
					.toString(HalJsonPayloadHandlerTest.class
							.getResourceAsStream(jsonFileName)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
