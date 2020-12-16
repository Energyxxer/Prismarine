package com.energyxxer.prismarine.typesystem.generics;

import com.energyxxer.prismarine.reporting.PrismarineException;
import com.energyxxer.prismarine.symbols.contexts.ISymbolContext;
import com.energyxxer.prismarine.typesystem.PrismarineTypeSystem;
import com.energyxxer.prismarine.typesystem.TypeConstraints;
import com.energyxxer.prismarine.typesystem.TypeHandler;
import com.energyxxer.prismarine.typesystem.functions.ActualParameterList;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;

public class GenericUtils {
    private GenericUtils() {}

    public static TypeConstraints nonGeneric(TypeConstraints constraints, Object thisObject, ActualParameterList actualParams, ISymbolContext ctx) {
        if(constraints.isGeneric()) {
            TypeHandler genericType = nonGeneric(constraints.getGenericHandler(), thisObject, actualParams, ctx);
            boolean nullable = constraints.isNullable();
            return new TypeConstraints(constraints.getTypeSystem(), genericType, nullable);
        } else if(constraints.getHandler() instanceof GenericStandInType) {
            TypeHandler genericType = nonGeneric(constraints.getHandler(), thisObject, actualParams, ctx);
            boolean nullable = constraints.isNullable();
            return new TypeConstraints(constraints.getTypeSystem(), genericType, nullable);
        } else {
            return constraints;
        }
    }

    public static TypeHandler<?> nonGeneric(TypeHandler<?> handler, Object thisObject, ActualParameterList actualParams, ISymbolContext ctx) {
        HashSet<GenericStandInType> seenStandInTypes = null;

        TypeHandler genericType = handler;
        while(genericType instanceof GenericStandInType) {
            GenericStandInType genericInfo = (GenericStandInType) genericType;
            boolean genericTypeFound = false;

            if(actualParams.isGenericSupplier()) {
                GenericSupplier genericSupplier = actualParams.getGenericSupplier();
                if(genericSupplier.hasBinding(genericInfo.getContext().binding)) {
                    genericType = genericSupplier.get(genericInfo.getContext().binding)[genericInfo.getTypeIndex()];
                    genericTypeFound = true;
                }
            }

            if(!genericTypeFound && thisObject instanceof GenericSupplierImplementer && ((GenericSupplierImplementer) thisObject).isGenericSupplier()) {
                GenericSupplier genericSupplier = ((GenericSupplierImplementer) thisObject).getGenericSupplier();
                if(genericSupplier.hasBinding(genericInfo.getContext().binding)) {
                    genericType = genericSupplier.get(genericInfo.getContext().binding)[genericInfo.getTypeIndex()];
                    genericTypeFound = true;
                }
            }

            if(!genericTypeFound) {
                throw new PrismarineException(PrismarineTypeSystem.TYPE_ERROR, "Could not resolve generic parameter '" + genericInfo.getTypeParamName() + "' (possibly a language error?)", actualParams.getPattern(), ctx);
            }

            if(genericType instanceof GenericStandInType) {
                if(seenStandInTypes == null) seenStandInTypes = new HashSet<>();
                if(!seenStandInTypes.add(genericInfo)) {
                    throw new PrismarineException(PrismarineTypeSystem.TYPE_ERROR, "Circular generic parameters (how did you even achieve that?)", actualParams.getPattern(), ctx);
                }
            }
        }
        return genericType;
    }

    public static TypeHandler<?> resolveStandIns(TypeHandler<?> handler, Object thisObject, ActualParameterList actualParams, ISymbolContext ctx) {
        if(handler instanceof GenericStandInType) {
            return nonGeneric(handler, thisObject, actualParams, ctx); //Trust that generic suppliers never return stand-ins
        } else if(handler instanceof GenericWrapperType && ((GenericWrapperType<?>) handler).isGenericSupplier()) {
            GenericSupplier genericSupplierCopy = resolveStandIns(((GenericWrapperType<?>) handler).getGenericSupplier(), thisObject, actualParams, ctx);
            if(genericSupplierCopy == ((GenericWrapperType<?>) handler).getGenericSupplier()) {
                return handler;
            } else {
                GenericWrapperType newWrapperType = new GenericWrapperType<>(((GenericWrapperType<?>) handler).getSourceType());
                newWrapperType.setGenericSupplier(genericSupplierCopy);
                return newWrapperType;
            }
        } else return handler;
    }

    public static GenericSupplier resolveStandIns(GenericSupplier genericSupplier, Object thisObject, ActualParameterList actualParams, ISymbolContext ctx) {
        IdentityHashMap<Object, TypeHandler[]> genericSupplierCopy = null;
        for(Map.Entry<Object, TypeHandler[]> entry : genericSupplier.entrySet()) {
            TypeHandler[] types = entry.getValue();
            TypeHandler[] copy = types;
            for(int i = 0; i < types.length; i++) {
                TypeHandler type = types[i];
                TypeHandler typeNonStandIn = resolveStandIns(type, thisObject, actualParams, ctx);
                if(type != typeNonStandIn) {
                    if(copy == types) { //actually make copy a copy
                        copy = new TypeHandler[types.length];
                        if(genericSupplierCopy == null) genericSupplierCopy = new IdentityHashMap<>();
                        for(int j = 0; j < i; i++) {
                            copy[j] = types[j];
                        }
                    }
                }

                if(copy != types) {
                    copy[i] = typeNonStandIn;
                }
            }
            if(genericSupplierCopy != null) {
                genericSupplierCopy.put(entry.getKey(), copy);
            }
        }

        if(genericSupplierCopy != null) {
            GenericSupplier newSupplier = new GenericSupplier();
            newSupplier.dumpFromMap(genericSupplierCopy);
            for(Map.Entry<Object, TypeHandler[]> entry : genericSupplier.entrySet()) {
                genericSupplierCopy.putIfAbsent(entry.getKey(), entry.getValue());
            }
            return newSupplier;
        } else {
            return genericSupplier;
        }
    }

    public static boolean hasStandIns(TypeHandler<?> handler) {
        if(handler instanceof GenericStandInType) {
            return true;
        } else if(handler instanceof GenericWrapperType && ((GenericWrapperType<?>) handler).isGenericSupplier()) {
            for(Map.Entry<Object, TypeHandler[]> entry : ((GenericWrapperType<?>) handler).getGenericSupplier().entrySet()) {
                TypeHandler[] types = entry.getValue();
                for (TypeHandler type : types) {
                    if (hasStandIns(type)) return true;
                }

            }
            return false;
        } else return false;
    }
}