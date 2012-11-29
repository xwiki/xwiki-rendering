package org.xwiki.rendering.internal.parser.markdown;

import org.pegdown.ast.SuperNode;
import org.xwiki.component.annotation.Role;
import org.xwiki.rendering.block.XDOM;

/**
 *
 */
@Role
public interface PegdownToXDOMConverter {
    XDOM buildBlocks(SuperNode superNode);
}
