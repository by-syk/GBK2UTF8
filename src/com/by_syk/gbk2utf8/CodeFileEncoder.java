package com.by_syk.gbk2utf8;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.ParsingDetector;
import info.monitorenter.cpdetector.io.UnicodeDetector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public class CodeFileEncoder {
    private File dirFile = null;
    private String rootDir = "";
    
    private boolean enable_log = true;
    
    public static final String SOURCE_ENCODING = "gbk";
    public static final String TARGET_ENCODING = "utf-8";
    
    private final String TAG = "CodeFileEncoder - ";
    private final String LOG_OUTPUT = TAG + "%1$s %2$s: \"%3$s\" -> \"%4$s\"";
    
    public CodeFileEncoder() {}
    
    public CodeFileEncoder(String dir) {
        setDir(dir);
    }
    
    public void setDir(String dir) {
        if (dir == null) {
            return;
        }
        
        dirFile = new File(dir);
    }
    
    public void enableLog(boolean enable_log) {
        this.enable_log = enable_log;
    }
    
    public boolean startConverting() {
        if (dirFile == null || !dirFile.exists()) {
            return false;
        }
        
        rootDir = dirFile.getParent();
        
        System.out.println(TAG + "ROOT_DIR: " + rootDir);
        
        ConvertThread convertThread = new ConvertThread();
        convertThread.start();
        
        return true;
    }
    
    private void ergodic(File sourceFile, File targetFile) {
        if (sourceFile == null || targetFile == null) {
            return;
        }
        
        if (sourceFile.isDirectory()) {
            targetFile.mkdir();
            for (File subFile : sourceFile.listFiles()) {
                ergodic(subFile, new File(targetFile, subFile.getName()));
            }
        } else if (sourceFile.isFile()) {
            convertOrCopy(sourceFile, targetFile);
            /*if (isCodeFile(sourceFile)) {
                convert(sourceFile, targetFile);
            } else {
                copy(sourceFile, targetFile);
            }*/
        }
    }
    
    private void convertOrCopy(File sourceFile, File targetFile) {
        boolean is_convert_not_copy = true;
        
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel fcIn = null;
        FileChannel fcOut = null;
        try {
            fis = new FileInputStream(sourceFile);
            fos = new FileOutputStream(targetFile);
            fcIn = fis.getChannel();
            fcOut = fos.getChannel();
        
            if (!isCodeFile(sourceFile)) {
                fcIn.transferTo(0, fcIn.size(), fcOut);
                is_convert_not_copy = false;
            } else {
                String source_encoding = getFileEncoding(sourceFile);
                if (TARGET_ENCODING.equals(source_encoding)) {
                    fcIn.transferTo(0, fcIn.size(), fcOut);
                    is_convert_not_copy = false;
                } else {
                    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
                    while (true) {
                        byteBuffer.clear();
                        if (fcIn.read(byteBuffer) == -1) {
                            break;
                        }
                        byteBuffer.flip();
                    
                        //fcOut.write(ByteBuffer.wrap(Charset.forName(SOURCE_ENCODING).decode(byteBuffer).toString()
                        //        .getBytes(TARGET_ENCODING)));
                        fcOut.write(ByteBuffer.wrap(Charset.forName(source_encoding).decode(byteBuffer).toString()
                                  .getBytes(TARGET_ENCODING)));
                    }
                }
            }
            
            if (enable_log) {
                System.out.println(String.format(LOG_OUTPUT, is_convert_not_copy ? "E" : "C", "OK",
                        sourceFile.getPath().replace(rootDir, "."),
                        targetFile.getPath().replace(rootDir, ".")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            
            if (enable_log) {
                System.out.println(String.format(LOG_OUTPUT, is_convert_not_copy ? "E" : "C", "FAILED",
                        sourceFile.getPath().replaceFirst(rootDir, "."),
                        targetFile.getPath().replaceFirst(rootDir, ".")));
            }
        } finally {
            if (fcIn != null) {
                try {
                    fcIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fcOut != null) {
                try {
                    fcOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void convert(File sourceFile, File targetFile) {
        String source_encoding = getFileEncoding(sourceFile);

        if (TARGET_ENCODING.equals(source_encoding)) {
            copy(sourceFile, targetFile);
            return;
        }
        
        FileInputStream fis = null;
        FileOutputStream fos = null;
        BufferedReader bufferedReader = null;
        try {
            fis = new FileInputStream(sourceFile);
            fos = new FileOutputStream(targetFile);
            //InputStreamReader isr = new InputStreamReader(fis, SOURCE_ENCODING);
            InputStreamReader isr = new InputStreamReader(fis, source_encoding);
            bufferedReader = new BufferedReader(isr);
            String temp;
            while ((temp = bufferedReader.readLine()) != null) {
                fos.write((temp + "\n").getBytes(TARGET_ENCODING));
            }
            
            if (enable_log) {
                System.out.println(String.format(LOG_OUTPUT, "E", "OK",
                        sourceFile.getPath().replaceFirst(rootDir, "."),
                        targetFile.getPath().replaceFirst(rootDir, ".")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            
            if (enable_log) {
                System.out.println(String.format(LOG_OUTPUT, "E", "OK",
                        sourceFile.getPath().replaceFirst(rootDir, "."),
                        targetFile.getPath().replaceFirst(rootDir, ".")));
            }
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void copy(File sourceFile, File targetFile) {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel fcIn = null;
        FileChannel fcOut = null;
        try {
            fis = new FileInputStream(sourceFile);
            fos = new FileOutputStream(targetFile);
            fcIn = fis.getChannel();
            fcOut = fos.getChannel();
        
            fcIn.transferTo(0, fcIn.size(), fcOut);
            
            if (enable_log) {
                System.out.println(String.format(LOG_OUTPUT, "C", "OK",
                        sourceFile.getPath().replaceFirst(rootDir, "."),
                        targetFile.getPath().replaceFirst(rootDir, ".")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            
            if (enable_log) {
                System.out.println(String.format(LOG_OUTPUT, "C", "OK",
                        sourceFile.getPath().replaceFirst(rootDir, "."),
                        targetFile.getPath().replaceFirst(rootDir, ".")));
            }
        } finally {
            if (fcIn != null) {
                try {
                    fcIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fcOut != null) {
                try {
                    fcOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private boolean isCodeFile(File sourceFile) {
        if (sourceFile == null) {
            return false;
        }
        
        String name = sourceFile.getName().toLowerCase();
        
        return name.endsWith(".java") || name.endsWith(".xml")
                || name.endsWith(".gradle") || name.endsWith(".txt");
    }
    
    private File getFile4Copy(File sourceFile) {
        if (sourceFile == null) {
            return null;
        }

        String fileName = sourceFile.getName();
        
        if (sourceFile.isDirectory()) {
            fileName += "_U";
        } else if (sourceFile.isFile()) {
            int index = fileName.lastIndexOf(".");
            if (index >= 0) {
                fileName = fileName.substring(0, index) + "_U" + fileName.substring(index);
            } else {
                fileName += "_U";
            }
        }
        
        return new File(sourceFile.getParent(), fileName);
    }
    
    /**
     * 参考：http://m.blog.csdn.net/article/details?id=8250592
     *
     * 利用第三方开源包cpdetector获取文件编码格式
     * 
     * @param path 要判断文件编码格式的源文件
     * @author huanglei
     * @version 2012-7-12 14:05
     */
    private String getFileEncoding(File sourceFile) {
        if (sourceFile == null) {
            return null;
        }
        
        /*
         * detector是探测器，它把探测任务交给具体的探测实现类的实例完成。
         * cpDetector内置了一些常用的探测实现类，这些探测实现类的实例可以通过add方法 加进来，如ParsingDetector、
         * JChardetFacade、ASCIIDetector、UnicodeDetector。
         * detector按照“谁最先返回非空的探测结果，就以该结果为准”的原则返回探测到的
         * 字符集编码。使用需要用到三个第三方JAR包：antlr.jar、chardet.jar和cpdetector.jar
         * cpDetector是基于统计学原理的，不保证完全正确。
         */
        CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();
        /*
         * ParsingDetector可用于检查HTML、XML等文件或字符流的编码,构造方法中的参数用于
         * 指示是否显示探测过程的详细信息，为false不显示。
         */
        detector.add(new ParsingDetector(false));
        /*
         * JChardetFacade封装了由Mozilla组织提供的JChardet，它可以完成大多数文件的编码
         * 测定。所以，一般有了这个探测器就可满足大多数项目的要求，如果你还不放心，可以
         * 再多加几个探测器，比如下面的ASCIIDetector、UnicodeDetector等。
         */
        detector.add(JChardetFacade.getInstance()); // 用到antlr.jar、chardet.jar
        // ASCIIDetector用于ASCII编码测定
        detector.add(ASCIIDetector.getInstance());
        // UnicodeDetector用于Unicode家族编码的测定
        detector.add(UnicodeDetector.getInstance());
        
        java.nio.charset.Charset charset = null;
        try {
            charset = detector.detectCodepage(sourceFile.toURI().toURL());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        if (charset != null) {
            return charset.name();
        }
        return null;
    }

    class ConvertThread extends Thread {
        @Override
        public void run() {
            System.out.println(TAG + "START");
            
            File file = getFile4Copy(dirFile);
            if (file.exists()) {
                System.out.println(String.format(TAG + "CANCEL: \"%1$s\" is existed.",
                        file.getPath().replaceFirst(rootDir, ".")));
            } else {
                ergodic(dirFile, file);
            }
            
            System.out.println(TAG + "END");
        }
    }
}
