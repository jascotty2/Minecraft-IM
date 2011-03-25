/*------------------------------------------------------------------------------
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 *
 * The Original Code is levelonelabs.com code.
 * The Initial Developer of the Original Code is Level One Labs. Portions
 * created by the Initial Developer are Copyright (C) 2001 the Initial
 * Developer. All Rights Reserved.
 *
 *         Contributor(s):
 *             Scott Oster      (ostersc@alum.rpi.edu)
 *             Steve Zingelwicz (sez@po.cwru.edu)
 *             William Gorman   (willgorman@hotmail.com)
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable
 * instead of those above. If you wish to allow use of your version of this
 * file only under the terms of either the GPL or the LGPL, and not to allow
 * others to use your version of this file under the terms of the NPL, indicate
 * your decision by deleting the provisions above and replace them with the
 * notice and other provisions required by the GPL or the LGPL. If you do not
 * delete the provisions above, a recipient may use your version of this file
 * under the terms of any one of the NPL, the GPL or the LGPL.
 *----------------------------------------------------------------------------*/


package com.levelonelabs.aim;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;


/**
 * Container for a group of AIMBuddies
 *
 * @author Scott Oster
 *
 * @created May 28, 2002
 */
public class AIMGroup implements XMLizable {
    private ArrayList buddies=new ArrayList();
    private String name;

    /**
     * Constructor for the AIMGroup object
     *
     * @param name
     */
    public AIMGroup(String name) {
        this.name=name;
    }

    /**
     * Gets the name attribute of the AIMGroup object
     *
     * @return The name value
     */
    public String getName() {
        return this.name;
    }


    /**
     * Gets the list attribute of the AIMGroup object
     *
     * @return The list value
     *
     * @todo Need to make a real enumer or deep clone, to lazy right now
     */
    public ArrayList getList() {
        return this.buddies;
    }


    /**
     * Add a buddy to the list
     *
     * @param buddy
     *
     * @return true if the buddy wasnt already part of the list
     */
    public boolean add(String buddy) {
        if(!this.buddies.contains(buddy)) {
            return this.buddies.add(buddy);
        }
        return false;
    }


    /**
     * Remove a buddy from the list
     *
     * @param buddy
     *
     * @return true if the buddy was part of the list and removed
     */
    public boolean remove(String buddy) {
        if(this.buddies.contains(buddy)) {
            this.buddies.remove(this.buddies.indexOf(buddy));
            return true;
        }
        return false;
    }


    /**
     * Returns the number of unique buddies in the group
     *
     * @return the size
     */
    public int size() {
        return this.buddies.size();
    }


    /**
     * List the buddies in the group
     *
     * @return string of space separated buddy names
     */
    public String toString() {
        StringBuffer sb=new StringBuffer();
        for(ListIterator it=this.buddies.listIterator(); it.hasNext();) {
            sb.append(it.next()+" ");
        }
        return sb.toString();
    }


    /**
     * @see com.levelonelabs.aim.XMLizable#readState(Element)
     */
    public void readState(Element fullStateElement) {
        buddies=new ArrayList();
        NodeList list=fullStateElement.getElementsByTagName("buddy");
        for(int i=0; i < list.getLength(); i++) {
            Element buddyElem=(Element) list.item(i);
            String name=buddyElem.getAttribute("name");
            add(name);
        }
    }


    /**
     * @see com.levelonelabs.aim.XMLizable#writeState(Element)
     */
    public void writeState(Element emptyStateElement) {
        Document doc=emptyStateElement.getOwnerDocument();
        emptyStateElement.setAttribute("name", this.getName());

        Iterator buds=buddies.listIterator();
        while(buds.hasNext()) {
            String bud=(String) buds.next();
            Element budElem=doc.createElement("buddy");
            budElem.setAttribute("name", bud);
            emptyStateElement.appendChild(budElem);
        }
    }
}
