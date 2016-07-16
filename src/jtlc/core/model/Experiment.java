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

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import ij.ImagePlus;
import java.io.File;
import java.util.Optional;
import java.util.stream.Collectors;
import jtlc.main.common.Pair;
import jtlc.main.common.Point;
import jtlc.core.processing.AnalysisProcessing.Axis;

/**
 * TLC experiment model.
 * Contains all the data needed for the process.
 * 
 * @author Baldani Sergio - Tardivo Cristian
 */
public class Experiment implements Serializable {
    // Original Image
    private ImagePlus sourceImage;
    // Processed Image
    private ImagePlus processedImage;
    // Cut Points
    private Pair<Point,Point> cutPoints;
    // Samples
    private List<Sample> subSamples;
    // Flip Axis
    private Axis flipAxis;
    // Rotation Angle
    private Double rotationAngle;
    // Steps Comments
    private String splitComments;
    private String rotationComments;
    private String cutComments;
    private String sourceComments;
    private String dataComments;
    private String comparationComments;
    // Sample analysis Date
    private Date analysisDate = new Date();
    // Experiment test Date
    private Date sampleDate = new Date();
    // Experiment Name
    private String experimentName;
    // Experiment Description
    private String experimentDescription;
    /**
     * Extra data for controls.
     */
    private File file;
    private boolean saved;
    
    /**
     * Empty Experiment Constructor.
     */
    public Experiment() {
        this(null, null, null, null);
    }
    
    /**
     * Name Experiment Constructor.
     * @param name new experiment name
     * @param description new experiment description
     * @param sample experiment sample date
     * @param analysis experiment analysis date
     */
    public Experiment(String name, String description, Date sample, Date analysis) {
        sourceImage = null;
        processedImage = null;
        subSamples = null;
        flipAxis = Axis.NONE;
        rotationAngle = 0.0;
        experimentName = name;
        experimentDescription = description;
        analysisDate = analysis;
        sampleDate = sample;
        //
        splitComments = "";
        rotationComments = "";
        cutComments = "";
        sourceComments = "";
        dataComments = "";
        comparationComments = "";
        //
        saved = false;
        file = null;
    }

    /**
     * Create experiment from another experiment like a clone.
     * @param other experiment to clone
     */
    public Experiment(Experiment other) {
        if (other.hasSourceImage())
            sourceImage = other.sourceImage.duplicate();
        if (other.hasProcessedImage())
            processedImage = other.processedImage.duplicate();
        if (other.hasCutPoints())
            cutPoints = new Pair(new Point(other.cutPoints.getFirst()), new Point(other.cutPoints.getSecond()));
        flipAxis = other.flipAxis;
        rotationAngle = other.rotationAngle;
        if (other.analysisDate != null)
            analysisDate = new Date(other.analysisDate.getTime());
        if (other.sampleDate != null)
            sampleDate = new Date(other.sampleDate.getTime());
        if (other.hasSamples())
            subSamples = other.subSamples.stream().map(Sample::new).collect(Collectors.toList());
        splitComments = other.splitComments;
        rotationComments = other.rotationComments;
        cutComments = other.cutComments;
        sourceComments = other.sourceComments;
        dataComments = other.dataComments;
        comparationComments = other.comparationComments;
        experimentName = other.experimentName;
        experimentDescription = other.experimentDescription;
        file = other.file;
        saved = other.saved;
    }
    
    /**
     * Clear current experiment data.
     */
    public void clear() {
        // Clear source image
        if (sourceImage != null) {
            sourceImage.flush();
            sourceImage = null;
        }
        // Clear processed image
        if (processedImage != null) {
            processedImage.flush();
            processedImage = null;
        }
        // Clear subsamples
        if (subSamples != null) {
            subSamples.forEach(s -> s.clear());
            subSamples.clear();
            subSamples = null;
        }
        flipAxis = Axis.NONE;
        rotationAngle = 0.0;
        cutPoints = null;
        splitComments = "";
        rotationComments = "";
        cutComments = "";
        sourceComments = "";
        dataComments = "";
        comparationComments = "";
        saved = false;
    }
    
    /**
     * Set experiment source image.
     * @param ip experiment source image
     */
    public void setSourceImage(ImagePlus ip) {
        if (ip == null)
            throw new NullPointerException("Source image can't be null");
        sourceImage = ip;
    }
    
    /**
     * Get experiment source image.
     * @return experiment source image
     */
    public ImagePlus getSourceImage() {
        return sourceImage;
    }
    
