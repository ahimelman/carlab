package com.example.carlab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class DrawingView extends View {
	
	Paint p;
	
	private int WINDOW_SIZE_RIGHT = 55;
	private int WINDOW_SIZE_LEFT = 55;
	private double STRAIGHT = 148;
	
	//drawing path
	private Path drawPath;
	//drawing and canvas paint
	private Paint drawPaint, canvasPaint;
	//initial color
	private int paintColor = 0xFF660000;
	//canvas
	private Canvas drawCanvas;
	//canvas bitmap
	private Bitmap canvasBitmap;
	
	private float brushSize, lastBrushSize;
	
	private boolean erase=false;
	
	private float prevX;
	private float prevY;
	
	private float pathL = 0;
	
	private int touchCount = 0;
	
	private ArrayList<dPoint> allPoints;
	
	private ArrayList<dPoint> points;
	
	private ArrayList<cTurn> turns;
	
	private boolean goBool = false;
	
	private double HEIGHT = 1600.0;
	
	float totalAngle;

	
	private class cTurn {
		private double direction;
		private double duration;
		private boolean left;
		
		private cTurn(double direction, double duration, boolean left) {
			this.direction = direction;
			this.duration = duration;
			this.left = left;
		}
		
		public String getDir() {
			return ("" + direction);
		}
		
		public String getDur() {
			return Double.toString(duration);
		}
	}
	
	private class dPoint {
		private float x;
		private float y;
		
		private dPoint(float x, float y) {
			this.x = x;
			this.y = y;
		}
		
		public String gx() {
			return Float.toString(x);
		}
		
		public String gy() {
			return Float.toString(y);
		}
		
		@Override
		public String toString() {
			return x + "," + y;
		}
	}
	
	public DrawingView(Context context, AttributeSet attrs){
	    super(context, attrs);
	    setupDrawing();
	}
	
	public void setErase(boolean isErase){
		//set erase true or false 
		erase=isErase;
		if(erase) {
			drawPaint.setAlpha(0xFF);
			drawPaint.setColor(Color.WHITE);
			drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		}
		else drawPaint.setXfermode(null);
	}
	
	public void startNew(){
//		Log.i("start New", "start New");
		touchCount = 0;
	    drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
	    invalidate();
	}
	
	public void goNew() {
		doCalcs();
		
		new Thread() {
			@Override
			public void run() {
				postData();
			}
		}.start();

	}
	
	public void goStop() {
		
		new Thread() {
			@Override
			public void run() {
				HttpClient httpclient = new DefaultHttpClient();
			    HttpPost httppost = new HttpPost("https://agent.electricimp.com/a4f-wesuNciJ?stop=1");
			    try {
					HttpResponse response = httpclient.execute(httppost);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	public void postData() {
	    // Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost("https://agent.electricimp.com/a4f-wesuNciJ?led=0");

	    try {
	        // Add your data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(10);
//	        for (int i = 0; i < WINDOW_SIZE_RIGHT*3; i++) {
//	        	nameValuePairs.add(new BasicNameValuePair(STRAIGHT + "", "x"));
//	        }
//	        for (int i = 0; i < WINDOW_SIZE_RIGHT; i++) {
//	        	nameValuePairs.add(new BasicNameValuePair("200", "x"));
//	        }
	        for (cTurn i : turns) {
	        	if (i.left == true) {
		        	for (int j = 0; j < WINDOW_SIZE_LEFT; j++) {
		        		nameValuePairs.add(new BasicNameValuePair(i.getDir(), "x"));
		        	}
	        	}
	        	else {
		        	for (int j = 0; j < WINDOW_SIZE_RIGHT; j++) {
		        		nameValuePairs.add(new BasicNameValuePair(i.getDir(), "x"));
		        	}
	        	}
	        }

//	        for (cTurn i : turns) {
//	        	Log.i("pwms", i.getDir() + " " + i.getDur());
//	        	for (int j = 0; j < Integer.parseInt(i.getDur()); j++) {
//	        		Log.i("actual pwms", i.getDir());
//	        		nameValuePairs.add(new BasicNameValuePair(i.getDir(), "x"));
//	        	}
//	        	nameValuePairs.add(new BasicNameValuePair(i.getDir(), i.getDur()));
//	        }
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httppost);
	        
	    } catch (ClientProtocolException e) {
	        // TODO Auto-generated catch block
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	    }
	} 
	
	private float getDist(dPoint p1, dPoint p2) {
		return (float) Math.sqrt((p1.x - p2.x)*(p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
	}
	
	private void doCalcs() {
//		points.add(allPoints.get(allPoints.size() - 1));
		
		dPoint zero = allPoints.get(0);
//		Log.i("allPoints at 0", zero.x + "," + zero.y);
		dPoint first = allPoints.get(2);
//		Log.i("allPoints at 2", first.x + "," + first.y);
		first.y = first.y - 30;
//		Log.i("after modified", first.x + "," + first.y);
		points.add(0, first);
		points.add(1, new dPoint(first.x, first.y + 30));
		for (int i = 0; i < points.size() - 2; i++) {
			dPoint p1 = points.get(i);
			dPoint p2 = points.get(i+1);
			dPoint p3 = points.get(i+2);
			
//			dPoint p1 = new dPoint(0,0);
//			dPoint p2 = new dPoint(3, 5);
//			dPoint p3 = new dPoint(3, 10);
			
			float a = getDist(p1, p2);
			float b = getDist(p2, p3);
			float c = getDist(p1, p3);
			
//			float angle = (float) ((float) ((Math.PI - Math.acos((a*a + b*b - c*c) / (2 * a * b)))) * 180 / Math.PI);
//			if (p3.x < p2.x) angle *= -1;
			
			float angle = (float) Math.toDegrees(Math.atan2(p1.y - p2.y,(p1.x - p2.x)) - Math.atan2(p2.y- p3.y,(p2.x- p3.x)));
		
			
//			float angle = (float) Math.toDegrees(Math.atan2(p2.x- p3.x,(p2.y- p3.y)) - Math.atan2(p1.x - p2.x,(p1.y - p2.y)));
			
			if (angle < -180) {
				angle = angle + 360;
			}
			
			if (angle > 180) {
				angle = -(360 - angle); 
			}
			// add to cTurns ArrayList
			double dir;
			double dur;
			boolean left;
			
			if (angle >= 0) {
				dur = (int) (angle / 90 * 27);
//				if (dur == 0) dir = 147;
//				else dir = 200;
				dir =  (angle / 90.0 * (195 - STRAIGHT) + STRAIGHT);
				left = false;
			} else {
				dur = (int) (angle / 90 * 33) * -1;
//				if (dur == 0) dir = 147;
//				else dir = 100;
				dir =  (angle / 90.0 * (STRAIGHT - 100) + STRAIGHT);
				left = true;
			}
//			Log.i("points", p1.x + "," + p1.y + "  " + p2.x + "," + p2.y + "  " + p3.x + "," + p3.y);
//			Log.i("angle   1", Math.toDegrees(Math.atan2(p1.y - p2.y,(p1.x - p2.x))) + "");
//			Log.i("angle   2", Math.toDegrees(Math.atan2(p2.y- p3.y,(p2.x- p3.x))) + "");
			Log.i("angles", "" + angle);
			totalAngle += angle;
			Log.i("total angle", totalAngle + "");
			
//			Log.i("dir", dir + "");
//			Log.i("direction", "" + dir);
//			Log.i("duration", Integer.toString(dur));
			
			turns.add(new cTurn(dir, dur, left));
//			turns.add(new cTurn(147, WINDOW_SIZE-dur));
			
//			Log.i("p1 + p2 + p3", p1.toString() + ' ' + p2.toString() + ' ' + p3.toString());
//			Log.i("Angle", Float.toString(angle));
			
		}
	}
	
	private void setupDrawing(){
		//get drawing area setup for interaction 
		
		brushSize = getResources().getInteger(R.integer.large_size);
		lastBrushSize = brushSize;
		drawPath = new Path();
		drawPaint = new Paint();
		
		drawPaint.setColor(paintColor);
		
		drawPaint.setAntiAlias(true);
		drawPaint.setStrokeWidth(brushSize);
		drawPaint.setStyle(Paint.Style.STROKE);
		drawPaint.setStrokeJoin(Paint.Join.ROUND);
		drawPaint.setStrokeCap(Paint.Cap.ROUND);
		
		canvasPaint = new Paint(Paint.DITHER_FLAG);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
	//view given size
		super.onSizeChanged(w, h, oldw, oldh);
		
		canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		drawCanvas = new Canvas(canvasBitmap);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
	//draw view
		
		p=new Paint();
        Bitmap b=BitmapFactory.decodeResource(getResources(), R.drawable.blackline3);
        p.setColor(Color.RED);
        canvas.drawBitmap(b, 230, 5, p);
		canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
		canvas.drawPath(drawPath, drawPaint);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		Paint tempPaint = new Paint();
		tempPaint.setAntiAlias(true);
		tempPaint.setStrokeWidth(brushSize);
		tempPaint.setStyle(Paint.Style.STROKE);
		tempPaint.setStrokeJoin(Paint.Join.ROUND);
		tempPaint.setStrokeCap(Paint.Cap.ROUND);
		tempPaint.setColor(Color.BLACK);
		
		//detect user touch     
		
		float touchX = event.getX();
		float touchY = event.getY();
		
		if (touchCount == 0) {
			totalAngle = 0;
			points = new ArrayList<dPoint>();
			turns = new ArrayList<cTurn>();
			allPoints = new ArrayList<dPoint>();
			drawCanvas.drawPoint(touchX - 10, touchY - 10, tempPaint);
			pathL = 0; 
//			points.add(new dPoint(touchX, (float) (HEIGHT - (touchY + 30))));
//			points.add(new dPoint(touchX, (float) (HEIGHT - touchY)));
//			allPoints.add(new dPoint(touchX, touchY + 30));
//			allPoints.add(new dPoint(touchX, (float) (HEIGHT - touchY)));
			}
		
		else pathL += Math.sqrt((touchX - prevX) * (touchX - prevX) + (touchY - prevY) * (touchY - prevY));
		
		allPoints.add(new dPoint(touchX, (float) (HEIGHT - touchY)));
		
		if (pathL > 150) {
			Log.i("pathLength", pathL + "");
			points.add(new dPoint(touchX, (float) (HEIGHT - touchY)));
			drawCanvas.drawPoint(touchX - 10, touchY - 10, tempPaint);
			pathL = 0;
		}
		
		prevX = touchX;
		prevY = touchY;
		
		touchCount++;
		
		
				
//		Log.i("X, Y", Float.toString(touchX) + ' ' + Float.toString(touchY));
//		Log.i("Path length", Float.toString(pathL));
//		Log.i("dPoints", Float.toString((points.get(points.size() - 1)).x) + ' ' + Float.toString((points.get(points.size() - 1)).y));

		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		    drawPath.moveTo(touchX, touchY);
		    break;
		case MotionEvent.ACTION_MOVE:
		    drawPath.lineTo(touchX, touchY);
		    break;
		case MotionEvent.ACTION_UP:
		    drawCanvas.drawPath(drawPath, drawPaint);
		    drawPath.reset();
		    break;
		default:
		    return false;
		}
		
		invalidate();
		return true;
	}
	
	public void setColor(String newColor){
		//set color  
		invalidate();
		paintColor = Color.parseColor(newColor);
		drawPaint.setColor(paintColor);
	}
	
	public void setBrushSize(float newSize){
		//update size
		float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 
			    newSize, getResources().getDisplayMetrics());
			brushSize=pixelAmount;
			drawPaint.setStrokeWidth(brushSize);
	}
	
	
	public void setLastBrushSize(float lastSize){
	    lastBrushSize=lastSize;
	}
	public float getLastBrushSize(){
	    return lastBrushSize;
	}
}
