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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.States;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.xwiki.observation.ObservationManager;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.transformation.Transformation;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.rendering.transformation.linkchecker.InvalidURLEvent;
import org.xwiki.rendering.transformation.linkchecker.LinkCheckerTransformationConfiguration;
import org.xwiki.rendering.transformation.linkchecker.LinkContextDataProvider;
import org.xwiki.rendering.transformation.linkchecker.LinkState;
import org.xwiki.rendering.transformation.linkchecker.LinkStateManager;
import org.xwiki.script.service.ScriptService;
import org.xwiki.test.AbstractComponentTestCase;

/**
 * Unit tests for {@link org.xwiki.rendering.internal.transformation.linkchecker.LinkCheckerTransformation}.
 *
 * @version $Id$
 * @since 3.3M1
 */
public class LinkCheckerTransformationTest extends AbstractComponentTestCase
{
    @After
    public void cleanUp() throws Exception
    {
        // Make sure we stop the Link Checker thread after each test (since it's started automatically when looking
        // up the LinkCheckerTransformation component.
        Transformation transformation = getComponentManager().getInstance(Transformation.class, "linkchecker");
        ((LinkCheckerTransformation) transformation).stopLinkCheckerThread();
    }

    @Test
    public void transform() throws Exception
    {
        String input = ""
            + "whatever"
            + "[[http://ok||class=\"myclass\"]]"
            + "[[invalid]]";

        final HTTPChecker httpChecker = registerMockComponent(HTTPChecker.class);
        getMockery().checking(new Expectations()
        {{
            oneOf(httpChecker).check("http://ok"); will(returnValue(200));
            oneOf(httpChecker).check("invalid"); will(returnValue(0));
        }});

        LinkStateManager linkStateManager = getComponentManager().getInstance(LinkStateManager.class);
        parseAndwait(input, linkStateManager, 2);

        // Verify we can access the link states through the Script Service
        LinkCheckerScriptService service = 
            (LinkCheckerScriptService) getComponentManager().getInstance(ScriptService.class, "linkchecker");
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
        // of states below since below we're waiting to get 2 states before stopping our test. If it were inverted
        // then we would get 2 states before we have time to process the link for which the state already exists.
        String input = ""
            + "[[http://ok]]"
            + "[[http://newok]]";

        // Set some state in the Link State Manager to verify that if an item that is on the queue is the same as one
        // already process not long ago, it's not processed again.
        LinkStateManager linkStateManager = getComponentManager().getInstance(LinkStateManager.class);
        Map<String, LinkState> contentReferences = new HashMap<String, LinkState>();
        long initialTime = System.currentTimeMillis();
        contentReferences.put("default", new LinkState(200, initialTime));
        linkStateManager.getLinkStates().put("http://ok", contentReferences);

        final HTTPChecker httpChecker = registerMockComponent(HTTPChecker.class);
        getMockery().checking(new Expectations() {{
            never(httpChecker).check("http://ok");
            oneOf(httpChecker).check("http://newok"); will(returnValue(200));
        }});

        parseAndwait(input, linkStateManager, 2);

        // Verify we can access the link states through the Script Service
        LinkCheckerScriptService service =
            (LinkCheckerScriptService) getComponentManager().getInstance(ScriptService.class, "linkchecker");
        Map<String, Map<String, LinkState>> states = service.getLinkStates();

        LinkState state1 = states.get("http://ok").get("default");
        Assert.assertEquals(200, state1.getResponseCode());
        Assert.assertEquals(initialTime, state1.getLastCheckedTime());

        LinkState state2 = states.get("http://newok").get("default");
        Assert.assertEquals(200, state2.getResponseCode());
    }

    /**
     * Verify behavior when a link has already been checked and it's asked to be checked again but after the timeout
     * has expired.
     */
    @Test
    public void transformWhenExistingLinkStateButAfterTimeoutHasExpired() throws Exception
    {
        // Note: it's important that the first link in the input be the link that we're updating below since below
        // we're waiting to get 2 states before stopping our test. If it were inverted then we would get 2 states
        // before we have time to process the link for which the state already exists.
        String input = ""
            + "[[http://ok]]"
            + "[[http://newok]]";

        // Set some state in the Link State Manager to verify that if an item that is on the queue is the same as one
        // already process not long ago, it's not processed again.
        LinkStateManager linkStateManager = getComponentManager().getInstance(LinkStateManager.class);
        Map<String, LinkState> contentReferences = new HashMap<String, LinkState>();
        long initialTime = System.currentTimeMillis();
        contentReferences.put("default", new LinkState(404, initialTime));
        linkStateManager.getLinkStates().put("http://ok", contentReferences);

        final HTTPChecker httpChecker = registerMockComponent(HTTPChecker.class);
        getMockery().checking(new Expectations() {{
            oneOf(httpChecker).check("http://newok"); will(returnValue(200));
            oneOf(httpChecker).check("http://ok"); will(returnValue(200));
        }});

        // Modify the default timeout so that we don't have to wait too long for the test...
        LinkCheckerTransformationConfiguration configuration =
            getComponentManager().getInstance(LinkCheckerTransformationConfiguration.class);
        configuration.setCheckTimeout(0L);

        parseAndwait(input, linkStateManager, 2);

        // We've put a timeout of 0ms but to be on the safe side we wait 1ms (since otherwise it could be possible
        // that the above executes in less than 1ms.
        Thread.sleep(1L);
        
        // Verify we can access the link states through the Script Service
        LinkCheckerScriptService service =
            (LinkCheckerScriptService) getComponentManager().getInstance(ScriptService.class, "linkchecker");
        Map<String, Map<String, LinkState>> states = service.getLinkStates();

        LinkState state = states.get("http://ok").get("default");
        Assert.assertEquals(200, state.getResponseCode());
    }

