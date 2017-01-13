package ge.altasoft.gia.cha.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Locale;

import ge.altasoft.gia.cha.light.LightRelayData;
import ge.altasoft.gia.cha.light.LightUtils;
import ge.altasoft.gia.cha.R;

public class LightRelayView extends LinearLayout {

    private TextView tvRelayName;
    private TextView tvComment;
    private ToggleButton tbButton;

    private Boolean disableOnCheckedListener = false;

    LightRelayData relayData;

    public LightRelayView(Context context) {
        super(context);
        initializeViews(context);
    }

    public LightRelayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public LightRelayView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.light_relay_layout, this);

        getOnOffButton();

        tbButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                if (!disableOnCheckedListener) {
                    ((ToggleButton) button).setTextOn("");
                    ((ToggleButton) button).setTextOff("");
                    button.setEnabled(false);

                    LightUtils.sendCommandToController(String.format(Locale.US, "#%01X%s", relayData.getId(), isChecked ? "1" : "0"));
                }
            }
        });
    }


    private TextView getRelayNameTextView() {
        if (tvRelayName == null)
            tvRelayName = (TextView) this.findViewById(R.id.relay_name);
        return tvRelayName;
    }

    private TextView getCommentTextView() {
        if (tvComment == null)
            tvComment = (TextView) this.findViewById(R.id.comment);
        return tvComment;
    }

    private ToggleButton getOnOffButton() {
        if (tbButton == null)
            tbButton = (ToggleButton) this.findViewById(R.id.onOffButton);
        return tbButton;
    }

    public void setRelayName(CharSequence value) {
        getRelayNameTextView().setText(value);
    }

    public void setComment(CharSequence value) {
        getCommentTextView().setText(value);
    }

    public void setIsOn(boolean value) {
        getOnOffButton();

        disableOnCheckedListener = true;
        try {
            tbButton.setTextOn(getResources().getString(R.string.on));
            tbButton.setTextOff(getResources().getString(R.string.off));
            tbButton.setChecked(value);
            tbButton.setEnabled(true);
        } finally {
            disableOnCheckedListener = false;
        }
    }

    public LightRelayData getRelayData() {
        return this.relayData;
    }

    public void setRelayData(LightRelayData value) {
        this.relayData = value;

        setRelayName(value.getName()); // + ", order=" + String.valueOf(value.getOrder()));
        setComment(value.getComment());
        setIsOn(value.isOn());
    }
}