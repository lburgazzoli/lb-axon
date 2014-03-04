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
package org.axonframework.ext;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.*;

/**
 * @author lburgazzoli
 */
public class AxonServiceLoader {

    private static final Map<Class<?>,Set<Object>> SERVICES = Maps.newHashMap();

    /**
     *
     * @param type
     * @param <T>
     * @return
     */
    public static <T> Iterator<T> iterator(final Class<T> type) {
        final Iterator<Object> localServiceIt = SERVICES.containsKey(type) ? SERVICES.get(type).iterator() : null;
        final Iterator<T> serviceIt = ServiceLoader.load(type).iterator();

        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                if(localServiceIt != null && localServiceIt.hasNext()) {
                    return true;
                }

                return serviceIt.hasNext();
            }

            @SuppressWarnings("unchecked")
            @Override
            public T next() {
                if(localServiceIt != null && localServiceIt.hasNext()) {
                    return (T)localServiceIt.next();
                }

                return serviceIt.next();
            }

            @Override
            public void remove() {
            }
        };
    }

    /**
     *
     * @param type
     * @param object
     * @param <T>
     */
    public static <T> void add(Class<T> type,T object) {
        Set<Object> services = SERVICES.get(type);
        if(services == null) {
            services = Sets.newHashSet();
            SERVICES.put(type,services);
        }

        services.add(object);
    }
}
