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

import org.junit.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
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
import org.xwiki.rendering.transformation.linkchecker.script.LinkCheckerScriptService;
import org.xwiki.script.service.ScriptService;
import org.xwiki.test.LogRule;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.mockito.MockitoComponentManagerRule;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link LinkCheckerTransformation}.
 *
 * @version $Id$
 * @since 3.3M1
 */
@AllComponents
public class LinkCheckerTransformationTest
{
    @Rule
    public LogRule logRule = new LogRule() {{
        record(LogLevel.ERROR);
        recordLoggingForType(DefaultLinkCheckerThread.class);
    }};

    @Rule
    public MockitoComponentManagerRule componentManager = new MockitoComponentManagerRule();

    @After
    public void cleanUp() throws Exception
    {
        // Make sure we stop the Link Checker thread after each test (since it's started automatically when looking
        // up the LinkCheckerTransformation component.
        Transformation transformation = this.componentManager.getInstance(Transformation.class, "linkchecker");
        ((LinkCheckerTransformation) transformation).stopLinkCheckerThread();
    }

    @Test
    public void transform() throws Exception
    {
        String input = ""
            + "whatever"
            + "[[http://ok||class=\"myclass\"]]"
            + "[[invalid]]"
            + "[[unsupportedrotocol://invalid]]";

        HTTPChecker httpChecker = this.componentManager.registerMockComponent(HTTPChecker.class);
        when(httpChecker.check("http://ok")).thenReturn(200);
        when(httpChecker.check("invalid")).thenReturn(0);

        LinkStateManager linkStateManager = this.componentManager.getInstance(LinkStateManager.class);
        transformAndWait(input, linkStateManager, 2);

        // Verify we can access the link states through the Script Service
        LinkCheckerScriptService service = this.componentManager.getInstance(ScriptService.class, "linkchecker");
        Map<String, Map<String, LinkState>> states = service.getLinkStates();

        LinkState state1 = states.get("http://ok").get("default");
        assertEquals(200, state1.getResponseCode());
        LinkState state2 = states.get("invalid").get("default");
        assertEquals(0, state2.getResponseCode());
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
        LinkStateManager linkStateManager = this.componentManager.getInstance(LinkStateManager.class);
        Map<String, LinkState> contentReferences = new HashMap<>();
        long initialTime = System.currentTimeMillis();
        contentReferences.put("default", new LinkState(200, initialTime));
        linkStateManager.getLinkStates().put("http://ok", contentReferences);

        HTTPChecker httpChecker = this.componentManager.registerMockComponent(HTTPChecker.class);
        verify(httpChecker, never()).check("http://ok");
        when(httpChecker.check("http://newok")).thenReturn(200);

        transformAndWait(input, linkStateManager, 2);

        // Verify we can access the link states through the Script Service
        LinkCheckerScriptService service = this.componentManager.getInstance(ScriptService.class, "linkchecker");
        Map<String, Map<String, LinkState>> states = service.getLinkStates();

        LinkState state1 = states.get("http://ok").get("default");
        assertEquals(200, state1.getResponseCode());
        assertEquals(initialTime, state1.getLastCheckedTime());

        LinkState state2 = states.get("http://newok").get("default");
        assertEquals(200, state2.getResponseCode());
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
        LinkStateManager linkStateManager = this.componentManager.getInstance(LinkStateManager.class);
        Map<String, LinkState> contentReferences = new HashMap<>();
        long initialTime = System.currentTimeMillis();
        contentReferences.put("default", new LinkState(404, initialTime));
        linkStateManager.getLinkStates().put("http://ok", contentReferences);

        HTTPChecker httpChecker = this.componentManager.registerMockComponent(HTTPChecker.class);
        when(httpChecker.check("http://newok")).thenReturn(200);
        when(httpChecker.check("http://ok")).thenReturn(200);

        // Modify the default timeout so that we don't have to wait too long for the test...
        LinkCheckerTransformationConfiguration configuration =
            this.componentManager.getInstance(LinkCheckerTransformationConfiguration.class);
        configuration.setCheckTimeout(0L);

        transformAndWait(input, linkStateManager, 2);

        // We've put a timeout of 0ms but to be on the safe side we wait 1ms (since otherwise it could be possible
        // that the above executes in less than 1ms.
        Thread.sleep(1L);

        // Verify we can access the link states through the Script Service
        LinkCheckerScriptService service = this.componentManager.getInstance(ScriptService.class, "linkchecker");
        Map<String, Map<String, LinkState>> states = service.getLinkStates();

        LinkState state = states.get("http://ok").get("default");
        assertEquals(200, state.getResponseCode());
    }

    @Test
    public void transformWithSourceMetaData() throws Exception
    {
        String input = "[[http://ok]]";
        Parser parser = this.componentManager.getInstance(Parser.class, "xwiki/2.0");
        XDOM xdom = parser.parse(new StringReader(input));

        // Add MetaData Block
        MetaData metaData = new MetaData();
        metaData.addMetaData(MetaData.SOURCE, "source");
        XDOM newXDOM = new XDOM(xdom.getChildren(), metaData);

        HTTPChecker httpChecker = this.componentManager.registerMockComponent(HTTPChecker.class);
        when(httpChecker.check("http://ok")).thenReturn(200);

        Transformation transformation = this.componentManager.getInstance(Transformation.class, "linkchecker");
        transformation.transform(newXDOM, new TransformationContext());

        LinkStateManager linkStateManager = this.componentManager.getInstance(LinkStateManager.class);
        wait(linkStateManager, 1);

        assertNotNull(linkStateManager.getLinkStates().get("http://ok").get("source"));
    }

