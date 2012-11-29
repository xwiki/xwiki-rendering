package org.xwiki.rendering.internal.markdown;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.RootNode;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.internal.parser.markdown.PegdownToXDOMConverter;

import java.io.IOException;
import java.util.List;

/**
 *
 */
public class SampleRichMarkdownTest {

    private static String sample = "";

    @BeforeClass
    public static void loadSample() throws IOException {
        String sourceFile = "sample-rich-markdown-test.txt";
        sample = IOUtils.toString(
                SampleRichMarkdownTest.class.getClassLoader().getResourceAsStream(sourceFile),
                "UTF-8");
    }

    @Test
    public void testTree() {
        PegDownProcessor processor = new PegDownProcessor(Extensions.ALL & ~Extensions.HARDWRAPS);

        RootNode rootNode = processor.parseMarkdown(sample.toCharArray());

//        List<Block> blocks = new PegdownToXDOMConverter().buildBlocks(rootNode);
    }
}