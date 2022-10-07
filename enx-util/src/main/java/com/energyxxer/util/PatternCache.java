package com.energyxxer.util;

import org.intellij.lang.annotations.RegExp;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternCache {
    private static HashMap<String, Pattern> literalCache = new HashMap<>();
    private static HashMap<String, Pattern> regexCache = new HashMap<>();
    public static Pattern NEWLINE = literal("\n");

    public static Pattern literal(String s) {
        Pattern cached = literalCache.get(s);
        if(cached == null) {
            Pattern created = Pattern.compile(s, Pattern.LITERAL);
            literalCache.put(s, created);
            return created;
        }
        return cached;
    }
    public static Pattern regex(@RegExp String regex) {
        Pattern cached = regexCache.get(regex);
        if(cached == null) {
            Pattern created = Pattern.compile(regex);
            regexCache.put(regex, created);
            return created;
        }
        return cached;
    }
    public static String replace(String str, String substr, String replacement) {
        return literal(substr).matcher(str).replaceAll(Matcher.quoteReplacement(replacement));
    }
    public static String replaceRegex(String str, @RegExp String substr, String replacement) {
        return regex(substr).matcher(str).replaceAll(Matcher.quoteReplacement(replacement));
    }
    public static String[] split(String str, String delimiter) {
        return literal(delimiter).split(str);
    }
    public static String[] splitRegex(String str, @RegExp String delimiter) {
        return regex(delimiter).split(str);
    }
}
