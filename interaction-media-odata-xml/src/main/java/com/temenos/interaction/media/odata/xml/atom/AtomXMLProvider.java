package com.temenos.interaction.media.odata.xml.atom;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OLink;
import org.odata4j.core.OLinks;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.exceptions.ODataProducerException;
import org.odata4j.format.Entry;
import org.odata4j.format.xml.AtomEntryFormatParserExt;
import org.odata4j.format.xml.XmlFormatWriter;
import org.odata4j.internal.InternalUtil;
import org.odata4j.producer.Responses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.temenos.interaction.core.entity.Entity;
import com.temenos.interaction.core.entity.EntityMetadata;
import com.temenos.interaction.core.entity.EntityProperties;
import com.temenos.interaction.core.entity.EntityProperty;
import com.temenos.interaction.core.entity.Metadata;
import com.temenos.interaction.core.hypermedia.BeanTransformer;
import com.temenos.interaction.core.hypermedia.CollectionResourceState;
import com.temenos.interaction.core.hypermedia.Link;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.hypermedia.ResourceStateMachine;
import com.temenos.interaction.core.hypermedia.Transformer;
import com.temenos.interaction.core.hypermedia.Transition;
import com.temenos.interaction.core.resource.CollectionResource;
import com.temenos.interaction.core.resource.EntityResource;
import com.temenos.interaction.core.resource.RESTResource;
import com.temenos.interaction.core.resource.ResourceTypeHelper;
import com.temenos.interaction.core.web.RequestContext;

@Provider
@Consumes({MediaType.APPLICATION_ATOM_XML})
@Produces({MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_XML})
public class AtomXMLProvider implements MessageBodyReader<RESTResource>, MessageBodyWriter<RESTResource> {
	private final Logger logger = LoggerFactory.getLogger(AtomXMLProvider.class);
	private final static Pattern RESOURCE_PATTERN = Pattern.compile("(.*)/(.+)");
	
	@Context
	private UriInfo uriInfo;
	private AtomEntryFormatWriter entryWriter = new AtomEntryFormatWriter();
	private AtomFeedFormatWriter feedWriter = new AtomFeedFormatWriter();
	
	private final EdmDataServices edmDataServices;
	private final Metadata metadata;
	private final ResourceStateMachine hypermediaEngine;
//	private final Transformer transformer;

	/**
	 * Construct the jax-rs Provider for OData media type.
	 * @param edmDataServices
	 * 		The entity metadata for reading and writing OData entities.
	 * @param metadata
	 * 		The entity metadata for reading and writing Entity entities.
	 * @param hypermediaEngine
	 * 		The hypermedia engine contains all the resource to entity mappings
	 * @param transformer
	 * 		Transformer to convert an entity to a properties map
	 */
	public AtomXMLProvider(EdmDataServices edmDataServices, Metadata metadata, ResourceStateMachine hypermediaEngine, Transformer transformer) {
		this.edmDataServices = edmDataServices;
		this.metadata = metadata;
		this.hypermediaEngine = hypermediaEngine;
//		this.transformer = transformer;
		assert(edmDataServices != null);
		assert(metadata != null);
		assert(hypermediaEngine != null);
//		assert(transformer != null);
	}
	
