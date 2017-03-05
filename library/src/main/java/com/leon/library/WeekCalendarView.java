package com.leon.library;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;

import com.leon.library.entity.SingleCourse;
import com.leon.library.entity.TimeSlotHttpObj;
import com.leon.library.util.DisplayUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LeonXtp
 */
public class WeekCalendarView extends View {

    private Context mContext;
    //线条的画笔
    private Paint mPaintLine;
    //周文字画笔
    private Paint mPaintWeekDayText;
    //月日期文字画笔
    private Paint mPaintMonthDayText;
    //时间段时间起点画笔
    private Paint mPaintTimeSlotText;
    //时间段序号画笔
    private Paint mPaintTimeSlotNoText;
    //课程代号画笔
    private Paint mPaintClassCodeText;
    //课程预约情况画笔
    private Paint mPaintClassStateText;
    //课程费用画笔
    private Paint mPaintClassPriceText;

    private String[] weekDayNamesArray = new String[]{"日", "一", "二", "三", "四", "五", "六"};

    //标题栏相关文本的宽度、高度、绘制的起始点
    float mWeekDayTextWidth;
    float mWeekDayTextHeight;
    float[] mMonthDayTextWidth = new float[31];
    float mMonthDayTextHeight;
    private float weekDayDrawStartY;
    private float monthDayDrawStartY;
    //临时保存当前状态下星期的”天“的文本起点横坐标数组
    float[] dayOffWeekStartXArr = new float[8];
    //临时保存当前状态下标题栏中月中的”天“的X方向起始绘制位置
    float[] dayOfMonthStartArr = new float[8];

    //时间段相关文本的宽度、高度、绘制的起始点偏移量
    float mTimeSlotTextWidth;
    float mTimeSlotTextHeight;
    float mTimeSlotNoTextWidth;
    float mTimeSlotNoTextHeight;
    float mTimeSlotNoTextStartX;
    float mTimeSlotTextStartX;
    float mTimeSlotStartCenterOffsetY;
    float mTimeSlotNoStartCenterOffsetY;
    //课程相关本的宽度、高度、绘制的起始点偏移量
    float mClassCodeTextWidth;
    float mClassCodeTextHeight;
    float mClassStateTextWidth;
    float mClassStateTextHeight;
    float mClassPriceTextWidth;
    float mClassPriceTextHeight;
    float mClassCodeOffsetX;
    float mClassCodeOffsetY;
    float mClassStateOffsetX;
    float mClassStateOffsetY;
    float mClassPriceOffsetX;
    float mClassPriceOffsetY;
    //背景的画笔
    private Paint mPaintBackground;
    //屏幕宽度
    private float mScreenWidth;
    //单元格的宽度
    private float mCellWidth;
    //“周”的标题栏高度
    private float mTitleHeight;
    //内容方格的高度
    private float mCellHeight;
    //内容总高度
    private float mContentHeight;
    //线条颜色
    private int mColorLine;
    //每节课的背景色
    private int mColorRectBg;
    //课程小十字架宽度
    private int mCrossWidth;
    //手势探测器
    private GestureDetectorCompat mGestureDetector;
    //当前的水平滑动方向
    private Direction mCurrentScrollDirection = Direction.NONE;
    //当前的快速滑动方向
    private Direction mCurrentFlingDirection = Direction.NONE;
    //水平方向的滚动位置计算器
    private OverScroller mHorizontalScroller;
    //垂直方向的位置计算器
    private OverScroller mVerticalScroller;
    //每周的第一条竖线绘制起始点
    private float mWeekStartX;
    //垂直滚动偏移量
    private float mScrollOffsetY = 0f;
    //View在水平方向的移动偏移量
    private float mScrollOffsetX = 0;

    private enum Direction {
        NONE, HORIZONTAL, VERTICAL, RIGHT, LEFT
    }

    private float mScreenHeight, mStatusbarHeight;

    //手指滑动方向（水平）
    private Direction MOVE_DIRECTION = Direction.NONE;

