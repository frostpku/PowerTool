package os.lowpower.powertool;  

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import android.os.Build;
import android.util.Log;

public class VoltageReader {
	
	public long getVoltage()
	{
		File f = null;
		f = new File("/sys/class/power_supply/battery/voltage_now");
		if (f.exists())
		{
			return getValue(f);
		}
		return -1;
	}
	public static long getValue(File _f) {
		
		Long result = null;
		
		try {
			
		
			FileInputStream fs = new FileInputStream(_f);
			
			DataInputStream ds = new DataInputStream(fs);
		
			result = ds.readLong();
			ds.close();		
			fs.close();	
			
		}
		catch (Exception ex) {
			Log.e("frost", ex.getMessage());
			ex.printStackTrace();
		}
		return result;
	}

}
