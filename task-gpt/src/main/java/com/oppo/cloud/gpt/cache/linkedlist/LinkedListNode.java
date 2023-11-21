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
