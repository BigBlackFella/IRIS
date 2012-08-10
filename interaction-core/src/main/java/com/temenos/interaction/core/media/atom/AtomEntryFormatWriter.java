package com.temenos.interaction.core.media.atom;

import java.io.Writer;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.odata4j.core.OAtomEntity;
import org.odata4j.core.OAtomStreamEntity;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OLink;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmProperty;
import org.odata4j.format.Entry;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.xml.XmlFormatWriter;
import org.odata4j.internal.InternalUtil;
import org.odata4j.producer.EntityResponse;
import org.odata4j.stax2.QName2;
import org.odata4j.stax2.XMLFactoryProvider2;
import org.odata4j.stax2.XMLWriter2;

/**
 * Slightly modified version of @link{org.odata4j.format.xml.AtomEntryFormatWriter} that 
 * is more aligned with JAX-RS.
 * @author aphethean
 *
 */
public class AtomEntryFormatWriter extends XmlFormatWriter implements FormatWriter<EntityResponse> {

  protected String baseUri;

  public void writeRequestEntry(Writer w, Entry entry) {

    DateTime utc = new DateTime().withZone(DateTimeZone.UTC);
    String updated = InternalUtil.toString(utc);

    XMLWriter2 writer = XMLFactoryProvider2.getInstance().newXMLWriterFactory2().createXMLWriter(w);
    writer.startDocument();

    writer.startElement(new QName2("entry"), atom);
    writer.writeNamespace("d", d);
    writer.writeNamespace("m", m);
    
    OEntity entity = entry.getEntity();
    writeEntry(writer, null, entity.getProperties(), entity.getLinks(),
        null, updated, entity.getEntitySet(), false);
    writer.endDocument();

  }

  @Override
  public String getContentType() {
    return ODataConstants.APPLICATION_ATOM_XML_CHARSET_UTF8;
  }

  public void write(UriInfo uriInfo, Writer w, EntityResponse target, EdmEntitySet entitySet, List<OLink> olinks) {
    String baseUri = uriInfo.getBaseUri().toString();

    DateTime utc = new DateTime().withZone(DateTimeZone.UTC);
    String updated = InternalUtil.toString(utc);

    XMLWriter2 writer = XMLFactoryProvider2.getInstance().newXMLWriterFactory2().createXMLWriter(w);
    writer.startDocument();

    writer.startElement(new QName2("entry"), atom);
    writer.writeNamespace("m", m);
    writer.writeNamespace("d", d);
    writer.writeAttribute("xml:base", baseUri);

    // this darn writer sometimes uses the EdmEntitySet we pass in and sometimes uses OEntity.getEntitySet
    OEntity origOE = target.getEntity() ;   
    OEntity newOE = OEntities.create(entitySet, origOE.getEntityKey(), origOE.getProperties(), olinks);

    writeEntry(writer, newOE, newOE.getProperties(), newOE.getLinks(), baseUri, updated, entitySet, true);
    writer.endDocument();
  }

