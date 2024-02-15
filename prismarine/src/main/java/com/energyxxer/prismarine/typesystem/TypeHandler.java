package com.energyxxer.prismarine.typesystem;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;
import com.energyxxer.prismarine.typesystem.functions.PrimitivePrismarineFunction;
import com.energyxxer.prismarine.typesystem.generics.GenericSupplier;

import java.util.Iterator;

public interface TypeHandler<T> {
    PrismarineTypeSystem getTypeSystem();

    Object getMember(T object, String member, TokenPattern<?> pattern, ISymbolContext ctx, boolean keepSymbol, Object additionalContext);

    Object getIndexer(T object, Object index, TokenPattern<?> pattern, ISymbolContext ctx, boolean keepSymbol, Object additionalContext);

    Object cast(T object, TypeHandler targetType, TokenPattern<?> pattern, ISymbolContext ctx);

    default Object coerce(T object, TypeHandler targetType, TokenPattern<?> pattern, ISymbolContext ctx) {
        return null;
    }

    default boolean canCoerce(Object object, TypeHandler into, ISymbolContext ctx) {
        return false;
    }

    default Iterator<?> getIterator(T object, TokenPattern<?> pattern, ISymbolContext ctx) {
        return null;
    }

    Class<T> getHandledClass();
    default boolean isPrimitive() {
        return true;
    }
    String getTypeIdentifier();
    default boolean isSelfHandler() {
        return this.getClass() == getHandledClass();
    }

    default boolean isInstance(Object obj) {
        return getHandledClass().isInstance(obj);
    }

    default TypeHandler<?> getSuperType() {
        return null;
    }

    default PrimitivePrismarineFunction getConstructor(TokenPattern<?> pattern, ISymbolContext ctx, GenericSupplier genericSupplier) {
        return null;
    }

    default boolean isStaticHandler() {
        return getTypeSystem().isStaticPrimitiveHandler(this);
    }

    default TypeHandler getStaticHandler() {
        if(isStaticHandler()) return getTypeSystem().getMetaTypeHandler();
        return getTypeSystem().getPrimitiveHandlerForShorthand(getTypeIdentifier());
    }

    default boolean isSubType(TypeHandler<?> other) {
        if(other == null) return false;
        TypeHandler<?> superType = other;
        while(superType != null) {
            if(this == superType) {
                return true;
            }
            superType = superType.getSuperType();
        }
        return false;
    }

    default void staticTypeSetup(PrismarineTypeSystem typeSystem, ISymbolContext globalCtx) {}
}
