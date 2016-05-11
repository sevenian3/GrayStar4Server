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

/**
 *
 * @author Ian
 */
public class GrayStar3Server {

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
        double logZScale = (Double.valueOf(logZStr)).doubleValue();

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
        if (logZScale < -3.0) {
            logZScale = -3.0;
            logZStr = "-3.0";
        }
        if (logZScale > 1.0) {
            logZScale = 1.0;
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
        double zScale = Math.pow(10.0, logZScale);

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
        double lamUV = 300.0;
        double lamIR = 900.0;
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
      // //sigh - don't ask me - makes the Balmer lines show up around A0: 
      //  if (teff > F0Vtemp){
      //      logKapFudge = -1.5;
      //      logKapFudgeStr = "-1.5";
      //  } 

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
        lamSetup[0] = 200.0 * 1.0e-7;  // test Start wavelength, cm
        lamSetup[1] = 1000.0 * 1.0e-7; // test End wavelength, cm
        lamSetup[2] = 250;  // test number of lambda
        //int numLams = (int) (( lamSetup[1] - lamSetup[0] ) / lamSetup[2]) + 1;  
        int numLams = (int) lamSetup[2];

        //CONTINUUM lambda scale (nm)
        double lambdaScale[] = LamGrid.lamgrid(numLams, lamSetup); //cm

// Solar parameters:
        double teffSun = 5778.0;
        double loggSun = 4.44;
        double gravSun = Math.pow(10.0, loggSun);
        double logZScaleSun = 0.0;
        double zScaleSun = Math.exp(logZScaleSun);

//Solar units:
        double massSun = 1.0;
        double radiusSun = 1.0;
        //double massStar = 1.0; //solar masses // test
        double logRadius = 0.5 * (Math.log(massStar) + Math.log(gravSun) - Math.log(grav));
        double radius = Math.exp(logRadius); //solar radii
        //double radius = Math.sqrt(massStar * gravSun / grav); // solar radii
        double logLum = 2.0 * Math.log(radius) + 4.0 * Math.log(teff / teffSun);
        double bolLum = Math.exp(logLum); // L_Bol in solar luminosities 

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

  int nelemAbnd = 40;
  int[] nome = new int[nelemAbnd];
  double[] eheu = new double[nelemAbnd]; //log_10 "A_12" values
  double[] logAz = new double[nelemAbnd]; //N_z/H_H for element z
  double[][] logNz = new double[nelemAbnd][numDeps]; //N_z for element z
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

  double ATot = 0.0;
  double thisAz;
  for (int i = 0; i < nelemAbnd; i++){
     logAz[i] = logE10 * (eheu[i] - 12.0); //natural log
     thisAz = Math.exp(logAz[i]);
     ATot = ATot + thisAz;
     //System.out.println("i " + i + " logAz " + logE*logAz[i]);
  }
  double logATot = Math.log(ATot); //natural log
  //System.out.println("logATot " + logE*logATot);

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
    //System.out.println("Sun: ");
    //for (int i=0; i<numDeps; i++){
    //  System.out.println("i " + i + " tauRos[1][i] " + logE*tauRos[1][i] + " temp " + tempSun[0][i] + " pGasSun " + logE*pGasSun[1][i] + " NeSun " + logE*NeSun[1][i] + " rhoSun " + logE*rhoSun[1][i]);
   // }
        //
        // BEGIN Initial guess for Sun section:
        //
        //Rescaled  kinetic temeprature structure: 
        //double F0Vtemp = 7300.0;  // Teff of F0 V star (K)                           
        double[][] temp = new double[2][numDeps];
        if (teff < F0Vtemp) {
            //We're a cool star! - rescale from Teff=5000 reference model!
            temp = ScaleT5000.phxRefTemp(teff, numDeps, tauRos);
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
            //We're a cool star - rescale from  Teff=5000 reference model!
            // logAz[1] = log_e(N_He/N_H)
            guessPGas = ScaleT5000.phxRefPGas(grav, zScale, logAz[1], numDeps, tauRos);
            guessPe = ScaleT5000.phxRefPe(teff, grav, numDeps, tauRos, zScale, logAz[1]);
            guessNe = ScaleT5000.phxRefNe(numDeps, temp, guessPe); 
            //Ne = ScaleSolar.phxSunNe(grav, numDeps, tauRos, temp, kappaScale);
            //guessKappa = ScaleSolar.phxSunKappa(numDeps, tauRos, kappaScale);
        } else if (teff >= F0Vtemp) {
            //We're a HOT star!! - rescale from Teff=10000 reference model 
            // logAz[1] = log_e(N_He/N_H)
            guessPGas = ScaleT10000.phxRefPGas(grav, zScale, logAz[1], numDeps, tauRos);
            guessPe = ScaleT10000.phxRefPe(teff, grav, numDeps, tauRos, zScale, logAz[1]);
            guessNe = ScaleT10000.phxRefNe(numDeps, temp, guessPe);
            //logKapFudge = -1.5;  //sigh - don't ask me - makes the Balmer lines show up around A0 
        }
        //

        // END initial guess for Sun section
        //
        // *********************
   //Apr 2016: Replace the following procedure for model building with the following PSEUDOCODE:
   //
   // 1st guess Tk(tau), Pe(tau), Pg(tau) from rescaling reference hot or cool model above
   // 1) fI(tau), fII, fIII, fIV, fV(tau) from Saha(Tk(tau), Pe(tau))
   //    - needed for spectrum synthesis anyway LevelPops.stagePops() 
   // 2) A_Tot = Sigma_z(A_z) 
   //    -> N_H(tau) = (Pg(tau)-Pe(tau))/{kTk(tau)A_Tot}
   //    -> N_z(tau) = A_z * N_H(tau)
   //    -> rho(tau) = Sigma_z(m_z*N_z(tau))
   // 3) Ne(tau) from Sigma_z{fI(tau) .... 5*fV(tau) * N_z}
   //    --> New Pe(tau) = Ne(tau)kTk(tau)
   // 4) N(tau) = Sigma_z(N_z(tau)) + Ne(tau)
   //    mu(tau) = rho(tau) / N(tau)
   //
   // 5) kappa(tau) from Gray Ch. 8 sources
   // 6) P_Tot(tau) from HSE on tau scale with kappa from 5)
   //    - PRad(tau) from Tk(tau)
   //    - New Pg(tau) from P_Tot(tau)-PRad(tau)
   // 7) Iterate with updated Pg(tau) & Pe(tau)??
   // 8) Temp correction??   

        //
        // mean molecular weight and Ne for Star & Sun
        //double[] mmw = State.mmwFn(numDeps, temp, kappaScale);
//
 //       double[][] guessRho = State.massDensity(numDeps, temp, guessPGas, mmw, kappaScale);


  int numStages = 5;
  String species = " "; //default initialization
  double[] logNH = new double[numDeps];
  double rho[][] = new double[2][numDeps];
  double[][][] masterStagePops = new double[nelemAbnd][numStages][numDeps];
  double[][] tauOneStagePops = new double[nelemAbnd][numStages];
  double unity = 1.0;
  double zScaleList = 1.0; //initialization   
  double[] thisUw1V = new double[2];
  double[] thisUw2V = new double[2];
  double[] thisUw3V = new double[2];
  double[] thisUw4V = new double[2];
 //Ground state ionization E - Stage I (eV) 
  double thisChiI1;
 //Ground state ionization E - Stage II (eV)
  double thisChiI2;
  double thisChiI3;
  double thisChiI4;
  double[][] newNe = new double[2][numDeps]; 
  double[][] newPe = new double[2][numDeps]; 
  double[][] logNums = new double[numStages][numDeps]; 
  double[] Ng = new double[numDeps];
  double[] mmw = new double[numDeps];
  double logMmw;
  double[][] logKappa = new double[numLams][numDeps];
  double[][] kappaRos = new double[2][numDeps];
  double[][] pGas = new double[2][numDeps]; 
  double[][] pRad = new double[2][numDeps]; 
  double[] depths = new double[numDeps];
  double[][] newTemp = new double[2][numDeps];

//
//
//
//Begin Pgas/Pe iteration
    for (int pIter = 0; pIter < 1; pIter++){
//
//
//Get the number densities of the chemical elements at all depths  
     logNz = State.getNz(numDeps, temp, guessPGas, guessPe, ATot, nelemAbnd, logAz);
     for (int i = 0 ; i < numDeps; i++){ 
        logNH[i] = logNz[0][i];
        //System.out.println("i " + i + " logNH[i] " + logE*logNH[i]);
     } 
    
//Get mass density from chemical composition: 
     rho = State.massDensity2(numDeps, nelemAbnd, logNz, cname);
     //for (int i = 0 ; i < numDeps; i++){
       //System.out.println("i " + i + " rho " + logE*rho[1][i]);
     //}


//
//  Compute ionization fractions for Ne calculation 
//  AND
// Populate the ionization stages of all the species for spectrum synthesis:
//stuff to save ion stage pops at tau=1:
  int iTauOne = ToolBox.tauPoint(numDeps, tauRos, unity);
//
//  Default inializations:
       zScaleList = 1.0; //initialization   
       //these 2-element temperature-dependent partition fns are logarithmic  
       thisUw1V[0] = 0.0; 
       thisUw1V[1] = 0.0; 
       thisUw2V[0] = 0.0; 
       thisUw2V[1] = 0.0; 
       thisUw3V[0] = 0.0; 
       thisUw3V[1] = 0.0; 
       thisUw4V[0] = 0.0; 
       thisUw4V[1] = 0.0; 

// Iteration *within* the outer Pe-Pgas iteration:
//Iterate the electron densities and ionization fractions:
//
 for (int neIter = 0; neIter < 3; neIter++){
 
   for (int iElem = 0; iElem < nelemAbnd; iElem++){
       species = cname[iElem] + "I";
       thisChiI1 = IonizationEnergy.getIonE(species);
    //THe following is a 2-element vector of temperature-dependent partitio fns, U, 
    // that are base 10 log_10 U
       thisUw1V = PartitionFn.getPartFn(species); //base 10 log_10 U
       species = cname[iElem] + "II";
       thisChiI2 = IonizationEnergy.getIonE(species);
       thisUw2V = PartitionFn.getPartFn(species); //base 10 log_10 U
       species = cname[iElem] + "III";
       thisChiI3 = IonizationEnergy.getIonE(species);
       thisUw3V = PartitionFn.getPartFn(species); //base 10 log_10 U
       species = cname[iElem] + "IV";
       thisChiI4 = IonizationEnergy.getIonE(species);
       thisUw4V = PartitionFn.getPartFn(species); //base 10 log_10 U
       //double logN = (eheu[iElem] - 12.0) + logNH;
            //if H or He, make sure kappaScale is unity:
            if ((cname[iElem].equals("H"))
                    || (cname[iElem].equals("He"))) {
                zScaleList = 1.0;
                //fakeGw1 = 2.0;  //fix for Balmer lines
            } else {
                zScaleList = zScale;
                //fakeGw1 = 1.0;  //fix for Balmer lines
            }
       logNums = LevelPopsServer.stagePops(logNz[iElem], guessNe, thisChiI1,
             thisChiI2, thisChiI3, thisChiI4, thisUw1V, thisUw2V, thisUw3V, thisUw4V, 
             numDeps, zScaleList, tauRos, temp);
     for (int iStage = 0; iStage < numStages; iStage++){
          for (int iTau = 0; iTau < numDeps; iTau++){
            masterStagePops[iElem][iStage][iTau] = logNums[iStage][iTau];
 //save ion stage populations at tau = 1:
       } //iTau loop
       tauOneStagePops[iElem][iStage] = logNums[iStage][iTauOne];
    } //iStage loop
            //System.out.println("iElem " + iElem);
            //if (iElem == 1){
            //  for (int iTau = 0; iTau < numDeps; iTau++){
            //   System.out.println("cname: " + cname[iElem] + " " + logE*list2LogNums[0][iTau] + " " + logE*list2LogNums[1][iTau]);
            //  } 
            // }
  } //iElem loop

//Compute updated Ne & Pe:
     //initialize accumulation of electrons at all depths
     for (int iTau = 0; iTau < numDeps; iTau++){
       newNe[0][iTau] = 0.0; 
     }
     for (int iTau = 0; iTau < numDeps; iTau++){
        for (int iElem = 0; iElem < nelemAbnd; iElem++){
          newNe[0][iTau] = newNe[0][iTau] 
                   + Math.exp(masterStagePops[iElem][1][iTau])   //1 e^- per ion
                   + 2.0 * Math.exp(masterStagePops[iElem][2][iTau])   //2 e^- per ion
                   + 3.0 * Math.exp(masterStagePops[iElem][3][iTau])   //3 e^- per ion
                   + 4.0 * Math.exp(masterStagePops[iElem][4][iTau]);   //3 e^- per ion
        }
        newNe[1][iTau] = Math.log(newNe[0][iTau]);
// Update guess for iteration:
        guessNe[0][iTau] = newNe[0][iTau]; 
        guessNe[1][iTau] = newNe[1][iTau]; 
        newPe[1][iTau] = newNe[1][iTau] + Useful.logK() + temp[1][iTau];
        newPe[0][iTau] = Math.exp(newPe[1][iTau]);
       //System.out.println("iTau " + iTau + " newNe " + logE*newNe[1][iTau] + " newPe " + logE*newPe[1][iTau]);
     }

  } //end Ne - ionzation fraction iteration



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
       //System.out.println("i " + i + " Ng " + Math.log10(Ng[i]) + " mmw " + (mmw[i]/Useful.amu));
    }


      logKappa = Kappas.kappas2(numDeps, newPe, zScale, temp, rho,
                     numLams, lambdaScale, logAz[1],
                     masterStagePops[0][0], masterStagePops[0][1], 
                     masterStagePops[1][0], masterStagePops[1][1], newNe, 
                     teff, logKapFudge);

      kappaRos = Kappas.kapRos(numDeps, numLams, lambdaScale, logKappa, temp); 

      int t500 = ToolBox.lamPoint(numLams, lambdaScale, 500.0e-7);
 
 

        //press = Hydrostat.hydrostatic(numDeps, grav, tauRos, kappaRos, temp);
        pGas = Hydrostat.hydroFormalSoln(numDeps, grav, tauRos, kappaRos, temp, guessPGas);
        pRad = Hydrostat.radPress(numDeps, temp);

//Update Pgas and Pe guesses for iteration:
        for (int iTau = 0; iTau < numDeps; iTau++){
//CHEAT to accelrate self-consistency: Scale the new Pe's by pGas/guessPGas
//  - also helps avoid negative Nz and NH values.
            guessPe[1][iTau] = newPe[1][iTau] + pGas[1][iTau] - guessPGas[1][iTau]; //logarithmic
            guessPe[0][iTau] = Math.exp(guessPe[1][iTau]);
// Now we can update guessPGas:
            guessPGas[0][iTau] = pGas[0][iTau];
            guessPGas[1][iTau] = pGas[1][iTau];
            //System.out.println("iTau " + iTau + " pGas[0][iTau] " + logE*pGas[1][iTau] + " newPe[0][iTau] " + logE*newPe[1][iTau]);
        } 

 } //end Pgas/Pe iteration

