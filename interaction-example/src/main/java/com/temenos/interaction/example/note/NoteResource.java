package com.temenos.interaction.example.note;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.odata4j.core.OEntity;

import com.temenos.interaction.core.EntityResource;

@XmlRootElement(name = "resource")
public class NoteResource implements EntityResource {

    @XmlElement(name = "body")
    private String body;

	private OEntity entity;
	
    /* Keep jaxb happy */
    public NoteResource() {}
	
	public NoteResource(OEntity entity) {
		this.entity = entity;
	}
	
	public Note getNote() {
		return new Note(body);
	}
	
	public OEntity getEntity() {
		return entity;
	}

}
