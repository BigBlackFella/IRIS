package com.temenos.interaction.core.state;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.temenos.interaction.core.EntityResource;

public interface HTTPResourceInteractionModel {

	/**
	 * GET a resource representation.
	 */
	@GET
	@Produces({
			MediaType.APPLICATION_ATOM_XML,
			MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON,
			com.temenos.interaction.core.media.hal.MediaType.APPLICATION_HAL_XML })
	public abstract Response get(@Context HttpHeaders headers,
			@PathParam("id") String id,
			@Context UriInfo uriInfo);

	/**
	 * PUT a resource.
	 */
	@PUT
	@Consumes({
			MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON,
			com.temenos.interaction.core.media.hal.MediaType.APPLICATION_HAL_XML })
	public abstract Response put(@Context HttpHeaders headers,
			@PathParam("id") String id, EntityResource resource);

	/**
	 * DELETE a resource.
	 */
	@DELETE
	public abstract Response delete(@Context HttpHeaders headers,
			@PathParam("id") String id);


}