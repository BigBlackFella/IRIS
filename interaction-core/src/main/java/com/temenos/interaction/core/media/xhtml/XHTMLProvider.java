package com.temenos.interaction.core.media.xhtml;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

import org.odata4j.core.OEntity;
import org.odata4j.core.OProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cambridge.Template;
import cambridge.TemplateFactory;

import com.temenos.interaction.core.entity.Entity;
import com.temenos.interaction.core.entity.EntityMetadata;
import com.temenos.interaction.core.entity.EntityProperties;
import com.temenos.interaction.core.entity.EntityProperty;
import com.temenos.interaction.core.entity.Metadata;
import com.temenos.interaction.core.hypermedia.Link;
import com.temenos.interaction.core.resource.CollectionResource;
import com.temenos.interaction.core.resource.EntityResource;
import com.temenos.interaction.core.resource.RESTResource;
import com.temenos.interaction.core.resource.ResourceTypeHelper;

@Provider
@Consumes({MediaType.APPLICATION_XHTML_XML})
@Produces({MediaType.APPLICATION_XHTML_XML, MediaType.TEXT_HTML})
public class XHTMLProvider implements MessageBodyReader<RESTResource>, MessageBodyWriter<RESTResource> {
	private final Logger logger = LoggerFactory.getLogger(XHTMLProvider.class);

	@SuppressWarnings("unused")
	@Context
	private UriInfo uriInfo;
	private Metadata metadata = null;
	private XHTMLTemplateFactories templateFactories = new XHTMLTemplateFactories();
	
	public XHTMLProvider(Metadata metadata) {
		this.metadata = metadata;
		assert(metadata != null);
	}
	
