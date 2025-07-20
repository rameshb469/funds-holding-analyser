package com.rms.funds.hodings.analyser.service;

import com.rms.funds.hodings.analyser.controller.dto.DownloadLinkCreateRequest;

public interface DownloadLinkManageService {

    DownloadLinkCreateRequest upsert(DownloadLinkCreateRequest request);

    DownloadLinkCreateRequest getAll();

}
