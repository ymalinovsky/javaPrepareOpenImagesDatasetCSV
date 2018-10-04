import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.util.List;
import java.util.Map;

public class LabelImgXMLs {

    private Main main;

    LabelImgXMLs(Main main) {
        this.main = main;
    }

    void prepareLabelImgXMLs(List<Map<String, String>> labelXMLsData) {

        for (Map<String, String> labelXMLData : labelXMLsData) {
            String filename = FilenameUtils.getBaseName(labelXMLData.get("filename"));
            String width = labelXMLData.get("width");
            String height = labelXMLData.get("height");
            String classDescription = labelXMLData.get("class");
            String xmin = labelXMLData.get("xmin");
            String xmax = labelXMLData.get("xmax");
            String ymin = labelXMLData.get("ymin");
            String ymax = labelXMLData.get("ymax");

            if (filename != null && width != null && height != null && classDescription != null && xmin != null && xmax != null && ymin != null && ymax != null) {
                String annotationsFilePath = main.PATH_TO_CSV_FILES_FOLDER + "annotations/" + filename + ".xml";
                File file = new File(annotationsFilePath);

                if (!file.exists() && !file.isDirectory()) {
                    createXMLFile(filename, width, height, classDescription, xmin, xmax, ymin, ymax, annotationsFilePath);
                } else {
                    modifyXMLFile(classDescription, xmin, xmax, ymin, ymax, annotationsFilePath);
                }
            }
        }
    }

    void createXMLFile(String filename, String width, String height, String classDescription, String xmin, String xmax, String ymin, String ymax, String annotationsFilePath) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            // annotation
            Element rootElement = doc.createElement("annotation");

            doc.appendChild(rootElement);

            // folder
            Element folderElement = doc.createElement("folder");
            folderElement.appendChild(doc.createTextNode("images"));
            rootElement.appendChild(folderElement);

            // filename
            Element filenameElement = doc.createElement("filename");
            filenameElement.appendChild(doc.createTextNode(filename));
            rootElement.appendChild(filenameElement);

            // source
            Element sourceElement = doc.createElement("source");
            Element sourceDatabaseElement = doc.createElement("database");
            sourceDatabaseElement.appendChild(doc.createTextNode("Unknown"));
            sourceElement.appendChild(sourceDatabaseElement);
            rootElement.appendChild(sourceElement);

            // size
            Element sizeElement = doc.createElement("size");
            Element widthElement = doc.createElement("width");
            widthElement.appendChild(doc.createTextNode(width));
            Element heightElement = doc.createElement("height");
            heightElement.appendChild(doc.createTextNode(height));
            Element depthElement = doc.createElement("depth");
            depthElement.appendChild(doc.createTextNode("3"));
            sizeElement.appendChild(widthElement);
            sizeElement.appendChild(heightElement);
            sizeElement.appendChild(depthElement);
            rootElement.appendChild(sizeElement);

            // segmented
            Element segmentedElement = doc.createElement("segmented");
            segmentedElement.appendChild(doc.createTextNode("0"));
            rootElement.appendChild(segmentedElement);

            // object
            Element objectElement = doc.createElement("object");
            Element nameElement = doc.createElement("name");
            nameElement.appendChild(doc.createTextNode(classDescription));
            Element poseElement = doc.createElement("pose");
            poseElement.appendChild(doc.createTextNode("Unspecified"));
            Element truncatedElement = doc.createElement("truncated");
            truncatedElement.appendChild(doc.createTextNode("0"));
            Element difficultElement = doc.createElement("difficult");
            difficultElement.appendChild(doc.createTextNode("0"));
            Element bndboxElement = doc.createElement("bndbox");
            Element xminElement = doc.createElement("xmin");
            xminElement.appendChild(doc.createTextNode(xmin));
            Element yminElement = doc.createElement("ymin");
            yminElement.appendChild(doc.createTextNode(ymin));
            Element xmaxElement = doc.createElement("xmax");
            xmaxElement.appendChild(doc.createTextNode(xmax));
            Element ymaxElement = doc.createElement("ymax");
            ymaxElement.appendChild(doc.createTextNode(ymax));
            bndboxElement.appendChild(xminElement);
            bndboxElement.appendChild(yminElement);
            bndboxElement.appendChild(xmaxElement);
            bndboxElement.appendChild(ymaxElement);
            objectElement.appendChild(nameElement);
            objectElement.appendChild(poseElement);
            objectElement.appendChild(truncatedElement);
            objectElement.appendChild(difficultElement);
            objectElement.appendChild(bndboxElement);
            rootElement.appendChild(objectElement);

            // write content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(annotationsFilePath));
            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void modifyXMLFile(String classDescription, String xmin, String xmax, String ymin, String ymax, String annotationsFilePath) {
        try {
            File inputFile = new File(annotationsFilePath);
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(inputFile);
            Node rootElement = doc.getFirstChild();

            // object
            Element objectElement = doc.createElement("object");
            Element nameElement = doc.createElement("name");
            nameElement.appendChild(doc.createTextNode(classDescription));
            Element poseElement = doc.createElement("pose");
            poseElement.appendChild(doc.createTextNode("Unspecified"));
            Element truncatedElement = doc.createElement("truncated");
            truncatedElement.appendChild(doc.createTextNode("0"));
            Element difficultElement = doc.createElement("difficult");
            difficultElement.appendChild(doc.createTextNode("0"));
            Element bndboxElement = doc.createElement("bndbox");
            Element xminElement = doc.createElement("xmin");
            xminElement.appendChild(doc.createTextNode(xmin));
            Element yminElement = doc.createElement("ymin");
            yminElement.appendChild(doc.createTextNode(ymin));
            Element xmaxElement = doc.createElement("xmax");
            xmaxElement.appendChild(doc.createTextNode(xmax));
            Element ymaxElement = doc.createElement("ymax");
            ymaxElement.appendChild(doc.createTextNode(ymax));
            bndboxElement.appendChild(xminElement);
            bndboxElement.appendChild(yminElement);
            bndboxElement.appendChild(xmaxElement);
            bndboxElement.appendChild(ymaxElement);
            objectElement.appendChild(nameElement);
            objectElement.appendChild(poseElement);
            objectElement.appendChild(truncatedElement);
            objectElement.appendChild(difficultElement);
            objectElement.appendChild(bndboxElement);
            rootElement.appendChild(objectElement);

            // write content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(annotationsFilePath));
            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