        // Then construct geometric depth scale from tau, kappa and rho
        depths = DepthScale.depthScale(numDeps, tauRos, kappaRos, rho);

        //int numTCorr = 10;  //test
        int numTCorr = 0;
        for (int i = 0; i < numTCorr; i++) {
            //newTemp = TCorr.tCorr(numDeps, tauRos, temp);
            newTemp = MulGrayTCorr.mgTCorr(numDeps, teff, tauRos, temp, rho, kappaRos);
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


        //Okay - Now all the emergent radiation stuff:
        // Set up theta grid
        //  cosTheta is a 2xnumThetas array:
        // row 0 is used for Gaussian quadrature weights
        // row 1 is used for cos(theta) values
        // Gaussian quadrature:
        // Number of angles, numThetas, will have to be determined after the fact
        double cosTheta[][] = Thetas.thetas();
        int numThetas = cosTheta[0].length;

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

//
//     FILE I/O Section
//
String dataPath = "./InputData/";
//External line list input file approach:
//
//Stuff for byte file method:
//
// *** NOTE: bArrSize must have been noted from the stadout of LineListServer and be consistent
// with whichever line list is linked to gsLineListBytes.dat, and be st manually here:
 String lineListBytes = dataPath + "gsLineListBytes.dat";
 File file = new File(lineListBytes);
 int bArrSize = (int) file.length();
 //System.out.println(" bArrSize =" +  bArrSize);
// int bArrSize = 484323;
 byte[] barray = new byte[bArrSize];
 barray = ByteFileRead.readFileBytes(lineListBytes, bArrSize);

//Path path = Paths.get(dataPath + lineListFile); //java.nio.file not available in Java 6
Charset charset = Charset.forName("US-ASCII");
String pattern = "0.0000000000000000";
//String pattern = "###.####";
DecimalFormat myFormatter = new DecimalFormat(pattern);

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

//System.out.println("decoded " + decoded);
 String[] arrayLineString = decoded.split("%%"); 

//Number of lines MUST be the ONLY entry on the first line 

        int numLineList = arrayLineString.length;
        //System.out.println("arrayLineString[0] " + arrayLineString[0]);
        //System.out.println("numLineList " + numLineList); 
//        for (int i = 0; i < 5; i++){
//           System.out.println(arrayLineString[i]);
//        }


//Okay, here we go:
        //System.out.println("numLineList " + numLineList);
        double[] list2Lam0 = new double[numLineList];  // nm
        String[] list2Element = new String[numLineList]; //element
        String[] list2StageRoman = new String[numLineList]; //ion stage
        int[] list2Stage = new int[numLineList]; //ion stage
        double[] list2Mass = new double[numLineList]; // amu
        double[] list2LogGammaCol = new double[numLineList];
        //abundance in logarithmic A12 sysytem
        double[] list2A12 = new double[numLineList];
        //Einstein coefficient for spontaneous de-exciation:
        double[] list2LogAij = new double[numLineList];
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
        //System.out.println("iLine " + iLine + " arrayLineString[iLine] " + arrayLineString[iLine]);
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
        //System.out.println("iLine " + iLine + " thisRecord[2] " + thisRecord[2]);    
        myString = thisRecord[2].trim(); 
        //System.out.println("myString " + myString);
        list2Lam0[iLine] = Double.parseDouble(myString);
        myString = thisRecord[3].trim();
        list2LogAij[iLine] = Double.parseDouble(myString);
        //list2Aij[iLine] = Math.pow(10.0, thisLogAij);
        myString = thisRecord[4].trim();
        list2Logf[iLine] = Double.parseDouble(myString);
        //list2Logf[iLine] = Math.log10(thisF); 
        myString = thisRecord[5].trim();
        list2GwL[iLine] = Double.parseDouble(myString);
        myString = thisRecord[6].trim();
        list2ChiL[iLine] = Double.parseDouble(myString);
/* Currently not used
        myString = thisRecord[7].trim();
        list2ChiU = Double.parseDouble(myString);
        myString = thisRecord[8].trim();
        list2Jl = Double.parseDouble(myString);
        myString = thisRecord[9].trim();
        list2Ju = Double.parseDouble(myString);
*/
           
    //System.out.println("iLine " + iLine + " list2Element[iLine] " + list2Element[iLine] + " list2StageRoman " + list2StageRoman[iLine] + " list2Lam0[iLine] " + list2Lam0[iLine] + " list2Logf[iLine] " + list2Logf[iLine] + " list2GwL[iLine] " + list2GwL[iLine] + " list2ChiL[iLine] " + list2ChiL[iLine]);   
        
           //Get the chemical element symbol - we don't know if it's one or two characters
           //System.out.println("i " + i + " array_ptr " + array_ptr + " arrayLineString[array_ptr] " + arrayLineString[array_ptr]);
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
            //System.out.println("list2Stage[list2_ptr] " + list2Stage[list2_ptr]);
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

     //We're going to have to fake the ground state statistical weight for now - sorry:
           //list2Gw1[list2_ptr] = 1.0;
           //list2Gw2[list2_ptr] = 1.0; 
           //list2Gw3[list2_ptr] = 1.0;
           //list2Gw4[list2_ptr] = 1.0; 
           list2LogGammaCol[list2_ptr] = logGammaCol; 

     // The base solar abundance for this species:
     // Java has not intrinsic method for finding a vlaue in an array:
          //System.out.println("list2Element[list2_ptr]" + list2Element[list2_ptr]+"!");
          int iAbnd = 0; //initialization
          for (int jj = 0; jj < nelemAbnd; jj++){
             //System.out.println("jj " + jj + " cname[jj]" + cname[jj]+"!");
             if (list2Element[list2_ptr].equals(cname[jj])){
                 break;   //we found it
                 }
             iAbnd++;
            }
           if (iAbnd == nelemAbnd){
              //the element is not in out set - flag this with a special abundance value
              list2A12[list2_ptr] = 0.0;
                 } else {
              list2A12[list2_ptr] = eheu[iAbnd]; 
                 } 
 
    //We've gotten everything we need from the NIST line list:
           list2_ptr++;
        
       } //iLine loop 

