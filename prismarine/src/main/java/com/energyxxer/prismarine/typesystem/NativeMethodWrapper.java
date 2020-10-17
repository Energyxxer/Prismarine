package com.energyxxer.prismarine.typesystem;

import com.energyxxer.prismarine.typesystem.functions.PrimitivePrismarineFunction;
import com.energyxxer.prismarine.typesystem.functions.PrismarineFunction;

public class NativeMethodWrapper<T> implements MemberWrapper<T> {
    private final PrimitivePrismarineFunction function;

    public NativeMethodWrapper(PrimitivePrismarineFunction function) {
        this.function = function;
    }

    @Override
    public Object unwrap(T instance) {
        return new PrismarineFunction.FixedThisFunction(function, instance);
    }
}
