package com.ubux.menuview;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.ubux.quadbubblemenu.QuadBubbleMenu;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final QuadBubbleMenu menuLayout = (QuadBubbleMenu)findViewById(R.id.qbMenu);
        final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)menuLayout.getLayoutParams();
        menuLayout.addItem(new QuadBubbleMenu.MenuItem("Bottom_Right", new ColorDrawable(0xffff2222),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        lp.gravity = Gravity.BOTTOM|Gravity.END;
                        menuLayout.setQuadrantLocation(QuadBubbleMenu.TOP_START);
                    }
                }));
        menuLayout.addItem(new QuadBubbleMenu.MenuItem("Green", new ColorDrawable(0xff22ff22),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        lp.gravity = Gravity.BOTTOM|Gravity.START;
                        menuLayout.setQuadrantLocation(QuadBubbleMenu.TOP_END);
                    }
                }));
        menuLayout.addItem(new QuadBubbleMenu.MenuItem("Yellow",
                new ColorDrawable(0xffffff22), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lp.gravity = Gravity.TOP|Gravity.START;
                menuLayout.setQuadrantLocation(QuadBubbleMenu.BOTTOM_END);
            }
        }));
        menuLayout.addItem(new QuadBubbleMenu.MenuItem("Blue",
                new ColorDrawable(0xff2222ff), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lp.gravity = Gravity.TOP|Gravity.END;
                menuLayout.setQuadrantLocation(QuadBubbleMenu.BOTTOM_START);
            }
        }));
    }
}
