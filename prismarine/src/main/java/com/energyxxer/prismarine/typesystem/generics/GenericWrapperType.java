package com.energyxxer.prismarine.typesystem.generics;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;
import com.energyxxer.prismarine.typesystem.PrismarineTypeSystem;
import com.energyxxer.prismarine.typesystem.TypeHandler;
import com.energyxxer.prismarine.typesystem.functions.PrimitivePrismarineFunction;

import java.util.Iterator;

public class GenericWrapperType<T> implements TypeHandler<T>, GenericSupplierImplementer {
    private final TypeHandler<T> sourceType;
    private GenericSupplier genericSupplier;

    public GenericWrapperType(TypeHandler<T> sourceType) {
        this.sourceType = sourceType;
    }

    @Override
    public PrismarineTypeSystem getTypeSystem() {
        return sourceType.getTypeSystem();
    }

    @Override
    public Object getMember(T object, String member, TokenPattern<?> pattern, ISymbolContext ctx, boolean keepSymbol) {
        return sourceType.getMember(object, member, pattern, ctx, keepSymbol);
    }

    @Override
    public Object getIndexer(T object, Object index, TokenPattern<?> pattern, ISymbolContext ctx, boolean keepSymbol) {
        return sourceType.getIndexer(object, index, pattern, ctx, keepSymbol);
    }

    @Override
    public Object cast(T object, TypeHandler targetType, TokenPattern<?> pattern, ISymbolContext ctx) {
        return sourceType.cast(object, targetType, pattern, ctx);
    }

    @Override
    public Object coerce(T object, TypeHandler targetType, TokenPattern<?> pattern, ISymbolContext ctx) {
        return sourceType.coerce(object, targetType, pattern, ctx);
    }

    @Override
    public boolean canCoerce(Object object, TypeHandler into, ISymbolContext ctx) {
        return sourceType.canCoerce(object, into, ctx);
    }

    @Override
    public Class<T> getHandledClass() {
        return sourceType.getHandledClass();
    }

    @Override
    public String getTypeIdentifier() {
        return sourceType.getTypeIdentifier();
    }

    @Override
    public Iterator<?> getIterator(T object, TokenPattern<?> pattern, ISymbolContext ctx) {
        return sourceType.getIterator(object, pattern, ctx);
    }

    @Override
    public boolean isPrimitive() {
        return sourceType.isPrimitive();
    }

    @Override
    public boolean isSelfHandler() {
        return sourceType.isSelfHandler();
    }

    @Override
    public boolean isInstance(Object obj) {
        return sourceType.isInstance(obj) &&
                obj instanceof GenericSupplierImplementer &&
                ((GenericSupplierImplementer) obj).isGenericSupplier() &&
                this.getGenericSupplier().supplierContains(((GenericSupplierImplementer) obj).getGenericSupplier());
    }

    @Override
    public TypeHandler<?> getSuperType() {
        return sourceType.getSuperType();
    }

    @Override
    public PrimitivePrismarineFunction getConstructor(TokenPattern<?> pattern, ISymbolContext ctx, GenericSupplier genericSupplier) {
        return sourceType.getConstructor(pattern, ctx, this.genericSupplier);
    }

    @Override
    public boolean isStaticHandler() {
        return sourceType.isStaticHandler();
    }

    @Override
    public TypeHandler getStaticHandler() {
        return sourceType.getStaticHandler();
    }

    @Override
    public boolean isSubType(TypeHandler<?> other) {
        return sourceType.isSubType(other);
    }

    @Override
    public void staticTypeSetup(PrismarineTypeSystem typeSystem, ISymbolContext globalCtx) {
        //ehhh... this should never run since this isn't a pre-registered type handler
    }

    public TypeHandler<T> getSourceType() {
        return sourceType;
    }

    @Override
    public GenericSupplier getGenericSupplier() {
        return genericSupplier;
    }

    public void setGenericSupplier(GenericSupplier genericSupplier) {
        this.genericSupplier = genericSupplier;
    }

    public void putGenericInfo(Object binding, TypeHandler[] types) {
        if(genericSupplier == null) genericSupplier = new GenericSupplier();
        genericSupplier.put(binding, types);
    }
}
