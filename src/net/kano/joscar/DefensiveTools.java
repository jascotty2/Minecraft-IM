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
 *  File created by keith @ Apr 23, 2003
 *
 */

package net.kano.joscar;

/**
 * A set of utilities for ensuring the validity (and non-<code>null</code>ness)
 * of arguments passed into methods or constructors.
 */
public final class DefensiveTools {
    /**
     * Never let anyone instantiate us.
     */
    private DefensiveTools() { }

    /**
     * Ensures that the given value is greater than or equal to the given
     * minimum value.
     *
     * @param val the value to check
     * @param name the name of this variable, for debugging purposes
     * @param min the minimum value of the given value
     *
     * @throws IllegalArgumentException if the given value is less than the
     *         given minimum
     */
    public static final void checkRange(int val, String name, int min)
            throws IllegalArgumentException {
        if (val < min) {
            throw new IllegalArgumentException(name + " (" + val + ") must " +
                    "be >= " + min);
        }
    }

    /**
     * Ensures that the given value is inclusively between the given minumum
     * and maximum values.
     *
     * @param val the value to check
     * @param name the name of this variable, for debugging purposes
     * @param min the minimum value of the given value
     * @param max the maximum value of the given value
     *
     * @throws IllegalArgumentException if the given value is less than the
     *         given minimum or greater than the given maximum
     */
    public static final void checkRange(int val, String name,
            int min, int max) throws IllegalArgumentException {
        if (val < min || val > max) {
            throw new IllegalArgumentException(name + " (" + val + ") must " +
                    "be >=" + min + " and <=" + max);
        }
    }


    /**
     * Ensures that the given value is greater than or equal to the given
     * minimum value.
     *
     * @param val the value to check
     * @param name the name of this variable, for debugging purposes
     * @param min the minimum value of the given value
     *
     * @throws IllegalArgumentException if the given value is less than the
     *         given minimum
     */
    public static void checkRange(long val, String name, int min)
            throws IllegalArgumentException {
        if (val < min) {
            throw new IllegalArgumentException(name + " (" + val + ") must " +
                    "be >= " + min);
        }
    }

    /**
     * Ensures that the given value is inclusively between the given minumum
     * and maximum values.
     *
     * @param val the value to check
     * @param name the name of this variable, for debugging purposes
     * @param min the minimum value of the given value
     * @param max the maximum value of the given value
     *
     * @throws IllegalArgumentException if the given value is less than the
     *         given minimum or greater than the given maximum
     */
    public static void checkRange(long val, String name, long min, long max) {
        if (val < min || val > max) {
            throw new IllegalArgumentException(name + " (" + val + ") must " +
                    "be >=" + min + " and <=" + max);
        }
    }


    /**
     * Ensures that the given value is not <code>null</code>.
     *
     * @param val the value to check
     * @param name the name of this variable, for debugging purposes
     *
     * @throws IllegalArgumentException if the given value is <code>null</code>
     */
    public static final void checkNull(Object val, String name)
            throws IllegalArgumentException {
        if (val == null) {
            throw new IllegalArgumentException("value of " + name
                    + " cannot be null");
        }
    }

    /**
     * Ensures that no element of the given array is <code>null</code>.
     *
     * @param array an array of objects
     * @param arrayName the name of this array, for debugging purposes
     *
     * @throws IllegalArgumentException if an element of the given array is
     *         <code>null</code>
     */
    public static void checkNullElements(Object[] array, String arrayName)
            throws IllegalArgumentException {
        checkNull(array, arrayName);

        checkNullElements(array, arrayName, 0, array.length);
    }

    /**
     * Ensures that each of the given number of elements (starting at the given
     * index) of the given array are not <code>null</code>.
     *
     * @param array an array of objects
     * @param arrayName the name of this array, for debugging purposes
     * @param offset the index in the given array at which
     *        <code>len</code> elements must not be <code>null</code>
     * @param len the number of elements, starting at the given index, which
     *        must not be <code>null</code>
     *
     * @throws IllegalArgumentException if an element of the given array is
     *         <code>null</code>
     */
    public static void checkNullElements(Object[] array, String arrayName,
            int offset, int len) throws IllegalArgumentException {
        checkNull(array, arrayName);

        for (int i = offset, end = offset + len; i < end; i++) {
            if (array[i] == null) {
                throw new IllegalArgumentException("'" + arrayName
                        + "' array must not contain any null elements at "
                        + "indices " + offset + " through " + (offset + len)
                        + " (" + arrayName + "[" + i + "] == null)");
            }
        }
    }

    /**
     * Returns a copy of the given array, ensuring that no element of the
     * returned array is <code>null</code>. If any elements of the given array
     * are <code>null</code>, an <code>IllegalArgumentException</code> is
     * thrown. Note that if the given <code>array</code> is <code>null</code>,
     * this method will simply return <code>null</code>
     *
     * @param array the array to clone and check for <code>null</code> elements
     * @param name the name of the array variable, for debugging purposes
     * @return a copy of the given array without any <code>null</code> elements,
     *         or <code>null</code> if the given array is <code>null</code>
     *
     * @throws IllegalArgumentException if any elements of the given array are
     *         <code>null</code>
     */
    public static Object[] getNonnullArray(Object[] array, String name) 
            throws IllegalArgumentException {
        if (array == null) return null;

        array = (Object[]) array.clone();
        DefensiveTools.checkNullElements(array, name);

        return array;
    }

}
