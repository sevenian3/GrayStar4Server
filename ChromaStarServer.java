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
public class ChromaStarServer {

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
//Associate diatomic molecules with each element that forms significant molecules:
//Initialize arrays:
  int numAssocMols = 4; //max number of associated molecules
  String[][] cnameMols = new String[nelemAbnd][numAssocMols];
  for (int iElem = 0; iElem < nelemAbnd; iElem++){
     for (int iMol = 0; iMol < numAssocMols; iMol++){
         cnameMols[iElem][iMol] = "None";
     }  //iMol loop
  } //iElem loop
//CAUTION: cnameMols names should match mnames names in general list of molecules blow
//List the four molecular species most likely to deplete the atomic species A
  cname[0]="H";
  cnameMols[0][0] = "H2";
  cnameMols[0][1] = "H2+";
  cnameMols[0][2] = "CH";
  cnameMols[0][3] = "OH";
  cname[1]="He";
  cname[2]="Li";
  cname[3]="Be";
  cname[4]="B";
  cname[5]="C";
  cnameMols[5][0] = "CH";
  cnameMols[5][1] = "CO";
  cnameMols[5][2] = "CN";
  cnameMols[5][3] = "C2";
  cname[6]="N";
  cnameMols[6][0] = "NH";
  cnameMols[6][1] = "NO";
  cnameMols[6][2] = "CN";
  cnameMols[6][3] = "N2";
  cname[7]="O";
  cnameMols[7][0] = "OH";
  cnameMols[7][1] = "CO";
  cnameMols[7][2] = "NO";
  cnameMols[7][3] = "O2";
  cname[8]="F";
  cname[9]="Ne";
  cname[10]="Na";
  cname[11]="Mg";
  cnameMols[11][0] = "MgH";
  cname[12]="Al";
  cname[13]="Si";
  cnameMols[13][0] = "SiO";
  cname[14]="P";
  cname[15]="S";
  cname[16]="Cl";
  cname[17]="Ar";
  cname[18]="K";
  cname[19]="Ca";
  cnameMols[19][0] = "CaH";
  cnameMols[19][1] = "CaO";
  cname[20]="Sc";
  cname[21]="Ti";
  cnameMols[21][0] = "TiO";
  cname[22]="V";
  cnameMols[22][0] = "VO";
  cname[23]="Cr";
  cname[24]="Mn";
  cname[25]="Fe";
  cnameMols[25][0] = "FeO";
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

//Diatomic molecules:
 // int nMols = 18;
  int nMols = 1;
  String[] mname = new String[nMols];
  String[] mnameA = new String[nMols];
  String[] mnameB = new String[nMols];

//CAUTION: The molecular number densities, N_AB, will be computed, and will deplete the atomic species, in this order!
// Put anything where A or B is Hydrogen FIRST - HI is an inexhaustable reservoir at low T
// Then rank molecules according to largest of A and B abundance, "weighted" by dissociation energy - ??
//
// For constituent atomic species, A and B, always designate as 'A' whichever element participates in the
//  *fewest other* molecuels - we'll put A on the LHS of the molecular Saha equation

/*  mname[0] = "H2";
  mnameA[0] = "H";
  mnameB[0] = "H";
  mname[1] = "H2+";
  mnameA[1] = "H";
  mnameB[1] = "H";
  mname[2] = "OH";
  mnameA[2] = "O";
  mnameB[2] = "H";
  mname[3] = "CH";
  mnameA[3] = "C";
  mnameB[3] = "H";
  mname[4] = "NH";
  mnameA[4] = "N";
  mnameB[4] = "H";
  mname[5] = "MgH";
  mnameA[5] = "Mg";
  mnameB[5] = "H";
  mname[6] = "CaH";
  mnameA[6] = "Ca";
  mnameB[6] = "H";
  mname[7] = "O2";
  mnameA[7] = "O";
  mnameB[7] = "O";
  mname[8] = "CO";
  mnameA[8] = "C";
  mnameB[8] = "O";
  mname[9] = "C2";
  mnameA[9] = "C";
  mnameB[9] = "C";
  mname[10] = "NO";
  mnameA[10] = "N";
  mnameB[10] = "O";
  mname[11] = "CN";
  mnameA[11] = "C";
  mnameB[11] = "N";
  mname[12] = "N2";
  mnameA[12] = "N";
  mnameB[12] = "N";
  mname[13] = "FeO";
  mnameA[13] = "Fe";
  mnameB[13] = "O";
  mname[14] = "SiO";
  mnameA[14] = "Si";
  mnameB[14] = "O";
  mname[15] = "CaO";
  mnameA[15] = "Ca";
  mnameB[15] = "O"; */
  mname[0] = "TiO";
  mnameA[0] = "Ti";
  mnameB[0] = "O";
/*  mname[17] = "VO";
  mnameA[17] = "V";
  mnameB[17] = "O"; */

//Set up for molecules with JOLA bands:
   double jolaTeff = 5000.0;
   int numJola = 3; //for now
   //int numJola = 1; //for now
   String[] jolaSpecies = new String[numJola]; // molecule name
   String[] jolaSystem = new String[numJola]; //band system
   int[] jolaDeltaLambda = new int[numJola]; //band system

