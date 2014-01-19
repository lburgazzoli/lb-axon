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
package org.axonframework.ext.hazelcast.samples.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class CommandCallbackTracer<T> extends CommandCallbackAdapter<T> {
    private final Logger m_logger;

    /**
     * c-tor
     */
    public CommandCallbackTracer() {
        this(CommandCallbackTracer.class.getName());
    }

    /**
     * c-tor
     *
     * @param type
     */
    public <C> CommandCallbackTracer(Class<C> type) {
        this(type.getName());
    }

    /**
     * c-tor
     *
     * @param loggerId
     */
    public CommandCallbackTracer(String loggerId) {
        this(LoggerFactory.getLogger(loggerId));
    }

    /**
     * c-tor
     *
     * @param logger
     */
    public CommandCallbackTracer(Logger logger) {
        m_logger = logger;
    }


    @Override
    public void onSuccess(T result) {
        m_logger.debug("onSuccess => <{}> ",result);
    }

    @Override
    public void onFailure(Throwable cause) {
        m_logger.debug("onFailure => <{}> ",cause.getClass().getName());
        m_logger.debug("onFailure",cause);
    }
}
