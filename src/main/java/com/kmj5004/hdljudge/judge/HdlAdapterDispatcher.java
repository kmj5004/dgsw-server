package com.kmj5004.hdljudge.judge;

import com.kmj5004.hdljudge.common.enums.Language;
import com.kmj5004.hdljudge.common.error.ApiException;
import com.kmj5004.hdljudge.common.error.ErrorCode;
import com.kmj5004.hdljudge.judge.adapter.HdlAdapter;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;










@Slf4j
@Component
public class HdlAdapterDispatcher {

    private final Map<Language, HdlAdapter> byLanguage;

    public HdlAdapterDispatcher(List<HdlAdapter> adapters) {
        Map<Language, HdlAdapter> map = new EnumMap<>(Language.class);
        for (HdlAdapter adapter : adapters) {
            HdlAdapter previous = map.put(adapter.language(), adapter);
            if (previous != null && previous != adapter) {
                log.info("HdlAdapter for {} overridden: {} → {}",
                    adapter.language(), previous.getClass().getSimpleName(), adapter.getClass().getSimpleName());
            }
        }
        this.byLanguage = Collections.unmodifiableMap(map);
        log.info("HdlAdapterDispatcher initialized with {} language(s): {}", map.size(), map.keySet());
    }

    public HdlAdapter forLanguage(Language language) {
        HdlAdapter adapter = byLanguage.get(language);
        if (adapter == null) {
            throw new ApiException(ErrorCode.JUDGE_FAILURE,
                "No HDL adapter registered for language " + language);
        }
        return adapter;
    }

    public boolean supports(Language language) {
        return byLanguage.containsKey(language);
    }
}
