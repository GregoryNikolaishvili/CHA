package ge.altasoft.gia.cha.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import ge.altasoft.gia.cha.LogStateActivity;
import ge.altasoft.gia.cha.R;

public class BoilerPumpView extends ImageView {

    private int state;
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
        state = 0;
        setBackgroundResource(R.drawable.pump_off);

        this.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(getContext(), LogStateActivity.class);
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

    public void setState(int value) {
        if (this.state == value)
            return;

        state = value;
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