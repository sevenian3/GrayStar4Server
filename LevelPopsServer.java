/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package chromastarserver;


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

    //public static double[] levelPops(double lam0In, double[] logNStage, double chiL, double[] log10UwStage, 
    //                double gwL, int numDeps, double[][] temp) {
    public static double[] levelPops(double lam0In, double[] logNStage, double chiL, double[] logUw, 
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
        //double[] logUw = new double[5];
        double logE10 = Math.log(10.0);
        //for (int kk = 0; kk < logUw.length; kk++){
        //    logUw[kk] = logE10*log10UwStage[kk]; // lburns new loop
        //}
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

        if (chiL <= 0.0){
           chiL = 1.0e-49;
        }
        double logChiL = Math.log(chiL) + Useful.logEv(); // Convert lower E-level from eV to ergs
        double logBoltzFacL = logChiL - Useful.logK(); // Pre-factor for exponent of excitation Boltzmann factor
        double boltzFacL = Math.exp(logBoltzFacL);

        double boltzFacGround = 0.0 / k; //I know - its zero, but let's do it this way anyway'


        // return a 1D numDeps array of logarithmic number densities
        // level population of lower level of bb transition (could be in either stage I or II!) 
        double[] logNums = new double[numDeps];

        double num, logNum, expFac;

        for (int id = 0; id < numDeps; id++) {

//NEW Determine temperature dependent partition functions Uw: lburns
        double thisTemp = temp[0][id];

        if (thisTemp >= 10000){
            thisLogUw = logUw[4];
        }
        if (thisTemp <= 130){
            thisLogUw = logUw[0];
        }
        if (thisTemp > 130 && thisTemp <= 500){
            thisLogUw = logUw[1] * (thisTemp - 130)/(500 - 130)
                      + logUw[0] * (500 - thisTemp)/(500 - 130);
        }
        if (thisTemp > 500 && thisTemp <= 3000){
            thisLogUw = logUw[2] * (thisTemp - 500)/(3000 - 500)
                      + logUw[1] * (3000 - thisTemp)/(3000 - 500);
        }
        if (thisTemp > 3000 && thisTemp <= 8000){
            thisLogUw = logUw[3] * (thisTemp - 3000)/(8000 - 3000)
                      + logUw[2] * (8000 - thisTemp)/(8000 - 3000);
        }
        if (thisTemp > 8000 && thisTemp < 10000){
            thisLogUw = logUw[4] * (thisTemp - 8000)/(10000 - 8000)
                      + logUw[3] * (10000 - thisTemp)/(10000 - 8000);
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

    //public static double[][] stagePops2(double[] logNum, double[][] Ne, double[] chiIArr, double[][] log10UwAArr,  //species A data - ionization equilibrium of A
    //             int numMols, double[][] logNumB, double[] dissEArr, double[][] log10UwBArr, double[][] logQwABArr, double[] logMuABArr,  //data for set of species "B" - molecular equlibrium for set {AB}
    //             int numDeps, double[][] temp) {
    public static double[][] stagePops2(double[] logNum, double[][] Ne, double[] chiIArr, double[][] logUw,  //species A data - ionization equilibrium of A
                 int numMols, double[][] logNumB, double[] dissEArr, double[][] logUwB, double[][] logQwABArr, double[] logMuABArr,  //data for set of species "B" - molecular equlibrium for set {AB}
                 int numDeps, double[][] temp) {


        double ln10 = Math.log(10.0);
        double logE = Math.log10(Math.E); // for debug output
        double log2pi = Math.log(2.0 * Math.PI);
        double log2 = Math.log(2.0);

    int numStages = chiIArr.length;// + 1; //need one more stage above the highest stage to be populated

//    var numMols = dissEArr.length;


// Parition functions passed in are 2-element vectore with remperature-dependent base 10 log Us
// Convert to natural logs:
        double Ttheta, thisTemp;
  //Default initializations:
//We need one more stage in size of saha factor than number of stages we're actualy populating
        double[] thisLogUw = new double[numStages+1];
        for (int i = 0; i < numStages+1; i++){
           thisLogUw[i] = 0.0;
        }

        double logE10 = Math.log(10.0);
//We need one more stage in size of saha factor than number of stages we're actualy populating
        //double[][] logUw = new double[numStages+1][5];
        //for (int i  = 0; i < numStages; i++){
        //   for (int kk = 0; kk < 5; kk++){
        //        logUw[i][kk] = logE10*log10UwAArr[i][kk];
        //   } // lburns- what variable can we use instead of 5?
        //} 
        //Assume ground state statistical weight (or partition fn) of highest stage is 1.0;
        //var logGw5 = 0.0;
        //for (int kk = 0; kk < 5; kk++){
        //    logUw[numStages][kk] = 0.0;
        //} // lburns

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
         double thisLogQwAB = Math.log(300.0); 

//For clarity: neutral stage of atom whose ionization equilibrium is being computed is element A
// for molecule formation:
        double[] logUwA = new double[5];
      if (numMols > 0){
        for (int kk = 0; kk < logUwA.length; kk++){
            logUwA[kk] = logUw[0][kk];
        } // lburns
      }
// Array of elements B for all molecular species AB:
       //double[][] logUwB = new double[numMols][5];
      //if (numMols > 0){
       // for (int iMol  = 0; iMol < numMols; iMol++){
       //    for (int kk = 0; kk < 5; kk++){
       //         logUwB[iMol][kk] = logE10*log10UwBArr[iMol][kk];
       //    } // lburns new loop
       // }
      //}
//// Molecular partition functions:
//       double[] logQwAB = new double[numMols];
//      //if (numMols > 0){
//       for (int iMol = 0; iMol < numMols; iMol++){
//          logQwAB[iMol] = logE10*log10QwABArr[iMol];
//       }
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
            thisTemp = temp[0][id];
// NEW Determine temperature dependent partition functions Uw: lburns
        if (thisTemp <= 130){
            for (int iStg = 0; iStg < numStages; iStg++){
                thisLogUw[iStg] = logUw[iStg][0];
            }
            for (int iMol = 0; iMol < numMols; iMol++){
                thisLogUwB[iMol] = logUwB[iMol][0];
            }
        }
        if (thisTemp > 130 && thisTemp <= 500){
            for (int iStg = 0; iStg < numStages; iStg++){
                thisLogUw[iStg] = logUw[iStg][1] * (thisTemp - 130)/(500 - 130)
                                + logUw[iStg][0] * (500 - thisTemp)/(500 - 130);
            }
            for (int iMol = 0; iMol < numMols; iMol++){
                thisLogUwB[iMol] = logUwB[iMol][1] * (thisTemp - 130)/(500 - 130)
                                 + logUwB[iMol][0] * (500 - thisTemp)/(500 - 130);
            }
        }
        if (thisTemp > 500 && thisTemp <= 3000){
            for (int iStg = 0; iStg < numStages; iStg++){
                thisLogUw[iStg] = logUw[iStg][2] * (thisTemp - 500)/(3000 - 500)
                                + logUw[iStg][1] * (3000 - thisTemp)/(3000 - 500);
            }
            for (int iMol = 0; iMol < numMols; iMol++){
                thisLogUwB[iMol] = logUwB[iMol][2] * (thisTemp - 500)/(3000 - 500)
                                 + logUwB[iMol][1] * (3000 - thisTemp)/(3000 - 500);
            }
        }
        if (thisTemp > 3000 && thisTemp <= 8000){
            for (int iStg = 0; iStg < numStages; iStg++){
                thisLogUw[iStg] = logUw[iStg][3] * (thisTemp - 3000)/(8000 - 3000)
                                + logUw[iStg][2] * (8000 - thisTemp)/(8000 - 3000);
            }
            for (int iMol = 0; iMol < numMols; iMol++){
                thisLogUwB[iMol] = logUwB[iMol][3] * (thisTemp - 3000)/(8000 - 3000)
                                 + logUwB[iMol][2] * (8000 - thisTemp)/(8000 - 3000);
            }
        }
        if (thisTemp > 8000 && thisTemp < 10000){
            for (int iStg = 0; iStg < numStages; iStg++){
                thisLogUw[iStg] = logUw[iStg][4] * (thisTemp - 8000)/(10000 - 8000)
                                + logUw[iStg][3] * (10000 - thisTemp)/(10000 - 8000);
            }
            for (int iMol = 0; iMol < numMols; iMol++){
                thisLogUwB[iMol] = logUwB[iMol][4] * (thisTemp - 8000)/(10000 - 8000)
                                 + logUwB[iMol][3] * (10000 - thisTemp)/(10000 - 8000);
            }
        }
        if (thisTemp >= 10000){
            for (int iStg = 0; iStg < numStages; iStg++){
                thisLogUw[iStg] = logUw[iStg][4];
            }
            for (int iMol = 0; iMol < numMols; iMol++){
                thisLogUwB[iMol] = logUwB[iMol][4];
            }
        }



         thisLogUw[numStages] = 0.0;
      for (int iMol = 0; iMol < numMols; iMol++){
         if (thisTemp < 3000.0){
            thisLogQwAB = ( logQwABArr[iMol][1] * (3000.0 - thisTemp)/(3000.0 - 500.0) )
                        + ( logQwABArr[iMol][2] * (thisTemp - 500.0)/(3000.0 - 500.0) ); 
         }
         if ( (thisTemp >= 3000.0) && (thisTemp <= 8000.0) ){
            thisLogQwAB = ( logQwABArr[iMol][2] * (8000.0 - thisTemp)/(8000.0 - 3000.0) )
                        + ( logQwABArr[iMol][3] * (thisTemp - 3000.0)/(8000.0 - 3000.0) ); 
         }
         if ( thisTemp > 8000.0 ){
            thisLogQwAB = ( logQwABArr[iMol][3] * (10000.0 - thisTemp)/(10000.0 - 8000.0) )
                        + ( logQwABArr[iMol][4] * (thisTemp - 8000.0)/(10000.0 - 8000.0) ); 
         }
      } // iMol loop 
            
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
             logSahaMol[iMol] = logMolSahaFac[iMol] - logNumB[iMol][id] - (boltzFacIAB[iMol] / temp[0][id]) + (3.0 * temp[1][id] / 2.0) + thisLogUwB[iMol] + thisLogUwA - thisLogQwAB;
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
    } //end method stagePops    
    

// RHS of partial pressure formulation of Saha equation in standard form (N_U*P_e/N_L on LHS)
 // Returns depth distribution of LHS: Phi(T) === N_U*P_e/N_L (David Gray notation)

// Input parameters:
// chiI - ground state ionization energy of lower stage 
// log10UwUArr, log10UwLArr - array of temperature-dependent partition function for upper and lower ionization stage
// Also needs atsmopheric structure information:
// numDeps
// temp structure 
//
// Atomic element "A" is the one whose ionization fractions are being computed
//  Element "B" refers to array of other species with which A forms molecules "AB" 

    //public static double[] sahaRHS(double chiI, double[] log10UwUArr, double[] log10UwLArr,
    //             int numDeps, double[][] temp) {
    //public static double sahaRHS(double chiI, double[] log10UwUArr, double[] log10UwLArr,
    //              double[] temp) {
    public static double sahaRHS(double chiI, double[] logUwU, double[] logUwL,
                  double[] temp) {


        double ln10 = Math.log(10.0);
        double logE = Math.log10(Math.E); // for debug output
        double log2pi = Math.log(2.0 * Math.PI);
        double log2 = Math.log(2.0);


//    var numMols = dissEArr.length;


// Parition functions passed in are 2-element vectore with remperature-dependent base 10 log Us
// Convert to natural logs:
        double Ttheta, thisTemp;
  //Default initializations:
//We need one more stage in size of saha factor than number of stages we're actualy populating
        double thisLogUwU = 0.0;
        double thisLogUwL = 0.0;

        double logE10 = Math.log(10.0);
//We need one more stage in size of saha factor than number of stages we're actualy populating
        //double[] logUwU = new double[5];
        //double[] logUwL = new double[5];
           for (int kk = 0; kk < logUwL.length; kk++){
                logUwU[kk] = logUwL[kk];
                //logUwL[kk] = logE10*log10UwLArr[kk];
           }


        //System.out.println("chiL before: " + chiL);
        // If we need to subtract chiI from chiL, do so *before* converting to tiny numbers in ergs!

//atomic ionization stage Boltzmann factors:
        double logChiI, logBoltzFacI;
        double boltzFacI;
           logChiI = Math.log(chiI) + Useful.logEv(); 
           logBoltzFacI = logChiI  - Useful.logK();
           boltzFacI = Math.exp(logBoltzFacI);

//Extra factor of k to get k^5/2 in the P_e formulation of Saha Eq.
        double logSahaFac = log2 + (3.0 / 2.0) * (log2pi + Useful.logMe() + Useful.logK() - 2.0 * Useful.logH()) + Useful.logK();

        //double[] logLHS = new double[numDeps];
        double logLHS;

//   For atomic ionization stages:
        double logSaha, saha, expFac;

//        for (int id = 0; id < numDeps; id++) {

//
//Determine temperature dependent partition functions Uw:
            thisTemp = temp[0];
        if (thisTemp <= 130){
            thisLogUwU = logUwU[0];
            thisLogUwL = logUwL[0];
        }
        if (thisTemp > 130 && thisTemp <= 500){
            thisLogUwU = logUwU[1] * (thisTemp - 130)/(500 - 130)
                       + logUwU[0] * (500 - thisTemp)/(500 - 130);
            thisLogUwL = logUwL[1] * (thisTemp - 130)/(500 - 130)
                       + logUwL[0] * (500 - thisTemp)/(500 - 130);
        }
        if (thisTemp > 500 && thisTemp <= 3000){
            thisLogUwU = logUwU[2] * (thisTemp - 500)/(3000 - 500)
                       + logUwU[1] * (3000 - thisTemp)/(3000 - 500);
            thisLogUwL = logUwL[2] * (thisTemp - 500)/(3000 - 500)
                       + logUwL[1] * (3000 - thisTemp)/(3000 - 500);
        }
        if (thisTemp > 3000 && thisTemp <= 8000){
            thisLogUwU = logUwU[3] * (thisTemp - 3000)/(8000 - 3000)
                       + logUwU[2] * (8000 - thisTemp)/(8000 - 3000);
            thisLogUwL = logUwL[3] * (thisTemp - 3000)/(8000 - 3000)
                       + logUwL[2] * (8000 - thisTemp)/(8000 - 3000);
        }
        if (thisTemp > 8000 && thisTemp < 10000){
            thisLogUwU = logUwU[4] * (thisTemp - 8000)/(10000 - 8000)
                       + logUwU[3] * (10000 - thisTemp)/(10000 - 8000);
            thisLogUwL = logUwL[4] * (thisTemp - 8000)/(10000 - 8000)
                       + logUwL[3] * (10000 - thisTemp)/(10000 - 8000);
        }
        if (thisTemp >= 10000){
            thisLogUwU = logUwU[4];
            thisLogUwL = logUwL[4];
        }
 

   //Ionization stage Saha factors: 
             
//Need T_kin^5/2 in the P_e formulation of Saha Eq.
               logSaha = logSahaFac - (boltzFacI /temp[0]) + (5.0 * temp[1] / 2.0) + thisLogUwU - thisLogUwL;
              // saha = Math.exp(logSaha);

                 //logLHS[id] = logSaha;
                 logLHS = logSaha;
 //       } //id loop

        return logLHS;
//
    } //end method sahaRHS 
    

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

    //public static double[] molPops(double[] nmrtrLogNumB, double nmrtrDissE, double[] log10UwA, double[] nmrtrLog10UwB, double[] nmrtrLogQwAB, double nmrtrLogMuAB,  //species A data - ionization equilibrium of A
     //            int numMolsB, double[][] logNumB, double[] dissEArr, double[][] log10UwBArr, double[][] logQwABArr, double[] logMuABArr,  //data for set of species "B" - molecular equlibrium for set {AB}
      //           double[] logGroundRatio, int numDeps, double[][] temp) {
    public static double[] molPops(double[] nmrtrLogNumB, double nmrtrDissE, double[] logUwA, double[] nmrtrLogUwB, double[] nmrtrLogQwAB, double nmrtrLogMuAB,  //species A data - ionization equilibrium of A
                 int numMolsB, double[][] logNumB, double[] dissEArr, double[][] logUwB, double[][] logQwABArr, double[] logMuABArr,  //data for set of species "B" - molecular equlibrium for set {AB}
                 double[] logGroundRatio, int numDeps, double[][] temp) {


        double logE = Math.log10(Math.E); // for debug output
       //System.out.println("molPops: nmrtrDissE " + nmrtrDissE + " log10UwA " + log10UwA[0] + " " + log10UwA[1] + " nmrtrLog10UwB " +
       //     nmrtrLog10UwB[0] + " " + nmrtrLog10UwB[1] + " nmrtrLog10QwAB " + logE*nmrtrLogQwAB[2] + " nmrtrLogMuAB " + logE*nmrtrLogMuAB
       //     + " numMolsB " + numMolsB + " dissEArr " + dissEArr[0] + " log10UwBArr " + log10UwBArr[0][0] + " " + log10UwBArr[0][1] + " log10QwABArr " +
       //     logE*logQwABArr[0][2] + " logMuABArr " + logE*logMuABArr[0]);
 //System.out.println("Line: nmrtrLog10UwB[0] " + logE*nmrtrLog10UwB[0] + " nmrtrLog10UwB[1] " + logE*nmrtrLog10UwB[1]);

        double ln10 = Math.log(10.0);
        double log2pi = Math.log(2.0 * Math.PI);
        double log2 = Math.log(2.0);

        double logE10 = Math.log(10.0);
// Convert to natural logs:
        double Ttheta, thisTemp;

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
         double thisLogQwAB = Math.log(300.0);
         double nmrtrThisLogQwAB = Math.log(300.0);

//For clarity: neutral stage of atom whose ionization equilibrium is being computed is element A
// for molecule formation:
        //double[] logUwA = new double[5];
        //double[] nmrtrLogUwB = new double[5];
        //for (int kk = 0; kk < logUwA.length; kk++){
        //    logUwA[kk] = logE10*log10UwA[kk];
        //    nmrtrLogUwB[kk] = logE10*nmrtrLog10UwB[kk];
        //} // lburns new loop
// Array of elements B for all molecular species AB:
      // double[][] logUwB = new double[numMolsB][5];
      //if (numMolsB > 0){
      //  for (int iMol  = 0; iMol < numMolsB; iMol++){
      //     for (int kk = 0; kk < 5; kk++){
      //          logUwB[iMol][kk] = logE10*log10UwBArr[iMol][kk];
      //     } // lburns new loop
      //  }
      //}
// Molecular partition functions:
//       double nmrtrLogQwAB = logE10*nmrtrLog10QwAB;
//       double[] logQwAB = new double[numMolsB];
//      //if (numMolsB > 0){
//       for (int iMol = 0; iMol < numMolsB; iMol++){
//          logQwAB[iMol] = logE10*log10QwABArr[iMol];
//       }
      //}
//Molecular dissociation Boltzmann factors:
        double nmrtrBoltzFacIAB = 0.0;
        double nmrtrLogMolSahaFac = 0.0;
        double logDissE = Math.log(nmrtrDissE)  + Useful.logEv();
  //System.out.println("logDissE " + logE*logDissE);
        double logBoltzFacIAB = logDissE  - Useful.logK();
  //System.out.println("logBoltzFacIAB " + logE*logBoltzFacIAB);
        nmrtrBoltzFacIAB = Math.exp(logBoltzFacIAB);
        nmrtrLogMolSahaFac = (3.0 / 2.0) * (log2pi + nmrtrLogMuAB  + Useful.logK() - 2.0 * Useful.logH());
  //System.out.println("nmrtrLogMolSahaFac " + logE*nmrtrLogMolSahaFac);
  //System.out.println("nmrtrDissE " + nmrtrDissE + " logDissE " + logE*logDissE + " logBoltzFacIAB " + logE*logBoltzFacIAB + " nmrtrBoltzFacIAB " + nmrtrBoltzFacIAB + " nmrtrLogMuAB " + logE*nmrtrLogMuAB + " nmrtrLogMolSahaFac " + logE*nmrtrLogMolSahaFac);
        double[] boltzFacIAB = new double[numMolsB];
        double[] logMolSahaFac = new double[numMolsB];
      //if (numMolsB > 0){
        for (int iMol = 0; iMol < numMolsB; iMol++){
           logDissE = Math.log(dissEArr[iMol]) + Useful.logEv(); 
           logBoltzFacIAB = logDissE  - Useful.logK();
           boltzFacIAB[iMol] = Math.exp(logBoltzFacIAB);
           logMolSahaFac[iMol] = (3.0 / 2.0) * (log2pi + logMuABArr[iMol] + Useful.logK() - 2.0 * Useful.logH());
  //System.out.println("logMolSahaFac[iMol] " + logE*logMolSahaFac[iMol]);
  //System.out.println("iMol " + iMol + " dissEArr[iMol] " + dissEArr[iMol] + " logDissE " + logE*logDissE + " logBoltzFacIAB " + logE*logBoltzFacIAB + " boltzFacIAB[iMol] " + boltzFacIAB[iMol] + " logMuABArr " + logE*logMuABArr[iMol] + " logMolSahaFac " + logE*logMolSahaFac[iMol]);
        }
       
      //double[] logNums = new double[numDeps];
 
       //}
//   For molecular species:
        double nmrtrSaha, nmrtrLogSahaMol, nmrtrLogInvSahaMol; //, nmrtrInvSahaMol;
        double[] logMolFrac = new double[numDeps];
        double[] logSahaMol = new double[numMolsB]; 
        double[] invSahaMol = new double[numMolsB];
//
//
       //  System.out.println("molPops: id      nmrtrLogNumB      logNumBArr[0]      logGroundRatio");
        for (int id = 0; id < numDeps; id++) {

        //   System.out.format("%03d, %21.15f, %21.15f, %21.15f, %n", id, logE*nmrtrLogNumB[id], logE*logNumB[0][id], logE*logGroundRatio[id]);

            //// reduce or enhance number density by over-all Rosseland opcity scale parameter

//Determine temparature dependent partition functions Uw:
            thisTemp = temp[0][id];
// NEW Determine temperature dependent partition functions Uw: lburns
        thisTemp = temp[0][id];
        if (thisTemp <= 130){
            thisLogUwA = logUwA[0];
            nmrtrThisLogUwB = nmrtrLogUwB[0];
            for (int iMol = 0; iMol < numMolsB; iMol++){
                thisLogUwB[iMol] = logUwB[iMol][0];
            }
        }
        if (thisTemp > 130 && thisTemp <= 500){
            thisLogUwA = logUwA[1] * (thisTemp - 130)/(500 - 130)
                       + logUwA[0] * (500 - thisTemp)/(500 - 130);
            nmrtrThisLogUwB = nmrtrLogUwB[1] * (thisTemp - 130)/(500 - 130)
                            + nmrtrLogUwB[0] * (500 - thisTemp)/(500 - 130);
            for (int iMol = 0; iMol < numMolsB; iMol++){
                thisLogUwB[iMol] = logUwB[iMol][1] * (thisTemp - 130)/(500 - 130)
                                 + logUwB[iMol][0] * (500 - thisTemp)/(500 - 130);
            }
        }
        if (thisTemp > 500 && thisTemp <= 3000){
            thisLogUwA = logUwA[2] * (thisTemp - 500)/(3000 - 500)
                       + logUwA[1] * (3000 - thisTemp)/(3000 - 500);
            nmrtrThisLogUwB = nmrtrLogUwB[2] * (thisTemp - 500)/(3000 - 500)
                            + nmrtrLogUwB[1] * (3000 - thisTemp)/(3000 - 500);
            for (int iMol = 0; iMol < numMolsB; iMol++){
                thisLogUwB[iMol] = logUwB[iMol][2] * (thisTemp - 500)/(3000 - 500)
                                 + logUwB[iMol][1] * (3000 - thisTemp)/(3000 - 500);
            }
        }
        if (thisTemp > 3000 && thisTemp <= 8000){
            thisLogUwA = logUwA[3] * (thisTemp - 3000)/(8000 - 3000)
                       + logUwA[2] * (8000 - thisTemp)/(8000 - 3000);
            nmrtrThisLogUwB = nmrtrLogUwB[3] * (thisTemp - 3000)/(8000 - 3000)
                            + nmrtrLogUwB[2] * (8000 - thisTemp)/(8000 - 3000);
            for (int iMol = 0; iMol < numMolsB; iMol++){
                thisLogUwB[iMol] = logUwB[iMol][3] * (thisTemp - 3000)/(8000 - 3000)
                                 + logUwB[iMol][2] * (8000 - thisTemp)/(8000 - 3000);
            }
        }
        if (thisTemp > 8000 && thisTemp < 10000){
            thisLogUwA = logUwA[4] * (thisTemp - 8000)/(10000 - 8000)
                       + logUwA[3] * (10000 - thisTemp)/(10000 - 8000);
            nmrtrThisLogUwB = nmrtrLogUwB[4] * (thisTemp - 8000)/(10000 - 8000)
                            + nmrtrLogUwB[3] * (10000 - thisTemp)/(10000 - 8000);
            for (int iMol = 0; iMol < numMolsB; iMol++){
                thisLogUwB[iMol] = logUwB[iMol][4] * (thisTemp - 8000)/(10000 - 8000)
                                 + logUwB[iMol][3] * (10000 - thisTemp)/(10000 - 8000);
            }
        }
        if (thisTemp >= 10000){
            thisLogUwA = logUwA[4];
            nmrtrThisLogUwB = nmrtrLogUwB[4];
            for (int iMol = 0; iMol < numMolsB; iMol++){
                thisLogUwB[iMol] = logUwB[iMol][4];
            }
        }

      for (int iMol = 0; iMol < numMolsB; iMol++){
         if (thisTemp < 3000.0){
            thisLogQwAB = ( logQwABArr[iMol][1] * (3000.0 - thisTemp)/(3000.0 - 500.0) )
                        + ( logQwABArr[iMol][2] * (thisTemp - 500.0)/(3000.0 - 500.0) ); 
         }
         if ( (thisTemp >= 3000.0) && (thisTemp <= 8000.0) ){
            thisLogQwAB = ( logQwABArr[iMol][2] * (8000.0 - thisTemp)/(8000.0 - 3000.0) )
                        + ( logQwABArr[iMol][3] * (thisTemp - 3000.0)/(8000.0 - 3000.0) ); 
         }
         if ( thisTemp > 8000.0 ){
            thisLogQwAB = ( logQwABArr[iMol][3] * (10000.0 - thisTemp)/(10000.0 - 8000.0) )
                        + ( logQwABArr[iMol][4] * (thisTemp - 8000.0)/(10000.0 - 8000.0) ); 
         }
         if (thisTemp < 3000.0){
            nmrtrThisLogQwAB = ( nmrtrLogQwAB[1] * (3000.0 - thisTemp)/(3000.0 - 500.0) )
                             + ( nmrtrLogQwAB[2] * (thisTemp - 500.0)/(3000.0 - 500.0) ); 
         }
         if ( (thisTemp >= 3000.0) && (thisTemp <= 8000.0) ){
            nmrtrThisLogQwAB = ( nmrtrLogQwAB[2] * (8000.0 - thisTemp)/(8000.0 - 3000.0) )
                             + ( nmrtrLogQwAB[3] * (thisTemp - 3000.0)/(8000.0 - 3000.0) ); 
         }
         if ( thisTemp > 8000.0 ){
            nmrtrThisLogQwAB = ( nmrtrLogQwAB[3] * (10000.0 - thisTemp)/(10000.0 - 8000.0) )
                             + ( nmrtrLogQwAB[4] * (thisTemp - 8000.0)/(10000.0 - 8000.0) ); 
         }
      } //iMol loop 
//For clarity: neutral stage of atom whose ionization equilibrium is being computed is element A
// for molecule formation:

   //Ionization stage Saha factors: 
//System.out.println("id " + id + " nmrtrLogNumB[id] " + logE*nmrtrLogNumB[id]);   
  // if (id == 16){ 
  //   System.out.println("id " + id + " nmrtrLogNumB[id] " + logE*nmrtrLogNumB[id] + " pp nmrtB " + (logE*(nmrtrLogNumB[id]+temp[1][id]+Useful.logK())) + " nmrtrThisLogUwB " + logE*nmrtrThisLogUwB + " thisLogUwA " + logE*thisLogUwA + " nmrtrLogQwAB " + logE*nmrtrThisLogQwAB);         
   //System.out.println("nmrtrThisLogUwB " + logE*nmrtrThisLogUwB + " thisLogUwA " + logE*thisLogUwA + " nmrtrThisLogQwAB " + logE*nmrtrThisLogQwAB);
   // }
               nmrtrLogSahaMol = nmrtrLogMolSahaFac - nmrtrLogNumB[id] - (nmrtrBoltzFacIAB / temp[0][id]) + (3.0 * temp[1][id] / 2.0) + nmrtrThisLogUwB + thisLogUwA - nmrtrThisLogQwAB;
               nmrtrLogInvSahaMol = -1.0 * nmrtrLogSahaMol;
               //System.out.println("nmrtrLogInvSahaMol " + logE*nmrtrLogInvSahaMol);
               //nmrtrInvSahaMol = Math.exp(nmrtrLogSahaMol);
       //   if (id == 16){
       //       System.out.println("nmrtrLogInvSahaMol " + logE*nmrtrLogInvSahaMol);
       //   }
       //   if (id == 16){
       //        System.out.println("nmrtrBoltzFacIAB " + nmrtrBoltzFacIAB + " nmrtrThisLogUwB " + logE*nmrtrThisLogUwB + " thisLogUwA " + logE*thisLogUwA + " nmrtrThisLogQwAB " + nmrtrThisLogQwAB);   
       //        System.out.println("nmrtrLogSahaMol " + logE*nmrtrLogSahaMol); // + " nmrtrInvSahaMol " + nmrtrInvSahaMol);
       //   }

//Molecular Saha factors:
         for (int iMol = 0; iMol < numMolsB; iMol++){
//System.out.println("iMol " + iMol + " id " + id + " logNumB[iMol][id] " + logE*nmrtrLogNumB[id]);             
   //System.out.println("iMol " + iMol + " thisLogUwB[iMol] " + logE*thisLogUwB[iMol] + " thisLogUwA " + logE*thisLogUwA + " thisLogQwAB " + logE*thisLogQwAB);
             logSahaMol[iMol] = logMolSahaFac[iMol] - logNumB[iMol][id] - (boltzFacIAB[iMol] / temp[0][id]) + (3.0 * temp[1][id] / 2.0) + thisLogUwB[iMol] + thisLogUwA - thisLogQwAB;
//For denominator of ionization fraction, we need *inverse* molecular Saha factors (N_AB/NI):
             logSahaMol[iMol] = -1.0 * logSahaMol[iMol];
             invSahaMol[iMol] = Math.exp(logSahaMol[iMol]);
             //TEST invSahaMol[iMol] = 1.0e-99; //test
     //     if (id == 16){
     //         System.out.println("iMol " + iMol + " boltzFacIAB[iMol] " + boltzFacIAB[iMol] + " thisLogUwB[iMol] " + logE*thisLogUwB[iMol] + " logQwAB[iMol] " + logE*thisLogQwAB + " logNumB[iMol][id] " + logE*logNumB[iMol][id] + " logMolSahaFac[iMol] " + logE*logMolSahaFac[iMol]);   
     //         System.out.println("iMol " + iMol + " logSahaMol " + logE*logSahaMol[iMol] + " invSahaMol[iMol] " + invSahaMol[iMol]);
     //     }
         }

//Compute log of denominator is ionization fraction, f_stage 
        //default initialization 
        //  - ratio of total atomic particles in all ionization stages to number in ground state: 
            double denominator = Math.exp(logGroundRatio[id]); //default initialization - ratio of total atomic particles in all ionization stages to number in ground state 
//molecular contribution
           for (int iMol = 0; iMol < numMolsB; iMol++){
           //  if (id == 16){
           //   System.out.println("invSahaMol[iMol] " + invSahaMol[iMol] + " denominator " + denominator);
           //  }
              denominator = denominator + invSahaMol[iMol];
           }
// 
            double logDenominator = Math.log(denominator); 
           //System.out.println("logGroundRatio[id] " + logE*logGroundRatio[id] + " logDenominator " + logE*logDenominator);
           //  if (id == 16){
           // System.out.println("id " + id + " logGroundRatio " + logGroundRatio[id] + " logDenominator " + logDenominator);
           //  } 
          //if (id == 36){
          //     System.out.println("logDenominator " + logE*logDenominator);
         // }
            //var logDenominator = Math.log( 1.0 + saha21 + (saha32 * saha21) + (saha43 * saha32 * saha21) + (saha54 * saha43 * saha32 * saha21) );

          logMolFrac[id] = nmrtrLogInvSahaMol - logDenominator;
        //  if (id == 16){
        //       System.out.println("id " + id + " logMolFrac[id] " + logE*logMolFrac[id]);
        //  }

            //logNums[id] = logNum[id] + logMolFrac;
        } //id loop

        return logMolFrac;
    }; //end method stagePops    
    
} //end class LevelPops
