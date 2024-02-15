package com.energyxxer.prismarine.typesystem.generics;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;
import com.energyxxer.prismarine.typesystem.PrismarineTypeSystem;
import com.energyxxer.prismarine.typesystem.TypeHandler;

public class GenericStandInType implements TypeHandler<GenericStandInType> {
    private final PrismarineTypeSystem typeSystem;
    private final GenericContext context;
    private final int typeIndex;

    public GenericStandInType(PrismarineTypeSystem typeSystem, GenericContext context, int typeIndex) {
        this.typeSystem = typeSystem;
        this.context = context;
        this.typeIndex = typeIndex;
    }

    @Override
    public PrismarineTypeSystem getTypeSystem() {
        return typeSystem;
    }

    @Override
    public Object getMember(GenericStandInType object, String member, TokenPattern<?> pattern, ISymbolContext ctx, boolean keepSymbol, Object additionalContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getIndexer(GenericStandInType object, Object index, TokenPattern<?> pattern, ISymbolContext ctx, boolean keepSymbol, Object additionalContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object cast(GenericStandInType object, TypeHandler targetType, TokenPattern<?> pattern, ISymbolContext ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<GenericStandInType> getHandledClass() {
        return GenericStandInType.class;
    }

    @Override
    public String getTypeIdentifier() {
        return "(generic stand-in type)";
//        throw new UnsupportedOperationException();
    }

    public GenericContext getContext() {
        return context;
    }

    public int getTypeIndex() {
        return typeIndex;
    }

    public String getTypeParamName() {
        return context.typeParameterNames[typeIndex];
    }

    @Override
    public boolean isStaticHandler() {
        return true;
    }
}
