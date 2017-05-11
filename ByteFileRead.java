
package chromastarserver;

//import java.nio.file;
//import java.io.BufferedWriter;
import java.io.BufferedInputStream;
//import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.IOException;
//import java.nio.file.Files;  //java.nio.file not available in Java 6
//import java.nio.file.Paths;  //java.nio.file not available in Java 6
//import java.nio.file.Path;  //java.nio.file not available in Java 6
import java.io.File;
import java.nio.charset.Charset;
import java.text.*;
import java.text.DecimalFormat;

//Amended From http://www.deepakgaikwad.net/index.php/2009/11/23/reading-text-file-line-by-line-in-java-6.html
//Kluged from http://nadeausoftware.com/articles/2008/02/java_tip_how_read_files_quickly

public class ByteFileRead{

    public static byte[] readFileBytes(String byteFile, int arrSize){

        //String dataPath = "./InputData/";
        //String lineListFile = dataPath + "gsLineList.dat";

        byte[] barray = new byte[arrSize];

        int bufferSize = 8 * 1024;
        //int arrSize = barray.length;

        //BufferedReader buffReader = null;
        BufferedInputStream buffStreamer = null;
        try{
            //buffReader = new BufferedReader (new FileReader(lineListFile));
            buffStreamer = new BufferedInputStream (new FileInputStream(byteFile), bufferSize);
            //String line = buffReader.readLine();
            buffStreamer.read(barray, 0, arrSize); //, arrSize); 
        }catch(IOException ioe){
            ioe.printStackTrace();
        }finally{
            try{
                buffStreamer.close();
            }catch(IOException ioe1){
                //Leave It
            }
        }

      return barray;

    } //end writeFileBytes() method

} //end ByteFileWrite class
