package com.marcluque.pandora.master.roundrobinlist;

/**
 * Created by marcluque on 03.01.2017.
 */
public interface RoundRobinList<E> {

    int size();

    E get();

    void add(E e);

    void remove(E e);
}