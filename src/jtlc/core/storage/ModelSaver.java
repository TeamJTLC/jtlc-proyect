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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import jtlc.assets.Assets;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//
import jtlc.main.common.Pair;
import jtlc.main.common.Point;
import jtlc.core.model.Experiment;
import jtlc.core.model.Peak;
import jtlc.core.model.Sample;

/**
 * Experiment Saver.
 * Implements project/experiment saver to zip file.
 * 
 * @author Baldani Sergio - Tardivo Cristian
 */
public class ModelSaver {
    // Folders
    private static final String SEPARATOR = File.separator;
    private static final String IMAGE_FOLDER = SEPARATOR + "images";
    private static final String SAMPLE_FOLDER = SEPARATOR + "sample-";
    private static final String DATA_FOLDER = SEPARATOR + "data";
    // Images Files
    private static final String SOURCE_IMAGE = "src-image.jpg";
    private static final String PROCESSED_IMAGE = "processed-image.jpg";
    // Mean Files
    private static final String MEAN_FILE = "mean-values.txt";
    // Experiment File
    private static final String EXPERIMENT_FILE = "experiment.xml";
    // Error logger
    private static final Logger LOG = LoggerFactory.getLogger(ModelLoader.class);
    
    /**
     * Save Project experiment to zip file
     * Project data into XML file
     * Project Images (and samples split images)
     * Samples Means and other data
     * @param model Experiment to save
     * @param file file path to save
     * @return save/can't save
     */    
    public static boolean saveExperiment(Experiment model, File file) {
        // Project ZIP
        try (FileOutputStream fos = new FileOutputStream(file, false)) {
            try (ZipOutputStream zos = new ZipOutputStream(fos)) {
                // Experiment XML Data
                ByteArrayOutputStream xmlData = generateXML(model);
                ZipEntry zie = new ZipEntry(EXPERIMENT_FILE);
                zos.putNextEntry(zie);
                xmlData.writeTo(zos);
                zos.closeEntry();
                // Experiment Source Image
                if (model.hasSourceImage()) {
                    ByteArrayOutputStream sourceImage = ImageStore.saveImage(model.getSourceImage(), 100);
                    zie = new ZipEntry(IMAGE_FOLDER + SEPARATOR + SOURCE_IMAGE);
                    zos.putNextEntry(zie);
                    sourceImage.writeTo(zos);
                    zos.closeEntry();
                }
                // Experiment Processed Image
                if (model.hasProcessedImage()) {
                    ByteArrayOutputStream processedImage = ImageStore.saveImage(model.getProcessedImage(), 100);
                    zie = new ZipEntry(IMAGE_FOLDER + SEPARATOR + PROCESSED_IMAGE);
                    zos.putNextEntry(zie);
                    processedImage.writeTo(zos);
                    zos.closeEntry();
                }
                // Samples
                if (model.hasSamples()) {
                    // Experiment samples
                    List<Sample> samples = model.getAllSamples();
                    // For each sample
                    for (Sample sample: samples) {
                        // Source Sample Image
                        if (sample.hasSourceImage()) {
                            ByteArrayOutputStream sourceImage = ImageStore.saveImage(sample.getSourceImage(), 100);
                            zie = new ZipEntry(IMAGE_FOLDER + SAMPLE_FOLDER + sample.getId() + SEPARATOR + SOURCE_IMAGE);
                            zos.putNextEntry(zie);
                            sourceImage.writeTo(zos);
                            zos.closeEntry();
                        }
                        // Processed sample Image
                        if (sample.hasProcessedImage()) {
                            ByteArrayOutputStream processedImage = ImageStore.saveImage(sample.getProcessedImage(), 100);
                            zie = new ZipEntry(IMAGE_FOLDER + SAMPLE_FOLDER + sample.getId() + SEPARATOR + PROCESSED_IMAGE);
                            zos.putNextEntry(zie);
                            processedImage.writeTo(zos);
                            zos.closeEntry();
                        }
                        // Sample Mean
                        if (sample.hasMean()) {
                            ByteArrayOutputStream sampleMean = saveMean(sample.getMean());
                            zie = new ZipEntry(DATA_FOLDER + SAMPLE_FOLDER + sample.getId() + SEPARATOR + MEAN_FILE);
                            zos.putNextEntry(zie);
                            sampleMean.writeTo(zos);
                            zos.closeEntry();
                        }
                    }
                }
            }
            // Set Experiment saved/changed status
            model.setSaved(true);
            model.setFile(file);
            // Experiment was correctly saved to zip file
            return true;
        } catch (TransformerException | IOException | ParserConfigurationException ex) {
            LOG.error("Error saving project zip file : " + file, ex);
        }
        // Experiment can't be correctly saved
        return false;
    }
    
