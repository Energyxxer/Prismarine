package com.energyxxer.enxlex.lexical_analysis.profiles;

import com.energyxxer.enxlex.lexical_analysis.token.TokenSection;
import com.energyxxer.enxlex.lexical_analysis.token.TokenType;
import com.energyxxer.util.StringLocation;

import java.util.HashMap;

/**
 * Defines a response model for custom analysis contexts.
 */
public class ScannerContextResponse {
    private static ThreadLocal<ScannerContextResponse> INSTANCE = ThreadLocal.withInitial(ScannerContextResponse::new);
    public static final ScannerContextResponse FAILED = new ScannerContextResponse(false);

    /**
     * Whether the analysis was successful and returned a token.
     * */
    public boolean success;
    /**
     * The full string that makes up the token associated with
     * the semanticContext, if applicable. null if the analysis wasn't successful.
     * */
    public String value;
    /**
     * The token type of the return token, if applicable.
     * null if the analysis wasn't successful.
     * */
    public TokenType tokenType;
    /**
     * The location within the string passed to the analysis method where
     * the given token ends, relative to the starting index.
     * Should include the index, line and column CHANGES.
     * null if the analysis wasn't successful.
     * */
    public StringLocation endLocation;
    /**
     * A map containing the indices at which the string should be formatted differently,
     * with the given syntax key.
     * */
    public HashMap<TokenSection, String> subSections;
    /**
     * A field containing a possible error message for the semanticContext response.
     * */
    public String errorMessage = null;
    /**
     * A field containing the index at which the error occurs, if any. Relative to the starting index.
     * */
    public int errorIndex = -1;
    /**
     * A field containing the length of the error, if any.
     * */
    public int errorLength = 0;
    /**
     * Whether this response is currently being read by the caller of an analyze method.
     * */
    private boolean locked = false;

    public ScannerContextResponse() {
    }

    /**
     * Creates a response from the given success value. This should <b>only</b> be used
     * when the analysis wasn't successful, otherwise the EagerLexer might throw a NullPointerException.
     *
     * @param success Whether the analysis was successful. For this constructor, it should only be false.
     * */
    private ScannerContextResponse(boolean success) {
        this.success = success;
    }

    /**
     * Creates a response.
     *
     * @param value The value of the resulting token.
     * @param tokenType The type of the resulting token.
     * <br>
     * <br>
     * The end location will be assumed to be:
     * <ul>
     * <li>index: Equal to the length of the value parameter.</li>
     * <li>line: 0</li>
     * <li>column: Equal to the length of the value parameter.</li>
     * </ul>
     * */
    public static ScannerContextResponse success(String value, TokenType tokenType) {
        return success(value, new StringLocation(value.length()), tokenType);
    }

    /**
     * Creates a response.
     *
     * @param value The value of the resulting token.
     * @param tokenType The type of the resulting token.
     * @param subSections Map containing sections of the string to be formatted differently.
     * <br>
     * <br>
     * The end location will be assumed to be:
     * <ul>
     * <li>index: Equal to the length of the value parameter.</li>
     * <li>line: 0</li>
     * <li>column: Equal to the length of the value parameter.</li>
     * </ul>
     * */
    public static ScannerContextResponse success(String value, TokenType tokenType, HashMap<TokenSection, String> subSections) {
        return success(value, new StringLocation(value.length()), tokenType, subSections);
    }


    /**
     * Creates a response.
     *
     * @param value The value of the resulting token.
     * @param endLoc The location of the end index within the substring.
     * @param tokenType The type of the resulting token.
     * */
    public static ScannerContextResponse success(String value, StringLocation endLoc, TokenType tokenType) {
        return success(value, endLoc, tokenType, null);
    }


    /**
     * Creates a response.
     *
     * @param value The value of the resulting token.
     * @param endLoc The location of the end index within the substring.
     * @param tokenType The type of the resulting token.
     * @param subSections Map containing sections of the string to be formatted differently.
     * */
    public static ScannerContextResponse success(String value, StringLocation endLoc, TokenType tokenType, HashMap<TokenSection, String> subSections) {
        ScannerContextResponse instance = INSTANCE.get();
        if(instance.locked) {
            throw new IllegalStateException("Must unlock ScannerContextResponse before trying to create another. Please report ASAP, and include the stack trace");
        }

        instance.success = true;
        instance.value = value;
        instance.endLocation = endLoc;
        instance.tokenType = tokenType;
        instance.subSections = subSections;
        instance.locked = true;

        return instance;
    }

    /**
     * Sets the error fields with the given information.
     *
     * @param message The error message.
     * @param index The index at which the error occurs, relative to the starting index passed to the lexer context.
     * @param length The length of the error.
     * */
    public void setError(String message, int index, int length) {
        this.errorMessage = message;
        this.errorIndex = index;
        this.errorLength = length;
    }

    public void unlock() {
        locked = false;
    }
}
