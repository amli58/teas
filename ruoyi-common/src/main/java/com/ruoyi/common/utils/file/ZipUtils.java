package com.ruoyi.common.utils.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *  ZIP压缩、解压缩工具类
 * 2020/1/3 15:14 * @Author King
 *
 **/
public class ZipUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ZipUtils.class);

    /**
     * 文件后缀
     */
    private static final String ZIP = ".zip";

    /**
     * 缓冲流大小
     */
    private static final int BUFFER = 1024;

    /**
     * 测试
     */
    public static void main(String[] args) throws IOException {
        String srcPath = "E:\\file\\";
        String zipFilePath = "E:\\file\\压缩.zip";
        zipFile(srcPath, zipFilePath);
        String targetPath = "E:\\file\\解压";
        unzipFile(zipFilePath, targetPath);
    }

    /**
     * 压缩文件
     * @param srcPath     需要压缩的文件路径
     * @param zipFilePath 压缩后路径(全路径,包含文件名)
     */
    public static void zipFile(String srcPath, String zipFilePath) throws IOException {
        LOG.info("【文件压缩】---文件路径:{},压缩后路径:{},开始压缩...", srcPath, zipFilePath);

        File srcFile = new File(srcPath);
        // 校验文件是否存在
        if (!srcFile.exists()) {
            throw new FileNotFoundException("文件不存在");
        }

        // 校验压缩路径是否符合格式
        if (!zipFilePath.endsWith(ZIP)) {
            throw new InvalidParameterException("压缩路径不正确");
        }

        // 如果压缩文件已存在,先删除
        File zipFile = new File(zipFilePath);
        if (zipFile.exists()) {
            Files.delete(zipFile.toPath());
        }

        // 获取文件中的所有文件
        List<File> files = getFiles(srcFile);

        // 缓冲流
        byte[] buffer = new byte[BUFFER];
        // 读出长度
        int length;

        //  输出流过滤器。用于以ZIP文件格式写入文件
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFilePath))) {

            for (File file : files) {
                // 获取源文件的父目录
                String srcFileParent = srcFile.getParent();
                String parent = srcFileParent.endsWith(File.separator) ?
                        srcFileParent.replace(String.valueOf(File.separatorChar), "") : srcFileParent;
                // 压缩目录的根目录与当前文件夹保持一致
                String name = file.getPath().replace(parent + File.separator, "");

                // ZIP文件项
                ZipEntry zipEntry = new ZipEntry(file.isFile() ? name : name + File.separator);
                // 未压缩的字节大小
                zipEntry.setSize(file.length());
                // 最后修改时间
                zipEntry.setTime(file.lastModified());
                // 压缩方法  默认 ZipEntry.DEFLATED -1
                zipEntry.setMethod(ZipEntry.DEFLATED);
                // 写入ZIP条目
                zipOutputStream.putNextEntry(zipEntry);
                if (file.isFile()) {
                    InputStream inputStream = new BufferedInputStream(new FileInputStream(file));

                    while ((length = inputStream.read(buffer, 0, BUFFER)) != -1) {
                        zipOutputStream.write(buffer, 0, length);
                    }
                    inputStream.close();
                }
            }

            LOG.info("【文件压缩】---文件路径:{},压缩后路径:{},压缩成功！！！", srcPath, zipFilePath);
        } catch (IOException e) {
            LOG.error("【文件压缩】---压缩文件:{},异常:", srcPath, e);
            throw new IOException("压缩失败,原因:" + e.getMessage());
        }

    }

    /**
     * 获取文件中的所有文件
     */
    public static List<File> getFiles(File srcFile) {
        List<File> list = new ArrayList<>(8);

        // 判断文件是否是普通文件, 如果是普通文件则直接放入集合
        if (srcFile.isFile()) {
            list.add(srcFile);
            return list;
        }

        // 判断文件是否是文件夹
        if (srcFile.isDirectory()) {
            File[] files = srcFile.listFiles();
            // 如果是空文件夹,则直接放入集合
            if (null == files || files.length == 0) {
                list.add(srcFile);
                return list;
            }
            // 递归获取所有文件
            for (File file : files) {
                list.addAll(getFiles(file));
            }
        }

        return list;
    }

    /**
     * 解压文件到指定目录
     * @param zipFilePath 压缩文件路径(全路径,包含文件名)
     * @param targetPath  解压后路径
     */
    public static void unzipFile(String zipFilePath, String targetPath) throws IOException {
        LOG.info("【文件解压】---压缩文件路径:{},解压后路径:{},开始解压...", zipFilePath, targetPath);

        // 校验文件是否存在
        File zipFile = new File(zipFilePath);
        if (!zipFile.exists()) {
            throw new FileNotFoundException("压缩文件不存在");
        }

        // 校验压缩路径是否符合格式
        if (!zipFilePath.endsWith(ZIP)) {
            throw new InvalidParameterException("压缩路径不正确");
        }

        //  输入流过滤器.用于读取ZIP文件格式的文件
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath))) {

            // ZIP文件项
            ZipEntry zipEntry;
            byte[] buffer = new byte[BUFFER];
            int length;

            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                // 如果文件不存在,则需创建文件
                File file = new File(targetPath + File.separator + zipEntry.getName());
                if (zipEntry.getName().endsWith(File.separator) && !file.exists()) {
                    file.mkdirs();
                    continue;
                }

                //  如果父包不存在则创建
                final File parent = file.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }

                // 如果不是文件夹则写入，如果文件存在则删除覆盖
                if (!file.isDirectory()) {
                    OutputStream outputStream = new FileOutputStream(file);

                    while ((length = zipInputStream.read(buffer, 0, BUFFER)) != -1) {
                        outputStream.write(buffer, 0, length);
                    }

                    outputStream.close();
                }
            }

            LOG.info("【文件解压】---压缩文件路径:{},解压后路径:{},解压成功！！！", zipFilePath, targetPath);
        } catch (IOException e) {
            LOG.error("【文件解压】---压缩文件:{},异常:", zipFilePath, e);
            throw new IOException("解压失败,原因:" + e.getMessage());
        }
    }

}




