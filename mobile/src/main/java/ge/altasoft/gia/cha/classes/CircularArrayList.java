package ge.altasoft.gia.cha.classes;

import java.util.ArrayList;

public class CircularArrayList<E> extends ArrayList<E> {

    private int capacity = 0;

    CircularArrayList(int capacity) {
        this.capacity = capacity;
    }

//    public int capacity() {
//        return this.capacity;
//    }

    @Override
    public boolean add(E e) {
        if (size() >= capacity)
            remove(0);

        return super.add(e);
    }
}