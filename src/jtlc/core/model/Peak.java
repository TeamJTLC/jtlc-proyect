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
import java.util.stream.Collectors;
import jtlc.main.common.Pair;

/**
 * TLC experiment sample peak.
 * Defines the structure/data of a peak on a sample.
 * 
 * @author Baldani Sergio - Tardivo Cristian
 */
public class Peak {
    // Peak unique id
    private int id;
    // Peak position on sample mean
    private int position;
    // Peak Name
    private String name;
    // Peak Start-End points limits
    private Pair<Float,Float> limits;
    // Peak baseline points
    private List<Pair<Float,Float>> baseline;
    // Peak surface
    private Float surface;
    // Peak relative surface
    private Float relativeSurface;
    // Peak maximum pos-value
    private Pair<Float,Float> maximum;
    // Peak height pos-value
    private Pair<Float,Float> height;
    
    /**
     * Create new empty limits.
     */
    public Peak() {
        this(-1, null, -1);
    }
    
    /**
     * Create peak from a pair of peak limit.
     * @param limtis peak start-end points
     */
    public Peak(Pair<Float,Float> limtis) {
        this(-1, null, -1);
        this.limits = limtis;
    }
    
    /**
     * Create new limits with name and id.
     * @param id unique limits id
     * @param name limits name
     * @param position limits position on sample mean
     */
    public Peak(int id, String name, int position) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.relativeSurface = null;
        this.surface = null;
    }
    
    /**
     * Create new limits with name and id.
     * @param name limits name
     * @param position limits position on sample mean
     * @param limits peak limits values start-end
     */
    public Peak(String name, int position, Pair<Float,Float> limits) {
        this();
        this.name = name;
        this.position = position;
        this.limits = limits;
    }
    
    /**
     * Create a peak from another peak like a clone
     * @param other peak to clone
     */
    public Peak(Peak other) {
        id = other.id;
        position = other.position;
        name = other.name;
        surface = other.surface;
        relativeSurface = other.relativeSurface;
        if (other.hasLimits())
            limits = new Pair<>(other.limits);
        if (other.hasMaximum())
            maximum = new Pair<>(other.maximum);
        if (other.hasHeight())
            height = new Pair<>(other.height);
        if (other.hasBaseline())
            baseline = other.baseline.stream().map(Pair::new).collect(Collectors.toList());
    }

    /**
     * Get peak id.
     * @return 
     */
    public int getId() {
        return id;
    }

    /**
     * Set peak id.
     * @param id 
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Get peak position on sample mean.
     * @return 
     */
    public int getPosition() {
        return position;
    }

    /**
     * Set peak position on sample mean.
     * @param position 
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Get peak name.
     * @return 
     */
    public String getName() {
        return name;
    }

    /**
     * Set peak name.
     * @param name 
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get peak limits (start-end points).
     * @return 
     */
    public Pair<Float,Float> getLimits() {
        return limits;
    }

    /**
     * Set peak limits (start-end points).
     * @param peak 
     */
    public void setLimits(Pair<Float,Float> peak) {
        this.limits = peak;
    }
    
    /**
     * Check if has peak limits.
     * @return 
     */
    public boolean hasLimits() {
        return limits != null;
    }

    /**
     * Get peak baseline points.
     * @return 
     */
    public List<Pair<Float,Float>> getBaseline() {
        return baseline;
    }

    /**
     * Set peak baseline points.
     * @param baseline 
     */
    public void setBaseline(List<Pair<Float, Float>> baseline) {
        this.baseline = baseline;
    }
    
    /**
     * Check if has baseline points.
     * @return 
     */
    public boolean hasBaseline() {
        return baseline != null;
    }

    /**
     * Get peak surface.
     * @return 
     */
    public Float getSurface() {
        return surface;
    }
    
    /**
     * Set peak surface.
     * @param surface 
     */
    public void setSurface(Float surface) {
        this.surface = surface;
    }
    
    /**
     * Check if has peak surface.
     * @return 
     */
    public boolean hasSurface() {
        return surface != null;
    }

    /**
     * Get peak relative surface.
     * @return 
     */
    public Float getRelativeSurface() {
        return relativeSurface;
    }
    
    /**
     * Set peak relative surface.
     * @param relativeSurface 
     */
    public void setRelativeSurface(Float relativeSurface) {
        this.relativeSurface = relativeSurface;
    }
    
    /**
     * Check if has peak relative surface.
     * @return 
     */
    public boolean hasRelativeSurface() {
        return relativeSurface != null;
    }

    /**
     * Get peak maximum (position - value).
     * @return 
     */
    public Pair<Float,Float> getMaximum() {
        return maximum;
    }
    
    /**
     * Set peak maximum (position - value).
     * @param maximum 
     */
    public void setMaximum(Pair<Float,Float> maximum) {
        this.maximum = maximum;
    }
    
    /**
     * Check if has peak maximum.
     * @return 
     */
    public boolean hasMaximum() {
        return maximum != null;
    }

    /**
     * Get peak height (position - value).
     * @return 
     */
    public Pair<Float,Float> getHeight() {
        return height;
    }

    /**
     * Set peak height (position - value).
     * @param height 
     */
    public void setHeight(Pair<Float,Float> height) {
        this.height = height;
    }
    
    /**
     * Check if has peak height.
     * @return 
     */
    public boolean hasHeight() {
        return height != null;
    }
    
    /**
     * Peak String representation
     * @return 
     */
    @Override
    public String toString() {
        return "Name: " + name + " Position: " + position + " Limits: " + limits + " Baseline: " + baseline + "\n"
            + "Surface: " + surface + " Relative: " + relativeSurface + " Maximum: " + maximum + " Height: "+ height;
    }
}
