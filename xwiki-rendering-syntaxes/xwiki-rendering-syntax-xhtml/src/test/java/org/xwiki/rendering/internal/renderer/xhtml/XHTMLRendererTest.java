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
package org.xwiki.rendering.internal.renderer.xhtml;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.ImageBlock;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.MetaDataBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.renderer.PrintRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.wiki.WikiModel;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link XHTMLRenderer}.
 */
@AllComponents
@ComponentTest
class XHTMLRendererTest
{
    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @MockComponent
    private WikiModel mockWikiModel;

    @Inject
    @Named("xhtml/1.0")
    private PrintRenderer renderer;

    /**
     * Verify that when an XDOM contains a MetaDataBlock with a "source" metaData specified, then this "source" is
     * set in the ResourceReference passed to the WikiModel call when getting a document link URL.
     */
    @Test
    void beginLinkHasBaseResourceReferencePassedWhenSourceMetaDataAdded()
    {
        ResourceReference blockReference = new ResourceReference("reference", ResourceType.DOCUMENT);
        List<Block> linkBlocks = List.of(new LinkBlock(List.of(new WordBlock("label")),
            blockReference, true));
        MetaData metaData1 = new MetaData();
        metaData1.addMetaData(MetaData.BASE, "base1");
        MetaData metaData2 = new MetaData();
        metaData2.addMetaData(MetaData.BASE, "base2");
        XDOM xdom = new XDOM(List.of((Block) new MetaDataBlock(
            List.of((Block) new MetaDataBlock(linkBlocks, metaData2)), metaData1)));

        // This is the part of the test verification: we verify that the passed Resource Reference has its base
        // reference set.
        ResourceReference reference = new ResourceReference("reference", ResourceType.DOCUMENT);
        reference.addBaseReference("base1");
        reference.addBaseReference("base2");
        when(this.mockWikiModel.isDocumentAvailable(any(ResourceReference.class))).thenReturn(true);
        when(this.mockWikiModel.getDocumentViewURL(any(ResourceReference.class))).thenReturn("viewurl");

        this.renderer.setPrinter(new DefaultWikiPrinter());
        xdom.traverse(this.renderer);

        verify(this.mockWikiModel).isDocumentAvailable(reference);
        verify(this.mockWikiModel).getDocumentViewURL(reference);
    }

    /**
     * Verify that when an XDOM contains a MetaDataBlock with a "source" metaData specified, then this "source" is
     * not used if the ResourceReference passed to the WikiModel already has a base reference specified.
     */
    @Test
    void beginLinkDoesntUseSourceMetaDataIfBaseReferenceSpecified()
    {
        ResourceReference blockReference = new ResourceReference("reference", ResourceType.DOCUMENT);
        blockReference.addBaseReference("original base");

        List<Block> linkBlocks = List.of(new LinkBlock(List.of(new WordBlock("label")),
            blockReference, true));
        MetaData metaData = new MetaData();
        metaData.addMetaData(MetaData.BASE, "base");
        XDOM xdom = new XDOM(List.of((Block) new MetaDataBlock(linkBlocks, metaData)));

        // This is the part of the test verification: we verify that the passed Resource Reference has its base
        // reference set.
        ResourceReference reference = new ResourceReference("reference", ResourceType.DOCUMENT);
        reference.addBaseReference("original base");
        when(this.mockWikiModel.isDocumentAvailable(any(ResourceReference.class))).thenReturn(true);
        when(this.mockWikiModel.getDocumentViewURL(any(ResourceReference.class))).thenReturn("viewurl");

        this.renderer.setPrinter(new DefaultWikiPrinter());
        xdom.traverse(this.renderer);

        verify(this.mockWikiModel).isDocumentAvailable(reference);
        verify(this.mockWikiModel).getDocumentViewURL(reference);
    }

    /**
     * Verify that when an XDOM contains a MetaDataBlock with a "source" metaData specified, then this "source" is
     * set in the ResourceReference passed to the WikiModel call when getting an image link URL.
     */
    @Test
    void onImageHasBaseResourceReferencePassedWhenSourceMetaDataAdded()
    {
        ResourceReference blockReference = new ResourceReference("reference", ResourceType.ATTACHMENT);
        List<Block> imageBlocks = List.of(new ImageBlock(blockReference, true));
        MetaData metaData = new MetaData();
        metaData.addMetaData(MetaData.BASE, "base");
        XDOM xdom = new XDOM(List.of((Block) new MetaDataBlock(imageBlocks, metaData)));

        // This is the part of the test verification: we verify that the passed Resource Reference has its base
        // reference set.
        ResourceReference reference = new ResourceReference("reference", ResourceType.ATTACHMENT);
        reference.addBaseReference("base");
        when(this.mockWikiModel.getImageURL(any(ResourceReference.class), any())).thenReturn("imageurl");

        this.renderer.setPrinter(new DefaultWikiPrinter());
        xdom.traverse(this.renderer);

        verify(this.mockWikiModel).getImageURL(reference, Map.of());
    }

    /**
     * Verify that when an XDOM contains a MetaDataBlock with a "source" metaData specified, then this "source" is
     * not used if the ResourceReference passed to the WikiModel already has a base reference specified.
     */
    @Test
    void onImageDoesntUseSourceMetaDataIfBaseReferenceSpecified()
    {
        ResourceReference blockReference = new ResourceReference("reference", ResourceType.ATTACHMENT);
        blockReference.addBaseReference("original base");

        List<Block> imageBlocks = List.of(new ImageBlock(blockReference, true));
        MetaData metaData = new MetaData();
        metaData.addMetaData(MetaData.BASE, "base");
        XDOM xdom = new XDOM(List.of((Block) new MetaDataBlock(imageBlocks, metaData)));

        // This is the part of the test verification: we verify that the passed Resource Reference has its base
        // reference set.
        ResourceReference reference = new ResourceReference("reference", ResourceType.ATTACHMENT);
        reference.addBaseReference("original base");
        when(this.mockWikiModel.getImageURL(any(ResourceReference.class), any())).thenReturn("imageurl");

        this.renderer.setPrinter(new DefaultWikiPrinter());
        xdom.traverse(this.renderer);

        verify(this.mockWikiModel).getImageURL(reference, Map.of());
    }
}
