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
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import org.axonframework.domain.EventMessage;
import org.axonframework.eventhandling.Cluster;
import org.axonframework.eventhandling.EventBusTerminal;
import org.axonframework.hazelcast.IHazelcastInstanceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
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
    private final Set<String> m_topicsOfInterest;
    private final Set<Cluster> m_clusters;

    /**
     * c-tor
     *
     * @param proxy the hazelcast proxy
     */
    public HazelcastEventBusTerminal(IHazelcastInstanceProxy proxy) {
        m_proxy = proxy;
        m_topicsOfInterest = Sets.newHashSet();
        m_clusters = Sets.newHashSet();
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     * @param topicsOfInterest the topics of interest
     */
    public void setTopicOfInterest(List<String> topicsOfInterest) {
        for(String topicName : m_topicsOfInterest) {
            unsubscribe(topicName, this);
        }

        m_topicsOfInterest.clear();
        m_topicsOfInterest.addAll(topicsOfInterest);

        for(String topicName : m_topicsOfInterest) {
            LOGGER.debug("Subscribe to <{}>",topicName);
            subscribe(topicName, this);
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void publish(EventMessage... events) {
        for(EventMessage event : events) {
            publish(event);
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

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     * @param event the event to publish
     */
    private void publish(EventMessage event) {
        LOGGER.debug("Publish <{}>",event.getPayloadType().getName());
        getTopic(event.getPayloadType().getName()).publish(event);
    }

    /**
     *
     * @param topicName the topic name
     * @param listener the listener
     */
    private void subscribe(String topicName,MessageListener<EventMessage> listener) {
        getTopic(topicName).addMessageListener(listener);
    }

    /**
     *
     * @param topicName the topic name
     * @param listener the listener
     */
    private void unsubscribe(String topicName,MessageListener<EventMessage> listener) {
        getTopic(topicName).removeMessageListener(listener);
    }

    /**
     *
     * @param topicName the topic name
     * @return the topic
     */
    private ITopic<EventMessage> getTopic(String topicName) {
        return m_proxy.getTopic(topicName);
    }
}