    //天数偏移量，右滑+，左滑-，在任何状态下，同时保持绘制8天，如果处于静止/初始状态，那么默认mDayOffset = 0；
    private int mDayOffset = 0;
    //当前的日期
    private Calendar mCalendarToday;
    //临时日期变量
    private Calendar tempDay;
    //是否可以垂直滚动：判断依据：view的底部未超出屏幕，即getTop()+标题+内容<屏幕高度
    private boolean mCanVerticalScroll = false;
    //是否显示价格
    private boolean isShowPrice = true;
    private final GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            mHorizontalScroller.forceFinished(true);
            mVerticalScroller.forceFinished(true);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (mCurrentScrollDirection == Direction.HORIZONTAL) {
                if (distanceX > 0) {
                    MOVE_DIRECTION = Direction.LEFT;
                } else if (distanceX < 0) {
                    MOVE_DIRECTION = Direction.RIGHT;
                } else {
                    MOVE_DIRECTION = Direction.NONE;
                }
                mScrollOffsetX -= distanceX;
            } else if (mCurrentScrollDirection == Direction.VERTICAL) {
                //上拉时，distanceY>0，拉动的距离不超过日历的高度在默认情况下不显示的部分的高度
                Log.v("onScroll", "getHeight=" + getHeight());
                if (mScrollOffsetY + distanceY >= 0) {
                    if (mScrollOffsetY + distanceY <=
                            mCellHeight * timeSlotList.size() - (mScreenHeight - mStatusbarHeight - mTitleHeight)) {
                        mScrollOffsetY += distanceY;
                    } else {
                        mScrollOffsetY = mCellHeight * timeSlotList.size() - (mScreenHeight - mStatusbarHeight - mTitleHeight);
                    }
                }
            } else {//down之后的第一次onscroll被舍弃掉，因为它一下子滑动太大导致跳帧的感觉
                if (Math.abs(distanceX) > Math.abs(distanceY)) {
                    mCurrentScrollDirection = Direction.HORIZONTAL;
                    mCurrentFlingDirection = Direction.HORIZONTAL;
                } else {
                    mCurrentFlingDirection = Direction.VERTICAL;
                    mCurrentScrollDirection = Direction.VERTICAL;
                }
            }
            postInvalidate();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (mCurrentFlingDirection == Direction.VERTICAL) {
//                int startY = (int) (mTitleHeight - mScrollOffsetY);
                int startY = - (int)mScrollOffsetY;
//                int minY = (int) (-mCellHeight * timeSlotList.size() + getHeight());
                int minY = -(int)(mCellHeight * timeSlotList.size() - (mScreenHeight - mStatusbarHeight - mTitleHeight));
                int maxY = 0;
                Log.v("onFling startY="+startY, "minY=" + minY+",  maxY="+maxY);
//                mVerticalScroller.fling(0, startY, 0, (int) (velocityY * 0.8), 0, 0, minY, maxY);
                mVerticalScroller.fling(0, startY, 0, (int) (velocityY * 0.8), 0, 0, minY, maxY, 0, 0);
                ViewCompat.postInvalidateOnAnimation(WeekCalendarView.this);
//            } else if (mCurrentFlingDirection == Direction.HORIZONTAL) {
//                mHorizontalScroller.forceFinished(true);
//                int startX = (int) mScrollOffsetX;
//                int minX = (int) (Math.floor(mScrollOffsetX / (mCellWidth * 7)) * (mCellWidth * 7));
//                int maxX = (int) (Math.ceil(mScrollOffsetX / (mCellWidth * 7)) * (mCellWidth * 7));
//                mHorizontalScroller.fling(startX, 0, (int) (velocityX * 0.8), 0, minX, maxX, 0, 0);
//                ViewCompat.postInvalidateOnAnimation(WeekClassView.this);
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (e.getX() > mCellWidth && e.getY() >= mTitleHeight) {
                if (mClickListener != null) {
                    int daysFromWeekStart = (int) ((e.getX() - mWeekStartX) / mCellWidth);
                    tempDay = getStartDayOfMonth();
                    tempDay.add(Calendar.DAY_OF_MONTH, daysFromWeekStart);
                    Map<Integer, List<SingleCourse>> dayClassMap = classMap.get(tempDay);
                    if (dayClassMap != null) {
                        int timeSlotNo = (int) ((e.getY() - mTitleHeight - mScrollOffsetY) / mCellHeight);
                        int slotIdInSec = timeSlotList.get(timeSlotNo).getId();
                        List<SingleCourse> timeslotClassList = dayClassMap.get(slotIdInSec);
                        if (null != timeslotClassList && timeslotClassList.size() > 0) {
                            mClickListener.onClassClick(tempDay, timeslotClassList);
                        }
                    }
                }
            }
            return super.onSingleTapConfirmed(e);
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mCurrentScrollDirection == Direction.HORIZONTAL) {
                mHorizontalScroller.startScroll((int) mScrollOffsetX, 0, (int) getOffset(), 0);
                ViewCompat.postInvalidateOnAnimation(WeekCalendarView.this);
            }
            mCurrentScrollDirection = Direction.NONE;
        }
        return mGestureDetector.onTouchEvent(event);
    }

    private boolean isComputing = false;
    private int lastWeekOffset = 0;

    @Override
    public void computeScroll() {
        super.computeScroll();
        //水平滑动松手之后的继续滑动
        if (mHorizontalScroller.computeScrollOffset()) {
            mScrollOffsetX = mHorizontalScroller.getCurrX();
            isComputing = true;
            ViewCompat.postInvalidateOnAnimation(this);
        } else {
            if (isComputing) {
                isComputing = false;
//                int weekOffset = Math.round((scrollStartOffsetX - mScrollOffsetX) / (mCellWidth *
                int weekOffset = Math.round((-mScrollOffsetX) / (mCellWidth * 7));
                if (mChangeListener != null && weekOffset != lastWeekOffset) {
                    mChangeListener.onWeekChange(weekOffset);
                    lastWeekOffset = weekOffset;
                }
            }
        }
        if (mVerticalScroller.computeScrollOffset()) {
            Log.v("mVerticalScroller.currY",""+ mVerticalScroller.getCurrY());
            //相对初始位置的偏移量
            mScrollOffsetY = /*mTitleHeight - */-mVerticalScroller.getCurrY();
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    //获取手松开后view所需要滑动的偏移量
    private float getOffset() {
        float offset = mScrollOffsetX % (mCellWidth * 7);
        if (offset > 0) {//松手之前View是向右偏移的
            if (MOVE_DIRECTION == Direction.RIGHT) {//手向右滑动
                if (offset >= 2 * mCellWidth) {//滑动大于2个单元宽度
                    offset = mCellWidth * 7 - offset;
                } else {
                    offset = -offset;
                }
            } else {//手指左移
                if (offset <= 5 * mCellWidth) {//进击
                    offset = -offset;
                } else {
                    offset = mCellWidth * 7 - offset;
                }
            }
        } else {//松手之前View是向左偏移的
            if (MOVE_DIRECTION == Direction.RIGHT) {
                if (offset >= -5 * mCellWidth) {//进击
                    offset = -offset;
                } else {
                    offset = -7 * mCellWidth - offset;
                }
            } else {
                if (offset <= -2 * mCellWidth) {//进击
                    offset = -7 * mCellWidth - offset;
                } else {
                    offset = -offset;
                }
            }
        }
        MOVE_DIRECTION = Direction.NONE;
        return offset;
    }

    public WeekCalendarView(Context context) {
        this(context, null);
    }

    public WeekCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    //    private int classBgArray[] = new int[]{Color.parseColor("#3ed96c"),
//            Color.parseColor("#fb9610"),
//            Color.parseColor("#cacaca"),
//            Color.parseColor("#44a9d8")};
    private float mClassRectPadding;

    private void init() {
        mCalendarToday = Calendar.getInstance();
        mCalendarToday.set(Calendar.HOUR_OF_DAY, 0);
        mCalendarToday.set(Calendar.MINUTE, 0);
        mCalendarToday.set(Calendar.SECOND, 0);
        mCalendarToday.set(Calendar.MILLISECOND, 0);

        mClassRectPadding = DisplayUtil.dip2px(mContext, 2);

        //线条颜色
        mColorLine = Color.parseColor("#009be6");
        //每节课的背景色
        mColorRectBg = Color.parseColor("#ffffff");

//        setBackgroundColor(Color.parseColor("#ffffff"));
        float mLineWidth = DisplayUtil.dip2px(mContext, 1);
        mScreenWidth = DisplayUtil.getScreenWidth(mContext);
        mCellWidth = (mScreenWidth - getPaddingLeft() - getPaddingRight()) / 8;
        mTitleHeight = DisplayUtil.dip2px(mContext, 42);
        mCellHeight = DisplayUtil.dip2px(mContext, 60);
        mContentHeight = mCellHeight * timeSlotList.size();
        mCrossWidth = DisplayUtil.dip2px(mContext, 8);

        mPaintLine = new Paint();
        mPaintLine.setAntiAlias(true);
        mPaintLine.setStyle(Paint.Style.FILL);
        mPaintLine.setStrokeWidth(mLineWidth);
        mPaintLine.setColor(mColorLine);

        mPaintBackground = new Paint();
        mPaintBackground.setAntiAlias(true);
        mPaintBackground.setStyle(Paint.Style.FILL);
        mPaintBackground.setColor(mColorRectBg);

        mGestureDetector = new GestureDetectorCompat(mContext, mGestureListener);
        mHorizontalScroller = new OverScroller(mContext);
        mVerticalScroller = new OverScroller(mContext);

        mPaintWeekDayText = new Paint();
        mPaintWeekDayText.setAntiAlias(true);
        mPaintWeekDayText.setStyle(Paint.Style.FILL);
        mPaintWeekDayText.setTextSize(DisplayUtil.sp2px(mContext, 16));
        mPaintWeekDayText.setColor(Color.parseColor("#7b7e7e"));
        //文字所占的区域
        Rect textBounds = new Rect();
        String testString1 = "六";
        mPaintWeekDayText.getTextBounds(testString1, 0, testString1.length(), textBounds);
        mWeekDayTextWidth = textBounds.width();
        mWeekDayTextHeight = textBounds.height();

        mPaintMonthDayText = new Paint();
        mPaintMonthDayText.setAntiAlias(true);
        mPaintMonthDayText.setStyle(Paint.Style.FILL);
        mPaintMonthDayText.setTextSize(DisplayUtil.sp2px(mContext, 9));
        mPaintMonthDayText.setColor(Color.parseColor("#969696"));
        //初始化1~31各个数字的占用宽度
        initMonthDayTextWidth(textBounds);
        //计算标题两行文字的描绘高度
        float titleTextGap = DisplayUtil.dip2px(mContext, 6);
        float titleTextHeight = mWeekDayTextHeight + titleTextGap + mMonthDayTextHeight;
        weekDayDrawStartY = (mTitleHeight - titleTextHeight) / 2 + mWeekDayTextHeight;
        monthDayDrawStartY = (mTitleHeight + titleTextHeight) / 2;

        mPaintTimeSlotText = new Paint();
        mPaintTimeSlotText.setAntiAlias(true);
        mPaintTimeSlotText.setStyle(Paint.Style.FILL);
        mPaintTimeSlotText.setTextSize(DisplayUtil.sp2px(mContext, 11));
        mPaintTimeSlotText.setColor(Color.parseColor("#ababab"));
        String testString3 = "09:00";
        mPaintTimeSlotText.getTextBounds(testString3, 0, testString3.length(), textBounds);
        mTimeSlotTextWidth = textBounds.width();
        mTimeSlotTextStartX = (mCellWidth - mTimeSlotTextWidth) / 2;
        mTimeSlotTextHeight = textBounds.height();

        mPaintTimeSlotNoText = new Paint();
        mPaintTimeSlotNoText.setAntiAlias(true);
        mPaintTimeSlotNoText.setStyle(Paint.Style.FILL);
        mPaintTimeSlotNoText.setTextSize(DisplayUtil.sp2px(mContext, 16));
        mPaintTimeSlotNoText.setColor(Color.parseColor("#7d7d7e"));
        String testString4 = "7";
        mPaintTimeSlotNoText.getTextBounds(testString4, 0, testString4.length(), textBounds);
        mTimeSlotNoTextWidth = textBounds.width();
        mTimeSlotNoTextStartX = (mCellWidth - mTimeSlotNoTextWidth) / 2;
        mTimeSlotNoTextHeight = textBounds.height();

        float timeSlotTextGap = DisplayUtil.dip2px(mContext, 7);
        float timeSlotTextHeight = mTimeSlotTextHeight + timeSlotTextGap + mTimeSlotNoTextHeight;
        mTimeSlotStartCenterOffsetY = mTimeSlotTextHeight - timeSlotTextHeight / 2;
        mTimeSlotNoStartCenterOffsetY = timeSlotTextHeight / 2;

        mPaintClassCodeText = new Paint();
        mPaintClassCodeText.setAntiAlias(true);
        mPaintClassCodeText.setStyle(Paint.Style.FILL);
        mPaintClassCodeText.setTypeface(Typeface.DEFAULT_BOLD);
        mPaintClassCodeText.setTextSize(DisplayUtil.sp2px(mContext, 11));
        mPaintClassCodeText.setColor(Color.parseColor("#ffffff"));
        String testString5 = "L2NK";
        mPaintClassCodeText.getTextBounds(testString5, 0, testString5.length(), textBounds);
        mClassCodeTextWidth = textBounds.width();
        mClassCodeTextHeight = textBounds.height();

        mPaintClassStateText = new Paint();
        mPaintClassStateText.setAntiAlias(true);
        mPaintClassStateText.setStyle(Paint.Style.FILL);
        mPaintClassStateText.setTextSize(DisplayUtil.sp2px(mContext, 11));
        mPaintClassStateText.setColor(Color.parseColor("#ffffff"));
        String testString6 = "20/30";
        mPaintClassStateText.getTextBounds(testString6, 0, testString6.length(), textBounds);
        mClassStateTextWidth = textBounds.width();
        mClassStateTextHeight = textBounds.height();

        mPaintClassPriceText = new Paint();
        mPaintClassPriceText.setAntiAlias(true);
        mPaintClassPriceText.setStyle(Paint.Style.FILL);
        mPaintClassCodeText.setTypeface(Typeface.DEFAULT_BOLD);
        mPaintClassPriceText.setTextSize(DisplayUtil.sp2px(mContext, 14));
        mPaintClassPriceText.setColor(Color.parseColor("#ffffff"));
        String testString7 = "¥200";
        mPaintClassPriceText.getTextBounds(testString7, 0, testString7.length(), textBounds);
        mClassPriceTextWidth = textBounds.width();
        mClassPriceTextHeight = textBounds.height();

        mClassCodeOffsetX = (mCellWidth - mClassCodeTextWidth) / 2;
        mClassStateOffsetX = (mCellWidth - mClassStateTextWidth) / 2;
        mClassPriceOffsetX = (mCellWidth - mClassPriceTextWidth) / 2;

        float classTextGap = DisplayUtil.dip2px(mContext, 7);
        float classTextHeight = mClassCodeTextHeight + mClassStateTextHeight + mClassPriceTextHeight + classTextGap * 2;

        mClassCodeOffsetY = -classTextHeight / 2 + mClassCodeTextHeight;
        mClassStateOffsetY = -classTextHeight / 2 + mClassCodeTextHeight + classTextGap + mClassStateTextHeight;
        mClassPriceOffsetY = classTextHeight / 2;


    }

    //初始化月的“天”所占的位置宽度，用于正确绘制“天”的位置
    private void initMonthDayTextWidth(Rect textBounds) {
        for (int i = 0; i < 31; i++) {
            String testString = String.valueOf(i + 1);
            mPaintMonthDayText.getTextBounds(testString, 0, testString.length(), textBounds);
            mMonthDayTextWidth[i] = textBounds.width();
        }
        mMonthDayTextHeight = textBounds.height();
    }

    //初始化当前页面月周的文本绘制起始点
    private void initDayOfWeekTextStartX() {
        for (int i = 0; i < 8; i++) {
            dayOffWeekStartXArr[i] = mWeekStartX + i * mCellWidth + (mCellWidth - mWeekDayTextWidth) / 2;
        }
    }

    //初始化当前页面月日期的文本绘制起始点
    private void initDayOfMonthTextStartX() {
        for (int i = 0; i < 8; i++) {
            int dayOfMonth = getDayOfMonthInWeek(i);
            dayOfMonthStartArr[i] = mWeekStartX + i * mCellWidth + (mCellWidth - mMonthDayTextWidth[dayOfMonth - 1]) / 2;
        }
    }

    private void drawTimeSlot(Canvas canvas) {
        canvas.drawRect(0, 0, mCellWidth, mTitleHeight + mContentHeight, mPaintBackground);
        //********画横线********
        float x1 = 0;
        float y1 = mTitleHeight + mCellHeight - mScrollOffsetY;
        float x2 = mCellWidth;
        float y2 = y1;
        for (int i = 0; i < timeSlotList.size(); i++) {
            canvas.drawText(timeSlotList.get(i).getStart(), mTimeSlotTextStartX, y1 - mCellHeight / 2 + mTimeSlotStartCenterOffsetY, mPaintTimeSlotText);
            canvas.drawText("" + (i + 1), mTimeSlotNoTextStartX, y1 - mCellHeight / 2 + mTimeSlotNoStartCenterOffsetY, mPaintTimeSlotNoText);
            if (i < timeSlotList.size() - 1) {
                canvas.drawLine(x1, y1, x2, y2, mPaintLine);
//                canvas.drawLine(x1, y1, mScreenWidth, y2, mPaintLine);
            }
            y1 += mCellHeight;
            y2 = y1;
        }
        //********画竖线********
        float x3 = mCellWidth;
        float y3 = mTitleHeight - mScrollOffsetY;
        float y4 = mTitleHeight + mContentHeight - mScrollOffsetY;
        canvas.drawLine(x3, y3, x3, y4, mPaintLine);
        canvas.drawLine(0, y4, mScreenWidth, y4, mPaintLine);
    }

    private void drawTitle(Canvas canvas) {
        float x1 = mCellWidth;
        float y1 = mTitleHeight;
        float x2 = mScreenWidth;
        //********画背景********
        canvas.drawRect(0, 0, mScreenWidth, y1, mPaintBackground);
        //********画横线********
        canvas.drawLine(x1, y1, x2, y1, mPaintLine);
        //********画文字********
        //初始化星期的文本绘制起点横坐标
        initDayOfWeekTextStartX();
        //初始化标题栏月中天的绘制起始位置
        initDayOfMonthTextStartX();
        //当前周日历中所显示的第一天为一星期中的第几天（以周日为一周的开始，包括滑动中的状态）
        int startDayOfWeek = getStartDayOfWeek();
        for (int j = 0; j < 8; j++) {
            //画星期
            canvas.drawText(weekDayNamesArray[(startDayOfWeek + j - 1) % 7], dayOffWeekStartXArr[j], weekDayDrawStartY, mPaintWeekDayText);
            //画月
            canvas.drawText("" + getDayOfMonthInWeek(j), dayOfMonthStartArr[j], monthDayDrawStartY, mPaintMonthDayText);
        }
        //绘制title星期分割线
        float xVertical = mWeekStartX;
        for (int j = 0; j <= 7; j++) {
            canvas.drawLine(xVertical, 0, xVertical, mTitleHeight, mPaintLine);
            xVertical += mCellWidth;
        }
    }

    private Calendar getStartDayOfMonth() {
        int dayOfWeek = mCalendarToday.get(Calendar.DAY_OF_WEEK);
        tempDay = (Calendar) mCalendarToday.clone();
        tempDay.add(Calendar.DAY_OF_MONTH, -dayOfWeek + 1 - mDayOffset);
        return tempDay;
    }

    private int getDayOfMonthInWeek(int j) {
        tempDay = getStartDayOfMonth();
        tempDay.add(Calendar.DAY_OF_MONTH, j);
        return tempDay.get(Calendar.DAY_OF_MONTH);
    }

    //获取周日历中第一天的为一星期中的第几天(包括滑动状态下)
    private int getStartDayOfWeek() {
        int dayOfWeek = mCalendarToday.get(Calendar.DAY_OF_WEEK);
        tempDay = (Calendar) mCalendarToday.clone();
        tempDay.add(Calendar.DAY_OF_MONTH, -dayOfWeek + 1 - mDayOffset);
        return tempDay.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * 跳转到指定的周
     *
     * @param startDayOfWeek 指定的那周的周一
     */
    public void jumpToDate(Calendar startDayOfWeek) {
        tempDay = (Calendar) mCalendarToday.clone();
        int dayOfWeek = mCalendarToday.get(Calendar.DAY_OF_WEEK);
        tempDay.add(Calendar.DAY_OF_MONTH, -dayOfWeek + 1);

        int currYear = tempDay.get(Calendar.YEAR);
        int destYear = startDayOfWeek.get(Calendar.YEAR);
        int currDayOfYear = tempDay.get(Calendar.DAY_OF_YEAR);
        int destDayOfYear = startDayOfWeek.get(Calendar.DAY_OF_YEAR);
        int offset;
        if (currYear == destYear) {
            offset = destDayOfYear - currDayOfYear;
        } else {
            int maxDaysOfYear;
            if (currYear < destYear) {
                maxDaysOfYear = tempDay.getActualMaximum(Calendar.DAY_OF_YEAR);
                offset = maxDaysOfYear - currDayOfYear + destDayOfYear;
            } else {
                maxDaysOfYear = startDayOfWeek.getActualMaximum(Calendar.DAY_OF_YEAR);
                offset = maxDaysOfYear - destDayOfYear + currDayOfYear;
            }
        }
        //调整当前周的偏移量
        lastWeekOffset = Math.round(offset / 7);
        mScrollOffsetX = -lastWeekOffset * 7 * mCellWidth;
        postInvalidate();
    }

    //绘制课程
    private void drawClasses(Canvas canvas) {
        if (mScrollOffsetX > 0) {
            mDayOffset = (int) (mScrollOffsetX / mCellWidth + 1);
        } else {
            mDayOffset = (int) (Math.ceil(mScrollOffsetX / mCellWidth));
        }

        //0 < mWeekStartX < mCellWidth
        if (mScrollOffsetX > 0) {
            mWeekStartX = (mScrollOffsetX % (mCellWidth * 7)) % mCellWidth;
        } else {
            mWeekStartX = mCellWidth + (mScrollOffsetX % (mCellWidth * 7)) % mCellWidth;
        }
        //********画课程********
        float x3 = mWeekStartX;
        float y3 = mTitleHeight - mScrollOffsetY;
        float y4 = mTitleHeight + mContentHeight - mScrollOffsetY;

        for (int j = 0; j <= 7; j++) {
            tempDay = getStartDayOfMonth();
            tempDay.add(Calendar.DAY_OF_MONTH, j);
            Map<Integer, List<SingleCourse>> dayClassMap = classMap.get(tempDay);
            if (dayClassMap != null) {
                for (int k = 0; k < timeSlotList.size(); k++) {
                    int slotIdInSec = timeSlotList.get(k).getId();
                    List<SingleCourse> slotClassList = dayClassMap.get(slotIdInSec);
                    if (slotClassList != null && slotClassList.size() > 0) {
                        if (slotClassList.size() == 1) {
                            drawSingleClass(canvas, slotClassList, x3, y3, j, k);
                        } else if ((slotClassList.size() == 2)) {
                            drawDoubleClass(canvas, slotClassList, x3, y3, j, k);
                        } else {
                            drawMoreClass(canvas, slotClassList, x3, y3, j, k);
                        }
                    }
                }
            }
        }
        //********画横线********
        float x1 = mCellWidth;
        float y1 = mTitleHeight + mCellHeight - mScrollOffsetY;
        float x2 = mScreenWidth;
        for (int i = 0; i <= timeSlotList.size() - 2; i++) {
//            mPaintLine.setColor(Color.parseColor("#ffffff"));
//            canvas.drawLine(x1, y1, x2, y1, mPaintLine);
//            mPaintLine.setColor(mColorLine);
            float x10 = mWeekStartX - mCrossWidth / 2;
            for (int j = 0; j <= 7; j++) {
                canvas.drawLine(x10, y1, x10 + mCrossWidth, y1, mPaintLine);
                x10 += mCellWidth;
            }
            y1 += mCellHeight;
        }
        //画竖线
        float xVertical = mWeekStartX;
        for (int j = 0; j <= 7; j++) {
            /*mPaintLine.setColor(Color.parseColor("#000000"));
            canvas.drawLine(xVertical, y3, xVertical, y4, mPaintLine);*/
            mPaintLine.setColor(mColorLine);
            float y10 = mCellHeight + y3 - mCrossWidth / 2;
            for (int k = 0; k <= timeSlotList.size() - 2; k++) {
                canvas.drawLine(xVertical, y10, xVertical, y10 + mCrossWidth, mPaintLine);
                y10 += mCellHeight;
            }
            xVertical += mCellWidth;
        }
        mPaintBackground.setColor(mColorRectBg);
    }

    private void drawSingleClass(Canvas canvas, List<SingleCourse> slotClassList,
                                 float weekStartX, float y3, int dayInWeek, int timeSlot) {
        for (SingleCourse singleClass : slotClassList) {
            mPaintBackground.setColor(getCourseColor(singleClass));
            //画背景框
            canvas.drawRect(weekStartX + dayInWeek * mCellWidth + 4, y3 + (timeSlot) * mCellHeight + 4,
                    weekStartX + dayInWeek * mCellWidth + mCellWidth - 4, y3 + (timeSlot + 1) * mCellHeight - 4, mPaintBackground);
            //画课程代码
            int type = singleClass.getType();
            String courseCode = "";
            if (type == 1) {
                courseCode = "户外";
            } else {
                courseCode = "" + singleClass.getClevel() + singleClass.getSerial();
            }
            canvas.drawText(courseCode,
                    weekStartX + dayInWeek * mCellWidth + mClassCodeOffsetX,
                    y3 + (timeSlot) * mCellHeight + mCellHeight / 2 + mClassCodeOffsetY,
                    mPaintClassCodeText);
            //画课程状态
            canvas.drawText("" + (singleClass.getQueueNum() + singleClass.getBookNum()) + "/" + singleClass.getCapacity(),
                    weekStartX + dayInWeek * mCellWidth + mClassStateOffsetX,
                    y3 + (timeSlot) * mCellHeight + mCellHeight / 2 + mClassStateOffsetY,
                    mPaintClassStateText);
            //画课程价格
            //@deprecate since 2015/11/30
            if (isShowPrice) {
                canvas.drawText("¥" + (int) singleClass.getPrice()/*+"¥200"*/,
                        weekStartX + dayInWeek * mCellWidth + mClassPriceOffsetX,
                        y3 + (timeSlot) * mCellHeight + mCellHeight / 2 + mClassPriceOffsetY,
                        mPaintClassPriceText);
            }
        }
    }

    private void drawDoubleClass(Canvas canvas, List<SingleCourse> slotClassList,
                                 float weekStartX, float y3, int dayInWeek, int timeSlot) {
        float xrb1, yrb1, xrb2, yrb2;
        mPaintBackground.setColor(getCourseColor(slotClassList.get(0)));

        xrb1 = weekStartX + dayInWeek * mCellWidth + mClassRectPadding;
        xrb2 = weekStartX + dayInWeek * mCellWidth + mCellWidth - mClassRectPadding;

        yrb1 = y3 + (timeSlot) * mCellHeight + mClassRectPadding;
        yrb2 = y3 + (timeSlot) * mCellHeight + mCellHeight / 2f;

        canvas.drawRect(xrb1, yrb1, xrb2, yrb2 - 1, mPaintBackground);

        SingleCourse singleClass = slotClassList.get(0);
        int type = singleClass.getType();
        String courseCode = "";
        if (type == 1) {
            courseCode = "室外";
        } else {
            courseCode = "" + singleClass.getClevel() + singleClass.getSerial();
        }
        canvas.drawText(courseCode,
                weekStartX + dayInWeek * mCellWidth + mClassCodeOffsetX,
                y3 + (timeSlot) * mCellHeight + mCellHeight / 4 + mClassCodeTextHeight / 2,
                mPaintClassCodeText);

        //***********************************************//
        float yrb3, yrb4;
        mPaintBackground.setColor(getCourseColor(slotClassList.get(1)));

        yrb3 = y3 + (timeSlot) * mCellHeight + mCellHeight / 2f;
        yrb4 = y3 + (timeSlot + 1) * mCellHeight - mClassRectPadding;

        canvas.drawRect(xrb1, yrb3 + 1, xrb2, yrb4, mPaintBackground);

        SingleCourse singleClass2 = slotClassList.get(1);
        int type2 = singleClass2.getType();
        String courseCode2 = "";
        if (type2 == 1) {
            courseCode2 = "室外";
        } else {
            courseCode2 = "" + singleClass2.getClevel() + singleClass2.getSerial();
        }
        canvas.drawText(courseCode2,
                weekStartX + dayInWeek * mCellWidth + mClassCodeOffsetX,
                y3 + (timeSlot) * mCellHeight + 3 * mCellHeight / 4 + mClassCodeTextHeight / 2,
                mPaintClassCodeText);

        mPaintBackground.setColor(mColorRectBg);
    }

//    if (courseIndex == 3) {
//        float y = ((cell.j * mCellHeight + courseStartOffsetY1 + 2 * (courseTextHeight + 6) + radiusDot + dotDistance));
//        float x = (float) (mCellWidth * (cell.i + 0.5)) - dotDistance;
//        mCirclePaint.setColor(mColorDot);
//        mCirclePaint.setStyle(Paint.Style.FILL);
//        for (int i = 0; i < 3; i++) {
//            canvas.drawCircle(x + i * dotDistance, y, radiusDot, mCirclePaint);
//        }
//    }

    private void drawMoreClass(Canvas canvas, List<SingleCourse> slotClassList,
                               float weekStartX, float y3, int dayInWeek, int timeSlot) {
        float xrb1, yrb1, xrb2, yrb2;
        mPaintBackground.setColor(getCourseColor(slotClassList.get(0)));

        xrb1 = weekStartX + dayInWeek * mCellWidth + mClassRectPadding;
        xrb2 = weekStartX + dayInWeek * mCellWidth + mCellWidth - mClassRectPadding;

        yrb1 = y3 + (timeSlot) * mCellHeight + mClassRectPadding;
        yrb2 = y3 + (timeSlot) * mCellHeight + mCellHeight / 3f;

        canvas.drawRect(xrb1, yrb1, xrb2, yrb2 - 1, mPaintBackground);

        SingleCourse singleClass = slotClassList.get(0);
        int type = singleClass.getType();
        String courseCode = "";
        if (type == 1) {
            courseCode = "室外";
        } else {
            courseCode = "" + singleClass.getClevel() + singleClass.getSerial();
        }
        canvas.drawText(courseCode,
                weekStartX + dayInWeek * mCellWidth + mClassCodeOffsetX,
                y3 + (timeSlot) * mCellHeight + mCellHeight / 6 + mClassCodeTextHeight / 2,
                mPaintClassCodeText);

        //***********************************************//
        float yrb3, yrb4;
        mPaintBackground.setColor(getCourseColor(slotClassList.get(1)));

        yrb3 = y3 + (timeSlot) * mCellHeight + mCellHeight / 3f;
        yrb4 = y3 + (timeSlot) * mCellHeight + 2 * mCellHeight / 3f;

        canvas.drawRect(xrb1, yrb3 + 1, xrb2, yrb4 - 1, mPaintBackground);

        SingleCourse singleClass2 = slotClassList.get(1);
        int type2 = singleClass2.getType();
        String courseCode2 = "";
        if (type2 == 1) {
            courseCode2 = "室外";
        } else {
            courseCode2 = "" + singleClass2.getClevel() + singleClass2.getSerial();
        }
        canvas.drawText(courseCode2,
                weekStartX + dayInWeek * mCellWidth + mClassCodeOffsetX,
                y3 + (timeSlot) * mCellHeight + mCellHeight / 2 + mClassCodeTextHeight / 2,
                mPaintClassCodeText);

        //***********************************************//

        float yrb5, yrb6;
        yrb5 = y3 + (timeSlot) * mCellHeight + 2 * mCellHeight / 3f;
        yrb6 = y3 + (timeSlot) * mCellHeight - mClassRectPadding;
        if (slotClassList.size() >= 3) {
//            mPaintBackground.setColor(getCourseColor(slotClassList.get(2)));
//            canvas.drawRect(xrb1, yrb5 + 1, xrb2, yrb6, mPaintBackground);
//            SingleCourse singleClass3 = slotClassList.get(2);
//            int type3 = singleClass3.getType();
//            String courseCode3 = "";
//            if (type3 == 1) {
//                courseCode3 = "室外";
//            } else {
//                courseCode3 = "" + singleClass3.getClevel() + singleClass3.getSerial();
//            }
//            canvas.drawText(courseCode3,
//                    weekStartX + dayInWeek * mCellWidth + mClassCodeOffsetX,
//                    y3 + (timeSlot) * mCellHeight + 5 * mCellHeight / 6 + mClassCodeTextHeight / 2,
//                    mPaintClassCodeText);
//        } else {
            mPaintBackground.setColor(Color.parseColor("#8e8c8a"));

            float radius = mClassCodeTextHeight * 0.4f;
            float centerDistance = 3f * radius;
            float startCenterX = weekStartX + dayInWeek * mCellWidth + mCellWidth / 2 - centerDistance;
            for (int c = 0; c < 3; c++) {
                canvas.drawCircle(startCenterX + c * centerDistance, yrb1 + 5 * mCellHeight / 6 - mClassRectPadding - 2, radius, mPaintBackground);
            }
        }

        mPaintBackground.setColor(mColorRectBg);
    }

    //获取不同状态下的课程颜色
    private int getCourseColor(SingleCourse course) {
        String state = course.getState();
        int color;
        if (state != null) {
            switch (state) {
                case "queuing":  //排位中
                    color = Color.parseColor("#fc9611");
                    break;
                case "ordered": //已预约
                    color = Color.parseColor("#0CCF06");
                    break;
                case "signed": //已签到
                    color = Color.parseColor("#00ddff");
                    break;
                case "absented": //已旷课
                    color = Color.parseColor("#FF6E01");
                    break;
                case "leaved": //已请假
                    color = Color.parseColor("#FF6E01");
                    break;
                case "canceled": //已取消
                    color = Color.parseColor("#FF6E01");
                    break;
                default:
                    color = Color.parseColor("#cacaca");
                    break;
            }
        } else {
            color = Color.parseColor("#cacaca");
        }
        return color;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        Log.v("WeekClassView", "onDraw start-->" + System.currentTimeMillis());
        //画内容
        drawClasses(canvas);
        //画周上面的标题
        drawTitle(canvas);
        //画时间段
        drawTimeSlot(canvas);
        //画左上角的区域
        drawLeftTopCorner(canvas);
//        Log.v("WeekClassView", "onDraw end-->" + System.currentTimeMillis());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.v("onMeasure", "" + mScreenWidth + " / " + (mTitleHeight + mContentHeight));
        setMeasuredDimension((int) mScreenWidth, (int) (mTitleHeight + mContentHeight));
    }

    private void drawLeftTopCorner(Canvas canvas) {
        canvas.drawRect(0, 0, mCellWidth, mTitleHeight, mPaintBackground);
        canvas.drawLine(0, 0, mScreenWidth, 0, mPaintLine);
        canvas.drawLine(0, mTitleHeight, mCellWidth, mTitleHeight, mPaintLine);
        canvas.drawLine(mCellWidth, 0, mCellWidth, mTitleHeight, mPaintLine);
    }

    //时间段数据
//    private String[] mTimeSlotArr;
    private List<TimeSlotHttpObj> timeSlotList = new ArrayList<>();
    //课程数据
    private Map<Calendar, Map<Integer, List<SingleCourse>>> classMap = new HashMap<>();

    public void setmTimeSlotArr(List<TimeSlotHttpObj> timeSlots) {
        this.timeSlotList = timeSlots;
        mContentHeight = mCellHeight * timeSlotList.size();

        mScreenHeight = DisplayUtil.getScreenHeight(mContext);
        mStatusbarHeight = DisplayUtil.getStatusBarHeight(mContext);
        int location[] = new int[2];
        getLocationOnScreen(location);
        Log.v("getLocationOnScreen", "location=" + location[0] + "/" + location[1]);
        int top = location[1];
        mContentHeight = mCellHeight * timeSlotList.size();
        Log.v("mCanVerticalScroll", "top=" + top +
                "/ mTitleHeight + mContentHeight=" + (mTitleHeight + mContentHeight) + "/ mScreenHeight=" + mScreenHeight);
        mCanVerticalScroll = top + mTitleHeight + mContentHeight > mScreenHeight;
        Log.v("mCanVerticalScroll", "mCanVerticalScroll:" + mCanVerticalScroll);
        postInvalidate();
    }

    public void setClassMap(Map<Calendar, Map<Integer, List<SingleCourse>>> map) {
        this.classMap = map;
        postInvalidate();
    }

    public void setClassMap(Map<Calendar, Map<Integer, List<SingleCourse>>> map, boolean isShowPrice) {
        this.classMap = map;
        this.isShowPrice = isShowPrice;
        postInvalidate();
    }

    public Map<Calendar, Map<Integer, List<SingleCourse>>> getClassMap() {
        return this.classMap;
    }

    public interface OnClassClickListener {
        void onClassClick(Calendar caldendar, List<SingleCourse> timeslotClassList);
    }

    private OnClassClickListener mClickListener;

    public void setOnClassClickListener(OnClassClickListener listener) {
        this.mClickListener = listener;
    }

    private OnWeekChangedListener mChangeListener;

    public void setOnWeekChangedListener(OnWeekChangedListener listener) {
        this.mChangeListener = listener;
    }

    public interface OnWeekChangedListener {
        void onWeekChange(int weekOffset);
    }

}
