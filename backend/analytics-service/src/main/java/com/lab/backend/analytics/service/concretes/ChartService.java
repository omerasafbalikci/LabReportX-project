package com.lab.backend.analytics.service.concretes;

import com.lab.backend.analytics.dto.requests.WeeklyStats;
import com.lab.backend.analytics.utilities.exceptions.FailedGenerateChartException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class ChartService {
    public byte[] generateChart(WeeklyStats weeklyStats) {
        Map<String, Long> stats = weeklyStats.getWeeklyStats();

        String[] dates = stats.keySet().toArray(new String[0]);
        Number[] counts = stats.values().toArray(new Number[0]);

        XYChart chart = new XYChartBuilder()
                .width(800)
                .height(600)
                .title("Weekly Patient Registrations")
                .xAxisTitle("Date")
                .yAxisTitle("Number of Patients")
                .build();

        chart.addSeries("Patients", Arrays.asList(dates), Arrays.asList(counts));
        chart.getStyler().setDatePattern("yyyy-MM-dd");
        chart.getStyler().setLegendPosition(org.knowm.xchart.style.Styler.LegendPosition.InsideNW);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            BitmapEncoder.saveBitmap(chart, bos, BitmapEncoder.BitmapFormat.PNG);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new FailedGenerateChartException("Error generating chart " + e);
        }
    }
}
