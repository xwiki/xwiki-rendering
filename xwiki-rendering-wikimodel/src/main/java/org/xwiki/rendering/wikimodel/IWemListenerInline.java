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
 * This method re-groups all methods used to notify about in-line elements. All inline elements should be "wrapped" in a
 * formats.
 *
 * <pre>
 * So a normal sequence of notifications is:
 *  * beginFormat(...)
 *    - onWord(...)
 *    - onSpace(...)
 *    - onReference(...)
 *    - onWord(...)
 *    - ...
 *  * endFormat(...)
 *  * beginFormat(...)
 *    - onWord(...)
 *    - ...
 *  * endFormat(...)
 * </pre>
 *
 * All inline elements can be splitted in the following categories:
 * <dl>
 *   <dt>Simple text elements</dt>
 *   <dd>These elements are notified using the following set of methods: {@link #onNewLine()}, {@link #onSpace(String)},
 *   {@link #onWord(String)} and {@link #onSpecialSymbol(String)}. This methods cover "simple text" and in sum they are
 *   used to represent all types of "natural" elements in the text - words, spaces, new lines and special symbols.
 *   Using only these methods it is possible to cover all combinations of all unicode characters. Parsers use these
 *   methods to notify about all non-interpreted symbols and their combinations. These methods calls can be considered
 *   as the base of any text documents.</dd>
 *   <dt>Logical in-line elements</dt>
 *   <dd>These elements require interpretation of some syntax-specific formatting and are notified using the following
 *   methods: {@link #onEscape(String)}, {@link #onLineBreak()}, {@link #onReference(String)}/
 *   {@link #onReference(WikiReference)}, {@link #onVerbatimInline(String, WikiParameters)}</dd>
 * </dl>
 *
 * @version $Id$
 * @since 4.0M1
 */
public interface IWemListenerInline
{
    /**
     * This method is called at the beginning of a sequence of in-line elements having the specified formatting
     * parameters.
     *
     * @param format the object defining formatting parameters of in-line elements.
     */
    void beginFormat(WikiFormat format);

    /**
     * This method is called to notify about the end of a sequence of in-line elements having common formatting
     * parameters.
     *
     * @param format the formatting object defining how contained in-line elements should be formatted
     */
    void endFormat(WikiFormat format);

    /**
     * Escaped symbols. More frequently the given string has just one symbol.
     *
     * @param str the escaped sequence of characters
     */
    void onEscape(String str);

    /**
     * This method is called to notify that an free standing image was found in the parsed wiki document.
     *
     * @param ref the reference the reference
     */
    void onImage(String ref);

    /**
     * This method is called to notify that a structured reference was found in the text
     *
     * @param ref the reference the reference
     */
    void onImage(WikiReference ref);

    /**
     * This method is called to notify about a forced line break found in the text. Note that the line break symbol can
     * be found in the middle of a "physical" line so this event is not equals to the {@link #onNewLine()}
     * notification.
     *
     * @see #onNewLine()
     */
    void onLineBreak();

    /**
     * This method is called to notify that the parsed block contains a new line sequence ({@code "\r\n"} or {@code
     * "\r"} or {@code "\n"} character sequence). Note that the new line symbols are not the same as a forced line break
     * sequence notified by the {@link #onLineBreak()} event.
     *
     * @see #onSpace(String)
     * @see #onWord(String)
     * @see #onSpecialSymbol(String)
     * @see #onLineBreak()
     */
    void onNewLine();

    /**
     * This method is called to notify that an URI (an implicit reference) was found in the parsed wiki document.
     *
     * @param ref the URI
     */
    void onReference(String ref);

    /**
     * This method is called to notify that a structured reference was found in the text
     *
     * @param ref the reference the reference
     */
    void onReference(WikiReference ref);

    /**
     * This method is called to notify about a sequence of space symbols (like {@code " "} or {@code "\t"} symbols).
     *
     * @param str the sequence of space characters
     * @see #onWord(String)
     * @see #onSpecialSymbol(String)
     * @see #onNewLine()
     */
    void onSpace(String str);

    /**
     * This method is called to notify about a sequence of special characters. Special symbols are characters which are
     * not interpreted as a part of a word (letters or digits) or as a space. Note that the handling of these symbols
     * requires special attention because these symbols most frequently used to define text formatting. Various wiki
     * syntaxes use combinations of these sequences to define structural elements in wiki documents. <p> The full list
     * of possible special symbols:
     * <pre>{@code
     *  "!",     "\"",     "#",     "$",     "%",     "&",     "'",     "(",
     *  ")",     "*",      "+",     ",",     "-",     ".",     "/",     ":",
     *  ";",     "<",      "=",     ">",     "?",     "@",     "[",     "\\",
     *  "]",     "^",      "_",     "`",     "{",     "|",     "}",     "~"
     * }</pre>
     *
     * @param str the sequence of special symbols
     * @see #onSpace(String)
     * @see #onWord(String)
     * @see #onNewLine()
     */
    void onSpecialSymbol(String str);

    /**
     * This method is called to notify about not-interpreted in-line sequence of characters which should be represented
     * in the final text "as is".
     *
     * @param str the sequence of non-interpreted characters
     * @param params the list of parameters for this event
     */
    void onVerbatimInline(String str, WikiParameters params);

    /**
     * This method is called to notify about a "word" found in the document. Words are formed by all characters which
     * are not considered as spaces or special symbols. <p> Words are formed by the all characters excluding the
     * following ones:
     * <pre>{@code
     *  "\t",    "\n",     "\r",    " ",
     *  "!",     "\"",     "#",     "$",     "%",     "&",     "'",     "(",
     *  ")",     "*",      "+",     ",",     "-",     ".",     "/",     ":",
     *  ";",     "<",      "=",     ">",     "?",     "@",     "[",     "\\",
     *  "]",     "^",      "_",     "`",     "{",     "|",     "}",     "~"
     * }</pre>
     *
     * @param str the sequence of characters forming a word
     * @see #onSpace(String)
     * @see #onSpecialSymbol(String)
     * @see #onNewLine()
     */
    void onWord(String str);
}
