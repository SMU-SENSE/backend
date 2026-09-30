package com.aac.ieojwo.report.service;

import com.aac.ieojwo.alert.domain.AlertType;
import com.aac.ieojwo.alert.service.AlertService;
import com.aac.ieojwo.common.exception.BadRequestException;
import com.aac.ieojwo.device.domain.AacDevice;
import com.aac.ieojwo.live.LiveEventService;
import com.aac.ieojwo.report.domain.Emotion;
import com.aac.ieojwo.report.domain.SensorEvent;
import com.aac.ieojwo.report.domain.SensorEventType;
import com.aac.ieojwo.report.repository.SensorEventRepository;
import com.aac.ieojwo.user.domain.UserStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
public class SensorService {
    public record SensorRequest(SensorEventType type, Double numericValue, String label,
                                Instant recordedAt) {
    }

    public record ExpressionRequest(@NotNull Emotion emotion,
                                    @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double confidence,
                                    Instant occurredAt) {
    }

    public record SensorResponse(Long id, SensorEventType type, Double numericValue, String label,
                                 Instant recordedAt) {
        public static SensorResponse from(SensorEvent event) {
            return new SensorResponse(event.getId(), event.getEventType(), event.getNumericValue(),
                    event.getLabel(), event.getRecordedAt());
        }
    }

    private final SensorEventRepository repository;
    private final AlertService alerts;
    private final LiveEventService live;

    public SensorService(SensorEventRepository repository, AlertService alerts, LiveEventService live) {
        this.repository = repository;
        this.alerts = alerts;
        this.live = live;
    }

    @Transactional
    public SensorResponse record(AacDevice device, SensorRequest request) {
        validate(request);
        SensorEvent event = repository.save(SensorEvent.create(
                device, request.type(), request.numericValue(), normalizeLabel(request),
                request.recordedAt()));
        if (request.type() == SensorEventType.HEART_RATE && request.numericValue() >= 110) {
            alerts.create(device.getUser(), AlertType.HEART_RATE, "고심박 감지",
                    "심박수 " + request.numericValue().intValue() + " BPM이 감지되었습니다.");
        }
        SensorResponse response = SensorResponse.from(event);
        live.publish(device.getUser().getId(), "SENSOR_EVENT", response);
        return response;
    }

    @Transactional
    public SensorResponse recordExpression(AacDevice device, ExpressionRequest request) {
        return record(device, new SensorRequest(SensorEventType.EXPRESSION,
                request.confidence(), request.emotion().name(), request.occurredAt()));
    }

    @Transactional
    public void emergency(AacDevice device, String message) {
        device.getUser().updateStatus(UserStatus.EMERGENCY);
        alerts.create(device.getUser(), AlertType.EMERGENCY, "긴급 모드",
                message == null || message.isBlank()
                        ? "사용자 기기에서 긴급 모드가 실행되었습니다." : message);
        live.publish(device.getUser().getId(), "STATUS_UPDATED", Map.of("status", "EMERGENCY"));
    }

    private void validate(SensorRequest request) {
        if (request.type() == null) throw new BadRequestException("센서 유형이 필요합니다.");
        if (request.type() == SensorEventType.HEART_RATE &&
                (request.numericValue() == null || request.numericValue() < 20 || request.numericValue() > 250)) {
            throw new BadRequestException("심박수 범위가 올바르지 않습니다.");
        }
        if (request.type() == SensorEventType.EXPRESSION) {
            if (request.numericValue() == null || request.numericValue() < 0 || request.numericValue() > 1) {
                throw new BadRequestException("표정 confidence는 0과 1 사이여야 합니다.");
            }
            try {
                Emotion.valueOf(request.label());
            } catch (Exception exception) {
                throw new BadRequestException("지원하지 않는 표정 값입니다.");
            }
        }
    }

    private String normalizeLabel(SensorRequest request) {
        return request.type() == SensorEventType.EXPRESSION
                ? Emotion.valueOf(request.label()).name()
                : request.label();
    }
}
