package com.energyxxer.enxlex.lexical_analysis.token;

import java.io.File;

public interface TokenSource {
    String getFileName();
    String getFullPath();
    String getPrettyName();

    File getRelatedFile();
    File getExactFile();
}
