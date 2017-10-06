package com.christoff.apps.sumolambda.utils;

import org.jetbrains.annotations.NotNull;

import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Contains Image manipulation methods
 * For the moment : optimize without resize
 */
public class PictureOptimizer {

    /**
     * Applied on this format
     */
    private static final String IMAGE_FORMAT = "jpg";

    private PictureOptimizer() {
    }

    /**
     * This is the method to decrease image quality
     *
     * @param picture the JPEG as byte array
     * @param quality between 0 (very very poor) and 1 (best quality)
     * @return downgraded jpeg picture
     */
    public static byte[] reducePicture(byte[] picture, Float quality) throws IOException {
        // First step "read" the JPG in memory
        try (ByteArrayInputStream bais = new ByteArrayInputStream(picture)) {
            ImageReader imageReader = getImageReader(bais);
            // Second Get writers
            Iterator<ImageWriter> writerIterator = ImageIO.getImageWritersByFormatName(IMAGE_FORMAT);
            ImageWriter imageWriter = writerIterator.next();
            ImageWriteParam imageWriteParam = getImageWriteParam(imageWriter, quality);
            // Prepare output
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageOutputStream imageOutputStream = new MemoryCacheImageOutputStream(baos);
            imageWriter.setOutput(imageOutputStream);
            IIOImage iioimage = new IIOImage(imageReader.read(0), null, null);
            // Write and flush
            imageWriter.write(null, iioimage, imageWriteParam);
            imageOutputStream.flush();
            return baos.toByteArray();
        }

    }

    /**
     * @param imageWriter the source writer
     * @return the paramters to apply
     */
    @NotNull
    private static ImageWriteParam getImageWriteParam(ImageWriter imageWriter, Float quality) {
        ImageWriteParam imageWriteParam = imageWriter.getDefaultWriteParam();
        imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        imageWriteParam.setCompressionQuality(quality);
        return imageWriteParam;
    }

    /**
     * Return an ImageReader for JPG
     *
     * @param bais the inputstream of the jpg to read
     * @return an JPEG Image reader
     */
    @NotNull
    private static ImageReader getImageReader(ByteArrayInputStream bais) {
        MemoryCacheImageInputStream cache = new MemoryCacheImageInputStream(bais);
        Iterator<ImageReader> readerIterator = ImageIO.getImageReadersByFormatName(IMAGE_FORMAT);
        ImageReader imageReader = readerIterator.next();
        imageReader.setInput(cache);
        return imageReader;
    }
}
