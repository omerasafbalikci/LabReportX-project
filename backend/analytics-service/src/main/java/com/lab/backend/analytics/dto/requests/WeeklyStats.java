package com.lab.backend.analytics.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeeklyStats {
    private String eventId;
    private LocalDateTime timestamp;
    private Map<String, Long> weeklyStats;
}
