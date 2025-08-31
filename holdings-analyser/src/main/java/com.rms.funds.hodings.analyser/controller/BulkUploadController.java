package com.rms.funds.hodings.analyser.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/bulk-operations")
@RequiredArgsConstructor
@CrossOrigin("*")
public class BulkUploadController {

    @PostMapping(value = "/mutual-funds")
    public void uploadMutualFunds() {

    }
}
