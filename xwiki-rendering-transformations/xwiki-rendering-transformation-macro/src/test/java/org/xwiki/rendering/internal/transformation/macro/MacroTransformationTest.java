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
package org.xwiki.rendering.internal.transformation.macro;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.properties.internal.DefaultBeanDescriptor;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.macro.Macro;
import org.xwiki.rendering.macro.MacroId;
import org.xwiki.rendering.macro.descriptor.DefaultMacroDescriptor;
import org.xwiki.rendering.macro.descriptor.MacroDescriptor;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.rendering.transformation.TransformationException;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MacroTransformation}.
 *
 * @version $Id$
 */
@AllComponents(excludes = IsolatedExecutionConfiguration.class)
@ComponentTest
class MacroTransformationTest
{
    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @InjectMockComponents
    @Named("macro")
    private MacroTransformation transformation;

    @MockComponent
    private IsolatedExecutionConfiguration isolatedExecutionConfiguration;

    @BeforeEach
    void setUp()
    {
        // By default, return whatever the macro specified.
        when(this.isolatedExecutionConfiguration.isExecutionIsolated(anyString(), anyBoolean()))
            .thenAnswer(invocation -> invocation.getArgument(1));
    }

    /**
     * Test that a simple macro is correctly evaluated.
     */
    @Test
    void transformSimpleMacro() throws Exception
    {
        String expected = "beginDocument\n"
            + "beginMacroMarkerStandalone [testsimplemacro] []\n"
            + "beginParagraph\n"
            + "onWord [simplemacro0]\n"
            + "endParagraph\n"
            + "endMacroMarkerStandalone [testsimplemacro] []\n"
            + "endDocument";

        XDOM dom = new XDOM(Arrays.asList((Block) new MacroBlock("testsimplemacro",
            Collections.<String, String>emptyMap(), false)));

        this.transformation.transform(dom, new TransformationContext(dom, Syntax.XWIKI_2_0));

        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer eventBlockRenderer =
            this.componentManager.getInstance(BlockRenderer.class, Syntax.EVENT_1_0.toIdString());
        eventBlockRenderer.render(dom, printer);
        assertEquals(expected, printer.toString());
    }

    /**
     * Test that a macro can generate another macro.
     */
    @Test
    void transformNestedMacro() throws Exception
    {
        String expected = "beginDocument\n"
            + "beginMacroMarkerStandalone [testnestedmacro] []\n"
            + "beginMacroMarkerStandalone [testsimplemacro] []\n"
            + "beginParagraph\n"
            + "onWord [simplemacro0]\n"
            + "endParagraph\n"
            + "endMacroMarkerStandalone [testsimplemacro] []\n"
            + "endMacroMarkerStandalone [testnestedmacro] []\n"
            + "endDocument";

        XDOM dom = new XDOM(Arrays.asList((Block) new MacroBlock("testnestedmacro",
            Collections.<String, String>emptyMap(), false)));

        this.transformation.transform(dom, new TransformationContext(dom, Syntax.XWIKI_2_0));

        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer eventBlockRenderer =
            this.componentManager.getInstance(BlockRenderer.class, Syntax.EVENT_1_0.toIdString());
        eventBlockRenderer.render(dom, printer);
        assertEquals(expected, printer.toString());
    }

    /**
     * Test that we have a safeguard against infinite recursive macros.
     */
    @Test
    void transformMacroWithInfiniteRecursion() throws Exception
    {
        String expected = "beginDocument\n"
            + StringUtils.repeat("beginMacroMarkerStandalone [testrecursivemacro] []\n", 129)
            + "onMacroStandalone [testrecursivemacro] []\n"
            + StringUtils.repeat("endMacroMarkerStandalone [testrecursivemacro] []\n", 129)
            + "endDocument";

        XDOM dom = new XDOM(Arrays.asList((Block) new MacroBlock("testrecursivemacro",
            Collections.<String, String>emptyMap(), false)));

        // To have a fast test, set the max macro execution to 128 macros.
        this.transformation.setMaxRecursions(128);

        this.transformation.transform(dom, new TransformationContext(dom, Syntax.XWIKI_2_0));

        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer eventBlockRenderer =
            this.componentManager.getInstance(BlockRenderer.class, Syntax.EVENT_1_0.toIdString());
        eventBlockRenderer.render(dom, printer);
        assertEquals(expected, printer.toString());
    }

