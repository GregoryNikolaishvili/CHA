package ge.altasoft.gia.cha.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;

import java.util.Locale;

import ge.altasoft.gia.cha.views.TimePicker;

// Based on http://stackoverflow.com/a/7484289/922168

public final class TimePreference extends DialogPreference {
    private short mTimeValueInMin = 0;

    private TimePicker picker = null;

    public TimePreference(Context context) {
        this(context, null);
    }

    public TimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPositiveButtonText("Set");
        setNegativeButtonText("Cancel");
    }

    public void setTime(short value) {
        mTimeValueInMin = value;

        persistInt(value);
        notifyDependencyChange(shouldDisableDependents());
        notifyChanged();
    }

    public short getValue() {
        return mTimeValueInMin;
    }

    public String getDisplayTime() {
        return getDisplayTime(mTimeValueInMin);
    }

    public String getDisplayTime(short value) {

        String sign = "";
        if (value < 0) {
            sign = "-";
            value = (short) -value;
        }

        return String.format(Locale.US, "%s %02d:%02d", sign, value / 60, value % 60);
    }

    public void updateSummary() {
        setSummary(getDisplayTime());
    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext(), null);

        //picker = new TimePicker(new ContextThemeWrapper(getContext(), android.R.style.Theme_Holo_Light_Dialog_NoActionBar));
        return picker;
        //return p;
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        picker.setTimeInMinutes(mTimeValueInMin);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            short time = picker.getTimeInMinutes();

            if (!callChangeListener(time)) {
                return;
            }

            // persist
            setTime(time);
            updateSummary();
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        short time;

        if (restorePersistedValue) {
            try {
                time = (short) getPersistedInt(0);
            } catch (Exception ex) {
                time = 0;
            }
        } else
            time = Short.parseShort(defaultValue.toString(), 0);


        // need to persist here for default value to work
        setTime(time);
        updateSummary();
    }

}