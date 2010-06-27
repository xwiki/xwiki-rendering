package org.xwiki.rendering.internal.parser.xml;

import java.io.Reader;

import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.internal.parser.XDOMGeneratorListener;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;

/**
 * @version $Id$
 */
public abstract class AbstractParser extends AbstractStreamParser implements Parser
{
    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.parser.Parser#parse(java.io.Reader)
     */
    public XDOM parse(Reader source) throws ParseException
    {
        XDOMGeneratorListener listener = new XDOMGeneratorListener();

        parse(source, listener);

        return listener.getXDOM();
    }
}
