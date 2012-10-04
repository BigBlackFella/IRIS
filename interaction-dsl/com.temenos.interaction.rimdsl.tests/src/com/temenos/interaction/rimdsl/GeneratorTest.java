package com.temenos.interaction.rimdsl;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.xtext.generator.AbstractFileSystemAccess;
import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.IGenerator;
import org.eclipse.xtext.generator.InMemoryFileSystemAccess;
import org.eclipse.xtext.junit4.InjectWith;
import org.eclipse.xtext.junit4.XtextRunner;
import org.eclipse.xtext.junit4.util.ParseHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;
import com.temenos.interaction.rimdsl.rim.ResourceInteractionModel;

@InjectWith(RIMDslInjectorProvider.class)
@RunWith(XtextRunner.class)
public class GeneratorTest {
	
	@Inject 
	IGenerator underTest;
	@Inject
	ParseHelper<ResourceInteractionModel> parseHelper;
	
	private final static String LINE_SEP = System.getProperty("line.separator");
	
	private final static String SIMPLE_STATES_RIM = "" +
	"commands" + LINE_SEP +
	"	GetEntity properties" + LINE_SEP +
	"	UpdateEntity properties" + LINE_SEP +
	"end" + LINE_SEP +
			
	"initial state A" + LINE_SEP +
	"	collection ENTITY" + LINE_SEP +
	"	actions { GetEntity }" + LINE_SEP +
	"end" + LINE_SEP +

	"state B" +
	"	item ENTITY" + LINE_SEP +
// TODO - new specification for View commands
//	"	view { GetEntity }" + LINE_SEP +
	"	actions { GetEntity, UpdateEntity }" + LINE_SEP +
	"end" + LINE_SEP +
	"";

	private final static String SIMPLE_STATES_BEHAVIOUR = "" +
	"import java.util.HashSet;" + LINE_SEP +
	"import java.util.Set;" + LINE_SEP +
	LINE_SEP +
	"import com.temenos.interaction.core.hypermedia.Action;" + LINE_SEP +
	"import com.temenos.interaction.core.hypermedia.CollectionResourceState;" + LINE_SEP +
	"import com.temenos.interaction.core.hypermedia.ResourceState;" + LINE_SEP +
	"import com.temenos.interaction.core.hypermedia.ResourceStateMachine;" + LINE_SEP +
	"import com.temenos.interaction.core.hypermedia.validation.HypermediaValidator;" + LINE_SEP +
	LINE_SEP +
	"public class __synthetic0Behaviour {" + LINE_SEP +
	LINE_SEP +
	"    public static void main(String[] args) {" + LINE_SEP +
	"        ResourceStateMachine hypermediaEngine = new __synthetic0Behaviour().getRIM();" + LINE_SEP +
	"        HypermediaValidator validator = HypermediaValidator.createValidator(hypermediaEngine);" + LINE_SEP +
	"        System.out.println(validator.graph());" + LINE_SEP +
	"    }" + LINE_SEP +
	LINE_SEP +
	"	public ResourceStateMachine getRIM() {" + LINE_SEP +
	"		ResourceState initial = null;" + LINE_SEP +
	"		// create states" + LINE_SEP +
	"		CollectionResourceState sA = new CollectionResourceState(\"ENTITY\", \"A\", createActionSet(new Action(\"GetEntity\", Action.TYPE.VIEW), null), \"\");" + LINE_SEP +
	"		// identify the initial state" + LINE_SEP +
	"		initial = sA;" + LINE_SEP +
	"		ResourceState sB = new ResourceState(\"ENTITY\", \"B\", createActionSet(new Action(\"GetEntity\", Action.TYPE.VIEW), new Action(\"UpdateEntity\", Action.TYPE.ENTRY)), \"\");" + LINE_SEP +
	LINE_SEP +
	"		// create regular transitions" + LINE_SEP +
	LINE_SEP +
    "        // create foreach transitions" + LINE_SEP +
	LINE_SEP +
    "        // create AUTO transitions" + LINE_SEP +
	LINE_SEP +
	"	    return new ResourceStateMachine(initial);" + LINE_SEP +
	"	}" + LINE_SEP +
	LINE_SEP +
	"    private Set<Action> createActionSet(Action view, Action entry) {" + LINE_SEP +
	"        Set<Action> actions = new HashSet<Action>();" + LINE_SEP +
	"        if (view != null)" + LINE_SEP +
	"            actions.add(view);" + LINE_SEP +
	"        if (entry != null)" + LINE_SEP +
	"            actions.add(entry);" + LINE_SEP +
	"        return actions;" + LINE_SEP +
	"    }" + LINE_SEP +
	LINE_SEP +
	"}" + LINE_SEP;
	
	@Test
	public void testGenerateSimpleStates() throws Exception {
		ResourceInteractionModel model = parseHelper.parse(SIMPLE_STATES_RIM);
		InMemoryFileSystemAccess fsa = new InMemoryFileSystemAccess();
		underTest.doGenerate(model.eResource(), fsa);
		System.out.println(fsa.getFiles());
		assertEquals(1, fsa.getFiles().size());
		
		String expectedKey = IFileSystemAccess.DEFAULT_OUTPUT + "__synthetic0Behaviour.java";
		assertTrue(fsa.getFiles().containsKey(expectedKey));
		assertEquals(SIMPLE_STATES_BEHAVIOUR, fsa.getFiles().get(expectedKey).toString());
		
	}

	
	private final static String SINGLE_STATE_VIEW_COMMAND_ONLY_RIM = "" +
	"commands" + LINE_SEP +
	"	GetEntity properties" + LINE_SEP +
	"end" + LINE_SEP +
			
	"initial state A" + LINE_SEP +
	"	collection ENTITY" + LINE_SEP +
	"	actions { GetEntity }" + LINE_SEP +
	"end" + LINE_SEP +
	"";

	/*
	 * doGenerate should producer one file
	 */
	@Test
	public void testGenerateOneFile() throws Exception {
		ResourceInteractionModel model = parseHelper.parse(SINGLE_STATE_VIEW_COMMAND_ONLY_RIM);
		InMemoryFileSystemAccess fsa = new InMemoryFileSystemAccess();
		underTest.doGenerate(model.eResource(), fsa);
		System.out.println(fsa.getFiles());
		assertEquals(1, fsa.getFiles().size());
	}

	@Test
	public void testGenerateSingleStateViewCommandOnly() throws Exception {
		ResourceInteractionModel model = parseHelper.parse(SINGLE_STATE_VIEW_COMMAND_ONLY_RIM);
		InMemoryFileSystemAccess fsa = new InMemoryFileSystemAccess();
		underTest.doGenerate(model.eResource(), fsa);
		
		String expectedKey = IFileSystemAccess.DEFAULT_OUTPUT + "__synthetic0Behaviour.java";
		assertTrue(fsa.getFiles().containsKey(expectedKey));
		assertTrue(fsa.getFiles().get(expectedKey).toString().contains("createActionSet(new Action(\"GetEntity\", Action.TYPE.VIEW), null"));
	}

}
