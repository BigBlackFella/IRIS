package com.temenos.interaction.example.hateoas.simple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.temenos.interaction.core.hypermedia.Action;
import com.temenos.interaction.core.hypermedia.CollectionResourceState;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.hypermedia.ResourceStateMachine;

public class Behaviour {

	// the entity that generates new IDs
	private final static String NEW_ENTITY_NAME = "ID";
	// the entity that stores notes
	private final static String NOTE_ENTITY_NAME = "Note";

	public ResourceState getInteractionModel() {
		// this will be the service root
		ResourceState initialState = new ResourceState("HOME", "home", createActionList(new Action("NoopGET", Action.TYPE.VIEW), null), "/");
		
		ResourceState profile = new ResourceState("Profile", "profile", createActionList(new Action("NoopGET", Action.TYPE.VIEW), null), "/profile");
		ResourceState preferences = new ResourceState("Preferences", "preferences", createActionList(new Action("GETPreferences", Action.TYPE.VIEW), null), "/preferences");
		
		initialState.addTransition("GET", profile);
		initialState.addTransition("GET", new ResourceStateMachine(preferences));
		initialState.addTransition("GET", getNotesInteractionModel());
		return initialState;
	}

	public ResourceStateMachine getNotesInteractionModel() {
		
		CollectionResourceState initialState = new CollectionResourceState(NOTE_ENTITY_NAME, "notes", createActionList(new Action("GETNotes", Action.TYPE.VIEW), null), "/notes");
		ResourceState newNoteState = new ResourceState(NEW_ENTITY_NAME, "newNote", createActionList(new Action("NoopGET", Action.TYPE.VIEW), new Action("NoopPOST", Action.TYPE.ENTRY)), "/notes/new");
		ResourceState exists = new ResourceState(NOTE_ENTITY_NAME, "note", createActionList(new Action("GETNote", Action.TYPE.VIEW), new Action("NoopPUT", Action.TYPE.ENTRY)), "/notes/{noteID}", "noteID", "self".split(" "));
		ResourceState deletedState = new ResourceState(NOTE_ENTITY_NAME, "deletedNote", createActionList(new Action("NoopGET", Action.TYPE.VIEW), new Action("DELETENote", Action.TYPE.ENTRY)), "/notes/{noteID}", "noteID");

		// a linkage map (target URI element, source entity element)
		Map<String, String> uriLinkageMap = new HashMap<String, String>();

		// a link from notes collection to create a new note (no arguments to this link)
		initialState.addTransition("POST", newNoteState);		

		// link from new note id to save the note
		uriLinkageMap.clear();
		uriLinkageMap.put("noteID", "lastId");
		Set<String> relations = new HashSet<String>();
		relations.add("_new");
		newNoteState.addTransition("PUT", exists, uriLinkageMap);
		
		/* 
		 * a link on each note in the collection to get view the note
		 * no linkage map as target URI element (self) must exist in source entity element (also self)
		 */
		uriLinkageMap.clear();
		initialState.addTransitionForEachItem("GET", exists, uriLinkageMap);		
		initialState.addTransitionForEachItem("DELETE", deletedState, uriLinkageMap);

		// update / delete note item (same linkage map)
		exists.addTransition("PUT", exists, uriLinkageMap);
		exists.addTransition("DELETE", deletedState, uriLinkageMap);
		
		// add the auto transition from deleted to collection
		deletedState.addTransition(initialState);
		
		return new ResourceStateMachine(initialState);
	}

	private List<Action> createActionList(Action view, Action entry) {
		List<Action> actions = new ArrayList<Action>();
		if (view != null)
			actions.add(view);
		if (entry != null)
			actions.add(entry);
		return actions;
	}

}
