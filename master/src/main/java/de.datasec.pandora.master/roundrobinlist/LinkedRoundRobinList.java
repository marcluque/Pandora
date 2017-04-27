package de.datasec.pandora.master.roundrobinlist;

/**
 * Created by DataSec on 03.01.2017.
 */
public class LinkedRoundRobinList<E> implements RoundRobinList<E> {

    private Element start;

    private int size, index, robinIndex;

    public LinkedRoundRobinList() {
        start = new Element("head");
        size = robinIndex = index = 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public E get() {
        Element e = start;
        while (e.next != null) {
            if (e.next.index == robinIndex) {
                robinIndex = (robinIndex + 1) % size;
                return (E) e.next.node;
            }

            e = e.next;
        }

        return null;
    }

    @Override
    public boolean add(E o) {
        if (o != null) {
            Element e = new Element<>(o);
            Element pointer = getLastElement();
            pointer.next = e;
            e.prev = pointer;
            e.index = index;
            index++;
            size++;
            return true;
        }

        return false;
    }

    @Override
    public void remove(E o) {
        Element e = findObject(o);
        if (e.next != null) {
            reduceIndex(e);
            e.prev.next = e.next;
            e.next.prev = e.prev;
            size--;
            index--;
        } else {
            e.prev.next = e.next;
            size--;
            index--;
        }
    }

    private Element getLastElement() {
        Element e = start;
        while (e.next != null) {
            e = e.next;
        }

        return e;
    }

    private void reduceIndex(Element e) {
        while (e.next != null) {
            e.next.index--;
            e = e.next;
        }
    }

    private Element<E> findObject(E node) {
        Element e = start;
        while (e.next != null) {
            if (e.next.node.equals(node)) {
                return e.next;
            }

            e = e.next;
        }

        return null;
    }
}
