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

package org.xwiki.rendering.internal.parser.xhtml.wikimodel;

/**
 * Common interface to all XWiki Tag Handler.
 *
 * @version $Id$
 */
public interface XWikiWikiModelHandler
{
    /**
     * Stack parameter which is true between startimage: and stopimage: comment.
     */
    String IS_IN_IMAGE = "isInImage";

    /**
     * Stack parameter which is true when the short form syntax is used for parsed image (image:... vs [[image:...]]).
     */
    String IS_FREE_STANDING_IMAGE = "isFreeStandingImage";

    /**
     * Stack parameter which hold image parameters during image parsing.
     */
    String IMAGE_PARAMETERS = "imageParameters";

    /**
     * Stack parameter which is true between startwikilink: and stopwikilink: comment.
     */
    String IS_IN_LINK = "isInLink";

    /**
     * Stack parameter which is true when the short form syntax is used for the parsed link.
     * (ie: http://... vs [[http://...]])
     */
    String IS_FREE_STANDING_LINK = "isFreeStandingLink";

    /**
     * Stack parameter which hold link parameters during link parsing.
     */
    String LINK_PARAMETERS = "linkParameters";

    /**
     * Stack parameter which hold the instance of the WikiModel listener bridge for link being parsed.
     */
    String LINK_LISTENER = "linkListener";

    /**
     * Stack parameter which hold the instances of the MacroInfo encountered.
     */
    String MACRO_INFO = "macroInfo";

    /**
     * Stack parameters which records the syntax metadata encountered in the document.
     */
    String CURRENT_SYNTAX = "currentSyntax";

    /**
     * Stack parameters which records if the previous div or span was triggered by an unchanged content metadata.
     */
    String UNCHANGED_CONTENT_STACK = "unchangedContentStack";
}
