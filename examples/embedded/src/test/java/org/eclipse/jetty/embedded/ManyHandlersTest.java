//
//  ========================================================================
//  Copyright (c) 1995-2021 Mort Bay Consulting Pty Ltd and others.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.embedded;

import java.net.URI;
import java.util.Map;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.ajax.JSON;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class ManyHandlersTest extends AbstractEmbeddedTest
{
    private Server server;

    @BeforeEach
    public void startServer() throws Exception
    {
        server = ManyHandlers.createServer(0);
        server.start();
    }

    @AfterEach
    public void stopServer() throws Exception
    {
        server.stop();
    }

    @Test
    public void testGetParams() throws Exception
    {
        URI uri = server.getURI().resolve("/params?a=b&foo=bar");

        ContentResponse response = client.newRequest(uri)
            .method(HttpMethod.GET)
            .header(HttpHeader.ACCEPT_ENCODING, "gzip")
            .send();
        assertThat("HTTP Response Status", response.getStatus(), is(HttpStatus.OK_200));

        // dumpResponseHeaders(response);

        // test gzip
        // Test that Gzip was used to produce the response
        String contentEncoding = response.getHeaders().get(HttpHeader.CONTENT_ENCODING);
        assertThat("Content-Encoding", contentEncoding, containsString("gzip"));

        // test response content
        String responseBody = response.getContentAsString();
        Object jsonObj = JSON.parse(responseBody);
        Map jsonMap = (Map)jsonObj;
        assertThat("Response JSON keys.size", jsonMap.keySet().size(), is(2));
    }

    @Test
    public void testGetHello() throws Exception
    {
        URI uri = server.getURI().resolve("/hello");
        ContentResponse response = client.newRequest(uri)
            .method(HttpMethod.GET)
            .header(HttpHeader.ACCEPT_ENCODING, "gzip")
            .send();
        assertThat("HTTP Response Status", response.getStatus(), is(HttpStatus.OK_200));

        // dumpResponseHeaders(response);

        // test gzip
        // Test that Gzip was used to produce the response
        String contentEncoding = response.getHeaders().get(HttpHeader.CONTENT_ENCODING);
        assertThat("Content-Encoding", contentEncoding, containsString("gzip"));

        // test expected header from wrapper
        String welcome = response.getHeaders().get("X-Welcome");
        assertThat("X-Welcome header", welcome, containsString("Greetings from WelcomeWrapHandler"));

        // test response content
        String responseBody = response.getContentAsString();
        assertThat("Response Content", responseBody, containsString("Hello"));
    }
}
