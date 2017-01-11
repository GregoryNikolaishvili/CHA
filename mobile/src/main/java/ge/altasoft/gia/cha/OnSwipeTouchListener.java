package ge.altasoft.gia.cha;

/* Detects left and right swipes across a view. */

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class OnSwipeTouchListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;

    private int swipeMinDistance;
    private int swipeMinVelocity;
    private int swipeMaxVelocity;
    //private int swipeMaxOffPath;

    public OnSwipeTouchListener(Context context) {

        final ViewConfiguration vc = ViewConfiguration.get(context);

        swipeMinDistance = vc.getScaledPagingTouchSlop();
        swipeMinVelocity = vc.getScaledMinimumFlingVelocity();
        swipeMaxVelocity = vc.getScaledMaximumFlingVelocity();
        //swipeMaxOffPath = vc.getScaledTouchSlop();

        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public void onSwipeLeft() {
    }

    public void onSwipeRight() {
    }

    public void onSwipeDown() {
    }

    public void onSwipeUp() {
    }

    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    public boolean onTouch(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float distanceX = e2.getX() - e1.getX();
            float distanceY = e2.getY() - e1.getY();

            if ((Math.abs(distanceX) >= Math.abs(distanceY)) && (Math.abs(distanceX) >= swipeMinDistance) && //(Math.abs(distanceY) <= swipeMaxOffPath) &&
                    (Math.abs(velocityX) >= swipeMinVelocity) && (Math.abs(velocityX) <= swipeMaxVelocity)) {
                if (distanceX > 0)
                    onSwipeRight();
                else
                    onSwipeLeft();
                return true;
            }

            if ((Math.abs(distanceY) >= Math.abs(distanceX)) && (Math.abs(distanceY) >= swipeMinDistance) && //(Math.abs(distanceX) <= swipeMaxOffPath) &&
                    (Math.abs(velocityY) >= swipeMinVelocity) && (Math.abs(velocityY) <= swipeMaxVelocity)) {
                if (distanceY > 0)
                    onSwipeDown();
                else
                    onSwipeUp();
                return true;
            }

            return false;
        }
    }
}