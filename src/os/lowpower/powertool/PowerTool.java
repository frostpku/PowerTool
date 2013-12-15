package os.lowpower.powertool;


import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PowerTool extends Activity {

	private Button startTrainingBTN;
	private Button stopTrainingBTN;
	private Button DoModelingBTN;
	private Button AutoTrainingBTN;
	private Button startProfilingBTN;
	private Button stopProfilingBTN;
	private Button showApps;
	private Button predictApps;
	private Button autoBrightnessBTN;
	private boolean isTraining = false;
	private PowerModel myModel;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_power_tool);
		initComponents();
	}

	private void initComponents()
	{
		myModel = new PowerModel();
		this.startTrainingBTN = (Button)findViewById(R.id.button1);
        this.stopTrainingBTN = (Button)findViewById(R.id.button2);
        this.DoModelingBTN = (Button)findViewById(R.id.button3);
        this.AutoTrainingBTN = (Button)findViewById(R.id.button4);
        this.startProfilingBTN = (Button)findViewById(R.id.button5);
        this.stopProfilingBTN = (Button)findViewById(R.id.button6);
        this.showApps = (Button)findViewById(R.id.button7);
        this.predictApps = (Button)findViewById(R.id.button8);
        this.autoBrightnessBTN = (Button)findViewById(R.id.button9);
        this.startTrainingBTN.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View paramView)
            {
            	PowerTool.this.startService(new Intent(PowerTool.this, TrainingService.class));
          	  	Toast.makeText(PowerTool.this,  
                        "Training service started.",   
                        Toast.LENGTH_LONG).show(); 
          	  isTraining = true;
            }
          });
        this.stopTrainingBTN.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View paramView)
            {
            	PowerTool.this.stopService(new Intent(PowerTool.this, TrainingService.class));
          	  	Toast.makeText(PowerTool.this,  
                        "Training service killed.",   
                        Toast.LENGTH_LONG).show();  
          	  isTraining = false;
            }
          });
        this.DoModelingBTN.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View paramView)
            {
            	if (isTraining)
            	{
            		Toast.makeText(PowerTool.this,  
                        "Please stop the training service first.",   
                        Toast.LENGTH_LONG).show();  
            	}
            	else
            	{
            		setModeling();
            	}
            }
          });
        this.startProfilingBTN.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View paramView)
            {
            	PowerTool.this.startService(new Intent(PowerTool.this, ProfilingService.class));
          	  	Toast.makeText(PowerTool.this,  
                        "Profiling service started.",   
                        Toast.LENGTH_LONG).show();  
            }
          });
        this.stopProfilingBTN.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View paramView)
            {
            	PowerTool.this.stopService(new Intent(PowerTool.this, ProfilingService.class));
          	  	Toast.makeText(PowerTool.this,  
                        "Profiling service killed.",   
                        Toast.LENGTH_LONG).show();  
            }
          });
        this.showApps.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View paramView)
            {
            	Intent intent = new Intent();
        		intent.setClass(PowerTool.this, ActivityListMain.class);
        		PowerTool.this.startActivity(intent);
        		//PowerTool.this.finish();
            }
          });
        this.autoBrightnessBTN.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View paramView)
            {
            	int brightness = 0;
            	int MINIMUM_BACKLIGHT = 30;
            	WindowManager.LayoutParams lp = getWindow().getAttributes(); 
            	for(brightness = 0; brightness <=255; brightness +=20)
            	{
            		   
                	 lp.screenBrightness = (float)(brightness+MINIMUM_BACKLIGHT)/255;
                	 getWindow().setAttributes(lp);   
                	 Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS, brightness + MINIMUM_BACKLIGHT);
                	 try {
						Thread.sleep(20000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
            }
          });
        
	}
	private void setModeling()
    {
    	final AlertDialog.Builder dia = new AlertDialog.Builder(PowerTool.this);
    	//final EditText tempEditText = new EditText(PowerTool.this);
    	dia.setTitle("Please confirm");
    	//dia.setView(tempEditText);
    	dia.setMessage("Doing modeling might cause high CPU calculation, hence we suggest this be done while your phone is connected to charger.");
    	dia.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (myModel.DoModeling("/sdcard/APT_TrainingData.txt"))
				{
					Toast.makeText(PowerTool.this,  
	                        "Model generated successfully.",   
	                        Toast.LENGTH_LONG).show();  
				}
				else
				{
					Toast.makeText(PowerTool.this,  
	                        "Sorry! Error occured whiling do modeling. Please check whether the training data is generated successfully.",   
	                        Toast.LENGTH_LONG).show();  
				}
			} 
    		
    	});
    	dia.setNegativeButton("Cancel", null);
    	dia.show();
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.power_tool, menu);
		return true;
	}

}