   if (teff <= jolaTeff){

     jolaSpecies[0] = "TiO"; // molecule name
     jolaSystem[0] = "TiO_C3Delta_X3Delta"; //band system //DeltaLambda=0
     jolaDeltaLambda[0] = 0; 
     jolaSpecies[1] = "TiO"; // molecule name
     jolaSystem[1] = "TiO_c1Phi_a1Delta"; //band system //DeltaLambda=1
     jolaDeltaLambda[1] = 1; 
     jolaSpecies[2] = "TiO"; // molecule name
     jolaSystem[2] = "TiO_A3Phi_X3Delta"; //band system //DeltaLambda=0
     jolaDeltaLambda[2] = 1; 

   }

  double ATot = 0.0;
  double thisAz, eheuScale;
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
  double[][] log10UwAArr = new double[numStages][2];
  for (int i = 0; i < numStages; i++){
    log10UwAArr[i][0] = 0.0; //default initialization - logarithmic
    log10UwAArr[i][1] = 0.0; //default initialization - logarithmic
  }
 
//Ground state ionization E - Stage I (eV) 
  double[] chiIArr = new double[numStages];
// safe initialization:
  for (int i = 0; i < numStages; i++){
      chiIArr[i] = 999999.0;
  }
// //Ground state ionization E - Stage II (eV)
//
//For diatomic molecules:
 String speciesAB = " ";
 String speciesA = " ";
 String speciesB = " ";
 double massA, massB, logMuAB;
 double[][] masterMolPops = new double[nMols][numDeps];
//initialize masterMolPops for mass density (rho) calculation:
  for (int i = 0; i < nMols; i++){
    for (int j = 0; j < numDeps; j++){
       masterMolPops[i][j] = -49.0;  //these are logarithmic
    }
  }
//We will interpolate in atomic partition fns tabulated at two temperatures
  double[] thisUwAV = new double[2];
  double[] thisUwBV = new double[2];
//We will interpolate in molecular partition fns tabulated at five temperatures
  double[] thisQwAB = new double[5];
  double thisDissE;

//
  double[][] newNe = new double[2][numDeps]; 
  double[][] newPe = new double[2][numDeps]; 
  double[][] logNums = new double[numStages][numDeps]; 
// For diatomic molecules:
  double[] logNumA = new double[numDeps];
  double[] logNumB = new double[numDeps];
  double[] logNumFracAB = new double[numDeps];
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


//Variables for ionization/molecular equilibrium treatment:
//for diatomic molecules
  double[][] logNumBArr = new double[numAssocMols][numDeps];
//We will interpolate in atomic partition fns tabulated at two temperatures
  double[][] log10UwBArr = new double[numAssocMols][2]; //base 10 log

