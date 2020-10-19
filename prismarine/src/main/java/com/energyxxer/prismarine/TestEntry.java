package com.energyxxer.prismarine;

import java.io.File;
import java.util.Arrays;

public class TestEntry {
    public static void main(String[] args) {
        File dir = new File("C:\\Users\\PC\\Trident\\workspace\\Block API\\datapack\\data\\blockapi\\functions");

        System.out.println(Arrays.toString(dir.listFiles())); //old method
        System.out.println(Arrays.toString(dir.list())); //new method
        //equivalent
    }
}
