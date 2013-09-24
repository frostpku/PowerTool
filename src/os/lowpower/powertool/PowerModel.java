package os.lowpower.powertool;

import java.util.Map;

public class PowerModel{
	private String trainDataFileName;
	private Map<String, Double> powerParams;
	/*<key, value> example: <"wifi", 4.5>
	  All keys:  "wifi"	"cpu"	"screen"	"3g"	"2g"	"audio"
	*/
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
	public Map<String, Double> getParams(String fileName)
	{
		return null;
	}
	
}