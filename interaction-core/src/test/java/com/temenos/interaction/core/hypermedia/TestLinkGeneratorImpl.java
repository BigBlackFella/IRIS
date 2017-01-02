package com.temenos.interaction.core.hypermedia;

/*
 * #%L
 * interaction-core
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.odata4j.core.OCollection;
import org.odata4j.core.OCollections;
import org.odata4j.core.OComplexObject;
import org.odata4j.core.OComplexObjects;
import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmEntitySet;

import com.temenos.interaction.core.MultivaluedMapImpl;
import com.temenos.interaction.core.command.InteractionContext;
import com.temenos.interaction.core.web.RequestContext;


/**
 * Created by ikarady on 06/04/2016.
 */
public class TestLinkGeneratorImpl {

    @Before
    public void setup() {
        // initialise the thread local request context with requestUri and baseUri
        RequestContext ctx = new RequestContext("/baseuri", "/requesturi", null);
        RequestContext.setRequestContext(ctx);
    }

    @Test
    public void testCreateLinkHrefSimple() {
        ResourceStateMachine engine = new ResourceStateMachine(mock(ResourceState.class));
        Transition t = new Transition.Builder().source(mock(ResourceState.class)).target(mockTarget("/test")).build();
        LinkGenerator linkGenerator = new LinkGeneratorImpl(engine, t, null);
        Collection<Link> links = linkGenerator.createLink(null, null, null);
        assertFalse(links.isEmpty());
        assertEquals("/baseuri/test", links.iterator().next().getHref());
    }

    @Test
    public void testCreateLinkHrefReplaceUsingEntity() {
        ResourceStateMachine engine = new ResourceStateMachine(mock(ResourceState.class), new BeanTransformer());
        Transition t = new Transition.Builder().source(mock(ResourceState.class)).target(mockTarget("/test/{noteId}")).build();
        Object entity = new TestNote("123");
        LinkGenerator linkGenerator = new LinkGeneratorImpl(engine, t, null);
        Collection<Link> links = linkGenerator.createLink(null, null, entity);
        assertFalse(links.isEmpty());
        assertEquals("/baseuri/test/123", links.iterator().next().getHref());
    }

    @Test
    public void testCreateLinkHrefUriParameterTokensReplaceUsingEntity() {
        ResourceStateMachine engine = new ResourceStateMachine(mock(ResourceState.class), new BeanTransformer());
        Map<String, String> uriParameters = new HashMap<String, String>();
        uriParameters.put("test", "{noteId}");
        Transition t = new Transition.Builder().source(mock(ResourceState.class)).target(mockTarget("/test")).uriParameters(uriParameters).build();

        Object entity = new TestNote("123");
        LinkGenerator linkGenerator = new LinkGeneratorImpl(engine, t, null);
        Collection<Link> links = linkGenerator.createLink(null, null, entity);
        assertFalse(links.isEmpty());
        assertEquals("/baseuri/test?test=123", links.iterator().next().getHref());
    }

    @Test
    public void testCreateLinkHrefUriParameterTokensReplaceQueryParameters() {
        ResourceStateMachine engine = new ResourceStateMachine(mock(ResourceState.class), new BeanTransformer());
        Map<String, String> uriParameters = new HashMap<String, String>();
        uriParameters.put("test", "{noteId}");
        Transition t = new Transition.Builder().source(mock(ResourceState.class)).target(mockTarget("/test")).uriParameters(uriParameters).build();
        MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl<String>();
        queryParameters.add("noteId", "123");
        LinkGenerator linkGenerator = new LinkGeneratorImpl(engine, t, null);
        Collection<Link> links = linkGenerator.createLink(null, queryParameters, null);
        assertFalse(links.isEmpty());
        assertEquals("/baseuri/test?test=123", links.iterator().next().getHref());
    }

    @Test
    public void testCreateLinkHrefUriParameterTokensReplaceQueryParametersSpecial() {
        ResourceStateMachine engine = new ResourceStateMachine(mock(ResourceState.class), new BeanTransformer());
        Map<String, String> uriParameters = new HashMap<String, String>();
        uriParameters.put("filter", "{$filter}");
        Transition t = new Transition.Builder().source(mock(ResourceState.class)).target(mockTarget("/test")).uriParameters(uriParameters).build();
        MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl<String>();
        queryParameters.add("$filter", "123");
        LinkGenerator linkGenerator = new LinkGeneratorImpl(engine, t, null);
        Collection<Link> links = linkGenerator.createLink(null, queryParameters, null);
        assertFalse(links.isEmpty());
        assertEquals("/baseuri/test?filter=123", links.iterator().next().getHref());
    }
    
