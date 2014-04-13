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
 *
 */
package org.axonframework.ext.hazelcast.samples.model;

import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class DataItem extends AbstractAnnotatedAggregateRoot implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataItem.class);

    @AggregateIdentifier
    private String m_id;
    private String m_text;

    public DataItem() {
        m_id   = null;
        m_text = null;
    }

    @CommandHandler
    public DataItem(DataItemCmd.Create command) {
        m_id   = null;
        m_text = null;

        apply(new DataItemEvt.Create(command.getId(),command.getText()));
    }

    // *************************************************************************
    //
    // *************************************************************************

    @CommandHandler
    public void handleDataItemUpdateCommand(DataItemCmd.Update command) {
        LOGGER.debug("handleDataItemUpdateCommand {}",command);
        apply(new DataItemEvt.Update(command.getId(), command.getText()));
    }

    // *************************************************************************
    //
    // *************************************************************************

    @EventHandler
    protected void handleDataItemCreatedEvent(DataItemEvt.Create event) {
        LOGGER.debug("handleDataItemCreatedEvent {}",event);
        m_id   = event.getId();
        m_text = event.getText();
    }

    @EventHandler
    protected void handleDataItemUpdateEvent(DataItemEvt.Update event) {
        LOGGER.debug("handleDataItemUpdateEvent {}",event);
        m_text = event.getText();
    }
}
