package com.temenos.useragent.generic.internal;

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

import com.temenos.useragent.generic.Result;
import com.temenos.useragent.generic.http.HttpHeader;

/**
 * Defines the data part of the http execution response.
 * 
 * @author ssethupathi
 *
 */
public interface ResponseData {

	/**
	 * Returns the header part of the response.
	 * 
	 * @return response header
	 */
	HttpHeader header();

	/**
	 * Returns the http execution result for the execution.
	 * 
	 * @return http execution result
	 */
	Result result();

	/**
	 * Returns the payload part of the response.
	 * 
	 * @return response payload
	 */
	Payload body();

}
