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
package org.axonframework.ext.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author lburgazzoli
 *
 * TODO: add eviction policy
 * TODO: issues in OSGi
 */
public class GuavaCache implements org.axonframework.cache.Cache {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuavaCache.class);

    private final String m_cacheName;
    private final Cache<Object,Object> m_cache;

    /**
     * c-tor
     *
     * @param cacheName
     */
    public GuavaCache(String cacheName) {
        m_cacheName = cacheName;
        m_cache = CacheBuilder.newBuilder()
            //.removalListener(this)
            .build();
    }

    // *************************************************************************
    //
    // *************************************************************************

    @SuppressWarnings("unchecked")
    @Override
    public <K, V> V get(K key) {
        return (V)m_cache.getIfPresent(key);
    }

    @Override
    public <K, V> void put(K key, V value) {
        m_cache.put(key,value);
    }

    @Override
    public <K, V> boolean putIfAbsent(K key, V value) {
        Object obj = m_cache.asMap().putIfAbsent(key,value);
        return obj == null;
    }

    @Override
    public <K> boolean remove(K key) {
        if(m_cache.asMap().containsKey(key)) {
            m_cache.invalidate(key);
            return true;
        }

        return false;
    }

    @Override
    public <K> boolean containsKey(K key) {
        return m_cache.asMap().containsKey(key);
    }

    @Override
    public void registerCacheEntryListener(EntryListener cacheEntryListener) {
        //TODO: really needed
    }

    @Override
    public void unregisterCacheEntryListener(EntryListener cacheEntryListener) {
        //TODO: really needed
    }
}
