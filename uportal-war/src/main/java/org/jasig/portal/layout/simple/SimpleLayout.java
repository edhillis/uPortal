/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.layout.simple;

import java.util.Enumeration;
import java.util.Vector;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.PortalException;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.LayoutEventListener;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.layout.node.UserLayoutNodeDescription;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The simple user layout implementation. This
 * layout is based on a Document.
 * 
 * Prior to uPortal 2.5, this class existed in the org.jasig.portal.layout package.
 * It was moved to its present package to reflect that it is part of the
 * Simple Layout Management implementation.
 *
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class SimpleLayout implements IUserLayout {
    
    private Document layout;
    private String layoutId;
    private String cacheKey;
    
    private Log log = LogFactory.getLog(getClass());
    

    public SimpleLayout(String layoutId, Document layout) {
        this.layoutId = layoutId;
        this.layout = layout;
    }

    public void writeTo(Document document) throws PortalException {
        document.appendChild(document.importNode(layout.getDocumentElement(), true));
    }

    public void writeTo(String nodeId, Document document) throws PortalException {
        document.appendChild(document.importNode(layout.getElementById(nodeId), true));
    }

    public IUserLayoutNodeDescription getNodeDescription(String nodeId) throws PortalException {
        Element element = (Element) layout.getElementById(nodeId);
        return UserLayoutNodeDescription.createUserLayoutNodeDescription(element);
    }

    public String getParentId(String nodeId) throws PortalException {
        String parentId = null;
        Element element = (Element)layout.getElementById(nodeId);
        if (element != null) {
            Node parent = element.getParentNode();
            if (parent != null && parent.getNodeType() == Node.ELEMENT_NODE) {
                Element parentE = (Element)parent;
                parentId = parentE.getAttribute("ID");
            }
        }
        return parentId;
    }

    public Enumeration getChildIds(String nodeId) throws PortalException {
        Vector v = new Vector();
        IUserLayoutNodeDescription node = getNodeDescription(nodeId);
        if (node instanceof IUserLayoutFolderDescription) {
            Element element = (Element)layout.getElementById(nodeId);
            for (Node n = element.getFirstChild(); n != null; n = n.getNextSibling()) {
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element)n;
                    if (e.getAttribute("ID") != null) {
                        v.add(e.getAttribute("ID"));
                    }
                }
            }
        }
        return v.elements();
    }

    public String getNextSiblingId(String nodeId) throws PortalException {
        String nextSiblingId = null;
        Element element = (Element)layout.getElementById(nodeId);
        if (element != null) {
            Node sibling = element.getNextSibling();
            // Find the next element node
            while (sibling != null && sibling.getNodeType() != Node.ELEMENT_NODE) {
                sibling = sibling.getNextSibling();
            }
            if (sibling != null) {
                Element e = (Element)sibling;
                nextSiblingId = e.getAttribute("ID");
            }
        }
        return nextSiblingId;
    }

    public String getPreviousSiblingId(String nodeId) throws PortalException {
        String prevSiblingId = null;
        Element element = (Element)layout.getElementById(nodeId);
        if (element != null) {
            Node sibling = element.getPreviousSibling();
            // Find the previous element node
            while (sibling != null && sibling.getNodeType() != Node.ELEMENT_NODE) {
                sibling = sibling.getPreviousSibling();
            }
            if (sibling != null) {
                Element e = (Element)sibling;
                prevSiblingId = e.getAttribute("ID");
            }
        }
        return prevSiblingId;
    }

    public String getCacheKey() throws PortalException {
        return cacheKey;
    }

    public boolean addLayoutEventListener(LayoutEventListener l) {
        // TODO: Implement this!
        return false;
    }

    public boolean removeLayoutEventListener(LayoutEventListener l) {
        // TODO: Implement this!
        return false;
    }

    public String getId() {
        return layoutId;
    }

    public String getNodeId(String fname) throws PortalException {
        String nodeId = null;
        NodeList nl = layout.getElementsByTagName("channel");
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element channelE = (Element)node;
                if (fname.equals(channelE.getAttribute("fname"))) {
                    nodeId = channelE.getAttribute("ID");
                    break;
                }
            }
        }
        return nodeId;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.IUserLayout#findNodeId(javax.xml.xpath.XPathExpression)
     */
    public String findNodeId(XPathExpression xpathExpression) throws PortalException {
        try {
            return xpathExpression.evaluate(this.layout);
        }
        catch (XPathExpressionException e) {
            throw new PortalException("Exception while executing XPathExpression: " + xpathExpression, e);
        }
    }

    public Enumeration getNodeIds() throws PortalException {
        Vector v = new Vector();
        try {
            String expression = "*";
            XPathFactory fac = XPathFactory.newInstance();
            XPath xpath = fac.newXPath();
            NodeList nl = (NodeList) xpath.evaluate(expression, layout, 
                    XPathConstants.NODESET);
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element)node;
                    v.add(e.getAttribute("ID"));
                }
            }
        } catch (Exception e) {
            log.error("Exception getting node ids.", e);
        }
        return v.elements();
    }

    public String getRootId() {
        String rootNode = null;
        try {
            
            String expression = "/layout/folder";
            XPathFactory fac = XPathFactory.newInstance();
            XPath xpath = fac.newXPath();
            Element rootNodeE = (Element) xpath.evaluate(expression, layout, 
                    XPathConstants.NODE);
            
            rootNode = rootNodeE.getAttribute("ID");
        } catch (Exception e) {
            log.error("Error getting root id.", e);
        }
        return rootNode;
    }

}
