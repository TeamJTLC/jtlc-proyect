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
package jtlc.view.dto;

import java.util.ArrayList;
import java.util.List;
import jtlc.main.common.Pair;
import jtlc.core.model.Experiment;
import jtlc.core.model.Sample;

/**
 * DataDTO implements DTO for DataPanel
 * Save individual samples: images, front point, seed point, comments and name
 * 
 * @author Cristian Tardivo
 */
public class ExportDTO extends AbstractDTO {
    private List<Pair<Integer,String>> datas;
    private List<Pair<Integer,String>> means;
    private List<Pair<Integer,String>> images;
    private List<Pair<Integer,String>> processedImages;
    private List<Pair<Integer,String>> reports;
    
    
    /**
     * Create new DataDTO from experiment.
     * @param project source project
     */
    public ExportDTO(Experiment project) {
        List<Sample> samples = project.getAllSamples();
        
        int initialCapacity = samples != null? samples.size() + 1: 1;
        datas = new ArrayList<>(initialCapacity);
        images = new ArrayList<>(initialCapacity);
        means = new ArrayList<>(initialCapacity - 1);
        processedImages = new ArrayList<>(initialCapacity);
        // Experiment source image
        if (project.hasSourceImage()) {
            images.add(new Pair<>(-1, project.getName()));
            setChanged(true);
        }
        // Experiment processed image
        if (project.hasProcessedImage()) {
            processedImages.add(new Pair<>(-1, project.getName()));
            setChanged(true);
        }
        datas.add(new Pair<>(-1, project.getName()));
        // samples
        if (samples == null) return;
        // Samples data
        for(Sample sample: samples) {
            if (sample.hasMean()) {
                means.add(new Pair<>(sample.getId(), sample.getName()));
                setChanged(true);
            }
            if (sample.hasProcessedImage()) {
                processedImages.add(new Pair<>(sample.getId(), sample.getName()));
                setChanged(true);
            }
            if (sample.hasSourceImage()) {
                images.add(new Pair<>(sample.getId(), sample.getName()));
                setChanged(true);
            }
            if (sample.hasPeaks()) {
                datas.add(new Pair<>(sample.getId(), sample.getName()));
                setChanged(true);
            }
        }
    }
    
    /**
     * Create new DataDTO from experiment, check if experiment has reports
     * @param project source project
     * @param hasReports true/false if experiment has reports
     */
    public ExportDTO(Experiment project, boolean hasReports) {
        this(project);
        if (hasReports) {
            List<Sample> samples = project.getAllSamples();
            int initialCapacity = samples != null? samples.size() + 1: 1;
            //
            reports = new ArrayList<>(initialCapacity);
            reports.add(new Pair<>(-1, project.getName()));
            // Samples data
            for(Sample sample: samples)
                reports.add(new Pair<>(sample.getId(), sample.getName()));
            setChanged(true);
        }
    }
    
    /**
     * Get export data's
     * @return 
     */
    public List<Pair<Integer,String>> getDatas() {
        return datas;
    }
    
    /**
     * Set export data's
     * @param datas 
     */
    public void setDatas(List<Pair<Integer,String>> datas) {
        this.datas = datas;
    }

    /**
     * Get export mean's
     * @return 
     */
    public List<Pair<Integer,String>> getMeans() {
        return means;
    }

    /**
     * Set export mean's
     * @param means 
     */
    public void setMeans(List<Pair<Integer,String>> means) {
        this.means = means;
    }

    /**
     * Get export image's
     * @return 
     */
    public List<Pair<Integer,String>> getImages() {
        return images;
    }

    /**
     * Set export image's
     * @param images 
     */
    public void setImages(List<Pair<Integer,String>> images) {
        this.images = images;
    }
    
    /**
     * Get export processed image's
     * @return 
     */
    public List<Pair<Integer,String>> getProcessedImages() {
        return processedImages;
    }

    /**
     * Set export processed images
     * @param processedImages 
     */
    public void setProcessedImages(List<Pair<Integer,String>> processedImages) {
        this.processedImages = processedImages;
    }
    
    /**
     * Get export reports
     * @return 
     */
    public List<Pair<Integer,String>> getReports() {
        return reports;
    }
    
    /**
     * Set export reports
     * @param reports
     */
    public void setReports(List<Pair<Integer,String>> reports) {
        this.reports = reports;
    }
    
    /**
     * Check if has export data's
     * @return 
     */
    public boolean hasDatas() {
        return datas != null && !datas.isEmpty();
    }

    /**
     * Check if has export mean's
     * @return 
     */
    public boolean hasMeans() {
        return means != null && !means.isEmpty();
    }

    /**
     * Check if has export image's
     * @return 
     */
    public boolean hasImages() {
        return images != null && !images.isEmpty();
    }
    
    /**
     * Check if has export processed image's
     * @return 
     */
    public boolean hasProcessedImages() {
        return processedImages != null && !processedImages.isEmpty();
    }
    
    /**
     * Check if has reports to export
     * @return 
     */
    public boolean hasReports() {
        return reports != null && !reports.isEmpty();
    }
}