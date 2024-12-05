package com.lab.backend.analytics.service.concretes;

import com.lab.backend.analytics.dto.requests.WeeklyStats;
import com.lab.backend.analytics.utilities.exceptions.FailedGenerateChartException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Log4j2
@Service
@RequiredArgsConstructor
public class ChartService {
    public byte[] generateChart(WeeklyStats weeklyStats) {
        try {
            // Create dataset
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            weeklyStats.getWeeklyStats().forEach((day, count) -> dataset.addValue(count, "Patients", day));

            // Create the chart
            JFreeChart chart = ChartFactory.createBarChart(
                    "Weekly Patient Density",    // Chart title
                    "Days",                      // X-Axis label
                    "Number of Patients",        // Y-Axis label
                    dataset,                     // Dataset
                    PlotOrientation.VERTICAL,    // Orientation
                    false,                       // Include legend
                    true,                        // Tooltips
                    false                        // URLs
            );

            // Customize chart appearance
            chart.setTitle(new TextTitle(
                    "Weekly Patient Density Overview",
                    new Font("SansSerif", Font.BOLD, 18)
            ));

            chart.getCategoryPlot().getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
            chart.getCategoryPlot().getRangeAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
            chart.getCategoryPlot().setBackgroundPaint(Color.WHITE);
            chart.getCategoryPlot().setDomainGridlinePaint(Color.LIGHT_GRAY);
            chart.getCategoryPlot().setRangeGridlinePaint(Color.LIGHT_GRAY);

            // Render chart as a PNG image
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(out, chart, 800, 600);
            return out.toByteArray();
        } catch (IOException e) {
            throw new FailedGenerateChartException("Failed to generate chart image " + e);
        }
    }
}
