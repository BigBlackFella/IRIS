package com.temenos.interaction.core.hypermedia.transition;

/*
 * #%L
 * interaction-core
 * %%
 * Copyright (C) 2012 - 2017 Temenos Holdings N.V.
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


import com.temenos.interaction.core.hypermedia.Transformer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Generates properties out of an entity object using a {@link Transformer}.
 *
 * @author ikarady
 */
public class EntityPropertiesGenerator implements PropertiesGenerator {

    private Transformer transformer;
    private Object entity;

    public EntityPropertiesGenerator(Transformer transformer, Object entity) {
        this.transformer = transformer;
        this.entity = entity;
    }

    /**
     * Generates properties out of its entity object.
     *
     * @return properties
     */
    @Override
    public Map<String, Object> generate() {
        Map<String, Object> properties = new HashMap<>();
        if (entity != null && transformer != null) {
            properties = transformer.transform(entity);
        }
        if (properties != null) {
            return properties;
        } else {
            return Collections.emptyMap();
        }
    }
}
