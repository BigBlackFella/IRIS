package com.temenos.useragent.generic.http;

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
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.temenos.useragent.generic.internal.RequestData;
import com.temenos.useragent.generic.internal.ResponseData;

public class DefaultHttpExecutorTest {

	private RequestData mockRequestData;
	private HttpClient mockHttpClient;
	private HttpResponse mockResponse;
	private HttpHeader mockHeader;

	@Before
	public void setUp() {
		mockHttpClient = mock(HttpClient.class);
		mockRequestData = mock(RequestData.class);
		mockResponse = mock(HttpResponse.class);
		mockHeader = mock(HttpHeader.class);
	}

	@Test
	public void testExecuteForGet() throws Exception {
		when(mockHttpClient.get(anyString(), any(HttpRequest.class)))
				.thenReturn(mockResponse);
		when(mockResponse.headers()).thenReturn(mockHeader);
		when(mockHeader.get("Content-Type")).thenReturn("application/atom+xml");
		when(mockResponse.payload())
				.thenReturn(
						IOUtils.toString(DefaultHttpExecutorTest.class
								.getResourceAsStream("/atom_feed_with_single_entry.txt")));
		DefaultHttpExecutor executor = new DefaultHttpExecutor(mockHttpClient,
				"http://myserver:8080/myservice/Test.svc", mockRequestData);
		ResponseData responseData = executor.execute(HttpMethod.GET);
		assertNotNull(responseData);
		assertEquals("application/atom+xml",
				responseData.header().get("Content-Type"));
		assertTrue(responseData.body().isCollection());
		assertEquals(1, responseData.body().entities().size());
	}

	@Test
	public void testExecuteForPost() throws Exception {
		when(mockHttpClient.post(anyString(), any(HttpRequest.class)))
				.thenReturn(mockResponse);
		when(mockResponse.headers()).thenReturn(mockHeader);
		when(mockHeader.get("Content-Type")).thenReturn("application/atom+xml");
		when(mockResponse.payload())
				.thenReturn(
						IOUtils.toString(DefaultHttpExecutorTest.class
								.getResourceAsStream("/atom_entry_with_xml_content.txt")));
		DefaultHttpExecutor executor = new DefaultHttpExecutor(mockHttpClient,
				"http://myserver:8080/myservice/Test.svc", mockRequestData);
		ResponseData responseData = executor.execute(HttpMethod.POST);
		assertNotNull(responseData);
		assertEquals("application/atom+xml",
				responseData.header().get("Content-Type"));
		assertFalse(responseData.body().isCollection());
		assertNotNull(responseData.body().entity());
	}

	@Test
	public void testExecuteForPut() {
		when(mockHttpClient.put(anyString(), any(HttpRequest.class)))
				.thenReturn(mockResponse);
		when(mockResponse.headers()).thenReturn(mockHeader);
		when(mockHeader.get("Content-Type")).thenReturn("text/plain");
		when(mockResponse.payload()).thenReturn("plain text payload");
		DefaultHttpExecutor executor = new DefaultHttpExecutor(mockHttpClient,
				"http://myserver:8080/myservice/Test.svc", mockRequestData);
		ResponseData responseData = executor.execute(HttpMethod.PUT);
		assertNotNull(responseData);
		assertEquals("text/plain", responseData.header().get("Content-Type"));
		assertFalse(responseData.body().isCollection());
	}
}
