/**
 * Copyright � 2017, viadee Unternehmensberatung GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the viadee Unternehmensberatung GmbH.
 * 4. Neither the name of the viadee Unternehmensberatung GmbH nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <viadee Unternehmensberatung GmbH> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.viadee.bpm.vPAV;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class BPMNScanner {

    private final String businessRuleTask_one = "bpmn:businessRuleTask";

    private final String serviceTask_one = "bpmn:serviceTask";

    private final String sendTask_one = "bpmn:sendTask";

    private final String gateway_one = "bpmn:exclusiveGateway";

    private final String out_one = "bpmn:outgoing";

    private final String sequence_one = "bpmn:sequenceFlow";

    private final String intermediateCatchEvent_one = "bpmn:intermediateCatchEvent";

    private final String intermediateThrowEvent_one = "bpmn:intermediateThrowEvent";

    private final String startEvent_one = "bpmn:startEvent";

    private final String boundaryEvent_one = "bpmn:boundaryEvent";

    private final String endEvent_one = "bpmn:endEvent";

    private final String extElements_one = "bpmn:extensionElements";

    // -----------------------

    private final String businessRuleTask_two = "bpmn2:businessRuleTask";

    private final String serviceTask_two = "bpmn2:serviceTask";

    private final String sendTask_two = "bpmn2:sendTask";

    private final String gateway_two = "bpmn2:exclusiveGateway";

    private final String out_two = "bpmn2:outgoing";

    private final String sequence_two = "bpmn2:sequenceFlow";

    private final String intermediateCatchEvent_two = "bpmn2:intermediateCatchEvent";

    private final String intermediateThrowEvent_two = "bpmn2:intermediateThrowEvent";

    private final String startEvent_two = "bpmn2:startEvent";

    private final String boundaryEvent_two = "bpmn2:boundaryEvent";

    private final String endEvent_two = "bpmn2:endEvent";

    private final String extElements_two = "bpmn2:extensionElements";

    // -----------------------

    private final String businessRuleTask_three = "businessRuleTask";

    private final String serviceTask_three = "serviceTask";

    private final String sendTask_three = "sendTask";

    private final String gateway_three = "exclusiveGateway";

    private final String out_three = "outgoing";

    private final String sequence_three = "sequenceFlow";

    private final String intermediateCatchEvent_three = "intermediateCatchEvent";

    private final String intermediateThrowEvent_three = "intermediateThrowEvent";

    private final String startEvent_three = "startEvent";

    private final String boundaryEvent_three = "boundaryEvent";

    private final String endEvent_three = "endEvent";

    private final String extElements_three = "extensionElements";

    // ------------------------

    private final String scriptTag = "camunda:script";

    private final String c_class = "camunda:class";

    private final String c_exp = "camunda:expression";

    private final String c_dexp = "camunda:delegateExpression";

    private final String c_dmn = "camunda:decisionRef";

    private final String c_ext = "camunda:type";

    private final String imp = "implementation";

    private final String timerEventDefinition = "timerEventDefinition";

    private final String condExp = "conditionExpression";

    private final String lang = "language";

    private String node_name;

    private DocumentBuilderFactory factory;

    private DocumentBuilder builder;

    private Document doc;

    private ModelVersionEnum model_Version;

    private enum ModelVersionEnum {
        V1, V2, V3
    }

    public static Logger logger = Logger.getLogger(BPMNScanner.class.getName());

    /**
     * The Camunda API's method "getimplementation" doesn't return the correct Implementation, so the we have to scan
     * the xml of the model for the implementation
     *
     * @throws ParserConfigurationException
     *             exception if document cant be parsed
     */
    public BPMNScanner() throws ParserConfigurationException {
        factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        builder = factory.newDocumentBuilder();
    }

    private void setModelVersion(String path) throws SAXException, IOException, ParserConfigurationException {
        // parse the given bpmn model
        doc = builder.parse(path);

        if (doc.getElementsByTagName("bpmn:definitions").getLength() > 0)
            model_Version = ModelVersionEnum.V1;
        else if (doc.getElementsByTagName("bpmn2:definitions").getLength() > 0)
            model_Version = ModelVersionEnum.V2;
        else if (doc.getElementsByTagName("definitions").getLength() > 0)
            model_Version = ModelVersionEnum.V3;
        else
            throw new ParserConfigurationException("Can't get the version of the BPMN Model");
    }

    /**
     * Return the Implementation of an specific element (sendTask, ServiceTask or BusinessRuleTask)
     *
     * @param path
     *            path to model
     * @param id
     *            id of bpmn element
     * @throws SAXException
     *             possible exception while process xml
     * @throws IOException
     *             possible exception if file not found
     * @throws ParserConfigurationException
     *             possible exception if file could not be parsed
     * @return return_implementation contains implementation
     */
    public String getImplementation(String path, String id)
            throws SAXException, IOException, ParserConfigurationException {
        // List to hold return values
        String return_implementation = null;

        // List for all Task elements
        ArrayList<NodeList> listNodeList = new ArrayList<NodeList>();

        // parse the given bpmn model
        doc = builder.parse(path);

        // set Model Version
        setModelVersion(path);

        switch (model_Version) {
            case V1:
                listNodeList.add(doc.getElementsByTagName(businessRuleTask_one));
                listNodeList.add(doc.getElementsByTagName(serviceTask_one));
                listNodeList.add(doc.getElementsByTagName(sendTask_one));
                listNodeList.add(doc.getElementsByTagName(endEvent_one));
                listNodeList.add(doc.getElementsByTagName(intermediateThrowEvent_one));
                break;
            case V2:
                listNodeList.add(doc.getElementsByTagName(businessRuleTask_two));
                listNodeList.add(doc.getElementsByTagName(serviceTask_two));
                listNodeList.add(doc.getElementsByTagName(sendTask_two));
                listNodeList.add(doc.getElementsByTagName(endEvent_two));
                listNodeList.add(doc.getElementsByTagName(intermediateThrowEvent_two));
                break;
            case V3:
                listNodeList.add(doc.getElementsByTagName(businessRuleTask_three));
                listNodeList.add(doc.getElementsByTagName(serviceTask_three));
                listNodeList.add(doc.getElementsByTagName(sendTask_three));
                listNodeList.add(doc.getElementsByTagName(endEvent_three));
                listNodeList.add(doc.getElementsByTagName(intermediateThrowEvent_three));
                break;
            default:
                listNodeList = null;
        }

        // iterate over list<NodeList> and check each NodeList (BRTask,
        // ServiceTask and SendTask)
        for (final NodeList list : listNodeList) {
            // iterate over list and check child of each node
            for (int i = 0; i < list.getLength(); i++) {
                Element Task_Element = (Element) list.item(i);
                NamedNodeMap Task_Element_Attr = Task_Element.getAttributes();

                // check if the ids are corresponding
                if (id.equals(Task_Element.getAttribute("id"))) {
                    // check if more than 1 inner attribute exists
                    if (Task_Element_Attr.getLength() > 1) {
                        // check all attributes, whether they fit an
                        // implementation
                        for (int x = 0; x < Task_Element_Attr.getLength(); x++) {
                            Node attr = Task_Element_Attr.item(x);
                            // node_name equals an implementation
                            node_name = attr.getNodeName();
                            if (node_name.equals(c_class) || node_name.equals(c_exp) || node_name.equals(c_dexp)
                                    || node_name.equals(c_dmn) || node_name.equals(c_ext)) {
                                return_implementation = node_name;
                            }
                        }
                        // if inner attributes dont consist of implementations
                    }
                    if (Task_Element_Attr.getNamedItem(c_class) == null && Task_Element_Attr.getNamedItem(c_exp) == null
                            && Task_Element_Attr.getNamedItem(c_dexp) == null
                            && Task_Element_Attr.getNamedItem(c_dmn) == null
                            && Task_Element_Attr.getNamedItem(c_ext) == null) {
                        return_implementation = imp;
                    }
                }
            }
        }
        return return_implementation;
    }

    /**
     * Return the Implementation of an specific element (endEvent and/or intermediateThrowEvent)
     *
     * @param path
     *            path to model
     * @param id
     *            id of bpmn element
     * @throws SAXException
     *             possible exception while process xml
     * @throws IOException
     *             possible exception if file not found
     * @throws ParserConfigurationException
     *             possible exception if file could not be parsed
     * @return return_implementation contains implementation
     */
    public String getEventImplementation(String path, String id)
            throws SAXException, IOException, ParserConfigurationException {
        // List to hold return values
        String return_implementation = null;

        // List for all Task elements
        ArrayList<NodeList> listNodeList = new ArrayList<NodeList>();

        // parse the given bpmn model
        doc = builder.parse(path);

        // set Model Version
        setModelVersion(path);

        switch (model_Version) {
            case V1:
                listNodeList.add(doc.getElementsByTagName(endEvent_one));
                listNodeList.add(doc.getElementsByTagName(intermediateThrowEvent_one));
                break;
            case V2:
                listNodeList.add(doc.getElementsByTagName(endEvent_two));
                listNodeList.add(doc.getElementsByTagName(intermediateThrowEvent_two));
                break;
            case V3:
                listNodeList.add(doc.getElementsByTagName(endEvent_three));
                listNodeList.add(doc.getElementsByTagName(intermediateThrowEvent_three));
                break;
            default:
                listNodeList = null;
        }

        // iterate over list<NodeList> and check each NodeList (endEvent, intermediateThrowEvent)
        for (final NodeList list : listNodeList) {
            // iterate over list and check child of each node
            for (int i = 0; i < list.getLength(); i++) {
                final Element Task_Element = (Element) list.item(i);

                // check if the ids are corresponding
                if (id.equals(Task_Element.getAttribute("id"))) {

                    final NodeList childNodes = Task_Element.getChildNodes();

                    // check all attributes, whether they equal a messageEventDefinition
                    for (int x = 0; x < childNodes.getLength(); x++) {
                        if (childNodes.item(x).getLocalName() != null
                                && childNodes.item(x).getLocalName().equals("messageEventDefinition")) {
                            final Element event = (Element) childNodes.item(x);

                            // if the node messageEventDefinition contains the camunda expression -> return
                            if (event.getAttributeNode(c_exp) != null) {
                                return_implementation = event.getAttributeNode(c_exp).toString();
                            }
                        }
                    }
                }
            }
        }
        return return_implementation;
    }

    /**
     *
     * @param path
     *            path to model
     * @param id
     *            id of bpmn element
     * @param listType
     *            Type of Attribute
     * @param extType
     *            Type of Listener
     * @return value of Listener
     * @throws SAXException
     *             possible exception while process xml
     * @throws IOException
     *             possible exception if file not found
     * @throws ParserConfigurationException
     *             possible exception if file could not be parsed
     */
    public ArrayList<String> getListener(String path, String id, String listType, String extType)
            throws SAXException, IOException, ParserConfigurationException {

        // list to hold return values
        ArrayList<String> returnAttrList = new ArrayList<String>();

        // List for all Task elements
        NodeList nodeListExtensionElements;

        // parse the given bpmn model
        doc = builder.parse(path);

        // set Model Version
        setModelVersion(path);

        // search for script tag

        switch (model_Version) {
            case V1:
                nodeListExtensionElements = doc.getElementsByTagName(extElements_one);
                break;
            case V2:
                nodeListExtensionElements = doc.getElementsByTagName(extElements_two);
                break;
            case V3:
                nodeListExtensionElements = doc.getElementsByTagName(extElements_three);
                break;
            default:
                nodeListExtensionElements = null;
        }

        // search for parent with id
        for (int i = 0; i < nodeListExtensionElements.getLength(); i++) {
            if (((Element) nodeListExtensionElements.item(i).getParentNode()).getAttribute("id").equals(id)) {
                NodeList childNodes = nodeListExtensionElements.item(i).getChildNodes();
                for (int x = 0; x < childNodes.getLength(); x++) {
                    if (childNodes.item(x).getNodeName().equals(extType)) {
                        String attName = checkAttributesOfNode(childNodes.item(x), listType);
                        if (attName != null)
                            returnAttrList.add(attName);
                    }
                }
            }
        }
        return returnAttrList;
    }

    /**
     * get Filename of the form
     * 
     * @param path
     *            path from model
     * @param id
     *            id of the element
     * @param tagName
     *            elementTyp
     * @return Filnename of the form
     * @throws SAXException
     *             possible exception while process xml
     * @throws IOException
     *             possible exception if file not found
     * @throws ParserConfigurationException
     *             possible exception while parse xml
     */
    public String getForm(final String path, final String id, String tagName)
            throws SAXException, IOException, ParserConfigurationException {
        NodeList nList;

        // set Model Version
        setModelVersion(path);

        switch (model_Version) {
            case V1:
                tagName = "bpmn:" + tagName;
                break;
            case V2:
                tagName += "bpmn2:" + tagName;
                break;
            default:
                break;
        }

        nList = doc.getElementsByTagName(tagName);

        for (int i = 0; i < nList.getLength(); i++) {
            final Element Task_Element = (Element) nList.item(i);
            if (Task_Element.getAttribute("id").equals(id)) {
                NamedNodeMap attList = Task_Element.getAttributes();

                for (int x = 0; x < attList.getLength(); x++) {
                    if (attList.item(x).getNodeName().equals("camunda:formKey"))
                        return attList.item(x).getTextContent()
                                .substring(attList.item(x).getTextContent().indexOf("/") + 1);
                }
            }
        }

        return null;
    }

    /**
     *
     * @param node
     *            node to check
     * @param listType
     *            Type of ExecutionListener
     * @return textContent of ListenerType
     */
    private String checkAttributesOfNode(Node node, String listType) {
        NamedNodeMap attributes = node.getAttributes();
        for (int x = 0; x < attributes.getLength(); x++) {
            if (attributes.item(x).getNodeName().equals(listType)) {
                return attributes.item(x).getTextContent();
            }
        }
        return null;
    }

    /**
     * Check if model has an scriptTag
     *
     * @param path
     *            path to model
     * @param id
     *            id of bpmn element
     * @throws SAXException
     *             possible exception while process xml
     * @throws IOException
     *             possible exception if file not found
     * @return scriptPlaces contains script type
     */
    public ArrayList<String> getScriptTypes(String path, String id) throws SAXException, IOException {
        // bool to hold return values
        ArrayList<String> return_scriptType = new ArrayList<String>();

        // List for all Task elements
        NodeList nodeList;

        // parse the given bpmn model
        doc = builder.parse(path);

        // search for script tag
        nodeList = doc.getElementsByTagName(scriptTag);

        // search for parent with id
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node n = nodeList.item(i).getParentNode();
            if (idMatch(nodeList.item(i), id)) {
                return_scriptType.add(n.getLocalName());
            }
        }

        return return_scriptType;
    }

    /**
     * Check if any parentnode has the specific id
     *
     * @param n
     *            Node to check their parents
     * @param id
     *            id to check
     * @return true if id was found
     */
    private boolean idMatch(Node n, String id) {
        Element e = (Element) n;

        if (e.getAttribute("id").equals(id))
            return true;

        while (e.getParentNode() != null && !e.getParentNode().getLocalName().equals("process")) {
            Element check = (Element) e.getParentNode();
            if (check.getAttribute("id").equals(id)) {
                return true;
            } else {
                e = (Element) e.getParentNode();
            }
        }
        return false;
    }

    public boolean hasScriptInCondExp(String path, String id)
            throws SAXException, IOException, ParserConfigurationException {
        // List for all Task elements
        NodeList nodeList = null;

        // set Model Version and parse doc
        setModelVersion(path);

        switch (model_Version) {
            case V1:
                // create nodelist that contains all Tasks with the namespace
                nodeList = doc.getElementsByTagName(sequence_one);
                break;
            case V2:
                nodeList = doc.getElementsByTagName(sequence_two);
                break;
            case V3:
                nodeList = doc.getElementsByTagName(sequence_three);
                break;
        }

        // search for parent with id
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element sequence_Element = (Element) nodeList.item(i);
            if (sequence_Element.getAttribute("id").equals(id)) {
                return hasCondExp(sequence_Element);
            }
        }

        return false;
    }

    /**
     * check if sequenceFlow has an Script (value in language attribute) in conditionalExpression
     *
     * @param sq
     *            sequenceFlowNode
     * @return true or false
     */
    private boolean hasCondExp(Element sq) {
        NodeList childNodes = null;
        if (sq.hasChildNodes()) {
            childNodes = sq.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node childNode = childNodes.item(i);
                if (childNode.getLocalName() != null && childNode.getLocalName().equals(condExp)) {
                    Element childElement = (Element) childNode;
                    if (childElement.getAttribute(lang).trim().length() > 0)
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Return a list of used gateways for a given bpmn model
     *
     * @param path
     *            path to model
     * @param id
     *            id of bpmn element
     * @throws SAXException
     *             possible exception while process xml
     * @throws IOException
     *             possible exception if file not found
     * @throws ParserConfigurationException
     *             possible exception if file could not be parsed
     * @return gateway contains script type
     *
     */
    public String getXorGateWays(String path, String id)
            throws SAXException, IOException, ParserConfigurationException {
        final NodeList nodeList;

        String gateway = "";

        doc = builder.parse(path);

        // set Model Version
        setModelVersion(path);

        switch (model_Version) {
            case V1:
                nodeList = doc.getElementsByTagName(gateway_one);
                break;
            case V2:
                nodeList = doc.getElementsByTagName(gateway_two);
                break;
            case V3:
                nodeList = doc.getElementsByTagName(gateway_three);
                break;
            default:
                return "";
        }

        // iterate over list and check each item
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element Task_Element = (Element) nodeList.item(i);

            // check if the ids are corresponding
            if (id.equals(Task_Element.getAttribute("id"))) {
                gateway = Task_Element.getAttribute("id");
            }
        }
        return gateway;
    }

    /**
     * Return number of outgoing
     *
     * @param path
     *            path to model
     * @param id
     *            id of bpmn element
     * @throws SAXException
     *             possible exception while process xml
     * @throws IOException
     *             possible exception if file not found
     * @throws ParserConfigurationException
     *             possible exception if file could not be parsed
     * @return outgoing number of outgoing
     */
    public int getOutgoing(String path, String id) throws SAXException, IOException, ParserConfigurationException {
        final NodeList nodeList;
        String out = "";
        int outgoing = 0;

        doc = builder.parse(path);

        // set Model Version
        setModelVersion(path);

        switch (model_Version) {
            case V1:
                // create nodelist that contains all Tasks with the namespace
                nodeList = doc.getElementsByTagName(gateway_one);
                out = out_one;
                break;
            case V2:
                nodeList = doc.getElementsByTagName(gateway_two);
                out = out_two;
                break;
            case V3:
                nodeList = doc.getElementsByTagName(gateway_three);
                out = out_three;
                break;
            default:
                return -1;
        }

        // iterate over list and check each item
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element Task_Element = (Element) nodeList.item(i);

            // check if the ids are corresponding
            if (id.equals(Task_Element.getAttribute("id"))) {
                NodeList childNodeGateway = Task_Element.getChildNodes();
                for (int x = 0; x < childNodeGateway.getLength(); x++) {
                    if (childNodeGateway.item(x).getNodeName().equals(out)) {
                        outgoing++;
                    }
                }
            }
        }
        return outgoing;
    }

    /**
     * check xor gateways for outgoing edges
     *
     * @param path
     *            path to model
     * @param id
     *            id of bpmn element
     * @return ArrayList of outgoing Nodes
     * @throws SAXException
     *             possible exception while process xml
     * @throws IOException
     *             possible exception if file not found
     * @throws ParserConfigurationException
     *             possible exception if file could not be parsed
     */
    public ArrayList<Node> getOutgoingEdges(String path, String id)
            throws SAXException, IOException, ParserConfigurationException {

        ArrayList<Node> outgoingEdges = new ArrayList<Node>();
        NodeList nodeList = null;
        String out = "";

        doc = builder.parse(path);

        // set Model Version
        setModelVersion(path);

        switch (model_Version) {
            case V1:
                // create nodelist that contains all Tasks with the namespace
                nodeList = doc.getElementsByTagName(gateway_one);
                out = out_one;
                break;
            case V2:
                nodeList = doc.getElementsByTagName(gateway_two);
                out = out_two;
                break;
            case V3:
                nodeList = doc.getElementsByTagName(gateway_three);
                out = out_three;
                break;
        }

        for (int i = 0; i < nodeList.getLength(); i++) {
            Element Task_Element = (Element) nodeList.item(i);

            // check if the ids are corresponding and retrieve the outgoing edges of the xor gateway
            if (id.equals(Task_Element.getAttribute("id"))) {
                NodeList children = Task_Element.getChildNodes();
                for (int j = 0; j < children.getLength(); j++) {
                    if (children.item(j).getNodeName().equals(out)) {
                        outgoingEdges.add(checkNamingOfEdges(children.item(j).getTextContent()));
                    }
                }
            }
        }
        return outgoingEdges;
    }

    /**
     * check xor gateways for outgoing edges
     *
     * @param id
     *            id of edge
     * @return edge
     */
    public Node checkNamingOfEdges(String id) {

        Node edge = null;
        NodeList nodeList = null;

        switch (model_Version) {
            case V1:
                // create nodelist that contains all Tasks with the namespace
                nodeList = doc.getElementsByTagName(sequence_one);
                break;
            case V2:
                nodeList = doc.getElementsByTagName(sequence_two);
                break;
            case V3:
                nodeList = doc.getElementsByTagName(sequence_three);
                break;
        }

        for (int i = 0; i < nodeList.getLength(); i++) {
            Element Task_Element = (Element) nodeList.item(i);
            if (Task_Element.getAttribute("id").equals(id)) {
                edge = Task_Element;
            }
        }
        return edge;
    }

    /**
     * get ids and timer definition for all timer event types
     *
     * @param path
     *            path to model
     * @param id
     *            id of bpmn element
     * @return Map with timerEventDefinition-Node and his child
     * @throws ParserConfigurationException
     *             possible exception if file could not be parsed
     * @throws IOException
     *             possible exception if file not found
     * @throws SAXException
     *             possible exception while process xml
     */
    public Map<Element, Element> getTimerImplementation(final String path, final String id)
            throws SAXException, IOException, ParserConfigurationException {

        // List for all Task elements
        ArrayList<NodeList> listNodeList = new ArrayList<NodeList>();

        // parse the given bpmn model
        doc = builder.parse(path);

        // set Model Version
        setModelVersion(path);

        switch (model_Version) {
            case V1:
                listNodeList.add(doc.getElementsByTagName(startEvent_one));
                listNodeList.add(doc.getElementsByTagName(intermediateCatchEvent_one));
                listNodeList.add(doc.getElementsByTagName(boundaryEvent_one));
                break;
            case V2:
                listNodeList.add(doc.getElementsByTagName(startEvent_two));
                listNodeList.add(doc.getElementsByTagName(intermediateCatchEvent_two));
                listNodeList.add(doc.getElementsByTagName(boundaryEvent_two));
                break;
            case V3:
                listNodeList.add(doc.getElementsByTagName(startEvent_three));
                listNodeList.add(doc.getElementsByTagName(intermediateCatchEvent_three));
                listNodeList.add(doc.getElementsByTagName(boundaryEvent_three));
                break;
            default:
                listNodeList = null;
        }

        // final ArrayList<Element> timerList = new ArrayList<>();
        final Map<Element, Element> timerList = new HashMap<>();

        // iterate over list<NodeList>
        for (final NodeList list : listNodeList) {
            for (int i = 0; i < list.getLength(); i++) {
                final Element Task_Element = (Element) list.item(i);

                // check whether a node matches with the provided id
                if (Task_Element.getAttribute("id").equals(id)) {

                    final NodeList childNodes = Task_Element.getChildNodes();
                    for (int x = 0; x < childNodes.getLength(); x++) {

                        // check if an event consists of a timereventdefinition tag
                        if (childNodes.item(x).getLocalName() != null
                                && childNodes.item(x).getLocalName().equals(timerEventDefinition)) {

                            timerList.put(Task_Element, null);

                            // retrieve values of children
                            final Element Task_Element2 = (Element) childNodes.item(x);
                            final NodeList childChildNodes = Task_Element2.getChildNodes();
                            for (int y = 0; y < childChildNodes.getLength(); y++) {
                                // localname must be either timeDate, timeCycle or timeDuration
                                // add nodes/elements to map
                                if (childChildNodes.item(y).getLocalName() != null) {
                                    timerList.put(Task_Element, (Element) childChildNodes.item(y));
                                }
                            }
                        }
                    }
                }
            }
        }
        return timerList;
    }

    public String getC_exp() {
        return c_exp;
    }

}