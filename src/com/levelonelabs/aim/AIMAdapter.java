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


/**
 * Default no-op impl for AIMListner
 *
 * @author Scott Oster
 *
 * @created January 1, 2002
 */
public class AIMAdapter implements AIMListener {

    public void handleMessage(AIMBuddy buddy, String request) {
    }


    public void handleBuddySignOn(AIMBuddy buddy, String info) {
    }


    public void handleBuddySignOff(AIMBuddy buddy, String info) {
    }


    public void handleError(String error, String message) {
    }


    public void handleWarning(AIMBuddy buddy, int amount) {
    }


	public void handleConnected() {
	}


	public void handleDisconnected() {
	}
    

	public void handleBuddyUnavailable(AIMBuddy aimbud, String message) {
	}
    

	public void handleBuddyAvailable(AIMBuddy aimbud, String message) {
	}
}
