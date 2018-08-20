package cn.shiguiyou.coupondemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import cn.shiguiyou.coupon.calendar.SearchCalendarView;

public class MainActivity extends AppCompatActivity {

    SearchCalendarView searchCalendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_test);

        searchCalendarView=findViewById(R.id.scv);
        searchCalendarView.setTime(2018,8);
    }
}
