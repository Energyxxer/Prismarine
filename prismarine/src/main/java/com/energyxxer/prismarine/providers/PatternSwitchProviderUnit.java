package com.energyxxer.prismarine.providers;

public interface PatternSwitchProviderUnit extends PatternProviderUnit {
    @Override
    default String[] getTargetProductionNames() {
        return null;
    }

    String[] getSwitchKeys();
}
