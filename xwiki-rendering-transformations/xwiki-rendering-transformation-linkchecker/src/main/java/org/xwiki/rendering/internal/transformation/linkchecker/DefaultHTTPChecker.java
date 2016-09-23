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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;

/**
 * Default implementation using Apache Http Client.
 *
 * @version $Id$
 * @since 3.3M1
 */
@Component
@Singleton
public class DefaultHTTPChecker implements HTTPChecker, Initializable
{
    /**
     * The logger to log.
     */
    @Inject
    private Logger logger;

    /**
     * The client to connect to the remote site using HTTP.
     * <p>
     * Note: If one day we wish to configure timeouts, here's some good documentation about it:
     * http://brian.olore.net/wp/2009/08/apache-httpclient-timeout/
     */
    private CloseableHttpClient httpClient;

    @Override
    public void initialize() throws InitializationException
    {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        // Make the Http Client reusable by several threads
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        httpClientBuilder.setConnectionManager(connectionManager);

        // Pre-configure with everything configured at JVM level (e.g. proxy setup).
        httpClientBuilder.useSystemProperties();

        // Set our user agent to be a good citizen.
        httpClientBuilder.setUserAgent("XWikiLinkChecker");

        // Ignore cookies since this can cause errors in logs and we don't need cookies when checking sites.
        RequestConfig config = RequestConfig.custom()
            .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
            .build();
        httpClientBuilder.setDefaultRequestConfig(config);

        this.httpClient = httpClientBuilder.build();
    }

    @Override
    public int check(String url)
    {
        int responseCode;

        CloseableHttpResponse httpResponse = null;
        try {
            HttpGet httpGet = new HttpGet(url);
            httpResponse = this.httpClient.execute(httpGet);
            responseCode = httpResponse.getStatusLine().getStatusCode();
            this.logger.debug("Result of pinging [{}]: code = [{}]", url, responseCode);
        } catch (Exception e) {
            // Some error in the transport or in the passed URL, use a special response code (0) which isn't in the
            // list of allowed response codes, see http://en.wikipedia.org/wiki/List_of_HTTP_status_codes
            responseCode = 0;
            this.logger.debug("Error while checking [{}]", url, e);
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (Exception ee) {
                    // Failed to close, ignore but log the error.
                    this.logger.error("Failed to close HTTP connection for [{}]", url, ee);
                }
            }
        }

        return responseCode;
    }
}
