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
package org.xwiki.rendering.internal.macro.jira.source;

import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.jira.JIRADataSource;
import org.xwiki.rendering.macro.jira.JIRAFields;

/**
 * Common implementation for JIRA Data Source that knowns how to execute a JQL query on a JIRA instance and retrieve the
 * list of matching JIRA issues.
 *
 * @version $Id$
 * @since 4.2M1
 */
public abstract class AbstractJIRADataSource implements JIRADataSource
{
    /**
     * URL Prefix to use to build the full JQL URL (doesn't contain the JQL query itself which needs to be appended).
     */
    private static final String JQL_URL_PREFIX =
        "/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?jqlQuery=";

    /**
     * SAX Builder to use to read the JIRA data.
     */
    private SAXBuilder saxBuilder = new SAXBuilder();

    /**
     * @param document the XML document from which to extract JIRA issues
     * @return the list of XML Elements for each JIRA issue, indexed in a Map with the issue id as the key
     */
    protected Map<String, Element> buildIssues(Document document)
    {
        Map<String, Element> issues = new LinkedHashMap<String, Element>();
        for (Element item : document.getRootElement().getChild("channel").getChildren("item")) {
            issues.put(item.getChildText(JIRAFields.KEY), item);
        }
        return issues;
    }

    /**
     * @param jiraURL the JIRA URL prefix (eg "http://jira.xwiki.org")
     * @param jqlQuery the JQL query to execute
     * @return the XML document containing the matching JIRA issues
     * @throws MacroExecutionException if the JIRA issues cannot be retrieved
     */
    public Document getXMLDocument(String jiraURL, String jqlQuery) throws MacroExecutionException
    {
        Document document;
        try {
            // Note: we encode using UTF8 since it's the W3C recommendation.
            // See http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars
            document = this.saxBuilder.build(new URL(String.format("%s%s%s", jiraURL, JQL_URL_PREFIX,
                URLEncoder.encode(jqlQuery, "UTF-8"))));
        } catch (Exception e) {
            throw new MacroExecutionException(String.format("Failed to retrieve JIRA data from [%s] for JQL [%s]",
                jiraURL, jqlQuery), e);
        }
        return document;
    }
}
