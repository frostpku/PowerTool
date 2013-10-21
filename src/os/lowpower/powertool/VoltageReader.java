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
		String text = null;
		try {
			
		
			FileInputStream fs = new FileInputStream(_f);
			
			DataInputStream ds = new DataInputStream(fs);
		
			text = ds.readLine();
			ds.readLine();
			
			ds.close();		
			fs.close();	
			
		}
		catch (Exception ex) {
			Log.e("frost", ex.getMessage());
			ex.printStackTrace();
		}
		Long value = null;
		
		if (text != null)
		{
			try
			{
				value = Long.parseLong(text);
			}
			catch (NumberFormatException nfe)
			{
				Log.e("CurrentWidget", nfe.getMessage());
				value = null;
			}

		}
		
		return value;
		//return getTop7(result);
	}
	private static int getTop7(long x)
	{
		String tmp= String.valueOf(x);
		int ans = Integer.valueOf(tmp.substring(0, 7));
		return ans;
		//3712345
	}

}
