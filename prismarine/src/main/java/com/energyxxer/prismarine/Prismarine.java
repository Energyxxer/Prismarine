package com.energyxxer.prismarine;

import java.io.File;
import java.nio.charset.Charset;

public class Prismarine {
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    public static final File NULL_FILE = new File(System.getProperty("user.home"));

    private Prismarine() {}
}
