package com.nevilon.moow;

import android.util.AttributeSet;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Paint.Style;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ZoomControls;

import com.nevilon.moow.core.PhysicMap;

public class DrawTesting extends Activity {
	public static final int DIRECTION_RIGHT = 0, DIRECTION_LEFT = 1;

	private Panel main;

	private volatile boolean running = true;

	private boolean inMove = false;

	private int nL = -1;
	private int nR = -1;
	private int nB = -1;
	private int nT = -1;

	private Point previousMovePoint = new Point();
	private Point nextMovePoint = new Point();
	private Point globalOffset = new Point();

	private long lastMove = -1;
	
	PhysicMap pmap = new PhysicMap();

	boolean moving = false;

	Bitmap[][] tiles = new Bitmap[4][4];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		main = new Panel(this);
		
		setContentView(main, new ViewGroup.LayoutParams(320, 480));
		(new Thread(new AnimationLoop())).start();
		
		class TransparentPanel extends RelativeLayout { 
		    
			
			public TransparentPanel(Context context) {
				super(context);
				ZoomControls zc = new ZoomControls(getContext());
				addView(zc);
				setPadding(80, 368, 0, 0);
			}

		}
		
		TransparentPanel ts = new TransparentPanel(DrawTesting.this);
	    addContentView(ts, new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
		//ts.setPadding(80, 368, 0, 0);
		
	}

	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			
		
			nextMovePoint.set((int) event.getX(), (int) event.getY());
			break;
		case MotionEvent.ACTION_MOVE:
			moveCoordinates(event.getX(), event.getY());
			//System.out.println("move");
			lastMove = System.currentTimeMillis();
			break;
		case MotionEvent.ACTION_UP:
			//inMove = false;
			if(lastMove!=-1 && System.currentTimeMillis()-lastMove>1000){
			} else {
			//	(new Thread(new InertionMoover())).start();
			}
			lastMove = -1;
			break;
		}

		return super.onTouchEvent(event);
	}

	private void moveCoordinates(float x, float y) {
		inMove = true;
		previousMovePoint.set(nextMovePoint.x, nextMovePoint.y);
		nextMovePoint.set((int) x, (int) y);
		globalOffset.set(globalOffset.x
				+ (nextMovePoint.x - previousMovePoint.x), globalOffset.y
				+ (nextMovePoint.y - previousMovePoint.y));
	}

	private synchronized void doDraw(Canvas canvas, Paint paint) {
		boolean movedR = false;
		boolean movedL = false;
		boolean movedB = false;
		boolean movedT = false;

		// обработка перемещения вправо
		if (isMovedRight()) {
			if (previousMovePoint.x < nextMovePoint.x) {
				globalOffset.x -= 256;
				pmap.moveLeft();
				movedR = true;
			}
		}
		if (!movedR) {
			nR = (int) Math.ceil((globalOffset.x - 256) / 256);
		} else {
			nR = 0;
		}

		//обработка перемещения влево
		if (isMovedLeft()) {
			if (previousMovePoint.x > nextMovePoint.x) {
				globalOffset.x += (256);
				pmap.moveRight();
				movedL = true;
			}
		}
		if (!movedL) {
			nL = (int) Math.ceil((globalOffset.x - 256+320) / 256);
			//System.out.println(nL);
		} else {
			nL = 0;
		}
        

		// обработка перемещения вниз
		if (isMovedBottom()) {
			if (previousMovePoint.y < nextMovePoint.y) {
				globalOffset.y -= (256);
				pmap.moveBottom();
				movedB = true;
			}

		}
		if (!movedB) {
			nB = (int) Math.ceil((globalOffset.y - (256)) / 256);
		} else {
			nB = 0;
		}
		
		// обработка перемещения вверх
		if (isMovedTop()) {
			if (previousMovePoint.y > nextMovePoint.y) {
				globalOffset.y += (256);
				pmap.moveTop();
				movedT = true;
			}

		}
		if (!movedT) {
			nT = (int) Math.ceil((globalOffset.y + 256+480) / 256);
		} else {
			nT = 0;
		}

		Bitmap tmpBitmap;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				tmpBitmap = pmap.getCells()[i][j];
				if (tmpBitmap != null) {
					canvas.drawBitmap(tmpBitmap, (i) * 256 + globalOffset.x,
							(j) * 256 + globalOffset.y, paint);
				}
			}
		}

	}

	// проверка, передвинуто ли вправо
	private boolean isMovedRight() {
		return inMove && nR != -1
				&& Math.abs(Math.ceil((globalOffset.x + 256) / 256) - nR) == 1;
	}

	// проверка, передвинуто ли влево
	private boolean isMovedLeft() {
		return 
			 Math.abs(Math.ceil((globalOffset.x -256 + 320) / 256) - nL) == 1;

	}

	// проверка, передвино ли вниз
	private boolean isMovedBottom() {
		return inMove && nB != -1
				&& Math.abs(Math.ceil((globalOffset.y + 256) / 256) - nB) == 1;
	}
	
	// проверка, передвино ли вниз
	private boolean isMovedTop() {
		return inMove && nT != -1
				&& Math.abs(Math.ceil((globalOffset.y - 256+480) / 256) - nT) == 1;
	}

	class Panel extends View {
		Paint paint;

		public Panel(Context context) {
			super(context);
			paint = new Paint();
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			doDraw(canvas, paint);
		}
	}

	class InertionMoover implements Runnable{
		
		private int counter = 400;
		
		public void run(){
			
			int x = nextMovePoint.x +1;
			int x1 = previousMovePoint.x;
			int x2 = nextMovePoint.x;
			int y1 = previousMovePoint.y;
			int y2 = nextMovePoint.y;
			
			//if(x1 == 0){
			//	x1 = x2-2;
			//}
			
			System.out.println(previousMovePoint.x + "^" + previousMovePoint.y);
			System.out.println(nextMovePoint.x + "*" + nextMovePoint.y);
			while(counter>0){
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				int y = ((x-x1)*(y2-y1)/(x2-x1))+y1;
				
				moveCoordinates(x,y);
				x-=x2>x1?-1:1;
				counter-=1;
			}
		}
	}
	
	class AnimationLoop implements Runnable {
		
	//	private int counter = 400;
			
		
		
		public void run() {
			//while (true) {
				while (running) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException ex) {
					}
					processInertion();
					main.postInvalidate();
				}
	//		}
		}
		
		private void processInertion(){
			
		}
	}
}