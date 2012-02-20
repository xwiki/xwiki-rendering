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
package org.xwiki.rendering.wikimodel.test;

import java.util.List;

import org.xwiki.rendering.wikimodel.IWemConstants;
import org.xwiki.rendering.wikimodel.WikiFormat;
import org.xwiki.rendering.wikimodel.WikiStyle;

import junit.framework.TestCase;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class WikiFormatTest extends TestCase
{
    public void testOrder()
    {
        WikiFormat format = new WikiFormat(IWemConstants.STRONG);
        format = format.addStyle(IWemConstants.EM);

        List<WikiStyle> styles = format.getStyles();
        assertEquals(IWemConstants.STRONG, styles.get(0));
        assertEquals(IWemConstants.EM, styles.get(1));

        format = new WikiFormat(IWemConstants.EM);
        format = format.addStyle(IWemConstants.STRONG);

        styles = format.getStyles();
        assertEquals(IWemConstants.EM, styles.get(0));
        assertEquals(IWemConstants.STRONG, styles.get(1));
    }
}
