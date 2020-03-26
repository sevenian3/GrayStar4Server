/*
 * The MIT License (MIT)
 * Copyright (c) 2016 C. Ian Short 
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* Main developers:
 *
 * C. Ian Short - Saint Mary's University
 * Philip D. Bennett (GAS package) - Saint Mary's University
 *
 * Students:
 * Jason H. T. Bayer 
 * Lindsey M. Burns
 *
 */  

package chromastarserver;

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

/**
 *
 * @author Ian
 */
public class ChromaStarGasServer {


//Public static (ie. global) variable declaration and set-up for Phil Bennett's GAS 
//chemical-equilibrium/ionizatio-equilibrium/EOS package:

public static String[] name = new String[150];
public static int[] ipr = new int[150];
public static int[] nch = new int[150];
public static int[] nel = new int[150];
public static int[][] nat = new int[5][150];
public static int[][] zat = new int[5][150];
public static double[] ip = new double[150];
public static double[] comp = new double[40];
public static double[] awt = new double[150];

public static double[][] logk = new double[5][150];
public static double[] logwt = new double[150];

public static int[] ntot = new int[150];
public static int[] neut = new int[150];
public static int[] idel = new int[150];
public static int[] natsp = new int[150];
public static int[][] iatsp = new int[40][40];

public static int[][][][][] indx = makeIndx();

public static int[][][][][] makeIndx(){

   int[][][][][] indx = new int[4][26][7][5][2];
   for (int i = 0; i < 4; i++){
       for (int j = 0; j < 26; j++){
           for (int k = 0; k < 7; k++){
               for (int l = 0; l < 5; l++){
                  for (int m = 0; m < 2; m++){
                     indx[i][j][k][l][m] = 149;
                  }
               }
           }
       }
   }

   return indx;

} //end method makeIndx

public static int[] iat = makeIat();
public static int[] indsp = makeIndsp();
public static int[] indzat = makeIndzat();

//Initialization:

public static int[] makeIat(){
   int[] iat = new int[150];
   for (int i = 0; i < 150; i++){
      iat[i] = 39;
   }
   return iat;
}//end method

public static int[] makeIndsp(){
    int[] indsp = new int[40];
    for (int i = 0; i < 40; i++){
       indsp[i] = 149;
    }
    return indsp;
} //end method

public static int[] makeIndzat(){
    int[] indzat = new int[100];
    for (int i = 0; i < 100; i++){
       indzat[i] = 39;
    }
    return indzat;
} //end method


public static int[] ixn = new int[70];


public static int[] lin1 = new int[40];
public static int[] lin2 = new int[40];
public static int[] linv1 = new int[40];
public static int[] linv2 = new int[40];

public static int nspec;
public static int natom;   //#neutral atomic species counter
public static int nlin1;
public static int nlin2;

public static int[] type0 = new int[150];

/*
 *
 *
 */

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // Argument 0: Effective temperature, Teff, in K:
        String teffStr = args[0];
        //String teffStr = "5870"; //test
        double teff = (Double.valueOf(teffStr)).doubleValue();

        // Argument 1: Logarithmic surface gravity, g, in cm/s/s:
        String loggStr = args[1];
        //String loggStr = "4.4"; //test
        double logg = (Double.valueOf(loggStr)).doubleValue();

        //Argument 2: Linear sclae factor for solar Rosseland oapcity distribution
        // mimics "metallicity" parameter - ??  (unitless)
        String logZStr = args[2];
        //String logKappaStr = "0.0"; //test
        double log10ZScale = (Double.valueOf(logZStr)).doubleValue();

        //Argument 3: Stellar mass, M, in solar masses
        String massStarStr = args[3];
        //String massStarStr = "1.0"; //test
        double massStar = (Double.valueOf(massStarStr)).doubleValue();

        // Sanity check:
        double F0Vtemp = 7300.0;  // Teff of F0 V star (K)                           
        if (teff < 500.0) {
            teff = 500.0;
            teffStr = "500";
        }
        if (teff > 50000.0) {
            teff = 50000.0;
            teffStr = "50000";
        }
        //logg limit is strongly Teff-dependent:
        double minLogg = 3.0; //safe initialization
        String minLoggStr = "3.0";
        if (teff <= 4000.0) {
            minLogg = 0.0;
            minLoggStr = "0.0";
        } else if ((teff > 4000.0) && (teff <= 5000.0)) {
            minLogg = 0.5;
            minLoggStr = "0.5";
        } else if ((teff > 5000.0) && (teff <= 6000.0)) {
            minLogg = 1.5;
            minLoggStr = "1.5";
        } else if ((teff > 6000.0) && (teff <= 7000.0)) {
            minLogg = 2.0;
            minLoggStr = "2.0";
        } else if ((teff > 7000.0) && (teff < 9000.0)) {
            minLogg = 2.5;
            minLoggStr = "2.5";
        } else if (teff >= 9000.0) {
            minLogg = 3.0;
            minLoggStr = "3.0";
        }
        if (logg < minLogg) {
            logg = minLogg;
            loggStr = minLoggStr;
        }
        if (logg > 7.0) {
            logg = 7.0;
            loggStr = "7.0";
        }
        if (log10ZScale < -3.0) {
            log10ZScale = -3.0;
            logZStr = "-3.0";
        }
        if (log10ZScale > 1.0) {
            log10ZScale = 1.0;
            logZStr = "1.0";
        }
        if (massStar < 0.1) {
            massStar = 0.1;
            massStarStr = "0.1";
        }
        if (massStar > 20.0) {
            massStar = 20.0;
            massStarStr = "20.0";
        }

        double grav = Math.pow(10.0, logg);
        double zScale = Math.pow(10.0, log10ZScale);

        // Argument 5: microturbulence, xi_T, in km/s:
           String xitStr = args[4];
           //String xitStr = "1.0"; //test
           double xiT = (Double.valueOf(xitStr)).doubleValue();

        if (xiT < 0.0) {
            xiT = 0.0;
            xitStr = "0.0";
        }
        if (xiT > 8.0) {
            xiT = 8.0;
            xitStr = "8.0";
        }

        // Argument 6: minimum ratio of monochromatic line center to background continuous
        // extinction for inclusion of linein spectrum 
           String lineThreshStr = args[5];
           //String xitStr = "1.0"; //test
           double lineThresh = (Double.valueOf(lineThreshStr)).doubleValue();

        if (lineThresh < -4.0) {
            lineThresh = -4.0;
            lineThreshStr = "-4.0";
        }
        if (lineThresh > 6.0) {
            lineThresh = 6.0;
            lineThreshStr = "6.0";
        }

        // Argument 7: minimum ratio of monochromatic line center to background continuous
        // extinction for treatmnt with Voigt profile 
           String voigtThreshStr = args[6];
           //String xitStr = "1.0"; //test
           double voigtThresh = (Double.valueOf(voigtThreshStr)).doubleValue();

        if (voigtThresh < lineThresh) {
            voigtThresh = lineThresh;
            voigtThreshStr = lineThreshStr;
        }
        if (voigtThresh > 6.0) {
            voigtThresh = 6.0;
            voigtThreshStr = "6.0";
        }

//User defined spetrum synthesis region:
        double lamUV = 260.0;
        //double lamUV = 100.0;
        double lamIR = 2600.0;
        //// Ca II K to H-alpha
        //double lamUV = 390.0;  //testing
        //double lamIR = 660.0;  //testing
        // Argument 8: starting wavelength for spectrum synthesis 
           String lambdaStartStr = args[7];
           //String xitStr = "1.0"; //test
           double lambdaStart = (Double.valueOf(lambdaStartStr)).doubleValue();

        if (lambdaStart < lamUV) {
            lambdaStart = lamUV;
            lambdaStartStr = String.valueOf(lamUV);
        }
        if (lambdaStart > lamIR - 1.0) {
            lambdaStart = lamIR - 1.0;
            lambdaStartStr = String.valueOf(lamIR - 1.0);
        }
        // Argument 9: stopping wavelength for spectrum synthesis 
           String lambdaStopStr = args[8];
           //String xitStr = "1.0"; //test
           double lambdaStop = (Double.valueOf(lambdaStopStr)).doubleValue();

        if (lambdaStop < lamUV + 1.0) {
            lambdaStop = lamUV + 1.0;
            lambdaStartStr = String.valueOf(lamUV + 1.0);
        }
        if (lambdaStart > lamIR) {
            lambdaStart = lamIR;
            lambdaStartStr = String.valueOf(lamIR);
        }
        lambdaStart = 1.0e-7 * lambdaStart; //nm to cm 
        lambdaStop = 1.0e-7 * lambdaStop;  //nm to cm
        lamUV = 1.0e-7 * lamUV;
        lamIR = 1.0e-7 * lamIR;
       //double lineThresh = +5.0; 

//argument 10: line sampling selection (fine or coarse)
        String sampling = args[9];

// Argument 11: Lorentzian line broadening enhancement 
           String logGammaColStr = args[10];
           double logGammaCol = (Double.valueOf(logGammaColStr)).doubleValue();

        if (logGammaCol < 0.0) {
            logGammaCol = 0.0;
            logGammaColStr = "0.0";
        }
        if (logGammaCol > 1.0) {
            logGammaCol = 1.0;
            logGammaColStr = "1.0";
        }

// Argument 12: log_10 gray mass extinction fudge 
           String logKapFudgeStr = args[11];
           double logKapFudge = (Double.valueOf(logKapFudgeStr)).doubleValue();

        if (logKapFudge < -2.0) {
            logKapFudge = -2.0;
            logKapFudgeStr = "-2.0";
        }
        if (logKapFudge > 2.0) {
            logKapFudge = 2.0;
            logKapFudgeStr = "2.0";
        }

// Argument 13: macroturbulent velocity broadening parameter (sigma) (km/s) 
        String macroVStr = args[12];
        double macroV = (Double.valueOf(macroVStr)).doubleValue();
// Argument 14: surface equatorial linear rotational velocity (km/s) 
        String rotVStr = args[13];
        double rotV  = (Double.valueOf(rotVStr)).doubleValue();
// Argument 15: inclination of rotation axis wrt line-of-sight (degrees) 
        String rotIStr = args[14];
        double rotI  = (Double.valueOf(rotIStr)).doubleValue();

// Argument 16: number of outer HSE-EOS-Opac iterations
        String nOuterIterStr = args[15];
        int nOuterIter = (Integer.valueOf(nOuterIterStr)).intValue();
// Argument 17: number of inner Pe-IonFrac iterations
        String nInnerIterStr = args[16];
        int nInnerIter = (Integer.valueOf(nInnerIterStr)).intValue();
//Argument 18: If TiO JOLA bands should be included:
        String ifTiOStr = args[17];
        int ifTiO = (Integer.valueOf(ifTiOStr)).intValue();
        //int ifTiO = 1; //test
//Argument 19 Helium abundance [He/Fe]:
        String logHeFeStr = args[18];
        double logHeFe = (Double.valueOf(logHeFeStr)).doubleValue();
//Argument 20: Carbon-to-Oxygen abundance ratio:
        String logCOStr = args[19];
        double logCO = (Double.valueOf(logCOStr)).doubleValue();
//Argument 21: alpha enhancement:
        String logAlphaFeStr = args[20];
        double logAlphaFe = (Double.valueOf(logAlphaFeStr)).doubleValue();


//
        if (macroV < 0.0) {
            macroV = 0.0;
            macroVStr = "0.0";
        }
        if (macroV > 8.0) {
            macroV = 8.0;
            macroVStr = "8.0";
        }

        if (rotV < 0.0) {
            rotV = 0.0;
            rotVStr = "0.0";
        }
        if (rotV > 20.0) {
            rotV = 20.0;
            rotVStr = "20.0";
        }

        if (rotI < 0.0) {
            rotI = 0.0;
            rotIStr = "0.0";
        }
        if (rotI > 90.0) {
            rotI = 90.0;
            rotIStr = "90.0";
        }


        if (nOuterIter < 1) {
            nOuterIter = 1;
            nOuterIterStr = "1";
        }
        if (nOuterIter > 12) {
            nOuterIter = 12;
            nOuterIterStr = "12";
        }


        if (nInnerIter < 1) {
            nInnerIter = 1;
            nInnerIterStr = "1";
        }
        if (nInnerIter > 12) {
            nInnerIter = 12;
            nInnerIterStr = "12";
        }

// For new metallicity commands lburns
// For logHeFe: (lburns)
    if (logHeFe < -1.0) {
        logHeFe = -1.0;
        logHeFeStr = "-1.0";
    }
    if (logHeFe > 1.0) {
        logHeFe = 1.0;
        logHeFeStr = "1.0";
    }

// For logCO: (lburns)
    if (logCO < -2.0) {
        logCO = -2.0;
        logCOStr = "-2.0";
    }
    if (logCO > 2.0) {
        logCO = 2.0;
        logCOStr = "2.0";
    }

// For logAlphaFe: (lburns)
    if (logAlphaFe < -0.5) {
        logAlphaFe = -0.5;
        logAlphaFeStr = "-0.5";
    }
    if (logAlphaFe > 0.5) {
        logAlphaFe = 0.5;
        logAlphaFeStr = "0.5";
    }


//For rotation:
    double inclntn = Math.PI * rotI / 180;  //degrees to radians
    double vsini = rotV * Math.sin(inclntn);
//

//

//
// ************************ 
//
//  OPACITY  PROBLEM #1 - logFudgeTune:  late type star coninuous oapcity needs to have by multiplied 
//  by 10.0^0.5 = 3.0 for T_kin(tau~1) to fall around Teff and SED to look like B_lmabda(Trad=Teff).
//   - related to Opacity problem #2 in LineKappa.lineKap() - ??
//
  double logFudgeTune = 0.0;
  //sigh - don't ask me - makes the Balmer lines show up around A0:
      if (teff <= F0Vtemp){
          logFudgeTune = 0.5;
      }
      if (teff > F0Vtemp){
          logFudgeTune = 0.0;
      }

   double logTotalFudge = logKapFudge + logFudgeTune;

        double logE = Math.log10(Math.E); // for debug output
        double logE10 = Math.log(10.0); //natural log of 10

        //Gray structure and Voigt line code code begins here:
// Initial set-up:
        // optical depth grid
        int numDeps = 48;
        double log10MinDepth = -6.0;
        double log10MaxDepth = 2.0;
        //int numThetas = 10;  // Guess

        //wavelength grid (cm):
        double[] lamSetup = new double[3];
        lamSetup[0] = 260.0 * 1.0e-7;  // test Start wavelength, cm
        //lamSetup[0] = 100.0 * 1.0e-7;  // test Start wavelength, cm
        lamSetup[1] = 2600.0 * 1.0e-7; // test End wavelength, cm
        lamSetup[2] = 350;  // test number of lambda
        //int numLams = (int) (( lamSetup[1] - lamSetup[0] ) / lamSetup[2]) + 1;  
        int numLams = (int) lamSetup[2];

        //CONTINUUM lambda scale (nm)
        double lambdaScale[] = LamGrid.lamgrid(numLams, lamSetup); //cm

// Solar parameters:
        double teffSun = 5778.0;
        double loggSun = 4.44;
        double gravSun = Math.pow(10.0, loggSun);
        double log10ZScaleSun = 0.0;
        double zScaleSun = Math.exp(log10ZScaleSun);

//Solar units:
        double massSun = 1.0;
        double radiusSun = 1.0;
        //double massStar = 1.0; //solar masses // test
        double logRadius = 0.5 * (Math.log(massStar) + Math.log(gravSun) - Math.log(grav));
        double radius = Math.exp(logRadius); //solar radii
        //double radius = Math.sqrt(massStar * gravSun / grav); // solar radii
        double logLum = 2.0 * Math.log(radius) + 4.0 * Math.log(teff / teffSun);
        double bolLum = Math.exp(logLum); // L_Bol in solar luminosities 
     //cgs units:
        double rSun = 6.955e10; // solar radii to cm

        double cgsRadius = radius * rSun;
        double omegaSini = (1.0e5 * vsini) / cgsRadius; // projected rotation rate in 1/sec
        double macroVkm = macroV * 1.0e5;  //km/s to cm/s

        //Composition by mass fraction - needed for opacity approximations
        //   and interior structure
        double massX = 0.70; //Hydrogen
        double massY = 0.28; //Helium
        double massZSun = 0.02; // "metals"
        double massZ = massZSun * zScale; //approximation


        //double logNH = 17.0;

//
////Detailed checmical composition:
//Abundance table adapted from PHOENIX V. 15 input bash file
// Grevesse Asplund et al 2010
//Solar abundances:
// c='abundances, Anders & Grevesse',

  int nelemAbnd = 41;
  int numStages = 7;
  int[] nome = new int[nelemAbnd];
  double[] eheu = new double[nelemAbnd]; //log_10 "A_12" values
  double[] logAz = new double[nelemAbnd]; //N_z/H_H for element z
  double[][] logNz = new double[nelemAbnd][numDeps]; //N_z for element z
  double[] logNH = new double[numDeps];
  double[][][] masterStagePops = new double[nelemAbnd][numStages][numDeps];
  String[] cname = new String[nelemAbnd];
//nome is the Kurucz code - in case it's ever useful
  nome[0]=   100; 
  nome[1]=   200; 
  nome[2]=   300; 
  nome[3]=   400; 
  nome[4]=   500; 
  nome[5]=   600; 
  nome[6]=   700; 
  nome[7]=   800; 
  nome[8]=   900; 
  nome[9]=  1000; 
  nome[10]=  1100; 
  nome[11]=  1200; 
  nome[12]=  1300; 
  nome[13]=  1400; 
  nome[14]=  1500; 
  nome[15]=  1600; 
  nome[16]=  1700; 
  nome[17]=  1800; 
  nome[18]=  1900; 
  nome[19]=  2000; 
  nome[20]=  2100; 
  nome[21]=  2200; 
  nome[22]=  2300; 
  nome[23]=  2400; 
  nome[24]=  2500; 
  nome[25]=  2600; 
  nome[26]=  2700; 
  nome[27]=  2800; 
  nome[28]=  2900;
  nome[29]=  3000; 
  nome[30]=  3100; 
  nome[31]=  3600; 
  nome[32]=  3700; 
  nome[33]=  3800; 
  nome[34]=  3900; 
  nome[35]=  4000;
  nome[36]=  4100; 
  nome[37]=  5600; 
  nome[38]=  5700;
  nome[39]=  5500;  
  nome[40]=  3200;  
//log_10 "A_12" values:
 eheu[0]= 12.00;  
 eheu[1]= 10.93; 
 eheu[2]=  1.05;
 eheu[3]=  1.38;  
 eheu[4]=  2.70; 
 eheu[5]=  8.43;
 eheu[6]=  7.83;  
 eheu[7]=  8.69; 
 eheu[8]=  4.56;
 eheu[9]=  7.93;  
 eheu[10]=  6.24;
 eheu[11]=  7.60;  
 eheu[12]=  6.45; 
 eheu[13]=  7.51;
 eheu[14]=  5.41;  
 eheu[15]=  7.12; 
 eheu[16]=  5.50;
 eheu[17]=  6.40;  
 eheu[18]=  5.03; 
 eheu[19]=  6.34;
 eheu[20]=  3.15;  
 eheu[21]=  4.95; 
 eheu[22]=  3.93;
 eheu[23]=  5.64;  
 eheu[24]=  5.43; 
 eheu[25]=  7.50;
 eheu[26]=  4.99; 
 eheu[27]=  6.22;
 eheu[28]=  4.19;  
 eheu[29]=  4.56; 
 eheu[30]=  3.04;
 eheu[31]=  3.25;  
 eheu[32]=  2.52; 
 eheu[33]=  2.87;
 eheu[34]=  2.21;  
 eheu[35]=  2.58; 
 eheu[36]=  1.46;
 eheu[37]=  2.18;  
 eheu[38]=  1.10; 
 eheu[39]=  1.12;
 eheu[40]=  3.65; // Ge - out of sequence 

