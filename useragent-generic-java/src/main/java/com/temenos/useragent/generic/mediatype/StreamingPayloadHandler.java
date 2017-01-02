package com.temenos.useragent.generic.mediatype;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import com.temenos.useragent.generic.Link;
import com.temenos.useragent.generic.PayloadHandler;
import com.temenos.useragent.generic.internal.EntityWrapper;

public class StreamingPayloadHandler implements PayloadHandler {
	
	private StreamingEntityWrapper streamingEntity;
	
	@Override
	public boolean isCollection() {
		return false;
	}

	@Override
	public List<Link> links() {
		return Collections.emptyList();
	}

	@Override
	public List<EntityWrapper> entities() {
		return Collections.emptyList();
	}

	@Override
	public EntityWrapper entity() {
		return streamingEntity;
	}

	@Override
	public void setPayload(InputStream payload) {
		streamingEntity = new StreamingEntityWrapper(payload);
	}

	@Override
	public void setParameter(String parameter) {
		// do nothing
	}
}
