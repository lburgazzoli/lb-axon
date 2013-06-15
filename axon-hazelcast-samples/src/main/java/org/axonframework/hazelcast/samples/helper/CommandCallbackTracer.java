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
package org.axonframework.hazelcast.samples.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class CommandCallbackTracer<T> extends CommandCallbackAdapter<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandCallbackTracer.class);

    @Override
    public void onSuccess(T result) {
        LOGGER.debug("onSuccess => <{}> ",result);
    }

    @Override
    public void onFailure(Throwable cause) {
        LOGGER.debug("onFailure => <{}> ",cause.getClass().getName());
        LOGGER.debug("onFailure",cause);
    }
}