	@Override
	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return 	ResourceTypeHelper.isType(type, genericType, EntityResource.class) ||
				ResourceTypeHelper.isType(type, genericType, CollectionResource.class, OEntity.class) ||
				ResourceTypeHelper.isType(type, genericType, CollectionResource.class, Entity.class);
	}

	@Override
	public long getSize(RESTResource t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	/**
	 * Writes a Atom (OData) representation of {@link EntityResource} to the output stream.
	 * 
	 * @precondition supplied {@link EntityResource} is non null
	 * @precondition {@link EntityResource#getEntity()} returns a valid OEntity, this 
	 * provider only supports serialising OEntities
	 * @postcondition non null Atom (OData) XML document written to OutputStream
	 * @invariant valid OutputStream
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void writeTo(RESTResource resource, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException,
			WebApplicationException {
		assert (resource != null);
		assert(uriInfo != null);
		
		try {
			if(ResourceTypeHelper.isType(type, genericType, EntityResource.class, OEntity.class)) {
				EntityResource<OEntity> entityResource = (EntityResource<OEntity>) resource;

				//Convert Links to list of OLink
				List<OLink> olinks = new ArrayList<OLink>();
				if (entityResource.getLinks() != null) {
					for(Link link : entityResource.getLinks()) {
						addLinkToOLinks(olinks, link);
					}
				}
				
				//Write entry
				OEntity tempEntity = entityResource.getEntity();
				String fqName = metadata.getModelName() + Metadata.MODEL_SUFFIX + "." + entityResource.getEntityName();
				EdmEntityType entityType = (EdmEntityType) edmDataServices.findEdmEntityType(fqName);
				EdmEntitySet entitySet = edmDataServices.getEdmEntitySet(entityType);
	        	// create OEntity with our EdmEntitySet see issue https://github.com/aphethean/IRIS/issues/20
            	OEntity oentity = OEntities.create(entitySet, tempEntity.getEntityKey(), tempEntity.getProperties(), null);
				entryWriter.write(uriInfo, new OutputStreamWriter(entityStream, "UTF-8"), Responses.entity(oentity), entitySet, olinks);
			} else if(ResourceTypeHelper.isType(type, genericType, EntityResource.class, Entity.class)) {
				EntityResource<Entity> entityResource = (EntityResource<Entity>) resource;

				Collection<Link> linksCollection = entityResource.getLinks();
				List<Link> links = new ArrayList<Link>(linksCollection);
				
				//Write entry
				Entity entity = entityResource.getEntity();
				assert(entity.getName().equals(entityResource.getEntityName()));
				EntityMetadata entityMetadata = metadata.getEntityMetadata((entityResource.getEntityName() == null ? entity.getName() : entityResource.getEntityName()));
				// Write Entity object with Abdera implementation
				AtomEntityEntryFormatWriter entityEntryWriter = new AtomEntityEntryFormatWriter();
				entityEntryWriter.write(uriInfo, new OutputStreamWriter(entityStream, "UTF-8"), entity, entityMetadata, links, metadata.getModelName());
			} else if(ResourceTypeHelper.isType(type, genericType, EntityResource.class)) {
				EntityResource<Object> entityResource = (EntityResource<Object>) resource;

				//Links and entity properties
				Collection<Link> linksCollection = entityResource.getLinks();
				List<Link> links = linksCollection != null ? new ArrayList<Link>(linksCollection) : new ArrayList<Link>();
				Object entity = entityResource.getEntity();
				String entityName = entityResource.getEntityName();
				EntityProperties props = new EntityProperties();
				if(entity != null) {
					Map<String, Object> objProps = new BeanTransformer().transform(entity);
					for(String propName : objProps.keySet()) {
						props.setProperty(new EntityProperty(propName, objProps.get(propName)));
					}
				}
				EntityMetadata entityMetadata = metadata.getEntityMetadata(entityName);
				AtomEntityEntryFormatWriter entityEntryWriter = new AtomEntityEntryFormatWriter();
				entityEntryWriter.write(uriInfo, new OutputStreamWriter(entityStream, "UTF-8"), new Entity(entityName, props), entityMetadata, links, metadata.getModelName());
			} else if(ResourceTypeHelper.isType(type, genericType, CollectionResource.class, OEntity.class)) {
				CollectionResource<OEntity> collectionResource = ((CollectionResource<OEntity>) resource);
				String fqName = metadata.getModelName() + Metadata.MODEL_SUFFIX + "." + collectionResource.getEntityName();
				EdmEntityType entityType = (EdmEntityType) edmDataServices.findEdmEntityType(fqName);
				EdmEntitySet entitySet = edmDataServices.getEdmEntitySet(entityType);
				List<EntityResource<OEntity>> collectionEntities = (List<EntityResource<OEntity>>) collectionResource.getEntities();
				List<OEntity> entities = new ArrayList<OEntity>();
				Map<String, List<OLink>> entityOlinks = new HashMap<String, List<OLink>>();
				for (EntityResource<OEntity> collectionEntity : collectionEntities) {
		        	// create OEntity with our EdmEntitySet see issue https://github.com/aphethean/IRIS/issues/20
					OEntity tempEntity = collectionEntity.getEntity();
	            	OEntity entity = OEntities.create(entitySet, tempEntity.getEntityKey(), tempEntity.getProperties(), null);
					
					//Add entity links
					List<OLink> olinks = new ArrayList<OLink>();
					if (collectionEntity.getLinks() != null) {
						for(Link link : collectionEntity.getLinks()) {
							addLinkToOLinks(olinks, link);		//Link to resource (feed entry) 		
							
							/*
							 * TODO we can remove this way of adding links to other resources once we support multiple transitions 
							 * to a resource state.  https://github.com/aphethean/IRIS/issues/17
							//Links to other resources
					        List<Transition> entityTransitions = resourceRegistry.getEntityTransitions(entity.getEntitySetName());
					        if(entityTransitions != null) {
						        for(Transition transition : entityTransitions) {
						        	//Create Link from transition
									String rel = transition.getTarget().getName();
									UriBuilder linkTemplate = UriBuilder.fromUri(RequestContext.getRequestContext().getBasePath()).path(transition.getCommand().getPath());
									Map<String, Object> properties = new HashMap<String, Object>();
									properties.putAll(transformer.transform(entity));
									URI href = linkTemplate.buildFromMap(properties);
									Link entityLink = new Link(transition, rel, href.toASCIIString(), "GET");
									
									addLinkToOLinks(olinks, entityLink);
								}
					        }
							 */
						}		
					}
					entityOlinks.put(InternalUtil.getEntityRelId(entity), olinks);					
					entities.add(entity);
				}
				// TODO implement collection properties and get transient values for inlinecount and skiptoken
				Integer inlineCount = null;
				String skipToken = null;
				feedWriter.write(uriInfo, new OutputStreamWriter(entityStream, "UTF-8"), collectionResource.getLinks(), Responses.entities(entities, entitySet, inlineCount, skipToken), entityOlinks);
			} else if(ResourceTypeHelper.isType(type, genericType, CollectionResource.class, Entity.class)) {
				CollectionResource<Entity> collectionResource = ((CollectionResource<Entity>) resource);
				
				// TODO implement collection properties and get transient values for inlinecount and skiptoken
				Integer inlineCount = null;
				String skipToken = null;
				
				//Write feed
				EntityMetadata entityMetadata = metadata.getEntityMetadata(collectionResource.getEntityName());
				AtomEntityFeedFormatWriter entityFeedWriter = new AtomEntityFeedFormatWriter();
				entityFeedWriter.write(uriInfo, new OutputStreamWriter(entityStream, "UTF-8"), collectionResource, entityMetadata, inlineCount, skipToken, metadata.getModelName());
			} else {
				logger.error("Accepted object for writing in isWriteable, but type not supported in writeTo method");
				throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
			}
		} catch (ODataProducerException e) {
			logger.error("An error occurred while writing " + mediaType + " resource representation", e);
		}
	}
	
	public void addLinkToOLinks(List<OLink> olinks, Link link) {
		RequestContext requestContext = RequestContext.getRequestContext();		//TODO move to constructor to improve performance
		String rel = link.getRel();
		if(rel.contains("item")) {
			if(link.getTransition().isGetFromCollectionToEntityResource()) {
				//Links from collection to entity resource of an entity are considered 'self' links within an odata feed
				rel = "self";
			}
			else {
				//entry type relations should use the entityType name
				rel = XmlFormatWriter.related + link.getTransition().getTarget().getEntityName();
			}
		} else if (rel.contains("collection")) {
			rel = XmlFormatWriter.related + link.getTitle();
		}
		String href = link.getHref();
		if(requestContext != null) {
			//Extract the transition fragment from the URI path
			href = link.getHrefTransition(requestContext.getBasePath());
		}
		String title = link.getTitle();
		OLink olink;
		Transition linkTransition = link.getTransition();
		if(linkTransition != null && linkTransition.getTarget().getClass() == CollectionResourceState.class) {
			olink = OLinks.relatedEntities(rel, title, href);
		} else {
			olink = OLinks.relatedEntity(rel, title, href);
		}
		olinks.add(olink);
		if (rel.contains("edit")) {
			dropLinkByRel(olinks, "self");
		}
	}

	private boolean dropLinkByRel(List<OLink> links, String rel) {
		boolean found = false;
		for (int i = 0; i < links.size(); i++) {
			OLink link = links.get(i);
			if (link.getRelation().equals(rel)) {
				links.remove(i);
				found = true;
				break;
			}
		}
		return found;
	}

	@Override
	public boolean isReadable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		// TODO this class can only deserialise EntityResource with OEntity, but at the moment we are accepting any EntityResource or CollectionResource
		return ResourceTypeHelper.isType(type, genericType, EntityResource.class)
				|| ResourceTypeHelper.isType(type, genericType, CollectionResource.class);
	}

	/**
	 * Reads a Atom (OData) representation of {@link EntityResource} from the input stream.
	 * 
	 * @precondition {@link InputStream} contains a valid Atom (OData) Entity enclosed in a <resource/> document
	 * @postcondition {@link EntityResource} will be constructed and returned.
	 * @invariant valid InputStream
	 */
	@Override
	public EntityResource<OEntity> readFrom(Class<RESTResource> type,
			Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {
		
		// TODO check media type can be handled
		
		if(ResourceTypeHelper.isType(type, genericType, EntityResource.class)) {
			ResourceState currentState = null;
			OEntityKey entityKey = null;
			/* 
			 * TODO add uritemplate helper class (something like the wink JaxRsUriTemplateProcessor) to 
			 * our project, or use wink directly, will also need it for handling link transitions
			 */
//			JaxRsUriTemplateProcessor processor = new JaxRsUriTemplateProcessor("/{therest}/");
//			UriTemplateMatcher matcher = processor.matcher();
//			matcher.matches(uriInfo.getPath());
//			String entityKey = matcher.getVariableValue("id");
			String path = uriInfo.getPath();
			logger.info("Reading atom xml content for [" + path + "]");
			Matcher matcher = RESOURCE_PATTERN.matcher(path);
			if (matcher.find()) {
				// the resource path
				String resourcePath = matcher.group(1);
				Set<ResourceState> states = hypermediaEngine.getResourceStatesForPath(resourcePath);
				if (states != null && states.size() > 0) {
					currentState = findCollectionResourceState(states);
					// at the moment things are pretty simply, the bit after the last slash is the key
					entityKey = OEntityKey.parse(matcher.group(2));
				}
			}
			if (currentState == null) {
				// might be a request without an entity key e.g. a POST
				if (!path.startsWith("/")) {
					// TODO remove this hack :-(
					path = "/" + path;
				}
				// TODO, improve this ridiculously basic support for Update
				if (path.contains("(")) {
					path = path.substring(0, path.indexOf("("));
					path = "^" + path + "(|\\(.*\\))";
				} else {
					path = "^" + path + "(|\\(\\))";
				}
				Set<ResourceState> states = hypermediaEngine.getResourceStatesForPathRegex(path);
				if (states != null && states.size() > 0) {
					currentState = findCollectionResourceState(states);
				} else {
					// give up, we can't handle this request 404
					logger.error("resource not found in registry");
					throw new WebApplicationException(Response.Status.NOT_FOUND);
				}
			}

			// parse the request content
			Reader reader = new InputStreamReader(entityStream);
			assert(currentState != null) : "Must have found a resource or thrown exception";
			Entry e = new AtomEntryFormatParserExt(edmDataServices, currentState.getName(), entityKey, null).parse(reader);
			
			return new EntityResource<OEntity>(e.getEntity());
		} else {
			logger.error("Unhandled type");
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		}

	}

	private CollectionResourceState findCollectionResourceState(Set<ResourceState> states) {
		for (ResourceState state : states) {
			if (state instanceof CollectionResourceState) {
				return (CollectionResourceState) state;
			}
		}
		return null;
	}
	
	protected void setUriInfo(UriInfo uriInfo) {
		this.uriInfo = uriInfo;
	}
}