    /**
     * Verify that when the Observation Manager is available we send an InvalidURLEvent event when there's an invalid
     * URL.
     */
    @Test
    public void transformAndSendEvent() throws Exception
    {
        ObservationManager observationManager = this.componentManager.registerMockComponent(ObservationManager.class);
        HTTPChecker httpChecker = this.componentManager.registerMockComponent(HTTPChecker.class);
        when(httpChecker.check("http://doesntexist")).thenReturn(404);

        class StateAnswer implements Answer
        {
            public boolean hasListenerBeenCalled;

            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                this.hasListenerBeenCalled = true;
                return null;
            }
        }

        StateAnswer stateAnswer = new StateAnswer();
        doAnswer(stateAnswer).when(observationManager).notify(
            eq(new InvalidURLEvent("http://doesntexist")), any(Map.class));

        LinkStateManager linkStateManager = this.componentManager.getInstance(LinkStateManager.class);
        transformAndWait("[[http://doesntexist]]", linkStateManager, 1);

        // Wait till the event has been sent since parseAndWait only waits for the link state to be in cache but it
        // doesn't wait for the event to be sent.
        while (!stateAnswer.hasListenerBeenCalled) {
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

        HTTPChecker httpChecker = this.componentManager.registerMockComponent(HTTPChecker.class);
        when(httpChecker.check("http://ok")).thenReturn(200);

        // Register a LinkContextDataProvider component
        LinkContextDataProvider linkContextDataProvider =
            this.componentManager.registerMockComponent(LinkContextDataProvider.class);
        when(linkContextDataProvider.getContextData("http://ok", "default")).thenReturn(
            Collections.<String, Object>singletonMap("contextKey", "contextValue"));

        LinkStateManager linkStateManager = this.componentManager.getInstance(LinkStateManager.class);
        transformAndWait(input, linkStateManager, 1);

        // Assert states
        LinkState linkState = linkStateManager.getLinkStates().get("http://ok").get("default");
        assertEquals("contextValue", linkState.getContextData().get("contextKey"));
    }

    /**
     * Verify the anti-flooding mechanism.
     */
    @Test
    public void transformWithAntiFloodKickingIn() throws Exception
    {
        // Replace the Link checker Thread with a mock so that it doesn't remove any link item from the queue
        // Note that the LinkCheckerTransformation getInstance() below will automatically start the Link Checker
        // Thread.
        this.componentManager.registerMockComponent(LinkCheckerThread.class);

        LinkCheckerTransformation transformation =
            this.componentManager.getInstance(Transformation.class, "linkchecker");

        StringBuilder input = new StringBuilder();
        for (int i = 0; i < LinkCheckerTransformation.MAX_LINKS_IN_QUEUE + 1; i++) {
            String url = "url" + i;
            input.append("[[url:").append(url).append("]]");
        }

        // Render a first page with MAX_LINKS_IN_QUEUE + 1 links in it so that they're all added to the Check Queue
        Parser xwiki20Parser = this.componentManager.getInstance(Parser.class, "xwiki/2.0");
        XDOM xdom = xwiki20Parser.parse(new StringReader(input.toString()));
        transformation.transform(xdom, new TransformationContext());
        assertEquals(LinkCheckerTransformation.MAX_LINKS_IN_QUEUE + 1, transformation.getLinkQueue().size());

        // Now render a second page and verify no new links are added to the queue since it's already full!
        transformation.transform(xdom, new TransformationContext());
        assertEquals(LinkCheckerTransformation.MAX_LINKS_IN_QUEUE + 1, transformation.getLinkQueue().size());
    }

    private void transformAndWait(String input, LinkStateManager linkStateManager, int numberOfItemsToWaitFor)
        throws Exception
    {
        transform(input);

        // At this point the links have been put on the queue and we're waiting for the Link Checker Thread to
        // process them
        wait(linkStateManager, numberOfItemsToWaitFor);
    }

    private void transform(String input) throws Exception
    {
        transform(input, null);
    }

    private void transform(String input, String source) throws Exception
    {
        Transformation transformation = this.componentManager.getInstance(Transformation.class, "linkchecker");
        Parser xwiki20Parser = this.componentManager.getInstance(Parser.class, "xwiki/2.0");
        XDOM xdom = xwiki20Parser.parse(new StringReader(input));

        if (source != null) {
            MetaData metaData = new MetaData();
            metaData.addMetaData(MetaData.SOURCE, source);
            xdom = new XDOM(xdom.getChildren(), metaData);
        }

        transformation.transform(xdom, new TransformationContext());
    }

    private void wait(LinkStateManager linkStateManager, int numberOfItemsToWaitFor) throws Exception
    {
        long time = System.currentTimeMillis();
        while (linkStateManager.getLinkStates().size() != numberOfItemsToWaitFor) {
            Thread.sleep(100L);
            // Protect against infinite loop
            assertTrue("Killed thread since it took too much time", System.currentTimeMillis() - time < 10000L);
        }
    }
}
