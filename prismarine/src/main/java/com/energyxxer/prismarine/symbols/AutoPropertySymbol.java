package com.energyxxer.prismarine.symbols;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;
import com.energyxxer.prismarine.typesystem.PrismarineTypeSystem;
import com.energyxxer.prismarine.typesystem.TypeConstraints;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AutoPropertySymbol<T> extends Symbol {

    public interface Getter<T> {
        T get();
    }
    public interface Setter<T> {
        void set(T value);
    }

    @NotNull
    private final Class<T> cls;
    private final Getter<T> getter;
    private final Setter<T> setter;

    public AutoPropertySymbol(String name, @NotNull Class<T> cls, Getter<T> getter, Setter<T> setter) {
        super(name, SymbolVisibility.GLOBAL);
        this.cls = cls;
        this.getter = getter;
        this.setter = setter;
    }

    public AutoPropertySymbol(String name, PrismarineTypeSystem typeSystem, @NotNull Class<T> cls, boolean nullable, Getter<T> getter, Setter<T> setter) {
        super(name, SymbolVisibility.GLOBAL);
        this.cls = cls;
        this.getter = getter;
        this.setter = setter;

        this.setTypeConstraints(new TypeConstraints(typeSystem, typeSystem.getHandlerForHandledClass(cls), nullable));
    }

    @Override
    public @Nullable Object getValue(TokenPattern<?> pattern, ISymbolContext ctx) {
        return getter.get();
    }

    @Override
    public void safeSetValue(Object value, TokenPattern<?> pattern, ISymbolContext ctx) {
        if(getTypeConstraints() != null) {
            getTypeConstraints().validate(value, pattern, ctx);
            value = getTypeConstraints().adjustValue(value, pattern, ctx);
        }
        setter.set(((T) value));
    }

    @Override
    public void setValue(Object value) {
        if(cls.isInstance(value)) {
            setter.set((T) value);
        }
    }
}
