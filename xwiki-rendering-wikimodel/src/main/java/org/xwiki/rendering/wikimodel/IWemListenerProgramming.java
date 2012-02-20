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
 * This interface re-groups all listener methods related to document elements
 * which should be interpreted by the client code. The meaning of extensions and
 * macros is not defined by the WikiModel. The general recommended semantic of
 * macros - macros can be used to define interpreted/executable code in the
 * handled document. Extensions are used mostly to <em>call</em> already defined
 * somewhere code to insert back the results of these calls in the document.
 * handled document. But the exact semantic and associated actions of these
 * elements should be defined by the client code in an implementation-specific
 * way.
 *
 * @version $Id$
 * @since 4.0M1
 */
public interface IWemListenerProgramming
{
    /**
     * This method is used to notify about a new extension which CAN generate
     * block elements as a result of its interpretation.
     *
     * @param extensionName the name of the extension
     * @param params parameters for the extension
     */
    void onExtensionBlock(String extensionName, WikiParameters params);

    /**
     * This method is used to notify about a new extension which CAN generate
     * in-line elements as a result of its interpretation. This method CAN NOT
     * generate block elements.
     *
     * @param extensionName the name of the extension
     * @param params parameters for the extension
     */
    void onExtensionInline(String extensionName, WikiParameters params);

    /**
     * This method is used to notify about a new in-line macro which CAN
     * generate block elements as a result of its interpretation.
     *
     * @param macroName the name of the macro
     * @param params parameters of the macro
     * @param content the content of the macro
     */
    void onMacroBlock(String macroName, WikiParameters params, String content);

    /**
     * This method is used to notify about a new in-line macro which CAN
     * generate only in-line elements as a result of its interpretation. This
     * method CAN NOT generate block elements.
     *
     * @param macroName the name of the macro
     * @param params parameters of the macro
     * @param content the content of the macro
     */
    void onMacroInline(String macroName, WikiParameters params, String content);
}
