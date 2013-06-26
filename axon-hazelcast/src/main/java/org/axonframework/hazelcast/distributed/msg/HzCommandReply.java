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
package org.axonframework.hazelcast.distributed.msg;

/**
 *
 */
public class HzCommandReply extends HzMessage {

    private String m_commandId;
    private Object m_returnValue;
    private boolean m_success;

    /**
     * c-tor
     */
    public HzCommandReply() {
        this(null,null,false);
    }

    /**
     * c-tor
     *
     * @param commandId
     * @param returnValue
     */
    public HzCommandReply(String commandId, Object returnValue) {
       this(commandId,returnValue,!(returnValue instanceof Throwable));
    }

    /**
     * c-tor
     *
     * @param commandId
     * @param returnValue
     * @param success
     */
    public HzCommandReply(String commandId, Object returnValue, boolean success) {
        m_commandId = commandId;
        m_returnValue = returnValue;
        m_success = success;
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     *
     * @return
     */
    public String getCommandId() {
        return m_commandId;
    }

    /**
     *
     * @return
     */
    public boolean isSuccess() {
        return m_success;
    }

    /**
     *
     * @return
     */
    public Object getReturnValue() {
        return m_success ? m_returnValue : null;
    }

    /**
     *
     * @return
     */
    public Throwable getError() {
        return m_success ? null : (Throwable)m_returnValue;
    }
}
