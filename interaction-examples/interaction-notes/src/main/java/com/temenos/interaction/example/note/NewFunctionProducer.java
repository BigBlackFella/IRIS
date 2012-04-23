package com.temenos.interaction.example.note;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.odata4j.core.OFunctionParameter;
import org.odata4j.core.OProperties;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntityContainer;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmFunctionParameter;
import org.odata4j.edm.EdmSchema;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.producer.BaseResponse;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.ODataProducerDelegate;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.Responses;
import org.odata4j.producer.jpa.JPAProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.temenos.interaction.core.command.NotSupportedCommand;

/**
 * A producer delegate that implements a simple new functions for testing.
 */
public class NewFunctionProducer extends ODataProducerDelegate {
	private final Logger logger = LoggerFactory.getLogger(NewFunctionProducer.class);

    private final EntityManagerFactory emf;
//    private final String odataNamespace;
	private final JPAProducer producer;
	private EdmDataServices metadata;

    public NewFunctionProducer(EntityManagerFactory emf, String odataNamespace, JPAProducer jpaProducer) {
    	this.emf = emf;
//    	this.odataNamespace = odataNamespace;
        producer = jpaProducer;
        metadata = getDelegate().getMetadata();
        extendModel();
    }
    
    // implement Delegate<ODataProducer>
    public ODataProducer getDelegate() {
        return producer;
    }
    
    @Override
    public BaseResponse callFunction(EdmFunctionImport function, java.util.Map<String, OFunctionParameter> params, QueryInfo queryInfo) {
        if (function.getName().equals("NEW")) {
          return createNewDomainObjectID(function, params, queryInfo);
        } else {
        	logger.error("Unknown function");
    	    throw new WebApplicationException(Response.status(NotSupportedCommand.HTTP_STATUS_NOT_IMPLEMENTED).entity(NotSupportedCommand.HTTP_STATUS_NOT_IMPLEMENTED_MSG).build());
        }

    }
    
    private BaseResponse createNewDomainObjectID(EdmFunctionImport function, java.util.Map<String, OFunctionParameter> params, QueryInfo queryInfo) {
    	// perform update to database ID table
    	EntityManager em = emf.createEntityManager();
    	em.getTransaction().begin();
    	Query update = em.createQuery("UPDATE ID x SET x.id=x.id+1 WHERE DomainObjectName = 'NOTE'");
    	update.executeUpdate();
    	Query query = em.createQuery("SELECT x.id FROM ID x WHERE DomainObjectName = 'NOTE'");
    	Long value = (Long) query.getSingleResult();
    	em.getTransaction().commit();

        return Responses.property(OProperties.int64("id", value));
    }
    
    private void extendModel() {
        // add some functions to the edm
        EdmDataServices ds = this.getMetadata();
        
        // Add functions to our own namespace
        //EdmSchema schema = ds.findSchema(odataNamespace + "Container");
//        EdmEntityContainer container = schema.findEntityContainer(odataNamespace + "Entities");
        
        /*
         * Add 'createNewDomainObjectID' function
         */
        List<EdmFunctionParameter.Builder> params = new ArrayList<EdmFunctionParameter.Builder>(1);
        EdmFunctionParameter.Builder efp = EdmFunctionParameter.newBuilder().setName("DOMAIN_OBJECT_NAME").setType(EdmSimpleType.STRING).setMode(EdmFunctionParameter.Mode.In);
        params.add(efp);
        
        EdmFunctionImport.Builder f = EdmFunctionImport.newBuilder().setName("NEW").setReturnType(EdmSimpleType.INT64).setHttpMethod("POST").addParameters(params);
        
        // replace metadata service?
        EdmEntityContainer.Builder newContainer = EdmEntityContainer.newBuilder()
        		.addFunctionImports(f);
        EdmSchema.Builder newSchema = EdmSchema.newBuilder()
        		.addEntityContainers(newContainer);
        metadata = EdmDataServices.newBuilder(ds).addSchemas(newSchema).build();
    }
    
    @Override
    public EdmDataServices getMetadata() {
    	return metadata;
    }
}
