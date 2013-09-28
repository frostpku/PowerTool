package os.lowpower.powertool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Calendar;


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.GestureDetector.OnGestureListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.Toast;


public class TrainingService extends Service{

	private BluetoothAdapter bluetooth;
	private TimerThread mthread;
	private float CpuUsage;
	private float memUsage;
	private float IOUsage;
	private float CpuA;
	private float CpuB;
	private int iowaitStart;
	private int iowaitEnd;
  
    
	private static final File statFile = new File("/proc/stat");
	private static final File memFile = new File("/proc/meminfo");
	private PowerDataIO vUpdateIO;
	private  InputStreamReader isr = null;
    private  BufferedReader brStat ;
    private  BufferedReader brMem; 
    private  int CpuUserStart;
    private  int CpuUserEnd;
    private  int idleEnd;
    private  int idleStart;
    private long cpu1;
    private long cpu2;
    private long idle1;
    private long idle2;
    private BroadcastReceiver batteryReceiver;
	
	
	private String ScreenBri;
	private String VolumeMus;
	private String WifiStatus;
	private String Power;
	private int brightness;
	private int batterylevel;
	private float voltagelevel;
	private int currentSignalStrenght;
	private long lastVoltage;
	private long lastTime;
	private long mobileVol;
	private long lastWIFI;
	
	private int memTotal;
	private int memFree;
    
	private String infoToWrite;
	private PowerDataIO mIO;
	private TelephonyManager Tel;
	private MyPhoneStateListener MyListener;
	private boolean serviceOn;
	public long VUpdateTime;
	private getVRateThread t_v;

	//attention: TrafficStats is not supported before android 2.2, so for earlier platforms,
	//consider using MTrafficStats, which I wrote to achieve the same effect as TrafficStats.
	//MTrafficStats myStats;
	private TrafficStats standStats;
	
	private boolean profiling;
	private int writeFlag;
	
