package codesquad;

import codesquad.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class HttpBody {
    private List<String> lines; //TODO mime type 에 따른 구현 필요

    public HttpBody() {
        lines = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line).append(StringUtils.LINE_SEPERATOR);
        }

        return sb.toString();
    }
}
