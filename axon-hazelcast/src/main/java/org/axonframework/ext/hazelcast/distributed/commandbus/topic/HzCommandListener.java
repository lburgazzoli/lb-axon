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
package org.axonframework.ext.hazelcast.distributed.commandbus.topic;

import com.hazelcast.core.IQueue;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.ext.hazelcast.distributed.commandbus.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class HzCommandListener extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(HzCommandListener.class);

    private final CommandBus m_segment;
    private final IQueue<HzMessage> m_queue;
    private final ITopic<HzMessage> m_topic;
    private final AtomicBoolean m_running;
    private final IHzCommandHandler m_cmdHandler;
    private final IHzCommandReplyHandler m_replyHandler;

    /**
     * c-tor
     *
     * @param cmdHandler    the command handler
     * @param replyHandler  the reply handler
     * @param segment       the segment
     * @param queue         the queue
     * @param topic         the topic
     */
    public HzCommandListener(IHzCommandHandler cmdHandler, IHzCommandReplyHandler replyHandler, CommandBus segment, IQueue<HzMessage> queue, ITopic<HzMessage> topic) {
        m_segment      = segment;
        m_queue        = queue;
        m_topic        = topic;
        m_running      = new AtomicBoolean(true);
        m_cmdHandler   = cmdHandler;
        m_replyHandler = replyHandler;
    }

    /**
     *
     */
    public void shutdown() {
        m_running.set(false);

        try {
            this.join(1000 * 5);
        } catch (InterruptedException e) {
            LOGGER.warn("Exception",e);
        }
    }

    @Override
    public void run() {

        String id = m_topic.addMessageListener(new MessageListener<HzMessage>() {
            @Override
            public void onMessage(Message<HzMessage> msg) {
                if(msg.getMessageObject() instanceof HzCommandReply) {
                    m_replyHandler.onHzCommandReply((HzCommandReply) msg.getMessageObject());
                }
            }
        });

        while(m_running.get()) {
            try {
                HzMessage msg = m_queue.poll(1, TimeUnit.SECONDS);
                if(msg != null) {
                    LOGGER.debug("Got a message of type {}",msg.getClass().getName());
                    if(msg instanceof HzCommand) {
                        m_cmdHandler.onHzCommand((HzCommand) msg);
                    }
                }
            } catch (InterruptedException e) {
                LOGGER.warn("Exception",e);
            }
        }

        m_topic.removeMessageListener(id);
    }
}