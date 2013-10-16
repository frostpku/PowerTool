package os.lowpower.powertool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;

import java.io.FileWriter;
import java.io.IOException;

import java.util.Map;



public class PowerDataIO
{
	private String Path = "/sdcard/";
	private String FileName = "APT_Data.txt";
	private File file;
	private FileWriter fw;
	private FileReader fr;

	private BufferedWriter bw;
	private BufferedReader br;
	public Map<String, Integer> processMap;
	public PowerDataIO() throws IOException
	{
		//processMap = new HashMap<String, Integer>();
		file = new File(Path + FileName);
		if(!file.exists()) 
			file.createNewFile();
	}
	public PowerDataIO(String iPath, String iName) throws IOException
	{
		Path = iPath;
		FileName = iName;
		file = new File(Path + FileName);
		if(!file.exists()) 
			file.createNewFile();
	}

	public void deleteData() throws IOException
	{
		fw = new FileWriter(file);
		bw  = new BufferedWriter(fw);
		bw.write("");
		bw.close();
		fw.close();
	}
	public void DataIntoSD(String str) throws IOException {
		fw = new FileWriter(file,true);
		bw = new BufferedWriter(fw);
		
		bw.write(str);
		bw.write(13);
		bw.write(10);

		bw.close();
		fw.close();
	}
	public void DataOverwrittenIntoSD(String str)throws IOException {
		fw = new FileWriter(file,false);
		//fw = new FileWriter(file,true);
		bw = new BufferedWriter(fw);
		
		bw.write(str);
		bw.write(13);
		bw.write(10);

		bw.close();
		fw.close();
	}
	public String DataFromSD() throws IOException{
		fr = new FileReader(file);
		br=new BufferedReader(fr);
		String str="";
		String res="";
		try {
			while((str=br.readLine())!=null){
				res+=str;
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
}