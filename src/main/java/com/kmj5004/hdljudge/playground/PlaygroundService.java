package com.kmj5004.hdljudge.playground;

import com.kmj5004.hdljudge.common.enums.Language;
import com.kmj5004.hdljudge.domain.synthesis.SynthesisResult;
import com.kmj5004.hdljudge.domain.synthesis.SynthesisResultRepository;
import com.kmj5004.hdljudge.judge.HdlAdapterDispatcher;
import com.kmj5004.hdljudge.judge.JudgeProperties;
import com.kmj5004.hdljudge.judge.adapter.ResourceLimits;
import com.kmj5004.hdljudge.judge.adapter.SimulationOutcome;
import com.kmj5004.hdljudge.judge.adapter.SynthesisOptions;
import com.kmj5004.hdljudge.judge.adapter.SynthesisOutcome;
import com.kmj5004.hdljudge.judge.output.SvgColorNormalizer;
import com.kmj5004.hdljudge.playground.dto.SimulateRequest;
import com.kmj5004.hdljudge.playground.dto.SimulateResponse;
import com.kmj5004.hdljudge.playground.dto.SynthesizeRequest;
import com.kmj5004.hdljudge.playground.dto.SynthesizeResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaygroundService {

    private final HdlAdapterDispatcher dispatcher;
    private final JudgeProperties judgeProps;
    private final SynthesisProperties synthesisProps;
    private final SynthesisResultRepository synthesisResults;

    public SimulateResponse simulate(SimulateRequest req) {
        ResourceLimits limits = new ResourceLimits(
            judgeProps.limits().defaultTimeLimitNs(),
            judgeProps.limits().defaultWallTimeLimitMs(),
            judgeProps.limits().defaultMemoryLimitMb()
        );
        Language lang = req.resolvedLanguage();
        SimulationOutcome outcome = dispatcher.forLanguage(lang)
            .simulate(req.code(), req.testbench(), limits);
        return SimulateResponse.from(outcome);
    }

    @Transactional
    public SynthesizeResponse synthesize(SynthesizeRequest req) {
        Language lang = req.resolvedLanguage();

        String hash = sha256Hex(lang.name() + ":" + req.code());

        Optional<SynthesisResult> cached = synthesisResults.findByCodeHash(hash);
        if (cached.isPresent()) {
            SynthesisResult r = cached.get();
            return new SynthesizeResponse(
                true, true,
                SvgColorNormalizer.relaxBlack(r.getSvgContent()),
                r.getGateCount(), r.getFfCount(),
                synthesisProps.maxGateCount(), null
            );
        }

        SynthesisOutcome outcome = dispatcher.forLanguage(lang)
            .synthesize(req.code(), new SynthesisOptions(synthesisProps.maxGateCount()));

        if (!outcome.ok()) {
            return new SynthesizeResponse(
                false, false, null, 0, 0,
                synthesisProps.maxGateCount(), outcome.stderrTail()
            );
        }



        String svgToStore = outcome.svg() == null ? "" : outcome.svg();
        synthesisResults.save(SynthesisResult.builder()
            .codeHash(hash)
            .svgContent(svgToStore)
            .gateCount(outcome.gateCount())
            .ffCount(outcome.ffCount())
            .build());

        return new SynthesizeResponse(
            true, false,
            SvgColorNormalizer.relaxBlack(outcome.svg()),
            outcome.gateCount(), outcome.ffCount(),
            synthesisProps.maxGateCount(), outcome.stderrTail()
        );
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
