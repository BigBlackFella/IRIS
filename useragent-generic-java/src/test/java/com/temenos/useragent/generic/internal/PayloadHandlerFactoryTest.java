package com.temenos.useragent.generic.internal;

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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.temenos.useragent.generic.Link;
import com.temenos.useragent.generic.PayloadHandler;

public class PayloadHandlerFactoryTest {

	@Test
	public void testCreateFactoryForValidHandler() {
		PayloadHandlerFactory factory = PayloadHandlerFactory
				.createFactory(MockPayloadHandler.class);
		assertNotNull(factory);
	}

	@Test
	public void testCreateFactoryForInvalidHandler() {
		try {
			PayloadHandlerFactory factory = PayloadHandlerFactory
					.createFactory(null);
			fail("IllegalArgumentException should have thrown");
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}
	}

	@Test
	public void testCreateHandler() throws Exception {
		PayloadHandlerFactory factory = PayloadHandlerFactory
				.createFactory(MockPayloadHandler.class);
		MockPayloadHandler handler = (MockPayloadHandler) factory
				.createHandler(IOUtils.toInputStream("Test payload","UTF-8"));
		assertEquals("Test payload", handler.getPayload());
	}

	public static class MockPayloadHandler implements PayloadHandler {

		private InputStream payload;

		@Override
		public boolean isCollection() {
			return false;
		}

		@Override
		public List<Link> links() {
			return Collections.emptyList();
		}

		@Override
		public List<EntityWrapper> entities() {
			return Collections.emptyList();
		}

		@Override
		public EntityWrapper entity() {
			return null;
		}

		@Override
		public void setPayload(InputStream payload) {
			this.payload = payload;
		}

		@Override
		public void setParameter(String parameter) {
		}

		public String getPayload() throws IOException {
			return IOUtils.toString(payload);
		}
	}
}
