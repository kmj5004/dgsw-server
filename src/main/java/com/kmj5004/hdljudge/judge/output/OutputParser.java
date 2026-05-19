package com.kmj5004.hdljudge.judge.output;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;





@Component
public class OutputParser {

    private static final Pattern MARKER = Pattern.compile(
        "::HDLJUDGE_VEC::ordering=(\\d+)::output=(.+)"
    );

    public Map<Integer, String> parse(String stdout) {
        Map<Integer, String> result = new LinkedHashMap<>();
        if (stdout == null || stdout.isEmpty()) {
            return result;
        }
        for (String rawLine : stdout.split("\\R")) {
            String line = rawLine.trim();
            Matcher m = MARKER.matcher(line);
            if (m.matches()) {
                int ordering = Integer.parseInt(m.group(1));
                String json = m.group(2);
                result.put(ordering, json);
            }
        }
        return result;
    }
}
