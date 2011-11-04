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

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.jmock.Expectations;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.observation.ObservationManager;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.rendering.transformation.linkchecker.InvalidURLEvent;
import org.xwiki.rendering.transformation.linkchecker.LinkState;
import org.xwiki.rendering.transformation.linkchecker.LinkStateManager;
import org.xwiki.script.service.ScriptService;
import org.xwiki.test.AbstractMockingComponentTestCase;
import org.xwiki.test.annotation.MockingRequirement;

/**
 * Unit tests for {@link org.xwiki.rendering.internal.transformation.linkchecker.LinkCheckerTransformation}.
 *
 * @version $Id$
 * @since 3.3M1
 */
public class LinkCheckerTransformationTest extends AbstractMockingComponentTestCase
{
    @MockingRequirement(exceptions = {ComponentManager.class})
    private LinkCheckerTransformation transformation;

    private HTTPChecker httpChecker;

    @After
    public void cleanUp()
    {
        // Make sure we stop the Link Checker thread after each test (since it's started automatically when looking
        // up the LinkCheckerTransformation component.
        this.transformation.stopLinkCheckerThread();
    }

    @Override
    public void configure() throws Exception
    {
        this.httpChecker = registerMockComponent(HTTPChecker.class);
    }

    @Test
    public void transform() throws Exception
    {
        String input = ""
            + "whatever"
            + "[[http://ok||class=\"myclass\"]]"
            + "[[invalid]]";

        getMockery().checking(new Expectations()
        {{
            oneOf(httpChecker).check("http://ok"); will(returnValue(200));
            oneOf(httpChecker).check("invalid"); will(returnValue(0));
        }});

        LinkStateManager linkStateManager = getComponentManager().lookup(LinkStateManager.class);
        parseAndwait(input, linkStateManager, 2);

        // Verify we can access the link states through the Script Service
        LinkCheckerScriptService service = 
            (LinkCheckerScriptService) getComponentManager().lookup(ScriptService.class, "linkchecker");
        Map<String, Map<String, LinkState>> states = service.getLinkStates();
        
        LinkState state1 = states.get("http://ok").get("default");
        Assert.assertEquals(200, state1.getResponseCode());
        LinkState state2 = states.get("invalid").get("default");
        Assert.assertEquals(0, state2.getResponseCode());
    }

    /**
     * Verify behavior when a link has already been checked and it's asked to be checked again before the timeout has
     * expired (for performance reasons we only recheck links after a certain timeout).
     */
    @Test
    public void transformWhenExistingLinkState() throws Exception
    {
        // Note: it's important that the first link in the input be the link that we're adding manually to the list
        // of states below since belowe we're waiting to get 2 states before stopping our test. If it were inverted
        // then we would get 2 states before we have time to process the link for which the state already exists.
        String input = ""
            + "[[http://ok]]"
            + "[[http://newok]]";

        // Set some state in the Link State Manager to verify that if an item that is on the queue is the same as one
        // already process not long ago, it's not processed again.
        LinkStateManager linkStateManager = getComponentManager().lookup(LinkStateManager.class);
        Map<String, LinkState> contentReferences = new HashMap<String, LinkState>();
        long initialTime = System.currentTimeMillis();
        contentReferences.put("default", new LinkState(200, initialTime));
        linkStateManager.getLinkStates().put("http://ok", contentReferences);

        final HTTPChecker checker = getComponentManager().lookup(HTTPChecker.class);
        getMockery().checking(new Expectations() {{
            never(checker).check("http://ok");
            oneOf(checker).check("http://newok"); will(returnValue(200));
        }});

        parseAndwait(input, linkStateManager, 2);

        // Verify we can access the link states through the Script Service
        LinkCheckerScriptService service =
            (LinkCheckerScriptService) getComponentManager().lookup(ScriptService.class, "linkchecker");
        Map<String, Map<String, LinkState>> states = service.getLinkStates();

        LinkState state1 = states.get("http://ok").get("default");
        Assert.assertEquals(200, state1.getResponseCode());
        Assert.assertEquals(initialTime, state1.getLastCheckedTime());

        LinkState state2 = states.get("http://newok").get("default");
        Assert.assertEquals(200, state2.getResponseCode());
    }

    @Test
    public void transformWithSourceMetaData() throws Exception
    {
        String input = "[[http://ok]]";
        XDOM xdom = getComponentManager().lookup(Parser.class, "xwiki/2.0").parse(new StringReader(input));

        // Add MetaData Block
        MetaData metaData = new MetaData();
        metaData.addMetaData(MetaData.SOURCE, "source");
        XDOM newXDOM = new XDOM(xdom.getChildren(), metaData);

        final HTTPChecker checker = getComponentManager().lookup(HTTPChecker.class);
        getMockery().checking(new Expectations() {{
            oneOf(checker).check("http://ok"); will(returnValue(200));
        }});

        this.transformation.transform(newXDOM, new TransformationContext());

        LinkStateManager linkStateManager = getComponentManager().lookup(LinkStateManager.class);
        wait(linkStateManager, 1);        

        Assert.assertNotNull(linkStateManager.getLinkStates().get("http://ok").get("source"));
    }

    /**
     * Verify that when the Observation Manager is available we send an InvalidURLEvent event when there's an invalid
     * URL.
     */
    @Test
    public void transformAndSendEvent() throws Exception
    {
        final ObservationManager observationManager = registerMockComponent(ObservationManager.class);
        getMockery().checking(new Expectations()
        {{
            oneOf(httpChecker).check("http://doesntexist"); 
            will(returnValue(404));
            // The test is here: we verify that the event is sent
            oneOf(observationManager).notify(with(equal(new InvalidURLEvent("http://doesntexist"))),
                with(any(Map.class)));
        }});

        LinkStateManager linkStateManager = getComponentManager().lookup(LinkStateManager.class);
        parseAndwait("[[http://doesntexist]]", linkStateManager, 1);
    }

    private void parseAndwait(String input, LinkStateManager linkStateManager, int numberOfItemsToWaitFor)
        throws Exception
    {
        XDOM xdom = getComponentManager().lookup(Parser.class, "xwiki/2.0").parse(new StringReader(input));
        this.transformation.transform(xdom, new TransformationContext());

        // At this point the links have been put on the queue and we're waiting for the Link Checker Thread to
        // process them
        wait(linkStateManager, numberOfItemsToWaitFor);
    }

    private void wait(LinkStateManager linkStateManager, int numberOfItemsToWaitFor) throws Exception
    {
        long time = System.currentTimeMillis();
        while (linkStateManager.getLinkStates().size() != numberOfItemsToWaitFor) {
            Thread.sleep(100L);
            // Protect against infinite loop
            Assert.assertTrue(System.currentTimeMillis() - time < 10000L);
        }
    }
}
