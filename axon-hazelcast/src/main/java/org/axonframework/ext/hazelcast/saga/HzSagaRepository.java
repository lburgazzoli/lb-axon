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
package org.axonframework.ext.hazelcast.saga;

import org.axonframework.saga.AssociationValue;
import org.axonframework.saga.Saga;
import org.axonframework.saga.SagaRepository;

import java.util.Set;

/**
 *
 */
public class HzSagaRepository implements SagaRepository {
    /**
     * c-tor
     */
    public HzSagaRepository() {
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public Set<String> find(Class<? extends Saga> type, AssociationValue associationValue) {
        return null;
    }

    @Override
    public Saga load(String sagaIdentifier) {
        return null;
    }

    @Override
    public void commit(Saga saga) {
    }

    @Override
    public void add(Saga saga) {
    }
}
