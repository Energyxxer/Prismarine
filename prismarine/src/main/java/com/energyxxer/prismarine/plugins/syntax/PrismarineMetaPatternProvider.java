package com.energyxxer.prismarine.plugins.syntax;

import com.energyxxer.enxlex.lexical_analysis.token.TokenType;
import com.energyxxer.enxlex.pattern_matching.matching.lazy.TokenGroupMatch;
import com.energyxxer.enxlex.pattern_matching.matching.lazy.TokenItemMatch;
import com.energyxxer.enxlex.pattern_matching.matching.lazy.TokenStructureMatch;
import com.energyxxer.prismarine.PrismarineProductions;
import com.energyxxer.prismarine.providers.PatternProviderSet;

import static com.energyxxer.prismarine.PrismarineProductions.*;
import static com.energyxxer.prismarine.plugins.syntax.PrismarineMetaLexerProfile.*;
import static com.energyxxer.prismarine.plugins.syntax.PrismarineMetaProductions.stringMatch;

public class PrismarineMetaPatternProvider extends PatternProviderSet {


    public PrismarineMetaPatternProvider() {
        super(null);
    }

    @Override
    protected void installUtilityProductions(PrismarineProductions productions, TokenStructureMatch providerStructure) {
        TokenStructureMatch STATEMENT = new TokenStructureMatch("STATEMENT");

        TokenStructureMatch VALUE = new TokenStructureMatch("VALUE");
        TokenStructureMatch ROOT_VALUE = new TokenStructureMatch("ROOT_VALUE");

        ROOT_VALUE.add(ofType(IDENTIFIER).setName("ROOT_IDENTIFIER"));
        ROOT_VALUE.add(ofType(FUNCTION).setName("ROOT_FUNCTION"));
        ROOT_VALUE.add(ofType(STRING_LITERAL).setName("ROOT_STRING_LITERAL"));
        ROOT_VALUE.add(ofType(BOOLEAN).setName("ROOT_BOOLEAN"));

        TokenStructureMatch MEMBER_ACCESS = new TokenStructureMatch("MEMBER_ACCESS");
        MEMBER_ACCESS.add(group(ofType(DOT), ofType(FUNCTION).setName("FUNCTION_NAME")).setName("FUNCTION_MEMBER"));
        MEMBER_ACCESS.add(group(stringMatch(BRACE, "("), list(VALUE, new TokenItemMatch(COMMA)).setOptional().setName("ARGUMENT_LIST"), stringMatch(BRACE, ")")).setName("FUNCTION_CALL"));

        VALUE.add(group(ROOT_VALUE, list(MEMBER_ACCESS).setOptional().setName("MEMBER_ACCESS_LIST")).setName("MEMBER_ACCESS_VALUE"));

        STATEMENT.add(group(stringMatch(KEYWORD, "define"), ofType(IDENTIFIER).setName("DEFINITION_NAME"), ofType(EQUALS), VALUE, ofType(SEMICOLON)).setName("DEFINE_STATEMENT"));

        TokenGroupMatch RETURN_STATEMENT = group(stringMatch(KEYWORD, "return"), VALUE, ofType(SEMICOLON)).setName("RETURN_STATEMENT");

        productions.addFileGroup(group(list(STATEMENT).setOptional().setName("STATEMENT_LIST"), RETURN_STATEMENT, ofType(TokenType.END_OF_FILE)));
    }
}