    @Test
    void transformWhenLotsOfMacrosButNoInfiniteRecursion() throws Exception
    {
        String expected = "beginDocument\n";
        for (int i = 0; i < 10; i++) {
            expected += "beginMacroMarkerStandalone [testsimplemacro] []\n"
                + "beginParagraph\n"
                + "onWord [simplemacro" + i + "]\n"
                + "endParagraph\n"
                + "endMacroMarkerStandalone [testsimplemacro] []\n";
        }
        expected += "endDocument";

        List<Block> macroBlocks = new ArrayList<Block>();
        for (int i = 0; i < 10; i++) {
            macroBlocks.add(new MacroBlock("testsimplemacro", Collections.<String, String>emptyMap(), false));
        }
        XDOM dom = new XDOM(macroBlocks);

        // Make sure that the max macro execution is less than the # of macros we have in the content to prove that
        // we only stop on real recursion.
        this.transformation.setMaxRecursions(5);

        this.transformation.transform(dom, new TransformationContext(dom, Syntax.XWIKI_2_0));

        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer eventBlockRenderer =
            this.componentManager.getInstance(BlockRenderer.class, Syntax.EVENT_1_0.toIdString());
        eventBlockRenderer.render(dom, printer);
        assertEquals(expected, printer.toString());
    }

    /**
     * Test that macro priorities are working.
     */
    @Test
    void transformMacrosWithPriorities() throws Exception
    {
        String expected = "beginDocument\n"
            + "beginMacroMarkerStandalone [testsimplemacro] []\n"
            + "beginParagraph\n"
            + "onWord [simplemacro1]\n"
            + "endParagraph\n"
            + "endMacroMarkerStandalone [testsimplemacro] []\n"
            + "beginMacroMarkerStandalone [testprioritymacro] []\n"
            + "beginParagraph\n"
            + "onWord [word]\n"
            + "endParagraph\n"
            + "endMacroMarkerStandalone [testprioritymacro] []\n"
            + "endDocument";

        // "testprioritymacro" has a highest priority than "testsimplemacro" and will be executed first.
        // This is verified as follows:
        // - "testprioritymacro" generates a WordBlock
        // - "testsimplemacro" outputs "simplemacro" followed by the number of WordBlocks that exist in the document
        // Thus if "testsimplemacro" is executed before "testprioritymacro" it would print "simplemacro0"
        XDOM dom = new XDOM(Arrays.<Block>asList(
            new MacroBlock("testsimplemacro", Collections.<String, String>emptyMap(), false),
            new MacroBlock("testprioritymacro", Collections.<String, String>emptyMap(), false)));

        this.transformation.transform(dom, new TransformationContext(dom, Syntax.XWIKI_2_0));

        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer eventBlockRenderer =
            this.componentManager.getInstance(BlockRenderer.class, Syntax.EVENT_1_0.toIdString());
        eventBlockRenderer.render(dom, printer);
        assertEquals(expected, printer.toString());
    }

