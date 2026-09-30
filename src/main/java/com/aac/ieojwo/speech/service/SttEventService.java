package com.aac.ieojwo.speech.service;

import com.aac.ieojwo.device.domain.AacDevice;
import com.aac.ieojwo.live.LiveEventService;
import com.aac.ieojwo.speech.domain.SttEvent;
import com.aac.ieojwo.speech.dto.SttEventRequest;
import com.aac.ieojwo.speech.dto.SttEventResponse;
import com.aac.ieojwo.speech.repository.SttEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SttEventService {
    private final SttEventRepository repository;
    private final LiveEventService live;

    public SttEventService(SttEventRepository repository, LiveEventService live) {
        this.repository = repository;
        this.live = live;
    }

    @Transactional
    public SttEventResponse record(AacDevice device, SttEventRequest request) {
        SttEvent event = repository.save(SttEvent.create(
                device, request.recognizedText(), request.successful(), request.confidence(),
                request.occurredAt()));
        SttEventResponse response = SttEventResponse.from(event);
        live.publish(device.getUser().getId(), "STT_EVENT", response);
        return response;
    }
}