    /**
     * Generate Experiment (and Samples) XML data
     * @param model Experiment to save
     * @return ByteArrayOutputStream
     * @throws TransformerException
     * @throws ParserConfigurationException 
     */
    private static ByteArrayOutputStream generateXML(Experiment model) throws TransformerException, ParserConfigurationException{
        // Document Builder
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        // Root Experiment
        Document doc = docBuilder.newDocument();
        Element experiment = doc.createElement("experiment");
        doc.appendChild(experiment);
        // Experiment Name
        Attr attr = doc.createAttribute("name");
        attr.setValue(model.getName());
        experiment.setAttributeNode(attr);
        // Experiment Description
        attr = doc.createAttribute("description");
        attr.setValue(model.getDescription());
        experiment.setAttributeNode(attr);
        // Dates
        DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss ZZZ yyyy", Locale.ROOT);
        // Experiments Date
        Element date = doc.createElement("dates");
        experiment.appendChild(date);
        // Sample experiment Date
        attr = doc.createAttribute("sample-date");
        attr.setValue(df.format(model.getSampleDate()));
        date.setAttributeNode(attr);
        // Sample Analysis Date
        attr = doc.createAttribute("analysis-date");
        attr.setValue(df.format(model.getAnalysisDate()));
        date.setAttributeNode(attr);
        // Experiment Images
        if (model.hasSourceImage() || model.hasProcessedImage()) {
            Element images = doc.createElement("images");
            experiment.appendChild(images);
            // Source Image Path
            if (model.hasSourceImage()) {
                attr = doc.createAttribute("source-image");
                attr.setValue(IMAGE_FOLDER + SEPARATOR + SOURCE_IMAGE);
                images.setAttributeNode(attr);
            }
            // Processed Image Path
            if (model.hasProcessedImage()) {
                attr = doc.createAttribute("processed-image");
                attr.setValue(IMAGE_FOLDER + SEPARATOR + PROCESSED_IMAGE);
                images.setAttributeNode(attr);
            }
        }
        // Experiments Parameters
        Element parameters = doc.createElement("parameters");
        experiment.appendChild(parameters);
        // Rotation Angle
        attr = doc.createAttribute("rotation-angle");
        attr.setValue(model.getRotationAngle().toString());
        parameters.setAttributeNode(attr);
        // Rotation Flip
        attr = doc.createAttribute("rotation-flip");
        attr.setValue(model.getFlipAxis().name());
        parameters.setAttributeNode(attr);
        // Cut Points
        if (model.hasCutPoints()) {
            Pair<Point,Point> points = model.getCutPoints();
            Element cutPoints = doc.createElement("cut-points");
            parameters.appendChild(cutPoints);
            // upper x
            attr = doc.createAttribute("upper-x");
            attr.setValue(String.valueOf(points.getFirst().getX()));
            cutPoints.setAttributeNode(attr);
            // upper y
            attr = doc.createAttribute("upper-y");
            attr.setValue(String.valueOf(points.getFirst().getY()));
            cutPoints.setAttributeNode(attr);
            // lower x
            attr = doc.createAttribute("lower-x");
            attr.setValue(String.valueOf(points.getSecond().getX()));
            cutPoints.setAttributeNode(attr);
            // lower y
            attr = doc.createAttribute("lower-y");
            attr.setValue(String.valueOf(points.getSecond().getY()));
            cutPoints.setAttributeNode(attr);
        }
        // Comments
        Element comments = doc.createElement("step-comments");
        parameters.appendChild(comments);
        // Source-Comments
        attr = doc.createAttribute("source");
        attr.setValue(model.getSourceImageComments());
        comments.setAttributeNode(attr);
        // Cut-Comments
        attr = doc.createAttribute("cut");
        attr.setValue(model.getCutComments());
        comments.setAttributeNode(attr);
        // Rotation-Comments
        attr = doc.createAttribute("rotation");
        attr.setValue(model.getRotationComments());
        comments.setAttributeNode(attr);
        // Split-Comments
        attr = doc.createAttribute("split");
        attr.setValue(model.getSplitComments());
        comments.setAttributeNode(attr);
        // Data-Comments
        attr = doc.createAttribute("data");
        attr.setValue(model.getDataComments());
        comments.setAttributeNode(attr);
        // Comparation-Comments
        attr = doc.createAttribute("comparation");
        attr.setValue(model.getComparationComments());
        comments.setAttributeNode(attr);
        // Samples
        if (model.hasSamples()) {
            List<Sample> samples = model.getAllSamples();
            Element esamples = doc.createElement("samples");
            experiment.appendChild(esamples);
            // Length
            attr = doc.createAttribute("length");
            attr.setValue(String.valueOf(samples.size()));
            esamples.setAttributeNode(attr);
            // Sample
            for (Sample s: samples) {
                // Sample element
                Element sample = doc.createElement("sample");
                esamples.appendChild(sample);
                // Sample Id
                attr = doc.createAttribute("id");
                attr.setValue(String.valueOf(s.getId()));
                sample.setAttributeNode(attr);
                // Sample Images
                if (s.hasSourceImage() || s.hasProcessedImage()) {
                    Element simages = doc.createElement("images");
                    sample.appendChild(simages);
                    // Source Image Path
                    if (s.hasSourceImage()) {
                        attr = doc.createAttribute("source-image");
                        attr.setValue(IMAGE_FOLDER + SAMPLE_FOLDER + s.getId() + SEPARATOR + SOURCE_IMAGE);
                        simages.setAttributeNode(attr);
                    }
                    // Processed Image Path
                    if (s.hasProcessedImage()) {
                        attr = doc.createAttribute("processed-image");
                        attr.setValue(IMAGE_FOLDER + SAMPLE_FOLDER + s.getId() + SEPARATOR + PROCESSED_IMAGE);
                        simages.setAttributeNode(attr);
                    }
                }
                // Parameters
                Element sampleParams = doc.createElement("parameters");
                sample.appendChild(sampleParams);
                // Sample Name
                attr = doc.createAttribute("name");
                attr.setValue(s.getName());
                sampleParams.setAttributeNode(attr);
                // Sample front point
                attr = doc.createAttribute("front-point");
                attr.setValue(String.valueOf(s.getFrontPoint()));
                sampleParams.setAttributeNode(attr);
                // Sample seed point
                attr = doc.createAttribute("seed-point");
                attr.setValue(String.valueOf(s.getSeedPoint()));
                sampleParams.setAttributeNode(attr);
                // Sample limits
                Point limits = s.getLimits();
                // First Value
                attr = doc.createAttribute("start");
                attr.setValue(String.valueOf(limits.getX()));
                sampleParams.setAttributeNode(attr);
                // Second Value
                attr = doc.createAttribute("end");
                attr.setValue(String.valueOf(limits.getY()));
                sampleParams.setAttributeNode(attr);
                // Sample total area
                if (s.getTotalSurface() != null) {
                    attr = doc.createAttribute("area");
                    attr.setValue(String.valueOf(s.getTotalSurface()));
                    sampleParams.setAttributeNode(attr);
                }
                // Sample linked to other
                attr = doc.createAttribute("linked");
                attr.setValue(String.valueOf(s.isLinked()));
                sampleParams.setAttributeNode(attr);
                // Sample comments
                Element sampleComments = doc.createElement("comments");
                sample.appendChild(sampleComments);
                // Source comments
                attr = doc.createAttribute("sample");
                attr.setValue(s.getComments());
                sampleComments.setAttributeNode(attr);
                // Analysis comments
                attr = doc.createAttribute("analysis");
                attr.setValue(s.getAnalysisComments());
                sampleComments.setAttributeNode(attr);
                // Analysis Results comments
                attr = doc.createAttribute("results");
                attr.setValue(s.getResultsComments());
                sampleComments.setAttributeNode(attr);
                // Sample mean
                if (s.hasMean()) {
                    // Sample mean
                    Element sampleMean = doc.createElement("mean");
                    sample.appendChild(sampleMean);
                    // Analysis comments
                    attr = doc.createAttribute("file");
                    attr.setValue(DATA_FOLDER + SAMPLE_FOLDER + s.getId() + SEPARATOR + MEAN_FILE);
                    sampleMean.setAttributeNode(attr);
                }
                // Peek Points
                if (s.hasPeaks()) {
                    List<Peak> speaks = s.getPeaks();
                    // Sample peaks
                    Element samplePeaks = doc.createElement("peaks");
                    sample.appendChild(samplePeaks);
                    // Peaks Number
                    attr = doc.createAttribute("length");
                    attr.setValue(String.valueOf(speaks.size()));
                    samplePeaks.setAttributeNode(attr);
                    // Values
                    for (Peak p: speaks) {
                        // Peak
                        Element peak = doc.createElement("peak");
                        samplePeaks.appendChild(peak);
                        // Peak id
                        attr = doc.createAttribute("id");
                        attr.setValue(String.valueOf(p.getId()));
                        peak.setAttributeNode(attr);
                        // Peak parameters
                        Element peakParameters = doc.createElement("parameters");
                        peak.appendChild(peakParameters);
                        Pair<Float,Float> plimits = p.getLimits();
                        // start point
                        attr = doc.createAttribute("start");
                        attr.setValue(String.valueOf(plimits.getFirst()));
                        peakParameters.setAttributeNode(attr);
                        // end point
                        attr = doc.createAttribute("end");
                        attr.setValue(String.valueOf(plimits.getSecond()));
                        peakParameters.setAttributeNode(attr);
                        // name
                        attr = doc.createAttribute("name");
                        attr.setValue(p.getName());
                        peakParameters.setAttributeNode(attr);
                        // position
                        attr = doc.createAttribute("position");
                        attr.setValue(String.valueOf(p.getPosition()));
                        peakParameters.setAttributeNode(attr);
                        // Baseline Points
                        if (p.hasBaseline()) {
                            List<Pair<Float,Float>> baseline = p.getBaseline();
                            // Sample baseline
                            Element sampleBaseline = doc.createElement("baseline");
                            peak.appendChild(sampleBaseline);
                            // Points Number
                            attr = doc.createAttribute("length");
                            attr.setValue(String.valueOf(baseline.size()));
                            sampleBaseline.setAttributeNode(attr);
                            // Values
                            for (Pair<Float,Float> pbase: baseline) {
                                // Point
                                Element pair = doc.createElement("line");
                                sampleBaseline.appendChild(pair);
                                // start point
                                attr = doc.createAttribute("start");
                                attr.setValue(String.valueOf(pbase.getFirst()));
                                pair.setAttributeNode(attr);
                                // end point
                                attr = doc.createAttribute("end");
                                attr.setValue(String.valueOf(pbase.getSecond()));
                                pair.setAttributeNode(attr);
                            }
                        }
                        // Peaks surfaces absoultes and relatives
                        if (p.hasSurface() && p.hasRelativeSurface()) {
                            Float surface = p.getSurface();
                            Float relativeSurface = p.getRelativeSurface();
                            // absolute area
                            attr = doc.createAttribute("area");
                            attr.setValue(String.valueOf(surface));
                            peakParameters.setAttributeNode(attr);
                            // relative area
                            attr = doc.createAttribute("relative-area");
                            attr.setValue(String.valueOf(relativeSurface));
                            peakParameters.setAttributeNode(attr);
                        }
                        // Peaks Maximum
                        if (p.hasMaximum()) {
                            Pair<Float, Float> maximum = p.getMaximum();
                            // Sample local maximums
                            Element peakMaximum = doc.createElement("maximum");
                            peak.appendChild(peakMaximum);
                            // position
                            attr = doc.createAttribute("position");
                            attr.setValue(String.valueOf(maximum.getFirst()));
                            peakMaximum.setAttributeNode(attr);
                            // value
                            attr = doc.createAttribute("value");
                            attr.setValue(String.valueOf(maximum.getSecond()));
                            peakMaximum.setAttributeNode(attr);
                        }
                        // Peaks Height
                        if (p.hasHeight()) {
                            Pair<Float, Float> height = p.getHeight();
                            // Sample local maximums
                            Element peakHeight = doc.createElement("height");
                            peak.appendChild(peakHeight);
                            // position
                            attr = doc.createAttribute("position");
                            attr.setValue(String.valueOf(height.getFirst()));
                            peakHeight.setAttributeNode(attr);
                            // value
                            attr = doc.createAttribute("value");
                            attr.setValue(String.valueOf(height.getSecond()));
                            peakHeight.setAttributeNode(attr);
                        }
                    }
                }
            }
        }
        // Write the content into xml OutputStream
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        //
        DOMSource source = new DOMSource(doc);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(stream);
        transformer.transform(source, result);
        //
        return stream;
    }
    