  int numLines2 = list2_ptr;


//Okay - what kind of mess did we make...
 //System.out.println("We processed " +  numLines2 + " lines");
 //System.out.println("list2Element  list2Stage  list2Lam0  list2Logf  list2GwL  list2ChiL  list2ChiI1  list2ChiI2  list2Mass  list2A12");

//for (int i = 0; i < numLines2; i++){
// System.out.println(" " + list2Element[i] + " " + list2Stage[i] + " " + list2Lam0[i] + " " + list2Logf[i] + " " + list2GwL[i] + 
//    " " + list2ChiL[i] + " " + list2ChiI1[i] + " " + list2ChiI2[i] + " " + list2Mass[i] + " " + list2A12[i]);
//} 

// END FILE I/O SECTION


//System.out.println(" *********************************************** ");
//System.out.println("  ");
//System.out.println("  ");
//System.out.println("BEFORE TRIAGE");
//System.out.println("  ");
//System.out.println("  ");
//System.out.println(" *********************************************** ");
//
//Triage: For each line: Voigt, Gaussian, or negelct??
//
//
        int gaussLineCntr = 0; //initialize accumulator
        //int sedLineCntr = 0; //initialize accumulator
        //No! boolean[] ifThisLine = new boolean[numLines2]; //initialize line strength flag
        int gaussLine_ptr[] = new int[numLines2]; //array of pointers to lines that make the cut in the 
        //int sedLine_ptr[] = new int[numLines2]; //array of pointers to lines that make the cut in the 
                                                  // master line list  
        //System.out.println("sedThresh " + sedThresh + " lineThresh " + lineThresh 
        //   + " lamUV " + lamUV + " lamIR " + lamIR + " lambdaStart " + lambdaStart + " lambdaStop " + lambdaStop);
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
                thisUwV = PartitionFn.getPartFn(species); //base 10 log_10 U
                 break;   //we found it
             }
             iAbnd++;
          } //jj loop
          //System.out.println("iAbnd " + iAbnd); // + " cname[iAbnd] " + cname[iAbnd]);
           //System.out.println("list2Element[iLine] " + list2Element[iLine] + " cname[iAbnd] " + cname[iAbnd] + " list2Stage[iLine] " + list2Stage[iLine]);
           double[][] list2LogNums = new double[numStages+2][numDeps];
            for (int iTau = 0; iTau < numDeps; iTau++){
               list2LogNums[0][iTau] = masterStagePops[iAbnd][0][iTau];
               list2LogNums[1][iTau] = masterStagePops[iAbnd][1][iTau];
               //System.out.println("list2LogNums[1][iTau] " + list2LogNums[1][iTau] + " list2LogNums2[1][iTau] " + list2LogNums2[1][iTau]);
               list2LogNums[4][iTau] = masterStagePops[iAbnd][2][iTau];
               list2LogNums[5][iTau] = masterStagePops[iAbnd][3][iTau];
               list2LogNums[6][iTau] = masterStagePops[iAbnd][4][iTau];
            }
            //double[] numHelp = LevelPopsServer.levelPops(list2Lam0[iLine], list2LogNums[list2Stage[iLine]], list2ChiL[iLine], thisUwV, 
             //       list2GwL[iLine], numDeps, tauRos, temp);
          // System.out.println("iLine " + iLine + " list2Lam0nm " +  list2Lam0[iLine] + " list2ChiL " + list2ChiL[iLine] +
