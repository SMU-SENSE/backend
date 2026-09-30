package com.aac.ieojwo.common.service;

import com.aac.ieojwo.board.repository.CardUsageLogRepository;
import com.aac.ieojwo.common.config.EventRetentionProperties;
import com.aac.ieojwo.location.repository.LocationSampleRepository;
import com.aac.ieojwo.report.repository.SensorEventRepository;
import com.aac.ieojwo.speech.repository.SttEventRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class EventRetentionService {
    private final EventRetentionProperties properties;
    private final CardUsageLogRepository cardUsageLogs;
    private final SttEventRepository sttEvents;
    private final SensorEventRepository sensorEvents;
    private final LocationSampleRepository locationSamples;
    private final Clock clock;

    @Autowired
    public EventRetentionService(EventRetentionProperties properties,
                                 CardUsageLogRepository cardUsageLogs,
                                 SttEventRepository sttEvents,
                                 SensorEventRepository sensorEvents,
                                 LocationSampleRepository locationSamples) {
        this(properties, cardUsageLogs, sttEvents, sensorEvents, locationSamples, Clock.systemUTC());
    }

    EventRetentionService(EventRetentionProperties properties,
                          CardUsageLogRepository cardUsageLogs,
                          SttEventRepository sttEvents,
                          SensorEventRepository sensorEvents,
                          LocationSampleRepository locationSamples,
                          Clock clock) {
        this.properties = properties;
        this.cardUsageLogs = cardUsageLogs;
        this.sttEvents = sttEvents;
        this.sensorEvents = sensorEvents;
        this.locationSamples = locationSamples;
        this.clock = clock;
    }

    @Scheduled(cron = "${app.retention.cleanup-cron:0 0 4 * * *}")
    @Transactional
    public void purgeExpiredEvents() {
        Instant now = clock.instant();
        delete(properties.cardUsage(), now, cardUsageLogs::deleteByOccurredAtBefore);
        delete(properties.stt(), now, sttEvents::deleteByRecordedAtBefore);
        delete(properties.sensor(), now, sensorEvents::deleteByRecordedAtBefore);
        delete(properties.location(), now, locationSamples::deleteByRecordedAtBefore);
    }

    private void delete(Duration retention, Instant now, DeleteOperation operation) {
        if (retention != null && !retention.isNegative() && !retention.isZero()) {
            operation.deleteBefore(now.minus(retention));
        }
    }

    @FunctionalInterface
    private interface DeleteOperation {
        long deleteBefore(Instant cutoff);
    }
}