    @Test
    public void testCreateLinkHrefUriParameterTransitionPropertiesReplaceQueryParameters() {
        ResourceStateMachine engine = new ResourceStateMachine(mock(ResourceState.class), new BeanTransformer());
        Map<String, String> uriParameters = new HashMap<String, String>();
        uriParameters.put("param1", "delta");
        Transition t = new Transition.Builder().source(mock(ResourceState.class)).target(mockTarget("/test")).uriParameters(uriParameters).build();
        InteractionContext ctx = mock(InteractionContext.class);
        MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl<String>();
        queryParameters.add("param1", "alpha");
        queryParameters.add("param2", "beta");
        queryParameters.add("param3", "gamma");
        when(ctx.getOutQueryParameters()).thenReturn(queryParameters);
        LinkGenerator linkGenerator = new LinkGeneratorImpl(engine, t, ctx);
        Collection<Link> links = linkGenerator.createLink(null, null, null);
        assertFalse(links.isEmpty());
        assertEquals("/baseuri/test?param1=delta&param2=beta&param3=gamma", links.iterator().next().getHref());
    }
    
    @Test
    public void testCreateLinkHrefUriParametersQueryParametersAndNoTransitionProperties() {
        ResourceStateMachine engine = new ResourceStateMachine(mock(ResourceState.class), new BeanTransformer());
        Map<String, String> uriParameters = new HashMap<String, String>();
        Transition t = new Transition.Builder().source(mock(ResourceState.class)).target(mockTarget("/test")).uriParameters(uriParameters).build();
        InteractionContext ctx = mock(InteractionContext.class);
        MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl<String>();
        queryParameters.add("param1", "alpha");
        queryParameters.add("param2", "beta");
        queryParameters.add("param3", "gamma");
        when(ctx.getOutQueryParameters()).thenReturn(queryParameters);
        LinkGenerator linkGenerator = new LinkGeneratorImpl(engine, t, ctx);
        Collection<Link> links = linkGenerator.createLink(null, null, null);
        assertFalse(links.isEmpty());
        assertEquals("/baseuri/test?param1=alpha&param2=beta&param3=gamma", links.iterator().next().getHref());
    }
    
    @Test
    public void testCreateLinkHrefUriParametersTransitionPropertiesAndNoQueryParameters() {
        ResourceStateMachine engine = new ResourceStateMachine(mock(ResourceState.class), new BeanTransformer());
        Map<String, String> uriParameters = new HashMap<String, String>();
        uriParameters.put("param1", "alpha");
        uriParameters.put("param2", "beta");
        uriParameters.put("param3", "gamma");
        Transition t = new Transition.Builder().source(mock(ResourceState.class)).target(mockTarget("/test")).uriParameters(uriParameters).build();
        InteractionContext ctx = mock(InteractionContext.class);
        MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl<String>();
        when(ctx.getOutQueryParameters()).thenReturn(queryParameters);
        LinkGenerator linkGenerator = new LinkGeneratorImpl(engine, t, ctx);
        Collection<Link> links = linkGenerator.createLink(null, null, null);
        assertFalse(links.isEmpty());
        assertEquals("/baseuri/test?param1=alpha&param2=beta&param3=gamma", links.iterator().next().getHref());
    }
    
    @Test
    public void testCreateLinkHrefUriParametersQueryParametersNonAsciiCharacters() {
        ResourceStateMachine engine = new ResourceStateMachine(mock(ResourceState.class), new BeanTransformer());
        Map<String, String> uriParameters = new HashMap<String, String>();
        Transition t = new Transition.Builder().source(mock(ResourceState.class)).target(mockTarget("/test")).uriParameters(uriParameters).build();
        InteractionContext ctx = mock(InteractionContext.class);
        MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl<String>();
        queryParameters.add("param1", "a@1");
        queryParameters.add("param2", "b@2");
        queryParameters.add("param3", "c@3");
        when(ctx.getOutQueryParameters()).thenReturn(queryParameters);
        LinkGenerator linkGenerator = new LinkGeneratorImpl(engine, t, ctx);
        Collection<Link> links = linkGenerator.createLink(null, null, null);
        assertFalse(links.isEmpty());
        assertEquals("/baseuri/test?param1=a%401&param2=b%402&param3=c%403", links.iterator().next().getHref());
    }
    
