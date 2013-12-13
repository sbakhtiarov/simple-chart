package com.basv.samples.simplechartviewsample;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.basv.simplechartview.SimpleChartView;

/**
 * Created by serge on 12/10/13.
 */
public class ChartFragment extends Fragment {
    float[] testData = new float[]
            {
                    5,  7,  8,  9, 13, 12, 11, 9, // Mon
                    6,  8, 10, 11, 12, 11, 10, 8, // Tue
                    5,  7, 10, 11, 12, 13, 10, 8, // Wed
                    8,  9, 11, 12, 10, 10,  7,  6, // Thu
                    6,  7,  7, 10, 11, 11,  9,  7, // Fri
                    7,  7,  8, 10, 11, 11, 10,  8, // Sat
                    6,  6,  5,  7,  9, 10, 10,  8,  // Sun
                    5,  7,  8,  9, 13, 12, 11, 9, // Mon
                    6,  8, 10, 11, 12, 11, 10, 8, // Tue
                    5,  7, 10, 11, 12, 13, 10, 8, // Wed
                    8,  9, 11, 12, 10, 10,  7,  6, // Thu
                    6,  7,  7, 10, 11, 11,  9,  7, // Fri
                    7,  7,  8, 10, 11, 11, 10,  8, // Sat
                    6,  6,  5,  7,  9, 10, 10,  8,  // Sun
                    0,  2,  3,  7,  9,  6,  2,  1, // Mon
                    6,  8, 10, 11, 12, 11, 10, 8, // Tue
                    5,  7, 10, 11, 12, 13, 10, 8, // Wed
                    8,  9, 11, 12, 10, 10,  7,  6, // Thu
                    6,  7,  7, 10, 11, 11,  9,  7, // Fri
                    7,  7,  8, 10, 11, 11, 10,  8, // Sat
                    6,  6,  5,  7,  9, 10, 10,  8,  // Sun
                    5,  7,  8,  9, 13, 12, 11, 9, // Mon
                    6,  8, 10, 11, 12, 11, 10, 8, // Tue
                    5,  7, 10, 11, 12, 13, 10, 8, // Wed
                    8,  9, 11, 12, 10, 10,  7,  6, // Thu
                    6,  7,  7, 10, 11, 11,  9,  7, // Fri
                    7,  7,  8, 10, 11, 11, 10,  8, // Sat
                    6,  6,  5,  7,  9, 10, 10,  8,  // Sun
                    5,  7,  8,  9, 13, 12, 11, 9, // Mon
                    6,  8, 10, 11, 12, 11, 10, 8, // Tue
                    5,  7, 10, 11, 12, 13, 10, 8, // Wed
                    8,  9, 11, 12, 10, 10,  7,  6, // Thu
                    6,  7,  7, 10, 11, 11,  9,  7, // Fri
                    7,  7,  8, 10, 11, 11, 10,  8, // Sat
                    6,  6,  5,  7,  9, 10, 10,  8  // Sun
            };

    private final Paint chartPaint = new Paint();
    private final Paint gridPaint = new Paint();
    private final Paint labelPaint = new Paint();


    public ChartFragment() {
        chartPaint.setColor(0xFFFF8800); // 0099CC
        chartPaint.setStrokeWidth(4);
        chartPaint.setStyle(Paint.Style.STROKE);
        chartPaint.setAntiAlias(true);

        gridPaint.setColor(0xFF00679A);
        gridPaint.setStrokeWidth(1);
        gridPaint.setStyle(Paint.Style.STROKE);

        labelPaint.setColor(0xFF666666);
        labelPaint.setAntiAlias(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        SimpleChartView graph = (SimpleChartView) rootView.findViewById(R.id.graphView);

        graph.setChartPaint(chartPaint);
        graph.setGridPaint(gridPaint);
        graph.setXLabelPaint(labelPaint);
        graph.setYLabelPaint(labelPaint);

        graph.setAdapter(new ChartDataAdapter(testData));

        return rootView;
    }
}
