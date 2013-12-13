package com.basv.samples.simplechartviewsample;

import android.util.SparseArray;

import com.basv.simplechartview.DataSeriesAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Sergey Bakhtiarov on 12/10/13.
 */
public class ChartDataAdapter extends DataSeriesAdapter {
    private SparseArray<Float> seriesData = new SparseArray<Float>();

    private float min = Integer.MAX_VALUE;
    private float max = Integer.MIN_VALUE;

    Calendar date;

    public ChartDataAdapter(float[] data) {

        for(int i = 0; i < data.length; i++) {
            seriesData.put(i, data[i]);
        }

        calculateRange(data);

        date = Calendar.getInstance();
    }

    private void calculateRange(float[] data) {

        for(float v : data) {
            if(v < min) {
                min = v;
            } else if (v > max) {
                max = v;
            }
        }
    }

    @Override
    public float getValue(int index) {
        Float value = seriesData.get(index);
        return value != null ? value : (max + min) / 2;
    }

    @Override
    public float getMax() {
        return max;
    }

    @Override
    public float getMin() {
        return min;
    }

    SimpleDateFormat dateFormat = new SimpleDateFormat("EE, dd");

    @Override
    public String getLabelX(int index) {

//        if((index - 8 / 2) % 8 == 0) {
//            int dayIndex = (index - 8 / 2) / 8;
//
//            date.roll(Calendar.DAY_OF_YEAR, dayIndex);
//            String result = dateFormat.format(date.getTime());
//            date.roll(Calendar.DAY_OF_YEAR, -dayIndex);
//
//            return result;
//        }

        return null;
    }

    @Override
    public String getLabelY(int index) {

        if(index % 5 == 0) {
            return String.valueOf(index);
        }

        return null;
    }
}