  cname[0]="H";
  cname[1]="He";
  cname[2]="Li";
  cname[3]="Be";
  cname[4]="B";
  cname[5]="C";
  cname[6]="N";
  cname[7]="O";
  cname[8]="F";
  cname[9]="Ne";
  cname[10]="Na";
  cname[11]="Mg";
  cname[12]="Al";
  cname[13]="Si";
  cname[14]="P";
  cname[15]="S";
  cname[16]="Cl";
  cname[17]="Ar";
  cname[18]="K";
  cname[19]="Ca";
  cname[20]="Sc";
  cname[21]="Ti";
  cname[22]="V";
  cname[23]="Cr";
  cname[24]="Mn";
  cname[25]="Fe";
  cname[26]="Co";
  cname[27]="Ni";
  cname[28]="Cu";
  cname[29]="Zn";
  cname[30]="Ga";
  cname[31]="Kr";
  cname[32]="Rb";
  cname[33]="Sr";
  cname[34]="Y";
  cname[35]="Zr";
  cname[36]="Nb";
  cname[37]="Ba";
  cname[38]="La";
  cname[39]="Cs";
  cname[40]="Ge";

//Variable declaration and set-up for Phil Bennett's GAS package

double kbol = CSGasInit.kbol;
double hmass = CSGasInit.hmass;
boolean print0 = CSGasInit.print0;
int[] itab = CSGasInit.itab;
int[] ntab = CSGasInit.ntab;
int nix = CSGasInit.nix;
int[][] ixa = CSGasInit.ixa;
String[] chix = CSGasInit.chix;
int iprint = CSGasInit.iprint;

/* Main "Gas data" table for input to GAS package (Phil Bennett) */
//This would require substantial re-arranging to put into a Class :
name[0] = "H";      ipr[0] = 1; nch[0] =  0; nel[0] = 1; nat[0][0] = 1;  zat[0][0] = 1;  awt[0] =  1.008; comp[0] = 9.32e-01;
name[1] = "H+";     ipr[1] = 1; nch[1] = +1; ip[1] = 13.598;  logwt[1] = 0.000;
name[2] = "H-";     ipr[2] = 1; nch[2] = -1; ip[2] =  0.754;  logwt[2] = 0.600;
name[3] = "He";     ipr[3] = 2; nch[3] =  0; nel[3] = 1; nat[0][3] = 1;  zat[0][3] = 2;  awt[3] =  4.003; comp[1] = 6.53e-02;
name[4] = "He+";    ipr[4] = 2; nch[4] = +1; ip[4] = 24.587;  logwt[4] = 0.600;
name[5] = "C";      ipr[5] = 1; nch[5] =  0; nel[5] = 1; nat[0][5] = 1;  zat[0][5] = 6;  awt[5] = 12.011; comp[2] = 4.94e-04;
name[6] = "C+";     ipr[6] = 1; nch[6] = +1; ip[6] = 11.260;  logwt[6] = 0.100;
name[7] = "N";      ipr[7] = 1; nch[7] =  0; nel[7] = 1; nat[0][7] = 1;  zat[0][7] = 7;  awt[7] = 14.007; comp[3] = 8.95e-04;
name[8] = "N+";     ipr[8] = 1; nch[8] = +1; ip[8] = 14.534;  logwt[8] = 0.650;
name[9] = "O";      ipr[9] = 1; nch[9] =  0; nel[9] = 1; nat[0][9] = 1;  zat[0][9] = 8;  awt[9] = 16.000; comp[4] = 8.48e-04;
name[10] = "O+";    ipr[10] = 1; nch[10] = +1; ip[10] = 13.618; logwt[10] = -0.050;
name[11] = "Ne";    ipr[11] = 2; nch[11] =  0; nel[11] = 1; nat[0][11] = 1;  zat[0][11] = 10; awt[11] = 20.179; comp[5] = 7.74e-05;
name[12] = "Ne+";   ipr[12] = 2; nch[12] = +1; ip[12] = 21.564;  logwt[12] = 1.080;
name[13] = "Na";    ipr[13] = 2; nch[13] =  0; nel[13] = 1; nat[0][13] = 1;  zat[0][13] = 11; awt[13] = 22.990; comp[6] = 1.68e-06;
name[14] = "Na+";   ipr[14] = 2; nch[14] = +1; ip[14] =  5.139;  logwt[14] = 0.000;
name[15] = "Mg";    ipr[15] = 2; nch[15] =  0; nel[15] = 1; nat[0][15] = 1;  zat[0][15] = 12; awt[15] = 24.305; comp[7] = 2.42e-05;
name[16] = "Mg+";   ipr[16] = 2; nch[16] = +1; ip[16] =  7.644;  logwt[16] = 0.600;
name[17] = "Mg++";  ipr[17] = 2; nch[17] = +2; ip[17] = 15.031;  logwt[17] = 0.000;
name[18] = "Al";    ipr[18] = 2; nch[18] =  0; nel[18] = 1; nat[0][18] = 1;  zat[0][18] = 13; awt[18] = 26.982; comp[8] = 2.24e-06;
name[19] = "Al+";   ipr[19] = 2; nch[19] = +1; ip[19] =  5.984; logwt[19] = -0.480;
name[20] = "Si";    ipr[20] = 1; nch[20] =  0; nel[20] = 1; nat[0][20] = 1;  zat[0][20] = 14; awt[20] = 28.086; comp[9] = 3.08e-05;
name[21] = "Si+";   ipr[21] = 1; nch[21] = +1; ip[21] =  8.149;  logwt[21] = 0.120;
name[22] = "S";     ipr[22] = 1; nch[22] =  0; nel[22] = 1; nat[0][22] = 1;  zat[0][22] = 16; awt[22] = 32.060; comp[10] = 1.49e-05;
name[23] = "S+";    ipr[23] = 1; nch[23] = +1; ip[23] = 10.360; logwt[23] = -0.050;
name[24] = "Cl";    ipr[24] = 3; nch[24] =  0; nel[24] = 1; nat[0][24] = 1;  zat[0][24] = 17; awt[24] = 35.453; comp[11] = 3.73e-07;
name[25] = "Cl-";   ipr[25] = 3; nch[25] = -1; ip[25] =  3.613;  logwt[25] = 1.080;
name[26] = "K";     ipr[26] = 2; nch[26] =  0; nel[26] = 1; nat[0][26] = 1;  zat[0][26] = 19; awt[26] = 39.102; comp[12] = 8.30e-08;
name[27] = "K+";    ipr[27] = 2; nch[27] = +1; ip[27] =  4.339;  logwt[27] = 0.000;
name[28] = "Ca";    ipr[28] = 2; nch[28] =  0; nel[28] = 1; nat[0][28] = 1;  zat[0][28] = 20; awt[28] = 40.080; comp[13] = 1.86e-06;
name[29] = "Ca+";   ipr[29] = 2; nch[29] = +1; ip[29] =  6.111;  logwt[29] = 0.600;
name[30] = "Ca++";  ipr[30] = 2; nch[30] = +2; ip[30] = 11.868;  logwt[30] = 0.000;
name[31] = "Sc";    ipr[31] = 3; nch[31] =  0; nel[31] = 1; nat[0][31] = 1;  zat[0][31] = 21; awt[31] = 44.956; comp[14] = 1.49e-09;
name[32] = "Sc+";   ipr[32] = 3; nch[32] = +1; ip[32] =  6.540;  logwt[32] = 0.480;
name[33] = "Ti";    ipr[33] = 3; nch[33] =  0; nel[33] = 1; nat[0][33] = 1;  zat[0][33] = 22; awt[33] = 47.900; comp[15] = 1.21e-07;
name[34] = "Ti+";   ipr[34] = 3; nch[34] = +1; ip[34] =  6.820;  logwt[34] = 0.430;
name[35] = "V";     ipr[35] = 3; nch[35] =  0; nel[35] = 1; nat[0][35] = 1;  zat[0][35] = 23; awt[35] = 50.941; comp[16] = 2.33e-08;
name[36] = "V+";    ipr[36] = 3; nch[36] = +1; ip[36] =  6.740;  logwt[36] = 0.250;
name[37] = "Cr";    ipr[37] = 3; nch[37] =  0; nel[37] = 1; nat[0][37] = 1;  zat[0][37] = 24; awt[37] = 51.996; comp[17] = 6.62e-07;
name[38] = "Cr+";   ipr[38] = 3; nch[38] = +1; ip[38] =  6.766;  logwt[38] = 0.230;
name[39] = "Mn";    ipr[39] = 3; nch[39] =  0; nel[39] = 1; nat[0][39] = 1;  zat[0][39] = 25; awt[39] = 54.938; comp[18] = 2.33e-07;
name[40] = "Mn+";   ipr[40] = 3; nch[40] = +1; ip[40] =  7.435;  logwt[40] = 0.370;
name[41] = "Fe";    ipr[41] = 2; nch[41] =  0; nel[41] = 1; nat[0][41] = 1;  zat[0][41] = 26; awt[41] = 55.847; comp[19] = 3.73e-05;
name[42] = "Fe+";   ipr[42] = 2; nch[42] = +1; ip[42] =  7.870;  logwt[42] = 0.380;
name[43] = "Co";    ipr[43] = 3; nch[43] =  0; nel[43] = 1; nat[0][43] = 1;  zat[0][43] = 27; awt[43] = 58.933; comp[20] = 1.12e-07;
name[44] = "Co+";   ipr[44] = 3; nch[44] = +1; ip[44] =  7.860;  logwt[44] = 0.180;
name[45] = "Ni";    ipr[45] = 2; nch[45] =  0; nel[45] = 1; nat[0][45] = 1;  zat[0][45] = 28; awt[45] = 58.710; comp[21] = 1.86e-06;
name[46] = "Ni+";   ipr[46] = 2; nch[46] = +1; ip[46] =  7.635; logwt[46] = -0.020;
name[47] = "Sr";    ipr[47] = 3; nch[47] =  0; nel[47] = 1; nat[0][47] = 1;  zat[0][47] = 38; awt[47] = 87.620; comp[22] = 6.62e-10;
name[48] = "Sr+";   ipr[48] = 3; nch[48] = +1; ip[48] =  5.695;  logwt[48] = 0.500;
name[49] = "Y";     ipr[49] = 3; nch[49] =  0; nel[49] = 1; nat[0][49] = 1;  zat[0][49] = 39; awt[49] = 88.906; comp[23] = 5.87e-11;
name[50] = "Y+";    ipr[50] = 3; nch[50] = +1; ip[50] =  6.380;  logwt[50] = 0.500;
name[51] = "Zr";    ipr[51] = 3; nch[51] =  0; nel[51] = 1; nat[0][51] = 1;  zat[0][51] = 40; awt[51] = 91.220; comp[24] = 2.98e-10;
name[52] = "Zr+";   ipr[52] = 3; nch[52] = +1; ip[52] =  6.840;  logwt[52] = 0.420;
name[53] = "H2";    ipr[53] = 1; nch[53] =  0; nel[53] = 1; nat[0][53] = 2;  zat[0][53] = 1;           logk[0][53] = 12.739; logk[1][53] = -5.1172;  logk[2][53] = 0.12572; logk[3][53] = -1.4149e-02; logk[4][53] = 6.3021e-04;
name[54] = "H2+";   ipr[54] = 1; nch[54] = +1; ip[54] = 15.422;  logwt[54] = 0.600;
name[55] = "C2";    ipr[55] = 1; nch[55] =  0; nel[55] = 1; nat[0][55] = 2;  zat[0][55] = 6;           logk[0][55] = 12.804; logk[1][55] = -6.5178;  logk[2][55] = .097719; logk[3][55] = -1.2739e-02;  logk[4][55] = 6.2603e-04;
name[56] = "C3";    ipr[56] = 1; nch[56] =  0; nel[56] = 1; nat[0][56] = 3;  zat[0][56] = 6;           logk[0][56] = 25.230; logk[1][56] = -14.445;  logk[2][56] = 0.12547; logk[3][56] = -1.7390e-02;  logk[4][56] = 8.8594e-04;
name[57] = "N2";    ipr[57] = 1; nch[57] =  0; nel[57] = 1; nat[0][57] = 2; zat[0][57] = 7;           logk[0][57] = 13.590; logk[1][57] = -10.585;  logk[2][57] = 0.22067; logk[3][57] = -2.9997e-02;  logk[4][57] = 1.4993e-03;
name[58] = "O2";    ipr[58] = 1; nch[58] =  0; nel[58] = 1; nat[0][58] = 2; zat[0][58] = 8;           logk[0][58] = 13.228; logk[1][58] = -5.5181;  logk[2][58] = .069935; logk[3][58] = -8.1511e-03;  logk[4][58] = 3.7970e-04;
name[59] = "CH";    ipr[59] = 1; nch[59] =  0; nel[59] = 2; nat[0][59] = 1; zat[0][59] = 6;  nat[1][59] = 1;  zat[1][59] = 1;  nat[2][59] = 0;  zat[2][59] = 0; logk[0][59] = 12.135; logk[1][59] = -4.0760; logk[2][59] =  0.12768; logk[3][59] = -1.5473e-02; logk[4][59] =  7.2661e-04;
name[60] = "C2H2";  ipr[60] = 1; nch[60] =  0; nel[60] = 2; nat[0][60] = 2; zat[0][60] = 6;  nat[1][60] = 2;  zat[1][60] = 1;  nat[2][60] = 0;  zat[2][60] = 0; logk[0][60] = 38.184; logk[1][60] = -17.365; logk[2][60] =  .021512; logk[3][60] = -8.8961e-05; logk[4][60] = -2.8720e-05;
name[61] = "NH";    ipr[61] = 1; nch[61] =  0; nel[61] = 2; nat[0][61] = 1; zat[0][61] = 7;  nat[1][61] = 1;  zat[1][61] = 1;  nat[2][61] = 0;  zat[2][61] = 0; logk[0][61] = 12.033; logk[1][61] = -3.8435; logk[2][61] =  0.13629; logk[3][61] = -1.6643e-02; logk[4][61] =  7.8691e-04;
name[62] = "NH2";   ipr[62] = 1; nch[62] =  0; nel[62] = 2; nat[0][62] = 1; zat[0][62] = 7;  nat[1][62] = 2;  zat[1][62] = 1;  nat[2][62] = 0;  zat[2][62] = 0; logk[0][62] = 24.603; logk[1][62] = -8.6300; logk[2][62] =  0.20048; logk[3][62] = -2.4124e-02; logk[4][62] =  1.1484e-03;
name[63] = "NH3";   ipr[63] = 1; nch[63] =  0; nel[63] = 2; nat[0][63] = 1; zat[0][63] = 7;  nat[1][63] = 3;  zat[1][63] = 1;  nat[2][63] = 0;  zat[2][63] = 0; logk[0][63] = 37.554; logk[1][63] = -13.059; logk[2][63] =  0.12910; logk[3][63] = -1.2338e-02; logk[4][63] =  5.3429e-04;
name[64] = "OH";    ipr[64] = 1; nch[64] =  0; nel[64] = 2; nat[0][64] = 1; zat[0][64] = 8;  nat[1][64] = 1;  zat[1][64] = 1;  nat[2][64] = 0;  zat[2][64] = 0; logk[0][64] = 12.371; logk[1][64] = -5.0578; logk[2][64] =  0.13822; logk[3][64] = -1.6547e-02; logk[4][64] =  7.7224e-04;
name[65] = "H2O";   ipr[65] = 1; nch[65] =  0; nel[65] = 2; nat[0][65] = 1; zat[0][65] = 8;  nat[1][65] = 2;  zat[1][65] = 1;  nat[2][65] = 0;  zat[2][65] = 0; logk[0][65] = 25.420; logk[1][65] = -10.522; logk[2][65] =  0.16939; logk[3][65] = -1.8368e-02; logk[4][65] =  8.1730e-04;
name[66] = "MgH";   ipr[66] = 2; nch[66] =  0; nel[66] = 2; nat[0][66] = 1; zat[0][66] = 12; nat[1][66] = 1;  zat[1][66] = 1;  nat[2][66] = 0;  zat[2][66] = 0; logk[0][66] = 11.285; logk[1][66] = -2.7164; logk[2][66] =  0.19658; logk[3][66] = -2.7310e-02; logk[4][66] =  1.3816e-03;
name[67] = "AlH";   ipr[67] = 2; nch[67] =  0; nel[67] = 2; nat[0][67] = 1; zat[0][67] = 13; nat[1][67] = 1;  zat[1][67] = 1;  nat[2][67] = 0;  zat[2][67] = 0; logk[0][67] = 12.191; logk[1][67] = -3.7636; logk[2][67] =  0.25557; logk[3][67] = -3.7261e-02; logk[4][67] =  1.9406e-03;
name[68] = "SiH";   ipr[68] = 1; nch[68] =  0; nel[68] = 2; nat[0][68] = 1; zat[0][68] = 14; nat[1][68] = 1;  zat[1][68] = 1;  nat[2][68] = 0;  zat[2][68] = 0; logk[0][68] = 11.852; logk[1][68] = -3.7418; logk[2][68] =  0.15999; logk[3][68] = -2.0629e-02; logk[4][68] =  9.9897e-04;
name[69] = "HS";    ipr[69] = 1; nch[69] =  0; nel[69] = 2; nat[0][69] = 1; zat[0][69] = 16; nat[1][69] = 1;  zat[1][69] = 1;  nat[2][69] = 0;  zat[2][69] = 0; logk[0][69] = 12.019; logk[1][69] = -4.2922; logk[2][69] =  0.14913; logk[3][69] = -1.8666e-02; logk[4][69] =  8.9438e-04;
name[70] = "H2S";   ipr[70] = 1; nch[70] =  0; nel[70] = 2; nat[0][70] = 1; zat[0][70] = 16; nat[1][70] = 2;  zat[1][70] = 1;  nat[2][70] = 0;  zat[2][70] = 0; logk[0][70] = 24.632; logk[1][70] = -8.4616; logk[2][70] =  0.17014; logk[3][70] = -2.0236e-02; logk[4][70] =  9.5782e-04;
name[71] = "HCl";   ipr[71] = 3; nch[71] =  0; nel[71] = 2; nat[0][71] = 1; zat[0][71] = 17; nat[1][71] = 1;  zat[1][71] = 1;  nat[2][71] = 0;  zat[2][71] = 0; logk[0][71] = 12.528; logk[1][71] = -5.1827; logk[2][71] =  0.18117; logk[3][71] = -2.4014e-02; logk[4][71] =  1.1994e-03;
name[72] = "CaH";   ipr[72] = 3; nch[72] =  0; nel[72] = 2; nat[0][72] = 1; zat[0][72] = 20; nat[1][72] = 1;  zat[1][72] = 1;  nat[2][72] = 0;  zat[2][72] = 0; logk[0][72] = 11.340; logk[1][72] = -3.0144; logk[2][72] =  0.42349; logk[3][72] = -6.1467e-02; logk[4][72] =  3.1639e-03;
name[73] = "CN";    ipr[73] = 1; nch[73] =  0; nel[73] = 2; nat[0][73] = 1; zat[0][73] = 7;  nat[1][73] = 1;  zat[1][73] = 6;  nat[2][73] = 0;  zat[2][73] = 0; logk[0][73] = 12.805; logk[1][73] = -8.2793; logk[2][73] =  .064162; logk[3][73] = -7.3627e-03; logk[4][73] =  3.4666e-04;
name[74] = "NO";    ipr[74] = 1; nch[74] =  0; nel[74] = 2; nat[0][74] = 1; zat[0][74] = 8;  nat[1][74] = 1;  zat[1][74] = 7;  nat[2][74] = 0;  zat[2][74] = 0; logk[0][74] = 12.831; logk[1][74] = -7.1964; logk[2][74] =  0.17349; logk[3][74] = -2.3065e-02; logk[4][74] =  1.1380e-03;
name[75] = "CO";    ipr[75] = 1; nch[75] =  0; nel[75] = 2; nat[0][75] = 1; zat[0][75] = 8;  nat[1][75] = 1;  zat[1][75] = 6;  nat[2][75] = 0;  zat[2][75] = 0; logk[0][75] = 13.820; logk[1][75] = -11.795; logk[2][75] =  0.17217; logk[3][75] = -2.2888e-02; logk[4][75] =  1.1349e-03;
name[76] = "CO2";   ipr[76] = 1; nch[76] =  0; nel[76] = 2; nat[0][76] = 2; zat[0][76] = 8;  nat[1][76] = 1;  zat[1][76] = 6;  nat[2][76] = 0;  zat[2][76] = 0; logk[0][76] = 27.478; logk[1][76] = -17.098; logk[2][76] =  .095012; logk[3][76] = -1.2579e-02; logk[4][76] =  6.4058e-04;
name[77] = "MgO";   ipr[77] = 3; nch[77] =  0; nel[77] = 2; nat[0][77] = 1; zat[0][77] = 12; nat[1][77] = 1;  zat[1][77] = 8;  nat[2][77] = 0;  zat[2][77] = 0; logk[0][77] = 11.702; logk[1][77] = -5.0326; logk[2][77] =  0.29641; logk[3][77] = -4.2811e-02; logk[4][77] =  2.2023e-03;
name[78] = "AlO";   ipr[78] = 2; nch[78] =  0; nel[78] = 2; nat[0][78] = 1; zat[0][78] = 13; nat[1][78] = 1;  zat[1][78] = 8;  nat[2][78] = 0;  zat[2][78] = 0; logk[0][78] = 12.739; logk[1][78] = -5.2534; logk[2][78] =  0.18218; logk[3][78] = -2.5793e-02; logk[4][78] =  1.3185e-03;
name[79] = "SiO";   ipr[79] = 1; nch[79] =  0; nel[79] = 2; nat[0][79] = 1; zat[0][79] = 14; nat[1][79] = 1;  zat[1][79] = 8;  nat[2][79] = 0;  zat[2][79] = 0; logk[0][79] = 13.413; logk[1][79] = -8.8710; logk[2][79] =  0.15042; logk[3][79] = -1.9581e-02; logk[4][79] =  9.4828e-04;
name[80] = "SO";    ipr[80] = 1; nch[80] =  0; nel[80] = 2; nat[0][80] = 1; zat[0][80] = 16; nat[1][80] = 1;  zat[1][80] = 8;  nat[2][80] = 0;  zat[2][80] = 0; logk[0][80] = 12.929; logk[1][80] = -6.0100; logk[2][80] =  0.16253; logk[3][80] = -2.1665e-02; logk[4][80] =  1.0676e-03;
name[81] = "CaO";   ipr[81] = 2; nch[81] =  0; nel[81] = 2; nat[0][81] = 1; zat[0][81] = 20; nat[1][81] = 1;  zat[1][81] = 8;  nat[2][81] = 0;  zat[2][81] = 0; logk[0][81] = 12.260; logk[1][81] = -6.0525; logk[2][81] =  0.58284; logk[3][81] = -8.5805e-02; logk[4][81] =  4.4425e-03;
name[82] = "ScO";   ipr[82] = 3; nch[82] =  0; nel[82] = 2; nat[0][82] = 1; zat[0][82] = 21; nat[1][82] = 1;  zat[1][82] = 8;  nat[2][82] = 0;  zat[2][82] = 0; logk[0][82] = 13.747; logk[1][82] = -8.6420; logk[2][82] =  0.48072; logk[3][82] = -6.9670e-02; logk[4][82] =  3.5747e-03;
name[83] = "ScO2";  ipr[83] = 3; nch[83] =  0; nel[83] = 2; nat[0][83] = 1; zat[0][83] = 21; nat[1][83] = 2;  zat[1][83] = 8;  nat[2][83] = 0;  zat[2][83] = 0; logk[0][83] = 26.909; logk[1][83] = -15.824; logk[2][83] =  0.39999; logk[3][83] = -5.9363e-02; logk[4][83] =  3.0875e-03;
name[84] = "TiO";   ipr[84] = 2; nch[84] =  0; nel[84] = 2; nat[0][84] = 1; zat[0][84] = 22; nat[1][84] = 1;  zat[1][84] = 8;  nat[2][84] = 0;  zat[2][84] = 0; logk[0][84] = 13.398; logk[1][84] = -8.5956; logk[2][84] =  0.40873; logk[3][84] = -5.7937e-02; logk[4][84] =  2.9287e-03;
name[85] = "VO";    ipr[85] = 3; nch[85] =  0; nel[85] = 2; nat[0][85] = 1; zat[0][85] = 23; nat[1][85] = 1;  zat[1][85] = 8;  nat[2][85] = 0;  zat[2][85] = 0; logk[0][85] = 13.811; logk[1][85] = -7.7520; logk[2][85] =  0.37056; logk[3][85] = -5.1467e-02; logk[4][85] =  2.5861e-03;
name[86] = "VO2";   ipr[86] = 3; nch[86] =  0; nel[86] = 2; nat[0][86] = 1; zat[0][86] = 23; nat[1][86] = 2;  zat[1][86] = 8;  nat[2][86] = 0;  zat[2][86] = 0; logk[0][86] = 27.754; logk[1][86] = -14.040; logk[2][86] =  0.33613; logk[3][86] = -4.8215e-02; logk[4][86] =  2.4780e-03;
name[87] = "YO";    ipr[87] = 3; nch[87] =  0; nel[87] = 2; nat[0][87] = 1; zat[0][87] = 39; nat[1][87] = 1;  zat[1][87] = 8;  nat[2][87] = 0;  zat[2][87] = 0; logk[0][87] = 13.514; logk[1][87] = -8.7775; logk[2][87] =  0.40700; logk[3][87] = -5.8053e-02; logk[4][87] =  2.9535e-03;
name[88] = "YO2";   ipr[88] = 3; nch[88] =  0; nel[88] = 2; nat[0][88] = 1; zat[0][88] = 39; nat[1][88] = 2;  zat[1][88] = 8;  nat[2][88] = 0;  zat[2][88] = 0; logk[0][88] = 26.764; logk[1][88] = -16.447; logk[2][88] =  0.39991; logk[3][88] = -5.8916e-02; logk[4][88] =  3.0506e-03;
name[89] = "ZrO";   ipr[89] = 3; nch[89] =  0; nel[89] = 2; nat[0][89] = 1; zat[0][89] = 40; nat[1][89] = 1;  zat[1][89] = 8;  nat[2][89] = 0;  zat[2][89] = 0; logk[0][89] = 13.296; logk[1][89] = -9.0129; logk[2][89] =  0.19562; logk[3][89] = -2.9892e-02; logk[4][89] =  1.6010e-03;
name[90] = "ZrO2";  ipr[90] = 3; nch[90] =  0; nel[90] = 2; nat[0][90] = 1; zat[0][90] = 40; nat[1][90] = 2;  zat[1][90] = 8;  nat[2][90] = 0;  zat[2][90] = 0; logk[0][90] = 26.793; logk[1][90] = -16.151; logk[2][90] =  0.46988; logk[3][90] = -6.4636e-02; logk[4][90] =  3.2277e-03;
name[91] = "CS";    ipr[91] = 1; nch[91] =  0; nel[91] = 2; nat[0][91] = 1; zat[0][91] = 16; nat[1][91] = 1;  zat[1][91] = 6;  nat[2][91] = 0;  zat[2][91] = 0; logk[0][91] = 13.436; logk[1][91] = -8.5574; logk[2][91] =  0.18754; logk[3][91] = -2.5507e-02; logk[4][91] =  1.2735e-03;
name[92] = "SiS";   ipr[92] = 1; nch[92] =  0; nel[92] = 2; nat[0][92] = 1; zat[0][92] = 14; nat[1][92] = 1;  zat[1][92] = 16; nat[2][92] = 0;  zat[2][92] = 0; logk[0][92] = 13.182; logk[1][92] = -7.1147; logk[2][92] =  0.19300; logk[3][92] = -2.5826e-02; logk[4][92] =  1.2648e-03;
name[93] = "TiS";   ipr[93] = 2; nch[93] =  0; nel[93] = 2; nat[0][93] = 1; zat[0][93] = 22; nat[1][93] = 1;  zat[1][93] = 16; nat[2][93] = 0;  zat[2][93] = 0; logk[0][93] = 13.316; logk[1][93] = -6.2216; logk[2][93] =  0.45829; logk[3][93] = -6.4903e-02; logk[4][93] =  3.2788e-03;
name[94] = "SiC";   ipr[94] = 1; nch[94] =  0; nel[94] = 2; nat[0][94] = 1; zat[0][94] = 14; nat[1][94] = 1;  zat[1][94] = 6;  nat[2][94] = 0;  zat[2][94] = 0; logk[0][94] = 12.327; logk[1][94] = -5.0419; logk[2][94] =  0.13941; logk[3][94] = -1.9363e-02; logk[4][94] =  9.6202e-04;
name[95] = "SiC2";  ipr[95] = 1; nch[95] =  0; nel[95] = 2; nat[0][95] = 1; zat[0][95] = 14; nat[1][95] = 2;  zat[1][95] = 6;  nat[2][95] = 0;  zat[2][95] = 0; logk[0][95] = 25.623; logk[1][95] = -13.085; logk[2][95] = -.055227; logk[3][95] =  9.3363e-03; logk[4][95] = -4.9876e-04;
name[96] = "NaCl";  ipr[96] = 2; nch[96] =  0; nel[96] = 2; nat[0][96] = 1; zat[0][96] = 11; nat[1][96] = 1;  zat[1][96] = 17; nat[2][96] = 0;  zat[2][96] = 0; logk[0][96] = 11.768; logk[1][96] = -4.9884; logk[2][96] =  0.23975; logk[3][96] = -3.4837e-02; logk[4][96] =  1.8034e-03;
name[97] = "MgCl";  ipr[97] = 2; nch[97] =  0; nel[97] = 2; nat[0][97] = 1; zat[0][97] = 12; nat[1][97] = 1;  zat[1][97] = 17; nat[2][97] = 0;  zat[2][97] = 0; logk[0][97] = 11.318; logk[1][97] = -4.2224; logk[2][97] =  0.21137; logk[3][97] = -3.0174e-02; logk[4][97] =  1.5480e-03;
name[98] = "AlCl";  ipr[98] = 2; nch[98] =  0; nel[98] = 2; nat[0][98] = 1; zat[0][98] = 13; nat[1][98] = 1;  zat[1][98] = 17; nat[2][98] = 0;  zat[2][98] = 0; logk[0][98] = 11.976; logk[1][98] = -5.2228; logk[2][98] = -.010263; logk[3][98] =  3.9344e-03; logk[4][98] = -2.6236e-04;
name[99] = "CaCl";  ipr[99] = 2; nch[99] =  0; nel[99] = 2; nat[0][99] = 1; zat[0][99] = 20; nat[1][99] = 1;  zat[1][99] = 17; nat[2][99] = 0;  zat[2][99] = 0; logk[0][99] = 12.314; logk[1][99] = -5.1814; logk[2][99] =  0.56532; logk[3][99] = -8.2868e-02; logk[4][99] =  4.2822e-03;
name[100] = "HCN";  ipr[100] = 1; nch[100] =  0; nel[100] = 3; nat[0][100] = 1; zat[0][100] = 7;  nat[1][100] = 1;  zat[1][100] = 6;  nat[2][100] = 1;  zat[2][100] = 1; logk[0][100] = 25.635; logk[1][100] = -13.833; logk[2][100] =  0.13827; logk[3][100] = -1.8122e-02; logk[4][100] =  9.1645e-04;
name[101] = "HCO";  ipr[101] = 1; nch[101] =  0; nel[101] = 3; nat[0][101] = 1; zat[0][101] = 8;  nat[1][101] = 1;  zat[1][101] = 6;  nat[2][101] = 1;  zat[2][101] = 1; logk[0][101] = 25.363; logk[1][101] = -13.213; logk[2][101] =  0.18451; logk[3][101] = -2.2973e-02; logk[4][101] =  1.1114e-03;
name[102] = "MgOH"; ipr[102] = 2; nch[102] =  0; nel[102] = 3; nat[0][102] = 1; zat[0][102] = 12; nat[1][102] = 1;  zat[1][102] = 8;  nat[2][102] = 1;  zat[2][102] = 1; logk[0][102] = 24.551; logk[1][102] = -9.3818; logk[2][102] =  0.19666; logk[3][102] = -2.7178e-02; logk[4][102] =  1.3887e-03;
name[103] = "AlOH"; ipr[103] = 2; nch[103] =  0; nel[103] = 3; nat[0][103] = 1; zat[0][103] = 13; nat[1][103] = 1;  zat[1][103] = 8;  nat[2][103] = 1;  zat[2][103] = 1; logk[0][103] = 25.707; logk[1][103] = -10.624; logk[2][103] =  .097901; logk[3][103] = -1.1835e-02; logk[4][103] =  5.8121e-04;
name[104] = "CaOH"; ipr[104] = 2; nch[104] =  0; nel[104] = 3; nat[0][104] = 1; zat[0][104] = 20; nat[1][104] = 1;  zat[1][104] = 8;  nat[2][104] = 1;  zat[2][104] = 1; logk[0][104] = 24.611; logk[1][104] = -10.910; logk[2][104] =  0.60803; logk[3][104] = -8.7197e-02; logk[4][104] =  4.4736e-03;

