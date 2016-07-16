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


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//
import jtlc.core.processing.AnalysisProcessing.Axis;
import jtlc.main.common.Pair;
import jtlc.main.common.Point;
import jtlc.core.model.Experiment;
import jtlc.core.model.Peak;
import jtlc.core.model.Sample;

/**
 * Experiment Loader.
 * Implements project/experiment loader from saved zip file.
 * 
 * @author Baldani Sergio - Tardivo Cristian
 */
public class ModelLoader {
    // Experiment xml data file
    private static final String EXPERIMENT_FILE = "experiment.xml";
    // Error logger
    private static final Logger LOG = LoggerFactory.getLogger(ModelLoader.class);
    
    /**
     * Load All experiments in a folder
     * @param folder folder to explore
     * @return list of experiments in the folder
     */
    public static List<Experiment> loadExperiments(File folder) {
        // List files, filter by extension .jtlc
        File[] files = folder.listFiles((File file, String name) -> name.endsWith(".jtlc"));
        // Experiments list result
        List<Experiment> experiments = new LinkedList<>();
        // Load all projects in the folder
        for (File file : files)
            if (!file.isDirectory() && file.isFile())
                experiments.add(ModelLoader.loadExperiment(file));
        return experiments;
    }
    
    /**
     * Load Projects Experiment zip file
     * Load XML Projects data file
     * Load Projects Images (and samples split images)
     * Load Samples Means and other data
     * @param file file path to load
     * @return loaded experiment
     */    
    public static Experiment loadExperiment(File file) {
        // Projects ZIP
        try (ZipFile zif = new ZipFile(file, ZipFile.OPEN_READ)) {
            // Experiment XML Data
            ZipEntry zie = zif.getEntry(EXPERIMENT_FILE);
            // XML file input stream
            try (InputStream is = zif.getInputStream(zie)) {
                // Result Experiment
                Experiment model = new Experiment();
                // Document Builder-Parser
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(is);
                // Experiment Node
                Element experiment =  doc.getDocumentElement();
                // Name
                model.setName(experiment.getAttribute("name"));
                // Description
                model.setDescription(experiment.getAttribute("description"));
                // Dates
                Node dates = experiment.getElementsByTagName("dates").item(0);
                NamedNodeMap attributes = dates.getAttributes();
                DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss ZZZ yyyy", Locale.ROOT);
                // Analysis Date
                Node date = attributes.getNamedItem("analysis-date");
                model.setAnalysisDate(df.parse(date.getNodeValue()));
                // Experiment Date
                date = attributes.getNamedItem("sample-date");
                model.setSampleDate(df.parse(date.getNodeValue()));
                // Images
                NodeList images = experiment.getElementsByTagName("images");
                if (images.getLength() > 0) {
                    Node image = images.item(0);
                    NamedNodeMap imagesPath = image.getAttributes();
                    // Source Image
                    Node sourceImagePath = imagesPath.getNamedItem("source-image");
                    if (sourceImagePath != null) {
                        String imagePath = sourceImagePath.getNodeValue();
                        ZipEntry zimage = zif.getEntry(imagePath);
                        if (zimage != null) {
                            InputStream inputs = zif.getInputStream(zimage);                        
                            model.setSourceImage(ImageStore.openImage(inputs, "source-image"));
                        }
                    }
                    // Processed Image
                    Node processedImagePath = imagesPath.getNamedItem("processed-image");
                    if (processedImagePath != null) {
                        String imagePath = processedImagePath.getNodeValue();
                        ZipEntry zimage = zif.getEntry(imagePath);
                        if (zimage != null) {
                            InputStream inputs = zif.getInputStream(zimage);                        
                            model.setProcessedImage(ImageStore.openImage(inputs, "processed-image"));
                        }                        
                    }
                }
                // Parameters
                NodeList parameters = experiment.getElementsByTagName("parameters");
                if (parameters.getLength() > 0) {
                    Node param = parameters.item(0);
                    attributes = param.getAttributes();
                    // Rotation Angle
                    model.setRotationAngle(Double.valueOf(attributes.getNamedItem("rotation-angle").getNodeValue()));
                    // Flip Axis
                    String savedFlip = attributes.getNamedItem("rotation-flip").getNodeValue();
                    Axis flip = Axis.valueOf(savedFlip);
                    model.setFlipAxis(flip);
                    // Data
                    Node other = parameters.item(0);                    
                    List<Node> parametersNodes = filterNodes(other.getChildNodes(), Node.ELEMENT_NODE);
                    for (Node item : parametersNodes) {
                        // Cut-Points
                        if (item.getNodeName().equals("cut-points")) {
                            attributes = item.getAttributes();
                            int lx = Integer.valueOf(attributes.getNamedItem("lower-x").getNodeValue());
                            int ly = Integer.valueOf(attributes.getNamedItem("lower-y").getNodeValue());
                            int ux = Integer.valueOf(attributes.getNamedItem("upper-x").getNodeValue());
                            int uy = Integer.valueOf(attributes.getNamedItem("upper-y").getNodeValue());
                            model.setCutPoints(new Point(ux, uy), new Point(lx, ly));
                            continue;
                        }
                        // Step-Comments
                        if (item.getNodeName().equals("step-comments")) {
                            attributes = item.getAttributes();
                            // Cut step comments
                            Node nit = attributes.getNamedItem("cut");
                            model.setCutComments((nit != null)? nit.getNodeValue() : "");
                            // Rotation step comments
                            nit = attributes.getNamedItem("rotation");
                            model.setRotationComments((nit != null)? nit.getNodeValue() : "");
                            // Source step comments
                            nit = attributes.getNamedItem("source");
                            model.setSourceImageComments((nit != null)? nit.getNodeValue() : "");
                            // Split step comments
                            nit = attributes.getNamedItem("split");
                            model.setSplitComments((nit != null)? nit.getNodeValue() : "");
                            // Data step comments
                            nit = attributes.getNamedItem("data");
                            model.setDataComments((nit != null)? nit.getNodeValue() : "");
                            // Comparation step comments
                            nit = attributes.getNamedItem("comparation");
                            model.setComparationComments((nit != null)? nit.getNodeValue() : "");
                        }
                    }
                }
                // Samples
                NodeList samples = experiment.getElementsByTagName("samples");
                if (samples.getLength() > 0) {
                    Node item = samples.item(0);
                    attributes = item.getAttributes();
                    int size = Integer.valueOf(attributes.getNamedItem("length").getNodeValue());
                    //
                    List<Node> sampleNodes = filterNodes(item.getChildNodes(), Node.ELEMENT_NODE);
                    if (sampleNodes.size() != size)
                        System.err.println("Experiment XML error: Invalid Samples List Length");
                    //
                    ArrayList<Sample> samplesList = new ArrayList<>(size);
                    //
                    for (Node node: sampleNodes) {
                        Sample sample = new Sample();
                        // Id
                        attributes = node.getAttributes();
                        sample.setId(Integer.valueOf(attributes.getNamedItem("id").getNodeValue()));
                        // Data
                        List<Node> sampleData = filterNodes(node.getChildNodes(), Node.ELEMENT_NODE);
                        for (Node dataNode : sampleData) {
                            // Images
                            if (dataNode.getNodeName().equals("images")) {
                                attributes = dataNode.getAttributes();
                                // Source Image
                                Node simage = attributes.getNamedItem("source-image");
                                if (simage != null) {
                                    String imagePath = simage.getNodeValue();
                                    ZipEntry zimage = zif.getEntry(imagePath);
                                    InputStream inputs = zif.getInputStream(zimage);
                                    sample.setSourceImage(ImageStore.openImage(inputs, "source-image"));
                                }
                                // Processed Image
                                Node pimage = attributes.getNamedItem("processed-image");
                                if (pimage != null) {
                                    String imagePath = pimage.getNodeValue();
                                    ZipEntry zimage = zif.getEntry(imagePath);
                                    InputStream inputs = zif.getInputStream(zimage);
                                    sample.setProcessedImage(ImageStore.openImage(inputs, "processed-image"));
                                }
                            }
                            // Parameters
                            if (dataNode.getNodeName().equals("parameters")) {
                                attributes = dataNode.getAttributes();
                                sample.setName(attributes.getNamedItem("name").getNodeValue());
                                sample.setFrontPoint(Integer.valueOf(attributes.getNamedItem("front-point").getNodeValue()));
                                sample.setSeedPoint(Integer.valueOf(attributes.getNamedItem("seed-point").getNodeValue()));
                                Node area = attributes.getNamedItem("area");
                                if (area != null)
                                    sample.setTotalSurface(Float.valueOf(area.getNodeValue()));
                                sample.setLinked(Boolean.valueOf(attributes.getNamedItem("linked").getNodeValue()));
                                int start = Integer.valueOf(attributes.getNamedItem("start").getNodeValue());
                                int end = Integer.valueOf(attributes.getNamedItem("end").getNodeValue());
                                sample.setLimits(new Point(start, end));
                            }
                            // Comments
                            if (dataNode.getNodeName().equals("comments")) {
                                attributes = dataNode.getAttributes();
                                sample.setAnalysisComments(attributes.getNamedItem("analysis").getNodeValue());
                                sample.setComments(attributes.getNamedItem("sample").getNodeValue());
                                sample.setResultsComments(attributes.getNamedItem("results").getNodeValue());
                            }
                            // Mean
                            if (dataNode.getNodeName().equals("mean")) {
                                attributes = dataNode.getAttributes();
                                String fpath = attributes.getNamedItem("file").getNodeValue();
                                ZipEntry zfile = zif.getEntry(fpath);
                                InputStream inputs = zif.getInputStream(zfile);
                                sample.setMean(loadMean(inputs));
                            }
                            // Peaks
                            if (dataNode.getNodeName().equals("peaks")) {
                                attributes = dataNode.getAttributes();
                                size = Integer.valueOf(attributes.getNamedItem("length").getNodeValue());
                                List<Node> peaksNodes = filterNodes(dataNode.getChildNodes(), Node.ELEMENT_NODE);
                                if (peaksNodes.size() != size)
                                    System.err.println("Experiment XML error: Invalid Peaks List Length");
                                List<Peak> peekList = new ArrayList<>(size);
                                for (Node peakNode: peaksNodes) {
                                    if (!peakNode.getNodeName().equals("peak")) {
                                        System.err.println("Experiment XML error: Invalid Peek point node");
                                        break;
                                    }
                                    Peak peak = new Peak();
                                    // Peak id
                                    attributes = peakNode.getAttributes();
                                    int id = Integer.valueOf(attributes.getNamedItem("id").getNodeValue());
                                    peak.setId(id);
                                    // Peak Data
                                    List<Node> peakData = filterNodes(peakNode.getChildNodes(), Node.ELEMENT_NODE);
                                    for (Node pdata: peakData) {
                                        // Peak Parameters
                                        if (pdata.getNodeName().equals("parameters")) {
                                            attributes = pdata.getAttributes();
                                            // Peak name
                                            peak.setName(attributes.getNamedItem("name").getNodeValue());
                                            // Peak start point
                                            float start = Float.valueOf(attributes.getNamedItem("start").getNodeValue());
                                            float end = Float.valueOf(attributes.getNamedItem("end").getNodeValue());
                                            peak.setLimits(new Pair<>(start, end));
                                            // Peak area
                                            Node area = attributes.getNamedItem("area");
                                            if (area != null)
                                                peak.setSurface(Float.valueOf(area.getNodeValue()));
                                            // Peak relative area
                                            Node relative = attributes.getNamedItem("relative-area");
                                            if (relative != null)
                                                peak.setRelativeSurface(Float.valueOf(relative.getNodeValue()));
                                            // Peak position
                                            peak.setPosition(Integer.valueOf(attributes.getNamedItem("position").getNodeValue()));
                                        }
                                        // Peak Baseline
                                        if (pdata.getNodeName().equals("baseline")) {
                                            attributes = pdata.getAttributes();
                                            size = Integer.valueOf(attributes.getNamedItem("length").getNodeValue());
                                            List<Node> baselineNode = filterNodes(pdata.getChildNodes(), Node.ELEMENT_NODE);
                                            if (baselineNode.size() != size)
                                                System.err.println("Experiment XML error: Invalid Baseline List Length");
                                            List<Pair<Float,Float>> baselineList = new ArrayList<>(size);
                                            for (Node line: baselineNode) {
                                                if (!line.getNodeName().equals("line"))
                                                    System.err.println("Experiment XML error: Invalid baseline line node");
                                                else {
                                                    attributes = line.getAttributes();
                                                    float start = Float.valueOf(attributes.getNamedItem("start").getNodeValue());
                                                    float end = Float.valueOf(attributes.getNamedItem("end").getNodeValue());
                                                    baselineList.add(new Pair<>(start, end));
                                                }
                                            }
                                            peak.setBaseline(baselineList);
                                        }
                                        // Peak Maximum
                                        if (pdata.getNodeName().equals("maximum")) {
                                            attributes = pdata.getAttributes();
                                            float position = Float.valueOf(attributes.getNamedItem("position").getNodeValue());
                                            float value = Float.valueOf(attributes.getNamedItem("value").getNodeValue());
                                            // Save peak maximum
                                            peak.setMaximum(new Pair<>(position, value));
                                        }
                                        // Peak Height
                                        if (pdata.getNodeName().equals("height")) {
                                            attributes = pdata.getAttributes();
                                            float position = Float.valueOf(attributes.getNamedItem("position").getNodeValue());
                                            float value = Float.valueOf(attributes.getNamedItem("value").getNodeValue());
                                            // Save peak height
                                            peak.setHeight(new Pair<>(position, value));
                                        }
                                    }
                                    // Save loaded peak
                                    peekList.add(peak);
                                }
                                sample.setPeaks(peekList);
                            }
                        }
                        // Add Sample
                        samplesList.add(sample.getId(), sample);
                    }
                    // Add all samples
                    model.setSamples(samplesList);
                }
                // Experiment file/save status
                model.setSaved(true);
                model.setFile(file);
                // return loaded experiment
                return model;
            }
        } catch (NullPointerException | IOException | SAXException | ParserConfigurationException ex) {
            LOG.error("Error reading project zip file : " + file, ex);
        } catch (ParseException ex) {
            LOG.error("Error parsing Date", ex);
        }
        // experiment can't be correctly loaded
        return null;
    }
    
    /**
     * Filter NodeList by node type
     * @param nodes NodeList to filter
     * @param typee filtered NodeType
     * @return List of filtered nodes
     */
    private static List<Node> filterNodes(NodeList nodes, int type) {
        List<Node> result = new LinkedList<>();
        for (Node node = nodes.item(0); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() == type)
                result.add(node);
        }
        return result;    
    }
    
    /**
     * Read Mean float pair list from text/file InputStream
     * @param input InputStream mean text "file" to read
     * @return Sample "Mean" pair list
     * @throws IOException 
     */
    private static List<Pair<Float, Float>> loadMean(InputStream input) throws IOException {
        List<Pair<Float,Float>> result = new LinkedList<>();
        // Read Lines, try-auto closeable.
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(input))) {
                for (String line = rd.readLine(); line != null; line = rd.readLine()) {
                    // Split line
                    String[] split = line.split("\\t");
                    // Get float values
                    float x = Float.valueOf(split[0]);
                    float y = Float.valueOf(split[1]);
                    // Save pair
                    result.add(new Pair<>(x,y));            
                }
            }
        // Return results
        return result;
    }
}