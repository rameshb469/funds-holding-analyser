package com.rms.funds.hodings.analyser.controller;

import com.rms.funds.hodings.analyser.controller.dto.MutualFundConfigDto;
import com.rms.funds.hodings.analyser.service.MutualFundConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/mf-configs")
@RequiredArgsConstructor
@CrossOrigin("*")
public class MFConfigController {

    private final MutualFundConfigService mutualFundConfigService;

    @GetMapping
    public List<MutualFundConfigDto> getAll() {
        return mutualFundConfigService.findAll();
    }

    @PostMapping
    public MutualFundConfigDto create(MutualFundConfigDto configDto){
        return mutualFundConfigService.create(configDto);
    }
}