     /**
     * Check if this experiment has source image.
     * @return true/false
     */
    public boolean hasSourceImage() {
        return sourceImage != null;
    }
    
    /**
     * Set samples split comments.
     * @param comments split step comments
     */
    public void setSplitComments(String comments) {
        splitComments = comments;
    }
    
    /**
     * Get samples split comments.
     * @return split step comments
     */
    public String getSplitComments() {
        return splitComments;
    }
    
    /**
     * Check if has split step comments.
     * @return true/false
     */
    public boolean hasSplitComments() {
        return splitComments != null && !splitComments.isEmpty();
    }
    
    /**
     * Set source image rotation comments.
     * @param comments rotation step comments
     */
    public void setRotationComments(String comments) {
        rotationComments = comments;
    }
    
    /**
     * Get source image rotation comments.
     * @return rotation step comments
     */
    public String getRotationComments() {
        return rotationComments;
    }
    
    /**
     * Check if has rotation step comments.
     * @return true/false
     */
    public boolean hasRotationComments() {
        return rotationComments != null && !rotationComments.isEmpty();
    }
    
    /**
     * Set source image crop comments.
     * @param comments cut step comments
     */
    public void setCutComments(String comments) {
        cutComments = comments;
    }
    
    /**
     * Get source image crop comments.
     * @return cut step comments
     */
    public String getCutComments() {
        return cutComments;
    }
    
    /**
     * Check if has cut step comments.
     * @return true/false
     */
    public boolean hasCutComments() {
        return cutComments != null && !cutComments.isEmpty();
    }
    
    /**
     * Set source image comments.
     * @param comments experiment source image comments
     */
    public void setSourceImageComments(String comments) {
        sourceComments = comments;
    }
    
    /**
     * Get source image comments.
     * @return experiment source image comments
     */
    public String getSourceImageComments() {
        return sourceComments;
    }
    
    /**
     * Check if has source image comments
     * @return true/false
     */
    public boolean hasSourceImageComments() {
        return sourceComments != null && !sourceComments.isEmpty();
    }

    /**
     * Get global experiment data step comments.
     * @return experiment sample-data comments
     */
    public String getDataComments() {
        return dataComments;
    }

    /**
     * Set global experiment data step comments.
     * @param dataComments experiment sample-data comments
     */
    public void setDataComments(String dataComments) {
        this.dataComments = dataComments;
    }
    
    /**
     * Check if has experiment data step comments.
     * @return true/false
     */
    public boolean hasDataComments() {
        return dataComments != null && !dataComments.isEmpty();
    }
    
    /**
     * Set samples means comparation comments.
     * @param comparationComments experiment samples comparation comments
     */
    public void setComparationComments(String comparationComments) {
        this.comparationComments = comparationComments;
    }
    
    /**
     * Get samples means comparation comments.
     * @return experiment samples comparation comments
     */
    public String getComparationComments() {
        return comparationComments;
    }

    /**
     * Get samples analysis date.
     * @return experiment analysis date
     */
    public Date getAnalysisDate() {
        return analysisDate;
    }

    /**
     * Set samples analysis date.
     * @param date experiment analysis date
     */
    public void setAnalysisDate(Date date) {
        if (date == null)
            throw new NullPointerException("Analysis date can't be null");
        analysisDate = date;
    }

    /**
     * Get sample experiment date.
     * @return experiment sample date
     */
    public Date getSampleDate() {
        return sampleDate;
    }

    /**
     * Set sample experiment date.
     * @param date experiment sample date
     */
    public void setSampleDate(Date date) {
        if (date == null)
            throw new NullPointerException("Sample date can't be null");
        sampleDate = date;
    }

    /**
     * Get experiment name.
     * @return current experiment name
     */
    public String getName() {
        return experimentName;
    }

    /**
     * Set experiment name.
     * @param name new experiment name
     */
    public void setName(String name) {
        experimentName = name;
    }
    
    /**
     * Get experiment description.
     * @return current experiment description
     */
    public String getDescription() {
        return experimentDescription;
    }

    /**
     * Set experiment description.
     * @param description current experiment description
     */
    public void setDescription(String description) {
        experimentDescription = description;
    }
    
    /**
     * Check if has description
     * @return true/false
     */
    public boolean hasDescription() {
        return experimentDescription != null && !experimentDescription.isEmpty();
    }
    
