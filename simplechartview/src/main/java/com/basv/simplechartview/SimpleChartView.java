package com.basv.simplechartview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

/**
 * Created by Sergey Bakhtiarov on 12/8/13.
 */
public class SimpleChartView extends View implements GestureDetector.OnGestureListener {

    public static final int GRID_HORIZONTAL = 0;
    public static final int GRID_VERTICAL = 1;
    public static final int GRID_HORIZONTAL_VERTICAL = 2;
    public static final int GRID_NONE = 3;

    private static final int DEFAULT_VIEWPORT_SIZE = 20;
    private static final float DEFAULT_GRID_GRANULARITY = 0.2f;
    private static final float DEFAULT_LABEL_FONT_SIZE = 30;

    private static final int PATH_LENGTH = 16;

    private final static int MAX_FLING_VELOCITY = 6000;

    private int viewportSize;
    private float gridGranularity = DEFAULT_GRID_GRANULARITY;
    private float xLabelFontSize = DEFAULT_LABEL_FONT_SIZE;
    private float yLabelFontSize = DEFAULT_LABEL_FONT_SIZE;

    private int gridVisibility = GRID_NONE;

    private static enum State {
        STATE_IDLE,
        STATE_SCROLLING,
        STATE_FLINGING,
        STATE_SCALING
    }

    private State touchState = State.STATE_IDLE;

    private float stepX;
    private float stepY;
    private float axisY;

    PointF[] controlPoints = new PointF[2];
    PointF p0 = new PointF();
    PointF p1 = new PointF();
    PointF p2 = new PointF();
    PointF p3 = new PointF();

    private DataSeriesAdapter adapter;

    private Paint gridPaintMain = new Paint();
    private Paint gridPaintAccent = new Paint();
    private Paint chartPaint = new Paint();
    private Paint pointPaint = new Paint();
    private Paint xLabelPaint = new Paint();
    private Paint yLabelPaint = new Paint();

    private Scroller scroller;
    private GestureDetector mDetector;

    private SparseArray<Path> pathCache = new SparseArray<Path>(50);

    public SimpleChartView(Context context) {
        this(context, null);
    }

    public SimpleChartView(Context context, AttributeSet attrs) {
        this(context, attrs, R.styleable.CustomTheme_simpleChartViewStyle);
    }

