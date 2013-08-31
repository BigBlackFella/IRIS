package com.temenos.interaction.commands.odata;

import javax.ws.rs.core.Response.Status;

import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.exceptions.ODataProducerException;
import org.odata4j.producer.ODataProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.temenos.interaction.core.command.InteractionCommand;
import com.temenos.interaction.core.command.InteractionContext;
import com.temenos.interaction.core.command.InteractionException;

public class DeleteEntityCommand extends AbstractODataCommand implements InteractionCommand {
	private final Logger logger = LoggerFactory.getLogger(DeleteEntityCommand.class);

	private ODataProducer producer;
	private EdmDataServices edmDataServices;

	public DeleteEntityCommand(ODataProducer producer) {
		this.producer = producer;
		this.edmDataServices = producer.getMetadata();
	}
	
	protected ODataProducer getProducer() {
		return producer;
	}

	/* Implement InteractionCommand interface */

	@Override
	public Result execute(InteractionContext ctx) throws InteractionException {
		assert(ctx != null);
		assert(ctx.getCurrentState() != null);
		assert(ctx.getCurrentState().getEntityName() != null && !ctx.getCurrentState().getEntityName().equals(""));
		assert(ctx.getResource() == null);
		
		String entity = getEntityName(ctx);
		logger.debug("Deleting entity for " + entity);
		EdmEntitySet entitySet = edmDataServices.getEdmEntitySet(entity);
		if (entitySet == null) {
			throw new InteractionException(Status.NOT_FOUND, "Entity set not found [" + entity + "]");		
		}
		assert(entity.equals(entitySet.getName()));

		// Create entity key (simple types only)
		OEntityKey key;
		try {
			key = CommandHelper.createEntityKey(edmDataServices, entity, ctx.getId());
		} catch(Exception e) {
			throw new InteractionException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
		}
		
		// delete the entity
		try {
			producer.deleteEntity(entity, key);
		}
		catch(ODataProducerException ope) {
			logger.debug("Failed to delete entity [" + key.toKeyString() + "]: " + ope.getMessage());
			throw new InteractionException(ope.getHttpStatus(), ope.getMessage());
		}
		catch(Exception e) {
			logger.debug("Error while deleting entity [" + key.toKeyString() + "]: " + e.getMessage());
			throw new InteractionException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
		}
		
		return Result.SUCCESS;
	}
}
