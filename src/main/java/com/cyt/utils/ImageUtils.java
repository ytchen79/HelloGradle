package com.cyt.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 图片工具
 *
 * @author cyt
 */
public class ImageUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(ImageUtils.class);

    public static byte[] compressImageByteLength(byte[] sourceImage, int targetLength) {
        if (sourceImage.length <= targetLength) {
            return sourceImage;
        }
        byte[] targetByte = sourceImage;
        int times = 0;
        while (targetByte.length > targetLength) {
            targetByte = compressImageByRate(targetByte, (double) targetLength / targetByte.length);
            LOGGER.info(StringUtils.formatString("第%s次压缩的大小为：%sKB", ++times, (double) targetByte.length / 1024));
        }

        return targetByte;
    }

    /**
     * 按比例压缩图片
     */
    public static byte[] compressImageByRate(byte[] sourceImage, double compressRate) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(sourceImage)) {
            BufferedImage bufferedImage = ImageIO.read(bais);
            int[] size = getImageSize(bufferedImage);
            compressRate = Math.sqrt(compressRate);
            return compressImageBySize(bufferedImage, (int) (size[0] * compressRate), (int) (size[1] * compressRate));
        } catch (IOException e) {
            LOGGER.error("ERROR:", e);
        }
        return null;
    }

    /**
     * 获取图片尺寸
     *
     * @param bufferedImage 图片buffer
     * @return 整形数组；数组下标0为宽度，数组下表1位长度
     */
    private static int[] getImageSize(BufferedImage bufferedImage) {
        int[] size = new int[2];
        size[0] = bufferedImage.getWidth();
        size[1] = bufferedImage.getHeight();
        return size;
    }

    private static byte[] compressImageBySize(BufferedImage sourceImage, int width, int height) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Image image = sourceImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage targetImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics graphics = targetImage.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.drawImage(image, 0, 0, null);
            graphics.dispose();
            ImageIO.write(targetImage, "JPEG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] compressImageByteLength(byte[] sourceImage, int targetLength, int dpi) {
        if (targetLength > sourceImage.length) {
            return sourceImage;
        }
        float quality = 0.85f;
        byte[] targetImage = sourceImage;
        int i = 0;
        while (targetImage.length > targetLength) {
            System.out.println(StringUtils.formatString("第%s次压缩大少为%sKB", i++, (double) targetImage.length / 1024));
            try (ByteArrayInputStream bais = new ByteArrayInputStream(sourceImage);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ImageOutputStream ios = ImageIO.createImageOutputStream(baos);) {
                BufferedImage bufferedImage = ImageIO.read(bais);
                ImageWriter imageWriter = ImageIO.getImageWritersBySuffix("JPEG").next();
                ImageWriteParam writeParams = imageWriter.getDefaultWriteParam();
                writeParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                writeParams.setCompressionQuality(quality);
                quality = quality * 0.5f;
                IIOMetadata data = imageWriter.getDefaultImageMetadata(new ImageTypeSpecifier(bufferedImage), writeParams);
                if (dpi != 0) {
                    Element tree = (Element) data.getAsTree("javax_imageio_jpeg_image_1.0");
                    Element jfif = (Element) tree.getElementsByTagName("app0JFIF").item(0);
                    jfif.setAttribute("Xdensity", Integer.toString(dpi));
                    jfif.setAttribute("Ydensity", Integer.toString(dpi));
                    jfif.setAttribute("resUnits", "1");
                    data.setFromTree("javax_imageio_jpeg_image_1.0", tree);
                }
                imageWriter.setOutput(ios);
                imageWriter.write(null, new IIOImage(bufferedImage, null, data), writeParams);
                targetImage = baos.toByteArray();
            } catch (IOException e) {
                LOGGER.error("ERROR:", e);
            }
        }

        return targetImage;
    }

    public static byte[] compressImageBySize(byte[] sourceImage, int width, int height, int dpi) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(sourceImage);
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ImageOutputStream ios = ImageIO.createImageOutputStream(baos);) {
            BufferedImage bufferedImage = ImageIO.read(bais);
            ImageWriter imageWriter = ImageIO.getImageWritersBySuffix("JPEG").next();
            ImageWriteParam writeParams = imageWriter.getDefaultWriteParam();
            writeParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            writeParams.setCompressionQuality(0.1f);
            IIOMetadata data = imageWriter.getDefaultImageMetadata(new ImageTypeSpecifier(bufferedImage), writeParams);
            if (dpi != 0) {
                Element tree = (Element) data.getAsTree("javax_imageio_jpeg_image_1.0");
                Element jfif = (Element) tree.getElementsByTagName("app0JFIF").item(0);
                jfif.setAttribute("Xdensity", Integer.toString(dpi));
                jfif.setAttribute("Ydensity", Integer.toString(dpi));
                jfif.setAttribute("resUnits", "1");
                data.setFromTree("javax_imageio_jpeg_image_1.0", tree);
            }

            imageWriter.setOutput(ios);
            imageWriter.write(null, new IIOImage(bufferedImage, null, data), writeParams);
            return baos.toByteArray();
        } catch (IOException e) {
            LOGGER.error("ERROR:", e);
        }
        return null;
    }


    /**
     * 按比例压缩图片
     */
    public static byte[] compressImageByRate(byte[] sourceImage, float compressRate) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(sourceImage)) {
            BufferedImage bufferedImage = ImageIO.read(bais);
            int[] size = getImageSize(bufferedImage);
            return compressImageBySize(bufferedImage, (int) (size[0] * compressRate), (int) (size[1] * compressRate));
        } catch (IOException e) {
            LOGGER.error("ERROR:", e);
        }
        return null;
    }

}
