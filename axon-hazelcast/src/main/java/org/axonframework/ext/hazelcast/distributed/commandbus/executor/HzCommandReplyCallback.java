/*
 * Copyright (c) 2010-2014. Axon Framework
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
package org.axonframework.ext.hazelcast.distributed.commandbus.executor;

import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.ext.hazelcast.distributed.commandbus.HzCommand;
import org.axonframework.ext.hazelcast.distributed.commandbus.HzCommandReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 *
 */
public class HzCommandReplyCallback<T> implements Future<HzCommandReply>, CommandCallback<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HzCommandReplyCallback.class);

    private T m_result;
    private Throwable m_failuureCause;

    private final CountDownLatch m_latch;
    private final HzCommand m_command;
    private final String m_nodeName;

    /**
     * c-tor
     *
     * @param command
     */
    public HzCommandReplyCallback(String nodeName,final HzCommand command) {
        m_nodeName = nodeName;
        m_command = command;
        m_result = null;
        m_failuureCause = null;

        m_latch = new CountDownLatch(1);
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void onSuccess(T result) {
        if(!isDone()) {
            m_result = result;
            m_latch.countDown();
        }
    }

    @Override
    public void onFailure(Throwable failuureCause) {
        if(!isDone()) {
            m_failuureCause = failuureCause;
            m_latch.countDown();
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        // TODO
        return false;
    }

    @Override
    public boolean isCancelled() {
        // TODO
        return false;
    }

    @Override
    public boolean isDone() {
        return m_result != null || m_failuureCause != null;
    }

    @Override
    public HzCommandReply get() throws InterruptedException, ExecutionException {
        if(!isDone()) {
            m_latch.await();
        }

        return new HzCommandReply(
            m_nodeName,
            m_command.getMessage().getIdentifier(),
            (m_failuureCause == null) ? m_result : m_failuureCause
        );
    }

    @Override
    public HzCommandReply get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if(!isDone()) {
            m_latch.await(timeout,unit);
        }

        return new HzCommandReply(
            m_nodeName,
            m_command.getMessage().getIdentifier(),
            (m_failuureCause == null) ? m_result : m_failuureCause
        );
    }
}
