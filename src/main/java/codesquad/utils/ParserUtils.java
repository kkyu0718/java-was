package codesquad.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParserUtils {

    public static Map<String, Object> parseJson(String json) {
        Map<String, Object> result = new HashMap<>();
        json = json.trim().substring(1, json.length() - 1); // Remove curly braces
        String[] pairs = splitKeyValuePairs(json);
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            String key = keyValue[0].trim().replaceAll("^\"|\"$", "");
            Object value = parseValue(keyValue[1].trim());
            result.put(key, value);
        }

        return result;
    }

    public static void populateObject(Object instance, Map<String, Object> jsonMap) {
        for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
            try {
                Field field = instance.getClass().getDeclaredField(entry.getKey());
                field.setAccessible(true); // To access private fields
                Object value = entry.getValue();
                if (value instanceof Map) {
                    Object nestedInstance = field.getType().getDeclaredConstructor().newInstance();
                    populateObject(nestedInstance, (Map<String, Object>) value);
                    field.set(instance, nestedInstance);
                } else if (value instanceof List) {
                    List<?> list = (List<?>) value;
                    List<Object> fieldList = new ArrayList<>();
                    for (Object item : list) {
                        if (item instanceof Map) {
                            Class<?> fieldType = (Class<?>) ((java.lang.reflect.ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                            Object nestedInstance = fieldType.getDeclaredConstructor().newInstance();
                            populateObject(nestedInstance, (Map<String, Object>) item);
                            fieldList.add(nestedInstance);
                        } else {
                            fieldList.add(item);
                        }
                    }
                    field.set(instance, fieldList);
                } else {
                    field.set(instance, value);
                }
            } catch (NoSuchFieldException | SecurityException e) {
                System.err.println("Error accessing field: " + entry.getKey());
                System.err.println("Error details: " + e.getMessage());
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                System.err.println("Cannot access field: " + entry.getKey());
                System.err.println("Error details: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("Unexpected error for field: " + entry.getKey());
                System.err.println("Error details: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static Object parseValue(String json) {
        json = json.trim();
        if (json.startsWith("{")) {
            return parseJson(json);
        } else if (json.startsWith("[")) {
            return parseArray(json);
        } else if (json.startsWith("\"")) {
            return json.substring(1, json.length() - 1); // Remove quotes
        } else if (json.equals("null")) {
            return null;
        } else if (json.equals("true") || json.equals("false")) {
            return Boolean.parseBoolean(json);
        } else {
            try {
                return Integer.parseInt(json);
            } catch (NumberFormatException e) {
                try {
                    return Double.parseDouble(json);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Unknown value: " + json);
                }
            }
        }
    }

    private static List<Object> parseArray(String json) {
        List<Object> result = new ArrayList<>();
        json = json.trim().substring(1, json.length() - 1); // Remove square brackets
        String[] values = splitArrayValues(json);
        for (String value : values) {
            result.add(parseValue(value.trim()));
        }
        return result;
    }

    private static String[] splitKeyValuePairs(String json) {
        return splitJson(json, ',');
    }

    private static String[] splitArrayValues(String json) {
        return splitJson(json, ',');
    }

    private static String[] splitJson(String json, char delimiter) {
        List<String> tokens = new ArrayList<>();
        int start = 0;
        boolean inQuotes = false;
        int braceCount = 0;

        for (int i = 0; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (ch == '\"') {
                inQuotes = !inQuotes;
            } else if (!inQuotes && (ch == '{' || ch == '[')) {
                braceCount++;
            } else if (!inQuotes && (ch == '}' || ch == ']')) {
                braceCount--;
            } else if (!inQuotes && braceCount == 0 && ch == delimiter) {
                tokens.add(json.substring(start, i));
                start = i + 1;
            }
        }
        tokens.add(json.substring(start));

        return tokens.toArray(new String[0]);
    }
}