    int nelt, nat1, nleft, nats, nsp1;
    int gsJ, gsK, kp, nn;

    double sum0;

    //double[] it = new double[150]; //needed
    //double[] kt = new double[150]; //needed


    //int[] type0 = new int[150];

    int iprt = 0;
    int ncht = 0;
    int[] ix = new int[5];

    //#blank = ' '
    String ename = "e-";
    int mxatom = 30;
    int mxspec = 150;

    int n = 0;   //#record counter
    int np = 0;
    natom = -1;   //#neutral atomic species counter
    nlin1 = -1;
    nlin2 = -1;
    double tcomp = 0.0e0;

    //#nspec = len(name)
    //#print("nspec ", nspec)
    while (name[n] != null){
    //#for n in range(nspec):    
        //#c
        //#c Each following input line specifies a distinct chemical species.
        //#c

    //#1 
        //#namet = name[n]
        iprt = ipr[n];
        ncht = nch[n];
        idel[n] = 1;
        //#print("iprt ", iprt, " ncht ", ncht)
    //#c
    //#c Determine the species type:
    //#c TYPE(N) = 1 --> Neutral atom
    //#c         = 2 --> Neutral molecule
    //#c         = 3 --> Negative ion
    //#c         = 4 --> Positive ion
    //#c

        if (nch[n] == 0){
//#c
//#c Species is neutral
//#c
            np = n;
            nelt = nel[n];
            nat1 = nat[0][n];

            if (nelt <= 1 && nat1 <= 1){
//#c
//#c Neutral atom (one atom of single element Z present)
//#c
                type0[n] = 1;
                natom = natom + 1;
                if (natom >= mxatom){
                    System.out.println(" *20 Error: Too many elements specified. " + "  Limit is " + mxatom);
                }

                iat[n] = natom;
                //#print("Setting indsp, n: ", n, " natom ", natom)
                indsp[natom] = n;  //#pointer to iat[], etc....
                //System.out.println("n " + n + " zat[0][n] " + zat[0][n]);
                indzat[zat[0][n]-1] = natom;   //#indzat's index is atomic number - 1
                ntot[n] = 1;
                neut[n] = n;

                tcomp = tcomp + comp[natom];
                iprt = ipr[n];
                if (iprt == 1){
                    nlin1 = nlin1 + 1;
                    lin1[natom] = nlin1;
                    linv1[nlin1] = natom;
                }

                if ( (iprt == 1) || (iprt == 2) ){
                    nlin2 = nlin2 + 1;
                    lin2[natom] = nlin2;
                    linv2[nlin2] = natom;
                }

            } else{  // <= 1 and nat1 <= 1 <= 1 and nat1 <= 1 condition

//#c
//#c Neutral molecule ( >1 atom present in species)
//#c
                type0[n] = 2;
                ntot[n] = nat1;
                neut[n] = n;

                nleft = (nelt - 1)*2;
                //#print("Neutral mol: n ", n, " name ", name[n], " nelt ", nelt, " nleft ", nleft)

                if (nleft > 0){
                    for (int ii = 1; ii < 3; ii++){
                        ntot[n] = ntot[n] + nat[ii][n];
                    }
                }
            //#print("5: n ", n, " logk ", logk[0][n], " ", logk[1][n], " ", logk[2][n], " ", logk[3][n], " ", logk[4][n])
            }   // <= 1 and nat1 <= 1 <= 1 and nat1 <= 1 condition
        } else {  //nch[n]=0 condition   
//#c
//#c Ionic species (nch .ne. 0)
//#c

            if (np <= -1){
                System.out.println(" *** error: ionic species encountered out of " + " sequence");
            }

            if (ncht < 0){
                type0[n] = 3;
            } else if (ncht > 0){
                type0[n] = 4;
            }

            neut[n] = np;
            nel[n] = nel[np];
            nelt = nel[n];
            for (int i = 0; i < nelt; i++){
                nat[i][n] = nat[i][np];
                zat[i][n] = zat[i][np];
            }

            ntot[n] = ntot[np];
         }  //nch[n]=0 condition

//#print("6: n ", n, " ip ", ip[n], " logwt ", logwt[n])

//#c
//#c Generate master array tying chemical formula of species to
//#c its table index. A unique index is generated for a given
//#c (possibly charged) species containing up to 4 atoms.
//#c
//#c Index #1 <--  Ionic charge + 2  (dim. 4, allows chg -1 to +2) 
//#c       #2 <--> Index to Z of 1st atom in species (23 allowed Z)
//#c       #3 <-->    "          2nd        "        ( 6 allowed Z)
//#c       #4 <-->    "          3rd        "        ( 4 allowed Z)
//#c       #5 <-->    "          4th        "        ( 1 allowed Z)
//#c

        //#ix[0] = nch[n] + 2;
        ix[0] = nch[n] + 1;
        nelt = nel[n];
        //#k = 1;
        gsK = 0;

        //#print("n ", n, " name ", name[n])
        for (int i = 0; i < nelt; i++){

            nats = nat[i][n];
            for (int j99 = 0; j99 < nats; j99++){

                gsK = gsK + 1;
                if (gsK > 4){
                    System.out.println(" *21 Error: species " + " contains > 4 atoms " + name[n]);
                }

                ix[gsK] = itab[zat[i][n]-1];
                //System.out.println("n "+ n + " name "+ name[n]+ " k "+ k+ " i "+ i+ " n "+ n+ " zat "+ zat[i][n]+ " itab "+ itab[zat[i][n]-1]);
                //#print("i ", i, " j ", j, " k ", k, " ix ", ix[k], "ntab ", ntab[k])
                //#print("zat-1 ", zat[i][n]-1, "itab ", itab[zat[i][n]-1])
                if ( (ix[gsK] <= 0) || (ix[gsK] > ntab[gsK]) ){
                    //System.out.println(" *22 Error: species atom z= not in allowed element list" 
                    //  + name[n] + " " + (zat[i][n]-1).toString());
                }
            }
        }

        if (gsK < 4){
            //System.out.println("k < 4, k= "+ k );
            kp = gsK + 1;
            for (int kk = kp; kk < 5; kk++){
                //System.out.println("kk "+ kk);
                ix[kk] = 0;
            }
            //#print("kk ", kk, " ix ", ix[kk])
        }


        indx[ix[0]][ix[1]][ix[2]][ix[3]][ix[4]] = n;
        //System.out.println("n " + n + " name " + name[n] + " ix[] " + ix[0] + " " + ix[1] + " " + ix[2] + " " + ix[3] + " " + ix[4]);
        n = n + 1;
            //#print("n ", n, " name ", name[n], " ix ", ix[0], ix[1], ix[2], ix[3], ix[4],\
            //#      " indx ", indx[ix[0]][ix[1]][ix[2]][ix[3]][ix[4]])

    }//end while
    //#go to 1
    //#Ends if namet != ''??

    //#Get next line of data and test of end-of-file:
    //#gsline = inputHandle.readline()
    //#lineLength = len(gsline)
            //#print("lineLength = ", lineLength)
    //#Ends file read loop "with open(infile...??)

    //#After read loop:
//#GAS composition should be corrected to CSPy values at this point:
    //#CSPy/Phoenix eheu[] values on A_12 scale where
    //# eheu[i] = log_10(N_i/N_H) + 12
    //# I *think* GAS comp[] value are comp[i] = N_i/N_tot
    //#print("n ", n)
    double CSNiOverNH = 0.0;
    double convTerm = 0.0;
    double invComp = 1.0;
    //#skip Hydrogen
    for (int i=0; i<n; i++){
        //#if (name[i].strip() != 'H'):
            //#print("element: name ", name[i], " comp[] ", comp[iat[i]])
            for (int j=0; j<cname.length; j++){
                //#print("element: name ", name[i], " cname ", cname[j])
                if ( name[i].trim().equals(cname[j].trim()) ){
                    CSNiOverNH = Math.pow(10.0, (eheu[j]-12.0));
                    //#Assumes 1st GAS element is H
                    for (int k=1; k<n; k++){
                        convTerm += comp[iat[k]]/comp[iat[i]];
                    }
                    invComp = 1.0/CSNiOverNH + convTerm;
                    comp[iat[i]] = 1.0 / invComp;
                    //#print("Abundance fix element: name ", name[i], " cname ", cname[j], " newComp ", comp[iat[i]])
                    convTerm = 0.0; //#reset accumulator
                }
            }
    }
//#c


//#c
//#c Normalize abundances such that SUM(COMP) = 1
//#c
    nspec = n;
    //#name[nspec+1] = ename
    name[nspec] = ename;
    iat[mxspec-1] = mxatom;
    comp[mxatom-1] = 0.0e0;
    neut[mxspec-1] = mxspec;
    nsp1 = nspec + 1;

    for (int n99 = nsp1-1; n99 < mxspec; n99++){
        idel[n99] = 0;
    }


