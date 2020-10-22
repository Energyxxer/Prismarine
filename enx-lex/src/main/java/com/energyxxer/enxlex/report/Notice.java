package com.energyxxer.enxlex.report;

import com.energyxxer.enxlex.lexical_analysis.token.Token;
import com.energyxxer.enxlex.lexical_analysis.token.TokenSource;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.util.StringBounds;

/**
 * Created by User on 5/15/2017.
 */
public class Notice {
    private NoticeType type;
    private String message;
    private String extendedMessage;

    private TokenSource source;
    private int locationIndex;
    private int locationLength;

    private StackTrace stackTrace;

    private String group;

    public Notice(NoticeType type, String message) {
        this(null, type, message);
    }

    public Notice(String group, NoticeType type, String message) {
        this(group, type, message, message);
    }

    public Notice(NoticeType type, String message, TokenPattern<?> pattern) {
        this(type, message, message, pattern);
    }

    public Notice(NoticeType type, String message, String extendedMessage, TokenPattern<?> pattern) {
        this(null, type, message, extendedMessage, pattern);
    }

    public Notice(String group, NoticeType type, String message, TokenPattern<?> pattern) {
        this(group, type, message, message, pattern);
    }

    public Notice(String group, NoticeType type, String message, String extendedMessage, TokenPattern<?> pattern) {
        this(group, type, message, extendedMessage);
        if(pattern != null) setSourceLocation(pattern);
    }

    public Notice(NoticeType type, String message, Token token) {
        this(null, type, message, token);
    }

    public Notice(String group, NoticeType type, String message, Token token) {
        this(group, type, message);
        if(token != null) setSourceLocation(token);
    }

    public Notice(String group, NoticeType type, String message, String extendedMessage) {
        this.type = type;
        this.message = message;
        this.extendedMessage = extendedMessage;
        if(group != null) this.group = group;
    }

    public NoticeType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public void setType(NoticeType type) {
        this.type = type;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSourceLocation(Token token) {
        pointToSource(token.source);
        this.locationIndex = token.loc.index;
        this.locationLength = token.value.length();
    }

    public void setSourceLocation(TokenPattern<?> pattern) {
        pointToSource(pattern.getSource());
        StringBounds bounds = pattern.getStringBounds();
        if(bounds != null) {
            this.locationIndex = bounds.start.index;
            this.locationLength = bounds.end.index - this.locationIndex;
        }
    }

    public void setSourceLocation(TokenSource source, int index, int length) {
        pointToSource(source);
        this.locationIndex = index;
        this.locationLength = length;
    }

    public TokenSource getSource() {
        return source;
    }

    public int getLocationIndex() {
        return locationIndex;
    }

    public int getLocationLength() {
        return locationLength;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return message;
    }

    public String getExtendedMessage() {
        return extendedMessage;
    }

    public void setExtendedMessage(String extendedMessage) {
        this.extendedMessage = extendedMessage;
    }

    public StackTrace getStackTrace() {
        return stackTrace;
    }

    public Notice setStackTrace(StackTrace stackTrace) {
        this.stackTrace = stackTrace;
        return this;
    }

    public void pointToSource(TokenSource source) {
        this.source = source;
        this.group = source.getPrettyName();
    }
}
