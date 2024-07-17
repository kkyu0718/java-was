package codesquad.utils;

import codesquad.model.MultipartFile;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserUtils {

    public static <T> T parseJson(String json, Class<T> clazz) {
        // UTF-8로 인코딩 변환
        String utf8Json = new String(json.getBytes(), StandardCharsets.UTF_8);
        T instance = getNewInstance(clazz);
        Map<String, Object> result = parseJsonRecursive(utf8Json);

        populateObject(instance, result);
        return instance;
    }

    private static Map<String, Object> parseJsonRecursive(String json) {
        Map<String, Object> result = new HashMap<>();
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1); // Remove curly braces
        }
        if (json.isEmpty()) {
            return result;
        }
        String[] pairs = splitKeyValuePairs(json);
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replaceAll("^\"|\"$", "");
                Object value = parseValue(keyValue[1].trim());
                result.put(key, value);
            }
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
        if (json.isEmpty()) {
            return null;
        }
        if (json.startsWith("{")) {
            return parseJsonRecursive(json);
        } else if (json.startsWith("[")) {
            return parseArray(json);
        } else if (json.startsWith("\"") && json.endsWith("\"")) {
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
                    // If it's not a recognized format, return as is
                    return json;
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

    public static Map<String, Object> parseMultiPartFormData(byte[] bytes, String boundary) {
        Map<String, Object> resultMap = new HashMap<>();
        List<byte[]> parts = splitBytesByBoundary(bytes, boundary);

        for (byte[] part : parts) {
            if (part.length == 0) continue;

            int headerEndIndex = findHeaderEnd(part);
            if (headerEndIndex == -1) continue;

            byte[] headerBytes = Arrays.copyOfRange(part, 0, headerEndIndex);
            byte[] bodyBytes = Arrays.copyOfRange(part, headerEndIndex + 4, part.length);

            String headers = new String(headerBytes, StandardCharsets.UTF_8).trim();

            // Content-Disposition 헤더를 추출하여 이름과 파일명을 파싱
            Pattern contentDispositionPattern = Pattern.compile("Content-Disposition: form-data; name=\"(.*?)\"(; filename=\"(.*?)\")?");
            Matcher matcher = contentDispositionPattern.matcher(headers);
            if (matcher.find()) {
                String name = matcher.group(1);
                String fileName = matcher.group(3);

                // Content-Type 헤더를 추출
                Pattern contentTypePattern = Pattern.compile("Content-Type: (.*)");
                Matcher contentTypeMatcher = contentTypePattern.matcher(headers);
                String contentType = null;
                if (contentTypeMatcher.find()) {
                    contentType = contentTypeMatcher.group(1).trim();
                }

                if (fileName != null) {
                    resultMap.put(name, new MultipartFile(fileName, contentType, bodyBytes));
                } else {
                    // 텍스트 데이터인 경우에만 String으로 변환
                    resultMap.put(name, new String(bodyBytes, StandardCharsets.UTF_8).trim());
                }
            }
        }

        return resultMap;
    }

    private static List<byte[]> splitBytesByBoundary(byte[] bytes, String boundary) {
        List<byte[]> parts = new ArrayList<>();
        byte[] boundaryBytes = ("--" + boundary).getBytes(StandardCharsets.UTF_8);
        int start = 0;
        int end;

        while ((end = findSequence(bytes, boundaryBytes, start)) != -1) {
            if (start != end) {
                parts.add(Arrays.copyOfRange(bytes, start, end));
            }
            start = end + boundaryBytes.length;
        }

        if (start < bytes.length) {
            parts.add(Arrays.copyOfRange(bytes, start, bytes.length));
        }

        return parts;
    }

    private static int findSequence(byte[] source, byte[] sequence, int start) {
        for (int i = start; i <= source.length - sequence.length; i++) {
            boolean found = true;
            for (int j = 0; j < sequence.length; j++) {
                if (source[i + j] != sequence[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return i;
            }
        }
        return -1;
    }

    private static int findHeaderEnd(byte[] part) {
        byte[] headerEnd = "\r\n\r\n".getBytes(StandardCharsets.UTF_8);
        return findSequence(part, headerEnd, 0);
    }

}
