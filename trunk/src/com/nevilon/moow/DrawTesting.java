package com.nevilon.moow;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nevilon.moow.core.PhysicMap;

public class DrawTesting extends Activity {
	public static final int DIRECTION_RIGHT = 0, DIRECTION_LEFT = 1;

	private Panel main;

	private volatile boolean running = true;

	private boolean inMove = false;

	private int nL = -1;
	private int nR = -1;
	private int nB = -1;

	private Point previousMovePoint = new Point();
	private Point nextMovePoint = new Point();
	private Point globalOffset = new Point();

	PhysicMap pmap = new PhysicMap();

	boolean moving = false;

	Bitmap[][] tiles = new Bitmap[4][4];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		main = new Panel(this);
		setContentView(main, new ViewGroup.LayoutParams(320, 480));
		(new Thread(new AnimationLoop())).start();
	}

	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			nextMovePoint.set((int) event.getX(), (int) event.getY());
			inMove = true;
			break;
		case MotionEvent.ACTION_MOVE:
			previousMovePoint.set(nextMovePoint.x, nextMovePoint.y);
			nextMovePoint.set((int) event.getX(), (int) event.getY());
			globalOffset.set(globalOffset.x
					+ (nextMovePoint.x - previousMovePoint.x), globalOffset.y
					+ (nextMovePoint.y - previousMovePoint.y));
			break;
		case MotionEvent.ACTION_UP:
			inMove = false;
			break;
		}

		return super.onTouchEvent(event);
	}

	private synchronized void doDraw(Canvas canvas, Paint paint) {
		boolean movedR = false;
		boolean movedL = false;
		boolean movedB = false;

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
		//return false;
		//System.out.println("e " + Math.abs(Math.ceil((globalOffset.x + 320) / 256) - nL));
		return 
			 Math.abs(Math.ceil((globalOffset.x -256 + 320) / 256) - nL) == 1;

	}

	// проверка, передвино ли вниз
	private boolean isMovedBottom() {
		return inMove && nB != -1
				&& Math.abs(Math.ceil((globalOffset.y + 256) / 256) - nB) == 1;
	}

	class Panel extends View {
		Paint paint;

		public Panel(Context context) {
			super(context);
			paint = new Paint();
			TextView txt = new TextView(getContext());
			txt.setText("xxxxxxxx");
			addContentView(txt, new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT));
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			doDraw(canvas, paint);
		}
	}

	class AnimationLoop implements Runnable {
		public void run() {
			while (true) {
				while (running) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException ex) {
					}
					main.postInvalidate();
				}
			}
		}
	}
}