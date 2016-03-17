/*
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
        String logKappaStr = args[2];
        //String logKappaStr = "0.0"; //test
        double logKappaScale = (Double.valueOf(logKappaStr)).doubleValue();

        //Argument 3: Stellar mass, M, in solar masses
        String massStarStr = args[3];
        //String massStarStr = "1.0"; //test
        double massStar = (Double.valueOf(massStarStr)).doubleValue();

        // Sanity check:
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
        if (logKappaScale < -3.0) {
            logKappaScale = -3.0;
            logKappaStr = "-3.0";
        }
        if (logKappaScale > 1.0) {
            logKappaScale = 1.0;
            logKappaStr = "1.0";
        }
        if (massStar < 0.1) {
            massStar = 0.1;
            massStarStr = "0.1";
        }
        if (massStar > 10.0) {
            massStar = 10.0;
            massStarStr = "10.0";
        }

        double grav = Math.pow(10.0, logg);
        double kappaScale = Math.pow(10.0, logKappaScale);

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

        // Argument 11: stopping wavelength for spectrum synthesis 
           String logGammaColStr = args[10];
           //String xitStr = "1.0"; //test
           double logGammaCol = (Double.valueOf(logGammaColStr)).doubleValue();

        if (logGammaCol < 0.0) {
            logGammaCol = 0.0;
            logGammaColStr = "0.0";
        }
        if (logGammaCol > 1.0) {
            logGammaCol = 1.0;
            logGammaColStr = "1.0";
        }


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

// Solar parameters:
        double teffSun = 5778.0;
        double loggSun = 4.44;
        double gravSun = Math.pow(10.0, loggSun);
        double logKappaScaleSun = 0.0;
        double kappaScaleSun = Math.exp(logKappaScaleSun);
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
        double massZ = massZSun * kappaScale; //approximation

        double logNH = 17.0;

        //Vega parameters (of Phoenix model- Teff not quite right!)
        double teffVega = 9950.0;
        double loggVega = 3.95;
        double gravVega = Math.pow(10.0, loggVega);
        double kappaScaleVega = 0.333;

        //double sedThresh = 4.0; //line to continuum extinction threshold for 
                                 // inclusion as low resolution line in overall SED
        ////Output files:
        //String outfile = "gray_structure."
        //        + teffStr + "-" + loggStr + "-" + logKappaStr + ".out";
        //String specFile = "gray_spectrum."
        //        + teffStr + "-" + loggStr + "-" + logKappaStr + ".out";
        //String lineFile = "voigt_line."
        //        + teffStr + "-" + loggStr + "-" + logKappaStr + "-" + xitStr + ".out";

        double logE = Math.log10(Math.E); // for debug output
        double logE10 = Math.log(10.0); //natural log of 10

        //log_10 Rosseland optical depth scale  
        double tauRos[][] = TauScale.tauScale(numDeps, log10MinDepth, log10MaxDepth);

        //Now do the same for the Sun, for reference:
        double[][] tempSun = ScaleSolar.phxSunTemp(teffSun, numDeps, tauRos);
        //Now do the same for the Sun, for reference:
        double[][] pGasSun = ScaleSolar.phxSunPGas(gravSun, numDeps, tauRos);
        double[][] NeSun = ScaleSolar.phxSunNe(gravSun, numDeps, tauRos, tempSun, kappaScaleSun);
        double[][] kappaSun = ScaleSolar.phxSunKappa(numDeps, tauRos, kappaScaleSun);
        double[] mmwSun = State.mmwFn(numDeps, tempSun, kappaScaleSun);
        double[][] rhoSun = State.massDensity(numDeps, tempSun, pGasSun, mmwSun, kappaScaleSun);
        double pressSun[][] = Hydrostat.hydrostatic(numDeps, gravSun, tauRos, kappaSun, tempSun);
        //Now do the same for Vega, for reference:
        double[][] tempVega = ScaleVega.phxVegaTemp(teffVega, numDeps, tauRos);
        //Now do the same for the Sun, for reference:
        double[][] pGasVega = ScaleVega.phxVegaPGas(gravVega, numDeps, tauRos);
        double[][] NeVega = ScaleVega.phxVegaNe(gravVega, numDeps, tauRos, tempVega, kappaScaleVega);
        double[][] kappaVega = ScaleVega.phxVegaKappa(numDeps, tauRos, kappaScaleVega);
        double[] mmwVega = State.mmwFn(numDeps, tempVega, kappaScaleVega);
        double[][] rhoVega = State.massDensity(numDeps, tempVega, pGasVega, mmwVega, kappaScaleVega);
        //Now do the same for the Sun, for reference:
        double pressVega[][] = Hydrostat.hydrostatic(numDeps, gravVega, tauRos, kappaVega, tempVega);
        //
        // BEGIN Initial guess for Sun section:
        //
        //Rescaled  kinetic temeprature structure: 
        double F0Vtemp = 7300.0;  // Teff of F0 V star (K)                           
        double[][] temp = new double[2][numDeps];
        if (teff < F0Vtemp) {
            //We're a cool star! - rescale from Sun!
            temp = ScaleSolar.phxSunTemp(teff, numDeps, tauRos);
        } else if (teff >= F0Vtemp) {
            //We're a HOT star! - rescale from Vega
            temp = ScaleVega.phxVegaTemp(teff, numDeps, tauRos);
        }
        //Scaled from Phoenix solar model:
        double[][] guessPGas = new double[2][numDeps];
        double[][] Ne = new double[2][numDeps];
        double[][] guessKappa = new double[2][numDeps];
        if (teff < F0Vtemp) {
            //We're a cool star - rescale from Sun!
            guessPGas = ScaleSolar.phxSunPGas(grav, numDeps, tauRos);
            Ne = ScaleSolar.phxSunNe(grav, numDeps, tauRos, temp, kappaScale);
            guessKappa = ScaleSolar.phxSunKappa(numDeps, tauRos, kappaScale);
        } else if (teff >= F0Vtemp) {
            //We're a HOT star!! - rescale from Vega
            guessPGas = ScaleVega.phxVegaPGas(grav, numDeps, tauRos);
            Ne = ScaleVega.phxVegaNe(grav, numDeps, tauRos, temp, kappaScale);
            guessKappa = ScaleVega.phxVegaKappa(numDeps, tauRos, kappaScale);
        }
        //

        // END initial guess for Sun section
        //
        // *********************
        //
        // mean molecular weight and Ne for Star & Sun
        double[] mmw = State.mmwFn(numDeps, temp, kappaScale);

        double[][] guessRho = State.massDensity(numDeps, temp, guessPGas, mmw, kappaScale);

        // Create kappa structure here: Initialize solar kappa_Ross structure and
        // scale it by logg, radius, and kappaScale:

        //Get H I n=2 & n=3 number densities for Balmer and Pashen continuum  for kappa calculation
        // Paschen:
        boolean ionizedHI = false;
        double chiI1H = 13.6; //eV
        double chiI2H = 1.0e6;  //eV //H has no third ionization stage!
        double gw1H = 2.0;
        double gw2H = 1.0;  // umm... doesn't exist - no "HIII"
        // n=3 level - Paschen jump
        double lamJump3 = 820.4 * 1.0e-7; //Paschen jump - cm
        double chiLH3 = 12.1; //eV
        double gwLH3 = 2 * 3 * 3; // 2n^2
        double logNumsH3[][];
        // n=2 level - Balmer jump
        double lamJump2 = 364.0 * 1.0e-7; //Paschen jump - cm
        double chiLH2 = 10.2; //eV
        double gwLH2 = 2 * 2 * 2; // 2n^2   
        double logNumsH2[][];
        int mode;

        mode = 1;  //call kappas with knowledge of rho

        logNumsH3 = LevelPops.levelPops(lamJump3, logNH, Ne, ionizedHI, chiI1H, chiI2H, chiLH3, gw1H, gw2H, gwLH3,
                numDeps, kappaScale, tauRos, temp, guessRho);
        logNumsH2 = LevelPops.levelPops(lamJump2, logNH, Ne, ionizedHI, chiI1H, chiI2H, chiLH2, gw1H, gw2H, gwLH2,
                numDeps, kappaScale, tauRos, temp, guessRho);
        double[][] kappa = new double[2][numDeps];
        if (teff < F0Vtemp) {
            kappa = Kappas.kappas(mode, numDeps, guessRho, rhoSun, kappaSun, kappaScale, logg, loggSun,
                    teff, teffSun, radius, massX, massZ, tauRos, temp, tempSun, logNumsH3, logNumsH2);
        } else if (teff >= F0Vtemp) {
            //System.out.println("Call 1: Hot branch taken: ");
            kappa = Kappas.kappas(mode, numDeps, guessRho, rhoVega, kappaVega, kappaScale, logg, loggSun,
                    teff, teffSun, radius, massX, massZ, tauRos, temp, tempVega, logNumsH3, logNumsH2);
        }

        double press[][] = Hydrostat.hydrostatic(numDeps, grav, tauRos, kappa, temp);

        // Then solve eos for the rho scale - need to pick a mean molecular weight, mu
        double[][] rho = State.massDensity(numDeps, temp, press, mmw, kappaScale);


        // Limb darkening:
        // Establish wavelength grid:

        //compute kappas again with in situ densities thsi time:
        mode = 1;  //call kappas ** with ** knowledge of rho
        logNumsH3 = LevelPops.levelPops(lamJump3, logNH, Ne, ionizedHI, chiI1H, chiI2H, chiLH3, gw1H, gw2H, gwLH3,
                 numDeps, kappaScale, tauRos, temp, rho);
        logNumsH2 = LevelPops.levelPops(lamJump2, logNH, Ne, ionizedHI, chiI1H, chiI2H, chiLH2, gw1H, gw2H, gwLH2,
                numDeps, kappaScale, tauRos, temp, rho);
        if (teff < F0Vtemp) {
             kappa = Kappas.kappas(mode, numDeps, rho, rhoSun, kappaSun, kappaScale, logg, loggSun,
                    teff, teffSun, radius, massX, massZ, tauRos, temp, tempSun, logNumsH3, logNumsH2);
         } else if (teff >= F0Vtemp) {
            //System.out.println("Call 2: Hot branch taken: ");
            kappa = Kappas.kappas(mode, numDeps, rho, rhoVega, kappaVega, kappaScale, logg, loggSun,
                    teff, teffSun, radius, massX, massZ, tauRos, temp, tempVega, logNumsH3, logNumsH2);
         }
        // Then construct geometric depth scale from tau, kappa and rho
        double depths[] = DepthScale.depthScale(numDeps, tauRos, kappa, rho);

        double newTemp[][] = new double[2][numDeps];
        //int numTCorr = 10;  //test
        int numTCorr = 0;
        for (int i = 0; i < numTCorr; i++) {
            //newTemp = TCorr.tCorr(numDeps, tauRos, temp);
            newTemp = MulGrayTCorr.mgTCorr(numDeps, teff, tauRos, temp, rho, kappa);
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
         convTemp = Convec.convec(numDeps, tauRos, depths, temp, press, rho, kappa, kappaSun, kappaScale, teff, logg);

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
            press = Hydrostat.hydrostatic(numDeps, grav, tauRos, kappa, temp);
            rho = State.massDensity(numDeps, temp, press, mmw, kappaScale);
            mode = 1;  //call kappas ** with ** knowledge of rho
            logNumsH3 = LevelPops.levelPops(lamJump3, logNH, Ne, ionizedHI, chiI1H, chiI2H, chiLH3, gw1H, gw2H, gwLH3,
                    numDeps, kappaScale, tauRos, temp, rho);
            logNumsH2 = LevelPops.levelPops(lamJump2, logNH, Ne, ionizedHI, chiI1H, chiI2H, chiLH2, gw1H, gw2H, gwLH2,
                    numDeps, kappaScale, tauRos, temp, rho);
            if (teff < F0Vtemp) {
                kappa = Kappas.kappas(mode, numDeps, rho, rhoSun, kappaSun, kappaScale, logg, loggSun,
                        teff, teffSun, radius, massX, massZ, tauRos, temp, tempSun, logNumsH3, logNumsH2);
            } else if (teff >= F0Vtemp) {
                kappa = Kappas.kappas(mode, numDeps, rho, rhoVega, kappaVega, kappaScale, logg, loggSun,
                        teff, teffSun, radius, massX, massZ, tauRos, temp, tempVega, logNumsH3, logNumsH2);
            }
        }

        depths = DepthScale.depthScale(numDeps, tauRos, kappa, rho);

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
        int maxNumBins = 11;
        double[][] grayLevelsEpsilons = MulGrayTCorr.grayLevEps(maxNumBins, minLambda, maxLambda, teff, isCool);
        //Find actual number of multi-gray bins:
        int numBins = 0; //initialization
        for (int i = 0; i < maxNumBins; i++) {
            if (grayLevelsEpsilons[0][i] < maxLambda) {
                numBins++;
            }
        }

//
//
//
//Abundance table adapted from PHOENIX V. 15 input bash file
// Grevesse Asplund et al 2010
//Solar abundances:
// c='abundances, Anders & Grevesse',

  int nelemAbnd = 40;
  int[] nome = new int[nelemAbnd];
  double[] eheu = new double[nelemAbnd];
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

//  int nelemAbnd = 1;
//  int[] nome = new int[nelemAbnd];
//  double[] eheu = new double[nelemAbnd];
//  String[] cname = new String[nelemAbnd];
//  nome[0] = 2000; 
//  eheu[0] = 6.36;
//  cname[0] = "Ca";
// Populate the ionization stages of all the species:
  int numStages = 4;
  double[][][] masterStagePops = new double[nelemAbnd][numStages][numDeps];
//stuff to save ion stage pops at tau=1:
  double[][] tauOneStagePops = new double[nelemAbnd][numStages];
  double unity = 1.0;
  int iTauOne = ToolBox.tauPoint(numDeps, tauRos, unity);
//
        double kappaScaleList = 1.0; //initialization   
       double fakeGw1 = 1.0; //for now                
       double fakeGw2 = 1.0; //for now                
       double fakeGw3 = 1.0; //for now                
       double fakeGw4 = 1.0; //for now                
        //Ground state ionization E - Stage I (eV) 
        double thisChiI1;
        //Ground state ionization E - Stage II (eV)
        double thisChiI2;
        double thisChiI3;
        double thisChiI4;
        String species;
   for (int iElem = 0; iElem < nelemAbnd; iElem++){
       species = cname[iElem] + "I";
       thisChiI1 = IonizationEnergy.getIonE(species); 
       species = cname[iElem] + "II";
       thisChiI2 = IonizationEnergy.getIonE(species);
       species = cname[iElem] + "III";
       thisChiI3 = IonizationEnergy.getIonE(species);
       species = cname[iElem] + "IV";
       thisChiI4 = IonizationEnergy.getIonE(species);
       double logN = (eheu[iElem] - 12.0) + logNH;
            //if H or He, make sure kappaScale is unity:
            if ((cname[iElem].equals("H"))
                    || (cname[iElem].equals("He"))) {
                kappaScaleList = 1.0;
                fakeGw1 = 2.0;  //fix for Balmer lines
            } else {
                kappaScaleList = kappaScale;
                fakeGw1 = 1.0;  //fix for Balmer lines
            }
       double[][] logNums = LevelPopsServer.stagePops(logN, Ne, thisChiI1,
             thisChiI2, thisChiI3, thisChiI4, fakeGw1, fakeGw2, fakeGw3, fakeGw4, 
             numDeps, kappaScaleList, tauRos, temp, rho);
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
        double[] list2Gw1 = new double[numLineList];
        //Unitless statisital weight, Ground state - Stage II
        double[] list2Gw2 = new double[numLineList];
        //Unitless statisital weight, Ground state - Stage III
        double[] list2Gw3 = new double[numLineList];
        //Unitless statisital weight, Ground state - Stage IV
        double[] list2Gw4 = new double[numLineList];
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
            switch(list2StageRoman[list2_ptr]){
                case "I": 
                     list2Stage[list2_ptr] = 0;
                     break;
                case "II": 
                     list2Stage[list2_ptr] = 1;
                     break;
                case "III": 
                     list2Stage[list2_ptr] = 2;
                     break;
                case "IV": 
                     list2Stage[list2_ptr] = 3;
                     break;
                default:
                     list2Stage[list2_ptr] = 0;
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
           list2Gw1[list2_ptr] = 1.0;
           list2Gw2[list2_ptr] = 1.0; 
           list2Gw3[list2_ptr] = 1.0;
           list2Gw4[list2_ptr] = 1.0; 
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
        for (int iLine = 0; iLine < numLines2; iLine++) {

            //No! ifThisLine[iLine] = false;
            //if H or He, make sure kappaScale is unity:
            if ((list2Element[iLine].equals("H"))
                    || (list2Element[iLine].equals("He"))) {
                kappaScaleList = 1.0;
                list2Gw1[iLine] = 2.0;  //fix for H lines
                if (list2Lam0[iLine] <= 6570.0){
                list2GwL[iLine] = 8.0;  //fix for Balmer lines
                    } else {
                list2GwL[iLine] = 18.0;  //fix for Paschen lines
                    }
            } else {
                kappaScaleList = kappaScale;
            }

          list2Lam0[iLine] = list2Lam0[iLine] * 1.0e-7;  // nm to cm
          int iAbnd = 0; //initialization
            //System.out.println("iLine " + iLine + " list2Element[iLine] " + list2Element[iLine]);
          for (int jj = 0; jj < nelemAbnd; jj++){
             //System.out.println("jj " + jj + " cname[jj]" + cname[jj]+"!");
             if (list2Element[iLine].equals(cname[jj])){
                 break;   //we found it
                 }
             iAbnd++;
          } //jj loop
          //System.out.println("iAbnd " + iAbnd); // + " cname[iAbnd] " + cname[iAbnd]);
           //System.out.println("list2Element[iLine] " + list2Element[iLine] + " cname[iAbnd] " + cname[iAbnd] + " list2Stage[iLine] " + list2Stage[iLine]);
           double[][] list2LogNums = new double[6][numDeps];
            for (int iTau = 0; iTau < numDeps; iTau++){
               list2LogNums[0][iTau] = masterStagePops[iAbnd][0][iTau];
               list2LogNums[1][iTau] = masterStagePops[iAbnd][1][iTau];
               //System.out.println("list2LogNums[1][iTau] " + list2LogNums[1][iTau] + " list2LogNums2[1][iTau] " + list2LogNums2[1][iTau]);
               list2LogNums[4][iTau] = masterStagePops[iAbnd][2][iTau];
               list2LogNums[5][iTau] = masterStagePops[iAbnd][3][iTau];
            }
            double[] numHelp = LevelPopsServer.levelPops(list2Lam0[iLine], list2LogNums[list2Stage[iLine]], list2ChiL[iLine], list2Gw1[iLine],
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
                    numDeps, kappaScaleList, tauRos, temp, rhoSun);
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
            if ( (logE*(listLogKappaLDelta[0][6] - kappa[1][6]) > lineThresh)  
              || (logE*(listLogKappaLDelta[0][18] - kappa[1][18]) > lineThresh)  
		      || (logE*(listLogKappaLDelta[0][30] - kappa[1][30]) > lineThresh) ){ 
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
        //CONTINUUM lambda scale (nm)
        double lambdaScale[] = LamGrid.lamgrid(numLams, lamSetup); //cm

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
        double[][] logContKaps = new double[numLams][numDeps];  
        //seed masterLams and logMasterKaps with continuum SED lambdas and kapaps:
        //This just initializes the first numLams of the numMaster elements
        //Also - put in multi-Gray opacities here:
        //Find which gray level bin the spectrum synthesis region starts in - assume that the first gray-level bin
        // is always at a shorter wavelength than the start of the synthesis region:
        int whichBin = 0;  //initialization
        for (int iB = 0; iB < numBins; iB++) {
            if (grayLevelsEpsilons[0][iB] >= lambdaScale[0]) {
                //System.out.println("grayLevelsEpsilons[0][iB] " + grayLevelsEpsilons[0][iB] + " lambdaScale[0] " + lambdaScale[0]);
                whichBin = iB;  //found it!
                break;
            }
        }

//Initialize monochromatic line blanketed opacity array
        //First wavelength definitely falls in first found bin:
        masterLams[0] = lambdaScale[0];
        for (int iD = 0; iD < numDeps; iD++) {
            logMasterKaps[0][iD] = kappa[1][iD]; // + Math.log(grayLevelsEpsilons[1][0]);
        }
        for (int iL = 1; iL < numLams; iL++) {
            masterLams[iL] = lambdaScale[iL];
            if ((lambdaScale[iL] >= grayLevelsEpsilons[0][whichBin + 1])
                    && (lambdaScale[iL - 1] < grayLevelsEpsilons[0][whichBin + 1])
                    && (whichBin < numBins - 1)) {
                whichBin++;
            }
            for (int iD = 0; iD < numDeps; iD++) {
                logMasterKaps[iL][iD] = kappa[1][iD]; // + Math.log(grayLevelsEpsilons[1][whichBin]);
            }
        }
        //initialize the rest with dummy values
        for (int iL = numLams; iL < numMaster; iL++) {
            masterLams[iL] = lambdaScale[numLams - 1];
            for (int iD = 0; iD < numDeps; iD++) {
                logMasterKaps[iL][iD] = kappa[1][iD];
            }
        }

//Initialize monochromatic continuum opacity array:
//This is a fake for now - it's just th gray opacity at every wavelength
        for (int iL = 0; iL < numLams; iL++) {
            for (int iD = 0; iD < numDeps; iD++) {
                logContKaps[iL][iD] = kappa[1][iD]; // + Math.log(grayLevelsEpsilons[1][whichBin]);
            }
        }

        //Stuff for the the Teff recovery test:
        double lambda1, lambda2, fluxSurfBol, logFluxSurfBol;
        fluxSurfBol = 0;

// Put in high res spectrum synthesis lines:
        for (int iLine = 0; iLine < numGaussLines; iLine++) {

            //if H or He, make sure kappaScale is unity:
            if ((list2Element[gaussLine_ptr[iLine]].equals("H"))
                    || (list2Element[gaussLine_ptr[iLine]].equals("He"))) {
                kappaScaleList = 1.0;
                list2Gw1[gaussLine_ptr[iLine]] = 2.0;  //fix for H lines
                if (list2Lam0[gaussLine_ptr[iLine]] <= 6570.0){
                    list2GwL[gaussLine_ptr[iLine]] = 8.0;  //fix for Balmer lines
                } else {
                    list2GwL[gaussLine_ptr[iLine]] = 18.0;  //fix for Paschen lines
                } 
            } else {
                kappaScaleList = kappaScale;
            }


//
          int iAbnd = 0; //initialization
          for (int jj = 0; jj < nelemAbnd; jj++){
             if (list2Element[gaussLine_ptr[iLine]].equals(cname[jj])){
                 break;   //we found it
                 }
             iAbnd++;
          } //jj loop
           double[][] list2LogNums = new double[5][numDeps];
            for (int iTau = 0; iTau < numDeps; iTau++){
               list2LogNums[0][iTau] = masterStagePops[iAbnd][0][iTau];
               list2LogNums[1][iTau] = masterStagePops[iAbnd][1][iTau];
               list2LogNums[4][iTau] = masterStagePops[iAbnd][2][iTau];
            }
            double[] numHelp = LevelPopsServer.levelPops(list2Lam0[gaussLine_ptr[iLine]], list2LogNums[list2Stage[gaussLine_ptr[iLine]]], list2ChiL[gaussLine_ptr[iLine]], list2Gw1[gaussLine_ptr[iLine]],
                    list2GwL[gaussLine_ptr[iLine]], numDeps, tauRos, temp);
            for (int iTau = 0; iTau < numDeps; iTau++){
               list2LogNums[2][iTau] = numHelp[iTau];
               list2LogNums[3][iTau] = numHelp[iTau] / 2.0; //fake for testing with gS3 line treatment
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
                    numDeps, teff, tauRos, temp, press, tempSun, pressSun);
            double[][] listLogKappaL = LineKappa.lineKap(list2Lam0[gaussLine_ptr[iLine]], list2LogNums, list2Logf[gaussLine_ptr[iLine]], listLinePoints, listLineProf,
                    numDeps, kappaScaleList, tauRos, temp, rhoSun);
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
        double logTauMaster[][] = LineTau2.tauLambda(numDeps, numMaster, logMasterKaps,
                kappa, tauRos);

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
        double logTauCont[][] = LineTau2.tauLambda(numDeps, numLams, logContKaps,
                kappa, tauRos);

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
        System.out.println("numDeps,numMaster,numThetas,numSpecSyn,numGaussLines,numLams,nelemAbnd,numSpecies"); 
     //values:
        System.out.format("%03d,%07d,%03d,%07d,%06d,%07d,%05d,%04d%n", 
            numDeps, numMaster, numThetas, numSpecSyn, numGaussLines, numLams, nelemAbnd, numSpecies);

    //Block 2: Atmosphere
     //keys:
        System.out.println("logTau,logTemp,logPGas,logPRad,logRho,logNe,logMmw,logKappa");
     //values:
        //Print out *NATURAL* logs - this is for communication between server & client: 
        for (int i = 0; i < numDeps; i++) {
            System.out.format("%13.8f,%13.8f,%13.8f,%13.8f,%13.8f,%13.8f,%13.8f,%13.8f%n", 
                    tauRos[1][i], temp[1][i], press[1][i], press[3][i], 
                    rho[1][i], Ne[1][i], Math.log(mmw[i]), kappa[1][i] );
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

//
   //Block 5: approximately rectified flux spectrum in spectrum synthesis region: 
   //keys:
        System.out.println("logWaveSS,logFluxSS");
        double[] specSynLams = new double[numSpecSyn];
        double[][] specSynFlux = new double[2][numSpecSyn];
   //values:
        int iCount = 0; 
        for (int i = iStart; i < iStop; i++){

   //This doesn't seem very useful (yet), but let's do it anyway for clarity:
           specSynLams[iCount] = masterLams[i]; 
 //Do quality control here:
           if ( (masterFlux[1][i] < logTiny) || (masterFlux[0][i] < tiny) ){
              masterFlux[1][i] = logTiny;
              masterFlux[0][i] = tiny;
               }
           //approximate rectification:
           specSynFlux[1][iCount] = masterFlux[1][i] - Planck.planck(teff, masterLams[i]);
           specSynFlux[0][iCount] = Math.exp(specSynFlux[1][iCount]);
           System.out.format("%13.8f,%13.8f%n", Math.log(specSynLams[iCount]), specSynFlux[1][iCount]); 
           iCount++;
         }

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
