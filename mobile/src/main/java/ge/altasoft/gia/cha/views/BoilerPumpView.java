package ge.altasoft.gia.cha.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import ge.altasoft.gia.cha.LogBooleanActivity;
import ge.altasoft.gia.cha.R;

public class BoilerPumpView extends ImageView {

    private boolean isOn;
    private int relayId;

    public BoilerPumpView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeViews();
    }

    public BoilerPumpView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews();
    }

    public BoilerPumpView(Context context) {
        super(context);
        initializeViews();
    }

    private void initializeViews() {
        isOn = false;
        setBackgroundResource(R.drawable.pump_off);

        this.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick (View v){
                Intent intent = new Intent(getContext(), LogBooleanActivity.class);
                intent.putExtra("id", relayId);
                intent.putExtra("scope", "BoilerPump");
                getContext().startActivity(intent);
                return true;
            }
        });
    }


    public void setRelayId(int relayId) {
        this.relayId = relayId;
    }

    public void setIsOn(boolean value) {
        if (isOn == value)
            return;

        isOn = value;
        if (value) {
            setBackgroundResource(R.drawable.pump_animation);
            final AnimationDrawable frameAnimation = (AnimationDrawable) getBackground();
            post(new Runnable() {
                public void run() {
                    frameAnimation.start();
                }
            });
        } else
            setBackgroundResource(R.drawable.pump_off);
    }
}