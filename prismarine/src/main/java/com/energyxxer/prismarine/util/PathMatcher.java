package com.energyxxer.prismarine.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Modified from com.sun.nio.zipfs.ZipUtils, decompiled using Fernflower in IntelliJ IDEA.
 *
 * The following changes have been made:
 *   1. Parentheses are no longer escaped (isRegexMeta)
 *
 * The purpose of these changes is to allow the following:
 *   1. Usage of capture groups in the created pattern.
 * */
public class PathMatcher {

    public static Pattern compile(String str) {
        return Pattern.compile(toRegexPattern(str));
    }

    public static PathMatcher createMatcher(String regex) {
        return new PathMatcher(compile(regex));
    }

    private static final char EOL = 0;

    private static char next(String var0, int var1) {
        return var1 < var0.length() ? var0.charAt(var1) : EOL;
    }

    private static boolean isRegexMeta(char var0) {
        return ".^$+{[]|".indexOf(var0) != -1;
    }

    private static boolean isGlobMeta(char var0) {
        return "\\*?[{".indexOf(var0) != -1;
    }

    private static String toRegexPattern(String var0) {
        boolean var1 = false;
        StringBuilder var2 = new StringBuilder("^");
        int var3 = 0;

        while(true) {
            while(var3 < var0.length()) {
                char var4 = var0.charAt(var3++);
                switch(var4) {
                    case '*':
                        if (next(var0, var3) == '*') {
                            var2.append(".*");
                            ++var3;
                        } else {
                            var2.append("[^/]*");
                        }
                        break;
                    case ',':
                        if (var1) {
                            var2.append(")|(?:");
                        } else {
                            var2.append(',');
                        }
                        break;
                    case '/':
                        var2.append(var4);
                        break;
                    case '?':
                        var2.append("[^/]");
                        break;
                    case '[':
                        var2.append("[[^/]&&[");
                        if (next(var0, var3) == '^') {
                            var2.append("\\^");
                            ++var3;
                        } else {
                            if (next(var0, var3) == '!') {
                                var2.append('^');
                                ++var3;
                            }

                            if (next(var0, var3) == '-') {
                                var2.append('-');
                                ++var3;
                            }
                        }

                        boolean var6 = false;
                        char var7 = 0;

                        while(var3 < var0.length()) {
                            var4 = var0.charAt(var3++);
                            if (var4 == ']') {
                                break;
                            }

                            if (var4 == '/') {
                                throw new PatternSyntaxException("Explicit 'name separator' in class", var0, var3 - 1);
                            }

                            if (var4 == '\\' || var4 == '[' || var4 == '&' && next(var0, var3) == '&') {
                                var2.append('\\');
                            }

                            var2.append(var4);
                            if (var4 == '-') {
                                if (!var6) {
                                    throw new PatternSyntaxException("Invalid range", var0, var3 - 1);
                                }

                                if ((var4 = next(var0, var3++)) == EOL || var4 == ']') {
                                    break;
                                }

                                if (var4 < var7) {
                                    throw new PatternSyntaxException("Invalid range", var0, var3 - 3);
                                }

                                var2.append(var4);
                                var6 = false;
                            } else {
                                var6 = true;
                                var7 = var4;
                            }
                        }

                        if (var4 != ']') {
                            throw new PatternSyntaxException("Missing ']", var0, var3 - 1);
                        }

                        var2.append("]]");
                        break;
                    case '\\':
                        if (var3 == var0.length()) {
                            throw new PatternSyntaxException("No character to escape", var0, var3 - 1);
                        }

                        char var5 = var0.charAt(var3++);
                        if (isGlobMeta(var5) || isRegexMeta(var5)) {
                            var2.append('\\');
                        }

                        var2.append(var5);
                        break;
                    case '{':
                        if (var1) {
                            throw new PatternSyntaxException("Cannot nest groups", var0, var3 - 1);
                        }

                        var2.append("(?:(?:");
                        var1 = true;
                        break;
                    case '}':
                        if (var1) {
                            var2.append("))");
                            var1 = false;
                        } else {
                            var2.append('}');
                        }
                        break;
                    default:
                        if (isRegexMeta(var4)) {
                            var2.append('\\');
                        }

                        var2.append(var4);
                }
            }

            if (var1) {
                throw new PatternSyntaxException("Missing '}", var0, var3 - 1);
            }

            return var2.append('$').toString();
        }
    }

    private final Pattern pattern;
    private Matcher matcher;

    public PathMatcher(Pattern pattern) {
        this.pattern = pattern;
    }

    public Result getMatchResult(String input) {
        if(matcher == null) matcher = pattern.matcher(input);
        else matcher.reset(input);

        Result result = new Result();
        result.matched = matcher.matches();
        result.hitEnd = matcher.hitEnd();
        if(result.matched) {
            result.groups = new String[matcher.groupCount()+1];

            for(int i = 0; i < result.groups.length; i++) {
                result.groups[i] = matcher.group(i);
            }
        }

        return result;
    }

    public static class Result {
        public boolean matched;
        public boolean hitEnd;
        public String[] groups;

        public Result() {
        }
    }
}
