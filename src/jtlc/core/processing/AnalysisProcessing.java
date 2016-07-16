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

import jtlc.main.common.Pair;
import jtlc.main.common.Point;
//
import ij.ImagePlus;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import jtlc.assets.Assets;
import jtlc.core.model.Peak;
import jtlc.core.model.Sample;

/**
 * This class implements processing for analysis data,
 * samples operations, data search and others.
 * 
 * @author Baldani Sergio - Tardivo Cristian
 */
public class AnalysisProcessing {
    
    /**
     * Axis Enumeration.
     */
    public static enum Axis {
        NONE, AXIS_X, AXIS_Y, AXIS_XY, AXIS_YX;
        
        @Override
        public String toString() {
            return Assets.getString(this.name());
        }
    }
    
    /**
     * Search image intial cut points.
     * @param ip samples (experiment) image as ImagePlus object
     * @return pair of points (delimits the cutting area)
     */
    public static Pair<Point,Point> searchCutPoints(ImagePlus ip) {
        // Clone orignal image
        ImagePlus img = ip.duplicate();
        // Convert and pre-process image
        ImageProcessing.gaussianBlur(img, 8);
        ImageProcessing.toGrayScale(img);
        ImageProcessing.invertImage(img);
        ImageProcessing.imageThreshold(img);
        // Compute image X and Y mean (no inverted-no relative)
        List<Pair<Float,Float>> xMeans = computeMean(img, Axis.AXIS_Y, false, false);
        List<Pair<Float,Float>> yMeans = computeMean(img, Axis.AXIS_X, false, false);
        // Initial values
        float x1 = 0, x2 = img.getWidth(), y1 = 0, y2 = img.getHeight();
        // Search 
        for (Pair<Float,Float> p: xMeans.subList(0, xMeans.size() / 2))
            if (p.getSecond() > 175)
                x1 = p.getFirst();
        //
        for (Pair<Float,Float> p: xMeans.subList((xMeans.size() / 2), xMeans.size()))
            if (p.getSecond() > 175) {
                x2 = p.getFirst();
                break;
            }        
        // Axis Y
        for (Pair<Float,Float> p: yMeans.subList(0, yMeans.size() / 2))
            if (p.getSecond() > 175)
                y1 = p.getFirst();
        //
        for (Pair<Float,Float> p: yMeans.subList((yMeans.size() / 2), yMeans.size()))
            if (p.getSecond() > 175) {
                y2 = p.getFirst();
                break;
            }
        //
        int fx1 = Math.round(x1 + (xMeans.size() * 0.5f) / 100);
        int fy1 = Math.round(y1 + (yMeans.size() * 0.5f) / 100);
        Point top = new Point(fx1, fy1);
        //
        int fx2 = Math.round(x2 - (xMeans.size() * 0.5f) / 100);
        int fy2 = Math.round(y2 - (yMeans.size() * 0.5f) / 100);
        Point foot = new Point(fx2, fy2);
        // Pre-Cut Image
        img = ImageProcessing.cutImage(img, top, foot);
        xMeans = computeMean(img, Axis.AXIS_Y, false, false);
        yMeans = computeMean(img, Axis.AXIS_X, false, false);
        // Better Search
        float xs1 = fx1;
        float xs2 = fx2;
        float ys1 = fy1;
        float ys2 = fy2;
        boolean status = false;
        // Axis X 
        for (Pair<Float,Float> p: xMeans.subList(0, xMeans.size() / 2)) {
            if (p.getSecond() < 5 && !status) {
                xs1 = p.getFirst() + fx1;
                status = true;
            }
            if (p.getSecond() > 5 && status)
                break;
        }
        //
        for (Pair<Float,Float> p: xMeans.subList((xMeans.size() / 2), xMeans.size())) {
            if (p.getSecond() < 5)
                xs2 = fx2 - (img.getWidth() - p.getFirst());
        }
        // Axis Y
        status = false;
        for (Pair<Float,Float> p: yMeans.subList(0, yMeans.size() / 2)) {
            if (p.getSecond() < 5 && !status) {
                ys1 = p.getFirst() + fy1;
                status = true;
            }
            if (p.getSecond() > 5 && status)
                break;
        }
        //
        for (Pair<Float,Float> p: yMeans.subList((yMeans.size() / 2), yMeans.size()))
            if (p.getSecond() < 5)
                ys2 = fy2 - (img.getHeight() - p.getFirst());
        //
        fx1 = Math.round(xs1 + (xMeans.size() * 0.5f) / 100);
        fy1 = Math.round(ys1 + (yMeans.size() * 0.5f) / 100);
        top = new Point(fx1, fy1);
        //
        fx2 = Math.round(xs2 - (xMeans.size() * 0.5f) / 100);
        fy2 = Math.round(ys2 - (yMeans.size() * 0.5f) / 100);
        foot = new Point(fx2, fy2);
        //
        return new Pair<>(top, foot);
    }

