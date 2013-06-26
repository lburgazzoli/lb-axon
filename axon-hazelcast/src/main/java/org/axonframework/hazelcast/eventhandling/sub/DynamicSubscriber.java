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
package org.axonframework.hazelcast.eventhandling.sub;

import com.google.common.collect.Sets;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Instance;
import com.hazelcast.core.InstanceEvent;
import com.hazelcast.core.InstanceListener;
import org.axonframework.domain.EventMessage;
import org.axonframework.hazelcast.IHzInstanceProxy;
import org.axonframework.hazelcast.eventhandling.HzEventBusTerminal;
import org.axonframework.hazelcast.eventhandling.IHzTopicSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 *
 */
public class DynamicSubscriber implements IHzTopicSubscriber, InstanceListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicSubscriber.class);

    private final Set<String> m_topicNames;
    private IHzInstanceProxy m_proxy;
    private HzEventBusTerminal m_terminal;

    /**
     * c-tor
     */
    public DynamicSubscriber() {
        m_topicNames = Sets.newHashSet();
        m_proxy      = null;
        m_terminal   = null;
    }

    /**
     * c-tor
     *
     * @param topicNames
     */
    public DynamicSubscriber(String... topicNames) {
        m_topicNames = Sets.newHashSet(topicNames);
        m_proxy      = null;
        m_terminal   = null;
    }

    /**
     * c-tor
     *
     * @param topicNames
     */
    public DynamicSubscriber(List<String> topicNames) {
        m_topicNames = Sets.newHashSet(topicNames);
        m_proxy      = null;
        m_terminal   = null;
    }

    @Override
    public void subscribe(IHzInstanceProxy proxy,HzEventBusTerminal terminal) {
        m_proxy    = proxy;
        m_terminal = terminal;

        if(m_terminal != null && m_proxy != null) {
            m_proxy.getInstance().addInstanceListener(this);
        }

        for(Instance instance : m_proxy.getDistributedObjects(Instance.InstanceType.TOPIC)) {
            subscribeTopic(instance);
        }
    }

    @Override
    public void unsubscribe(IHzInstanceProxy proxy,HzEventBusTerminal terminal) {
        for(Instance instance : m_proxy.getDistributedObjects(Instance.InstanceType.TOPIC)) {
            unsubscribeTopic(instance);
        }

        m_proxy    = null;
        m_terminal = null;
    }

    @Override
    @SuppressWarnings("uncheked")
    public void instanceCreated(InstanceEvent event) {
        Instance instance = event.getInstance();
        if(instance.getInstanceType() == Instance.InstanceType.TOPIC) {
            subscribeTopic(instance);
        }
    }

    @Override
    @SuppressWarnings("uncheked")
    public void instanceDestroyed(InstanceEvent event) {
        Instance instance = event.getInstance();
        if(instance.getInstanceType() == Instance.InstanceType.TOPIC) {
            unsubscribeTopic(instance);
        }
    }

    /**
     *
     * @param instance
     */
    @SuppressWarnings("unchecked")
    private void subscribeTopic(Instance instance) {
        String name = ((ITopic<?>)instance).getName();
        for(String topicName : m_topicNames) {
            if(name.matches(topicName)) {
                LOGGER.debug("Subscribing to <{}>",name);
                ((ITopic<EventMessage>)instance).addMessageListener(m_terminal);
            }
        }
    }

    /**
     *
     * @param instance
     */
    @SuppressWarnings("unchecked")
    private void unsubscribeTopic(Instance instance) {
        String name = ((ITopic<?>)instance).getName();
        for(String topicName : m_topicNames) {
            if(name.matches(topicName)) {
                LOGGER.debug("Unsubscribing from <{}>",name);
                ((ITopic<EventMessage>)instance).removeMessageListener(m_terminal);
            }
        }
    }
}