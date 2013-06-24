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
package org.axonframework.hazelcast.distributed;

import com.google.common.collect.Sets;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.MultiMap;
import org.apache.commons.lang3.StringUtils;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.distributed.CommandBusConnector;
import org.axonframework.hazelcast.IHazelcastInstanceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Date;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class HazelcastCommandBusConnector implements CommandBusConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastCommandBusConnector.class);

    private static final String REG_NODES             = "reg.nodes";
    private static final String REG_CMD_DESTINATIONS  = "reg.cmd.destinations";
    private static final String REG_CMD_HANDLERS      = "reg.cmd.handlers";

    private final ScheduledExecutorService m_scheduler;
    private final IHazelcastInstanceProxy m_proxy;
    private final CommandBus m_localSegment;
    private final String m_clusterName;
    private final String m_nodeName;
    private final Set<String> m_supportedCmds;
    private final IMap<String,HazelcastNode> m_registry;
    private final IMap<String,String> m_destinations;

    private IQueue<CommandMessage<?>> m_queue;
    private HazelcastCommandListener m_queueListener;

    /**
     * c-tor
     *
     * @param proxy the hazelcast proxy
     * @param localSegment CommandBus that dispatches Commands destined for the local JVM
     * @param clusterName the name of the Cluster this segment registers to
     * @param nodeName
     */
    public HazelcastCommandBusConnector(IHazelcastInstanceProxy proxy,CommandBus localSegment,String clusterName,String nodeName) {
        m_proxy         = proxy;
        m_localSegment  = localSegment;
        m_clusterName   = clusterName;
        m_nodeName      = nodeName + "@" + m_clusterName;
        m_supportedCmds = Sets.newHashSet();
        m_registry      = m_proxy.getMap(REG_NODES);
        m_destinations  = m_proxy.getMap(REG_CMD_DESTINATIONS);
        m_queueListener = null;
        m_scheduler     = Executors.newScheduledThreadPool(1);
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     */
    public void connect() {
        if(StringUtils.isNotBlank(m_nodeName)) {
            if(!m_registry.containsKey(m_nodeName)) {
                m_queue = m_proxy.getQueue(m_nodeName);
                m_registry.put(m_nodeName,new HazelcastNode(m_nodeName,m_queue.getName()));

                LOGGER.debug("{} - registered <{}>",m_nodeName,m_registry.getName());
                LOGGER.debug("{} - queue.name <{}>",m_nodeName,m_queue.getName());

                if(m_queue != null && m_queueListener == null) {
                    m_queueListener = new HazelcastCommandListener();
                    m_queueListener.run();
                }

                m_scheduler.scheduleAtFixedRate(new HazelcashNodeHeartBeat(),10,5,TimeUnit.SECONDS);

            } else {
                LOGGER.warn("Service {} already registered",m_nodeName);
            }
        } else {
            LOGGER.warn("Service does not declare an ID");
        }
    }

    /**
     *
     */
    public void disconenct() {
        if(m_proxy != null) {
            m_queueListener.shutdown();
            try {
                m_queueListener.join(1000 * 5);
            } catch (InterruptedException e) {
                LOGGER.warn("Exception",e);
            }
        }

        m_scheduler.shutdown();
        m_registry.remove(m_nodeName);

        m_queueListener = null;
        m_queue = null;
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void send(String routingKey, CommandMessage<?> command) throws Exception {
        send(routingKey,command,null);
    }

    @Override
    public <R> void send(String routingKey, CommandMessage<?> command, CommandCallback<R> callback) throws Exception {
        String destination = getCommandDestination(routingKey, command);

        m_proxy.getQueue(destination).put(command);

        try {
            m_proxy.getQueue(destination).put(command);
            if(callback != null) {
                //TODO: do something
            }
        } catch(Exception e) {
            LOGGER.warn("Exception,e");
            throw e;
        }
    }

    @Override
    public <C> void subscribe(String commandName, CommandHandler<? super C> handler) {
        LOGGER.debug("subscribe: {}",commandName);
        if(m_supportedCmds.add(commandName)) {
            m_localSegment.subscribe(commandName,handler);
            LOGGER.debug("subscribed: {}",commandName);

            m_proxy.getMultiMap(REG_CMD_HANDLERS).put(commandName,m_nodeName);
        }
    }

    @Override
    public <C> boolean unsubscribe(String commandName, CommandHandler<? super C> handler) {
        LOGGER.debug("unsubscribe: {}",commandName);
        if (m_localSegment.unsubscribe(commandName, handler)) {
            LOGGER.debug("unsubscribed: {}",commandName);

            if(m_supportedCmds.remove(commandName)) {
                m_proxy.getMultiMap(REG_CMD_HANDLERS).remove(commandName,m_nodeName);
            }

            return true;
        }

        return false;
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     * @param command
     * @return
     */
    private String getHandlerForCommand(CommandMessage<?> command) {
        MultiMap<String,String> map   = m_proxy.getMultiMap("");
        String[]                items = map.get(command.getCommandName()).toArray(new String[]{});

        if(items.length == 1) {
            return items[0];
        } else if(items.length > 1) {
            return items[new Random().nextInt(items.length)];
        }

        return null;
    }

    /**
     *
     * @param routingKey
     * @param command
     * @return
     */
    public String getCommandDestination(String routingKey, CommandMessage<?> command) {
        String destination = m_destinations.get(routingKey);
        if(StringUtils.isBlank(destination)) {
            destination = getHandlerForCommand(command);
            if(m_registry.containsKey(destination)) {
                m_destinations.put(routingKey,destination);
            }
        }

        if(!m_registry.containsKey(destination)) {
            destination = null;
        }

        return destination;
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     */
    private class HazelcastNode implements Serializable {
        private String m_name;
        private String m_queueName;
        private Date m_lastHeartBeat;

        /**
         * c-tor
         *
         * @param name
         * @param queueName
         */
        public HazelcastNode(String name,String queueName) {
            m_name = name;
            m_queueName = queueName;
            m_lastHeartBeat = new Date();
        }
    }

    /**
     *
     */
    private class HazelcastCommandListener extends Thread {
        private final AtomicBoolean m_running;

        /**
         * c-tor
         */
        public HazelcastCommandListener() {
            m_running = new AtomicBoolean(true);
        }

        /**
         *
         */
        public void shutdown() {
            m_running.set(false);
        }

        @Override
        public void run() {
            while(m_running.get()) {
                try {
                    LOGGER.debug("poll...");

                    CommandMessage<?> cmd = m_queue.poll(1, TimeUnit.SECONDS);
                    if(cmd != null && m_localSegment != null) {
                        m_localSegment.dispatch(cmd);
                    }

                } catch (InterruptedException e) {
                    LOGGER.warn("Exception",e);
                }
            }
        }
    }

    /**
     *
     */
    private class HazelcashNodeHeartBeat implements Runnable {
        @Override
        public void run() {
            if(m_registry != null && m_queue != null) {
                m_registry.put(m_nodeName,new HazelcastNode(m_nodeName,m_queue.getName()));
            }
        }
    }
}
