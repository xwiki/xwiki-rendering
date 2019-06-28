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
package org.xwiki.rendering.wikimodel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link ReferenceHandler}.
 *
 * @version $Id$
 * @since 4.0M1
 */
public class ReferenceHandlerTest
{
    private TestReferenceHandler clazz;

    @BeforeEach
    private void setUp()
    {
        this.clazz = new TestReferenceHandler(true, true);
    }

    @Test
    public void handleImageUppercase()
    {
        WikiReference ref = new WikiReference("Image:foo.png", "bar");
        clazz.handle(ref);
        assertEquals("foo.png", clazz.getImgRef());
        assertEquals("bar", clazz.getImgLabel());
    }

    @Test
    public void handleImageLowercase()
    {
        WikiReference ref = new WikiReference("image:bar.png", "foo");
        clazz.handle(ref);
        assertEquals("bar.png", clazz.getImgRef());
        assertEquals("foo", clazz.getImgLabel());
    }

    /*
     * ========================================================================
     */

    /**
     * @author mkirst(at portolancs dot com)
     */
    private static class TestReferenceHandler extends ReferenceHandler
    {
        private String imgRef;

        private String imgLabel;

        protected TestReferenceHandler(boolean supportImage,
            boolean supportDownload)
        {
            super(supportImage, supportDownload);
        }

        /*
         * (non-Javadoc)
         * 
         * @see ReferenceHandler#handleImage(java.lang.String,
         * java.lang.String, WikiParameters)
         */
        @Override
        protected void handleImage(String ref, String label,
            WikiParameters params)
        {
            imgRef = ref;
            imgLabel = label;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * ReferenceHandler#handleReference(java.lang.String,
         * java.lang.String, WikiParameters)
         */
        @Override
        protected void handleReference(String ref, String label,
            WikiParameters params)
        {
            // not interested in.
        }

        /**
         * @return the imgRef
         */
        public String getImgRef()
        {
            return imgRef;
        }

        /**
         * @return the imgLabel
         */
        public String getImgLabel()
        {
            return imgLabel;
        }
    }
}