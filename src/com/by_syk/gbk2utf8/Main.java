package com.by_syk.gbk2utf8;

import java.util.Scanner;

/**
 * @author By_syk
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== GBK2UTF8 for Java/Android Projects ===");
        System.out.println("===          v1.0.1            @By_syk ===");
        
        String dir = "";
        
        if (args != null && args.length > 0) {
            dir = args[0];
        } else {
            System.out.println("Enter the directory or file (\"exit\" to exit):");
        
            Scanner scanner = new Scanner(System.in);
            dir = scanner.nextLine();
            scanner.close();
        }
        
        if (dir.equalsIgnoreCase("exit")) {
            return;
        }
        
        if (dir.startsWith("./")) {
            dir = "/storage/emulated/0" + dir.substring(1);
        }
        
        CodeFileEncoder codeFileEncoder = new CodeFileEncoder(dir);
        //codeFileEncoder.enableLog(true);
        codeFileEncoder.startConverting();
    }
}
