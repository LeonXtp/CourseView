package com.leon.coursecalendarview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.leon.coursecalendarview.entity.SingleCourse;
import com.leon.coursecalendarview.entity.TimeSlotHttpObj;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private WeekCalendarView weekCalendarView;
    private List<TimeSlotHttpObj> timeSlotList = new ArrayList<>();
    //层级关系：每天课程-每个时段课程(时段ID)-单节课程
    private Map<Calendar, Map<Integer, List<SingleCourse>>> weekClassMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        weekCalendarView = (WeekCalendarView) findViewById(R.id.weekcalendarview);
        init();
    }

    private void mockData() {
        for (int i = 0; i < 15; i++) {
            TimeSlotHttpObj slot = new TimeSlotHttpObj();
            slot.setId(i+1);
            slot.setStart("09:00");
            slot.setEnd("17:00");
            timeSlotList.add(slot);
        }
    }

    private void init() {
        mockData();
        weekCalendarView.setmTimeSlotArr(timeSlotList);
        weekCalendarView.setClassMap(weekClassMap, false);
    }
}
