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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jdom2.Document;
import org.jdom2.Element;
import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.jira.JIRAFields;
import org.xwiki.rendering.macro.jira.JIRAMacroParameters;

/**
 * Takes a discrete list of JIRA issues from the Macro content and return their field values.
 *
 * The format is one issue id per line, ignoring any data after the pipe symbol (used to add some notes).
 * <p/>
 * Example:
 * <code><pre>
 *   ISSUE-1
 *   ISSUE-2|Whatever here
 *   ISSUE-3
 * </pre></code>
 *
 * @version $Id$
 * @since 4.2M1
 */
@Component
@Named("list")
@Singleton
public class ListJIRADataSource extends AbstractJIRADataSource
{
    /**
     * The symbol used to separate the issue id from a user-specified note.
     */
    private static final String PIPE = "|";

    @Override
    public Collection<Element> getData(String macroContent, JIRAMacroParameters parameters)
        throws MacroExecutionException
    {
        List<Pair<String, String>> ids = parseIds(macroContent);
        if (ids.isEmpty()) {
            throw new MacroExecutionException("Empty list of JIRA ids!");
        }

        Document document = getXMLDocument(parameters.getURL(), constructJQLQuery(ids));
        return buildIssues(document, ids);
    }

    /**
     * @param document the XML document containing all the JIRA issues and which was returned by the JIRA server
     * @param issueIds the list of JIRA issue ids specified by the user
     * @return the list of JIRA issues (returned as XML elements), in the same order as the JIRA issue id list
     *         specified by the user
     */
    public List<Element> buildIssues(Document document, List<Pair<String, String>> issueIds)
    {
        // Note: JIRA doesn't return items in the order specified in the JQL query, thus we need to manually order
        // them in the same order as passed in the issueIds parameter.
        Map<String, Element> mappedIssues = buildIssues(document);

        List<Element> issues = new ArrayList<Element>();
        for (Pair<String, String> id : issueIds) {
            Element issue = mappedIssues.get(id.getLeft());
            if (issue != null) {
                // Add the Note field if there's one specified by the user
                String note = id.getRight();
                if (!StringUtils.isBlank(note)) {
                    Element noteElement = new Element(JIRAFields.NOTE);
                    noteElement.addContent(note);
                    issue.addContent(noteElement);
                }
                issues.add(issue);
            }
        }

        return issues;
    }

    /**
     * @param ids the list of JIRA issue ids specified by the user
     * @return the JQL query that will return all JIRA issues specified by the user
     */
    public String constructJQLQuery(List<Pair<String, String>> ids)
    {
        StringBuffer buffer = new StringBuffer("issueKey in (");
        Iterator<Pair<String, String>> it = ids.iterator();
        while (it.hasNext()) {
            buffer.append(it.next().getLeft());
            if (it.hasNext()) {
                buffer.append(',');
            }
        }
        buffer.append(')');
        return buffer.toString();
    }

    /**
     * @param macroContent the macro content listing JIRA issue ids and optional notes
     * @return the list of issue ids and optional notes specified in the macro content
     */
    public List<Pair<String, String>> parseIds(String macroContent)
    {
        List<Pair<String, String>> ids = new ArrayList<Pair<String, String>>();
        if (macroContent != null) {
            for (String issueLine : macroContent.split("\\r?\\n")) {
                // Split on pipe symbol
                String issue = StringUtils.substringBefore(issueLine, PIPE).trim();
                String note = StringUtils.substringAfter(issueLine, PIPE).trim();
                // Only add if the issue is not empty
                if (StringUtils.isNotBlank(issue)) {
                    ids.add(new ImmutablePair<String, String>(issue, note));
                }
            }
        }
        return ids;
    }
}
