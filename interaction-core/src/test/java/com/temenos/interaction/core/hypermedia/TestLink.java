package com.temenos.interaction.core.hypermedia;

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


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

public class TestLink {

	@Test
	public void testGetHrefTransition() throws Exception {
		Link link = new Link(null, "NoteToPersonLink", "Person", 
				"http://localhost:8080/example/interaction-odata-notes.svc/Notes(1)/Person",
				null, null, "GET", null);
		String hrefTransition = link.getHrefTransition("example/interaction-odata-notes.svc");
		assertEquals("Notes(1)/Person", hrefTransition);
	}

	@Test
	public void testTitleWithoutLabel() throws Exception {
		Transition t = mock(Transition.class);
		when(t.getLabel()).thenReturn(null);
		ResourceState state = mock(ResourceState.class);
		when(state.getName()).thenReturn("FlightSchedules");
		when(t.getTarget()).thenReturn(state);
		Link link = new Link(t, "arrivals", "http://localhost:8080/example/Airport/arrivals", "GET");

		assertEquals("FlightSchedules", link.getTitle());
		assertEquals("arrivals", link.getRel());
	}

	@Test
	public void testTitleWithEmptyLabel() throws Exception {
		Transition t = mock(Transition.class);
		when(t.getLabel()).thenReturn("");
		ResourceState state = mock(ResourceState.class);
		when(state.getName()).thenReturn("FlightSchedules");
		when(t.getTarget()).thenReturn(state);
		Link link = new Link(t, "arrivals", "http://localhost:8080/example/Airport/arrivals", "GET");

		assertEquals("FlightSchedules", link.getTitle());
		assertEquals("arrivals", link.getRel());
	}
	
	@Test
	public void testTitleWithLabel() throws Exception {
		Transition t = mock(Transition.class);
		when(t.getLabel()).thenReturn("arrivals");
		ResourceState state = mock(ResourceState.class);
		when(state.getName()).thenReturn("FlightSchedules");
		when(t.getTarget()).thenReturn(state);
		Link link = new Link(t, "arrivals", "http://localhost:8080/example/Airport/arrivals", "GET");

		assertEquals("arrivals", link.getTitle());
		assertEquals("arrivals", link.getRel());
	}
}
