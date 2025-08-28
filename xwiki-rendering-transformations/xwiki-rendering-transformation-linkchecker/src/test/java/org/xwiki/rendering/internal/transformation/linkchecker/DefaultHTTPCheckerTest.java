/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.rendering.internal.transformation.linkchecker;

import org.junit.jupiter.api.Test;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.allRequests;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link org.xwiki.rendering.internal.transformation.linkchecker.DefaultHTTPChecker}.
 *
 * @version $Id$
 * @since 6.1M2
 */
@ComponentTest
@WireMockTest(httpPort = 8888)
class DefaultHTTPCheckerTest
{
    @InjectMockComponents
    private DefaultHTTPChecker checker;

    @Test
    void proxy()
    {
        // First call the link checker but since we haven't set up any proxy our Mock HTTP Server is not going to be
        // called (since http://unknownhostforxwikitest will lead to nowhere...
        assertEquals(0, this.checker.check("http://unknownhostforxwikitest"));
        assertTrue(findAll(allRequests()).isEmpty(), "The HTTP server was not called by the link checker");

        // Second, setup a proxy by using System Properties, then call again the checker and this time it should
        // succeed since http://host will go to the proxy which is pointing to our Mock HTTP Server!
        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", "8888");

        stubFor(get(urlEqualTo("/")).willReturn(notFound()));

        assertEquals(404, this.checker.check("http://unknownhostforxwikitest"));
        assertFalse(findAll(allRequests()).isEmpty(), "The HTTP server was called by the link checker");
    }
}
