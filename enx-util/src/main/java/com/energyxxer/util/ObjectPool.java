package com.energyxxer.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ObjectPool<T> {
    private final ArrayList<T> available = new ArrayList<>();
    private final ArrayList<T> claimed = new ArrayList<>();

    private final Supplier<T> factoryFunction;
    private final Consumer<T> resetFunction;

    public ObjectPool(Supplier<T> factoryFunction, Consumer<T> resetFunction) {
        this.factoryFunction = factoryFunction;
        this.resetFunction = resetFunction;
    }

    @NotNull
    public T claim() {
        if (available.isEmpty()) {
            T t = factoryFunction.get();
            claimed.add(t);
            return t;
        } else {
            T t = available.get(available.size() - 1);
            available.remove(available.size() - 1);
            claimed.add(t);
            return t;
        }
    }

    public void free(@NotNull T t) {
        if(resetFunction != null) resetFunction.accept(t);
        claimed.remove(t);
        available.add(t);
    }
}
