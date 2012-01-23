package com.temenos.interaction.example.note;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;

import org.odata4j.producer.ODataProducer;

import com.temenos.interaction.commands.odata.GETEntitiesCommand;
import com.temenos.interaction.core.command.CommandController;
import com.temenos.interaction.core.state.TRANSIENTResourceInteractionModel;

@Path("/notes")
public class NotesRIM extends TRANSIENTResourceInteractionModel {

	private final static String RESOURCE_PATH = "/notes";
	private final static String ENTITYSET_NAME = "note";

	public NotesRIM() {
		super(ENTITYSET_NAME, RESOURCE_PATH);
		/*
		 * Not required when wired with Spring
		 */
  		NoteProducerFactory npf = new NoteProducerFactory();
		initialise(npf.getFunctionsProducer());
	}
		  	
	public NotesRIM(ODataProducer producer) {
		super(ENTITYSET_NAME, RESOURCE_PATH);
		initialise(producer);
	}
	
	public void initialise(ODataProducer producer) {
		/*
		 * Configure the New Note RIM
		 */
		CommandController commandController = getCommandController();
		commandController.setGetCommand(new GETEntitiesCommand(ENTITYSET_NAME, producer));
	}

}
