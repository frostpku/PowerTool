package os.lowpower.powertool;


import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class PowerTool extends Activity {

	private Button startTrainingBTN;
	private Button stopTrainingBTN;
	private Button DoModelingBTN;
	private Button AutoTrainingBTN;
	private Button startProfilingBTN;
	private Button stopProfilingBTN;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_power_tool);
		initComponents();
	}

	private void initComponents()
	{
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
            }
          });
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.power_tool, menu);
		return true;
	}

}
