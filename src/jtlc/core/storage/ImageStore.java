/*
 * Copyright (C) 2015 Baldani Sergio - Tardivo Cristian
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jtlc.core.storage;

import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.io.Opener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

/**
 * Image Storage Manipulation.
 * Allows to load/save serialize and deserialize image/image-plus objects.
 * 
 * @author Baldani Sergio - Tardivo Cristian
 */
public class ImageStore {
    
    /**
     * Open Any Image
     * @param path image path
     * @return opened image as ImagePlus object
     */
    public static ImagePlus openImage(String path) {
        return IJ.openImage(path);
    }
        
    /**
     * Load Image from image "file" InputStream, generate ImagePlus object
     * @param istream InputStream image to load
     * @param name image name
     * @return ImagePlus - image object
     * @throws IOException 
     */
    public static ImagePlus openImage(InputStream istream, String name) throws IOException {
        BufferedImage bimg = ImageIO.read(istream);
        ImagePlus image = new ImagePlus(name, bimg);
        istream.close();
        return image;
    }
    
    /**
     * Save Any Image as JPG
     * @param img ImagePlus image object to save
     * @param path save file path
     */
    public static void saveImage(ImagePlus img, String path) {
         FileSaver fs = new FileSaver(img);
         FileSaver.setJpegQuality(100);
         fs.saveAsJpeg(path);
    }
    
    /**
     * Save ImagePlus as JPEG to ByteArrayOutputStream
     * @param img Image to save
     * @param quality jpeg quality (0..100)
     * @return ByteArrayOutputStream
     * @throws IOException
     */
    public static ByteArrayOutputStream saveImage(ImagePlus img, int quality) throws IOException {
        // JpgWritter
        ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
        // Result Stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // ImageOutputStream
        ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream);
        // JpgParams
        ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
        jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpgWriteParam.setCompressionQuality(quality/100f);
        if (quality == 100)
            jpgWriteParam.setSourceSubsampling(1, 1, 0, 0);
        //
        IIOImage iioImage = new IIOImage(img.getBufferedImage(), null, null);
        // Write Image
        jpgWriter.setOutput(imageOutputStream);
        jpgWriter.write(null, iioImage, jpgWriteParam);
        // Close and dispose streams
        jpgWriter.dispose();
        imageOutputStream.close();
        outputStream.close();
        return outputStream;
    }
    
    /**
     * Serialize ImagePlus as byte array
     * @param img ImagePlus to serialize
     * @return byte[] with serialized ImagePlus
     */
    public static byte[] serializeImage(ImagePlus img) {
        FileSaver fs = new FileSaver(img);
        return fs.serialize();
    }
    
    /**
     * Deserialize byte array to ImagePlus object
     * @param array serialize imageplus byte array
     * @return ImagePlus deserialized object
     */
    public static ImagePlus deserializeImage(byte[] array) {
        Opener op = new Opener();
        return op.deserialize(array);
    }
}