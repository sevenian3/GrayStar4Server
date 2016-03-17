/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package graystar3server;


 // Returns depth distribution of occupation numbers in lower level of b-b transition,

// Input parameters:
// lam0 - line centre wavelength in nm
// logNStage - log_e density of absorbers in relevent ion stage (cm^-3)
// logFlu - log_10 oscillator strength (unitless)
// chiL - energy of lower atomic E-level of b-b transition in eV
// Also needs atsmopheric structure information:
// numDeps
// tauRos structure
// temp structure 
// rho structure
public class LevelPopsServer{

    public static double[] levelPops(double lam0In, double[] logNStage, double chiL, double gwStage, 
                    double gwL, int numDeps, double[][] tauRos, double[][] temp) {
        double c = Useful.c;
        double logC = Useful.logC();
        double k = Useful.k;
        double logK = Useful.logK();
        double logH = Useful.logH();
        double logEe = Useful.logEe();
        double logMe = Useful.logMe();

        double ln10 = Math.log(10.0);
        double logE = Math.log10(Math.E); // for debug output
        double log2pi = Math.log(2.0 * Math.PI);
        double log2 = Math.log(2.0);

        //double logNl = logNlIn * ln10;  // Convert to base e


        double logGwStage = Math.log(gwStage);
        double logGwL = Math.log(gwL);

        //System.out.println("chiL before: " + chiL);
        // If we need to subtract chiI from chiL, do so *before* converting to tiny numbers in ergs!
        ////For testing with Ca II lines using gS3 internal line list only:
        //boolean ionized = true;
        //if (ionized) {
        //    //System.out.println("ionized, doing chiL - chiI: " + ionized);
        //    //         chiL = chiL - chiI;
        //             chiL = chiL - 6.113;
        //          }
         //   //

        chiL = chiL * Useful.eV;  // Convert lower E-level from eV to ergs

        //Log of line-center wavelength in cm
        double logLam0 = Math.log(lam0In); // * 1.0e-7);

        // energy of b-b transition
        double logTransE = logH + logC - logLam0; //ergs

        double boltzFacL = chiL / k; // Pre-factor for exponent of excitation Boltzmann factor

        double boltzFacGround = 0.0 / k; //I know - its zero, but let's do it this way anyway'


        // return a 1D numDeps array of logarithmic number densities
        // level population of lower level of bb transition (could be in either stage I or II!) 
        double[] logNums = new double[numDeps];

        double num, logNum, expFac;

        for (int id = 0; id < numDeps; id++) {

                //System.out.println("LevPops: ionized branch taken, ionized =  " + ionized);
// Take stat weight of ground state as partition function:
                logNums[id] = logNStage[id] - boltzFacL / temp[0][id] + logGwL - logGwStage; // lower level of b-b transition
                //System.out.println("LevelPopsServer.stagePops id " + id + " logNStage[id] " + logNStage[id] + " boltzFacL " + boltzFacL + " temp[0][id] " + temp[0][id] + " logGwL " + logGwL + " logGwStage " + logGwStage + " logNums[id] " + logNums[id]);

            // System.out.println("LevelPops: id, logNums[0][id], logNums[1][id], logNums[2][id], logNums[3][id]: " + id + " "
            //          + Math.exp(logNums[0][id]) + " "
            //         + Math.exp(logNums[1][id]) + " "
            //          + Math.exp(logNums[2][id]) + " "
            //        + Math.exp(logNums[3][id]));
            //System.out.println("LevelPops: id, logNums[0][id], logNums[1][id], logNums[2][id], logNums[3][id], logNums[4][id]: " + id + " "
            //        + logE * (logNums[0][id]) + " "
            //        + logE * (logNums[1][id]) + " "
            //        + logE * (logNums[2][id]) + " "
            //        + logE * (logNums[3][id]) + " "
            //        + logE * (logNums[4][id]) );
            //System.out.println("LevelPops: id, logIonFracI, logIonFracII: " + id + " " + logE*logIonFracI + " " + logE*logIonFracII
            //        + "logNum, logNumI, logNums[0][id], logNums[1][id] "
            //        + logE*logNum + " " + logE*logNumI + " " + logE*logNums[0][id] + " " + logE*logNums[1][id]);
            //System.out.println("LevelPops: id, logIonFracI: " + id + " " + logE*logIonFracI
            //        + "logNums[0][id], boltzFacL/temp[0][id], logNums[2][id]: " 
            //        + logNums[0][id] + " " + boltzFacL/temp[0][id] + " " + logNums[2][id]);
        } //id loop

        return logNums;
    } //end method levelPops    
    