    //#print("GsRead: nspec ", nspec, " natom ", natom)
    if (nspec != 0){

        //System.out.println("natom " + natom);
        for (int j99 = 0; j99 < natom; j99++){
            natsp[j99] = -1;
            comp[j99] = comp[j99]/tcomp;
        }

//#c
//#c Calculate the atomic (molecular) weight of each constituent
//#c
        for (int n99 = 0; n99 < nspec; n99++){

            //#print("name ", name[n], " nel ", nel[n])
            nelt = nel[n99];
            sum0 = 0.0e0;
            iprt = ipr[n99];

            //System.out.println("nelt " + nelt);
            for (int i = 0; i < nelt; i++){

                //#print("i ", i, " n ", n, " zat ", zat[i][n]-1, " indzat ", indzat[zat[i][n]-1])
                gsJ = indzat[zat[i][n99]-1];
                //#print("j ", j)
                nn = indsp[gsJ];
                //#print(" nn ", nn)
                natsp[gsJ] = natsp[gsJ] + 1;
                //System.out.println("gsJ " + gsJ+ " natsp " + natsp[gsJ]);
                iatsp[gsJ][natsp[gsJ]] = n99;
                sum0 = sum0 + nat[i][n99]*awt[nn];
                if (ipr[nn] > iprt){
                    iprt = ipr[nn];
                }
            }
            awt[n99] = sum0;
            ipr[n99] = iprt;
        }

//#c
//#c Fill array of direct indices of species needed for opacity
//#c calculations.
//#c
        if (nix > 0){
            for (int i = 0; i < nix; i++){
                ixn[i] = indx[ixa[0][i]][ixa[1][i]][ixa[2][i]][ixa[3][i]][ixa[4][i]];
                //System.out.println("i "+ i+ " indx " +
                //  indx[ixa[0][i]][ixa[1][i]][ixa[2][i]][ixa[3][i]][ixa[4][i]] +
                //  " ixa[] "+ ixa[0][i]+ " " + ixa[1][i]+ " " + ixa[2][i]+ " " + ixa[3][i]+ " " + ixa[4][i]);
                if (ixn[i] == 149){
                    System.out.println("0*** Warning: Opacity source " + " not included in GAS data tables " + chix[i]);
                }
            }
        }

        //#cis: Try this:
        nlin1+=1;
        nlin2+=1;
        natom+=1;

    } // if nspec !=0 condition

int gsNspec = nspec;
String[] gsName = new String[nspec];
for (int i = 0; i < nspec; i++){
   gsName[i] = name[i];
}
//# Number of atomic elements in GAS package:
int gsNumEls = comp.length;
double[] gsComp = new double[gsNumEls];
for (int i = 0; i < gsNumEls; i++){
   gsComp[i] = comp[i];
}

//#Array of pointers FROM CSPy elements TO GAS elements
//#CAUTION: elements are not contiguous in GAS' species array (are
//# NOT the first gsNumEls entries!)

//#Default value of -1 means CSPy element NOT in GAS package
int[] csp2gas = new int[nelemAbnd];
for (int i = 0; i < nelemAbnd; i++){
   csp2gas[i] = -1;
}
int[] csp2gasIon1 = new int[nelemAbnd];
for (int i = 0; i < nelemAbnd; i++){
   csp2gasIon1[i] = -1;
}
int[] csp2gasIon2 = new int[nelemAbnd];
for (int i = 0; i < nelemAbnd; i++){
   csp2gasIon2[i] = -1;
}

for (int i = 0; i < nelemAbnd; i++){
    for (int j = 0; j < gsNspec; j++){
        if ( cname[i].trim().equals(gsName[j].trim()) ){
            csp2gas[i] = j;
        }
        if ( (cname[i].trim()+"+").equals(gsName[j].trim()) ){
            csp2gasIon1[i] = j;
        }
        if ( (cname[i].trim()+"++").equals(gsName[j].trim()) ){
            csp2gasIon2[i] = j;
        }
    }
}
            
//#print("csp2gas ", csp2gas)

double[][] gsLogk = new double[5][150];    
for (int i = 0; i < 5; i++){
   for (int j = 0; j < 150; j++){
      gsLogk[i][j] = logk[i][j];
   }
}

int gsFirstMol = -1; //  # index of 1st molecular species in Gas' species list
for (int i = 0; i < gsNspec; i++){
    gsFirstMol+=1;
    if (gsLogk[0][i] != 0.0){
        break;
    }
}

//# Number of molecular species in GAS package:
int gsNumMols = gsNspec - gsFirstMol;

//# Number of ionic species in GAS package:
int gsNumIons = gsNspec - gsNumEls - gsNumMols;
//#print("gsNspec ", gsNspec, " gsFirstMol ", gsFirstMol, " gsNumMols ", 
//      #gsNumMols, " gsNumIon ", gsNumIons)

//Set up for molecules with JOLA bands:
   double jolaTeff = 5000.0;
   int numJola = 7; //for now
   //int numJola = 1; //for now
   String[] jolaSpecies = new String[numJola]; // molecule name
   String[] jolaSystem = new String[numJola]; //band system
   //int[] jolaDeltaLambda = new int[numJola]; //band system
   String[] jolaWhichF = new String[numJola];

   if (teff <= jolaTeff){

     jolaSpecies[0] = "TiO"; // molecule name
     jolaSystem[0] = "TiO_C3Delta_X3Delta"; //band system //DeltaLambda=0
     jolaWhichF[0] = "Jorgensen";
     //jolaDeltaLambda[0] = 0; 
     jolaSpecies[1] = "TiO"; // molecule name
     jolaSystem[1] = "TiO_c1Phi_a1Delta"; //band system //DeltaLambda=1
     jolaWhichF[1] = "Jorgensen";
     //jolaDeltaLambda[1] = 1; 
     jolaSpecies[2] = "TiO"; // molecule name
     jolaSystem[2] = "TiO_A3Phi_X3Delta"; //band system //DeltaLambda=0
     jolaWhichF[2] = "Jorgensen";
     //jolaDeltaLambda[2] = 1; 
     jolaSpecies[3] = "TiO"; // molecule name
     jolaSystem[3] = "TiO_B3Pi_X3Delta"; //band system 
     jolaWhichF[3] = "Jorgensen";
     jolaSpecies[4] = "TiO"; // molecule name
     jolaSystem[4] = "TiO_E3Pi_X3Delta"; //band system  
     jolaWhichF[4] = "Jorgensen";
     jolaSpecies[5] = "TiO"; // molecule name
     jolaSystem[5] = "TiO_b1Pi_a1Delta"; //band system 
     jolaWhichF[5] = "Jorgensen";
     jolaSpecies[6] = "TiO"; // molecule name
     jolaSystem[6] = "TiO_b1Pi_d1Sigma"; //band system
     jolaWhichF[6] = "Jorgensen";

    //#"G-band" at 4300 A - MK classification diagnostic:
    //#Needs Allen's approach to getting f
    //#jolaSpecies[7] = "CH" #// molecule name
    //#jolaSystem[7] = "CH_A2Delta_X2Pi" #//band system  
    //#jolaWhichF[7] = "Allen"


   }

  double ATot = 0.0;
  double thisAz, eheuScale;

     // Set value of eheuScale for new metallicity options. 06/17 lburns
      if (logHeFe != 0.0) {
           eheu[1] = eheu[1] + logHeFe;
        }
        if (logAlphaFe != 0.0) {
           eheu[7] = eheu[7] + logAlphaFe;
           eheu[9] = eheu[9] + logAlphaFe;
           eheu[11] = eheu[11] + logAlphaFe;
           eheu[13] = eheu[13] + logAlphaFe;
           eheu[15] = eheu[15] + logAlphaFe;
           eheu[17] = eheu[17] + logAlphaFe;
           eheu[19] = eheu[19] + logAlphaFe;
           eheu[21] = eheu[21] + logAlphaFe;
        }
        if (logCO > 0.0) {
           eheu[5] = eheu[5] + logCO;
           //console.log("logCO " + logCO);
        }
        if (logCO < 0.0) {
           eheu[7] = eheu[7] + Math.abs(logCO);
           //console.log("logCO " + logCO);
        }
        //console.log("logCO " + logCO);


  for (int i = 0; i < nelemAbnd; i++){
     eheuScale = eheu[i];  //default initialization //still base 10
     if (i > 1){ //if not H or He
        eheuScale = eheu[i] + log10ZScale; //still base 10  
     }
     //logAz[i] = logE10 * (eheu[i] - 12.0); //natural log
     logAz[i] = logE10 * (eheuScale - 12.0); //natural log
     thisAz = Math.exp(logAz[i]);
     ATot = ATot + thisAz;
     //System.out.println("i " + i + " logAz " + logE*logAz[i]);
  }
  double logATot = Math.log(ATot); //natural log

        //double sedThresh = 4.0; //line to continuum extinction threshold for 
                                 // inclusion as low resolution line in overall SED
        ////Output files:
        //String outfile = "gray_structure."
        //        + teffStr + "-" + loggStr + "-" + logKappaStr + ".out";
        //String specFile = "gray_spectrum."
        //        + teffStr + "-" + loggStr + "-" + logKappaStr + ".out";
        //String lineFile = "voigt_line."
        //        + teffStr + "-" + loggStr + "-" + logKappaStr + "-" + xitStr + ".out";

        //log_10 Rosseland optical depth scale  
        double tauRos[][] = TauScale.tauScale(numDeps, log10MinDepth, log10MaxDepth);



   //Apr 2016: Replace the following initial guesses with the following PSEUDOCODE:
   //
   // PHOENIX models at Teff0=5000 K, log(g0)=4.5, M0=0.0 (linear "zscl" = 10.0^M)
   //                   Teff0=10000 K, log(g0)=4.0, M0=0.0 (linear "zscl" = 10.0^M)
   //                   --> Tk0(tau), Pe0(tau), Pg0(tau)
   //
   //From Gray 3rd Ed. Ch.9, esp p. 189, 196
   // 1) Tk(tau)=Teff/Teff0*tk0(tau)
   // 2) Pg(tau)=(g/g0)^exp * Pg0(tau); exp = 0.64(bottom) - 0.54(top) for "cool" models
   //                                   exp = 0.85(bottom) - 0.53(top) for "hotter" models
   //    Pg(tau)= zscl^-0.333*Pg0(tau) if metals neutral - cooler models  
   //    Pg(tau)= zscl^-0.5*Pg0(tau) if metals ionized - hotter models
   //    Pg(tau) = {(1+4A_He)/(1+4A_He0)}^2/3 * Pg0(tau)  

   // 3) Pe(tau)=(g/g0)^exp * Pe0(tau); exp = 0.33(bottom) - 0.48(top) for "cool" models
   //                                   exp = 0.82(bottom) - 0.53(top) for "hotter" models
   //    Pe(tau)=exp(omega*Teff)/exp(omega*Teff0)* Pe0(tau), Teff < 10000 K
   //             - omega = 0.0015@log(tau)=1.0 & 0.0012@log(tau)=-1 to -3   
   //    Pe(tau)= zscl^+0.333*Pe0(tau) if metals neutral - cooler models  
   //    Pe(tau)= zscl^+0.5*Pe0(tau) if metals ionized - hotter models  
   //    Pe(tau) = {(1+4A_He)/(1+4A_He0)}^1/3 * Pe0(tau)  

        //Now do the same for the Sun, for reference:
        double[][] tempSun = ScaleSolar.phxSunTemp(teffSun, numDeps, tauRos);
        //Now do the same for the Sun, for reference:
        double[][] pGasSunGuess = ScaleSolar.phxSunPGas(gravSun, numDeps, tauRos);
        double[][] NeSun = ScaleSolar.phxSunNe(gravSun, numDeps, tauRos, tempSun, zScaleSun);
        double[][] kappaSun = ScaleSolar.phxSunKappa(numDeps, tauRos, zScaleSun);
        double[] mmwSun = State.mmwFn(numDeps, tempSun, zScaleSun);
        double[][] rhoSun = State.massDensity(numDeps, tempSun, pGasSunGuess, mmwSun, zScaleSun);
        //double pressSun[][] = Hydrostat.hydrostatic(numDeps, gravSun, tauRos, kappaSun, tempSun);
        double pGasSun[][] = Hydrostat.hydroFormalSoln(numDeps, gravSun, tauRos, kappaSun, tempSun, pGasSunGuess);
        //
        // BEGIN Initial guess for Sun section:
        //
        //Rescaled  kinetic temeprature structure: 
        //double F0Vtemp = 7300.0;  // Teff of F0 V star (K)                           
        double[][] temp = new double[2][numDeps];
        if (teff < F0Vtemp) {
            if (logg > 3.5){
               //We're a cool dwarf! - rescale from 5000 K reference model 
               temp = ScaleT5000.phxRefTemp(teff, numDeps, tauRos);
            } else {
               //We're a cool giant - rescale from 4250g20 reference model
               temp = ScaleT4250g20.phxRefTemp(teff, numDeps, tauRos);
            }
        } else if (teff >= F0Vtemp) {
            //We're a HOT star! - rescale from Teff=10000 reference model! 
            temp = ScaleT10000.phxRefTemp(teff, numDeps, tauRos);
        }

        //Scaled from Phoenix solar model:
        double[][] guessPGas = new double[2][numDeps];
        double[][] guessPe = new double[2][numDeps];
        double[][] guessNe = new double[2][numDeps];
        //double[][] guessKappa = new double[2][numDeps];
        if (teff < F0Vtemp) {
            if (logg > 3.5){
               //We're a cool dwarf! - rescale from 5000 K reference model 
               // logAz[1] = log_e(N_He/N_H)
               guessPGas = ScaleT5000.phxRefPGas(grav, zScale, logAz[1], numDeps, tauRos);
               guessPe = ScaleT5000.phxRefPe(teff, grav, numDeps, tauRos, zScale, logAz[1]);
               guessNe = ScaleT5000.phxRefNe(numDeps, temp, guessPe); 
               //Ne = ScaleSolar.phxSunNe(grav, numDeps, tauRos, temp, kappaScale);
               //guessKappa = ScaleSolar.phxSunKappa(numDeps, tauRos, kappaScale);
             } else {
               //We're a cool giant - rescale from 4250g20 reference model
               guessPGas = ScaleT4250g20.phxRefPGas(grav, zScale, logAz[1], numDeps, tauRos);
               guessPe = ScaleT4250g20.phxRefPe(teff, grav, numDeps, tauRos, zScale, logAz[1]);
               guessNe = ScaleT4250g20.phxRefNe(numDeps, temp, guessPe);
             }
        } else if (teff >= F0Vtemp) {
            //We're a HOT star!! - rescale from Teff=10000 reference model 
            // logAz[1] = log_e(N_He/N_H)
            guessPGas = ScaleT10000.phxRefPGas(grav, zScale, logAz[1], numDeps, tauRos);
            guessPe = ScaleT10000.phxRefPe(teff, grav, numDeps, tauRos, zScale, logAz[1]);
            guessNe = ScaleT10000.phxRefNe(numDeps, temp, guessPe);
            //logKapFudge = -1.5;  //sigh - don't ask me - makes the Balmer lines show up around A0 
        }

     logNz = State.getNz(numDeps, temp, guessPGas, guessPe, ATot, nelemAbnd, logAz);
     for (int i = 0 ; i < numDeps; i++){ 
        logNH[i] = logNz[0][i];
 //set the initial guess H^+ number density to the e^-1 number density
        masterStagePops[0][1][i] = guessPe[1][i]; //iElem = 0: H; iStage = 1: II
        //System.out.println("i " + i + " logNH[i] " + logE*logNH[i]);
     } 

//Load the total no. density of each element into the nuetral stage slots of the masterStagePops array as a first guess at "species B" neutral
//populations for the molecular Saha eq. - Reasonable first guess at low temp where molecuales form

   for (int iElem = 0; iElem < nelemAbnd; iElem++){
      for (int iD = 0; iD < numDeps; iD++){
         masterStagePops[iElem][0][iD] = logNz[iElem][iD];
      }
   }

        // END initial guess for Sun section
        //
        // *********************
   //Jul 2016: Replace the following procedure for model building with the following PSEUDOCODE:
   //
   // 1st guess Tk(tau), Pe(tau), Pg(tau) from rescaling reference hot or cool model above
   // 1) Converge Pg-Pe relation for Az abundance distribution and  T_Kin(Tau)
   //   assuming all free e^-s from single ionizations - *inner* convergence
   // 2) Compute N_H from converged Pg-Pe relation 
   //    A_Tot = Sigma_z(A_z)
   //         -> N_H(tau) = (Pg(tau)-Pe(tau))/{kTk(tau)A_Tot}
   //         -> N_z(tau) = A_z * N_H(tau) 
   // 3) Obtain N_HI, N_HII, N_HeI, and N_HeII at all depths
   // 4)Get rho(tau) = Sigma_z(m_z*N_z(tau)) and mu(tau) = rho(tau) / N(tau)
   //    - needed for kappa in cm^2/g 
   // 5) kappa(tau) from Gray Ch. 8 sources - H, He, and e^- oapcity sources only
   // 6) P_Tot(tau) from HSE on tau scale with kappa from 4)
   //    - PRad(tau) from Tk(tau)
   //    - New Pg(tau) from P_Tot(tau)-PRad(tau)
   // 7) Iterate Pg - kappa relation to convergence - *outer* convergence
   // 8)Get rho(tau) = Sigma_z(m_z*N_z(tau)) and mu(tau) = rho(tau) / N(tau)
   //   and depth scale
   //
   //  ** Atmospheric structure converged **
   //
   // THEN for spectrum synthesis:
   //
   // 1) Separate iteration of Ne against ionization fractions fI(tau), fII, fIII, fIV, fV(tau) 
   // from Saha(Tk(tau), Pe(tau))
   //   AND coupled molecualr equilibrium 
   //    -  LevelPops.stagePops2() 
   // 2) new Ne(tau) from Sigma_z{fI(tau) .... 5*fV(tau) * N_z}
   // 3) Iterate
   // 4) Temp correction??   


//    **** STOP ****  No - do we *really* need N_HI, ... for kappa if we use rho in HSE? - Yes - needed even if kappa
//    is in cm^-1 instead of cm^2/g - sigh


  String species = " "; //default initialization
  double rho[][] = new double[2][numDeps];
  double[][] tauOneStagePops = new double[nelemAbnd][numStages];
  double unity = 1.0;
  double zScaleList = 1.0; //initialization   
  double[][] log10UwAArr = new double[numStages][5];
  for (int i = 0; i < numStages; i++){
    for (int k = 0; k < log10UwAArr[0].length; k++){
        log10UwAArr[i][k] = 0.0; //lburns default initialization - logarithmic
    }
  }
 
//Ground state ionization E - Stage I (eV) 
  double[] chiIArr = new double[numStages];
// safe initialization:
  for (int i = 0; i < numStages; i++){
      chiIArr[i] = 999999.0;
  }
// //Ground state ionization E - Stage II (eV)
//
 double[][] masterMolPops = new double[gsNumMols][numDeps];
//initialize masterMolPops for mass density (rho) calculation:
  for (int i = 0; i < gsNumMols; i++){
    for (int j = 0; j < numDeps; j++){
       masterMolPops[i][j] = -49.0;  //these are logarithmic
    }
  }

//
  double[][] newNe = new double[2][numDeps]; 
  double[][] newPe = new double[2][numDeps]; 
  double[][] logNums = new double[numStages][numDeps]; 
//

