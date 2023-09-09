package de.wigeogis.pmedian.optimizer.math;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.List;

public class Statistics {

    private DescriptiveStatistics ds = new DescriptiveStatistics();

    public <T> Statistics(List<T> input) {
        for (T item : input) {
            if (item instanceof Double) {
                ds.addValue((Double) item);
            } else if (item instanceof Long) {
                ds.addValue(((Long) item).doubleValue());
            }
        }
    }

    public double calculateStandardDeviation() {
        return ds.getStandardDeviation();
    }

    public double calculateMean() {
        return ds.getMean();
    }

}
