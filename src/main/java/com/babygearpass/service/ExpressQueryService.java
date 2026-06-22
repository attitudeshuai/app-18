package com.babygearpass.service;

import com.babygearpass.dto.logistics.ExpressTrackResult;

public interface ExpressQueryService {

    ExpressTrackResult queryTrack(String trackingNumber, String expressCompanyCode);

    String getProviderName();

    boolean isAvailable();
}