	public void initVaribles(){
		brightness =-1;
		batterylevel=0;
		voltagelevel=0.0f;
		currentSignalStrenght = 0;
		lastVoltage = 0;
		lastTime = 0;
		mobileVol= 0;
		lastWIFI = 0;
		
		memTotal = 0;
		memFree = 0;
		serviceOn = false;
		VUpdateTime = 0;
		profiling = false;
		writeFlag = 0;
	    infoToWrite="";
	}
	private boolean checkVUpdateTime()
	{
		vUpdateIO = new PowerDataIO("/sdcard/","vUpdateTime");
		vUpdateIO.DataIntoSD(str);
	}
	public void onCreate() {
		  super.onCreate();
		  initVaribles();
		  if (t_v == null)
		  {
			  t_v = new getVRateThread();
			  t_v.start();
		  }
		  startTraining();
	}
	public void onStart(Intent intent, int startId, Context context) {  
		//Log.i("frost","service started.");
		
	}
	public void onDestroy() {
	      super.onDestroy();
	      stopTraining();
	}
	public void setVUpdateRate(long l)
	{
		VUpdateTime = l;
	}
	void startTraining(){
		if (profiling)
			return;
		profiling = true;
		writeFlag = 0;
		try {
			mIO=new PowerDataIO();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		standStats = new TrafficStats();
		MyListener = new MyPhoneStateListener();
		Tel = ( TelephonyManager )getSystemService(Context.TELEPHONY_SERVICE);
		Tel.listen(MyListener ,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		batteryReceiver=new BroadcastReceiver(){  
		public void onReceive(Context context, Intent intent) {  
			if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction()))
			{
				int scale = intent.getIntExtra("scale", 100);
				batterylevel = (int)((float)intent.getIntExtra("level", 0)*100/scale);
				voltagelevel = (float)intent.getIntExtra("voltage", 0) ;
			}
		}}; 
		if(mthread==null){
			mthread = new TimerThread();
			mthread.start();
		}
		bluetooth = BluetoothAdapter.getDefaultAdapter();  
	}
	void stopTraining(){
		if (!profiling)
			return;
		profiling = false;
		try {
			mIO.DataIntoSD(infoToWrite);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        mthread.interrupt();
        mthread=null;
        writeFlag = 0;
	}
	private class MyPhoneStateListener extends PhoneStateListener{

    	@Override
    	public void onSignalStrengthsChanged(SignalStrength signalStrength){
    		super.onSignalStrengthsChanged(signalStrength);
    		currentSignalStrenght = signalStrength.getGsmSignalStrength();
    	}
    };
    //not used right now.
    private class setCPUThread extends Thread{
    	public setCPUThread()
    	{
    		
    	}
    	public void run(){
    		while (!Thread.interrupted()){
    			for (int i = 0; i < 100000; i++)
    				;
    			try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	} 	
    }
    private class getVRateThread extends Thread{
    	long startT;
    	long endT;
    	boolean started;
    	boolean finished;
    	long lastV;
    	long currentV;
    	//setCPUThread t_cpu;
    	public getVRateThread()
    	{	
    		startT= 0;
    		endT = 0;
    		started = false;
    		lastV = 0;
    		//t_cpu = new setCPUThread();
    	}
    	public void run()
    	{
    		while (!Thread.interrupted()){
	    		try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		if (!started)
	    		{
	    			lastV = getCurrentVoltage();
	    			started = true;
	    		}
	    		else{
	    			currentV = getCurrentVoltage();
	    			if (startT == 0  && currentV!=lastV)
	    			{
	    				startT = getCurrentTime();
	    			}
	    			else if (endT == 0 && currentV != lastV)
	    			{
	    				endT = getCurrentTime();
	    				TrainingService.this.VUpdateTime = endT - startT ;
	    				this.interrupt();
	    				return;
	    			}
	    			lastV = currentV;
	    		}
    		}
    	}
    }
    
	private class TimerThread extends Thread{
    	public TimerThread()
    	{
    		infoToWrite = "";
    	}
    	public void run() {
    		try {
    			
    			while (TrainingService.this.VUpdateTime == 0)
    				sleep(1);
    			if (t_v != null)
    			{
	    			t_v.interrupt();
	    			t_v = null;
    			}
    			//initialize delta variables
    			lastWIFI = getTotalWIFIBytes();
    			lastTime = getCurrentTime();
    			lastVoltage = getCurrentVoltage();
    			
    			while (!Thread.interrupted()) {
	    			Log.i("frost","Running Service");
	    			mobileVol = getTotalWIFIBytes();
					Thread.sleep(VUpdateTime*1000);
					writeFlag++;
					if (writeFlag == 30)
					{
						try {
							mIO.DataIntoSD(infoToWrite);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						infoToWrite = "";
						writeFlag = 0;
					}
					//All keys: "wifi" "cpu" "screen" "3g" "audio" "constant"
	    			infoToWrite+= String.valueOf(getCurrentTime())+'\t'
	    						 +String.valueOf(getDeltaWIFIBytes())+'\t'
	    						 +String.valueOf(getCpuUsage())+'\t'
	    					     +String.valueOf(getBrightness())+'\t'
	    					     +String.valueOf(getNET())+'\t'
	    					     +String.valueOf(getVolume())+'\t'
	    					     +String.valueOf(getCurrentVoltage());
	    			infoToWrite+='\n';
    			}
    		} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
	

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	public long getCurrentTime(){
    	long absoluteTimeEnd;
    	Calendar rightNow = Calendar.getInstance();
    	absoluteTimeEnd = rightNow.getTimeInMillis()/1000;
    	return absoluteTimeEnd;
    }
	public long getDeltaTime(){
		long currentTime = getCurrentTime();
		long delta = currentTime - lastTime;
		lastTime = currentTime;
		return delta;
	}
  
    public void refreshCPUIOUsage()
  	{
    	try{
	    	RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
	        String load = reader.readLine();
	        String[] toks = load.split(" ");
	        idle2 = Long.parseLong(toks[5]);
	        cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
	              + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
	        CpuUsage= (float)(cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));
	        cpu1 = cpu2;
	        idle1 = idle2;
    	} catch (IOException ex) {
            ex.printStackTrace();
        }
  	 }
  	public float getCpuUsage()
  	{
  		refreshCPUIOUsage();
  		return CpuUsage;
  	}
  	public int getBattery()
 	 {
  		return batterylevel;
 	 }
	public int getBrightness() {
		// TODO Auto-generated method stub
		int value = 0;
		ContentResolver cr = this.getContentResolver();
		try {value = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS);} 
		catch (SettingNotFoundException e) {}
		return value;
	}
	public int getVolume(){
		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int currentSys = am.getStreamVolume( AudioManager.STREAM_SYSTEM ); 
        int currentTalk = am.getStreamVolume( AudioManager.STREAM_VOICE_CALL );
        int currentRing = am.getStreamVolume( AudioManager.STREAM_RING );
        int currentMusic = am.getStreamVolume( AudioManager.STREAM_MUSIC );
        int currentAlarm = am.getStreamVolume( AudioManager.STREAM_ALARM );
        if (am.isMusicActive())
        	return currentMusic;   //now only return the system volume, change it if needed 
        else return 0;
	}
	public int getGPS() {
		// TODO Auto-generated method stub
		String str = Settings.System.getString(getContentResolver(),  Settings.System.LOCATION_PROVIDERS_ALLOWED);
		if (str.contains("gps"))
			return 1;
		else return 0;
	}
	public int getNET() {
		String ans="0";
		long currentMobileVol = TrainingService.this.getTotalMobileBytes();
		
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectivityManager!=null){
				NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
				if(networkInfo!=null){
					ans = networkInfo.getSubtypeName();
					if (ans.equalsIgnoreCase("HSPA"))
						return 3;
					else if (ans.equalsIgnoreCase("UMTS"))
					{
						if (currentMobileVol == this.mobileVol)
							return 1;
						else return 2;
					} 
				}
		}
		return 0;
	}
	public int getSignalStrength(){
		return currentSignalStrenght;
	}
	public int getBluetooth() {
		// TODO Auto-generated method stub
		if(bluetooth != null) 
		{
			if (bluetooth.isEnabled()) 
			{   
				//bluetooth here specifies the enabled status into 4 states, use it if needed.
				int state = bluetooth.getState();
				switch (state){
				case BluetoothAdapter.STATE_OFF :
				case BluetoothAdapter.STATE_TURNING_OFF  :
				case BluetoothAdapter.STATE_TURNING_ON :
				case BluetoothAdapter.STATE_ON :
				}
				return 1;
			} 
			else
			{    
				return 0;
			}
			
		}  
		return -1;//identify no bluetooth device is found
	}
	public long getTotalMobileBytes()
	{
		long total =this.standStats.getMobileRxBytes()+this.standStats.getMobileTxBytes();
		return total;
	}
	public long getTotalWIFIBytes()
	{
		long total =this.standStats.getTotalRxBytes()+this.standStats.getTotalTxBytes()
					-this.standStats.getMobileRxBytes()-this.standStats.getMobileTxBytes();
		return total;
	}
	public long getDeltaWIFIBytes()
	{
		long currentWIFI = getTotalWIFIBytes();
		long delta = currentWIFI - lastWIFI;
		lastWIFI = currentWIFI;
		return delta;
	}
	public long getCurrentVoltage(){
		VoltageReader vr = new VoltageReader();
		return vr.getVoltage();
	}
	public long getDeltaVoltage()
	{
		long currentVoltage = getCurrentVoltage();
		if (currentVoltage == -1)
			return -1;
		long delta = currentVoltage - lastVoltage;
		lastVoltage = currentVoltage;
		return delta;
	}
}