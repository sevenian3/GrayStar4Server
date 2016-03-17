
package graystar3server;

//import java.nio.file;
//import java.io.BufferedWriter;
import java.io.BufferedReader;
//import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
//import java.nio.file.Files;  //java.nio.file not available in Java 6
//import java.nio.file.Paths;  //java.nio.file not available in Java 6
//import java.nio.file.Path;  //java.nio.file not available in Java 6
import java.io.File;
import java.nio.charset.Charset;
import java.text.*;
import java.text.DecimalFormat;

//From http://www.deepakgaikwad.net/index.php/2009/11/23/reading-text-file-line-by-line-in-java-6.html

public class FileLineByLine {

    public static String readFileLineByLine(String lineListFile){

        //String dataPath = "./InputData/";
        //String lineListFile = dataPath + "gsLineList.dat";

        //Put entire line list into one big string - we'll sort it out later
        String masterLineString = ""; //initialize
        String splitChar = "%%"; //character separating new lines

        BufferedReader buffReader = null;
        try{
            buffReader = new BufferedReader (new FileReader(lineListFile));
            String line = buffReader.readLine();
            masterLineString = masterLineString + line;
            while(line != null){
                //System.out.println(line);
                line = buffReader.readLine();
                masterLineString = masterLineString + splitChar + line;
            }
        }catch(IOException ioe){
            ioe.printStackTrace();
        }finally{
            try{
                buffReader.close();
            }catch(IOException ioe1){
                //Leave It
            }
        }

    return masterLineString;

    } //end readFileLineByLine() method

} //end FileLineByLine class
