package com.kmj5004.hdljudge.playground.dto;

import com.kmj5004.hdljudge.common.enums.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SimulateRequest(
    @NotBlank @Size(max = 65535) String code,
    @NotBlank @Size(max = 65535) String testbench,
    Language language
) {


    public Language resolvedLanguage() {
        return language != null ? language : Language.VERILOG;
    }
}
