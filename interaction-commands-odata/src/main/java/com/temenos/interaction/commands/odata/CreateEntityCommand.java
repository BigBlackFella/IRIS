package com.temenos.interaction.commands.odata;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;

import org.odata4j.core.OEntity;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.ODataProducer;

import com.temenos.interaction.core.resource.EntityResource;
import com.temenos.interaction.core.RESTResponse;
import com.temenos.interaction.core.command.InteractionCommand;
import com.temenos.interaction.core.command.InteractionContext;

public class CreateEntityCommand implements InteractionCommand {

	// Command configuration
	private String entity;

	private ODataProducer producer;

	public CreateEntityCommand(String entity, ODataProducer producer) {
		this.entity = entity;
		this.producer = producer;
	}

	@Override
	public String getMethod() {
		return HttpMethod.POST;
	}

	/* Implement InteractionCommand interface */
	
	@SuppressWarnings("unchecked")
	@Override
	public Result execute(InteractionContext ctx) {
		assert(ctx != null);
		assert(entity != null && !entity.equals(""));
		assert(ctx.getResource() != null);
		
		// create the entity
		EntityResource<OEntity> entityResource = (EntityResource<OEntity>) ctx.getResource();
		EntityResponse er = producer.createEntity(entity, entityResource.getEntity());
		OEntity oEntity = er.getEntity();
		
		ctx.setResource(CommandHelper.createEntityResource(oEntity));
		return Result.SUCCESS;
	}

}
