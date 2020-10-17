package com.energyxxer.prismarine.typesystem;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.reporting.PrismarineException;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.energyxxer.prismarine.typesystem.PrismarineTypeSystem.convertToType;

public class TypeConstraints {
    public enum SpecialInferInstruction {
        NO_INSTANCE_INFER
    }

    private final PrismarineTypeSystem typeSystem;
    private TypeHandler<?> handler = null;
    private boolean nullable = true;

    @SuppressWarnings({"FieldCanBeLocal", "unused"}) //This field is extremely useful in the debugger
    private String userDefinedIdentifier = null;

    public TypeConstraints(PrismarineTypeSystem typeSystem, TypeHandler<?> handler, boolean nullable) {
        this.typeSystem = typeSystem;
        this.handler = handler;
        this.nullable = nullable;
    }

    public TypeConstraints(PrismarineTypeSystem typeSystem, @NotNull String userDefinedIdentifier, boolean nullable) {
        this.typeSystem = typeSystem;
        this.nullable = nullable;
        this.userDefinedIdentifier = userDefinedIdentifier;

        typeSystem.registerUserDefinedTypeListener(userDefinedIdentifier, cls -> handler = cls);
    }

    public Object adjustValue(Object value, TokenPattern<?> pattern, ISymbolContext ctx) {
        if(handler != null) value = convertToType(value, pattern, ctx, nullable, handler);
        return value;
    }

    public void validate(Object value, TokenPattern<?> pattern, ISymbolContext ctx) {
        if(value == null && !nullable) {
            throw new PrismarineException(PrismarineTypeSystem.TYPE_ERROR, "Expected a non-null value, Found null", pattern, ctx);
        }
        if(value != null && handler != null && !handler.isInstance(value) && !typeSystem.getHandlerForObject(value).canCoerce(value, handler, ctx)) {
            throw new PrismarineException(PrismarineTypeSystem.TYPE_ERROR, "Incompatible types. Expected '" + handler.getTypeIdentifier() + "', Found '" + typeSystem.getTypeIdentifierForObject(value) + "'", pattern, ctx);
        }
    }

    public void validateExact(Object value, TokenPattern<?> pattern, ISymbolContext ctx) {
        if(value == null && !nullable) {
            throw new PrismarineException(PrismarineTypeSystem.TYPE_ERROR, "Expected a non-null value, Found null", pattern, ctx);
        }
        if(value != null && handler != null && !handler.isInstance(value)) {
            throw new PrismarineException(PrismarineTypeSystem.TYPE_ERROR, "Incompatible types. Expected '" + handler.getTypeIdentifier() + "', Found '" + typeSystem.getTypeIdentifierForObject(value) + "'", pattern, ctx);
        }
    }

    public boolean verify(Object value, ISymbolContext ctx) {
        return (value != null || nullable) && (value == null || handler == null || handler.isInstance(value) || ctx.getTypeSystem().getHandlerForObject(value).canCoerce(value, handler, ctx));
    }

    public TypeHandler<?> getHandler() {
        return handler;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isNullConstraint() {
        return handler == null && nullable;
    }

    @Override
    public String toString() {
        return (handler != null ? typeSystem.getTypeIdentifierForType(handler) : "*") + (nullable ? "?" : "");
    }

    //6: exact match
    //5: subtype match
    //4: any type match
    //3: coercion match
    //2: null match
    //1: overfill match
    //0: no match
    public int rateMatch(Object value, ISymbolContext ctx) {
        if(value == null && nullable) return 2;
        if(handler == null && value != null) return 4;
        if(!verify(value, ctx)) return 0;
        TypeHandler objectTypeHandler = typeSystem.getHandlerForObject(value);
        if(objectTypeHandler == handler) return 6;
        if(handler.isInstance(value)) return 5;
        if(objectTypeHandler.canCoerce(value, handler, ctx)) return 3;
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeConstraints that = (TypeConstraints) o;
        return nullable == that.nullable &&
                Objects.equals(handler, that.handler);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handler, nullable);
    }

    public static boolean constraintsEqual(TypeConstraints a, TypeConstraints b) {
        if(a == b) return true;
        if(
                (a == null && b.isNullConstraint()) ||
                        (b == null && a.isNullConstraint())
        ) {
            return true;
        }
        return a != null && a.equals(b);
    }

    public static boolean constraintAContainsB(TypeConstraints a, TypeConstraints b) {
        if(constraintsEqual(a, b)) return true;

        if(a == null || a.isNullConstraint() || a == b) return true;
        if(b == null || b.isNullConstraint()) return false;

        TypeHandler<?> handlerA = a.getHandler();
        TypeHandler<?> handlerB = b.getHandler();

        return typeAContainsB(handlerA, handlerB) && (a.isNullable() || !b.isNullable());
    }

    private static boolean typeAContainsB(TypeHandler<?> a, TypeHandler<?> b) {
        if(a == null || a == b) return true;
        if(b == null) return false;

        return a.isSubType(b);
    }
}
