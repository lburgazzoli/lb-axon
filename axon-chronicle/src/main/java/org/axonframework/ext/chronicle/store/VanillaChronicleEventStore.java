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
import net.openhft.chronicle.VanillaChronicleConfig;
import org.axonframework.ext.eventstore.CloseableDomainEventStore;
import org.axonframework.serializer.Serializer;
import org.axonframework.serializer.xml.XStreamSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class VanillaChronicleEventStore<T> extends ChronicleEventStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(VanillaChronicleEventStore.class);

    private final VanillaChronicleConfig m_chronicleConfig;

    /**
     * c-tor
     *
     * @param basePath the Chronicle base path
     */
    public VanillaChronicleEventStore(String basePath) {
        this(basePath, new XStreamSerializer(), VanillaChronicleConfig.DEFAULT.clone());
    }

    /**
     * c-tor
     *
     * @param basePath          the Chronicle base path
     * @param chronicleConfig   the Chronicle configurations
     */
    public VanillaChronicleEventStore(String basePath, VanillaChronicleConfig chronicleConfig) {
        this(basePath, new XStreamSerializer(), chronicleConfig);
    }

    /**
     * c-tor
     *
     * @param basePath   the Chronicle base path
     * @param serializer the DomainEventStream serializer
     */
    public VanillaChronicleEventStore(String basePath, final Serializer serializer) {
        this(basePath, serializer, VanillaChronicleConfig.DEFAULT.clone());
    }

    /**
     * c-tor
     *
     * @param basePath          the Chronicle base path
     * @param serializer        the DomainEventStream serializer
     * @param chronicleConfig   the Chronicle configurations
     */
    public VanillaChronicleEventStore(String basePath, final Serializer serializer, VanillaChronicleConfig chronicleConfig) {
        super(basePath, serializer);

        m_chronicleConfig = chronicleConfig;
    }

    // *************************************************************************
    //
    // *************************************************************************

    protected ChronicleDomainEventStore<T> createDomainEventStore(
        Serializer serializer, String basePath, String storageId, String aggregateType, String aggregateId) {
        return new VanillaChronicleDomainEventStore<>(
            serializer,
            basePath,
            storageId,
            aggregateType,
            aggregateId,
            m_chronicleConfig
        );
    }
}
