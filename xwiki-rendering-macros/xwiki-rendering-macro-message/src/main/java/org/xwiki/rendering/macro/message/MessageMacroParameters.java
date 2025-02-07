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
package org.xwiki.rendering.macro.message;

import org.xwiki.properties.annotation.PropertyAdvanced;
import org.xwiki.properties.annotation.PropertyDescription;
import org.xwiki.rendering.macro.box.BoxMacroParameters;
import org.xwiki.stability.Unstable;

/**
 * Parameters for the Message macro.
 *
 * @version $Id$
 * @since 17.1.0RC1
 */

@Unstable
public class MessageMacroParameters extends BoxMacroParameters 
{
    /**
     * @see #isStatus()
     */
    private boolean status;

    /**
     * Set your macro as a status message when you use it to display temporary feedback. The status role will make sure 
     * that users of assistive technologies will take notice of the content of the message 
     * even if they cannot see it appear. 
     * @since 17.1.0RC1
     * @return whether the current message is an ARIA status.
     */
    @Unstable
    public boolean isStatus()
    {
        return this.status;
    }

    /**
     * @since 17.0.0RC1
     * @param status refers to {@link #isStatus()}
     */
    @PropertyDescription("Whether this message should be set as an ARIA status.")
    @PropertyAdvanced
    @Unstable
    public void setStatus(boolean status)
    {
        this.status = status;
    }
}
