package com.temenos.interaction.core.link;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class TestResourceStateMachine {

	@Test
	public void testStates() {
		String ENTITY_NAME = "T24CONTRACT";
		ResourceState begin = new ResourceState(ENTITY_NAME, "begin", "{id}");
		ResourceState unauthorised = new ResourceState(ENTITY_NAME, "INAU", "unauthorised/{id}");
		ResourceState authorised = new ResourceState(ENTITY_NAME, "LIVE", "authorised/{id}");
		ResourceState reversed = new ResourceState(ENTITY_NAME, "RNAU", "reversed/{id}");
		ResourceState history = new ResourceState(ENTITY_NAME, "REVE", "history/{id}");
		ResourceState end = new ResourceState(ENTITY_NAME, "end", "{id}");
	
		begin.addTransition("PUT", unauthorised);
		
		unauthorised.addTransition("PUT", unauthorised);
		unauthorised.addTransition("PUT", authorised);
		unauthorised.addTransition("PUT", end);
		
		authorised.addTransition("PUT", history);
		
		history.addTransition("PUT", reversed);
		
		
		ResourceStateMachine sm = new ResourceStateMachine(ENTITY_NAME, begin);
		assertEquals(6, sm.getStates().size());
		
		Set<ResourceState> testStates = new HashSet<ResourceState>();
		testStates.add(begin);
		testStates.add(unauthorised);
		testStates.add(authorised);
		testStates.add(reversed);
		testStates.add(history);
		testStates.add(end);
		for (ResourceState s : testStates) {
			assertTrue(sm.getStates().contains(s));
		}
		
		System.out.println(new ASTValidation().graph(sm));
	}
	
	@Test
	public void testGetStates() {
		String ENTITY_NAME = "";
		ResourceState begin = new ResourceState(ENTITY_NAME, "begin", "{id}");
		ResourceState exists = new ResourceState(ENTITY_NAME, "exists", "{id}");
		ResourceState end = new ResourceState(ENTITY_NAME, "end", "{id}");
	
		begin.addTransition("PUT", exists);
		exists.addTransition("PUT", exists);
		exists.addTransition("DELETE", end);
		
		ResourceStateMachine sm = new ResourceStateMachine(ENTITY_NAME, begin);
		Collection<ResourceState> states = sm.getStates();
		assertEquals("Number of states", 3, states.size());
		assertTrue(states.contains(begin));
		assertTrue(states.contains(exists));
		assertTrue(states.contains(end));
	}

	@Test
	public void testInteractionMap() {
		String ENTITY_NAME = "";
		ResourceState begin = new ResourceState(ENTITY_NAME, "begin", "{id}");
		ResourceState exists = new ResourceState(ENTITY_NAME, "exists", "{id}");
		ResourceState end = new ResourceState(ENTITY_NAME, "end", "{id}");
	
		begin.addTransition("PUT", exists);
		exists.addTransition("PUT", exists);
		exists.addTransition("DELETE", end);
		
		ResourceStateMachine sm = new ResourceStateMachine(ENTITY_NAME, begin);

		Map<String, Set<String>> interactionMap = sm.getInteractionMap();
		assertEquals("Number of resources", 1, interactionMap.size());
		Set<String> entrySet = interactionMap.keySet();
		assertTrue(entrySet.contains("{id}"));
		Collection<String> interactions = interactionMap.get("{id}");
		assertEquals("Number of interactions", 2, interactions.size());
		assertTrue(interactions.contains("PUT"));
		assertTrue(interactions.contains("DELETE"));
	}

	@Test
	public void testInteractions() {
		String ENTITY_NAME = "";
		ResourceState begin = new ResourceState(ENTITY_NAME, "begin", "{id}");
		ResourceState exists = new ResourceState(ENTITY_NAME, "exists", "{id}");
		ResourceState end = new ResourceState(ENTITY_NAME, "end", "{id}");
	
		begin.addTransition("PUT", exists);
		exists.addTransition("PUT", exists);
		exists.addTransition("DELETE", end);
		
		ResourceStateMachine sm = new ResourceStateMachine(ENTITY_NAME, begin);

		Set<String> interactions = sm.getInteractions(begin);
		assertEquals("Number of interactions", 2, interactions.size());
		assertTrue(interactions.contains("PUT"));
		assertTrue(interactions.contains("DELETE"));
	}

	@Test
	public void testSubstateInteractions() {
		String ENTITY_NAME = "";
  		ResourceState initial = new ResourceState(ENTITY_NAME, "initial");
		ResourceState published = new ResourceState(ENTITY_NAME, "published", "/published");
		ResourceState draft = new ResourceState(ENTITY_NAME, "draft", "/draft");
		ResourceState deleted = new ResourceState(ENTITY_NAME, "deleted");
	
		// create draft
		initial.addTransition("PUT", draft);
		// updated draft
		draft.addTransition("PUT", draft);
		// publish
		draft.addTransition("PUT", published);
		// delete draft
		draft.addTransition("DELETE", deleted);
		// delete published
		published.addTransition("DELETE", deleted);
		
		ResourceStateMachine sm = new ResourceStateMachine(ENTITY_NAME, initial);

		Set<String> initialInteractions = sm.getInteractions(initial);
		assertNull("Number of interactions", initialInteractions);

		Set<String> draftInteractions = sm.getInteractions(draft);
		assertEquals("Number of interactions", 2, draftInteractions.size());
		assertTrue(draftInteractions.contains("PUT"));
		assertTrue(draftInteractions.contains("DELETE"));

		Set<String> publishInteractions = sm.getInteractions(published);
		assertEquals("Number of interactions", 2, publishInteractions.size());
		assertTrue(publishInteractions.contains("PUT"));
		assertTrue(publishInteractions.contains("DELETE"));

		Set<String> deletedInteractions = sm.getInteractions(deleted);
		assertNull("Number of interactions", deletedInteractions);

	}

	@Test
	public void testStateMap() {
		String ENTITY_NAME = "";
  		ResourceState initial = new ResourceState(ENTITY_NAME, "initial");
		ResourceState published = new ResourceState(ENTITY_NAME, "published", "/published");
		ResourceState draft = new ResourceState(ENTITY_NAME, "draft", "/draft");
		ResourceState deleted = new ResourceState(ENTITY_NAME, "deleted");
	
		// create draft
		initial.addTransition("PUT", draft);
		// updated draft
		draft.addTransition("PUT", draft);
		// publish
		draft.addTransition("PUT", published);
		// delete draft
		draft.addTransition("DELETE", deleted);
		// delete published
		published.addTransition("DELETE", deleted);
		
		ResourceStateMachine sm = new ResourceStateMachine(ENTITY_NAME, initial);

		Map<String, ResourceState> stateMap = sm.getStateMap();
		assertEquals("Number of states", 2, stateMap.size());
		Set<String> entrySet = stateMap.keySet();
		assertTrue(entrySet.contains("/published"));
		assertTrue(entrySet.contains("/draft"));
		assertEquals(published, stateMap.get("/published"));
		assertEquals(draft, stateMap.get("/draft"));
	}

	@Test
	public void testGetState() {
		String ENTITY_NAME = "";
  		ResourceState initial = new ResourceState(ENTITY_NAME, "initial");
		ResourceState published = new ResourceState(ENTITY_NAME, "published", "/published");
		ResourceState draft = new ResourceState(ENTITY_NAME, "draft", "/draft");
		ResourceState deleted = new ResourceState(ENTITY_NAME, "deleted");
	
		// create draft
		initial.addTransition("PUT", draft);
		// updated draft
		draft.addTransition("PUT", draft);
		// publish
		draft.addTransition("PUT", published);
		// delete draft
		draft.addTransition("DELETE", deleted);
		// delete published
		published.addTransition("DELETE", deleted);
		
		ResourceStateMachine sm = new ResourceStateMachine(ENTITY_NAME, initial);

		assertEquals(initial, sm.getState(null));
		assertEquals(published, sm.getState("/published"));
		assertEquals(draft, sm.getState("/draft"));
	}

	@Test(expected=AssertionError.class)
	public void testInteractionsInvalidState() {
		String ENTITY_NAME = "";
		ResourceState begin = new ResourceState(ENTITY_NAME, "begin", "{id}");
		ResourceState exists = new ResourceState(ENTITY_NAME, "exists", "{id}");
		ResourceState end = new ResourceState(ENTITY_NAME, "end", "{id}");
	
		begin.addTransition("PUT", exists);
		exists.addTransition("PUT", exists);
		exists.addTransition("DELETE", end);
		
		ResourceStateMachine sm = new ResourceStateMachine(ENTITY_NAME, begin);

		ResourceState other = new ResourceState("other", "/other");
		sm.getInteractions(other);
	}

	
//    @Test
    public void testProcessOrchestration() {
		String PROCESS_ENTITY_NAME = "process";
		String TASK_ENTITY_NAME = "task";

          // process behaviour
          ResourceState processes = new ResourceState(PROCESS_ENTITY_NAME, "processes", "/processes");
          ResourceState newProcess = new ResourceState(PROCESS_ENTITY_NAME, "new", "/new");

          // create new process
          processes.addTransition("POST", newProcess);

          
          // Process states
          ResourceState processInitial = new ResourceState(PROCESS_ENTITY_NAME, "initialProcess");
          ResourceState processStarted = new ResourceState(PROCESS_ENTITY_NAME, "started");
          ResourceState nextTask = new ResourceState(PROCESS_ENTITY_NAME, "taskAvailable", "/nextTask");
          ResourceState processCompleted = new ResourceState(PROCESS_ENTITY_NAME, "completedProcess");

          // start new process
          newProcess.addTransition("PUT", processInitial);
          processInitial.addTransition("PUT", processStarted);
          // do a task
          processStarted.addTransition("GET", nextTask);
          // finish the process
          processStarted.addTransition("DELETE", processCompleted);

          
          // Task states
          ResourceState taskAcquired = new ResourceState(TASK_ENTITY_NAME, "acquired", "/acquired");
          ResourceState taskComplete = new ResourceState(TASK_ENTITY_NAME, "complete", "/completed");
          ResourceState taskCompleted = new ResourceState(TASK_ENTITY_NAME, "deletedTask");
          
          nextTask.addTransition("PUT", taskAcquired);
          taskAcquired.addTransition("DELETE", nextTask);
          taskAcquired.addTransition("PUT", taskComplete);
//          taskComplete.addTransition("DELETE", taskCompleted);
          
          // TODO need to link one state machine to another
          ResourceStateMachine sm = new ResourceStateMachine(PROCESS_ENTITY_NAME, processInitial);
          System.out.println(new ASTValidation().graph(sm));

          assertEquals("", new ASTValidation().graph(sm));
    }

}
