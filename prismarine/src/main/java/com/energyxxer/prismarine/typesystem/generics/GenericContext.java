package com.energyxxer.prismarine.typesystem.generics;

public class GenericContext {
    public Object binding;
    public String[] typeParameterNames;

    public GenericContext(Object binding, String[] typeParameterNames) {
        this.binding = binding;
        this.typeParameterNames = typeParameterNames;
    }
}
