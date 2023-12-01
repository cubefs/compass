package com.oppo.cloud.gpt.cache.linkedlist;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class DoubleLinkedListTest {

    @Test
    public void testAdd() {
        DoubleLinkedList<Integer> doubleLinkedList = new DoubleLinkedList<>();
        doubleLinkedList.add(0);
        doubleLinkedList.add(1);
        doubleLinkedList.add(2);
        doubleLinkedList.add(3);
        doubleLinkedList.add(4);
        doubleLinkedList.add(5);

        List<Integer> items = doubleLinkedList.items();
        for (int i = 0; i <= 5; i++) {
            Assertions.assertEquals(items.get(i), 5 - i);
        }
    }
}