  double[] Ng = new double[numDeps];
  double[] mmw = new double[numDeps];
  double logMmw;
  double[][] logKappa = new double[numLams][numDeps];
  double[][] logKappaHHe = new double[numLams][numDeps];
  double[][] logKappaMetalBF = new double[numLams][numDeps];
  double[][] logKappaRayl = new double[numLams][numDeps];
  double[][] kappaRos = new double[2][numDeps];
  double[][] kappa500 = new double[2][numDeps];
  double[][] pGas = new double[2][numDeps]; 
  double[][] pRad = new double[2][numDeps]; 
  double[] depths = new double[numDeps];
  double[][] newTemp = new double[2][numDeps];


//
//
//We converge the Pgas - Pe relation first under the assumption that all free e^-s are from single ionizations
// a la David Gray Ch. 9.  
// This approach separates converging ionization fractions and Ne for spectrum synthesis purposes from
// converging the Pgas-Pe-N_H-N_He relation for computing the mean opacity for HSE
//
double[] thisTemp = new double[2];
double[] log10UwUArr = new double[5];
double[] log10UwLArr = new double[5];
double chiI, peNumerator, peDenominator, logPhi, logPhiOverPe, logOnePlusPhiOverPe, logPeNumerTerm, logPeDenomTerm;

double log300 = Math.log(300.0);
double log2 = Math.log(2.0);

//#GAS package parameters:
int isolv = 1;
double tol = 1.0e-2;
int maxit = 10;

//#GAS package interface variables:
int neq;
double gsPe0, gsPe, gsMu, gsRho;

double[] gsP0 = new double[40];
double[] topP0 = new double[40];
double[] gsPp = new double[150];

//#For reporting purposes only:
double[][] log10MasterGsPp = new double[gsNspec][numDeps];
for (int iD = 0; iD < numDeps; iD++){
   for (int iSpec = 0; iSpec < gsNspec; iSpec++){
       log10MasterGsPp[iSpec][iD] = -99.0;
   }
}
double thisN;

double GAStemp = 6000.0;

//Begin Pgas-kapp iteration
  for (int pIter = 0; pIter < nOuterIter; pIter++){
//

    if (teff <= GAStemp){
    //#if (teff <= 100000.0):   #test
        
        for (int iD = 0; iD < numDeps; iD++){
            //System.out.println("isolv "+ isolv+ " temp "+ temp[0][iD]+ " guessPGas "+ guessPGas[0][iD]);

            double[] returnGasEst = CSGasEst.gasest(isolv, temp[0][iD], guessPGas[0][iD]);
         // Unpack structure returned by GasEst - sigh!
            neq = (int) returnGasEst[0];
            gsPe0 = returnGasEst[1];
            for (int k99 = 2; k99 < 42; k99++){
               gsP0[k99-2] = returnGasEst[k99];
            }
       
            if (iD == 1){
                for (int iSpec = 0; iSpec < 40; iSpec++){
                   topP0[iSpec] = 0.5 * gsP0[iSpec]; 
                }
            } 
                
            //#Upper boundary causes problems:
            if (pIter > 0 && iD == 0){
                gsPe0 = 0.5 * newPe[0][1];
                for (int iSpec = 0; iSpec < 40; iSpec++){
                   gsP0[iSpec] = topP0[iSpec]; 
                }
            }
 
            //System.out.println("iD "+ iD+ " gsPe0 "+ gsPe0+ " gsP0[0] "+ gsP0[0]+ " neq "+ neq);

            double[] returnGas = CSGas.gas(isolv, temp[0][iD], guessPGas[0][iD], gsPe0, gsP0, neq, tol, maxit);

            gsPe = returnGas[0];
            gsRho = returnGas[1];
            gsMu = returnGas[2];
            for (int k99 = 3; k99 < 153; k99++){
               gsPp[k99-3] = returnGas[k99];
            }

            //System.out.println("iD "+ iD+ " gsPe "+ gsPe+ " gsPp[0] "+ gsPp[0]+ " gsMu "+ gsMu+ " gsRho "+ gsRho);

        
            newPe[0][iD] = gsPe;
            newPe[1][iD] = Math.log(gsPe);
            newNe[0][iD] = gsPe / Useful.k / temp[0][iD];
            newNe[1][iD] = Math.log(newNe[0][iD]);
            guessPe[0][iD] = newPe[0][iD];
            guessPe[1][iD] = newPe[1][iD];
            guessNe[0][iD] = newNe[0][iD];
            guessNe[1][iD] = newNe[1][iD];       
        
            rho[0][iD] = gsRho;
            rho[1][iD] = Math.log(gsRho);
            mmw[iD] = gsMu * Useful.amu;
            
            //#Take neutral stage populations for atomic species from GAS: 
            for (int iElem = 0; iElem < nelemAbnd; iElem++){
            
                if (csp2gas[iElem] != -1){
                    //#element is in GAS package:
                    thisN = gsPp[csp2gas[iElem]] / Useful.k / temp[0][iD];
                    masterStagePops[iElem][0][iD] = Math.log(thisN);
                    //if (iD == 10 && iElem == 0){
                    //   console.log("iD " + iD + " iElem " + iElem + " mSP[][] " + masterStagePops[iElem][0][iD];
                    //}
                }
            }
 
            //#print("iD ", iD, cname[19], gsName[csp2gas[19]], " logNCaI ", logE*masterStagePops[19][0][iD])
            for (int i = 0; i < gsNumMols; i++){
                thisN = gsPp[i+gsFirstMol] / Useful.k / temp[0][iD];
                masterMolPops[i][iD] = Math.log(thisN);
            }
        
            //#Needed  now GAS??  
            for (int iA = 0; iA < nelemAbnd; iA++){
                if (csp2gas[iA] != -1){
                    //#element is in GAS package:
                    logNz[iA][iD] = Math.log10(gsPp[csp2gas[iA]]) - Useful.logK() - temp[1][iD];
                }
            }

        } //iD loop
                    
        for (int iElem = 0; iElem < 26; iElem++){
            species = cname[iElem] + "I";
            chiIArr[0] = IonizationEnergy.getIonE(species);
            //THe following is a 2-element vector of temperature-dependent partitio fns, U, 
            // that are base e log_e U
            log10UwAArr[0] = PartitionFn.getPartFn2(species); //base e log_e U
            species = cname[iElem] + "II";
            chiIArr[1] = IonizationEnergy.getIonE(species);
            log10UwAArr[1] = PartitionFn.getPartFn2(species); //base e log_e U
            species = cname[iElem] + "III";
            chiIArr[2] = IonizationEnergy.getIonE(species);
            log10UwAArr[2] = PartitionFn.getPartFn2(species); //base e log_e U
            species = cname[iElem] + "IV";
            chiIArr[3] = IonizationEnergy.getIonE(species);
            log10UwAArr[3] = PartitionFn.getPartFn2(species); //base e log_e U
            species = cname[iElem] + "V";
            chiIArr[4] = IonizationEnergy.getIonE(species);
            log10UwAArr[4] = PartitionFn.getPartFn2(species); //base e log_e U
            species = cname[iElem] + "VI";
            chiIArr[5] = IonizationEnergy.getIonE(species);
            log10UwAArr[5] = PartitionFn.getPartFn2(species); //base e log_e U
            //double logN = (eheu[iElem] - 12.0) + logNH;


        
            //#Neeed?  Now GAS:
            logNums = LevelPopsGasServer.stagePops3(masterStagePops[iElem][0], guessNe, chiIArr, log10UwAArr, 
                                //#thisNumMols, logNumBArr, dissEArr, log10UwBArr, logQwABArr, logMuABArr, \
                                numDeps, temp);

        for (int iStage = 0; iStage < numStages; iStage++){
            for (int iTau = 0; iTau < numDeps; iTau++){
        
                masterStagePops[iElem][iStage][iTau] = logNums[iStage][iTau];

            }
        }
     }
        
   } // end teff <= GAStemp     

   if (teff > GAStemp){  //#teff > FoVtemp:

//  Converge Pg-Pe relation starting from intital guesses at Pg and Pe
//  - assumes all free electrons are from single ionizations
//  - David Gray 3rd Ed. Eq. 9.8:

      for (int neIter = 0; neIter < nInnerIter; neIter++){
    //System.out.println("iD    logE*newPe[1][iD]     logE*guessPe[1]     logE*guessPGas[1]");
        for (int iD = 0; iD < numDeps; iD++){
    //re-initialize accumulators:
       thisTemp[0] = temp[0][iD];
       thisTemp[1] = temp[1][iD];
       peNumerator = 0.0; 
       peDenominator = 0.0;
       for (int iElem = 0; iElem < nelemAbnd; iElem++){
           species = cname[iElem] + "I";
           chiI = IonizationEnergy.getIonE(species);
    //THe following is a 2-element vector of temperature-dependent partitio fns, U, 
    // that are base e log_e U
           log10UwLArr = PartitionFn.getPartFn2(species); //base e log_e U
           species = cname[iElem] + "II";
           log10UwUArr = PartitionFn.getPartFn2(species); //base e log_e U
           logPhi = LevelPopsGasServer.sahaRHS(chiI, log10UwUArr, log10UwLArr, thisTemp);
           logPhiOverPe = logPhi - guessPe[1][iD];
           logOnePlusPhiOverPe = Math.log(1.0 + Math.exp(logPhiOverPe)); 
           logPeNumerTerm = logAz[iElem] + logPhiOverPe - logOnePlusPhiOverPe;
           peNumerator = peNumerator + Math.exp(logPeNumerTerm);
           logPeDenomTerm = logAz[iElem] + Math.log(1.0 + Math.exp(logPeNumerTerm));
           peDenominator = peDenominator + Math.exp(logPeDenomTerm);
       } //iElem chemical element loop
       newPe[1][iD] = guessPGas[1][iD] + Math.log(peNumerator) - Math.log(peDenominator); 
       newPe[0][iD] = Math.exp(newPe[1][iD]);
       guessPe[1][iD] = newPe[1][iD];
       guessPe[0][iD] = Math.exp(guessPe[1][iD]);
    } //iD depth loop

  } //end Pg_Pe iteration neIter

    for (int iD = 0; iD < numDeps; iD++){
       newNe[1][iD] = newPe[1][iD] - temp[1][iD] - Useful.logK();
       newNe[0][iD] = Math.exp(newNe[1][iD]);
       guessNe[1][iD] = newNe[1][iD];
       guessNe[0][iD] = newNe[0][iD];
    }

//
//Refine the number densities of the chemical elements at all depths  
     logNz = State.getNz(numDeps, temp, guessPGas, guessPe, ATot, nelemAbnd, logAz);
     for (int i = 0 ; i < numDeps; i++){ 
        logNH[i] = logNz[0][i];
        //System.out.println("i " + i + " logNH[i] " + logE*logNH[i]);
     } 

//
//  Compute ionization fractions of H & He for kappa calculation 
//
//  Default inializations:
       zScaleList = 1.0; //initialization   
       //these 2-element temperature-dependent partition fns are logarithmic  

//
////H & He only for now... we only compute H, He, and e^- opacity sources: 
   //for (int iElem = 0; iElem < 2; iElem++){
//H to Fe only for now... we only compute opacity sources for elements up to Fe: 
//FLAG!
   for (int iElem = 0; iElem < 26; iElem++){
   //for (int iElem = 0; iElem < 2; iElem++){
       species = cname[iElem] + "I";
       chiIArr[0] = IonizationEnergy.getIonE(species);
    //THe following is a 2-element vector of temperature-dependent partitio fns, U, 
    // that are base e log_e U
       log10UwAArr[0] = PartitionFn.getPartFn2(species); //base e log_e U
       species = cname[iElem] + "II";
       chiIArr[1] = IonizationEnergy.getIonE(species);
       log10UwAArr[1] = PartitionFn.getPartFn2(species); //base e log_e U
       species = cname[iElem] + "III";
       chiIArr[2] = IonizationEnergy.getIonE(species);
       log10UwAArr[2] = PartitionFn.getPartFn2(species); //base e log_e U
       species = cname[iElem] + "IV";
       chiIArr[3] = IonizationEnergy.getIonE(species);
       log10UwAArr[3] = PartitionFn.getPartFn2(species); //base e log_e U
       species = cname[iElem] + "V";
       chiIArr[4] = IonizationEnergy.getIonE(species);
       log10UwAArr[4] = PartitionFn.getPartFn2(species); //base e log_e U
       species = cname[iElem] + "VI";
       chiIArr[5] = IonizationEnergy.getIonE(species);
       log10UwAArr[5] = PartitionFn.getPartFn2(species); //base e log_e U
       //double logN = (eheu[iElem] - 12.0) + logNH;

       logNums = LevelPopsGasServer.stagePops(logNz[iElem], guessNe, chiIArr, log10UwAArr,
                     numDeps, temp);

     for (int iStage = 0; iStage < numStages; iStage++){
          for (int iTau = 0; iTau < numDeps; iTau++){

            masterStagePops[iElem][iStage][iTau] = logNums[iStage][iTau];
 //save ion stage populations at tau = 1:
       } //iTau loop
    } //iStage loop
  } //iElem loop

    
//Get mass density from chemical composition: 
     rho = State.massDensity2(numDeps, nelemAbnd, logNz, cname);

//Total number density of gas particles: nuclear species + free electrons:
//AND
 //Compute mean molecular weight, mmw ("mu"):
    for (int i = 0; i < numDeps; i++){
      Ng[i] =  newNe[0][i]; //initialize accumulation with Ne 
    }
    for (int i = 0; i < numDeps; i++){
      for (int j = 0; j < nelemAbnd; j++){
         Ng[i] =  Ng[i] + Math.exp(logNz[j][i]); //initialize accumulation 
      }
     logMmw = rho[1][i] - Math.log(Ng[i]);  // in g
     mmw[i] = Math.exp(logMmw); 
    }

 } //end teff > GAStemp

//H & He only for now... we only compute H, He, and e^- opacity sources: 
      logKappaHHe = Kappas.kappas2(numDeps, newPe, zScale, temp, rho,
                     numLams, lambdaScale, logAz[1],
                     masterStagePops[0][0], masterStagePops[0][1], 
                     masterStagePops[1][0], masterStagePops[1][1], newNe, 
                     teff, logTotalFudge);

//Add in metal b-f opacity from adapted Moog routines:
      logKappaMetalBF = KappasMetal.masterMetal(numDeps, numLams, temp, lambdaScale, masterStagePops);
//Add in Rayleigh scattering opacity from adapted Moog routines:
      logKappaRayl = KappasRaylGas.masterRayl(numDeps, numLams, temp, lambdaScale, masterStagePops, gsName, gsFirstMol, masterMolPops);

  //for (int i = 0; i < numLams; i++){
  //  System.out.println("logKappaHHe " + logE*logKappaHHe[i][36]); 
  //}
  //for (int i = 0; i < numLams; i++){
  //  System.out.println("logKappaMetalBF " + logE*logKappaMetalBF[i][36]); 
 // }
  //for (int i = 0; i < numLams; i++){
  //  System.out.println("logKappaRayl " + logE*logKappaRayl[i][36]); 
 // }

//Convert metal b-f & Rayleigh scattering opacities to cm^2/g and sum up total opacities
   double logKapMetalBF, logKapRayl, kapContTot;
   for (int iL = 0; iL < numLams; iL++){
       for (int iD = 0; iD < numDeps; iD++){
          logKapMetalBF = logKappaMetalBF[iL][iD] - rho[1][iD]; 
          logKapRayl = logKappaRayl[iL][iD] - rho[1][iD]; 
          kapContTot = Math.exp(logKappaHHe[iL][iD]) + Math.exp(logKapMetalBF) + Math.exp(logKapRayl); 
          logKappa[iL][iD] = Math.log(kapContTot);
       }
   } 

      kappaRos = Kappas.kapRos(numDeps, numLams, lambdaScale, logKappa, temp); 

//Extract the "kappa_500" monochroamtic continuum oapcity scale
// - this means we'll try interpreting the prescribed tau grid (still called "tauRos")as the "tau500" scale
      int it500 = ToolBox.lamPoint(numLams, lambdaScale, 500.0e-7);
      for (int i = 0; i < numDeps; i++){
         kappa500[1][i] = logKappa[it500][i];
         kappa500[0][i] = Math.exp(kappa500[1][i]);
      }

        pGas = Hydrostat.hydroFormalSoln(numDeps, grav, tauRos, kappaRos, temp, guessPGas);
        pRad = Hydrostat.radPress(numDeps, temp);

//Update Pgas guess for iteration:
        for (int iTau = 0; iTau < numDeps; iTau++){
// Now we can update guessPGas:
            guessPGas[0][iTau] = pGas[0][iTau];
            guessPGas[1][iTau] = pGas[1][iTau];
        } 

 } //end Pgas-kappa iteration, nOuter

//diagnostic for ChromaStarDB paper (May 2017):
   ////int tauKapPnt01 = ToolBox.tauPoint(numDeps, tauRos, 0.01);
   ////System.out.println("logTauRos " + logE*tauRos[1][tauKapPnt01] + " temp " + temp[0][tauKapPnt01] + " pGas " + logE*pGas[1][tauKapPnt01]);
   //System.out.println("tau " + " temp " + " logPgas " + " logPe " + " logRho "); 
   //for (int iD = 1; iD < numDeps; iD+=5){
   //    System.out.println(" " + tauRos[0][iD] + " " + temp[0][iD] + " " +  logE*pGas[1][iD] + " " + logE*newPe[1][iD] + " " + logE*rho[1][iD]); 
   //}
   //for (int iL = 0; iL < numLams; iL++){
   //    //System.out.println(" " + lambdaScale[iL] + " " + logE*logKappa[iL][tauKapPnt01]); 
   //    System.out.println(" " + lambdaScale[iL]); 
   //    for (int iD = 1; iD < numDeps; iD+=5){
   //       //If we want *mass* extinction, we convert it in the IDL routine...
   //        System.out.println(" " + logE*(logKappa[iL][iD]+rho[1][iD]));  //cm^-1
   //        //System.out.println(" " + logE*(logKappa[iL][iD]));  //cm^2/g
   //    }
   //} 
   ////int tauKapPnt1 = ToolBox.tauPoint(numDeps, tauRos, 1.0);
   ////System.out.println("logTauRos " + logE*tauRos[1][tauKapPnt1] + " temp " + temp[0][tauKapPnt1] + " pGas " + logE*pGas[1][tauKapPnt1]);
   ////for (int iL = 0; iL < numLams; iL++){
   ////    //System.out.println(" " + lambdaScale[iL] + " " + logE*logKappa[iL][tauKapPnt1]); 
   ////} 

        // Then construct geometric depth scale from tau, kappa and rho
        depths = DepthScale.depthScale(numDeps, tauRos, kappaRos, rho);

        //int numTCorr = 10;  //test
        int numTCorr = 0;
        for (int i = 0; i < numTCorr; i++) {
            //newTemp = TCorr.tCorr(numDeps, tauRos, temp);
            newTemp = MulGrayTCorr.mgTCorr(numDeps, teff, tauRos, temp, rho, kappaRos);
            //newTemp = MulGrayTCorr.mgTCorr(numDeps, teff, tauRos, temp, rho, kappa500);
            for (int iTau = 0; iTau < numDeps; iTau++) {
                temp[1][iTau] = newTemp[1][iTau];
                temp[0][iTau] = newTemp[0][iTau];
            }
        }

