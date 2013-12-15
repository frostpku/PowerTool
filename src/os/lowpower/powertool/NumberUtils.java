package os.lowpower.powertool;

import java.text.DecimalFormat;

public class NumberUtils {
 /**
  * ����С����2λ
  * @param d
  * @return
  */
 public static String getDecimal(double d){
  DecimalFormat df2  = new DecimalFormat("0.00");  
  return df2.format(d);
 }
}