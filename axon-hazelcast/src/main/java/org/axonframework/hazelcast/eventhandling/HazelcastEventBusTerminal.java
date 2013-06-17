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
package org.axonframework.hazelcast.eventhandling;

import com.google.common.collect.Sets;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import org.axonframework.domain.EventMessage;
import org.axonframework.eventhandling.Cluster;
import org.axonframework.eventhandling.EventBusTerminal;
import org.axonframework.hazelcast.IHazelcastInstanceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * EventBusTerminal implementation that uses an Hazelcast to dispatch event messages.
 *
 * All outgoing messages are sent to a topic matching the <code>EventMessage.getPayloadType()</code>.
 * This terminal does not dispatch Events internally, as it relies on each cluster
 * to listen to the topics of interest.
 *
 * @author Luca Burgazzoli
 */
public class HazelcastEventBusTerminal implements EventBusTerminal,MessageListener<EventMessage> {
    private final static Logger LOGGER = LoggerFactory.getLogger(HazelcastEventBusTerminal.class);

    private final IHazelcastInstanceProxy m_proxy;
    private final Set<Cluster> m_clusters;
    private IHazelcastTopicPublisher m_publisher;
    private IHazelcastTopicSubscriber m_subscriber;

    /**
     * c-tor
     *
     * @param proxy the hazelcast proxy
     */
    public HazelcastEventBusTerminal(IHazelcastInstanceProxy proxy) {
        m_proxy      = proxy;
        m_clusters   = Sets.newHashSet();
        m_publisher  = null;
        m_subscriber = null;
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     * @param publisher the topics of interest
     */
    public void setPublisher(IHazelcastTopicPublisher publisher) {
        m_publisher = publisher;
    }

    /**
     * @param subscriber the topics of interest
     */
    public void setSubscriber(IHazelcastTopicSubscriber subscriber) {
        m_subscriber = subscriber;
        m_subscriber.subscribe(m_proxy,this);
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void publish(EventMessage... events) {
        for(EventMessage event : events) {
            m_publisher.publish(event);
        }
    }

    @Override
    public void onClusterCreated(Cluster cluster) {
        LOGGER.debug("ClusterCreated: <{}>",cluster.getName());
        m_clusters.add(cluster);
    }

    @Override
    public void onMessage(Message<EventMessage> event) {
        for(Cluster cluster : m_clusters) {
            cluster.publish(event.getMessageObject());
        }
    }
}