    @Test
    public void testCreateLinkHrefAllQueryParameters() {
        ResourceStateMachine engine = new ResourceStateMachine(mock(ResourceState.class), new BeanTransformer());
        Transition t = new Transition.Builder().source(mock(ResourceState.class)).target(mockTarget("/test")).build();
        MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl<String>();
        queryParameters.add("$filter", "123");
        LinkGenerator linkGenerator = new LinkGeneratorImpl(engine, t, null).setAllQueryParameters(true);
        Collection<Link> links = linkGenerator.createLink(new MultivaluedMapImpl<String>(), queryParameters, null);
        assertFalse(links.isEmpty());
        assertEquals("/baseuri/test?$filter=123", links.iterator().next().getHref());
    }

    @Test
    public void testCreateLinkFromDynamicResource() {
        ResourceStateMachine engineMock = Mockito.mock(ResourceStateMachine.class);
        ResourceStateAndParameters resourceStateAndParameters = new ResourceStateAndParameters();
        resourceStateAndParameters.setState(mockTarget("/testDynamic", "resolvedTargetEntityName", "resolvedTargetName", ""));
        resourceStateAndParameters.setParams(new ParameterAndValue[] { new ParameterAndValue("filter2", "564") });
        DynamicResourceState dynamicResourceStateMock = mockDynamicTarget(null);
        Map<String, String> uriParameters = new HashMap<String, String>();
        uriParameters.put("filter", "{nodeId}");
        Transition t = new Transition.Builder().source(mock(ResourceState.class)).target(dynamicResourceStateMock).uriParameters(uriParameters).build();
        MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl<String>();
        queryParameters.add("nodeId", "123");
        Map<String, Object> transitionProperties = new HashMap<String, Object>();
        transitionProperties.put("filter", "{nodeId}");
        transitionProperties.put("nodeId", "123");
        Mockito.when(engineMock.resolveDynamicState(dynamicResourceStateMock, transitionProperties, null)).thenReturn(resourceStateAndParameters);
        Mockito.when(engineMock.getTransitionProperties(t, null, new MultivaluedMapImpl<String>(), queryParameters)).thenReturn(transitionProperties);

        LinkGenerator linkGenerator = new LinkGeneratorImpl(engineMock, t, null).setAllQueryParameters(true);
        Collection<Link> links = linkGenerator.createLink(new MultivaluedMapImpl<String>(), queryParameters, null);
        assertFalse(links.isEmpty());
        Link result = links.iterator().next();        
        assertEquals("/baseuri/testDynamic?filter=123&filter2=564", result.getHref());
        assertEquals("resolvedTargetEntityName", result.getTransition().getTarget().getEntityName());
        assertEquals("resolvedTargetName", result.getTransition().getTarget().getName());
    }
    
