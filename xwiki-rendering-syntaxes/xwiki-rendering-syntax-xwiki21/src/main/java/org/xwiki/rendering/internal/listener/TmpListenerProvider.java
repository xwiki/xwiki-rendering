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
package org.xwiki.rendering.internal.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.listener.ListenerProvider;
import org.xwiki.rendering.listener.chaining.AbstractChainingListener;
import org.xwiki.rendering.listener.chaining.ChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.listener.reference.ResourceReference;

/**
 * TODO.
 *
 * @version $Id$
 * @since x.y.z
 */
@Component
@Singleton
public class TmpListenerProvider implements ListenerProvider
{
    private static class FigureStyleChainingListener extends AbstractChainingListener
    {
        private static final List<String> KNOWN_PARAMETERS = List.of(
            "data-xwiki-image-style",
            "width",
            "data-xwiki-image-style-alignment",
            "data-xwiki-image-style-border",
            "data-xwiki-image-style-text-wrap",
            "style"
        );

        private Map<String, String> additionalFigureParameters;

        /**
         * TODO.
         *
         * @param listenerChain TODO
         */
        protected FigureStyleChainingListener(ListenerChain listenerChain)
        {
            setListenerChain(listenerChain);
        }

        @Override
        public void beginFigure(Map<String, String> parameters)
        {
            Map<String, String> cleanedUpParameters = new HashMap<>(parameters);
            KNOWN_PARAMETERS.forEach(cleanedUpParameters::remove);
            super.beginFigure(cleanedUpParameters);
        }

        @Override
        public void onImage(ResourceReference reference, boolean freestanding, String id,
            Map<String, String> parameters)
        {
            this.additionalFigureParameters = new HashMap<>();
            KNOWN_PARAMETERS.forEach(knowParameter -> {
                if (parameters.containsKey(knowParameter)) {
                    this.additionalFigureParameters.put(knowParameter, parameters.get(knowParameter));
                }
            });

            super.onImage(reference, freestanding, id, parameters);
        }

        @Override
        public void endFigure(Map<String, String> parameters)
        {
            Map<String, String> withImageParams = new HashMap<>(parameters);
            if (this.additionalFigureParameters != null) {
                withImageParams.putAll(this.additionalFigureParameters);
                this.additionalFigureParameters = null;
            }
            super.endFigure(withImageParams);
        }
    }

    @Override
    public ChainingListener getListener(ListenerChain listenerChain)
    {
        return new FigureStyleChainingListener(listenerChain);
    }
}