    /**
     * Save Mean as text to OutputStream
     * @param mean Sample mean to save
     * @return ByteArrayOutputStream
     * @throws IOException 
     */
    private static ByteArrayOutputStream saveMean(List<Pair<Float, Float>> mean) throws IOException {
        ByteArrayOutputStream ops = new ByteArrayOutputStream();
        for (Pair<Float,Float> p: mean) {
            String line = "";
            // X
            line += String.valueOf(p.getFirst());
            line += "\t";
            // Y
            line += String.valueOf(p.getSecond());
            line += "\n";
            // Write
            ops.write(line.getBytes()); 
        }
        ops.close();
        return ops;
    }
    
    /**
     * Save Mean as text to file
     * @param file file to write
     * @param mean sample mean to save
     * @return saved or error
     */
    public static boolean saveMean(File file, List<Pair<Float, Float>> mean) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            ByteArrayOutputStream outs = saveMean(mean);
            fos.write(outs.toByteArray());
            return true;            
        } catch(IOException e) {
            return false;
        }
    }
    
    /**
     * Save sample data to a plain text file.
     * @param file file to write
     * @param sample sample data to save
     * @return saved or error
     */
    public static boolean saveSampleData(File file, Sample sample) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            // name
            pw.println(Assets.getString("PARAM_SAMPLE", sample.getName()));
            // limits
            if (sample.hasLimits()) pw.println("  " + Assets.getString("PARAM_LIMITS", sample.getLimits().toString()));
            // seed point
            pw.println("  " + Assets.getString("PARAM_SEED_POINT", sample.getSeedPoint()));
            // front point
            pw.println("  " + Assets.getString("PARAM_FRONT_POINT", sample.getFrontPoint()));
            // comments
            pw.println("  " + Assets.getString("PARAM_COMMENTS", sample.getComments()));
            // analysis comments
            pw.println("  " + Assets.getString("PARAM_ANALYSIS_COMMENTS", sample.getAnalysisComments()));
            // results comments
            pw.println("  " + Assets.getString("PARAM_RESULTS_COMMENTS", sample.getResultsComments()));
            // total surface
            if (sample.hasTotalSurface()) pw.println("  " + Assets.getString("PARAM_TOTAL_SURFACE", sample.getTotalSurface()));
            // Peaks
            if (sample.hasPeaks()) {
                for (Peak peak: sample.getPeaks()) {
                    // name
                    pw.println("\n  " + Assets.getString("PARAM_PEAK", peak.getName()));
                    // number
                    pw.println("    " + Assets.getString("PARAM_NUMBER", peak.getPosition()));
                    // Limit
                    if (peak.hasLimits()) pw.println("    " + Assets.getString("PARAM_LIMITS", peak.getLimits().toString()));
                    // Height
                    if (peak.hasHeight()) pw.println("    " + Assets.getString("PARAM_HEIGHT", peak.getHeight().getSecond(), peak.getHeight().getFirst()));
                    // Maximum
                    if (peak.hasMaximum()) pw.println("    " + Assets.getString("PARAM_MAXIMUM", peak.getMaximum().getSecond(), peak.getMaximum().getFirst()));
                    // Absolute surface
                    if (peak.hasSurface()) pw.println("    " + Assets.getString("PARAM_ABSOLUTE_SURFACE", peak.getSurface()));
                    // Realtive surface
                    if (peak.hasRelativeSurface()) pw.println("    " + Assets.getString("PARAM_RELATIVE_SURFACE", peak.getRelativeSurface()));
                    // Baseline
                    if (peak.hasBaseline()) pw.println("    " + Assets.getString("PARAM_BASELINE", peak.getBaseline().toString()));
                }
            }
            return true;
        } catch(IOException e) {
            return false;
        }
    }
    
    /**
     * Save experiment data to a plain text file.
     * @param file file to write
     * @param experiment data to save
     * @return saved or error
     */
    public static boolean saveExperimentData(File file, Experiment experiment) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            // name
            pw.println(Assets.getString("PARAM_EXPERIMENT", experiment.getName()));
            // sample date
            pw.println("  " + Assets.getString("PARAM_SAMPLE_DATE", experiment.getSampleDate()));
            // analysis date
            pw.println("  " + Assets.getString("PARAM_ANALYSIS_DATE", experiment.getAnalysisDate()));
            // description
            pw.println("  " + Assets.getString("PARAM_DESCRIPTION", experiment.getDescription()));
            // source image comments
            pw.println("  " + Assets.getString("PARAM_SOURCE_COMMENTS", experiment.getSourceImageComments()));
            // cut points
            if (experiment.hasCutPoints()) pw.println("  " + Assets.getString("PARAM_CUT_POINT", experiment.getCutPoints().toString()));
            // cut comments
            if (experiment.hasCutPoints()) pw.println("  " + Assets.getString("PARAM_CUT_COMMENTS", experiment.getCutComments()));
            // rotation angle
            pw.println("  " + Assets.getString("PARAM_ROTATION_ANGLE", experiment.getRotationAngle()));
            // flip axis
            pw.println("  " + Assets.getString("PARAM_FLIP_AXIS", experiment.getFlipAxis()));
            // rotation comments
            pw.println("  " + Assets.getString("PARAM_ROTATION_COMMENTS", experiment.getRotationComments()));
            // sample split comments
            pw.println("  " + Assets.getString("PARAM_SPLIT_COMMENTS", experiment.getSplitComments()));
            // sample data commetens
            pw.println("  " + Assets.getString("PARAM_DATA_COMMENTS", experiment.getDataComments()));
            // samples mean's comparation comments
            pw.println("  " + Assets.getString("PARAM_COMPARATION_COMMENTS", experiment.getComparationComments()));
            return true;
        } catch(IOException e) {
            return false;
        }
    }
}