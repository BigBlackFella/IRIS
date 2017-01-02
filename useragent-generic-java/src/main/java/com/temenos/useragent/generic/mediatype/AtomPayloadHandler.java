package com.temenos.useragent.generic.mediatype;

/*
 * #%L
 * useragent-generic-java
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.commons.io.IOUtils;

import com.temenos.useragent.generic.Link;
import com.temenos.useragent.generic.PayloadHandler;
import com.temenos.useragent.generic.internal.DefaultEntityWrapper;
import com.temenos.useragent.generic.internal.EntityWrapper;
import com.temenos.useragent.generic.internal.LinkImpl;
import com.temenos.useragent.generic.internal.NullEntityWrapper;

/**
 * A payload handler for <i>application/atom+xml</i> media type.
 * 
 * @author ssethupathi
 *
 */
public class AtomPayloadHandler implements PayloadHandler {

	private AtomEntryHandler entityTransformer = new AtomEntryHandler();
	private boolean isCollection;
	private Feed feed;
	private String parameter; // not used yet

	public AtomPayloadHandler() {
		feed = new Abdera().newFeed();
	}

	@Override
	public boolean isCollection() {
		return isCollection;
	}

	public List<Link> links() {
		if (isCollection()) {
			return buildLinks(feed.getLinks());
		} else {
			return entityTransformer.getLinks();
		}
	}

	@Override
	public List<EntityWrapper> entities() {
		List<EntityWrapper> entityWrappers = new ArrayList<EntityWrapper>();
		for (Entry entry : feed.getEntries()) {
			EntityWrapper entityWrapper = new DefaultEntityWrapper();
			AtomEntryHandler entryHandler = new AtomEntryHandler();
			entryHandler.setEntry(entry);
			entityWrapper.setHandler(entryHandler);
			entityWrappers.add(entityWrapper);
		}
		return entityWrappers;
	}

	@Override
	public void setPayload(InputStream payload) {
		if (payload == null) {
			throw new IllegalArgumentException("Payload is null");
		}
		Document<Element> payloadDoc = null;
		try {
			payloadDoc = new Abdera().getParser().parse(payload);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Unexpected payload for media type '" + AtomUtil.MEDIA_TYPE
							+ "'.", e);
		}
		QName rootElementQName = payloadDoc.getRoot().getQName();
		if (new QName(AtomUtil.NS_ATOM, "feed").equals(rootElementQName)) {
			feed = (Feed) payloadDoc.getRoot();
			isCollection = true;
		} else if (new QName(AtomUtil.NS_ATOM, "entry")
				.equals(rootElementQName)) {
			entityTransformer.setEntry((Entry) payloadDoc.getRoot());
			isCollection = false;
		} else {
			throw new IllegalArgumentException(
					"Unexpected payload for media type '" + AtomUtil.MEDIA_TYPE
							+ "'. Payload [" + payloadDoc.getRoot().toString()
							+ "]");
		}
	}

	@Override
	public EntityWrapper entity() {
		if (!isCollection) {
			EntityWrapper wrapper = new DefaultEntityWrapper();
			wrapper.setHandler(entityTransformer);
			return wrapper;
		}
		return new NullEntityWrapper();
	}

	@Override
	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	private List<Link> buildLinks(List<org.apache.abdera.model.Link> abderaLinks) {
		List<Link> links = new ArrayList<Link>();
		for (org.apache.abdera.model.Link abderaLink : abderaLinks) {
			links.add(new LinkImpl.Builder(abderaLink.getAttributeValue("href"))
					.baseUrl(AtomUtil.getBaseUrl(feed))
					.rel(AtomUtil.extractRel(abderaLink
							.getAttributeValue("rel")))
					.title(abderaLink.getAttributeValue("title"))
					.description(
							AtomUtil.extractDescription(abderaLink
									.getAttributeValue("rel"))).build());
		}
		return links;
	}
}
