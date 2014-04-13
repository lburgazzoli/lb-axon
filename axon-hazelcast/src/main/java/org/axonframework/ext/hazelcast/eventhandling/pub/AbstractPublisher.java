/*
 * Copyright (c) 2010-2013. Axon Framework
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
package org.axonframework.ext.hazelcast.eventhandling.pub;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import org.axonframework.domain.EventMessage;
import org.axonframework.ext.hazelcast.eventhandling.IHzTopicPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public abstract class AbstractPublisher implements IHzTopicPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPublisher.class);

    /**
     * c-tor
     */
    public AbstractPublisher() {
    }

    /**
     * c-tor
     *
     * @param hzInstance the Hazelcast instance
     * @param event      the event
     */
    public void publish(final HazelcastInstance hzInstance,final EventMessage event) {
        String               topicName = resolve(event);
        ITopic<EventMessage> topic     = hzInstance.getTopic(topicName);

        LOGGER.debug("Publish event <{}> to {}",event.getIdentifier(),topic.getName());
        topic.publish(event);
    }

    protected abstract String resolve(EventMessage event);
}
