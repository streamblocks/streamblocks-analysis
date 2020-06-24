package ch.epfl.vlsc.analysis.core.weights;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.chart.ui.UIUtils;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import java.awt.*;

/**
 * An example of a time series chart create using JFreeChart.  For the most
 * part, default settings are used, except that the renderer is modified to
 * show filled shapes (as well as lines) at each data point.
 */
public class WeightsVisualization extends ApplicationFrame {

    private static final long serialVersionUID = 1L;

    /**
     * A demonstration application showing how to create a simple time series
     * chart.  This example uses monthly data.
     *
     * @param title the frame title.
     */
    public WeightsVisualization(String title, XYDataset dataset) {
        super(title);
        ChartPanel chartPanel = (ChartPanel) createPanel(dataset);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 470));
        setContentPane(chartPanel);
    }

    /**
     * Creates a chart.
     *
     * @param dataset a dataset.
     * @return A chart.
     */
    private static JFreeChart createChart(XYDataset dataset) {

        JFreeChart chart = ChartFactory.createScatterPlot(
                "Action Clock-Ticks Weights",  // title
                "Firings",             // x-axis label
                "Weight",   // y-axis label
                dataset);

        chart.setBackgroundPaint(Color.WHITE);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.WHITE);
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);

        return chart;
    }

    /**
     * Creates a panel for the demo (used by SuperDemo.java).
     *
     * @return A panel.
     */
    public static JPanel createPanel(XYDataset dataset) {
        JFreeChart chart = createChart(dataset);
        ChartPanel panel = new ChartPanel(chart, false);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        return panel;
    }


    public static void visulize(String title, XYDataset dataset) {
        WeightsVisualization demo = new WeightsVisualization(
                title, dataset);
        demo.pack();
        UIUtils.centerFrameOnScreen(demo);
        demo.setVisible(true);
    }
}