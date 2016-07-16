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
package jtlc.core.model;

import java.util.List;
import ij.ImagePlus;
import java.util.LinkedList;
import java.util.stream.Collectors;
import jtlc.main.common.Pair;
import jtlc.main.common.Point;

/**
 * TLC experiment sample.
 * Defines the structure of an individual experiment sample.
 * 
 * @author Baldani Sergio - Tardivo Cristian
 */
public class Sample {
    // Sample Start-End points limits over experiment image
    private Point limits;
    // Sample Image
    private ImagePlus sourceImage;
    // Processed Sample Image
    private ImagePlus processedImage;
    // Solvent front Point (in sample image pixels)
    private int frontPoint;
    // Sample Seed Point (in sample image pixels)
    private int seedPoint;
    // Image GrayScale-Inverted Image Mean
    private List<Pair<Float,Float>> sampleMean;
    // Sample Image Mean Peaks
    private List<Peak> samplePeaks;
    // Sample ID
    private int sampleId;
    // Sample Name
    private String sampleName;
    // Sample Comments
    private String sampleComments;
    // Analysis Comments
    private String analysisComments;
    // Analysis Results Comments
    private String resultsComments;
    // Save if the seed/front points of this sample is linked to the other samples
    private boolean linked;
    // Sample peaks total surface
    private Float totalSurface;
    
    /**
     * Empty Sampel Constructor.
     */
    public Sample() {
        this(null, -1, null);
    }
    
    /**
     * Create sample from limit point
     * @param limits 
     */
    public Sample(Point limits) {
        this();
        this.limits = limits;
    }
    
    /**
     * Sample Constructor.
     * @param ip sample source image
     * @param id sample id number
     * @param name sample name
     */
    public Sample(ImagePlus ip, int id, String name) {
        sourceImage = ip;
        processedImage = null;
        frontPoint = 0;
        seedPoint = (ip != null)? ip.getHeight() : 0;
        sampleId = id;
        sampleName = name;
        linked = true;
        totalSurface = null;
        sampleComments = "";
        analysisComments = "";
        resultsComments = "";
    }
    
    /**
     * Create a sample from another sample like a clone.
     * @param other sample to clone
     */
    public Sample(Sample other) {
        if (other.hasSourceImage())
            sourceImage = other.sourceImage.duplicate();
        if (other.hasProcessedImage())
            processedImage = other.processedImage.duplicate();
        if (other.hasLimits())
            limits = new Point(other.limits);
        if (other.hasMean())
            sampleMean = other.sampleMean.stream().map(Pair::new).collect(Collectors.toList());
        if (other.hasPeaks())
            samplePeaks = other.samplePeaks.stream().map(Peak::new).collect(Collectors.toList());
        frontPoint = other.frontPoint;
        seedPoint = other.seedPoint;
        sampleId = other.sampleId;
        sampleName = other.sampleName;
        sampleComments = other.sampleComments;
        analysisComments = other.analysisComments;
        resultsComments = other.resultsComments;
        linked = other.linked;
        totalSurface = other.totalSurface;
    }
    
    /**
     * Clear Current Sample data.
     */
    public void clear() {
        if (sourceImage != null) {
            sourceImage.flush();
            sourceImage = null;
        }
        if (processedImage != null) {
            processedImage.flush();
            processedImage = null;
        }
        if (sampleMean != null) {
            sampleMean.clear();
            sampleMean = null;
        }
        if (samplePeaks != null) {
            samplePeaks.clear();
            samplePeaks = null;
        }
        frontPoint = 0;
        seedPoint = 0;
        linked = true;
        sourceImage = null;
        sampleComments = "";
        analysisComments = "";
        resultsComments = "";
        totalSurface = null;
    }
    
    /**
     * Get peak limits (start-end points).
     * @return 
     */
    public Point getLimits() {
        return limits;
    }

    /**
     * Set peak limits (start-end points).
     * @param limit 
     */
    public void setLimits(Point limit) {
        this.limits = limit;
    }
    
    /**
     * Check if has peak limits.
     * @return 
     */
    public boolean hasLimits() {
        return limits != null;
    }
    
    /**
     * Set sample source image.
     * @param ip sample source image
     */
    public void setSourceImage(ImagePlus ip) {
        sourceImage = ip;
    }
    
    /**
     * Get sample source image
     * @return sample source image
     */
    public ImagePlus getSourceImage() {
        return sourceImage;
    }
    
    /**
     * Check if has source sample image.
     * @return true/false
     */
    public boolean hasSourceImage() {
        return sourceImage != null;
    }
    
    /**
     * Set processed source sample image.
     * @param ip processed source sample image
     */
    public void setProcessedImage(ImagePlus ip) {
        processedImage = ip;
    }
    
    /**
     * Get processed source sample image.
     * @return processed source sample image
     */
    public ImagePlus getProcessedImage() {
        return processedImage;
    }
    
    /**
     * Clear processed source sample image.
     */
    public void clearProcessedImage() {
        processedImage = null;
    }
    
    /**
     * Check if has processed source sample image.
     * @return true/false
     */
    public boolean hasProcessedImage() {
        return processedImage != null;
    }
    
