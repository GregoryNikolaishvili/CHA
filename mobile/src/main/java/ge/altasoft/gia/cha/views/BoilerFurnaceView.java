package ge.altasoft.gia.cha.views;

import android.content.Context;
import android.util.AttributeSet;

import ge.altasoft.gia.cha.R;

public class BoilerFurnaceView extends PumpView {

    public BoilerFurnaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BoilerFurnaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BoilerFurnaceView(Context context) {
        super(context);
    }

    @Override
    public void drawState(int value) {

        if (value > 0)
            setBackgroundResource(R.drawable.furnace_on);
        else
            setBackgroundResource(R.drawable.furnace_off);
    }
}