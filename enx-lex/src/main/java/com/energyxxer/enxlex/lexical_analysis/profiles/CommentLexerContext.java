package com.energyxxer.enxlex.lexical_analysis.profiles;

import com.energyxxer.enxlex.lexical_analysis.token.TokenSection;
import com.energyxxer.enxlex.lexical_analysis.token.TokenType;

import java.util.HashMap;
import java.util.Locale;

public class CommentLexerContext implements LexerContext {
    private final String commentStart;
    private final String commentEnd;
    private final boolean includeEnd;
    private final TokenType handledType;

    public CommentLexerContext(String commentStart, TokenType handledType) {
        this.commentStart = commentStart;
        this.commentEnd = "\n";
        this.includeEnd = false;
        this.handledType = handledType;
    }
    public CommentLexerContext(String commentStart, String commentEnd, TokenType handledType) {
        this.commentStart = commentStart;
        this.commentEnd = commentEnd;
        this.includeEnd = true;
        this.handledType = handledType;
    }

    @Override
    public ScannerContextResponse analyze(String str, int startIndex, LexerProfile profile) {
        if(!str.startsWith(commentStart, startIndex)) return ScannerContextResponse.FAILED;

        int endIndex = str.indexOf(commentEnd, startIndex);
        if(endIndex != -1) {
            if(includeEnd) endIndex += commentEnd.length();
            if(endIndex > 0 && str.charAt(endIndex-1) == '\r') endIndex--;
        } else endIndex = str.length();


        if(endIndex != -1) {
            return handleComment(str.substring(startIndex, endIndex));
        } else return handleComment(str.substring(startIndex));
    }

    private ScannerContextResponse handleComment(String str) {
        HashMap<TokenSection, String> sections = new HashMap<>();
        int todoIndex = str.toUpperCase(Locale.ENGLISH).indexOf("TODO");
        if(todoIndex >= 0) {
            int todoEnd = str.indexOf("\n");
            if(todoEnd < 0) todoEnd = str.length();
            sections.put(new TokenSection(todoIndex, todoEnd-todoIndex), "comment.todo");
        }
        return new ScannerContextResponse(true, str, handledType, sections);
    }

    @Override
    public ContextCondition getCondition() {
        return ContextCondition.LINE_START;
    }

    @Override
    public boolean handlesType(TokenType type) {
        return type == handledType;
    }
}
