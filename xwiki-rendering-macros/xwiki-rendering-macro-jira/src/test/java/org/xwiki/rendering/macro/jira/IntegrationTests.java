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

import java.net.URL;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.runner.RunWith;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.rendering.test.integration.RenderingTestSuite;

/**
 * Run all tests found in {@code *.test} files located in the classpath. These {@code *.test} files must follow the
 * conventions described in {@link org.xwiki.rendering.test.integration.TestDataParser}.
 *
 * @version $Id$
 * @since 4.2M1
 */
@RunWith(RenderingTestSuite.class)
public class IntegrationTests
{
    @RenderingTestSuite.Initialized
    public void initialize(ComponentManager componentManager) throws Exception
    {
        Mockery mockery = new JUnit4Mockery();
        // SAXBuilder is a class and not an interface which is why we need to set up the Class Imposteriser
        mockery.setImposteriser(ClassImposteriser.INSTANCE);

        // Replace the SAXBuilder used in the Source implementations for hint "list" by a mock so that we don't
        // go out on the internet and thus control our test environment.

        SAXBuilder localSaxBuilder = new SAXBuilder();
        final Document document = localSaxBuilder.build(getClass().getResourceAsStream("/input.xml"));

        final SAXBuilder saxBuilder = mockery.mock(SAXBuilder.class);
        mockery.checking(new Expectations() {{
            allowing(saxBuilder).build(new URL(
                "http://localhost/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?jqlQuery="
                + "issueKey+in+%28XWIKI-1000%2CXWIKI-1001%29"));
            will(returnValue(document));
        }});

        JIRADataSource listDataSource = componentManager.getInstance(JIRADataSource.class, "list");
        ReflectionUtils.setFieldValue(listDataSource, "saxBuilder", saxBuilder);

        JIRADataSource jqlDataSource = componentManager.getInstance(JIRADataSource.class, "jql");
        ReflectionUtils.setFieldValue(jqlDataSource, "saxBuilder", saxBuilder);
    }
}
