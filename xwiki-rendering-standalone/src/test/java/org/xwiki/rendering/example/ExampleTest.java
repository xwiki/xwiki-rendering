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
package org.xwiki.rendering.example;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.block.SpecialSymbolBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.converter.Converter;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.Transformation;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.rendering.transformation.icon.IconTransformationConfiguration;

/**
 * Examples of using the XWiki Rendering API standalone, using the Embedded Component Manager.
 *
 * @version $Id$
 * @since 2.0M1
 */
public class ExampleTest
{
    @Test
    public void renderXWiki20SyntaxAsXHTML() throws Exception
    {
        // Initialize Rendering components and allow getting instances
        EmbeddableComponentManager cm = new EmbeddableComponentManager();
        cm.initialize(this.getClass().getClassLoader());

        // Use the Converter component to convert between one syntax to another.
        Converter converter = cm.getInstance(Converter.class);

        // Convert input in XWiki Syntax 2.1 into XHTML. The result is stored in the printer.
        WikiPrinter printer = new DefaultWikiPrinter();
        converter.convert(new StringReader("This is **bold**"), Syntax.XWIKI_2_1, Syntax.XHTML_1_0, printer);

        Assert.assertEquals("<p>This is <strong>bold</strong></p>", printer.toString());
    }

    @Test
    public void makeAllLinksItalic() throws Exception
    {
        // Initialize Rendering components and allow getting instances
        EmbeddableComponentManager cm = new EmbeddableComponentManager();
        cm.initialize(this.getClass().getClassLoader());

        // Parse XWiki 2.1 Syntax using a Parser.
        Parser parser = cm.getInstance(Parser.class, Syntax.XWIKI_2_1.toIdString());
        XDOM xdom = parser.parse(new StringReader("This a [[link>>MyPage]]"));

        // Find all links and make them italic by manipulating the XDOM
        for (Block block : xdom.getBlocks(new ClassBlockMatcher(LinkBlock.class), Block.Axes.DESCENDANT)) {
            Block parentBlock = block.getParent();
            Block newBlock = new FormatBlock(Collections.<Block>singletonList(block), Format.ITALIC);
            parentBlock.replaceChild(newBlock, block);
        }

        // Generate XWiki 2.1 Syntax as output for example
        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer renderer = cm.getInstance(BlockRenderer.class, Syntax.XWIKI_2_1.toIdString());
        renderer.render(xdom, printer);

        Assert.assertEquals("This a //[[link>>MyPage]]//", printer.toString());
    }

    @Test
    public void executeMacroTransformation() throws Exception
    {
        // Initialize Rendering components and allow getting instances
        final EmbeddableComponentManager cm = new EmbeddableComponentManager();
        cm.initialize(this.getClass().getClassLoader());

        Parser parser = cm.getInstance(Parser.class, Syntax.XWIKI_2_1.toIdString());
        XDOM xdom = parser.parse(new StringReader("{{id name=\"test\"/}}"));

        // Execute the Macro Transformation to execute Macros.
        Transformation transformation = cm.getInstance(Transformation.class, "macro");
        TransformationContext txContext = new TransformationContext(xdom, parser.getSyntax());
        transformation.transform(xdom, txContext);

        // Convert input in XWiki Syntax 2.1 into XHTML. The result is stored in the printer.
        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer renderer = cm.getInstance(BlockRenderer.class, Syntax.XHTML_1_0.toIdString());
        renderer.render(xdom, printer);

        Assert.assertEquals("<div id=\"test\"></div>", printer.toString());
    }

