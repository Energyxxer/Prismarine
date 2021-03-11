package com.energyxxer.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

public class SimpleReadArrayList<E> extends ArrayList<E> {
    public SimpleReadArrayList(int initialCapacity) {
        super(initialCapacity);
    }

    public SimpleReadArrayList() {
    }

    public SimpleReadArrayList(@NotNull Collection<? extends E> c) {
        super(c);
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<E> {
        private int i = 0;

        @Override
        public void remove() {
            if(i > 0 && i-1 < size()) {
                SimpleReadArrayList.this.remove(i);
            } else throw new IllegalStateException();
        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            while(hasNext()) {
                action.accept(next());
            }
        }

        @Override
        public boolean hasNext() {
            return i < size();
        }

        @Override
        public E next() {
            if(hasNext()) return get(i++);
            else throw new NoSuchElementException();
        }
    }
}
