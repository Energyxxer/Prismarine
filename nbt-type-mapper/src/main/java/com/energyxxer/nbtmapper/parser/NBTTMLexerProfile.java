package com.energyxxer.nbtmapper.parser;

import com.energyxxer.commodore.defpacks.CategoryDeclaration;
import com.energyxxer.commodore.defpacks.DefinitionPack;
import com.energyxxer.commodore.module.CommandModule;
import com.energyxxer.enxlex.lexical_analysis.profiles.*;
import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.enxlex.lexical_analysis.token.TokenType;
import com.energyxxer.util.PatternCache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

import static com.energyxxer.nbtmapper.parser.NBTTMTokens.*;

public class NBTTMLexerProfile extends LexerProfile {

    /**
     * Holds the previous token for multi-token analysis.
     * */
    private final Token tokenBuffer = null;

    public NBTTMLexerProfile(DefinitionPack defPack) {
        ArrayList<String> defcategories = new ArrayList<>();
        try {
            defPack.load();
            for(CategoryDeclaration decl : defPack.getDefinedCategories()) {
                defcategories.add(decl.getCategory().toLowerCase());
            }
        } catch (IOException e) {
            defcategories.addAll(Arrays.asList(PatternCache.split("entity, block, item, particle, enchantment, dimension, effect, difficulty, gamemode, gamerule, slot, attributes", ", ")));
            e.printStackTrace();
        }

        this.initialize(defcategories);
    }

    public NBTTMLexerProfile(CommandModule module) {
        this.initialize(module.minecraft.getAllValidCategoriesAndAliases());
    }

    private void initialize(Collection<String> defcategories) {
        contexts.add(new StringTypeMatchLexerContext(
                new String[] {",", ":", ";", "#", "*"},
                new TokenType[] {COMMA, COLON, SEMICOLON, HASH, WILDCARD}
        ));
        contexts.add(new StringMatchLexerContext(BRACE, "{", "}", "[", "]", "(", ")"));
        contexts.add(new StringMatchLexerContext(PRIMITIVE_TYPE, "Byte", "Short", "Int", "Float", "Double", "Long", "String", "JSON_Boolean"));
        contexts.add(new StringMatchLexerContext(ARRAY_TYPE, "B", "I", "L"));
        contexts.add(new RegexLexerContext(Pattern.compile("\\$[a-zA-Z0-9_]*"), REFERENCE, false));
        contexts.add(new RegexLexerContext(Pattern.compile("[a-zA-Z0-9_]+"), KEY, false));
        contexts.add(new RegexLexerContext(Pattern.compile("[a-zA-Z0-9_]+"), IDENTIFIER, false));

        contexts.add(new StringMatchLexerContext(DEFINITION_CATEGORY, defcategories.toArray(new String[0])));


        //String literals
        contexts.add(new StringLiteralLexerContext("\"", STRING_LITERAL));

        //Comments
        contexts.add(new CommentLexerContext("#", COMMENT) {
            @Override
            public ContextCondition getCondition() {
                return ContextCondition.LINE_START;
            }
        });
    }

    @Override
    public boolean canMerge(char ch0, char ch1) {
        return Character.isJavaIdentifierPart(ch0) && Character.isJavaIdentifierPart(ch1);
    }

    @Override
    public void putHeaderInfo(Token header) {
        header.putAttribute("TYPE","nbttm");
        header.putAttribute("DESC","NBT Type Map File");
    }
}