    /**
     * Search samples.
     * @param ip samples (experiment) image as ImagePlus object
     * @return list of samples (with sample start-end point or limit)
     */
    public static List<Sample> searchSamples(ImagePlus ip) {
        ImagePlus img = ip.duplicate();
        ImageProcessing.gaussianBlur(img, 8);
        ImageProcessing.toGrayScale(img);
        ImageProcessing.invertImage(img);
        ImageProcessing.imageThreshold(img);
        List<Pair<Integer,Integer>> edgeImageMean = computeCrush(img, Axis.AXIS_Y, false);
        List<Point> samples = searchBinaryAreas(edgeImageMean);        
        return validateSamples(img, samples).stream().map(Sample::new).collect(Collectors.toList());
    }
    
    /**
     * Compute GrayScale-Inverted Image Mean between relative values [0..1].
     * @param ip image as ImagePlus object
     * @return list of pair (float,float) as x-y cordinated values
     */
    public static List<Pair<Float,Float>> computeGIM(ImagePlus ip) {
        ImagePlus img = ip.duplicate();
        ImageProcessing.toGrayScale(img);
        ImageProcessing.invertImage(img);
        return computeMean(img, Axis.AXIS_X, true, true);
    }
    
    /**
     * Search image mean peaks points.
     * @param ip individual sample image as ImagePlus object
     * @return list of pair (float,float) of peak start-end x-axis value
     */
    public static List<Peak> searchPeaks(ImagePlus ip) {
        ImagePlus img = ip.duplicate();
        ImageProcessing.gaussianBlur(img, 10.0f);
        ImageProcessing.toGrayScale(img);
        ImageProcessing.invertImage(img);
        List<Pair<Float,Float>> mean = AnalysisProcessing.computeMean(img, Axis.AXIS_X, true, true);
        List<Pair<Float,Float>> areas = AnalysisProcessing.searchAreas(mean);
        List<Pair<Float,Float>> validAreas = AnalysisProcessing.validateAreas(mean, areas);
        // Retrun the list of peaks
        return validAreas.stream().map(Peak::new).collect(Collectors.toList());
    }
    
    /**
     * Search peak baseline points
     * @param sample Original sample
     * @param peak Input peak
     * @return 
     */
    public static List<Pair<Float,Float>> searchBaseline(Sample sample, Peak peak) {
        return validateArea(sample.getMean(), peak.getLimits());
    }

    /**
     * Validate integration areas and base line (check that don't cut the curve)
     * @param fn image mean/function to process.
     * @param areas integration areas to validate.
     * @return list of pair (float,float) of peak start-end x-axis value
     */
    public static List<Pair<Float,Float>> validateAreas(List<Pair<Float,Float>> fn, float[] areas) {
        // Convert input
        List<Pair<Float,Float>> pAreas = new LinkedList<>();
        for (int i = 0; i < areas.length - 1; i += 2)
            pAreas.add(new Pair<>(areas[i], areas[i + 1]));
        // validate areas
        return validateAreas(fn, pAreas);
    }
    
