package com.energyxxer.prismarine.typesystem;

import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.enxlex.pattern_matching.structures.TokenGroup;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.enxlex.report.Notice;
import com.energyxxer.enxlex.report.NoticeType;
import com.energyxxer.prismarine.PrismarineCompiler;
import com.energyxxer.prismarine.PrismarineProductions;
import com.energyxxer.prismarine.operators.OperatorManager;
import com.energyxxer.prismarine.reporting.PrismarineException;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;
import com.energyxxer.prismarine.typesystem.generics.GenericSupplierImplementer;
import com.energyxxer.prismarine.typesystem.generics.GenericWrapperType;
import com.energyxxer.util.logger.Debug;
import org.jetbrains.annotations.Contract;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class PrismarineTypeSystem {
    public static final PrismarineException.Type TYPE_ERROR = new PrismarineException.Type("Type Error");

    protected final PrismarineCompiler compiler;
    protected final ISymbolContext globalCtx;

    private TypeHandler<?> nullHandler;
    private final ArrayList<TypeHandler<?>> primitiveHandlers = new ArrayList<>();
    private final HashMap<String, TypeHandler<?>> primitiveHandlersById = new HashMap<>();
    private final HashMap<Class, TypeHandler<?>> primitiveHandlersByClass = new HashMap<>();
    private final HashMap<String, TypeHandler<?>> userDefinedTypes = new HashMap<>();
    private final HashMap<String, ArrayList<Consumer<TypeHandler<?>>>> userDefinedTypeListeners = new HashMap<>();

    private TypeHandler<?> metaTypeHandler;

    public PrismarineTypeSystem(PrismarineCompiler compiler, ISymbolContext globalCtx) {
        this.compiler = compiler;
        this.globalCtx = globalCtx;

        registerTypes();

        for(TypeHandler<?> handler : primitiveHandlers) {
            handler.staticTypeSetup(this, globalCtx);
        }
    }

    //region Registering types

    protected abstract void registerTypes();

    protected void registerPrimitiveTypeHandler(TypeHandler<?> handler) {
        registerPrimitiveTypeHandler(handler.getTypeIdentifier(), handler);
    }

    protected void registerPrimitiveTypeHandler(String alias, TypeHandler<?> handler) {
        if(handler.isPrimitive()) {
            if(primitiveHandlersById.containsKey(alias)) { //huh?
                Debug.log("Primitive type handler overwritten: " + alias);
                TypeHandler<?> existing = primitiveHandlersById.get(alias);
                primitiveHandlers.remove(existing);
            }

            primitiveHandlers.add(handler);
            primitiveHandlersById.put(alias, handler);
            primitiveHandlersByClass.put(handler.getHandledClass(), handler);
            if("null".equals(alias)) nullHandler = handler;
        }
    }

    protected void setMetaTypeHandler(TypeHandler<?> handler) {
        registerPrimitiveTypeHandler(handler);
        metaTypeHandler = handler;
    }

    //endregion

    //region Getting handlers
    public TypeHandler<?> getPrimitiveHandlerForShorthand(String shorthand) {
        return primitiveHandlersById.get(shorthand);
    }

    public TypeHandler<?> getHandlerForObject(Object obj) {
        if(obj == null) return nullHandler;
        if(obj instanceof TypeHandler) {
            if(((TypeHandler) obj).isSelfHandler()) return (TypeHandler<?>) obj;
            return ((TypeHandler) obj).getStaticHandler();
        }
        TypeHandler superHandler = getHandlerForHandledClass(obj.getClass());
        if(superHandler != null) return superHandler;
        for(TypeHandler<?> handler : primitiveHandlers) {
            if(handler.getHandledClass() == obj.getClass()) {
                //A sure match
                return handler;
            }
            if(handler.isInstance(obj)) {
                //Found a super handler, not sure if the handler for the exact type exists though
                //This code only really works if the inheritance tree is at most 2-tall
                superHandler = handler;
            }
        }
        return superHandler;
    }

    public TypeHandler<?> getPublicFacingHandlerForObject(Object obj) {
        while(obj instanceof GenericSupplierImplementer && ((GenericSupplierImplementer) obj).isGenericSupplier()) {
            TypeHandler<?> returned = ((GenericSupplierImplementer) obj).getGenericWrapper();
            if(returned != null) {
                obj = returned;
            } else {
                return getHandlerForObject(obj);
            }
        }
        return getHandlerForObject(obj);
    }

    public <T extends TypeHandler> T getHandlerForHandlerClass(Class handlerClass) {
        if(handlerClass == null) return null;
        for(TypeHandler<?> handler : primitiveHandlers) {
            if(handler.getClass() == handlerClass) return (T) handler;
        }
        return null;
    }

    public TypeHandler<?> getHandlerForHandledClass(Class handledClass) {
        if(handledClass == null) return null;
        return primitiveHandlersByClass.get(handledClass);
    }

    public TypeHandler getStaticHandlerForObject(Object obj) {
        TypeHandler handler = getHandlerForObject(obj);
        if(handler != obj) return handler; //is self handler
        if(handler.isStaticHandler()) {
            return getMetaTypeHandler();
        }
        return handler.getStaticHandler();
    }

    public TypeHandler getHandlerForObject(Object value, TokenPattern<?> pattern, ISymbolContext ctx) {
        TypeHandler handler = getHandlerForObject(value);
        if(handler == null) {
            throw new PrismarineException(TYPE_ERROR, "Couldn't find handler for type " + value.getClass().getName(), pattern, ctx);
        }
        return handler;
    }

    public TypeHandler getMetaTypeHandler() {
        return metaTypeHandler;
    }
    //endregion

    //region Getting type identifiers
    public String getTypeIdentifierForObject(Object param) {
        if (param instanceof TypeHandler<?> && ((TypeHandler) param).isStaticHandler()) {
            return "type_definition<" + ((TypeHandler) param).getTypeIdentifier() + ">";
        } else {
            return typeHandlerToString(getPublicFacingHandlerForObject(param));
        }
    }

    public String getTypeIdentifierForType(TypeHandler<?> handler) {
        return handler.getTypeIdentifier();
    }

    public String getInternalTypeIdentifierForType(TypeHandler handler) {
        if(handler.isPrimitive()) {
            return "primitive(" + typeHandlerToString(handler) + ")";
        } else {
            return "user_defined(" + handler + ")";
        }
    }
    //endregion

    public boolean isStaticPrimitiveHandler(TypeHandler<?> handler) {
        return primitiveHandlersById.containsValue(handler);
    }

    //region Sanitization
    public Object sanitizeObject(Object obj) {
        return obj;
    }

    //Java amirite
    @Contract(pure = true)
    public static Class sanitizeClass(Class cls) {
        if(cls == double.class) return Double.class;
        if(cls == int.class) return Integer.class;
        if(cls == boolean.class) return Boolean.class;
        return cls;
    }
    //endregion

    //region Casting
    public Object cast(Object obj, TypeHandler targetType, TokenPattern<?> pattern, ISymbolContext ctx) {
        return cast(obj, targetType, pattern, ctx, true);
    }

    public Object cast(Object obj, TypeHandler targetType, TokenPattern<?> pattern, ISymbolContext ctx, boolean failureException) {
        if(obj == null) {
            if("primitive(string)".equals(getInternalTypeIdentifierForType(targetType))) {
                return castToString(null, pattern, ctx);
            }
            return null;
        }
        if(targetType.isInstance(obj)) return obj;
        TypeHandler sourceType = getHandlerForObject(obj, pattern, ctx);
        Object result = sourceType.cast(obj, targetType, pattern, ctx);
        if(result == null || result instanceof ClassCastException) {
            if("primitive(string)".equals(getInternalTypeIdentifierForType(targetType))) {
                return castToString(obj, pattern, ctx);
            }
            if(failureException) {
                if(result != null && ((ClassCastException) result).getMessage() != null) {
                    throw new PrismarineException(TYPE_ERROR, "Couldn't cast '" + obj + "' to type " + typeHandlerToString(targetType) + ": " + ((ClassCastException) result).getMessage(), pattern, ctx);
                } else {
                    throw new PrismarineException(TYPE_ERROR, "Unable to cast " + getTypeIdentifierForObject(obj) + " to type " + typeHandlerToString(targetType), pattern, ctx);
                }
            }
            return null;
        }
        return result;
    }

    @Contract("null, _, _, _ -> null")
    public Object castOrCoerce(Object obj, TypeHandler targetType, TokenPattern<?> pattern, ISymbolContext ctx) {
        return castOrCoerce(obj, targetType, pattern, ctx, true);
    }

    @Contract("null, _, _, _, _ -> null")
    public Object castOrCoerce(Object obj, TypeHandler targetType, TokenPattern<?> pattern, ISymbolContext ctx, boolean failureException) {
        if(obj == null) return null;
        Object cast = cast(obj, targetType, pattern, ctx, false);
        if(cast != null) return cast;

        TypeHandler sourceType = getHandlerForObject(obj, pattern, ctx);
        if(sourceType.canCoerce(obj, targetType, ctx)) {
            return sourceType.coerce(obj, targetType, pattern, ctx);
        }
        if(failureException) {
            throw new PrismarineException(TYPE_ERROR, "Unable to cast or coerce " + getTypeIdentifierForObject(obj) + " to type " + typeHandlerToString(targetType), pattern, ctx);
        }
        return null;
    }

    public String castToString(Object obj) {
        return String.valueOf(obj);
    }

    public String castToString(Object obj, TokenPattern<?> pattern, ISymbolContext ctx) {
        if(obj instanceof ContextualToString) {
            return ((ContextualToString) obj).contextualToString(pattern, ctx);
        } else {
            return String.valueOf(obj);
        }
    }

    public String typeHandlerToString(TypeHandler<?> handler) {
        if(handler instanceof GenericWrapperType) {
            StringBuilder sb = new StringBuilder(handler.getTypeIdentifier());
            sb.append("<");
            TypeHandler<?>[] parameterTypes = ((GenericWrapperType) handler).getGenericSupplier().get(((GenericWrapperType) handler).getSourceType());
            for(int i = 0; i < parameterTypes.length; i++) {
                sb.append(typeHandlerToString(parameterTypes[i]));
                if(i < parameterTypes.length-1) {
                    sb.append(", ");
                }
            }
            sb.append(">");
            return sb.toString();
        } else {
            return handler.getTypeIdentifier();
        }
    }
    //endregion

    //region Type Assertions
    public static <T> T assertOfClass(Object param, TokenPattern<?> pattern, ISymbolContext ctx, Class<? extends T>... expected) {
        TypeHandler[] expectedTypes = new TypeHandler[expected.length];
        for(int i = 0; i < expected.length; i++) {
            expectedTypes[i] = ctx.getTypeSystem().getHandlerForHandledClass(sanitizeClass(expected[i]));
        }
        param = convertToType(param, pattern, ctx, false, expectedTypes);

        //Ensure of the expected types
        for(Class cls : expected) {
            cls = sanitizeClass(cls);
            if(cls.isInstance(param)) return (T) param;
        }
        throw new PrismarineException(PrismarineException.Type.IMPOSSIBLE, "Expected one of the following java classes: " + Arrays.stream(expected).map(Class::getSimpleName).collect(Collectors.joining(", ")) + "; Instead got: " + param.getClass().getSimpleName(), pattern, ctx);
    }

    public static void assertOfType(Object value, TokenPattern<?> pattern, ISymbolContext ctx, boolean nullable, TypeHandler... expected) { //no coercion
        if(value == null && nullable) return;
        if(value != null) {
            for(TypeHandler type : expected) {
                if(type == null) return;
                if(type.isInstance(value)) return;
            }
        }

        if(expected.length > 1) {
            throw new PrismarineException(TYPE_ERROR, "Expected value of one of the following types: " + Arrays.stream(expected).map(h -> ctx.getTypeSystem().typeHandlerToString(h)).collect(Collectors.joining(", ")) + "; Instead got " + ctx.getTypeSystem().getTypeIdentifierForObject(value), pattern, ctx);
        } else {
            throw new PrismarineException(TYPE_ERROR, "Expected value of type " + ctx.getTypeSystem().typeHandlerToString(expected[0]) + "; Instead got " + ctx.getTypeSystem().getTypeIdentifierForObject(value), pattern, ctx);
        }
    }

    @Contract("null, _, _, true, _ -> null")
    public static Object convertToType(Object value, TokenPattern<?> pattern, ISymbolContext ctx, boolean nullable, TypeHandler... expected) { //coercion
        if(value == null && nullable) return null;
        if(value != null) {
            TypeHandler valueType = ctx.getTypeSystem().getHandlerForObject(value);

            TypeHandler couldCoerce = null;
            for(TypeHandler type : expected) {
                if(type == null) return value;
                if(type.isInstance(value)) return value;
                if(couldCoerce == null && valueType.getHandledClass().isInstance(value) && valueType.canCoerce(value, type, ctx)) couldCoerce = type;
            }

            //not instance of accepted types. Attempt to coerce into the first applicable expected value
            if(couldCoerce != null) {
                Object coerced = valueType.coerce(value, couldCoerce, pattern, ctx);
                if(coerced != null) {
                    return coerced;
                } else {
                    Debug.log("LIES");
                }
            }
        }

        if(expected.length > 1) {
            throw new PrismarineException(TYPE_ERROR, "Expected value of one of the following types: " + Arrays.stream(expected).map(TypeHandler::getTypeIdentifier).collect(Collectors.joining(", ")) + "; Instead got " + ctx.getTypeSystem().getTypeIdentifierForObject(value), pattern, ctx);
        } else {
            throw new PrismarineException(TYPE_ERROR, "Expected value of type " + expected[0].getTypeIdentifier() + "; Instead got " + ctx.getTypeSystem().getTypeIdentifierForObject(value), pattern, ctx);
        }
    }

    public static TokenPatternMatch validatorGroup(TokenPatternMatch innerMatch, boolean nullable, Class... expected) {
        return PrismarineProductions.group(innerMatch).setEvaluator((TokenPattern<?> p, ISymbolContext ctx, Object[] d) -> {
            TokenPattern<?> inner = ((TokenGroup) p).getContents()[0];
            Object result = inner.evaluate(ctx, d);
            if(result == null && nullable) return null;
            return assertOfClass(result, inner, ctx, expected);
        });
    }

    public static TokenPatternMatch validatorGroup(TokenPatternMatch innerMatch, PrismarineProductions.PostValidationPatternEvaluator<ISymbolContext> finalEvaluator, boolean nullable, Class... expected) {
        return PrismarineProductions.group(innerMatch).setEvaluator((TokenPattern<?> p, ISymbolContext ctx, Object[] d) -> {
            TokenPattern<?> inner = ((TokenGroup) p).getContents()[0];
            Object result = inner.evaluate(ctx, d);
            if(result == null && nullable) return null;
            result = assertOfClass(result, inner, ctx, expected);
            return finalEvaluator.apply(result, p, ctx, d);
        });
    }

    public static TokenPatternMatch validatorGroup(TokenPatternMatch innerMatch, Function<Object[], Object[]> dataTransformer, PrismarineProductions.PostValidationPatternEvaluator<ISymbolContext> finalEvaluator, boolean nullable, Class... expected) {
        return PrismarineProductions.group(innerMatch).setEvaluator((TokenPattern<?> p, ISymbolContext ctx, Object[] d) -> {
            TokenPattern<?> inner = ((TokenGroup) p).getContents()[0];
            Object result = inner.evaluate(ctx, dataTransformer.apply(d));
            if(result == null && nullable) return null;
            result = assertOfClass(result, inner, ctx, expected);
            return finalEvaluator.apply(result, p, ctx, d);
        });
    }
    //endregion




    public void registerUserDefinedTypeListener(String typeIdentifier, Consumer<TypeHandler<?>> listener) {
        if(userDefinedTypes.containsKey(typeIdentifier)) {
            listener.accept(userDefinedTypes.get(typeIdentifier));
            return;
        }
        userDefinedTypeListeners.computeIfAbsent(typeIdentifier, k -> new ArrayList<>())
                .add(listener);
    }

    public void registerUserDefinedType(TypeHandler<?> type) {
        userDefinedTypes.put(type.getTypeIdentifier(), type);
        List<Consumer<TypeHandler<?>>> listeners = userDefinedTypeListeners.get(type.getTypeIdentifier());
        if(listeners != null) {
            for(Consumer<TypeHandler<?>> listener : listeners) {
                listener.accept(type);
            }
            userDefinedTypeListeners.remove(type.getTypeIdentifier());
        }
    }

    public TypeHandler<?> getUserDefinedType(String typeIdentifier) {
        return userDefinedTypes.get(typeIdentifier);
    }

    public void reportUnfulfilledUserDefinedTypes() {
        for(String typeIdentifier : userDefinedTypeListeners.keySet()) {
            compiler.getReport().addNotice(new Notice(NoticeType.ERROR, "Unfulfilled user-defined type constraint: " + typeIdentifier));
        }
    }

    public abstract OperatorManager getOperatorManager();

    public ISymbolContext getGlobalContext() {
        return globalCtx;
    }
}
