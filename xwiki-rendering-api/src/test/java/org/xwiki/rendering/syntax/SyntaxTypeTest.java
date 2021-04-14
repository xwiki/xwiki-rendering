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
package org.xwiki.rendering.syntax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.parser.ParseException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link SyntaxType}.
 *
 * @version $Id$
 * @since 8.2M1
 */
class SyntaxTypeTest
{
    @Test
    void constructorWithNullVariants()
    {
        SyntaxType syntaxType = new SyntaxType("id", null, "name");
        assertEquals("id", syntaxType.getId());
        assertEquals("name", syntaxType.getName());
        assertTrue(syntaxType.getVariants().isEmpty());
    }

    @Test
    void getters()
    {
        SyntaxType syntaxType1 = new SyntaxType("id", "name");
        SyntaxType syntaxType2 = new SyntaxType("id", Arrays.asList("variant1", "variant2"), "name");

        assertEquals("id", syntaxType1.getId());
        assertEquals("name", syntaxType1.getName());
        assertTrue(syntaxType1.getVariants().isEmpty());

        assertThat(syntaxType2.getVariants(), contains("variant1", "variant2"));
    }

    @Test
    void equalityAndInequality()
    {
        SyntaxType syntaxType1 = new SyntaxType("id", "name");
        SyntaxType syntaxType2 = new SyntaxType("id", "name");
        SyntaxType syntaxType3 = new SyntaxType("id", "othername");
        SyntaxType syntaxType4 = new SyntaxType("otherid", "name");
        SyntaxType syntaxType5 = new SyntaxType("id", Arrays.asList("variant1"), "name");
        SyntaxType syntaxType6 = new SyntaxType("id", Arrays.asList("variant1", "variant2"), "name");

        // Equality

        assertEquals(syntaxType2, syntaxType1);
        // The syntax type name is not part of the equality test.
        assertEquals(syntaxType3, syntaxType1);

        // Inequality

        assertFalse(syntaxType4.equals(syntaxType1));
        assertFalse(syntaxType6.equals(syntaxType5));
    }

    @Test
    void toStringValidation()
    {
        SyntaxType syntaxType1 = new SyntaxType("id", "Name");
        assertEquals("Name", syntaxType1.toString());
        assertEquals("id", syntaxType1.toIdString());

        SyntaxType syntaxType2 = new SyntaxType("id", Arrays.asList("variant1", "variant2"), "Name with variants");
        assertEquals("Name with variants", syntaxType2.toString());
        assertEquals("id+variant1+variant2", syntaxType2.toIdString());
    }

    @Test
    void getSyntaxTypes()
    {
        assertEquals(18, SyntaxType.getSyntaxTypes().size());
        assertEquals(new SyntaxType("xwiki", "XWiki"), SyntaxType.getSyntaxTypes().get("xwiki"));
    }

    @Test
    void comparisons()
    {
        SyntaxType syntaxType1 = new SyntaxType("mytype2", "BBB");
        SyntaxType syntaxType2 = new SyntaxType("mytype1", "BBB");
        SyntaxType syntaxType3 = new SyntaxType("mytype1", "AAA");
        SyntaxType syntaxType4 = new SyntaxType("mytype1", Arrays.asList("variant1", "variant2"), "AAA");
        SyntaxType syntaxType5 = new SyntaxType("mytype1", Arrays.asList("variant1"), "AAA");

        assertEquals(0, syntaxType1.compareTo(syntaxType1));

        List<SyntaxType> syntaxTypes = new ArrayList<>();
        syntaxTypes.add(syntaxType1);
        syntaxTypes.add(syntaxType2);
        syntaxTypes.add(syntaxType3);
        syntaxTypes.add(syntaxType4);
        syntaxTypes.add(syntaxType5);
        assertThat(syntaxTypes, contains(syntaxType1, syntaxType2, syntaxType3, syntaxType4, syntaxType5));
        Collections.sort(syntaxTypes);
        // The comparison is done only on the name!
        assertThat(syntaxTypes, contains(syntaxType3, syntaxType4, syntaxType5, syntaxType1, syntaxType2));
    }

    @Test
    void valueOfOk() throws Exception
    {
        SyntaxType syntaxType1 = SyntaxType.valueOf("id");
        assertEquals("id", syntaxType1.getId());
        assertEquals("id", syntaxType1.getName());
        assertTrue(syntaxType1.getVariants().isEmpty());

        SyntaxType syntaxType2 = SyntaxType.valueOf("id+variant1+variant2");
        assertEquals("id", syntaxType2.getId());
        assertEquals("id+variant1+variant2", syntaxType2.getName());
        assertThat(syntaxType2.getVariants(), contains("variant1", "variant2"));

        // Verify that if the syntax id exists in the registry but the variant doesn't exist, then it's not the existing
        // registry syntax type tha is used but a new type.
        // We test with "xwiki" as the main syntax id since it's a known syntax
        SyntaxType syntaxType3 = SyntaxType.valueOf("xwiki+testvariant");
        assertEquals("xwiki", syntaxType3.getId());
        assertEquals("xwiki+testvariant", syntaxType3.getName());
        assertThat(syntaxType3.getVariants(), contains("testvariant"));
    }

    @Test
    void valueOfWhenNull()
    {
        Throwable exception = assertThrows(ParseException.class, () -> SyntaxType.valueOf(null));
        assertEquals("The passed Syntax type cannot be NULL", exception.getMessage());
    }
}