  double[] dissEArr = new double[numAssocMols];
//We will interpolate in molecular partition fns tabulated at five temperatures
  double[][] logQwABArr = new double[numAssocMols][5]; //natural log
  double[] logMuABArr = new double[numAssocMols];

// Arrays ofpointers into master molecule and element lists:
   int[] mname_ptr = new int[numAssocMols];
   int[] specB_ptr = new int[numAssocMols];
   int specA_ptr = 0;
   int specB2_ptr = 0;
   String mnameBtemplate = " ";

//
//
//We converge the Pgas - Pe relation first under the assumption that all free e^-s are from single ionizations
// a la David Gray Ch. 9.  
// This approach separates converging ionization fractions and Ne for spectrum synthesis purposes from
// converging the Pgas-Pe-N_H-N_He relation for computing the mean opacity for HSE
//
double[] thisTemp = new double[2];
double[] log10UwUArr = new double[2];
double[] log10UwLArr = new double[2];
double chiI, peNumerator, peDenominator, logPhi, logPhiOverPe, logOnePlusPhiOverPe, logPeNumerTerm, logPeDenomTerm;
//Begin Pgas-kapp iteration
    for (int pIter = 0; pIter < nOuterIter; pIter++){
//

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
    // that are base 10 log_10 U
           log10UwLArr = PartitionFn.getPartFn(species); //base 10 log_10 U
           species = cname[iElem] + "II";
           log10UwUArr = PartitionFn.getPartFn(species); //base 10 log_10 U
           logPhi = LevelPopsServer.sahaRHS(chiI, log10UwUArr, log10UwLArr, thisTemp);
           logPhiOverPe = logPhi - guessPe[1][iD];
           logOnePlusPhiOverPe = Math.log(1.0 + Math.exp(logPhiOverPe)); 
           logPeNumerTerm = logAz[iElem] + logPhiOverPe - logOnePlusPhiOverPe;
           peNumerator = peNumerator + Math.exp(logPeNumerTerm);
           logPeDenomTerm = logAz[iElem] + Math.log(1.0 + Math.exp(logPeNumerTerm));
           peDenominator = peDenominator + Math.exp(logPeDenomTerm);
       } //iElem chemical element loop
       newPe[1][iD] = guessPGas[1][iD] + Math.log(peNumerator) - Math.log(peDenominator); 
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

//Default initialization:
       for (int i = 0; i < numAssocMols; i++){
           for (int j = 0; j < numDeps; j++){
               logNumBArr[i][j] = -49.0;
           }
           log10UwBArr[i][0] = 0.0;
           log10UwBArr[i][1] = 0.0;
           dissEArr[i] = 29.0;  //eV
           for (int kk = 0; kk < 5; kk++){ 
               logQwABArr[i][kk] = Math.log(300.0);
           }
           logMuABArr[i] = Math.log(2.0) + Useful.logAmu();  //g
           mname_ptr[i] = 0;
           specB_ptr[i] = 0;
       }

       double defaultQwAB = Math.log(300.0); //for now
    //default that applies to most cases - neutral stage (I) forms molecules
       int specBStage = 0; //default that applies to most cases

   //For element A of main molecule being treated in *molecular* equilibrium:
   //For safety, assign default values where possible
       double nmrtrDissE = 15.0; //prohitively high by default
       double[] nmrtrLog10UwB = new double[2];
       nmrtrLog10UwB[0] = 0.0;
       nmrtrLog10UwB[1] = 0.0;
       double nmrtrLog10UwA = 0.0;
       double[] nmrtrLogQwAB = new double[5];
       for (int kk = 0; kk < 5; kk++){
          nmrtrLogQwAB[kk] = Math.log(300.0);
       }
       double nmrtrLogMuAB = Useful.logAmu();
       double[] nmrtrLogNumB = new double[numDeps];
       for (int i = 0; i < numDeps; i++){
          nmrtrLogNumB[i] = 0.0;
       }

     double totalIonic;
     double[] logGroundRatio = new double[numDeps];


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
    // that are base 10 log_10 U
       log10UwAArr[0] = PartitionFn.getPartFn(species); //base 10 log_10 U
       species = cname[iElem] + "II";
       chiIArr[1] = IonizationEnergy.getIonE(species);
       log10UwAArr[1] = PartitionFn.getPartFn(species); //base 10 log_10 U
       species = cname[iElem] + "III";
       chiIArr[2] = IonizationEnergy.getIonE(species);
       log10UwAArr[2] = PartitionFn.getPartFn(species); //base 10 log_10 U
       species = cname[iElem] + "IV";
       chiIArr[3] = IonizationEnergy.getIonE(species);
       log10UwAArr[3] = PartitionFn.getPartFn(species); //base 10 log_10 U
       species = cname[iElem] + "V";
       chiIArr[4] = IonizationEnergy.getIonE(species);
       log10UwAArr[4] = PartitionFn.getPartFn(species); //base 10 log_10 U
       species = cname[iElem] + "VI";
       chiIArr[5] = IonizationEnergy.getIonE(species);
       log10UwAArr[5] = PartitionFn.getPartFn(species); //base 10 log_10 U
       //double logN = (eheu[iElem] - 12.0) + logNH;

