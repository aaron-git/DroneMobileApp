package yeow.tk.devicegravity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

/**
 * Draws an JoyStick and get's the JoyStickPositions
 * Class of the JoyStick
 * @author Ruslan, Atai, Ivan, Jerome, Aaron
 *  */

public class JoyStick {
    public static final int STICK_NONE = 0;
    public static final int STICK_UP = 1;
    public static final int STICK_UPRIGHT = 2;
    public static final int STICK_RIGHT = 3;
    public static final int STICK_DOWNRIGHT = 4;
    public static final int STICK_DOWN = 5;
    public static final int STICK_DOWNLEFT = 6;
    public static final int STICK_LEFT = 7;
    public static final int STICK_UPLEFT = 8;

    private int STICK_ALPHA = 200;
    private int LAYOUT_ALPHA = 200;
    private int OFFSET = 0;

    private Context mContext;
    private ViewGroup mLayout;
    private LayoutParams params;
    private int stick_width, stick_height;

    private int position_x = 0, position_y = 0, min_distance = 0;
    private float distance = 0, angle = 0;

    private DrawCanvas draw;
    private Paint paint;
    private Bitmap stick;

    private boolean touch_state = false;

    /**
     * Constructor of the JoyStick
     * @param context
     * @param layout
     * @param stick_res_id
     */
    public JoyStick(Context context, ViewGroup layout, int stick_res_id) {
        mContext = context;

        stick = BitmapFactory.decodeResource(mContext.getResources(),
                stick_res_id);

        stick_width = stick.getWidth();
        stick_height = stick.getHeight();
        draw = new DrawCanvas(mContext);
        paint = new Paint();
        mLayout = layout;
        params = mLayout.getLayoutParams();
    }

    /**
     * Draws the sick of the layout
     * @param arg1
     */
    public void drawStick(MotionEvent arg1) {
        position_x = (int) (arg1.getX() - (params.width / 2));
        position_y = (int) (arg1.getY() - (params.height / 2));
        distance = (float) Math.sqrt(Math.pow(position_x, 2) + Math.pow(position_y, 2));
        angle = (float) cal_angle(position_x, position_y);


        if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
            if (distance <= (params.width / 2) - OFFSET) {
                draw.position(arg1.getX(), arg1.getY());
                draw();
                touch_state = true;
            }
        } else if (arg1.getAction() == MotionEvent.ACTION_MOVE && touch_state) {
            if (distance <= (params.width / 2) - OFFSET) {
                draw.position(arg1.getX(), arg1.getY());
                draw();
            } else if (distance > (params.width / 2) - OFFSET) {
                float x = (float) (Math.cos(Math.toRadians(cal_angle(position_x, position_y))) * ((params.width / 2) - OFFSET));
                float y = (float) (Math.sin(Math.toRadians(cal_angle(position_x, position_y))) * ((params.height / 2) - OFFSET));
                x += (params.width / 2);
                y += (params.height / 2);
                draw.position(x, y);
                draw();
            } else {
                mLayout.removeView(draw);
            }
        } else if (arg1.getAction() == MotionEvent.ACTION_UP) {
            mLayout.removeView(draw);
            touch_state = false;
        }
    }

    /**
     * Get the position value form the stick
     * @return position
     * */
    public int[] getPosition() {
        if (distance > min_distance && touch_state) {
            return new int[]{position_x, position_y};
        }
        return new int[]{0, 0};
    }

    /**
     * Get the X value form the stick
     * @return position_x
     * */
    public int getX() {
        if (distance > min_distance && touch_state) {
            return position_x;
        }
        return 0;
    }

    /**
     * Get the Y value form the stick
     * @return position_y
     * */
    public int getY() {
        if (distance > min_distance && touch_state) {
            return position_y;
        }
        return 0;
    }

    /**
     * Get the angle value form the stick
     * @return angle
     * */
    public float getAngle() {
        if (distance > min_distance && touch_state) {
            return angle;
        }
        return 0;
    }

    /**
     * Get the distance value form the stick
     * @return distance
     * */
    public float getDistance() {
        if (distance > min_distance && touch_state) {
            return distance;
        }
        return 0;
    }

    /**
     * Set the minDistance value form the stick
     * @pram minDistance
     * */
    public void setMinimumDistance(int minDistance) {
        min_distance = minDistance;
    }

    /**
     * Get the minDistance value form the stick
     * @return minDistance
     * */
    public int getMinimumDistance() {
        return min_distance;
    }
