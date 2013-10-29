package os.lowpower.powertool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;


public class ProfilingService extends Service{

	private BluetoothAdapter bluetooth;
	private TimerThread mthread;
	private float CpuUsage;
    private long cpu1;
    private long cpu2;
    private long idle1;
    private long idle2;
    private BroadcastReceiver batteryReceiver;
	private int batterylevel;
	private int currentSignalStrenght;
	private long lastVoltage;
	private long lastTime;
	private long mobileVol;
	private long lastWIFI;
	private PowerManager pm;
	private TrafficStats standStats;
    
	private String infoToWrite;
	private MyPhoneStateListener MyListener;
	private TelephonyManager Tel;
	ActivityManager am;
	
	private boolean profiling;
	private PowerDataIO testIO;

	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onCreate() {
		  super.onCreate();
		  initVaribles();
		  startProfiling();
	}
	public void onStart(Intent intent, int startId, Context context) {  
		//Log.i("frost","service started.");
		
	}
	public void onDestroy() {
	      super.onDestroy();
	      stopProfiling();
	}
	public void initVaribles(){
		profiling =false;
		am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
		pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		
	}
	public void startProfiling(){
		if (profiling)
			return;
		profiling = true;
		standStats = new TrafficStats();
		//  SIGNAL STRENGTH
		MyListener = new MyPhoneStateListener();
		Tel = ( TelephonyManager )getSystemService(Context.TELEPHONY_SERVICE);
		Tel.listen(MyListener ,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		//  BLUETOOTH
		bluetooth = BluetoothAdapter.getDefaultAdapter();  
		if(mthread==null){
			mthread = new TimerThread();
			mthread.start();
		}
	}
	private class TimerThread extends Thread{
		
		HashMap<String, Resources> taskMap;
		Resources totalResource;
		int maxServices = 200;
		int maxSamplingCount = 60;// 60
		int windowInterval = 300000; // window interval: 5 minutes
		int profilingInterval = 1000; // profiling interval: 1 second
		boolean usingWIFI;
		WifiManager mWiFiManager;
		public class ProfilingContent{
			double cpu;
			double brightness;
			double wifi;
			double net;
			int frequency;
			int cpu_freq;
			int screen_freq;
			int wifi_freq;
			int net_freq;
			public ProfilingContent()
			{
				cpu = 0.0f;
				brightness = 0.0f;
				wifi = 0.0f;
				net= 0.0f;
				frequency = 0;
				cpu_freq =0;
				screen_freq =0;
				wifi_freq =0;
				net_freq =0;
			}
		}
		public class Resources{

			long processCPUTime[];
			long totalCPUTime[];
			long WIFIBytes[];
			long netBytes[];
			int screen[];
			boolean counted;
			
			int samplingNumber;
			
			public Resources()
			{
				samplingNumber = 0;
				processCPUTime = new long[maxSamplingCount+1];
				totalCPUTime = new long[maxSamplingCount+1];
				WIFIBytes = new long[maxSamplingCount+1];
				netBytes = new long[maxSamplingCount+1];
				screen = new int[maxSamplingCount+1];
				counted = false;
			}
		}
		
		
    	public TimerThread()
    	{
    		infoToWrite = "";
    		usingWIFI=true;
    	}
    	public void setFileInfo(String fileName, HashMap<String, ProfilingContent> mFileInfo) throws IOException
    	{
    		File file;
    		FileWriter fw;
    		BufferedWriter bw;
    		ProfilingContent mWriteInfo;
    		
    		file = new File(fileName);
    		if(!file.exists()) 
    			file.createNewFile();
    		fw = new FileWriter(file,false);
    		//overwrite
    		bw = new BufferedWriter(fw);
    		String aim;
    		for (String str: mFileInfo.keySet())
    		{
    			mWriteInfo = mFileInfo.get(str);
    			aim="";
    			aim+=	str +'\t' + 
    					String.valueOf(mWriteInfo.frequency) +'\t'+
    					String.valueOf(mWriteInfo.cpu_freq)+'\t'+
    					String.valueOf(mWriteInfo.screen_freq)+'\t'+
    					String.valueOf(mWriteInfo.wifi_freq)+'\t'+
    					String.valueOf(mWriteInfo.net_freq)+'\t'+
    					String.valueOf(mWriteInfo.cpu) + '\t' + 
    					String.valueOf(mWriteInfo.brightness) +'\t'+
    					String.valueOf(mWriteInfo.wifi) +'\t'+
    					String.valueOf(mWriteInfo.net);
    			bw.write(aim);
    			bw.write(13);
    			bw.write(10);
    		}
    		bw.close();
    		fw.close();
    	}
    	public HashMap<String, ProfilingContent> getFileInfo(String fileName){
    		HashMap<String, ProfilingContent> fileInfoMap = new HashMap<String, ProfilingContent>();
    		fileInfoMap.clear();
    		try {
    			File file;
    			file = new File(fileName);
    			if(!file.exists()) 
    				file.createNewFile();
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);

                String line;
                String[] arrs;

                //format:   0			1			2			3				4			5			6		7				8			9
                //			processName	frequency	cpu_freq	screen_freq		wifi_freq	net_freq	accuCPU	accuBrightness	accuWIFI	accuNET
                while ((line = br.readLine()) != null) {
                	//br.readLine();
                    arrs = line.split("\\t");
                    ProfilingContent mWriteInfo = new  ProfilingContent();
                    mWriteInfo.frequency = Integer.parseInt(arrs[1]);
                    mWriteInfo.cpu_freq = Integer.parseInt(arrs[2]);
                    mWriteInfo.screen_freq = Integer.parseInt(arrs[3]);
                    mWriteInfo.wifi_freq = Integer.parseInt(arrs[4]);
                    mWriteInfo.net_freq = Integer.parseInt(arrs[5]);
                    mWriteInfo.cpu = Double.parseDouble(arrs[6]);
                    mWriteInfo.brightness = Double.parseDouble(arrs[7]);
                    mWriteInfo.wifi = Double.parseDouble(arrs[8]);
                    mWriteInfo.net = Double.parseDouble(arrs[9]);
                    fileInfoMap.put(arrs[0], mWriteInfo);
                }
    		 fr.close();
             br.close();
         } catch (FileNotFoundException e) {
             e.printStackTrace();
             return null;
         } catch (IOException e) {
             e.printStackTrace();
             return null;
         }
    		return fileInfoMap;
    	}
    	public void run() {
    		try {
    			/*
    			try {
					testIO = new PowerDataIO("/sdcard/","APT_RunningList.txt");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				*/
    			while (!Thread.interrupted()) {
	    			Log.i("frost","Running Service");
	    			int count =0;
	    			infoToWrite = "";
	    			taskMap= new HashMap<String, Resources>();
	    			totalResource  = new Resources();
	    			mWiFiManager=(WifiManager)getSystemService(Context.WIFI_SERVICE);
	    			if (mWiFiManager.getWifiState()==WifiManager.WIFI_STATE_ENABLED) 
	    				usingWIFI = true;
	    			else usingWIFI = false;
	    			
	    			// a consecutive 1 min's sampling
	    			
	    			while (count < maxSamplingCount)
	    			{
	    				//IF WIFI state changes during this sampling interval, this sampling would be regarded invalid.
	    				if ((mWiFiManager.getWifiState()==WifiManager.WIFI_STATE_ENABLED) && usingWIFI== false)
	    				{
	    					break;
	    				}
	    				if ((mWiFiManager.getWifiState()!=WifiManager.WIFI_STATE_ENABLED) && usingWIFI== true)
	    				{
	    					break;
	    				}
	    				totalResource.totalCPUTime[count] = getTotalCPUTime();
	    				totalResource.screen[count] = getBrightness();
	    				if (usingWIFI)
	    					totalResource.WIFIBytes[count] =getTotalWIFIBytes();
	    				else totalResource.netBytes[count] = getTotalMobileBytes();
	    				
	    				Resources mResource;
	    				List< ActivityManager.RunningAppProcessInfo > processes = am.getRunningAppProcesses(); 
	    				List<ActivityManager.RunningTaskInfo> runningTaskInfos = am.getRunningTasks(1) ;
	    				String topActivityName="";
	    		        if(runningTaskInfos != null){
	    		             ComponentName f=runningTaskInfos.get(0).topActivity;
	    		             topActivityName=f.getPackageName();
	    		        }
	    				for (String str:taskMap.keySet())
	    				{
	    					taskMap.get(str).counted = false;
	    				}
	    				
	    				for (ActivityManager.RunningAppProcessInfo process : processes)
	    				{
	    					int pid = process.pid;
	    					int uid = process.uid;
	    					String packageName = process.processName;
	    					//packageName = cutTail(packageName);
	    					if (packageName.equals("system"))
	    						continue;
	    					if (taskMap.get(packageName) == null)
	    					{
	    						mResource = new Resources();
	    					}
	    					else 
	    					{
	    						 mResource = taskMap.get(packageName);
	    						 if (mResource.counted)
	    							 continue;
	    					}
	    					mResource.processCPUTime[mResource.samplingNumber] = getProcessCPUTime(pid);
	    					mResource.totalCPUTime[mResource.samplingNumber] = getTotalCPUTime();
	    					//if (process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
	    					if (packageName.equalsIgnoreCase(topActivityName) && pm.isScreenOn())
	    						mResource.screen[mResource.samplingNumber] = getBrightness();
	    					if (usingWIFI)
	    						mResource.WIFIBytes[mResource.samplingNumber] = getUIDTotalBytes(uid);
	    					else 
	    						mResource.netBytes[mResource.samplingNumber] = getUIDTotalBytes(uid);
	    					mResource.samplingNumber++;
	    					mResource.counted = true;
	    					taskMap.put(packageName, mResource);
	    				}
	    				
	    				List <ActivityManager.RunningServiceInfo> services = am.getRunningServices(maxServices);
	    				for (ActivityManager.RunningServiceInfo service : services)
	    				{
	    					int pid = service.pid;
	    					int uid = service.uid;
	    					String packageName = service.process;
	    					//packageName = cutTail(packageName);
	    					if (packageName.equals("system"))
	    						continue;
	    					if (taskMap.get(packageName) == null)
	    					{
	    						mResource = new Resources();
	    					}
	    					else
	    					{
	    						mResource = taskMap.get(packageName);
	    						 if (mResource.counted)
	    							 continue;
	    					}
	    					mResource.processCPUTime[mResource.samplingNumber] = getProcessCPUTime(pid);
		    				mResource.totalCPUTime[mResource.samplingNumber] = getTotalCPUTime();
		    				if (service.foreground && pm.isScreenOn())
		    					mResource.screen[mResource.samplingNumber] = getBrightness();
		    				if (usingWIFI)
		    					mResource.WIFIBytes[mResource.samplingNumber] = getUIDTotalBytes(uid);
		    				else 
		    					mResource.netBytes[mResource.samplingNumber] = getUIDTotalBytes(uid);
		    				mResource.samplingNumber++;
		    				mResource.counted = true;
		    				taskMap.put(packageName, mResource);
	    				}
	    				Thread.sleep(profilingInterval);
	    				count++;
	    			}
	    			if (count == maxSamplingCount)
	    			{
	    				HashMap<String, Integer> existed = new HashMap<String, Integer>();
	    				HashMap<String, ProfilingContent> fileInfo = getFileInfo("/sdcard/APT_TaskData.txt");
	    				
	    				if (fileInfo == null)
	    				{
	    					Toast.makeText(ProfilingService.this,  
	    	                        "Visiting file error.",   
	    	                        Toast.LENGTH_LONG).show(); 
	    					return;
	    				}
	    				existed.clear();
	    				//String testSTR = "=====\r\n";
	    				for (String str : taskMap.keySet())
	    				{
	    					Resources mResource = taskMap.get(str);
	    					String shortName = cutTail(str);
	    					ProfilingContent mWriteInfo;
	    					
	    					//testSTR+=str;
	    					//testSTR +="\r\n";
	    					if (fileInfo.containsKey(shortName))
	    					{
	    						mWriteInfo = fileInfo.get(shortName);
	    					}
	    					else {
	    						mWriteInfo = new ProfilingContent();
	    					}
	    					if (existed.containsKey(shortName))
	    					{
	    						int tmp_freq =0;
	    						//AAA:B  AAA are the same app, calculate the cpu usage together.
	    						for (int i = 1; i < mResource.samplingNumber; i++)
		    					{
		    						if (mResource.totalCPUTime[i] != mResource.totalCPUTime[i-1] 
		    						&& mResource.processCPUTime[i] > mResource.processCPUTime[i-1])
		    						{
		    							mWriteInfo.cpu += (Double.parseDouble(String.valueOf(mResource.processCPUTime[i] - mResource.processCPUTime[i-1])))/
		    											(Double.parseDouble(String.valueOf(mResource.totalCPUTime[i] - mResource.totalCPUTime[i-1])));
		    							tmp_freq ++;
		    						}
		    					}
	    						if (existed.get(shortName) < tmp_freq)
	    						{
	    							mWriteInfo.cpu_freq= mWriteInfo.cpu_freq - existed.get(shortName) + tmp_freq;
	    							existed.put(shortName, tmp_freq);
	    						}
	    						//use the larger cpu freq of several different threads
	    					}
	    					else
	    					{
		    					mWriteInfo.frequency++;
		    					int tmp_freq = 0;
		    					for (int i = 1; i < mResource.samplingNumber; i++)
		    					{
		    						if (mResource.totalCPUTime[i] != mResource.totalCPUTime[i-1] 
		    						&& mResource.processCPUTime[i] > mResource.processCPUTime[i-1])
		    						{
		    							mWriteInfo.cpu += (Double.parseDouble(String.valueOf(mResource.processCPUTime[i] - mResource.processCPUTime[i-1])))/
		    											(Double.parseDouble(String.valueOf(mResource.totalCPUTime[i] - mResource.totalCPUTime[i-1])));
		    							tmp_freq ++;
		    						}
		    						if (mResource.screen[i] != 0)
		    						{
		    							mWriteInfo.brightness += mResource.screen[i];
		    							mWriteInfo.screen_freq++;
		    						}
		    						if (mResource.WIFIBytes[i] > mResource.WIFIBytes[i-1])
		    						{
		    							mWriteInfo.wifi += mResource.WIFIBytes[i] - mResource.WIFIBytes[i-1];
		    							mWriteInfo.wifi_freq++;
		    						}
		    						if ( mResource.netBytes[i] > mResource.netBytes[i-1])
		    						{
		    							mWriteInfo.net += mResource.netBytes[i] -  mResource.netBytes[i-1];
		    							mWriteInfo.net_freq++;
		    						}
		    					}
		    					mWriteInfo.cpu_freq += tmp_freq;
	    						existed.put(shortName, tmp_freq);
	    					}
	    					fileInfo.put(shortName, mWriteInfo);
	    				}
	    				
	    				try {
							setFileInfo("/sdcard/APT_TaskData.txt", fileInfo);
							//testIO.DataIntoSD(testSTR);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	    			}
					Thread.sleep(windowInterval);
					
    			}
    		} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
		private String cutTail(String str) {
			// TODO Auto-generated method stub
			for (int i = 1; i < str.length(); i++)
			{
				if (str.charAt(i)==':')
				{
					return str.substring(0, i);
				}
			}
			return str;
		}
    }
	void stopProfiling(){
		if (!profiling)
			return;
		profiling = false;
        mthread.interrupt();
        mthread=null;
	}
	private class MyPhoneStateListener extends PhoneStateListener{

    	@Override
    	public void onSignalStrengthsChanged(SignalStrength signalStrength){
    		super.onSignalStrengthsChanged(signalStrength);
    		currentSignalStrenght = signalStrength.getGsmSignalStrength();
    	}
    };
    public long getProcessCPUTime(int pid)
    {
    	long aim= 0;
    	String fileName = "/proc/" + String.valueOf(pid)+"/stat";
    	try{
	    	RandomAccessFile reader = new RandomAccessFile(fileName, "r");
	        String load = reader.readLine();
	        String[] toks = load.split(" ");
	        aim = Long.parseLong(toks[13]) + Long.parseLong(toks[14]) + Long.parseLong(toks[15])
	              + Long.parseLong(toks[16]);
	        reader.close();
    	}
    	 catch (IOException ex) {
             ex.printStackTrace();
         }
    	return aim;
    }
    public long getTotalCPUTime()
    {
    	long idle = 0;
    	long cpu =0;
    	String fileName = "/proc/stat";
    	try{
	    	RandomAccessFile reader = new RandomAccessFile(fileName, "r");
	        String load = reader.readLine();
	        String[] toks = load.split(" ");
	        idle = Long.parseLong(toks[5]);
	        cpu = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
	              + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
	        reader.close();
    	}
    	 catch (IOException ex) {
             ex.printStackTrace();
         }
    	return idle + cpu;
    	
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
	        reader.close();
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
		long currentMobileVol = getTotalMobileBytes();
		
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
		long total =standStats.getMobileRxBytes()+standStats.getMobileTxBytes();
		return total;
	}
	public long getTotalWIFIBytes()
	{
		long total =standStats.getTotalRxBytes()+standStats.getTotalTxBytes()
					-standStats.getMobileRxBytes()-standStats.getMobileTxBytes();
		return total;
	}
	public long getUIDTotalBytes(int uid)
	{
		long total =standStats.getUidRxBytes(uid)+standStats.getUidTxBytes(uid);
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