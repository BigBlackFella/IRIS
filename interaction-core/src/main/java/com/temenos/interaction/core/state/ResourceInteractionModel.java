package com.temenos.interaction.core.state;

import javax.ws.rs.OPTIONS;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

public interface ResourceInteractionModel {

	public String getEntityName();
	public String getResourcePath();
	
    @OPTIONS
    public Response options( @PathParam("id") String id );
	
}
