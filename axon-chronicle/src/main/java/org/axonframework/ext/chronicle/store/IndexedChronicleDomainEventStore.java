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


import net.openhft.chronicle.ChronicleConfig;
import net.openhft.chronicle.IndexedChronicle;
import org.apache.commons.io.FilenameUtils;
import org.axonframework.serializer.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class IndexedChronicleDomainEventStore<T> extends ChronicleDomainEventStore<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexedChronicleDomainEventStore.class);

    private final ChronicleConfig m_chronicleConfig;

    /**
     * c-tor
     *
     * @param serializer        the DomainEventStream serializer
     * @param basePath          the Chronicle data path
     * @param storageId         the Chronicle data name
     * @param aggregateType     the AggregateType
     * @param aggregateId       the AggregateId
     * @param chronicleConfig   the Chronicle config
     */
    public IndexedChronicleDomainEventStore(Serializer serializer, String basePath, String storageId, String aggregateType, String aggregateId, ChronicleConfig chronicleConfig) {
        super(serializer, basePath, storageId, aggregateType, aggregateId);

        m_chronicleConfig = chronicleConfig;
    }

    @Override
    public void init() {
        String dataPath = FilenameUtils.concat(getBasePath(), getStorageId());
        LOGGER.debug("IndexedChronicle => BasePath: {}, DataPath: {}", getBasePath(), dataPath);

        try {
            init(new IndexedChronicle(dataPath, m_chronicleConfig));
        } catch(Exception e) {
            LOGGER.warn("Exception",e);
        }
    }
}
