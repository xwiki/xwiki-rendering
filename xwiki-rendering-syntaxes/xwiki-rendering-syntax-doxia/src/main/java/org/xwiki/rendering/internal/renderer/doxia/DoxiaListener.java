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
package org.xwiki.rendering.internal.renderer.doxia;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import org.apache.maven.doxia.sink.Sink;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.QueueListener;
import org.xwiki.rendering.listener.WrappingListener;

/**
 * Bridge XWiki Rendering Events to Doxia Events. This allows rendering an XDOM using a Doxia Sink.
 * <p>
 * Note that the implementation is broken into this class and {@link DoxiaSinkListener} since we need to handle
 * the special case of Tables: Doxia requires an extra event to be sent (tableRows()) which requires us to count
 * the number of rows since we need to pass it as the number of justification elements.
 * </p>
 *
 * @version $Id$
 * @since 4.3M1
 */
public class DoxiaListener extends WrappingListener
{
    /**
     * The stack of Listener which allow to push some queue listener when we find some tables in order to count rows
     * and send the tableRows event to the Doxia Sink.
     */
    private Deque<Listener> listenerStack = new ArrayDeque<Listener>();

    /**
     * Count the number of rows. Note that we use a stack since we can have nested tables.
     */
    private Deque<Integer> rowCountStack = new ArrayDeque<Integer>();

    /**
     * Number of tables being handled.
     */
    private int tableCount;

    /**
     * If true then this listener is in unstacking mode and the stacked events are being unstacked.
     */
    private boolean unstacking;

    /**
     * @see #DoxiaListener(org.apache.maven.doxia.sink.Sink)
     */
    private Sink sink;

    /**
     * @param sink the underlying Doxia sink into which we emit the Table Rows events.
     */
    public DoxiaListener(Sink sink)
    {
        this.sink = sink;
        pushListener(new DoxiaSinkListener(sink));
    }

    @Override
    public void beginTable(Map<String, String> parameters)
    {
        if (this.unstacking) {
            // We need to send a begin table rows event to the Doxia Sink since it requires such an Event before the
            // Row events.
            int rowCount = this.rowCountStack.peek();
            int[] justifications = new int[rowCount];
            for (int i = 0; i < rowCount; i++) {
                justifications[i] = Sink.JUSTIFY_CENTER;
            }
            super.beginTable(parameters);
            sink.tableRows(justifications, true);
        } else {
            // Start stacking Events since we need to count the number of table rows
            if (this.tableCount == 0) {
                pushListener(new QueueListener());
                this.rowCountStack.push(0);
            }
            this.tableCount++;
            super.beginTable(parameters);
        }
    }

    @Override
    public void endTable(Map<String, String> parameters)
    {
        if (!this.unstacking) {
            super.endTable(parameters);

            this.tableCount--;

            // Unstack if we're on the end table event of the first table
            if (this.tableCount == 0) {
                // Stop stacking Events
                QueueListener queueListener = (QueueListener) popListener();

                // Send all stacked Events to the current listener so that we can handle nested tables.
                this.unstacking = true;
                queueListener.consumeEvents(this);
                this.unstacking = false;
            }
        } else {
            sink.tableRows_();
            this.rowCountStack.pop();
            super.endTable(parameters);
        }
    }

    @Override
    public void beginTableRow(Map<String, String> parameters)
    {
        super.beginTableRow(parameters);

        // Increase row count
        if (!this.unstacking) {
            int rowCount = this.rowCountStack.pop();
            rowCount++;
            this.rowCountStack.push(rowCount);
        }
    }

    /**
     * Push a new Listener.
     *
     * @param listener the new listener that becomes the current active listener
     */
    private void pushListener(Listener listener)
    {
        this.listenerStack.push(listener);
        setWrappedListener(listener);
    }

    /**
     * Removes the last listener from the stack.
     *
     * @return the last listener that has been removed from the stack
     */
    private Listener popListener()
    {
        Listener listener = this.listenerStack.pop();
        if (!this.listenerStack.isEmpty()) {
            setWrappedListener(this.listenerStack.peek());
        }
        return listener;
    }

    /**
     * @return the last listener in the stack
     */
    private Listener peekListener()
    {
        return this.listenerStack.peek();
    }
}