    /**
     * Set experiment processed image.
     * @param ip processed source image
     */
    public void setProcessedImage(ImagePlus ip) {
        if (ip == null)
            throw new NullPointerException("Processed image can't be null");
        processedImage = ip;
    }
    
    /**
     * Get experiment source processed image.
     * @return image plus (processed source image)
     */
    public ImagePlus getProcessedImage() {
        return processedImage;
    }
    
    /**
     * Clear experiment source processed image.
     */
    public void clearProcessedImage() {
        processedImage = null;
    }
    
    /**
     * Check if this experiment has source processed image.
     * @return true/false
     */
    public boolean hasProcessedImage() {
        return processedImage != null;
    }
    
    /**
     * Get Source Image Flip Axis.
     * @return flip axis combination
     */
    public Axis getFlipAxis() {
        return flipAxis;
    }
    
    /**
     * Set Source Image Flip Axis.
     * @param axis flip axis combination
     */
    public void setFlipAxis(Axis axis) {
        flipAxis = axis;
    }
    
    /**
     * Get Source Image Rotation Angle.
     * @return current rotation angle
     */
    public Double getRotationAngle() {
        return rotationAngle;
    }
    
    /**
     * Set Source Image Rotation Angle.
     * @param angle current rotation angle
     */
    public void setRotationAngle(Double angle) {
        rotationAngle = angle;
    }
    
    /**
     * Set processed image cut points.
     * @param points pair of points (upper,lower)
     */
    public void setCutPoints(Pair<Point,Point> points) {
        if (points == null)
            throw new NullPointerException("Cut points can't be null");
        cutPoints = points;
    }
    
    /**
     * Set processed image cut points.
     * @param upper upper point
     * @param lower lower point
     */
    public void setCutPoints(Point upper, Point lower) {
        if (upper == null)
            throw new NullPointerException("Upper cut point can't be null");
        if (lower == null)
            throw new NullPointerException("Lower cut point can't be null");
        cutPoints = new Pair<>(upper,lower);
    }
    
    /**
     * Get processed image cut points.
     * @return pair of points upper and lower (cut points)
     */
    public Pair<Point,Point> getCutPoints() {
        return cutPoints;
    }
    
    /**
     * Check if has experiment source image cut points.
     * @return true/false
     */
    public boolean hasCutPoints() {
        return cutPoints != null;
    }
        
    /**
     * Add a sample to this experiment.
     * @param sample sample to add
     */
    public void addSample(Sample sample) {
        if (subSamples == null)
            subSamples = new LinkedList<>();
        // Set correct peak id
        sample.setId(subSamples.size());
        // Save peak
        subSamples.add(sample);
    }
    
    /**
     * Get all experiment samples.
     * @return sample list
     */
    public List<Sample> getAllSamples() {
        return subSamples;
    }
    
    /**
     * Get experiment sample by id.
     * @param id 
     * @return sample
     */
    public Sample getSampleById(int id) {
        // Filter sample and get first ocurrence
        Optional<Sample> sample = subSamples.stream().filter(s -> s.getId() == id).findFirst();
        // return result if present
        if (sample.isPresent()) 
            return sample.get();
        // not sample with that id
        return null;
    }
    
    /**
     * Add samples to this experiment.
     * @param samples samples collection to add
     */
    public void setSamples(List<Sample> samples) {
        if (samples == null)
            throw new NullPointerException("Samples can't be null");
        subSamples = samples;
    }
    
    /**
     * Check if the experiment has samples.
     * @return true/false
     */
    public boolean hasSamples() {
        return subSamples != null && !subSamples.isEmpty();
    }
    
    /**
     * Remove all experiment samples.
     */
    public void removeAllSamples() {
        if (subSamples != null) {
            subSamples.forEach(s -> s.clear());
            subSamples.clear();
            subSamples = null;
        }
    }

    /**
     * Get Experiment loaded file.
     * @return experiment file
     */
    public File getFile() {
        return file;
    }

    /**
     * Set experiment saved file.
     * @param file experiment file
     */
    public void setFile(File file) {
        this.file = file;
    }
    
    /**
     * Check if experiment has save file.
     * @return 
     */
    public boolean hasFile() {
        return file != null;
    }

    /**
     * Check if the experiment is saved to a file.
     * @return true/false
     */
    public boolean isSaved() {
        return saved;
    }

    /**
     * Set if the experiment is saved to a file.
     * @param saved true/false
     */
    public void setSaved(boolean saved) {
        this.saved = saved;
    }
}