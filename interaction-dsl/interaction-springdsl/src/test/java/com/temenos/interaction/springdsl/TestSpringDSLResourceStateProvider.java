package com.temenos.interaction.springdsl;

/*
 * #%L
 * interaction-springdsl
 * %%
 * Copyright (C) 2012 - 2014 Temenos Holdings N.V.
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

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.temenos.interaction.core.hypermedia.Event;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.hypermedia.ResourceStateProvider;

@RunWith(SpringJUnit4ClassRunner.class)
//ApplicationContext will be loaded from "classpath:/com/temenos/interaction/springdsl/TestSpringDSLResourceStateProvider-context.xml"
@ContextConfiguration
public class TestSpringDSLResourceStateProvider {

	@Autowired
	private ResourceStateProvider resourceStateProvider;

	@Test
	public void testGetResourceState() {
		ResourceState actual = resourceStateProvider.getResourceState("SimpleModel_Home_home");
		assertEquals("home", actual.getName());
	}

	@Test
	public void testGetResourceStatesByPath() {
		Properties properties = new Properties();
		properties.put("SimpleModel_Home_home", "GET /test");
		ResourceStateProvider rsp = new SpringDSLResourceStateProvider(properties);
		Map<String, Set<String>> statesByPath = rsp.getResourceStatesByPath();
		assertEquals(1, statesByPath.size());
		assertEquals(1, statesByPath.get("/test").size());
		assertEquals("SimpleModel_Home_home", statesByPath.get("/test").toArray()[0]);
	}

	@Test
	public void testGetResourceStateByRequest() {
		// properties: SimpleModel_Home_home=GET,PUT /test
		ResourceState foundGetState = resourceStateProvider.determineState(new Event("GET", "GET"), "/test");
		assertEquals("home", foundGetState.getName());
		ResourceState foundPutState = resourceStateProvider.determineState(new Event("PUT", "PUT"), "/test");
		assertEquals("home", foundPutState.getName());
	}

}
