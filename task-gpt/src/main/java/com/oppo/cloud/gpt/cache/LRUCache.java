/*
 * Copyright 2023 OPPO.
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

package com.oppo.cloud.gpt.cache;

import com.oppo.cloud.gpt.cache.linkedlist.DoubleLinkedList;
import com.oppo.cloud.gpt.cache.linkedlist.ListNode;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LRUCache<K, V> implements Cache<K, V> {
    /**
     * cache size
     */
    private int size;
    /**
     * hash table
     */
    private Map<K, ListNode<CacheNode<K, V>>> linkedListNodeMap;
    /**
     * linked list
     */
    private DoubleLinkedList<CacheNode<K, V>> doubleLinkedList;
    /**
     * Read & Write lock
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public LRUCache(int size) {
        this.size = size;
        this.linkedListNodeMap = new ConcurrentHashMap<>();
        this.doubleLinkedList = new DoubleLinkedList<>();
    }

    /**
     * 回收节点
     *
     * @return
     */
    public boolean evictNode() {
        try {
            this.lock.writeLock().lock();
            ListNode<CacheNode<K, V>> listNode = doubleLinkedList.removeTail();
            if (listNode.isEmpty()) {
                return false;
            }
            linkedListNodeMap.remove(listNode.getValue().getKey());
            return true;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public void clear() {
        try {
            this.lock.writeLock().lock();
            linkedListNodeMap.clear();
            doubleLinkedList.clear();
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        try {
            this.lock.readLock().lock();
            return doubleLinkedList.size();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public boolean put(K key, V value) {
        try {
            this.lock.writeLock().lock();
            CacheNode<K, V> cacheNode = new CacheNode<>(key, value);
            ListNode<CacheNode<K, V>> listNode = null;
            if (this.linkedListNodeMap.containsKey(key)) {
                ListNode<CacheNode<K, V>> node = this.linkedListNodeMap.get(key);
                listNode = doubleLinkedList.updateAndMoveToHead(node, cacheNode);
            } else {
                if (this.size() >= this.size) {
                    this.evictNode();
                }
                listNode = this.doubleLinkedList.add(cacheNode);
            }
            if (listNode.isEmpty()) {
                return false;
            }
            this.linkedListNodeMap.put(key, listNode);
            return true;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<V> get(K key) {
        try {
            this.lock.readLock().lock();
            ListNode<CacheNode<K, V>> listNode = this.linkedListNodeMap.get(key);
            if (listNode != null && !listNode.isEmpty()) {
                linkedListNodeMap.put(key, this.doubleLinkedList.moveToHead(listNode));
                return Optional.of(listNode.getValue().getValue());
            }
            return Optional.empty();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public boolean contains(K key) {
        try {
            this.lock.readLock().lock();
            return this.linkedListNodeMap.containsKey(key);
        } finally {
            this.lock.readLock().unlock();
        }
    }
}