        /*
         //Convection:
         // Teff below which stars are convective.  
         //  - has to be finessed because Convec.convec() does not work well :-(
         double convTeff = 6500.0;
         double[][] convTemp = new double[2][numDeps];
         if (teff < convTeff) {
         convTemp = Convec.convec(numDeps, tauRos, depths, temp, press, rho, kappaRos, kappaSun, zScale, teff, logg);

         for (int iTau = 0; iTau < numDeps; iTau++) {
         temp[1][iTau] = convTemp[1][iTau];
         temp[0][iTau] = convTemp[0][iTau];
         }
         }
         */
        boolean ifTcorr = false;
        boolean ifConvec = false;
        if ((ifTcorr == true) || (ifConvec == true)) {
            //Recall hydrostat with updates temps            
            //Recall state withupdated Press                    
            //recall kappas withupdates rhos
            //Recall depths with re-updated kappas
        }

//###################################################
//#
//#
//#
//# Re-converge Ionization/chemical equilibrium WITH molecules
//#
//#
//#
//####################################################

//
// Now that the atmospheric structure is settled: 
// Separately converge the Ne-ionization-fractions-molecular equilibrium for
// all elements and populate the ionization stages of all the species for spectrum synthesis:
//
//stuff to save ion stage pops at tau=1:
  int iTauOne = ToolBox.tauPoint(numDeps, tauRos, unity);

//
//  Default inializations:
       zScaleList = 1.0; //initialization
       //these 2-element temperature-dependent partition fns are logarithmic

//#Final run through Phil's GAS EOS/Chemic equil. for consistency with last HSE call above:

    
if (teff <= GAStemp){
        
    for (int iD = 0; iD < numDeps; iD++){    
        
        //#print("isolv ", isolv, " temp ", temp[0][iD], " guessPGas ", guessPGas[0][iD])
        double[] returnGasEst = CSGasEst.gasest(isolv, temp[0][iD], guessPGas[0][iD]);
        // Unpack structure returned by GasEst - sigh!
        neq = (int) returnGasEst[0];
        gsPe0 = returnGasEst[1];
        for (int k99 = 2; k99 < 42; k99++){
           gsP0[k99-2] = returnGasEst[k99];
        }
        
        //#print("iD ", iD, " gsPe0 ", gsPe0, " gsP0 ", gsP0, " neq ", neq)

        double[] returnGas = CSGas.gas(isolv, temp[0][iD], guessPGas[0][iD], gsPe0, gsP0, neq, tol, maxit);

        gsPe = returnGas[0];
        gsRho = returnGas[1];
        gsMu = returnGas[2];
        for (int k99 = 3; k99 < 153; k99++){
            gsPp[k99-3] = returnGas[k99];
        }
     
        for (int iSpec = 0; iSpec < gsNspec; iSpec++){
            log10MasterGsPp[iSpec][iD] = Math.log10(gsPp[iSpec]); 
        }
        
        newPe[0][iD] = gsPe;
        newPe[1][iD] = Math.log(gsPe);
        newNe[0][iD] = gsPe / Useful.k / temp[0][iD];
        newNe[1][iD] = Math.log(newNe[0][iD]);
        guessPe[0][iD] = newPe[0][iD];
        guessPe[1][iD] = newPe[1][iD];
        
        rho[0][iD] = gsRho;
        rho[1][iD] = Math.log(gsRho);
        mmw[iD] = gsMu * Useful.amu;
        
        //#print("iD ", iD, " logT ", logE*temp[1][iD], " logNe ", logE*newNe[1][iD], " logRho ", logE*rho[1][iD], " mmw ", logE*math.log(mmw[iD]*Useful.amu()) )
        
        
        //#Take neutral stage populations for atomic species from GAS: 
        for (int iElem = 0; iElem < nelemAbnd; iElem++){
            
            if (csp2gas[iElem] != -1){
                //#element is in GAS package:
                thisN = gsPp[csp2gas[iElem]] / Useful.k / temp[0][iD];    
                masterStagePops[iElem][0][iD] = Math.log(thisN);
            }

         }
            
        //#print("iD ", iD, cname[19], gsName[csp2gas[19]], " logNCaI ", logE*masterStagePops[19][0][iD])
        for (int i = 0; i < gsNumMols; i++){
            thisN = gsPp[i+gsFirstMol] / Useful.k / temp[0][iD];
            masterMolPops[i][iD] = Math.log(thisN);
        }
        
        //#Needed  now GAS??  
        for (int iA = 0; iA < nelemAbnd; iA++){
            if (csp2gas[iA] != -1){
                //#element is in GAS package:
                logNz[iA][iD] = Math.log10(gsPp[csp2gas[iA]]) - Useful.logK() - temp[1][iD];
            }
        }

    } //#end iD loop        

    //#Catch species NOT in Phil's GAS Chem. Equil. package   
    for (int iElem = 0; iElem < nelemAbnd; iElem++){
        
        if (csp2gas[iElem] == -1){

            species = cname[iElem] + "I";
            chiIArr[0] = IonizationEnergy.getIonE(species);
            //The following is a 2-element vector of temperature-dependent partitio fns, U,
            // that are base e log_e U
            log10UwAArr[0] = PartitionFn.getPartFn2(species); //base e log_e U
            species = cname[iElem] + "II";
            chiIArr[1] = IonizationEnergy.getIonE(species);
            log10UwAArr[1] = PartitionFn.getPartFn2(species); //base e log_e U
            species = cname[iElem] + "III";
            chiIArr[2] = IonizationEnergy.getIonE(species);
            log10UwAArr[2] = PartitionFn.getPartFn2(species); //base e log_e U
            species = cname[iElem] + "IV";
            chiIArr[3] = IonizationEnergy.getIonE(species);
            log10UwAArr[3]= PartitionFn.getPartFn2(species); //base 1e log_e U
            species = cname[iElem] + "V";
            chiIArr[4] = IonizationEnergy.getIonE(species);
            log10UwAArr[4]= PartitionFn.getPartFn2(species); //base 1e log_e U
            species = cname[iElem] + "VI";
            chiIArr[5] = IonizationEnergy.getIonE(species);
            log10UwAArr[5]= PartitionFn.getPartFn2(species); //base e log_e U
    
        

            //#Element NOT in GAS package - compute ionization equilibrium:
            logNums = LevelPopsGasServer.stagePops(logNz[iElem], guessNe, chiIArr, log10UwAArr, 
                                                   //#thisNumMols, logNumBArr, dissEArr, log10UwBArr, logQwABArr, logMuABArr, \
                            numDeps, temp);

            for (int iStage = 0; iStage < numStages; iStage++){
               for (int iTau = 0; iTau < numDeps; iTau++){
                  masterStagePops[iElem][iStage][iTau] = logNums[iStage][iTau];
                  //save ion stage populations at tau = 1:
               } //iTau loop
             tauOneStagePops[iElem][iStage] = logNums[iStage][iTauOne];
            } //iStage loop

     } //csp2gas = -1 if

 } //iElem loop

} //end if teff <= GAStemp 

if (teff > GAStemp){

//Iterate the electron densities, ionization fractions, and molecular densities:
//
//FLAG!
 //for (int neIter2 = 0; neIter2 < 3; neIter2++){
 for (int neIter2 = 0; neIter2 < nInnerIter; neIter2++){

   //System.out.println("neIter2 " + neIter2);

   for (int iElem = 0; iElem < nelemAbnd; iElem++){
       species = cname[iElem] + "I";
       chiIArr[0] = IonizationEnergy.getIonE(species);
    //THe following is a 2-element vector of temperature-dependent partitio fns, U,
    // that are base e log_e U
       log10UwAArr[0] = PartitionFn.getPartFn2(species); //base e log_e U
       species = cname[iElem] + "II";
       chiIArr[1] = IonizationEnergy.getIonE(species);
       log10UwAArr[1] = PartitionFn.getPartFn2(species); //base e log_e U
       species = cname[iElem] + "III";
       chiIArr[2] = IonizationEnergy.getIonE(species);
       log10UwAArr[2] = PartitionFn.getPartFn2(species); //base e log_e U
       species = cname[iElem] + "IV";
       chiIArr[3] = IonizationEnergy.getIonE(species);
       log10UwAArr[3]= PartitionFn.getPartFn2(species); //base e log_e U
       species = cname[iElem] + "V";
       chiIArr[4] = IonizationEnergy.getIonE(species);
       log10UwAArr[4]= PartitionFn.getPartFn2(species); //base e log_e U
       species = cname[iElem] + "VI";
       chiIArr[5] = IonizationEnergy.getIonE(species);
       log10UwAArr[5]= PartitionFn.getPartFn2(species); //base e log_e U


       logNums = LevelPopsGasServer.stagePops(logNz[iElem], guessNe, chiIArr, log10UwAArr,
                     numDeps, temp);

     for (int iStage = 0; iStage < numStages; iStage++){
          for (int iTau = 0; iTau < numDeps; iTau++){
            masterStagePops[iElem][iStage][iTau] = logNums[iStage][iTau];
 //save ion stage populations at tau = 1:
       } //iTau loop
       tauOneStagePops[iElem][iStage] = logNums[iStage][iTauOne];
    } //iStage loop

    // #Fill in in PP report:
    for (int iTau = 0; iTau < numDeps; iTau++){

       if (csp2gas[iElem] != -1){
          log10MasterGsPp[csp2gas[iElem]][iTau] = logE*(logNums[0][iTau] + temp[1][iTau] + Useful.logK());
       }
       if (csp2gasIon1[iElem] != -1){
          log10MasterGsPp[csp2gasIon1[iElem]][iTau] = logE*(logNums[1][iTau] + temp[1][iTau] + Useful.logK());
       }
       if (csp2gasIon2[iElem] != -1){
          log10MasterGsPp[csp2gasIon2[iElem]][iTau] = logE*(logNums[2][iTau] + temp[1][iTau] + Useful.logK());
       }

    } //iTau loop        

  } //iElem loop

   double[] log10UwA = new double[5];

//Compute updated Ne & Pe:
     //initialize accumulation of electrons at all depths
     for (int iTau = 0; iTau < numDeps; iTau++){
       newNe[0][iTau] = 0.0;
     }
     for (int iTau = 0; iTau < numDeps; iTau++){
        for (int iElem = 0; iElem < nelemAbnd; iElem++){
          newNe[0][iTau] = newNe[0][iTau]
                   + Math.exp(masterStagePops[iElem][1][iTau])   //1 e^- per ion
                   + 2.0 * Math.exp(masterStagePops[iElem][2][iTau]);   //2 e^- per ion
                   //+ 3.0 * Math.exp(masterStagePops[iElem][3][iTau])   //3 e^- per ion
                   //+ 4.0 * Math.exp(masterStagePops[iElem][4][iTau]);   //3 e^- per ion
        }
        newNe[1][iTau] = Math.log(newNe[0][iTau]);
// Update guess for iteration:
        guessNe[0][iTau] = newNe[0][iTau];
        guessNe[1][iTau] = newNe[1][iTau];
     }

  } //end Ne - ionzation fraction -molecular equilibrium iteration neIter2

} // end teff > GAStemp if

//


        //Okay - Now all the emergent radiation stuff:
        // Set up theta grid
        //  cosTheta is a 2xnumThetas array:
        // row 0 is used for Gaussian quadrature weights
        // row 1 is used for cos(theta) values
        // Gaussian quadrature:
        // Number of angles, numThetas, will have to be determined after the fact
        double cosTheta[][] = Thetas.thetas();
        int numThetas = cosTheta[0].length;

//establish a phi grid for non-axi-symmetric situations (eg. spots, in situ rotation, ...)
//    //number of phi values per quandrant of unit circle centered on sub-stellar point
//        //    in plane of sky:
//        //For geometry calculations: phi = 0 is direction of positive x-axis of right-handed
//        // 2D Cartesian coord system in plane of sky with origin at sub-stellar point (phi
//        // increases CCW)
    int numPhiPerQuad = 9;
    int numPhi = 4 * numPhiPerQuad;
    double numPhiD = (double) numPhi;
    double[] phi = new double[numPhi];
    //Compute phi values in whole range (0 - 2pi radians):
    double delPhi = 2.0 * Math.PI / numPhiD;
    double ii;
    for (int i = 0; i < numPhi; i++){
      ii = (double) i;
      phi[i] = delPhi * ii;
    }
    

        boolean lineMode;

        //
        // ************
        //
        //  Spectrum synthesis section:
        // Set up multi-Gray continuum info:
        double isCool = 7300.0;  //Class A0

        //Set up multi-gray opacity:
        // lambda break-points and gray levels:
        // No. multi-gray bins = num lambda breakpoints +1
        double minLambda = 30.0;  //nm
        double maxLambda = 1.0e6;  //nm

// JOLA molecular bands here:
// Just-overlapping line approximation treats molecular ro-vibrational bands as pseudo-continuum
//opacity sources by "smearing" out the individual rotational fine-structure lines
//See 1982A&A...113..173Z, Zeidler & Koester, 1982


        double jolaOmega0;  //band origin ?? //Hz OR waveno in cm^-1 ??
        //double[] jolaLogF; //total vibrational band oscillator strength (f_v'v")
        double jolaRSqu; //needed for total vibrational band oscillator strength (f_v'v")
        double[] jolaB = new double[2]; // B' value of upper vibational state (energy in cm^-1)??
        double[] jolaLambda = new double[2];
        int jolaDeltaLambda;
        double jolaAlphP = 0.0; // alpha_P - weight of P branch (Delta J = -1)
        double jolaAlphR = 0.0; // alpha_R - weight of R branch (Delta J = 1)
        double jolaAlphQ = 0.0; // alpha_Q - weight of Q branch (Delta J = 0)
//Allen's Astrophysical quantities, 4th Ed., 4.12.2 - 4.13.1:
// Electronic transition moment, Re
//"Line strength", S = |R_e|^2*q_v'v" or just |R_e|^2 (R_00 is for the band head)
//Section 4.4.2 - for atoms or molecules:
// then: gf = (8pi^2m_e*nu/3he^2) * S
//
// ^48Ti^16O systems: Table 4.18, p. 91
//  C^3Delta - X^3Delta ("alpha system") (Delta Lambda = 0??, p. 84 - no Q branch??)
//  c^1Phi - a^1Delta ("beta system") (Delta Lambda = 1??, p. 84)
//  A^3Phi - X^3Delta ("gamma system") (Delta Lambda = 0??, p. 84 - no Q branch??)
// //
// Rotational & vibrational constants for TiO states:, p. 87, Table 4.17
// C^3Delta, X^3Delta a^1Delta, -- No "c^1Phi" - ??
//
//General TiO molecular rotational & vibrational constants - Table 3.12, p. 47

//Zeidler & Koester 1982 p. 175, Sect vi):
//If Q branch (deltaLambda = +/-1): alpP = alpR = 0.25, alpQ = 0.5
//If NO Q branch (deltaLambda = 0): alpP = alpR = 0.5, alpQ = 0.0

  //number of wavelength point sampling a JOLA band
  int jolaNumPoints = 100; 
  //int jolaNumPoints = 10; 

// branch weights for transitions of DeltaLambda = +/- 1
  double jolaAlphP_DL1 = 0.25;
  double jolaAlphR_DL1 = 0.25;
  double jolaAlphQ_DL1 = 0.5;
// branch weights for transitions of DeltaLambda = 0
  double jolaAlphP_DL0 = 0.5;
  double jolaAlphR_DL0 = 0.5;
  double jolaAlphQ_DL0 = 0.0; //no Q branch in this case

  double jolaS; //line strength
  double jolaLogF; //line strength


   double logSTofHelp = Math.log(8.0/3.0) + 2.0*Math.log(Math.PI) + Useful.logMe() - Useful.logH() - 2.0*Useful.logEe();
  //Hand-tuned for now - Maybe this is the "script S" factor in Allen 4th Ed., p. 88 (S = |R|^2*q_v'v"*scriptS)
   double jolaQuantumS = 1.0; //default for multiplicative factor 
   double[] logNumJola = new double[numDeps];
   double[][] jolaProfPR = new double[jolaNumPoints][numDeps]; // For unified P & R branch
   double[][] jolaProfQ = new double[jolaNumPoints][numDeps]; //For Q branch
//Differential cross-section - the main "product" of the JOLA approximation:
   double[][] dfBydv = new double[jolaNumPoints][numDeps];

//
//     FILE I/O Section
//External line list input file approach:
//
Charset charset = Charset.forName("US-ASCII");
String pattern = "0.0000000000000000";
//String pattern = "###.####";
DecimalFormat myFormatter = new DecimalFormat(pattern);
//
String dataPath = "./InputData/";
//
//
// **************  Atomic line list:
//
//NIST Atomic Spectra Database Lines Data
//Kramida, A., Ralchenko, Yu., Reader, J., and NIST ASD Team (2015). NIST Atomic Spectra Database (ver. 5.3), [Online]. Available: http://physics.nist.gov/asd [2017, January 30]. National Institute of Standards and Technology, Gaithersburg, MD.
//
//Stuff for byte file method:
//
// *** NOTE: bArrSize must have been noted from the stadout of LineListServer and be consistent
// with whichever line list is linked to gsLineListBytes.dat, and be st manually here:
 String lineListBytes = dataPath + "gsLineListBytes.dat";
 File file = new File(lineListBytes);
 int bArrSize = (int) file.length();
 byte[] barray = new byte[bArrSize];
 //barray = ByteFileRead.readFileBytes(lineListBytes, bArrSize);

//Path path = Paths.get(dataPath + lineListFile); //java.nio.file not available in Java 6

// We have Java SE 6 - we don't have the java.nio package!
//From http://www.deepakgaikwad.net/index.php/2009/11/23/reading-text-file-line-by-line-in-java-6.html
//

//System.out.println(" *********************************************** ");
//System.out.println("  ");
//System.out.println("  ");
//System.out.println("BEFORE FILE READ");
//System.out.println("  ");
//System.out.println("  ");
//System.out.println(" *********************************************** ");


 barray = ByteFileRead.readFileBytes(lineListBytes, bArrSize);
 String decoded = new String(barray, 0, bArrSize);  // example for one encoding type 
// String decoded = new String(barray, 0, bArrSize, "UTF-8") throws UnsupportedEncodingException;  // example for one encoding type 

//System.out.println(" *********************************************** ");
//System.out.println("  ");
//System.out.println("  ");
//System.out.println("AFTER FILE READ");
//System.out.println("  ");
//System.out.println("  ");
//System.out.println(" *********************************************** ");

 String[] arrayLineString = decoded.split("%%"); 

//Number of lines MUST be the ONLY entry on the first line 

        int numLineList = arrayLineString.length;


//Atomic lines:
//Okay, here we go:
        double[] list2Lam0 = new double[numLineList];  // nm
        String[] list2Element = new String[numLineList]; //element
        String[] list2StageRoman = new String[numLineList]; //ion stage
        int[] list2Stage = new int[numLineList]; //ion stage
        double[] list2Mass = new double[numLineList]; // amu
        double[] list2LogGammaCol = new double[numLineList];
        //Einstein coefficient for spontaneous de-exciation:
        double[] list2LogAij = new double[numLineList]; //log base 10
        //Log of unitless oscillator strength, f 
        double[] list2Logf = new double[numLineList];
        //Ground state ionization E - Stage I (eV) 
        double[] list2ChiI1 = new double[numLineList];
        //Ground state ionization E - Stage II (eV)
        double[] list2ChiI2 = new double[numLineList];
        //Ground state ionization E - Stage III (eV) 
        double[] list2ChiI3 = new double[numLineList];
        //Ground state ionization E - Stage IV (eV)
        double[] list2ChiI4 = new double[numLineList];
        //Ground state ionization E - Stage V (eV)
        double[] list2ChiI5 = new double[numLineList];
        //Ground state ionization E - Stage VI (eV)
        double[] list2ChiI6 = new double[numLineList];
        //Excitation E of lower E-level of b-b transition (eV)
        double[] list2ChiL = new double[numLineList];
        //Unitless statisital weight, Ground state - Stage I
        //double[] list2Gw1 = new double[numLineList];
        //Unitless statisital weight, Ground state - Stage II
        //double[] list2Gw2 = new double[numLineList];
        //Unitless statisital weight, Ground state - Stage III
        //double[] list2Gw3 = new double[numLineList];
        //Unitless statisital weight, Ground state - Stage IV
        //double[] list2Gw4 = new double[numLineList];
        //double[] list2Gw4 = new double[numLineList];
        //Unitless statisital weight, lower E-level of b-b transition                 
        double[] list2GwL = new double[numLineList];
        //double[] list2GwU For now we'll just set GwU to 1.0
        // Is stage II?

        //Atomic Data sources:
 
 double thisF;
 int list2_ptr = 0; //pointer into line list2 that we're populating
 int numFields = 7; //number of field per record 
 // 0: element, 1: ion stage, 2: lambda_0, 3: logf, 4: g_l, 5: chi_l
 String[] thisRecord = new String[numFields]; 
    
 String myString;  //useful helper
 
     for (int iLine = 0; iLine < numLineList; iLine++){

        // "|" turns out to mean something in regexp, so we need to escape with '\\':
        thisRecord = arrayLineString[iLine].split("\\|");
        //System.out.println("thisRecord[0] " + thisRecord[0]
        //                 + "thisRecord[1] " + thisRecord[1] 
        //                 + "thisRecord[2] " + thisRecord[2] 
        //                 + "thisRecord[3] " + thisRecord[3] 
        //                 + "thisRecord[4] " + thisRecord[4] 
        //                 + "thisRecord[5] " + thisRecord[5]);
                 
       
        myString = thisRecord[0].trim(); 
        list2Element[iLine] = myString;
        myString = thisRecord[1].trim();
        list2StageRoman[iLine] = myString;  
        myString = thisRecord[2].trim(); 
        list2Lam0[iLine] = Double.parseDouble(myString);
        myString = thisRecord[3].trim();
        list2LogAij[iLine] = Double.parseDouble(myString);
        myString = thisRecord[4].trim();
        list2Logf[iLine] = Double.parseDouble(myString);
        myString = thisRecord[5].trim();
        list2ChiL[iLine] = Double.parseDouble(myString);
//// Currently not used
//        myString = thisRecord[6].trim();
//        list2ChiU = Double.parseDouble(myString);
//        myString = thisRecord[7].trim();
//        list2Jl = Double.parseDouble(myString);
//        myString = thisRecord[8].trim();
//        list2Ju = Double.parseDouble(myString);
        myString = thisRecord[9].trim();
        list2GwL[iLine] = Double.parseDouble(myString);
//// Currently not used
//        myString = thisRecord[10].trim();
//        list2GwU = Double.parseDouble(myString);
           
           //Get the chemical element symbol - we don't know if it's one or two characters
            if (list2StageRoman[list2_ptr].equals("I")){
              list2Stage[list2_ptr] = 0;
             }
            if (list2StageRoman[list2_ptr].equals("II")){
              list2Stage[list2_ptr] = 1;
             }
            if (list2StageRoman[list2_ptr].equals("III")){
              list2Stage[list2_ptr] = 2;
             }
            if (list2StageRoman[list2_ptr].equals("IV")){
              list2Stage[list2_ptr] = 3;
             }
            if (list2StageRoman[list2_ptr].equals("V")){
              list2Stage[list2_ptr] = 4;
             }
            if (list2StageRoman[list2_ptr].equals("VI")){
              list2Stage[list2_ptr] = 5;
             }
            if (list2StageRoman[list2_ptr].equals("VII")){
              list2Stage[list2_ptr] = 6;
             }
           //wavelength in nm starts at position 23 and is in %8.3f format - we're not expecting anything greater than 9999.999 nm

    // Some more processing:
           list2Mass[list2_ptr] = AtomicMass.getMass(list2Element[list2_ptr]);
           species = list2Element[list2_ptr] + "I";
           list2ChiI1[list2_ptr] = IonizationEnergy.getIonE(species); 
           species = list2Element[list2_ptr] + "II";
           list2ChiI2[list2_ptr] = IonizationEnergy.getIonE(species);
           species = list2Element[list2_ptr] + "III";
           list2ChiI3[list2_ptr] = IonizationEnergy.getIonE(species); 
           species = list2Element[list2_ptr] + "IV";
           list2ChiI4[list2_ptr] = IonizationEnergy.getIonE(species);
           species = list2Element[list2_ptr] + "V";
           list2ChiI5[list2_ptr] = IonizationEnergy.getIonE(species);
           species = list2Element[list2_ptr] + "VI";
           list2ChiI6[list2_ptr] = IonizationEnergy.getIonE(species);

     //We're going to have to fake the ground state statistical weight for now - sorry:
           //list2Gw1[list2_ptr] = 1.0;
           //list2Gw2[list2_ptr] = 1.0; 
           //list2Gw3[list2_ptr] = 1.0;
           //list2Gw4[list2_ptr] = 1.0; 
           list2LogGammaCol[list2_ptr] = logGammaCol; 

    //We've gotten everything we need from the NIST line list:
           list2_ptr++;
        
       } //iLine loop 