    @Test
    public void testCreateLinkFromDynamicResourceEditVersion() {
    	ResourceStateMachine engineMock = Mockito.mock(ResourceStateMachine.class);
        ResourceStateAndParameters resourceStateAndParameters = new ResourceStateAndParameters();
        resourceStateAndParameters.setState(mockTarget("/testDynamic", "resolvedTargetEntityName", "resolvedTargetName", "http.../populate"));
        resourceStateAndParameters.setParams(new ParameterAndValue[] { new ParameterAndValue("filter2", "564"), new ParameterAndValue("id", "ABCD") });
        DynamicResourceState dynamicResourceStateMock = mockDynamicTarget(null);
        Map<String, String> uriParameters = new HashMap<String, String>();
        uriParameters.put("filter", "{nodeId}");
        Transition t = new Transition.Builder().source(mock(ResourceState.class)).target(dynamicResourceStateMock).uriParameters(uriParameters).build();
        MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl<String>();
        queryParameters.add("nodeId", "123");
        Map<String, Object> transitionProperties = new HashMap<String, Object>();
        transitionProperties.put("filter", "{nodeId}");
        transitionProperties.put("nodeId", "123");
        Mockito.when(engineMock.resolveDynamicState(dynamicResourceStateMock, transitionProperties, null)).thenReturn(resourceStateAndParameters);
        Mockito.when(engineMock.getTransitionProperties(t, null, new MultivaluedMapImpl<String>(), queryParameters)).thenReturn(transitionProperties);

        LinkGenerator linkGenerator = new LinkGeneratorImpl(engineMock, t, null).setAllQueryParameters(true);
        Collection<Link> links = linkGenerator.createLink(new MultivaluedMapImpl<String>(), queryParameters, null);
        assertFalse(links.isEmpty());
        Link result = links.iterator().next();        
        assertEquals("/baseuri/testDynamic?filter=123&filter2=564&id=ABCD", result.getHref());
        assertEquals("resolvedTargetEntityName", result.getTransition().getTarget().getEntityName());
        assertEquals("resolvedTargetName", result.getTransition().getTarget().getName());
        assertEquals("POST", result.getMethod());
    }

    @Test
    public void testCreateLinkForCollectionEntity() {
        Link result = null;
        Map<String, String> uriParameters = new HashMap<String, String>();
        uriParameters.put("test", "{Contact.Email}");
        CollectionResourceState customerState = new CollectionResourceState("customer", "customer", new ArrayList<Action>(), "/customer()", null, null);
        CollectionResourceState contactState = new CollectionResourceState("contact", "contact", new ArrayList<Action>(), "/contact()", null, null);
        customerState.addTransition(new Transition.Builder().method("GET").target(contactState).uriParameters(uriParameters).flags(Transition.FOR_EACH).sourceField("AField").build());
        OCollection<?> contactColl = OCollections.newBuilder(null).add(createComplexObject("Email", "johnEmailAddr", "Tel", "12345")).add(createComplexObject("Email", "smithEmailAddr", "Tel", "66778")).build();
        ResourceStateMachine engine = new ResourceStateMachine(customerState, getOEntityTransformer(contactColl));

        OProperty<?> contactProp = OProperties.collection("source_Contact", null, contactColl);
        List<OProperty<?>> contactPropList = new ArrayList<OProperty<?>>();
        contactPropList.add(contactProp);
        OEntity entity = OEntities.createRequest(EdmEntitySet.newBuilder().build(), contactPropList, null);
        Transition t = customerState.getTransitions().get(0);
        LinkGenerator linkGenerator = new LinkGeneratorImpl(engine, t, null);
        Collection<Link> links = linkGenerator.createLink(null, null, entity);
        Iterator<Link> iterator = links.iterator();

        assertEquals(2, links.size());

        result = iterator.next();
        assertEquals("/baseuri/contact()?test=johnEmailAddr", result.getHref());
        assertEquals("collection", result.getRel());

        result = iterator.next();
        assertEquals("/baseuri/contact()?test=smithEmailAddr", result.getHref());
        assertEquals("collection", result.getRel());
    }

    @Test
    public void testCreateLinkForCollectionEntityTwoLevel() {
        Link result = null;
        Map<String, String> uriParameters = new HashMap<String, String>();
        uriParameters.put("test", "{Contact.Address.PostCode}");
        CollectionResourceState customerState = new CollectionResourceState("customer", "customer", new ArrayList<Action>(), "/customer()", null, null);
        CollectionResourceState contactState = new CollectionResourceState("contact", "contact", new ArrayList<Action>(), "/contact()", null, null);
        customerState.addTransition(new Transition.Builder().method("GET").target(contactState).uriParameters(uriParameters).flags(Transition.FOR_EACH).sourceField("AField").build());

        // Inner collection
        OCollection<?> postCodeColl = OCollections.newBuilder(null).add(createComplexObject("PostCode", "ABCD")).add(createComplexObject("PostCode", "EFGH")).build();

        // Outer Collection
        OProperty<?> addressCollProperty = OProperties.collection("Address", null, postCodeColl);
        List<OProperty<?>> addressPropList = new ArrayList<OProperty<?>>();
        addressPropList.add(addressCollProperty);
        OComplexObject addressDetails = OComplexObjects.create(EdmComplexType.newBuilder().build(), addressPropList);
        OCollection<?> addressColl = OCollections.newBuilder(null).add(addressDetails).build();

        ResourceStateMachine engine = new ResourceStateMachine(customerState, getOEntityTransformer(addressColl));

        OProperty<?> contactProp = OProperties.collection("source_Contact", null, addressColl);
        List<OProperty<?>> contactPropList = new ArrayList<OProperty<?>>();
        contactPropList.add(contactProp);
        OEntity entity = OEntities.createRequest(EdmEntitySet.newBuilder().build(), contactPropList, null);
        Transition t = customerState.getTransitions().get(0);
        LinkGenerator linkGenerator = new LinkGeneratorImpl(engine, t, null);
        Collection<Link> links = linkGenerator.createLink(null, null, entity);
        Iterator<Link> iterator = links.iterator();

        assertEquals(2, links.size());

        result = iterator.next();
        assertEquals("/baseuri/contact()?test=ABCD", result.getHref());
        assertEquals("collection", result.getRel());

        result = iterator.next();
        assertEquals("/baseuri/contact()?test=EFGH", result.getHref());
        assertEquals("collection", result.getRel());
    }

