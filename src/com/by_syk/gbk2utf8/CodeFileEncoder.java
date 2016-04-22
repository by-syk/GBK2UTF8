package com.by_syk.gbk2utf8;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.ParsingDetector;
import info.monitorenter.cpdetector.io.UnicodeDetector;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.List;

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.exception.RarException;

public class CodeFileEncoder {
    private File sourceFile = null;
    
    private boolean enable_log = true;
    
    public static final String DEF_SOURCE_ENCODING = "gbk";
    public static final String DEF_TARGET_ENCODING = "utf-8";
    
    public static final String TAG = "CodeFileEncoder - ";
    public static final String LOG_CONVERTED = TAG + "CONVERTED: \"%1$s\" -> \"%2$s\"";
    public static final String LOG_COPIED = TAG + "COPIED: \"%1$s\" -> \"%2$s\"";
    public static final String LOG_SKIPPED = TAG + "SKIPPED: %1$s";
    public static final String LOG_CANCELLED = TAG + "CANCELLED: %1$s";
    
    public CodeFileEncoder() {}
    
    public CodeFileEncoder(String path) {
        setSourcePath(path);
    }
    
    public void setSourcePath(String path) {
        if (path == null) {
            return;
        }
        
        if (path.startsWith("\"") && path.endsWith("\"")) {
            path = path.substring(1, path.length() - 1);
        }
        
        sourceFile = new File(path);
    }
    
    public void enableLog(boolean enable_log) {
        this.enable_log = enable_log;
    }
    
    public boolean startConverting() {
        System.out.println(CodeFileEncoder.TAG + (sourceFile == null
                ? "START" : String.format("START: \"%1$s\"", sourceFile.getPath())));
        
        if (sourceFile == null || !sourceFile.exists()) {
            System.out.println(String.format(LOG_CANCELLED, "Invalid file."));
            return false;
        }
        
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
            
            if (!Util.isCodeFile(sourceFile)) {
                fcIn.transferTo(0, fcIn.size(), fcOut);
                is_convert_not_copy = false;
            } else {
                String source_encoding = getFileEncoding(sourceFile);
                if (DEF_TARGET_ENCODING.equalsIgnoreCase(source_encoding)) {
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
                        
                        //fcOut.write(ByteBuffer.wrap(Charset.forName(source_encoding).decode(byteBuffer).toString()
                        //          .getBytes(DEF_TARGET_ENCODING)));
                        fcOut.write(ByteBuffer.wrap(Charset.forName(DEF_SOURCE_ENCODING).decode(byteBuffer).toString()
                                .getBytes(DEF_TARGET_ENCODING)));
                    }
                }
            }
            
