package os.lowpower.powertool;

import java.text.DecimalFormat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.view.View;

public class CenterView extends View{

	DecimalFormat df;
 private Paint centerPaint;
 private Paint centerTextPaint;
 private RadialGradient mRadialGradient;
 private double sum;
 
 public CenterView(Context context, AttributeSet attrs, int defStyle) {
  super(context, attrs, defStyle);
  	
  // TODO Auto-generated constructor stub
 }

 public CenterView(Context context, AttributeSet attrs) {
  super(context, attrs);
  
 }

 public CenterView(Context context) {
  super(context);
  df = new DecimalFormat();
	df.setMaximumFractionDigits(2);
	df.setMinimumFractionDigits(2);
  centerPaint = new Paint();
  centerPaint.setAntiAlias(true);
  centerPaint.setAntiAlias(true);
  centerPaint.setStrokeCap(Cap.ROUND);
  centerPaint.setStrokeJoin(Join.ROUND);
  
  centerTextPaint = new Paint();
  centerTextPaint.setColor(Color.WHITE);
  centerTextPaint.setTextSize(18);
  centerTextPaint.setTextAlign(Align.CENTER);
  centerTextPaint.setAntiAlias(true);
  
 }
 
 @Override
 protected void onDraw(Canvas canvas) {
  super.onDraw(canvas);

  this.sum = getSum();
  
  for(int i = 0 ; i<10;i++){//������СԲ
   mRadialGradient = new RadialGradient(270,570,90,  
                 new int[]{Color.GRAY,Color.GRAY,Color.GRAY,Color.GRAY,Color.GRAY,Color.GRAY,Color.GRAY,Color.GRAY,Color.GRAY,
     Color.GRAY,Color.GRAY,Color.GRAY,Color.GRAY,Color.GRAY,Color.WHITE},  
                 null,Shader.TileMode.MIRROR);
  
   centerPaint.setShader(mRadialGradient);
   centerPaint.setAlpha(30);
   canvas.drawCircle(270,570,90, centerPaint);
   
  // canvas.drawText("SUM ENERGY", 270, 565, centerTextPaint);
   //canvas.drawText(df.format(sum)+"J", 270, 585, centerTextPaint);
  }
 }

 public double getSum() {
  return sum;
 }

 public void setSum(double sum) {
  this.sum = sum;
 }
 
 

}