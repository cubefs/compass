package com.oppo.cloud.gpt.cache.linkedlist;

public interface ListNode<V> {

    DoubleLinkedList<V> getList();

    boolean hasNode();

    boolean isEmpty();

    V getValue() throws NullPointerException;

    void detach();

    ListNode<V> setPrev(ListNode<V> prev);

    ListNode<V> setNext(ListNode<V> next);

    ListNode<V> getPrev();

    ListNode<V> getNext();

    ListNode<V> search(V value);
}