// " thisUwV[] " + thisUwV[0] + " " + thisUwV[1] + " list2GwL " + list2GwL[iLine]);
            double[] numHelp = LevelPopsServer.levelPops(list2Lam0[iLine], list2LogNums[logNums_ptr], list2ChiL[iLine], thisUwV, 
                    list2GwL[iLine], numDeps, tauRos, temp);
            for (int iTau = 0; iTau < numDeps; iTau++){
               list2LogNums[2][iTau] = numHelp[iTau];
               //System.out.println("list2LogNums[2][iTau] " + list2LogNums[2][iTau] + " list2LogNums2[2][iTau] " + list2LogNums2[2][iTau]);
               list2LogNums[3][iTau] = numHelp[iTau] / 2.0; //fake for testing with gS3 line treatment
               //System.out.println("iTau " + iTau + " list2LogNums[2][iTau] " + list2LogNums[2][iTau] + " list2LogNumsOld[2][iTau] " + list2LogNumsOld[2][iTau]);
            } 

        //linePoints: Row 0 in cm (will need to be in nm for Plack.planck), Row 1 in Doppler widths
        //For now - initial strength check with delta fn profiles at line centre for triage:
        int listNumPointsDelta = 1;
           double[][] listLinePointsDelta = LineGrid.lineGridDelta(list2Lam0[iLine], list2Mass[iLine], xiT, numDeps, teff);
           double[][] listLineProfDelta = LineProf.delta(listLinePointsDelta, list2Lam0[iLine], numDeps, tauRos, list2Mass[iLine], xiT, teff); 
           double[][] listLogKappaLDelta = LineKappa.lineKap(list2Lam0[iLine], list2LogNums, list2Logf[iLine], listLinePointsDelta, listLineProfDelta,
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
			    ||  ( list2Stage[iLine] == 2) || (list2Stage[iLine] == 3) ){
				if ( (list2Lam0[iLine] > lambdaStart) && (list2Lam0[iLine] < lambdaStop) ){ 
			      //No! ifThisLine[iLine] = true;
			      gaussLine_ptr[gaussLineCntr] = iLine;
			      gaussLineCntr++;
                              if (isFirstLine == true){
                                 firstLine = iLine;
                                 isFirstLine = false;
                              } 
//	      System.out.println("specSyn passed, iLine= " + iLine + " gaussLineCntr " + gaussLineCntr 
//		 + " list2Lam0[iLine] " + list2Lam0[iLine] 
//		 + " list2Element[iLine] " + list2Element[iLine]
//		 + " list2Stage[iLine] " + list2Stage[iLine]); 
                            }
                      } 
                }