    @Test
    public void testCreateLinkForCollectionEntityMultiParams() {
        Link result = null;
        Map<String, String> uriParameters = new HashMap<String, String>();
        uriParameters.put("test", "{personName} email {Contact.Email}");
        CollectionResourceState customerState = new CollectionResourceState("customer", "customer", new ArrayList<Action>(), "/customer()", null, null);
        CollectionResourceState contactState = new CollectionResourceState("contact", "contact", new ArrayList<Action>(), "/contact()", null, null);
        customerState.addTransition(new Transition.Builder().method("GET").target(contactState).uriParameters(uriParameters).flags(Transition.FOR_EACH).sourceField("AField").build());
        OCollection<?> contactColl = OCollections.newBuilder(null).add(createComplexObject("Email", "johnEmailAddr", "Tel", "12345")).add(createComplexObject("Email", "smithEmailAddr", "Tel", "66778")).build();
        ResourceStateMachine engine = new ResourceStateMachine(customerState, getOEntityTransformer(contactColl));

        OProperty<?> contactProp = OProperties.collection("source_Contact", null, contactColl);
        List<OProperty<?>> contactPropList = new ArrayList<OProperty<?>>();
        contactPropList.add(contactProp);
        OEntity entity = OEntities.createRequest(EdmEntitySet.newBuilder().build(), contactPropList, null);
        Transition t = customerState.getTransitions().get(0);
        LinkGenerator linkGenerator = new LinkGeneratorImpl(engine, t, null);
        MultivaluedMap<String, String> pathParameters = new MultivaluedMapImpl<String>();
        pathParameters.add("personName", "John");
        Collection<Link> links = linkGenerator.createLink(pathParameters, null, entity);
        Iterator<Link> iterator = links.iterator();

        assertEquals(2, links.size());

        result = iterator.next();
        assertEquals("/baseuri/contact()?test=John+email+johnEmailAddr", result.getHref());
        assertEquals("collection", result.getRel());

        result = iterator.next();
        assertEquals("/baseuri/contact()?test=John+email+smithEmailAddr", result.getHref());
        assertEquals("collection", result.getRel());
    }

