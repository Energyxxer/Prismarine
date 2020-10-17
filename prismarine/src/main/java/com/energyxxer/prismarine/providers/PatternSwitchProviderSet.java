package com.energyxxer.prismarine.providers;

import com.energyxxer.enxlex.lexical_analysis.token.TokenType;
import com.energyxxer.enxlex.pattern_matching.matching.TokenPatternMatch;
import com.energyxxer.enxlex.pattern_matching.matching.lazy.TokenStructureMatch;
import com.energyxxer.enxlex.pattern_matching.matching.lazy.TokenSwitchMatch;
import com.energyxxer.prismarine.PrismarineProductions;

import static com.energyxxer.prismarine.PrismarineProductions.tokenSwitch;


public class PatternSwitchProviderSet extends PatternProviderSet {

    private final String internalName;
    private final TokenType switchTokenType;

    public PatternSwitchProviderSet(String internalName, TokenType switchTokenType) {
        super(null);
        this.internalName = internalName;
        this.switchTokenType = switchTokenType;
    }

    public PatternSwitchProviderSet(String internalName, String humanReadableName, TokenType switchTokenType) {
        super(null, humanReadableName);
        this.internalName = internalName;
        this.switchTokenType = switchTokenType;
    }

    private TokenSwitchMatch switchMatch;

    @Override
    protected void installUtilityProductions(PrismarineProductions productions, TokenStructureMatch providerStructure) {
        switchMatch = tokenSwitch(internalName, switchTokenType);

        productions.getOrCreateStructure(internalName).add(
                switchMatch
        );
    }

    @Override
    protected void installOrphanUnit(PatternProviderUnit unit, TokenPatternMatch createdMatch, PrismarineProductions productions) {
        for(String key : ((PatternSwitchProviderUnit) unit).getSwitchKeys()) {
            switchMatch.add(key, createdMatch);
        }
    }
}
