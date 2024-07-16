package codesquad.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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

    public static <T> T parseXWWWFormUrlEncoded(String data, Class<T> clazz) {
        T instance = getNewInstance(clazz);

        String decoded = URLDecoder.decode(data, StandardCharsets.UTF_8);
        String[] pairs = decoded.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = keyValue[1];
                try {
                    Field field = clazz.getDeclaredField(key);
                    field.setAccessible(true);
                    field.set(instance, convertStringToFieldType(field, value));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    // 필드를 찾을 수 없거나 접근할 수 없는 경우 예외 처리
                    System.err.println("Error setting field: " + key);
                    e.printStackTrace();
                }
            }
        }

        return instance;
    }

    private static <T> T getNewInstance(Class<T> clazz) {
        T instance;
        try {
            instance = clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException("Error creating new instance", e);
        }
        return instance;
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

    private static Object convertStringToFieldType(Field field, String value) {
        Class<?> type = field.getType();
        if (type == String.class) {
            return value;
        } else if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        } else if (type == long.class || type == Long.class) {
            return Long.parseLong(value);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (type == float.class || type == Float.class) {
            return Float.parseFloat(value);
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        }
        // 다른 타입에 대한 변환 로직 추가 가능
        return value; // 기본적으로 문자열 반환
    }
}
