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
package jtlc.view.panels.dto;

import ij.ImagePlus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jtlc.core.model.Experiment;
import jtlc.view.dto.AbstractDTO;
import jtlc.core.model.Sample;

/**
 * DataDTO implements DTO for DataPanel
 * Save individual samples: images, front point, seed point, comments and name
 * 
 * @author Cristian Tardivo
 */
public class DataDTO extends AbstractDTO {
    private final HashMap<Integer,String> samplesNames;
    private final HashMap<Integer,Integer> samplesFrontPoint;
    private final HashMap<Integer,Integer> samplesSeedPoint;
    private final HashMap<Integer,String> samplesComments;
    private final HashMap<Integer,Boolean> samplesChanged;
    private final HashMap<Integer,Boolean> samplesLinkedStatus;
    private final HashMap<ImagePlus,Integer> samplesImageId;
    private final List<ImagePlus> samplesImages;
    private String comments;
    
    /**
     * Create new DataDTO
     * @param initialCapacity 
     */
    public DataDTO(int initialCapacity) {
        samplesNames = new HashMap<>(initialCapacity);
        samplesFrontPoint = new HashMap<>(initialCapacity);
        samplesSeedPoint = new HashMap<>(initialCapacity);
        samplesImageId = new HashMap<>(initialCapacity);
        samplesComments = new HashMap<>(initialCapacity);
        samplesChanged = new HashMap<>(initialCapacity);
        samplesLinkedStatus = new HashMap<>(initialCapacity);
        samplesImages = new ArrayList<>(initialCapacity);
    }
    
    /**
     * Create new DataDTO from samples list.
     * @param samples step samples (data and comments)
     * @param comments global step comments
     */
    public DataDTO(List<Sample> samples, String comments) {
        this(samples.size());
        this.comments = comments;
        for (Sample sample : samples) {
            if (samplesImageId.containsValue(sample.getId()))
                throw new IllegalArgumentException("Sample id alredy in use ::" + sample.getId());
            samplesImageId.put(sample.getSourceImage(), sample.getId());
            samplesImages.add(sample.getSourceImage());
            samplesNames.put(sample.getId(), sample.getName());
            samplesFrontPoint.put(sample.getId(), sample.getFrontPoint());
            samplesSeedPoint.put(sample.getId(), sample.getSeedPoint());
            samplesComments.put(sample.getId(), sample.getComments());
            samplesLinkedStatus.put(sample.getId(), sample.isLinked());
            samplesChanged.put(sample.getId(), false);
        }
    }
    
    /**
     * Create new DataDTO from experiment.
     * @param project source project
     */
    public DataDTO(Experiment project) {
        this(project.getAllSamples(), project.getDataComments());
    }
    
    /**
     * Get sample name (using sample id)
     * @param sampleId
     * @return 
     */
    public String getSampleName(int sampleId) {
        return samplesNames.get(sampleId);
    }
    
    /**
     * Get sample name (using sample image)
     * @param img
     * @return 
     */
    public String getSampleName(ImagePlus img) {
        return samplesNames.get(samplesImageId.get(img));
    }
    
    /**
     * Set sample name (using sample id)
     * @param sampleId
     * @param name 
     */
    public void setSampleName(int sampleId, String name) {
        samplesNames.put(sampleId, name);
    }
    
    /**
     * Set sample name (using sample image)
     * @param img
     * @param name 
     */
    public void setSampleName(ImagePlus img, String name) {
        samplesNames.put(samplesImageId.get(img), name);
    }
    
    /**
     * Get sample front point (using sample id)
     * @param sampleId
     * @return 
     */
    public int getSampleFrontPoint(int sampleId) {
        return samplesFrontPoint.get(sampleId);
    }
    
    /**
     * Get sample front point (using sample image)
     * @param img
     * @return 
     */
    public int getSampleFrontPoint(ImagePlus img) {
        return samplesFrontPoint.get(samplesImageId.get(img));
    }
    
    /**
     * Set sample front point (using sample id)
     * @param sampleId
     * @param point 
     */
    public void setSampleFrontPoint(int sampleId, int point) {
        samplesFrontPoint.put(sampleId, point);
    }
    
