/*
 *  Copyright (c) 2003, The Joust Project
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without 
 *  modification, are permitted provided that the following conditions 
 *  are met:
 *
 *  - Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer. 
 *  - Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the 
 *    distribution. 
 *  - Neither the name of the Joust Project nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 *  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 *  COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 *  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 *  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
 *  LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
 *  ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 *  POSSIBILITY OF SUCH DAMAGE.
 *
 *  File created by keith @ May 15, 2003
 *
 */

package net.kano.joscar;

/**
 * Provides a set of methods and fields designed for retrieving information
 * about the joscar library itself.
 */
public final class JoscarTools {
    /**
     * The "major" version of joscar being used. This will return the
     * <code>1</code> in version <code>1.2.3</code>.
     */
    public static final int JOSCAR_VERSION_MAJOR = 0;
    /**
     * The "minor" version of joscar being used. This will return the
     * <code>2</code> in version <code>1.2.3</code>.
     */
    public static final int JOSCAR_VERSION_MINOR = 9;
    /**
     * The "patch" version of joscar being used. This will return the
     * <code>3</code> in version <code>1.2.3</code>.
     */
    public static final int JOSCAR_VERSION_PATCH = 3;

    /** A version string, like "1.2.3". */
    private static final String VERSION_STRING
            = JOSCAR_VERSION_MAJOR + "."
            + JOSCAR_VERSION_MINOR + "."
            + JOSCAR_VERSION_PATCH;

    /**
     * Ensures that this class is never instantiated.
     */
    private JoscarTools() { }

    /**
     * Returns a version string describing the version of joscar loaded. This
     * will be a string like <code>"0.9"</code> or <code>"2.4.6"</code>.
     *
     * @return a version string describing the version of joscar loaded
     *
     * @see #JOSCAR_VERSION_MAJOR
     * @see #JOSCAR_VERSION_MINOR
     * @see #JOSCAR_VERSION_PATCH
     */
    public static String getVersionString() { return VERSION_STRING; }
}
