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


import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.odata4j.core.OEntity;
import org.odata4j.format.xml.XmlFormatWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.temenos.interaction.core.command.InteractionContext;
import com.temenos.interaction.core.web.RequestContext;


/**
 * A {@link LinkGenerator} that generates a {@link Collection} of {@link Link}
 * for a {@link Transition} using data from a resource entity.
 *
 */
public class LinkGeneratorImpl implements LinkGenerator {

    protected static final String PARAM_REPLACEMENT_REGEX = "\\((\\d+)\\)";
    private static final Logger logger = LoggerFactory.getLogger(LinkGeneratorImpl.class);
    private ResourceStateMachine resourceStateMachine;
    private Transition transition;
    private InteractionContext interactionContext;
    private String collectionName;
    private boolean allQueryParameters;
    private LinkToFieldAssociation linkToFieldAssociation;

    public LinkGeneratorImpl(ResourceStateMachine resourceStateMachine,
            Transition transition, InteractionContext interactionContext) {
        this.resourceStateMachine = resourceStateMachine;
        this.transition = transition;
        this.interactionContext = interactionContext;
        this.collectionName = extractCollectionParamName(transition.getCommand().getUriParameters());
        this.linkToFieldAssociation = new LinkToFieldAssociationImpl(transition.getSourceField(), collectionName);
    }

    public LinkGeneratorImpl setAllQueryParameters(boolean allQueryParameters) {
        this.allQueryParameters = allQueryParameters;
        return this;
    }

    @Override
    public Collection<Link> createLink(MultivaluedMap<String, String> pathParameters,
            MultivaluedMap<String, String> queryParameters, Object entity) {
        Collection<Link> eLinks = new ArrayList<Link>();
        Map<String, Object> transitionProperties = resourceStateMachine.getTransitionProperties(
                transition,
                entity,
                pathParameters,
                queryParameters
        );
        
        if(linkToFieldAssociation.generateOneLinkPerField()) {
            eLinks.addAll(createMultiLink(transitionProperties, queryParameters, entity, null));
        }
        else {
            List<String> targetFieldLabelList = linkToFieldAssociation.getFullyQualifiedFieldNames(HypermediaTemplateHelper.normalizeProperties(transitionProperties));
            for(String targetFieldLabel : targetFieldLabelList) {
                if (collectionName != null) {
                    eLinks.addAll(createMultiLink(transitionProperties, queryParameters, entity, targetFieldLabel));
                } else {
                    eLinks.add(createLink(transitionProperties, queryParameters, entity, targetFieldLabel));
                }
            }
        }
        
        return eLinks;
    }
    
    private String extractCollectionParamName(Map<String, String> transitionUriMap) {
        if (transitionUriMap==null) {
            return null;
        }
        String collectionParamName = null;
        for (Map.Entry<String, String> entry : transitionUriMap.entrySet()) {
            String entryValue = entry.getValue();
            collectionParamName = extractCollectionParamName(entryValue);
            // Assuming we have only one collection param per URI parameter list
            if (collectionParamName != null) {
                break;
            }
        }
        return collectionParamName;
    }

    private String extractCollectionParamName(String transitionUriValue) {
        if (transitionUriValue.contains("{") && transitionUriValue.contains("}")) {
            // URI contains collection if it contains a value within curly braces having a dot
            int indexOfLeftBrace = transitionUriValue.indexOf("{");
            int indexOfRightBrace = transitionUriValue.indexOf("}");
            String parameter = transitionUriValue.substring(indexOfLeftBrace+1, indexOfRightBrace);
            if (parameter.contains(".")) {
                return parameter;
            } else if (indexOfRightBrace == transitionUriValue.length()-1) {//End of string
                return null;
            }
            return extractCollectionParamName(transitionUriValue.substring(indexOfRightBrace+1));
        }
        return null;
    }

