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
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class ChartService {
    public byte[] generateChart(WeeklyStats weeklyStats) {
        Map<String, Long> stats = weeklyStats.getWeeklyStats();

        String[] dates = stats.keySet().toArray(new String[0]);
        Number[] counts = stats.values().toArray(new Number[0]);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<Date> convertedDates = Arrays.stream(dates)
                .map(date -> Date.from(LocalDate.parse(date, formatter)
                        .atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .toList();

        XYChart chart = new XYChartBuilder()
                .width(800)
                .height(600)
                .title("Weekly Patient Registrations")
                .xAxisTitle("Date")
                .yAxisTitle("Number of Patients")
                .build();

        chart.addSeries("Patients", convertedDates, Arrays.asList(counts));

        chart.getStyler().setDatePattern("yyyy-MM-dd");
        chart.getStyler().setLegendPosition(org.knowm.xchart.style.Styler.LegendPosition.InsideNW);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setXAxisLabelRotation(45);
        chart.getStyler().setPlotGridLinesVisible(true);
        chart.getStyler().setYAxisDecimalPattern("0");
        chart.getStyler().setChartBackgroundColor(java.awt.Color.WHITE);
        chart.getStyler().setPlotBackgroundColor(new java.awt.Color(250, 250, 250));
        chart.getStyler().setAxisTicksMarksVisible(true);

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            BitmapEncoder.saveBitmap(chart, bos, BitmapEncoder.BitmapFormat.PNG);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new FailedGenerateChartException("Error generating chart: " + e.getMessage() + e);
        }
    }
}