  @Override
  public String writeEntry(XMLWriter2 writer, OEntity oe,
	      List<OProperty<?>> entityProperties, List<OLink> entityLinks,
	      String baseUri, String updated,
	      EdmEntitySet ees, boolean isResponse) {

	    String relid = null;
	    String absid = null;
	    if (isResponse) {
	      relid = InternalUtil.getEntityRelId(oe);
	      //Odata 4j creates IDs with an L suffix on Edm.Int types, quotes on Edm.String types - remove to conform to interaction links
	      List<String> keys = oe.getEntityType().getKeys();
	      if(keys.size() > 0) {
		      EdmProperty keyProperty = oe.getEntityType().findDeclaredProperty(keys.get(0));
		      if(keyProperty.getType().getFullyQualifiedTypeName().startsWith("Edm.Int") && relid.endsWith("L)")) {
		    	  relid = relid.substring(0, relid.length()-2) + ")";
		      }
	      }
	      absid = baseUri + relid;
	      writeElement(writer, "id", absid);
	    }

	    OAtomEntity oae = getAtomInfo(oe);

	    writeElement(writer, "title", oae.getAtomEntityTitle(), "type", "text");
	    String summary = oae.getAtomEntitySummary();
	    if (summary != null) {
	      writeElement(writer, "summary", summary, "type", "text");
	    }

	    LocalDateTime updatedTime = oae.getAtomEntityUpdated();
	    if (updatedTime != null) {
	      updated = InternalUtil.toString(updatedTime.toDateTime(DateTimeZone.UTC));
	    }
	    writeElement(writer, "updated", updated);

	    writer.startElement("author");
	    writeElement(writer, "name", oae.getAtomEntityAuthor());
	    writer.endElement("author");

	    if (entityLinks != null) {
	      if (isResponse) {
	        // the producer has populated the link collection, we just what he gave us.
	        for (OLink link : entityLinks) {
	          String type = (link.isCollection())
	              ? atom_feed_content_type
	              : atom_entry_content_type;
	          String href = link.getHref();
	          if (link.isInline()) {
	            writer.startElement("link");
	            writer.writeAttribute("rel", link.getRelation());
	            writer.writeAttribute("type", type);
	            writer.writeAttribute("title", link.getTitle());
	            writer.writeAttribute("href", href);
	            // write the inlined entities inside the link element
	            writeLinkInline(writer, link,
	                href, baseUri, updated, isResponse);
	            writer.endElement("link");
	          } else {
	            // deferred link.
	            writeElement(writer, "link", null,
	                "rel", link.getRelation(),
	                "type", type,
	                "title", link.getTitle(),
	                "href", href);
	          }
	        }
	      } else {
	        // for requests we include only the provided links
	        // Note: It seems that OLinks for responses are only built using the
	        // title and OLinks for requests have the additional info in them
	        // alread.  I'm leaving that inconsistency in place for now but this
	        // else and its preceding if could probably be unified.
	        for (OLink olink : entityLinks) {
	          String type = olink.isCollection()
	              ? atom_feed_content_type
	              : atom_entry_content_type;

	          writer.startElement("link");
	          writer.writeAttribute("rel", olink.getRelation());
	          writer.writeAttribute("type", type);
	          writer.writeAttribute("title", olink.getTitle());
	          writer.writeAttribute("href", olink.getHref());
	          if (olink.isInline()) {
	            // write the inlined entities inside the link element
	            writeLinkInline(writer, olink, olink.getHref(),
	                baseUri, updated, isResponse);
	          }
	          writer.endElement("link");
	        }
	      }
	    } // else entityLinks null

	    writeElement(writer, "category", null,
	        // oe is null for creates
	        "term", oe == null ? ees.getType().getFullyQualifiedTypeName() : oe.getEntityType().getFullyQualifiedTypeName(),
	        "scheme", scheme);

	    boolean hasStream = false;
	    if (oe != null) {
	      OAtomStreamEntity stream = oe.findExtension(OAtomStreamEntity.class);
	      if (stream != null) {
	        hasStream = true;
	        writer.startElement("content");
	        writer.writeAttribute("type", stream.getAtomEntityType());
	        writer.writeAttribute("src", baseUri + stream.getAtomEntitySource());
	        writer.endElement("content");
	      }
	    }

	    if (!hasStream) {
	      writer.startElement("content");
	      writer.writeAttribute("type", MediaType.APPLICATION_XML);
	    }

	    writer.startElement(new QName2(m, "properties", "m"));
	    writeProperties(writer, entityProperties);
	    writer.endElement("properties");

	    if (!hasStream) {
	      writer.endElement("content");
	    }
	    return absid;
  }  

  private OAtomEntity getAtomInfo(OEntity oe) {
	    if (oe != null) {
	      OAtomEntity atomEntity = oe.findExtension(OAtomEntity.class);
	      if (atomEntity != null)
	        return atomEntity;
	    }
	    return new OAtomEntity() {
	      @Override
	      public String getAtomEntityTitle() {
	        return null;
	      }

	      @Override
	      public String getAtomEntitySummary() {
	        return null;
	      }

	      @Override
	      public String getAtomEntityAuthor() {
	        return null;
	      }

	      @Override
	      public LocalDateTime getAtomEntityUpdated() {
	        return null;
	      }
	    };
	  }
  
  @Override
  // the original implementation that uses the OEntity EdmEntitySet
  public void write(UriInfo uriInfo, Writer w, EntityResponse target) {
	    EdmEntitySet ees = target.getEntity().getEntitySet();
	    write(uriInfo, w, target, ees, target.getEntity().getLinks());
  }
}
