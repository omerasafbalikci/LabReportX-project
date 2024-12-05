package com.lab.backend.analytics.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for weekly stats.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeeklyStats implements Serializable {
    private String eventId;
    private LocalDateTime timestamp;
    private Map<String, Long> weeklyStats;
}
