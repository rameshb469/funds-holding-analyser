package com.rms.funds.hodings.analyser.controller;

import com.rms.funds.hodings.analyser.controller.dto.DownloadLinkCreateRequest;
import com.rms.funds.hodings.analyser.service.DownloadLinkManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/mf-download-url")
@RequiredArgsConstructor
@CrossOrigin("*")
public class DownloadLinkManageController {

    private final DownloadLinkManageService service;

    @PostMapping
    public DownloadLinkCreateRequest upsert(@RequestBody DownloadLinkCreateRequest request) {
        return service.upsert(request);
    }

    @GetMapping
    public DownloadLinkCreateRequest getAll() {
        return service.getAll();
    }
}
