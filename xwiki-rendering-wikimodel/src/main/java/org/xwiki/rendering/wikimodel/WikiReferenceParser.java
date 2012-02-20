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
 * This class is used as a common parser of references. It is used to transform
 * references found in wiki documents into corresponding structured objects -
 * {@link WikiReference}. Methods of this class should be overloaded to parse
 * correctly wiki-specific references.
 *
 * @version $Id$
 * @since 4.0M1
 * @see WikiReference
 */
public class WikiReferenceParser implements IWikiReferenceParser
{
    /**
     * Extracts the label from the array of chunks and returns it.
     *
     * @param chunks the array of chunks
     * @return a label extracted from the given array of chunks
     */
    protected String getLabel(String[] chunks)
    {
        return chunks.length > 1 ? chunks[1].trim() : null;
    }

    /**
     * Extracts the link from the array of chunks and returns it.
     *
     * @param chunks the array of chunks
     * @return a link extracted from the given array of chunks
     */
    protected String getLink(String[] chunks)
    {
        return chunks[0].trim();
    }

    /**
     * Extracts parameters part of the original reference and returns it as a
     * WikiParameters.
     *
     * @param chunks the array of chunks
     * @return the parameters
     */
    protected WikiParameters getParameters(String[] chunks)
    {
        return WikiParameters.newWikiParameters(chunks.length > 2 ? chunks[2]
            .trim() : null);
    }

    /**
     * @see IWikiReferenceParser#parse(java.lang.String)
     */
    public WikiReference parse(String str)
    {
        if (str == null) {
            return null;
        }

        String[] chunks = splitToChunks(str);
        if (chunks.length == 0) {
            return null;
        }

        String link = getLink(chunks);
        String label = getLabel(chunks);
        WikiParameters parameters = getParameters(chunks);

        return new WikiReference(link, label, parameters);
    }

    /**
     * Returns the given string split to individual segments
     *
     * @param str the string to split
     * @return the given string split to individual segments
     */
    protected String[] splitToChunks(String str)
    {
        String delimiter = "[|>]";
        String[] chunks = str.split(delimiter);
        return chunks;
    }
}