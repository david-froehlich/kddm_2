package org.kddm2.indexing.xml;

import org.kddm2.indexing.WikiPage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by david on 6/16/17.
 */
public class WikiXmlWriter {
    String xmlOutPath;
    Element rootEle;
    DocumentBuilder db;
    Document dom;

    private void addPageToDom(WikiPage page) {
        Element pageElement, titleElement, textElement, nsElement, revisionElement;
        pageElement = dom.createElement("page");

        titleElement = dom.createElement("title");
        titleElement.setTextContent(page.getTitle());
        pageElement.appendChild(titleElement);

        nsElement = dom.createElement("ns");
        nsElement.setTextContent("0");
        pageElement.appendChild(nsElement);

        revisionElement = dom.createElement("revision");
        pageElement.appendChild(revisionElement);

        textElement = dom.createElement("text");
        textElement.setTextContent(page.getText());
        revisionElement.appendChild(textElement);

        rootEle.appendChild(pageElement);
    }

    /**
     * writes the pages into the xml-file at xmlOutPath
     * The xml format is not the same as the full Wikipedia XmlFile format.
     * Only fields needed by the WikiXmlReader are written
     * @param pages
     */
    public void writePages(List<WikiPage> pages) {
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            dom = db.newDocument();
            rootEle = dom.createElement("pages");

            for (WikiPage page : pages) {
                addPageToDom(page);
            }

            dom.appendChild(rootEle);
            writeXml();
        } catch (TransformerException | FileNotFoundException | ParserConfigurationException e1) {
            e1.printStackTrace();
        }

    }

    private void writeXml() throws TransformerException, FileNotFoundException {
        Transformer tr = TransformerFactory.newInstance().newTransformer();
        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.setOutputProperty(OutputKeys.METHOD, "xml");
        tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        // send DOM to file
        tr.transform(new DOMSource(dom),
                new StreamResult(new FileOutputStream(xmlOutPath)));
    }

    public WikiXmlWriter(String xmlOutPath) {
        this.xmlOutPath = xmlOutPath;
    }
}
