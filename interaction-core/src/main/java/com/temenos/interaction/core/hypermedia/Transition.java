package com.temenos.interaction.core.hypermedia;


public class Transition {

	/**
	 * Add transition to every item in collection
	 */
	public static final int FOR_EACH = 1;
	/**
	 * This transition is an auto transition.<br>
	 * A transition to this state from the same state as the auto target will result 
	 * in a 205 Reset Content HTTP status at runtime.
	 * A transition to this state from a different state to the auto target will result
	 * in a 303 Redirect HTTP status at runtime.
	 */
	public static final int AUTO = 2;

	private final ResourceState source, target;
	private final TransitionCommandSpec command;
	private final String label;
	
	public Transition(ResourceState source, TransitionCommandSpec command, ResourceState target) {
		this(source, command, target, null);
	}

	public Transition(ResourceState source, TransitionCommandSpec command, ResourceState target, String label) {
		this.source = source;
		this.target = target;
		this.command = command;
		if(label == null) {
			//Generate label from command parameters
			String s = null;
			if(command.getParameters() != null) {
				for(String parameter : command.getParameters().values()) {
					s = s != null ? s + ", " + parameter : parameter;
				}
			}
			this.label = s;
		}
		else {
			this.label = label;
		}
	}
	
	public ResourceState getSource() {
		return source;
	}

	public ResourceState getTarget() {
		return target;
	}

	public TransitionCommandSpec getCommand() {
		return command;
	}
	
	public String getLabel() {
		return label;
	}
	
	public String getId() {
		String labelText = "";
		if(label != null && !label.equals(target.getName())) {
			labelText = "(" + label + ")";
		}
		if(source == null) {
			return target.getId() + ">" + command.getMethod() + labelText + ">" + target.getId();		//transition to itself
		}
		else {
			return source.getId() + ">" + command.getMethod() + labelText + ">" + target.getId();
		}
	}
	
	/**
	 * Indicates whether this is a GET transition from a collection resource state
	 * to an entity resource state within the same entity.
	 * @return true/false
	 */
	public boolean isGetFromCollectionToEntityResource() {
		return source != null && command.getMethod() != null &&
				command.getMethod().equals("GET") &&
				source.getEntityName().equals(target.getEntityName()) &&
				source instanceof CollectionResourceState &&
				target instanceof ResourceState;		
	}
	
	public boolean equals(Object other) {
		//check for self-comparison
	    if ( this == other ) return true;
	    if ( !(other instanceof Transition) ) return false;
	    Transition otherTrans = (Transition) other;
	    // only compare the ResourceState name to avoid recursion
	    return ((source == null && otherTrans.source == null)
	    		|| source != null && otherTrans.source != null && source.getName().equals(otherTrans.source.getName()) ) &&
	    	target.getName().equals(otherTrans.target.getName()) &&
	    	command.equals(otherTrans.command);
	}
	
	public int hashCode() {
		return (source != null ? source.getName().hashCode() : 0) +
			target.getName().hashCode() +
			command.hashCode();
	}

	public String toString() {
		return getId();
	}
	
}
