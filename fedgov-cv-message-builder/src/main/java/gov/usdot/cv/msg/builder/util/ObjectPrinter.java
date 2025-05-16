/*
 * Copyright (C) 2025 LEIDOS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package gov.usdot.cv.msg.builder.util;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class ObjectPrinter {
    public static void printObject(Object obj) {
        printObject(obj, 0);
    }

    // This method prints the passed Java Object along with the assigned data values
    private static void printObject(Object obj, int indent) {
        if (obj == null) {
            System.out.println(getIndent(indent) + "null");
            return;
        }

        Class<?> objClass = obj.getClass();

        // Handle primitive types, wrappers, and String
        if (objClass.isPrimitive() || obj instanceof String || obj instanceof Number || obj instanceof Boolean || obj instanceof Character) {
            System.out.println(getIndent(indent) + obj);
            return;
        }

        // Handle arrays
        if (objClass.isArray()) {
            // Handle primitive type arrays (e.g., int[], char[], etc.)
            if (obj instanceof int[]) {
                int[] array = (int[]) obj;
                System.out.println(getIndent(indent) + "[");
                for (int element : array) {
                    System.out.println(getIndent(indent + 2) + element);
                }
                System.out.println(getIndent(indent) + "]");
            } else if (obj instanceof char[]) {
                char[] array = (char[]) obj;
                System.out.println(getIndent(indent) + "[");
                for (char element : array) {
                    System.out.println(getIndent(indent + 2) + element);
                }
                System.out.println(getIndent(indent) + "]");
            } else {
                // Handle general object arrays
                Object[] array = (Object[]) obj;
                System.out.println(getIndent(indent) + "[");
                for (Object element : array) {
                    printObject(element, indent + 2);
                }
                System.out.println(getIndent(indent) + "]");
            }
            return;
        }

        // Handle Collections (Lists, Sets, etc.)
        if (obj instanceof Collection) {
            System.out.println(getIndent(indent) + "[");
            for (Object element : (Collection<?>) obj) {
                printObject(element, indent + 2);
            }
            System.out.println(getIndent(indent) + "]");
            return;
        }

        // Handle Maps (Key-Value pairs)
        if (obj instanceof Map) {
            System.out.println(getIndent(indent) + "{");
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet()) {
                System.out.print(getIndent(indent + 2) + entry.getKey() + " : ");
                printObject(entry.getValue(), indent + 2);
            }
            System.out.println(getIndent(indent) + "}");
            return;
        }

        // Handle general objects
        System.out.println(getIndent(indent) + objClass.getSimpleName() + " {");

        Field[] fields = objClass.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(obj);
                System.out.print(getIndent(indent + 2) + field.getName() + " = ");
                printObject(fieldValue, indent + 2);
            } catch (IllegalAccessException e) {
                System.out.println(getIndent(indent + 2) + field.getName() + " = [access denied]");
            }
        }

        System.out.println(getIndent(indent) + "}");
    }

    private static String getIndent(int indent) {
        return String.join("", Collections.nCopies(indent, " "));
    }
}

