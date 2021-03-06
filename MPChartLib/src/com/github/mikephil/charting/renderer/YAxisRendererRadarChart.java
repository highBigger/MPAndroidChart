
package com.github.mikephil.charting.renderer;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PointF;

import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;

public class YAxisRendererRadarChart extends YAxisRenderer {

    private RadarChart mChart;

    public YAxisRendererRadarChart(ViewPortHandler viewPortHandler, YAxis yAxis, RadarChart chart) {
        super(viewPortHandler, yAxis, null);

        this.mChart = chart;
    }

    @Override
    public void computeAxis(float yMin, float yMax) {
        computeAxisValues(yMin, yMax);
    }

    @Override
    protected void computeAxisValues(float min, float max) {
        float yMin = min;
        float yMax = max;

        int labelCount = mYAxis.getLabelCount();
        double range = Math.abs(yMax - yMin);

        if (labelCount == 0 || range <= 0) {
            mYAxis.mEntries = new float[] {};
            mYAxis.mEntryCount = 0;
            return;
        }

        double rawInterval = range / labelCount;
        double interval = Utils.roundToNextSignificant(rawInterval);
        double intervalMagnitude = Math.pow(10, (int) Math.log10(interval));
        int intervalSigDigit = (int) (interval / intervalMagnitude);
        if (intervalSigDigit > 5) {
            // Use one order of magnitude higher, to avoid intervals like 0.9 or
            // 90
            interval = Math.floor(10 * intervalMagnitude);
        }

        // if the labels should only show min and max
        if (mYAxis.isShowOnlyMinMaxEnabled()) {

            mYAxis.mEntryCount = 2;
            mYAxis.mEntries = new float[2];
            mYAxis.mEntries[0] = yMin;
            mYAxis.mEntries[1] = yMax;

        } else {

            double first = Math.ceil(yMin / interval) * interval;
            double last = Utils.nextUp(Math.floor(yMax / interval) * interval);

            double f;
            int i;
            int n = 0;
            for (f = first; f <= last; f += interval) {
                ++n;
            }

            if (Float.isNaN(mYAxis.getAxisMaxValue()))
                n += 1;
            
            mYAxis.mEntryCount = n;

            if (mYAxis.mEntries.length < n) {
                // Ensure stops contains at least numStops elements.
                mYAxis.mEntries = new float[n];
            }

            for (f = first, i = 0; i < n; f += interval, ++i) {
                mYAxis.mEntries[i] = (float) f;
            }
        }

        if (interval < 1) {
            mYAxis.mDecimals = (int) Math.ceil(-Math.log10(interval));
        } else {
            mYAxis.mDecimals = 0;
        }

        mYAxis.mAxisMaximum = mYAxis.mEntries[mYAxis.mEntryCount - 1];
        mYAxis.mAxisRange = Math.abs(mYAxis.mAxisMaximum - mYAxis.mAxisMinimum);
    }

    @Override
    public void renderAxisLabels(Canvas c) {

        if (!mYAxis.isEnabled() || !mYAxis.isDrawLabelsEnabled())
            return;

        mAxisPaint.setTypeface(mYAxis.getTypeface());
        mAxisPaint.setTextSize(mYAxis.getTextSize());
        mAxisPaint.setColor(mYAxis.getTextColor());

        PointF center = mChart.getCenterOffsets();
        float factor = mChart.getFactor();

        int labelCount = mYAxis.mEntryCount;

        for (int j = 0; j < labelCount; j++) {

            if (j == labelCount - 1 && mYAxis.isDrawTopYLabelEntryEnabled() == false)
                break;

            float r = (mYAxis.mEntries[j] - mYAxis.mAxisMinimum) * factor;

            PointF p = Utils.getPosition(center, r, mChart.getRotationAngle());

            String label = mYAxis.getFormattedLabel(j);

            c.drawText(label, p.x + 10, p.y, mAxisPaint);
        }
    }

    @Override
    public void renderLimitLines(Canvas c) {

        ArrayList<LimitLine> limitLines = mYAxis.getLimitLines();

        if (limitLines == null)
            return;

        float sliceangle = mChart.getSliceAngle();

        // calculate the factor that is needed for transforming the value to
        // pixels
        float factor = mChart.getFactor();

        PointF center = mChart.getCenterOffsets();

        for (int i = 0; i < limitLines.size(); i++) {

            LimitLine l = limitLines.get(i);

            mLimitLinePaint.setColor(l.getLineColor());
            mLimitLinePaint.setPathEffect(l.getDashPathEffect());
            mLimitLinePaint.setStrokeWidth(l.getLineWidth());

            float r = (l.getLimit() - mChart.getYChartMin()) * factor;

            Path limitPath = new Path();

            for (int j = 0; j < mChart.getData().getXValCount(); j++) {

                PointF p = Utils.getPosition(center, r, sliceangle * j + mChart.getRotationAngle());

                if (j == 0)
                    limitPath.moveTo(p.x, p.y);
                else
                    limitPath.lineTo(p.x, p.y);
            }

            limitPath.close();

            c.drawPath(limitPath, mLimitLinePaint);
        }
    }
}
