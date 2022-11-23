package com.energyxxer.util;

public class PrimitiveIntList {
    private int[] buffer;
    private int len;

    public PrimitiveIntList() {
        this(10);
    }
    public PrimitiveIntList(int capacity) {
        buffer = new int[capacity];
    }

    public int size() {
        return len;
    }

    public void setSize(int size) {
        if(size > len) throw new IndexOutOfBoundsException("new size > size");
        if(size < 0) throw new IndexOutOfBoundsException("new size < 0");
        len = size;
    }

    public boolean isEmpty() {
        return len == 0;
    }

    public int get(int index) {
        if(index < 0) throw new IndexOutOfBoundsException("index < 0");
        if(index > len) throw new IndexOutOfBoundsException("index >= size");
        return buffer[index];
    }

    public void set(int index, int element) {
        if(index < 0) throw new IndexOutOfBoundsException("index < 0");
        if(index > len) throw new IndexOutOfBoundsException("index >= size");
        buffer[index] = element;
    }

    public void add(int element) {
        if(len == buffer.length) {
            expandCapacity();
        }
        buffer[len] = element;
        len++;
    }

    public void add(int index, int element) {
        if(index < 0) throw new IndexOutOfBoundsException("index < 0");
        if(index > len) throw new IndexOutOfBoundsException("index >= size");
        if(len == buffer.length) {
            expandCapacity();
        }
        for(int i = len-1; i >= index; i--) {
            buffer[i+1] = buffer[i];
        }
        buffer[index] = element;
        len++;
    }

    private void expandCapacity() {
        int[] newBuffer = new int[buffer.length*2];
        System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
        this.buffer = newBuffer;
    }
}
