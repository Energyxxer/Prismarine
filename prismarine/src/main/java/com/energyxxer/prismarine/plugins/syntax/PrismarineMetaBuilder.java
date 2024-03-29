package com.energyxxer.prismarine.plugins.syntax;

import com.energyxxer.commodore.CommandUtils;
import com.energyxxer.enxlex.lexical_analysis.token.TokenType;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.enxlex.pattern_matching.matching.lazy.TokenGroupMatch;
import com.energyxxer.enxlex.pattern_matching.matching.lazy.TokenItemMatch;
import com.energyxxer.enxlex.pattern_matching.matching.lazy.TokenListMatch;
import com.energyxxer.enxlex.pattern_matching.matching.lazy.TokenStructureMatch;
import com.energyxxer.enxlex.pattern_matching.structures.TokenList;
import com.energyxxer.enxlex.pattern_matching.structures.TokenPattern;
import com.energyxxer.enxlex.pattern_matching.structures.TokenStructure;
import com.energyxxer.enxlex.suggestions.SuggestionTags;
import com.energyxxer.prismarine.PrismarineProductions;
import com.energyxxer.prismarine.plugins.PrismarinePluginUnit;
import com.energyxxer.prismarine.plugins.PrismarinePluginUnitConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public class PrismarineMetaBuilder {

    public static final String PLUGIN_CREATED_TAG = "__PLUGIN_CREATED";

    private final ArrayList<FunctionDefinition> FUNCTIONS = new ArrayList<>();

    private final PrismarinePluginUnitConfiguration unitConfig;
    private final TokenPattern<?> filePattern;
    private final PrismarineProductions productions;

    protected final HashMap<String, Value> variables = new HashMap<>();

    protected TokenPatternMatch returnValue = null;

    public PrismarineMetaBuilder(PrismarinePluginUnitConfiguration unitConfig, TokenPattern<?> filePattern, PrismarineProductions productions) {
        this.unitConfig = unitConfig;
        this.filePattern = filePattern;
        this.productions = productions;

        registerFunction("group", (ignore, args) -> {
            TokenGroupMatch g = new TokenGroupMatch();
            g.addTags(PLUGIN_CREATED_TAG);
            for(Value arg : args) {
                if(arg instanceof TokenMatchValue) {
                    g.append(((TokenMatchValue) arg).patternMatch);
                } else throw new IllegalArgumentException("Function 'group' only accepts Token Match values, found " + arg.getClass().getSimpleName());
            }
            return new TokenMatchValue(g);
        });
        registerFunction("optional", (ignore, args) -> {
            TokenGroupMatch g = new TokenGroupMatch(true);
            g.addTags(PLUGIN_CREATED_TAG);
            for(Value arg : args) {
                if(arg instanceof TokenMatchValue) {
                    g.append(((TokenMatchValue) arg).patternMatch);
                } else throw new IllegalArgumentException("Function 'optional' only accepts Token Match values, found " + arg.getClass().getSimpleName());
            }
            return new TokenMatchValue(g);
        });
        registerFunction("choice", (ignore, args) -> {
            TokenStructureMatch s = new TokenStructureMatch("CHOICE");
            s.addTags(PLUGIN_CREATED_TAG);
            if(args.size() == 0) {
                throw new IllegalArgumentException("Function 'choice' requires at least 1 parameter, found " + args.size());
            }
            for(Value arg : args) {
                if(arg instanceof TokenMatchValue) {
                    s.add(((TokenMatchValue) arg).patternMatch);
                } else if(arg instanceof StringLiteralValue) {
                    String text = ((StringLiteralValue) arg).stringValue;
                    s.add(new TokenItemMatch(TokenType.UNKNOWN, text).setName("LITERAL_" + text.toUpperCase()).addTags(SuggestionTags.ENABLED, PLUGIN_CREATED_TAG));
                } else {
                    throw new IllegalArgumentException("Function 'choice' only accepts Token Match values or Strings, found " + arg.getClass().getSimpleName());
                }
            }
            return new TokenMatchValue(s);
        });
        registerFunction("list", (ignore, args) -> {
            TokenPatternMatch listValue;
            TokenPatternMatch separatorValue = null;
            if(args.size() >= 1) {
                if(args.get(0) instanceof TokenMatchValue) {
                    listValue = ((TokenMatchValue) args.get(0)).patternMatch;
                } else {
                    throw new IllegalArgumentException("Function 'list' only accepts Token Match values at argument 0, found " + args.get(0).getClass().getSimpleName());
                }
            } else {
                throw new IllegalArgumentException("Function 'list' requires at least 1 parameter, found " + args.size());
            }
            if(args.size() >= 2) {
                if(args.get(1) instanceof TokenMatchValue) {
                    separatorValue = ((TokenMatchValue) args.get(1)).patternMatch;
                } else {
                    throw new IllegalArgumentException("Function 'list' only accepts Token Match values at argument 1, found " + args.get(1).getClass().getSimpleName());
                }
            }
            return new TokenMatchValue(new TokenListMatch(listValue, separatorValue).addTags(PLUGIN_CREATED_TAG));
        });
        registerFunction("name", TokenMatchValue.class, (value, args) -> {
            if(args.size() >= 1) {
                if(args.get(0) instanceof StringLiteralValue) {
                    if(((TokenMatchValue) value).patternMatch.hasTag(PLUGIN_CREATED_TAG)) {
                        ((TokenMatchValue) value).patternMatch.setName(((StringLiteralValue) args.get(0)).stringValue);
                    } else {
                        throw new IllegalArgumentException("Function 'name' can only be performed on plugin-created patterns");
                    }
                } else {
                    throw new IllegalArgumentException("Function 'name' only accepts String Literal values at argument 0, found " + args.get(0).getClass().getSimpleName());
                }
            } else {
                throw new IllegalArgumentException("Function 'name' requires at least 1 parameter, found " + args.size());
            }
            return value;
        });
        registerFunction("hint", TokenMatchValue.class, (value, args) -> {
            if(args.size() >= 1) {
                if(args.get(0) instanceof StringLiteralValue) {
                    if(((TokenMatchValue) value).patternMatch.hasTag(PLUGIN_CREATED_TAG)) {
                        ((TokenMatchValue) value).patternMatch.addTags("cspn:" + ((StringLiteralValue) args.get(0)).stringValue);
                    } else {
                        throw new IllegalArgumentException("Function 'hint' can only be performed on plugin-created patterns");
                    }
                } else {
                    throw new IllegalArgumentException("Function 'hint' only accepts String Literal values at argument 0, found " + args.get(0).getClass().getSimpleName());
                }
            } else {
                throw new IllegalArgumentException("Function 'hint' requires at least 1 parameter, found " + args.size());
            }
            return value;
        });
        registerFunction("recessive", TokenMatchValue.class, (value, args) -> {
            boolean recessive = true;
            if(args.size() >= 1) {
                if(args.get(0) instanceof BooleanValue) {
                    recessive = ((BooleanValue) args.get(0)).boolValue;

                } else {
                    throw new IllegalArgumentException("Function 'recessive' only accepts Boolean values at argument 0, found " + args.get(0).getClass().getSimpleName());
                }
            }
            if(((TokenMatchValue) value).patternMatch.hasTag(PLUGIN_CREATED_TAG)) {
                ((TokenMatchValue) value).patternMatch.setRecessive(recessive);
            } else {
                throw new IllegalArgumentException("Function 'recessive' can only be performed on plugin-created patterns");
            }
            return value;
        });
        registerFunction("optional", TokenMatchValue.class, (value, args) -> {
            boolean shouldBeOptional = true;
            if(args.size() >= 1) {
                if(args.get(0) instanceof BooleanValue) {
                    shouldBeOptional = ((BooleanValue) args.get(0)).boolValue;
                } else {
                    throw new IllegalArgumentException("Function 'optional' only accepts Boolean values at argument 0, found " + args.get(0).getClass().getSimpleName());
                }
            }
            if(((TokenMatchValue) value).patternMatch.hasTag(PLUGIN_CREATED_TAG)) {
                ((TokenMatchValue) value).patternMatch.setOptional(shouldBeOptional);
            } else {
                throw new IllegalArgumentException("Function 'optional' can only be performed on plugin-created patterns");
            }
            return value;
        });
        registerFunction("literal", (ignore, args) -> {
            if(args.size() >= 1) {
                if(args.get(0) instanceof StringLiteralValue) {
                    String text = ((StringLiteralValue) args.get(0)).stringValue;
                    return new TokenMatchValue(new TokenItemMatch(TokenType.UNKNOWN, text).setName("LITERAL_" + text.toUpperCase()).addTags(SuggestionTags.ENABLED, PLUGIN_CREATED_TAG));
                } else {
                    throw new IllegalArgumentException("Function 'literal' only accepts String Literal values at argument 0, found " + args.get(0).getClass().getSimpleName());
                }
            } else {
                throw new IllegalArgumentException("Function 'literal' requires at least 1 parameter, found " + args.size());
            }
        });
    }

    public void build(PrismarinePluginUnit unit) {
        variables.clear();

        TokenList statementList = ((TokenList) filePattern.find("STATEMENT_LIST"));

        if(statementList != null) {
            for(TokenPattern<?> statement : statementList.getContents()) {
                runStatement(statement);
            }
        }

        runStatement(filePattern.find("RETURN_STATEMENT"));
    }

    private void runStatement(TokenPattern<?> pattern) {
        while(true) {
            switch(pattern.getName()) {
                case "STATEMENT": {
                    pattern = ((TokenStructure) pattern).getContents();
                    continue;
                }
                case "DEFINE_STATEMENT": {
                    String definitionName = pattern.find("DEFINITION_NAME").flatten(false);
                    Value value = parseValue(pattern.find("VALUE"));

                    if(definitionName.startsWith("GLOBAL__")) {
                        if(!(value instanceof TokenMatchValue)) {
                            throw new PrismarineMetaException("Global variables can only hold token match values", pattern);
                        }
                        value = new TokenMatchValue(productions.getOrCreateStructure(definitionName).add(((TokenMatchValue) value).patternMatch));
                    }

                    variables.put(definitionName, value);
                    return;
                }
                case "RETURN_STATEMENT": {
                    Value value = parseValue(pattern.find("VALUE"));
                    if(value instanceof TokenMatchValue) {
                        returnValue = ((TokenMatchValue) value).patternMatch;
                    } else {
                        throw new PrismarineMetaException("Return value must be a token match", pattern);
                    }
                    return;
                }
            }
        }
    }

    private Value parseValue(TokenPattern<?> pattern) {
        while(true) {
            switch(pattern.getName()) {
                case "VALUE":
                case "ROOT_VALUE": {
                    pattern = ((TokenStructure) pattern).getContents();
                    continue;
                }
                case "ROOT_IDENTIFIER": {
                    String identifier = pattern.flatten(false);
                    Value defined = variables.get(identifier);
                    if(defined != null) return defined;
                    if(identifier.startsWith("GLOBAL__")) {
                        return new TokenMatchValue(productions.getOrCreateStructure(identifier));
                    }
                    TokenPatternMatch production = unitConfig.getStructureByName(identifier, productions);
                    if(production != null) return new TokenMatchValue(production);
                    throw new PrismarineMetaException("Identifier not found: '" + identifier + "'", pattern);
                }
                case "ROOT_STRING_LITERAL": {
                    String raw = pattern.flatten(false);
                    return new StringLiteralValue(CommandUtils.parseQuotedString(raw));
                }
                case "ROOT_BOOLEAN": {
                    return new BooleanValue(pattern.flatten(false).equals("true"));
                }
                case "ROOT_FUNCTION": {
                    String identifier = pattern.flatten(false);
                    Optional<FunctionDefinition> defined = FUNCTIONS.stream().filter(f -> f.functionName.equals(identifier) && f.memberOfType == null).findFirst();
                    if(defined.isPresent()) return defined.get().createForValue(null);
                    throw new PrismarineMetaException("Function not found: '" + identifier + "'", pattern);
                }
                case "MEMBER_ACCESS_VALUE": {
                    Value value = parseValue(pattern.find("ROOT_VALUE"));

                    TokenList memberAccessList = ((TokenList) pattern.find("MEMBER_ACCESS_LIST"));
                    if(memberAccessList != null) {
                        for(TokenPattern<?> memberAccess : memberAccessList.getContents()) {
                            value = resolveMemberAccess(value, memberAccess);
                        }
                    }

                    return value;
                }
            }
        }
    }

    private Value resolveMemberAccess(Value value, TokenPattern<?> pattern) {
        while(true) {
            switch(pattern.getName()) {
                case "MEMBER_ACCESS": {
                    pattern = ((TokenStructure) pattern).getContents();
                    continue;
                }
                case "FUNCTION_MEMBER": {
                    String functionName = pattern.find("FUNCTION_NAME").flatten(false);
                    Optional<FunctionDefinition> defined = FUNCTIONS.stream().filter(f -> f.functionName.equals(functionName) && f.memberOfType == value.getClass()).findFirst();
                    if(defined.isPresent()) return defined.get().createForValue(value);
                    throw new PrismarineMetaException("Function not found: '" + functionName + "'; Not defined as member of " + value.getClass() + " type", pattern);
                }
                case "FUNCTION_CALL": {
                    if(value instanceof FunctionValue) {
                        ArrayList<Value> args = new ArrayList<>();
                        TokenList argList = ((TokenList) pattern.find("ARGUMENT_LIST"));
                        if(argList != null) {
                            ArrayList<TokenPattern<?>> valueList = TokenPattern.PATTERN_LIST_POOL.get().claim();
                            try {
                                argList.collectByName("VALUE", valueList);
                                for(TokenPattern<?> rawArg : valueList) {
                                    args.add(parseValue(rawArg));
                                }
                            } finally {
                                TokenPattern.PATTERN_LIST_POOL.get().free(valueList);
                            }
                        }
                        return ((FunctionValue) value).evaluate(args);
                    } else {
                        throw new PrismarineMetaException("Not a function", pattern);
                    }
                }
            }
        }
    }

    public TokenPatternMatch getReturnValue() {
        return returnValue;
    }

    protected static abstract class Value {
    }

    protected static class TokenMatchValue extends Value {
        public TokenPatternMatch patternMatch;

        public TokenMatchValue(TokenPatternMatch patternMatch) {
            this.patternMatch = patternMatch;
        }
    }

    protected static class FunctionDefinition {
        public final String functionName;
        public final Class<? extends Value> memberOfType;
        public final BiFunction<Value, List<Value>, Value> handler;

        public FunctionDefinition(String functionName, Class<? extends Value> memberOfType, BiFunction<Value, List<Value>, Value> handler) {
            this.functionName = functionName;
            this.memberOfType = memberOfType;
            this.handler = handler;
        }

        public FunctionValue createForValue(Value value) {
            return new FunctionValue(value, this);
        }
    }

    protected static class FunctionValue extends Value {
        public final Value functionOwner;
        public final FunctionDefinition definition;

        public FunctionValue(Value functionOwner, FunctionDefinition definition) {
            this.functionOwner = functionOwner;
            this.definition = definition;
        }

        public Value evaluate(List<Value> args) {
            return definition.handler.apply(functionOwner, args);
        }
    }

    protected static class StringLiteralValue extends Value {
        public String stringValue;

        public StringLiteralValue(String stringValue) {
            this.stringValue = stringValue;
        }
    }

    protected static class BooleanValue extends Value {
        public boolean boolValue;

        public BooleanValue(boolean boolValue) {
            this.boolValue = boolValue;
        }
    }

    protected void registerFunction(String functionName, BiFunction<Value, List<Value>, Value> handler) {
        registerFunction(functionName, null, handler);
    }

    protected void registerFunction(String functionName, Class<? extends Value> memberOfType, BiFunction<Value, List<Value>, Value> handler) {
        FUNCTIONS.add(new FunctionDefinition(functionName, memberOfType, handler));
    }

    public static class PrismarineMetaException extends RuntimeException {
        private final String error;
        private final TokenPattern<?> cause;

        public PrismarineMetaException(String error, TokenPattern<?> cause) {
            this.error = error;
            this.cause = cause;
        }

        public String getErrorMessage() {
            return error;
        }

        public TokenPattern<?> getCausedBy() {
            return cause;
        }
    }
}
