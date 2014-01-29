package com.temenos.interaction.core.workflow;

/*
 * #%L
 * interaction-core
 * %%
 * Copyright (C) 2012 - 2014 Temenos Holdings N.V.
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


import static org.mockito.Mockito.mock;

import javax.ws.rs.core.Response.Status;

import com.temenos.interaction.core.command.InteractionCommand;
import com.temenos.interaction.core.command.InteractionContext;
import com.temenos.interaction.core.command.InteractionException;

/**
 * MockInteractionCommand
 *
 * @author mjangid
 *
 */
public class MockInteractionCommand implements InteractionCommand {

	InteractionContext ctx = mock(InteractionContext.class);

	
	/* Implement InteractionCommand interface */
	@Override
	public Result execute( InteractionContext ctx ) throws InteractionException
	{
		throw new InteractionException(Status.INTERNAL_SERVER_ERROR, "Test Exception");
	}
}
