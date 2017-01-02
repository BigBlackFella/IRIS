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

import java.io.InputStream;

import com.temenos.useragent.generic.PayloadHandler;

/**
 * Factory to supply {@link PayloadHandler payload handlers} for supported media
 * types.
 * <p>
 * Payload handlers implementing {@link PayloadHandler} are registered against
 * the media types they support in a registry and available for handling payload
 * dynamically.
 * </p>
 * TODO support for non string type/generic payloads
 * 
 * @author ssethupathi
 *
 */
public class PayloadHandlerFactory<T extends PayloadHandler> {
	private final Class<? extends PayloadHandler> handlerClass;

	private PayloadHandlerFactory(
			final Class<? extends PayloadHandler> handlerType) {
		this.handlerClass = handlerType;
	}

	/**
	 * Creates the factory for a {@link PayloadHandler payload handler} type.
	 * 
	 * @param handlerClass
	 * @return Payload hander factory
	 */
	public static <T extends PayloadHandler> PayloadHandlerFactory<T> createFactory(
			final Class<? extends PayloadHandler> handlerClass) {
		if (handlerClass == null) {
			throw new IllegalArgumentException(
					"Illegal payload handler class 'null'");
		}
		return new PayloadHandlerFactory<T>(handlerClass);
	}

	/**
	 * Creates the appropriate {@link PayloadHandler payload handler} for the
	 * payload/
	 * 
	 * @param payload
	 * @return payload handler
	 */
	public PayloadHandler createHandler(InputStream payload) {
		try {
			PayloadHandler handler = handlerClass.newInstance();
			handler.setPayload(payload);
			return handler;
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
	}
}
