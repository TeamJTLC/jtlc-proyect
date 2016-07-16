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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import jtlc.view.dto.AbstractDTO;
import jtlc.main.common.Pair;
import jtlc.main.common.Point;
import jtlc.core.model.Experiment;
import jtlc.core.model.Peak;
import jtlc.core.model.Sample;
import jtlc.core.processing.AnalysisProcessing;

/**
 * ResultsDTO implements DTO for ReportsPanel
 * Save individual samples: images, names, means, peaks points and comments
 * 
 * @author Cristian Tardivo
 */
public class ReportsDTO extends AbstractDTO {
    // Samples
    private final HashMap<Integer,String> samplesNames;
    private final HashMap<Integer,String> samplesComments;
    private final HashMap<Integer,String> samplesAnalysisComments;
    private final HashMap<Integer,String> samplesResultsComments;
    private final HashMap<Integer,Integer> samplesSeedPoints;
    private final HashMap<Integer,Integer> samplesFrontPoints;
    private final HashMap<Integer,List<Pair<Float,Float>>> samplesMeans;
    private final HashMap<Integer,List<Pair<Float,Float>>> samplesPeaksPoints;
    private final HashMap<Integer,List<Pair<Float,Float>>> samplesBaselinePoints;
    private final List<Integer> samplesIds;
    private final HashMap<String,Boolean> resultsChanges;
    private final HashMap<Integer,Point> samplesLimits;
    private final HashMap<Integer,Float> samplesTotalSurfaces;
    private final HashMap<Integer,ImagePlus> samplesSourceImages;
    private final HashMap<Integer,ImagePlus> samplesProcessedImages;
    // Peaks
    private final HashMap<Integer,List<Integer>> samplesPeaksIds;
    private final HashMap<String,Pair<Float,Float>> peaksLimits;
    private final HashMap<String,List<Pair<Float,Float>>> peaksBaselines;
    private final HashMap<String,Integer> peaksPositions;
    private final HashMap<String,Float> peaksSurfaces;
    private final HashMap<String,Float> peaksRelativeSurfaces;
    private final HashMap<String,Pair<Float,Float>> peaksMaximums;
    private final HashMap<String,Pair<Float,Float>> peaksHeights;
    private final HashMap<String,String> peaksNames;
    //
    private Date analysisDate;
    private String comparationComments;
    private String cutComments;
    private Pair<Point, Point> cutPoints;
    private String dataComments;
    private String description;
    private AnalysisProcessing.Axis flipAxis;
    private String projectName;
    private ImagePlus processedImage;
    private Double rotationAngle;
    private String rotationComments;
    private Date sampleDate;
    private ImagePlus sourceImage;
    private String sourceImageComments;
    private String splitComments;
    private String splitPoints;
    
    /**
     * Create new ReportsDTO
     * @param initialCapacity 
     */
    private ReportsDTO(int initialCapacity) {
        resultsChanges = new HashMap<>(initialCapacity);
        samplesNames = new HashMap<>(initialCapacity);
        samplesComments = new HashMap<>(initialCapacity);
        samplesAnalysisComments = new HashMap<>(initialCapacity);
        samplesMeans = new HashMap<>(initialCapacity);
        samplesPeaksPoints = new HashMap<>(initialCapacity);
        samplesBaselinePoints = new HashMap<>(initialCapacity);
        samplesIds = new ArrayList<>(initialCapacity);
        samplesResultsComments = new HashMap<>(initialCapacity);
        samplesSeedPoints = new HashMap<>(initialCapacity);
        samplesFrontPoints = new HashMap<>(initialCapacity);
        samplesLimits = new HashMap<>(initialCapacity);
        samplesTotalSurfaces = new HashMap<>(initialCapacity);
        samplesSourceImages = new HashMap<>(initialCapacity);
        samplesProcessedImages = new HashMap<>(initialCapacity);
        
        peaksLimits = new HashMap<>(initialCapacity);
        samplesPeaksIds = new HashMap<>(initialCapacity);
        peaksBaselines = new HashMap<>(initialCapacity);
        peaksPositions = new HashMap<>(initialCapacity);
        peaksSurfaces = new HashMap<>(initialCapacity);
        peaksRelativeSurfaces = new HashMap<>(initialCapacity);
        peaksMaximums = new HashMap<>(initialCapacity);
        peaksHeights = new HashMap<>(initialCapacity);
        peaksNames = new HashMap<>(initialCapacity);
    }
    
