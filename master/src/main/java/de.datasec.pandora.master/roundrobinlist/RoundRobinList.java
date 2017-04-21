package de.datasec.pandora.master.roundrobinlist;

/**
 * Created by DataSec on 03.01.2017.
 */
public interface RoundRobinList<E> {

    int size();

    E get();

    boolean add(E e);

    void remove(E e);
}
