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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 *
 */
public class HazelcastEventBusTerminal implements EventBusTerminal,MessageListener<EventMessage> {
    private final static Logger LOGEGR = LoggerFactory.getLogger(HazelcastEventBusTerminal.class);

    private final HazelcastEventBusManager m_manager;
    private final Set<String> m_topicsOfInterest;
    private final Set<Cluster> m_clusters;
    private String m_topicName;

    /**
     *
     * @param manager
     */
    public HazelcastEventBusTerminal(HazelcastEventBusManager manager) {
        m_manager = manager;
        m_topicName = "default";
        m_topicsOfInterest = Sets.newHashSet();
        m_clusters = Sets.newHashSet();
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     * @param topicName
     */
    public void setTopicName(String topicName) {
        m_topicName = topicName;
    }

    /**
     *
     * @param topicsOfInterest
     */
    public void setTopicOfInterest(List<String> topicsOfInterest) {
        for(String topicName : m_topicsOfInterest) {
            m_manager.unsubscribe(topicName,this);
        }

        m_topicsOfInterest.clear();
        m_topicsOfInterest.addAll(topicsOfInterest);

        for(String topicName : m_topicsOfInterest) {
            m_manager.subscribe(topicName,this);
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void publish(EventMessage... events) {
        for(EventMessage event : events) {
            LOGEGR.debug("Publish:"
                + "\n\ttopicGroup = {}"
                + "\n\teventId    = {}"
                + "\n\tevent      = {}",m_topicName,event.getIdentifier(),event);

            m_manager.publish(m_topicName,event);
        }
    }

    @Override
    public void onClusterCreated(Cluster cluster) {
        LOGEGR.debug("ClusterCreated: <{}>",cluster.getName());
        m_clusters.add(cluster);
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void onMessage(Message<EventMessage> event) {
        for(Cluster cluster : m_clusters) {
            cluster.publish(event.getMessageObject());
        }
    }
}
