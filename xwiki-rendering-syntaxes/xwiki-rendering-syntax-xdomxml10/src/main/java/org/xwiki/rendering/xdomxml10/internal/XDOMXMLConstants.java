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
package org.xwiki.rendering.xdomxml10.internal;

import java.util.regex.Pattern;

/**
 * Constants used by the XDOM+XML 1.0 syntax parsers and renderers.
 *
 * @version $Id$
 */
public final class XDOMXMLConstants
{
    /**
     * Name of the XML element representing a block.
     */
    public static final String ELEM_BLOCK = "block";

    /**
     * Name of the XML attribute holding the block name.
     */
    public static final String ATT_BLOCK_NAME = "name";

    /**
     * Name of the XML attribute holding the block version.
     */
    public static final String ATT_BLOCK_VERSION = "version";

    /**
     * Name of the XML element representing a group of parameters.
     */
    public static final String ELEM_PARAMETERS = "parameters";

    /**
     * Name of the XML element representing a single parameter.
     */
    public static final String ELEM_PARAMETER = "parameter";

    /**
     * Pattern matching valid XML element names.
     */
    public static final Pattern VALID_ELEMENTNAME = Pattern.compile("[A-Za-z][A-Za-z0-9:_.-]*");

    private XDOMXMLConstants()
    {
    }
}