    private Collection<Link> createMultiLink(Map<String, Object> transitionProperties,
            MultivaluedMap<String, String> queryParameters, Object entity, String targetFieldLabel) {
        Collection<Link> eLinks = new ArrayList<Link>();
        Map<String, Object> normalizedProperties = HypermediaTemplateHelper.normalizeProperties(transitionProperties);

        Iterator<Map.Entry<String,Object>> entryItr = normalizedProperties.entrySet().iterator();
        while (entryItr.hasNext()) {
            Map.Entry<String,Object> entry = entryItr.next();
            String entryKey = entry.getKey();
            if (collectionName.equals(entryKey.replaceAll(LinkGeneratorImpl.PARAM_REPLACEMENT_REGEX, ""))) {
                Map<String, Object> transitionPropsCopy = createUriPropertyMap(transitionProperties, entry);
                String resolvedFieldLabel = linkToFieldAssociation.determineTargetFieldName(targetFieldLabel, entryKey, normalizedProperties);
                Link link = createLink(transitionPropsCopy, queryParameters, entity, resolvedFieldLabel);
                if (link != null) {
                    eLinks.add(link);
                }
            }
        }
        return eLinks;
    }
    
    
    private Link createLink(Map<String, Object> transitionProperties,
            MultivaluedMap<String, String> queryParameters, Object entity, String fieldLabel) {
        assert (RequestContext.getRequestContext() != null);
        ResourceStateProvider resourceStateProvider = resourceStateMachine.getResourceStateProvider();
        try {
            ResourceState targetState = transition.getTarget();

            if (targetState instanceof LazyResourceState || targetState instanceof LazyCollectionResourceState) {
                targetState = resourceStateProvider.getResourceState(targetState.getName());
            }

            if (targetState != null) {
                for (Transition tmpTransition : targetState.getTransitions()) {
                    if (tmpTransition.isType(Transition.EMBEDDED)) {
                        if (tmpTransition.getTarget() instanceof LazyResourceState
                                || tmpTransition.getTarget() instanceof LazyCollectionResourceState) {
                            if (tmpTransition.getTarget() != null) {
                                ResourceState tt = resourceStateProvider.getResourceState(tmpTransition.getTarget()
                                        .getName());
                                if (tt == null) {
                                    logger.error("Invalid transition [" + tmpTransition.getId() + "]");
                                }
                                tmpTransition.setTarget(tt);
                            }
                        }
                    }
                }

                // Target can have errorState which is not a normal transition,
                // so resolve and add it here
                if (targetState.getErrorState() != null) {
                    ResourceState errorState = targetState.getErrorState();
                    if ((errorState instanceof LazyResourceState || errorState instanceof LazyCollectionResourceState)
                            && errorState.getId().startsWith(".")) {
                        // We should resolve and overwrite the one already there
                        errorState = resourceStateProvider.getResourceState(errorState.getName());
                        targetState.setErrorState(errorState);
                    }
                }
            }

            if (targetState == null) {
                // a dead link, target could not be found
                logger.error("Dead link to [" + transition.getId() + "]");

                return null;
            }

            UriBuilder linkTemplate = UriBuilder.fromUri(RequestContext.getRequestContext().getBasePath());

            // Add any query parameters set by the command to the response
            if (interactionContext != null) {
                Map<String, String> outQueryParams = interactionContext.getOutQueryParameters();

                for (Map.Entry<String, String> param : outQueryParams.entrySet()) {
                    linkTemplate.queryParam(param.getKey(), param.getValue());
                }
            }

            if (targetState instanceof DynamicResourceState) {
                return createLinkForDynamicResource(linkTemplate, transitionProperties, targetState, entity, fieldLabel);
                        } else {
                return createLinkForResource(linkTemplate, transitionProperties, targetState, queryParameters, entity, fieldLabel);
                    }
        } catch (IllegalArgumentException e) {
            logger.warn("Dead link [" + transition + "]", e);

            return null;

        } catch (UriBuilderException e) {
            logger.error("Dead link [" + transition + "]", e);
            throw e;
        }
    }

