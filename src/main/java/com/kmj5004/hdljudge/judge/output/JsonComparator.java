package com.kmj5004.hdljudge.judge.output;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class JsonComparator {

    private final ObjectMapper mapper;

    public boolean equalsJson(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        try {
            JsonNode l = mapper.readTree(left);
            JsonNode r = mapper.readTree(right);
            return l.equals(r);
        } catch (RuntimeException e) {
            return false;
        }
    }
}
