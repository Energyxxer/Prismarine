package com.energyxxer.prismarine.worker;

public abstract class PrismarineProjectWorkerTask<T> {
    public abstract T perform(PrismarineProjectWorker worker) throws Exception;
    public abstract String getProgressMessage();

    public abstract PrismarineProjectWorkerTask[] getImplications();
}
