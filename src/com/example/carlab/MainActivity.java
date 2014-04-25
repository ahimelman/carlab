package com.example.carlab;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
public class MainActivity extends Activity implements OnClickListener{
	
	private DrawingView drawView;
	private ImageButton currPaint, drawBtn, eraseBtn, newBtn;
	private Button goBtn;
	private float smallBrush, mediumBrush, largeBrush;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        drawView = (DrawingView)findViewById(R.id.drawing);
        LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
        currPaint = (ImageButton)paintLayout.getChildAt(0);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
        
        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);
        drawBtn = (ImageButton)findViewById(R.id.draw_btn);
        
        drawBtn.setOnClickListener(this);
        drawView.setBrushSize(largeBrush);
        
        eraseBtn = (ImageButton)findViewById(R.id.erase_btn);
        eraseBtn.setOnClickListener(this);
        
        newBtn = (ImageButton)findViewById(R.id.new_btn);
        newBtn.setOnClickListener(this);
        
        goBtn = (Button)findViewById(R.id.go_btn);
        goBtn.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public void paintClicked(View view){
    	drawView.setErase(false);
    	drawView.setBrushSize(drawView.getLastBrushSize());
    	
        //use chosen color
    	if(view!=currPaint){
    		//update color
    		ImageButton imgView = (ImageButton)view;
    		String color = view.getTag().toString();
        	drawView.setColor(color);
        	imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
        	currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
        	currPaint=(ImageButton)view;
    	}
    }
    
    @Override
    public void onClick(View view){
    //respond to clicks 
    	if(view.getId()==R.id.draw_btn){
    	    //draw button clicked
    		final Dialog brushDialog = new Dialog(this);
    		brushDialog.setTitle("Brush size:");
    		brushDialog.setContentView(R.layout.brush_chooser);
    		ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
    		smallBtn.setOnClickListener(new OnClickListener(){
    		    @Override
    		    public void onClick(View v) {
    		        drawView.setBrushSize(smallBrush);
    		        drawView.setLastBrushSize(smallBrush);
    		        drawView.setErase(false);
    		        brushDialog.dismiss();
    		    }
    		});
    		ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
    		mediumBtn.setOnClickListener(new OnClickListener(){
    		    @Override
    		    public void onClick(View v) {
    		        drawView.setBrushSize(mediumBrush);
    		        drawView.setLastBrushSize(mediumBrush);
    		        drawView.setErase(false);
    		        brushDialog.dismiss();
    		    }
    		});
    		 
    		ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
    		largeBtn.setOnClickListener(new OnClickListener(){
    		    @Override
    		    public void onClick(View v) {
    		        drawView.setBrushSize(largeBrush);
    		        drawView.setLastBrushSize(largeBrush);
    		        drawView.setErase(false);
    		        brushDialog.dismiss();
    		    }
    		});
    		brushDialog.show();
    	}
    	
    	else if (view.getId() == R.id.erase_btn) {
    		final Dialog brushDialog = new Dialog(this);
    		brushDialog.setTitle("Eraser size:");
    		brushDialog.setContentView(R.layout.brush_chooser);
    		ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
    		smallBtn.setOnClickListener(new OnClickListener(){
    		    @Override
    		    public void onClick(View v) {
    		        drawView.setErase(true);
    		        drawView.setBrushSize(smallBrush);
    		        brushDialog.dismiss();
    		    }
    		});
    		ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
    		mediumBtn.setOnClickListener(new OnClickListener(){
    		    @Override
    		    public void onClick(View v) {
    		        drawView.setErase(true);
    		        drawView.setBrushSize(mediumBrush);
    		        brushDialog.dismiss();
    		    }
    		});
    		ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
    		largeBtn.setOnClickListener(new OnClickListener(){
    		    @Override
    		    public void onClick(View v) {
    		        drawView.setErase(true);
    		        drawView.setBrushSize(largeBrush);
    		        brushDialog.dismiss();
    		    }
    		});
    		brushDialog.show();
    	}
    	
    	else if(view.getId()==R.id.new_btn){
    	    //new button
    		AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
    		newDialog.setTitle("New drawing");
    		newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
    		newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
    		    public void onClick(DialogInterface dialog, int which){
    		        drawView.startNew();
    		        dialog.dismiss();
    		    }
    		});
    		newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
    		    public void onClick(DialogInterface dialog, int which){
    		        dialog.cancel();
    		    }
    		});
    		newDialog.show();
    	}
    	else if(view.getId()==R.id.go_btn) {
    		drawView.goNew();
    	}
    }
    
}
