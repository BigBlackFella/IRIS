package com.temenos.useragent.generic.http;

/*
 * #%L
 * useragent-generic-java
 * %%
 * Copyright (C) 2012 - 2016 Temenos Holdings N.V.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */


import java.io.InputStream;

import com.temenos.useragent.generic.Result;

public class HttpResponseImpl implements HttpResponse {

	private HttpHeader header;
	private InputStream payload;
	private Result result;

	public HttpResponseImpl(HttpHeader header, InputStream payload, Result result) {
		this.header = header;
		this.payload = payload;
		this.result = result;
	}

	@Override
	public HttpHeader headers() {
		return header;
	}

	@Override
	public InputStream payload() {
		return payload;
	}

	@Override
	public Result result() {
		return result;
	}
	
}
