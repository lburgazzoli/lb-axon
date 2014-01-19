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
package org.axonframework.eventstore.chronicle;

import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.IndexedChronicle;
import org.axonframework.domain.DomainEventMessage;
import org.axonframework.serializer.SerializedObject;
import org.axonframework.serializer.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ChronicleDomainEventWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChronicleDomainEventWriter.class);

    private final Serializer m_serializer;
    private ExcerptAppender m_excerpt;

    /**
     * c-tor
     *
     * @param serializer
     */
    public ChronicleDomainEventWriter(IndexedChronicle chronicle, Serializer serializer) {
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
     * @param message
     */
    public long write(DomainEventMessage message) {
        if(m_excerpt != null) {
            SerializedObject<byte[]> data = m_serializer.serialize(message,byte[].class);
            int len = data.getData().length;

            m_excerpt.startExcerpt(4 + len);
            m_excerpt.writeInt(len);
            m_excerpt.write(data.getData());
            m_excerpt.finish();

            return m_excerpt.lastWrittenIndex();
        }

        return -1;
    }
}
