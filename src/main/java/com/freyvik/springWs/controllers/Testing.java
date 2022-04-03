package com.freyvik.springWs.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;

@RestController
@RequestMapping("/testing")
public class Testing {

    Logger log = LoggerFactory.getLogger(Testing.class);

    @GetMapping("/xpath")
    public String useXpath() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("/home/frey/Projects/spring-ws/src/main/resources/students.xml");
        XPath xPath = XPathFactory.newInstance().newXPath();

        String expression = "//*[local-name()='ListOfCountryNamesByCodeResult']/*[local-name()='tCountryCodeAndName']";

        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Element country = (Element) nodeList.item(i).cloneNode(true);
            String codeExpression = "//*[local-name()='sISOCode']";
            String nameExpression = "//*[local-name()='sName']";
            String code = (String) xPath.compile(codeExpression).evaluate(country, XPathConstants.STRING);
            String name = (String) xPath.compile(nameExpression).evaluate(country, XPathConstants.STRING);
            log.info("Country: " + name + " - " + code);

        }

        return "OK";
    }
}
