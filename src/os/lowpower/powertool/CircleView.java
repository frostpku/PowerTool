package os.lowpower.powertool;

import java.util.List;

//import com.example.bingtu.MyGestureListener;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.drawable.shapes.ArcShape;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;  
import android.graphics.SweepGradient;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;


public class CircleView extends android.view.View {
 
 private int mLastX;
 private int mLastY;
 
 private double dA = 0d;
 private double dB = 0d;
 private double dC = 0d;
 private double sum = 0d ;
 
 private float mTop = 350f;
 private float mLeft = 50f;
 private float mRadius = 220f;
 private float[] drawAngle;
 private float[] percent;
 private String[] name;
 
 float sweepAngle = 0f;
 float startAngle = 0f;
 float textSweepAngle = 0f;
 float preSweepAngle = 0f;

 private Paint piePaint;
 private Paint centerPaint;
 private Paint centerTextPaint;
 
 private RadialGradient mRadialGradient;
 
 private RelativeLayout drawPieLayout;
 private RelativeLayout drawCenterCircleLayout;
 
 public CircleView(Context context, AttributeSet attrs, int defStyle) {
  super(context, attrs, defStyle);
 }
 public CircleView(Context context) {
  super(context);
  
  initPaint();//��ʼ��paint
  
  initLayout();//��ʼ��layout����
  
 }
 public CircleView(Context context, AttributeSet attrs) {
  super(context, attrs);
  
 }

 public void initPaint(){
  piePaint = new Paint();
  piePaint.setAntiAlias(true);
  piePaint.setStyle(Paint.Style.FILL);
  piePaint.setStrokeCap(Cap.ROUND);
  piePaint.setStrokeJoin(Join.ROUND);
  
  centerPaint = new Paint();
  centerPaint.setAntiAlias(true);
  centerPaint.setStyle(Paint.Style.FILL);
  centerPaint.setStrokeCap(Cap.ROUND);
  centerPaint.setStrokeJoin(Join.ROUND);
  
  centerTextPaint = new Paint();
  centerTextPaint.setColor(Color.BLACK);
  centerTextPaint.setTextSize(24);
  centerTextPaint.setTextAlign(Align.CENTER);
  centerTextPaint.setStrokeWidth(0);
  centerTextPaint.setAntiAlias(true);
 }
 
 public void initLayout(){

  drawPieLayout = new RelativeLayout(getContext());
  drawPieLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
//  CircleView cv = new CircleView(getContext());
//  cv.setSum(sum);
//  drawPieLayout.addView(cv);
 }
 
 public void initData(List<Double> listData){
  for(Double d : listData){
   sum += d;
  }
  drawAngle = new float[listData.size()];
  percent = new float[listData.size()];
  for(int i = 0 ;i < listData.size();i++){
   float temp = (float) (listData.get(i)/sum * 100);//�ٷֱ�
   percent[i] = Float.parseFloat(NumberUtils.getDecimal((double)temp));
   drawAngle[i] = temp*3.6f;//��ͼ�Ƕ�
  }
 }
 public void initName(List<String> listName){
	  name = new String[listName.size()];
	  for(int i = 0 ;i < listName.size();i++){
		  name[i]  = listName.get(i);
	  }
	 }
 
 
 @Override
 protected void onDraw(Canvas canvas) {
  // TODO Auto-generated method stub
  super.onDraw(canvas);
  
  boolean bCanDrawText = true;

  int[] colors = {Color.BLUE,Color.MAGENTA,Color.GREEN,Color.CYAN,Color.YELLOW};
  
  for(int i = 0 ;i < drawAngle.length;i++){//���������
   
   sweepAngle = drawAngle[i];
   
   //������Ⱦ
   mRadialGradient = new RadialGradient(mLeft+mRadius,mTop + mRadius,mRadius,  
                    new int[]{colors[i%colors.length],colors[i%colors.length],colors[i%colors.length],
     colors[i%colors.length],colors[i%colors.length],colors[i%colors.length],
     colors[i%colors.length],colors[i%colors.length]},  
                    null,Shader.TileMode.MIRROR);  
   piePaint.setShader(mRadialGradient);
   /*
    *��һ������
    *RectF oval ���ڻ����εľ�������
    * float startAngle ��ʼ�Ƕ�
    * float sweepAngle ��Ҫ�������νǶ�
    * boolean useCenter 
    * Paint paint ����
    */
   canvas.drawArc(new RectF(mLeft, mTop, mLeft + 2*mRadius, mTop + 2*mRadius), startAngle,sweepAngle, true, piePaint);
   
   //��Ҫ���ֵĽǶ�
   textSweepAngle = startAngle + drawAngle[i]/2;
   
   double x = 0d;
   double y = 0d;
   
   double adjacentSide = Math.abs(Math.cos(textSweepAngle*Math.PI/180)*mRadius*4/3);//�ڱ�
   double oppositeSide = Math.abs(Math.sin(textSweepAngle*Math.PI/180)*mRadius*4/3);//�Ա�
   
   if(textSweepAngle>360){
    textSweepAngle = textSweepAngle%360;
   }
   
   //˳ʱ��
   if(textSweepAngle >= 0 && textSweepAngle < 90){
    x =  adjacentSide + (mLeft + mRadius);
    y =  oppositeSide + (mTop + mRadius);
   }else if(textSweepAngle >= 90 && textSweepAngle < 180){
    x = (mLeft + mRadius) - adjacentSide;
    y = oppositeSide + (mTop + mRadius);
   }else if(textSweepAngle >= 180 && textSweepAngle < 270){
    x = (mLeft + mRadius) - adjacentSide;
    y = (mTop + mRadius) - oppositeSide;
   }else {
    x = adjacentSide + (mLeft + mRadius);
    y = (mTop + mRadius) - oppositeSide;
   }
   
   Path path = new Path();
   path.moveTo(mLeft + mRadius, mTop + mRadius);//Բ��
   path.lineTo((int)x, (int)y);
   //����·������
   canvas.drawTextOnPath(percent[i]+"%", path, 0, 0, centerTextPaint);

   
   float temp1 = startAngle % 360;//ǰ
   startAngle += sweepAngle;
   float temp2 = temp1 + sweepAngle;//��
   if (!bCanDrawText) {
    continue;
   }
   if (temp2 >= 360 && temp1 <= 360) {//�����սǶȿ�Խ360����һ��
    temp2 %= 360;
    if (temp1 <= 90 || temp2 >= 90) {
     drawText(canvas, name[i] + ":"+String.valueOf(percent[i]));
     bCanDrawText = false;
    }
   } else if (temp1 <= 90 && (temp2 %= 360) >= 90) {
    drawText(canvas, name[i] +":"+String.valueOf(percent[i]));
    bCanDrawText = false;
   }
   
  }
 }