    @Test
    public void testCreateLinkForCollectionEntityWithFieldContainingNumber() {
        Link result = null;
        Map<String, String> uriParameters = new HashMap<String, String>();
        uriParameters.put("test", "{Contact.Email8}");
        CollectionResourceState customerState = new CollectionResourceState("customer", "customer", new ArrayList<Action>(), "/customer()", null, null);
        CollectionResourceState contactState = new CollectionResourceState("contact", "contact", new ArrayList<Action>(), "/contact()", null, null);
        customerState.addTransition(new Transition.Builder().method("GET").target(contactState).uriParameters(uriParameters).flags(Transition.FOR_EACH).sourceField("AField").build());
        OCollection<?> contactColl = OCollections.newBuilder(null).add(createComplexObject("Email8", "johnEmailAddr", "Tel", "12345")).add(createComplexObject("Email8", "smithEmailAddr", "Tel", "66778")).build();
        ResourceStateMachine engine = new ResourceStateMachine(customerState, getOEntityTransformer(contactColl));

        OProperty<?> contactProp = OProperties.collection("source_Contact", null, contactColl);
        List<OProperty<?>> contactPropList = new ArrayList<OProperty<?>>();
        contactPropList.add(contactProp);
        OEntity entity = OEntities.createRequest(EdmEntitySet.newBuilder().build(), contactPropList, null);
        Transition t = customerState.getTransitions().get(0);
        LinkGenerator linkGenerator = new LinkGeneratorImpl(engine, t, null);
        Collection<Link> links = linkGenerator.createLink(null, null, entity);
        Iterator<Link> iterator = links.iterator();

        assertEquals(2, links.size());

        result = iterator.next();
        assertEquals("/baseuri/contact()?test=johnEmailAddr", result.getHref());
        assertEquals("collection", result.getRel());

        result = iterator.next();
        assertEquals("/baseuri/contact()?test=smithEmailAddr", result.getHref());
        assertEquals("collection", result.getRel());
    }

    @Test
    public void testTransitionFieldLabelInParams() {
        Link result = null;
        Map<String, String> uriParameters = new HashMap<String, String>();
        uriParameters.put("test", "{Contact.Email}");
        CollectionResourceState customerState = new CollectionResourceState("customer", "customer", new ArrayList<Action>(), "/customer()", null, null);
        CollectionResourceState contactState = new CollectionResourceState("contact", "contact", new ArrayList<Action>(), "/contact()", null, null);
        Transition transition = new Transition.Builder().method("GET").target(contactState).uriParameters(uriParameters).flags(Transition.FOR_EACH).sourceField("Contact.Email").build();
        customerState.addTransition(transition);
        OCollection<?> contactColl = OCollections.newBuilder(null).add(createComplexObject("Email", "johnEmailAddr", "Tel", "12345")).add(createComplexObject("Email", "smithEmailAddr", "Tel", "66778")).build();
        ResourceStateMachine engine = new ResourceStateMachine(customerState, getOEntityTransformer(contactColl));

        OProperty<?> contactProp = OProperties.collection("customer_Contact", null, contactColl);
        List<OProperty<?>> contactPropList = new ArrayList<OProperty<?>>();
        contactPropList.add(contactProp);
        OEntity entity = OEntities.createRequest(EdmEntitySet.newBuilder().build(), contactPropList, null);
        Transition t = customerState.getTransitions().get(0);
        LinkGenerator linkGenerator = new LinkGeneratorImpl(engine, t, null);
        Collection<Link> links = linkGenerator.createLink(null, null, entity);
        Iterator<Link> iterator = links.iterator();

        assertEquals(2, links.size());

        result = iterator.next();
        assertEquals("/baseuri/contact()?test=johnEmailAddr", result.getHref());
        assertEquals("collection", result.getRel());
        assertEquals("customer_Contact(0).Email", result.getSourceField());

        result = iterator.next();
        assertEquals("/baseuri/contact()?test=smithEmailAddr", result.getHref());
        assertEquals("collection", result.getRel());
        assertEquals("customer_Contact(1).Email", result.getSourceField());
    }

    @Test
    public void testTransitionFieldLabelParentInParams() {
        Link result = null;
        Map<String, String> uriParameters = new HashMap<String, String>();
        uriParameters.put("test", "{Contact.Email}");
        CollectionResourceState customerState = new CollectionResourceState("customer", "customer", new ArrayList<Action>(), "/customer()", null, null);
        CollectionResourceState contactState = new CollectionResourceState("contact", "contact", new ArrayList<Action>(), "/contact()", null, null);
        Transition transition = new Transition.Builder().method("GET").target(contactState).uriParameters(uriParameters).flags(Transition.FOR_EACH).sourceField("Contact.Tel").build();
        customerState.addTransition(transition);
        OCollection<?> contactColl = OCollections.newBuilder(null).add(createComplexObject("Email", "johnEmailAddr", "Tel", "12345")).add(createComplexObject("Email", "smithEmailAddr", "Tel", "66778")).build();
        ResourceStateMachine engine = new ResourceStateMachine(customerState, getOEntityTransformer(contactColl));

        OProperty<?> contactProp = OProperties.collection("customer_Contact", null, contactColl);
        List<OProperty<?>> contactPropList = new ArrayList<OProperty<?>>();
        contactPropList.add(contactProp);
        OEntity entity = OEntities.createRequest(EdmEntitySet.newBuilder().build(), contactPropList, null);
        Transition t = customerState.getTransitions().get(0);
        LinkGenerator linkGenerator = new LinkGeneratorImpl(engine, t, null);
        Collection<Link> links = linkGenerator.createLink(null, null, entity);
        Iterator<Link> iterator = links.iterator();

        assertEquals(2, links.size());

        result = iterator.next();
        assertEquals("/baseuri/contact()?test=johnEmailAddr", result.getHref());
        assertEquals("collection", result.getRel());
        assertEquals("customer_Contact(0).Tel", result.getSourceField());

        result = iterator.next();
        assertEquals("/baseuri/contact()?test=smithEmailAddr", result.getHref());
        assertEquals("collection", result.getRel());
        assertEquals("customer_Contact(1).Tel", result.getSourceField());
    }

