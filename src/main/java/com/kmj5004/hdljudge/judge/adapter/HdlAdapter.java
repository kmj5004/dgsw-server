package com.kmj5004.hdljudge.judge.adapter;

import com.kmj5004.hdljudge.common.enums.Language;

public interface HdlAdapter {

    Language language();

    SimulationOutcome simulate(String userCode, String testbench, ResourceLimits limits);

    SynthesisOutcome synthesize(String userCode, SynthesisOptions options);
}
