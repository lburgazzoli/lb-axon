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

import com.google.common.cache.*;
import net.sf.jsr107cache.CacheEntry;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheListener;
import net.sf.jsr107cache.CacheStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author lburgazzoli
 *
 * TODO: add eviction policy
 * TODO: switch to javax.cache.Cache<K,V>
 */
public class GuavaCache implements net.sf.jsr107cache.Cache, RemovalListener<Object,Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuavaCache.class);

    private final Cache<Object,Object> m_cache;

    /**
     * c-tor
     */
    public GuavaCache() {
        m_cache = CacheBuilder.newBuilder()
            .removalListener(this)
            .build();
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public boolean containsKey(Object key) {
        return m_cache.asMap().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return m_cache.asMap().containsValue(value);
    }

    @Override
    public Set entrySet() {
        return m_cache.asMap().entrySet();
    }

    @Override
    public boolean isEmpty() {
        return m_cache.size() == 0;
    }

    @Override
    public Set keySet() {
        return m_cache.asMap().keySet();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void putAll(Map t) {
        m_cache.putAll(t);
    }

    @Override
    public int size() {
        return (int)m_cache.size();
    }

    @Override
    public Collection values() {
        return m_cache.asMap().values();
    }

    @Override
    public Object get(Object key) {
        return m_cache.getIfPresent(key);
    }

    @Override
    public Map getAll(Collection keys) throws CacheException {
        return m_cache.getAllPresent(keys);
    }

    @Override
    public void load(Object key) throws CacheException {
    }

    @Override
    public void loadAll(Collection keys) throws CacheException {
    }

    @Override
    public Object peek(Object key) {
        return m_cache.getIfPresent(key);
    }

    @Override
    public Object put(Object key, Object value) {
        Object old = get(key);
        m_cache.put(key,value);

        return old;
    }

    @Override
    public CacheEntry getCacheEntry(Object key) {
        throw new UnsupportedOperationException("getCacheEntry");
    }

    @Override
    public CacheStatistics getCacheStatistics() {
        throw new UnsupportedOperationException("getCacheStatistics");
    }

    @Override
    public Object remove(Object key) {
        Object old = get(key);
        m_cache.invalidate(key);

        return old;
    }

    @Override
    public void clear() {
        m_cache.invalidateAll();
    }

    @Override
    public void evict() {
        throw new UnsupportedOperationException("evict");
    }

    @Override
    public void addListener(CacheListener listener) {
        throw new UnsupportedOperationException("addListener");
    }

    @Override
    public void removeListener(CacheListener listener) {
        throw new UnsupportedOperationException("removeListener");
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void onRemoval(RemovalNotification<Object, Object> notification) {
        LOGGER.debug("onRemoval: {}",notification.getCause());
    }
}
