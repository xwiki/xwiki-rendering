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
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;

/**
 * Default implementation using Apache Http Client.
 *
 * @version $Id$
 * @since 3.3M1
 */
@Component
@Singleton
public class DefaultHTTPChecker implements HTTPChecker
{
    /**
     * The logger to log.
     */
    @Inject
    private Logger logger;

    @Override
    public int check(String url)
    {
        int responseCode;

        HttpClient client = new HttpClient();
        try {
            GetMethod method = new GetMethod(url);
            // Ignore cookies since this can cause errors in logs and we don't need cookies when checking sites.
            method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);

            // Execute the method.
            responseCode = client.executeMethod(method);

            this.logger.debug("Result of pinging [{}]: code = [{}]", url, responseCode);
        } catch (Exception e) {
            // Some error in the transport or in the passed URL, use a special response code (0) which isn't in the
            // list of allowed response codes, see http://en.wikipedia.org/wiki/List_of_HTTP_status_codes
            responseCode = 0;
            this.logger.debug("Error while checking [{}]", url, e);
        }
        return responseCode;
    }
}