    /**
     * Integrate peaks according to baseline points (compute absoule area value)
     * @param sample origin sample
     * @param peak peak to integrate
     * @return list of triplets peak-start, peak-end, peak-surface
     */
    public static Float integratePeak(Sample sample, Peak peak) {
        // Get data
        List<Pair<Float,Float>> mean = sample.getMean();
        Pair<Float,Float> limits = peak.getLimits();
        List<Pair<Float,Float>> baseline = peak.getBaseline();
        // Max X-Value
        float max_x = mean.get(mean.size() - 1).getFirst();
        // Round relativized indexs
        int i = Math.round(((mean.size() - 1) * (limits.getFirst() / max_x)));
        int j = Math.round(((mean.size() - 1) * (limits.getSecond() / max_x)));
        // Function Points
        Pair<Float,Float> iP = mean.get(i);
        Pair<Float,Float> jP = mean.get(j);
        // Y-values
        float y1 = iP.getSecond();
        float y2 = jP.getSecond();
        // X-values
        float x1 = iP.getFirst();
        float x2 = jP.getFirst();
        // Peak surface sum
        float peakSurface = 0;
        // for each inner area
        for (Pair<Float,Float> inner: baseline) {
            // Round relativized indexs
            int k = Math.round(((mean.size() - 1) * (inner.getFirst() / max_x)));
            int l = Math.round(((mean.size() - 1) * (inner.getSecond() / max_x)));
            // Get Function values between these inner points
            List<Pair<Float, Float>> fnValues = mean.subList(k, l);
            // Inner area sum
            float innerSurface = 0;
            // For all values in the sub-function
            for (Pair<Float,Float> value: fnValues) {
                // Function values
                float vx = value.getFirst();
                float vy = value.getSecond();
                // Base-line value
                float bv = lineEval(x1, x2, y1, y2, vx);
                // Single 'rectangle' area
                float simpleSurface = vy - bv;
                // Check for valid surface
                if (simpleSurface < 0) System.err.println("area negativa :( (loggear mensajes)");
                // Sum current sub-area
                innerSurface += (simpleSurface < 0)? 0 : simpleSurface;
            }
            peakSurface += innerSurface;
        }
        return peakSurface;
    }
    
    /**
     * Relativize computed surface
     * @param sample
     * @param peak
     * @return relativized surface
     */
    public static Float relativizeSurface(Sample sample, Peak peak) {
        return peak.getSurface() / sample.getTotalSurface() * 100f;
    }
    
    /**
     * Compute Sample total surface (Defined by sample peaks)
     * @param sample
     * @return 
     */
    public static Float computeTotalSurface(Sample sample) {
        return (float) sample.getPeaks().stream().mapToDouble(p -> p.getSurface()).sum();
    }
    
    /**
     * Search local maximuns for function peaks.
     * @param sample origin sample
     * @param peak peak to search maximum
     * @return pair of float,float with maximum pos-value
     */
    public static Pair<Float,Float> computeMaximum(Sample sample, Peak peak) {
        // Get data
        List<Pair<Float,Float>> mean = sample.getMean();
        Pair<Float,Float> limits = peak.getLimits();
        // Max X-Value
        float max_x = mean.get(mean.size() - 1).getFirst();
        // Round relativized indexs
        int i = Math.round(((mean.size() - 1) * (limits.getFirst() / max_x)));
        int j = Math.round(((mean.size() - 1) * (limits.getSecond() / max_x)));
        // Get Maximun y-value between [i-j]
        OptionalDouble opMax = mean.subList(i, j).stream().mapToDouble(p -> p.getSecond()).max();
        // If exist maximun value
        if (opMax.isPresent()) {
            double max = opMax.getAsDouble();
            // filter elements by max-y value
            List<Pair<Float, Float>> maxs = mean.subList(i, j).stream().filter(p -> p.getSecond() == max).collect(Collectors.toList());
            // return mid-element of maxs values (because list can have more than one x-value max-y)
            return maxs.get(maxs.size() / 2);
        }
        // if no maximum found
        return new Pair<>(Float.NaN, Float.NaN);
    }
    
