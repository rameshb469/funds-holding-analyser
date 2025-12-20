package com.rms.funds.holdings.analyser.service;

import com.rms.funds.holdings.analyser.controller.dto.DownloadLinkCreateRequest;

public interface DownloadLinkManageService {

    DownloadLinkCreateRequest upsert(DownloadLinkCreateRequest request);

    DownloadLinkCreateRequest getAll();

}