    @Test
    public void testTransitionFieldLabelParentInParamsMissingOneFieldLabel() {
        Link result = null;
        Map<String, String> uriParameters = new HashMap<String, String>();
        uriParameters.put("test", "{Contact.Email}");
        CollectionResourceState customerState = new CollectionResourceState("customer", "customer", new ArrayList<Action>(), "/customer()", null, null);
        CollectionResourceState contactState = new CollectionResourceState("contact", "contact", new ArrayList<Action>(), "/contact()", null, null);
        Transition transition = new Transition.Builder().method("GET").target(contactState).uriParameters(uriParameters).flags(Transition.FOR_EACH).sourceField("Contact.Tel").build();
        customerState.addTransition(transition);
        OCollection<?> contactColl = OCollections.newBuilder(null).add(createComplexObject("Email", "johnEmailAddr", "Tel", "12345")).add(createComplexObject("Email", "smithEmailAddr")).build();
        ResourceStateMachine engine = new ResourceStateMachine(customerState, getOEntityTransformer(contactColl));

        OProperty<?> contactProp = OProperties.collection("customer_Contact", null, contactColl);
        List<OProperty<?>> contactPropList = new ArrayList<OProperty<?>>();
        contactPropList.add(contactProp);
        OEntity entity = OEntities.createRequest(EdmEntitySet.newBuilder().build(), contactPropList, null);
        Transition t = customerState.getTransitions().get(0);
        LinkGenerator linkGenerator = new LinkGeneratorImpl(engine, t, null);
        Collection<Link> links = linkGenerator.createLink(null, null, entity);
        Iterator<Link> iterator = links.iterator();

        assertEquals(1, links.size());

        result = iterator.next();
        assertEquals("/baseuri/contact()?test=johnEmailAddr", result.getHref());
        assertEquals("collection", result.getRel());
        assertEquals("customer_Contact(0).Tel", result.getSourceField());

    }

