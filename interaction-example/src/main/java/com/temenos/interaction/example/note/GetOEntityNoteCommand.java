package com.temenos.interaction.example.note;

import javax.ws.rs.core.Response;

import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.ODataProducer;

import com.temenos.interaction.core.EntityResource;
import com.temenos.interaction.core.RESTResponse;
import com.temenos.interaction.core.command.ResourceGetCommand;

public class GetOEntityNoteCommand implements ResourceGetCommand {

	private ODataProducer producer;
	
	public GetOEntityNoteCommand(ODataProducer producer) {
		this.producer = producer;
	}
	
	/* Implement ResourceGetCommand (OEntity) */
	public RESTResponse get(String id) {
		OEntityKey key = OEntityKey.create(new Long(id));
		EntityResponse er = producer.getEntity(NoteRIM.ENTITY_NAME, key, null);
		OEntity oEntity = er.getEntity();
		
		RESTResponse rr = new RESTResponse(Response.Status.OK, new EntityResource(oEntity), NoteRIM.getValidNextStates());
		return rr;
	}

}
