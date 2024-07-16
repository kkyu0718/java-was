package codesquad.http;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static codesquad.utils.StringUtils.LINE_SEPERATOR;

@Deprecated
public class Parameters {
    private Map<String, String> parameter;

    public Parameters() {
        this.parameter = new HashMap<>();
    }

    public static Parameters of(String paramString) {
        if (paramString == null) {
            return new Parameters();
        }

        Parameters parameters = new Parameters();
        String[] split = paramString.split("&");
        for (String entry : split) {
            String[] keyValue = entry.split("=");
            parameters.addParameter(keyValue[0], keyValue[1]);
        }

        return parameters;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String key : parameter.keySet()) {
            sb.append("(" + key + ", " + parameter.get(key) + ")").append(LINE_SEPERATOR);
        }
        return sb.toString();
    }

    public void extend(Parameters other) {
        parameter.putAll(other.getParameters());
    }
}
