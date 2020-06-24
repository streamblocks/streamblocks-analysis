package ch.epfl.vlsc.analysis.core.weights;

import org.apache.commons.math3.stat.StatUtils;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.ArrayList;
import java.util.List;

public class ActionWeight {

    private final int actionId;
    private final List<Double> dataPoints;
    private final List<Double> filterDataPoints;
    private long sum;
    private double min;
    private double max;
    private double average;
    private double variance;
    private long firings;
    private long filteredFirings;

    public ActionWeight(int actionId) {
        this.dataPoints = new ArrayList<>();
        this.filterDataPoints = new ArrayList<>();
        this.actionId = actionId;
        sum = 0;
        min = Integer.MAX_VALUE;
        max = Integer.MIN_VALUE;
        firings = 0;
    }

    public void update(int execTime) {
        sum += execTime;
        dataPoints.add((double) execTime);
        firings++;
    }

    public void finalize(boolean useFilter) {
        double threshold = Double.MAX_VALUE;

        if (useFilter && firings > 2) {
            double[] dataPointsArray = dataPoints.stream().mapToDouble(Double::doubleValue).toArray();
            double variance = StatUtils.variance(dataPointsArray);

            threshold = StatUtils.mean(dataPointsArray) + 2 + Math.sqrt(variance);
        }

        calcWeightSimple(threshold);
    }

    private void calcWeightSimple(double threshold) {
        double[] filtered = new double[dataPoints.size()];
        int i = 0;
        for (Double weight : dataPoints) {
            if (weight < threshold) {
                // -- Minimum
                if (weight < min) {
                    min = weight;
                }

                // -- Maximum
                if (weight > max) {
                    max = weight;
                }
                filtered[i] = weight;
                i++;
                filterDataPoints.add(weight);
            }
        }

        filteredFirings = i;
        average = StatUtils.mean(filtered);
        variance = StatUtils.variance(filtered);
    }

    private double calcVariance(List<Integer> dataPoints) {
        double tmp = 0.0;
        double sumDiffMean = 0.0;
        double variance = 0.0;
        if (dataPoints.size() > 2) {
            for (Integer weight : dataPoints) {
                tmp = weight - calcAverage(dataPoints);
                sumDiffMean += tmp * tmp;
            }
            variance = sumDiffMean / (firings - 1);
        } else {
            variance = 0.0;
        }
        return variance;
    }

    private double calcAverage(List<Integer> dataPoints) {
        long sum = 0;
        for (Integer weight : dataPoints) {
            sum += weight;
        }
        return dataPoints.size() > 0 ? (double) sum / dataPoints.size() : 0.0;
    }

    public double getAverage() {
        return average;
    }

    public double getVariance() {
        return variance;
    }

    public double getMin() {
        return (firings == 0) ? 0.0 : min;
    }

    public int getActionId() {
        return actionId;
    }

    public double getMax() {
        return firings == 0 ? 0.0 : max;
    }

    public long getFirings() {
        return firings;
    }

    public long getFilteredFirings() {
        return filteredFirings;
    }

    public XYDataset getPlot(boolean filtered) {

        XYSeries series = new XYSeries("Weights");
        List<Double> dataPoints;
        if (filtered) {
            dataPoints = filterDataPoints;
        } else {
            dataPoints = this.dataPoints;
        }

        for (int i = 0; i < dataPoints.size(); i++) {
            series.add(i, dataPoints.get(i));
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        return dataset;
    }
}