    private String configureLink(UriBuilder linkTemplate, Transition transition,
            Map<String, Object> transitionProperties, ResourceState targetState) {
        String targetResourcePath = targetState.getPath();
        linkTemplate.path(targetResourcePath);

        String rel = targetState.getRel();

        if (transition.getSource() == targetState) {
            rel = "self";
        }

        // Pass uri parameters as query parameters if they are not
        // replaceable in the path, and replace any token.

        Map<String, String> uriParameters = transition.getCommand().getUriParameters();
        if (uriParameters != null) {
            for (String key : uriParameters.keySet()) {
                String value = uriParameters.get(key);
                if (!targetResourcePath.contains("{" + key + "}")) {
                    linkTemplate.queryParam(key, HypermediaTemplateHelper.templateReplace(value, transitionProperties));
                }
            }
        }

        return rel;
    }

    private String createLinkForState( ResourceState targetState){
        StringBuilder rel = new StringBuilder(XmlFormatWriter.related);
        rel.append(targetState.getName());

        return  rel.toString();
    }

    private String createLinkForProfile (Transition transition) {

        return transition.getLabel() != null
                && !transition.getLabel().equals("") ? transition.getLabel()
                : transition.getTarget().getName();
    }

    private void addQueryParams(MultivaluedMap<String, String> queryParameters, boolean allQueryParameters,
            UriBuilder linkTemplate, String targetResourcePath, Map<String, String> uriParameters) {
        if (queryParameters != null && allQueryParameters) {
            for (String param : queryParameters.keySet()) {
                if (!targetResourcePath.contains("{" + param + "}")
                        && (uriParameters == null || !uriParameters.containsKey(param))) {
                    linkTemplate.queryParam(param, queryParameters.getFirst(param));
                }
            }
        }
    }
    
    /**
     * Performs the following actions
     * <p>1. A.B.C=value is added to the map, i.e. the value for the uri param in the transition uri map.
     * <p>2. Id={A.B.C} is replaced with Id=value in the map.
     * @param transitionProperties
     * @param entry
     * @return
     */
    private Map<String, Object> createUriPropertyMap(Map<String, Object> transitionProperties, Map.Entry<String,Object> entry) {
        Map<String, Object> transitionPropsCopy = new HashMap<String, Object>();
        transitionPropsCopy.putAll(transitionProperties);
        transitionPropsCopy.put(collectionName, entry.getValue()); //Add A.B.C=val
        
        for (String key : transitionPropsCopy.keySet()) {
            Object value = transitionPropsCopy.get(key);
            if (value instanceof String && ((String)value).contains(collectionName)) {
                String encodedValue = encodeUriValue(entry.getValue());
                if (encodedValue != null) {
                    String output = ((String) value).replaceAll("\\{" + Pattern.quote(collectionName) + "\\}", encodedValue);
                    transitionPropsCopy.put(key, output); //Replace Id={A.B.C} with Id=val
                }
            }
        }
        return transitionPropsCopy;
    }
    
    private String encodeUriValue(Object value) {
        try {
            return URLEncoder.encode(value.toString(), "UTF-8");
        } catch (UnsupportedEncodingException exp) {//Should never happen as UTF-8 is supported
            logger.error("UTF-8 not supported when encoding " + value, exp);
            return null;
        }
    }

