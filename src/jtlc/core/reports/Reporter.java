/*
 * Copyright (C) 2015 Cristian Tardivo
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
package jtlc.core.reports;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
//
import com.alee.utils.FileUtils;
import com.lowagie.text.pdf.PdfWriter;
import ij.ImagePlus;
import java.io.FileWriter;
import java.io.PrintWriter;
//
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jopendocument.dom.ODSingleXMLDocument;
import org.jopendocument.dom.template.TemplateException;
import org.odftoolkit.odfdom.converter.core.BasicURIResolver;
import org.odftoolkit.odfdom.converter.pdf.PdfConverter;
import org.odftoolkit.odfdom.converter.pdf.PdfOptions;
import org.odftoolkit.odfdom.converter.xhtml.XHTMLConverter;
import org.odftoolkit.odfdom.converter.xhtml.XHTMLOptions;
import org.odftoolkit.odfdom.doc.OdfTextDocument;
//
import jtlc.assets.Assets;
import jtlc.main.common.Pair;
import jtlc.main.common.Triplet;
import jtlc.core.model.Experiment;
import jtlc.core.model.Peak;
import jtlc.core.model.Sample;
import jtlc.core.storage.ImageStore;
import jtlc.view.components.Plotter;


/**
 * Experimemnts and Samples Reports generator.
 * This class implements all functions needed to generate
 * reports for experiments and samples. Can generate ODT
 * report, and from odt model generate PDF and HTML reports.
 * 
 * @author Cristian Tardivo
 */
public class Reporter {
    
    // Image folder inside document package
    private static final String IMAGE_FOLDER = "Pictures/";
    
