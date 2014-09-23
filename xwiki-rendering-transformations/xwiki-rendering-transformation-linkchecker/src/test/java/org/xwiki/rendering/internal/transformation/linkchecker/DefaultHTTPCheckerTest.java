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

import org.junit.Rule;
import org.junit.Test;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import static com.github.tomakehurst.wiremock.client.RequestPatternBuilder.allRequests;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static org.junit.Assert.*;

/**
 * Integration tests for {@link org.xwiki.rendering.internal.transformation.linkchecker.DefaultHTTPChecker}.
 *
 * @version $Id$
 * @since 6.1M2
 */
public class DefaultHTTPCheckerTest
{
    @Rule
    public WireMockRule proxyWireMockRule = new WireMockRule(8888);

    @Rule
    public MockitoComponentMockingRule<DefaultHTTPChecker> mocker =
        new MockitoComponentMockingRule<>(DefaultHTTPChecker.class);

    @Test
    public void testProxy() throws Exception
    {
        // First call the link checker but since we haven't set up any proxy our Mock HTTP Server is not going to be
        // called (since http://unknownhostforxwikitest will lead to nowhere...
        assertEquals(0, this.mocker.getComponentUnderTest().check("http://unknownhostforxwikitest"));
        assertTrue("The HTTP server was not called by the link checker", findAll(allRequests()).isEmpty());

        // Second, setup a proxy by using System Properties, then call again the checker and this time it should
        // succeed since http://host will go to the proxy which is pointing to our Mock HTTP Server!
        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", "8888");
        assertEquals(404, this.mocker.getComponentUnderTest().check("http://unknownhostforxwikitest"));
        assertFalse("The HTTP server was called by the link checker", findAll(allRequests()).isEmpty());
    }
}
