package os.lowpower.powertool;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TaskDetail extends Activity{
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
	DecimalFormat df;
	RelativeLayout relativeLayout ;
	private double energy_cpu;
 	private double energy_screen;
 	private double energy_wifi;
 	private double energy_net;
 	private double energy_other;
 	
 	private String[] keyName = {"wifi", "cpu", "screen", "net", "audio", "constant"};
 	HashMap<String, Double> params;
	public void onCreate(Bundle savedInstanceState) {  
		df = new DecimalFormat();
    	df.setMaximumFractionDigits(2);
    	df.setMinimumFractionDigits(2);
    	
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.task_detail);
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativelayout1);
        rl.setBackgroundColor(Color.WHITE);
        
        params = new HashMap<String, Double>();
        params.clear();
        
        Intent intent=getIntent(); 
        name = intent.getStringExtra("name");
        cpu = intent.getDoubleExtra("cpu", 0.0);
        brightness= intent.getDoubleExtra("brightness", 0.0);
        wifi = intent.getDoubleExtra("wifi", 0.0);
        net = intent.getDoubleExtra("net", 0.0);
        frequency = intent.getIntExtra("frequency", 0);
        cpu_freq = intent.getIntExtra("cpu_freq", 0);
        screen_freq = intent.getIntExtra("screen_freq", 0);
        wifi_freq = intent.getIntExtra("wifi_freq", 0);
        net_freq = intent.getIntExtra("net_freq", 0);
        
        setTitle(name);
        if (!getModelParams("/sdcard/APT_Model.txt"))
        {
        	Toast.makeText(TaskDetail.this,  
                    "Sorry, the model data is invalid.",   
                    Toast.LENGTH_LONG).show();
        	return;
        }
        calculateEnergy();
        
        List<Double> listData = new ArrayList<Double>();
        listData.add(energy_cpu);
        listData.add(energy_screen);
        listData.add(energy_wifi);
        listData.add(energy_net);
        
        List<String> listName = new ArrayList<String>();
        listName.add("cpu");
        listName.add("screen");
        listName.add("wifi");
        listName.add("net");
        CircleView view = new CircleView(TaskDetail.this);
        view.initData(listData);
        view.initName(listName);
        view.setClickable(true);
        view.setLongClickable(true);
        
        TextView tv1 = new TextView(TaskDetail.this);
        tv1.setText("Usage information"+
        			"\n\tCPU average usage:\t"+df.format((cpu_freq==0)?0:100*cpu/cpu_freq)+"%" +
        			"\n\tScreen average brightness:\t"+ df.format((screen_freq==0)?0:brightness/screen_freq) +
        			"\n\tWIFI average speed:\t"+df.format((wifi_freq==0)?0:wifi/(wifi_freq*1024))+"\tkbps" +
        			"\n\tNET average speed:\t"+ df.format((net_freq==0)?0:net/(net_freq * 1024))+"\tkbps");
        tv1.setTextSize(20);
        relativeLayout = (RelativeLayout) this.findViewById(R.id.view);
        relativeLayout.addView(view);
        
        relativeLayout.addView(tv1);

        
        CenterView cv = new CenterView(TaskDetail.this);
        cv.setSum(view.getSum());
        relativeLayout.addView(cv);
       
        ListView lv = new ListView(this);
        lv.getSelectedItemPosition();
	}
	private boolean getModelParams(String name){
		 FileReader f;
		try {
			f = new FileReader(name);
			BufferedReader br = new BufferedReader(f);
			if (f== null || br==null)
				return false;
	        String line;
	        String[] arrs;
	        while ((line = br.readLine()) != null) {
                arrs = line.split("\\t");
                if (arrs.length == 1 && arrs[0]=="")
                	continue;
                if (arrs.length !=2)
                	return false;
                params.put(arrs[0], Double.valueOf(arrs[1]));
	        }
	        f.close();
            br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
         
	}
	private void calculateEnergy(){
		energy_cpu = params.get("cpu") * cpu* cpu_freq;
		energy_screen = params.get("screen") * brightness * screen_freq;
		energy_wifi = params.get("wifi") * wifi* wifi_freq;
		energy_net = params.get("net") * net* net_freq;
	}
}