//
       } //iLine loop

//We need to have at least one line in rgion:
       boolean areNoLines = false; //initialization
       if (gaussLineCntr == 0){
            gaussLineCntr = 1;
            gaussLine_ptr[0] = firstLine;
            areNoLines = true;
             }

       int numGaussLines = gaussLineCntr;
       //int numSedLines = sedLineCntr; //Gauss lines double-counted
       int numTotalLines = numGaussLines; // + numSedLines;
//       System.out.println("We found " + numGaussLines + " lines strong enough for Gaussian and " + numSedLines + " strong enough for blanketing SED");
//System.out.println(" *********************************************** ");
//System.out.println("  ");
//System.out.println("  ");
//System.out.println("AFTER TRIAGE");
//System.out.println("  ");
//System.out.println("  ");
//System.out.println(" *********************************************** ");

 //

        //Notes
        //if Hydrogen or Helium, kappaScale should be unity for these purposes:
        //double kappaScaleList = 1.0; //initialization                   
        //

        int listNumCore = 3;  //half-core //default initialization
        //int sedNumCore = 3;  //half-core //default initialization
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
        int numMaster = numLams + (numGaussLines * listNumPoints); // + (numSedLines * sedNumPoints); //total size (number of wavelengths) of master lambda & total kappa arrays 
        double[] masterLams = new double[numMaster];