    @Test
    public void testTransitionFieldLabelNotInParams() {
        Map<String, String> uriParameters = new HashMap<String, String>();
        uriParameters.put("test", "{Contact.Email}");
        CollectionResourceState customerState = new CollectionResourceState("customer", "customer", new ArrayList<Action>(), "/customer()", null, null);
        CollectionResourceState contactState = new CollectionResourceState("contact", "contact", new ArrayList<Action>(), "/contact()", null, null);
        Transition transition = new Transition.Builder().method("GET").target(contactState).uriParameters(uriParameters).flags(Transition.FOR_EACH).sourceField("Customer.Name").build();
        customerState.addTransition(transition);
        OCollection<?> contactColl = OCollections.newBuilder(null).add(createComplexObject("Email", "johnEmailAddr", "Tel", "12345")).add(createComplexObject("Email", "smithEmailAddr", "Tel", "66778")).add(createComplexObject("Email", "peterEmailAddr", "Tel", "24680")).build();

        OCollection<?> customerColl = OCollections.newBuilder(null).add(createComplexObject("Name", "Paul")).add(createComplexObject("Name", "Andrew")).add(createComplexObject("Name", "Jon")).build();

        Map<String, OCollection<?>> collectionMap = new HashMap<String, OCollection<?>>();
        collectionMap.put("source_Contact", contactColl);
        collectionMap.put("source_Customer", customerColl);

        ResourceStateMachine engine = new ResourceStateMachine(customerState, getOEntityTransformer(collectionMap));

        OProperty<?> contactProp = OProperties.collection("customer_Contact", null, contactColl);
        OProperty<?> customerProp = OProperties.collection("customer_Customer", null, contactColl);
        List<OProperty<?>> entityPropList = new ArrayList<OProperty<?>>();
        entityPropList.add(contactProp);
        entityPropList.add(customerProp);
        OEntity entity = OEntities.createRequest(EdmEntitySet.newBuilder().build(), entityPropList, null);
        Transition t = customerState.getTransitions().get(0);
        LinkGenerator linkGenerator = new LinkGeneratorImpl(engine, t, null);
        Collection<Link> links = linkGenerator.createLink(null, null, entity);

        assertEquals(3, links.size());

        ArrayList<Link> genertedLinks = (ArrayList<Link>) links;
        assertEquals("/baseuri/contact()?test=johnEmailAddr", genertedLinks.get(0).getHref());
        assertEquals("collection", genertedLinks.get(0).getRel());
        assertEquals("customer_Customer(0).Name", genertedLinks.get(0).getSourceField());
        
        assertEquals("/baseuri/contact()?test=smithEmailAddr", genertedLinks.get(1).getHref());
        assertEquals("collection", genertedLinks.get(1).getRel());
        assertEquals("customer_Customer(1).Name", genertedLinks.get(1).getSourceField());
        
        assertEquals("/baseuri/contact()?test=peterEmailAddr", genertedLinks.get(2).getHref());
        assertEquals("collection", genertedLinks.get(2).getRel());
        assertEquals("customer_Customer(2).Name", genertedLinks.get(2).getSourceField());

    }

    private OComplexObject createComplexObject(String... values) {
        List<OProperty<?>> propertyList = new ArrayList<OProperty<?>>();
        for (int i = 0; i < values.length; i += 2) {
            OProperty<String> property = OProperties.string(values[i], values[i + 1]);
            propertyList.add(property);
        }
        OComplexObject complexObj = OComplexObjects.create(EdmComplexType.newBuilder().build(), propertyList);
        return complexObj;
    }

    private Transformer getOEntityTransformer(OCollection<?> collection) {
        Map<String, Object> entityProperties = new HashMap<String, Object>();
        entityProperties.put("source_Contact", collection);
        Transformer transformerMock = mock(Transformer.class);
        when(transformerMock.transform(anyObject())).thenReturn(entityProperties);
        return transformerMock;
    }

    private Transformer getOEntityTransformer(Map<String, OCollection<?>> collectionMap) {
        Map<String, Object> entityProperties = new HashMap<String, Object>();
        for (Map.Entry<String, OCollection<?>> entry : collectionMap.entrySet()) {
            entityProperties.put(entry.getKey(), entry.getValue());
        }
        Transformer transformerMock = mock(Transformer.class);
        when(transformerMock.transform(anyObject())).thenReturn(entityProperties);
        return transformerMock;
    }

    private ResourceState mockTarget(String path) {
        ResourceState target = mock(ResourceState.class);
        when(target.getPath()).thenReturn(path);
        when(target.getRel()).thenReturn("collection");
        return target;
    }

    private DynamicResourceState mockDynamicTarget(String path) {
        DynamicResourceState target = mock(DynamicResourceState.class);
        when(target.getPath()).thenReturn(path);
        return target;
    }

    public static class TestNote {

        String noteId;

        public TestNote(String noteId) {
            this.noteId = noteId;
        }

        public String getNoteId() {
            return noteId;
        }
    }

    private ResourceState mockTarget(String path, String entityName, String name, String rel) {
        ResourceState target = mockTarget(path);
        when(target.getEntityName()).thenReturn(entityName);
        when(target.getName()).thenReturn(name);
        when(target.getRel()).thenReturn(rel);
        return target;
    }

    private void sortLinkCollection(List<Link> links) {
        Collections.sort(links, new Comparator<Link>() {

            @Override
            public int compare(Link link1, Link link2) {
                int val = link1.getHref().compareTo(link2.getHref());
                return val == 0 ? link1.getSourceField().compareTo(link2.getSourceField()) : val;
            }
        });
    }
}
