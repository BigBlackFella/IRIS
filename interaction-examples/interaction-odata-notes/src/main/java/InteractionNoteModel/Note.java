package InteractionNoteModel;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Note {

	@Id
	/* Added this GeneratedValue annotation to auto create ids, otherwise this class in generated from the EDMX */
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	private Integer Id;
	
	private String body;
	
	/* link to the person whose note this belongs to */
	private Integer personId;
	
	@ManyToOne
	@JoinColumn(name = "PERSONID")
	private Person person;
	
	public Note() {}
}