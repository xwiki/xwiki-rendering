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

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite("Test for WikiModel grammars");
        // $JUnit-BEGIN$
        suite.addTestSuite(WikiScannerUtilTest.class);
        suite.addTestSuite(CreoleWikiParserTest.class);
        suite.addTestSuite(ConfluenceWikiParserTest.class);
        suite.addTestSuite(GWikiParserTest.class);
        suite.addTestSuite(JspWikiParserTest.class);
        suite.addTestSuite(MediawikiParserTest.class);
        suite.addTestSuite(XWiki20ParserTest.class);
        suite.addTestSuite(CommonWikiParserTest.class);
        // $JUnit-END$
        return suite;
    }
}