            if (enable_log) {
                System.out.println(String.format(is_convert_not_copy ? LOG_CONVERTED : LOG_COPIED,
                        sourceFile.getPath(), targetFile.getPath()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            
            if (enable_log) {
                System.out.println(String.format(LOG_SKIPPED,
                        sourceFile.getPath(), targetFile.getPath()));
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
    
    /*private void convert(File sourceFile, File targetFile) {
        String source_encoding = getFileEncoding(sourceFile);

        if (DEF_TARGET_ENCODING.equalsIgnoreCase(source_encoding)) {
            copy(sourceFile, targetFile);
            return;
        }
        
        FileInputStream fis = null;
        FileOutputStream fos = null;
        BufferedReader bufferedReader = null;
        try {
            fis = new FileInputStream(sourceFile);
            fos = new FileOutputStream(targetFile);
            //InputStreamReader isr = new InputStreamReader(fis, source_encoding);
            InputStreamReader isr = new InputStreamReader(fis, DEF_SOURCE_ENCODING);
            bufferedReader = new BufferedReader(isr);
            String temp;
            while ((temp = bufferedReader.readLine()) != null) {
                fos.write((temp + "\n").getBytes(DEF_TARGET_ENCODING));
            }
            
            if (enable_log) {
                System.out.println(String.format(LOG_CONVERTED,
                        sourceFile.getPath(), targetFile.getPath()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            
            if (enable_log) {
                System.out.println(String.format(LOG_SKIPPED,
                        sourceFile.getPath(), targetFile.getPath()));
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
                System.out.println(String.format(LOG_COPIED,
                        sourceFile.getPath(), targetFile.getPath()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            
            if (enable_log) {
                System.out.println(String.format(LOG_SKIPPED,
                        sourceFile.getPath(), targetFile.getPath()));
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
    }*/
    
    private File getFile4Copy(File sourceFile, File targetDir) {
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
        
        if (targetDir != null && targetDir.isDirectory()) {
            return new File(targetDir, fileName);
        }
        return new File(sourceFile.getParent(), fileName);
    }
    
    private File getFile4Copy(File sourceFile) {
        return getFile4Copy(sourceFile, null);
    }
    
    /**
     * 参考：http://m.blog.csdn.net/article/details?id=8250592
     * 由于基于统计，未见内容很少时不太准
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
    
    private File unzip(File sourceFile, File targetDir) {
        if (sourceFile == null) {
            return null;
        }
        
        if (targetDir == null || !targetDir.isDirectory()) {
            targetDir = new File(new File(System.getProperty("java.io.tmpdir")),
                    sourceFile.getName().replace(".zip", ""));
            
            if (targetDir.exists()) {
                Util.deleteFile(targetDir);
            }
        }
        
        try {
            ZipFile zipFile = new ZipFile(sourceFile);
            
            if (!zipFile.isValidZipFile()) {
                return null;
            }
            
            if (zipFile.isEncrypted()) {
                return null;
            }
            
            boolean is_files_name_utf8_encoded = true;
            List list = zipFile.getFileHeaders();
            FileHeader fileHeader;
            for (int i = 0, len = list.size(); i < len; ++i) {
                fileHeader = (FileHeader) list.get(i);
                is_files_name_utf8_encoded &= fileHeader.isFileNameUTF8Encoded();
            }
            
            if (!is_files_name_utf8_encoded) {
                zipFile = new ZipFile(sourceFile);
                zipFile.setFileNameCharset(DEF_SOURCE_ENCODING);
            }
            
            zipFile.extractAll(targetDir.getPath());
            
            return targetDir;
        } catch (ZipException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    private File unrar(File sourceFile, File targetDir) {
        if (sourceFile == null) {
            return null;
        }
        
        if (targetDir == null || !targetDir.isDirectory()) {
            targetDir = new File(new File(System.getProperty("java.io.tmpdir")),
                    sourceFile.getName().replace(".rar", ""));
            
            if (targetDir.exists()) {
                Util.deleteFile(targetDir);
            }
        }
        
        Archive archive = null;
        // net.lingala.zip4j.model.FileHeader
        de.innosystec.unrar.rarfile.FileHeader fileHeader = null;
        
        File tempFile;
        FileOutputStream fos = null;
        
        try {
            archive = new Archive(sourceFile);
            
            if (archive.isEncrypted()) {
                return null;
            }
            
            while ((fileHeader = archive.nextFileHeader()) != null) {
                if (fileHeader.isDirectory()) {
                    continue;
                }
                
                tempFile = new File(targetDir, fileHeader.getFileNameString().trim());
                tempFile.getParentFile().mkdirs();
                
                fos = new FileOutputStream(tempFile);
                archive.extractFile(fileHeader, fos);
                fos.close();
                fos = null;
            }
            
            return targetDir;
        } catch (RarException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            if (archive != null) {
                try {
                    archive.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return null;
    }

    class ConvertThread extends Thread {
        @Override
        public void run() {
            File oldFile = sourceFile;
            File newFile = getFile4Copy(oldFile);
            
            if (sourceFile.isFile()) {
                if (sourceFile.getName().endsWith(".zip")) {
                    System.out.println(TAG + "UNZIPPING...");
                    oldFile = unzip(sourceFile, null);
                    if (oldFile == null) {
                        System.out.println(String.format(LOG_CANCELLED, "failed to unzip."));
                        return;
                    }
                    System.out.println(TAG + String.format("UNZIPPED: \"%1$s\"", oldFile.getPath()));
                    
                    newFile = getFile4Copy(oldFile, sourceFile.getParentFile());
                } else if (sourceFile.getName().endsWith(".rar")) {
                    System.out.println(TAG + "UNRARING...");
                    oldFile = unrar(sourceFile, null);
                    if (oldFile == null) {
                        System.out.println(String.format(LOG_CANCELLED, "failed to unrar."));
                        return;
                    }
                    System.out.println(TAG + String.format("UNRARED: \"%1$s\"", oldFile.getPath()));

                    newFile = getFile4Copy(oldFile, sourceFile.getParentFile());
                }
            }
            
            if (newFile.exists()) {
                System.out.println(String.format(LOG_CANCELLED + "\"%1$s\" is existed.",
                        newFile.getPath()));
                return;
            }
            
            ergodic(oldFile, newFile);
            
            System.out.println(CodeFileEncoder.TAG + String.format("DONE: \"%1$s\"", newFile.getPath()));
        }
    }
}