    /**
     * Create document report for a experiment
     * @param experiment Experiment to report
     * @param withSamples add samples report to main report
     * @return document
     */
    public ODSingleXMLDocument createExperimentReport(Experiment experiment, boolean withSamples) {
        try {
            // Date format according to current locale
            final DateFormat df = new SimpleDateFormat(Assets.getString("DATE_FORMAT"));
            // Load the template. (using assets manager)
            Template template = new Template(Assets.getString("EXPERIMENT_TEMPLATE"));
            // fill experiment name
            template.setField("exp_name", experiment.getName());
            // fill experiment description
            template.setField("exp_description", experiment.hasDescription()? experiment.getDescription() : Assets.getString("NO_DESCRIPTION"));
            // fill experiment creation date
            template.setField("creation_date", df.format(experiment.getSampleDate()));
            // fill experiment analysis date
            template.setField("analysis_date", df.format(experiment.getAnalysisDate()));
            // fill experiment source image comments
            template.setField("exp_image_comments", experiment.hasSourceImageComments()? experiment.getSourceImageComments() : Assets.getString("NO_COMMENTS"));
            // fill experiment cut step comments
            template.setField("cut_comments", experiment.hasCutComments()? experiment.getCutComments() : Assets.getString("NO_COMMENTS"));
            // fill experiment cut step points
            template.setField("cut_points", experiment.hasCutPoints()? Assets.getString("UPPER_LOWER", experiment.getCutPoints().getFirst(), experiment.getCutPoints().getSecond()) : Assets.getString("NO_DATA"));
            // fill experiment rotation step comments
            template.setField("rotation_comments", experiment.hasRotationComments()? experiment.getRotationComments() : Assets.getString("NO_COMMENTS"));
            // fill experiment rotation step angle
            template.setField("rotation_angle", experiment.getRotationAngle());
            // fill experiment rotation step flip axis
            template.setField("flip_axis", experiment.getFlipAxis());
            // fill experiment samples split step comments
            template.setField("split_comments", experiment.hasSplitComments()? experiment.getSplitComments() : Assets.getString("NO_COMMENTS"));
            // fill experiment samples split points
            if (experiment.hasSamples()) {
                String points = experiment.getAllSamples().stream().map(s -> s.getLimits().toString()).collect(Collectors.joining(" "));
                template.setField("split_points", points);
            }
            // fill experiment samples split step comments
            template.setField("data_comments", experiment.hasDataComments()? experiment.getDataComments() : Assets.getString("NO_COMMENTS"));
            
            /**
             * Insert Images to document
             */
            // Get document from template
            ODSingleXMLDocument document = template.createDocument();
            // Experiment source image
            ImagePlus img = experiment.getSourceImage();
            String path = putImage(img, "source_image", document);
            boolean success = replaceImage("exp_image", path, img.getWidth(), img.getHeight(), document);
            // Experiment processed image
            img = experiment.getProcessedImage();
            path = putImage(img, "processed_image", document);
            success &= replaceImage("analysis_image", path, img.getWidth(), img.getHeight(), document);
            // Check if image was correctly added
            if (!success) throw new TemplateException("Can't attach image file to document package");
            // Attach samples if necessary
            if (withSamples)
                for (Sample sample: experiment.getAllSamples())
                    document.add(createSampleReport(sample));            
            // Return reference to document
            return document;
        } catch (IOException | TemplateException | JDOMException ex) {
            Logger.getLogger(Reporter.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Create CSV Text report file for a experiment and his samples
     * @param experiment Experiment to report
     * @param file file to write
     * @return success or error
     */
    public boolean saveExperimentCSVReport(Experiment experiment, File file) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.print(createExperimentCSVReport(experiment));
        } catch (IOException ex) {
            System.err.println("Can't create experiment csv file " + file.toString());
            return false;
        }
        return true;
    }
    
    /**
     * Generate Experiment CSV Text report as string
     * @param experiment exmperient to process
     * @return string with csv text separeted by comman and new lines (\r\n)
     */
    public String createExperimentCSVReport(Experiment experiment) {
        StringBuilder sb = new StringBuilder();
        // Head
        sb.append(Assets.getString("PROJECT")).append(", ").append(Assets.getString("SAMPLE_DATE")).append(", ").append(Assets.getString("ANALYSIS_DATE")).append(", ").append(Assets.getString("DESCRIPTION")).append("\r\n");
        // Dates
        DateFormat df = new SimpleDateFormat(Assets.getString("DATE_FORMAT"));
        String sampleDate = df.format(experiment.getSampleDate());
        String analysisDate =  df.format(experiment.getAnalysisDate());
        // Main data
        sb.append(experiment.getName()).append(", ").append(sampleDate).append(", ").append(analysisDate).append(", ").append(experiment.getDescription()).append("\r\n");
        // Break
        sb.append("\r\n");
        // Samples
        sb.append(Assets.getString("ALL_SAMPLES")).append("\r\n");
        // Break
        sb.append("\r\n");
        // Generate CSV for all samples
        List<Sample> samples = experiment.getAllSamples();
        samples.stream().forEach((sample) -> {
            sb.append(generateSampleCSVReport(sample)).append("\r\n").append("-------").append("\r\n");
        });
        // Return csv string
        return sb.toString();
    }
    
    /**
     * Create document report for a experiment Sample
     * @param sample Sample to report
     * @return document
     */
    public ODSingleXMLDocument createSampleReport(Sample sample) {
        try {
            // Load the template. (using assets manager)
            Template template = new Template(Assets.getString("SAMPLE_TEMPLATE"));
            // Fill sample name
            template.setField("sample_name", sample.getName());
            // Fill sample comments
            template.setField("sample_comments", sample.hasComments()? sample.getComments() : Assets.getString("NO_COMMENTS"));
            // Fill sample limits
            template.setField("limits", sample.getLimits().toString());
            // Fill sample seed point
            template.setField("seed_point", sample.getSeedPoint());
            // Fill sample front point
            template.setField("front_point", sample.getFrontPoint());
            // Fill sample total surface
            template.setField("total_surface", sample.getTotalSurface());
            // Fill sample analysis comments
            template.setField("analysis_comments", sample.hasAnalysisComments()? sample.getAnalysisComments() : Assets.getString("NO_COMMENTS"));
            // Fill sample results comments
            template.setField("results_comments", sample.hasResultsComments()? sample.getResultsComments() : Assets.getString("NO_COMMENTS"));
            // Get sample peaks and create peak data map
            List<Map> peaksMap = sample.getPeaks().stream().map(peak -> createPeakMap(peak)).collect(Collectors.toList());
            template.setField("peaks", peaksMap);
            
            /**
             * Insert Images to document
             */
            // Get document from template
            ODSingleXMLDocument document = template.createDocument();
            // Sample source image
            ImagePlus img = sample.getSourceImage();
            String path = putImage(img, "source_image_sample_" + sample.getId(), document);
            boolean success = replaceImage("sample_img", path, img.getWidth(), img.getHeight(), document);
            // Sample processed image
            img = sample.getProcessedImage();
            path = putImage(img, "processed_image_sample_" + sample.getId(), document);
            success &= replaceImage("processed_image", path, img.getWidth(), img.getHeight(), document);
            // Mean and Peaks image
            List<Pair<Float, Float>> baseline = sample.getPeaks().stream().map(p -> p.getBaseline()).flatMap(Collection::stream).collect(Collectors.toList());
            List<Triplet<Float, Float, Integer>> peaksData = sample.getPeaks().stream().map(peak -> new Triplet<Float,Float,Integer>(peak.getMaximum(), peak.getPosition())).collect(Collectors.toList());
            img = new Plotter(sample.getMean(), baseline, sample.getName(), peaksData).getImagePlus(600, 400);
            path = putImage(img, "mean_sample_" + sample.getId(), document);
            success &= replaceImage("mean_image", path, img.getWidth(), img.getHeight(), document);            
            // Check if image was correctly added
            if (!success) throw new TemplateException("Can't attach image file to document package");
            // Return reference to document
            return document;
        } catch (IOException | TemplateException | JDOMException ex) {
            Logger.getLogger(Reporter.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    /**
     * Create CSV Text report file for a experiment Sample
     * @param sample Sample to report
     * @param file file to write
     * @return success or error
     */
    public boolean saveSampleCSVReport(Sample sample, File file) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.print(generateSampleCSVReport(sample));
        } catch (IOException ex) {
            System.err.println("Can't create sample csv file " + file.toString());
            return false;
        }
        return true;
    }
    
    /**
     * Generate Sample CSV Text report as string
     * @param sample sample to process
     * @return string with csv text separeted by comman and new lines (\r\n)
     */
    public String generateSampleCSVReport(Sample sample) {
        StringBuilder sb = new StringBuilder();
        // Head
        sb.append(Assets.getString("SAMPLE")).append(", ")
            .append(Assets.getString("LIMITS")).append(" (px -px), ")
            .append(Assets.getString("SEED_POINT")).append(" (px), ")
            .append(Assets.getString("FRONT_POINT")).append(" (px), ")
            .append(Assets.getString("VALUES_IN_PROCESSED_IMAGE")).append("\r\n");
        // Data
        String sampleLimits = "(" + sample.getLimits().getX() + " - " + sample.getLimits().getY() + ")";
        sb.append(sample.getName()).append(", ").append(sampleLimits).append(", ").append(sample.getSeedPoint()).append(", ").append(sample.getFrontPoint()).append("\r\n");
        // Sample peaks
        if (sample.hasPeaks()) {
            // Peaks Columns
            sb.append(Assets.getString("PEAK")).append(", ")
                .append(Assets.getString("NAME")).append(", ")
                .append(Assets.getString("LIMITS")).append(" (px -px), ")
                .append(Assets.getString("MAXIMUM")).append(", ")
                .append(Assets.getString("MAXIMUM_POSITION")).append(", ")
                .append(Assets.getString("HEIGHT_VALUE")).append(", ")
                .append(Assets.getString("HEIGHT_POSITION")).append(", ")
                .append(Assets.getString("SURFACE")).append(", ")
                .append(Assets.getString("RELATIVE")).append(" %").append(", ")
                .append(Assets.getString("BASELINE")).append("\r\n");
            // Get all peaks
            List<Peak> peaks = sample.getPeaks();
            // For each peak write data
            peaks.stream().forEach(peak -> {
                String limits = "(" + peak.getLimits().getFirst() + " - " + peak.getLimits().getSecond() + ")";
                String baseline = peak.getBaseline().stream().map(b -> ("(" + b.getFirst() + " - " + b.getSecond() + ")")).collect(Collectors.joining(" "));
                Pair<Float,Float> maximum = peak.getMaximum();
                Pair<Float,Float> height = peak.getHeight();
                sb.append(peak.getPosition()).append(", ").append(peak.getName()).append(", ").append(limits).append(", ").append(maximum.getSecond()).append(", ").append(maximum.getFirst()).append(", ").append(height.getSecond()).append(", ").append(height.getFirst()).append(", ").append(peak.getSurface()).append(", ").append(peak.getRelativeSurface()).append(", ").append(baseline).append("\r\n");
            });
            // Compute total relative surface (must be equals to 100)
            float totalRelativeSurface = (float) peaks.stream().mapToDouble(peak -> peak.getRelativeSurface()).sum();
            // Attach sum columns
            sb.append(" , , , , , , ,").append(sample.getTotalSurface()).append(", ").append(totalRelativeSurface);
        }
        // Return csv string
        return sb.toString();
    }
    
    
    /**
     * Append image to document package
     * @param img image to append
     * @param name image name (with/without extension jpg)
     * @param document document to modify
     * @return String image path
     * @throws IOException 
     */
    private String putImage(ImagePlus img, String name, ODSingleXMLDocument document) throws IOException {
        // Check name extension
        String ext = !name.endsWith(".jpg")? ".jpg" : "";
        // Get image byte array
        byte[] bytes = ImageStore.saveImage(img, 100).toByteArray();
        // Put image inside package
        document.getPackage().putFile(IMAGE_FOLDER + name + ext, bytes, "image/jpeg", false);
        // Image path
        return IMAGE_FOLDER + name + ext;
    }
    
    /**
     * Replace image in document
     * @param element referenced image element to replace
     * @param path image file path (inside the document)
     * @param document document to modify
     * @return success
     */
    private boolean replaceImage(String element, String path, long width, long height, ODSingleXMLDocument document) {
        // Get Image Frame
        Element frame = document.getDescendantByName("draw:frame", element);
        // Get Image element
        List image = frame.getChildren();
        // Set data
        if (image != null && !image.isEmpty() && image.get(0) instanceof Element) {
            // Get document image max size
            long maxWidth = toPixels(Double.valueOf((frame.getAttribute("width", Namespace.getNamespace("svg", "urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0")).getValue().split("cm"))[0]));
            long maxHeight = toPixels(Double.valueOf((frame.getAttribute("height", Namespace.getNamespace("svg", "urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0")).getValue().split("cm"))[0]));
            // Compute image proportions relation
            double wrel = ((double) height / width);
            double hrel = ((double) width / height);
            // Check max width
            if (width > maxWidth) {
                width = maxWidth;
                height = (int)(width * wrel);
            }
            // Check max height
            if (height > maxHeight) {
                height = maxHeight;
                width = (int)(height * hrel);
            }
            // Set image size
            frame.setAttribute("width", toCm(width) + "cm", Namespace.getNamespace("svg", "urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0"));
            frame.setAttribute("height", toCm(height) + "cm", Namespace.getNamespace("svg", "urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0"));
            // Set image path
            Element imgsrc = (Element)image.get(0);
            imgsrc.setAttribute("href", path, Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink"));
            return true;
        }
        return false;
    }
    
    /**
     * Conver Centimeters to Pixel
     * @param cm
     * @return 
     */
    private long toPixels(Double cm) {
        return Math.round(cm * 37.79527559055d);
    }
    
    /**
     * Conver pixels to centimeters
     * @param pixels
     * @return 
     */
    private Double toCm(long pixels) {
        return pixels / 37.79527559055d;
    }
    
    /**
     * Create data map from sample peak
     * @param peak peak to map
     * @return peak data mapped to id
     */
    private Map createPeakMap(Peak peak) {
        HashMap<String, String> map = new HashMap<>();
        map.put("name", peak.getName());
        map.put("position", String.valueOf(peak.getPosition()));
        map.put("surface", String.valueOf(peak.getSurface()));
        map.put("relative", String.valueOf(peak.getRelativeSurface()));
        map.put("limits", Assets.getString("FROM_TO", peak.getLimits().getFirst(), peak.getLimits().getSecond()));
        map.put("maximum", Assets.getString("X_AT_Y", peak.getMaximum().getSecond(), peak.getMaximum().getFirst()));
        map.put("height", Assets.getString("X_AT_Y", peak.getHeight().getSecond(), peak.getHeight().getFirst()));
        map.put("baseline", peak.getBaseline().stream().map(b -> b.toString()).collect(Collectors.joining(" ")));
        return map;       
    }
    
    /**
     * Save a ODT file from ODSingleXMLDocument
     * @param doc document to save
     * @param file file to write like odt document
     * @throws IOException 
     */
    public void saveAsODT(ODSingleXMLDocument doc, File file) throws IOException {
        doc.saveToPackageAs(file);
    }
    
    /**
     * Save a PDF file from ODSingleXMLDocument
     * @param doc document to save
     * @param file file to write with pdf data
     * @throws IOException
     * @throws Exception 
     */
    public void saveAsPDF(ODSingleXMLDocument doc, File file) throws IOException, Exception {
        // 0) Save to ODT package
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        doc.saveToPackage(bao);
        
        // 1) Load ODT into ODFDOM OdfTextDocument 
        ByteArrayInputStream bis = new ByteArrayInputStream(bao.toByteArray());
        OdfTextDocument document = OdfTextDocument.loadDocument(bis);

        // 2) Prepare Pdf options
        PdfOptions options = PdfOptions.create();
        options.setConfiguration((PdfWriter writer) -> {
            writer.setLinearPageMode();
            writer.setPageEmpty(true);
            writer.setStrictImageSequence(true);
            writer.setUserProperties(true);
            //writer.setPDFXConformance(PdfWriter.PDFA1A);
            writer.setPageEmpty(true);
        });
        
        // 3) Convert OdfTextDocument to PDF via IText
        OutputStream out = new FileOutputStream(file);
        PdfConverter.getInstance().convert(document, out, options);
    }
        
    /**
     * Save a HTML file from ODSingleXMLDocument
     * @param doc document to save
     * @param file file to write with pdf data
     * @throws FileNotFoundException
     * @throws Exception 
     */
    public void saveAsHTML(ODSingleXMLDocument doc, File file) throws FileNotFoundException, Exception{
        // 0) Save to ODT package
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        doc.saveToPackage(bao);
        
        // 1) Load ODT into ODFDOM OdfTextDocument 
        ByteArrayInputStream bis = new ByteArrayInputStream(bao.toByteArray());
        OdfTextDocument document = OdfTextDocument.loadDocument(bis);
        
        // Get images from package
        String fileName = FileUtils.getFileNamePart(file);
        String fileFolder = file.getParentFile().toString(); 
        Set<String> filePaths = document.getPackage().getFilePaths();
        for (String path: filePaths) {
            if (path.endsWith(".jpg")) {
                byte[] binaryFile = doc.getPackage().getBinaryFile(path);
                File imgFile = new File(fileFolder + File.separator + fileName + File.separator + "_content" + File.separator + path);
                imgFile.getParentFile().mkdirs();
                try (FileOutputStream fos = new FileOutputStream(imgFile)) {
                    fos.write(binaryFile);
                }
            }
        }
        
        // 2) Prepare XHTML options (here we set the IURIResolver to load images from a "Pictures" folder)
        XHTMLOptions options = XHTMLOptions.create();
        options.URIResolver(new BasicURIResolver("." + File.separator + fileName + File.separator +"_content" + File.separator));
        options.indent(2);
        
        // 3) Convert OdfTextDocument to XHTML
        OutputStream out = new FileOutputStream(file);
        XHTMLConverter.getInstance().convert(document, out, options);
    }   
}