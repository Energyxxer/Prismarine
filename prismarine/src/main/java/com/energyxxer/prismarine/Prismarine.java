package com.energyxxer.prismarine;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Prismarine {
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    public static final File NULL_FILE = new File(System.getProperty("user.home"));

    private Prismarine() {}
}
