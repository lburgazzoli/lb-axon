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
package org.axonframework.ext.hazelcast.eventhandling.sub;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.DistributedObjectEvent;
import com.hazelcast.core.DistributedObjectListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import org.apache.commons.lang3.StringUtils;
import org.axonframework.domain.EventMessage;
import org.axonframework.ext.hazelcast.eventhandling.HzEventBusTerminal;
import org.axonframework.ext.hazelcast.eventhandling.IHzTopicSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class DynamicSubscriber implements IHzTopicSubscriber, DistributedObjectListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicSubscriber.class);

    private final Set<String> m_topicNames;
    private final Map<String,String> m_subKeys;
    private HazelcastInstance m_hzInstance;
    private HzEventBusTerminal m_terminal;

    /**
     * c-tor
     */
    public DynamicSubscriber() {
        m_topicNames = Sets.newHashSet();
        m_hzInstance = null;
        m_terminal   = null;
        m_subKeys    = Maps.newHashMap();
    }

    /**
     * c-tor
     *
     * @param topicNames the topic names
     */
    public DynamicSubscriber(String... topicNames) {
        m_topicNames = Sets.newHashSet(topicNames);
        m_hzInstance = null;
        m_terminal   = null;
        m_subKeys    = Maps.newHashMap();
    }

    /**
     * c-tor
     *
     * @param topicNames the topic names
     */
    public DynamicSubscriber(List<String> topicNames) {
        m_topicNames = Sets.newHashSet(topicNames);
        m_hzInstance = null;
        m_terminal   = null;
        m_subKeys    = Maps.newHashMap();
    }

    @Override
    public void subscribe(final HazelcastInstance hzInstance,final HzEventBusTerminal terminal) {
        m_hzInstance = hzInstance;
        m_terminal   = terminal;

        if(m_terminal != null && m_hzInstance != null) {
            m_hzInstance.addDistributedObjectListener(this);
        }

        for(DistributedObject object : m_hzInstance.getDistributedObjects()) {
            if(object instanceof ITopic) {
                subscribeTopic(object);
            }
        }
    }

    @Override
    public void unsubscribe(final HazelcastInstance hzInstance,final HzEventBusTerminal terminal) {
        for(DistributedObject object : hzInstance.getDistributedObjects()) {
            if(object instanceof ITopic) {
                unsubscribeTopic(object);
            }
        }

        m_hzInstance = null;
        m_terminal   = null;
    }

    @Override
    @SuppressWarnings("uncheked")
    public void distributedObjectCreated(DistributedObjectEvent event) {
        DistributedObject object = event.getDistributedObject();
        if(object instanceof ITopic) {
            subscribeTopic(object);
        }
    }

    @Override
    @SuppressWarnings("uncheked")
    public void distributedObjectDestroyed(DistributedObjectEvent event) {
        DistributedObject object = event.getDistributedObject();
        if(object instanceof ITopic) {
            unsubscribeTopic(object);
        }
    }

    /**
     *
     * @param object the objects
     */
    @SuppressWarnings("unchecked")
    private void subscribeTopic(DistributedObject object) {
        String name = object.getName();
        for(String topicName : m_topicNames) {
            if(name.matches(topicName)) {
                LOGGER.debug("Subscribing to <{}>",name);
                m_subKeys.put(
                    topicName,
                    ((ITopic<EventMessage>) object).addMessageListener(m_terminal));
            }
        }
    }

    /**
     *
     * @param object the objects
     */
    @SuppressWarnings("unchecked")
    private void unsubscribeTopic(DistributedObject object) {
        String name = object.getName();
        for(String topicName : m_topicNames) {
            if(name.matches(topicName)) {
                LOGGER.debug("Unsubscribing from <{}>",name);

                String key = m_subKeys.remove(topicName);
                if(StringUtils.isNotEmpty(key)) {
                    ((ITopic<EventMessage>)object).removeMessageListener(key);
                }
            }
        }
    }
}