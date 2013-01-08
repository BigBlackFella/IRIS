package com.temenos.interaction.core.hypermedia;

import com.temenos.interaction.core.hypermedia.expression.ResourceGETExpression;

/**
 * Define how a transition from one state to another should occur.
 * @author aphethean
 */
public class TransitionCommandSpec {

	private final String method;
	private final String path;
	private final int flags;
	// conditional link evaluation expression 
	private final ResourceGETExpression evaluation;
	// the original unmapped resourcePath (required to form a correct interaction map by paths)
	private final String originalPath;
	// TODO will need to define query params for transitions
	//private final List<String> queryParams;
	
	protected TransitionCommandSpec(String method, String path) {
		this(method, path, 0);
	}

	protected TransitionCommandSpec(String method, String path, int flags) {
		this(method, path, flags, null, path);
	}
	
	protected TransitionCommandSpec(String method, String path, int flags, ResourceGETExpression evaluation, String originalPath) {
		this.method = method;
		this.path = path;
		this.flags = flags;
		this.evaluation = evaluation;
		this.originalPath = originalPath;
	}
	
	public String getPath() {
		return path;
	}
	
	public String getOriginalPath() {
		return originalPath;
	}
	
	public int getFlags() {
		return flags;
	}

	public String getMethod() {
		return method;
	}

	public ResourceGETExpression getEvaluation() {
		return evaluation;
	}

	/**
	 * Is this transition command to be applied to each item in a collection?
	 * @return
	 */
	public boolean isForEach() {
		return ((flags & Transition.FOR_EACH) == Transition.FOR_EACH);
	}
	
	/**
	 * Is this transition and auto transition?
	 * @return
	 */
	public boolean isAutoTransition() {
		return ((flags & Transition.AUTO) == Transition.AUTO);
	}

	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof TransitionCommandSpec)) return false;
		TransitionCommandSpec otherObj = (TransitionCommandSpec) other;
		return this.getFlags() == otherObj.getFlags() &&
				((this.getPath() == null && otherObj.getPath() == null) || (this.getPath() != null && this.getPath().equals(otherObj.getPath()))) &&
				((this.getMethod() == null && otherObj.getMethod() == null) || (this.getMethod() != null && this.getMethod().equals(otherObj.getMethod())));
	}
	
	public int hashCode() {
		return this.flags 
				+ (this.path != null ? this.path.hashCode() : 0)
				+ (this.method != null ? this.method.hashCode() : 0);
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (isForEach())
			sb.append("*");
		sb.append(method + (path != null && path.length() > 0 ? " " + path : ""));
		if (evaluation != null) {
			sb.append(" (");
			if (evaluation.getFunction().equals(ResourceGETExpression.Function.OK))
				sb.append("OK(").append(evaluation.getState()).append(")");
			if (evaluation.getFunction().equals(ResourceGETExpression.Function.NOT_FOUND))
				sb.append("NOT_FOUND").append(evaluation.getState()).append(")");
			sb.append(")");
		}
		return sb.toString();
	}
}