    /**
     * Create new ReportsDTO from samples list.
     * @param samples
     */
    private ReportsDTO(List<Sample> samples) {
        this(samples.size());
        for (Sample sample : samples) {
            if (samplesIds.contains(sample.getId()))
                throw new IllegalArgumentException("Sample id alredy in use ::" + sample.getId());
            samplesIds.add(sample.getId());
            samplesNames.put(sample.getId(), sample.getName());
            samplesMeans.put(sample.getId(), sample.getMean());
            samplesSeedPoints.put(sample.getId(), sample.getSeedPoint());
            samplesFrontPoints.put(sample.getId(), sample.getFrontPoint());
            samplesResultsComments.put(sample.getId(), sample.getResultsComments());
            samplesComments.put(sample.getId(), sample.getComments());
            samplesLimits.put(sample.getId(), sample.getLimits());
            samplesTotalSurfaces.put(sample.getId(), sample.getTotalSurface());
            samplesAnalysisComments.put(sample.getId(), sample.getAnalysisComments());
            samplesSourceImages.put(sample.getId(), sample.getSourceImage().duplicate());
            samplesProcessedImages.put(sample.getId(), sample.getProcessedImage().duplicate());
            //
            samplesPeaksPoints.put(sample.getId(), sample.getPeaks().stream().map(p -> p.getLimits()).collect(Collectors.toList()));
            samplesBaselinePoints.put(sample.getId(), sample.getPeaks().stream().map(p -> p.getBaseline()).flatMap(Collection::stream).collect(Collectors.toList()));
            //
            List<Integer> samplePeaksIds = new ArrayList<>(sample.getPeaks().size());
            for (Peak peak: sample.getPeaks()) {
                samplePeaksIds.add(peak.getId());
                peaksLimits.put(getSamplePeakId(sample, peak), peak.getLimits());
                peaksBaselines.put(getSamplePeakId(sample, peak), peak.getBaseline());
                peaksHeights.put(getSamplePeakId(sample, peak), peak.getHeight());
                peaksMaximums.put(getSamplePeakId(sample, peak), peak.getMaximum());
                peaksNames.put(getSamplePeakId(sample, peak), peak.getName());
                peaksPositions.put(getSamplePeakId(sample, peak), peak.getPosition());
                peaksSurfaces.put(getSamplePeakId(sample, peak), peak.getSurface());
                peaksRelativeSurfaces.put(getSamplePeakId(sample, peak), peak.getRelativeSurface());
                resultsChanges.put(getSamplePeakId(sample, peak), false);
            }
            resultsChanges.put(String.valueOf(sample.getId()), false);
            samplesPeaksIds.put(sample.getId(), samplePeaksIds);
        }
    }

    /**
     * Create new ReportsDTO from experiment.
     * @param project source project
     */
    public ReportsDTO(Experiment project) {
        this(project.getAllSamples());
        analysisDate = project.getAnalysisDate();
        comparationComments = project.getComparationComments();
        cutComments = project.getCutComments();
        cutPoints = project.getCutPoints();
        dataComments = project.getDataComments();
        description = project.getDescription();
        flipAxis = project.getFlipAxis();
        projectName = project.getName();
        processedImage = project.getProcessedImage().duplicate();
        rotationAngle = project.getRotationAngle();
        rotationComments = project.getRotationComments();
        sampleDate = project.getSampleDate();
        sourceImage = project.getSourceImage().duplicate();
        sourceImageComments = project.getSourceImageComments();
        splitComments = project.getSplitComments();
        splitPoints = project.getAllSamples().stream().map(s -> s.getLimits().toString()).collect(Collectors.joining(" "));
    }
    
