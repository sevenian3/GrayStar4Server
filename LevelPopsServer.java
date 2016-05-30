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
// temp structure 
public class LevelPopsServer{

    public static double[] levelPops(double lam0In, double[] logNStage, double chiL, double[] log10UwStage, 
                    double gwL, int numDeps, double[][] temp) {
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


// Parition functions passed in are 2-element vectore with remperature-dependent base 10 log Us
// Convert to natural logs:
        double thisLogUw, Ttheta;
        thisLogUw = 0.0; //default initialization
        double[] logUw = new double[2];
        double logE10 = Math.log(10.0);
        logUw[0] = logE10*log10UwStage[0];
        logUw[1] = logE10*log10UwStage[1];
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

        //Log of line-center wavelength in cm
        double logLam0 = Math.log(lam0In); // * 1.0e-7);

        // energy of b-b transition
        double logTransE = logH + logC - logLam0; //ergs

        double logChiL = Math.log(chiL) + Useful.logEv(); // Convert lower E-level from eV to ergs
        double logBoltzFacL = logChiL - Useful.logK(); // Pre-factor for exponent of excitation Boltzmann factor
        double boltzFacL = Math.exp(logBoltzFacL);

        double boltzFacGround = 0.0 / k; //I know - its zero, but let's do it this way anyway'


        // return a 1D numDeps array of logarithmic number densities
        // level population of lower level of bb transition (could be in either stage I or II!) 
        double[] logNums = new double[numDeps];

        double num, logNum, expFac;

        for (int id = 0; id < numDeps; id++) {


//Determine temeprature dependenet aprtition functions Uw:
        Ttheta = 5040.0 / temp[0][id];

        if (Ttheta >= 1.0){
            thisLogUw = logUw[0];
        }
        if (Ttheta <= 0.5){
            thisLogUw = logUw[1];
        }
        if (Ttheta > 0.5 && Ttheta < 1.0){
            thisLogUw = 0.5 * (Ttheta - 0.5) * logUw[1]
                + 0.5 * (1.0 - Ttheta) * logUw[0];
        }

                //System.out.println("LevPops: ionized branch taken, ionized =  " + ionized);
// Take stat weight of ground state as partition function:
                logNums[id] = logNStage[id] - boltzFacL / temp[0][id] + logGwL - thisLogUw; // lower level of b-b transition
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
// logNum - log_10 depth-dependent array of total element number density (cm^-3)
// chiI1 - ground state ionization energy of neutral stage 
// chiI2 - ground state ionization energy of singly ionized stage 
//   - we are assuming this is the neutral stage
// Also needs atsmopheric structure information:
// numDeps
// temp structure 

    public static double[][] stagePops(double[] logNum, double[][] Ne, double chiI1, double chiI2, double chiI3, double chiI4,
            double[] log10Uw1, double[] log10Uw2, double[] log10Uw3, double[] log10Uw4, 
            int numDeps, double[][] temp) {

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

        //Assume ground state statistical weight (or partition fn) of Stage III is 1.0;
        double logGw5 = 0.0;


        //double logUw1 = Math.log(uw1);
        //double logUw2 = Math.log(uw2);
        //double logUw3 = Math.log(uw3);
        //double logUw4 = Math.log(uw4);

// Parition functions passed in are 2-element vectore with remperature-dependent base 10 log Us
// Convert to natural logs:
        double thisLogUw1, thisLogUw2, thisLogUw3, thisLogUw4, Ttheta;
  //Default initializations:
        thisLogUw1 = 0.0;
        thisLogUw2 = 0.0;
        thisLogUw3 = 0.0;
        thisLogUw4 = 0.0;
        double[] logUw1 = new double[2];
        double[] logUw2 = new double[2];
        double[] logUw3 = new double[2];
        double[] logUw4 = new double[2];
        double logE10 = Math.log(10.0);
        logUw1[0] = logE10*log10Uw1[0];
        logUw1[1] = logE10*log10Uw1[1];
        logUw2[0] = logE10*log10Uw2[0];
        logUw2[1] = logE10*log10Uw2[1];
        logUw3[0] = logE10*log10Uw3[0];
        logUw3[1] = logE10*log10Uw3[1];
        logUw4[0] = logE10*log10Uw4[0];
        logUw4[1] = logE10*log10Uw4[1];

        //System.out.println("chiL before: " + chiL);
        // If we need to subtract chiI from chiL, do so *before* converting to tiny numbers in ergs!
        double logChiI, logBoltzfacI;
        logChiI = Math.log(chiI1) + Useful.logEv(); // Convert lower E-level from eV to ergs
        logBoltzfacI = logChiI - Useful.logK();
        double boltzFacI1 = Math.exp(logBoltzfacI);
        logChiI = Math.log(chiI2) + Useful.logEv(); // Convert lower E-level from eV to ergs
        logBoltzfacI = logChiI - Useful.logK();
        double boltzFacI2 = Math.exp(logBoltzfacI);
        logChiI = Math.log(chiI3) + Useful.logEv(); // Convert lower E-level from eV to ergs
        logBoltzfacI = logChiI - Useful.logK();
        double boltzFacI3 = Math.exp(logBoltzfacI);
        logChiI = Math.log(chiI4) + Useful.logEv(); // Convert lower E-level from eV to ergs
        logBoltzfacI = logChiI - Useful.logK();
        double boltzFacI4 = Math.exp(logBoltzfacI);

        //System.out.println("boltzFacI1 " + boltzFacI1 + " boltzFacI2 " + boltzFacI2 + " chiI1 " + chiI1 + " chiI2 " + chiI2);

        double logSahaFac = log2 + (3.0 / 2.0) * (log2pi + Useful.logMe() + logK - 2.0 * logH);


        // return a 2D 5 x numDeps array of logarithmic number densities
        // Row 0: neutral stage ground state population
        // Row 1: singly ionized stage ground state population
        // Row 2: doubly ionized stage ground state population        
        // Row 3: triply ionized stage ground state population        
        // Row 4: quadruply ionized stage ground state population        
        double[][] logNums = new double[5][numDeps];

        double num, expFac, logSaha, saha, logIonFracI, logIonFracII, logIonFracIII, logIonFracIV, logIonFracV;  
        double saha21, logSaha21, saha32, logSaha32, saha43, logSaha43, saha54, logSaha54;
        double logNe;

        for (int id = 0; id < numDeps; id++) {

            //// reduce or enhance number density by over-all Rosseland opcity scale parameter
            //logNum = logNl + logKScale;
//
 //           // scale numer density by relative depth variation of mass density

            //Row 1 of Ne is log_e Ne in cm^-3
            logNe = Ne[1][id];

//Determine temeprature dependenet aprtition functions Uw:
    Ttheta = 5040.0 / temp[0][id];

       if (Ttheta >= 1.0){
           thisLogUw1 = logUw1[0];
           thisLogUw2 = logUw2[0];
           thisLogUw3 = logUw3[0];
           thisLogUw4 = logUw4[0];
       }
       if (Ttheta <= 0.5){
           thisLogUw1 = logUw1[1];
           thisLogUw2 = logUw2[1];
           thisLogUw3 = logUw3[1];
           thisLogUw4 = logUw4[1];
       }
       if (Ttheta > 0.5 && Ttheta < 1.0){
           thisLogUw1 = 0.5 * (Ttheta - 0.5) * logUw1[1]
               + 0.5 * (1.0 - Ttheta) * logUw1[0];
           thisLogUw2 = 0.5 * (Ttheta - 0.5) * logUw2[1]
               + 0.5 * (1.0 - Ttheta) * logUw2[0];
           thisLogUw3 = 0.5 * (Ttheta - 0.5) * logUw3[1]
               + 0.5 * (1.0 - Ttheta) * logUw3[0];
           thisLogUw4 = 0.5 * (Ttheta - 0.5) * logUw4[1]
               + 0.5 * (1.0 - Ttheta) * logUw4[0];
       }
       //System.out.println("thisLogUw1, ... thisLogUw4 " + logE*thisLogUw1 + " " + logE*thisLogUw2 + " " + logE*thisLogUw3 + " " + logE*thisLogUw4);
       double thisLogUw5 = 0.0; //ionization stage V partition fn, U = 1.0

            //
            // ********** Accounting for FOUR ionization stages (I, II, III, IV):
            //
            logSaha21 = logSahaFac - logNe - (boltzFacI1 / temp[0][id]) + (3.0 * temp[1][id] / 2.0) + thisLogUw2 - thisLogUw1; // log(RHS) of standard Saha equation
            saha21 = Math.exp(logSaha21);   //RHS of standard Saha equation
            logSaha32 = logSahaFac - logNe - (boltzFacI2 / temp[0][id]) + (3.0 * temp[1][id] / 2.0) + thisLogUw3 - thisLogUw2; // log(RHS) of standard Saha equation
            saha32 = Math.exp(logSaha32);   //RHS of standard Saha equation
            logSaha43 = logSahaFac - logNe - (boltzFacI3 / temp[0][id]) + (3.0 * temp[1][id] / 2.0) + thisLogUw4 - thisLogUw3; // log(RHS) of standard Saha equation
            saha43 = Math.exp(logSaha43);   //RHS of standard Saha equation
            logSaha54 = logSahaFac - logNe - (boltzFacI4 / temp[0][id]) + (3.0 * temp[1][id] / 2.0) + thisLogUw5 - thisLogUw4; // log(RHS) of standard Saha equation
            saha54 = Math.exp(logSaha54);   //RHS of standard Saha equation
            //System.out.println("logSahaFac, logNe, logSaha= " + logE*logSahaFac + " " + logE*logNe + " " + logE*logSaha);

//System.out.println("LevelPopsServer: id " + id + " logSaha21 " + logSaha21 + " logSaha32 " + logSaha32 + " logNe " + logNe + " boltzFacI1 " + boltzFacI1 + " boltzFacI2 " + boltzFacI2 + " temp[0][id] " + temp[0][id] + " temp[1][id] " + temp[1][id] + " logGw2 " + logGw2 + " logGw1 " + logGw1 + " logGw3 " + logGw3); 

            double logDenominator = Math.log( 1.0 + saha21 + (saha32 * saha21) + (saha43 * saha32 * saha21) + (saha54 * saha43 * saha32 * saha21) );
            logIonFracI = -1.0 * logDenominator;     // log ionization fraction in stage I
            logIonFracII = logSaha21 - logDenominator; // log ionization fraction in stage II
            logIonFracIII = logSaha32 + logSaha21 - logDenominator; //log ionization fraction in stage III
            logIonFracIV = logSaha43 + logSaha32 + logSaha21 - logDenominator; //log ionization fraction in stage III
            logIonFracV = logSaha54 + logSaha43 + logSaha32 + logSaha21 - logDenominator; //log ionization fraction in stage III

            //if (id == 36) {
            //    System.out.println("logSaha21 " + logE*logSaha21 + " logSaha32 " + logE*logSaha32);
            //    System.out.println("IonFracII " + Math.exp(logIonFracII) + " IonFracI " + Math.exp(logIonFracI) + " logNe " + logE*logNe);
            //}
            //System.out.println("LevelPops: id, ionFracI, ionFracII: " + id + " " + Math.exp(logIonFracI) + " " + Math.exp(logIonFracII) );
                //System.out.println("LevPops: ionized branch taken, ionized =  " + ionized);

                logNums[0][id] = logNum[id] + logIonFracI; // neutral stage pop 
                //System.out.println("LevelPopsServer.stagePops id " + id + " logNum " + logNum + " logIonFracI " + logIonFracI 
                 //      + " logNums[0][id] " + logNums[0][id]);
                logNums[1][id] = logNum[id] + logIonFracII; // singly ionized stage pop 
                //System.out.println("LevelPopsServer.stagePops id " + id + " logNum " + logNum + " logIonFracII " + logIonFracII 
                 //      + " logNums[1][id] " + logNums[1][id]);
                logNums[2][id] = logNum[id] + logIonFracIII; // doubly ionized stage pop                 
                logNums[3][id] = logNum[id] + logIonFracIV; // triply ionized stage pop                 
                logNums[4][id] = logNum[id] + logIonFracV; // triply ionized stage pop                 

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
    
    

//Ionization equilibrium routine that accounts for molecule formation:
 // Returns depth distribution of ionization stage populations 

// Input parameters:
// logNum - array with depth-dependent total element number densities (cm^-3) 
// chiI1 - ground state ionization energy of neutral stage 
// chiI2 - ground state ionization energy of singly ionized stage 
// Also needs atsmopheric structure information:
// numDeps
// temp structure 
// rho structure
//
// Atomic element "A" is the one whose ionization fractions are being computed
//  Element "B" refers to array of other species with which A forms molecules "AB" 

    public static double[][] stagePops2(double[] logNum, double[][] Ne, double[] chiIArr, double[][] log10UwAArr,  //species A data - ionization equilibrium of A
                 int numMols, double[][] logNumB, double[] dissEArr, double[][] log10UwBArr, double[] log10QwABArr, double[] logMuABArr,  //data for set of species "B" - molecular equlibrium for set {AB}
                 int numDeps, double[][] temp) {


        double ln10 = Math.log(10.0);
        double logE = Math.log10(Math.E); // for debug output
        double log2pi = Math.log(2.0 * Math.PI);
        double log2 = Math.log(2.0);

    int numStages = chiIArr.length;// + 1; //need one more stage above the highest stage to be populated

//    var numMols = dissEArr.length;


// Parition functions passed in are 2-element vectore with remperature-dependent base 10 log Us
// Convert to natural logs:
        double Ttheta;
  //Default initializations:
//We need one more stage in size of saha factor than number of stages we're actualy populating
        double[] thisLogUw = new double[numStages+1];
        for (int i = 0; i < numStages+1; i++){
           thisLogUw[i] = 0.0;
        }

        double logE10 = Math.log(10.0);
//We need one more stage in size of saha factor than number of stages we're actualy populating
        double[][] logUw = new double[numStages+1][2];
        for (int i  = 0; i < numStages; i++){
           logUw[i][0] = logE10*log10UwAArr[i][0];
           logUw[i][1] = logE10*log10UwAArr[i][1];
        } 
        //Assume ground state statistical weight (or partition fn) of highest stage is 1.0;
        //var logGw5 = 0.0;
        logUw[numStages][0] = 0.0;
        logUw[numStages][1] = 0.0;

        //System.out.println("chiL before: " + chiL);
        // If we need to subtract chiI from chiL, do so *before* converting to tiny numbers in ergs!

//atomic ionization stage Boltzmann factors:
        double logChiI, logBoltzFacI;
        double[] boltzFacI = new double[numStages];
        for (int i = 0; i < numStages; i++){
           logChiI = Math.log(chiIArr[i]) + Useful.logEv(); 
           logBoltzFacI = logChiI  - Useful.logK();
           boltzFacI[i] = Math.exp(logBoltzFacI);
        }

        double logSahaFac = log2 + (3.0 / 2.0) * (log2pi + Useful.logMe() + Useful.logK() - 2.0 * Useful.logH());

        // return a 2D 5 x numDeps array of logarithmic number densities
        // Row 0: neutral stage ground state population
        // Row 1: singly ionized stage ground state population
        // Row 2: doubly ionized stage ground state population        
        // Row 3: triply ionized stage ground state population        
        // Row 4: quadruply ionized stage ground state population        
        double[][] logNums = new double[numStages][numDeps];

//We need one more stage in size of saha factor than number of stages we're actualy populating
//   for index accounting pirposes
//   For atomic ionization stages:
        double[][] logSaha = new double[numStages+1][numStages+1]; 
        double[][] saha = new double[numStages+1][numStages+1];
//
        double[] logIonFrac = new double[numStages];
        double expFac, logNe;

// Now - molecular variables:

//Treat at least one molecule - if there are really no molecules for an atomic species, 
//there will be one phantom molecule in the denominator of the ionization fraction
//with an impossibly high dissociation energy
   boolean ifMols = true;
   if (numMols == 0){
       ifMols = false;
       numMols = 1;
//This should be inherited, but let's make sure: 
       dissEArr[0] = 19.0; //eV
   }

//Molecular partition functions - default initialization:
       double[] thisLogUwB = new double[numMols];
       for (int iMol = 0; iMol < numMols; iMol++){
          thisLogUwB[iMol] = 0.0; // variable for temp-dependent computed partn fn of array element B 
       }
         double thisLogUwA = 0.0; // element A 

//For clarity: neutral stage of atom whose ionization equilibrium is being computed is element A
// for molecule formation:
        double[] logUwA = new double[2];
      if (numMols > 0){
        logUwA[0] = logUw[0][0];
        logUwA[1] = logUw[0][1];
      }
// Array of elements B for all molecular species AB:
       double[][] logUwB = new double[numMols][2];
      //if (numMols > 0){
        for (int iMol  = 0; iMol < numMols; iMol++){
           logUwB[iMol][0] = logE10*log10UwBArr[iMol][0];
           logUwB[iMol][1] = logE10*log10UwBArr[iMol][1];
        }
      //}
// Molecular partition functions:
       double[] logQwAB = new double[numMols];
      //if (numMols > 0){
       for (int iMol = 0; iMol < numMols; iMol++){
          logQwAB[iMol] = logE10*log10QwABArr[iMol];
       }
      //}
//Molecular dissociation Boltzmann factors:
        double[] boltzFacIAB = new double[numMols];
        double[] logMolSahaFac = new double[numMols];
      //if (numMols > 0){
       double logDissE, logBoltzFacIAB;
        for (int iMol = 0; iMol < numMols; iMol++){
           logDissE = Math.log(dissEArr[iMol]) + Useful.logEv(); 
           logBoltzFacIAB = logDissE  - Useful.logK();
           boltzFacIAB[iMol] = Math.exp(logBoltzFacIAB);
           logMolSahaFac[iMol] = (3.0 / 2.0) * (log2pi + logMuABArr[iMol] + Useful.logK() - 2.0 * Useful.logH());
  //console.log("iMol " + iMol + " dissEArr[iMol] " + dissEArr[iMol] + " logDissE " + logE*logDissE + " logBoltzFacIAB " + logE*logBoltzFacIAB + " boltzFacIAB[iMol] " + boltzFacIAB[iMol] + " logMuABArr " + logE*logMuABArr[iMol] + " logMolSahaFac " + logE*logMolSahaFac[iMol]);
        }
       //}
//   For molecular species:
        double[] logSahaMol = new double[numMols]; 
        double[] invSahaMol = new double[numMols];

        for (int id = 0; id < numDeps; id++) {

            //// reduce or enhance number density by over-all Rosseland opcity scale parameter
//
            //Row 1 of Ne is log_e Ne in cm^-3
            logNe = Ne[1][id];

//Determine temeprature dependenet aprtition functions Uw:
            Ttheta = 5040.0 / temp[0][id];

       if (Ttheta >= 1.0){
           for (int iStg = 0; iStg < numStages; iStg++){
              thisLogUw[iStg] = logUw[iStg][0];
           }
           for (int iMol = 0; iMol < numMols; iMol++){
              thisLogUwB[iMol] = logUwB[iMol][0];
           }
       }
       if (Ttheta <= 0.5){
           for (int iStg = 0; iStg < numStages; iStg++){
              thisLogUw[iStg] = logUw[iStg][1];
           }
           for (int iMol = 0; iMol < numMols; iMol++){
              thisLogUwB[iMol] = logUwB[iMol][1];
           }
       }
       if (Ttheta > 0.5 && Ttheta < 1.0){
           for (int iStg = 0; iStg < numStages; iStg++){
              thisLogUw[iStg] = 0.5 * (Ttheta - 0.5) * logUw[iStg][1]
                + 0.5 * (1.0 - Ttheta) * logUw[iStg][0];
           }
           for (int iMol = 0; iMol < numMols; iMol++){
              thisLogUwB[iMol] = 0.5 * (Ttheta - 0.5) * logUwB[iMol][1]
                + 0.5 * (1.0 - Ttheta) * logUwB[iMol][0];
           }
       }
         thisLogUw[numStages] = 0.0;
//For clarity: neutral stage of atom whose ionization equilibrium is being computed is element A
// for molecule formation:
     thisLogUwA = thisLogUw[0];

   //Ionization stage Saha factors: 
            for (int iStg = 0; iStg < numStages; iStg++){
             
               logSaha[iStg+1][iStg] = logSahaFac - logNe - (boltzFacI[iStg] /temp[0][id]) + (3.0 * temp[1][id] / 2.0) + thisLogUw[iStg+1] - thisLogUw[iStg];
               saha[iStg+1][iStg] = Math.exp(logSaha[iStg+1][iStg]);
         // if (id == 36){
              // console.log("iStg " + iStg + " boltzFacI[iStg] " + boltzFacI[iStg] + " thisLogUw[iStg] " + logE*thisLogUw[iStg] + " thisLogUw[iStg+1] " + logE*thisLogUw[iStg+1]);   
              // console.log("iStg+1 " + (iStg+1) + " iStg " + iStg + " logSahaji " + logE*logSaha[iStg+1][iStg] + " saha[iStg+1][iStg] " + saha[iStg+1][iStg]);
         // }
            }

//Molecular Saha factors:
         for (int iMol = 0; iMol < numMols; iMol++){
             logSahaMol[iMol] = logMolSahaFac[iMol] - logNumB[iMol][id] - (boltzFacIAB[iMol] / temp[0][id]) + (3.0 * temp[1][id] / 2.0) + thisLogUwB[iMol] + thisLogUwA - logQwAB[iMol];
//For denominator of ionization fraction, we need *inverse* molecular Saha factors (N_AB/NI):
             logSahaMol[iMol] = -1.0 * logSahaMol[iMol];
             invSahaMol[iMol] = Math.exp(logSahaMol[iMol]);
             //TEST invSahaMol[iMol] = 1.0e-99; //test
         // if (id == 36){
         //     console.log("iMol " + iMol + " boltzFacIAB[iMol] " + boltzFacIAB[iMol] + " thisLogUwB[iMol] " + logE*thisLogUwB[iMol] + " logNumB[iMol][id] " + logE*logNumB[iMol][id] + " logMolSahaFac[iMol] " + logMolSahaFac[iMol]);   
         //     console.log("iMol " + iMol + " logSahaMol " + logE*logSahaMol[iMol] + " invSahaMol[iMol] " + invSahaMol[iMol]);
         // }
         }
            //logSaha32 = logSahaFac - logNe - (boltzFacI2 / temp[0][id]) + (3.0 * temp[1][id] / 2.0) + thisLogUw3 - thisLogUw2; // log(RHS) of standard Saha equation
            //saha32 = Math.exp(logSaha32);   //RHS of standard Saha equation

//Compute log of denominator is ionization fraction, f_stage 
            double denominator = 1.0; //default initialization - leading term is always unity 
//ion stage contributions:
            for (int jStg = 1; jStg < numStages+1; jStg++){
               double addend = 1.0; //default initialization for product series
               for (int iStg = 0; iStg < jStg; iStg++){
                  //console.log("jStg " + jStg + " saha[][] indices " + (iStg+1) + " " + iStg); 
                  addend = addend * saha[iStg+1][iStg]; 
               }
               denominator = denominator + addend; 
            }
//molecular contribution
           if (ifMols == true){
             for (int iMol = 0; iMol < numMols; iMol++){
                denominator = denominator + invSahaMol[iMol];
             }
           }
// 
            double logDenominator = Math.log(denominator); 
          //if (id == 36){
          //     console.log("logDenominator " + logE*logDenominator);
         // }
            //var logDenominator = Math.log( 1.0 + saha21 + (saha32 * saha21) + (saha43 * saha32 * saha21) + (saha54 * saha43 * saha32 * saha21) );

            logIonFrac[0] = -1.0 * logDenominator;     // log ionization fraction in stage I
          //if (id == 36){
               //console.log("jStg 0 " + " logIonFrac[jStg] " + logE*logIonFrac[0]);
          //}
            for (int jStg = 1; jStg < numStages; jStg++){
               double addend = 0.0; //default initialization for product series
               for (int iStg = 0; iStg < jStg; iStg++){
                  //console.log("jStg " + jStg + " saha[][] indices " + (iStg+1) + " " + iStg); 
                  addend = addend + logSaha[iStg+1][iStg];
               }
               logIonFrac[jStg] = addend - logDenominator;
          //if (id == 36){
           //    console.log("jStg " + jStg + " logIonFrac[jStg] " + logE*logIonFrac[jStg]);
          //}
            }

            //logIonFracI = -1.0 * logDenominator;     // log ionization fraction in stage I
            //logIonFracII = logSaha21 - logDenominator; // log ionization fraction in stage II
            //logIonFracIII = logSaha32 + logSaha21 - logDenominator; //log ionization fraction in stage III
            //logIonFracIV = logSaha43 + logSaha32 + logSaha21 - logDenominator; //log ionization fraction in stage III

            //if (id == 36) {
            //    System.out.println("logSaha21 " + logE*logSaha21 + " logSaha32 " + logE*logSaha32);
            //    System.out.println("IonFracII " + Math.exp(logIonFracII) + " IonFracI " + Math.exp(logIonFracI) + " logNe " + logE*logNe);
            //}
            //System.out.println("LevelPops: id, ionFracI, ionFracII: " + id + " " + Math.exp(logIonFracI) + " " + Math.exp(logIonFracII) );
                //System.out.println("LevPops: ionized branch taken, ionized =  " + ionized);

              for (int iStg = 0; iStg < numStages; iStg++){
                 logNums[iStg][id] = logNum[id] + logIonFrac[iStg];
              }
        } //id loop

        return logNums;
    }; //end method stagePops    
    

//Diatomic molecular equilibrium routine that accounts for molecule formation:
 // Returns depth distribution of molecular population 

// Input parameters:
// logNum - array with depth-dependent total element number densities (cm^-3) 
// chiI1 - ground state ionization energy of neutral stage 
// chiI2 - ground state ionization energy of singly ionized stage 
// Also needs atsmopheric structure information:
// numDeps
// temp structure 
// rho structure
//
// Atomic element "A" is the one kept on the LHS of the master fraction, whose ionization fractions are included 
//   in the denominator of the master fraction
//  Element "B" refers to array of other sintpecies with which A forms molecules "AB" 

    public static double[] molPops(double[] nmrtrLogNumB, double nmrtrDissE, double[] log10UwA, double[] nmrtrLog10UwB, double nmrtrLog10QwAB, double nmrtrLogMuAB,  //species A data - ionization equilibrium of A
                 int numMolsB, double[][] logNumB, double[] dissEArr, double[][] log10UwBArr, double[] log10QwABArr, double[] logMuABArr,  //data for set of species "B" - molecular equlibrium for set {AB}
                 double[] logGroundRatio, int numDeps, double[][] temp) {


 //console.log("Line: nmrtrLog10UwB[0] " + nmrtrLog10UwB[0] + " nmrtrLog10UwB[1] " + nmrtrLog10UwB[1]);

        double ln10 = Math.log(10.0);
        double logE = Math.log10(Math.E); // for debug output
        double log2pi = Math.log(2.0 * Math.PI);
        double log2 = Math.log(2.0);

        double logE10 = Math.log(10.0);
// Convert to natural logs:
        double Ttheta;

//Treat at least one molecule - if there are really no molecules for an atomic species, 
//there will be one phantom molecule in the denominator of the ionization fraction
//with an impossibly high dissociation energy
   if (numMolsB == 0){
       numMolsB = 1;
//This should be inherited, but let's make sure: 
       dissEArr[0] = 29.0; //eV
   }

    //var molPops = function(logNum, numeratorLogNumB, numeratorDissE, numeratorLog10UwA, numeratorLog10QwAB, numeratorLogMuAB,  //species A data - ionization equilibrium of A
//Molecular partition functions - default initialization:
       double[] thisLogUwB = new double[numMolsB];
       for (int iMol = 0; iMol < numMolsB; iMol++){
          thisLogUwB[iMol] = 0.0; // variable for temp-dependent computed partn fn of array element B 
       }
         double thisLogUwA = 0.0; // element A 
         double nmrtrThisLogUwB = 0.0; // element A 

//For clarity: neutral stage of atom whose ionization equilibrium is being computed is element A
// for molecule formation:
        double[] logUwA = new double[2];
        logUwA[0] = logE10*log10UwA[0];
        logUwA[1] = logE10*log10UwA[1];
        double[] nmrtrLogUwB = new double[2];
        nmrtrLogUwB[0] = logE10*nmrtrLog10UwB[0];
        nmrtrLogUwB[1] = logE10*nmrtrLog10UwB[1];
// Array of elements B for all molecular species AB:
       double[][] logUwB = new double[numMolsB][2];
      //if (numMolsB > 0){
        for (int iMol  = 0; iMol < numMolsB; iMol++){
           logUwB[iMol][0] = logE10*log10UwBArr[iMol][0];
           logUwB[iMol][1] = logE10*log10UwBArr[iMol][1];
        }
      //}
// Molecular partition functions:
       double nmrtrLogQwAB = logE10*nmrtrLog10QwAB;
       double[] logQwAB = new double[numMolsB];
      //if (numMolsB > 0){
       for (int iMol = 0; iMol < numMolsB; iMol++){
          logQwAB[iMol] = logE10*log10QwABArr[iMol];
       }
      //}
//Molecular dissociation Boltzmann factors:
        double nmrtrBoltzFacIAB = 0.0;
        double nmrtrLogMolSahaFac = 0.0;
        double logDissE = Math.log(nmrtrDissE)  + Useful.logEv();
        double logBoltzFacIAB = logDissE  - Useful.logK();
        nmrtrBoltzFacIAB = Math.exp(logBoltzFacIAB);
        nmrtrLogMolSahaFac = (3.0 / 2.0) * (log2pi + nmrtrLogMuAB  + Useful.logK() - 2.0 * Useful.logH());
  //console.log("nmrtrDissE " + nmrtrDissE + " logDissE " + logE*logDissE + " logBoltzFacIAB " + logE*logBoltzFacIAB + " nmrtrBoltzFacIAB " + nmrtrBoltzFacIAB + " nmrtrLogMuAB " + logE*nmrtrLogMuAB + " nmrtrLogMolSahaFac " + logE*nmrtrLogMolSahaFac);
        double[] boltzFacIAB = new double[numMolsB];
        double[] logMolSahaFac = new double[numMolsB];
      //if (numMolsB > 0){
        for (int iMol = 0; iMol < numMolsB; iMol++){
           logDissE = Math.log(dissEArr[iMol]) + Useful.logEv(); 
           logBoltzFacIAB = logDissE  - Useful.logK();
           boltzFacIAB[iMol] = Math.exp(logBoltzFacIAB);
           logMolSahaFac[iMol] = (3.0 / 2.0) * (log2pi + logMuABArr[iMol] + Useful.logK() - 2.0 * Useful.logH());
  //console.log("iMol " + iMol + " dissEArr[iMol] " + dissEArr[iMol] + " logDissE " + logE*logDissE + " logBoltzFacIAB " + logE*logBoltzFacIAB + " boltzFacIAB[iMol] " + boltzFacIAB[iMol] + " logMuABArr " + logE*logMuABArr[iMol] + " logMolSahaFac " + logE*logMolSahaFac[iMol]);
        }
       
      //double[] logNums = new double[numDeps];
 
       //}
//   For molecular species:
        double nmrtrSaha, nmrtrLogSahaMol, nmrtrInvSahaMol;
        double[] logMolFrac = new double[numDeps];
        double[] logSahaMol = new double[numMolsB]; 
        double[] invSahaMol = new double[numMolsB];

        for (int id = 0; id < numDeps; id++) {

            //// reduce or enhance number density by over-all Rosseland opcity scale parameter

//Determine temeprature dependenet aprtition functions Uw:
            Ttheta = 5040.0 / temp[0][id];

       if (Ttheta >= 1.0){
           thisLogUwA = logUwA[0];
           nmrtrThisLogUwB = nmrtrLogUwB[0];
           for (int iMol = 0; iMol < numMolsB; iMol++){
              thisLogUwB[iMol] = logUwB[iMol][0];
           }
       }
       if (Ttheta <= 0.5){
           thisLogUwA = logUwA[1];
           nmrtrThisLogUwB = nmrtrLogUwB[1];
           for (int iMol = 0; iMol < numMolsB; iMol++){
              thisLogUwB[iMol] = logUwB[iMol][1];
           }
       }
       if (Ttheta > 0.5 && Ttheta < 1.0){
           thisLogUwA = 0.5 * (Ttheta - 0.5) * logUwA[1]
                + 0.5 * (1.0 - Ttheta) * logUwA[0];
           nmrtrThisLogUwB = 0.5 * (Ttheta - 0.5) * nmrtrLogUwB[1]
                + 0.5 * (1.0 - Ttheta) * nmrtrLogUwB[0];
           for (int iMol = 0; iMol < numMolsB; iMol++){
              thisLogUwB[iMol] = 0.5 * (Ttheta - 0.5) * logUwB[iMol][1]
                + 0.5 * (1.0 - Ttheta) * logUwB[iMol][0];
           }
       }
//For clarity: neutral stage of atom whose ionization equilibrium is being computed is element A
// for molecule formation:

   //Ionization stage Saha factors: 
//console.log("id " + id + " nmrtrLogNumB[id] " + logE*nmrtrLogNumB[id]);             
               nmrtrLogSahaMol = nmrtrLogMolSahaFac - nmrtrLogNumB[id] - (nmrtrBoltzFacIAB / temp[0][id]) + (3.0 * temp[1][id] / 2.0) + nmrtrThisLogUwB + thisLogUwA - nmrtrLogQwAB;
               nmrtrLogSahaMol = -1.0 * nmrtrLogSahaMol;
               nmrtrInvSahaMol = Math.exp(nmrtrLogSahaMol);

          //if (id == 36){
          //     console.log("nmrtrBoltzFacIAB " + nmrtrBoltzFacIAB + " nmrtrThisLogUwB " + logE*nmrtrThisLogUwB + " thisLogUwA " + logE*thisLogUwA + " nmrtrLogQwAB " + nmrtrLogQwAB);   
          //     console.log("nmrtrLogSahaMol " + logE*nmrtrLogSahaMol + " nmrtrInvSahaMol " + nmrtrInvSahaMol);
         // }

//Molecular Saha factors:
         for (int iMol = 0; iMol < numMolsB; iMol++){
//console.log("iMol " + iMol + " id " + id + " logNumB[iMol][id] " + logE*nmrtrLogNumB[id]);             
             logSahaMol[iMol] = logMolSahaFac[iMol] - logNumB[iMol][id] - (boltzFacIAB[iMol] / temp[0][id]) + (3.0 * temp[1][id] / 2.0) + thisLogUwB[iMol] + thisLogUwA - logQwAB[iMol];
//For denominator of ionization fraction, we need *inverse* molecular Saha factors (N_AB/NI):
             logSahaMol[iMol] = -1.0 * logSahaMol[iMol];
             invSahaMol[iMol] = Math.exp(logSahaMol[iMol]);
             //TEST invSahaMol[iMol] = 1.0e-99; //test
          //if (id == 36){
              //console.log("iMol " + iMol + " boltzFacIAB[iMol] " + boltzFacIAB[iMol] + " thisLogUwB[iMol] " + logE*thisLogUwB[iMol] + " logQwAB[iMol] " + logE*logQwAB[iMol] + " logNumB[iMol][id] " + logE*logNumB[iMol][id] + " logMolSahaFac[iMol] " + logE*logMolSahaFac[iMol]);   
              //console.log("iMol " + iMol + " logSahaMol " + logE*logSahaMol[iMol] + " invSahaMol[iMol] " + invSahaMol[iMol]);
          //}
         }

//Compute log of denominator is ionization fraction, f_stage 
        //default initialization 
        //  - ratio of total atomic particles in all ionization stages to number in ground state: 
            double denominator = Math.exp(logGroundRatio[id]); //default initialization - ratio of total atomic particles in all ionization stages to number in ground state 
//molecular contribution
           for (int iMol = 0; iMol < numMolsB; iMol++){
              denominator = denominator + invSahaMol[iMol];
           }
// 
            double logDenominator = Math.log(denominator); 
            //console.log("id " + id + " logGroundRatio " + logGroundRatio[id] + " logDenominator " + logDenominator);
            
          //if (id == 36){
          //     console.log("logDenominator " + logE*logDenominator);
         // }
            //var logDenominator = Math.log( 1.0 + saha21 + (saha32 * saha21) + (saha43 * saha32 * saha21) + (saha54 * saha43 * saha32 * saha21) );

          logMolFrac[id] = nmrtrInvSahaMol - logDenominator;
          //if (id == 36){
           //    console.log("jStg " + jStg + " logIonFrac[jStg] " + logE*logIonFrac[jStg]);
          //}

            //if (id == 36) {
            //    System.out.println("logSaha21 " + logE*logSaha21 + " logSaha32 " + logE*logSaha32);
            //    System.out.println("IonFracII " + Math.exp(logIonFracII) + " IonFracI " + Math.exp(logIonFracI) + " logNe " + logE*logNe);
            //}
            //System.out.println("LevelPops: id, ionFracI, ionFracII: " + id + " " + Math.exp(logIonFracI) + " " + Math.exp(logIonFracII) );
                //System.out.println("LevPops: ionized branch taken, ionized =  " + ionized);

            //logNums[id] = logNum[id] + logMolFrac;
        } //id loop

        return logMolFrac;
    }; //end method stagePops    
    
} //end class LevelPops
