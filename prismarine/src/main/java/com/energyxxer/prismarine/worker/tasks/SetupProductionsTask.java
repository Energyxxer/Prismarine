package com.energyxxer.prismarine.worker.tasks;

import com.energyxxer.prismarine.PrismarineLanguageUnitConfiguration;
import com.energyxxer.prismarine.PrismarineProductions;
import com.energyxxer.prismarine.worker.PrismarineProjectWorker;
import com.energyxxer.prismarine.worker.PrismarineProjectWorkerTask;

import java.util.HashMap;

public class SetupProductionsTask extends PrismarineProjectWorkerTask<HashMap<PrismarineLanguageUnitConfiguration, PrismarineProductions>> {

    public static final SetupProductionsTask INSTANCE = new SetupProductionsTask();

    private SetupProductionsTask() {}

    @Override
    public HashMap<PrismarineLanguageUnitConfiguration, PrismarineProductions> perform(PrismarineProjectWorker worker) throws Exception {
        HashMap<PrismarineLanguageUnitConfiguration, PrismarineProductions> productionMap = new HashMap<>();
        for(PrismarineLanguageUnitConfiguration unitConfig : worker.suiteConfig.getLanguageUnitConfigurations().values()) {
            PrismarineProductions unitProductions = new PrismarineProductions(worker, unitConfig);
            unitConfig.setupProductions(unitProductions);
            productionMap.put(unitConfig, unitProductions);
        }
        return productionMap;
    }

    @Override
    public String getProgressMessage() {
        return "Setting up syntax";
    }

    @Override
    public PrismarineProjectWorkerTask[] getImplications() {
        return new PrismarineProjectWorkerTask[0];
    }
}
