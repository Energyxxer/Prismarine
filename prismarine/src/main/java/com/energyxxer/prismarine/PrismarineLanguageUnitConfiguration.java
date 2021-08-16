package com.energyxxer.prismarine;

import com.energyxxer.enxlex.lexical_analysis.profiles.LexerProfile;
import com.energyxxer.enxlex.lexical_analysis.token.TokenSource;
import com.energyxxer.prismarine.in.ProjectReader;
import com.energyxxer.prismarine.operators.OperatorPool;
import com.energyxxer.prismarine.summaries.PrismarineProjectSummary;
import com.energyxxer.prismarine.summaries.PrismarineSummaryModule;
import com.energyxxer.prismarine.worker.PrismarineProjectWorker;

import java.nio.file.Path;

public abstract class PrismarineLanguageUnitConfiguration<T extends PrismarineLanguageUnit> {
    public abstract Class<T> getUnitClass();

    public abstract int getNumberOfPasses();
    public abstract PrismarineCompiler.PassResult performPass(T unit, PrismarineCompiler compiler, int passNumber);

    public abstract OperatorPool getOperatorPool();

    public abstract void setupProductions(PrismarineProductions productions, PrismarineProjectWorker worker);

    public abstract String getStopPath();

    public abstract LexerProfile createLexerProfile();

    public abstract PrismarineSummaryModule createSummaryModule(TokenSource source, Path relativePath, PrismarineProjectSummary parentSummary);

    public abstract T createUnit(PrismarineCompiler compiler, ProjectReader.Result readResult);

    public void onPassStart(PrismarineCompiler compiler, int passNumber) {}
    public void onPassEnd(PrismarineCompiler compiler, int passNumber) {}

    public boolean consumeCompilerWalkerStop() {
        return true;
    }
    public boolean consumeSummaryWalkerStop() {
        return true;
    }
}