    /**
     * Compute height for function peaks (from baseline to peak maximum)
     * @param sample origin sample
     * @param peak peak to compute height
     * @return peak height as pair x,y (x-pos, y-value height)
     */
    public static Pair<Float,Float> computeHeight(Sample sample, Peak peak) {
        // Get data
        List<Pair<Float,Float>> mean = sample.getMean();
        Pair<Float,Float> maximum = peak.getMaximum();
        List<Pair<Float,Float>> baseline = peak.getBaseline();
        // Max X-Value
        float max_x = mean.get(mean.size() - 1).getFirst();
        // X-Pos of maximum value
        Float maxPos = maximum.getFirst();
        // Y-Value of maximum value
        Float maxValue = maximum.getSecond();
        // Filter all baselines where max is contained
        Optional<Pair<Float,Float>> base = baseline.stream().filter(p -> p.contains(maxPos)).findFirst();
        // Check if exist these pair
        if (base.isPresent()) {
            // Base point
            Pair<Float, Float> point = base.get();
            // Get base indexs
            float x1 = point.getFirst();
            float x2 = point.getSecond();
            // Round relativized indexs
            int i = Math.round(((mean.size() - 1) * (x1 / max_x)));
            int j = Math.round(((mean.size() - 1) * (x2 / max_x)));
            // Get base y values
            float y1 = mean.get(i).getSecond();
            float y2 = mean.get(j).getSecond();
            // Evaluate baseline
            float baseValue = lineEval(x1, x2, y1, y2, maxPos);
            // Compute peak height
            float height = maxValue - baseValue;
            // check for valid height
            if (height < 0) System.err.println("altura negativa :( (loggear mensajes)");
            // retrun height result
            return new Pair<>(maxPos, height);
        }
        // no valid height found
        return new Pair<>(Float.NaN, Float.NaN);
    }
    
    /*******************/
    /* Private Methods */
    /*******************/
    
    /**
     * Validate searched samples split points.
     * @param ip samples (experiment) image as ImagePlus object
     * @param samples samples split points to check
     * @return validated split points
     */
    private static List<Point> validateSamples(ImagePlus ip, List<Point> samples) {
        // Expand few pixels every area
        int extnd = 30;
        // Expand sample "area"
        for (Point p: samples) {
            p.setX(p.getX() - extnd);
            p.setY(p.getY() + extnd);
        }
        // Assume no previous intersections (only when extends point)
        for (int i = 0; i < samples.size() - 1; i ++) {
            Point p0 = samples.get(i);
            Point p1 = samples.get(i + 1);
            // Fix Limits
            if (p0.getX() < 0)
                p0.setX(0);
            if (p1.getY() >= ip.getWidth())
                p1.setY(ip.getWidth() - 1);
            // Fix Intersections
            if (p0.getY() >= p1.getX()) {
                int fix = (p0.getY() - p1.getX() + (extnd / 2)) / 2;
                p0.setY(p0.getY() - fix);
                p1.setX(p1.getX() + fix);
            }
        }
        // Avoid empty samples list
        if (samples.isEmpty()) samples.add(new Point(0, ip.getWidth()));
        // return validated samples list
        return samples;
    }
    
