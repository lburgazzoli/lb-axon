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

import com.google.common.collect.Lists;
import org.axonframework.domain.EventMessage;
import org.axonframework.eventhandling.Cluster;
import org.axonframework.eventhandling.EventBusTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *
 */
public class HazelcastEventBusTerminal implements EventBusTerminal {
    private final static Logger LOGEGR = LoggerFactory.getLogger(HazelcastEventBusTerminal.class);

    private final HazelcastEventBusManager m_manager;
    private String m_topicName;
    private List<String> m_interestedTopics;

    /**
     *
     * @param manager
     */
    public HazelcastEventBusTerminal(HazelcastEventBusManager manager) {
        m_manager = manager;
        m_topicName = "default";
        m_interestedTopics = Lists.newArrayList();
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

    @Override
    public void publish(EventMessage... events) {
        for(EventMessage event : events) {
            //m_manager.publish(m_topicName,event);
            LOGEGR.debug("Publish {}/{}",m_topicName,event);
        }
    }

    @Override
    public void onClusterCreated(Cluster cluster) {
        LOGEGR.debug("Cluster Created {}",cluster);
    }
}
