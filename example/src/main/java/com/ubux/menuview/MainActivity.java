package com.ubux.menuview;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ubux.quadbubblemenu.QuadBubbleMenu;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        QuadBubbleMenu menuLayout = (QuadBubbleMenu)findViewById(R.id.qbMenu);
        menuLayout.addItem(new QuadBubbleMenu.MenuItem("Red", new ColorDrawable(0xffff2222)));
        menuLayout.addItem(new QuadBubbleMenu.MenuItem("Green", new ColorDrawable(0xff22ff22)));
        menuLayout.addItem(new QuadBubbleMenu.MenuItem("Yellow", new ColorDrawable(0xffffff22)));
        menuLayout.addItem(new QuadBubbleMenu.MenuItem("Blue", new ColorDrawable(0xff2222ff)));
    }
}