  int numLines2 = list2_ptr;


//

//Okay - what kind of mess did we make...


// END FILE I/O SECTION


//System.out.println(" *********************************************** ");
//System.out.println("  ");
//System.out.println("  ");
//System.out.println("BEFORE TRIAGE");
//System.out.println("  ");
//System.out.println("  ");
//System.out.println(" *********************************************** ");
//
//Triage: For each line: Voigt, Gaussian, or neglect??
//
//
        int gaussLineCntr = 0; //initialize accumulator
        //int sedLineCntr = 0; //initialize accumulator
        //No! boolean[] ifThisLine = new boolean[numLines2]; //initialize line strength flag
        int gaussLine_ptr[] = new int[numLines2]; //array of pointers to lines that make the cut in the 
        //int sedLine_ptr[] = new int[numLines2]; //array of pointers to lines that make the cut in the 
                                                  // master line list  
        boolean isFirstLine = true; //initialization
        int firstLine = 0; //default initialization
// This holds 2-element temperature-dependent base e logarithmic parition fn:
        double[] thisUwV = new double[5];
   // Below created a loop to initialize each value to zero for the five temperatures lburns
   for (int i = 0; i < thisUwV.length; i++) {
        thisUwV[i] = 0.0;
   }
        for (int iLine = 0; iLine < numLines2; iLine++) {

            //No! ifThisLine[iLine] = false;
            //if H or He, make sure kappaScale is unity:
            if ((list2Element[iLine].equals("H"))
                    || (list2Element[iLine].equals("He"))) {
                zScaleList = 1.0;
                //list2Gw1[iLine] = 2.0;  //fix for H lines
                if (list2Lam0[iLine] <= 657.0){
                list2GwL[iLine] = 8.0;  //fix for Balmer lines
                    } else {
                list2GwL[iLine] = 18.0;  //fix for Paschen lines
                    }
            } else {
                zScaleList = zScale;
            }

          list2Lam0[iLine] = list2Lam0[iLine] * 1.0e-7;  // nm to cm
          int iAbnd = 0; //initialization
          int logNums_ptr = 0;
            //System.out.println("iLine " + iLine + " list2Element[iLine] " + list2Element[iLine]);
          for (int jj = 0; jj < nelemAbnd; jj++){
             //System.out.println("jj " + jj + " cname[jj]" + cname[jj]+"!");
             if (list2Element[iLine].equals(cname[jj])){
                if (list2Stage[iLine] == 0){
                  species = cname[jj] + "I";
                  logNums_ptr = 0;
                }
                if (list2Stage[iLine] == 1){
                  species = cname[jj] + "II";
                  logNums_ptr = 1;
                }
                if (list2Stage[iLine] == 2){
                  species = cname[jj] + "III";
                  logNums_ptr = 4;
                }
                if (list2Stage[iLine] == 3){
                  species = cname[jj] + "IV";
                  logNums_ptr = 5;
                }
                if (list2Stage[iLine] == 4){
                  species = cname[jj] + "V";
                  logNums_ptr = 6;
                }
                if (list2Stage[iLine] == 5){
                  species = cname[jj] + "VI";
                  logNums_ptr = 7;
                }
                thisUwV = PartitionFn.getPartFn2(species); //base e log_10 U
                 break;   //we found it
             }
             iAbnd++;
          } //jj loop
           double[][] list2LogNums = new double[numStages+2][numDeps];
            for (int iTau = 0; iTau < numDeps; iTau++){
               list2LogNums[0][iTau] = masterStagePops[iAbnd][0][iTau];
               list2LogNums[1][iTau] = masterStagePops[iAbnd][1][iTau];
               list2LogNums[4][iTau] = masterStagePops[iAbnd][2][iTau];
               list2LogNums[5][iTau] = masterStagePops[iAbnd][3][iTau];
               list2LogNums[6][iTau] = masterStagePops[iAbnd][4][iTau];
               list2LogNums[7][iTau] = masterStagePops[iAbnd][5][iTau];
            }
            double[] numHelp = LevelPopsGasServer.levelPops(list2Lam0[iLine], list2LogNums[logNums_ptr], list2ChiL[iLine], thisUwV, 
                    list2GwL[iLine], numDeps, temp);
            for (int iTau = 0; iTau < numDeps; iTau++){
               list2LogNums[2][iTau] = numHelp[iTau];
               list2LogNums[3][iTau] = numHelp[iTau] / 2.0; //fake for testing with gS3 line treatment
            } 

        //linePoints: Row 0 in cm (will need to be in nm for Plack.planck), Row 1 in Doppler widths
        //For now - initial strength check with delta fn profiles at line centre for triage:
        int listNumPointsDelta = 1;
           double[][] listLinePointsDelta = LineGrid.lineGridDelta(list2Lam0[iLine], list2Mass[iLine], xiT, numDeps, teff);
           double[][] listLineProfDelta = LineProf.delta(listLinePointsDelta, list2Lam0[iLine], numDeps, tauRos, list2Mass[iLine], xiT, teff); 
           double[][] listLogKappaLDelta = LineKappa.lineKap(list2Lam0[iLine], list2LogNums[2], list2Logf[iLine], listLinePointsDelta, listLineProfDelta,
                    numDeps, zScaleList, tauRos, temp, rho, logFudgeTune);
   /* Let's not do this - too slow:
            // Low resolution SED lines and high res spectrum synthesis region lines are mutually
            // exclusive sets in wavelength space:
            //Does line qualify for inclusion in SED as low res line at all??
            // Check ratio of line centre opacity to continuum at log(TauRos) = -5, -3, -1
            if ( (logE*(listLogKappaLDelta[0][6] - kappa[1][6]) > sedThresh)  
              || (logE*(listLogKappaLDelta[0][18] - kappa[1][18]) > sedThresh)  
              || (logE*(listLogKappaLDelta[0][30] - kappa[1][30]) > sedThresh) ){ 
                   if ( ( list2Stage[iLine] == 0) || (list2Stage[iLine] == 1) 
                    ||  ( list2Stage[iLine] == 2) || (list2Stage[iLine] == 3) ){
                        if ( (list2Lam0[iLine] > lamUV) && (list2Lam0[iLine] < lamIR) ){
                           if ( (list2Lam0[iLine] < lambdaStart) || (list2Lam0[iLine] > lambdaStop) ){ 
                      //No! ifThisLine[iLine] = true;
                      sedLine_ptr[sedLineCntr] = iLine;
                      sedLineCntr++;
      //System.out.println("SED passed, iLine= " + iLine + " sedLineCntr " + sedLineCntr 
      //   + " list2Lam0[iLine] " + list2Lam0[iLine] 
      //   + " list2Element[iLine] " + list2Element[iLine]
      //   + " list2Stage[iLine] " + list2Stage[iLine]); 
                                 }
                            }
                      } 
                }
  */
            //Does line qualify for inclusion in high res spectrum synthesis region??
            // Check ratio of line centre opacity to continuum at log(TauRos) = -5, -3, -1
           //Find local value of lambda-dependent continuum kappa - list2Lam0 & lambdaScale both in cm here: 
            int thisLambdaPtr = ToolBox.lamPoint(numLams, lambdaScale, list2Lam0[iLine]);
            if ( (logE*(listLogKappaLDelta[0][6] - logKappa[thisLambdaPtr][6]) > lineThresh)  
              || (logE*(listLogKappaLDelta[0][18] - logKappa[thisLambdaPtr][18]) > lineThresh)  
		      || (logE*(listLogKappaLDelta[0][30] - logKappa[thisLambdaPtr][30]) > lineThresh) ){ 
			   if ( ( list2Stage[iLine] == 0) || (list2Stage[iLine] == 1) 
			    ||  ( list2Stage[iLine] == 2) || (list2Stage[iLine] == 3) 
                            ||  ( list2Stage[iLine] == 4) || (list2Stage[iLine] == 5) ){
				if ( (list2Lam0[iLine] > lambdaStart) && (list2Lam0[iLine] < lambdaStop) ){ 
			      //No! ifThisLine[iLine] = true;
			      gaussLine_ptr[gaussLineCntr] = iLine;
			      gaussLineCntr++;
                              if (isFirstLine == true){
                                 firstLine = iLine;
                                 isFirstLine = false;
                              } 
                            }
                      } 
                }
//
       } //iLine loop

//

//We need to have at least one line in rgion:
       boolean areNoLines = false; //initialization
       if (gaussLineCntr == 0){
            gaussLineCntr = 1;
            gaussLine_ptr[0] = firstLine;
            areNoLines = true;
             }

       int numGaussLines = gaussLineCntr;
//System.out.println(" *********************************************** ");
//System.out.println("  ");
//System.out.println("  ");
//System.out.println("AFTER TRIAGE");
//System.out.println("  ");
//System.out.println("  ");
//System.out.println(" *********************************************** ");

        //Notes
        //if Hydrogen or Helium, kappaScale should be unity for these purposes:
        //double kappaScaleList = 1.0; //initialization                   
        //

        int listNumCore = 3;  //half-core //default initialization
        int listNumWing = 1;  //per wing
        //int sedNumWing = 1;  //per wing
        //int thisNumCore = sedNumCore; //default initialization
        //int thisNumWing = sedNumWing; //default initialization
        if (sampling.equals("coarse")){
           listNumCore = 3;  //half-core
           listNumWing = 3;  //per wing
        } else {
           listNumCore = 5;  //half-core
           listNumWing = 9;  //per wing
        } 
//Delta fn - for testing and strength triage
        //int listNumPoints = 1;
//All gaussian
        //int listNumPoints = 2 * listNumCore - 1; // + 1;  //Extra wavelength point at end for monochromatic continuum tau scale
////All full voigt:
        int listNumPoints = (2 * (listNumCore + listNumWing) - 1); // + 1;  //Extra wavelength point at end for monochromatic continuum tau scale
        //int sedNumPoints = (2 * (sedNumCore + sedNumWing) - 1); // + 1;  //Extra wavelength point at end for monochromatic continuum tau scale
        //int thisNumPoints = sedNumPoints; //default initialization
        int numNow = numLams;  //initialize dynamic counter of how many array elements are in use
        int numMaster;
        if (ifTiO == 1){
            numMaster = numLams + (numGaussLines * listNumPoints) + (numJola * jolaNumPoints); // + (numSedLines * sedNumPoints); //total size (number of wavelengths) of master lambda & total kappa arrays 
        } else {
            numMaster = numLams + (numGaussLines * listNumPoints);
        } 
        double[] masterLams = new double[numMaster];
//Line blanketed opacity array:
        double[][] logMasterKaps = new double[numMaster][numDeps];
        //seed masterLams and logMasterKaps with continuum SED lambdas and kappas:
        //This just initializes the first numLams of the numMaster elements

//Initialize monochromatic line blanketed opacity array:
// Seed first numLams wavelengths with continuum wavelength and kappa values 
        for (int iL = 0; iL < numLams; iL++) {
            masterLams[iL] = lambdaScale[iL];
            for (int iD = 0; iD < numDeps; iD++) {
                logMasterKaps[iL][iD] = logKappa[iL][iD]; 
            }
        }
        //initialize the remainder with dummy values - these values will be clobbered as line wavelengths are inserted, 
        // and don't matter
        for (int iL = numLams; iL < numMaster; iL++) {
            masterLams[iL] = lambdaScale[numLams - 1];
            for (int iD = 0; iD < numDeps; iD++) {
                logMasterKaps[iL][iD] = logKappa[numLams-1][iD];
            }
        }

        //Stuff for the the Teff recovery test:
        double lambda1, lambda2, fluxSurfBol, logFluxSurfBol;
        fluxSurfBol = 0;
 
 //Get the components for the power series expansion approximation of the Hjerting function
 //for treating Voigt profiles:
        double[][] hjertComp = HjertingComponents.hjertingComponents();

// This holds 2-element temperature-dependent base e logarithmic parition fn:
        for (int k = 0; k < thisUwV.length; k++){
           thisUwV[k] = 0.0; //default initialization
        }

        double[][] listLineProf = new double[listNumPoints][numDeps];

// Put in high res spectrum synthesis lines:
        for (int iLine = 0; iLine < numGaussLines; iLine++) {

            //if H or He, make sure kappaScale is unity:
            if ((list2Element[gaussLine_ptr[iLine]].equals("H"))
                    || (list2Element[gaussLine_ptr[iLine]].equals("He"))) {
                zScaleList = 1.0;
                //list2Gw1[gaussLine_ptr[iLine]] = 2.0;  //fix for H lines
                if (list2Lam0[gaussLine_ptr[iLine]] <= 657.0e-7){
                    list2GwL[gaussLine_ptr[iLine]] = 8.0;  //fix for Balmer lines
                } else {
                    list2GwL[gaussLine_ptr[iLine]] = 18.0;  //fix for Paschen lines
                } 
            } else {
                zScaleList = zScale;
            }


//
          int iAbnd = 0; //initialization
          int logNums_ptr = 0;
          for (int jj = 0; jj < nelemAbnd; jj++){
             if (list2Element[gaussLine_ptr[iLine]].equals(cname[jj])){
                if (list2Stage[gaussLine_ptr[iLine]] == 0){
                  species = cname[jj] + "I";
                  logNums_ptr = 0;
                }
                if (list2Stage[gaussLine_ptr[iLine]] == 1){
                  species = cname[jj] + "II";
                  logNums_ptr = 1;
                }
                if (list2Stage[gaussLine_ptr[iLine]] == 2){
                  species = cname[jj] + "III";
                  logNums_ptr = 4;
                }
                if (list2Stage[gaussLine_ptr[iLine]] == 3){
                  species = cname[jj] + "IV";
                  logNums_ptr = 5;
                }
                if (list2Stage[gaussLine_ptr[iLine]] == 4){
                  species = cname[jj] + "V";
                  logNums_ptr = 6;
                }
                if (list2Stage[gaussLine_ptr[iLine]] == 5){
                  species = cname[jj] + "VI";
                  logNums_ptr = 7;
                }
                thisUwV = PartitionFn.getPartFn2(species); //base e log_e U
                 break;   //we found it
                 }
             iAbnd++;
          } //jj loop
           double[][] list2LogNums = new double[numStages+2][numDeps];
            for (int iTau = 0; iTau < numDeps; iTau++){
               list2LogNums[0][iTau] = masterStagePops[iAbnd][0][iTau];
               list2LogNums[1][iTau] = masterStagePops[iAbnd][1][iTau];
               list2LogNums[4][iTau] = masterStagePops[iAbnd][2][iTau];
               list2LogNums[5][iTau] = masterStagePops[iAbnd][3][iTau];
               list2LogNums[6][iTau] = masterStagePops[iAbnd][4][iTau];
               list2LogNums[7][iTau] = masterStagePops[iAbnd][5][iTau];
            }
            double[] numHelp = LevelPopsGasServer.levelPops(list2Lam0[gaussLine_ptr[iLine]], list2LogNums[logNums_ptr], list2ChiL[gaussLine_ptr[iLine]], thisUwV,
                    list2GwL[gaussLine_ptr[iLine]], numDeps, temp);
            for (int iTau = 0; iTau < numDeps; iTau++){
               list2LogNums[2][iTau] = numHelp[iTau];
               list2LogNums[3][iTau] = -19.0; //upper E-level - not used - fake for testing with gS3 line treatment
               //if ( (list2Element[gaussLine_ptr[iLine]].equals("Na")) && (list2Stage[gaussLine_ptr[iLine]] == 0) ){
               //    if (iTau%5 == 1){
               //       System.out.println("iTau "+ iTau+ " Na I list2LogNums[2]: "+ logE*list2LogNums[2][iTau]);
               //    }
               //}
            } 

             //Proceed only if line strong enough: 
             // 
             //ifThisLine[gaussLine_ptr[iLine]] = true; //for testing
             //No! if (ifThisLine[gaussLine_ptr[iLine]] == true){
              
            // Gaussian only approximation to profile (voigt()):
//            double[][] listLinePoints = LineGrid.lineGridGauss(list2Lam0[gaussLine_ptr[iLine]], list2Mass[gaussLine_ptr[iLine]], xiT, numDeps, teff, listNumCore);
//            double[][] listLineProf = LineProf.gauss(listLinePoints, list2Lam0[gaussLine_ptr[iLine]],
//                    numDeps, teff, tauRos, temp, tempSun);
            // Gaussian + Lorentzian approximation to profile (voigt()):
            double[][] listLinePoints = LineGrid.lineGridVoigt(list2Lam0[gaussLine_ptr[iLine]], list2Mass[gaussLine_ptr[iLine]], xiT, numDeps, teff, listNumCore, listNumWing, species);
            //if ( (list2Element[gaussLine_ptr[iLine]].equals("Na")) && (list2Stage[gaussLine_ptr[iLine]] == 0) ){
            //   System.out.println("iLine "+ iLine+ " gaussLine_ptr "+ gaussLine_ptr[iLine]+ " list2Lam0 "+ list2Lam0[gaussLine_ptr[iLine]]+ " list2LogAij "+ 
          //list2LogAij[gaussLine_ptr[iLine]]+ " list2LogGammaCol "+ list2LogGammaCol[gaussLine_ptr[iLine]]+ " list2Logf "+ list2Logf[gaussLine_ptr[iLine]]);
           // }
            if (species.equals("HI")){
 //System.out.println("Calling Stark...");
                 listLineProf = LineProf.stark(listLinePoints, list2Lam0[gaussLine_ptr[iLine]], list2LogAij[gaussLine_ptr[iLine]],
                    list2LogGammaCol[gaussLine_ptr[iLine]],
                    numDeps, teff, tauRos, temp, pGas, newNe, tempSun, pGasSun, hjertComp, species);
            } else {
                 listLineProf = LineProf.voigt(listLinePoints, list2Lam0[gaussLine_ptr[iLine]], list2LogAij[gaussLine_ptr[iLine]],
                    list2LogGammaCol[gaussLine_ptr[iLine]],
                    numDeps, teff, tauRos, temp, pGas, tempSun, pGasSun, hjertComp);
            } 
            //if ( (list2Element[gaussLine_ptr[iLine]].equals("Na")) && (list2Stage[gaussLine_ptr[iLine]] == 0) ){
            //   System.out.println("iLine "+ iLine+ " gaussLine_ptr "+ gaussLine_ptr[iLine]+ "list2Logf "+ list2Logf[gaussLine_ptr[iLine]]);
            //}
            double[][] listLogKappaL = LineKappa.lineKap(list2Lam0[gaussLine_ptr[iLine]], list2LogNums[2], list2Logf[gaussLine_ptr[iLine]], listLinePoints, listLineProf,
                    numDeps, zScaleList, tauRos, temp, rho, logFudgeTune);
            //if ( (list2Element[gaussLine_ptr[iLine]].equals("Na")) && (list2Stage[gaussLine_ptr[iLine]] == 0) ){
            //  for (int iTau = 0; iTau < numDeps; iTau++){
            //    if (iTau%5 == 1){
            //        for (int iL = 0; iL < listNumPoints; iL++){
            //            if (iL%2 == 0){
            //                System.out.println("iTau "+ iTau+ " iL "+ iL+ " listLinePoints[0]&[1] "+ listLinePoints[0][iL]+ "  "+ listLinePoints[1][iL]+" listLineProf "+ listLineProf[iL][iTau]  + " listLogKappaL "+ logE*listLogKappaL[iL][iTau]);
             //           }
             //       }
             //   }
             // }
            // }
            double[] listLineLambdas = new double[listNumPoints];
            for (int il = 0; il < listNumPoints; il++) {
                // // lineProf[gaussLine_ptr[iLine]][*] is DeltaLambda from line centre in cm
                listLineLambdas[il] = listLinePoints[0][il] + list2Lam0[gaussLine_ptr[iLine]];
            }

            double[] masterLamsOut = SpecSyn.masterLambda(numLams, numMaster, numNow, masterLams, listNumPoints, listLineLambdas);
            double[][] logMasterKapsOut = SpecSyn.masterKappa(numDeps, numLams, numMaster, numNow, masterLams, masterLamsOut, logMasterKaps, listNumPoints, listLineLambdas, listLogKappaL);
            numNow = numNow + listNumPoints;

            //update masterLams and logMasterKaps:
            for (int iL = 0; iL < numNow; iL++) {
                masterLams[iL] = masterLamsOut[iL];
                for (int iD = 0; iD < numDeps; iD++) {
                    //Still need to put in multi-Gray levels here:
                    logMasterKaps[iL][iD] = logMasterKapsOut[iL][iD];
                }
            }
          //No! } //ifThisLine strength condition
        } //numLines loop

       //  for (int iL = 0; iL < numNow; iL++) {
       //      System.out.println("logMasterKaps iL " + iL + " masterLams " + masterLams[iL]);
       //      for (int iD = 0; iD < numDeps; iD+=5) {
       //          //Still need to put in multi-Gray levels here:
       //          System.out.println(" " + logMasterKaps[iL][iD]);
       //      }
       //  }


////

