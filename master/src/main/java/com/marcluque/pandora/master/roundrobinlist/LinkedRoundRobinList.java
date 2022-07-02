package com.marcluque.pandora.master.roundrobinlist;

/**
 * Created by marcluque on 03.01.2017.
 */
public class LinkedRoundRobinList<T> implements RoundRobinList<T> {

    private final Element<T> start;

    private int size, index, robinIndex;

    public LinkedRoundRobinList() {
        start = new Element<>(null);
        size = 0;
        robinIndex = 0;
        index = 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public T get() {
        Element<T> e = start;
        while (e.next != null) {
            if (e.next.index == robinIndex) {
                robinIndex = (robinIndex + 1) % size;
                return e.next.node;
            }

            e = e.next;
        }

        return null;
    }

    @Override
    public void add(T o) {
        if (o != null) {
            Element<T> e = new Element<>(o);
            Element<T> pointer = getLastElement();
            pointer.next = e;
            e.prev = pointer;
            e.index = index;
            index++;
            size++;
        }
    }

    @Override
    public void remove(T o) {
        Element<T> e = findObject(o);
        if (e != null) {
            if (e.next != null) {
                reduceIndex(e);
                e.prev.next = e.next;
                e.next.prev = e.prev;
            } else {
                e.prev.next = null;
            }

            size--;
            index--;
            robinIndex = (size != 0) ? (robinIndex + 1) % size : 0;
        }
    }

    private Element<T> getLastElement() {
        Element<T> e = start;
        while (e.next != null) {
            e = e.next;
        }

        return e;
    }

    private void reduceIndex(Element<T> e) {
        while (e.next != null) {
            e.next.index--;
            e = e.next;
        }
    }

    private Element<T> findObject(T node) {
        Element<T> e = start;
        while (e.next != null) {
            if (e.next.node.equals(node)) {
                return e.next;
            }

            e = e.next;
        }

        return null;
    }
}
