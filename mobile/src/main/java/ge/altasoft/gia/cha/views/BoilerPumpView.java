package ge.altasoft.gia.cha.views;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import ge.altasoft.gia.cha.R;

public class BoilerPumpView extends ImageView {

    private boolean isOn;

    public BoilerPumpView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public BoilerPumpView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoilerPumpView(Context context) {
        super(context);
        init();
    }

    private void init() {
        isOn = false;
        setBackgroundResource(R.drawable.pump_off);
//        if (isInEditMode())
//            return;
    }

    public void setIsOn(boolean value) {
        if (isOn == value)
            return;

        isOn = value;
        if (value)
        {
            setBackgroundResource(R.drawable.pump_animation);
            final AnimationDrawable frameAnimation = (AnimationDrawable) getBackground();
            post(new Runnable(){
                public void run(){
                    frameAnimation.start();
                }
            });
        }
        else
            setBackgroundResource(R.drawable.pump_off);
    }


}