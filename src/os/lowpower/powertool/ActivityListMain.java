package os.lowpower.powertool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
public class ActivityListMain extends Activity {  
	
	List<ProfilingContent> historyContent;
	public class ProfilingContent implements Comparable
	{
		String name;
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
		public int compareTo(Object arg0) {
			// TODO Auto-generated method stub
			//according to the screen count, decrease sort
			return ((ProfilingContent)arg0).screen_freq - this.screen_freq;
		}
	}
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);  
        historyContent = null;
        setContentView(R.layout.list_main);
       List<Programe> list = getFrequentProcess();  
       if (list == null)
       {
    	   Toast.makeText(ActivityListMain.this,  
                   "Sorry, no frequent tasks have been profiled.",   
                   Toast.LENGTH_LONG).show();  
    	   return;
       }
       ListAdapter adapter = new ListAdapter(list,this);  
       getListView().setAdapter(adapter);  
       getListView().setOnItemClickListener(new OnItemClickListener() {

           public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        	   ProfilingContent oneItem = historyContent.get(arg2);
        	   Intent intent =new Intent();
        	   intent.putExtra("name", oneItem.name);
        	   intent.putExtra("cpu", oneItem.cpu);
        	   intent.putExtra("brightness", oneItem.brightness);
        	   intent.putExtra("wifi", oneItem.wifi);
        	   intent.putExtra("net", oneItem.net);
        	   intent.putExtra("frequency", oneItem.frequency);
        	   intent.putExtra("cpu_freq", oneItem.cpu_freq);
        	   intent.putExtra("screen_freq", oneItem.screen_freq);
        	   intent.putExtra("wifi_freq", oneItem.wifi_freq);
        	   intent.putExtra("net_freq", oneItem.net_freq);
        	   intent.setClass(ActivityListMain.this, TaskDetail.class);
        	   startActivity(intent);
        	   //start item activity!!!!!!!
           }
       });
    }  
    ListView getListView() {
        return (ListView) this.findViewById(R.id.listbody);
    }
    public List<Programe> getFrequentProcess(){
    	
    	if (historyContent == null)
    		historyContent = getFileInfo("/sdcard/APT_TaskData.txt");
    	if (historyContent == null || historyContent.size() == 0)
    		return null;
    	 List<Programe> list = new ArrayList<Programe>();
    	 
    	 for (int i = 0; i< historyContent.size();i++)
    	 {
    		 if (historyContent.get(i).screen_freq == 0)
    		 {
    			 historyContent.remove(i);
    			 i--;
    		 }
    	 }

    	 Collections.sort(historyContent);
    	 
    	 PackagesInfo pi = new PackagesInfo(this);  
     	 PackageManager pm =this.getPackageManager();  
    	 for (int i = 0; i< historyContent.size();i++)
    	 {
    		 String packageName = historyContent.get(i).name;
    		 Programe  pr = new Programe();  
             ApplicationInfo tmpAPP = pi.getInfo(packageName);
             if (tmpAPP != null)
             {
 	            pr.setIcon(tmpAPP.loadIcon(pm));  
 	            pr.setName(tmpAPP.loadLabel(pm).toString());  
 	            System.out.println(tmpAPP.loadLabel(pm).toString());  
 	            list.add(pr);  
             }
    	 }
    	 return list;
    }
    //正在运行的   
    
    public List<Programe> getRunningProcess(){  
        PackagesInfo pi = new PackagesInfo(this);  
          
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);  
        //获取正在运行的应用   
        List<RunningAppProcessInfo> run = am.getRunningAppProcesses();  
        //获取包管理器，在这里主要通过包名获取程序的图标和程序名   
        PackageManager pm =this.getPackageManager();  
        List<Programe> list = new ArrayList<Programe>();      
          
        for(RunningAppProcessInfo ra : run){  
            //这里主要是过滤系统的应用和电话应用，当然你也可以把它注释掉。   
            if(ra.processName.equals("system") || ra.processName.equals("com.android.phone")){  
                continue;  
            }  
              
            Programe  pr = new Programe();  
            ApplicationInfo tmpAPP = pi.getInfo(ra.processName);
            if (tmpAPP != null)
            {
	            pr.setIcon(tmpAPP.loadIcon(pm));  
	            pr.setName(tmpAPP.loadLabel(pm).toString());  
	            System.out.println(tmpAPP.loadLabel(pm).toString());  
	            list.add(pr);  
            }
        }  
        return list;  
    }  
    public List<ProfilingContent> getFileInfo(String fileName){
    	List<ProfilingContent> fileInfoList = new ArrayList<ProfilingContent>();
    	fileInfoList.clear();
		try {
			File file;
			file = new File(fileName);
			if(!file.exists()) 
				file.createNewFile();
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            if (fr == null || br == null)
            	return null;

            String line;
            String[] arrs;

            //format:   0			1			2			3				4			5			6		7				8			9
            //			processName	frequency	cpu_freq	screen_freq		wifi_freq	net_freq	accuCPU	accuBrightness	accuWIFI	accuNET
            while ((line = br.readLine()) != null) {
            	//br.readLine();
                arrs = line.split("\\t");
                if (arrs.length == 1 && arrs[0]=="")
                	continue;
                if (arrs.length !=10)
                	return null;
                ProfilingContent mWriteInfo = new  ProfilingContent();
                mWriteInfo.name = arrs[0];
                mWriteInfo.frequency = Integer.parseInt(arrs[1]);
                mWriteInfo.cpu_freq = Integer.parseInt(arrs[2]);
                mWriteInfo.screen_freq = Integer.parseInt(arrs[3]);
                mWriteInfo.wifi_freq = Integer.parseInt(arrs[4]);
                mWriteInfo.net_freq = Integer.parseInt(arrs[5]);
                mWriteInfo.cpu = Double.parseDouble(arrs[6]);
                mWriteInfo.brightness = Double.parseDouble(arrs[7]);
                mWriteInfo.wifi = Double.parseDouble(arrs[8]);
                mWriteInfo.net = Double.parseDouble(arrs[9]);
                fileInfoList.add(mWriteInfo);
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
		return fileInfoList;
	}
      
}  