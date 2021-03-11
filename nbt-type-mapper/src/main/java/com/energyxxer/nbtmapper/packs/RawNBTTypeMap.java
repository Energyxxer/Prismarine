package com.energyxxer.nbtmapper.packs;

import com.energyxxer.enxlex.lexical_analysis.token.TokenSource;

public class RawNBTTypeMap {
    private final TokenSource source;
    private final String contents;

    public RawNBTTypeMap(TokenSource source, String contents) {
        this.source = source;
        this.contents = contents;
    }

    public TokenSource getSource() {
        return source;
    }

    public String getContents() {
        return contents;
    }
}
