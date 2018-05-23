package in.bhumiputra.nakshatra;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by damodarreddy on 1/13/18.
 */

public class Guesture implements View.OnTouchListener {

    private final String tag= "saiga";
    private final Context context;
    private final GestureDetector gd;

    public Guesture(Context context) {
        this.context= context;
        this.gd= new GestureDetector(context, new SwypeListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.requestFocus();
        boolean retn= gd.onTouchEvent(event);
        return retn;
    }

    private final class SwypeListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return onDownGesture();
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //Log.i(tag, "onFling() called.");
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                //Log.i(tag, "diffX: "+ diffX+ "; diffy: "+ diffY+ "\nvelX: "+velocityX+ "; velY: "+ velocityY);
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            //Log.i(tag, "onSwipeRight() to be called.");
                            result= onSwipeRight();
                        } else {
                            //Log.i(tag, "onSwipeLeft() to be called.");
                            result= onSwipeLeft();
                        }
                    }
                }
                else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        result= onSwipeBottom();
                    } else {
                        result= onSwipeTop();
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    public boolean onDownGesture() {
        return false;
    }

    public boolean onSwipeTop() {
        return false;
    }

    public boolean onSwipeRight() {
        return false;
    }

    public boolean onSwipeBottom() {
        return false;
    }

    public boolean onSwipeLeft() {
        return false;
    }

}
