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

/**
 * Empty List node pretends to be a normal node.
 * @param <V>
 */
public class DummyListNode<V> implements ListNode<V> {

    private DoubleLinkedList<V> list;

    public DummyListNode(DoubleLinkedList<V> list) {
        this.list = list;
    }

    @Override
    public DoubleLinkedList<V> getList() {
        return list;
    }

    @Override
    public boolean hasNode() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public V getValue() throws NullPointerException {
        throw new NullPointerException();
    }

    @Override
    public void detach() {
        return;
    }

    @Override
    public ListNode<V> setPrev(ListNode<V> prev) {
        return prev;
    }

    @Override
    public ListNode<V> setNext(ListNode<V> next) {
        return next;
    }

    @Override
    public ListNode<V> getPrev() {
        return this;
    }

    @Override
    public ListNode<V> getNext() {
        return this;
    }

    @Override
    public ListNode<V> search(V value) {
        return this;
    }
}
