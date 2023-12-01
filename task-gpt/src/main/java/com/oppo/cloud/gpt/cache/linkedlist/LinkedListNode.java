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
 * Implementation of list node
 *
 * @param <V>
 */
public class LinkedListNode<V> implements ListNode<V> {

    private DoubleLinkedList<V> list;

    private V value;

    private ListNode<V> next;

    private ListNode<V> prev;

    public LinkedListNode(V value, ListNode<V> next, DoubleLinkedList<V> list) {
        this.value = value;
        this.next = next;
        this.setPrev(next.getPrev());
        this.prev.setNext(this);
        this.next.setPrev(this);
        this.list = list;
    }

    @Override
    public DoubleLinkedList<V> getList() {
        return list;
    }

    @Override
    public boolean hasNode() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public void detach() {
        this.prev.setNext(this.getNext());
        this.next.setPrev(this.getPrev());
    }

    @Override
    public ListNode<V> setPrev(ListNode<V> prev) {
        this.prev = prev;
        return this;
    }

    @Override
    public ListNode<V> setNext(ListNode<V> next) {
        this.next = next;
        return this;
    }

    @Override
    public ListNode<V> getPrev() {
        return prev;
    }

    @Override
    public ListNode<V> getNext() {
        return next;
    }

    @Override
    public ListNode<V> search(V value) {
        return this.getValue() == value ? this : this.getNext().search(value);
    }
}
