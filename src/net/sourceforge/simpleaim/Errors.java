/*
 * SimpleAIM
 * A miniature console AIM client
 * http://simpleaim.sourceforge.net
 * Copyright (C) 2002-2003
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package net.sourceforge.simpleaim;

import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * Handles incoming error messages for SimpleAIM.
 *
 * @author  simpleaim.sourceforge.net
 */
public class Errors {
  /**
   * Used for logging
   */
  private static final Logger mLogger =
    Logger.getLogger(Errors.class.getName());

  /**
   * Prints a friendly error message to System.out based on the error's id.
   *
   * @param Err The error id retrieved from the AIM server.
   */
  public static void expand(String err) {
    StringTokenizer t = new StringTokenizer(err, ":");
    String tmp = t.nextToken();
    if (tmp.equals("ERROR")) {
      System.out.print("ERROR: ");
      int errorNum = Integer.parseInt(t.nextToken());
      mLogger.fine("Error: " + errorNum);
      switch (errorNum) {
        // General net.sourceforge.simpleaim.Errors
        case 901 :
          System.out.println(t.nextToken() + " is not currently available.");
          break;
        case 902 :
          System.out.println(
            "Warning of " + t.nextToken() + " not currently available");
          break;
        case 903 :
          System.out.println(
            "A message has been dropped, you are exceeding"
              + " the server speed limit.");
          break;
          // Admin net.sourceforge.simpleaim.Errors
        case 911 :
          System.out.println("Error validating input.");
          break;
        case 912 :
          System.out.println("Invalid account.");
          break;
        case 913 :
          System.out.println("Error encountered while processing request.");
          break;
        case 914 :
          System.out.println("Service unavailable.");
          break;
          // Chat net.sourceforge.simpleaim.Errors
        case 950 :
          System.out.println("Chat in " + t.nextToken() + " is unavailable.");
          break;
          // IM & Info net.sourceforge.simpleaim.Errors
        case 960 :
          System.out.println(
            "You are sending messages too fast to " + t.nextToken() + ".");
          break;
        case 961 :
          System.out.println(
            "You missed an IM from "
              + t.nextToken()
              + " because it was too big.");
          break;
        case 962 :
          System.out.println(
            "You missed an IM from "
              + t.nextToken()
              + " because it was sent too fast.");
          break;
          // Dir net.sourceforge.simpleaim.Errors
        case 970 :
          System.out.println("Failure.");
          break;
        case 971 :
          System.out.println("Too many matches.");
          break;
        case 972 :
          System.out.println("Need more qualifiers.");
          break;
        case 973 :
          System.out.println("Dir service temporarily unavailable.");
          break;
        case 974 :
          System.out.println("E-mail lookup restricted.");
          break;
        case 975 :
          System.out.println("Keyword Ignored.");
          break;
        case 976 :
          System.out.println("No Keywords.");
          break;
        case 977 :
          System.out.println("Language not supported.");
          break;
        case 978 :
          System.out.println("Country not supported.");
          break;
        case 979 :
          System.out.println("Failure unknown " + t.nextToken());
          break;
          // Auth errors
        case 980 :
          System.out.println("Incorrect nickname or password.");
          break;
        case 981 :
          System.out.println("The service is temporarily unavailable.");
          break;
        case 982 :
          System.out.println(
            "Your warning level is currently too high to sign on.");
          break;
        case 983 :
          System.out.println(
            "You have been connecting and disconnecting too frequently."
              + " Wait 10 minutes and try again.");
          break;
        case 989 :
          System.out.println(
            "An unknown signon error has occurred " + t.nextToken());
          break;
        default :
          System.out.println("An unknown error has occurred.");
          break;
      }
    }
  }
}
