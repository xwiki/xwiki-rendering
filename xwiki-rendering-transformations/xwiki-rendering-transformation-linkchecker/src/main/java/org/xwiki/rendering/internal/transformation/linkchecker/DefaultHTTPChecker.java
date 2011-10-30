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

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
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
            // Don't retry automatically since we want it to be as fast as possible to not slow down the rendering
            // process.
            method.getParams()
                .setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
            // Sets the socket timeout (SO_TIMEOUT) in milliseconds to be used when executing the method.
            // We use a small value since we don't want to slow down the rendering process.
            method.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, new Integer(1000));

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
