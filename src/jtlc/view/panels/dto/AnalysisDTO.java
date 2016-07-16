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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import jtlc.view.dto.AbstractDTO;
import jtlc.main.common.Pair;
import jtlc.core.model.Experiment;
import jtlc.core.model.Sample;

/**
 * AnalysisDTO implements DTO for AnalysisPanel
 * Save individual samples: images, names, means, peaks points and comments
 * 
 * @author Cristian Tardivo
 */
public class AnalysisDTO extends AbstractDTO {
    private final HashMap<ImagePlus,Integer> samplesImageId;
    private final HashMap<Integer,String> samplesNames;
    private final HashMap<Integer,List<Pair<Float,Float>>> samplesMeans;
    private final HashMap<Integer,List<Pair<Float,Float>>> samplesPeaksPoints;
    private final HashMap<Integer,List<Pair<Float,Float>>> samplesBaselinePoints;
    private final HashMap<Integer,Integer> samplesSeedPoints;
    private final HashMap<Integer,Integer> samplesFrontPoints;
    private final HashMap<Integer,String> samplesAnalysisComments;
    private final HashMap<Integer,Boolean> samplesChanged;
    private final List<ImagePlus> samplesImages;
    private String comparationComments;
    
    /**
     * Create new AnalysisDTO
     * @param initialCapacity 
     */
    private AnalysisDTO(int initialCapacity) {
        samplesNames = new HashMap<>(initialCapacity);
        samplesMeans = new HashMap<>(initialCapacity);
        samplesPeaksPoints = new HashMap<>(initialCapacity);
        samplesBaselinePoints = new HashMap<>(initialCapacity);
        samplesImageId = new HashMap<>(initialCapacity);
        samplesAnalysisComments = new HashMap<>(initialCapacity);
        samplesSeedPoints = new HashMap<>(initialCapacity);
        samplesFrontPoints = new HashMap<>(initialCapacity);
        samplesChanged = new HashMap<>(initialCapacity);
        samplesImages = new ArrayList<>(initialCapacity);
    }
    
    /**
     * Create new AnalysisDTO from samples list.
     * @param samples
     */
    private AnalysisDTO(List<Sample> samples) {
        this(samples.size());
        for (Sample sample : samples) {
            if (samplesImageId.containsValue(sample.getId()))
                throw new IllegalArgumentException("Sample id alredy in use ::" + sample.getId());
            samplesImageId.put(sample.getSourceImage(), sample.getId());
            samplesImages.add(sample.getSourceImage());
            samplesNames.put(sample.getId(), sample.getName());
            samplesMeans.put(sample.getId(), sample.getMean());
            samplesPeaksPoints.put(sample.getId(), sample.getPeaks().stream().map(p -> p.getLimits()).collect(Collectors.toList()));
            samplesBaselinePoints.put(sample.getId(), sample.getPeaks().stream().map(p -> p.getBaseline()).flatMap(Collection::stream).collect(Collectors.toList()));
            samplesSeedPoints.put(sample.getId(), sample.getSeedPoint());
            samplesFrontPoints.put(sample.getId(), sample.getFrontPoint());
            samplesAnalysisComments.put(sample.getId(), sample.getAnalysisComments());
            samplesChanged.put(sample.getId(), false);
        }
    }
    
