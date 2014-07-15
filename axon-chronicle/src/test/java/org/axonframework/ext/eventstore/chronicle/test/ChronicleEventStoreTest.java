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
package org.axonframework.ext.eventstore.chronicle.test;


import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.DomainEventStream;
import org.axonframework.domain.SimpleDomainEventStream;
import org.axonframework.ext.eventstore.CloseableEventStore;
import org.axonframework.ext.chronicle.store.ChronicleEventStore;
import org.axonframework.ext.eventstore.chronicle.test.model.ChronicleAxonEvent;
import org.axonframework.ext.eventstore.chronicle.test.model.ChronicleAxonEventMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class ChronicleEventStoreTest {
    public static final Logger LOGGER   = LoggerFactory.getLogger(ChronicleEventStoreTest.class);
    public static final File   DATA_DIR = new File(FileUtils.getTempDirectoryPath(),"axon-chronicle");

    // *************************************************************************
    //
    // *************************************************************************

    @Before
    public void setUp() throws Exception {
        LOGGER.debug("setUp::deleteQuietly : {} => {}", DATA_DIR, FileUtils.deleteQuietly(DATA_DIR));
    }

    @After
    public void tearDown() throws Exception {
        LOGGER.debug("tearDown::deleteQuietly : {} => {}", DATA_DIR, FileUtils.deleteQuietly(DATA_DIR));
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testSaveStreamAndReadBack() {
        final String type  = "axon-chronicle-test";
        final String aid   = UUID.randomUUID().toString();
        final int    evts  = 10;

        try(CloseableEventStore store = new ChronicleEventStore(DATA_DIR.getAbsolutePath())) {
            List<DomainEventMessage<?>> demw = Lists.newArrayListWithCapacity(evts);
            for(int i=0;i<evts;i++) {
                demw.add(new ChronicleAxonEventMessage(aid,i,"evt-" + i));
            }

            store.appendEvents(type,new SimpleDomainEventStream(demw));

            List<DomainEventMessage<?>> demr = Lists.newArrayListWithCapacity(evts);
            DomainEventStream des = store.readEvents(type,aid);
            while (des.hasNext()) {
                demr.add(des.next());
            }

            assertEquals(demw.size(),demr.size());

            for(int i=0;i<evts;i++) {
                assertEquals(demw.get(i).getIdentifier(),demr.get(i).getIdentifier());
                assertEquals(demw.get(i).getPayloadType(),demr.get(i).getPayloadType());
                assertEquals(demw.get(i).getPayloadType(),ChronicleAxonEvent.class);
                assertEquals(demw.get(i).getPayload(),demr.get(i).getPayload());
            }
        } catch(IOException e) {
            LOGGER.warn("IOException",e);
        }
    }
}