    /**
     * Verifies that all bundled macro work fine (this is to verify we bundle all their required dependencies).
     */
    @Test
    public void executeAllBundledMacros() throws Exception
    {
        // Initialize Rendering components and allow getting instances
        final EmbeddableComponentManager cm = new EmbeddableComponentManager();
        cm.initialize(this.getClass().getClassLoader());

        // Content containing all bundled macros
        String content = "{{toc/}}\n\n"
            + "{{id name=\"header1\"/}}\n"
            + "= header =\n"
            + "{{box}}content{{/box}}\n\n"
            + "{{info}}info{{/info}}\n\n"
            + "{{warning}}warning{{/warning}}\n\n"
            + "{{error}}error{{/error}}\n\n"
            + "{{html}}<strong>bold</strong>{{/html}}\n\n"
            + "{{footnote}}footnote{{/footnote}}\n\n"
            + "{{putFootnotes/}}\n\n"
            + "{{comment}}comment{{/comment}}";

        Parser parser = cm.getInstance(Parser.class, Syntax.XWIKI_2_1.toIdString());
        XDOM xdom = parser.parse(new StringReader(content));

        // Execute the Macro Transformation to execute Macros.
        Transformation transformation = cm.getInstance(Transformation.class, "macro");
        TransformationContext txContext = new TransformationContext(xdom, parser.getSyntax());
        transformation.transform(xdom, txContext);

        // Convert input in XWiki Syntax 2.1 into XHTML. The result is stored in the printer.
        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer renderer = cm.getInstance(BlockRenderer.class, Syntax.XHTML_1_0.toIdString());
        renderer.render(xdom, printer);

        String expected = "<ul class=\"wikitoc\">"
            + "<li><span class=\"wikilink\"><a href=\"#Hheader\">header</a></span></li></ul>"
            + "<div id=\"header1\"></div>"
            + "<h1 id=\"Hheader\" class=\"wikigeneratedid\"><span>header</span></h1>"
            + "<div class=\"box\"><p>content</p></div>"
            + "<div class=\"box infomessage\">"
            + "<img src=\"icon:information\" class=\"wikimodel-freestanding\" alt=\"information\"/>"
            + "<div>"
            + "<p>info</p>"
            + "</div>"
            + "</div>"
            + "<div class=\"box warningmessage\">"
            + "<img src=\"icon:error\" class=\"wikimodel-freestanding\" alt=\"error\"/>"
            + "<div>"
            + "<p>warning</p>"
            + "</div>"
            + "</div>"
            + "<div class=\"box errormessage\">"
            + "<img src=\"icon:exclamation\" class=\"wikimodel-freestanding\" alt=\"exclamation\"/>"
            + "<div>"
            + "<p>error</p>"
            + "</div>"
            + "</div>"
            + "<p><strong>bold</strong></p>"
            + "<p><sup><span id=\"x_footnote_ref_1\" class=\"footnoteRef\"><span class=\"wikilink\">"
            + "<a href=\"#x_footnote_1\">1</a></span></span></sup></p>"
            + "<ol class=\"footnotes\"><li><span class=\"wikilink\">"
            + "<a id=\"x_footnote_1\" class=\"footnoteBackRef\" href=\"#x_footnote_ref_1\">^</a>"
            + "</span> footnote</li></ol>";

        Assert.assertEquals(expected, printer.toString());
    }

    /**
     * Verifies that the WikiWord Transformation is bundled and working.
     */
    @Test
    public void executeWikiWordTransformation() throws Exception
    {
        // Initialize Rendering components and allow getting instances
        final EmbeddableComponentManager cm = new EmbeddableComponentManager();
        cm.initialize(this.getClass().getClassLoader());

        XDOM xdom = new XDOM(
            Arrays.<Block>asList(new ParagraphBlock(Arrays.asList((Block) new WordBlock("WikiWord")))));

        Transformation transformation = cm.getInstance(Transformation.class, "wikiword");
        TransformationContext txContext = new TransformationContext();
        transformation.transform(xdom, txContext);

        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer renderer = cm.getInstance(BlockRenderer.class, Syntax.XWIKI_2_1.toIdString());
        renderer.render(xdom, printer);

        String expected = "[[doc:WikiWord]]";

        Assert.assertEquals(expected, printer.toString());
    }

    /**
     * Verifies that the Icon Transformation is bundled and working.
     */
    @Test
    public void executeIconTransformation() throws Exception
    {
        // Initialize Rendering components and allow getting instances
        final EmbeddableComponentManager cm = new EmbeddableComponentManager();
        cm.initialize(this.getClass().getClassLoader());

        XDOM xdom = new XDOM(Arrays.<Block>asList(new ParagraphBlock(Arrays.asList((Block) new SpecialSymbolBlock(':'),
            new SpecialSymbolBlock(':')))));

        // Test adding a new Icon Mapping.
        IconTransformationConfiguration configuration = cm.getInstance(IconTransformationConfiguration.class);
        configuration.addMapping("::", "something");

        Transformation transformation = cm.getInstance(Transformation.class, "icon");
        TransformationContext txContext = new TransformationContext();
        transformation.transform(xdom, txContext);

        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer renderer = cm.getInstance(BlockRenderer.class, Syntax.XWIKI_2_1.toIdString());
        renderer.render(xdom, printer);

        String expected = "image:icon:something";

        Assert.assertEquals(expected, printer.toString());
    }
}
