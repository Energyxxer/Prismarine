package com.energyxxer.prismarine.providers;

public interface PatternSwitchProviderUnit<CTX> extends PatternProviderUnit<CTX> {
    @Override
    default String[] getTargetProductionNames() {
        return null;
    }

    String[] getSwitchKeys();
}
