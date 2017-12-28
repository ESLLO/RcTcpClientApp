package com.esllo.rccar.rccar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by Esllo on 2017-12-22.
 */

public class DrawSheet extends View{
    private Point size;
    private boolean isRun = false, isWheel, isBrake, isAccel, isWheelStart = false;
    private Bitmap wheel, brake, backpane, parking[] = new Bitmap[2];
    private int px,py,pw,ph, park = 0;
    private int wx, wy, bx, by, rotate = 0, lastRotate = 0, wcx, wcy;
    private int wheelIndex, brakeIndex, accelIndex;
    private int wheelSize, brakeWidth, brakeHeight;
    private long lastSend = System.currentTimeMillis();
    private float[][] points = new float[10][2];
    private NetworkClient client;
    public DrawSheet(Context context, Point size, String ip) {
        super(context);
        client = new NetworkClient(ip);
        this.size = size;
        wheel = BitmapFactory.decodeResource(getResources(), R.drawable.wheel);
        brake = BitmapFactory.decodeResource(getResources(), R.drawable.brake);
        backpane = BitmapFactory.decodeResource(getResources(), R.drawable.backpane);//.copy(Bitmap.Config.ARGB_8888, true);
        parking[0] = BitmapFactory.decodeResource(getResources(), R.drawable.parking2);
        parking[1] = BitmapFactory.decodeResource(getResources(), R.drawable.parking);
        wheelSize = (int)(size.y * 0.5d);
        brakeWidth = (int)(size.y * 0.35d / 7d * 9d);
        brakeHeight = (int)(size.y * 0.35d);
        wheel = Bitmap.createScaledBitmap(wheel, wheelSize, wheelSize, true);
        brake = Bitmap.createScaledBitmap(brake, brakeWidth, brakeHeight, true);
        backpane = Bitmap.createScaledBitmap(backpane, size.x, size.y , true);
        wx = (int)(size.x * 0.05);
        wy = size.y - wx - wheel.getHeight();
        bx = size.x - wx/2 - brake.getWidth();
        by = size.y - wx/2 - brake.getHeight();
        wcx = wx + wheelSize/2;
        wcy = wy + wheelSize/2;
        pw = (int)(size.x / 8d);
        ph = (int)(pw / 9d * 7d);
        px = (size.x-pw)/2;
        py = (size.y-ph-20);
        parking[0] = Bitmap.createScaledBitmap(parking[0], pw, ph, true);
        parking[1] = Bitmap.createScaledBitmap(parking[1], pw, ph, true);
    }

    public DrawSheet(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawSheet(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean aobChanged = false;
        switch(event.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                for(int i = 0 ; i < event.getPointerCount(); i++){
                    points[i] = new float[]{event.getX(i), event.getY(i)};
                    if(isBounds(points[i], 0)){
                        wheelIndex = i;
                        isWheel = isWheelStart = true;
                    }
                    if(isBounds(points[i], 1)){
                        brakeIndex = i;
                        isBrake = aobChanged = true;
                    }
                    if(isBounds(points[i], 2)){
                        accelIndex = i;
                        isAccel = aobChanged = true;
                    }
                    if(isBounds(points[i], 3)){
                        park = (park+1)%2;
                        aobChanged = true;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                for(int i = 0 ; i < event.getPointerCount(); i++){
                    if(isWheel && wheelIndex == i) {
                        if(isWheelStart){
                            lastRotate = rotate;
                            isWheelStart = false;
                        }
                        int angle = calcAngle(points[i], event.getX(i), event.getY(i));
                        rotate = lastRotate + angle;
                        while(rotate > 360)
                            rotate -= 360;
                        while (rotate <0)
                            rotate += 360;
                        if(isBounds(points[i], 0) && !isBounds(new float[]{event.getX(i), event.getY(i)}, 0)){
                            isWheel = false;
                            aobChanged = true;
                        }
                    }
                    if(isBrake && brakeIndex == i){
                        if(isBounds(points[i], 1) && !isBounds(new float[]{event.getX(i), event.getY(i)}, 1)){
                            isBrake = false;
                            aobChanged = true;
                            brakeIndex = -1;
                        }
                    }
                    if(isAccel && accelIndex == i){
                        if(isBounds(points[i], 2) && !isBounds(new float[]{event.getX(i), event.getY(i)}, 2)){
                            isAccel = false;
                            aobChanged = true;
                            accelIndex = -1;
                        }
                    }
                    if(!isWheel || (i!=wheelIndex) && (i!=accelIndex || i!=brakeIndex))
                        points[i] = new float[]{event.getX(i), event.getY(i)};
                    if(i != accelIndex && isBounds(points[i], 2)){
                        isAccel =aobChanged =true;
                        accelIndex = i;
                    }
                    if(i != brakeIndex && isBounds(points[i] , 1)){
                        isBrake = aobChanged = true;
                        brakeIndex = i;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                for(int i = 0 ; i < event.getPointerCount(); i++){
                    if(isWheel && wheelIndex == i){
                        isWheel = false;
                        aobChanged = true;
                    }
                    if(isBrake && brakeIndex == i){
                        isBrake = false;
                        aobChanged = true;
                        brakeIndex = -1;
                    }
                    if(isAccel && accelIndex == i){
                        isAccel = false;
                        aobChanged = true;
                        accelIndex = -1;
                    }
                }
                break;
        }
        if(rotate > 180)
            rotate -= 360;
        if(rotate > 90)
            rotate = 90;
        else if(rotate  < -90)
            rotate = -90;

        if(aobChanged || (rotate % 2 == 0 && System.currentTimeMillis() - lastSend > 59)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    client.writeBytes(client.convert((rotate+90)/2, isAccel ? 1 : 0, (isBrake ? 1 : 0)+2*park, 0, 0));
                }
            }).start();
            lastSend = System.currentTimeMillis();
        }
        invalidate();
        return true;
    }

    public int calcAngle(float[] sp, float x, float y) {
        double a = Math.atan2(sp[0] - wcx, sp[1] - wcy);
        double b = Math.atan2(x - wcx, y - wcy);
        double diff = a-b;
        int angle = (int)Math.toDegrees(diff);
        if(angle > 179)
            angle -= 360;
        return angle;
    }

    public void stop(){
        client.close();
    }

    public boolean isBounds(float[] arr, int target){
        int x = 0, y = 0, w = 0, h = 0;
        switch(target){
            case 0:
                x = wx; y = wy; w = h = wheelSize;
                break;
            case 1:
                x = bx;  w = (int)(brakeWidth*0.58d); h = (int)(brakeHeight/7d*4d);y = by+(brakeHeight-h);
                break;
            case 2:
                x = bx+(int)(brakeWidth * 0.58d); y = by; w = brakeWidth - (x-bx); h = brakeHeight;
                break;
            case 3:
                x = px; y = py; w = pw; h = ph;
        }
        Rect rect = new Rect();
        rect.set(x, y, x+w, y+h);
        return rect.contains((int)arr[0], (int)arr[1]);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(backpane, 0, 0, null);
        canvas.drawBitmap(brake, bx, by, null);
        canvas.drawBitmap(parking[park], px, py, null);
        canvas.save();
        canvas.rotate(rotate, wcx, wcy);
        canvas.drawBitmap(wheel, wx, wy, null);
        canvas.restore();
    }

}
