package com.energyxxer.prismarine.typesystem;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.reporting.PrismarineException;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;
import com.energyxxer.prismarine.typesystem.functions.ActualParameterList;
import com.energyxxer.prismarine.typesystem.generics.GenericStandInType;
import com.energyxxer.prismarine.typesystem.generics.GenericUtils;
import org.jetbrains.annotations.NotNull;
import sun.plugin.dom.exception.InvalidStateException;

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

    private TypeHandler<?> genericInfo = null;
    private boolean genericSubstituted = false;

    public TypeConstraints(PrismarineTypeSystem typeSystem, TypeHandler<?> handler, boolean nullable) {
        this.typeSystem = typeSystem;
        this.nullable = nullable;

        if(GenericUtils.hasStandIns(handler)) {
            this.genericInfo = handler;
            this.handler = null;
        } else {
            this.handler = handler;
        }
    }

    public TypeConstraints(PrismarineTypeSystem typeSystem, @NotNull String userDefinedIdentifier, boolean nullable) {
        this.typeSystem = typeSystem;
        this.nullable = nullable;
        this.userDefinedIdentifier = userDefinedIdentifier;

        typeSystem.registerUserDefinedTypeListener(userDefinedIdentifier, cls -> handler = cls);
    }

    public Object adjustValue(Object value, TokenPattern<?> pattern, ISymbolContext ctx) {
        if(isGeneric() && !genericSubstituted) throw new IllegalStateException("Cannot use this generic handler without starting a generic substitution");
        if(handler != null) value = convertToType(value, pattern, ctx, nullable, handler);
        return value;
    }

    public void validate(Object value, TokenPattern<?> pattern, ISymbolContext ctx) {
        if(isGeneric() && !genericSubstituted) throw new IllegalStateException("Cannot use this generic handler without starting a generic substitution");
        if(value == null && !nullable) {
            throw new PrismarineException(PrismarineTypeSystem.TYPE_ERROR, "Expected a non-null value, Found null", pattern, ctx);
        }
        if(value != null && handler != null && !handler.isInstance(value) && !typeSystem.getHandlerForObject(value).canCoerce(value, handler, ctx)) {
            throw new PrismarineException(PrismarineTypeSystem.TYPE_ERROR, "Incompatible types. Expected '" + typeSystem.typeHandlerToString(handler) + "', Found '" + typeSystem.getTypeIdentifierForObject(value) + "'", pattern, ctx);
        }
    }

    public void validateExact(Object value, TokenPattern<?> pattern, ISymbolContext ctx) {
        if(isGeneric() && !genericSubstituted) throw new IllegalStateException("Cannot use this generic handler without starting a generic substitution");
        if(value == null && !nullable) {
            throw new PrismarineException(PrismarineTypeSystem.TYPE_ERROR, "Expected a non-null value, Found null", pattern, ctx);
        }
        if(value != null && handler != null && !handler.isInstance(value)) {
            throw new PrismarineException(PrismarineTypeSystem.TYPE_ERROR, "Incompatible types. Expected '" + typeSystem.typeHandlerToString(handler) + "', Found '" + typeSystem.getTypeIdentifierForObject(value) + "'", pattern, ctx);
        }
    }

    public boolean verify(Object value, ISymbolContext ctx) {
        if(isGeneric() && !genericSubstituted) throw new IllegalStateException("Cannot use this generic handler without starting a generic substitution");
        return (value != null || nullable) && (value == null || handler == null || handler.isInstance(value) || ctx.getTypeSystem().getHandlerForObject(value).canCoerce(value, handler, ctx));
    }

    public TypeHandler<?> getHandler() {
        if(isGeneric() && !genericSubstituted) throw new IllegalStateException("Cannot use this generic handler without starting a generic substitution");
        return handler;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isNullConstraint() {
        if(isGeneric() && !genericSubstituted) throw new IllegalStateException("Cannot use this generic handler without starting a generic substitution");
        return handler == null && nullable;
    }

    @Override
    public String toString() {
        return (genericInfo instanceof GenericStandInType ? "<" + ((GenericStandInType) genericInfo).getTypeParamName() + ">" : (handler != null ? typeSystem.typeHandlerToString(handler) : "*")) + (nullable ? "?" : "");
    }

    //6: exact match
    //5: subtype match
    //4: coercion match
    //3: any type match
    //2: null-explicit match
    //1: null-implicit match
    //0: no match
    public int rateMatch(Object value, ISymbolContext ctx) {
        if(isGeneric() && !genericSubstituted) throw new IllegalStateException("Cannot use this generic handler without starting a generic substitution"); //TODO generic parameter rating
        if(value == null && nullable) return 2;
        if(handler == null && value != null) return 3;
        if(!verify(value, ctx)) return 0;
        TypeHandler objectTypeHandler = typeSystem.getHandlerForObject(value);
        if(objectTypeHandler == handler) return 6;
        if(handler.isInstance(value)) return 5;
        if(objectTypeHandler.canCoerce(value, handler, ctx)) return 4;
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

    public PrismarineTypeSystem getTypeSystem() {
        return typeSystem;
    }

    public boolean isGeneric() {
        return genericInfo != null;
    }

    public TypeHandler<?> getGenericHandler() { //TODO better name
        return genericInfo;
    }

    public void startGenericSubstitution(TypeHandler handler) {
        if(!isGeneric()) throw new InvalidStateException("Cannot start generic substitution on a non-generic type constraint");
        if(genericSubstituted) throw new InvalidStateException("Cannot start generic substitution; one is already in progress!");
        genericSubstituted = true;
        this.handler = handler;
    }

    public void startGenericSubstitution(Object thisObject, ActualParameterList actualParams, ISymbolContext ctx) {
        if(!isGeneric()) throw new InvalidStateException("Cannot start generic substitution on a non-generic type constraint");
        if(genericSubstituted) throw new InvalidStateException("Cannot start generic substitution; one is already in progress!");
        genericSubstituted = true;
        this.handler = GenericUtils.resolveStandIns(genericInfo, thisObject, actualParams, ctx);
    }

    public void endGenericSubstitution() {
        if(!isGeneric()) throw new InvalidStateException("Cannot end generic substitution on a non-generic type constraint");
//        if(!genericSubstituted) throw new InvalidStateException("Cannot end generic substitution; none has been started!");
        genericSubstituted = false;
        this.handler = null;
    }
}
