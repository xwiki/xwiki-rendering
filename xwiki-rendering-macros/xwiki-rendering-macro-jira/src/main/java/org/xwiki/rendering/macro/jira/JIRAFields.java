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

/**
 * Definition of JIRA field ids.
 *
 * @version $Id$
 * @since 4.2M1
 */
public interface JIRAFields
{
    /**
     * JIRA Summary field.
     */
    String SUMMARY = "summary";

    /**
     * JIRA Key field (eg "XWIKI-1000").
     */
    String KEY = "key";

    /**
     * JIRA Type field (eg Bug, Improvement, etc).
     */
    String TYPE = "type";

    /**
     * JIRA Status field (eg Closed, Open, etc).
     */
    String STATUS = "status";

    /**
     * JIRA Assignee field (the person assigned to fix the issue).
     */
    String ASSIGNEE = "assignee";

    /**
     * JIRA Reporter field (the person who reported the issue).
     */
    String REPORTER = "reporter";

    /**
     * JIRA Created date field (the date the issue was created).
     */
    String CREATED = "created";

    /**
     * JIRA Updated date field (the date the issue was last modified).
     */
    String UPDATED = "updated";

    /**
     * JIRA Resolved date field (the date the issue was resolved).
     */
    String RESOLVED = "resolved";

    /**
     * JIRA Fix Version field (the version in which the issue was resolved or closed).
     */
    String FIXVERSION = "fixVersion";

    /**
     * JIRA Affected Versions field (the list of Versions for which the issue was reported).
     */
    String VERSION = "version";

    /**
     * JIRA Component field (the list of domains/categories for the issue).
     */
    String COMPONENT = "component";

    /**
     * JIRA Vote field (the number of votes for the issue).
     */
    String VOTES = "votes";

    /**
     * JIRA Resolution field (eg Closed, Won't Fix, Duplicate; etc).
     */
    String RESOLUTION = "resolution";

    /**
     * JIRA link field (the URL to the issue on the JIRA instance).
     */
    String LINK = "link";

    /**
     * Special field used by the List Data Source which allows the user to define notes for a given issue.
     */
    String NOTE = "note";
}
