package ge.altasoft.gia.cha.views;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Locale;

import ge.altasoft.gia.cha.LogBooleanActivity;
import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.thermostat.ThermostatUtils;
import ge.altasoft.gia.cha.thermostat.ThermostatRelayData;

public class ThermostatRelayView extends LinearLayout {

    private TextView tvRelayName;
    private TextView tvComment;
    private ToggleButton tbButton;

    private ThermostatRelayData relayData;

    public ThermostatRelayView(Context context) {
        super(context);
        initializeViews(context);
    }

    public ThermostatRelayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public ThermostatRelayView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.thermostat_relay_layout, this);

        getOnOffButton().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                if (!Utils.disableOnCheckedListener) {
                    ((ToggleButton) button).setTextOn("");
                    ((ToggleButton) button).setTextOff("");
                    button.setEnabled(false);

                    ThermostatUtils.sendCommandToController(getContext(), String.format(Locale.US, "#%01X%s", relayData.getId(), isChecked ? "1" : "0"));
                }
            }
        });

        this.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (relayData != null) {
                    Intent intent = new Intent(getContext(), LogBooleanActivity.class);
                    intent.putExtra("id", relayData.getId());
                    intent.putExtra("scope", "ThermostatRelay");
                    getContext().startActivity(intent);
                }
                return true;
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

    private void setRelayName(CharSequence value) {
        getRelayNameTextView().setText(value);
    }

    private void setComment(CharSequence value) {
        getCommentTextView().setText(value);
    }

    public void setIsOn(boolean value) {
        getOnOffButton();

        Utils.disableOnCheckedListener = true;
        try {
            tbButton.setTextOn(getResources().getString(R.string.on));
            tbButton.setTextOff(getResources().getString(R.string.off));
            tbButton.setChecked(value);
            tbButton.setEnabled(true);
        } finally {
            Utils.disableOnCheckedListener = false;
        }
    }

    public ThermostatRelayData getRelayData() {
        return this.relayData;
    }

    public void setRelayData(ThermostatRelayData value) {
        this.relayData = value;

        setRelayName(value.getName() + ", order=" + String.valueOf(value.getOrder()));
        setComment(value.getComment());
        setIsOn(value.isOn());
    }
}