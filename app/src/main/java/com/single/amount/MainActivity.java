package com.single.amount;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;

import com.xiangcheng.amount.AmountView;

public class MainActivity extends AppCompatActivity {
    DisplayMetrics dm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        final AmountView amountView = (AmountView) findViewById(R.id.amount_view);
        final AmountView originalAmountView = (AmountView) findViewById(R.id.original_amountview);

        dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        Button button =(Button)findViewById(R.id.start) ;

        button.getLayoutParams().width = (int) (dm.widthPixels * 0.6);
        button.getLayoutParams().height = (int) (dm.widthPixels * 0.6);

        final int ARC_WIDTH = dm.widthPixels / 12;
        final int ARC_MARGIN = dm.widthPixels / 16;

        originalAmountView.setStrokeWidth(12);
        originalAmountView.setOvalSize(ARC_MARGIN, ARC_MARGIN,
                originalAmountView.getLayoutParams().width - ARC_MARGIN,
                originalAmountView.getLayoutParams().width - ARC_MARGIN);
        originalAmountView.setStartAngle(0);
        originalAmountView.setEndAngle(200);
        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                amountView.start();
                originalAmountView.start();
            }
        });
    }
}
