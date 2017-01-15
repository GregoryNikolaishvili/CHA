package ge.altasoft.gia.cha.views;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;

import java.util.Locale;

public class TemperaturePreference extends FriendlyEditTextPreference {

    private Float mTemperature;

    public TemperaturePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public TemperaturePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TemperaturePreference(Context context) {
        super(context);
        init();
    }

    private void init() {
        getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }

    @Override
    public void setText(String text) {
        final boolean wasBlocking = shouldDisableDependents();
        mTemperature = parseDecimal(text);
        persistString(mTemperature != null ? mTemperature.toString() : null);
        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) notifyDependencyChange(isBlocking);
    }

    @Override
    public String getText() {
        return mTemperature != null ? String.format(Locale.US, "%.1f", mTemperature) : null;
    }

    private static Float parseDecimal(String text) {
        try {
            return Float.parseFloat(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}