package de.datasecs.pandora.master.roundrobinlist;

/**
 * Created by DataSecs on 03.01.2017.
 */
public class Element<E> {

    public int index;

    public Element next, prev;

    public E node;

    public Element(E node) {
        this.node = node;
    }

    @Override
    public String toString() {
        return node.toString();
    }
}