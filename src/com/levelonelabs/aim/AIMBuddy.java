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

import org.w3c.dom.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


/**
 * Represents and AIM Buddy
 * 
 * @author Will Gorman, Scott Oster
 * @created November 8, 2001
 */
public class AIMBuddy implements XMLizable {
    String name;
    transient boolean online;
    transient int warningAmount = 0;
    boolean banned;
    ArrayList messages = new ArrayList();
    HashMap roles = new HashMap();
    HashMap preferences = new HashMap();
    String group;


    /**
     * Constructor for the AIMBuddy object
     * 
     * @param name
     */
    public AIMBuddy(String name) {
        this(name, AIMClient.DEFAULT_GROUP);
    }


    /**
     * Constructor for the AIMBuddy object
     * 
     * @param name
     * @param group
     *            buddy group in buddylist
     */
    public AIMBuddy(String name, String group) {
        this.name = name;
        setGroup(group);
    }


    /**
     * Sets the name attribute of the AIMBuddy object
     * 
     * @param name
     *            The new name value
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * Sets the online attribute of the AIMBuddy object
     * 
     * @param online
     *            The new online value
     */
    public void setOnline(boolean online) {
        this.online = online;
    }


    /**
     * Sets the preference attribute of the AIMBuddy object
     * 
     * @param pref
     *            The new preference value
     * @param val
     *            The new preference value
     */
    public void setPreference(String pref, String val) {
        preferences.put(pref, val);
    }


    /**
     * Gets the name attribute of the AIMBuddy object
     * 
     * @return The name value
     */
    public String getName() {
        return name;
    }


    /**
     * Gets the online attribute of the AIMBuddy object
     * 
     * @return The online value
     */
    public boolean isOnline() {
        return online;
    }


    /**
     * Gets the preference attribute of the AIMBuddy object
     * 
     * @param pref
     * @return The preference value
     */
    public String getPreference(String pref) {
        return (String) preferences.get(pref);
    }


    /**
     * Gets the preferences attribute of the AIMBuddy object
     * 
     * @return The preferences value
     */
    public HashMap getPreferences() {
        return preferences;
    }


    /**
     * Adds a feature to the Role attribute of the AIMBuddy object
     * 
     * @param role
     *            The feature to be added to the Role attribute
     */
    public void addRole(String role) {
        roles.put(role, role);
    }


    /**
     * Gets the messages attribute of the AIMBuddy object
     * 
     * @return The messages value
     */
    public ArrayList getMessages() {
        return messages;
    }


    /**
     * Adds a feature to the Message attribute of the AIMBuddy object
     * 
     * @param message
     *            The feature to be added to the Message attribute
     */
    public void addMessage(String message) {
        messages.add(message);
    }


    /**
     * Remove all messages
     */
    public void clearMessages() {
        messages.clear();
    }


    /**
     * Does buddy have messages?
     * 
     * @return true for more than 0 messages
     */
    public boolean hasMessages() {
        return !messages.isEmpty();
    }


    /**
     * Do I have specified role
     * 
     * @param role
     * @return true if buddy has the role
     */
    public boolean hasRole(String role) {
        return roles.containsKey(role);
    }


    /**
     * Returns the banned.
     * 
     * @return boolean
     */
    public boolean isBanned() {
        return banned;
    }


    /**
     * Sets the banned.
     * 
     * @param banned
     *            The banned to set
     */
    public void setBanned(boolean banned) {
        this.banned = banned;
    }


    /**
     * @see com.levelonelabs.aim.XMLizable#readState(Element)
     */
    public void readState(Element fullStateElement) {
        // parse group
        String group = fullStateElement.getAttribute("group");
        if (group == null || group.trim().equals("")) {
            group = AIMSender.DEFAULT_GROUP;
        }
        setGroup(group);

        // parse banned
        String ban = fullStateElement.getAttribute("isBanned");
        if (ban.equalsIgnoreCase("true")) {
            setBanned(true);
        } else {
            setBanned(false);
        }

        // parse roles
        roles = new HashMap();
        NodeList list = fullStateElement.getElementsByTagName("role");
        for (int i = 0; i < list.getLength(); i++) {
            Element roleElem = (Element) list.item(i);
            String role = roleElem.getAttribute("name");
            addRole(role);
        }

        // parse messages
        messages = new ArrayList();
        list = fullStateElement.getElementsByTagName("message");
        for (int i = 0; i < list.getLength(); i++) {
            Element messElem = (Element) list.item(i);
            NodeList cdatas = messElem.getChildNodes();
            for (int j = 0; j < cdatas.getLength(); j++) {
                Node node = cdatas.item(j);
                if (node.getNodeType() == Node.CDATA_SECTION_NODE) {
                    String message = node.getNodeValue();
                    addMessage(message);
                    break;
                }
            }
        }

        // parse prefs
        preferences = new HashMap();
        list = fullStateElement.getElementsByTagName("preference");
        for (int i = 0; i < list.getLength(); i++) {
            Element prefElem = (Element) list.item(i);
            String pref = prefElem.getAttribute("name");
            String val = prefElem.getAttribute("value");
            this.setPreference(pref, val);
        }
    }


    /**
     * @see com.levelonelabs.aim.XMLizable#writeState(Element)
     */
    public void writeState(Element emptyStateElement) {
        Document doc = emptyStateElement.getOwnerDocument();
        emptyStateElement.setAttribute("name", this.getName());
        emptyStateElement.setAttribute("group", this.getGroup());
        emptyStateElement.setAttribute("isBanned", Boolean.toString(this.isBanned()));

        Iterator roleit = roles.keySet().iterator();
        while (roleit.hasNext()) {
            String role = (String) roleit.next();
            Element roleElem = doc.createElement("role");
            roleElem.setAttribute("name", role);
            emptyStateElement.appendChild(roleElem);
        }

        Iterator prefs = preferences.keySet().iterator();
        while (prefs.hasNext()) {
            String pref = (String) prefs.next();
            Element prefElem = doc.createElement("preference");
            prefElem.setAttribute("name", pref);
            prefElem.setAttribute("value", (String) preferences.get(pref));
            emptyStateElement.appendChild(prefElem);
        }

        for (int i = 0; i < messages.size(); i++) {
            String message = (String) messages.get(i);
            Element messElem = doc.createElement("message");
            CDATASection data = doc.createCDATASection(message);
            messElem.appendChild(data);
            emptyStateElement.appendChild(messElem);
        }
    }


    /**
     * Gets the current warning amount
     * 
     * @return the warning amount
     */
    public int getWarningAmount() {
        return warningAmount;
    }


    /**
     * Sets the current warning amount
     * 
     * @param amount
     */
    public void setWarningAmount(int amount) {
        warningAmount = amount;
    }


    /**
     * Set the group the buddy is in, in the buddy list
     * 
     * @param group
     *            The name of the group this buddy belongs to.
     */
    public void setGroup(String group) {
        this.group = group;
    }


    /**
     * The name of the group this buddy belongs to, in the buddylist
     * 
     * @return The name of the group this buddy belongs to.
     */
    public String getGroup() {
        return group;
    }
}
