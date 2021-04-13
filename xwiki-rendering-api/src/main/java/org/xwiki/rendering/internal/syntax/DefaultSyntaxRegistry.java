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
package org.xwiki.rendering.internal.syntax;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxRegistry;
import org.xwiki.rendering.syntax.SyntaxType;

/**
 * Default implementation of the Syntax Registry, storing the known syntaxes in memory.
 *
 * @version $Id$
 * @since 13.2RC1
 */
@Component
@Singleton
public class DefaultSyntaxRegistry implements SyntaxRegistry
{
    /**
     * Used to cut the syntax identifier into syntax name and syntax version.
     */
    private static final Pattern SYNTAX_PATTERN = Pattern.compile("(.*)/(.*)");

    private Map<String, Syntax> syntaxes = new HashMap<>();

    @Override
    public void registerSyntaxes(Syntax...syntaxes)
    {
        for (Syntax syntax : syntaxes) {
            registerSyntax(syntax);
        }
    }

    @Override
    public void unregisterSyntaxes(Syntax... syntaxes)
    {
        for (Syntax syntax : syntaxes) {
            unregisterSyntax(syntax);
        }
    }

    @Override
    public Map<String, Syntax> getSyntaxes()
    {
        return Collections.unmodifiableMap(this.syntaxes);
    }

    @Override
    public Optional<Syntax> getSyntax(String syntaxId)
    {
        return Optional.ofNullable(this.syntaxes.get(syntaxId));
    }

    @Override
    public Syntax resolveSyntax(String syntaxId) throws ParseException
    {
        // Try to find the syntax in the registered list and if not there, fallback to parsing the syntax id string.
        // However note that this means that the returned syntax's name type will default to the syntax id type.
        return getSyntax(syntaxId).orElse(valueOf(syntaxId));
    }

    private Syntax valueOf(String syntaxIdAsString) throws ParseException
    {
        if (syntaxIdAsString == null) {
            throw new ParseException("The passed Syntax cannot be NULL");
        }

        Matcher matcher = SYNTAX_PATTERN.matcher(syntaxIdAsString);
        if (!matcher.matches()) {
            throw new ParseException(String.format("Invalid Syntax format [%s]", syntaxIdAsString));
        }

        String syntaxId = matcher.group(1);
        String version = matcher.group(2);

        // For well-known syntaxes, get the Syntax Name from the registered SyntaxType, otherwise use the id as both
        // the human readable name and the technical id (since the syntax string doesn't contain any information about
        // the pretty name of a syntax type).
        SyntaxType syntaxType = SyntaxType.getSyntaxTypes().get(syntaxId);
        if (syntaxType == null) {
            syntaxType = new SyntaxType(syntaxId, syntaxId);
        }

        return new Syntax(syntaxType, version);
    }

    private void registerSyntax(Syntax syntax)
    {
        this.syntaxes.put(syntax.toIdString(), syntax);
    }

    private void unregisterSyntax(Syntax syntax)
    {
        this.syntaxes.remove(syntax.toIdString());
    }
}
