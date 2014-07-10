package com.temenos.interaction.springdsl;

/*
 * #%L
 * interaction-springdsl
 * %%
 * Copyright (C) 2012 - 2014 Temenos Holdings N.V.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.springframework.beans.factory.FactoryBean;

import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.hypermedia.Transition;
import com.temenos.interaction.core.hypermedia.expression.Expression;

public class TransitionFactoryBean implements FactoryBean<Transition> {

	private ResourceState source, target;
	private String label;

	// TransitionCommand parameters
	private String method;
	private String path;
	private int flags;
	// conditional link evaluation expression
	private Expression evaluation;
	private Map<String, String> uriParameters;
	private List<String> functionList;

	@Override
	public Transition getObject() throws Exception {
		Transition.Builder builder = new Transition.Builder();
		builder.source(source);
		builder.target(target);
		builder.method(method);
		builder.path(path);
		builder.flags(flags);
		builder.evaluation(evaluation);
		builder.uriParameters(uriParameters);
		return builder.build();
	}

	@Override
	public Class<? extends Transition> getObjectType() {
		return Transition.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public ResourceState getSource() {
		return source;
	}

	public void setSource(ResourceState source) {
		this.source = source;
	}

	public ResourceState getTarget() {
		return target;
	}

	public void setTarget(ResourceState target) {
		this.target = target;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public Expression getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(Expression evaluation) {
		this.evaluation = evaluation;
	}

	public Map<String, String> getUriParameters() {
		return uriParameters;
	}

	public void setUriParameters(Map<String, String> uriParameters) {
		this.uriParameters = uriParameters;
	}

	/**
	 * Sets the function list.
	 * 
	 * @param functionList
	 *            the new function list
	 */
	public void setFunctionList(List<String> functionList) {
		this.functionList = functionList;
		
		if (functionList != null) {
			List<Expression> expressionsList = new ArrayList<Expression>();
			Expression expression = null;

			for (String expressionTxt : functionList) {

				StringTokenizer tokenizer = new StringTokenizer(expressionTxt, ",");
				String name = tokenizer.nextToken();
				String functionName = tokenizer.nextToken();

				if (functionName.contains("OK")) {
					//expression = new ResourceGETExpression(resourceState, Function.OK);
				} else {
					//expression = new ResourceGETExpression(resourceState, Function.NOT_FOUND);
				}

				expressionsList.add(expression);

			}
			//evaluation = new SimpleLogicalExpressionEvaluator(expressionsList);

		}
	}
}
