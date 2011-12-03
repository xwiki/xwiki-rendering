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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
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
     */
    private HttpClient httpClient;

    @Override
    public void initialize() throws InitializationException
    {
        this.httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());

        // Set our user agent to be a good citizen.
        this.httpClient.getParams().setParameter(HttpMethodParams.USER_AGENT, "XWikiLinkChecker");
        // Ignore cookies since this can cause errors in logs and we don't need cookies when checking sites.
        this.httpClient.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
    }

    @Override
    public int check(String url)
    {
        int responseCode;
        GetMethod method = new GetMethod(url);

        try {
            // Execute the method.
            responseCode = this.httpClient.executeMethod(method);

            this.logger.debug("Result of pinging [{}]: code = [{}]", url, responseCode);
        } catch (Exception e) {
            // Some error in the transport or in the passed URL, use a special response code (0) which isn't in the
            // list of allowed response codes, see http://en.wikipedia.org/wiki/List_of_HTTP_status_codes
            responseCode = 0;
            this.logger.debug("Error while checking [{}]", url, e);
        } finally {
            method.releaseConnection();
        }
        return responseCode;
    }
}
