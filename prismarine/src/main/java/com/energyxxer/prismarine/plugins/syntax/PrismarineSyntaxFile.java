package com.energyxxer.prismarine.plugins.syntax;

import com.energyxxer.enxlex.lexical_analysis.EagerLexer;
import com.energyxxer.enxlex.pattern_matching.TokenMatchResponse;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.prismarine.PrismarineLanguageUnitConfiguration;
import com.energyxxer.prismarine.plugins.PrismarinePluginFile;
import com.energyxxer.prismarine.plugins.PrismarinePluginUnit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class PrismarineSyntaxFile extends PrismarinePluginFile<TokenPatternMatch> {

    public PrismarineSyntaxFile(Path relativePath, PrismarinePluginUnit unit, String rawSyntaxFile) {
        super(relativePath, unit);
        this.string = rawSyntaxFile;
    }

    public void update() throws IOException {
        update(null);
    }

    @Override
    public void update(PrismarineLanguageUnitConfiguration langUnitConfig) throws IOException {
        //Since this uses an eager lexer and a fixed production...
        EagerLexer lexer = unit.getDefiningPlugin().getEagerLexer();

        File file = relativePath.toFile();

        lexer.start(file, string, new PrismarineMetaLexerProfile());

        lexer.getStream().tokens.remove(0);

        TokenMatchResponse response = PrismarineMetaProductions.FILE.match(0, lexer);

        lexer.getStream().tokens.clear();

        if(!response.matched) {
            throw new IOException("Syntax error in Meta Syntax file '" + file + "': " + response.getErrorMessage());
        }

        pattern = response.pattern;
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