    @Test
    void transformWithSeveralNestedMacros() throws Exception
    {
        String expected = """
            beginDocument
            beginMacroMarkerStandalone [testsimplemacro] []
            beginParagraph
            onWord [simplemacro1]
            endParagraph
            endMacroMarkerStandalone [testsimplemacro] []
            beginMacroMarkerStandalone [testprioritymacro] []
            beginParagraph
            onWord [word]
            endParagraph
            endMacroMarkerStandalone [testprioritymacro] []
            beginMacroMarkerStandalone [testnestedmacro] []
            beginMacroMarkerStandalone [testsimplemacro] []
            beginParagraph
            onWord [simplemacro2]
            endParagraph
            endMacroMarkerStandalone [testsimplemacro] []
            endMacroMarkerStandalone [testnestedmacro] []
            beginMacroMarkerStandalone [testsimplemacro] []
            beginParagraph
            onWord [simplemacro3]
            endParagraph
            endMacroMarkerStandalone [testsimplemacro] []
            beginMacroMarkerStandalone [testtwonestedmacros] []
            beginMacroMarkerStandalone [testsimplemacro] []
            beginParagraph
            onWord [simplemacro4]
            endParagraph
            endMacroMarkerStandalone [testsimplemacro] []
            beginMacroMarkerStandalone [testnestedmacro] []
            beginMacroMarkerStandalone [testsimplemacro] []
            beginParagraph
            onWord [simplemacro5]
            endParagraph
            endMacroMarkerStandalone [testsimplemacro] []
            endMacroMarkerStandalone [testnestedmacro] []
            endMacroMarkerStandalone [testtwonestedmacros] []
            beginMacroMarkerStandalone [testsimplemacro] []
            beginParagraph
            onWord [simplemacro6]
            endParagraph
            endMacroMarkerStandalone [testsimplemacro] []
            endDocument""";
        XDOM dom = new XDOM(List.of(
            new MacroBlock("testsimplemacro", Listener.EMPTY_PARAMETERS, false),
            new MacroBlock("testprioritymacro", Listener.EMPTY_PARAMETERS, false),
            new MacroBlock("testnestedmacro", Listener.EMPTY_PARAMETERS, false),
            new MacroBlock("testsimplemacro", Listener.EMPTY_PARAMETERS, false),
            new MacroBlock("testtwonestedmacros", Listener.EMPTY_PARAMETERS, false),
            new MacroBlock("testsimplemacro", Listener.EMPTY_PARAMETERS, false)
        ));

        this.transformation.transform(dom, new

            TransformationContext(dom, Syntax.XWIKI_2_0));

        WikiPrinter printer = new DefaultWikiPrinter();

        BlockRenderer eventBlockRenderer =
            this.componentManager.getInstance(BlockRenderer.class, Syntax.EVENT_1_0.toIdString());
        eventBlockRenderer.render(dom, printer);

        assertEquals(expected, printer.toString());
    }

    /**
     * Test that macro with same priorities execute in the order in which they are defined.
     */
    @Test
    void macroWithSamePriorityExecuteOnPageOrder() throws Exception
    {
        // Both macros have the same priorities and thus "testsimplemacro" should be executed first and generate
        // "simplemacro0".
        XDOM dom = new XDOM(Arrays.<Block>asList(
            new MacroBlock("testsimplemacro", Collections.<String, String>emptyMap(), false),
            new MacroBlock("testcontentmacro", Collections.<String, String>emptyMap(), "content", false)));

        TransformationContext context = new TransformationContext(dom, Syntax.XWIKI_2_0);
        this.transformation.transform(dom, context);

        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer eventBlockRenderer =
            this.componentManager.getInstance(BlockRenderer.class, Syntax.EVENT_1_0.toIdString());
        eventBlockRenderer.render(dom, printer);

        String expected = "beginDocument\n"
            + "beginMacroMarkerStandalone [testsimplemacro] []\n"
            + "beginParagraph\n"
            + "onWord [simplemacro0]\n"
            + "endParagraph\n"
            + "endMacroMarkerStandalone [testsimplemacro] []\n"
            + "beginMacroMarkerStandalone [testcontentmacro] [] [content]\n"
            + "onWord [content]\n"
            + "endMacroMarkerStandalone [testcontentmacro] [] [content]\n"
            + "endDocument";
        assertEquals(expected, printer.toString());

        // We must also test the other order ("testcontentmacro" before "testsimplemacro") to ensure for example that
        // there's no lexical order on Macro class names for example.
        dom = new XDOM(Arrays.<Block>asList(
            new MacroBlock("testcontentmacro", Collections.<String, String>emptyMap(), "content", false),
            new MacroBlock("testsimplemacro", Collections.<String, String>emptyMap(), false)));

        context.setXDOM(dom);
        this.transformation.transform(dom, context);

        printer = new DefaultWikiPrinter();
        eventBlockRenderer.render(dom, printer);

        expected = "beginDocument\n"
            + "beginMacroMarkerStandalone [testcontentmacro] [] [content]\n"
            + "onWord [content]\n"
            + "endMacroMarkerStandalone [testcontentmacro] [] [content]\n"
            + "beginMacroMarkerStandalone [testsimplemacro] []\n"
            + "beginParagraph\n"
            + "onWord [simplemacro1]\n"
            + "endParagraph\n"
            + "endMacroMarkerStandalone [testsimplemacro] []\n"
            + "endDocument";
        assertEquals(expected, printer.toString());
    }

