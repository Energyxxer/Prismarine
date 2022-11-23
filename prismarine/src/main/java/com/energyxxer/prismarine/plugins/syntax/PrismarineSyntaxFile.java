package com.energyxxer.prismarine.plugins.syntax;

import com.energyxxer.enxlex.lexical_analysis.EagerLexer;
import com.energyxxer.enxlex.lexical_analysis.token.TokenSource;
import com.energyxxer.enxlex.pattern_matching.TokenMatchResponse;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.prismarine.PrismarineLanguageUnitConfiguration;
import com.energyxxer.prismarine.plugins.PrismarinePluginFile;
import com.energyxxer.prismarine.plugins.PrismarinePluginUnit;
import com.energyxxer.prismarine.worker.PrismarineProjectWorker;

import java.io.IOException;
import java.nio.file.Path;

public class PrismarineSyntaxFile extends PrismarinePluginFile<TokenPatternMatch> {

    public PrismarineSyntaxFile(Path relativePath, PrismarinePluginUnit unit, String rawSyntaxFile) {
        super(relativePath, unit);
        this.string = rawSyntaxFile;
    }

    public void update(PrismarineProjectWorker worker) throws IOException {
        update(null, worker);
    }

    @Override
    public void update(PrismarineLanguageUnitConfiguration langUnitConfig,  PrismarineProjectWorker worker) throws IOException {
        //Since this uses an eager lexer and a fixed production...
        EagerLexer lexer = unit.getDefiningPlugin().getEagerLexer();

        TokenSource source = unit.getDefiningPlugin().getWalker().getReader().getSourceFunction().apply(relativePath);

        lexer.start(source, string, new PrismarineMetaLexerProfile());

        lexer.getStream().tokens.remove(0);

        TokenMatchResponse response = PrismarineMetaProductions.FILE.match(0, lexer);

        lexer.clear();

        if(!response.matched) {
            response.discard();
            throw new IOException("Syntax error in Meta Syntax file '" + source + "': " + response.getErrorMessage());
        }

        pattern = response.pattern;
        response.discard();
    }

    public void createSyntax(PrismarineMetaBuilder builder) throws IOException {
        if(pattern == null) return;
        try {
            builder.build(unit);
        } catch(PrismarineMetaBuilder.PrismarineMetaException x) {
            throw new IOException("Parsing error in Meta Syntax file: " + x.getErrorMessage() + "; Caused by: " + x.getCausedBy().getLocation());
        }
        output = builder.getReturnValue();
    }

}
