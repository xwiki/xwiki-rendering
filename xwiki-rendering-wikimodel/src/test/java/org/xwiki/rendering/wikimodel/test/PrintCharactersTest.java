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
package org.xwiki.rendering.wikimodel.test;

import junit.framework.TestCase;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class PrintCharactersTest extends TestCase
{
    /**
     * @param name
     */
    public PrintCharactersTest(String name)
    {
        super(name);
    }

    public void test()
    {
        for (int i = 0; i < 256; i++) {
            if ((i % 16) == 0) {
                System.out.println();
            }
            System.out.print("\"");
            char ch = (char) i;
            String str = "";
            switch (ch) {
                case '\t':
                    str = "\\t";
                    break;
                case '\n':
                    str = "\\n";
                    break;
                case '\r':
                    str = "\\r";
                    break;
                case '\"':
                    str = "\\\"";
                    break;
                default:
                    str += ch;
                    break;
            }
            System.out.print(str);
            System.out.print("\", ");
        }
    }

    /**
     * <pre>
     | <#NEW_LINE : "\r\n" | "\r" | "\n" >
     | <#SPACE : [" ", "\t"] >
     | <#SPECIAL_SYMBOL : [
     "!",     "\"",     "#",     "$",     "%",     "&",     "'",     "(",
     ")",     "*",      "+",     ",",     "-",     ".",     "/",     ":",
     ";",     "<",      "=",     ">",     "?",     "@",     "[",     "\\",
     "]",     "^",      "_",     "`",     "{",     "|",     "}",     "~"
     ] >
     | <#CHAR : ~[
     "\t",    "\n",     "\r",    " ",
     "!",     "\"",     "#",     "$",     "%",     "&",     "'",     "(",
     ")",     "*",      "+",     ",",     "-",     ".",     "/",     ":",
     ";",     "<",      "=",     ">",     "?",     "@",     "[",     "\\",
     "]",     "^",      "_",     "`",     "{",     "|",     "}",     "~"
     ] >
     </pre>
     */
    /**
     * http://en.wikipedia.org/wiki/Uniform_Resource_Identifier
     * http://en.wikipedia.org/wiki/URI_scheme#Generic_syntax
     * http://www.iana.org/assignments/uri-schemes.html
     *
     * <pre>
     *
     *
     *
     * // URI syntax - see http://tools.ietf.org/html/rfc3986#page-49
     * <#URI: <URI_SCHEME> ":" <URI_HIER_PART> ("?" <URI_QUERY>)? ("#" <URI_FRAGMENT>)? >
     *
     * | <#ALPHA: ["A"-"z"]>
     * | <#DIGIT: ["0"-"9"]>
     * | <#HEXDIG: ( <DIGIT> | ["A"-"F"] | ["a"-"f"] ) >
     * | <#DEC_OCTET: (
     * <DIGIT>                  // 0-9
     * | ["1"-"9"] <DIGIT>      // 10-99
     * | "1" <DIGIT> <DIGIT>    // 100-199
     * | "2" ["0"-"4"] <DIGIT>  // 200-249
     * | "25" ["0"-"5"]         // 250-255
     * )>
     * | <#URI_GEN_DELIMS: [ ":", "/", "?", "#", "[", "]", "@" ]>
     * | <#URI_SUB_DELIMS: [ "!", "$", "&", "'", "(", ")", "*", "+", ",", ";", "=" ]>
     * | <#URI_UNRESERVED: ( <ALPHA> | <DIGIT> | "-" | "." | "_" | "~" )>
     * | <#URI_RESERVED: ( <URI_GEN_DELIMS> | <URI_SUB_DELIMS> ) >
     * | <#URI_SCHEME: <ALPHA> ( <ALPHA> | <DIGIT> | "+" | "-" | "." )* >
     * | <#URI_PCT_ENCODED: "%" <HEXDIG> <HEXDIG> >
     * | <#URI_PCHAR:  ( <URI_UNRESERVED> | <URI_PCT_ENCODED> | <URI_SUB_DELIMS> | ":" | "@" ) >
     * | <#URI_QUERY:    ( <URI_PCHAR> | "/" | "?" )* >
     * | <#URI_FRAGMENT: ( <URI_PCHAR> | "/" | "?" )* >
     *
     * | <#URI_AUTHORITY: ( <URI_USERINFO> "@" )? <URI_HOST> ( ":" <URI_PORT> )? >
     * | <#URI_USERINFO: ( <URI_UNRESERVED> | <URI_PCT_ENCODED> | <URI_SUB_DELIMS> | ":" )* >
     * | <#URI_HOST: ( <URI_IP_LITERAL> | <URI_IP_V4_ADDRESS> | <URI_REG_NAME> ) >
     * | <#URI_REG_NAME: ( <URI_UNRESERVED> | <URI_PCT_ENCODED> | <URI_SUB_DELIMS> )* >
     * | <#URI_PORT: (<DIGIT>)* >
     *
     * | <#URI_IP_LITERAL: "[" ( <URI_IP_V6_ADDRESS> | <URI_IP_V_FUTURE>  ) "]" >
     * | <#URI_IP_V_FUTURE : "v" ( <HEXDIG> )+ "." ( <URI_UNRESERVED> | <URI_SUB_DELIMS> | ":" )+ >
     * | <#URI_IP_V4_ADDRESS: <DEC_OCTET> "." <DEC_OCTET> "." <DEC_OCTET> "." <DEC_OCTET> >
     *
     * | <#URI_IP_V6_ADDRESS: (
     * ( <URI_H16> ":" ){6} <URI_LS32>
     * |                                       "::"  ( <URI_H16> ":" ){5} <URI_LS32>
     * | (                        <URI_H16> )? "::"  ( <URI_H16> ":" ){4} <URI_LS32>
     * | ( ( <URI_H16> ":" ){0,1} <URI_H16> )? "::"  ( <URI_H16> ":" ){3} <URI_LS32>
     * | ( ( <URI_H16> ":" ){0,2} <URI_H16> )? "::"  ( <URI_H16> ":" ){2} <URI_LS32>
     * | ( ( <URI_H16> ":" ){0,3} <URI_H16> )? "::"    <URI_H16> ":"      <URI_LS32>
     * | ( ( <URI_H16> ":" ){0,4} <URI_H16> )? "::"                       <URI_LS32>
     * | ( ( <URI_H16> ":" ){0,5} <URI_H16> )? "::"                       <URI_H16>
     * | ( ( <URI_H16> ":" ){0,6} <URI_H16> )? "::"
     * ) >
     * | <#URI_H16:  (<HEXDIG>){1,4} >
     * | <#URI_LS32: ( <URI_H16> ":" <URI_H16> ) | <URI_IP_V4_ADDRESS> >
     *
     * | <#URI_PATH_ABEMPTY: ( "/" <URI_SEGMENT> )* >
     * | <#URI_PATH_ABSOLUTE: "/" ( <URI_SEGMENT_NZ> ( "/" <URI_SEGMENT> )* )? >
     * | <#URI_PATH_ROOTLESS: <URI_SEGMENT_NZ> ( "/" <URI_SEGMENT> )* >
     * | <#URI_SEGMENT: (<URI_PCHAR>)* >
     * | <#URI_SEGMENT_NZ: (<URI_PCHAR>)+ >
     *
     * // A simplified URI definition: it does not contain an empty path.
     * | <#URI_HIER_PART: ( "//" <URI_AUTHORITY> <URI_PATH_ABEMPTY> | <URI_PATH_ABSOLUTE> | <URI_PATH_ROOTLESS> )>
     *
     * </pre>
     */
    public void test1()
    {
        System.out.println();
        System.out
            .println("===================================================");
        for (int i = 0; i < 127; i++) {
            char ch = (char) i;
            String str = "";
            switch (ch) {
                case '\t':
                    str = "\\t";
                    break;
                case '\n':
                    str = "\\n";
                    break;
                case '\r':
                    str = "\\r";
                    break;
                case '\\':
                    str = "\\\\";
                    break;
                case '\"':
                    str = "\\\"";
                    break;
                default:
                    str += ch;
                    break;
            }
            System.out.print("[" + Integer.toHexString(i) + "] - ");
            System.out.print("\"");
            System.out.print(str);
            System.out.print("\", ");
            if (Character.isSpaceChar(ch)) {
                System.out.print(" space");
            }
            if (Character.isLetter(ch)) {
                System.out.print(" letter");
            }
            if (Character.isLetterOrDigit(ch)) {
                System.out.print(" letterOrDigit");
            }
            if (Character.isDigit(ch)) {
                System.out.print(" digit");
            }
            if (Character.isWhitespace(ch)) {
                System.out.print(" whitespace");
            }
            System.out.println();
        }
    }
}
