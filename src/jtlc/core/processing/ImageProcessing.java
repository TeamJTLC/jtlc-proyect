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
package jtlc.core.processing;

import jtlc.main.common.Point;
import jtlc.core.processing.AnalysisProcessing.Axis;
import ij.ImagePlus;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import java.awt.Color;
import jtlc.main.common.Pair;

/**
 * This class allows basic image processing, 
 * implements a bridge to ImageJ Library.
 * 
 * @author Baldani Sergio - Tardivo Cristian
 */
public class ImageProcessing {
    
    /**
     * Conver ImagePlus image to GrayScale image
     * Overwrites the original (be careful)
     * @param img ImagePlus object
     */
    public static void toGrayScale(ImagePlus img) {
        ImageConverter ic = new ImageConverter(img);
        ic.convertToGray8();
    }
    
    /**
     * Get ImagePlus image data as Integer valued array
     * @param img ImagePlus to process
     * @return integer bi-array with pixels values
     */
    public static int[][] getImageArray(ImagePlus img) {
        return img.getProcessor().getIntArray();
    }
    
    /**
     * Invert ImagePlus image colors
     * Overwrites the original (be careful)
     * @param img ImagePlus object
     */
    public static void invertImage(ImagePlus img) {
        img.getProcessor().invert();
    }
    
    /**
     * Apply Auto-Threshold algorithm to ImagePlus image
     * Overwrites the original (be careful)
     * @param img ImagePlus object
     */
    public static void imageThreshold(ImagePlus img) {
        img.getProcessor().autoThreshold();
    }
    
    /**
     * Apply Find Edges algorithm to ImagePlus image
     * Overwrites the original (be careful)
     * @param img ImagePlus object
     */
    public static void findEdges(ImagePlus img) {
        img.getProcessor().findEdges();
    }
    
    /**
     * Apply Binary Erode process to ImagePlus image
     * Overwrites the original (be careful)
     * Requires 8-bit or RGB image
     * @param img ImagePlus object
     */
    public static void erodeImage(ImagePlus img) {
        if (img.getBitDepth() != 8)
            throw new IllegalArgumentException("Invalid Image 8-bit image");
        else
            img.getProcessor().erode();
    }
    
    /**
     * Apply Gaussian Blur filter to ImagePlus image
     * Overwrites the original (be careful)
     * @param img ImagePlus object
     * @param sigma blur sigma-level
     */
    public static void gaussianBlur(ImagePlus img, float sigma) {
        img.getProcessor().blurGaussian(sigma);
    }
    
    /**
     * Cut ImagePlus image, to the ROI delimited by two points inside the image
     * Return new cutted ImagePlus
     * @param img ImagePlus object
     * @param points upper and lower cut points
     * @return processed ImagePlus object
     */
    public static ImagePlus cutImage(ImagePlus img, Pair<Point,Point> points) {
        return cutImage(img, points.getFirst(), points.getSecond());
    }
    
    /**
     * Cut ImagePlus image, to the ROI delimited by two points inside the image
     * Return new cutted ImagePlus
     * @param img ImagePlus object
     * @param upper upper cut point (pair of int inside the image)
     * @param lower lower cut point (pair of int inside the image)
     * @return processed ImagePlus object
     */
    public static ImagePlus cutImage(ImagePlus img, Point upper, Point lower) {
        // Compute Values
        int x = upper.getX();
        int y = upper.getY();
        int width = lower.getX() - x;
        int height = lower.getY() - y;
        // Cut Image
        img.setRoi(x, y, width, height);
        ImagePlus result = new ImagePlus(img.getTitle(),img.getProcessor().crop());
        img.deleteRoi();
        return result;
    }
    
    /**
     * Resize ImagePlus image, keeping or not his aspect ratio
     * Overwrites the original (be careful)
     * @param img ImagePlus object
     * @param width new image width (-1 only process image height)
     * @param height new image height (-1 only proces image width)
     */
    public static void resizeImage(ImagePlus img, int width, int height) {
        if (width > 0 && height > 0)
            img.setProcessor(img.getProcessor().resize(width, height));
        if (width <= 0 && height > 0)
            img.setProcessor(img.getProcessor().resize((int)(height * ((double)img.getWidth() / img.getHeight())), height));
        if (height <= 0 && width > 0)
            img.setProcessor(img.getProcessor().resize(width, (int)(width * ((double) img.getHeight() / img.getWidth()))));
    }
    
