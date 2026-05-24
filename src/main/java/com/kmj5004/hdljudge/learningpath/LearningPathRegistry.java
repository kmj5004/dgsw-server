package com.kmj5004.hdljudge.learningpath;

import com.kmj5004.hdljudge.learningpath.dto.LearningPath;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;





@Slf4j
@Component
@RequiredArgsConstructor
public class LearningPathRegistry {

    private final ObjectMapper mapper;
    private Map<String, LearningPath> bySlug = Map.of();

    @PostConstruct
    void load() {
        var resolver = new PathMatchingResourcePatternResolver();
        Map<String, LearningPath> map = new LinkedHashMap<>();
        try {
            Resource[] resources = resolver.getResources("classpath:/paths/*.json");
            for (Resource r : resources) {
                try (InputStream in = r.getInputStream()) {
                    LearningPath p = mapper.readValue(in, LearningPath.class);
                    LearningPath previous = map.put(p.slug(), p);
                    if (previous != null) {
                        log.warn("Duplicate learning-path slug '{}' — last wins", p.slug());
                    }
                } catch (Exception e) {
                    log.error("Failed to load path resource {}", r.getFilename(), e);
                }
            }
        } catch (Exception e) {
            log.warn("paths/*.json scan failed", e);
        }
        this.bySlug = Collections.unmodifiableMap(map);
        log.info("LearningPathRegistry loaded {} path(s): {}", bySlug.size(), bySlug.keySet());
    }

    public List<LearningPath> all() {
        return List.copyOf(bySlug.values());
    }

    public LearningPath findBySlug(String slug) {
        return bySlug.get(slug);
    }
}