/*
    public int get8Direction() {
        if (distance > min_distance && touch_state) {
            if (angle >= 247.5 && angle < 292.5) {
                return STICK_UP;
            } else if (angle >= 292.5 && angle < 337.5) {
                return STICK_UPRIGHT;
            } else if (angle >= 337.5 || angle < 22.5) {
                return STICK_RIGHT;
            } else if (angle >= 22.5 && angle < 67.5) {
                return STICK_DOWNRIGHT;
            } else if (angle >= 67.5 && angle < 112.5) {
                return STICK_DOWN;
            } else if (angle >= 112.5 && angle < 157.5) {
                return STICK_DOWNLEFT;
            } else if (angle >= 157.5 && angle < 202.5) {
                return STICK_LEFT;
            } else if (angle >= 202.5 && angle < 247.5) {
                return STICK_UPLEFT;
            }
        } else if (distance <= min_distance && touch_state) {
            return STICK_NONE;
        }
        return 0;
    }
*/
    /**
     * Sets the fly- command from the Position of the
     * JoyStick
     * @return
     * */
    public int get4Direction() {
        if (distance > min_distance && touch_state) {
            if (angle >= 225 && angle < 315) {
                return STICK_UP;
            } else if (angle >= 315 || angle < 45) {
                return STICK_RIGHT;
            } else if (angle >= 45 && angle < 135) {
                return STICK_DOWN;
            } else if (angle >= 135 && angle < 225) {
                return STICK_LEFT;
            }
        } else if (distance <= min_distance && touch_state) {
            return STICK_NONE;
        }
        return 0;
    }

    /**
     * set the Offset
     * @param offset
     */
    public void setOffset(int offset) {
        OFFSET = offset;
    }

    /**
     * get the Offset
     * @return
     */
    public int getOffset() {
        return OFFSET;
    }

    /**
     * set the Alpha- value of the JoyStick
     * @param alpha
     */
    public void setStickAlpha(int alpha) {
        STICK_ALPHA = alpha;
        paint.setAlpha(alpha);
    }

    /**
     * get the Alpha- value of the JoyStick
     * @return
     */
    public int getStickAlpha() {
        return STICK_ALPHA;
    }

    /**
     * Set the Alphavalue of the JoySticklayout
     * @param alpha
     */
    public void setLayoutAlpha(int alpha) {
        LAYOUT_ALPHA = alpha;
        //mLayout.getBackground().setAlpha(alpha);
    }

    /**
     * get the Alphavalue of the JoySticklayout
     * @return
     */
    public int getLayoutAlpha() {
        return LAYOUT_ALPHA;
    }

    /**
     * set the heigth and the width of the JoyStick
     * @param width
     * @param height
     */
    public void setStickSize(int width, int height) {
        stick = Bitmap.createScaledBitmap(stick, width, height, false);
        stick_width = stick.getWidth();
        stick_height = stick.getHeight();
    }

    /**
     * set the width of the JoyStick
     * @param width
     */
    public void setStickWidth(int width) {
        stick = Bitmap.createScaledBitmap(stick, width, stick_height, false);
        stick_width = stick.getWidth();
    }

    /**
     * set the heigth of the JoyStick
     * @param height
     */
    public void setStickHeight(int height) {
        stick = Bitmap.createScaledBitmap(stick, stick_width, height, false);
        stick_height = stick.getHeight();
    }

    /**
     * get' s the width of the JoyStick
     * @return
     */
    public int getStickWidth() {
        return stick_width;
    }

    /**
     * get's the heigth of the JoyStick
     * @return
     */
    public int getStickHeight() {
        return stick_height;
    }

    /**
     * set's the heigth and the width of the Layout
     * @param width
     * @param height
     */
    public void setLayoutSize(int width, int height) {
        params.width = width;
        params.height = height;
    }

    /**
     * get's the width of the Layout
     * @return
     */
    public int getLayoutWidth() {
        return params.width;
    }

    /**
     * geht's the heigth of the Layout
     * @return
     */
    public int getLayoutHeight() {
        return params.height;
    }

    /**
     * calculate the quadrant of the coordinatesystem in
     * witch the Joystick is
     * @param x
     * @param y
     * @return
     */
    private double cal_angle(float x, float y) {
        if (x >= 0 && y >= 0)
            return Math.toDegrees(Math.atan(y / x));
        else if (x < 0 && y >= 0)
            return Math.toDegrees(Math.atan(y / x)) + 180;
        else if (x < 0 && y < 0)
            return Math.toDegrees(Math.atan(y / x)) + 180;
        else if (x >= 0 && y < 0)
            return Math.toDegrees(Math.atan(y / x)) + 360;
        return 0;
    }

    /**
     * update the position of the JoyStick
     */
    private void draw() {
        try {
            mLayout.removeView(draw);
        } catch (Exception e) {
        }
        mLayout.addView(draw);
    }

    /**
     * class canvas on witch the JoyStick get's drawn
     */
    private class DrawCanvas extends View {
        float x, y;

        private DrawCanvas(Context mContext) {
            super(mContext);
        }

        /**
         * draws the Stick on the current position
         * @param canvas
         */
        public void onDraw(Canvas canvas) {
            canvas.drawBitmap(stick, x, y, paint);
        }

        /**
         * set's the current position
         * @param pos_x
         * @param pos_y
         */
        private void position(float pos_x, float pos_y) {
            x = pos_x - (stick_width / 2);
            y = pos_y - (stick_height / 2);
        }
    }
}
