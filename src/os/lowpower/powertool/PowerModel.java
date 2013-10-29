package os.lowpower.powertool;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

import android.os.Environment;

public class PowerModel{
    private String trainDataFileName;
    //you can decide the file format by yourself. i would generate file with your format

    private HashMap<String, Double> powerParams = new HashMap<String, Double>();
    //<key, value> example: <"wifi", 4.5>
    //All keys: "wifi" "cpu" "screen" "3g" "audio" "constant"
    
    private double param_screen_none_linear = 0.0f;
    PowerDataIO modelIO;
    private String[] keyName = {"wifi", "cpu", "screen", "3g", "audio", "constant"};
    //IO??
    public PowerModel()
    {
        powerParams.clear();
        trainDataFileName = "";
        try {
			modelIO = new PowerDataIO("/sdcard/" ,"APT_Model.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    

    private boolean calculateParams()
    {
        int paramNum = keyName.length -1;
        //int maxDataNum = 604800; // 3600*24*7
        int maxDataNum = 86400;
        

        double[] deltaVotage = new double[maxDataNum];
        double[][] data = new double[paramNum][maxDataNum];

        int dataNum = 0;
        try {
            FileReader f = new FileReader(trainDataFileName);
            BufferedReader br = new BufferedReader(f);

            String line;
            String[] arrs;
            long lastTime = 0;
            long startVoltage =0;
            while ((line = br.readLine()) != null) {
            	br.readLine();
                arrs = line.split("\\t");
                int i;
                
                if (Long.valueOf(arrs[0]) -lastTime >=100)
                {
                	startVoltage =Long.valueOf(arrs[paramNum+1]);
                	lastTime = Long.valueOf(arrs[0]);
                	continue;
                }
              //adjacent events' time should be less than 5 seconds, otherwise regarded as invalid events.
                if (Long.valueOf(arrs[0]) -lastTime <=5)
                {
                	lastTime = Long.valueOf(arrs[0]);
                	Long deltaV = Long.valueOf(arrs[paramNum+1]) - startVoltage;
                	//if (deltaV >= 8000 || deltaV <= -8000)
                	//{
                	//	continue;
                	//}
                	for (i = 1; i <= paramNum; i++) 
                	{
                		data[i-1][dataNum] = Double.valueOf(arrs[i]);
                	}
                	deltaVotage[dataNum] = deltaV;
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
        
        //for testing, delete in final version!!!
        PowerDataIO filteredDataIO;
        DecimalFormat dcmFmt = new DecimalFormat("0.00");
        try {
        	filteredDataIO = new PowerDataIO("/sdcard/","APT_FilterDATA.txt");
        	filteredDataIO.DataIntoSD("============");
        	for (int i = 0 ; i < dataNum; i++)
            {
        		String tmp="";
        		for (int j = 0; j < paramNum; j++)
        		{
        			tmp += String.valueOf(data[j][i]) + "\t";
        		}
        		tmp+=deltaVotage[i];
        		filteredDataIO.DataIntoSD(tmp);
            }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        

        boolean[] isZero = new boolean[paramNum];
        double[][] newData = new double[paramNum][maxDataNum];
        int p = 0;
        for (int i = 0; i < paramNum; i++) {
            isZero[i] = true;
            for (int j = 1; j < dataNum; j++) {
                if (data[i][j] != data[i][0]) {
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
    public boolean isModelValid()
    {
    	for (int i = 0; i < keyName.length; i++)
    	{
    		if (powerParams.get(keyName[i]) == null)
    			return false;
    	}
    	return true;
    }
    public boolean DoModeling(String fileName)
    {
    	trainDataFileName = fileName;
    	DecimalFormat dcmFmt = new DecimalFormat("0.00000");
    	if (calculateParams())
    	{
    		String aim="";
    		for (String str:powerParams.keySet())
    		{
    			aim+=str+'\t'+dcmFmt.format(powerParams.get(str))+'\t';
    		}
    		try {
				modelIO.DataIntoSD(aim);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		return true;
    	}
    	return false;
    }
}