    public SimpleChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initView(context, attrs, defStyle);
    }

    private void initView(Context context, AttributeSet attrs, int defStyle) {

        final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SimpleChartView, defStyle,
                R.style.Widget_SimpleChartView);

        viewportSize = array.getInt(R.styleable.SimpleChartView_viewportSize, DEFAULT_VIEWPORT_SIZE);
        gridVisibility = array.getInt(R.styleable.SimpleChartView_gridVisibility, GRID_NONE);

        array.recycle();

        gridPaintMain.setColor(0xFFCCCCCC);
        gridPaintMain.setStrokeWidth(1);
        gridPaintMain.setStyle(Paint.Style.STROKE);

        gridPaintAccent.setColor(0xFF999999);
        gridPaintAccent.setStrokeWidth(1);
        gridPaintAccent.setStyle(Paint.Style.STROKE);

        chartPaint.setColor(0xFFFF0000);
        chartPaint.setStrokeWidth(7);
        chartPaint.setStyle(Paint.Style.STROKE);
        chartPaint.setAntiAlias(true);

        pointPaint.setColor(0xFF0000FF);
        pointPaint.setStrokeWidth(1);
        pointPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        xLabelPaint.setColor(0xFF000000);
        xLabelPaint.setTextSize(xLabelFontSize);
        xLabelPaint.setAntiAlias(true);

        yLabelPaint.setColor(0xFF000000);
        yLabelPaint.setTextSize(yLabelFontSize);
        yLabelPaint.setAntiAlias(true);

        scroller = new Scroller(context);
        mDetector = new GestureDetector(context, this);
    }

    public void setAdapter(DataSeriesAdapter adapter) {
        this.adapter = adapter;
    }

    public void setChartPaint(Paint paint) {
        chartPaint = paint;
        invalidate();
    }

    public void setGridPaint(Paint paint) {
        gridPaintMain = paint;
        invalidate();
    }

    public void setXLabelPaint(Paint paint) {
        xLabelPaint = paint;
        xLabelPaint.setTextSize(xLabelFontSize);
        invalidate();
    }

    public void setYLabelPaint(Paint paint) {
        yLabelPaint = paint;
        yLabelPaint.setTextSize(yLabelFontSize);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if(adapter != null) {
            updateStepValues();
        }
    }

    private void updateStepValues() {

        stepX = (float)getWidth() / (viewportSize - 1);
        stepY = stepX;

        int axisOffset = (int) (stepY * (adapter.getMax() + adapter.getMin()) / 2);
        axisY = getHeight() / 2 + axisOffset;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int scrollX = getScrollX();

        if(gridVisibility != GRID_NONE) {
            drawGrid(canvas);
        }

        /**
         * Draw chart
         */
        int startIndex = (int) (scrollX / stepX - 1);
        int endIndex = (int) ((scrollX + getWidth()) / stepX + 1);

        ensureCached(startIndex, endIndex);

        drawCachedPath(canvas, startIndex, endIndex);

        /**
         * Draw X labels
         */
         drawLabelsX(canvas, scrollX, getWidth() + scrollX);

        /**
         * Draw Y labels (not scrollable)
         */
        canvas.translate(scrollX, 0);
        drawLabelsY(canvas);
        canvas.translate(-scrollX, 0);
    }

    private void drawGrid(Canvas canvas) {

        int scrollX = getScrollX();

        canvas.save();
        canvas.translate(scrollX, 0);

        /**
         * Draw portion of grid that just became visible
         */
        if(scrollX > 0) {
            drawGrid(canvas, getWidth() - scrollX, getWidth());
        } else {
            drawGrid(canvas, -scrollX, 0);
        }

        canvas.restore();

        /**
         * Draw scrolled portion of grid
         */
        if(scrollX > 0) {
            drawGrid(canvas, getWidth(), scrollX);
        } else {
            drawGrid(canvas, 0, getWidth() + scrollX);
        }
    }

    /**
     * Draw chart on canvas
     *
     * Parameters startX and endX are used to determine range of data
     * which is displayed on the screen
     *
     * @param canvas
     * @param startIndex
     * @param endIndex
     */
    private void drawCachedPath(Canvas canvas, int startIndex, int endIndex) {

        int start = startIndex / PATH_LENGTH - 1;
        int end = endIndex / PATH_LENGTH + 1;

        for(int i = start; i < end; i++) {
            Path p = pathCache.get(i);
            if(p != null) {
                canvas.drawPath(p, chartPaint);
            }
        }

    }

    private void ensureCached(int startIndex, int endIndex) {
        if(startIndex > endIndex) {
            int temp = startIndex;
            startIndex = endIndex;
            endIndex = temp;
        }

        int startPath = startIndex / PATH_LENGTH - 1;
        int endPath = endIndex / PATH_LENGTH + 1;

        for(int i = startPath; i < endPath; i++) {
            if(pathCache.get(i) == null) {

                int start = i * PATH_LENGTH;
                int end = start + PATH_LENGTH;

                pathCache.put(i, buildPathForRange(start, end));
            }
        }
    }

    /**
     *
     * Drawing labels for X axis placed on top of the screen above the chart.
     * Parameters startX and endX are required to determine currently visible labels.
     * Label is displayed for every X points specified by getLabelRange()
     * method in DataSeriesAdapter
     *
     * @param canvas
     * @param startX
     * @param endX
     */
    private void drawLabelsX(Canvas canvas, int startX, int endX) {

        int startIndex = (int) (startX / stepX - 1);
        int endIndex = (int) (endX / stepX + 1);

        for(int i = startIndex; i <= endIndex; i++) {

            String label = adapter.getLabelX(i);

            if (label != null) {
                float textWidth = xLabelPaint.measureText(label);
                canvas.drawText(label, i * stepX - textWidth / 2, xLabelFontSize + 5, xLabelPaint);
            }
        }
    }

    /**
     * Draw labels for the Y axis placed on the left side of the chart
     *
     * @param canvas
     */
    private void drawLabelsY(Canvas canvas) {

        int maxY = (int) ((axisY - yLabelFontSize + 10) / stepY);
        int minY = (int) ((axisY - getHeight()) / stepY);

        float labelY = axisY - (maxY - 1) * stepY;

        for(int index = maxY - 1; index > minY; index--) {
            String label = adapter.getLabelY(index);
            if(label != null) {
                canvas.drawText(label, 10, labelY + xLabelPaint.getTextSize() / 2f - 6, yLabelPaint);
            }
            labelY += stepY;
        }

    }

    /**
     * Draw the grid
     *
     * Parameters startX and endX are user to determine which portion of grid to build
     * and in which direction
     *
     * @param canvas
     * @param startX
     * @param endX
     */
    private void drawGrid(Canvas canvas, int startX, int endX) {

        float height = getHeight();

        float sY = stepY / gridGranularity;

        if(gridVisibility == GRID_HORIZONTAL_VERTICAL ||
                gridVisibility == GRID_VERTICAL) {

            float sX = stepX / gridGranularity;

            if(endX > startX) {
                for (float x = startX; x < endX; x += sX) {
                    canvas.drawLine(x, 0, x, height, gridPaintMain);
                }
            } else {
                for(int x = startX; x > endX; x -= sX) {
                    canvas.drawLine(x, 0, x, height, gridPaintMain);
                }
            }
        }

        if(gridVisibility == GRID_HORIZONTAL_VERTICAL ||
                gridVisibility == GRID_HORIZONTAL) {

            for(float y = axisY, i = 0; y > yLabelFontSize; y -= sY, i++) {
                canvas.drawLine(startX, y, endX, y, gridPaintMain);
            }

            for(float y = axisY + sY, i = 1; y < height; y += sY, i++) {
                canvas.drawLine(startX, y, endX, y, gridPaintMain);
            }
        }
    }

    /**
     * Build the path for the chart going through the data points
     * for a single path segment
     */
    private Path buildPathForRange(int startIndex, int endIndex) {

        Path result = new Path();

        getScreenPointForData(startIndex, p0);
        result.moveTo(p0.x, p0.y);

        for (int i = startIndex; i < endIndex; i++) {

            getScreenPointForData(i - 1, p0);
            getScreenPointForData(i, p1);
            getScreenPointForData(i + 1, p2);
            getScreenPointForData(i + 2, p3);

            calculateControlPoints(p0, p1, p2, p3);

            result.cubicTo(controlPoints[0].x, controlPoints[0].y, controlPoints[1].x, controlPoints[1].y, p2.x, p2.y);
        }

        return result;
    }

    /**
     * Convert data point into screen point
     *
     * @param index
     * @param point
     */
    private void getScreenPointForData(int index, PointF point) {
        point.x = index * stepX;
        point.y = axisY - adapter.getValue(index) * stepY;
    }

    /**
     * Calculate control points for drawing a cubic curve.
     *
     * Curve is drawn from P1 to P2.
     * Points P0 and P4 used for calculation of control points.
     *
     * @param points
     */
    private void calculateControlPoints(PointF... points) {

        PointF p0 = points[0];
        PointF p1 = points[1];
        PointF p2 = points[2];
        PointF p3 = points[3];

        float x1 = p1.x + (p2.x - p0.x) / 6f;
        float y1 = p1.y + (p2.y - p0.y) / 6f;

        float x2 = p2.x - (p3.x - p1.x) / 6f;
        float y2 = p2.y - (p3.y - p1.y) / 6f;

        controlPoints[0] = new PointF(x1, y1);
        controlPoints[1] = new PointF(x2, y2);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(touchState != State.STATE_SCALING) {
            mDetector.onTouchEvent(event);
        }

        switch (event.getActionMasked() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN: {

                setTouchState(State.STATE_SCROLLING);

                if (!scroller.isFinished()) {
                    scroller.abortAnimation();
                }

                break;
            }

            case MotionEvent.ACTION_UP: {

                if(scroller.isFinished()) {
                    setTouchState(State.STATE_IDLE);
                }

                break;
            }

            case MotionEvent.ACTION_POINTER_DOWN: {
                setTouchState(State.STATE_SCALING);
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                setTouchState(State.STATE_SCROLLING);
                break;
            }

            case MotionEvent.ACTION_MOVE: {

//                if(touchState == State.STATE_SCALING && event.getPointerCount() == 2) {
//                    // handle scaling here
//                }

                break;
            }

            default:
                break;
        }

        return true;
    }

    @Override
    public void computeScroll() {

        if (scroller.computeScrollOffset()) {

            int oldX = getScrollX();

            int x = scroller.getCurrX();

            scrollTo(x, 0);

            if (oldX != getScrollX()) {
                onScrollChanged(getScrollX(), 0, oldX, 0);
            }

            postInvalidate();
        } else {
            // scroll finished?
            if(scroller.isFinished() && touchState == State.STATE_FLINGING) {
                setTouchState(State.STATE_IDLE);
            }
        }
    }

    private void setTouchState(State newState) {
        if(newState != touchState) {
            touchState  = newState;
        }
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        scrollBy((int) v, 0);
        return true;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float velocityX, float velocityY) {

        int velocity = (int) velocityX;

        if(Math.abs(velocity) > MAX_FLING_VELOCITY) {
            if(velocity < 0) {
                velocity = -MAX_FLING_VELOCITY;
            } else {
                velocity = MAX_FLING_VELOCITY;
            }
        }

        scroller.fling(getScrollX(), 0, -velocity, 0, getScrollX() - getWidth(), getScrollX() + getWidth(), 0, 0);
        setTouchState(State.STATE_FLINGING);
        return true;
    }
}
