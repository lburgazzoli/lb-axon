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
package org.axonframework.ext.hazelcast.eventhandling;

import com.google.common.collect.Sets;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import org.axonframework.domain.EventMessage;
import org.axonframework.eventhandling.Cluster;
import org.axonframework.eventhandling.EventBusTerminal;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * EventBusTerminal implementation that uses Hazelcast to dispatch event messages.
 *
 * This terminal does not dispatch Events internally, as it relies on each cluster
 * to listen to the topics of interest.
 *
 * @author Luca Burgazzoli
 */
public class HzEventBusTerminal implements EventBusTerminal,MessageListener<EventMessage> {
    private final HazelcastInstance m_hzInstance;
    private final Set<Cluster> m_clusters;
    private final AtomicBoolean m_subscribed;

    private IHzTopicPublisher m_publisher;
    private IHzTopicSubscriber m_subscriber;

    /**
     * c-tor
     *
     * @param hzInstance the hazelcast instance
     */
    public HzEventBusTerminal(HazelcastInstance hzInstance) {
        this(hzInstance, null, null);
    }

    /**
     *
     * @param hzInstance the hazelcast instance
     * @param publisher  the data publisher
     * @param subscriber the data subscriber
     */
    public HzEventBusTerminal(HazelcastInstance hzInstance, IHzTopicPublisher publisher, IHzTopicSubscriber subscriber) {
        m_hzInstance = hzInstance;
        m_clusters   = Sets.newHashSet();
        m_publisher  = publisher;
        m_subscriber = subscriber;
        m_subscribed = new AtomicBoolean(false);
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     * @return true if the pubisher is set
     */
    public boolean hasPublisher() {
        return m_publisher != null;
    }

    /**
     * @param publisher the TopicPublisher
     */
    public void setPublisher(IHzTopicPublisher publisher) {
        m_publisher = publisher;
    }

    /**
     * @param subscriber the TopicSubscriber
     */
    public void setSubscriber(IHzTopicSubscriber subscriber) {
        if(m_subscriber != null) {
            m_subscriber.unsubscribe(m_hzInstance,this);
        }

        m_subscriber = subscriber;
    }

    /**
     *
     * @return true if the subscriber is set
     */
    public boolean hasSubscriber() {
        return m_subscriber != null;
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void publish(EventMessage... events) {
        if(m_publisher != null) {
            for(EventMessage event : events) {
                m_publisher.publish(m_hzInstance,event);
            }
        }
    }

    @Override
    public void onClusterCreated(Cluster cluster) {
        if(m_subscriber != null && m_subscribed.get() == false) {
            m_subscriber.subscribe(m_hzInstance,this);
            m_subscribed.set(true);
        }

        m_clusters.add(cluster);
    }

    @Override
    public void onMessage(Message<EventMessage> event) {
        for(Cluster cluster : m_clusters) {
            cluster.publish(event.getMessageObject());
        }
    }
}
