package com.yinghuanhang.pdf.parser.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Region;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.Scroller;

public class MyPageWidget extends FrameLayout {

    private int mWidth = 0;
    private int mHeight = 0;
    private int mCornerX = 0;
    private int mCornerY = 0;
    private Path mPath0;
    private Path mPath1;


    PointF mTouch = new PointF(); // 拖拽点
    PointF mBezierStart1 = new PointF(); // 贝塞尔曲线起始点
    PointF mBezierControl1 = new PointF(); // 贝塞尔曲线控制点
    PointF mBeziervertex1 = new PointF(); //贝塞尔曲线顶点
    PointF mBezierEnd1 = new PointF(); // 贝塞尔曲线结束点

    PointF mBezierStart2 = new PointF(); // 另一条贝塞尔曲线
    PointF mBezierControl2 = new PointF();
    PointF mBeziervertex2 = new PointF();
    PointF mBezierEnd2 = new PointF();

    PointF mLT = new PointF();
    PointF mRT = new PointF();
    PointF mLB = new PointF();
    PointF mRB = new PointF();
    PointF mBztemp;
    PointF mBztempStart = new PointF();

    float mMiddleX;
    float mMiddleY;
    float mDegrees;
    float mTouchToCornerDis;
    ColorMatrixColorFilter mColorMatrixFilter;
    Matrix mMatrix;
    float[] mMatrixArray = {0, 0, 0, 0, 0, 0, 0, 0, 1.0f};

    //true,从左往右边滑动,false从右边往左边滑动.
    private boolean mIsRTandLB;
    float mMaxLength;
    int[] mBackShadowColors;// 背面颜色组
    int[] mFrontShadowColors;// 前面颜色组
    GradientDrawable mBackShadowDrawableLR;// 有阴影的GradientDrawable
    GradientDrawable mBackShadowDrawableRL;
    GradientDrawable mFolderShadowDrawableLR;
    GradientDrawable mFolderShadowDrawableRL;

    GradientDrawable mFrontShadowDrawableHBT;
    GradientDrawable mFrontShadowDrawableHTB;
    GradientDrawable mFrontShadowDrawableVLR;
    GradientDrawable mFrontShadowDrawableVRL;

    Paint mPaint;

    //创建一个具有默认持续时间和内插器的滚动体
    Scroller mScroller;
    private boolean isAnimated = false;

    //当前的view
    private View currentView = null;
    //下一页的view
    private View nextView = null;
    //下一页转写本,副本的view
    private View nextViewTranscript = null;
    private Context mContext;

    //数据的适配器.
    private BaseAdapter mAdapter = null;
    private int currentPosition = -1;

    //签名册的总页数
    private int itemCount = 0;
    private OnPageTurnListener turnListener;
    private OnPageBeforeTurnListener mBeforeTurnListener;
    private String TAG = "MyPageWidget";

