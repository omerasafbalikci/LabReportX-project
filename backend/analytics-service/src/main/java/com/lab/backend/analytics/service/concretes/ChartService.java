package com.lab.backend.analytics.service.concretes;

import com.lab.backend.analytics.dto.requests.WeeklyStats;
import com.lab.backend.analytics.utilities.exceptions.FailedGenerateChartException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Service class for generating chart images using JFreeChart.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Service
@RequiredArgsConstructor
@Log4j2
public class ChartService {
    private static final String SANS_SERIF = "SansSerif";

    /**
     * Generates a line chart as a PNG image byte array.
     *
     * @param rowKey      the row key for the dataset
     * @param title       the title of the chart
     * @param label       the label for the Y-axis
     * @param text        additional text to display as the chart title
     * @param weeklyStats the data object containing weekly statistics
     * @return a byte array representing the chart image in PNG format
     * @throws FailedGenerateChartException if an error occurs during chart generation
     */
    public byte[] generateChart(String rowKey, String title, String label, String text, WeeklyStats weeklyStats) {
        log.trace("Starting chart generation with title: {}", title);
        try {
            log.debug("Creating dataset for chart.");
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            weeklyStats.getWeeklyStats().forEach((day, count) -> dataset.addValue(count, rowKey, day));

            log.debug("Creating chart with title: {}", title);
            JFreeChart chart = ChartFactory.createLineChart(
                    title,                       // Chart title
                    "Days",                      // X-Axis label
                    label,                       // Y-Axis label
                    dataset,                     // Dataset
                    PlotOrientation.VERTICAL,    // Orientation
                    false,                       // Include legend
                    true,                        // Tooltips
                    false                        // URLs
            );

            log.debug("Customizing chart appearance.");
            chart.setTitle(new TextTitle(
                    text,
                    new Font(SANS_SERIF, Font.BOLD, 20)
            ));

            var plot = chart.getCategoryPlot();

            LineAndShapeRenderer renderer = new LineAndShapeRenderer();
            renderer.setSeriesPaint(0, Color.BLUE);
            renderer.setDefaultShapesVisible(true);
            renderer.setDefaultShapesFilled(true);
            plot.setRenderer(renderer);

            plot.getDomainAxis().setTickLabelFont(new Font(SANS_SERIF, Font.PLAIN, 12));
            plot.getRangeAxis().setTickLabelFont(new Font(SANS_SERIF, Font.PLAIN, 12));

            plot.setBackgroundPaint(Color.WHITE);
            plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
            plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            rangeAxis.setRange(0, 500);

            log.debug("Writing chart to PNG byte array.");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(out, chart, 800, 600);
            log.trace("Chart generation completed successfully.");
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Failed to generate chart image: {}", e.getMessage(), e);
            throw new FailedGenerateChartException("Failed to generate chart image " + e);
        }
    }
}
