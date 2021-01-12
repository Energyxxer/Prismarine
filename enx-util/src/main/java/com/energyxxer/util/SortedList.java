package com.energyxxer.util;

import java.util.ArrayList;
import java.util.function.Function;

public class SortedList<T> extends ArrayList<T> {
    private Function<T, Integer> sortingKeyFunction;

    public SortedList(Function<T, Integer> sortingKeyFunction) {
        this.sortingKeyFunction = sortingKeyFunction;
    }

    @Override
    public T set(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(T t) {
        int key = sortingKeyFunction.apply(t);
        int index = findIndexForKey(key);
        while(index >= 0 && index < size() && sortingKeyFunction.apply(this.get(index)) <= key) {
            index++;
        }
        super.add(index, t);
        return true;
    }

    public T getByKey(int key) {
        int index = findIndexForKey(key);
        if(index < 0 || index >= size()) return null;
        T element = this.get(index);
        if(sortingKeyFunction.apply(element) != key) return null;
        return element;
    }
    public T getClosestByKey(int key) {
        int index = findIndexForKey(key);
        if(index < 0 || index >= size()) return null;
        return this.get(index);
    }


    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException();
    }

    public int findIndexForKey(int key)
    {
        if (this.isEmpty()) return 0;

        int minIndex = 0; // inclusive
        int maxIndex = this.size(); // exclusive

        if (key < sortingKeyFunction.apply(this.get(minIndex)))
        {
            return minIndex;
        }
        if (key > sortingKeyFunction.apply(this.get(maxIndex-1)))
        {
            return maxIndex;
        }

        while (minIndex < maxIndex)
        {
            int pivotIndex = (minIndex + maxIndex) / 2;

            int pivotId = sortingKeyFunction.apply(this.get(pivotIndex));
            if (pivotId == key)
            {
                return pivotIndex;
            }
            else if (key > pivotId)
            {
                minIndex = pivotIndex + 1;
            }
            else
            {
                maxIndex = pivotIndex;
            }
        }

        return minIndex;
    }
}
