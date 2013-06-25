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

import com.hazelcast.core.IQueue;
import org.axonframework.commandhandling.CommandBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class HazelcastCommandListener extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastCommandListener.class);

    private final CommandBus m_segment;
    private final IQueue<HazelcastCommand> m_queue;
    private final AtomicBoolean m_running;

    /**
     * c-tor
     *
     * @param segment
     * @param queue
     */
    public HazelcastCommandListener(CommandBus segment,IQueue<HazelcastCommand> queue) {
        m_segment = segment;
        m_queue   = queue;
        m_running = new AtomicBoolean(true);
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
                LOGGER.debug("poll...");
                HazelcastCommand cmd = m_queue.poll(1, TimeUnit.SECONDS);

                if(cmd != null && m_segment != null) {
                    LOGGER.debug(".. got {}",cmd);
                    m_segment.dispatch(cmd.getMessage());
                }

            } catch (InterruptedException e) {
                LOGGER.warn("Exception",e);
            }
        }
    }
}