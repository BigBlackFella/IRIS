package com.temenos.interaction.odataext.odataparser;

/*
 * Utilities for converting between oData parameters and editable structures containing the same information. This is
 * intended to replace the, light weight, oData parser under com.temenos.interaction.authorization.command.util.
 * 
 * This version uses parsing based on oData4j to implement the full oData parameter syntax (previous version used in 
 * house code to support a limited subset).
 */

/*
 * #%L
 * interaction-authorization
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.odata4j.expression.CommonExpression;
import org.odata4j.expression.EntitySimpleProperty;
import org.odata4j.expression.OrderByExpression;
import org.odata4j.producer.resources.OptionsQueryParser;

import com.temenos.interaction.odataext.odataparser.data.FieldName;
import com.temenos.interaction.odataext.odataparser.data.OrderBy;
import com.temenos.interaction.odataext.odataparser.data.RowFilter;
import com.temenos.interaction.odataext.odataparser.data.RowFilters;
import com.temenos.interaction.odataext.odataparser.output.OutputParameters;

public class ODataParser {

    // Odata parameter keys.
    public static final String FILTER_KEY = "$filter";
    public static final String SELECT_KEY = "$select";
    public static final String TOP_KEY = "$top";
    public static final String SKIP_KEY = "$skip";
    public static final String ORDERBY_KEY = "$orderby";

    // Convert an OData string parameter into filters.
    public static RowFilters parseFilters(String filterStr) {
        // Parse in odat4j format
        return (new RowFilters(filterStr));
    }

    // Support for old code that still uses RowFilters (note method name Filter
    // NOT Filters)
    public static List<RowFilter> parseFilter(String filterStr) throws UnsupportedQueryOperationException {
        // First do it the new way
        RowFilters filters = parseFilters(filterStr);

        // Then convert to old style
        return filters.asRowFilters();
    }

    // Convert an OData select string parameter into a set of field names.
    public static Set<FieldName> parseSelect(String selectStr) {
        // Parse in odat4j format
        List<EntitySimpleProperty> odata4jSelects = OData4jParseSelect(selectStr);

        // Wrap the odata4j output.
        Set<FieldName> selects = new HashSet<FieldName>();
        for (EntitySimpleProperty select : odata4jSelects) {
            selects.add(new FieldName(select));
        }
        return selects;
    }

    private static List<EntitySimpleProperty> OData4jParseSelect(String selectStr) {
        if (null == selectStr) {
            return (null);
        }
        return OptionsQueryParser.parseSelect(selectStr);
    }

    // Parse an $orderby expression.
    public static List<OrderBy> parseOrderBy(String orderBy) {
        if (null == orderBy) {
            return null;
        }

        List<OrderByExpression> expressions = OData4jParseOrderBys(orderBy);

        // Wrap the odata4j output.
        List<OrderBy> orderBys = new ArrayList<OrderBy>();
        for (OrderByExpression expression : expressions) {
            orderBys.add(new OrderBy(expression));
        }
        return orderBys;
    }

    private static List<OrderByExpression> OData4jParseOrderBys(String orderByStr) {
        if (null == orderByStr) {
            return (null);
        }
        return OptionsQueryParser.parseOrderBy(orderByStr);
    }

    // Convert filter to an oData parameter
    public static String toFilters(RowFilters filters) {
        return (OData4jToFilters(filters.getBoolCommonExpression()));
    }

    // Once it is no longer needed for testing this shold probably be made
    // private.
    public static String OData4jToFilters(CommonExpression filters) {

        if (null != filters) {
            StringBuffer sb = new StringBuffer();
            OutputParameters.appendParameter(sb, filters, true);
            return (sb.toString());
        } else {
            // This is the empty filter list case. Just return an empty string;
            return ("");
        }
    }

    // Convert select to an oData parameter
    public static String toSelect(Set<FieldName> selects) {
        List<EntitySimpleProperty> oData4jSelects = new ArrayList<EntitySimpleProperty>();
        for (FieldName select : selects) {
            oData4jSelects.add(select.getEntitySimpleProperty());
        }
        return OData4jToSelect(oData4jSelects);
    }

    private static String OData4jToSelect(List<EntitySimpleProperty> selects) {
        StringBuffer sb = new StringBuffer();
        OutputParameters.appendParameter(sb, selects, true);
        return (sb.toString());
    }

    // Convert order by to an OData parameter.
    public static String toOrderBy(List<OrderBy> orderByList) {
        List<OrderByExpression> oData4jOrderBys = new ArrayList<OrderByExpression>();
        for (OrderBy orderBy : orderByList) {
            oData4jOrderBys.add(orderBy.getOrderByExpression());
        }
        return OData4jToOrderBy(oData4jOrderBys);
    }

    private static String OData4jToOrderBy(List<OrderByExpression> orderBys) {
        StringBuffer sb = new StringBuffer();
        OutputParameters.appendParameter(sb, orderBys, true);
        return (sb.toString());
    }

    // Errors thrown by parsing
    public static class UnsupportedQueryOperationException extends Exception {
        private static final long serialVersionUID = 1L;

        public UnsupportedQueryOperationException(String message) {
            super(message);
        }
    }
}