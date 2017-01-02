package com.temenos.useragent.generic.http;

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


import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements {@link HttpClient http client} using Apache
 * HttpComponents {@linkplain https://hc.apache.org/}
 * 
 * @author ssethupathi
 *
 */
public class DefaultHttpClient implements HttpClient {

	private Logger logger = LoggerFactory.getLogger(DefaultHttpClient.class);

	@Override
	public HttpResponse get(String url, HttpRequest request) {
		logHttpRequest(url, request);
		CloseableHttpClient client = HttpClientBuilder
				.create()
				.setDefaultCredentialsProvider(
						DefaultHttpClientHelper.getBasicCredentialProvider())
				.build();
		HttpGet getRequest = new HttpGet(url);
		DefaultHttpClientHelper.buildRequestHeaders(request, getRequest);
		try {
		    CloseableHttpResponse httpResponse = client.execute(getRequest);
            HttpEntity responseEntity = httpResponse.getEntity();
            return handleResponse(httpResponse, responseEntity);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public HttpResponse post(String url, HttpRequest request) {
		logHttpRequest(url, request);
		CloseableHttpClient client = HttpClientBuilder
				.create()
				.setDefaultCredentialsProvider(
						DefaultHttpClientHelper.getBasicCredentialProvider())
				.build();
		HttpPost postRequest = new HttpPost(url);
		DefaultHttpClientHelper.buildRequestHeaders(request, postRequest);
		postRequest.setEntity(new StringEntity(request.payload(), "UTF-8"));
		try {
		    CloseableHttpResponse httpResponse = client.execute(postRequest);
            HttpEntity responseEntity = httpResponse.getEntity();
            return handleResponse(httpResponse, responseEntity);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public HttpResponse put(String url, HttpRequest request) {
		logHttpRequest(url, request);
		CloseableHttpClient client = HttpClientBuilder
				.create()
				.setDefaultCredentialsProvider(
						DefaultHttpClientHelper.getBasicCredentialProvider())
				.build();
		HttpPut putRequest = new HttpPut(url);
		DefaultHttpClientHelper.buildRequestHeaders(request, putRequest);
		putRequest.setEntity(new StringEntity(request.payload(), "UTF-8"));
		try {
		    CloseableHttpResponse httpResponse = client.execute(putRequest);
            HttpEntity responseEntity = httpResponse.getEntity();
            return handleResponse(httpResponse, responseEntity);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public HttpResponse delete(String url, HttpRequest request) {
	    logHttpRequest(url, request);
        CloseableHttpClient client = HttpClientBuilder
                .create()
                .setDefaultCredentialsProvider(
                        DefaultHttpClientHelper.getBasicCredentialProvider())
                .build();
        HttpDelete deleteRequest = new HttpDelete(url);
        DefaultHttpClientHelper.buildRequestHeaders(request, deleteRequest);
        try {
            CloseableHttpResponse httpResponse = client.execute(deleteRequest);
            HttpEntity responseEntity = httpResponse.getEntity();
            return handleResponse(httpResponse, responseEntity);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}

    private HttpResponse handleResponse(CloseableHttpResponse httpResponse, HttpEntity responseEntity)
            throws IOException {
        HttpResponse response;
        if(responseEntity != null){ 
            InputStream contentStream = httpResponse.getEntity().getContent();
            response = new HttpResponseImpl(
                    DefaultHttpClientHelper.buildResponseHeaders(httpResponse),
                    contentStream,
                    DefaultHttpClientHelper.buildResult(httpResponse));
        }else{ //e.g. HTTP 204
            response = new HttpResponseImpl(
                    DefaultHttpClientHelper.buildResponseHeaders(httpResponse),
                    IOUtils.toInputStream(""),
                    DefaultHttpClientHelper.buildResult(httpResponse));
        }
        logHttpResponse(response);
        return response;
    }

    private void logHttpRequest(String url, HttpRequest request) {
        if (logger.isInfoEnabled()) {
            String payload = request.payload();
            if (payload != null && !payload.isEmpty()) {
                logger.info("\nURL: {}\nHEADERS: {}\nREQUEST: {}", url,
                        request.headers(),
                        DefaultHttpClientHelper.prettyPrintXml(payload));
            } else {
                logger.info("\nURL: {}\nHEADERS:{}\nNO REQUEST BODY", url,
                        request.headers());
            }
        }
    }

    private void logHttpResponse(HttpResponse response) {
	    if (logger.isInfoEnabled()) {
//	        String payload = IOUtils.toString(response.payload());
//	        if (payload != null && !payload.isEmpty()) {
//	            logger.info("\nHEADERS: {}\nRESPONSE: {}", response.headers(),
//	                    DefaultHttpClientHelper.prettyPrintXml(payload));
//	        } else {
//	            logger.info("\nHEADERS: {}\nNO RESPONSE", response.headers() );
//	        }
	    }
	}
}
