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
package org.xwiki.rendering.block;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.util.IdGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link XDOM}.
 *
 * @version $Id$
 */
class XDOMTest
{
    private IdGenerator idGenerator = new IdGenerator();

    @Test
    void setIdGenerator()
    {
        HeaderBlock heading1 = new HeaderBlock(List.of(new WordBlock("Heading 1")), HeaderLevel.LEVEL1);
        heading1.setId("Hheading");

        ImageBlock image1 = new ImageBlock(null, true);
        image1.setId("Ilogo");
        ParagraphBlock paragraph1 = new ParagraphBlock(List.of(image1));

        HeaderBlock heading2 = new HeaderBlock(List.of(new WordBlock("Heading 2")), HeaderLevel.LEVEL2);
        heading2.setId("Hheading-2");

        ImageBlock image2 = new ImageBlock(null, true);
        image2.setId("Ilogo-2");
        ParagraphBlock paragraph2 = new ParagraphBlock(List.of(image2));

        HeaderBlock heading3 = new HeaderBlock(List.of(new WordBlock("Heading 3")), HeaderLevel.LEVEL3);
        heading3.setId("Hheading");

        ImageBlock image3 = new ImageBlock(null, true);
        image3.setId("Ilogo");
        ParagraphBlock paragraph3 = new ParagraphBlock(List.of(image3));

        HeaderBlock heading4 = new HeaderBlock(List.of(new WordBlock("Heading 4")), HeaderLevel.LEVEL1);
        ImageBlock image4 = new ImageBlock(null, true);
        ParagraphBlock paragraph4 = new ParagraphBlock(List.of(image4));

        XDOM xdom =
            new XDOM(
                List.of(
                    new SectionBlock(List.of(heading1, paragraph1,
                        new SectionBlock(
                            List.of(heading2, paragraph2, new SectionBlock(List.of(heading3, paragraph3)))))),
                    new SectionBlock(List.of(heading4, paragraph4))));

        // Suppose the id generated has already been used to generated some ids.
        idGenerator.generateUniqueId("H", "heading");
        idGenerator.generateUniqueId("logo");

        // Set the id generator without adapting the existing ids.
        xdom.setIdGenerator(idGenerator);

        assertEquals("Hheading", heading1.getId());
        assertEquals("Hheading-2", heading2.getId());
        assertEquals("Hheading", heading3.getId());
        assertNull(heading4.getId());

        assertEquals("Ilogo", image1.getId());
        assertEquals("Ilogo-2", image2.getId());
        assertEquals("Ilogo", image3.getId());
        assertNull(image4.getId());

        // Set the same id generator again. The existing ids are not adapted because it's the same id generator.
        xdom.setIdGenerator(idGenerator, true);

        assertEquals("Hheading", heading1.getId());
        assertEquals("Hheading-2", heading2.getId());
        assertEquals("Hheading", heading3.getId());
        assertNull(heading4.getId());

        assertEquals("Ilogo", image1.getId());
        assertEquals("Ilogo-2", image2.getId());
        assertEquals("Ilogo", image3.getId());
        assertNull(image4.getId());

        // Set a new id generator and adapt the existing ids.
        xdom.setIdGenerator(new IdGenerator(idGenerator), true);

        assertEquals("Hheading-1", heading1.getId());
        assertEquals("Hheading-2", heading2.getId());
        assertEquals("Hheading-3", heading3.getId());
        assertNull(heading4.getId());

        assertEquals("Ilogo-1", image1.getId());
        assertEquals("Ilogo-2", image2.getId());
        assertEquals("Ilogo-3", image3.getId());
        assertNull(image4.getId());

        // Verify that setting a null id generator doesn't change the existing ids.
        xdom.setIdGenerator(null, true);

        assertEquals("Hheading-1", heading1.getId());
        assertEquals("Hheading-2", heading2.getId());
        assertEquals("Hheading-3", heading3.getId());
        assertNull(heading4.getId());

        assertEquals("Ilogo-1", image1.getId());
        assertEquals("Ilogo-2", image2.getId());
        assertEquals("Ilogo-3", image3.getId());
        assertNull(image4.getId());

        // Set again a new id generator without adapting the existing ids.
        xdom.setIdGenerator(idGenerator, false);

        assertEquals("Hheading-1", heading1.getId());
        assertEquals("Hheading-2", heading2.getId());
        assertEquals("Hheading-3", heading3.getId());
        assertNull(heading4.getId());

        assertEquals("Ilogo-1", image1.getId());
        assertEquals("Ilogo-2", image2.getId());
        assertEquals("Ilogo-3", image3.getId());
        assertNull(image4.getId());
    }
}