    /**
     * Create new AnalysisDTO from experiment.
     * @param project source project
     */
    public AnalysisDTO(Experiment project) {
        this(project.getAllSamples());
        comparationComments = project.getComparationComments();
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
     * Get sample analysis comments (using sample id)
     * @param sampleId
     * @return 
     */
    public String getAnalysisComments(int sampleId) {
        return samplesAnalysisComments.get(sampleId);
    }
    
    /**
     * Get sample analysis comments (using sample image)
     * @param img
     * @return 
     */
    public String getAnalysisComments(ImagePlus img) {
        return samplesAnalysisComments.get(samplesImageId.get(img));
    }
    
    /**
     * Set sample analysis comments (using sample id)
     * @param sampleId
     * @param comments 
     */
    public void setAnalysisComments(int sampleId, String comments) {
        samplesAnalysisComments.put(sampleId, comments);
    }
    
    /**
     * Set sample analysis comments (using sample image)
     * @param img
     * @param comments 
     */
    public void setAnalysisComments(ImagePlus img, String comments) {
        samplesAnalysisComments.put(samplesImageId.get(img), comments);
    }
    
    /**
     * Get sample mean (using sample id)
     * @param sampleId
     * @return 
     */
    public List<Pair<Float,Float>> getSampleMean(int sampleId) {
        return samplesMeans.get(sampleId);
    }
    
    /**
     * Get sample mean (using sample image)
     * @param img
     * @return 
     */
    public List<Pair<Float,Float>> getSampleMean(ImagePlus img) {
        return samplesMeans.get(samplesImageId.get(img));
    }
    
    /**
     * Set sample mean (using sample id)
     * @param sampleId
     * @param mean 
     */
    public void setSampleMean(int sampleId, List<Pair<Float,Float>> mean) {
        samplesMeans.put(sampleId, mean);
    }
    
    /**
     * Set sample mean (using sample image)
     * @param img
     * @param mean 
     */
    public void setSampleMean(ImagePlus img, List<Pair<Float,Float>> mean) {
        samplesMeans.put(samplesImageId.get(img), mean);
    }
    
    /**
     * Get sample peaks (using sample id)
     * @param sampleId
     * @return 
     */
    public List<Pair<Float,Float>> getSamplePeaks(int sampleId) {
        return samplesPeaksPoints.get(sampleId);
    }
    
    /**
     * Get sample peaks (using sample image)
     * @param img
     * @return 
     */
    public List<Pair<Float,Float>> getSamplePeaks(ImagePlus img) {
        return samplesPeaksPoints.get(samplesImageId.get(img));
    }
    
    /**
     * Set sample peaks (using sample id)
     * @param sampleId
     * @param peaks 
     */
    public void setSamplePeaks(int sampleId, List<Pair<Float,Float>> peaks) {
        samplesPeaksPoints.put(sampleId, peaks);
    }
    
    /**
     * Set sample peaks (using sample image)
     * @param img
     * @param peaks 
     */
    public void setSamplePeaks(ImagePlus img, List<Pair<Float,Float>> peaks) {
        samplesPeaksPoints.put(samplesImageId.get(img), peaks);
    }
    
    /**
     * Get sample baseline points (using sample id)
     * @param sampleId
     * @return 
     */
    public List<Pair<Float,Float>> getSampleBaseline(int sampleId) {
        return samplesBaselinePoints.get(sampleId);
    }
    
    /**
     * Get sample base line points (using sample image)
     * @param img
     * @return 
     */
    public List<Pair<Float,Float>> getSampleBaseline(ImagePlus img) {
        return samplesBaselinePoints.get(samplesImageId.get(img));
    }
    
    /**
     * Set sample baseline points (using sample id)
     * @param sampleId
     * @param peaks 
     */
    public void setSampleBaseline(int sampleId, List<Pair<Float,Float>> peaks) {
        samplesBaselinePoints.put(sampleId, peaks);
    }
    
    /**
     * Set sample baseline points (using sample image)
     * @param img
     * @param peaks 
     */
    public void setSampleBaseline(ImagePlus img, List<Pair<Float,Float>> peaks) {
        samplesBaselinePoints.put(samplesImageId.get(img), peaks);
    }
    
    /**
     * Get sample seed point (using sample id)
     * @param sampleId
     * @return 
     */
    public int getSampleSeedPoint(int sampleId) {
        return samplesSeedPoints.get(sampleId);
    }
    
    /**
     * Get sample seed point (using sample image)
     * @param img
     * @return 
     */
    public int getSampleSeedPoint(ImagePlus img) {
        return samplesSeedPoints.get(samplesImageId.get(img));
    }
    
    /**
     * Set sample seed point (using sample id)
     * @param sampleId
     * @param seed 
     */
    public void setSampleSeedPoint(int sampleId, int seed) {
        samplesSeedPoints.put(sampleId, seed);
    }
    
    /**
     * Set sample seed point (using sample image)
     * @param img
     * @param seed 
     */
    public void setSampleSeedPoint(ImagePlus img, int seed) {
        samplesSeedPoints.put(samplesImageId.get(img), seed);
    }
    
    /**
     * Get sample front point (using sample id)
     * @param sampleId
     * @return 
     */
    public int getSampleFrontPoint(int sampleId) {
        return samplesFrontPoints.get(sampleId);
    }
    
    /**
     * Get sample front point (using sample image)
     * @param img
     * @return 
     */
    public int getSampleFrontPoint(ImagePlus img) {
        return samplesFrontPoints.get(samplesImageId.get(img));
    }
    
    /**
     * Set sample front point (using sample id)
     * @param sampleId
     * @param front 
     */
    public void setSampleFrontPoint(int sampleId, int front) {
        samplesFrontPoints.put(sampleId, front);
    }
    
    /**
     * Set sample front point (using sample image)
     * @param img
     * @param front 
     */
    public void setSampleFrontPoint(ImagePlus img, int front) {
        samplesFrontPoints.put(samplesImageId.get(img), front);
    }

    /**
     * Get experiment samples comparation comments
     * @return 
     */
    public String getComparationComments() {
        return comparationComments;
    }

    /**
     * Set experiment samples comparations comments
     * @param comparationComments 
     */
    public void setComparationComments(String comparationComments) {
        this.comparationComments = comparationComments;
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
}