    /**
     * Set sample front point (using sample image)
     * @param img
     * @param point 
     */
    public void setSampleFrontPoint(ImagePlus img, int point) {
        samplesFrontPoint.put(samplesImageId.get(img), point);
    }
    
    /**
     * Get sample seed point (using sample id)
     * @param sampleId
     * @return 
     */
    public int getSampleSeedPoint(int sampleId) {
        return samplesSeedPoint.get(sampleId);
    }
    
    /**
     * Get sample seed point (using sample image)
     * @param img
     * @return 
     */
    public int getSampleSeedPoint(ImagePlus img) {
        return samplesSeedPoint.get(samplesImageId.get(img));
    }
    
    /**
     * Set sample seed point (using sample id)
     * @param sampleId
     * @param point 
     */
    public void setSampleSeedPoint(int sampleId, int point) {
        samplesSeedPoint.put(sampleId, point);
    }
    
    /**
     * Set sample seed point (using sample image)
     * @param img
     * @param point 
     */
    public void setSampleSeedPoint(ImagePlus img, int point) {
        samplesSeedPoint.put(samplesImageId.get(img), point);
    }
    
    /**
     * Get sample comment (using sample id)
     * @param sampleId
     * @return 
     */
    public String getSampleComment(int sampleId) {
        return samplesComments.get(sampleId);
    }
    
    /**
     * Get sample comment (using sample image)
     * @param img
     * @return 
     */
    public String getSampleComment(ImagePlus img) {
        return samplesComments.get(samplesImageId.get(img));
    }
    
    /**
     * Set sample comment (using sample id)
     * @param sampleId
     * @param comment 
     */
    public void setSampleComment(int sampleId, String comment) {
        samplesComments.put(sampleId, comment);
    }
    
    /**
     * Set sample comment (using sample image)
     * @param img
     * @param comment 
     */
    public void setSampleComment(ImagePlus img, String comment) {
        samplesComments.put(samplesImageId.get(img), comment);
    }
    
    /**
     * Add sample image
     * @param img Sample image
     * @param id  Sample ID (unique)
     */
    public void addSampleImage(ImagePlus img, int id) {
        if (samplesImageId.containsValue(id))
            throw new IllegalArgumentException("Sample id alredy in use ::" + id);
        samplesImageId.put(img, id);
        samplesImages.add(img);
    }
    
    /**
     * Get Samples image list
     * @return 
     */
    public List<ImagePlus> getSamplesImage() {
        return samplesImages;
    }
    
    /**
     * Set comments
     * @param s 
     */
    public void setComments(String s) {
        comments = s;
    }
    
    /**
     * Get comments
     * @return 
     */
    public String getComments() {
        return comments;
    }
    
    /**
     * Set sample changed status (by img)
     * @param img
     * @param changed 
     */
    public void setChanged(ImagePlus img, boolean changed) {
        samplesChanged.put(samplesImageId.get(img), changed);
    }
    
    /**
     * Set sample changed status (by id)
     * @param id
     * @param changed 
     */
    public void setChanged(int id, boolean changed) {
        samplesChanged.put(id, changed);
    }
    
    /**
     * Get sample changed status (by img)
     * @param img
     * @return 
     */
    public boolean hasChanged(ImagePlus img) {
        return samplesChanged.get(samplesImageId.get(img));
    }
    
    /**
     * Get sample changed status (by id)
     * @param id
     * @return 
     */
    public boolean hasChanged(int id) {
        return samplesChanged.get(id);
    }
    
    
    /**
     * Set sample linked status (by img)
     * @param img
     * @param linked 
     */
    public void setLinked(ImagePlus img, boolean linked) {
        samplesLinkedStatus.put(samplesImageId.get(img), linked);
    }
    
    /**
     * Set sample linked status (by id)
     * @param id
     * @param linked 
     */
    public void setLinked(int id, boolean linked) {
        samplesLinkedStatus.put(id, linked);
    }
    
    /**
     * Get sample linked status (by img)
     * @param img
     * @return 
     */
    public boolean isLinked(ImagePlus img) {
        return samplesLinkedStatus.get(samplesImageId.get(img));
    }
    
    /**
     * Get sample linked status (by id)
     * @param id
     * @return 
     */
    public boolean isLinked(int id) {
        return samplesLinkedStatus.get(id);
    }
}