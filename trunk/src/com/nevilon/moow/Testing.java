package com.nevilon.moow;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

public class Testing extends Activity
{   
     public static final int DIRECTION_RIGHT = 0, DIRECTION_LEFT = 1;
     
     private Panel main;
     private Bitmap scratch;
     private Canvas offscreen;
     
     public boolean start = true;
     private volatile boolean running = true;
     private int direction = DIRECTION_RIGHT;
     
     private int box = 10;
     
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);  
       
        setOffscreenBitmap();
       
        main = new Panel(this);        
        setContentView(main,new ViewGroup.LayoutParams(320,480));
       
    }
   
    private void setOffscreenBitmap()
    {
      scratch = Bitmap.createBitmap(30,30,Bitmap.Config.ARGB_8888);
      offscreen = new Canvas();
      offscreen.setBitmap(scratch);
      offscreen.drawColor(Color.RED);
    }
   
    
    private synchronized void doDraw(Canvas canvas, Paint paint)
    {
     if(start)
          {
               canvas.drawColor(Color.BLACK);
               Bitmap btp = BitmapFactory.decodeResource(getResources(), R.drawable.mt);
               canvas.drawBitmap(btp,0,0,paint);
               start = false;
          }
          else
          {              
          canvas.save();
          canvas.clipRect(box,8,box+32,40);            
          canvas.drawColor(Color.RED);
          // canvas.drawBitmap(scratch,box,10,paint);
          canvas.restore();             
          }
    }
   
   
    class Panel extends View
    {
     Paint paint;
     
     public Panel(Context context)
     {
          super(context);
          paint = new Paint();
     }
     
     @Override
     protected void onDraw(Canvas canvas)
     {
          doDraw(canvas,paint);
     }
    }
   
    
}