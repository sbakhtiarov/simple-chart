package com.basv.simplechartview;

/**
 * Created by Sergey Bakhtiarov on 12/8/13.
 */
public abstract class DataSeriesAdapter {
    public abstract float getValue(int index);
    public abstract float getMax();
    public abstract float getMin();

    public String getLabelX(int index) {
        return null;
    }

    public String getLabelY(int index) {
        return null;
    }
}
