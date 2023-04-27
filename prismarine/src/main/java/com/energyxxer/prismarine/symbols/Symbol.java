package com.energyxxer.prismarine.symbols;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.reporting.PrismarineException;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;
import com.energyxxer.prismarine.typesystem.PrismarineTypeSystem;
import com.energyxxer.prismarine.typesystem.TypeConstraints;
import com.energyxxer.prismarine.typesystem.ValueConstraints;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Symbol {

    private String name;
    private final SymbolVisibility visibility;
    private Object value;
    private boolean maySet = true;
    private boolean isFinal = false;

    private TypeConstraints typeConstraints = null;
    private ValueConstraints valueConstraints = null;

    private Symbol() {
        visibility = SymbolVisibility.PUBLIC;
    }

    public Symbol(String name) {
        this(name, SymbolVisibility.PUBLIC);
    }

    public Symbol(String name, SymbolVisibility visibility) {
        this(name, visibility, null);
    }

    public Symbol(String name, SymbolVisibility visibility, Object value) {
        this.name = name;
        this.visibility = visibility;
        if(value != null) setValue(value);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SymbolVisibility getVisibility() {
        return visibility;
    }

    public TypeConstraints getTypeConstraints() {
        return typeConstraints;
    }

    public void setTypeConstraints(TypeConstraints newConstraints) {
        this.typeConstraints = newConstraints;
    }

    public ValueConstraints getValueConstraints() {
        return valueConstraints;
    }

    public void setValueConstraints(ValueConstraints valueConstraints) {
        this.valueConstraints = valueConstraints;
    }

    @Nullable
    public Object getValue(TokenPattern<?> pattern, ISymbolContext ctx) {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean aFinal) {
        isFinal = aFinal;
    }

    public boolean maySet() {
        return maySet;
    }

    public void setFinalAndLock() {
        setFinal(true);
        maySet = false;
    }

    public void safeSetValue(Object value, TokenPattern<?> pattern, ISymbolContext ctx) {
        if(maySet) {
            if(typeConstraints != null) {
                value = ctx.getTypeSystem().sanitizeObject(value);
                value = typeConstraints.validateAndAdjust(value, null, pattern, ctx);
            }
            if(valueConstraints != null) {
                valueConstraints.validate(value, name, pattern, ctx);
            }
            this.value = value;
            if(isFinal) maySet = false;
        } else {
            throw new PrismarineException(PrismarineTypeSystem.TYPE_ERROR, "Cannot assign a value to a final variable", pattern, ctx);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Symbol symbol = (Symbol) o;
        return Objects.equals(name, symbol.name) &&
                visibility == symbol.visibility &&
                Objects.equals(value, symbol.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, visibility, value);
    }

}
