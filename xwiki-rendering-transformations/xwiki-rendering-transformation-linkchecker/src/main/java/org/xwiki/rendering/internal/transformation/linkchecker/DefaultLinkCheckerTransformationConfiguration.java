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

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.rendering.transformation.linkchecker.LinkCheckerTransformationConfiguration;

/**
 * Default implementation, by default rechecks for link validity every hour.
 *
 * @version $Id$
 * @since 3.3M2
 */
@Component
@Singleton
public class DefaultLinkCheckerTransformationConfiguration implements LinkCheckerTransformationConfiguration
{
    /**
     * Prefix for configuration keys for the Icon transformation module.
     */
    private static final String PREFIX = "rendering.transformation.linkchecker.";

    /**
     * Wait 1 hour before rechecking a link.
     */
    private static final long TIMEOUT = 3600000L;

    /**
     * Overridden default timeout to be used if set.
     */
    private Long checkTimeout;

    /**
     * Used to dynamically lookup a ConfigurationSource implementation since we want to make it work even if there's
     * none available.
     */
    @Inject
    private ComponentManager componentManager;

    @Override
    public long getCheckTimeout()
    {
        long result;

        if (this.checkTimeout != null) {
            result = this.checkTimeout;
        } else {
            ConfigurationSource source;
            try {
                source = this.componentManager.lookup(ConfigurationSource.class);
                result = source.getProperty(PREFIX + "timeout", TIMEOUT);
            } catch (ComponentLookupException e) {
                result = TIMEOUT;
            }
        }

        return result;
    }

    /**
     * @param checkTimeout the time after which a link should be checked again for validity
     */
    @Override
    public void setCheckTimeout(long checkTimeout)
    {
        this.checkTimeout = checkTimeout;
    }
}
