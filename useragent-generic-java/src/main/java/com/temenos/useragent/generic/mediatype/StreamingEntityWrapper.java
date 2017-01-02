package com.temenos.useragent.generic.mediatype;

import java.io.InputStream;

import com.temenos.useragent.generic.Links;
import com.temenos.useragent.generic.internal.EntityHandler;
import com.temenos.useragent.generic.internal.EntityWrapper;
import com.temenos.useragent.generic.internal.SessionContext;

public class StreamingEntityWrapper implements EntityWrapper {

	private InputStream stream;

	public StreamingEntityWrapper(InputStream stream) {
		this.stream = stream;
	}

	@Override
	public String id() {
		throw new IllegalStateException(
				"Unsupported operation for streaming entity");
	}

	@Override
	public String get(String fqName) {
		throw new IllegalStateException(
				"Unsupported operation for streaming entity");
	}

	@Override
	public int count(String fqName) {
		throw new IllegalStateException(
				"Unsupported operation for streaming entity");
	}

	@Override
	public Links links() {
		throw new IllegalStateException(
				"Unsupported operation for streaming entity");
	}

	@Override
	public void setHandler(EntityHandler handler) {
		// do nothing
	}

	@Override
	public void setSessionContext(SessionContext sessionContext) {
		// do nothing
	}

	@Override
	public void set(String fqPropertyName, String value) {
		throw new IllegalStateException(
				"Unsupported operation for streaming entity");
	}

	@Override
	public void remove(String fqPropertyName) {
		throw new IllegalStateException(
				"Unsupported operation for streaming entity");
	}

	@Override
	public InputStream getContent() {
		return stream;
	}
}
