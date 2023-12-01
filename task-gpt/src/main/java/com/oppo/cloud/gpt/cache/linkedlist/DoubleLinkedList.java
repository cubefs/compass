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

package com.oppo.cloud.gpt.cache.linkedlist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Double linked list
 *
 * @param <V>
 */
public class DoubleLinkedList<V> {

    private DummyListNode<V> dummyListNode;

    private ListNode<V> head;

    private ListNode<V> tail;

    private AtomicInteger size;

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public DoubleLinkedList() {
        this.dummyListNode = new DummyListNode<>(this);
        this.size = new AtomicInteger(0);
        clear();
    }

    private void detach(ListNode<V> node) {
        if (node != tail) {
            node.detach();
            if (node == head) {
                head = head.getNext();
            }
            size.decrementAndGet();
        } else {
            removeTail();
        }
    }

    public ListNode<V> updateAndMoveToHead(ListNode<V> node, V value) {
        try {
            this.lock.writeLock().lock();
            if (node.isEmpty() || (this != (node.getList()))) {
                return dummyListNode;
            }
            detach(node);
            add(value);
            return head;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public ListNode<V> moveToHead(ListNode<V> node) {
        return node.isEmpty() ? dummyListNode : updateAndMoveToHead(node, node.getValue());
    }

    public ListNode<V> removeTail() {
        try {
            this.lock.writeLock().lock();
            ListNode<V> tmpTail = tail;
            if (tmpTail == head) {
                tail = head = dummyListNode;
            } else {
                tail = tail.getPrev();
                tmpTail.detach();
            }
            if (!tmpTail.isEmpty()) {
                size.decrementAndGet();
            }
            return tmpTail;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public ListNode<V> remove(V value) {
        try {
            this.lock.writeLock().lock();
            ListNode<V> node = head.search(value);
            if (!node.isEmpty()) {
                if (node == tail) {
                    tail = tail.getPrev();
                }
                if (node == head) {
                    head = head.getNext();
                }
                node.detach();
                size.decrementAndGet();
            }
            return node;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public boolean add(Collection<V> values) {
        try {
            this.lock.writeLock().lock();
            for (V value : values) {
                if (add(value).isEmpty()) {
                    return false;
                }
            }
            return true;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public ListNode<V> add(V value) {
        try {
            this.lock.writeLock().lock();
            head = new LinkedListNode<>(value, head, this);
            if (tail.isEmpty()) {
                tail = head;
            }
            size.incrementAndGet();
            return head;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    // get all items
    public List<V> items() {
        List<V> res = new ArrayList<>();
        try {
            this.lock.readLock().lock();
            int size = this.size();
            ListNode<V> p = head;
            while (size > 0) {
                res.add(p.getValue());
                p = p.getNext();
                size -= 1;
            }
            return res;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public ListNode<V> search(V value) {
        try {
            this.lock.readLock().lock();
            return head.search(value);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public boolean contains(V value) {
        try {
            this.lock.readLock().lock();
            return search(value).hasNode();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public boolean isEmpty() {
        try {
            this.lock.readLock().lock();
            return head.isEmpty();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public int size() {
        try {
            this.lock.readLock().lock();
            return size.get();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public void clear() {
        try {
            this.lock.writeLock().lock();
            head = dummyListNode;
            tail = dummyListNode;
            size = new AtomicInteger(0);
        } finally {
            this.lock.writeLock().unlock();
        }
    }
}

