package com.temenos.interaction.media.odata.xml.error;

/*
 * #%L
 * interaction-media-odata-xml
 * %%
 * Copyright (C) 2012 - 2016 Temenos Holdings N.V.
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.temenos.interaction.core.command.CommandHelper;
import com.temenos.interaction.core.entity.GenericError;
import com.temenos.interaction.core.resource.EntityResource;

import org.apache.commons.httpclient.HttpStatus;

/**
 * Marshals an unhandled, unchecked exception thrown by IRIS
 * into a GenericError. 
 *
 * @author dgroves
 *
 */
@Provider
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
public class WebApplicationExceptionHandler extends InteractionExceptionHandler<WebApplicationException> 
        implements ExceptionMapper<WebApplicationException> {
    
    private static final Logger logger = LoggerFactory.getLogger(WebApplicationExceptionHandler.class);
    
    @Override
    public Response toResponse(WebApplicationException exception) {
        String code = Integer.toString(exception.getResponse().getStatus());
        String message = new StringBuilder("HTTP ")
            .append(code)
            .append(" ")
            .append(HttpStatus.getStatusText(exception.getResponse().getStatus()))
            .toString();
        logger.error(message, exception);
        EntityResource<?> er = CommandHelper.createEntityResource(new GenericError(code, 
                getStackTraceAsString(exception)), GenericError.class);
        return Response.status(exception.getResponse().getStatus())
            .entity(er.getGenericEntity()).build();
    }
}
