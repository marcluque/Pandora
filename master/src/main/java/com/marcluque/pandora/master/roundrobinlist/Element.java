package com.marcluque.pandora.master.roundrobinlist;

/**
 * Created by marcluque on 03.01.2017.
 */
public class Element<T> {

    public int index;

    public Element<T> next, prev;

    public T node;

    public Element(T node) {
        this.node = node;
    }

    @Override
    public String toString() {
        return node.toString();
    }
}