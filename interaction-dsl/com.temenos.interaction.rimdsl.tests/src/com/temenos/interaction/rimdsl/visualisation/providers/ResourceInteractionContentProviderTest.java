package com.temenos.interaction.rimdsl.visualisation.providers;

/*
 * #%L
 * com.temenos.interaction.rimdsl.tests
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


import static org.junit.Assert.*;

import java.util.Iterator;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.generator.IGenerator;
import org.eclipse.xtext.junit4.InjectWith;
import org.eclipse.xtext.junit4.XtextRunner;
import org.eclipse.xtext.junit4.util.ParseHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Iterators;
import com.google.inject.Inject;
import com.temenos.interaction.rimdsl.RIMDslInjectorProvider;
import com.temenos.interaction.rimdsl.rim.DomainModel;
import com.temenos.interaction.rimdsl.rim.State;
import com.temenos.interaction.rimdsl.visualisation.providers.ResourceInteractionContentProvider;

@InjectWith(RIMDslInjectorProvider.class)
@RunWith(XtextRunner.class)
public class ResourceInteractionContentProviderTest {

	@Inject 
	IGenerator underTest;
	@Inject
	ParseHelper<DomainModel> parseHelper;
	
	private final static String LINE_SEP = System.getProperty("line.separator");

	private final static String MULTIPLE_STATES = "" +
	"domain blah {" + LINE_SEP +
	"rim Test {" + LINE_SEP +
	"command GetEntity" + LINE_SEP +
			
	"initial resource A {" + LINE_SEP +
	"	type: collection" + LINE_SEP +
	"	entity: ENTITY" + LINE_SEP +
	"	view: GetEntity" + LINE_SEP +
	"	GET -> B" + LINE_SEP +
	"}" + LINE_SEP +

	"resource B {" + LINE_SEP +
	"	type: collection" + LINE_SEP +
	"	entity: ENTITY" + LINE_SEP +
	"	view: GetEntity" + LINE_SEP +
	"}" + LINE_SEP +

	"}" + LINE_SEP +  // end rim
	"}" + LINE_SEP +  // end domain
	"";

	@Test
	public void testTransitionsSimpleTransition() throws Exception {
		DomainModel domainModel = parseHelper.parse(MULTIPLE_STATES);
		EList<Resource.Diagnostic> errors = domainModel.eResource().getErrors();
		assertEquals(0, errors.size());

		Iterator<State> states = Iterators.filter(domainModel.eAllContents(), State.class);
		State A = states.next();
		assertEquals("A", A.getName());
		
		ResourceInteractionContentProvider cp = new ResourceInteractionContentProvider(true, true);
		cp.inputChanged(null, null, A);
		Object[] result = cp.getConnectedTo(A);
		assertEquals(1, result.length);
		assertEquals("B", ((State) result[0]).getName());
	}

}
