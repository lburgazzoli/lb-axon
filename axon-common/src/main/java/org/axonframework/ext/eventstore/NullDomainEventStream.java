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
package org.axonframework.ext.eventstore;

import org.axonframework.domain.DomainEventMessage;
import org.axonframework.domain.DomainEventStream;

/**
 *
 */
public class NullDomainEventStream implements DomainEventStream {
    public static final DomainEventStream INSTANCE = new NullDomainEventStream();

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public DomainEventMessage next() {
        return null;
    }

    @Override
    public DomainEventMessage peek() {
        return null;
    }
}
