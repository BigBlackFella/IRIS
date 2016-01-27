package com.temenos.interaction.odataext.odataparser;

/*
 * Test class for the oData parser/printer filter operators.
 */

/* 
 * #%L
 * interaction-commands-authorization
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import com.temenos.interaction.odataext.odataparser.data.Relation;
import com.temenos.interaction.odataext.odataparser.data.RowFilter;

public class ODataParserOperatorFilterTest extends AbstractODataParserFilterTest {

    /**
     * Test a simple filter
     */
    @Test
    public void testSimpleFilter() {
        testValid("a eq b");
    }

    /**
     * Test binary string filters
     */
    @Test
    public void testStringFilters() {
        for (Relation rel : Relation.values()) {
            if (!rel.isNumeric() && !rel.isBoolean() && (2 == rel.getExpectedArgumentCount()) && !rel.isFunctionCall()) {
                testValid("a " + rel.getoDataString() + " b");
            }
        }
    }

    /**
     * Test binary numeric filters.
     */
    @Test
    public void testNumericFilters() {
        // Test all binary relations
        for (Relation rel : Relation.values()) {
            if (rel.isNumeric() && !rel.isFunctionCall()) {
                testValid("a eq 1 " + rel.getoDataString() + " 2");
                testValid("1 " + rel.getoDataString() + " 2 eq b");
            }
        }
    }

    /**
     * Test boolean unary filters.
     */
    @Test
    public void testBooleanUnaryFilters() {
        for (Relation rel : Relation.values()) {
            if (rel.isBoolean() && (1 == rel.getExpectedArgumentCount()) && !rel.isFunctionCall()) {
                testValid(rel.getoDataString() + " false");
            }
        }
    }

    /**
     * Test boolean binary filters.
     */
    @Test
    public void testBooleanBinaryFilters() {
        for (Relation rel : Relation.values()) {
            if (rel.isBoolean() && (2 == rel.getExpectedArgumentCount()) && (!rel.isFunctionCall())) {
                testValid("true " + rel.getoDataString() + " false");
            }
        }
    }

    /**
     * Test bracketed operations.
     */
    @Test
    public void testBracketFilter() {
        testValid("(a eq 1) and (b ne 2)");
        testValid("(1 add 2) ne (3 div 4)");
        testValid("((1 add 2)) ne (3 div 4)");
    }

    // TODO add tests for functions
    @Test
    public void testBinaryFunctionsFilter() {
        testValid("a eq substringof('a', b)");
    }

    @Test
    public void testUnaryFunctionsFilter() {
        testValid("b eq tolower('TeSt')");
    }

    /**
     * Test gel Sql symbol
     */
    @Test
    public void testGetSqlSymbolFilter() {
        List<RowFilter> filter = null;
        try {
            filter = ODataParser.parseFilter("a eq b");
        } catch (Exception e) {
            fail();
        }
        assertEquals("=", filter.get(0).getRelation().getSqlSymbol());
    }

    /**
     * Test empty filters.
     */
    @Test
    public void testEmptyFilter() {
        testValid("");
    }

    /*
     * Test filter containing multiple terms
     */
    @Test
    public void testMultipleFilter() {
        testValid("a eq b and bb eq cc");
    }

    /*
     * Test filter containing quoted space elements.
     */
    @Test
    public void testQuotesSpaceFilter() {
        testValid("'a b' eq 'b c'");
    }

    /*
     * Test filter containing quoted dot elements.
     */
    @Test
    public void testQuotesDotFilter() {
        testValid("'a.b' eq 'b.c'");
    }

    /**
     * Test invalid filters throw.
     */
    @Test
    public void testBadFilter() {
        // Bad condition
        testInvalid("a xx b");

        // Can't parse a null string.
        testInvalid(null);

        // Wrong number of element
        testInvalid("a");
        testInvalid("a b");
        testInvalid("a b c");
    }

    /**
     * Test null intermediate filter.
     */
    @Test
    public void testNullFilter() {

        String actual = null;
        boolean threw = false;
        try {
            actual = ODataParser.toFilters(null);
        } catch (Exception e) {
            threw = true;
        }

        assertTrue("Didn't throw. Expected \"" + null + "\"Actual is \"" + actual + "\"", threw);
    }
}
