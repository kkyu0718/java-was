package codesquad.http;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Parameters {
    private Map<String, String> parameter;

    private Parameters(Map<String, String> parameter) {
        this.parameter = parameter;
    }

    public static Parameters of(String paramString) {
        HashMap<String, String> map = new HashMap<>();
        String[] split = paramString.split("&");
        for (String entry : split) {
            String[] keyValue = entry.split("=");
            map.put(keyValue[0], keyValue[1]);
        }

        return new Parameters(map);
    }

    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(parameter);
    }

    public String getParameter(String key) {
        if (!parameter.containsKey(key)) {
            throw new IllegalArgumentException("해당 parameter 가 존재하지 않습니다." + key);
        }
        return parameter.get(key);
    }

    public void addParameter(String key, String value) {
        parameter.put(key, value);
    }

    public int size() {
        return parameter.size();
    }
}
