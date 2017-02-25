package com.temenos.interaction.core;

/*
 * #%L
 * interaction-core
 * %%
 * Copyright (C) 2012 - 2013 Temenos Holdings N.V.
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


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

public class MultivaluedMapImpl<T> extends HashMap<String, List<T>> implements
		MultivaluedMap<String, T> {

	private static final long serialVersionUID = 2743101209228906279L;

	@Override
	public void putSingle(String key, T value) {
		List<T> values = getList(key);
		values.clear();
		values.add(value);
	}

	@Override
	public void add(String key, T value) {
		List<T> values = getList(key);
		if (!values.contains(value)) {
			values.add(value);
		}
	}

	@Override
	public T getFirst(String key) {
		List<T> values = getList(key);
		return (values.size() > 0 ? values.get(0) : null);
	}

	protected List<T> getList(String key) {
		List<T> values = get(key);
		if (values == null) {
			values = new LinkedList<T>();
			put(key, values);
		}
		return values;
	}
}
