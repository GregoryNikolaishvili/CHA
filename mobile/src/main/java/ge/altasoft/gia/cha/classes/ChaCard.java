package ge.altasoft.gia.cha.classes;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class ChaCard extends LinearLayout {
    private boolean dragMode = false;
    private boolean fromDashboard = false;

    public ChaCard(Context context, boolean fromDashboard) {
        this(context, null);
        this.fromDashboard = fromDashboard;
    }

    public ChaCard(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChaCard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setDragMode(boolean dragMode) {
        this.dragMode = dragMode;
    }

    protected boolean getDragMode() {
        return this.dragMode;
    }

    protected boolean getIsFromDashboard() {
        return this.fromDashboard;
    }

}