    /**
     * Set sample name.
     * @param name new sample name
     */
    public void setName(String name) {
        sampleName = name;
        if (sourceImage != null)
            sourceImage.setTitle(name);
        if (processedImage != null)
            processedImage.setTitle(name);
    }
    
    /**
     * Get sample name.
     * @return current sample name
     */
    public String getName() {
        return sampleName;
    }
    
    /**
     * Get sample comments.
     * @return current sample comments
     */
    public String getComments() {
        return sampleComments;
    }
    
    /**
     * Set sample comments.
     * @param comments current sample coments
     */
    public void setComments(String comments) {
        sampleComments = comments;
    }
    
    /**
     * Check if has comments.
     * @return true/false
     */
    public boolean hasComments() {
        return sampleComments != null && !sampleComments.isEmpty();
    }

    /**
     * Get sample analysis comments.
     * @return current sample analysis comments
     */
    public String getAnalysisComments() {
        return analysisComments;
    }

    /**
     * Set sample analysis comments.
     * @param comments current sample analysis comments
     */
    public void setAnalysisComments(String comments) {
        analysisComments = comments;
    }
    
    /**
     * Check if has analysis comments.
     * @return true/false
     */
    public boolean hasAnalysisComments() {
        return analysisComments != null && !analysisComments.isEmpty();
    }
    
    /**
     * Get sample results comments.
     * @return current sample results comments
     */
    public String getResultsComments() {
        return resultsComments;
    }
    
    /**
     * Set sample results comments.
     * @param comments 
     */
    public void setResultsComments(String comments) {
        resultsComments = comments;
    }
    
    /**
     * Check if has results comments.
     * @return true/false
     */
    public boolean hasResultsComments() {
        return resultsComments != null && !resultsComments.isEmpty();
    }
    
    /**
     * Set sample 'Front' point.
     * @param p new sample front point
     */
    public void setFrontPoint(int p) {
        frontPoint = p;
    }
    
    /**
     * Get sample 'front' point.
     * @return current sample front point
     */
    public int getFrontPoint() {
        return frontPoint;
    }
    
    /**
     * Set sample 'seed' point.
     * @param p new sample seed point
     */
    public void setSeedPoint(int p) {
        seedPoint = p;
    }
    
    /**
     * Get sample 'seed' point.
     * @return current sample seed point
     */
    public int getSeedPoint() {
        return seedPoint;
    }
    
    /**
     * Get sample processed image mean.
     * @return sample processed image mean
     */
    public List<Pair<Float,Float>> getMean() {
        return sampleMean;
    }
    
    /**
     * Set sample processed image mean.
     * @param mean sample processed image mean
     */
    public void setMean(List<Pair<Float,Float>> mean) {
        sampleMean = mean;
    }
    
    /**
     * Check if this sample has processed image mean.
     * @return true/false
     */
    public boolean hasMean() {
        return sampleMean != null;
    }
    
    /**
     * Clear sample mean.
     */
    public void clearMean() {
        if (sampleMean != null) {
            sampleMean.clear();
            sampleMean = null;
        }
    }
    
    /**
     * Get Sample peaks
     * @return 
     */
    public List<Peak> getPeaks() {
        return samplePeaks;
    }
    
    /**
     * Set Sample peaks
     * @param peaks 
     */
    public void setPeaks(List<Peak> peaks) {
        this.samplePeaks = peaks;
    }
    
    /**
     * Check if has sample peaks
     * @return 
     */
    public boolean hasPeaks() {
        return samplePeaks != null;
    }
    
    /**
     * Clear sample peaks list.
     */
    public void clearPeaks() {
        if (samplePeaks != null) {
            samplePeaks.clear();
            samplePeaks = null;
        }
        // Clean sample total surfaces because depends of sample peaks
        totalSurface = null;
    }
    
    /**
     * Add a peak to this experiment.
     * @param peak peak to add
     */ 
    public void addPeak(Peak peak) {
        if (samplePeaks == null)
            samplePeaks = new LinkedList<>();
        // Set correct peak id
        peak.setId(samplePeaks.size());
        // Save peak
        samplePeaks.add(peak);
    }
    
    /**
     * Check if the seed/front points of this sample is linked to the other samples
     * @return 
     */
    public boolean isLinked() {
        return linked;
    }
    
    /**
     * Set if the seed/front points of this sample is linked to the other samples
     * @param status linked/unlinked
     */
    public void setLinked(boolean status) {
        linked = status;
    }
    
    /**
     * Get sample total surface defined by sample peaks
     * @return 
     */
    public Float getTotalSurface() {
        return totalSurface;
    }

    /**
     * Set sample total surface defined by sample peaks
     * @param totalSurface 
     */
    public void setTotalSurface(Float totalSurface) {
        this.totalSurface = totalSurface;
    }
    
    /**
     * Check if have sample total surface defined by sample peaks
     * @return 
     */
    public boolean hasTotalSurface() {
        return totalSurface != null;
    }
    
    /**
     * Get current sample id.
     * @return sample 'unique' id
     */
    public int getId() {
        return sampleId;
    }
    
    /**
     * Set current sample id.
     * @param id sample 'unique' id
     */
    public void setId(int id) {
        sampleId = id;
    }
}