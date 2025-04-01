package gov.usdot.cv.msg.builder.util;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class ObjectPrinter {
    public static void printObject(Object obj) {
        printObject(obj, 0);
    }

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

