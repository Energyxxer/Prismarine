package com.energyxxer.prismarine.typesystem.generics;

public interface GenericSupplierImplementer {
    GenericSupplier getGenericSupplier();

    default boolean isGenericSupplier() {
        return getGenericSupplier() != null;
    }

    default GenericWrapperType<?> getGenericWrapper() {
        return null;
    }
}
