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
package org.axonframework.hazelcast.distributed.cmd;

import com.hazelcast.core.IQueue;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.hazelcast.distributed.cmd.msg.HzCommand;
import org.axonframework.hazelcast.distributed.cmd.msg.HzCommandReply;
import org.axonframework.hazelcast.distributed.cmd.msg.HzMessage;
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
    private final AtomicBoolean m_running;
    private final IHZCommandHandler m_handler;

    /**
     * c-tor
     *
     * @param handler
     * @param segment
     * @param queue
     */
    public HzCommandListener(IHZCommandHandler handler, CommandBus segment, IQueue<HzMessage> queue) {
        m_segment = segment;
        m_queue   = queue;
        m_running = new AtomicBoolean(true);
        m_handler = handler;
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
        while(m_running.get()) {
            try {
                HzMessage msg = m_queue.poll(1, TimeUnit.SECONDS);
                if(msg != null) {
                    LOGGER.debug("Got a message of type {}",msg.getClass().getName());
                    if(msg instanceof HzCommand) {
                        m_handler.onHzCommand((HzCommand) msg);
                    } else if(msg instanceof HzCommandReply) {
                        m_handler.onHzCommandReply((HzCommandReply) msg);
                    }
                }

            } catch (InterruptedException e) {
                LOGGER.warn("Exception",e);
            }
        }
    }
}