    /**
     * Validate integration area and base line (check that don't cut the curve)
     * @param fn image mean/function to process.
     * @param area integration area to validate.
     * @return list of pair (float,float) of peak start-end x-axis value
     */
    private static List<Pair<Float,Float>> validateArea(List<Pair<Float,Float>> fn, Pair<Float,Float> area) {
        // Max X-Value
        float max_x = fn.get(fn.size() - 1).getFirst();
        // Check for valid areas (don't cut the curve)
        List<Pair<Float,Float>> points = new LinkedList<>();
        // Round relativized indexs
        int i = Math.round(((fn.size() - 1) * (area.getFirst() / max_x)));
        int j = Math.round(((fn.size() - 1) * (area.getSecond() / max_x)));
        // Function Points
        Pair<Float,Float> iP = fn.get(i);
        Pair<Float,Float> jP = fn.get(j);
        // Y-values
        float y1 = iP.getSecond();
        float y2 = jP.getSecond();
        // X-values
        float x1 = iP.getFirst();
        float x2 = jP.getFirst();
        // for all indexs between integration area
        Pair<Float,Float> p = null;
        for (int x = i; x <= j; x++) {
            // Evalue line equation between two points at x
            Pair<Float,Float> xP = fn.get(x);
            float yLine = lineEval(x1, x2, y1, y2, xP.getFirst());
            float yCurve = xP.getSecond();
            // start point
            if (yLine <= yCurve && p == null)
                p = new Pair<>(fn.get(x).getFirst(), null);
            // end point
            if ((yLine >= yCurve || x == j) && p != null) {
                p.setSecond(fn.get(x).getFirst());
                points.add(p);
                p = null;
            }
        }
        // Remove small spaces
        points.removeIf((Pair<Float,Float> pair) -> Math.abs(pair.getSecond() - pair.getFirst()) < max_x * 0.025);
        // Avoid empty integration areas
        if (points.isEmpty()) points.add(new Pair<>(0f, 0f));
        // Return list
        return points;
    }
    
    /**
     * Validate integration areas and base line (check that don't cut the curve)
     * @param fn image mean/function to process.
     * @param areas integration areas to validate.
     * @return list of pair (float,float) of peak start-end x-axis value
     */
    private static List<Pair<Float,Float>> validateAreas(List<Pair<Float,Float>> fn, List<Pair<Float,Float>> areas) {
        // Max X-Value
        float max_x = fn.get(fn.size() - 1).getFirst();
        // Init areas if empty, and try to validate it
        if (areas.isEmpty()) areas.add(new Pair<>(0f, max_x));
        // Check for valid areas (don't cut the curve)
        List<Pair<Float,Float>> points = new LinkedList<>();
        // For each area
        for (Pair<Float,Float> pair: areas) {
            // Round relativized indexs
            int i = Math.round(((fn.size() - 1) * (pair.getFirst() / max_x)));
            int j = Math.round(((fn.size() - 1) * (pair.getSecond() / max_x)));
            // Function Points
            Pair<Float,Float> iP = fn.get(i);
            Pair<Float,Float> jP = fn.get(j);
            // Y-values
            float y1 = iP.getSecond();
            float y2 = jP.getSecond();
            // X-values
            float x1 = iP.getFirst();
            float x2 = jP.getFirst();
            // Avoid empty areas
            if (x1 == x2) continue;
            // for all indexs between integration area
            Pair<Float,Float> p = null;
            for (int x = i; x <= j; x++) {
                // Evalue line equation between two points at x
                Pair<Float,Float> xP = fn.get(x);
                float yLine = lineEval(x1, x2, y1, y2, xP.getFirst());
                float yCurve = xP.getSecond();
                // start point
                if (yLine <= yCurve && p == null)
                    p = new Pair<>(fn.get(x).getFirst(), null);
                // end point
                if ((yLine >= yCurve || x == j) && p != null) {
                    p.setSecond(fn.get(x).getFirst());
                    points.add(p);
                    p = null;
                }
            }
        }
        // Remove small spaces
        points.removeIf((Pair<Float,Float> p) -> Math.abs(p.getSecond() - p.getFirst()) < max_x * 0.025);
        // Avoid empty integration areas
        if (points.isEmpty()) points.add(new Pair<>(0f, 0f));
        // Return list
        return points;
    }
    
