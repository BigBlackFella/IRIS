package com.temenos.interaction.core.command;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.temenos.interaction.core.resource.EntityResource;
import com.temenos.interaction.core.RESTResponse;

/**
 * A GET command that does nothing.  Can be useful for laying out a straw
 * man of resources and not needing to implement them all initially.
 * @author aphethean
 */
public final class NoopGETCommand implements ResourceGetCommand {

	@Override
	public RESTResponse get(String id,
			MultivaluedMap<String, String> queryParams) {
		return new RESTResponse(Response.Status.OK, new EntityResource<String>(""));
	}
	
}
