package com.by_syk.gbk2utf8;

import java.io.File;

class Util {
    public static void deleteFile(File file) {
        if (file == null) {
            return;
        }
        
        if (file.isDirectory()) {
            for (File subFile : file.listFiles()) {
                deleteFile(subFile);
            }
        }
        
        file.delete();
    }

    public static boolean isCodeFile(File sourceFile) {
        if (sourceFile == null) {
            return false;
        }
        
        String name = sourceFile.getName().toLowerCase();
        
        return name.endsWith(".java") || name.endsWith(".xml")
                || name.endsWith(".gradle") || name.endsWith(".txt");
    }
}