 private void drawText(Canvas canvas, String valueOf) {
     canvas.drawText(valueOf + "%", (mLeft + mRadius), (mTop + 2*mRadius) + 10, centerTextPaint);
   }
 
 
 @Override
 public boolean onTouchEvent(MotionEvent event) {
  //��ô�����ָ����
  int point = event.getPointerCount();
  if(point == 1){
   int x = (int) event.getX();
   int y = (int) event.getY();
   int action = event.getAction();
   switch(action){
   // down
   case MotionEvent.ACTION_DOWN:
    mLastX = x;
    mLastY = y;
    double c = Math.abs(mLastX - (mLeft + mRadius));
    double d = Math.abs(mLastY- (mTop + mRadius));
    //�õ����µ㵽Բ�ĵľ���
    dA = getDistance(c, d);
    break;
   // move
   case MotionEvent.ACTION_MOVE:

    double c1 = Math.abs(event.getX() - mLastX);
    double d1 = Math.abs(event.getY() - mLastY);
    dC = getDistance(c1, d1);
    
    double c2 = Math.abs(event.getX() - (mLeft + mRadius));
    double d2 = Math.abs(event.getY() - (mTop + mRadius));
    dB = getDistance(c2, d2);
    
    //������Բ��ȷ���ĽǶ�����ֵ
    double cos = (dA*dA + dB*dB - dC*dC)/(2*dA*dB);
    
    if(mLastY < (mTop + mRadius)){//���µĵ���Բ���ϰ벿��
     
     if(event.getY() >= (mTop + mRadius) && mLastX <= (mLeft + mRadius)){//����������,��ʱ��
      startAngle -= Math.acos(cos)*180/Math.PI;
     }else if(event.getY() >= (mTop + mRadius) && mLastX > (mLeft + mRadius)){//�������Ҳ��,˳ʱ��
      startAngle += Math.acos(cos)*180/Math.PI;
     }else{//û�о���߽��
      if(event.getX() - mLastX > 0){//˳ʱ��
       startAngle += Math.acos(cos)*180/Math.PI;
      }else if(event.getX() - mLastX < 0){//��ʱ��
       startAngle -= Math.acos(cos)*180/Math.PI;
      }
     }
     
    }else if(mLastY>(mTop + mRadius)){//���µĵ���Բ���°벿��
     
     if(event.getY() <= (mTop + mRadius) && mLastX <= (mLeft + mRadius)){//���������㣬˳ʱ��
      startAngle += Math.acos(cos)*180/Math.PI;
     }else if(event.getY() <= (mTop + mRadius) && mLastX <= (mLeft + mRadius)){//�������ҵ㣬��ʱ��
      startAngle -= Math.acos(cos)*180/Math.PI;
     }else{//û�о���߽��
      if(event.getX() - mLastX < 0){//˳ʱ��
       startAngle += Math.acos(cos)*180/Math.PI;
      }else if(event.getX() - mLastX > 0){//��ʱ��
       startAngle -= Math.acos(cos)*180/Math.PI;
      }
     }
    }else if(mLastY == (mTop + mRadius) && mLastX == mLeft){//���µĵ��������
     if(event.getY() - mLastY < 0){//˳ʱ��
      startAngle += Math.acos(cos)*180/Math.PI;
     }else if(event.getY() - mLastY > 0){//��ʱ��
      startAngle -= Math.acos(cos)*180/Math.PI;
     }
     
    }else if(mLastY == (mTop + mRadius) && mLastX == (mLeft + 2*mRadius)){//���µĵ������Ҳ�
     if(event.getY() - mLastY > 0){//˳ʱ��
      startAngle += Math.acos(cos)*180/Math.PI;
     }else if(event.getY() - mLastY < 0){//��ʱ��
      startAngle -= Math.acos(cos)*180/Math.PI;
     }
    }

    invalidate();//ˢ�»�ͼ
    
    mLastX = (int)event.getX();
    mLastY = (int)event.getY();
    
    double c3 = Math.abs(mLastX - (mLeft + mRadius));
    double d3 = Math.abs(mLastY- (mTop + mRadius));
    //�õ����µ㵽Բ�ĵľ���
    dA = getDistance(c3, d3);
    
    break;
   
   // up
   case MotionEvent.ACTION_UP:
    break;
   }
  }else{
   System.out.println("point != 1");
  }
  
  return super.onTouchEvent(event);
 }
 
 /**
  * ��������֮�����
  * @param x ����x��Ĳ��
  * @param y ����y��Ĳ��
  * @return
  */
 public double getDistance(double x,double y ){
  double z = Math.sqrt(x*x + y*y);
  return z;
 }
 
 public double getSum(){
  return sum;
 }
 
}