    /**
     * Get sample peak string id
     * @param sampleId sample id
     * @param peakId peak id
     * @return 
     */
    private String getSamplePeakId(int sampleId, int peakId) {
        return sampleId + "-" + peakId;
    }
    
    /**
     * Get sample peak string id (using Sample and Peak)
     * @param sample sample
     * @param peak peak
     * @return 
     */
    private String getSamplePeakId(Sample sample, Peak peak) {
        return getSamplePeakId(sample.getId(), peak.getId());
    }
    
    /**
     * Get samples ids list
     * @return 
     */
    public List<Integer> getSamplesIds() {
        return samplesIds;
    }
    
    /**
     * Get sample peaks ids list
     * @param sampleId
     * @return 
     */
    public List<Integer> getSamplePeaksIds(int sampleId) {
        return samplesPeaksIds.get(sampleId);
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
     * Set sample name (using sample id)
     * @param sampleId
     * @param name 
     */
    public void setSampleName(int sampleId, String name) {
        samplesNames.put(sampleId, name);
    }
    
    /**
     * Get sample results comments (using sample id)
     * @param sampleId
     * @return 
     */
    public String getResultsComments(int sampleId) {
        return samplesResultsComments.get(sampleId);
    }
    
    /**
     * Set sample results comments (using sample id)
     * @param sampleId
     * @param comments 
     */
    public void setResultsComments(int sampleId, String comments) {
        samplesResultsComments.put(sampleId, comments);
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
     * Set sample mean (using sample id)
     * @param sampleId
     * @param mean 
     */
    public void setSampleMean(int sampleId, List<Pair<Float,Float>> mean) {
        samplesMeans.put(sampleId, mean);
    }
    
    /**
     * Get sample Peak name (using sample id and peak id)
     * @param sampleId
     * @param peakId
     * @return 
     */
    public String getPeakName(int sampleId, int peakId) {
        return peaksNames.get(getSamplePeakId(sampleId, peakId));
    }
    
    /**
     * Set sample Peak name (using sample id and peak id)
     * @param sampleId
     * @param peakId
     * @param name
     */
    public void setPeakName(int sampleId, int peakId, String name) {
        peaksNames.put(getSamplePeakId(sampleId, peakId), name);
    }
    
    /**
     * Get sample peak position  (using sample id and peak id)
     * @param sampleId
     * @param peakId
     * @return 
     */
    public int getPeakPosition(int sampleId, int peakId) {
        return peaksPositions.get(getSamplePeakId(sampleId, peakId));
    }
    
    /**
     * Set sample peak position  (using sample id and peak id)
     * @param sampleId
     * @param peakId
     * @param position
     */
    public void setPeakPosition(int sampleId, int peakId, int position) {
        peaksPositions.put(getSamplePeakId(sampleId, peakId), position);
    }
    
    /**
     * Get sample peaks (using sample id and peak id)
     * @param sampleId
     * @param peakId
     * @return 
     */
    public Pair<Float,Float> getPeaksLimits(int sampleId, int peakId) {
        return peaksLimits.get(getSamplePeakId(sampleId, peakId));
    }
    
    /**
     * Set sample peaks (using sample id and peak id)
     * @param sampleId
     * @param peakId
     * @param limits
     */
    public void setPeaksLimits(int sampleId, int peakId, Pair<Float,Float> limits) {
        peaksLimits.put(getSamplePeakId(sampleId, peakId), limits);
    }
    
    /**
     * Get sample baseline points (using sample id and peak id)
     * @param sampleId
     * @param peakId
     * @return 
     */
    public List<Pair<Float,Float>> getPeakBaseline(int sampleId, int peakId) {
        return peaksBaselines.get(getSamplePeakId(sampleId, peakId));
    }
    
    /**
     * Set sample baseline points (using sample id and peak id)
     * @param sampleId
     * @param peakId
     * @param baseline 
     */
    public void setPeakBaseline(int sampleId, int peakId, List<Pair<Float,Float>> baseline) {
        peaksBaselines.put(getSamplePeakId(sampleId, peakId), baseline);
    }
    
    /**
     * Get sample peaks maximum (using sample id and peak id)
     * @param sampleId
     * @param peakId
     * @return 
     */
    public Pair<Float,Float> getPeakMaximum(int sampleId, int peakId) {
        return peaksMaximums.get(getSamplePeakId(sampleId, peakId));
    }
    
    /**
     * Set sample peaks maximum (using sample id and peak id)
     * @param sampleId
     * @param peakId
     * @param maximum
     */
    public void setPeakMaximum(int sampleId, int peakId, Pair<Float,Float> maximum) {
        peaksMaximums.put(getSamplePeakId(sampleId, peakId), maximum);
    }
    
    /**
     * Get sample peaks height (using sample id and peak id)
     * @param sampleId
     * @param peakId
     * @return 
     */
    public Pair<Float,Float> getPeakHeight(int sampleId, int peakId) {
        return peaksHeights.get(getSamplePeakId(sampleId, peakId));
    }
    
    /**
     * Set sample peaks height (using sample id and peak id)
     * @param sampleId
     * @param peakId
     * @param height
     */
    public void setPeakHeight(int sampleId, int peakId, Pair<Float,Float> height) {
        peaksHeights.put(getSamplePeakId(sampleId, peakId), height);
    }
    
    /**
     * Get sample peaks surfaces (using sample id and peak id)
     * @param sampleId
     * @param peakId
     * @return 
     */
    public Float getPeakSurface(int sampleId, int peakId) {
        return peaksSurfaces.get(getSamplePeakId(sampleId, peakId));
    }
    
    /**
     * Set sample peaks surfaces (using sample id and peak id)
     * @param sampleId
     * @param peakId
     * @param surface
     */
    public void setPeakSurface(int sampleId, int peakId, float surface) {
        peaksSurfaces.put(getSamplePeakId(sampleId, peakId), surface);
    }
    
    /**
     * Get sample peaks relative surfaces (using sample id and peak id)
     * @param sampleId
     * @param peakId
     * @return 
     */
    public Float getPeakRelativeSurface(int sampleId, int peakId) {
        return peaksRelativeSurfaces.get(getSamplePeakId(sampleId, peakId));
    }
    
    /**
     * Set sample peaks relative surfaces (using sample id and peak id)
     * @param sampleId 
     * @param peakId 
     * @param relativeSurface 
     */
    public void setPeakRelativeSurface(int sampleId, int peakId, float relativeSurface) {
        peaksRelativeSurfaces.put(getSamplePeakId(sampleId, peakId), relativeSurface);
        
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
     * Set sample seed point (using sample id)
     * @param sampleId
     * @param seed 
     */
    public void setSampleSeedPoint(int sampleId, int seed) {
        samplesSeedPoints.put(sampleId, seed);
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
     * Set sample front point (using sample id)
     * @param sampleId
     * @param front 
     */
    public void setSampleFrontPoint(int sampleId, int front) {
        samplesFrontPoints.put(sampleId, front);
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
     * Set sample peaks (using sample id)
     * @param sampleId
     * @param peaks 
     */
    public void setSamplePeaks(int sampleId, List<Pair<Float,Float>> peaks) {
        samplesPeaksPoints.put(sampleId, peaks);
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
     * Set sample baseline points (using sample id)
     * @param sampleId
     * @param peaks 
     */
    public void setSampleBaseline(int sampleId, List<Pair<Float,Float>> peaks) {
        samplesBaselinePoints.put(sampleId, peaks);
    }
    
    public String getSampleComments(int sampleId) {
        return samplesComments.get(sampleId);
    }
    
    public void setSampleComments(int sampleId, String comments) {
        samplesComments.put(sampleId, comments);
    }
    
    public Point getSampleLimits(int sampleId) {
        return samplesLimits.get(sampleId);
    }
    
    public void setSampleLimits(int sampleId, Point limits) {
        samplesLimits.put(sampleId, limits);
    }
    
    public Float getSampleTotalSurface(int sampleId) {
        return samplesTotalSurfaces.get(sampleId);
    }
    
    public void setSampleTotalSurface(int sampleId, Float surface) {
        samplesTotalSurfaces.put(sampleId, surface);
    }
    
    public String getSampleAnalysisComments(int sampleId) {
        return samplesAnalysisComments.get(sampleId);
    }
    
    public void setSampleAnalysisComments(int sampleId, String comments) {
        samplesAnalysisComments.put(sampleId, comments);
    }
    
    public ImagePlus getSampleSourceImage(int sampleId) {
        return samplesSourceImages.get(sampleId);
    }
    
    public void setSampleSourceImage(int sampleId, ImagePlus img) {
        samplesSourceImages.put(sampleId, img.duplicate());
    }
    
    public ImagePlus getSampleProcessedImage(int sampleId) {
        return samplesProcessedImages.get(sampleId);
    }
    
    public void setSampleProcessedImage(int sampleId, ImagePlus img) {
        samplesProcessedImages.put(sampleId, img.duplicate());
    }
    
    public Date getAnalysisDate() {
        return analysisDate;
    }

    public void setAnalysisDate(Date date) {
        this.analysisDate = date;
    }

    public String getComparationComments() {
        return comparationComments;
    }

    public void setComparationComments(String comments) {
        this.comparationComments = comments;
    }

    public String getCutComments() {
        return cutComments;
    }

    public void setCutComments(String comments) {
        this.cutComments = comments;
    }

    public Pair<Point, Point> getCutPoints() {
        return cutPoints;
    }

    public void setCutPoints(Pair<Point,Point> points) {
        this.cutPoints = points;
    }

    public String getDataComments() {
        return dataComments;
    }

    public void setDataComments(String comments) {
        this.dataComments = comments;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AnalysisProcessing.Axis getFlipAxis() {
        return flipAxis;
    }

    public void setFlipAxis(AnalysisProcessing.Axis axis) {
        this.flipAxis = axis;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String name) {
        this.projectName = name;
    }

    public ImagePlus getProcessedImage() {
        return processedImage;
    }

    public void setProcessedImage(ImagePlus img) {
        this.processedImage = img.duplicate();
    }

    public Double getRotationAngle() {
        return rotationAngle;
    }

    public void setRotationAngle(Double angle) {
        this.rotationAngle = angle;
    }

    public String getRotationComments() {
        return rotationComments;
    }

    public void setRotationComments(String comments) {
        this.rotationComments = comments;
    }

    public Date getSampleDate() {
        return sampleDate;
    }

    public void setSampleDate(Date date) {
        this.sampleDate = date;
    }

    public ImagePlus getSourceImage() {
        return sourceImage;
    }

    public void setSourceImage(ImagePlus img) {
        this.sourceImage = img.duplicate();
    }

    public String getSourceImageComments() {
        return sourceImageComments;
    }

    public void setSourceImageComments(String comments) {
        this.sourceImageComments = comments;
    }

    public String getSplitComments() {
        return splitComments;
    }

    public void setSplitComments(String comments) {
        this.splitComments = comments;
    }

    public String getSplitPoints() {
        return splitPoints;
    }

    public void setSplitPoints(String points) {
        this.splitPoints = points;
    }
        
    /**
     * Set sample changed status (by sample id)
     * @param sampleId
     * @param changed 
     */
    public void setChanged(int sampleId, boolean changed) {
        resultsChanges.put(String.valueOf(sampleId), changed);
    }
    
    /**
     * Get sample changed status (by sample id)
     * @param sampleId
     * @return 
     */
    public boolean hasChanged(int sampleId) {
        return resultsChanges.get(String.valueOf(sampleId));
    }
    
    /**
     * Set sample peak changed status (by sample id and peak id)
     * @param sampleId
     * @param peakId
     * @param changed 
     */
    public void setChanged(int sampleId, int peakId, boolean changed) {
        resultsChanges.put(getSamplePeakId(sampleId, peakId), changed);
    }
    
    /**
     * Get sample peak changed status (by sample id and peak id)
     * @param sampleId
     * @param peakId
     * @return 
     */
    public boolean hasChanged(int sampleId, int peakId) {
        return resultsChanges.get(getSamplePeakId(sampleId, peakId));
    }
}