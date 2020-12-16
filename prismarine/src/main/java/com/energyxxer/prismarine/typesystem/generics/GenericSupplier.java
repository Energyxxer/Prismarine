package com.energyxxer.prismarine.typesystem.generics;

import com.energyxxer.prismarine.typesystem.TypeHandler;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class GenericSupplier {
    private final IdentityHashMap<Object, TypeHandler[]> map = new IdentityHashMap<>();

    public GenericSupplier() {
    }

    public void put(Object binding, TypeHandler[] types) {
        map.put(binding, types);
    }

    public boolean hasBinding(Object binding) {
        return map.containsKey(binding);
    }

    public TypeHandler[] get(Object binding) {
        return map.get(binding);
    }

    public boolean supplierContains(GenericSupplier other) {
        for(Map.Entry<Object, TypeHandler[]> thisEntry : this.map.entrySet()) {
            if (!other.map.containsKey(thisEntry.getKey()) || !Arrays.equals(thisEntry.getValue(), other.map.get(thisEntry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    public void dumpInto(GenericSupplier other) {
        other.map.putAll(this.map);
    }

    public void dumpFrom(GenericSupplier other) {
        this.map.putAll(other.map);
    }

    public void dumpFromMap(IdentityHashMap<Object, TypeHandler[]> otherMap) {
        this.map.putAll(otherMap);
    }

    public Set<Map.Entry<Object, TypeHandler[]>> entrySet() {
        return map.entrySet();
    }
}
