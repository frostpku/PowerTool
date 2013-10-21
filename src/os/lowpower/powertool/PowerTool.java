package os.lowpower.powertool;


import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
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
				if (myModel.DoModeling("/sdcard/APT_Data.txt"))
				{
					Toast.makeText(PowerTool.this,  
	                        "Model generated successfully.",   
	                        Toast.LENGTH_LONG).show();  
				}
				else
				{
					Toast.makeText(PowerTool.this,  
	                        "Sorry! Error occured whiling do modeling.",   
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
