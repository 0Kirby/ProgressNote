package com.xqjtqy.progressnote;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class OpeningActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opening);
        final LinearLayout vis = findViewById(R.id.opening1);
        final LinearLayout invis = findViewById(R.id.opening2);
        final ImageView imageView = findViewById(R.id.logo);
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.opening);
        imageView.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animation=AnimationUtils.loadAnimation(OpeningActivity.this,R.anim.fonts);
                vis.startAnimation(animation);
                animation=AnimationUtils.loadAnimation(OpeningActivity.this,R.anim.pull);
                invis.startAnimation(animation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


}
