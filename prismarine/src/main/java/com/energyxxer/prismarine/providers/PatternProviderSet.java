package com.energyxxer.prismarine.providers;

import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.enxlex.pattern_matching.matching.lazy.TokenStructureMatch;
import com.energyxxer.prismarine.PrismarineProductions;
import com.energyxxer.util.logger.Debug;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public abstract class PatternProviderSet {
    public final String internalName;
    public String humanReadableName;

    private final ArrayList<PatternProviderUnit> units = new ArrayList<>();

    public PatternProviderSet(String internalName) {
        this(internalName, internalName);
    }

    public PatternProviderSet(String internalName, String humanReadableName) {
        this.internalName = internalName;
        this.humanReadableName = humanReadableName;
    }

    protected final void importUnits(Class... unitClasses) {
        for(Class<?> unitClass : unitClasses) {
            importUnit(unitClass);
        }
    }

    protected final void importUnit(Class unitClass) {
        try {
            if(PatternProviderUnit.class.isAssignableFrom(unitClass)) {
                importUnit((PatternProviderUnit) unitClass.getConstructor().newInstance());
            } else {
                Debug.log("Class '" + unitClass + "' does not implement 'PatternProviderUnit'!", Debug.MessageType.WARN);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            Debug.log("Could not instantiate class '" + unitClass + "': " + e.getMessage());
        }
    }

    protected final void importUnit(PatternProviderUnit unit) {
        units.add(unit);
    }

    public final void install(PrismarineProductions productions) {

        TokenStructureMatch providerStructure;
        if(internalName != null) {
            providerStructure = productions.getOrCreateStructure(internalName);
        } else {
            providerStructure = null;
        }

        installUtilityProductions(productions, providerStructure);

        for(PatternProviderUnit unit : units) {
            TokenPatternMatch createdMatch = unit.createPatternMatch(productions);
            if(createdMatch != null) {
                if(createdMatch.getEvaluator() == null && createdMatch.getSimplificationFunction() == null) {
                    createdMatch.setEvaluator(unit);
                }

                String[] targetProductionNames = unit.getTargetProductionNames();
                if(targetProductionNames != null) {
                    for(String productionName : targetProductionNames) {
                        productions.getOrCreateStructure(productionName).add(createdMatch);
                    }
                } else {
                    if(providerStructure == null) {
                        installOrphanUnit(unit, createdMatch, productions);
                    } else {
                        providerStructure.add(createdMatch);
                    }
                }
            }
        }
    }

    protected void installUtilityProductions(PrismarineProductions productions, TokenStructureMatch providerStructure) {

    }

    protected void installOrphanUnit(PatternProviderUnit unit, TokenPatternMatch createdMatch, PrismarineProductions productions) {
        throw new NullPointerException("Given null for both the internal name of the PatternProviderSet and the PatternProviderUnit");
    }
}
