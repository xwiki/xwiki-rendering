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
package org.xwiki.rendering.wikimodel.util.tmp;

import org.xwiki.rendering.wikimodel.util.tmp.TreeBuilder1.ITreeBuilderListener;

import junit.framework.TestCase;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class TreeBuilder1Test extends TestCase
{
    final StringBuffer fBuf = new StringBuffer();

    TreeBuilder1<String> fBuilder;

    /**
     *
     */
    public TreeBuilder1Test()
    {
    }

    /**
     * @param name
     */
    public TreeBuilder1Test(String name)
    {
        super(name);
    }

    /**
     * @param control
     */
    private void check(String control)
    {
        assertEquals(control, fBuf.toString());
        fBuf.delete(0, fBuf.length());
    }

    @Override
    protected void setUp() throws Exception
    {
        ITreeBuilderListener<String> listener = new ITreeBuilderListener<String>()
        {
            public void beginItem(int depth, String data)
            {
                fBuf.append("<" + data + ">");
            }

            public void beginLevel(int depth, String prevBegin)
            {
                fBuf.append("<level>");
            }

            public void endItem(int depth, String data)
            {
                fBuf.append("</" + data + ">");
            }

            public void endLevel(int i, String prevBegin)
            {
                fBuf.append("</level>");
            }
        };
        fBuilder = new TreeBuilder1<String>(listener);
    }

    public void test() throws Exception
    {
        test(0, "a", "<level><a>");
        test(30, "b", "<level><b>");
        test(10, "c", "</b><c>");

        test(30, "d", "<level><d>");
        test(15, "e", "</d><e>");
        test(8, "f", "</e></level></c><f>");
        test(8, "g", "</f><g>");

        fBuilder.trim(10);
        check("");
        fBuilder.trim(8, false);
        check("");
        fBuilder.finish();
        check("</g></level></a></level>");
    }

    /**
     * @param value
     * @param control
     * @param string
     */
    private void test(int value, String data, String control)
    {
        fBuilder.align(value, data);
        check(control);
    }
}
