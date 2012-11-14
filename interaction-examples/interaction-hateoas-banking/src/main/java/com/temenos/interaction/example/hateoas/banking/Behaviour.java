package com.temenos.interaction.example.hateoas.banking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.temenos.interaction.core.hypermedia.Action;
import com.temenos.interaction.core.hypermedia.CollectionResourceState;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.hypermedia.ResourceStateMachine;

public class Behaviour {

	public ResourceState getInteractionModel() {

		// this will be the service root
		ResourceState initialState = new ResourceState("home", "initial", createActionList(new Action("NoopGET", Action.TYPE.VIEW), null), "/");
		ResourceState preferences = new ResourceState("Preferences", "preferences", createActionList(new Action("GETPreferences", Action.TYPE.VIEW), null), "/preferences");
		
		initialState.addTransition("GET", preferences);
		initialState.addTransition("GET", getFundsTransferInteractionModel());
		initialState.addTransition("GET", getCustomerInteractionModel());
		return initialState;
	}

	public ResourceStateMachine getFundsTransferInteractionModel() {
		CollectionResourceState initialState = new CollectionResourceState("FundsTransfer", "initial", createActionList(new Action("GETFundTransfers", Action.TYPE.VIEW), null), "/fundtransfers");
		ResourceState newFtState = new ResourceState(initialState, "new", createActionList(new Action("NoopGET", Action.TYPE.VIEW), new Action("NEWFundTransfer", Action.TYPE.ENTRY)), "/new");
		ResourceState exists = new ResourceState("FundsTransfer", "exists", createActionList(new Action("GETFundTransfer", Action.TYPE.VIEW), new Action("PUTFundTransfer", Action.TYPE.ENTRY)), "/fundtransfers/{id}", "id", "self".split(" "));
		ResourceState finalState = new ResourceState(exists, "end", createActionList(new Action("NoopGET", Action.TYPE.VIEW), null));

		Map<String, String> uriLinkageMap = new HashMap<String, String>();
		initialState.addTransition("POST", newFtState);		

		uriLinkageMap.clear();
		newFtState.addTransition("PUT", exists, uriLinkageMap);
		//newFtState.addTransition("GET", exists, uriLinkageMap);
		
		uriLinkageMap.clear();
		initialState.addTransitionForEachItem("GET", exists, uriLinkageMap);		

		exists.addTransition("PUT", exists, uriLinkageMap);		
		exists.addTransition("DELETE", finalState, uriLinkageMap);
		return new ResourceStateMachine(initialState);
	}

	public ResourceStateMachine getCustomerInteractionModel() {
		CollectionResourceState customers = new CollectionResourceState("Customer", "customers", createActionList(new Action("GETCustomers", Action.TYPE.VIEW), null), "/customers");
		ResourceState customer = new ResourceState("Customer", "customer", createActionList(new Action("GETCustomer", Action.TYPE.VIEW), new Action("PUTCustomer", Action.TYPE.ENTRY)), "/{id}");
		ResourceState deleted = new ResourceState(customer, "deleted", createActionList(null, new Action("NoopDELETE", Action.TYPE.ENTRY)));
		
		Map<String, String> uriLinkageMap = new HashMap<String, String>();
		uriLinkageMap.clear();
		uriLinkageMap.put("id", "name");
		customers.addTransitionForEachItem("GET", customer, uriLinkageMap);		

		customer.addTransition("PUT", customer, uriLinkageMap);		
		customer.addTransition("DELETE", deleted, uriLinkageMap);
		return new ResourceStateMachine(customers);
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
