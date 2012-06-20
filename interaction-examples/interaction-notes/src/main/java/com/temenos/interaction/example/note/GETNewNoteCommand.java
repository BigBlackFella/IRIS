package com.temenos.interaction.example.note;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.ODataProducer;

import com.temenos.interaction.core.resource.EntityResource;
import com.temenos.interaction.core.RESTResponse;
import com.temenos.interaction.core.command.ResourceGetCommand;

public class GETNewNoteCommand implements ResourceGetCommand {

	// Command configuration
	private String domainObjectName;

	private ODataProducer producer;

	public GETNewNoteCommand(String domainObjectName, ODataProducer producer) {
		this.domainObjectName = domainObjectName;
		this.producer = producer;
	}

	/**
	 * Implement {@link ResourceGetCommand}
	 */
	public RESTResponse get(String id, MultivaluedMap<String, String> queryParams) {
		assert(id == null || "".equals(id));

		OEntityKey key = OEntityKey.create(domainObjectName);
		EntityResponse eResp = producer.getEntity("ID", key, null);
		OEntity oEntity = eResp.getEntity();
		EntityResource<OEntity> er = new EntityResource<OEntity>(oEntity) {};
		return new RESTResponse(Response.Status.OK, er);
	}

}
