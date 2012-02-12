package com.temenos.interaction.winkext;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.wink.common.DynamicResource;

import com.temenos.interaction.core.EntityResource;
import com.temenos.interaction.core.state.HTTPResourceInteractionModel;
import com.temenos.interaction.core.state.HTTPResourceInteractionModelIntf;

public class DynamicResourceDelegate implements HTTPResourceInteractionModelIntf, DynamicResource {

	private final HTTPResourceInteractionModelIntf parent;
	private final HTTPResourceInteractionModel resource;
	
	public DynamicResourceDelegate(HTTPResourceInteractionModelIntf parent, HTTPResourceInteractionModel resource) {
		this.parent = parent;
		this.resource = resource;
	}

	@Override
    public String getBeanName() {
        return resource.getEntityName();
    }

	@Override
    public void setBeanName(String beanName) {
        throw new AssertionError("Not supported");
    }

    public void setWorkspaceTitle(String workspaceTitle) {
        throw new AssertionError("Not supported");
    }

	@Override
    public String getWorkspaceTitle() {
        return null;
    }

    public void setCollectionTitle(String collectionTitle) {
        throw new AssertionError("Not supported");
    }

	@Override
    public String getCollectionTitle() {
        return null;
    }

	@Override
    public String getPath() {
        return resource.getResourcePath();
    }

	@Override
    public void setParent(Object parent) {
        throw new AssertionError("Not supported");
    }

	@Override
    public HTTPResourceInteractionModelIntf getParent() {
        return parent;
    }

	@Override
	public Response get(HttpHeaders headers, String id) {
		return resource.get(headers, id);
	}

	@Override
	public Response put(HttpHeaders headers, String id, EntityResource eresource) {
		return resource.put(headers, id, eresource);
	}

	@Override
	public Response delete(HttpHeaders headers, String id) {
		return resource.delete(headers, id);
	}

	public Response options(String id) {
		return resource.options(id);
	}
    
}
