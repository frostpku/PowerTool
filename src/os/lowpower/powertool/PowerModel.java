package os.lowpower.powertool;

import java.util.Map;

public class PowerModel{
	private String trainDataFileName;
	//you can decide the file format by yourself. i would generate file with your format
	private Map<String, Double> powerParams;
	//<key, value> example: <"wifi", 4.5>
	//All keys:  "wifi"	"cpu"	"screen"	"3g"	"2g"	"audio"	"constant"
	
	public PowerModel()
	{
		powerParams.clear();
		trainDataFileName = "";
	}
	//to check whether this instance is valid, which means all parameters have been generated.
	public boolean isValid()
	{
		return false;
	}
	public boolean setFileName(String fileName)
	{
		trainDataFileName = fileName;
		return true;
	}
	public boolean DoModeling()
	{
		return false;
	}
	public Map<String, Double> getParams()
	{
		return null;
	}
	
}