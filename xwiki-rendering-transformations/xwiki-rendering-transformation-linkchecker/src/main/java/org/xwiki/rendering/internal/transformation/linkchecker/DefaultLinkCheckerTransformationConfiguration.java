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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.configuration.internal.MemoryConfigurationSource;
import org.xwiki.rendering.transformation.linkchecker.LinkCheckerTransformationConfiguration;

/**
 * Default implementation, by default rechecks for link validity every hour.
 *
 * @version $Id$
 * @since 3.3M2
 */
@Component
@Singleton
public class DefaultLinkCheckerTransformationConfiguration implements LinkCheckerTransformationConfiguration,
    Initializable
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

    private ConfigurationSource configurationSource;

    @Override
    public void initialize() throws InitializationException
    {
        try {
            this.configurationSource = this.componentManager.getInstance(ConfigurationSource.class);
        } catch (ComponentLookupException e) {
            this.configurationSource = new MemoryConfigurationSource();
        }
    }

    @Override
    public long getCheckTimeout()
    {
        long result;

        if (this.checkTimeout != null) {
            result = this.checkTimeout;
        } else {
            result = this.configurationSource.getProperty(PREFIX + "timeout", TIMEOUT);
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

    /**
     * Allows extending classes to override it.
     *
     * @return the list of page reference patterns to exclude from link checking
     */
    protected List<Pattern> getDefaultExcludedReferencePatterns()
    {
        return Collections.emptyList();
    }

    @Override
    public List<Pattern> getExcludedReferencePatterns()
    {
        List<Pattern> patterns;

        List<String> patternsAsString =
            this.configurationSource.getProperty(PREFIX + "excludedReferencePatterns", List.class);
        if (patternsAsString.isEmpty()) {
            patterns = getDefaultExcludedReferencePatterns();
        } else {
            patterns = new ArrayList<Pattern>();
            for (String patternAsString : patternsAsString) {
                patterns.add(Pattern.compile(patternAsString));
            }
        }

        return patterns;
    }
}
