package ge.altasoft.gia.cha.views;

import android.content.Context;
import android.support.annotation.Keep;
import android.text.InputType;
import android.util.AttributeSet;

class IntegerPreference extends FriendlyEditTextPreference {

    private Integer mInteger;

    @Keep
    public IntegerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Keep
    public IntegerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Keep
    public IntegerPreference(Context context) {
        super(context);
        init();
    }

    @Keep
    private void init() {
        getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
    }

    @Override
    public void setText(String text) {
        final boolean wasBlocking = shouldDisableDependents();
        mInteger = parseInteger(text);
        persistString(mInteger != null ? mInteger.toString() : null);
        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) notifyDependencyChange(isBlocking);
    }

    @Override
    public String getText() {
        return mInteger != null ? mInteger.toString() : null;
    }

    private static Integer parseInteger(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}