 if (teff <= jolaTeff){
//Begin loop over JOLA bands - isert JOLA oapcity into opacity spectum...
   double helpJolaSum = 0.0;
  
 if (ifTiO == 1){

   for (int iJola = 0; iJola < numJola; iJola++){

      //Find species in molecule set:
      for (int iMol = gsFirstMol; iMol < gsNspec; iMol++){
        if (gsName[iMol].equals(jolaSpecies[iJola])){
          //System.out.println("mname " + mname[iMol]);
          for (int iTau= 0; iTau < numDeps; iTau++){
             logNumJola[iTau] = masterMolPops[iMol-gsFirstMol][iTau];
            // double logTiOpp = logNumJola[iTau] + temp[1][iTau] + Useful.logK();
            // System.out.println("TiO pp " + logE*logTiOpp);
          }
        }
      }

        jolaOmega0 = MolecData.getOrigin(jolaSystem[iJola]);  //band origin ?? //Freq in Hz OR waveno in cm^-1 ??
        //jolaRSqu = MolecData.getSqTransMoment(jolaSystem[iJola]); //needed for total vibrational band oscillator strength (f_v'v")
        jolaB = MolecData.getRotConst(jolaSystem[iJola]); // B' and b" values of upper and lower vibational state
        jolaLambda = MolecData.getWaveRange(jolaSystem[iJola]); //approx wavelength range of band
        jolaDeltaLambda = MolecData.getDeltaLambda(jolaSystem[iJola]);
        //Line strength factor from Allen's 4th Ed., p. 88, "script S":
        //jolaQuantumS = MolecData.getQuantumS(jolaSystem[iJola]); 

        jolaLogF = -99.0; // #Default

      if (jolaWhichF[iJola].equals("Allen")){

        jolaRSqu = MolecData.getSqTransMoment(jolaSystem[iJola]); //needed for total vibrational band oscillator strength (f_v'v")
        jolaQuantumS = MolecData.getQuantumS(jolaSystem[iJola]); 

//Compute line strength, S, Allen, p. 88:
        jolaS = jolaRSqu * jolaQuantumS; //may not be this simple (need q?)
//Compute logf , Allen, p. 61 Section 4.4.2 - for atoms or molecules - assumes g=1 so logGf = logF:
        //jolaLogF = logSTofHelp + Math.log(jolaOmega0) + Math.log(jolaS); //if omega0 is a freq in Hz
        //Gives wrong result?? jolaLogF = logSTofHelp + Useful.logC() + Math.log(jolaOmega0) + Math.log(jolaS); //if omega0 is a waveno in cm^-1 
        double checkgf = 303.8*jolaS/(10.0*jolaLambda[0]); //"Numerical relation", Allen 4th, p. 62 - lambda in A
        jolaLogF = Math.log(checkgf); //better??

      }

     double jolaRawF = 0.0;
     double jolaF = 0.0;
     
     if (jolaWhichF[iJola].equals("Jorgensen")){
         //#Band strength: Jorgensen, 1994, A&A, 284, 179 approach - we have the f values directly:
            
         //#This is practically the astrophysical tuning factor:
         jolaQuantumS = MolecData.getQuantumS(jolaSystem[iJola]);
            
         jolaRawF = MolecData.getFel(jolaSystem[iJola]);
         jolaF = jolaRawF * jolaQuantumS;
         //#print(iJola, " jQS ", jolaQuantumS, " jRF ", jolaRawF, " jF ", jolaF)
         jolaLogF = Math.log(jolaF);
         //#print("iJola ", iJola, " logF ", 10.0**(logE*jolaLogF+14) )
     }

        if (jolaDeltaLambda == 0){ 
           jolaAlphP = jolaAlphP_DL0; // alpha_P - weight of P branch (Delta J = 1)
           jolaAlphR = jolaAlphR_DL0; // alpha_R - weight of R branch (Delta J = -1)
           jolaAlphQ = jolaAlphQ_DL0; // alpha_Q - weight of Q branch (Delta J = 0)
        }
        if (jolaDeltaLambda != 0){ 
           jolaAlphP = jolaAlphP_DL1; // alpha_P - weight of P branch (Delta J = 1)
           jolaAlphR = jolaAlphR_DL1; // alpha_R - weight of R branch (Delta J = -1)
           jolaAlphQ = jolaAlphQ_DL1; // alpha_Q - weight of Q branch (Delta J = 0)
        }

        double[] jolaPoints = Jola.jolaGrid(jolaLambda, jolaNumPoints);

//This sequence of methods might not be the best way, but it's based on the procedure for atomic lines
// Put in JOLA bands:

//P & R brnaches in every case:
        dfBydv = Jola.jolaProfilePR(jolaOmega0, jolaLogF, jolaB,
                                     jolaPoints, jolaAlphP, jolaAlphR, numDeps, temp);

        double[][] jolaLogKappaL = Jola.jolaKap(logNumJola, dfBydv, jolaPoints, 
                  numDeps, temp, rho);
            //for (int iW = 0; iW < jolaNumPoints; iW+=10){
            //   for (int iD = 1; iD < numDeps; iD+=5){
            //       System.out.println("iW " + iW + " iD " + iD + " jolaLogKappaL " + jolaLogKappaL[iW][iD]);
            //   } //iD loop
            //} //iW loop

////Q branch if DeltaLambda not equal to 0
//         if (jolaDeltaLambda[iJola] != 0){ 
//            dfBydv = Jola.jolaProfileQ(jolaOmega0, jolaLogF, jolaB,
//                                      jolaPoints, jolaAlphQ, numDeps, temp);
// //
//            double[][] jolaLogKappaQL = Jola.jolaKap(logNumJola, dfBydv, jolaPoints, 
//                   numDeps, temp, rho);
//            //Now add it to the P & R branch opacity:
//            for (int iW = 0; iW < jolaNumPoints; iW++){
//               for (int iD = 0; iD < numDeps; iD++){
//             //   //  if (iD%10 == 1){
//              //       //System.out.println("iW " + iW + " iD " + iD + " jolaLogKappaL " + jolaLogKappaL[iW][iD]);
//               //  // }
//                   helpJolaSum = Math.exp(jolaLogKappaL[iW][iD]) + Math.exp(jolaLogKappaQL[iW][iD]);
//                   jolaLogKappaL[iW][iD] = Math.log(helpJolaSum); 
//               } //iD loop
//            } //iW loop
//         } //Q-branch if

            double[] jolaLambdas = new double[jolaNumPoints];
            for (int il = 0; il < jolaNumPoints; il++) {
                // // lineProf[gaussLine_ptr[iLine]][*] is DeltaLambda from line centre in cm
                jolaLambdas[il] = 1.0e-7 * jolaPoints[il];
            }

            double[] masterLamsOut = SpecSyn.masterLambda(numLams, numMaster, numNow, masterLams, jolaNumPoints, jolaLambdas);
            double[][] logMasterKapsOut = SpecSyn.masterKappa(numDeps, numLams, numMaster, numNow, masterLams, masterLamsOut, logMasterKaps, jolaNumPoints, jolaLambdas, jolaLogKappaL);
            numNow = numNow + jolaNumPoints;

            //update masterLams and logMasterKaps:
            for (int iL = 0; iL < numNow; iL++) {
                masterLams[iL] = masterLamsOut[iL];
                for (int iD = 0; iD < numDeps; iD++) {
                    //Still need to put in multi-Gray levels here:
                    logMasterKaps[iL][iD] = logMasterKapsOut[iL][iD];
                }
            }

    } //iJola JOLA band loop

  } //ifTiO condition

 } //jolaTeff condition

//

//Sweep the wavelength grid for line-specific wavelength points that are closer together than needed for
//critical sampling:
   //equivalent spectral resolution of wavelength-dependent critical sampling interval
   double sweepRes = 500000.0; //equivalent spectral resolution of wavelength-dependent critical sampling interval
   //cm //use shortest wavelength to avoid under-smapling:
   double sweepDelta = lambdaStart / sweepRes; //cm //use shortest wavelength to avoid under-smapling
   double[] sweepHelp = new double[numMaster]; //to be truncated later

//Initialize sweepHelp
   for (int iSweep = 0; iSweep < numMaster; iSweep++){
       sweepHelp[iSweep] = 0.0;
   }

//
   sweepHelp[0] = masterLams[0]; //An auspicous start :-)
   int lastLam = 0; //index of last masterLam wavelength NOT swept out
   int iSweep = 1; //current sweepHelp index
//
   for (int iLam = 1; iLam < numMaster; iLam++){
      if ( (masterLams[iLam] - masterLams[lastLam]) >= sweepDelta){
         //Kept - ie. NOT swept out:
         sweepHelp[iSweep] = masterLams[iLam];
         lastLam = iLam;
         iSweep++;
      }
   }

  int numKept = iSweep-1;
  double[] sweptLams = new double[numKept];
  for (int iKept = 0; iKept < numKept; iKept++){
     sweptLams[iKept] = sweepHelp[iKept];
  }

//Interpolate the total extinction array onto the swept wavelength grid:
   double[] keptHelp = new double[numKept];
   double[][] logSweptKaps = new double[numKept][numDeps];
   double[] logMasterKapsId = new double[numMaster];
   for (int iD = 0; iD < numDeps; iD++){
      //extract 1D kappa vs lambda at each depth:
      for (int iL = 0; iL < numMaster; iL++){
         logMasterKapsId[iL] = logMasterKaps[iL][iD];
      }
      keptHelp = ToolBox.interpolV(logMasterKapsId, masterLams, sweptLams);
      for (int iL = 0; iL < numKept; iL++){
         logSweptKaps[iL][iD] = keptHelp[iL];
      }
   } //iD loop

//
////
//Continuum monochromatic optical depth array:
        double logTauCont[][] = LineTau2.tauLambdaCont(numLams, logKappa,
                 kappa500, numDeps, tauRos, logTotalFudge);

        //Evaluate formal solution of rad trans eq at each lambda 
        // Initial set to put lambda and tau arrays into form that formalsoln expects
        double[][] contIntens = new double[numLams][numThetas];
        double[] contIntensLam = new double[numThetas];

        double[][] contFlux = new double[2][numLams];
        double[] contFluxLam = new double[2];
        double[][] thisTau = new double[2][numDeps];

        lineMode = false;  //no scattering for overall SED

        for (int il = 0; il < numLams; il++) {

            for (int id = 0; id < numDeps; id++) {
                thisTau[1][id] = logTauCont[il][id];
                thisTau[0][id] = Math.exp(logTauCont[il][id]);
            } // id loop

            contIntensLam = FormalSoln.formalSoln(numDeps,
                    cosTheta, lambdaScale[il], thisTau, temp, lineMode);

            for (int it = 0; it < numThetas; it++) {
                contIntens[il][it] = contIntensLam[it];
            } //it loop - thetas


            //// Teff test - Also needed for convection module!:
            if (il > 1) {
                lambda2 = lambdaScale[il]; // * 1.0E-7;  // convert nm to cm
                lambda1 = lambdaScale[il - 1]; // * 1.0E-7;  // convert nm to cm
                fluxSurfBol = fluxSurfBol
                        + contFluxLam[0] * (lambda2 - lambda1);
            }
        } //il loop

        contFlux = Flux.flux3(contIntens, lambdaScale, cosTheta, phi, cgsRadius, omegaSini, macroVkm);

        double logTauMaster[][] = LineTau2.tauLambda(numKept, sweptLams, logSweptKaps,
                numDeps, kappa500, tauRos, logTotalFudge);

//Lin blanketed formal Rad Trans solution:
        //Evaluate formal solution of rad trans eq at each lambda throughout line profile
        // Initial set to put lambda and tau arrays into form that formalsoln expects
        double[][] masterIntens = new double[numKept][numThetas];
        double[] masterIntensLam = new double[numThetas];

        double[][] masterFlux = new double[2][numKept];
        double[] masterFluxLam = new double[2];

        lineMode = false;  //no scattering for overall SED

        for (int il = 0; il < numKept; il++) {

//                        }
            for (int id = 0; id < numDeps; id++) {
                thisTau[1][id] = logTauMaster[il][id];
                thisTau[0][id] = Math.exp(logTauMaster[il][id]);
            } // id loop

            masterIntensLam = FormalSoln.formalSoln(numDeps,
                    cosTheta, sweptLams[il], thisTau, temp, lineMode);


            for (int it = 0; it < numThetas; it++) {
                masterIntens[il][it] = masterIntensLam[it];
            } //it loop - thetas

            //// Teff test - Also needed for convection module!:
            if (il > 1) {
                lambda2 = sweptLams[il]; // * 1.0E-7;  // convert nm to cm
                lambda1 = sweptLams[il - 1]; // * 1.0E-7;  // convert nm to cm
                fluxSurfBol = fluxSurfBol
                        + masterFluxLam[0] * (lambda2 - lambda1);
            }
        } //il loop

        masterFlux = Flux.flux3(masterIntens, sweptLams, cosTheta, phi, cgsRadius, omegaSini, macroVkm);


        for (int il = 0; il < numKept; il++) {
            //// Teff test - Also needed for convection module!:
            if (il > 1) {
                lambda2 = sweptLams[il]; // * 1.0E-7;  // convert nm to cm
                lambda1 = sweptLams[il - 1]; // * 1.0E-7;  // convert nm to cm
                fluxSurfBol = fluxSurfBol
                        + masterFlux[0][il] * (lambda2 - lambda1);
            }
      }

        logFluxSurfBol = Math.log(fluxSurfBol);
        double logTeffFlux = (logFluxSurfBol - Useful.logSigma()) / 4.0;
        double teffFlux = Math.exp(logTeffFlux);

//Extract linear monochromatic continuum limb darlening coefficients (LDCs) ("epsilon"s):
    double[] ldc = new double[numLams];
    ldc = LDC.ldc(numLams, lambdaScale, numThetas, cosTheta, contIntens);

        //String pattern = "0000.00";
        ////String pattern = "#####.##";
        //DecimalFormat myFormatter = new DecimalFormat(pattern);

// 
   //printout prepared for processing by server-side PHP script:

// Each 'value' (a scalar or a vector (1D array)) should have a unique corresponding text 'key'

// Just return basic modeling outputs - leave post-processing 
//of higher order observables to client-side to minimize amount of 
//data needing packing: 
//

  //Quality control:
     double tiny = 1.0e-19;
     double logTiny = Math.log(tiny);
     
   //int iStart = ToolBox.lamPoint(numMaster, masterLams, lambdaStart);
   //int iStop = ToolBox.lamPoint(numMaster, masterLams, lambdaStop);
   int iStart = ToolBox.lamPoint(numKept, sweptLams, lambdaStart);
   int iStop = ToolBox.lamPoint(numKept, sweptLams, lambdaStop);
   int numSpecSyn = iStop - iStart;
 
  int numSpecies = nelemAbnd * numStages; 
//Count the number of depths at which we'll report GAS partial pressures *carefully*:
//Skip upper boundary
    int numGasDepths = 0;
    for (int i = 1; i < numDeps; i+=4){
        numGasDepths++;
    }
     //Block 1: Array dimensions
     //keys:
        System.out.println("numDeps,numMaster,numThetas,numGaussLines,numLams,nelemAbnd,numSpecies,numGasDepths,numGas"); 
     //values:
        System.out.format("%03d,%07d,%03d,%06d,%07d,%05d,%04d,%03d,%05d%n", 
            numDeps, numKept, numThetas, numGaussLines, numLams, nelemAbnd, numSpecies, numGasDepths, gsNspec);

    //Block 2: Atmosphere
     //keys:
        System.out.println("logTau,logZ,logTemp,logPGas,logPRad,logRho,logNe,logMmw,logKappa");
     //values:
        //Print out *NATURAL* logs - this is for communication between server & client: 
        for (int i = 0; i < numDeps; i++) {
            System.out.format("%13.8f,%13.8f,%13.8f,%13.8f,%13.8f,%13.8f,%13.8f,%13.8f,%13.8f%n", 
                    tauRos[1][i], Math.log(depths[i]), temp[1][i], pGas[1][i], pRad[1][i], 
                    rho[1][i], newNe[1][i], Math.log(mmw[i]), kappaRos[1][i] );
               }

   //Block 3: line blanketed flux spectrum (SED)
   //keys:
        System.out.println("logWave,logFlux");
   //values:
        //for (int i = 0; i < numMaster; i++){
        for (int i = 0; i < numKept; i++){
 //Do quality control here:
           if ( (masterFlux[1][i] < logTiny) || (masterFlux[0][i] < tiny) ){
              masterFlux[1][i] = logTiny;
              masterFlux[0][i] = tiny;
               }
           System.out.format("%13.8f,%13.8f%n", Math.log(sweptLams[i]), masterFlux[1][i]); 
         }

//
   //Block 4: Line blanketed Specific intensity distribution: 
//
       //Structure: One major vectorized block per theta with the 
      // cos(theta) value followed by the intensity spectrum for that theta: 
        for (int i = 0; i < numThetas; i++){
            System.out.println("cosTheta" + i);  //key
            System.out.format("%11.6f%n", cosTheta[1][i]);  //value
            System.out.println("Intensity" + i); //key 
            //for (int j = 0; j < numMaster; j++){
            for (int j = 0; j < numKept; j++){
 //Do quality control here:
           if ( masterIntens[j][i] < tiny ){
              masterIntens[j][i] = tiny;
               }
               System.out.format("%13.8f%n", Math.log(masterIntens[j][i])); //value
            }
         }

////
//   //Block 5: approximately rectified flux spectrum in spectrum synthesis region: 
//   //keys:
//        System.out.println("logWaveSS,logFluxSS");
//        double[] specSynLams = new double[numSpecSyn];
//        double[][] specSynFlux = new double[2][numSpecSyn];
//   //values:
//        int iCount = 0; 
//        for (int i = iStart; i < iStop; i++){
//
//   //This doesn't seem very useful (yet), but let's do it anyway for clarity:
//           specSynLams[iCount] = masterLams[i]; 
// //Do quality control here:
//           if ( (masterFlux[1][i] < logTiny) || (masterFlux[0][i] < tiny) ){
//              masterFlux[1][i] = logTiny;
//              masterFlux[0][i] = tiny;
//               }
//           //approximate rectification:
//           specSynFlux[1][iCount] = masterFlux[1][i] - Planck.planck(teff, masterLams[i]);
//           specSynFlux[0][iCount] = Math.exp(specSynFlux[1][iCount]);
//           System.out.format("%13.8f,%13.8f%n", Math.log(specSynLams[iCount]), specSynFlux[1][iCount]); 
//           iCount++;
 //        }

//Block 6: line ID tags for lines included in spectrum:
        System.out.println("listElement,listStage,listLam0");
        for (int iGauss = 0; iGauss < numGaussLines; iGauss++) {
             System.out.format("%3s,%4s,%9.3f%n",list2Element[gaussLine_ptr[iGauss]], list2StageRoman[gaussLine_ptr[iGauss]], 
                                 1.0e7*list2Lam0[gaussLine_ptr[iGauss]]); 
                }


   //Block 7: continuum flux spectrum (SED)
   //keys:
        System.out.println("logWaveC,logFluxC");
   //values:
        for (int i = 0; i < numLams; i++){
 //Do quality control here:
           if ( (contFlux[1][i] < logTiny) || (contFlux[0][i] < tiny) ){
              contFlux[1][i] = logTiny;
              contFlux[0][i] = tiny;
               }
           System.out.format("%13.8f,%13.8f%n", Math.log(lambdaScale[i]), contFlux[1][i]); 
         }

   //Block 8: linear monochromatic continuum limb darkening coefficients (LDCs):
   //keys:
        System.out.println("LDC");
   //values:
        for (int i = 0; i < numLams; i++){
           System.out.format("%13.8f%n", ldc[i]);
         }

//
//Block 9: A12 abundances of chemical species
   //keys:
          System.out.println("element,abundance");
      //values:
       for (int i = 0; i < nelemAbnd; i++){
          System.out.format("%4s,%9.3f%n", cname[i], eheu[i]);  
       }

//
//Block 10: Ground state ionization Es & total ion stage log_e populations for chemical species (ionization stages)

   //keys:
        System.out.println("species,ionStage,chiI,logNumTau1");
   //values:
        //Ground state ionization E - Stage I (eV) 
   for (int iElem = 0; iElem < nelemAbnd; iElem++){
     for (int iStage = 0; iStage < numStages; iStage++){
       species = "HI"; //default initialization
       switch (iStage){
          case 0: species = cname[iElem] + "I";
          break;
          case 1: species = cname[iElem] + "II";
          break;
          case 2: species = cname[iElem] + "III";
          break;
          case 3: species = cname[iElem] + "IV";
          break;
          case 4: species = cname[iElem] + "V";
          break;
          case 5: species = cname[iElem] + "VI";
          break;
       }
       double thisChiI = IonizationEnergy.getIonE(species);  //ev
       System.out.format("%7s,%03d,%9.3f,%13.8f%n", cname[iElem], iStage, thisChiI, tauOneStagePops[iElem][iStage]);
    } //iStage loop
  } //iElem loop


// Block 11 for Phil Bennett's GAS package output

       //Structure: One major vectorized block per species with the 
      // species name followed by the partial pressure depth structure for that species: 
        for (int i = 0; i < gsNspec; i++){
            System.out.println("Gas" + i);  //key
            System.out.format("%6s%n", gsName[i]);  //key
            System.out.println("PrtlPrs" + i); //key 
            //for (int j = 0; j < numMaster; j++){
            for (int j = 1; j < numDeps; j+=4){
 //Do quality control here:
              if ( log10MasterGsPp[i][j] < logE*logTiny ){
                 log10MasterGsPp[i][j] = logE*logTiny;
              }
               System.out.format("%13.8f%n", log10MasterGsPp[i][j]); //value
            }
        }

           
//        System.out.println("areNoLines");
//        System.out.format("%b%n",areNoLines);

//
    } // end main()

        //

} //end class GrayStar3Server
