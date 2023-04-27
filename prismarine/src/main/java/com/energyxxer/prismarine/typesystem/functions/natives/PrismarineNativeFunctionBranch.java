package com.energyxxer.prismarine.typesystem.functions.natives;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.reporting.PrismarineException;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;
import com.energyxxer.prismarine.typesystem.PrismarineTypeSystem;
import com.energyxxer.prismarine.typesystem.TypeConstraints;
import com.energyxxer.prismarine.typesystem.TypeHandler;
import com.energyxxer.prismarine.typesystem.functions.ActualParameterList;
import com.energyxxer.prismarine.typesystem.functions.FormalParameter;
import com.energyxxer.prismarine.typesystem.functions.PrismarineFunction;
import com.energyxxer.prismarine.typesystem.functions.PrismarineFunctionBranch;
import com.energyxxer.prismarine.typesystem.functions.typed.TypedFunction;
import com.energyxxer.util.logger.Debug;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;

public class PrismarineNativeFunctionBranch extends PrismarineFunctionBranch {
    private static MethodHandles.Lookup lookup;
    private final MethodHandle handle;
    private int modifiers;
    private Class<?>[] paramTypes;
    private int thisArgIndex = -1;
    private int leadingArgCount;
    private Object[] invocationParams;

    public PrismarineNativeFunctionBranch(PrismarineTypeSystem typeSystem, Method method) {
        super(typeSystem, createFormalParameters(typeSystem, method));
        if(lookup == null) lookup = MethodHandles.publicLookup();
        this.modifiers = method.getModifiers();
        this.paramTypes = method.getParameterTypes();
        this.leadingArgCount = (modifiers & Modifier.STATIC) != 0 ? 0 : 1;
        try {
            int objectArgCount = paramTypes.length + leadingArgCount;
            this.handle = lookup
                    .unreflect(method)
                    .asType(MethodType.genericMethodType(objectArgCount))
                    .asSpreader(Object[].class, objectArgCount)
            ;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        invocationParams = new Object[paramTypes.length+leadingArgCount];

        Class<?> returnType = PrismarineTypeSystem.sanitizeClass(method.getReturnType());
        TypeHandler correspondingHandler = typeSystem.getHandlerForHandledClass(returnType);
        NativeFunctionAnnotations.UserDefinedTypeObjectArgument userDefinedConstraintAnnot = method.getAnnotation(NativeFunctionAnnotations.UserDefinedTypeObjectArgument.class);

        if(correspondingHandler == null && returnType != Object.class && returnType != Void.TYPE && userDefinedConstraintAnnot == null) {
            Debug.log("Could not create return constraints for method '" + method + "': Did not find appropriate TypeHandler instance for class: " + returnType);
        }
        boolean nullable = true;

        if(method.isAnnotationPresent(NativeFunctionAnnotations.NotNullReturn.class) || (method.getReturnType().isPrimitive() && method.getReturnType() != Void.TYPE)) {
            nullable = false;
        }

        Parameter[] javaParams = method.getParameters();
        for(int i = 0; i < javaParams.length; i++) {
            if(javaParams[i].isAnnotationPresent(NativeFunctionAnnotations.ThisArg.class)) {
                thisArgIndex = i;
                break;
            }
        }

        String userDefinedIdentifier = null;
        if(userDefinedConstraintAnnot != null) {
            userDefinedIdentifier = userDefinedConstraintAnnot.typeIdentifier();
        }

        if(userDefinedIdentifier == null) {
            this.returnConstraints = new TypeConstraints(typeSystem, correspondingHandler, nullable);
        } else {
            this.returnConstraints = new TypeConstraints(typeSystem, userDefinedIdentifier, nullable);
        }

    }

    private static Collection<FormalParameter> createFormalParameters(PrismarineTypeSystem typeSystem, Method method) {
        ArrayList<FormalParameter> params = new ArrayList<>();

        Parameter[] parameterJavaTypes = method.getParameters();
        for(Parameter param : parameterJavaTypes) {
            Class<?> paramType = param.getType();
            paramType = PrismarineTypeSystem.sanitizeClass(paramType);
            if(paramType == TokenPattern.class || paramType == ISymbolContext.class || param.isAnnotationPresent(NativeFunctionAnnotations.ThisArg.class)) {
                //Reserved for calling pattern, context and this
                continue;
            }
            TypeHandler correspondingHandler = typeSystem.getHandlerForHandledClass(paramType);
            NativeFunctionAnnotations.UserDefinedTypeObjectArgument userDefinedConstraintAnnot = param.getAnnotation(NativeFunctionAnnotations.UserDefinedTypeObjectArgument.class);

            if(correspondingHandler == null && paramType != Object.class && userDefinedConstraintAnnot == null) {
                throw new IllegalArgumentException("Could not create formal parameter for type '" + paramType.getName() + "'; Did not find appropriate TypeHandler instance. Method: " + method);
            }

            boolean nullable = correspondingHandler == null;

            String userDefinedIdentifier = null;
            if(userDefinedConstraintAnnot != null) {
                userDefinedIdentifier = userDefinedConstraintAnnot.typeIdentifier();
                nullable = false;
            }

            if(!nullable) {
                nullable = param.getAnnotation(NativeFunctionAnnotations.NullableArg.class) != null;
            }

            if(userDefinedIdentifier == null) {
                params.add(new FormalParameter(param.getName(), new TypeConstraints(typeSystem, correspondingHandler, nullable)));
            } else {
                params.add(new FormalParameter(param.getName(), new TypeConstraints(typeSystem, userDefinedIdentifier, nullable)));
            }
        }
        return params;
    }

    @Override
    public TokenPattern<?> getFunctionPattern() {
        return null;
    }

    @Override
    public Object call(ActualParameterList actualParams, ISymbolContext declaringCtx, ISymbolContext callingCtx, Object thisObject) {
        actualParams.reportInvalidNames(formalParameters, callingCtx);

        int formalIndex = 0;
        for(int methodIndex = 0; methodIndex < paramTypes.length; methodIndex++, formalIndex++) {
            if(paramTypes[methodIndex] == ISymbolContext.class) {
                invocationParams[methodIndex+leadingArgCount] = callingCtx;
                formalIndex--;
                continue;
            }
            if(paramTypes[methodIndex] == TokenPattern.class) {
                invocationParams[methodIndex+leadingArgCount] = actualParams.getPattern();
                formalIndex--;
                continue;
            }
            if(methodIndex == thisArgIndex) {
                invocationParams[methodIndex+leadingArgCount] = thisObject;
                formalIndex--;
                continue;
            }

            invocationParams[methodIndex+leadingArgCount] = TypedFunction.getActualParameterByFormalIndex(formalIndex, formalParameters, actualParams, callingCtx, thisObject).value;
        }

        Object returnValue;
        try {
            if(leadingArgCount > 0) {
                invocationParams[0] = thisObject; //not static, invocation object must not be null
            }
            returnValue = handle.invokeExact((Object[]) invocationParams);
        } catch (IllegalAccessException x) {
            throw new PrismarineException(PrismarineException.Type.IMPOSSIBLE, x.toString(), actualParams.getPattern(), callingCtx);
        } catch (Throwable x) {
            if(x instanceof PrismarineException) {
                throw ((PrismarineException) x);
            }
            if(x instanceof PrismarineException.Grouped) {
                throw ((PrismarineException.Grouped) x);
            }
            throw new PrismarineException(PrismarineException.Type.INTERNAL_EXCEPTION, x.getClass().getSimpleName() + ": " + x.getMessage(), actualParams.getPattern(), callingCtx);
        }

        if(returnConstraints != null) {
            if(shouldCoerceReturn) {
                returnValue = returnConstraints.validateAndAdjust(returnValue, null, actualParams.getPattern(), callingCtx);
            } else {
                returnConstraints.validateExact(returnValue, actualParams.getPattern(), callingCtx);
            }
        }
        return returnValue;
    }

    public static PrismarineFunction nativeMethodsToFunction(@NotNull PrismarineTypeSystem typeSystem, ISymbolContext ctx, Method met) {
        return nativeMethodsToFunction(typeSystem, ctx, null, met);
    }

    public static PrismarineFunction nativeMethodsToFunction(@NotNull PrismarineTypeSystem typeSystem, ISymbolContext ctx, String name, Method met) {
        if(name == null) name = met.getName();
        return new PrismarineFunction(name, new PrismarineNativeFunctionBranch(typeSystem, met), ctx);
    }
}