    /**
     * Compute image "crush" to binary array, using 8bit w/b image.
     * @param img ImagePlus image object to process
     * @param axis crush axis axis-x -> vertical axis-y -> horizontal 
     * @param inverted invert process bottom-up to top-down
     * @return list of pair integer,integer as position-value -> index::[0|1]
     */
    private static List<Pair<Integer,Integer>> computeCrush(ImagePlus img, Axis axis, boolean inverted) {
        if (axis != Axis.AXIS_X && axis != Axis.AXIS_Y)
            throw new IllegalArgumentException("Invalid Axis");
        if (img.getType() != ImagePlus.GRAY8)
            throw new IllegalArgumentException("Invalid Image");
        //
        int[][] imgArray = ImageProcessing.getImageArray(img);
        int size = (axis == Axis.AXIS_X)? img.getHeight() : img.getWidth();
        int length = (axis == Axis.AXIS_X)? img.getWidth() : img.getHeight();
        int sum = 0;
        List<Pair<Integer,Integer>> result = new ArrayList<>(size);
        //
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < length; j++) {
                sum += (axis == Axis.AXIS_X)? imgArray[j][i] : imgArray[i][j];
            }
            result.add(new Pair<>(((inverted)? (size - 1) - i : i), (sum > 0)? 1 : 0));
            sum = 0;
        }
        // If inverted mean, reverse list (to fix ascending index order)
        if (inverted)
            Collections.reverse(result);
        return result;
    }
    
    /**
     * Compute image mean, vertical or horizonal, inverted and relative
     * @param img ImagePlus image object to process
     * @param axis mean axis axis-x -> vertical axis-y -> horizontal 
     * @param inverted invert process bottom-up to top-down
     * @param relative compute relative indexs between [0..1]
     * @return list of pair float,float as position-value (like a evaluated function)
     */
    private static List<Pair<Float,Float>> computeMean(ImagePlus img, Axis axis, boolean inverted, boolean relative) {
        if (axis != Axis.AXIS_X && axis != Axis.AXIS_Y)
            throw new IllegalArgumentException("Invalid Axis");
        //
        int[][] imgArray = ImageProcessing.getImageArray(img);
        int size = (axis == Axis.AXIS_X)? img.getHeight() : img.getWidth();
        int length = (axis == Axis.AXIS_X)? img.getWidth() : img.getHeight();
        float mean = 0;
        List<Pair<Float,Float>> result = new ArrayList<>(size);
        //
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < length; j++) {
                mean += (axis == Axis.AXIS_X)? imgArray[j][i] : imgArray[i][j];
            }
            float index = (inverted)? (size - 1) - i : i;
            index = (relative)? index / (size - 1) : index;
            result.add(new Pair<>(index, mean / length));
            mean = 0;
        }
        // If inverted mean, reverse list (to fix ascending index order)
        if (inverted)
            Collections.reverse(result);
        return result;
    }
        
    /**
     * Search one-valued areas of a binary function.
     * @param fn binary function to process
     * @return list of points with start-end area position
     */
    private static List<Point> searchBinaryAreas(List<Pair<Integer,Integer>> fn) {
        // Comparator to sort in crescent order the  points list resultant
        Comparator orderCmp = (Comparator<Point>) (Point p1, Point p2) -> {
            if (p1.getX() > p2.getX()) return 1;
            if (p1.getX() < p2.getX()) return -1;
            return 0;
        };
        // Clone function values
        List<Pair<Integer, Integer>> values = fn.stream().map(Pair::new).collect(Collectors.toList());
        // Result List
        List<Point> result = new LinkedList<>();
        // init variables
        int startPos = -1;
        int endPos = -1;
        final int minimunLength = 50;
        // search areas
        for (int i = 0; i < values.size(); i++) {
            Pair<Integer,Integer> p = values.get(i);
            // First 1
            if (startPos == -1 && p.getSecond() == 1) {
                startPos = i;
                continue;
            }
            // First 0 after first 1
            if (startPos != -1 && p.getSecond() == 0) {
                endPos = i - 1;
            }
            // Valid Area?
            if (startPos != -1 && endPos != -1) {
                if (endPos - startPos > minimunLength)
                    result.add(new Point(startPos, endPos));
                startPos = -1;
                endPos = -1;
            }
        }
        // Check list order
        Collections.sort(result, orderCmp);
        // Return result
        return result;
    }
    
    /**
     * Search integration areas or function peaks.
     * @param fn generic function to process (relative or not)
     * @return list of pair with star-end area position
     */
    private static List<Pair<Float,Float>> searchAreas(List<Pair<Float,Float>> fn) {
        // Comparator to sort in crescent order the  points list resultant
        Comparator orderCmp = (Comparator<Point>) (Point p1, Point p2) -> {
            if (p1.getX() > p2.getX()) return 1;
            if (p1.getX() < p2.getX()) return -1;
            return 0;
        };
        // Clone function values
        List<Pair<Float, Float>> values = fn.stream().map(Pair::new).collect(Collectors.toList());
        // Result List
        List<Point> indexs = new LinkedList<>();
        // Local Max value and position
        float maxLocal = 0;
        int maxPos = 0;
        // Left Min value and position
        float minA = 255;
        int minAPos = 0;
        // Rigth Min value and position
        float minB = 255;
        int minBPos = 0;
        // Parameters
        int MAX_PEAKS_COUNT = 10;
        float MIN_PEAK_HEIGHT = 30.0f;
        int SIZE = values.size();
        // Limit search to N peaks
        for (int peaks = 0; peaks < MAX_PEAKS_COUNT; peaks ++) {
            // Search maximum overall
            for (int i = 0; i < SIZE; i++) {
                Pair<Float,Float> p = values.get(i);
                if (p.getSecond() > maxLocal) {
                    maxLocal = p.getSecond();
                    maxPos = i;
                }
            }
            // Stop search at lower peak
            if (maxLocal <= MIN_PEAK_HEIGHT)
                break;
            // Search Left Minimun/Inflection Point
            for (int i = maxPos - 1; i > 1; i--) {
                Pair<Float,Float> p = values.get(i);
                if (p.getSecond() < minA) {
                        minA = p.getSecond();
                        minAPos = i;
                } else {
                    Pair<Float,Float> pa = values.get(i - 1);
                    Pair<Float,Float> pb = values.get(i + 1);
                    if (pa.getSecond() > minA && pb.getSecond() > minA)
                        if (Math.abs(lineSlope(i - 1, i + 1, pa.getSecond(), pb.getSecond())) > 0.05)
                            break;
                }
            }
            // Refine search of left point
            for (int i = minAPos + 1; i < maxPos; i ++) {
                Pair<Float,Float> aux = values.get(i);
                // According to line slope (line between 2 points)
                if (Math.abs(lineSlope(minAPos, i, minA, aux.getSecond())) > 0.075) {
                    minAPos = i - 1;
                    break;
                }
            }
            // Search Rigth Minimun/Inflection Point
            for (int i = maxPos + 1; i < SIZE - 2; i++) {
                Pair<Float,Float> p = values.get(i);
                if (p.getSecond() < minB) {
                    minB = p.getSecond();
                    minBPos = i;
                } else {
                    Pair<Float,Float> pa = values.get(i - 1);
                    Pair<Float,Float> pb = values.get(i + 1);
                    if (pa.getSecond() > minB && pb.getSecond() > minB)
                        if (Math.abs(lineSlope(i - 1, i + 1, pa.getSecond(), pb.getSecond())) < 0.3)
                            break;
                }
            }
            // Refine search of rigth point
            for (int i = minBPos - 1; i > maxPos; i--) {
                Pair<Float,Float> aux = values.get(i);
                // According to line slope (line between 2 points)
                if (Math.abs(lineSlope(i, minBPos, aux.getSecond(), minB)) > 0.075) {
                    minBPos = i + 1;
                    break;
                }
            }
            // fix
            minAPos++; minBPos--;
            // Check final positions
            minAPos = (minAPos < 0)? 0 : ((minAPos < SIZE)? minAPos : SIZE - 1);
            minBPos = (minBPos < 0)? 0 : ((minBPos < SIZE)? minBPos : SIZE - 1);
            // Save values
            indexs.add(new Point(minAPos, minBPos));
            // Clear Current peak
            for (int i = minAPos - 0; i < minBPos + 0; i++)
                values.get(i).setSecond(0.0f);
            // Restart Values
            maxLocal = 0;
            maxPos = 0;
            minA = 255;
            minAPos = 0;
            minB = 255;
            minBPos = 0;
        }
        // Remove Closest Points
        indexs.removeIf((Point p) -> Math.abs(p.getX() - p.getY()) <= 10);
        // Oder List
        Collections.sort(indexs, orderCmp);
        // Clear Intersections
        int oldSize;
        do {
            oldSize = indexs.size();
            // Search of intersected areas
            for (int i = 0; i < indexs.size() - 1; i += 2) { 
                Point p1 = indexs.get(i);
                Point p2 = indexs.get(i + 1);
                if (p2.getX() <  p1.getY()) {
                    // Remove points in conflict
                    indexs.remove(p1);
                    indexs.remove(p2);
                    // Add Super Point
                    indexs.add(new Point(p1.getX(),p2.getY()));
                }
            }
            // Re-Order List
            Collections.sort(indexs, orderCmp); 
        } while(oldSize != indexs.size());
        /*
        // Join similar areas
        do {
            oldSize = result.size();
            // Search of intersected areas
            for (int i = 0; i < result.size() - 1; i += 2) { 
                Point p1 = result.get(i);
                Point p2 = result.get(i + 1);
                // Areas Spaccing < 2
                if (Math.abs(p2.getFirst() -  p1.getSecond()) < 2) {
                    Float v1 = fn.get(p1.getSecond()).getValue();
                    Float v2 = fn.get(p2.getFirst()).getValue();
                    // Smilar Values
                    if (Math.abs(v1 - v2) < 10) {
                        // Remove points in conflict
                        result.remove(p1);
                        result.remove(p2);
                        // Add Super Point
                        result.add(new Point(p1.getFirst(),p2.getSecond()));
                    }
                }
            }
            // Re-Order List
            Collections.sort(result, orderCmp); 
        } while(oldSize != result.size());
        */
        // Return converted results
        return  indexs.stream().map(p -> new Pair<Float,Float>(values.get(p.getX()).getFirst(), values.get(p.getY()).getFirst())).collect(Collectors.toList());
    }
    
    /**
     * Slop/Gradient of a Straight Line.
     * @param x1 left x-coordinate of the line
     * @param x2 right x-coordinate of the line
     * @param y1 left y-coordinate of the line
     * @param y2 right y-coordinate of the line
     * @return slope of the line defined by the coordinates
     */
    private static float lineSlope(float x1, float x2, float y1, float y2) {
        if (x1 != x2)
            return (y2 - y1) / (x1 - x2);
        System.err.println("lineSlope: x1 == x2 (division por cero)");
        System.err.println(Arrays.toString(Thread.currentThread().getStackTrace()));
        return 0;
    }
    
    /**
     * Evaluated Equation of a Straight Line.
     * @param x1 left x-coordinate of the line
     * @param x2 right x-coordinate of the line
     * @param y1 left y-coordinate of the line
     * @param y2 right y-coordinate of the line
     * @param x x coordinate to evaluate
     * @return value at x of the line defined by the coordinates
     */
    private static float lineEval(float x1, float x2, float y1, float y2, float x) {
        if (x1 != x2) {
            float p = (y2 - y1) / (x2 - x1);
            float b = y1 - (p * x1);
            return ((p * x) + b);
        }
        System.err.println("lineEval: x1 == x2 (division por cero)");
        System.err.println(Arrays.toString(Thread.currentThread().getStackTrace()));
        return 0;
    }
}
