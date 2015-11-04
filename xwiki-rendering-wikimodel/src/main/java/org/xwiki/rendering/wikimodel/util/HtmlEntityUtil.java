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
package org.xwiki.rendering.wikimodel.util;

import java.util.HashMap;
import java.util.Map;

/**
 * This class provides utility methods to access HTML entities with their
 * respective descriptions and numerical codes.
 *
 * @version $Id$
 * @since 4.0M1
 */
public class HtmlEntityUtil
{
    private static class CharInfo
    {
        int fCode;

        String fDescription;

        String fSymbol;

        CharInfo(String symbol, int code, String description)
        {
            fSymbol = symbol;
            fCode = code;
            fDescription = description;
        }
    }

    /**
     * This map contains codes of entity symbols
     */
    private static Map fCodeMap = new HashMap();

    /**
     * This map contains descriptions of entities
     */
    private static Map fSymbolMap = new HashMap();

    static {
        // For more information see http://www.w3.org/TR/WD-entities-961125
        add("nbsp", 160, "no-break space");
        add("iexcl", 161, "inverted exclamation mark");
        add("cent", 162, "cent sign");
        add("pound", 163, "pound sterling sign");
        add("curren", 164, "general currency sign");
        add("yen", 165, "yen sign");
        add("brvbar", 166, "broken (vertical) bar");
        add("sect", 167, "section sign");
        add("uml", 168, "umlaut (dieresis)");
        add("copy", 169, "copyright sign");
        add("ordf", 170, "ordinal indicator, feminine");
        add("laquo", 171, "angle quotation mark, left");
        add("not", 172, "not sign");
        add("shy", 173, "soft hyphen");
        add("reg", 174, "registered sign");
        add("macr", 175, "macron");
        add("deg", 176, "degree sign");
        add("plusmn", 177, "plus-or-minus sign");
        add("sup2", 178, "superscript two");
        add("sup3", 179, "superscript three");
        add("acute", 180, "acute accent");
        add("micro", 181, "micro sign");
        add("para", 182, "pilcrow (paragraph sign)");
        add("middot", 183, "middle dot");
        add("cedil", 184, "cedilla");
        add("sup1", 185, "superscript one");
        add("ordm", 186, "ordinal indicator, masculine");
        add("raquo", 187, "angle quotation mark, right");
        add("frac14", 188, "fraction one-quarter");
        add("frac12", 189, "fraction one-half");
        add("frac34", 190, "fraction three-quarters");
        add("iquest", 191, "inverted question mark");
        add("Agrave", 192, "capital A, grave accent");
        add("Aacute", 193, "capital A, acute accent");
        add("Acirc", 194, "capital A, circumflex accent");
        add("Atilde", 195, "capital A, tilde");
        add("Auml", 196, "capital A, dieresis or umlaut mark");
        add("Aring", 197, "capital A, ring");
        add("AElig", 198, "capital AE diphthong (ligature)");
        add("Ccedil", 199, "capital C, cedilla");
        add("Egrave", 200, "capital E, grave accent");
        add("Eacute", 201, "capital E, acute accent");
        add("Ecirc", 202, "capital E, circumflex accent");
        add("Euml", 203, "capital E, dieresis or umlaut mark");
        add("Igrave", 204, "capital I, grave accent");
        add("Iacute", 205, "capital I, acute accent");
        add("Icirc", 206, "capital I, circumflex accent");
        add("Iuml", 207, "capital I, dieresis or umlaut mark");
        add("ETH", 208, "capital Eth, Icelandic");
        add("Ntilde", 209, "capital N, tilde");
        add("Ograve", 210, "capital O, grave accent");
        add("Oacute", 211, "capital O, acute accent");
        add("Ocirc", 212, "capital O, circumflex accent");
        add("Otilde", 213, "capital O, tilde");
        add("Ouml", 214, "capital O, dieresis or umlaut mark");
        add("times", 215, "multiply sign");
        add("Oslash", 216, "capital O, slash");
        add("Ugrave", 217, "capital U, grave accent");
        add("Uacute", 218, "capital U, acute accent");
        add("Ucirc", 219, "capital U, circumflex accent");
        add("Uuml", 220, "capital U, dieresis or umlaut mark");
        add("Yacute", 221, "capital Y, acute accent");
        add("THORN", 222, "capital THORN, Icelandic");
        add("szlig", 223, "small sharp s, German (sz ligature)");
        add("agrave", 224, "small a, grave accent");
        add("aacute", 225, "small a, acute accent");
        add("acirc", 226, "small a, circumflex accent");
        add("atilde", 227, "small a, tilde");
        add("auml", 228, "small a, dieresis or umlaut mark");
        add("aring", 229, "small a, ring");
        add("aelig", 230, "small ae diphthong (ligature)");
        add("ccedil", 231, "small c, cedilla");
        add("egrave", 232, "small e, grave accent");
        add("eacute", 233, "small e, acute accent");
        add("ecirc", 234, "small e, circumflex accent");
        add("euml", 235, "small e, dieresis or umlaut mark");
        add("igrave", 236, "small i, grave accent");
        add("iacute", 237, "small i, acute accent");
        add("icirc", 238, "small i, circumflex accent");
        add("iuml", 239, "small i, dieresis or umlaut mark");
        add("eth", 240, "small eth, Icelandic");
        add("ntilde", 241, "small n, tilde");
        add("ograve", 242, "small o, grave accent");
        add("oacute", 243, "small o, acute accent");
        add("ocirc", 244, "small o, circumflex accent");
        add("otilde", 245, "small o, tilde");
        add("ouml", 246, "small o, dieresis or umlaut mark");
        add("divide", 247, "divide sign");
        add("oslash", 248, "small o, slash");
        add("ugrave", 249, "small u, grave accent");
        add("uacute", 250, "small u, acute accent");
        add("ucirc", 251, "small u, circumflex accent");
        add("uuml", 252, "small u, dieresis or umlaut mark");
        add("yacute", 253, "small y, acute accent");
        add("thorn", 254, "small thorn, Icelandic");
        add("yuml", 255, "small y, dieresis or umlaut mark");

        // Mathematical, Greek and Symbolic characters for HTML");
        /*
         * Portions International Organization for Standardization 1986:
         * Permission to copy in any form is granted for use with conforming
         * SGML systems and applications as defined in ISO 8879, provided this
         * notice is included in all copies.
         */

        /*
         * Relevant ISO entity set is given unless names are newly introduced.
         * New names (ie, not in ISO 8879 list) do not clash with any existing
         * ISO 8879 entity names. Unicode character numbers are given for each
         * character, in hex, and are identical for Unicode 1.1 and Unicode 2.0.
         * CDATA values are decimal conversions of the Unicode values. Names are
         * Unicode 2.0 names.
         */

        // Latin Extended-B
        add(
            "fnof",
            192,
            "latin small f with hook, =function, =florin, U0192 ISOtech");

        // Greek
        add("Alpha", 913, "greek capital letter alpha,  U0391");
        add("Beta", 914, "greek capital letter beta,  U0392");
        add("Gamma", 915, "greek capital letter gamma,  U0393 ISOgrk3");
        add("Delta", 916, "greek capital letter delta,  U0394 ISOgrk3");
        add("Epsilon", 917, "greek capital letter epsilon,  U0395");
        add("Zeta", 918, "greek capital letter zeta,  U0396");
        add("Eta", 919, "greek capital letter eta,  U0397");
        add("Theta", 920, "greek capital letter theta,  U0398 ISOgrk3");
        add("Iota", 921, "greek capital letter iota,  U0399");
        add("Kappa", 922, "greek capital letter kappa,  U039A");
        add("Lambda", 923, "greek capital letter lambda,  U039B ISOgrk3");
        add("Mu", 924, "greek capital letter mu,  U039C");
        add("Nu", 925, "greek capital letter nu,  U039D");
        add("Xi", 926, "greek capital letter xi,  U039E ISOgrk3");
        add("Omicron", 927, "greek capital letter omicron,  U039F");
        add("Pi", 928, "greek capital letter pi,  U03A0 ISOgrk3");
        add("Rho", 929, "greek capital letter rho,  U03A1");
        // there is no Sigmaf, and no U03A2 character either
        add("Sigma", 931, "greek capital letter sigma,  U03A3 ISOgrk3");
        add("Tau", 932, "greek capital letter tau,  U03A4");
        add("Upsi", 933, "greek capital letter upsilon,  U03A5 ISOgrk3");
        add("Phi", 934, "greek capital letter phi,  U03A6 ISOgrk3");
        add("Chi", 935, "greek capital letter chi,  U03A7");
        add("Psi", 936, "greek capital letter psi,  U03A8 ISOgrk3");
        add("Omega", 937, "greek capital letter omega,  U03A9 ISOgrk3");

        add("alpha", 945, "greek small letter alpha, U03B1 ISOgrk3");
        add("beta", 946, "greek small letter beta,  U03B2 ISOgrk3");
        add("gamma", 947, "greek small letter gamma,  U03B3 ISOgrk3");
        add("delta", 948, "greek small letter delta,  U03B4 ISOgrk3");
        add("epsi", 949, "greek small letter epsilon,  U03B5 ISOgrk3");
        // why not 'epsilon' ?? how to remember which three are truncated??
        add("zeta", 950, "greek small letter zeta,  U03B6 ISOgrk3");
        add("eta", 951, "greek small letter eta,  U03B7 ISOgrk3");
        add("theta", 952, "greek small letter theta,  U03B8 ISOgrk3");
        add("iota", 953, "greek small letter iota,  U03B9 ISOgrk3");
        add("kappa", 954, "greek small letter kappa,  U03BA ISOgrk3");
        add("lambda", 955, "greek small letter lambda,  U03BB ISOgrk3");
        add("mu", 956, "greek small letter mu,  U03BC ISOgrk3");
        add("nu", 957, "greek small letter nu,  U03BD ISOgrk3");
        add("xi", 958, "greek small letter xi,  U03BE ISOgrk3");
        add("omicron", 959, "greek small letter omicron,  U03BF NEW");
        add("pi", 960, "greek small letter pi,  U03C0 ISOgrk3");
        add("rho", 961, "greek small letter rho,  U03C1 ISOgrk3");
        add("sigmaf", 962, "greek small letter final sigma,  U03C2 ISOgrk3");
        add("sigma", 963, "greek small letter sigma,  U03C3 ISOgrk3");
        add("tau", 964, "greek small letter tau,  U03C4 ISOgrk3");
        add("upsi", 965, "greek small letter upsilon,  U03C5 ISOgrk3");
        // why not 'upsilon'
        add("phi", 966, "greek small letter phi,  U03C6 ISOgrk3");
        add("chi", 967, "greek small letter chi,  U03C7 ISOgrk3");
        add("psi", 968, "greek small letter psi,  U03C8 ISOgrk3");
        add("omega", 969, "greek small letter omega,  U03C9 ISOgrk3");
        add("theta", 977, "greek small letter theta symbol,  U03D1 NEW");
        add("upsih", 978, "greek upsilon with hook symbol,  U03D2 NEW");
        add("piv", 982, "greek pi symbol,  U03D6 ISOgrk3");

        // General Punctuation
        add("bull", 8226, "bullet, =black small circle, U2022 ISOpub ");
        // bullet is NOT the same as bullet operator, U2219
        add(
            "hellip",
            8230,
            "horizontal ellipsis, =three dot leader, U2026 ISOpub ");
        add("prime", 8242, "prime, =minutes, =feet, U2032 ISOtech");
        add("Prime", 8243, "double prime, =seconds, =inches, U2033 ISOtech");
        add("oline", 8254, "overline, =spacing overscore, U203E NEW");
        add("frasl", 8260, "fraction slash, U2044 NEW");

        // Letterlike Symbols
        add(
            "weierp",
            8472,
            "script capital P, =power set, =Weierstrass p, U2118 ISOamso");
        add(
            "image",
            8465,
            "blackletter capital I, =imaginary part, U2111 ISOamso");
        add(
            "real",
            8476,
            "blackletter capital R, =real part symbol, U211C ISOamso");
        add("trade", 8482, "trade mark sign, U2122 ISOnum");
        add(
            "alefsym",
            8501,
            "alef symbol, =first transfinite cardinal, U2135 NEW");
        // alef symbol is NOT the same as hebrew letter alef, U05D0 although the
        // same glyph could be used to depict both characters

        // Arrows
        add("larr", 8592, "leftwards arrow, U2190 ISOnum");
        add("uarr", 8593, "upwards arrow, U2191 ISOnum");
        add("rarr", 8594, "rightwards arrow, U2192 ISOnum");
        add("darr", 8595, "downwards arrow, U2193 ISOnum");
        add("harr", 8596, "left right arrow, U2194 ISOamsa");
        add(
            "crarr",
            8629,
            "downwards arrow with corner leftwards, =carriage return, U21B5 NEW");
        add("lArr", 8656, "leftwards double arrow, U21D0 ISOtech");
        // Unicode does not say that lArr is the same as the 'is implied by'
        // arrow but also does not have any other character for that function.
        // So ? lArr can be used for 'is implied by' as ISOtech suggests
        add("uArr", 8657, "upwards double arrow, U21D1 ISOamsa");
        add("rArr", 8658, "rightwards double arrow, U21D2 ISOtech");
        // Unicode does not say this is the 'implies' character but does not
        // have another character with this function so ? rArr can be used for
        // 'implies' as ISOtech suggests
        add("dArr", 8659, "downwards double arrow, U21D3 ISOamsa");
        add("hArr", 8660, "left right double arrow, U21D4 ISOamsa");

        // Mathematical Operators
        add("forall", 8704, "for all, U2200 ISOtech");
        add("part", 8706, "partial differential, U2202 ISOtech ");
        add("exist", 8707, "there exists, U2203 ISOtech");
        add("empty", 8709, "empty set, =null set, =diameter, U2205 ISOamso");
        add("nabla", 8711, "nabla, =backward difference, U2207 ISOtech");
        add("isin", 8712, "element of, U2208 ISOtech");
        add("notin", 8713, "not an element of, U2209 ISOtech");
        add("ni", 8715, "contains as member, U220B ISOtech");
        // should there be a more memorable name than 'ni'?
        add("prod", 8719, "n-ary product, =product sign, U220F ISOamsb");
        // prod is NOT the same character as U03A0 'greek capital letter pi'
        // though the same glyph might be used for both
        add("sum", 8722, "n-ary sumation, U2211 ISOamsb");
        // sum is NOT the same character as U03A3 'greek capital letter sigma'
        // though the same glyph might be used for both
        add("minus", 8722, "minus sign, U2212 ISOtech");
        add("lowast", 8727, "asterisk operator, U2217 ISOtech");
        add("radic", 8730, "square root, =radical sign, U221A ISOtech");
        add("prop", 8733, "proportional to, U221D ISOtech");
        add("infin", 8734, "infinity, U221E ISOtech");
        add("ang", 8736, "angle, U2220 ISOamso");
        add("and", 8869, "logical and, =wedge, U2227 ISOtech");
        add("or", 8870, "logical or, =vee, U2228 ISOtech");
        add("cap", 8745, "intersection, =cap, U2229 ISOtech");
        add("cup", 8746, "union, =cup, U222A ISOtech");
        add("int", 8747, "integral, U222B ISOtech");
        add("there4", 8756, "therefore, U2234 ISOtech");
        add(
            "sim",
            8764,
            "tilde operator, =varies with, =similar to, U223C ISOtech");
        // tilde operator is NOT the same character as the tilde, U007E,
        // although the same glyph might be used to represent both
        add("cong", 8773, "approximately equal to, U2245 ISOtech");
        add("asymp", 8773, "almost equal to, =asymptotic to, U2248 ISOamsr");
        add("ne", 8800, "not equal to, U2260 ISOtech");
        add("equiv", 8801, "identical to, U2261 ISOtech");
        add("le", 8804, "less-than or equal to, U2264 ISOtech");
        add("ge", 8805, "greater-than or equal to, U2265 ISOtech");
        add("sub", 8834, "subset of, U2282 ISOtech");
        add("sup", 8835, "superset of, U2283 ISOtech");
        // note that nsup, 'not a superset of, U2283' is not covered by the
        // Symbol font encoding and is not included. Should it be, for symmetry?
        // It is in ISOamsn
        add("nsub", 8836, "not a subset of, U2284 ISOamsn");
        add("sube", 8838, "subset of or equal to, U2286 ISOtech");
        add("supe", 8839, "superset of or equal to, U2287 ISOtech");
        add("oplus", 8853, "circled plus, =direct sum, U2295 ISOamsb");
        add("otimes", 8855, "circled times, =vector product, U2297 ISOamsb");
        add(
            "perp",
            8869,
            "up tack, =orthogonal to, =perpendicular, U22A5 ISOtech");
        add("sdot", 8901, "dot operator, U22C5 ISOamsb");
        // dot operator is NOT the same character as U00B7 middle dot

        // Miscellaneous Technical
        add("lceil", 8968, "left ceiling, =apl upstile, U2308, ISOamsc ");
        add("rceil", 8969, "right ceiling, U2309, ISOamsc ");
        add("lfloor", 8970, "left floor, =apl downstile, U230A, ISOamsc ");
        add("rfloor", 8971, "right floor, U230B, ISOamsc ");
        add("lang", 9001, "left-pointing angle bracket, =bra, U2329 ISOtech");
        // lang is NOT the same character as U003C 'less than' or U2039 'single
        // left-pointing angle quotation mark'
        add("rang", 9002, "right-pointing angle bracket, =ket, U232A ISOtech");
        // rang is NOT the same character as U003E 'greater than' or U203A
        // 'single right-pointing angle quotation mark'

        // Geometric Shapes
        add("loz", 9674, "lozenge, U25CA ISOpub");

        // Miscellaneous Symbols
        add("spades", 9824, "black spade suit, U2660 ISOpub");
        // black here seems to mean filled as opposed to hollow
        add("clubs", 9827, "black club suit, =shamrock, U2663 ISOpub");
        add("hearts", 9829, "black heart suit, =valentine, U2665 ISOpub");
        add("diams", 9830, "black diamond suit, U2666 ISOpub");

        // Appendix C: Character Entities for special symbols and BIDI text

        // Special characters for HTML

        // Character entity set.

        /*
         * Portions International Organization for Standardization 1986:
         * Permission to copy in any form is granted for use with conforming
         * SGML systems and applications as defined in ISO 8879, provided this
         * notice is included in all copies.
         */

        /*
         * Relevant ISO entity set is given unless names are newly introduced.
         * New names (ie, not in ISO 8879 list) do not clash with any existing
         * ISO 8879 entity names. Unicode character numbers are given for each
         * character, in hex, and are identical for Unicode 1.1 and Unicode 2.0.
         * CDATA values are decimal conversions of the Unicode values. Names are
         * Unicode 2.0 names. C0 Controls and Basic Latin
         */
        add("quot", 34, "quotation mark, =apl quote, U0022 ISOnum");
        add("amp", 38, "ampersand, U0026 ISOnum");
        add("lt", 60, "less-than sign, U003C ISOnum");
        add("gt", 62, "greater-than sign, U003E ISOnum");

        // Latin Extended-A
        add("OElig", 338, "latin capital ligature oe, U0152 ISOlat2");
        add("oelig", 339, "latin small ligature oe, U0153 ISOlat2");
        // ligature is a misnomer, this is a separate character in some
        // languages
        add("Scaron", 352, "latin capital letter s with caron, U0160 ISOlat2");
        add("scaron", 353, "latin small letter s with caron, U0161 ISOlat2");
        add("Yuml", 376, "latin capital letter y with diaeresis, U0178 ISOlat2");

        // Spacing Modifier Letters
        add("circ", 710, "modifier letter circumflex accent, U02C6 ISOpub");
        add("tilde", 732, "small tilde, U02DC ISOdia");

        // General Punctuation");
        add("ensp", 8194, "en space, U2002 ISOpub");
        add("emsp", 8195, "em space, U2003 ISOpub");
        add("thinsp", 8201, "thin space, U2009 ISOpub");
        add("zwnj", 8204, "zero width non-joiner, U200C NEW RFC 2070");
        add("zwj", 8205, "zero width joiner, U200D NEW RFC 2070");
        add("lrm", 8206, "left-to-right mark, U200E NEW RFC 2070");
        add("rlm", 8207, "right-to-left mark, U200F NEW RFC 2070");
        add("ndash", 8211, "en dash, U2013 ISOpub");
        add("mdash", 8212, "em dash, U2014 ISOpub");
        add("lsquo", 8216, "left single quotation mark, U2018 ISOnum");
        add("rsquo", 8217, "right single quotation mark, U2019 ISOnum");
        add("sbquo", 8218, "single low-9 quotation mark, U201A NEW");
        add("ldquo", 8220, "left double quotation mark, U201C ISOnum");
        add("rdquo", 8221, "right double quotation mark, U201D ISOnum");
        add("bdquo", 8222, "double low-9 quotation mark, U201E NEW");
        add("dagger", 8224, "dagger, U2020 ISOpub");
        add("Dagger", 8225, "double dagger, U2021 ISOpub");
        add("permil", 8240, "per mille sign, U2030 ISOtech");
        add(
            "lsaquo",
            8249,
            "single left-pointing angle quotation mark, U2039 ISO proposed");
        // lsaquo is proposed but not yet ISO standardised");
        add(
            "rsaquo",
            8250,
            "single right-pointing angle quotation mark, U203A ISO proposed");
        // rsaquo is proposed but not yet ISO standardised");

    }

