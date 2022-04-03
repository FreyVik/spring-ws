package com.freyvik.springWs.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.*;

@org.springframework.web.bind.annotation.RestController
public class RestController {

    final static Logger log = LoggerFactory.getLogger(RestController.class);

    // Properties
    @Value("${countryInfo.endpoint}")
    private String countryWsdl;
    @Value("${web.namespace}")
    private String webNamespace;
    @Value("${web.namespace.url}")
    private String webNamespaceUrl;

    @GetMapping("/test")
    public String test() {
        log.info("Test method called");
        return "Spring app running";
    }

    @GetMapping("/countries")
    public List<Map<String, String>> countries() {
        SOAPMessage soapResponse = null;
        try {
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();
            soapResponse = soapConnection.call(createSoap(), countryWsdl);

        } catch (Exception e) {
            log.error("SOAP error", e);
        }

        List<Map<String, String>> countries = new ArrayList<>();
        try {
            assert soapResponse != null;
            countries = getXML(soapResponse);

        } catch (SOAPException soapEx) {
            log.error("Soap error:", soapEx);
        } catch (XPathExpressionException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
        }

        return countries;
    }


    /*
     * Generate basic SOAPMessage object
     *
     * @return soapMessage
     * @throws SOAPException
     */
    private SOAPMessage createSoap() throws SOAPException {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        // Define namespaces
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration(webNamespace, webNamespaceUrl);

        SOAPBody soapBody = envelope.getBody();
        /*
        * <soapenv:Body><web:ListOfCountryNamesByCode/>
        </soapenv:Body>
         */
        SOAPElement listofCountryElement = soapBody.addChildElement("ListOfCountryNamesByCode", webNamespace);
        soapMessage.saveChanges();

        return soapMessage;
    }

    private List<Map<String, String>> getXML(SOAPMessage soapMessage) throws ParserConfigurationException, SOAPException, IOException, XPathExpressionException {
        List<Map<String, String>> countries = new ArrayList<>();
        Document xmlResponse = soapMessage.getSOAPBody().extractContentAsDocument();
        XPath xPath = XPathFactory.newInstance().newXPath();

        String expression = "//*[local-name()='ListOfCountryNamesByCodeResult']/*[local-name()='tCountryCodeAndName']";
        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlResponse, XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Map<String, String> country = new HashMap<>();

            Element countryElement = (Element) nodeList.item(i).cloneNode(true);
            String codeExpression = "//*[local-name()='sISOCode']";
            String nameExpression = "//*[local-name()='sName']";
            String code = (String) xPath.compile(codeExpression).evaluate(countryElement, XPathConstants.STRING);
            String name = (String) xPath.compile(nameExpression).evaluate(countryElement, XPathConstants.STRING);

            country.put("name", name);
            country.put("code", code);
            countries.add(country);
        }

        return countries;
    }
}
