package ge.altasoft.gia.cha.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;

import ge.altasoft.gia.cha.LogStateActivity;
import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.classes.WidgetType;

public abstract class PumpView extends android.support.v7.widget.AppCompatImageView {

    private int state;
    private int relayId;

    public PumpView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeViews();
    }

    public PumpView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews();
    }

    public PumpView(Context context) {
        super(context);
        initializeViews();
    }


    private void initializeViews() {
        state = 0;
        drawState(0);

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                PopupMenu popupMenu = new PopupMenu(getContext(), v);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Intent intent = new Intent(getContext(), LogStateActivity.class);
                        intent.putExtra("id", relayId);
                        intent.putExtra("widget", WidgetType.BoilerPump);
                        getContext().startActivity(intent);

                        return false;
                    }
                });
                popupMenu.inflate(R.menu.pump_popup_menu);
                popupMenu.show();

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
        drawState(value);
    }

    protected abstract void drawState(int value);
}