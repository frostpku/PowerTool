package os.lowpower.powertool;

import java.io.*;
import java.util.*;

public class PowerModel{
    private String trainDataFileName;
    //you can decide the file format by yourself. i would generate file with your format

    private HashMap<String, Double> powerParams = new HashMap<String, Double>();
    //<key, value> example: <"wifi", 4.5>
    //All keys: "wifi" "cpu" "screen" "3g" "audio" "constant"
    
    private double param_screen_none_linear = 0.0f;
    private String[] keyName = {"wifi", "cpu", "screen", "3g", "audio", "constant"};
    public PowerModel()
    {
        powerParams.clear();
        trainDataFileName = "";
    }

    

    private boolean DoModeling()
    {
        int paramNum = keyName.length -1;
        int maxDataNum = 604800; // 3600*24*7
        

        double[] deltaVotage = new double[maxDataNum];
        double[][] data = new double[paramNum][maxDataNum];

        int dataNum = 0;
        try {
            FileReader f = new FileReader(trainDataFileName);
            BufferedReader br = new BufferedReader(f);

            String line;
            String[] arrs;
            long lastTime = 0;
            while ((line = br.readLine()) != null) {
                arrs = line.split("\\t");
                int i;
                //adjacent events' time should be less than 1 seconds, otherwise regarded as invalid events.
                if (Long.valueOf(arrs[0]) -lastTime ==1)
                {
                	for (i = 1; i <= paramNum; i++) 
                	{
                		data[i-1][dataNum] = Double.valueOf(arrs[i]);
                	}
                	lastTime = Long.valueOf(arrs[0]);
                	deltaVotage[dataNum] = Double.valueOf(arrs[i+1]);
                    dataNum++;
                }
                
            }

            f.close();
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        boolean[] isZero = new boolean[paramNum];
        double[][] newData = new double[paramNum][maxDataNum];
        int p = 0;
        for (int i = 0; i < paramNum; i++) {
            isZero[i] = true;
            for (int j = 0; j < dataNum; j++) {
                if (data[i][j] != 0) {
                    isZero[i] = false;
                    break;
                }
            }
            if (!isZero[i]) {
                for (int j = 0; j < dataNum; j++) {
                    newData[p][j] = data[i][j];
                }
                p++;
            }
        }

        double[] a = new double[p + 1];
        double[] v = new double[p];
        double[] dt = new double[4];
        Regression.sqtn(newData, deltaVotage, p, dataNum, a, dt, v);

        int q = 0;
        for (int i = 0; i < paramNum; i++) {
            if (isZero[i])
                powerParams.put(keyName[i], 0.0);
            else
                powerParams.put(keyName[i], a[q++]);
        }
        powerParams.put(keyName[paramNum], a[q]);

       // for (int i = 0; i < paramNum + 1; i++) {
        //    System.out.println(keyName[i] + " " + powerParams.get(keyName[i]));
        //}

        return true;
    }

  //to check whether this instance is valid, which means all parameters have been generated.
    public boolean isValid()
    {
    	for (int i = 0; i < keyName.length; i++)
    	{
    		if (powerParams.get(keyName[i]) == null)
    			return false;
    	}
    	return true;
    }
    public Map<String, Double> calculateParams(String fileName)
    {
    	trainDataFileName = fileName;
    	if (DoModeling())
    		return powerParams;
    	else return null;
    }
}
