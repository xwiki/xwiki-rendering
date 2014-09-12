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
package org.xwiki.rendering.macro.jira;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.xwiki.rendering.internal.macro.jira.source.ListJIRADataSource;

/**
 * Unit tests for {@link ListJIRADataSource} and
 * {@link org.xwiki.rendering.internal.macro.jira.source.AbstractJIRADataSource}.
 *
 * @version $Id$
 * @since 4.2M1
 */
public class ListJIRADataSourceTest implements JIRAFields
{
    @Test
    public void testParseIdsWhenNull()
    {
        ListJIRADataSource source = new ListJIRADataSource();
        Assert.assertEquals(Collections.emptyList(), source.parseIds(null));
    }

    @Test
    public void testParseIds()
    {
        ListJIRADataSource source = new ListJIRADataSource();
        List<Pair<String, String>> expected = Arrays.<Pair<String, String>>asList(
            new ImmutablePair<String, String>("ISSUE-1", ""),
            new ImmutablePair<String, String>("ISSUE-2", "Whatever"),
            new ImmutablePair<String, String>("ISSUE-3", ""));
        Assert.assertEquals(expected, source.parseIds("\nISSUE-1\nISSUE-2 |Whatever \n ISSUE-3\n"));
    }

    @Test
    public void testConstructJQLQuery()
    {
        ListJIRADataSource source = new ListJIRADataSource();

        List<Pair<String, String>> ids = Arrays.<Pair<String, String>>asList(
            new ImmutablePair<String, String>("ISSUE-1", ""),
            new ImmutablePair<String, String>("ISSUE-2", "Whatever"));
        Assert.assertEquals("issueKey in (ISSUE-1,ISSUE-2)", source.constructJQLQuery(ids));
    }

    /**
     * Verify several things:
     * <ul>
     *     <li>Issue order is preserved even though JIRA returns them in no specific order</li>
     *     <li>List fields are supported (for example the "version" field)</li>
     *     <li>Notes are taken into account</li>
     * </ul>
     */
    @Test
    public void testBuildIssues() throws Exception
    {
        ListJIRADataSource source = new ListJIRADataSource();

        Document document = new SAXBuilder().build(getClass().getResourceAsStream("/input.xml"));
        List<Pair<String, String>> ids = Arrays.<Pair<String, String>>asList(
            new ImmutablePair<String, String>("XWIKI-1000", ""),
            new ImmutablePair<String, String>("XWIKI-1001", "Note"));

        List<Element> issues = source.buildIssues(document, ids);

        Assert.assertEquals(2, issues.size());
        Element issue1 = issues.get(0);
        Assert.assertEquals("XWIKI-1000", issue1.getChildTextTrim(KEY));
        Assert.assertEquals("Improve PDF Output", issue1.getChildTextTrim(SUMMARY));
        Element issue2 = issues.get(1);
        Assert.assertEquals("XWIKI-1001", issue2.getChildTextTrim(KEY));
        Assert.assertEquals("On jetty, non-default skins are not usable", issue2.getChildTextTrim(SUMMARY));
        Assert.assertEquals("Note", issue2.getChildTextTrim(NOTE));
    }
}
