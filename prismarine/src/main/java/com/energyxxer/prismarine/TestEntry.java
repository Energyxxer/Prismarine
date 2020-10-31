package com.energyxxer.prismarine;

import com.energyxxer.prismarine.util.FileUtil;

import java.io.File;
import java.io.InputStream;

public class TestEntry {
    public static void main(String[] args) throws NoSuchMethodException {
        File dir = new File("C:\\Users\\PC\\Trident\\workspace\\Block API\\datapack\\data\\blockapi\\functions");

        System.out.println(FileUtil.class.getMethod("read", InputStream.class, int.class).getParameters()[0].getName());
    }
}
