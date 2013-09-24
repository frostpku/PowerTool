package os.lowpower.powertool;  

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import android.os.Build;
import android.util.Log;

public class VoltageReader {
	
	public String getVoltage()
	{
		File f = null;
		f = new File("/sys/class/power_supply/battery/voltage_now");
		if (f.exists())
		{
			return getValue(f);
		}
		return null;
	}
	public static String getValue(File _f) {
		
		String text = null;
		
		try {
			
		
			FileInputStream fs = new FileInputStream(_f);
			
			DataInputStream ds = new DataInputStream(fs);
		
			text =String.valueOf(ds.readLong());
			ds.close();		
			fs.close();	
			
		}
		catch (Exception ex) {
			Log.e("frost", ex.getMessage());
			ex.printStackTrace();
		}
		return text;
	}

}
