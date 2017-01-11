package ge.altasoft.gia.cha.views;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.TextUtils;
import android.util.AttributeSet;

import java.util.Locale;

public final class FriendlyEditTextPreference extends EditTextPreference {

    public FriendlyEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public FriendlyEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FriendlyEditTextPreference(Context context) {
        super(context);
    }

    // According to ListPreference implementation
    @Override
    public CharSequence getSummary() {
        String text = getText();
        if (TextUtils.isEmpty(text)) {
            return getEditText().getHint();
        } else {
            CharSequence summary = super.getSummary();
            if (summary != null) {
                return String.format(Locale.US, summary.toString(), text);
            } else {
                return null;
            }
        }
    }
}