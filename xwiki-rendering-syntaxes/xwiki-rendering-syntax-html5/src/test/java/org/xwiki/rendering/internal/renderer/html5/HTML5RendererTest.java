package org.xwiki.rendering.internal.renderer.html5;

import org.jmock.Expectations;
import org.junit.Test;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.rendering.block.*;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.renderer.PrintRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.wiki.WikiModel;
import org.xwiki.test.AbstractComponentTestCase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * User: ElderMael
 * Date: 11/30/12
 * Time: 11:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class HTML5RendererTest extends AbstractComponentTestCase {

    private PrintRenderer renderer;

    private WikiModel mockWikiModel;

    @Override
    protected void registerComponents() throws Exception
    {
        // Register a mock implementation of WikiModel in order to perform expectations on the generation of
        // document and image URLs.
        this.mockWikiModel = getMockery().mock(WikiModel.class);
        DefaultComponentDescriptor<WikiModel> cd = new DefaultComponentDescriptor<WikiModel>();
        cd.setRoleType(WikiModel.class);
        getComponentManager().registerComponent(cd, this.mockWikiModel);

        this.renderer = getComponentManager().getInstance(PrintRenderer.class, "html/5.0");
    }

    /**
     * Verify that when an XDOM contains a MetaDataBlock with a "source" metaData specified, then this "source" is
     * set in the ResourceReference passed to the WikiModel call when getting a document link URL.
     */
    @Test
    public void testBeginLinkHasBaseResourceReferencePassedWhenSourceMetaDataAdded()
    {
        final ResourceReference blockReference = new ResourceReference("reference", ResourceType.DOCUMENT);
        List<Block> linkBlocks = Arrays.asList((Block) new LinkBlock(Arrays.asList((Block) new WordBlock("label")),
                blockReference, true));
        MetaData metaData1 = new MetaData();
        metaData1.addMetaData(MetaData.BASE, "base1");
        MetaData metaData2 = new MetaData();
        metaData2.addMetaData(MetaData.BASE, "base2");
        XDOM xdom = new XDOM(Arrays.asList((Block) new MetaDataBlock(
                Arrays.asList((Block) new MetaDataBlock(linkBlocks, metaData2)), metaData1)));

        getMockery().checking(new Expectations() {{
            // This is the part of the test verification: we verify that the passed Resource Reference has its base
            // reference set.
            ResourceReference reference = new ResourceReference("reference", ResourceType.DOCUMENT);
            reference.addBaseReference("base1");
            reference.addBaseReference("base2");
            oneOf(mockWikiModel).isDocumentAvailable(reference);
            will(returnValue(true));
            oneOf(mockWikiModel).getDocumentViewURL(reference);
            will(returnValue("viewurl"));
        }});

        this.renderer.setPrinter(new DefaultWikiPrinter());
        xdom.traverse(this.renderer);
    }

    /**
     * Verify that when an XDOM contains a MetaDataBlock with a "source" metaData specified, then this "source" is
     * not used if the ResourceReference passed to the WikiModel already has a base reference specified.
     */
    @Test
    public void testBeginLinkDoesntUseSourceMetaDataIfBaseReferenceSpecified()
    {
        final ResourceReference blockReference = new ResourceReference("reference", ResourceType.DOCUMENT);
        blockReference.addBaseReference("original base");

        List<Block> linkBlocks = Arrays.asList((Block) new LinkBlock(Arrays.asList((Block) new WordBlock("label")),
                blockReference, true));
        MetaData metaData = new MetaData();
        metaData.addMetaData(MetaData.BASE, "base");
        XDOM xdom = new XDOM(Arrays.asList((Block) new MetaDataBlock(linkBlocks, metaData)));

        getMockery().checking(new Expectations() {{
            // This is the part of the test verification: we verify that the passed Resource Reference has its base
            // reference set.
            ResourceReference reference = new ResourceReference("reference", ResourceType.DOCUMENT);
            reference.addBaseReference("original base");
            oneOf(mockWikiModel).isDocumentAvailable(reference);
            will(returnValue(true));
            oneOf(mockWikiModel).getDocumentViewURL(reference);
            will(returnValue("viewurl"));
        }});

        this.renderer.setPrinter(new DefaultWikiPrinter());
        xdom.traverse(this.renderer);
    }

    /**
     * Verify that when an XDOM contains a MetaDataBlock with a "source" metaData specified, then this "source" is
     * set in the ResourceReference passed to the WikiModel call when getting an image link URL.
     */
    @Test
    public void testOnImageHasBaseResourceReferencePassedWhenSourceMetaDataAdded()
    {
        final ResourceReference blockReference = new ResourceReference("reference", ResourceType.ATTACHMENT);
        List<Block> imageBlocks = Arrays.asList((Block) new ImageBlock(blockReference, true));
        MetaData metaData = new MetaData();
        metaData.addMetaData(MetaData.BASE, "base");
        XDOM xdom = new XDOM(Arrays.asList((Block) new MetaDataBlock(imageBlocks, metaData)));

        getMockery().checking(new Expectations() {{
            // This is the part of the test verification: we verify that the passed Resource Reference has its base
            // reference set.
            ResourceReference reference = new ResourceReference("reference", ResourceType.ATTACHMENT);
            reference.addBaseReference("base");
            oneOf(mockWikiModel).getImageURL(reference, Collections.<String, String>emptyMap());
            will(returnValue("imageurl"));
        }});

        this.renderer.setPrinter(new DefaultWikiPrinter());
        xdom.traverse(this.renderer);
    }

    /**
     * Verify that when an XDOM contains a MetaDataBlock with a "source" metaData specified, then this "source" is
     * not used if the ResourceReference passed to the WikiModel already has a base reference specified.
     */
    @Test
    public void testOnImageDoesntUseSourceMetaDataIfBaseReferenceSpecified()
    {
        final ResourceReference blockReference = new ResourceReference("reference", ResourceType.ATTACHMENT);
        blockReference.addBaseReference("original base");

        List<Block> imageBlocks = Arrays.asList((Block) new ImageBlock(blockReference, true));
        MetaData metaData = new MetaData();
        metaData.addMetaData(MetaData.BASE, "base");
        XDOM xdom = new XDOM(Arrays.asList((Block) new MetaDataBlock(imageBlocks, metaData)));

        getMockery().checking(new Expectations() {{
            // This is the part of the test verification: we verify that the passed Resource Reference has its base
            // reference set.
            ResourceReference reference = new ResourceReference("reference", ResourceType.ATTACHMENT);
            reference.addBaseReference("original base");
            oneOf(mockWikiModel).getImageURL(reference, Collections.<String, String>emptyMap());
            will(returnValue("imageurl"));
        }});

        this.renderer.setPrinter(new DefaultWikiPrinter());
        xdom.traverse(this.renderer);
    }
}
