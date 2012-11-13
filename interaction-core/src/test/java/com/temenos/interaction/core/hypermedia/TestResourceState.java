package com.temenos.interaction.core.hypermedia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class TestResourceState {

	@Test
	public void testRels() {
		String ENTITY_NAME = "entity";
		String linkRels = "self geospatial";
		ResourceState initial = new ResourceState(ENTITY_NAME, "begin", new HashSet<Action>(), "/", linkRels.split(" "));
		assertEquals(2, initial.getRels().length);
		assertEquals("self", initial.getRels()[0]);
		assertEquals("geospatial", initial.getRels()[1]);
	}

	@Test
	public void testRel() {
		String ENTITY_NAME = "entity";
		String linkRels = "self geospatial";
		ResourceState initial = new ResourceState(ENTITY_NAME, "begin", new HashSet<Action>(), "/", linkRels.split(" "));
		assertEquals("self geospatial", initial.getRel());
	}

	@Test
	public void testDefaultRel() {
		String ENTITY_NAME = "entity";
		ResourceState initial = new ResourceState(ENTITY_NAME, "begin", new HashSet<Action>(), "/");
//		assertEquals("item self", initial.getRel());
		assertEquals("item", initial.getRel());
	}

	@Test
	public void testId() {
		String ENTITY_NAME = "entity";
		ResourceState initial = new ResourceState(ENTITY_NAME, "begin", new HashSet<Action>(), "/");
		assertEquals("entity.begin", initial.getId());
	}
	
	@Test
	public void testCollection() {
		String ENTITY_NAME = "entity";
		ResourceState begin = new ResourceState(ENTITY_NAME, "begin", new HashSet<Action>(), "/");
		ResourceState exists = new ResourceState(ENTITY_NAME, "exists", new HashSet<Action>(), "{id}");
		ResourceState end = new ResourceState(ENTITY_NAME, "end", new HashSet<Action>(), "/");
		
		Set<ResourceState> states = new HashSet<ResourceState>();
		states.add(begin);
		states.add(exists);
		states.add(end);

		Set<ResourceState> states2 = new HashSet<ResourceState>();
		states2.add(begin);
		states2.add(exists);
		states2.add(end);
		
		states.removeAll(states2);
		assertEquals(0, states.size());
	}

	/**
	 * Each resource state must have a unique path.  Test the constructor
	 * that uses the state name as the path.
	 */
	@Test
	public void testSelfStatePath() {
		String ENTITY_NAME = "entity";
		ResourceState initial = new ResourceState(ENTITY_NAME, "initial", new HashSet<Action>(), "/test");
		ResourceState exists = new ResourceState(initial, "exists", new HashSet<Action>(), "/exists");
		ResourceState root = new ResourceState(ENTITY_NAME, "root", new HashSet<Action>(), "/");
		ResourceState archived = new ResourceState(ENTITY_NAME, "archived", new HashSet<Action>(), "/archived");
		assertEquals("/test", initial.getPath());
		assertEquals("/test/exists", exists.getPath());
		assertEquals("/", root.getPath());
		assertEquals("/archived", archived.getPath());
	}

	@Test
	public void testGetCommand() {
		String ENTITY_NAME = "entity";
		ResourceState begin = new ResourceState(ENTITY_NAME, "begin", new HashSet<Action>(), "{id}");
		ResourceState exists = new ResourceState(ENTITY_NAME, "exists", new HashSet<Action>(), "{id}");
		begin.addTransition("PUT", exists);
		assertEquals("PUT", begin.getTransition(exists).getCommand().getMethod());
		assertEquals("{id}", begin.getTransition(exists).getCommand().getPath());
	}

	@Test
	public void testAutoTransition() {
		String ENTITY_NAME = "entity";
		ResourceState begin = new ResourceState(ENTITY_NAME, "begin", new HashSet<Action>(), "{id}");
		ResourceState exists = new ResourceState(ENTITY_NAME, "exists", new HashSet<Action>(), "{id}");
		begin.addTransition(null, exists, Transition.AUTO);
		assertTrue(begin.getTransition(exists).getCommand().isAutoTransition());
	}	

	@Test (expected = IllegalArgumentException.class)
	public void testInvalidAutoTransition() {
		String ENTITY_NAME = "entity";
		ResourceState begin = new ResourceState(ENTITY_NAME, "begin", new HashSet<Action>(), "{id}");
		ResourceState exists = new ResourceState(ENTITY_NAME, "exists", new HashSet<Action>(), "{id}");
		begin.addTransition("PUT", exists, Transition.AUTO);
	}	
	
	@Test
	public void testAddTransitionLinkageMap() {
		// define a linkage map (target URI element, source entity element)
		Map<String, String> uriLinkageMap = new HashMap<String, String>();
		// uri defines a template with {id}, our entity needs to supply {NoteId} as the id
		uriLinkageMap.put("id", "NoteId");

		String ENTITY_NAME = "entity";
		ResourceState begin = new ResourceState("SomeEntity", "initial", new HashSet<Action>(), "/tests");
		ResourceState exists = new ResourceState(ENTITY_NAME, "exists", new HashSet<Action>(), "/test/{id}");
		begin.addTransition("PUT", exists, uriLinkageMap);
		assertEquals("/test/{NoteId}", begin.getTransition(exists).getCommand().getPath());

	}
	
	@Test
	public void testAddMultiplTransitionsNoPath() {
		ResourceState initial = new ResourceState("ENTITY", "initial", new HashSet<Action>(), "/");
		ResourceState A = new ResourceState("ENTITY", "initial", new HashSet<Action>(), "/A");
		ResourceState B = new ResourceState("ENTITY", "initial", new HashSet<Action>(), "/B");
		
		initial.addTransition(A);
		initial.addTransition(B);
		
		assertEquals(2, initial.getAllTargets().size());
	}
	
	@Test(expected = AssertionError.class)
	public void testResourceStateNoPath() {
		new ResourceState("ENTITY", "initial", new HashSet<Action>(), "");
	}

	@Test
	public void testTransitionToStateMachine() {
		String ENTITY_NAME1 = "entity1";
		ResourceState initial = new ResourceState(ENTITY_NAME1, "initial", new HashSet<Action>(), "/test/{id}");
		ResourceState exists = new ResourceState(initial, "exists", new HashSet<Action>());
		ResourceState deleted = new ResourceState(initial, "deleted", new HashSet<Action>());
		initial.addTransition("PUT", exists);
		exists.addTransition("DELETE", deleted);
		
		String ENTITY_NAME2 = "entity2";
		ResourceState initial2 = new ResourceState(ENTITY_NAME2, "initial", new HashSet<Action>(), "/entity/2");
		ResourceState exists2 = new ResourceState(initial2, "exists", new HashSet<Action>());
		ResourceState deleted2 = new ResourceState(initial2, "deleted", new HashSet<Action>());
		initial2.addTransition("PUT", exists2);
		exists2.addTransition("DELETE", deleted2);
		
		ResourceStateMachine rsm1 = new ResourceStateMachine(initial);
		ResourceStateMachine rsm2 = new ResourceStateMachine(initial2);
		exists.addTransition("GET", rsm2);
		exists2.addTransition("GET", rsm1);
		
		assertEquals("GET", exists.getTransition(initial2).getCommand().getMethod());
		assertEquals("/entity/2", exists.getTransition(initial2).getCommand().getPath());
	}
	
	@Test
	public void testEquality() {
		String ENTITY_NAME = "entity";
		ResourceState begin = new ResourceState(ENTITY_NAME, "begin", new HashSet<Action>(), "/");
		ResourceState begin2 = new ResourceState(ENTITY_NAME, "begin", new HashSet<Action>(), "/");
		assertEquals(begin, begin2);
		assertEquals(begin.hashCode(), begin2.hashCode());
	}

	@Test
	public void testInequality() {
		String ENTITY_NAME = "entity";
		ResourceState begin = new ResourceState(ENTITY_NAME, "begin", new HashSet<Action>(), "/");
		ResourceState end = new ResourceState(ENTITY_NAME, "end", new HashSet<Action>(), "/");
		assertFalse(begin.equals(end));
		assertFalse(begin.hashCode() == end.hashCode());
	}

	@Test
	public void testEqualityEntity() {
		String STATE_NAME = "pseudo";
		ResourceState one = new ResourceState("entity1", STATE_NAME, new HashSet<Action>(), "/");
		ResourceState two = new ResourceState("entity1", STATE_NAME, new HashSet<Action>(), "/");
		assertEquals(one, two);
		assertEquals(one.hashCode(), two.hashCode());
	}

	@Test
	public void testInequalityEntity() {
		String STATE_NAME = "pseudo";
		ResourceState one = new ResourceState("entity1", STATE_NAME, new HashSet<Action>(), "/");
		ResourceState two = new ResourceState("entity2", STATE_NAME, new HashSet<Action>(), "/");
		assertFalse(one.equals(two));
		assertFalse(one.hashCode() == two.hashCode());
	}

	@Test
	public void testEndState() {
		String ENTITY_NAME = "entity";
		ResourceState begin = new ResourceState(ENTITY_NAME, "begin", new HashSet<Action>(), "/");
		ResourceState end = new ResourceState(ENTITY_NAME, "end", new HashSet<Action>(), "/");
		begin.addTransition("DELETE", end);
		assertFalse(begin.isFinalState());
		assertTrue(end.isFinalState());
	}

	/**
	 * A pseudo state is a resource state without a resource to represent its state.
	 * e.g. a deleted state is a state of an entity resource.
	 */
	@Test
	public void testPseudoState() {
		ResourceState exists = new ResourceState("entity", "exists", new HashSet<Action>(), "/exists");
		ResourceState deleted = new ResourceState(exists, "deleted", new HashSet<Action>());
		assertTrue(deleted.isPseudoState());
	}

	/**
	 * A transient state is a resource state with a single AUTO transition.
	 */
	@Test
	public void testTransientState() {
		ResourceState home = new ResourceState("root", "root", new HashSet<Action>(), "/");
		ResourceState reboot = new ResourceState("entity", "reboot", new HashSet<Action>(), "/reboot");
		home.addTransition("POST", reboot);
		reboot.addTransition(home);
		assertTrue(reboot.isTransientState());
	}

	/**
	 * A transient state is a resource state with a single AUTO transition, get the
	 * target state.
	 */
	@Test
	public void testTransientTarget() {
		ResourceState home = new ResourceState("root", "root", new HashSet<Action>(), "/");
		ResourceState reboot = new ResourceState("entity", "reboot", new HashSet<Action>(), "/reboot");
		home.addTransition("POST", reboot);
		reboot.addTransition(home);
		assertEquals(home, reboot.getAutoTransition().getTarget());
	}

}
