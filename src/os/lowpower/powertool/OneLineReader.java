package os.lowpower.powertool;  

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import android.util.Log;

public class OneLineReader {

	/*private File _f = null;
	private boolean _convertToMillis = false;
	
	public OneLineReader(File f, boolean convertToMillis) {
		_f = f;
		_convertToMillis = convertToMillis;
	}*/
	
	public static Long getValue(File _f, boolean _convertToMillis) {
		
		String text = null;
		
		try {
			
		
			FileInputStream fs = new FileInputStream(_f);
			
			DataInputStream ds = new DataInputStream(fs);
		
			text = ds.readLine();
			
			ds.close();		
			fs.close();	
			
		}
		catch (Exception ex) {
			Log.e("CurrentWidget", ex.getMessage());
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
			
			if (_convertToMillis && value != null)
				//value = value/1000; // convert to milliampere
				;
		}
		
		return value;
	}

}
