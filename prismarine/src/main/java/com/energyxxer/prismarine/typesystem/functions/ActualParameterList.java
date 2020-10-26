package com.energyxxer.prismarine.typesystem.functions;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.reporting.PrismarineException;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;
import com.energyxxer.prismarine.typesystem.PrismarineTypeSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class ActualParameterList {
    private @Nullable String[] names;
    private Object[] values;
    private TokenPattern<?>[] patterns;
    private @NotNull TokenPattern<?> pattern;

    public ActualParameterList(@NotNull TokenPattern<?> pattern) {
        this(new Object[0], new TokenPattern<?>[0], pattern);
    }

    public ActualParameterList(Object[] values, TokenPattern<?>[] patterns, @NotNull TokenPattern<?> pattern) {
        this(values, null, patterns, pattern);
    }

    public ActualParameterList(Object[] values, @Nullable String[] names, TokenPattern<?>[] patterns, @NotNull TokenPattern<?> pattern) {
        this.values = values;
        this.names = names;

        if(patterns == null) {
            patterns = new TokenPattern<?>[values.length];
            for(int i = 0; i < patterns.length; i++) {
                patterns[i] = pattern;
            }
        }

        this.patterns = patterns;
        this.pattern = pattern;

        if(values.length != patterns.length || (names != null && names.length != values.length)) {
            throw new IllegalArgumentException("Mismatching list lengths");
        }
    }

    @NotNull
    public Object[] getValues() {
        return values;
    }

    public Object getValue(int index) {
        return values[index];
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
        if(names == null) return null;
        return names[index];
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
}