    public MyPageWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        viewInit();
    }


    public MyPageWidget(Context context) {
        super(context);
        mContext = context;
        viewInit();
    }

    private void viewInit() {
        mPath0 = new Path();
        mPath1 = new Path();
        createDrawable();

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);

        ColorMatrix cm = new ColorMatrix();
        float array[] = {0.55f, 0, 0, 0, 80.0f, 0, 0.55f, 0, 0, 80.0f, 0, 0,
                0.55f, 0, 80.0f, 0, 0, 0, 0.2f, 0};
        cm.set(array);
        mColorMatrixFilter = new ColorMatrixColorFilter(cm);
        mMatrix = new Matrix();
        mScroller = new Scroller(getContext());

        setOnTouchListener(new FingerTouchListener());

    }

    //手指触摸监听,是否消费触摸事件.
    private class FingerTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v == MyPageWidget.this && mAdapter != null) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (itemCount == 0) {
                        return false;
                    }
                    //处理动画,并且回去滚动的情况.
                    abortAnimation();

                    //计算拖拽脚.
                    calcCornerXY(event.getX(), event.getY());
                    if (DragToRight()) {//从左边往右边拖动的时候.
                        if (currentPosition == 0) {//第0页面则不处理滑动事件
                            //标记归位.
                            mCornerX = 0;
                            mCornerY = 0;
                            return false;
                        }
                        //复用nextView,nextViewTranscript,适配数据.回调了2次getView.
                        nextView = mAdapter.getView(currentPosition - 1, nextView, null);
                        nextViewTranscript = mAdapter.getView(currentPosition - 1, nextViewTranscript, null);
                    } else { //从右边往左边拖动的时候.
                        if (currentPosition == itemCount - 1) {//如果是最后1页则不处理滑动事件.
                            mCornerX = 0;
                            mCornerY = 0;
                            return false;
                        }
                        if (currentPosition < 0) {
                            currentPosition = 0;
                        }
                        //回调getView,展示下一页.
                        nextView = mAdapter.getView(currentPosition + 1, nextView, null);
                        nextViewTranscript = mAdapter.getView(currentPosition + 1, nextViewTranscript, null);
                    }
                    isAnimated = false;
                    mTouch.x = event.getX();
                    mTouch.y = event.getY();

                    //跟新视图,显示下一页.
                    nextView.setVisibility(View.VISIBLE);
                    nextViewTranscript.setVisibility(View.VISIBLE);
                    MyPageWidget.this.postInvalidate();

                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {//移动过程中
                    //移动过程中获取x,y坐标.
                    float x = event.getX();
                    float y = event.getY();

                    //将拖动点宽度控制在控件范围内部.
                    if (x > mWidth) {
                        mTouch.x = mWidth - 0.01f;
                    } else if (x < 0) {
                        mTouch.x = 0.01f;
                    } else {
                        mTouch.x = x;
                    }

                    //将拖动点高度控制在控件范围内部.
                    if (y > mHeight) {
                        mTouch.y = mHeight - 0.01f;
                    } else if (y < 0) {
                        mTouch.y = 0.01f;
                    } else {
                        mTouch.y = y;
                    }
                    //重新绘制视图.
                    MyPageWidget.this.postInvalidate();

                } else if (event.getAction() == MotionEvent.ACTION_UP) {//手指抬起 的时候.
                    if (canDragOver()) {//能够滑动翻页成功,
                        //释放动画,释放滚动!
                        isAnimated = true;
                        startAnimation(100);//控制中间翻页速度

                        //翻页成功之前的回调.
                        if (null != mBeforeTurnListener) {
                            mBeforeTurnListener.onBeforeTurn(MyPageWidget.this.mIsRTandLB);
                        }
                        Log.i(TAG, "onTouch: ===>翻页成功");

                    } else {//不能够滑动翻页
                        //标记归位.
                        mTouch.x = 0.01f;
                        mTouch.y = 0.01f;
                        mCornerX = 0;
                        mCornerY = 0;
                        nextView.setVisibility(View.INVISIBLE);
                        nextViewTranscript.setVisibility(View.INVISIBLE);
                    }
                    MyPageWidget.this.postInvalidate();
                }
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mWidth == 0) {
            mWidth = getWidth();
            mHeight = getHeight();

            mTouch.x = 0.01f; // 不让x,y为0,否则在点计算时会有问题
            mTouch.y = 0.01f;
            mLT.x = 0;
            mLT.y = 0;
            mLB.x = 0;
            mLB.y = mHeight;
            mRT.x = mWidth;
            mRT.y = 0;
            mRB.x = mWidth;
            mRB.y = mHeight;
            mMaxLength = (float) Math.hypot(mWidth, mHeight);
        }

    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        calcPoints();
        super.dispatchDraw(canvas);
        if (itemCount > 1) {
            drawCurrentPageShadow(canvas);
            drawCurrentBackArea(canvas, nextViewTranscript);
        }

    }


    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (child.equals(currentView)) {
            drawCurrentPageArea(canvas, child, mPath0);
        } else {
            drawNextPageAreaAndShadow(canvas, child);
        }

        return true;
    }

    /**
     * 计算拖拽点对应的拖拽脚
     */
    public void calcCornerXY(float x, float y) {
        if (x <= mWidth / 2)
            mCornerX = 0;
        else
            mCornerX = mWidth;
        if (y <= mHeight / 2)
            mCornerY = 0;
        else
            mCornerY = mHeight;
        if ((mCornerX == 0 && mCornerY == mHeight) || (mCornerX == mWidth && mCornerY == 0))
            mIsRTandLB = true;
        else
            mIsRTandLB = false;
        if (mIsRTandLB) {
            Log.i(MyPageWidget.this.getClass().getSimpleName(), "mIsRTandLB: 手指滑动--->" + mIsRTandLB);
        } else {
            Log.i(MyPageWidget.this.getClass().getSimpleName(), "mIsRTandLB: 手指滑动--->" + mIsRTandLB);
        }
    }

    /**
     * 求解直线P1P2和直线P3P4的交点坐标
     */
    public PointF getCross(PointF P1, PointF P2, PointF P3, PointF P4) {
        PointF CrossP = new PointF();
        // 二元函数通式： y=ax+b
        float a1 = (P2.y - P1.y) / (P2.x - P1.x);
        float b1 = ((P1.x * P2.y) - (P2.x * P1.y)) / (P1.x - P2.x);

        float a2 = (P4.y - P3.y) / (P4.x - P3.x);
        float b2 = ((P3.x * P4.y) - (P4.x * P3.y)) / (P3.x - P4.x);
        CrossP.x = (b2 - b1) / (a1 - a2);
        CrossP.y = a1 * CrossP.x + b1;
        return CrossP;
    }

    private void calcPoints() {
        mMiddleX = (mTouch.x + mCornerX) / 2;
        mMiddleY = (mTouch.y + mCornerY) / 2;
        mBezierControl1.x = mMiddleX - (mCornerY - mMiddleY)
                * (mCornerY - mMiddleY) / (mCornerX - mMiddleX);
        mBezierControl1.y = mCornerY;
        mBezierControl2.x = mCornerX;
        mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX)
                * (mCornerX - mMiddleX) / (mCornerY - mMiddleY);


        mBezierStart1.x = mBezierControl1.x - (mCornerX - mBezierControl1.x)
                / 3;
        mBezierStart1.y = mCornerY;

        // 当mBezierStart1.x < 0或者mBezierStart1.x > 480时
        // 如果继续翻页，会出现BUG故在此限制
        if (!isAnimated) {
            if (mCornerX == 0 && mBezierStart1.x > mWidth / 2) {
                float f1 = Math.abs(mCornerX - mTouch.x);
                float f2 = mWidth / 2 * f1 / mBezierStart1.x;
                mTouch.x = Math.abs(mCornerX - f2);

                float f3 = Math.abs(mCornerX - mTouch.x)
                        * Math.abs(mCornerY - mTouch.y) / f1;
                mTouch.y = Math.abs(mCornerY - f3);

                mMiddleX = (mTouch.x + mCornerX) / 2;
                mMiddleY = (mTouch.y + mCornerY) / 2;

                mBezierControl1.x = mMiddleX - (mCornerY - mMiddleY)
                        * (mCornerY - mMiddleY) / (mCornerX - mMiddleX);
                mBezierControl1.y = mCornerY;

                mBezierControl2.x = mCornerX;
                mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX)
                        * (mCornerX - mMiddleX) / (mCornerY - mMiddleY);

                mBezierStart1.x = mBezierControl1.x
                        - (mCornerX - mBezierControl1.x) / 3;
            }
            if (mCornerX == mWidth && mBezierStart1.x < mWidth / 2) {
                mBezierStart1.x = mWidth - mBezierStart1.x;
                float f1 = Math.abs(mCornerX - mTouch.x);
                float f2 = mWidth / 2 * f1 / mBezierStart1.x;
                mTouch.x = Math.abs(mCornerX - f2);
                float f3 = Math.abs(mCornerX - mTouch.x)
                        * Math.abs(mCornerY - mTouch.y) / f1;
                mTouch.y = Math.abs(mCornerY - f3);
                mMiddleX = (mTouch.x + mCornerX) / 2;
                mMiddleY = (mTouch.y + mCornerY) / 2;

                mBezierControl1.x = mMiddleX - (mCornerY - mMiddleY)
                        * (mCornerY - mMiddleY) / (mCornerX - mMiddleX);
                mBezierControl1.y = mCornerY;

                mBezierControl2.x = mCornerX;
                mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX)
                        * (mCornerX - mMiddleX) / (mCornerY - mMiddleY);
                mBezierStart1.x = mBezierControl1.x
                        - (mCornerX - mBezierControl1.x) / 3;
            }
        }

        mBezierStart2.x = mCornerX;
        mBezierStart2.y = mBezierControl2.y - (mCornerY - mBezierControl2.y)
                / 3;

        mTouchToCornerDis = (float) Math.hypot((mTouch.x - mCornerX),
                (mTouch.y - mCornerY));

        mBezierEnd1 = getCross(mTouch, mBezierControl1, mBezierStart1,
                mBezierStart2);
        mBezierEnd2 = getCross(mTouch, mBezierControl2, mBezierStart1,
                mBezierStart2);

        /*
         * mBeziervertex1.x 推导
         * ((mBezierStart1.x+mBezierEnd1.x)/2+mBezierControl1.x)/2 化简等价于
         * (mBezierStart1.x+ 2*mBezierControl1.x+mBezierEnd1.x) / 4
         */

        mBeziervertex1.x = (mBezierStart1.x + 2 * mBezierControl1.x + mBezierEnd1.x) / 4;
        mBeziervertex1.y = (2 * mBezierControl1.y + mBezierStart1.y + mBezierEnd1.y) / 4;
        mBeziervertex2.x = (mBezierStart2.x + 2 * mBezierControl2.x + mBezierEnd2.x) / 4;
        mBeziervertex2.y = (2 * mBezierControl2.y + mBezierStart2.y + mBezierEnd2.y) / 4;
    }

    private void drawCurrentPageArea(Canvas canvas, View child, Path path) {
        mPath0.reset();
        mPath0.moveTo(mBezierStart1.x, mBezierStart1.y);
        mPath0.quadTo(mBezierControl1.x, mBezierControl1.y, mBezierEnd1.x,
                mBezierEnd1.y);
        mPath0.lineTo(mTouch.x, mTouch.y);
        mPath0.lineTo(mBezierEnd2.x, mBezierEnd2.y);
        mPath0.quadTo(mBezierControl2.x, mBezierControl2.y, mBezierStart2.x,
                mBezierStart2.y);
        mPath0.lineTo(mCornerX, mCornerY);
        mPath0.close();

        canvas.save();
        canvas.clipPath(path, Region.Op.XOR);
        child.draw(canvas);
        canvas.restore();
    }

    private void drawNextPageAreaAndShadow(Canvas canvas, View child) {
        mPath1.reset();
        mPath1.moveTo(mBezierStart1.x, mBezierStart1.y);
        mPath1.lineTo(mBeziervertex1.x, mBeziervertex1.y);
        mPath1.lineTo(mBeziervertex2.x, mBeziervertex2.y);
        mPath1.lineTo(mBezierStart2.x, mBezierStart2.y);
        mPath1.lineTo(mCornerX, mCornerY);
        mPath1.close();

        mDegrees = (float) Math.toDegrees(Math.atan2(mBezierControl1.x
                - mCornerX, mBezierControl2.y - mCornerY));
        int leftx;
        int rightx;
        GradientDrawable mBackShadowDrawable;
        if (mIsRTandLB) {
            leftx = (int) (mBezierStart1.x);
            rightx = (int) (mBezierStart1.x + mTouchToCornerDis / 4);
            mBackShadowDrawable = mBackShadowDrawableLR;
        } else {
            leftx = (int) (mBezierStart1.x - mTouchToCornerDis / 4);
            rightx = (int) mBezierStart1.x;
            mBackShadowDrawable = mBackShadowDrawableRL;
        }
        canvas.save();
        canvas.clipPath(mPath0);
        canvas.clipPath(mPath1, Region.Op.INTERSECT);
        child.draw(canvas);
        canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y);
        mBackShadowDrawable.setBounds(leftx, (int) mBezierStart1.y, rightx,
                (int) (mMaxLength + mBezierStart1.y));
        mBackShadowDrawable.draw(canvas);
        canvas.restore();
    }

    public void setScreen(int w, int h) {
        mWidth = w;
        mHeight = h;
    }


    /**
     * 创建阴影的GradientDrawable
     */
    private void createDrawable() {
        int[] color = {0x333333, 0xb0333333};
        mFolderShadowDrawableRL = new GradientDrawable(
                GradientDrawable.Orientation.RIGHT_LEFT, color);
        mFolderShadowDrawableRL
                .setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mFolderShadowDrawableLR = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, color);
        mFolderShadowDrawableLR
                .setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mBackShadowColors = new int[]{0xff111111, 0x111111};
        mBackShadowDrawableRL = new GradientDrawable(
                GradientDrawable.Orientation.RIGHT_LEFT, mBackShadowColors);
        mBackShadowDrawableRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mBackShadowDrawableLR = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, mBackShadowColors);
        mBackShadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mFrontShadowColors = new int[]{0x80111111, 0x111111};
        mFrontShadowDrawableVLR = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, mFrontShadowColors);
        mFrontShadowDrawableVLR
                .setGradientType(GradientDrawable.LINEAR_GRADIENT);
        mFrontShadowDrawableVRL = new GradientDrawable(
                GradientDrawable.Orientation.RIGHT_LEFT, mFrontShadowColors);
        mFrontShadowDrawableVRL
                .setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mFrontShadowDrawableHTB = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, mFrontShadowColors);
        mFrontShadowDrawableHTB
                .setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mFrontShadowDrawableHBT = new GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP, mFrontShadowColors);
        mFrontShadowDrawableHBT
                .setGradientType(GradientDrawable.LINEAR_GRADIENT);
    }

    /**
     * 绘制翻起页的阴影
     */
    private void drawCurrentPageShadow(Canvas canvas) {
        double degree;
        if (mIsRTandLB) {
            degree = Math.PI / 4
                    - Math.atan2(mBezierControl1.y - mTouch.y, mTouch.x
                    - mBezierControl1.x);
        } else {
            degree = Math.PI
                    / 4
                    - Math.atan2(mTouch.y - mBezierControl1.y, mTouch.x
                    - mBezierControl1.x);
        }

        if (Math.toDegrees(degree) > 220) {
            return;
        }

        // 翻起页阴影顶点与touch点的距离
        double d1 = (float) 25 * 1.414 * Math.cos(degree);
        double d2 = (float) 25 * 1.414 * Math.sin(degree);
        float x = (float) (mTouch.x + d1);
        float y;
        if (mIsRTandLB) {
            y = (float) (mTouch.y + d2);
        } else {
            y = (float) (mTouch.y - d2);
        }
        mPath1.reset();
        mPath1.moveTo(x, y);
        mPath1.lineTo(mTouch.x, mTouch.y);
        mPath1.lineTo(mBezierControl1.x, mBezierControl1.y);
        mPath1.lineTo(mBezierStart1.x, mBezierStart1.y);
        mPath1.close();
        float rotateDegrees;
        canvas.save();

        canvas.clipPath(mPath0, Region.Op.XOR);
        canvas.clipPath(mPath1, Region.Op.INTERSECT);
        int leftx;
        int rightx;
        GradientDrawable mCurrentPageShadow;
        if (mIsRTandLB) {
            leftx = (int) (mBezierControl1.x);
            rightx = (int) mBezierControl1.x + 25;
            mCurrentPageShadow = mFrontShadowDrawableVLR;
        } else {
            leftx = (int) (mBezierControl1.x - 25);
            rightx = (int) mBezierControl1.x + 1;
            mCurrentPageShadow = mFrontShadowDrawableVRL;
        }

        rotateDegrees = (float) Math.toDegrees(Math.atan2(mTouch.x
                - mBezierControl1.x, mBezierControl1.y - mTouch.y));
        canvas.rotate(rotateDegrees, mBezierControl1.x, mBezierControl1.y);
        mCurrentPageShadow.setBounds(leftx,
                (int) (mBezierControl1.y - mMaxLength), rightx,
                (int) (mBezierControl1.y));
        mCurrentPageShadow.draw(canvas);
        canvas.restore();

        int offset = mCornerX > 0 ? 30 : -30;

        if (mBezierControl2.y < 0) {
            mBztemp = getCross(mLT, mRT, mTouch, mBezierControl2);
            mBztempStart.x = mBztemp.x - offset;
            mBztempStart.y = mBztemp.y;
        } else if (mBezierControl2.y > mHeight) {
            mBztemp = getCross(mLB, mRB, mTouch, mBezierControl2);
            mBztempStart.x = mBztemp.x - offset;
            mBztempStart.y = mBztemp.y;
        } else {
            mBztemp = mBezierControl2;
            mBztempStart = mBezierStart2;
        }
        mPath1.reset();
        mPath1.moveTo(x, y);
        mPath1.lineTo(mTouch.x, mTouch.y);
        mPath1.lineTo(mBztemp.x, mBztemp.y);
        mPath1.lineTo(mBztempStart.x, mBztempStart.y);
        mPath1.close();
        canvas.save();
        canvas.clipPath(mPath0, Region.Op.XOR);
        canvas.clipPath(mPath1, Region.Op.INTERSECT);
        if (mIsRTandLB) {
            leftx = (int) (mBztemp.y);
            rightx = (int) (mBztemp.y + 25);
            mCurrentPageShadow = mFrontShadowDrawableHTB;
        } else {
            leftx = (int) (mBztemp.y - 25);
            rightx = (int) (mBztemp.y);
            mCurrentPageShadow = mFrontShadowDrawableHBT;
        }
        rotateDegrees = (float) Math.toDegrees(Math.atan2(mBztemp.y
                - mTouch.y, mBztemp.x - mTouch.x));
        canvas.rotate(rotateDegrees, mBztemp.x, mBztemp.y);
        mCurrentPageShadow.setBounds(
                (int) (mBztemp.x - mMaxLength), leftx,
                (int) (mBztemp.x), rightx);
        mCurrentPageShadow.draw(canvas);
        canvas.restore();

    }


    /**
     * 绘制翻起页背面
     */

    private void drawCurrentBackArea(Canvas canvas, View view) {
        int i = (int) (mBezierStart1.x + mBezierControl1.x) / 2;
        float f1 = Math.abs(i - mBezierControl1.x);
        /*int i1 = (int) (mBezierStart2.y + mBezierControl2.y) / 2;
        float f2 = Math.abs(i1 - mBezierControl2.y);*/
        float f3 = f1;
        mPath1.reset();
        mPath1.moveTo(mBeziervertex2.x, mBeziervertex2.y);
        mPath1.lineTo(mBeziervertex1.x, mBeziervertex1.y);
        mPath1.lineTo(mBezierEnd1.x, mBezierEnd1.y);
        mPath1.lineTo(mTouch.x, mTouch.y);
        mPath1.lineTo(mBezierEnd2.x, mBezierEnd2.y);
        mPath1.close();
        GradientDrawable mFolderShadowDrawable;
        int left;
        int right;
        if (mIsRTandLB) {
            left = (int) (mBezierStart1.x - 1);
            right = (int) (mBezierStart1.x + f3 + 1);
            mFolderShadowDrawable = mFolderShadowDrawableLR;
        } else {
            left = (int) (mBezierStart1.x - f3 - 1);
            right = (int) (mBezierStart1.x + 1);
            mFolderShadowDrawable = mFolderShadowDrawableRL;
        }
        canvas.save();
        canvas.clipPath(mPath0);
        canvas.clipPath(mPath1, Region.Op.INTERSECT);

        //mPaint.setColorFilter(mColorMatrixFilter);

        float rotateDegrees = (float) Math.toDegrees(Math.PI / 2 + Math.atan2(mBezierControl2.y
                - mTouch.y, mBezierControl2.x - mTouch.x));

        if (mCornerY == 0) {
            rotateDegrees -= 180;
        }
        mMatrix.reset();
        mMatrix.setPolyToPoly(new float[]{Math.abs(mWidth - mCornerX), mCornerY}, 0, new float[]{mTouch.x, mTouch.y}, 0, 1);
        mMatrix.postRotate(rotateDegrees, mTouch.x, mTouch.y);
        canvas.save();
        canvas.concat(mMatrix);
        view.draw(canvas);//----
        canvas.restore();
        canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y);
        mFolderShadowDrawable.setBounds(left, (int) mBezierStart1.y, right + 2,
                (int) (mBezierStart1.y + mMaxLength));
        mFolderShadowDrawable.draw(canvas);
        canvas.restore();
    }

    //主要功能是计算拖动的位移量、更新背景
    //重写computeScroll()的原因
    //只有在computeScroll()获取滚动情况，做出滚动的响应
    //computeScroll在父控件执行drawChild时，会调用这个方法
    @Override
    public void computeScroll() {
        //获取新的滑动位置,true:动画还没有结束.
        if (mScroller.computeScrollOffset()) {
            //获取当前滚动位置的x,y坐标,并且赋值给滚动点.更新视图.
            float x = mScroller.getCurrX();
            float y = mScroller.getCurrY();
            mTouch.x = x;
            mTouch.y = y;
            postInvalidate();
        }

        //如果动画结束,并且滚动结束
        if (isAnimated && mScroller.isFinished()) {
            //动画标记归位.
            isAnimated = false;

            //往右边滑动,到上1页面.
            if (DragToRight()) {
                currentPosition -= 1;
            } else { //往左边滑动,到下1页面.
                currentPosition += 1;
            }
            //回调适配器getView,并且使用currentView作为convertView缓存视图,便于重新适配数据.同时更新索引.
            currentView = mAdapter.getView(currentPosition, currentView, null);

            //触摸点归位.
            mTouch.x = 0.01f;
            mTouch.y = 0.01f;
            mCornerX = 0;
            mCornerY = 0;
            //下一页面的view隐藏起来.
            nextView.setVisibility(View.INVISIBLE);
            nextViewTranscript.setVisibility(View.INVISIBLE);

            //翻页结束,更新视图.
            postInvalidate();
            //翻页回调.
            if (turnListener != null) {
                Log.i(TAG, "turnListener: ===>翻页成功回调了");
                turnListener.onTurn(itemCount, currentPosition);
            }
        }
    }


    private void startAnimation(int delayMillis) {
        int dx, dy;
        // dx 水平方向滑动的距离，负值会使滚动向左滚动
        // dy 垂直方向滑动的距离，负值会使滚动向上滚动

        if (mCornerX > 0) {
            dx = (int) (-mTouch.x + 1);
        } else {
            dx = (int) (mWidth - mTouch.x - 1);
        }
        if (mCornerY > 0) {
            dy = (int) (mHeight - mTouch.y - 1);
        } else {
            dy = (int) (1 - mTouch.y); // 防止mTouch.y最终变为0
        }
        mScroller.startScroll((int) mTouch.x, (int) mTouch.y, dx, dy,
                delayMillis);

    }


    //中止计划,动画.
    public void abortAnimation() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
            computeScroll();
        }
    }

    public boolean canDragOver() {
        if (mTouchToCornerDis > mWidth / 10)
            return true;
        return false;
    }

    /**
     * Author :    /**
     * 是否从左边翻向右边
     *
     * @return
     */

    public boolean DragToRight() {
        if (mCornerX > 0)
            return false;
        return true;
    }

    public void setAdapter(BaseAdapter adapter) {
        mAdapter = adapter;
        itemCount = mAdapter.getCount();
        currentView = null;
        nextView = null;
        nextViewTranscript = null;

        //清空视图页面.
        removeAllViews();
        if (itemCount != 0) {
            //如果签名册的数量大于0,
            //那么就添加并显示第1页
            currentPosition = 0;
            currentView = mAdapter.getView(currentPosition, null, null);
            addView(currentView);

            //如果签名册的数量大于1条以上.则会初始化3个view条目.
            if (itemCount > 1) {
                //如果签名册的数量大于1条以上.
                //那么就添加并显示下1页
                ////那么就添加并显示下1页副本.
                nextView = mAdapter.getView(currentPosition, null, null);
                nextView.setVisibility(View.INVISIBLE);
                addView(nextView);
                nextViewTranscript = mAdapter.getView(currentPosition, null, null);
                nextViewTranscript.setVisibility(View.INVISIBLE);
                addView(nextViewTranscript);
            }
        } else {
            //如果签名册的数量等于0,
            //那么当前页面索引恢复默认值.-1
            currentPosition = -1;
        }

        //拖拽点归零.
        mTouch.x = 0.01f;
        mTouch.y = 0.01f;

        mCornerX = 0;
        mCornerY = 0;
        /*if (null != mBeforeTurnListener) {
            PaletteView lastPaletteView
                    = (PaletteView) MyPageWidget.this
                    .getChildAt(0)
                    .findViewById(R.id.palette);
            if (null != lastPaletteView) {
                mBeforeTurnListener.onBeforeTurn(lastPaletteView.buildBitmap());
            }
        }*/
        //更新视图
        postInvalidate();
        //选中第0页的回调.
        if (turnListener != null) {
            turnListener.onTurn(itemCount, currentPosition);
        }
    }

    /**
     * ÓÃÓÚ·­Ò³½áÊøºóµÄÒ³ÂëÍ¨Öª
     *
     * @author xf
     */
    public interface OnPageTurnListener {
        public void onTurn(int count, int currentPosition);
    }

    public void setOnPageTurnListener(OnPageTurnListener listener) {
        turnListener = listener;
    }

    public void setCurrentPosition(int page) {
        if (mAdapter == null || mAdapter.getCount() <= 0) {
            return;
        }

        if (currentPosition == page) {
            return;
        }

        if (page < 0) {
            page = 0;
        } else if (page >= mAdapter.getCount()) {
            page = mAdapter.getCount() - 1;
        }

        currentPosition = page;
        currentView = mAdapter.getView(currentPosition, currentView, null);

        if (nextView != null) {
            nextView.setVisibility(View.INVISIBLE);
        }
        if (null != nextViewTranscript) {
            nextViewTranscript.setVisibility(View.INVISIBLE);
        }
        /*if (null != mBeforeTurnListener) {
            PaletteView lastPaletteView
                    = (PaletteView) MyPageWidget.this
                    .getChildAt(0)
                    .findViewById(R.id.palette);
            if (null != lastPaletteView) {
                mBeforeTurnListener.onBeforeTurn(lastPaletteView.buildBitmap());
            }
        }*/
        postInvalidate();
        if (turnListener != null) {
            turnListener.onTurn(itemCount, currentPosition);
        }
    }

    public boolean ismIsRTandLB() {
        return mIsRTandLB;
    }

    /**
     * ÓÃÓÚ·­Ò³½áÊøºóµÄÒ³ÂëÍ¨Öª
     *
     * @author xf
     */
    public interface OnPageBeforeTurnListener {
        void onBeforeTurn(boolean isFromL2R);//ismIsRTandLB
    }

    public void setOnOnPageBeforeTurnListener(OnPageBeforeTurnListener listener) {
        mBeforeTurnListener = listener;
    }
}
