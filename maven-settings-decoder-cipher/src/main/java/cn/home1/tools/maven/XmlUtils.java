package cn.home1.tools.maven;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public final class XmlUtils {

    private XmlUtils() {
    }

    public static Document xmlDocument(
        final File file
    ) throws ParserConfigurationException, IOException, SAXException {
        // see: http://www.ibm.com/developerworks/cn/xml/x-javaxpathapi.html
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false); // Note: never forget this !
        final DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(file);
    }

    public static String xmlNodeText(
        final File file,
        final String xpathExpression
    ) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        final Document doc = xmlDocument(file);

        final Object result = XPathFactory.newInstance().newXPath() //
            .compile(xpathExpression) //
            .evaluate(doc, XPathConstants.STRING);
        return result != null ? result.toString() : null;
    }
}