 // Returns depth distribution of ionization stage populations,

// Input parameters:
// logNl - log_10 column density of absorbers in lower E-level, l (cm^-2)
// chiI1 - ground state ionization energy of neutral stage 
// chiI2 - ground state ionization energy of singly ionized stage 
//   - we are assuming this is the neutral stage
// Also needs atsmopheric structure information:
// numDeps
// tauRos structure
// temp structure 
// rho structure

    public static double[][] stagePops(double logNlIn, double[][] Ne, double chiI1, double chiI2, double chiI3, double chiI4,
            double gw1, double gw2, double gw3, double gw4, 
            int numDeps, double kappaScale, double[][] tauRos, double[][] temp, double[][] rho) {

        double c = Useful.c;
        double logC = Useful.logC();
        double k = Useful.k;
        double logK = Useful.logK();
        double logH = Useful.logH();
        double logEe = Useful.logEe();
        double logMe = Useful.logMe();

        double ln10 = Math.log(10.0);
        double logE = Math.log10(Math.E); // for debug output
        double log2pi = Math.log(2.0 * Math.PI);
        double log2 = Math.log(2.0);

        double logNl = logNlIn * ln10;  // Convert to base e
        double logKScale = Math.log(kappaScale);

        //Assume ground state statistical weight (or partition fn) of Stage III is 1.0;
        double logGw5 = 0.0;


        double logGw1 = Math.log(gw1);
        double logGw2 = Math.log(gw2);
        double logGw3 = Math.log(gw3);
        double logGw4 = Math.log(gw4);

        //System.out.println("chiL before: " + chiL);
        // If we need to subtract chiI from chiL, do so *before* converting to tiny numbers in ergs!

        chiI1 = chiI1 * Useful.eV;  // Convert lower E-level from eV to ergs
        chiI2 = chiI2 * Useful.eV;  // Convert lower E-level from eV to ergs
        chiI3 = chiI3 * Useful.eV;  // Convert lower E-level from eV to ergs
        chiI4 = chiI4 * Useful.eV;  // Convert lower E-level from eV to ergs

        double boltzFacI1 = chiI1 / k; // Pre-factor for exponent of ionization Boltzmann factor for ion stage I
        double boltzFacI2 = chiI2 / k; // Pre-factor for exponent of ionization Boltzmann factor for ion stage I
        double boltzFacI3 = chiI3 / k; // Pre-factor for exponent of ionization Boltzmann factor for ion stage I
        double boltzFacI4 = chiI4 / k; // Pre-factor for exponent of ionization Boltzmann factor for ion stage I
        //System.out.println("boltzFacI1 " + boltzFacI1 + " boltzFacI2 " + boltzFacI2 + " chiI1 " + chiI1 + " chiI2 " + chiI2);

        double logSahaFac = log2 + (3.0 / 2.0) * (log2pi + logMe + logK - 2.0 * logH);

        int refRhoIndx = ToolBox.tauPoint(numDeps, tauRos, 1.0);
        double refLogRho = rho[1][refRhoIndx];
        //System.out.println("LINEKAPPA: refRhoIndx, refRho " + refRhoIndx + " " + logE*refRho);

        // return a 2D 3 x numDeps array of logarithmic number densities
        // Row 0: neutral stage ground state population
        // Row 1: singly ionized stage ground state population
        // Row 2: doubly ionized stage ground state population        
        double[][] logNums = new double[4][numDeps];

        double num, logNum, expFac, logSaha, saha, logIonFracI, logIonFracII, logIonFracIII, logIonFracIV,  
        logNumI, logNumII, logNumIII, logNumIV;
        double saha21, logSaha21, saha32, logSaha32, saha43, logSaha43;
        logNumI = 0.0;
        logNumII = 0.0;
        logNumIII = 0.0;
        logNumIV = 0.0;
        double logNe;

        for (int id = 0; id < numDeps; id++) {

            // reduce or enhance number density by over-all Rosseland opcity scale parameter
            logNum = logNl + logKScale;

            // scale numer density by relative depth variation of mass density
            logNum = logNum + rho[1][id] - refLogRho;

            //Row 1 of Ne is log_e Ne in cm^-3
            logNe = Ne[1][id];

            //
            // ********** Accounting for THREE ionization stages (I, II, III):
            //
            logSaha21 = logSahaFac - logNe - boltzFacI1 / temp[0][id] + (3.0 * temp[1][id] / 2.0) + logGw2 - logGw1; // log(RHS) of standard Saha equation
            saha21 = Math.exp(logSaha21);   //RHS of standard Saha equation
            logSaha32 = logSahaFac - logNe - boltzFacI2 / temp[0][id] + (3.0 * temp[1][id] / 2.0) + logGw3 - logGw2; // log(RHS) of standard Saha equation
            saha32 = Math.exp(logSaha32);   //RHS of standard Saha equation
            logSaha43 = logSahaFac - logNe - boltzFacI3 / temp[0][id] + (3.0 * temp[1][id] / 2.0) + logGw4 - logGw3; // log(RHS) of standard Saha equation
            saha43 = Math.exp(logSaha43);   //RHS of standard Saha equation
            //System.out.println("logSahaFac, logNe, logSaha= " + logE*logSahaFac + " " + logE*logNe + " " + logE*logSaha);

//System.out.println("LevelPopsServer: id " + id + " logSaha21 " + logSaha21 + " logSaha32 " + logSaha32 + " logNe " + logNe + " boltzFacI1 " + boltzFacI1 + " boltzFacI2 " + boltzFacI2 + " temp[0][id] " + temp[0][id] + " temp[1][id] " + temp[1][id] + " logGw2 " + logGw2 + " logGw1 " + logGw1 + " logGw3 " + logGw3); 

            double logDenominator = Math.log( 1.0 + saha21 + (saha32 * saha21) + (saha43 * saha32 * saha21) );
            logIonFracII = logSaha21 - logDenominator; // log ionization fraction in stage II
            logIonFracI = -1.0 * logDenominator;     // log ionization fraction in stage I
            logIonFracIII = logSaha32 + logSaha21 - logDenominator; //log ionization fraction in stage III
            logIonFracIV = logSaha43 + logSaha32 + logSaha21 - logDenominator; //log ionization fraction in stage III

            //if (id == 36) {
            //    System.out.println("logSaha21 " + logE*logSaha21 + " logSaha32 " + logE*logSaha32);
            //    System.out.println("IonFracII " + Math.exp(logIonFracII) + " IonFracI " + Math.exp(logIonFracI) + " logNe " + logE*logNe);
            //}
            //System.out.println("LevelPops: id, ionFracI, ionFracII: " + id + " " + Math.exp(logIonFracI) + " " + Math.exp(logIonFracII) );
                //System.out.println("LevPops: ionized branch taken, ionized =  " + ionized);

                logNums[0][id] = logNum + logIonFracI; // neutral stage pop 
                //System.out.println("LevelPopsServer.stagePops id " + id + " logNum " + logNum + " logIonFracI " + logIonFracI 
                 //      + " logNums[0][id] " + logNums[0][id]);
                logNums[1][id] = logNum + logIonFracII; // singly ionized stage pop 
                //System.out.println("LevelPopsServer.stagePops id " + id + " logNum " + logNum + " logIonFracII " + logIonFracII 
                 //      + " logNums[1][id] " + logNums[1][id]);
                logNums[2][id] = logNum + logIonFracIII; // doubly ionized stage pop                 
                logNums[3][id] = logNum + logIonFracIV; // doubly ionized stage pop                 

            // System.out.println("LevelPops: id, logNums[0][id], logNums[1][id], logNums[2][id], logNums[3][id]: " + id + " "
            //          + Math.exp(logNums[0][id]) + " "
            //         + Math.exp(logNums[1][id]) + " "
            //          + Math.exp(logNums[2][id]) + " "
            //        + Math.exp(logNums[3][id]));
            //System.out.println("LevelPops: id, logNums[0][id], logNums[1][id], logNums[2][id], logNums[3][id], logNums[4][id]: " + id + " "
            //        + logE * (logNums[0][id]) + " "
            //        + logE * (logNums[1][id]) + " "
            //        + logE * (logNums[2][id]) + " "
            //        + logE * (logNums[3][id]) + " "
            //        + logE * (logNums[4][id]) );
            //System.out.println("LevelPops: id, logIonFracI, logIonFracII: " + id + " " + logE*logIonFracI + " " + logE*logIonFracII
            //        + "logNum, logNumI, logNums[0][id], logNums[1][id] "
            //        + logE*logNum + " " + logE*logNumI + " " + logE*logNums[0][id] + " " + logE*logNums[1][id]);
            //System.out.println("LevelPops: id, logIonFracI: " + id + " " + logE*logIonFracI
            //        + "logNums[0][id], boltzFacL/temp[0][id], logNums[2][id]: " 
            //        + logNums[0][id] + " " + boltzFacL/temp[0][id] + " " + logNums[2][id]);
        } //id loop

        return logNums;
    } //end method levelPops    
    
} //end class LevelPops