    private Link createLinkForDynamicResource(UriBuilder linkTemplate, Map<String, Object> transitionProperties, ResourceState targetState, Object entity, String fieldLabel) {
        // We are dealing with a dynamic target
        // Identify real target state
        ResourceStateAndParameters stateAndParams = resourceStateMachine.resolveDynamicState((DynamicResourceState) targetState,
                transitionProperties, interactionContext);

        if (stateAndParams.getState() == null) {
            // Bail out as we failed to resolve resource
            return null;
        } else {
            targetState = stateAndParams.getState();
        }
        
        String method = transition.getCommand().getMethod();
        if (targetState.getRel().contains("http://temenostech.temenos.com/rels/new")) {
            method = "POST";
        }

        String rel = configureLink(linkTemplate, transition, transitionProperties, targetState);

        if ("item".equals(rel) || "collection".equals(rel)) {
            rel = createLinkForState(targetState);
        }
        if (stateAndParams.getParams() != null) {
            // Add query parameters
            for (ParameterAndValue paramAndValue : stateAndParams.getParams()) {
                String param = paramAndValue.getParameter();
                String value = paramAndValue.getValue();

                if ("id".equalsIgnoreCase(param)) {
                    transitionProperties.put(param, value);
                } else {
                    linkTemplate.queryParam(param, value);
                }
            }
        }
        // Links in the transition properties are already encoded so
        // build the href using encoded map.
        URI href = linkTemplate.buildFromEncodedMap(transitionProperties);
        
        Transition linkTransition = rebuildTransitionWithResolvedTarget(targetState);        
        return buildLink(linkTransition, transitionProperties, entity, rel, href, method, fieldLabel);
    }
    
    private Link createLinkForResource(UriBuilder linkTemplate, Map<String, Object> transitionProperties, ResourceState targetState, MultivaluedMap<String, String> queryParameters, Object entity, String fieldLabel) {
        // We are NOT dealing with a dynamic target
        String rel = configureLink(linkTemplate, transition, transitionProperties, targetState);

        // Pass any query parameters
        addQueryParams(queryParameters, allQueryParameters, linkTemplate, targetState.getPath(), transition
                .getCommand().getUriParameters());

        // Build href from template
        URI href;
        if (entity != null && resourceStateMachine.getTransformer() == null) {
            logger.debug("Building link with entity (No Transformer) [" + entity + "] [" + transition + "]");
            href = linkTemplate.build(entity);
        } else {
            // Links in the transition properties are already encoded so
            // build the href using encoded map.
            href = linkTemplate.buildFromEncodedMap(transitionProperties);
        }
        
        return buildLink(transition, transitionProperties, entity, rel, href, transition.getCommand().getMethod(), fieldLabel);
    }
    
    private Link buildLink(Transition linkTransition, Map<String, Object> transitionProperties, Object entity, String rel, URI href, String method, String fieldLabel) {
        Link link;        
        if (transitionProperties.containsKey("profileOEntity") && "self".equals(rel) && entity instanceof OEntity) {
            //Create link adding profile to href to be resolved later on AtomXMLProvider
            link = new Link(linkTransition, rel, href.toASCIIString()+"#@"+createLinkForProfile(linkTransition), method);
        } else {
            //Create link as normal behaviour
            String linkFieldLabel = fieldLabel;
            if(fieldLabel!=null && fieldLabel.contains("."))
            {
                linkFieldLabel = transition.getSource().getEntityName() + "_" +fieldLabel;
            }
            link = new Link(linkTransition, rel, href.toASCIIString(), method, linkFieldLabel);
        }
        
        logger.debug("Created link for transition [" + linkTransition + "] [title=" + linkTransition.getId() + ", rel="
                + rel + ", method=" + method + ", href=" + href.toString() + "(ASCII=" + href.toASCIIString()
                + ")]");
        
        return link;
    }
    

    private Transition rebuildTransitionWithResolvedTarget(ResourceState resolvedTarget) {
        Transition updatedtransition = new Transition.Builder()
                                .source(this.transition.getSource())
                                .target(resolvedTarget)
                                .label(this.transition.getLabel())
                                .method(this.transition.getCommand().getMethod())
                                .flags(this.transition.getCommand().getFlags())
                                .evaluation(this.transition.getCommand().getEvaluation())
                                .locator(this.transition.getLocator())
                                .uriParameters(this.transition.getCommand().getUriParameters())
                                .linkId(this.transition.getLinkId())
                                .sourceField(this.transition.getSourceField())
                                .build(); 
        
        return updatedtransition;
    }
    
}
