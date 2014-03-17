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
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.CacheConfiguration;
import javax.cache.CacheManager;
import javax.cache.CacheStatistics;
import javax.cache.Status;
import javax.cache.event.CacheEntryListener;
import javax.cache.mbeans.CacheMXBean;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * @author lburgazzoli
 *
 * TODO: add eviction policy
 * TODO: issues in OSGi
 */
public class GuavaCache<K,V> implements javax.cache.Cache<K,V>, RemovalListener<K,V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuavaCache.class);

    private final String m_cacheName;
    private final Cache<K,V> m_cache;

    /**
     * c-tor
     *
     * @param cacheName
     */
    public GuavaCache(String cacheName) {
        m_cacheName = cacheName;
        m_cache = CacheBuilder.newBuilder()
            .removalListener(this)
            .build();
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public V get(K key) {
        return m_cache.getIfPresent(key);
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        return m_cache.getAllPresent(keys);
    }

    @Override
    public boolean containsKey(K key) {
        return m_cache.asMap().containsKey(key);
    }

    @Override
    public Future<V> load(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Future<Map<K, ? extends V>> loadAll(Set<? extends K> keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CacheStatistics getStatistics() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(K key, V value) {
        m_cache.put(key,value);
    }

    @Override
    public V getAndPut(K key, V value) {
        V old = m_cache.getIfPresent(key);
        m_cache.put(key,value);

        return old;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        m_cache.putAll(map);
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        if(!m_cache.asMap().containsKey(key)) {
            m_cache.put(key, value);
            return true;
        }

        return false;
    }

    @Override
    public boolean remove(K key) {
        if(m_cache.asMap().containsKey(key)) {
            m_cache.invalidate(key);
            return true;
        }

        return false;
    }

    @Override
    public boolean remove(K key, V oldValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V getAndRemove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean replace(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V getAndReplace(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAll(Set<? extends K> keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CacheConfiguration<K, V> getConfiguration() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean registerCacheEntryListener(CacheEntryListener<? super K, ? super V> cacheEntryListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean unregisterCacheEntryListener(CacheEntryListener<?, ?> cacheEntryListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object invokeEntryProcessor(K key, EntryProcessor<K, V> entryProcessor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        return m_cacheName;
    }

    @Override
    public CacheManager getCacheManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CacheMXBean getMBean() {
        throw new UnsupportedOperationException();
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void onRemoval(RemovalNotification<K, V> notification) {
        LOGGER.debug("onRemoval: {}",notification.getCause());
    }

    @Override
    public void start() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void stop() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Status getStatus() {
        throw new UnsupportedOperationException();
    }
}