    /**
     * Test that a not existing macro generate an error in the XDOM.
     */
    @Test
    void transformNotExistingMacro() throws Exception
    {
        String expected = "beginDocument\n"
            + "beginMacroMarkerStandalone [notexisting] []\n"
            + "beginGroup [[class]=[xwikirenderingerror]]\n"
            + "onWord [Unknown macro: notexisting. Click on this message for details.]\n"
            + "endGroup [[class]=[xwikirenderingerror]]\n"
            + "beginGroup [[class]=[xwikirenderingerrordescription hidden]]\n"
            + "onVerbatim [The [notexisting] macro is not in the list of registered macros. "
            + "Verify the spelling or contact your administrator.] [false]\n"
            + "endGroup [[class]=[xwikirenderingerrordescription hidden]]\n"
            + "endMacroMarkerStandalone [notexisting] []\n"
            + "endDocument";

        XDOM dom = new XDOM(Arrays.asList((Block) new MacroBlock("notexisting",
            Collections.<String, String>emptyMap(), false)));

        assertEquals(expected, transformAndRenderEvents(dom));
    }

    @Test
    void prepareSimpleMacro() throws Exception
    {
        MacroBlock macroBlock = new MacroBlock("testmacro",
            Collections.<String, String>emptyMap(), false);

        Macro testMacro = this.componentManager.registerMockComponent(Macro.class, macroBlock.getId());
        doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                invocation.<MacroBlock>getArgument(0).setAttribute("test", "prepared");

                return null;
            }
        }).when(testMacro).prepare(macroBlock);

        this.transformation.prepare(macroBlock);

        assertEquals(Map.of("test", "prepared"), macroBlock.getAttributes());
    }

    @Test
    void prepareSimpleMacros() throws Exception
    {
        MacroBlock macroBlock1 = new MacroBlock("testmacro1",
            Collections.<String, String>emptyMap(), false);
        MacroBlock macroBlock2 = new MacroBlock("testmacro2",
            Collections.<String, String>emptyMap(), false);

        Macro testMacro1 = this.componentManager.registerMockComponent(Macro.class, macroBlock1.getId());
        Macro testMacro2 = this.componentManager.registerMockComponent(Macro.class, macroBlock2.getId());
        doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                MacroBlock block = invocation.getArgument(0);
                block.setAttribute("test", "prepared1");

                return null;
            }
        }).when(testMacro1).prepare(macroBlock1);
        doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                MacroBlock block = invocation.getArgument(0);
                block.setAttribute("test", "prepared2");

                return null;
            }
        }).when(testMacro2).prepare(macroBlock2);

        this.transformation.prepare(new XDOM(List.of(macroBlock1, macroBlock2)));

        assertEquals(Map.of("test", "prepared1"), macroBlock1.getAttributes());
        assertEquals(Map.of("test", "prepared2"), macroBlock2.getAttributes());
    }

    @Test
    void prepareNotExistingMacro()
    {
        MacroBlock macroBlock = new MacroBlock("notexisting", Collections.<String, String>emptyMap(), false);
        XDOM dom = new XDOM(List.of(macroBlock));

        assertTrue(macroBlock.getAttributes().isEmpty());

        this.transformation.prepare(dom);

        assertTrue(macroBlock.getAttributes().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void replacementMacro(boolean isIsolated) throws Exception
    {
        // Expect that while the replacement happens, the new macro isn't executed as no new scan of macros happens when
        // the macro is isolated.
        String expected = isIsolated ? """
            beginDocument
            onMacroStandalone [testReplacement] [param1=newValue] [macroContent]
            endDocument"""
            : """
            beginDocument
            beginMacroMarkerStandalone [testReplacement] [param1=newValue] [macroContent]
            onWord [testReplacement]
            onWord [macroContent]
            onWord [newValue]
            endMacroMarkerStandalone [testReplacement] [param1=newValue] [macroContent]
            endDocument""";

        String macroID = "testReplaceMe";
        MacroBlock macroBlock = new MacroBlock(macroID, Map.of("oldParameter", "oldValue"), "macroContent", false);
        XDOM dom = new XDOM(List.of(macroBlock));

        when(this.isolatedExecutionConfiguration.isExecutionIsolated(macroID, false)).thenReturn(isIsolated);

        assertEquals(expected, transformAndRenderEvents(dom));
    }

    @ParameterizedTest
    @CsvSource({
        "false, false",
        "true, false",
        "true, true",
        "false, true"
    })
    void isIsolated(boolean macroIsolated, boolean executionIsolated) throws Exception
    {
        StringBuilder expected = new StringBuilder("""
            beginDocument
            beginMacroMarkerStandalone [isolatedMacro] []
            onWord [isolated]
            endMacroMarkerStandalone [isolatedMacro] []
            """);
        if (executionIsolated) {
            expected.append("onMacroStandalone [testReplacement] [param1=Hello] [inside]\n");
        } else {
            expected.append("""
                beginMacroMarkerStandalone [testReplacement] [param1=Hello] [inside]
                onWord [testReplacement]
                onWord [inside]
                onWord [Hello]
                endMacroMarkerStandalone [testReplacement] [param1=Hello] [inside]
                """);
        }
        expected.append("endDocument");

        // Create a mock macro to be able to dynamically change the return value of isExecutionIsolated.
        String macroId = "isolatedMacro";
        createMockMacro(macroId, 100, macroIsolated, invocation -> {
            MacroTransformationContext context = invocation.getArgument(2);
            context.getXDOM().addChild(new MacroBlock("testReplacement", Map.of("param1", "Hello"), "inside", false));
            return List.of(new WordBlock("isolated"));
        });

        MacroBlock macroBlock = new MacroBlock(macroId, Collections.emptyMap(), false);
        XDOM dom = new XDOM(List.of(macroBlock));

        when(this.isolatedExecutionConfiguration.isExecutionIsolated(eq(macroId), anyBoolean()))
            .thenReturn(executionIsolated);

        assertEquals(expected.toString(), transformAndRenderEvents(dom));

        verify(this.isolatedExecutionConfiguration).isExecutionIsolated(macroId, macroIsolated);
    }

    @Test
    void testNestedPriorities() throws Exception
    {
        // Ensure that a macro nested in a high-priority macro gets inserted at the right position in the PQ - at the
        // position of the parent macro.
        String counterMacro = "testCounter";
        String containerMacro = "testContainer";
        MutableInt executionCounter = new MutableInt(0);

        createMockMacro(counterMacro, 1000, true,
            invocation -> List.of(new WordBlock("counter" + executionCounter.getAndIncrement())));
        createMockMacro(containerMacro, 100, true,
            invocation -> List.of(
                new WordBlock("container" + executionCounter.getAndIncrement()),
                new MacroBlock(counterMacro, Map.of(), false))
        );

        XDOM dom = new XDOM(List.of(
            new MacroBlock(counterMacro, Map.of(), false),
            new MacroBlock(containerMacro, Map.of(), false),
            new MacroBlock(counterMacro, Map.of(), false)
        ));

        String expected = """
            beginDocument
            beginMacroMarkerStandalone [testCounter] []
            onWord [counter1]
            endMacroMarkerStandalone [testCounter] []
            beginMacroMarkerStandalone [testContainer] []
            onWord [container0]
            beginMacroMarkerStandalone [testCounter] []
            onWord [counter2]
            endMacroMarkerStandalone [testCounter] []
            endMacroMarkerStandalone [testContainer] []
            beginMacroMarkerStandalone [testCounter] []
            onWord [counter3]
            endMacroMarkerStandalone [testCounter] []
            endDocument""";

        assertEquals(expected, transformAndRenderEvents(dom));
    }

    private void createMockMacro(String macroId, int priority, boolean macroIsolated, Answer<List<Block>> execute)
        throws Exception
    {
        Macro<Object> macro = this.componentManager.registerMockComponent(Macro.class, macroId);
        when(macro.getPriority()).thenReturn(priority);
        when(macro.execute(any(), any(), any())).thenAnswer(execute);
        when(macro.isExecutionIsolated(any(), any())).thenReturn(macroIsolated);
        MacroDescriptor macroDescriptor = new DefaultMacroDescriptor(new MacroId(macroId), macroId, macroId, null,
            new DefaultBeanDescriptor(Object.class));
        when(macro.getDescriptor()).thenReturn(macroDescriptor);
        when(macro.compareTo(any())).thenAnswer(invocation -> {
            Macro<?> other = invocation.getArgument(0);
            return priority - other.getPriority();
        });
    }

    private String transformAndRenderEvents(XDOM dom) throws TransformationException, ComponentLookupException
    {
        this.transformation.transform(dom, new TransformationContext(dom, Syntax.XWIKI_2_0));

        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer eventBlockRenderer =
            this.componentManager.getInstance(BlockRenderer.class, Syntax.EVENT_1_0.toIdString());
        eventBlockRenderer.render(dom, printer);
        return printer.toString();
    }
}
