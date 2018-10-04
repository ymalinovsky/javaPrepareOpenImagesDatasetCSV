import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.File;

public class LabelImgXMLs {

    private Main main;

    LabelImgXMLs(Main main) {
        this.main = main;
    }

//<cars>
//   <supercars company = "Ferrari">
//      <carname type = "formula one">Ferrari 101</carname>
//      <carname type = "sports">Ferrari 202</carname>
//   </supercars>
//</cars>

    void prepareLabelImgXMLs(String filename) {
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

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("C:\\cars.xml"));
            transformer.transform(source, result);

            // Output to console for testing
            StreamResult consoleResult = new StreamResult(System.out);
            transformer.transform(source, consoleResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
