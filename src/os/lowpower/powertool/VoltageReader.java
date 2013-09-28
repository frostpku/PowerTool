package os.lowpower.powertool;  

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import android.os.Build;
import android.util.Log;

public class VoltageReader {
	
	public int getVoltage()
	{
		File f = null;
		f = new File("/sys/class/power_supply/battery/voltage_now");
		if (f.exists())
		{
			return getValue(f);
		}
		return -1;
	}
	public static int getValue(File _f) {
		
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
		return getTop7(result);
	}
	private static int getTop7(long x)
	{
		String tmp= String.valueOf(x);
		int ans = Integer.valueOf(tmp.substring(0, 7));
		return ans;
		//3712345
	}

}
