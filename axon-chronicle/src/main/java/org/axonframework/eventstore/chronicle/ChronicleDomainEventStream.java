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
package org.axonframework.eventstore.chronicle;

import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.DomainEventStream;

import java.util.NoSuchElementException;

/**
 *
 */
public class ChronicleDomainEventStream implements DomainEventStream {

    public static final ChronicleDomainEventStream EMPTY = new ChronicleDomainEventStream();

    /**
     * c-tor
     */
    public ChronicleDomainEventStream() {
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public DomainEventMessage next() {
        if (!hasNext()) {
            throw new NoSuchElementException("Trying to peek beyond the limits of this stream.");
        }

        return null;
    }

    @Override
    public DomainEventMessage peek() {
        if (!hasNext()) {
            throw new NoSuchElementException("Trying to peek beyond the limits of this stream.");
        }

        return null;
    }
}