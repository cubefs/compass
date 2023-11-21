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
