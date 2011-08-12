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
 *  File created by keith @ Mar 28, 2003
 *
 */

package net.kano.joscar;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Provides a set of miscellaneous tools used throughout joscar.
 */
public final class MiscTools {
    /**
     * This private constructor is never called, ensuring that an instance of
     * <code>MiscTools</code> will not be created.
     */
    private MiscTools() { }

    /**
     * Returns the class name of the given object, with the package name
     * stripped off. There must be a better way to do this.
     *
     * @param obj the object whose class name will be returned
     * @return the class name of the given object, without package name
     */
    public static String getClassName(Object obj) {
        return getClassName(obj.getClass());
    }

    /**
     * Returns the name of the given class, with the package name stripped off.
     *
     * @param cl the class whose name will be returned
     * @return the name of the given class, without package name
     */
    public static String getClassName(Class cl) {
        return getClassName(cl.getName());
    }

    /**
     * Returns the class name of the given fully qualified class name. For
     * example, <code>getClassName("net.kano.joscar.MiscTools")</code> would
     * produce the string <code>"MiscTools"</code>.
     *
     * @param fullName the fully qualified class name
     * @return the given fully qualified class name, without the package name
     */
    public static String getClassName(String fullName) {
        return fullName.substring(fullName.lastIndexOf('.') + 1);
    }

    /**
     * Finds a <code>static final</code> field of the given class whose name
     * matches the given pattern and whose value is equal to the given
     * <code>value</code>. The field's name is returned, or <code>null</code> if
     * no field is found.
     *
     * @param cl a class
     * @param value the value to search for
     * @param pattern a regular expression pattern that matches the fields to be
     *        searched, or <code>null</code> to match all fields of the class
     * @return the name of a field matching the given constraints
     */
    public static String findIntField(Class cl, long value, String pattern) {
        Field[] fields = cl.getFields();
        Pattern p = pattern == null ? null : Pattern.compile(pattern);

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];

            int modifiers = field.getModifiers();

            // only accept static final fields
            if (!Modifier.isStatic(modifiers) || !Modifier.isFinal(modifiers)) {
                continue;
            }

            String fieldName = field.getName();
            if (p != null && !p.matcher(fieldName).matches()) continue;

            try {
                if (field.getLong(null) == value) return fieldName;
            } catch (IllegalAccessException e) {
                continue;
            }
        }

        // we couldn't find anything
        return null;
    }

    /**
     * Finds a <code>static final</code> field of the given class whose name
     * matches the given pattern and whose value is equal to the given
     * <code>value</code>. The field's name is returned, or <code>null</code> if
     * no field is found.
     *
     * @param cl a class
     * @param value the value to search for
     * @param pattern a regular expression pattern that matches the fields to be
     *        searched, or <code>null</code> to match all fields of the class
     * @return the name of a field matching the given constraints
     */
    public static String findEqualField(Class cl, Object value, String pattern) {
        Pattern p = pattern == null ? null : Pattern.compile(pattern);

        Field[] fields = cl.getFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];

            int modifiers = field.getModifiers();
            if (!Modifier.isStatic(modifiers) || !Modifier.isFinal(modifiers)) {
                continue;
            }

            String fieldName = field.getName();
            if (p != null && !p.matcher(fieldName).matches()) continue;

            Object fieldValue;
            try {
                fieldValue = field.get(null);
            } catch (IllegalAccessException e) {
                continue;
            }
            if (fieldValue == null) {
                if (value == null) return fieldName;
            } else {
                // fieldValue is not null
                if (fieldValue.equals(value)) return fieldName;
            }
        }

        // we didn't find anything
        return null;
    }

    /**
     * Returns a list of names of public static final fields in the given class
     * whose values' "on" bits are a subset of the "on" bits in the given
     * value's binary representation, and whose name matches the given regular
     * expression pattern.
     * <p>
     * For example, given the class:
     * <pre>
class Something {
     private static final int FLAG_A = 0x04;
     private static final int FLAG_B = 0x02;
     private static final int FLAG_C = 0x08;
}
</pre>
     * The call <code>MiscTools.findFlagFields(Something.class, 0x02 | 0x08 |
     * 0x01 | 0x40, Pattern.compile("FLAG_.*"))</code> would return an array
     * containing <code>"FLAG_B"</code> and <code>"FLAG_C"</code>.
     *
     * @param cl the class containing the fields
     * @param value the value to match
     * @param p the regular expression pattern that matching fields must match
     * @return an array of field names matching the given criteria
     *
     * @see #getFlagFieldsString(Class, long, String)
     */
    public static String[] findFlagFields(Class cl, long value,
            String p) {
        Field[] fields = cl.getFields();
        SortedMap matches = new TreeMap(Collections.reverseOrder());
        Pattern pattern = p == null ? null : Pattern.compile(p);

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];

            int modifiers = field.getModifiers();

            // only accept static final fields
            if (!Modifier.isStatic(modifiers) || !Modifier.isFinal(modifiers)) {
                continue;
            }

            String fieldName = field.getName();
            if (pattern != null && !pattern.matcher(fieldName).matches()) {
                continue;
            }

            long fieldValue;
            try {
                fieldValue = field.getLong(null);
            } catch (IllegalAccessException e) {
                continue;
            }

            if ((value & fieldValue) == fieldValue) {
                matches.put(new Long(fieldValue), fieldName);
            }
        }

        Collection values = matches.values();
        return (String[]) values.toArray(new String[values.size()]);
    }

    /**
     *
     * Returns a textual list of names of public static final fields in the
     * given class whose values' "on" bits are a subset of the "on" bits in the
     * given value's binary representation, and whose name matches the given
     * regular expression pattern. If any bits in the given value are not set in
     * any of the matching fields, a number whose binary representation contains
     * the remaining bits will be appended to the end of the string.
     * <p>
     * For example, given the class:
     * <pre>
class Something {
    private static final int FLAG_A = 0x04;
    private static final int FLAG_B = 0x02;
    private static final int FLAG_C = 0x08;
}
     </pre>
     * The call <code>MiscTools.findFlagFields(Something.class, 0x02 | 0x08 |
     * 0x01 | 0x40, Pattern.compile("FLAG_.*"))</code> would return
     * <code>"FLAG_B | FLAG_C | 0x41"</code>.
     *
     * @param cl the class containing the fields
     * @param value the value to match
     * @param pattern the regular expression pattern that matching fields must
     *        match
     * @return a string containing the names of the matching fields
     */
    public static String getFlagFieldsString(Class cl, long value,
            String pattern) {
        String[] fields = findFlagFields(cl, value, pattern);

        StringBuffer b = new StringBuffer();
        long covered = 0;
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            long fieldValue = 0;
            try {
                fieldValue = cl.getField(field).getLong(null);
            } catch (NoSuchFieldException ignored) {
            } catch (SecurityException ignoredToo) {
            } catch (IllegalAccessException ignoredThree) {
            }
            if ((covered | fieldValue) == covered) continue;

            covered |= fieldValue;

            if (i != 0) b.append(" | ");
            b.append(field);
        }

        if (covered != 0 && (value & ~covered) != 0) {
            b.append(" | 0x");
            b.append(Long.toHexString(value & ~covered));
        }

        return b.toString();
    }
}
