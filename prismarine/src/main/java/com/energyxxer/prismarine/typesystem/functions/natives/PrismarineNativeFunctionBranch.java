package com.energyxxer.prismarine.typesystem.functions.natives;

import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.prismarine.reporting.PrismarineException;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;
import com.energyxxer.prismarine.typesystem.PrismarineTypeSystem;
import com.energyxxer.prismarine.typesystem.TypeConstraints;
import com.energyxxer.prismarine.typesystem.TypeHandler;
import com.energyxxer.prismarine.typesystem.functions.FormalParameter;
import com.energyxxer.prismarine.typesystem.functions.PrismarineFunction;
import com.energyxxer.prismarine.typesystem.functions.PrismarineFunctionBranch;
import com.energyxxer.util.logger.Debug;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;

public class PrismarineNativeFunctionBranch extends PrismarineFunctionBranch {
    private Method method;

    public PrismarineNativeFunctionBranch(PrismarineTypeSystem typeSystem, Method method) {
        super(typeSystem, createFormalParameters(typeSystem, method));
        this.method = method;

        Class<?> returnType = PrismarineTypeSystem.sanitizeClass(method.getReturnType());
        TypeHandler correspondingHandler = typeSystem.getHandlerForHandledClass(returnType);
        if(correspondingHandler == null && returnType != Object.class && returnType != Void.TYPE) {
            Debug.log("Could not create return constraints for method '" + method + "': Did not find appropriate TypeHandler instance for class: " + returnType);
        }
        boolean nullable = true;

        if(method.isAnnotationPresent(NativeFunctionAnnotations.NotNullReturn.class) || (method.getReturnType().isPrimitive() && method.getReturnType() != Void.TYPE)) {
            nullable = false;
        }

        this.returnConstraints = new TypeConstraints(typeSystem, correspondingHandler, nullable);
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
    public Object call(Object[] params, TokenPattern<?>[] patterns, TokenPattern<?> pattern, ISymbolContext declaringCtx, ISymbolContext callingCtx, Object thisObject) {
        Object[] actualParams = new Object[method.getParameterCount()];

        int j = 0;
        for(int i = 0; i < method.getParameterCount(); i++,j++) {
            if(method.getParameterTypes()[i] == ISymbolContext.class) {
                actualParams[i] = callingCtx;
                j--;
                continue;
            }
            if(method.getParameterTypes()[i] == TokenPattern.class) {
                actualParams[i] = pattern;
                j--;
                continue;
            }
            if(method.getParameters()[i].isAnnotationPresent(NativeFunctionAnnotations.ThisArg.class)) {
                actualParams[i] = thisObject;
                j--;
                continue;
            }
            FormalParameter formalParameter = formalParameters.get(j);
            if(i < params.length) {
                actualParams[i] = params[i];
            } else {
                actualParams[i] = null;
            }
            if(formalParameter.getConstraints() != null) {
                formalParameter.getConstraints().validate(actualParams[i], i < params.length ? patterns[i] : pattern, callingCtx);
                actualParams[i] = formalParameter.getConstraints().adjustValue(actualParams[i], i < params.length ? patterns[i] : pattern, callingCtx);
            }
        }

        Object returnValue;
        try {
            Object invocObject = null;
            if((method.getModifiers() & Modifier.STATIC) == 0) { //not static, invocation object must not be null
                invocObject = thisObject;
            }
            returnValue = method.invoke(invocObject, actualParams);
        } catch (IllegalAccessException x) {
            throw new PrismarineException(PrismarineException.Type.IMPOSSIBLE, x.toString(), pattern, callingCtx);
        } catch (InvocationTargetException x) {
            throw new PrismarineException(PrismarineException.Type.INTERNAL_EXCEPTION, x.getTargetException().getMessage(), pattern, callingCtx);
        }

        if(returnConstraints != null) {
            if(shouldCoerceReturn) {
                returnConstraints.validate(returnValue, null, callingCtx);
                returnValue = returnConstraints.adjustValue(returnValue, pattern, callingCtx);
            } else {
                returnConstraints.validateExact(returnValue, null, callingCtx);
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