	@Override
	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return ResourceTypeHelper.isType(type, genericType, EntityResource.class);
	}

	@Override
	public long getSize(RESTResource t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	/**
	 * Writes a XHTML representation of
	 * {@link EntityResource} to the output stream.
	 * 
	 * @precondition supplied {@link EntityResource} is non null
	 * @precondition {@link EntityResource#getEntity()} returns a valid OEntity, this 
	 * provider only supports serialising OEntities
	 * @postcondition non null XHTML document written to OutputStream
	 * @invariant valid OutputStream
	 */
	@Override
	public void writeTo(RESTResource resource, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException,
			WebApplicationException {
		assert (resource != null);
		logger.debug("Writing " + mediaType);
		
		if (!ResourceTypeHelper.isType(type, genericType, EntityResource.class)
				&& !ResourceTypeHelper.isType(type, genericType, CollectionResource.class)) {
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		}
		OutputStreamWriter writer = new OutputStreamWriter(entityStream, "UTF-8");
		
		//Render header
		getTemplate(XHTMLTemplateFactories.TEMPLATE_HEADER).printTo(writer);
		
		// create the xhtml resource
		if (resource.getGenericEntity() != null) {
			RESTResource rResource = (RESTResource) resource.getGenericEntity().getEntity();

			//render resource links
			Collection<Link> links = rResource.getLinks();
			if (links != null) {
				Template template = getTemplate(XHTMLTemplateFactories.TEMPLATE_RESOURCE_LINKS);
				template.setProperty("resourceLinks", links);
				template.printTo(writer);
			}
			
			// add contents of supplied entity to the property map
			if (ResourceTypeHelper.isType(type, genericType, EntityResource.class, OEntity.class)) {
				@SuppressWarnings("unchecked")
				EntityResource<OEntity> oentityResource = (EntityResource<OEntity>) resource;
				Map<String, String> entityProperties = new HashMap<String, String>();
				buildFromOEntity(entityProperties, oentityResource.getEntity());
				Template template = getTemplate(XHTMLTemplateFactories.TEMPLATE_ENTITY);
				template.setProperty("entityName", oentityResource.getEntity().getEntitySetName());
				template.setProperty("entityProperties", entityProperties);
				template.printTo(writer);
			} else if (ResourceTypeHelper.isType(type, genericType, EntityResource.class, Entity.class)) {
				@SuppressWarnings("unchecked")
				EntityResource<Entity> entityResource = (EntityResource<Entity>) resource;
				Map<String, Object> entityProperties = new HashMap<String, Object>();
				buildFromEntity(entityProperties, entityResource.getEntity());
				Template template = getTemplate(XHTMLTemplateFactories.TEMPLATE_ENTITY);
				template.setProperty("entityName", entityResource.getEntity().getName());
				template.setProperty("entityProperties", entityProperties);
				template.printTo(writer);
			} else if (ResourceTypeHelper.isType(type, genericType, EntityResource.class)) {
				EntityResource<?> entityResource = (EntityResource<?>) resource;
				Map<String, Object> entityProperties = new HashMap<String, Object>();
				buildFromBean(entityProperties, entityResource.getEntity(), entityResource.getEntityName());
				Template template = getTemplate(XHTMLTemplateFactories.TEMPLATE_ENTITY);
				template.setProperty("entityName", entityResource.getEntityName());
				template.setProperty("entityProperties", entityProperties);
				template.printTo(writer);
			} else {
				logger.error("Accepted object for writing in isWriteable, but type not supported in writeTo method");
				throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
			}

			//Render footer
			getTemplate(XHTMLTemplateFactories.TEMPLATE_FOOTER).printTo(writer);
			writer.flush();
		}
	}

	protected void buildFromOEntity(Map<String, String> map, OEntity entity) {
		EntityMetadata entityMetadata = metadata.getEntityMetadata(entity.getEntitySetName());
		if (entityMetadata == null)
			throw new IllegalStateException("Entity metadata could not be found [" + entity.getEntitySetName() + "]");

		// add properties if they are present on the resolved entity
		for (OProperty<?> property : entity.getProperties()) {
			if (entityMetadata.getPropertyVocabulary(property.getName()) != null && property.getValue() != null) {
				map.put(property.getName(), property.getValue().toString());				
			}
		}
	}
	
	protected void buildFromEntity(Map<String, Object> map, Entity entity) {

		EntityProperties entityProperties = entity.getProperties();
		Map<String, EntityProperty> properties = entityProperties.getProperties();
				
		for (Map.Entry<String, EntityProperty> property : properties.entrySet()) 
		{
			String propertyName = property.getKey(); 
			EntityProperty propertyValue = (EntityProperty) property.getValue();
	   		map.put(propertyName, propertyValue.getValue());	
		}
	}
	
	protected void buildFromBean(Map<String, Object> map, Object bean, String entityName) {
		EntityMetadata entityMetadata = metadata.getEntityMetadata(entityName);
		if (entityMetadata == null)
			throw new IllegalStateException("Entity metadata could not be found [" + entityName + "]");

		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
			for (PropertyDescriptor propertyDesc : beanInfo.getPropertyDescriptors()) {
			    String propertyName = propertyDesc.getName();
				if (entityMetadata.getPropertyVocabulary(propertyName) != null) {
				    Object value = propertyDesc.getReadMethod().invoke(bean);
					map.put(propertyName, value);				
				}
			}
		} catch (IllegalArgumentException e) {
			logger.error("Error accessing bean property", e);
		} catch (IntrospectionException e) {
			logger.error("Error accessing bean property", e);
		} catch (IllegalAccessException e) {
			logger.error("Error accessing bean property", e);
		} catch (InvocationTargetException e) {
			logger.error("Error accessing bean property", e);
		}
	}

	@Override
	public boolean isReadable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		// this class can only deserialise EntityResource
		return ResourceTypeHelper.isType(type, genericType, EntityResource.class);
	}

	/**
	 * Reads a Hypertext Application Language (HAL) representation of
	 * {@link EntityResource} from the input stream.
	 * 
	 * @precondition {@link InputStream} contains a valid HAL <resource/> document
	 * @postcondition {@link EntityResource} will be constructed and returned.
	 * @invariant valid InputStream
	 */
	@Override
	public RESTResource readFrom(Class<RESTResource> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {

		if (!ResourceTypeHelper.isType(type, genericType, EntityResource.class))
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		
		return null;
	}
	
	/* Ugly testing support :-( */
	protected void setUriInfo(UriInfo uriInfo) {
		this.uriInfo = uriInfo;
	}
	
	/*
	 * Return the specified template factory
	 */
	private Template getTemplate(String templateFactoryName) throws WebApplicationException {
		TemplateFactory tf = templateFactories.getTemplateFactory(templateFactoryName);
		if(tf != null) {
			return tf.createTemplate();
		}
		else {
			logger.error("Failed to obtain template factory " + templateFactoryName);
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
}
