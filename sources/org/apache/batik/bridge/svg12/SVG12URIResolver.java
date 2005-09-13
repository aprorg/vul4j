/*

   Copyright 2005  The Apache Software Foundation 

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.apache.batik.bridge.svg12;

import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.URIResolver;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.xbl.NodeXBL;
import org.apache.batik.dom.xbl.XBLShadowTreeElement;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;

/**
 * A URIResolver for SVG 1.2 documents.  This is to allow resolution of
 * fragment IDs within shadow trees to work properly.
 *
 * @author <a href="mailto:cam%40mcc%2eid%2eau">Cameron McCormack</a>
 * @version $Id$
 */
public class SVG12URIResolver extends URIResolver {

    /**
     * Creates a new SVG12URIResolver object.
     */
    public SVG12URIResolver(SVGDocument doc, DocumentLoader dl) {
        super(doc, dl);
    }

    /**
     * Returns the base URI of the referer element.
     */
    protected String getRefererBaseURI(Element ref) {
        AbstractNode aref = (AbstractNode) ref;
        if (aref.getXblBoundElement() != null) {
            return null;
        }
        return aref.getBaseURI();
    }

    /**
     * Returns the node referenced by the given fragment identifier.
     * This is called when the whole URI just contains a fragment identifier
     * and there is no XML Base URI in effect.
     * @param frag the URI fragment
     * @param ref  the context element from which to resolve the URI fragment
     */
    protected Node getNodeByFragment(String frag, Element ref) {
        NodeXBL refx = (NodeXBL) ref;
        NodeXBL boundElt = (NodeXBL) refx.getXblBoundElement();
        if (boundElt != null) {
            XBLShadowTreeElement shadow
                = (XBLShadowTreeElement) boundElt.getXblShadowTree();
            Node n = shadow.getElementById(frag);
            if (n != null) {
                return n;
            }
            NodeList nl = refx.getXblDefinitions();
            for (int i = 0; i < nl.getLength(); i++) {
                n = nl.item(i).getOwnerDocument().getElementById(frag);
                if (n != null) {
                    return n;
                }
            }
        }
        return super.getNodeByFragment(frag, ref);
    }
}
