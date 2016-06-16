package com.temenos.interaction.core.rim;

/*
 * #%L
 * interaction-core
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

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.temenos.interaction.core.MultivaluedMapImpl;
import com.temenos.interaction.core.web.RequestContext;

public class TestHeaderHelper {

	@Before
	public void setup() {
		// initialise the thread local request context with requestUri and baseUri
        RequestContext ctx = new RequestContext("http://localhost/myservice.svc", "/baseuri/", null);
        RequestContext.setRequestContext(ctx);
	}

	@Test
	public void testOptionsAllowHeader() {
		SortedSet<String> validNextStates = new TreeSet<String>();
		validNextStates.add("SEE");
		validNextStates.add("HISTORY");
		validNextStates.add("AUTHORISE");
		validNextStates.add("REVERSE");
		validNextStates.add("DELETE");
		validNextStates.add("INPUT");
		validNextStates.add("GET");
		validNextStates.add("HEAD");
		validNextStates.add("OPTIONS");
		
		Response r = HeaderHelper.allowHeader(Response.ok(), validNextStates).build();
		assertEquals("AUTHORISE, DELETE, GET, HEAD, HISTORY, INPUT, OPTIONS, REVERSE, SEE", r.getMetadata().getFirst("Allow"));
	}

	@Test
	public void testOptionsNoAllowHeader() {
		Response r = HeaderHelper.allowHeader(Response.ok(), null).build();
		assertNull(r.getMetadata().getFirst("Allow"));
	}

	@Test
	public void testOptionsNoValidStates() {
		Response r = HeaderHelper.allowHeader(Response.ok(), new HashSet<String>()).build();
		assertEquals("", r.getMetadata().getFirst("Allow"));
	}

	@Test
	public void testLocation() {
		Response r = HeaderHelper.locationHeader(Response.ok(), "/path").build();
		assertEquals("/path", r.getMetadata().getFirst("Location"));
	}
	
	@Test
	public void testLocationNull() {
		Response r = HeaderHelper.locationHeader(Response.ok(), null).build();
		assertNull(r.getMetadata().getFirst("Location"));
	}
	
	@Test
	public void testEtag() {
		Response r = HeaderHelper.etagHeader(Response.ok(), "ABCDEFG").build();
		assertEquals("ABCDEFG", r.getMetadata().getFirst(HttpHeaders.ETAG));
	}

	@Test
	public void testEtagNull() {
		Response r = HeaderHelper.etagHeader(Response.ok(), null).build();
		assertNull(r.getMetadata().getFirst(HttpHeaders.ETAG));
	}

	@Test
	public void testEtagNotSpecified() {
		Response r = Response.ok().build();
		assertNull(r.getMetadata().getFirst(HttpHeaders.ETAG));
	}

	@Test
	public void testEtagEmpty() {
		Response r = HeaderHelper.etagHeader(Response.ok(), "").build();
		assertNull(r.getMetadata().getFirst(HttpHeaders.ETAG));
	}
	
    @Test
    public void testEncodeRequestParameters(){
        MultivaluedMap<String, String> values = new MultivaluedMapImpl<String>();
        values.add("customerName", "Jack");
        values.add("customerName", "Jill");
        values.add("transaction", "101");
        String queryParam = HeaderHelper.encodeMultivalueRequestParameters(values);
        assertThat(queryParam, allOf(
                startsWith("?"),
                containsString("customerName=Jack"),
                containsString("customerName=Jill"),
                containsString("transaction=101")
        ));
        assertThat(StringUtils.countMatches(queryParam, "&"), equalTo(2));
    }
    
    @Test
    public void testEncodeRequestParametersDropsDuplicateKeyValues(){
        MultivaluedMap<String, String> values = new MultivaluedMapImpl<String>();
        values.add("customerName", "Jack");
        values.add("customerName", "Jack");
        values.add("transaction", "101");
        values.add("transaction", "102");
        String queryParam = HeaderHelper.encodeMultivalueRequestParameters(values);
        assertThat(queryParam, allOf(
                startsWith("?"),
                containsString("customerName=Jack"),
                containsString("transaction=101"),
                containsString("transaction=102")
        ));
        assertThat(StringUtils.countMatches(queryParam, "customerName=Jack"), equalTo(1));
        assertThat(StringUtils.countMatches(queryParam, "&"), equalTo(2));
    }
    
    @Test
    public void testEncodeRequestParametersWithHttpEntities(){
        MultivaluedMap<String, String> values = new MultivaluedMapImpl<String>();
        values.add("customerNam=", "J&ck");
        values.add("trans&ction", "!0!");
        String queryParam = HeaderHelper.encodeMultivalueRequestParameters(values);
        assertThat(queryParam, allOf(
                startsWith("?"),
                containsString("customerNam%3D=J%26ck"),
                containsString("trans%26ction=%210%21")
        ));
        assertThat(StringUtils.countMatches(queryParam, "&"), equalTo(1));
    }
    
    @Test
    public void testEncodeRequestParametersWithoutAnyQueryParams(){
        assertThat(
            HeaderHelper.encodeMultivalueRequestParameters(
                new MultivaluedMapImpl<String>()
            ), equalTo("")
        );
    }
}
