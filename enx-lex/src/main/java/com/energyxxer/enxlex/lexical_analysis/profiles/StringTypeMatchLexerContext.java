package com.energyxxer.enxlex.lexical_analysis.profiles;

import com.energyxxer.enxlex.lexical_analysis.token.TokenType;

public class StringTypeMatchLexerContext implements LexerContext {
    private final String[] strings;
    private final TokenType[] types;

    public StringTypeMatchLexerContext(String[] strings, TokenType[] types) {
        this.strings = strings;
        this.types = types;
    }

    @Override
    public ScannerContextResponse analyze(String str, int startIndex, LexerProfile profile) {
        for(int i = 0; i < strings.length; i++) {
            if(str.startsWith(strings[i], startIndex) && (str.length() == startIndex + strings[i].length() || !(profile.canMerge(str.charAt(startIndex+strings[i].length()-1), str.charAt(startIndex+strings[i].length())))))
                return ScannerContextResponse.success(strings[i], types[i]);
        }
        return ScannerContextResponse.FAILED;
    }

    @Override
    public ScannerContextResponse analyzeExpectingType(String str, int startIndex, TokenType type, LexerProfile profile) {
        for(int i = 0; i < strings.length; i++) {
            if(type == types[i] && str.startsWith(strings[i], startIndex)) {
                return ScannerContextResponse.success(strings[i], types[i]);
            }
        }
        return ScannerContextResponse.FAILED;
    } //substring done

    @Override
    public boolean handlesType(TokenType type) {
        for(TokenType tokenType : types) {
            if (tokenType == type) return true;
        }
        return false;
    }
}
