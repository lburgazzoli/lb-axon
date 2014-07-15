/*
 * Copyright (c) 2010-2014. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.axonframework.ext.chronicle.store;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptAppender;
import org.axonframework.domain.DomainEventMessage;
import org.axonframework.serializer.SerializedObject;
import org.axonframework.serializer.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.axonframework.ext.eventstore.CloseableDomainEventWriter;

/**
 *
 */
public class ChronicleDomainEventWriter<T> implements CloseableDomainEventWriter<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChronicleDomainEventWriter.class);

    private final Serializer m_serializer;
    private ExcerptAppender m_excerpt;

    /**
     * c-tor
     *
     * @param chronicle  the Chronicle
     * @param serializer the DomainEventStream serializer
     */
    public ChronicleDomainEventWriter(Chronicle chronicle, Serializer serializer) {
        m_serializer = serializer;

        try {
            m_excerpt = chronicle.createAppender();
        } catch (Exception e) {
            m_excerpt = null;
            LOGGER.warn("CreateAppender - Exception",e);
        }
    }

    /**
     * TODO: check serialization
     *
     * @param message the mesage to persist
     */
    @Override
    public void write(final DomainEventMessage<T> message) {
        if(m_excerpt != null) {
            SerializedObject<byte[]> data = m_serializer.serialize(message,byte[].class);

            m_excerpt.startExcerpt(4 + data.getData().length);
            m_excerpt.writeInt(data.getData().length);
            m_excerpt.write(data.getData());
            m_excerpt.finish();
        }
    }

    @Override
    public void close() {
        if(m_excerpt != null) {
            m_excerpt.close();
            m_excerpt = null;
        }
    }
}
