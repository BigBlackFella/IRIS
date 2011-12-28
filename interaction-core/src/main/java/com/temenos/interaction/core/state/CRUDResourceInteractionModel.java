package com.temenos.interaction.core.state;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.temenos.interaction.core.EntityResource;
import com.temenos.interaction.core.RESTResponse;
import com.temenos.interaction.core.command.CommandController;
import com.temenos.interaction.core.command.ResourceDeleteCommand;
import com.temenos.interaction.core.command.ResourceGetCommand;
import com.temenos.interaction.core.command.ResourcePostCommand;
import com.temenos.interaction.core.command.ResourcePutCommand;
import com.temenos.interaction.core.decorator.Decorator;
import com.temenos.interaction.core.decorator.JSONStreamingDecorator;
import com.temenos.interaction.core.decorator.hal.HALXMLDecorator;

/**
 * Define a Create Read Update Delete 'CRUD' Resource Interaction Model.
 * @author aphethean
 *
 */
public abstract class CRUDResourceInteractionModel implements ResourceStateTransition {
	private final Logger logger = LoggerFactory.getLogger(CRUDResourceInteractionModel.class);

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
	public CRUDResourceInteractionModel() {}
	
	public CRUDResourceInteractionModel(String resourcePath) {
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
	 * @postcondition a Response with non null Status must be returned
	 * @invariant resourcePath not null
	 */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, com.temenos.interaction.core.decorator.hal.MediaType.APPLICATION_HAL_XML})
    public Response get( @Context HttpHeaders headers, @PathParam("id") String id ) {
    	assert(resourcePath != null);
    	ResourceGetCommand getCommand = commandController.fetchGetCommand(getResourcePath());
    	RESTResponse response = getCommand.get(id);
    	assert (response != null);
    	StatusType status = response.getStatus();
		assert (status != null);  // not a valid get command
		if (status.getFamily() == Response.Status.Family.SUCCESSFUL) {
			assert(response.getResource() != null);
			return Response.ok(response.getResource()).status(status).build();
		}
		return Response.status(status).build();
    }

    /*
    @GET
    @Produces({com.temenos.interaction.core.decorator.hal.MediaType.APPLICATION_HAL_XML})
    public Response get( @Context HttpHeaders headers, @PathParam("id") String id ) {
    	assert(resourcePath != null);
    	ResourceGetCommand getCommand = commandController.fetchGetCommand(getResourcePath());
    	RESTResponse response = getCommand.get(id);
    	assert (response != null);
    	StatusType status = response.getStatus();
		assert (status != null);  // not a valid get command
		if (status.getFamily() == Response.Status.Family.SUCCESSFUL) {
			assert(response.getResource() != null);
			return decoratedResponse(headers, response);
		}
		return Response.status(status).build();
    }
    */
    
    private Response decoratedResponse(HttpHeaders headers, RESTResponse response) {
		assert(response != null);
		assert(headers != null);
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
		
    	StreamingOutput so = d.decorateRESTResponse(response);
    	return HeaderHelper.allowHeader(Response.status(response.getStatus()).entity(so), response).build();
    }
    
	/**
	 * POST a document to a resource.
	 * @precondition a valid POST command for this resourcePath + id must be registered with the command controller
	 * @postcondition a Response with non null Status must be returned
	 * @invariant resourcePath not null
	 */
    @POST
// TODO not used in CRUD
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, com.temenos.interaction.core.decorator.hal.MediaType.APPLICATION_HAL_XML})
    public Response post( @Context HttpHeaders headers, @PathParam("id") String id, EntityResource resource ) {
    	assert(resourcePath != null);
		ResourcePostCommand postCommand = (ResourcePostCommand) commandController.fetchStateTransitionCommand("POST", getResourcePath());
    	RESTResponse response = postCommand.post(id, resource);
    	assert (response != null);
    	StatusType status = response.getStatus();
    	assert (status != null);  // not a valid post command
		if (status.getFamily() == Response.Status.Family.SUCCESSFUL) {
			assert(response.getResource() != null);
			return decoratedResponse(headers, response);
		}
   		return Response.status(status).build();
    }
    
    
	/**
	 * PUT a resource.
	 * @precondition a valid PUT command for this resourcePath + id must be registered with the command controller
	 * @postcondition a Response with non null Status must be returned
	 * @invariant resourcePath not null
	 */
    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, com.temenos.interaction.core.decorator.hal.MediaType.APPLICATION_HAL_XML})
    public Response put( @Context HttpHeaders headers, @PathParam("id") String id, EntityResource resource ) {
    	assert(resourcePath != null);
		ResourcePutCommand putCommand = (ResourcePutCommand) commandController.fetchStateTransitionCommand("PUT", getResourcePath());
		StatusType status = putCommand.put(id, resource);
		assert (status != null);  // not a valid put command
    	if (status == Response.Status.OK) {
        	return get(headers, id);
    	} else {
    		return Response.status(status).build();
    	}
    }

	/**
	 * DELETE a resource.
	 * @precondition a valid DELETE command for this resourcePath + id must be registered with the command controller
	 * @postcondition a Response with non null Status must be returned
	 * @invariant resourcePath not null
	 */
    @DELETE
    public Response delete( @Context HttpHeaders headers, @PathParam("id") String id ) {
    	assert(resourcePath != null);
    	ResourceDeleteCommand deleteCommand = (ResourceDeleteCommand) commandController.fetchStateTransitionCommand("DELETE", getResourcePath());
		StatusType status = deleteCommand.delete(id);
		assert (status != null);  // not a valid put command
   		return Response.status(status).build();
    }

	/**
	 * OPTIONS for a resource.
	 * @precondition a valid GET command for this resourcePath + id must be registered with the command controller
	 * @postcondition a Response with non null Status must be returned
	 * @invariant resourcePath not null
	 */
    @Override
    public Response options(String id) {
    	assert(resourcePath != null);
    	ResourceGetCommand getCommand = commandController.fetchGetCommand(getResourcePath());
    	ResponseBuilder response = Response.ok();
    	RESTResponse rResponse = getCommand.get(id);
    	assert (rResponse != null);
    	StatusType status = rResponse.getStatus();
		assert (status != null);  // not a valid get command
    	if (status == Response.Status.OK) {
        	response = HeaderHelper.allowHeader(response, rResponse);
    	}
    	return response.build();
    }
}
