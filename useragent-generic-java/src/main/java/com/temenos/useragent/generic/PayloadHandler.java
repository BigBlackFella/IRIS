package com.temenos.useragent.generic;

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
import java.util.List;

import com.temenos.useragent.generic.internal.EntityWrapper;

/**
 * Defines a handler for the payload for a media type.
 * 
 * @author ssethupathi
 *
 */
public interface PayloadHandler {

	/**
	 * Returns whether or not this payload contains collection of {@link Entity
	 * entities}.
	 * 
	 * @return true for collection of {@link Entity entities}, false otherwise
	 */
	boolean isCollection();

	/**
	 * Returns available {@link Link links} from the payload.
	 * 
	 * @return {@link Link links}
	 */
	List<Link> links();

	/**
	 * Returns the {@link EntityWrapper entities} in the payload which is a
	 * collection type.
	 * 
	 * @return
	 */
	List<EntityWrapper> entities();

	/**
	 * Returns the single {@link EntityWrapper entity} in the payload which is
	 * an item type.
	 * 
	 * @return
	 */
	EntityWrapper entity();

	/**
	 * Sets the payload to the handler.
	 * 
	 * @param payload
	 */
	void setPayload(InputStream payload);

	/**
	 * Sets any parameter part of the media type associated to this handler.
	 * <p>
	 * For example, <i>type=entry</i> is the parameter in the media type
	 * <i>application/atom+xml;type=entry</i> which will bet for handler to use
	 * in any processing.
	 * </p>
	 * 
	 * @param parameter
	 */
	void setParameter(String parameter);

}
