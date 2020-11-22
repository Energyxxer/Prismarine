package com.energyxxer.prismarine.typesystem;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.controlflow.MemberNotFoundException;
import com.energyxxer.prismarine.symbols.Symbol;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;
import com.energyxxer.prismarine.typesystem.functions.PrimitivePrismarineFunction;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.function.Function;

import static com.energyxxer.prismarine.typesystem.functions.natives.PrismarineNativeFunctionBranch.nativeMethodsToFunction;

public class TypeHandlerMemberCollection<T> {
    private final PrismarineTypeSystem typeSystem;
    private final ISymbolContext globalCtx;
    private MemberNotFoundPolicy notFoundPolicy = MemberNotFoundPolicy.RETURN_NULL;

    private final HashMap<String, MemberWrapper<T>> members = new HashMap<>();
    private PrimitivePrismarineFunction constructor;

    public TypeHandlerMemberCollection(PrismarineTypeSystem typeSystem, ISymbolContext globalCtx) {
        this.typeSystem = typeSystem;
        this.globalCtx = globalCtx;
    }

    public TypeHandlerMemberCollection<T> putMethod(Method method) {
        return putMethod(method.getName(), method);
    }

    public TypeHandlerMemberCollection<T> putMethod(String name, Method method) {
        members.put(name, new NativeMethodWrapper<>(nativeMethodsToFunction(typeSystem, globalCtx, method)));
        return this;
    }

    public TypeHandlerMemberCollection<T> putReadOnlyField(String name, Function<T, Object> picker) {
        members.put(name, picker::apply);
        return this;
    }

    public TypeHandlerMemberCollection<T> putSymbol(String name, Function<T, Symbol> picker) {
        members.put(name, picker::apply);
        return this;
    }

    public TypeHandlerMemberCollection<T> put(String name, MemberWrapper<T> picker) {
        members.put(name, picker);
        return this;
    }

    public PrimitivePrismarineFunction getConstructor() {
        return constructor;
    }

    public void setConstructor(PrimitivePrismarineFunction constructor) {
        this.constructor = constructor;
    }

    public void setConstructor(Method method) {
        this.constructor = nativeMethodsToFunction(typeSystem, globalCtx, method);
    }

    public Object getMember(T instance, String name, TokenPattern<?> pattern, ISymbolContext ctx) {
        return getMember(instance, name, pattern, ctx, false);
    }

    public Object getMember(T instance, String name, TokenPattern<?> pattern, ISymbolContext ctx, boolean keepSymbol) {
        if(members.containsKey(name)) {
            Object found = members.get(name).unwrap(instance);
            if(found instanceof Symbol && !keepSymbol) {
                found = ((Symbol) found).getValue(pattern, ctx);
            }
            return found;
        }
        if(notFoundPolicy == MemberNotFoundPolicy.THROW_EXCEPTION) {
            throw new MemberNotFoundException();
        } else {
            return null;
        }
    }

    public void setNotFoundPolicy(MemberNotFoundPolicy notFoundPolicy) {
        this.notFoundPolicy = notFoundPolicy;
    }

    public static enum MemberNotFoundPolicy {
        RETURN_NULL,
        THROW_EXCEPTION
    }
}
