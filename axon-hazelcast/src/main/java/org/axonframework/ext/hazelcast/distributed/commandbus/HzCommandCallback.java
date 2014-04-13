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
package org.axonframework.ext.hazelcast.distributed.commandbus;

import org.axonframework.commandhandling.CommandCallback;

/**
 *
 */
public class HzCommandCallback<T> implements CommandCallback<T> {
    private final boolean m_local;
    private final CommandCallback<T> m_callback;

    /**
     * c-tor
     *
     * @param callback the callback
     */
    public HzCommandCallback(CommandCallback<T> callback) {
        this(false, callback);
    }

    /**
     * c-tor
     *
     * @param local     local command
     * @param callback  the callback
     */
    public HzCommandCallback(boolean local, CommandCallback<T> callback) {
        m_local      = local;
        m_callback   = callback;
    }

    /**
     *
     * @return  true if local
     */
    public boolean isLocal() {
        return m_local;
    }

    @Override
    public void onSuccess(T result) {
        m_callback.onSuccess(result);
    }

    @Override
    public void onFailure(Throwable cause) {
        m_callback.onFailure(cause);
    }
}
