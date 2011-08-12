/*
 *  Copyright (c) 2002-2003, The Joust Project
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
 *  File created by keith @ Apr 3, 2003
 *
 */

package net.kano.joscar.flap;

/**
 * An exception thrown when an invalid FLAP header is read.
 */
public class InvalidFlapHeaderException extends RuntimeException {
    /**
     * Creates a new <code>InvalidFlapHeaderException</code> with no message.
     */
    public InvalidFlapHeaderException() { }

    /**
     * Creates a new <code>InvalidFlapHeaderException</code> with the given
     * message.
     *
     * @param message the detail message for this exception
     */
    public InvalidFlapHeaderException(String message) {
        super(message);
    }

    /**
     * Creates a new <code>InvalidFlapHeaderException</code> with the given
     * message and cause.
     *
     * @param message the detail message for this exception
     * @param cause the cause of this exception
     */
    public InvalidFlapHeaderException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new <code>InvalidFlapHeaderException</code> with no message and
     * the given cause.
     *
     * @param cause the cause of this exception
     */
    public InvalidFlapHeaderException(Throwable cause) {
        super(cause);
    }
}
