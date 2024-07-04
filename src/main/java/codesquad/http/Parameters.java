package codesquad.http;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Parameters {
    private HashMap<String, String> parameter;

    private Parameters(HashMap<String, String> parameter) {
        this.parameter = parameter;
    }

    public static Parameters of(String paramString) {
        HashMap<String, String> map = new HashMap<>();
        String[] split = paramString.split("\\?");
        for (String entry : split) {
            String[] keyValue = entry.split(":");
            map.put(keyValue[0], keyValue[1]);
        }

        return new Parameters(map);
    }

    public Map<String, String> getParameter() {
        return Collections.unmodifiableMap(parameter);
    }

    public void addParameter(String key, String value) {
        parameter.put(key, value);
    }
}