    /**
     * Rotate (in degrees) and resize ImagePlus image
     * Return new rotated and resized ImagePlus
     * @param img ImagePlus object
     * @param angle degrees to rotate
     * @param resize resize or not image while rotating
     * @return processed ImagePlus object
     */
    public static ImagePlus rotateImage(ImagePlus img, double angle, boolean resize) {
        ImagePlus result = img.duplicate();
        ImageProcessor ip = result.getProcessor();
        // Convert always to positive angle
        float fangle = (float) (angle + Math.ceil(-angle / 360.0) * 360.0);
        // Exact Rotations
        int spin = (int)(fangle / 90.0f);
        // Right rotation
        if (spin > 0) {
            ip.setInterpolationMethod(ImageProcessor.BILINEAR);
            for (int i = 0; i < spin; i++) {
                result.setProcessor(ip.rotateRight());
                ip = result.getProcessor();
            }
        }
        // Calculate Rotation Excess
        float excess = (fangle - (90.0f * (float)spin));
        // Excess Rotations
        if (excess != 0.0) {
            // Generic Rotation With Canvas Resizing
            int IH = result.getHeight();
            int IW = result.getWidth();
            // Final Width
            double NWL = IW * Math.cos(Math.toRadians(excess));
            double NWR = IH * Math.sin(Math.toRadians(excess));
            int NW = (int) Math.floor((NWL + NWR) + 0.5);
            // Final Height
            double NHL = IW * Math.sin(Math.toRadians(excess));
            double NHU = IH * Math.cos(Math.toRadians(excess));
            int NH = (int) Math.floor((NHL + NHU) + 0.5);
            // Max size (width or hegiht)
            int maxSize = (NW > NH)? NW : NH;
            // Square Resize
            if (resize)
                ip = resizeCanvas(ip, maxSize, maxSize, (maxSize - IW) / 2, (maxSize - IH) / 2, Color.WHITE);
            // Rotation
            ip.setInterpolationMethod(ImageProcessor.BILINEAR);
            ip.setBackgroundValue(-1);  // White
            ip.rotate(excess);
            // Final Resize Canvas
            if (resize)
                ip = resizeCanvas(ip, NW, NH, (NW - maxSize) / 2, (NH - maxSize) / 2, Color.WHITE);                
            result.setProcessor(ip);
        }
        return result;
    }
        
    /**
     * Resize Image Canvas
     * @param ip  Original ImageProcessor
     * @param nWidth new Width
     * @param nHeight new Height
     * @param xOffset horizontal offset centering
     * @param yOffset vertical offset centering
     * @param bgColor new image background color
     * @return  Resized ImageProcessor
     */
    public static ImageProcessor resizeCanvas(ImageProcessor ip, int nWidth, int nHeight, int xOffset, int yOffset, Color bgColor) {
        ImageProcessor ipNew = ip.createProcessor(nWidth, nHeight);
        ipNew.setColor(bgColor);
        ipNew.fill();
        ipNew.insert(ip, xOffset, yOffset);
        return ipNew;
    }
    
    /**
     * Flips ImagePlus image depending selected axis combination
     * @param img ImagePlus object
     * @param axis Axis to flip or axis combinations
     * @return processed ImagePlus object
     */
    public static ImagePlus flipImage(ImagePlus img, Axis axis) {
        ImagePlus result = img.duplicate();
        ImageProcessor ip = result.getProcessor();
        ip.setInterpolate(false);
        switch(axis) {
            case NONE : break; // No flip
            case AXIS_X : ip.flipHorizontal();break;
            case AXIS_Y : ip.flipVertical();break;
            case AXIS_XY: ip.flipHorizontal();ip.flipVertical();break;
            case AXIS_YX: ip.flipVertical();ip.flipHorizontal();break;
        }
        return result;
    }
    
    /**
     * Draws horizontal line to ImagePlus image
     * Overwrites the original (be careful)
     * @param img ImagePlus object
     * @param yoffset line y position (between 0 and img.getHeight())
     * @param width line width in pixels
     * @param color line color
     */
    public static void drawHorizontalLine(ImagePlus img, int yoffset, int width, Color color) {
        ImageProcessor ip = img.getProcessor();
        ip.setColor(color);
        ip.setLineWidth(width);
        ip.drawLine(0, yoffset, img.getWidth(), yoffset);
    }
    
    /**
     * Draws horizontal square to ImagePlus image
     * Overwrites the original (be careful)
     * @param img ImagePlus object
     * @param ystart square y start point
     * @param yend square y end point
     * @param color line color
     */
    public static void drawHorizontalSquare(ImagePlus img, int ystart, int yend, Color color) {
        ImageProcessor ip = img.getProcessor();
        ip.setColor(color);
        ip.setRoi(0, ystart, img.getWidth(), yend);
        ip.fill();
    }
}