//Line blanketed opacity array:
        double[][] logMasterKaps = new double[numMaster][numDeps];
// Construct a continuum opacity array with same structure as logMasterKaps
        //double[][] logContKaps = new double[numLams][numDeps];  
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
// This holds 2-element temperature-dependent base 10 logarithmic parition fn:
        thisUwV[0] = 0.0; //default initialization
        thisUwV[1] = 0.0;

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
            }
            //double[] numHelp = LevelPopsServer.levelPops(list2Lam0[gaussLine_ptr[iLine]], list2LogNums[list2Stage[gaussLine_ptr[iLine]]], list2ChiL[gaussLine_ptr[iLine]], thisUwV,
             //       list2GwL[gaussLine_ptr[iLine]], numDeps, tauRos, temp);
//System.out.println("iLine " + iLine + " list2Lam0 " + list2Lam0[gaussLine_ptr[iLine]] + " list2ChiL" + list2ChiL[gaussLine_ptr[iLine]] +
// " thisUwV[] " + thisUwV[0] + " " + thisUwV[1] + " list2GwL " + list2GwL[gaussLine_ptr[iLine]]);
            double[] numHelp = LevelPopsServer.levelPops(list2Lam0[gaussLine_ptr[iLine]], list2LogNums[logNums_ptr], list2ChiL[gaussLine_ptr[iLine]], thisUwV,
                    list2GwL[gaussLine_ptr[iLine]], numDeps, tauRos, temp);
            for (int iTau = 0; iTau < numDeps; iTau++){
               list2LogNums[2][iTau] = numHelp[iTau];
               list2LogNums[3][iTau] = -19.0; //upper E-level - not used - fake for testing with gS3 line treatment
              // if (iTau == 36){
               //  System.out.println("iLine " + iLine + " iTau " + iTau + " listLogNums[2] " + logE*list2LogNums[2][iTau]);
              // }
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
            double[][] listLineProf = LineProf.voigt(listLinePoints, list2Lam0[gaussLine_ptr[iLine]], list2LogAij[gaussLine_ptr[iLine]],
                    list2LogGammaCol[gaussLine_ptr[iLine]],
                    numDeps, teff, tauRos, temp, pGas, tempSun, pGasSun);
            double[][] listLogKappaL = LineKappa.lineKap(list2Lam0[gaussLine_ptr[iLine]], list2LogNums, list2Logf[gaussLine_ptr[iLine]], listLinePoints, listLineProf,
                    numDeps, zScaleList, tauRos, temp, rho);
            double[] listLineLambdas = new double[listNumPoints];
            for (int il = 0; il < listNumPoints; il++) {
                // // lineProf[gaussLine_ptr[iLine]][*] is DeltaLambda from line centre in cm
                listLineLambdas[il] = listLinePoints[0][il] + list2Lam0[gaussLine_ptr[iLine]];
            }

            for (int ll = 0; ll < listNumPoints; ll++){
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


//Line blanketed monochromatic optical depth array:
        //System.out.println("tauLambda call 1:");
        double logTauMaster[][] = LineTau2.tauLambda(numDeps, numMaster, logMasterKaps,
                logKappa, tauRos, numLams, lambdaScale, masterLams);

//Lin blanketed formal Rad Trans solution:
        //Evaluate formal solution of rad trans eq at each lambda throughout line profile
        // Initial set to put lambda and tau arrays into form that formalsoln expects
        double[][] masterIntens = new double[numMaster][numThetas];
        double[] masterIntensLam = new double[numThetas];

        double[][] masterFlux = new double[2][numMaster];
        double[] masterFluxLam = new double[2];

        double[][] thisTau = new double[2][numDeps];

        lineMode = false;  //no scattering for overall SED

        for (int il = 0; il < numMaster; il++) {

//                        }
            for (int id = 0; id < numDeps; id++) {
                thisTau[1][id] = logTauMaster[il][id];
                thisTau[0][id] = Math.exp(logTauMaster[il][id]);
            } // id loop

            masterIntensLam = FormalSoln.formalSoln(numDeps,
                    cosTheta, masterLams[il], thisTau, temp, lineMode);

            masterFluxLam = Flux.flux(masterIntensLam, cosTheta);

            for (int it = 0; it < numThetas; it++) {
                masterIntens[il][it] = masterIntensLam[it];
            } //it loop - thetas

            masterFlux[0][il] = masterFluxLam[0];
            masterFlux[1][il] = masterFluxLam[1];

            //// Teff test - Also needed for convection module!:
            if (il > 1) {
                lambda2 = masterLams[il]; // * 1.0E-7;  // convert nm to cm
                lambda1 = masterLams[il - 1]; // * 1.0E-7;  // convert nm to cm
                fluxSurfBol = fluxSurfBol
                        + masterFluxLam[0] * (lambda2 - lambda1);
            }
        } //il loop

        logFluxSurfBol = Math.log(fluxSurfBol);
        double logTeffFlux = (logFluxSurfBol - Useful.logSigma()) / 4.0;
        double teffFlux = Math.exp(logTeffFlux);

//
////
//Continuum monochromatic optical depth array:
        double logTauCont[][] = LineTau2.tauLambda(numDeps, numLams, logKappa,
                logKappa, tauRos, numLams, lambdaScale, masterLams);

        //Evaluate formal solution of rad trans eq at each lambda 
        // Initial set to put lambda and tau arrays into form that formalsoln expects
        double[][] contIntens = new double[numLams][numThetas];
        double[] contIntensLam = new double[numThetas];

        double[][] contFlux = new double[2][numLams];
        double[] contFluxLam = new double[2];

        lineMode = false;  //no scattering for overall SED

        for (int il = 0; il < numLams; il++) {

            for (int id = 0; id < numDeps; id++) {
                thisTau[1][id] = logTauCont[il][id];
                thisTau[0][id] = Math.exp(logTauCont[il][id]);
            } // id loop

            contIntensLam = FormalSoln.formalSoln(numDeps,
                    cosTheta, lambdaScale[il], thisTau, temp, lineMode);

            contFluxLam = Flux.flux(contIntensLam, cosTheta);

            for (int it = 0; it < numThetas; it++) {
                contIntens[il][it] = contIntensLam[it];
            } //it loop - thetas

            contFlux[0][il] = contFluxLam[0];
            contFlux[1][il] = contFluxLam[1];

            //// Teff test - Also needed for convection module!:
            if (il > 1) {
                lambda2 = lambdaScale[il]; // * 1.0E-7;  // convert nm to cm
                lambda1 = lambdaScale[il - 1]; // * 1.0E-7;  // convert nm to cm
                fluxSurfBol = fluxSurfBol
                        + contFluxLam[0] * (lambda2 - lambda1);
            }
        } //il loop

//Extract linear monochromatic continuum limb darlening coefficients (LDCs) ("epsilon"s):
    double[] ldc = new double[numLams];
    ldc = LDC.ldc(numLams, lambdaScale, numThetas, cosTheta, contIntens);

        logFluxSurfBol = Math.log(fluxSurfBol);
        logTeffFlux = (logFluxSurfBol - Useful.logSigma()) / 4.0;
        teffFlux = Math.exp(logTeffFlux);
        //String pattern = "0000.00";
        ////String pattern = "#####.##";
        //DecimalFormat myFormatter = new DecimalFormat(pattern);

        //Do this photometry on client side:
        ////disk integrated colors:
        //double colors[] = Photometry.UBVRI(masterLams, masterFlux, numDeps, tauRos, temp);
        ////band-integrated  specific intensities :
        //double iColors[][] = Photometry.iColors(lambdaScale, intens, numDeps, numThetas, numLams, tauRos, temp);
//

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
     
   int iStart = ToolBox.lamPoint(numMaster, masterLams, lambdaStart);
   int iStop = ToolBox.lamPoint(numMaster, masterLams, lambdaStop);
   int numSpecSyn = iStop - iStart;
 
  int numSpecies = nelemAbnd * numStages; 
     //Block 1: Array dimensions
     //keys:
        //System.out.println("numDeps,numMaster,numThetas,numSpecSyn,numGaussLines,numLams,nelemAbnd,numSpecies"); 
        System.out.println("numDeps,numMaster,numThetas,numGaussLines,numLams,nelemAbnd,numSpecies"); 
     //values:
        //System.out.format("%03d,%07d,%03d,%07d,%06d,%07d,%05d,%04d%n", 
         //   numDeps, numMaster, numThetas, numSpecSyn, numGaussLines, numLams, nelemAbnd, numSpecies);
        System.out.format("%03d,%07d,%03d,%06d,%07d,%05d,%04d%n", 
            numDeps, numMaster, numThetas, numGaussLines, numLams, nelemAbnd, numSpecies);

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
        for (int i = 0; i < numMaster; i++){
 //Do quality control here:
           if ( (masterFlux[1][i] < logTiny) || (masterFlux[0][i] < tiny) ){
              masterFlux[1][i] = logTiny;
              masterFlux[0][i] = tiny;
               }
           System.out.format("%13.8f,%13.8f%n", Math.log(masterLams[i]), masterFlux[1][i]); 
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
            for (int j = 0; j < numMaster; j++){
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
