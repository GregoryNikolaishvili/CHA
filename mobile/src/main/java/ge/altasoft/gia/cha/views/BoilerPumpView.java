package ge.altasoft.gia.cha.views;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;

import ge.altasoft.gia.cha.R;

public class BoilerPumpView extends PumpView {

    public BoilerPumpView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BoilerPumpView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BoilerPumpView(Context context) {
        super(context);
    }

    @Override
    protected void drawState(int value) {

        switch (value) {
            case 1:
                setBackgroundResource(R.drawable.pump_animation_active);
                final AnimationDrawable frameAnimation = (AnimationDrawable) getBackground();
                post(new Runnable() {
                    public void run() {
                        frameAnimation.start();
                    }
                });
                break;
            case 2:
                setBackgroundResource(R.drawable.pump_animation_standby);
                final AnimationDrawable frameAnimation2 = (AnimationDrawable) getBackground();
                post(new Runnable() {
                    public void run() {
                        frameAnimation2.start();
                    }
                });
                break;
            default:
                setBackgroundResource(R.drawable.pump_off);
        }
    }
}