    private static void add(String symbol, int code, String descr)
    {
        CharInfo info = new CharInfo(symbol, code, descr);
        fCodeMap.put(code, info);
        fSymbolMap.put(symbol, info);
    }

    /**
     * A character corresponding to the given symbol.
     *
     * @param symbol for this symbol the corresponding character will be
     * returned
     * @return a character corresponding to the given symbol
     */
    public static char getChar(String symbol)
    {
        CharInfo info = (CharInfo) fSymbolMap.get(symbol);
        return (info != null) ? (char) info.fCode : '\0';
    }

    /**
     * Returns description of the given character
     *
     * @param ch for this character the corresponding description will be
     * returned
     * @return description of the given character
     */
    public static String getDescription(char ch)
    {
        CharInfo info = (CharInfo) fCodeMap.get(Integer.valueOf(ch));
        return (info != null) ? info.fDescription : null;
    }

    /**
     * Returns description of the given symbol.
     *
     * @param symbol for this symbol the corresponding description will be
     * returned
     * @return description of the given symbol
     */
    public static String getDescription(String symbol)
    {
        CharInfo info = (CharInfo) fSymbolMap.get(symbol);
        return (info != null) ? info.fDescription : null;
    }

    /**
     * Returns a symbol corresponding to the given character or
     * <code>null</code> if nothing was found
     *
     * @param ch for this character the corresponding symbol will be returned
     * @return a symbol corresponding to the given character
     */
    public static String getSymbol(char ch)
    {
        CharInfo info = (CharInfo) fCodeMap.get(Integer.valueOf(ch));
        return (info != null) ? info.fSymbol : null;
    }
}
