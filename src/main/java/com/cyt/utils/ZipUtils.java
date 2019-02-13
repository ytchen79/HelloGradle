package com.cyt.utils;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ZipUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZipUtils.class);

    private static final int BUFFER_SIZE = 1024;

    public static boolean zip(String fileName, byte[] fileBytes, ZipOutputStream zos)  {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes)) {
            return zip(fileName, bais, zos);
        } catch (Exception e) {
            LOGGER.error("ERROR: ", e);
            return false;
        }
    }

    public static boolean zip(String fileName, InputStream is, ZipOutputStream zos) {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int len ;
            zos.putNextEntry(new ZipEntry(fileName));
            while ((len = is.read(buffer)) != -1) {
                zos.write(buffer, 0, len);
            }
            zos.flush();
            return true;
        } catch (Exception e) {
            LOGGER.error("压缩文件异常, ", e);
            return false;
        }
    }

}
