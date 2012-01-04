package com.temenos.interaction.example.note;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Path;

import org.odata4j.producer.ODataProducer;

import com.temenos.interaction.core.command.CommandController;
import com.temenos.interaction.core.state.CRUDResourceInteractionModel;

/**
 * Define the Note Resource Interaction Model (implemented with a JAXB provider)
 * Interactions with Notes are simple.  You can put them, you can put them, you can get them, and you can delete them.
 * @author aphethean
 */
@Path("/notesjaxb/{id}")
public class JAXBNoteRIM extends CRUDResourceInteractionModel {

	public final static String RESOURCE_PATH = "/notesjaxb/{id}";
	public final static String ENTITY_NAME = OEntityNoteRIM.ENTITY_NAME;
	private ODataProducer producer;
	
	public JAXBNoteRIM() {
		super(RESOURCE_PATH);

		/*
		 * Not required when wired with Spring
		 */
		NoteProducerFactory npf = new NoteProducerFactory();
		producer = npf.getFunctionsProducer();
		/*
		 * Not required when wired with Spring
		 * 		NoteProducerFactory npf = new NoteProducerFactory();
		 * 		producer = npf.getFunctionsProducer();
		 * 		edmDataServices = producer.getMetadata();
		 */

		CommandController commandController = getCommandController();
		commandController.addGetCommand(RESOURCE_PATH, new JAXBGetNoteCommand(producer));
		commandController.addStateTransitionCommand("PUT", RESOURCE_PATH, new JAXBPutNoteCommand(producer));
		commandController.addStateTransitionCommand("DELETE", RESOURCE_PATH, new DeleteNoteCommand(producer));
	}

	public ODataProducer getProducer() {
		return producer;
	}

	public void setProducer(ODataProducer producer) {
		this.producer = producer;
	}
	
	public static Set<String> getValidNextStates() {
		Set<String> states = new HashSet<String>();
		states.add("GET");
		states.add("PUT");
		states.add("DELETE");
		states.add("OPTIONS");
		states.add("HEAD");
		return states;
	}

}
