/*
 * Copyright 2011 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.temenos.interaction.core.web;

import java.security.Principal;

import javax.ws.rs.core.UriBuilder;

/**
 * @author Mattias Hellborg Arthursson
 * @author Kalle Stenflo
 */
public class RequestContext {

    public static final String HATEOAS_OPTIONS_HEADER = "x-jax-rs-hateoas-options";

    private final static ThreadLocal<RequestContext> currentContext = new ThreadLocal<RequestContext>();

    public static void setRequestContext(RequestContext context) {
        currentContext.set(context);
    }

    public static RequestContext getRequestContext() {
        return currentContext.get();
    }

    public static void clearRequestContext() {
        currentContext.remove();
    }


    private final UriBuilder basePath;
    private final String requestUri;
    private final String verbosityHeader;
    private Principal userPrincipal;

    public RequestContext(UriBuilder basePath, String requestUri, String verbosityHeader) {
        this.basePath = basePath;
        this.requestUri = requestUri;
        this.verbosityHeader = verbosityHeader;
    }

    public RequestContext(UriBuilder basePath, String requestUri, String verbosityHeader, Principal userPrincipal) {
        this.basePath = basePath;
        this.requestUri = requestUri;
        this.verbosityHeader = verbosityHeader;
        this.userPrincipal = userPrincipal;
    }
    
    public UriBuilder getBasePath() {
        return basePath.clone();
    }

    public String getRequestUri() {
        return requestUri;
    }

    public String getVerbosityHeader() {
        return verbosityHeader;
    }
    
    public Principal getUserPrincipal(){
    	return this.userPrincipal;
    }
    
}
