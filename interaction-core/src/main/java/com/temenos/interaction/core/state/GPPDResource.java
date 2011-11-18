package com.temenos.interaction.core.state;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.temenos.interaction.core.RESTResource;
import com.temenos.interaction.core.RESTResponse;
import com.temenos.interaction.core.command.CommandController;
import com.temenos.interaction.core.command.ResourceGetCommand;
import com.temenos.interaction.core.command.ResourcePutCommand;
import com.temenos.interaction.core.decorator.Decorator;
import com.temenos.interaction.core.decorator.JSONStreamingDecorator;
import com.temenos.interaction.core.decorator.hal.HALXMLDecorator;

public abstract class GPPDResource<RESOURCE extends RESTResource> implements ResourceStateTransition {
	private final Logger logger = LoggerFactory.getLogger(GPPDResource.class);

	// TODO inject decorators
	private static Decorator<StreamingOutput> DEFAULT_DECORATOR = new JSONStreamingDecorator();
	private static Map<String, Decorator<StreamingOutput>> decorators = new HashMap<String, Decorator<StreamingOutput>>();

	private String resourcePath;
	private CommandController commandController = new CommandController();
	
	static {
		decorators.put(MediaType.APPLICATION_JSON, new JSONStreamingDecorator());
		decorators.put(com.temenos.interaction.core.decorator.hal.MediaType.APPLICATION_HAL_XML, new HALXMLDecorator());
	}
	
	/* Keep JAXB happy */
	public GPPDResource() {}
	
	public GPPDResource(String resourcePath) {
		this.resourcePath = resourcePath;
	}
	
	public String getResourcePath() {
		return resourcePath;
	}

	protected CommandController getCommandController() {
		return commandController;
	}
	
	/**
	 * GET a resource representation.
	 * @precondition a valid GET command for this resourcePath + id must be registered with the command controller
	 * @invariant resourcePath not null
	 */
    @GET
//    @Produces({MediaType.APPLICATION_JSON, com.temenos.interaction.core.decorator.hal.MediaType.APPLICATION_HAL_XML})
    public Response get( @Context HttpHeaders headers, @PathParam("id") String id ) {
    	ResourceGetCommand getCommand = commandController.fetchGetCommand(getResourcePath());
		Response.Status status = getCommand.get(id);
		assert (status != null);  // not a valid get command
		RESTResource resource = getCommand.getResource();
		if (status.getFamily() == Response.Status.Family.SUCCESSFUL) {
			assert(resource != null);
			// TODO need to be smarter in the way we evaluate acceptable media types
			Decorator<StreamingOutput> d = null;
			if (headers.getAcceptableMediaTypes().size() > 0) {
				logger.debug("Accept header: " + headers.getAcceptableMediaTypes());
				String usingMT = headers.getAcceptableMediaTypes().get(0).toString();
				logger.info("Using media type: " + usingMT);
				d = decorators.get(usingMT);
			}
			if (d == null)
				d = DEFAULT_DECORATOR;
			
	    	StreamingOutput so = d.decorateRESTResponse(new RESTResponse(status, resource));
	    	return HeaderHelper.allowHeader(Response.ok(so), getCommand).build();
		}
		return Response.status(status).build();
    }

    /*
    @GET
    @Produces(com.temenos.interaction.core.decorator.hal.MediaType.APPLICATION_HAL_XML)
    public Response getHALXml( @PathParam("id") String id ) {
    	ResourceGetCommand getCommand = commandController.fetchGetCommand(getResourcePath());
    	Response.Status status = getCommand.get(id);
    	Decorator<Response> d = new HALXMLDecorator();
    	Response r = d.decorateRESTResponse(new RESTResponse(status, getCommand.getResource()));
    	return HeaderHelper.allowHeader(Response.ok(r), getCommand).build();
    }
*/

    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    public Response putText( @PathParam("id") String id, String resource ) {
    	return null;
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, com.temenos.interaction.core.decorator.hal.MediaType.APPLICATION_HAL_XML})
    public Response post( @PathParam("id") String id, RESOURCE resource ) {
    	return null;
    }
    
    
	/**
	 * GET a resource.
	 * @precondition a valid PUT command for this resourcePath + id must be registered with the command controller
	 * @invariant resourcePath not null
	 */
    @PUT
    @Consumes({MediaType.APPLICATION_JSON, com.temenos.interaction.core.decorator.hal.MediaType.APPLICATION_HAL_XML})
    public Response put( @Context HttpHeaders headers, @PathParam("id") String id, RESOURCE resource ) {
		ResourcePutCommand<RESOURCE> putCommand = (ResourcePutCommand<RESOURCE>) commandController.fetchStateTransitionCommand("PUT", getResourcePath());
    	Response.Status status = putCommand.put(id, resource);
		assert (status != null);  // not a valid put command
    	if (status == Response.Status.OK) {
        	return get(headers, id);
    	} else {
    		return Response.status(status).build();
    	}
    }

    /*
    @PUT
    @Consumes(com.temenos.interaction.core.decorator.hal.MediaType.APPLICATION_HAL_XML)
    public Response putHAL( @Context HttpHeaders headers, @PathParam("id") String id, RESOURCE resource ) {
    	ResourcePutCommand<RESOURCE> putCommand = (ResourcePutCommand<RESOURCE>) commandController.fetchStateTransitionCommand("PUT", getResourcePath());
    	Response.Status status = putCommand.put(id, resource);
    	if (status == Response.Status.OK) {
        	return get(headers, id);
    	} else {
    		return Response.status(status).build();
    	}
    }
*/
    
    public Response options(String id ) {
    	ResourceGetCommand getCommand = commandController.fetchGetCommand(getResourcePath());
    	ResponseBuilder response = Response.ok();
    	Response.Status status = getCommand.get(id);
    	if (status == Response.Status.OK) {
        	response = HeaderHelper.allowHeader(response, getCommand);
    	}
    	return response.build();
    }
}
