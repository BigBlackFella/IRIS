package com.temenos.interaction.commands.odata;

/*
 * #%L
 * interaction-commands-odata
 * %%
 * Copyright (C) 2012 - 2013 Temenos Holdings N.V.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */


import javax.ws.rs.core.Response.Status;

import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.exceptions.NotFoundException;
import org.odata4j.exceptions.ODataProducerException;
import org.odata4j.producer.ODataProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.temenos.interaction.core.resource.EntityResource;
import com.temenos.interaction.core.command.InteractionCommand;
import com.temenos.interaction.core.command.InteractionContext;
import com.temenos.interaction.core.command.InteractionException;
import com.temenos.interaction.core.entity.Entity;

/**
 * This command attempts to update an entity, it will create the entity if it doesn't exist.
 * @author aphethean
 *
 */
public class UpdateCreateEntityCommand extends AbstractODataCommand implements InteractionCommand {
	private final static Logger LOGGER = LoggerFactory.getLogger(UpdateCreateEntityCommand.class);

	public UpdateCreateEntityCommand(ODataProducer producer) {
		super(producer);
	}

	/* Implement InteractionCommand interface */
	
	@SuppressWarnings("unchecked")
	@Override
	public Result execute(InteractionContext ctx) throws InteractionException {
		assert(ctx != null);
		assert(ctx.getCurrentState() != null);
		assert(ctx.getCurrentState().getEntityName() != null && !ctx.getCurrentState().getEntityName().equals(""));
		assert(ctx.getResource() != null);
		
		Result result = null;
		
		// update the entity
		String entityName = getEntityName(ctx);
		LOGGER.debug("Getting entity for " + entityName);
		try {
			EdmEntitySet entitySet = getEdmEntitySet(entityName);
			// create the entity
			OEntity entity = null;
			try {
				entity = ((EntityResource<OEntity>) ctx.getResource()).getEntity();
			} catch (ClassCastException cce) {
			    if(LOGGER.isDebugEnabled()) {
			        LOGGER.debug("OEntity class not found.", cce);
			    }
				//Create entity key (simple types only)
				OEntityKey key = CommandHelper.createEntityKey(entitySet, ctx.getId());
				entity = CommandHelper.createOEntityFromEntity(this, producer, ((EntityResource<Entity>) ctx.getResource()).getEntity(), key);
			}

			if (merge(entityName, entity)) {
				result = Result.SUCCESS;
			} else {
				producer.createEntity(entitySet.getName(), entity);
				result = Result.CREATED;
			}
		} catch(ODataProducerException ope) {
			LOGGER.debug("Failed to update entity [" + entityName + "]: ", ope);
			throw new InteractionException(ope.getHttpStatus(), ope);
		} catch(Exception e) {
			LOGGER.debug("Error while updating entity [" + entityName + "]: ", e);
			throw new InteractionException(Status.INTERNAL_SERVER_ERROR, e);
		}
		
		assert(result != null);
		ctx.setResource(null);
		return result;
	}
	
	private boolean merge(String entityName, OEntity entity) throws ODataProducerException {
		try {
			producer.mergeEntity(entityName, entity);
		} catch(NotFoundException nfe) {
			LOGGER.debug("Failed to update entity, not found [" + entityName + "]: ", nfe);
			return false;
		}
		return true;
	}

}
