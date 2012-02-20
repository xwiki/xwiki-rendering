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
package org.xwiki.rendering.wikimodel.util;

import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.WikiReference;
import org.xwiki.rendering.wikimodel.WikiReferenceParser;

import junit.framework.TestCase;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class ReferenceUtilTest extends TestCase
{
    public ReferenceUtilTest(String name)
    {
        super(name);
    }

    public void test()
    {
        test("a", "a");
        test("   a   ", "a");
        test("a|b", "a", "b");
        test("   a |   b   ", "a", "b");
        test("a>b", "a", "b");
        test("   a >   b   ", "a", "b");
        test("   a   >   b | x=y  ", "a", "b", "x=y");
        test("   a   >   b > x=y  ", "a", "b", "x=y");
        test("   a   >   b > x=y > toto titi ", "a", "b", "x=y");
    }

    private void test(String str, String link)
    {
        WikiReference ref = new WikiReferenceParser().parse(str);
        assertEquals(new WikiReference(link), ref);
    }

    private void test(String str, String link, String label)
    {
        WikiReference ref = new WikiReferenceParser().parse(str);
        assertEquals(new WikiReference(link, label), ref);
    }

    private void test(String str, String link, String label, String params)
    {
        WikiReference ref = new WikiReferenceParser().parse(str);
        assertEquals(
            new WikiReference(link, label, new WikiParameters(params)),
            ref);
    }
}
