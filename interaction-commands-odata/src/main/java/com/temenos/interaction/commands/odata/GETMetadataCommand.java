package com.temenos.interaction.commands.odata;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.odata4j.edm.EdmDataServices;
import org.odata4j.producer.ODataProducer;

import com.temenos.interaction.core.media.ODataMetadata;
import com.temenos.interaction.core.resource.MetaDataResource;
import com.temenos.interaction.core.RESTResponse;
import com.temenos.interaction.core.resource.ServiceDocumentResource;
import com.temenos.interaction.core.command.ResourceGetCommand;

/**
 * GET command for obtaining meta data defining either the
 * resource model or the service document. 
 */
public class GETMetadataCommand implements ResourceGetCommand {

	private EdmDataServices edmDataServices;
	private String entity;

	/**
	 * Construct an instance of this command
	 * @param entity Entity name
	 * @param producer Producer
	 */
	public GETMetadataCommand(String entity, ODataProducer producer) {
		this.entity = entity;
		this.edmDataServices = producer.getMetadata();
	}

	/**
	 * Construct an instance of this command
	 * @param entity Entity name
	 * @param odataMetadata OData metadata
	 */
	public GETMetadataCommand(String entity, ODataMetadata odataMetadata) {
		this.entity = entity;
		this.edmDataServices = odataMetadata.getMetadata();
	}
	
	@Override
	public RESTResponse get(String id, MultivaluedMap<String, String> queryParams) {
		RESTResponse rr;
		if(entity.equals("ServiceDocument")) {
			ServiceDocumentResource<EdmDataServices> sdr = CommandHelper.createServiceDocumentResource(edmDataServices);
			rr = new RESTResponse(Response.Status.OK, sdr);
		}
		else {
			MetaDataResource<EdmDataServices> mdr = CommandHelper.createMetaDataResource(edmDataServices);
			rr = new RESTResponse(Response.Status.OK, mdr);
		}
		return rr;
	}
}