       int thisNumMols = 0; //default initialization
       for (int iMol = 0; iMol < numAssocMols; iMol++){
          //console.log("iMol " + iMol + " cnameMols " + cnameMols[iElem][iMol]);
          if (cnameMols[iElem][iMol] == "None"){
            break;
          }
          thisNumMols++;
       }
//FLAG!  Accounting for mols in ion eq destabilizes everything 
thisNumMols = 0;
     if (thisNumMols > 0){
       //Find pointer to molecule in master mname list for each associated molecule:
       for (int iMol = 0; iMol < thisNumMols; iMol++){
          for (int jj = 0; jj < nMols; jj++){
             if (cnameMols[iElem][iMol] == mname[jj]){
                mname_ptr[iMol] = jj; //Found it!
                break;
             }
          } //jj loop in master mnames list
       } //iMol loop in associated molecules
//Now find pointer to atomic species B in master cname list for each associated molecule found in master mname list!
       for (int iMol = 0; iMol < thisNumMols; iMol++){
          for (int jj = 0; jj < nelemAbnd; jj++){
             if (mnameB[mname_ptr[iMol]] == cname[jj]){
                specB_ptr[iMol] = jj; //Found it!
                break;
             }
          } //jj loop in master cnames list
       } //iMol loop in associated molecules

//Now load arrays with molecular species AB and atomic species B data for method stagePops2()
       for (int iMol = 0; iMol < thisNumMols; iMol++){
  //special fix for H^+_2:
         if (mnameB[mname_ptr[iMol]] == "H2+"){
            specBStage = 1;
         } else {
            specBStage = 0;
         }
          for (int iTau = 0; iTau < numDeps; iTau++){
             //console.log("iMol " + iMol + " iTau " + iTau + " specB_ptr[iMol] " + specB_ptr[iMol]);
//Note: Here's one place where ionization equilibrium iteratively couples to molecular equilibrium!
             logNumBArr[iMol][iTau] = masterStagePops[specB_ptr[iMol]][specBStage][iTau];
          }
          dissEArr[iMol] = IonizationEnergy.getDissE(mname[mname_ptr[iMol]]);
          species = cname[specB_ptr[iMol]] + "I"; //neutral stage
          log10UwBArr[iMol] = PartitionFn.getPartFn(species); //base 10 log_10 U
          //logQwABArr[iMol] = defaultQwAB;
          logQwABArr[iMol] = PartitionFn.getMolPartFn(mname[mname_ptr[iMol]]);
          //Compute the reduced mass, muAB, in g:
          massA = AtomicMass.getMass(cname[iElem]);
          massB = AtomicMass.getMass(cname[specB_ptr[iMol]]);
          logMuABArr[iMol] = Math.log(massA) + Math.log(massB) - Math.log(massA + massB) + Useful.logAmu();
       }
   } //if thisNumMols > 0 condition

