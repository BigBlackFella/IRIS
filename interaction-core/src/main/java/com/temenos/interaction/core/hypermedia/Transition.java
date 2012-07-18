package com.temenos.interaction.core.hypermedia;

public class Transition {

	public static final int FOR_EACH = 1;  // add transition to every item in collection
	public static final int RESET_CONTENT = 2;  // inform the user agent they need to 205 Reset Content following this transition

	private final ResourceState source, target;
	private final TransitionCommandSpec command;
	
	public Transition(ResourceState source, TransitionCommandSpec command, ResourceState target) {
		this.source = source;
		this.target = target;
		this.command = command;
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
	
	public String getId() {
		return source.getId() + ">" + target.getId();
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
