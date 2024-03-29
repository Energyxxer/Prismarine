package com.energyxxer.prismarine.typesystem.functions;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.reporting.PrismarineException;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;
import com.energyxxer.prismarine.typesystem.PrismarineTypeSystem;
import com.energyxxer.prismarine.typesystem.TypeHandler;
import com.energyxxer.prismarine.typesystem.generics.GenericSupplier;
import com.energyxxer.prismarine.typesystem.generics.GenericSupplierImplementer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

public class ActualParameterList implements GenericSupplierImplementer {
    private static final Object[] NO_ARGS = new Object[0];
    private static final TokenPattern<?>[] NO_PATTERNS = new TokenPattern<?>[0];

    private @Nullable
    String[] names;
    private Object[] values;
    private TokenPattern<?>[] patterns;
    private TypeHandler<?>[] types;
    private @NotNull
    TokenPattern<?> pattern;
    private PrismarineTypeSystem typeSystem;

    private GenericSupplier genericSupplier;

    public ActualParameterList() {
    }

    public ActualParameterList(@NotNull TokenPattern<?> pattern) {
        this(NO_ARGS, NO_PATTERNS, pattern, null);
    }

    public ActualParameterList(Object[] values, TokenPattern<?>[] patterns, @NotNull TokenPattern<?> pattern, PrismarineTypeSystem typeSystem) {
        this(values, null, patterns, pattern, typeSystem);
    }

    public ActualParameterList(Object[] values, @Nullable String[] names, TokenPattern<?>[] patterns, @NotNull TokenPattern<?> pattern, PrismarineTypeSystem typeSystem) {
        reset(values, names, patterns, pattern, typeSystem);
    }

    public ActualParameterList reset(Object[] values, TokenPattern<?>[] patterns, @NotNull TokenPattern<?> pattern, PrismarineTypeSystem typeSystem) {
        return reset(values, null, patterns, pattern, typeSystem);
    }
    public ActualParameterList reset(Object[] values, @Nullable String[] names, TokenPattern<?>[] patterns, @NotNull TokenPattern<?> pattern, PrismarineTypeSystem typeSystem) {
        this.values = values;
        this.names = names;

        if(patterns == null) {
            patterns = new TokenPattern<?>[values.length];
            Arrays.fill(patterns, pattern);
        }

        this.patterns = patterns;
        this.pattern = pattern;

        this.typeSystem = typeSystem;

        if(values.length != patterns.length || (names != null && names.length != values.length)) {
            throw new IllegalArgumentException("Mismatching list lengths");
        }

        this.types = new TypeHandler<?>[values.length];

        return this;
    }

    @NotNull
    public Object[] getValues() {
        return values;
    }

    public Object getValue(int index) {
        return values[index];
    }

    public ActualParameterList setType(int index, TypeHandler<?> type) {
        types[index] = type;
        return this;
    }

    public TypeHandler<?> getType(int index) {
        if(types[index] == null) {
            return types[index] = typeSystem.getHandlerForObject(values[index]);
        } else {
            return types[index];
        }
    }

    @NotNull
    public TokenPattern<?>[] getPatterns() {
        return patterns;
    }

    public TokenPattern<?> getPattern(int index) {
        return patterns[index];
    }

    @NotNull
    public TokenPattern<?> getPattern() {
        return pattern;
    }

    public int size() {
        return values.length;
    }

    public int getIndexOfName(String nameToFind) {
        if(names != null && nameToFind != null) {
            for(int i = 0; i < names.length; i++) {
                if(nameToFind.equals(names[i])) return i;
            }
        }
        return -1;
    }

    public String getNameForIndex(int index) {
        if(names == null || index < 0 || index >= names.length) return null;
        return names[index];
    }

    public String[] getNames() {
        return names;
    }

    public boolean hasNames() {
        return names != null;
    }

    public void reportInvalidNames(ArrayList<FormalParameter> formalParameters, ISymbolContext callingCtx) {
        if(this.hasNames()) {
            for(int i = 0; i < this.size(); i++) {
                String actualName = this.getNameForIndex(i);
                if(actualName == null) continue;
                boolean nameFound = false;
                for(FormalParameter formalParam : formalParameters) {
                    if(formalParam.getName().equals(actualName)) {
                        nameFound = true;
                        break;
                    }
                }
                if(!nameFound) {
                    throw new PrismarineException(PrismarineTypeSystem.TYPE_ERROR, "No formal parameter found with the name '" + actualName + "'", this.getPattern(i), callingCtx);
                }
            }
        }
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