       logNums = LevelPopsServer.stagePops2(logNz[iElem], guessNe, chiIArr, log10UwAArr,
                     thisNumMols, logNumBArr, dissEArr, log10UwBArr, logQwABArr, logMuABArr,
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

//H & He only for now... we only compute H, He, and e^- opacity sources: 
      logKappaHHe = Kappas.kappas2(numDeps, newPe, zScale, temp, rho,
                     numLams, lambdaScale, logAz[1],
                     masterStagePops[0][0], masterStagePops[0][1], 
                     masterStagePops[1][0], masterStagePops[1][1], newNe, 
                     teff, logTotalFudge);

//Add in metal b-f opacity from adapted Moog routines:
      logKappaMetalBF = KappasMetal.masterMetal(numDeps, numLams, temp, lambdaScale, masterStagePops);
//Add in Rayleigh scattering opacity from adapted Moog routines:
      logKappaRayl = KappasRayl.masterRayl(numDeps, numLams, temp, lambdaScale, masterStagePops, masterMolPops);

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

//diagnostic
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
   //int tauKapPnt1 = ToolBox.tauPoint(numDeps, tauRos, 1.0);
   //System.out.println("logTauRos " + logE*tauRos[1][tauKapPnt1] + " temp " + temp[0][tauKapPnt1] + " pGas " + logE*pGas[1][tauKapPnt1]);
   //for (int iL = 0; iL < numLams; iL++){
   //    //System.out.println(" " + lambdaScale[iL] + " " + logE*logKappa[iL][tauKapPnt1]); 
   //} 

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

//Default initialization:
       for (int i = 0; i < numAssocMols; i++){
           for (int j = 0; j < numDeps; j++){
               logNumBArr[i][j] = -49.0;
           }
           log10UwBArr[i][0] = 0.0;
           log10UwBArr[i][1] = 0.0;
           dissEArr[i] = 29.0;  //eV
           for (int kk = 0; kk < 5; kk++){
               logQwABArr[i][kk] = Math.log(300.0);
           }
           logMuABArr[i] = Math.log(2.0) + Useful.logAmu();  //g
           mname_ptr[i] = 0;
           specB_ptr[i] = 0;
       }

       double defaultQwAB = Math.log(300.0); //for now
    //default that applies to most cases - neutral stage (I) forms molecules
       int specBStage = 0; //default that applies to most cases

   //For element A of main molecule being treated in *molecular* equilibrium:
   //For safety, assign default values where possible
       double nmrtrDissE = 15.0; //prohitively high by default
       double[] nmrtrLog10UwB = new double[2];
       nmrtrLog10UwB[0] = 0.0;
       nmrtrLog10UwB[1] = 0.0;
       double nmrtrLog10UwA = 0.0;
       double[] nmrtrLogQwAB = new double[5];
       for (int kk = 0; kk < 5; kk++){
          nmrtrLogQwAB[kk] = Math.log(300.0);
       }
       double nmrtrLogMuAB = Useful.logAmu();
       double[] nmrtrLogNumB = new double[numDeps];
       for (int i = 0; i < numDeps; i++){
          nmrtrLogNumB[i] = 0.0;
       }

     double totalIonic;
     double[] logGroundRatio = new double[numDeps];


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
    // that are base 10 log_10 U
       log10UwAArr[0] = PartitionFn.getPartFn(species); //base 10 log_10 U
       species = cname[iElem] + "II";
       chiIArr[1] = IonizationEnergy.getIonE(species);
       log10UwAArr[1] = PartitionFn.getPartFn(species); //base 10 log_10 U
       species = cname[iElem] + "III";
       chiIArr[2] = IonizationEnergy.getIonE(species);
       log10UwAArr[2] = PartitionFn.getPartFn(species); //base 10 log_10 U
       species = cname[iElem] + "IV";
       chiIArr[3] = IonizationEnergy.getIonE(species);
       log10UwAArr[3]= PartitionFn.getPartFn(species); //base 10 log_10 U
       species = cname[iElem] + "V";
       chiIArr[4] = IonizationEnergy.getIonE(species);
       log10UwAArr[4]= PartitionFn.getPartFn(species); //base 10 log_10 U
       species = cname[iElem] + "VI";
       chiIArr[5] = IonizationEnergy.getIonE(species);
       log10UwAArr[5]= PartitionFn.getPartFn(species); //base 10 log_10 U

       int thisNumMols = 0; //default initialization
       for (int iMol = 0; iMol < numAssocMols; iMol++){
          if (cnameMols[iElem][iMol] == "None"){
            break;
          }
          thisNumMols++;
       }
//FLAG!
//thisNumMols = 0;
     if (thisNumMols > 0){
       //Find pointer to molecule in master mname list for each associated molecule:
       for (int iMol = 0; iMol < thisNumMols; iMol++){
          for (int jj = 0; jj < nMols; jj++){
             if (cnameMols[iElem][iMol] == mname[jj]){
                mname_ptr[iMol] = jj; //Found it!
                break;
             }
          } //jj loop in master mnames list
       } //iMol loop in associated molecules
//Now find pointer to atomic species B in master cname list for each associated molecule found in master mname list!
       for (int iMol = 0; iMol < thisNumMols; iMol++){
          for (int jj = 0; jj < nelemAbnd; jj++){
             if (mnameB[mname_ptr[iMol]] == cname[jj]){
                specB_ptr[iMol] = jj; //Found it!
                break;
             }
          } //jj loop in master cnames list
       } //iMol loop in associated molecules

//Now load arrays with molecular species AB and atomic species B data for method stagePops2()
       for (int iMol = 0; iMol < thisNumMols; iMol++){
  //special fix for H^+_2:
         if (mnameB[mname_ptr[iMol]] == "H2+"){
            specBStage = 1;
         } else {
            specBStage = 0;
         }
          for (int iTau = 0; iTau < numDeps; iTau++){
//Note: Here's one place where ionization equilibrium iteratively couples to molecular equilibrium!
             logNumBArr[iMol][iTau] = masterStagePops[specB_ptr[iMol]][specBStage][iTau];
          }
          dissEArr[iMol] = IonizationEnergy.getDissE(mname[mname_ptr[iMol]]);
          species = cname[specB_ptr[iMol]] + "I"; //neutral stage
          log10UwBArr[iMol] = PartitionFn.getPartFn(species); //base 10 log_10 U
          //logQwABArr[iMol] = defaultQwAB;
          logQwABArr[iMol] = PartitionFn.getMolPartFn(mname[mname_ptr[iMol]]);
          //Compute the reduced mass, muAB, in g:
          massA = AtomicMass.getMass(cname[iElem]);
          massB = AtomicMass.getMass(cname[specB_ptr[iMol]]);
          logMuABArr[iMol] = Math.log(massA) + Math.log(massB) - Math.log(massA + massB) + Useful.logAmu();
       }
   } //if thisNumMols > 0 condition

       logNums = LevelPopsServer.stagePops2(logNz[iElem], guessNe, chiIArr, log10UwAArr,
                     thisNumMols, logNumBArr, dissEArr, log10UwBArr, logQwABArr, logMuABArr,
                     numDeps, temp);

     for (int iStage = 0; iStage < numStages; iStage++){
          for (int iTau = 0; iTau < numDeps; iTau++){
            masterStagePops[iElem][iStage][iTau] = logNums[iStage][iTau];
 //save ion stage populations at tau = 1:
       } //iTau loop
       tauOneStagePops[iElem][iStage] = logNums[iStage][iTauOne];
    } //iStage loop
  } //iElem loop

// Compute all molecular populations:
//
// *** CAUTION: specB2_ptr refers to element B of main molecule being treated
// specB_ptr[] is an array of pointers to element B of all molecules associated with
// element A
// mname_ptr[] is an array of pointers pointing to the molecules themselves that are
// associated with element A
   double[] log10UwA = new double[2];
   for (int iMol = 0; iMol < nMols; iMol++){

 //Find elements A and B in master atomic element list:
    specA_ptr = 0;
    specB2_ptr = 0;
    for (int jj = 0; jj < nelemAbnd; jj++){
       if (mnameA[iMol] == cname[jj]){
         specA_ptr = jj;
         break;  //found it!
       }
    }
// Get its partition fn:
    species = cname[specA_ptr] + "I"; //neutral stage
    log10UwA = PartitionFn.getPartFn(species); //base 10 log_10 U
    for (int jj = 0; jj < nelemAbnd; jj++){
       if (mnameB[iMol] == cname[jj]){
         specB2_ptr = jj;
         break;  //found it!
       }
    }

//We will solve for N_AB/N_A - neutral stage of species A (AI) will be kept on LHS of molecular Saha equations -
// Therefore, we need ALL the molecules species A participates in - including the current molecule itself
// - at this point, it's just like setting up the ionization equilibrium to account for molecules as above...
       int thisNumMols = 0; //default initialization
       for (int im = 0; im < numAssocMols; im++){
          //console.log("iMol " + iMol + " cnameMols " + cnameMols[iElem][iMol]);
          if (cnameMols[specA_ptr][im] == "None"){
            break;
          }
          thisNumMols++;
       }
     if (thisNumMols > 0){
       //Find pointer to molecule in master mname list for each associated molecule:
       for (int im = 0; im < thisNumMols; im++){
          for (int jj = 0; jj < nMols; jj++){
             if (cnameMols[specA_ptr][im] == mname[jj]){
                mname_ptr[im] = jj; //Found it!
                break;
             }
          } //jj loop in master mnames list
       } //im loop in associated molecules

//Now find pointer to atomic species B in master cname list for each associated molecule found in master mname list!
       for (int im = 0; im < thisNumMols; im++){
          mnameBtemplate = " "; //initialization
// "Species B" is whichever element is NOT species "A" in master molecule
          if (mnameB[mname_ptr[im]] == mnameA[iMol]){
             //get the *other* atom
             mnameBtemplate = mnameA[mname_ptr[im]];
          } else {
             mnameBtemplate = mnameB[mname_ptr[im]];
          }
          for (int jj = 0; jj < nelemAbnd; jj++){
             if (mnameBtemplate == cname[jj]){
                //console.log("If condition met: jj " + jj + " cname[jj] " + cname[jj]);
                specB_ptr[im] = jj; //Found it!
                break;
             }
          } //jj loop in master cnames list
       } //iMol loop in associated molecules

//Now load arrays with molecular species AB and atomic species B data for method molPops()
       for (int im = 0; im < thisNumMols; im++){
      //special fix for H^+_2:
         if (mname[mname_ptr[im]] == "H2+"){
           specBStage = 1;
         } else {
           specBStage = 0;
         }
          for (int iTau = 0; iTau < numDeps; iTau++){
//Note: Here's one place where ionization equilibrium iteratively couples to molecular equilibrium!
             logNumBArr[im][iTau] = masterStagePops[specB_ptr[im]][specBStage][iTau];
          }
          dissEArr[im] = IonizationEnergy.getDissE(mname[mname_ptr[im]]);
          species = cname[specB_ptr[im]] + "I";
          log10UwBArr[im] = PartitionFn.getPartFn(species); //base 10 log_10 U
          //logQwABArr[im] = defaultQwAB;
          logQwABArr[im] = PartitionFn.getMolPartFn(mname[mname_ptr[im]]);
          //Compute the reduced mass, muAB, in g:
          massA = AtomicMass.getMass(cname[specA_ptr]);
          massB = AtomicMass.getMass(cname[specB_ptr[im]]);
          logMuABArr[im] = Math.log(massA) + Math.log(massB) - Math.log(massA + massB) + Useful.logAmu();
 // One of the species A-associated molecules will be the actual molecule, AB, for which we want
 // the population - pick this out for the numerator in the master fraction:
          if (mname[mname_ptr[im]] == mname[iMol]){
              nmrtrDissE = dissEArr[im];
              nmrtrLog10UwB[0] = log10UwBArr[im][0];
              nmrtrLog10UwB[1] = log10UwBArr[im][1];
              for (int kk = 0; kk < 5; kk++){
                  nmrtrLogQwAB[kk] = logQwABArr[im][kk];
              }
              nmrtrLogMuAB = logMuABArr[im];
              for (int iTau = 0; iTau < numDeps; iTau++){
                 nmrtrLogNumB[iTau] = logNumBArr[im][iTau];
              }
          }
       } //im loop
//
   } //if thisNumMols > 0 condition
   //Compute total population of particle in atomic ionic stages over number in ground ionization stage
   //for master denominator so we don't have to re-compue it:
         for (int iTau = 0; iTau < numDeps; iTau++){
           //initialization:
           totalIonic = 0.0;
           for (int iStage = 0; iStage < numStages; iStage++){
              totalIonic = totalIonic + Math.exp(masterStagePops[specA_ptr][iStage][iTau]);
           }
           logGroundRatio[iTau] = Math.log(totalIonic) - masterStagePops[specA_ptr][0][iTau];
         } //iTau loop
       logNumFracAB = LevelPopsServer.molPops(nmrtrLogNumB, nmrtrDissE, log10UwA, nmrtrLog10UwB, nmrtrLogQwAB, nmrtrLogMuAB,
                     thisNumMols, logNumBArr, dissEArr, log10UwBArr, logQwABArr, logMuABArr,
                     logGroundRatio, numDeps, temp);

//Load molecules into master molecular population array:
   //System.out.println("iMol " + iMol);
      for (int iTau = 0; iTau < numDeps; iTau++){
         masterMolPops[iMol][iTau] = logNz[specA_ptr][iTau] + logNumFracAB[iTau];
         //if (iTau%5 == 1){
         //  System.out.println("iTau " + iTau + " logNz[specA_ptr] " + logNz[specA_ptr][iTau] + " logNumFracAB " + logNumFracAB[iTau]);
         //}
    } //iTau loop
  } //master iMol loop
//
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
// This holds 2-element temperature-dependent base 10 logarithmic parition fn:
        double[] thisUwV = new double[2];
        thisUwV[0] = 0.0; //default initialization
        thisUwV[1] = 0.0;
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
                thisUwV = PartitionFn.getPartFn(species); //base 10 log_10 U
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
            double[] numHelp = LevelPopsServer.levelPops(list2Lam0[iLine], list2LogNums[logNums_ptr], list2ChiL[iLine], thisUwV, 
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
                    numDeps, zScaleList, tauRos, temp, rho);
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

// This holds 2-element temperature-dependent base 10 logarithmic parition fn:
        thisUwV[0] = 0.0; //default initialization
        thisUwV[1] = 0.0;

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
                thisUwV = PartitionFn.getPartFn(species); //base 10 log_10 U
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
            double[] numHelp = LevelPopsServer.levelPops(list2Lam0[gaussLine_ptr[iLine]], list2LogNums[logNums_ptr], list2ChiL[gaussLine_ptr[iLine]], thisUwV,
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
            double[][] listLinePoints = LineGrid.lineGridVoigt(list2Lam0[gaussLine_ptr[iLine]], list2Mass[gaussLine_ptr[iLine]], xiT, numDeps, teff, listNumCore, listNumWing);
            //if ( (list2Element[gaussLine_ptr[iLine]].equals("Na")) && (list2Stage[gaussLine_ptr[iLine]] == 0) ){
            //   System.out.println("iLine "+ iLine+ " gaussLine_ptr "+ gaussLine_ptr[iLine]+ " list2Lam0 "+ list2Lam0[gaussLine_ptr[iLine]]+ " list2LogAij "+ 
          //list2LogAij[gaussLine_ptr[iLine]]+ " list2LogGammaCol "+ list2LogGammaCol[gaussLine_ptr[iLine]]+ " list2Logf "+ list2Logf[gaussLine_ptr[iLine]]);
           // }
            if (species.equals("HI")){
 //System.out.println("Calling Stark...");
                 listLineProf = LineProf.stark(listLinePoints, list2Lam0[gaussLine_ptr[iLine]], list2LogAij[gaussLine_ptr[iLine]],
                    list2LogGammaCol[gaussLine_ptr[iLine]],
                    numDeps, teff, tauRos, temp, pGas, newNe, tempSun, pGasSun, hjertComp);
            } else {
                 listLineProf = LineProf.voigt(listLinePoints, list2Lam0[gaussLine_ptr[iLine]], list2LogAij[gaussLine_ptr[iLine]],
                    list2LogGammaCol[gaussLine_ptr[iLine]],
                    numDeps, teff, tauRos, temp, pGas, tempSun, pGasSun, hjertComp);
            } 
            //if ( (list2Element[gaussLine_ptr[iLine]].equals("Na")) && (list2Stage[gaussLine_ptr[iLine]] == 0) ){
            //   System.out.println("iLine "+ iLine+ " gaussLine_ptr "+ gaussLine_ptr[iLine]+ "list2Logf "+ list2Logf[gaussLine_ptr[iLine]]);
            //}
            double[][] listLogKappaL = LineKappa.lineKap(list2Lam0[gaussLine_ptr[iLine]], list2LogNums[2], list2Logf[gaussLine_ptr[iLine]], listLinePoints, listLineProf,
                    numDeps, zScaleList, tauRos, temp, rho);
            //if ( (list2Element[gaussLine_ptr[iLine]].equals("Na")) && (list2Stage[gaussLine_ptr[iLine]] == 0) ){
            //  for (int iTau = 0; iTau < numDeps; iTau++){
            //    if (iTau%5 == 1){
            //        for (int iL = 0; iL < listNumPoints; iL++){
            //            if (iL%2 == 0){
            //                System.out.println("iTau "+ iTau+ " iL "+ iL+ " listLinePoints[0]&[1] "+ listLinePoints[0][iL]+ "  "+ listLinePoints[1][iL]+" listLineProf "+ listLineProf[iL][iTau]  + " listLogKappaL "+ logE*listLogKappaL[iL][iTau]);
            //            }
            //        }
            //    }
            //  }
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
////

 if (teff <= jolaTeff){
//Begin loop over JOLA bands - isert JOLA oapcity into opacity spectum...
   double helpJolaSum = 0.0;
  
 if (ifTiO == 1){

   for (int iJola = 0; iJola < numJola; iJola++){

      //Find species in molecule set:
      for (int iMol = 0; iMol < nMols; iMol++){
        if (mname[iMol].equals(jolaSpecies[iJola])){
          //System.out.println("mname " + mname[iMol]);
          for (int iTau= 0; iTau < numDeps; iTau++){
             logNumJola[iTau] = masterMolPops[iMol][iTau];
            // double logTiOpp = logNumJola[iTau] + temp[1][iTau] + Useful.logK();
            // System.out.println("TiO pp " + logE*logTiOpp);
          }
        }
      }

        jolaOmega0 = MolecData.getOrigin(jolaSystem[iJola]);  //band origin ?? //Freq in Hz OR waveno in cm^-1 ??
        jolaRSqu = MolecData.getSqTransMoment(jolaSystem[iJola]); //needed for total vibrational band oscillator strength (f_v'v")
        jolaB = MolecData.getRotConst(jolaSystem[iJola]); // B' and b" values of upper and lower vibational state
        jolaLambda = MolecData.getWaveRange(jolaSystem[iJola]); //approx wavelength range of band
        //Line strength factor from Allen's 4th Ed., p. 88, "script S":
        jolaQuantumS = MolecData.getQuantumS(jolaSystem[iJola]); 

//Compute line strength, S, Allen, p. 88:
        jolaS = jolaRSqu * jolaQuantumS; //may not be this simple (need q?)
//Compute logf , Allen, p. 61 Section 4.4.2 - for atoms or molecules - assumes g=1 so logGf = logF:
        //jolaLogF = logSTofHelp + Math.log(jolaOmega0) + Math.log(jolaS); //if omega0 is a freq in Hz
        //Gives wrong result?? jolaLogF = logSTofHelp + Useful.logC() + Math.log(jolaOmega0) + Math.log(jolaS); //if omega0 is a waveno in cm^-1 
        double checkgf = 303.8*jolaS/(10.0*jolaLambda[0]); //"Numerical relation", Allen 4th, p. 62 - lambda in A
        jolaLogF = Math.log(checkgf); //better??

        if (jolaDeltaLambda[iJola] == 0){ 
           jolaAlphP = jolaAlphP_DL0; // alpha_P - weight of P branch (Delta J = 1)
           jolaAlphR = jolaAlphR_DL0; // alpha_R - weight of R branch (Delta J = -1)
           jolaAlphQ = jolaAlphQ_DL0; // alpha_Q - weight of Q branch (Delta J = 0)
        }
        if (jolaDeltaLambda[iJola] != 0){ 
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
     //Block 1: Array dimensions
     //keys:
        System.out.println("numDeps,numMaster,numThetas,numGaussLines,numLams,nelemAbnd,numSpecies"); 
     //values:
        System.out.format("%03d,%07d,%03d,%06d,%07d,%05d,%04d%n", 
            numDeps, numKept, numThetas, numGaussLines, numLams, nelemAbnd, numSpecies);

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

           
//        System.out.println("areNoLines");
//        System.out.format("%b%n",areNoLines);

//
    } // end main()

        //

} //end class GrayStar3Server
