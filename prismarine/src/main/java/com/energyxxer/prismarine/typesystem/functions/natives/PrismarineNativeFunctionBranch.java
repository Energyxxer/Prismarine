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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;

public class PrismarineNativeFunctionBranch extends PrismarineFunctionBranch {
    private final Method method;

    public PrismarineNativeFunctionBranch(PrismarineTypeSystem typeSystem, Method method) {
        super(typeSystem, createFormalParameters(typeSystem, method));
        this.method = method;

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
        Object[] methodParams = new Object[method.getParameterCount()];

        actualParams.reportInvalidNames(formalParameters, callingCtx);

        int formalIndex = 0;
        for(int methodIndex = 0; methodIndex < method.getParameterCount(); methodIndex++, formalIndex++) {
            if(method.getParameterTypes()[methodIndex] == ISymbolContext.class) {
                methodParams[methodIndex] = callingCtx;
                formalIndex--;
                continue;
            }
            if(method.getParameterTypes()[methodIndex] == TokenPattern.class) {
                methodParams[methodIndex] = actualParams.getPattern();
                formalIndex--;
                continue;
            }
            if(method.getParameters()[methodIndex].isAnnotationPresent(NativeFunctionAnnotations.ThisArg.class)) {
                methodParams[methodIndex] = thisObject;
                formalIndex--;
                continue;
            }

            methodParams[methodIndex] = TypedFunction.getActualParameterByFormalIndex(formalIndex, formalParameters, actualParams, callingCtx, thisObject)[0];
        }

        Object returnValue;
        try {
            Object invocObject = null;
            if((method.getModifiers() & Modifier.STATIC) == 0) { //not static, invocation object must not be null
                invocObject = thisObject;
            }
            returnValue = method.invoke(invocObject, methodParams);
        } catch (IllegalAccessException x) {
            throw new PrismarineException(PrismarineException.Type.IMPOSSIBLE, x.toString(), actualParams.getPattern(), callingCtx);
        } catch (InvocationTargetException x) {
            if(x.getTargetException() instanceof PrismarineException) {
                throw ((PrismarineException) x.getTargetException());
            }
            if(x.getTargetException() instanceof PrismarineException.Grouped) {
                throw ((PrismarineException.Grouped) x.getTargetException());
            }
            throw new PrismarineException(PrismarineException.Type.INTERNAL_EXCEPTION, x.getTargetException().getClass().getSimpleName() + ": " + x.getTargetException().getMessage(), actualParams.getPattern(), callingCtx);
        }

        if(returnConstraints != null) {
            if(shouldCoerceReturn) {
                returnConstraints.validate(returnValue, actualParams.getPattern(), callingCtx);
                returnValue = returnConstraints.adjustValue(returnValue, actualParams.getPattern(), callingCtx);
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
