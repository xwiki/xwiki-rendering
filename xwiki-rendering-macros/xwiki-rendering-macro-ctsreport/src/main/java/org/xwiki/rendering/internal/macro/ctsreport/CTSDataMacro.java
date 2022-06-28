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
package org.xwiki.rendering.internal.macro.ctsreport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.script.ScriptContext;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.macro.AbstractNoParameterMacro;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.script.ScriptContextManager;

/**
 * Parses CTS JUnit Results and make the generated data structures available in the Script Context for Script macros
 * to use.
 *
 * @version $Id$
 * @since 4.1M2
 */
@Component
@Named("ctsdata")
@Singleton
public class CTSDataMacro extends AbstractNoParameterMacro
{
    /**
     * The description of the macro.
     */
    private static final String DESCRIPTION =
        "Parses XWiki Rendering Compatibility Test Suite (CTS) JUnit Results";

    /**
     * The description of the macro content.
     */
    private static final String CONTENT_DESCRIPTION = "Textual results of CTS execution, one result per line";

    /**
     * The logger to log.
     */
    @Inject
    private Logger logger;

    @Inject
    private ScriptContextManager scriptContextManager;

    /**
     * Used to parse the macro content since it can contain wiki markup.
     */
    @Inject
    private MacroContentParser contentParser;

    /**
     * Used to render the macro content as text.
     */
    @Inject
    @Named("plain/1.0")
    private BlockRenderer plainTextRenderer;

    /**
     * Create and initialize the descriptor of the macro.
     */
    public CTSDataMacro()
    {
        super("CTS Data", DESCRIPTION, new DefaultContentDescriptor(CONTENT_DESCRIPTION));
        setDefaultCategories(Set.of(DEFAULT_CATEGORY_DEVELOPMENT));
    }

    @Override
    public boolean supportsInlineMode()
    {
        return true;
    }

    @Override
    public List<Block> execute(Object unusedParameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        // We consider the content as containing wiki syntax so we parse it and render it with the Plain text parser.
        XDOM xdom = this.contentParser.parse(content, context, true, false);
        DefaultWikiPrinter printer = new DefaultWikiPrinter();
        this.plainTextRenderer.render(xdom, printer);
        String parsedContent = printer.toString();

        // Parse the results
        TestParser parser = new TestParser();
        List<Result> results = new ArrayList<Result>();
        for (String resultLine : parsedContent.split("[\\r\\n]+")) {
            results.add(parser.parse(resultLine));
        }

        // Bind 2 variables in the Script Context so that they can be used by Script macros
        ScriptContext scriptContext = this.scriptContextManager.getCurrentScriptContext();
        if (scriptContext != null) {
            ResultExtractor extractor = new ResultExtractor();
            Set<String> testNames = extractor.extractByTestName(results);
            scriptContext.setAttribute("ctsTestNames", testNames, ScriptContext.ENGINE_SCOPE);
            Map<String, Pair<Set<Test>, Set<Test>>> tests = extractor.extractBySyntax(results);
            extractor.normalize(testNames, tests);
            scriptContext.setAttribute("ctsTests", tests, ScriptContext.ENGINE_SCOPE);
        } else {
            this.logger.warn("Script Context not found in the Execution Context. CTS Data variable not bound!");
        }

        return Collections.emptyList();
    }
}
