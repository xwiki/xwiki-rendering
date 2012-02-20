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

/**
 * This interface contains main styles used to define text formatting. Note that
 * individual parsers can extends this set by adding new styles.
 *
 * @version $Id$
 * @since 4.0M1
 */
public interface IWemConstants
{
    /**
     * Bigger font
     *
     * @see "http://www.w3.org/TR/xhtml1/dtds.html#dtdentry_xhtml1-strict.dtd_big"
     */
    WikiStyle BIG = new WikiStyle("big");
    /**
     * Inline citation.
     *
     * @see "http://www.w3.org/TR/xhtml1/dtds.html#dtdentry_xhtml1-strict.dtd_cite"
     */
    WikiStyle CITE = new WikiStyle("cite");
    /**
     * Program code.
     *
     * @see "http://www.w3.org/TR/xhtml1/dtds.html#dtdentry_xhtml1-strict.dtd_code"
     */
    WikiStyle CODE = new WikiStyle("code");
    /**
     * Deleted Text
     *
     * @see "http://www.w3.org/TR/xhtml1/dtds.html#dtdentry_xhtml1-strict.dtd_del"
     */
    WikiStyle DEL = new WikiStyle("del");
    /**
     * Emphasis (should be used as a replacement for the "i" element).
     *
     * @see "http://www.w3.org/TR/xhtml1/dtds.html#dtdentry_xhtml1-strict.dtd_em"
     */
    WikiStyle EM = new WikiStyle("em");
    /**
     * Inserted Text
     *
     * @see "http://www.w3.org/TR/xhtml1/dtds.html#dtdentry_xhtml1-strict.dtd_ins"
     */
    WikiStyle INS = new WikiStyle("ins");
    /**
     * FIXME: there is no such a symbol. Should be replaced by the {@link #TT}
     * element.
     */
    WikiStyle MONO = new WikiStyle("mono");
    /**
     * References... FIXME: check what does it mean... I (kotelnikov) did not
     * found any references on such an HTML element. This style should be
     * removed or replaced by something else.
     */
    WikiStyle REF = new WikiStyle("ref");
    /**
     * Smaller font.
     *
     * @see "http://www.w3.org/TR/xhtml1/dtds.html#dtdentry_xhtml1-strict.dtd_small"
     */
    WikiStyle SMALL = new WikiStyle("small");
    /**
     * Strike-through
     *
     * @see "http://www.w3.org/TR/xhtml1/dtds.html#dtdentry_xhtml1-strict.dtd_strike"
     */
    WikiStyle STRIKE = new WikiStyle("strike");
    /**
     * Strong emphasis.
     *
     * @see "http://www.w3.org/TR/xhtml1/dtds.html#dtdentry_xhtml1-strict.dtd_strong"
     */
    WikiStyle STRONG = new WikiStyle("strong");
    /**
     * Subscript
     *
     * @see "http://www.w3.org/TR/xhtml1/dtds.html#dtdentry_xhtml1-strict.dtd_sub"
     */
    WikiStyle SUB = new WikiStyle("sub");
    /**
     * Superscript
     *
     * @see "http://www.w3.org/TR/xhtml1/dtds.html#dtdentry_xhtml1-strict.dtd_sup"
     */
    WikiStyle SUP = new WikiStyle("sup");
    /**
     * Fixed pitch font.
     *
     * @see "http://www.w3.org/TR/xhtml1/dtds.html#dtdentry_xhtml1-strict.dtd_tt"
     */
    WikiStyle TT = new WikiStyle("tt");
}