    @Test
    public void transformWithSourceMetaData() throws Exception
    {
        String input = "[[http://ok]]";
        Parser parser = getComponentManager().getInstance(Parser.class, "xwiki/2.0");
        XDOM xdom = parser.parse(new StringReader(input));

        // Add MetaData Block
        MetaData metaData = new MetaData();
        metaData.addMetaData(MetaData.SOURCE, "source");
        XDOM newXDOM = new XDOM(xdom.getChildren(), metaData);

        final HTTPChecker httpChecker = registerMockComponent(HTTPChecker.class);
        getMockery().checking(new Expectations() {{
            oneOf(httpChecker).check("http://ok"); will(returnValue(200));
        }});

        Transformation transformation = getComponentManager().getInstance(Transformation.class, "linkchecker");
        transformation.transform(newXDOM, new TransformationContext());

        LinkStateManager linkStateManager = getComponentManager().getInstance(LinkStateManager.class);
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
        final States eventState = getMockery().states("event");

        final ObservationManager observationManager = registerMockComponent(ObservationManager.class);
        final HTTPChecker httpChecker = registerMockComponent(HTTPChecker.class);
        getMockery().checking(new Expectations()
        {{
            oneOf(httpChecker).check("http://doesntexist");
            will(returnValue(404));
            // The real test is here: we verify that the event is sent
            oneOf(observationManager).notify(with(equal(new InvalidURLEvent("http://doesntexist"))),
                with(any(Map.class)));
            then(eventState.is("ok"));
        }});

        LinkStateManager linkStateManager = getComponentManager().getInstance(LinkStateManager.class);
        parseAndwait("[[http://doesntexist]]", linkStateManager, 1);

        // Wait till the event has been sent since parseAndWait only waits for the link state to be in cache but it
        // doesn't wait for the event to be sent.
        while (!eventState.is("ok").isActive()) {
            Thread.sleep(100L);
        }
    }

    /**
     * Verify that if a LinkContextDataProvider is available it's used to store context data in the LinkStateManager.
     */
    @Test
    public void transformWithLinkContextDataProvider() throws Exception
    {
        String input = "[[http://ok]]";

        final HTTPChecker httpChecker = registerMockComponent(HTTPChecker.class);
        getMockery().checking(new Expectations() {{
            oneOf(httpChecker).check("http://ok"); will(returnValue(200));
        }});

        // Register a LinkContextDataProvider component
        final LinkContextDataProvider linkContextDataProvider = registerMockComponent(LinkContextDataProvider.class);
        getMockery().checking(new Expectations() {{
            oneOf(linkContextDataProvider).getContextData("http://ok", "default");
            will(returnValue(Collections.singletonMap("contextKey", "contextValue")));
        }});

        LinkStateManager linkStateManager = getComponentManager().getInstance(LinkStateManager.class);
        parseAndwait(input, linkStateManager, 1);
        
        // Assert states
        LinkState linkState = linkStateManager.getLinkStates().get("http://ok").get("default");
        Assert.assertEquals("contextValue", linkState.getContextData().get("contextKey"));
    }
    
    private void parseAndwait(String input, LinkStateManager linkStateManager, int numberOfItemsToWaitFor)
        throws Exception
    {
        Transformation transformation = getComponentManager().getInstance(Transformation.class, "linkchecker");
        Parser xwiki20Parser = getComponentManager().getInstance(Parser.class, "xwiki/2.0");
        XDOM xdom = xwiki20Parser.parse(new StringReader(input));
        transformation.transform(xdom, new TransformationContext());

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
            Assert.assertTrue("Killed thread since it took too much time", System.currentTimeMillis() - time < 10000L);
        }
    }
}
