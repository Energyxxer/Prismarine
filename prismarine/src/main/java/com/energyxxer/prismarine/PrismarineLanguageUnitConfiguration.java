package com.energyxxer.prismarine;

import com.energyxxer.enxlex.lexical_analysis.profiles.LexerProfile;
import com.energyxxer.prismarine.in.ProjectReader;
import com.energyxxer.prismarine.operators.OperatorPool;
import com.energyxxer.prismarine.summaries.PrismarineSummaryModule;

import java.io.File;
import java.nio.file.Path;

public abstract class PrismarineLanguageUnitConfiguration<T extends PrismarineLanguageUnit> {
    public abstract Class<T> getUnitClass();

    public abstract int getNumberOfPasses();
    public abstract void performPass(T unit, PrismarineCompiler compiler, int passNumber);

    public abstract OperatorPool getOperatorPool();

    public abstract void setupProductions(PrismarineProductions productions);

    public abstract String getStopPath();

    public abstract LexerProfile createLexerProfile();

    public abstract PrismarineSummaryModule createSummaryModule(File file, Path relativePath);

    public abstract T createUnit(PrismarineCompiler compiler, ProjectReader.Result readResult);

    public void onPassStart(PrismarineCompiler compiler, int passNumber) {}
    public void onPassEnd(PrismarineCompiler compiler, int passNumber) {}
}
