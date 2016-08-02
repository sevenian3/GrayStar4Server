/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package graystar3server;


 /**
 * Compute Rosseland mean extinction coefficient (cm^2/g) structure by scaling
 * from Sun
 *
 */
public class Kappas {

    /**
     *
     * @param numDeps
     * @param zScale
     * @param teff
     * @param teffSun
     * @param logg
     * @param loggSun
     * @return
     */
    //** Input parameter mode: valid values are 0 or 1
    // If mode = 0: We don't yet know the mass density, rho & rhoSun, yet - the rhos passed in are meaningless and rhos must be faked
    // If mode = 1" We already have a previous in situ calculation of mass density, rho & rhoSun - passed in as parameters
    //public static double[][] kappas(int numDeps, double kappaScale,  double teff, double teffSun, double logg, double loggSun) {
    public static double[][] kappas(int mode, int numDeps, double[][] rho, double[][] rhoRef, double[][] kappaRosSun, double zScale, double logg, double loggSun, double teff, double teffSun, double radius,
            double massX, double massZ, double[][] tauRos, double[][] temp, double[][] tempRef, double[][] logNumsH3, double[][] logNumsH2) {


        double[][] kappa = new double[2][numDeps];

        double hotT = 6000.0;  //hotter than this in K and we use hot star formula

        double logRadiusSun = 0.0;  //solar units
        double massZSun = 0.02;

        double dilute, rhoStarFake, rhoSunFake;
        double logRhoStarFake = 0.0; //enforced initialization
        double logRhoSunFake = 0.0;  //enforced initialization

        if (mode == 0) {
            //We don't yet know rho & rhoSun - the rhos passed in are meaningless - fake up rhos:
            // Approximate mass density in atmosphere by scaling with logg and radius, then diluting:
            dilute = 5.0e-5; //tuned to give rho ~10^-1 g/cm^-3 in Sun's atmosphere
            logRhoStarFake = Math.log(3.0 / 4.0 / Math.PI) - Useful.logGConst() + logg - Math.log(Useful.rSun * radius);
            rhoStarFake = dilute * Math.exp(logRhoStarFake);

            // Do the same for Sun for consistency
            logRhoSunFake = Math.log(3.0 / 4.0 / Math.PI) - Useful.logGConst() + loggSun - Useful.logRSun();
            rhoSunFake = dilute * Math.exp(logRhoSunFake);
        }

        //System.out.println("rhoSunFake: " + rhoSunFake + " rhoStarFake: " + rhoStarFake);
        // System.out.println("logHelp " + logHelp + " help " + help + " kappaScale out " + kappaScale); //debug
        /*
         double[][] kappaRosSun = new double[2][numDeps];

        
         double minLog10KappaRosSun = -3.5;
         double maxLog10KappaRosSun = 2.0;

         double ln10 = Math.log(10.0);
         double minLogKappaRosSun = minLog10KappaRosSun * ln10;
         double maxLogKappaRosSun = maxLog10KappaRosSun * ln10;

         double deltaKappa = (maxLogKappaRosSun - minLogKappaRosSun) / numDeps;

         double ii;

         //Sun:
         for (int i = 0; i < numDeps; i++) {

         ii = (double) i;
         kappaRosSun[1][i] = minLogKappaRosSun + ii * deltaKappa;
         kappaRosSun[0][i] = Math.exp(kappaRosSun[1][i]);

         }
         */
        //Star:
        double numerator = 1.0;  //enforced initialization
        double denominator = 1.0;  //enforced initialization

        double logHelp, help, reScale, logNH3, logNH2;
        //double kappaOld, logKappaOld;
        //reScale = 1.0 * kappaScale;
        reScale = 1.0;
        //System.out.println("kappaScale: " + kappaScale);
        for (int i = 0; i < numDeps; i++) {

            logNH3 = logNumsH3[2][i];
            logNH2 = logNumsH2[2][i];
            //System.out.println("i " + i);
            if (mode == 0) {
                numerator = kappaFac(numDeps, hotT, logRhoStarFake, temp[1][i], massX, massZ, logNH3, logNH2);
                denominator = kappaFac(numDeps, hotT, logRhoSunFake, tempRef[1][i], massX, massZSun, logNH3, logNH2);
            } else if (mode == 1) {
                numerator = kappaFac(numDeps, hotT, rho[1][i], temp[1][i], massX, massZ, logNH3, logNH2);
                //numerator = kappaFac(numDeps, hotT, rhoSun[1][i], temp[1][i], massX, massZ);
                denominator = kappaFac(numDeps, hotT, rhoRef[1][i], tempRef[1][i], massX, massZSun, logNH3, logNH2);
                //System.out.println("hotT " + hotT + " rho[1][i] " + rho[1][i] + "  temp[1][i] " +  temp[1][i] + " massX " + massX + " massZ " + massZ); 
            }
            //System.out.println("i " + i + " kappaRosSun[0][i] " + kappaRosSun[0][i]);
            kappa[0][i] = reScale * kappaRosSun[0][i] * (numerator / denominator);
            kappa[1][i] = Math.log(kappa[0][i]);
            //System.out.println("i " + i + " kappa[1][i] " + kappa[1][i]); 
            //System.out.println("i " + i + " numerator " + numerator + " denominator " + denominator + " temp[1][i] " + temp[1][i] 
            // + " rho[1][i] " + rho[1][i] + " tempRef[1][i] " + tempRef[1][i] + " rhoRef[1][i] " + rhoRef[1][i] + " kappa[1][i] " + kappa[1][i]);
            //System.out.println("kappa factor: " + (numerator / denominator) + " numerator: " + numerator + " denominator: " + denominator);
        }

        return kappa;

    }

    public static double kappaFac(int numDeps, double hotT, double logRho, double logTemp, double massX, double massZ, double logNH3, double logNH2) {

        double logE = Math.log10(Math.E); // for debug output

        double kapFac = 0.0;

        // These values tuned to produce total kappas of right order of magnitude for Sun
        double constbf = 2.34e19; // b-f pre-factor cm^2/g
        double constff = 3.68e15; // f-f pre-factor cm^2/g
        double constes = 0.2;  // Thomson scattering from free electron pre-factor cm^2/g
        double constHm = 3.9e-31 / 0.02; // H^- b-f pre-factor with 1/0.02 factor from Z term cm^2/g
        //should b-b opacity rho-and T- scaling track b-f oapcity?
        double sigmabf = 1.31e-15;  // Hydrogen b-f x-section, cm^-2
        double refLambda = 500.0; //reference lambda in nm for HI bf opacity formula

        // Paschen continuum H I opacity from n=3:
        double n3 = 3.0;
        double lamJump3 = 820.4; //Paschen jump in nm
        double logHbfFac3 = Math.log(sigmabf) - 5.0 * n3 + 3.0 * (Math.log(lamJump3) - Math.log(refLambda));
        //double hbfFac = Math.pow(lamJump / refLambda, 3.0) / Math.pow(n, 5);
        // Paschen continuum H I opacity from n=3:
        double n2 = 2.0;
        double lamJump2 = 364.0; //Paschen jump in nm
        double logHbfFac2 = Math.log(sigmabf) - 5.0 * n2 + 3.0 * (Math.log(lamJump2) - Math.log(refLambda));

        double logRhoT35, rhoT35;
        double logHmTerm, HmTerm, HmTermHot, HmHotFac;
        double logHIbfTerm3, logHIbfTerm2, HIbfTerm;
        double logHotT = Math.log(hotT);
        double thisTemp = Math.exp(logTemp);

        logRhoT35 = logRho - 3.5 * logTemp;
        rhoT35 = Math.exp(logRhoT35);

        logHmTerm = Math.log(constHm) + Math.log(massZ) + 0.5 * logRho + 9.0 * logTemp; // H^- b-f term
        HmTerm = Math.exp(logHmTerm);
        double midRange = 1500.0;  //H^- opacity ramp-down T range

        if ((thisTemp > 3000.0) && (thisTemp < 6000.0)
                && (logE * logRho > -13.0) && (logE * logRho < -8.0)
                && (massZ > 0.001) && (massZ < 0.03)) {
            // Caroll & Ostlie 2nd Ed. Ch. 9 - (1+X) factors do NOT cancel out when we divide kappa_Star/kappa_Sun
//            // Cool stars: kappa_bf + kappa_ff + kappa_H^- + kappa_es
            kapFac = rhoT35 * (1.0 + massX) * (constbf * massZ + constff * (1.0 - massZ)) + HmTerm + (1.0 + massX) * constes;
            // Cool stars: kappa_ff + kappa_H^- + kappa_es
            //kapFac = rhoT35 * (1.0 + massX) * (constff * (1.0 - massZ)) + HmTerm + (1.0 + massX) * constes;
            //kapFac =  HmTerm + (1.0 + massX) * constes;
            //System.out.println("Cool T: " + Math.exp(logTemp)
            //        + " b-f: " + logE * Math.log(rhoT35 * (1.0 + massX) * (constbf * massZ))
            //        + " f-f: " + logE * Math.log(rhoT35 * (1.0 + massX) * (constff * (1.0 - massZ)))
            //        + " H^-: " + logE * logHmTerm + " es: " + logE * Math.log((1.0 + massX) * constes)
            //        + " kapFac " + kapFac);
        } else {
            kapFac = rhoT35 * (1.0 + massX) * (constbf * massZ + constff * (1.0 - massZ)) + (1.0 + massX) * constes;
        }

        logHIbfTerm3 = logHbfFac3 + logNH3 - logRho;  // cm^2/g //neglects stimualted emission (for now);
        logHIbfTerm2 = logHbfFac2 + logNH2 - logRho;  // cm^2/g //neglects stimualted emission (for now)
        HIbfTerm = Math.exp(logHIbfTerm3) + Math.exp(logHIbfTerm2);

        if ((thisTemp >= hotT) && (thisTemp < (hotT + midRange))) {
            HmHotFac = 1.0 - ((thisTemp - hotT) / midRange);
            HmTermHot = HmTerm * Math.sqrt(HmHotFac);
            //System.out.println("HmHotFac: " + HmHotFac);
            kapFac = rhoT35 * (constbf * massZ + constff * (1.0 - massZ)) + constes + HIbfTerm; // + HmTermHot;
            //System.out.println("Middle T: " + Math.exp(logTemp) + " b-f: " + rhoT35 * (constbf * massZ)
            //        + " f-f: " + rhoT35 * (constff * (1.0 - massZ))
            //        + " es: " + constes + " HIbf: " + HIbfTerm + " HmTermHot: " + HmTermHot + " kapFac " + kapFac);
        }

        if (thisTemp >= (hotT + midRange)) {
            // Caroll & Ostlie 2nd Ed. Ch. 9 - (1+X) factors in every term will cancel out when we divide kappa_Star/kappa_Sun
            // Hot stars: kappa_bf + kappa_ff + kappa_es
            //kapFac = rhoT35 * (constbf * massZ + constff * (1.0 - massZ)) + constes; // + HIbfTerm;
            kapFac = rhoT35 * (constbf * massZ + constff * (1.0 - massZ)) + constes + HIbfTerm;
            //System.out.println("Hot T: " + Math.exp(logTemp) + " b-f: " + rhoT35 * (constbf * massZ)
            //        + " f-f: " + rhoT35 * (constff * (1.0 - massZ))
            //       + " es: " + constes + " HIbf: " + HIbfTerm + " kapFac " + kapFac);
        }

        return kapFac;

    }
  
  public static double[][] kappas2(int numDeps, double[][] pe, double zScale, double[][] temp, double[][] rho,
                                   int numLams, double[] lambdas, double logAHe,
                                   double[] logNH1, double[] logNH2, double[] logNHe1, double[] logNHe2, double[][] Ne,
                                   double teff, double logKapFudge){


//
//  *** CAUTION:
//
//  This return's "kappa" as defined by Gray 3rd Ed. - cm^2 per *relelvant particle* where the "releveant particle"
//  depends on *which* kappa

     double log10E = Math.log10(Math.E); //needed for g_ff
     double logLog10E = Math.log(log10E);
     double logE10 = Math.log(10.0);
     double[] logNH = new double[numDeps]; //Total H particle number density cm^-3
     double logPH1, logPH2, logPHe1, logPHe2;
     for (int i=0; i<numDeps; i++){
         logNH[i] = Math.exp(logNH1[i]) + Math.exp(logNH2[i]);
         logNH[i] = Math.log(logNH[i]);

        //System.out.println("i " + i + " logNH1 " + log10E*logNH1[i] + " logNH2 " + log10E*logNH2[i] 
    //+ " logNHe1 " + log10E*logNHe1[i] + " logNHe2 " + log10E*logNHe2[i] + " logPe " + log10E*pe[1][i]);
     //   logPH1 = logNH1[i] + temp[1][i] + Useful.logK();
      //  logPH2 = logNH2[i] + temp[1][i] + Useful.logK();
       // logPHe1 = logNHe1[i] + temp[1][i] + Useful.logK();
       // logPHe2 = logNHe2[i] + temp[1][i] + Useful.logK();
        //System.out.println("i " + i + " logPH1 " + log10E*logPH1 + " logPH2 " + log10E*logPH2 
    //+ " logPHe1 " + log10E*logPHe1 + " logPHe2 " + log10E*logPHe2 + " logPe " + log10E*pe[1][i]);
     }

     double[][] logKappa = new double[numLams][numDeps];
     double kappa; //helper
     double stimEm; //temperature- and wavelength-dependent stimulated emission correction  
     double stimHelp, logStimEm;
 
     double ii; //useful for converting integer loop counter, i, to float
//
//
//Input data and variable declarations:
//
//
// H I b-f & f-f
     double chiIH = 13.598433;  //eV
     double Rydberg = 1.0968e-2;  // "R" in nm^-1
     //Generate threshold wavelengths and b-f Gaunt (g_bf) helper factors up to n=10:
     double n; //principle quantum number of Bohr atom E-level
     int numHlevs = 10;
     double[] invThresh = new double[numHlevs]; //also serves as g_bf helper factor
     double[] threshLambs = new double[numHlevs];
     double[] chiHlev = new double[numHlevs];
     double logChiHlev;
     for (int i = 0; i < numHlevs; i++){
        n = 1.0 + (double) i;
        invThresh[i] = Rydberg / n / n; //nm^-1; also serves as g_bf helper factor 
        threshLambs[i] = 1.0 / invThresh[i]; //nm
        logChiHlev = Useful.logH() + Useful.logC() + Math.log(invThresh[i]) + 7.0*logE10; // ergs
        chiHlev[i] = Math.exp(logChiHlev - Useful.logEv()); //eV
        chiHlev[i] = chiIH - chiHlev[i];
//        System.out.println("i " + i + " n " + n + " invThresh " + invThresh[i] + " threshLambs[i] " + threshLambs[i] + " chiHlev " + chiHlev[i]);
     } 

     double logGauntPrefac = Math.log(0.3456) - 0.333333*Math.log(Rydberg);

     // ****  Caution: this will require lamba in A!:
     double a0 = 1.0449e-26;  //if lambda in A 
     double logA0 = Math.log(a0);
// Boltzmann const "k" in eV/K - needed for "theta"
     double logKeV = Useful.logK() - Useful.logEv(); 

     //g_bf Gaunt factor - depends on lower E-level, n:
     double[] loggbf = new double[numHlevs];

     //initialize quantities that depend on lowest E-level contributing to opacity at current wavelength:
     for (int iThresh = 0; iThresh < numHlevs; iThresh++){
        loggbf[iThresh] = 0.0;
     }
     double logGauntHelp, gauntHelp; 
     double gbf, gbfHelp, loggbfHelp;
     double gff, gffHelp, loggffHelp, logffHelp, loggff;
     double help, logHelp3;
     double chiLambda, logChiLambda;
     double bfTerm, logbfTerm, bfSum, logKapH1bf, logKapH1ff;
 
//initial defaults:
   gbf = 1.0;
   gff = 1.0;
   loggff = 0.0;
 
     double logChiFac = Math.log(1.2398e3); // eV per lambda, for lambda in nm

// Needed for kappa_ff: 
  double ffBracket; 
     logffHelp = logLog10E - Math.log(chiIH) - Math.log(2.0);
     //logHelp = logffHelp - Math.log(2.0);

//
//Hminus:
//
// H^- b-f
//This is for the sixth order polynomial fit to the cross-section's wavelength dependence
  int numHmTerms = 7;
  double[] logAHm = new double[numHmTerms];
  double[] signAHm = new double[numHmTerms];
 
  double aHmbf = 4.158e-10;
  //double logAHmbf = Math.log(aHmbf);
  //Is the factor of 10^-18cm^2 from the polynomial fit to alpha_Hmbf missing in Eq. 8.12 on p. 156 of Gray 3rd Ed??
  double logAHmbf = Math.log(aHmbf) - 18.0*logE10;
  double alphaHmbf, logAlphaHmbf, logTermHmbf, logKapHmbf; 

  //Computing each polynomial term logarithmically
     logAHm[0] = Math.log(1.99654);
     signAHm[0] = 1.0;
     logAHm[1] = Math.log(1.18267e-5);
     signAHm[1] = -1.0;
     logAHm[2] = Math.log(2.64243e-6);
     signAHm[2] = 1.0;
     logAHm[3] = Math.log(4.40524e-10);
     signAHm[3] = -1.0;
     logAHm[4] = Math.log(3.23992e-14);
     signAHm[4] = 1.0;
     logAHm[5] = Math.log(1.39568e-18);
     signAHm[5] = -1.0;
     logAHm[6] = Math.log(2.78701e-23);
     signAHm[6] = 1.0;
     alphaHmbf = Math.exp(logAHm[0]); //initialize accumulator

// H^- f-f:

  double logAHmff = -26.0*logE10;
  int numHmffTerms = 5;
  double fPoly, logKapHmff, logLambdaAFac; 
    double[][] fHmTerms = new double[3][numHmffTerms];
    double[] fHm = new double[3];
    fHmTerms[0][0] = -2.2763;
    fHmTerms[0][1] = -1.6850;
    fHmTerms[0][2] = 0.76661;
    fHmTerms[0][3] = -0.053346;
    fHmTerms[0][4] = 0.0;
    fHmTerms[1][0] = 15.2827;
    fHmTerms[1][1] = -9.2846;
    fHmTerms[1][2] = 1.99381;
    fHmTerms[1][3] = -0.142631;
    fHmTerms[1][4] = 0.0;
    fHmTerms[2][0] = -197.789;
    fHmTerms[2][1] = 190.266;
    fHmTerms[2][2] = -67.9775;
    fHmTerms[2][3] = 10.6913;
    fHmTerms[2][4] = -0.625151;

//
//H_2^+ molecular opacity - cool stars
// scasles with proton density (H^+)
//This is for the third order polynomial fit to the "sigma_l(lambda)" and "U_l(lambda)"
//terms in the cross-section
     int numH2pTerms = 4;
     double[] sigmaH2pTerm = new double[numH2pTerms];
     double[] UH2pTerm = new double[numH2pTerms];
     double logSigmaH2p, sigmaH2p, UH2p, logKapH2p;  
     double aH2p = 2.51e-42;
     double logAH2p = Math.log(aH2p);
       sigmaH2pTerm[0] = -1040.54;
       sigmaH2pTerm[1] = 1345.71;
       sigmaH2pTerm[2] = -547.628;
       sigmaH2pTerm[3] = 71.9684;
       //UH2pTerm[0] = 54.0532;
       //UH2pTerm[1] = -32.713;
       //UH2pTerm[2] = 6.6699;
       //UH2pTerm[3] = -0.4574;
      //Reverse signs on U_1 polynomial expansion co-efficients - Dave Gray private communcation 
      //based on Bates (1952)
       UH2pTerm[0] = -54.0532;
       UH2pTerm[1] = 32.713;
       UH2pTerm[2] = -6.6699;
       UH2pTerm[3] = 0.4574;
 

// He I b-f & ff: 
       double totalH1Kap, logTotalH1Kap, helpHe, logKapHe;

//
//He^- f-f
  
  double AHe = Math.exp(logAHe); 
     double logKapHemff, nHe, logNHe, thisTerm, thisLogTerm, alphaHemff, log10AlphaHemff;

// Gray does not have this pre-factor, but PHOENIX seems to and without it
// the He opacity is about 10^26 too high!:
  double logAHemff = -26.0*logE10;

     int numHemffTerms = 5;
     double[] logC0HemffTerm = new double[numHemffTerms];
     double[] logC1HemffTerm = new double[numHemffTerms];
     double[] logC2HemffTerm = new double[numHemffTerms];
     double[] logC3HemffTerm = new double[numHemffTerms];
     double[] signC0HemffTerm = new double[numHemffTerms];
     double[] signC1HemffTerm = new double[numHemffTerms];
     double[] signC2HemffTerm = new double[numHemffTerms];
     double[] signC3HemffTerm = new double[numHemffTerms];

//we'll be evaluating the polynominal in theta logarithmically by adding logarithmic terms - 
     logC0HemffTerm[0] = Math.log(9.66736); 
     signC0HemffTerm[0] = 1.0;
     logC0HemffTerm[1] = Math.log(71.76242); 
     signC0HemffTerm[1] = -1.0;
     logC0HemffTerm[2] = Math.log(105.29576); 
     signC0HemffTerm[2] = 1.0;
     logC0HemffTerm[3] = Math.log(56.49259); 
     signC0HemffTerm[3] = -1.0;
     logC0HemffTerm[4] = Math.log(10.69206); 
     signC0HemffTerm[4] = 1.0;
     logC1HemffTerm[0] = Math.log(10.50614); 
     signC1HemffTerm[0] = -1.0;
     logC1HemffTerm[1] = Math.log(48.28802); 
     signC1HemffTerm[1] = 1.0;
     logC1HemffTerm[2] = Math.log(70.43363); 
     signC1HemffTerm[2] = -1.0;
     logC1HemffTerm[3] = Math.log(37.80099); 
     signC1HemffTerm[3] = 1.0;
     logC1HemffTerm[4] = Math.log(7.15445);
     signC1HemffTerm[4] = -1.0;
     logC2HemffTerm[0] = Math.log(2.74020); 
     signC2HemffTerm[0] = 1.0;
     logC2HemffTerm[1] = Math.log(10.62144); 
     signC2HemffTerm[1] = -1.0;
     logC2HemffTerm[2] = Math.log(15.50518); 
     signC2HemffTerm[2] = 1.0;
     logC2HemffTerm[3] = Math.log(8.33845); 
     signC2HemffTerm[3] = -1.0;
     logC2HemffTerm[4] = Math.log(1.57960);
     signC2HemffTerm[4] = 1.0;
     logC3HemffTerm[0] = Math.log(0.19923); 
     signC3HemffTerm[0] = -1.0;
     logC3HemffTerm[1] = Math.log(0.77485); 
     signC3HemffTerm[1] = 1.0;
     logC3HemffTerm[2] = Math.log(1.13200); 
     signC3HemffTerm[2] = -1.0;
     logC3HemffTerm[3] = Math.log(0.60994); 
     signC3HemffTerm[3] = 1.0;
     logC3HemffTerm[4] = Math.log(0.11564);
     signC3HemffTerm[4] = -1.0;
     //initialize accumulators:
     double[] cHemff = new double[4];
     cHemff[0] = signC0HemffTerm[0] * Math.exp(logC0HemffTerm[0]);   
     cHemff[1] = signC1HemffTerm[0] * Math.exp(logC1HemffTerm[0]);   
     cHemff[2] = signC2HemffTerm[0] * Math.exp(logC2HemffTerm[0]);   
     cHemff[3] = signC3HemffTerm[0] * Math.exp(logC3HemffTerm[0]);   
//
//Should the polynomial expansion for the Cs by in 10g10Theta??  No! Doesn't help:
     //double[] C0HemffTerm = new double[numHemffTerms];
     //double[] C1HemffTerm = new double[numHemffTerms];
     //double[] C2HemffTerm = new double[numHemffTerms];
     //double[] C3HemffTerm = new double[numHemffTerms];
//
     //C0HemffTerm[0] = 9.66736; 
     //C0HemffTerm[1] = -71.76242; 
     //C0HemffTerm[2] = 105.29576; 
     //C0HemffTerm[3] = -56.49259; 
     //C0HemffTerm[4] = 10.69206; 
     //C1HemffTerm[0] = -10.50614; 
     //C1HemffTerm[1] = 48.28802; 
     //C1HemffTerm[2] = -70.43363; 
     //C1HemffTerm[3] = 37.80099; 
     //C1HemffTerm[4] = -7.15445;
     //C2HemffTerm[0] = 2.74020; 
     //C2HemffTerm[1] = -10.62144; 
     //C2HemffTerm[2] = 15.50518; 
     //C2HemffTerm[3] = -8.33845; 
     //C2HemffTerm[4] = 1.57960;
     //C3HemffTerm[0] = -0.19923; 
     //C3HemffTerm[1] = 0.77485; 
     //C3HemffTerm[2] = -1.13200; 
     //C3HemffTerm[3] = 0.60994; 
     //C3HemffTerm[4] = -0.11564;
    //initialize accumulators:
    // double[] cHemff = new double[4];
    // cHemff[0] = C0HemffTerm[0];   
    // cHemff[1] = C1HemffTerm[0];   
    // cHemff[2] = C2HemffTerm[0];   
    // cHemff[3] = C3HemffTerm[0];   

//
// electron (e^-1) scattering (Thomson scattering)

    double kapE, logKapE;
    double alphaE = 0.6648e-24; //cm^2/e^-1
    double logAlphaE = Math.log(0.6648e-24);
  

//Universal:
//
     double theta, logTheta, log10Theta, log10ThetaFac;
     double logLambda, lambdaA, logLambdaA, log10LambdaA, lambdanm, logLambdanm;
//Okay - here we go:
//Make the wavelength loop the outer loop - lots of depth-independnet lambda-dependent quantities:
//
//
//
//  **** START WAVELENGTH LOOP iLam
//
//
//
     for (int iLam = 0; iLam < numLams; iLam++){
 //
 //Re-initialize all accumulators to be on safe side:
           kappa = 0.0;
           logKapH1bf = -99.0; 
           logKapH1ff = -99.0;
           logKapHmbf = -99.0; 
           logKapHmff = -99.0;
           logKapH2p = -99.0;
           logKapHe = -99.0;
           logKapHemff = -99.0;
           logKapE = -99.0;
 //
//*** CAUTION: lambda MUST be in nm here for consistency with Rydbeg 
        logLambda = Math.log(lambdas[iLam]);  //log cm
        lambdanm = 1.0e7 * lambdas[iLam];
        logLambdanm = Math.log(lambdanm);
        lambdaA = 1.0e8 * lambdas[iLam]; //Angstroms
        logLambdaA = Math.log(lambdaA);
        log10LambdaA = log10E * logLambdaA;

        logChiLambda = logChiFac - logLambdanm;
        chiLambda = Math.exp(logChiLambda);   //eV

// Needed for both g_bf AND g_ff: 
        logGauntHelp = logGauntPrefac - 0.333333*logLambdanm; //lambda in nm here
        gauntHelp = Math.exp(logGauntHelp);

  //            if (iLam == 142){
   // System.out.println("lambdaA " + lambdaA);
   //         }

//HI b-f depth independent factors:
//Start at largest threshold wavelength and break out of loop when next threshold lambda is less than current lambda:
        for (int iThresh = numHlevs-1; iThresh >= 0; iThresh--){
           if (threshLambs[iThresh] < lambdanm){
              break;
           }
           if (lambdanm <= threshLambs[iThresh]){
           //this E-level contributes
              loggbfHelp = logLambdanm + Math.log(invThresh[iThresh]); //lambda in nm here; invThresh here as R/n^2
              gbfHelp = Math.exp(loggbfHelp);
              gbf = 1.0 - (gauntHelp * (gbfHelp - 0.5));
//              if (iLam == 1){
//    System.out.println("iThresh " + iThresh + " threshLambs " + threshLambs[iThresh] +  " gbf " + gbf);
//              }
              loggbf[iThresh] = Math.log(gbf);
           }
        }  //end iThresh loop 

//HI f-f depth independent factors:
        //logChi = logLog10E + logLambdanm - logChiFac; //lambda in nm here
        //chi = Math.exp(logChi);
        loggffHelp = logLog10E - logChiLambda;

//
//
//
//  ******  Start depth loop iTau ******
//
//
//
//
        for (int iTau = 0; iTau < numDeps; iTau++){
//
 //Re-initialize all accumulators to be on safe side:
           kappa = 0.0;
           logKapH1bf = -99.0; 
           logKapH1ff = -99.0;
           logKapHmbf = -99.0; 
           logKapHmff = -99.0;
           logKapH2p = -99.0;
           logKapHe = -99.0;
           logKapHemff = -99.0;
           logKapE = -99.0;
//
//
//if (iTau == 36 && iLam == 142){
//    System.out.println("lambdanm[142] " + lambdanm + " temp[0][iTau=36] " + temp[0][iTau=36]);
// }
//This is "theta" ~ 5040/T:
           logTheta = logLog10E - logKeV - temp[1][iTau];
           log10Theta = log10E * logTheta;
           theta = Math.exp(logTheta);
           //System.out.println("theta " + theta + " logTheta " + logTheta);

// temperature- and wavelength-dependent stimulated emission coefficient:
           stimHelp = -1.0 * theta * chiLambda * logE10;
           stimEm = 1.0 - Math.exp(stimHelp); 
           logStimEm = Math.log(stimEm);
 //          if (iTau == 36 && iLam == 142){
 //   System.out.println("stimEm " + stimEm);
 //}


           ffBracket = Math.exp(loggffHelp - logTheta) + 0.5; 
           gff = 1.0 + (gauntHelp*ffBracket);


//if (iTau == 36 && iLam == 1){
//    System.out.println("gff " + gff);
// }
           loggff = Math.log(gff);

//H I b-f:
//Start at largest threshold wavelength and break out of loop when next threshold lambda is less than current lambda:
           bfSum = 0.0; //initialize accumulator
           logHelp3 = logA0 + 3.0*logLambdaA; //lambda in A here
           for (int iThresh = numHlevs-1; iThresh >= 0; iThresh--){
              if (threshLambs[iThresh] < lambdanm){
                 break;
              }
              n = 1.0 + (double) iThresh; 
              if (lambdanm <= threshLambs[iThresh]){
                //this E-level contributes
                logbfTerm = loggbf[iThresh] - 3.0*Math.log(n); 
                logbfTerm = logbfTerm - (theta*chiHlev[iThresh])*logE10; 
                bfSum = bfSum + Math.exp(logbfTerm);
//if (iTau == 36 && iLam == 142){
  //System.out.println("lambdanm " + lambdanm + " iThresh " + iThresh + " threshLambs[iThresh] " + threshLambs[iThresh]);
  //System.out.println("loggbf " + loggbf[iThresh] + " theta " + theta + " chiHlev " + chiHlev[iThresh]);
  //System.out.println("bfSum " + bfSum + " logbfTerm " + logbfTerm);
//  }
              }
           }  //end iThresh loop 

// cm^2 per *neutral* H atom
           logKapH1bf = logHelp3 + Math.log(bfSum); 

//Stimulated emission correction
           logKapH1bf = logKapH1bf + logStimEm;

//Add it in to total - opacity per neutral HI atom, so multiply by logNH1 
// This is now linear opacity in cm^-1
           logKapH1bf = logKapH1bf + logNH1[iTau];
////Nasty fix to make Balmer lines show up in A0 stars!
//     if (teff > 8000){
//          logKapH1bf = logKapH1bf - logE10*1.5;
//     }
                     kappa = Math.exp(logKapH1bf); 
  //System.out.println("HIbf " + log10E*logKapH1bf);
//if (iTau == 36 && iLam == 142){
//           System.out.println("lambdaA " + lambdaA + " logKapH1bf " + log10E*(logKapH1bf)); //-rho[1][iTau]));
//}
//H I f-f:
// cm^2 per *neutral* H atom
           logKapH1ff = logHelp3 + loggff + logffHelp - logTheta - (theta*chiIH)*logE10;

//Stimulated emission correction
           logKapH1ff = logKapH1ff + logStimEm;
//Add it in to total - opacity per neutral HI atom, so multiply by logNH1 
// This is now linear opacity in cm^-1
           logKapH1ff = logKapH1ff + logNH1[iTau];
////Nasty fix to make Balmer lines show up in A0 stars!
//     if (teff > 8000){
//          logKapH1ff = logKapH1ff - logE10*1.5;
//     }
                  kappa = kappa + Math.exp(logKapH1ff); 
       //System.out.println("HIff " + log10E*logKapH1ff);

//if (iTau == 36 && iLam == 142){
//           System.out.println("logKapH1ff " + log10E*(logKapH1ff)); //-rho[1][iTau]));
//}

//
//Hminus:
//
// H^- b-f:
//if (iTau == 36 && iLam == 142){
 // System.out.println("temp " + temp[0][iTau] + " lambdanm " + lambdanm);
 // }
          logKapHmbf =  -99.0; //initialize default
          //if ( (temp[0][iTau] > 2500.0) && (temp[0][iTau] < 10000.0) ){
          //if ( (temp[0][iTau] > 2500.0) && (temp[0][iTau] < 8000.0) ){
          //Try lowering lower Teff limit to avoid oapcity collapse in outer layers of late-type stars
          if ( (temp[0][iTau] > 1000.0) && (temp[0][iTau] < 10000.0) ){
             if ((lambdanm > 225.0) && (lambdanm < 1500.0) ){ //nm 
//if (iTau == 36 && iLam == 142){
 //              System.out.println("In KapHmbf condition...");
//}
                ii = 0.0;
                alphaHmbf = signAHm[0]*Math.exp(logAHm[0]); //initialize accumulator
                for (int i = 1; i < numHmTerms; i++){
                   ii = (double) i;
//if (iTau == 36 && iLam == 142){
//                   System.out.println("ii " + ii);
//}
                   logTermHmbf = logAHm[i] + ii*logLambdaA; 
                   alphaHmbf = alphaHmbf + signAHm[i]*Math.exp(logTermHmbf);  
//if (iTau == 36 && iLam == 142){
//                  System.out.println("logTermHmbf " + log10E*logTermHmbf + " i " + i + " logAHm " + log10E*logAHm[i]); 
//}
                }
                logAlphaHmbf = Math.log(alphaHmbf);
// cm^2 per neutral H atom
                logKapHmbf = logAHmbf + logAlphaHmbf + pe[1][iTau] + 2.5*logTheta + (0.754*theta)*logE10; 
//Stimulated emission correction
           logKapHmbf = logKapHmbf + logStimEm;
//if (iTau == 36 && iLam == 142){
//  System.out.println("alphaHmbf " + alphaHmbf);
//  System.out.println("logKapHmbf " + log10E*logKapHmbf + " logAHmbf " + log10E*logAHmbf + " logAlphaHmbf " + log10E*logAlphaHmbf);
//  }

//Add it in to total - opacity per neutral HI atom, so multiply by logNH1 
// This is now linear opacity in cm^-1
           logKapHmbf = logKapHmbf + logNH1[iTau];
                  kappa = kappa + Math.exp(logKapHmbf); 
       //System.out.println("Hmbf " + log10E*logKapHmbf);
//if (iTau == 36 && iLam == 142){
//           System.out.println("logKapHmbf " + log10E*(logKapHmbf)); //-rho[1][iTau]));
//}
             } //wavelength condition
          } // temperature condition

// H^- f-f:
          logKapHmff = -99.0; //initialize default
          //if ( (temp[0][iTau] > 2500.0) && (temp[0][iTau] < 10000.0) ){
          //Try lowering lower Teff limit to avoid oapcity collapse in outer layers of late-type stars
          //if ( (temp[0][iTau] > 2500.0) && (temp[0][iTau] < 8000.0) ){
          if ( (temp[0][iTau] > 1000.0) && (temp[0][iTau] < 10000.0) ){
             if ((lambdanm > 260.0) && (lambdanm < 11390.0) ){ //nm 
                 //construct "f_n" polynomials in log(lambda)
                 for (int j = 0; j < 3; j++){
                     fHm[j] = fHmTerms[j][0];  //initialize accumulators
                 }    
                 ii = 0.0;               
                 for (int i = 1; i < numHmffTerms; i++){
                     ii = (double) i;
                     logLambdaAFac = Math.pow(log10LambdaA, ii);
                     for (int j = 0; j < 3; j++){
                        fHm[j] = fHm[j] + (fHmTerms[j][i]*logLambdaAFac);    
                     } // i
                  } // j
// 
     fPoly = fHm[0] + fHm[1]*log10Theta + fHm[2]*log10Theta*log10Theta;
// In cm^2 per neutral H atom:
// Stimulated emission alreadya ccounted for
          logKapHmff = logAHmff + pe[1][iTau] + fPoly*logE10;

//Add it in to total - opacity per neutral HI atom, so multiply by logNH1 
// This is now linear opacity in cm^-1
           logKapHmff = logKapHmff + logNH1[iTau];
                  kappa = kappa + Math.exp(logKapHmff); 
       //System.out.println("Hmff " + log10E*logKapHmff);
//if (iTau == 36 && iLam == 142){
//           System.out.println("logKapHmff " + log10E*(logKapHmff)); //-rho[1][iTau]));
//}
             } //wavelength condition
          } // temperature condition


// H^+_2:
//
       logKapH2p = -99.0; //initialize default 
       if ( temp[0][iTau] < 4000.0 ){
          if ((lambdanm > 380.0) && (lambdanm < 2500.0) ){ //nm 
             sigmaH2p = sigmaH2pTerm[0]; //initialize accumulator
             UH2p = UH2pTerm[0]; //initialize accumulator
             ii = 0.0;
             for (int i = 1; i < numH2pTerms; i++){
                ii = (double) i; 
                logLambdaAFac = Math.pow(log10LambdaA, ii);
                // kapH2p way too large with lambda in A - try cm:  No! - leads to negative logs
                //logLambdaAFac = Math.pow(logLambda, ii);
                sigmaH2p = sigmaH2p +  sigmaH2pTerm[i] * logLambdaAFac; 
                UH2p = UH2p +  UH2pTerm[i] * logLambdaAFac; 
             }
             logSigmaH2p = Math.log(sigmaH2p);
             logKapH2p = logAH2p + logSigmaH2p - (UH2p*theta)*logE10 + logNH2[iTau]; 
//Stimulated emission correction
           logKapH2p = logKapH2p + logStimEm;

//Add it in to total - opacity per neutral HI atom, so multiply by logNH1 
// This is now linear opacity in cm^-1
           logKapH2p = logKapH2p + logNH1[iTau];
           kappa = kappa + Math.exp(logKapH2p); 
  //System.out.println("H2p " + log10E*logKapH2p);
//if (iTau == 16 && iLam == 142){
           //System.out.println("logKapH2p " + log10E*(logKapH2p-rho[1][iTau]) + " logAH2p " + log10E*logAH2p
// + " logSigmaH2p " + log10E*logSigmaH2p + " (UH2p*theta)*logE10 " + log10E*((UH2p*theta)*logE10) + " logNH2[iTau] " + log10E*logNH2[iTau]);
//}
          } //wavelength condition
       } // temperature condition


//He I 
//
//  HeI b-f + f-f
  //Scale sum of He b-f and f-f with sum of HI b-f and f-f 

//wavelength condition comes from requirement that lower E level be greater than n=2 (edge at 22.78 nm)
       logKapHe = -99.0; //default intialization
       if ( temp[0][iTau] > 10000.0 ){
          if (lambdanm > 22.8){ //nm  
             totalH1Kap = Math.exp(logKapH1bf) + Math.exp(logKapH1ff);
             logTotalH1Kap = Math.log(totalH1Kap); 
             helpHe = Useful.k * temp[0][iTau];
// cm^2 per neutral H atom (after all, it's scaled wrt kappHI
// Stimulated emission already accounted for
//
//  *** CAUTION: Is this *really* the right thing to do???
//    - we're re-scaling the final H I kappa in cm^2/g corrected for stim em, NOT the raw cross section
             logKapHe = Math.log(4.0) - (10.92 / helpHe) + logTotalH1Kap;

//Add it in to total - opacity per neutral HI atom, so multiply by logNH1 
// This is now linear opacity in cm^-1
           logKapHe = logKapHe + logNH1[iTau];
                kappa = kappa + Math.exp(logKapHe); 
       //System.out.println("He " + log10E*logKapHe);
//if (iTau == 36 && iLam == 142){
//           System.out.println("logKapHe " + log10E*(logKapHe)); //-rho[1][iTau]));
//}
          } //wavelength condition
       } // temperature condition


//
//He^- f-f:
       logKapHemff = -99.0; //default initialization
       if ( (theta > 0.5) && (theta < 2.0) ){
          if ((lambdanm > 500.0) && (lambdanm < 15000.0) ){ //nm 

// initialize accumulators:
     cHemff[0] = signC0HemffTerm[0]*Math.exp(logC0HemffTerm[0]);   
     //System.out.println("C0HemffTerm " + signC0HemffTerm[0]*Math.exp(logC0HemffTerm[0]));
     cHemff[1] = signC1HemffTerm[0]*Math.exp(logC1HemffTerm[0]);   
     //System.out.println("C1HemffTerm " + signC1HemffTerm[0]*Math.exp(logC1HemffTerm[0]));
     cHemff[2] = signC2HemffTerm[0]*Math.exp(logC2HemffTerm[0]);   
     //System.out.println("C2HemffTerm " + signC2HemffTerm[0]*Math.exp(logC2HemffTerm[0]));
     cHemff[3] = signC3HemffTerm[0]*Math.exp(logC3HemffTerm[0]);   
     //System.out.println("C3HemffTerm " + signC3HemffTerm[0]*Math.exp(logC3HemffTerm[0]));
//build the theta polynomial coefficients
     ii = 0.0;
     for (int i = 1; i < numHemffTerms; i++){
        ii = (double) i;
        thisLogTerm = ii*logTheta + logC0HemffTerm[i]; 
        cHemff[0] = cHemff[0] + signC0HemffTerm[i]*Math.exp(thisLogTerm); 
        //System.out.println("i " + i + " ii " + ii + " C0HemffTerm " + signC0HemffTerm[i]*Math.exp(logC0HemffTerm[i]));
        thisLogTerm = ii*logTheta + logC1HemffTerm[i]; 
        cHemff[1] = cHemff[1] + signC1HemffTerm[i]*Math.exp(thisLogTerm); 
        //System.out.println("i " + i + " ii " + ii + " C1HemffTerm " + signC1HemffTerm[i]*Math.exp(logC1HemffTerm[i]));
        thisLogTerm = ii*logTheta + logC2HemffTerm[i]; 
        cHemff[2] = cHemff[2] + signC2HemffTerm[i]*Math.exp(thisLogTerm); 
        //System.out.println("i " + i + " ii " + ii + " C2HemffTerm " + signC2HemffTerm[i]*Math.exp(logC2HemffTerm[i]));
        thisLogTerm = ii*logTheta + logC3HemffTerm[i]; 
        cHemff[3] = cHemff[3] + signC3HemffTerm[i]*Math.exp(thisLogTerm); 
        //System.out.println("i " + i + " ii " + ii + " C3HemffTerm " + signC3HemffTerm[i]*Math.exp(logC3HemffTerm[i]));
     }
    //// Should polynomial expansion for Cs be in log10Theta??: - No! Doesn't help
    // initialize accumulators:
    // cHemff[0] = C0HemffTerm[0];   
    // cHemff[1] = C1HemffTerm[0];   
    // cHemff[2] = C2HemffTerm[0];   
    // cHemff[3] = C3HemffTerm[0];   
    // ii = 0.0;
    // for (int i = 1; i < numHemffTerms; i++){
    //    ii = (double) i;
    //    log10ThetaFac = Math.pow(log10Theta, ii);
    //    thisTerm = log10ThetaFac * C0HemffTerm[i]; 
    //    cHemff[0] = cHemff[0] + thisTerm; 
    //    thisTerm = log10ThetaFac * C1HemffTerm[i]; 
    //    cHemff[1] = cHemff[1] + thisTerm; 
    //    thisTerm = log10ThetaFac * C2HemffTerm[i]; 
    //    cHemff[2] = cHemff[2] + thisTerm; 
    //    thisTerm = log10ThetaFac * C3HemffTerm[i]; 
    //    cHemff[3] = cHemff[3] + thisTerm; 
    // }
     
//Build polynomial in logLambda for alpha(He^1_ff):
       log10AlphaHemff = cHemff[0]; //initialize accumulation
       //System.out.println("cHemff[0] " + cHemff[0]);
       ii = 0.0;
       for (int i = 1; i <= 3; i++){
          //System.out.println("i " + i + " cHemff[i] " + cHemff[i]);
          ii = (double) i;
          thisTerm = cHemff[i] * Math.pow(log10LambdaA, ii);
          log10AlphaHemff = log10AlphaHemff + thisTerm; 
       } 
       //System.out.println("log10AlphaHemff " + log10AlphaHemff);
       alphaHemff = Math.pow(10.0, log10AlphaHemff); //gives infinite alphas!
       // alphaHemff = log10AlphaHemff; // ?????!!!!!
       //System.out.println("alphaHemff " + alphaHemff);

// Note: this is the extinction coefficient per *Hydrogen* particle (NOT He- particle!)
       //nHe = Math.exp(logNHe1[iTau]) + Math.exp(logNHe2[iTau]);
       //logNHe = Math.log(nHe);
       //logKapHemff = Math.log(alphaHemff) + Math.log(AHe) + pe[1][iTau] + logNHe1[iTau] - logNHe;
       logKapHemff = logAHemff + Math.log(alphaHemff) + pe[1][iTau] + logNHe1[iTau] - logNH[iTau];

//Stimulated emission already accounted for
//Add it in to total - opacity per H particle, so multiply by logNH 
// This is now linear opacity in cm^-1
           logKapHemff = logKapHemff + logNH[iTau];
                   kappa = kappa + Math.exp(logKapHemff); 
       //System.out.println("Hemff " + log10E*logKapHemff);
//if (iTau == 36 && iLam == 155){
//if (iLam == 155){
//           System.out.println("logKapHemff " + log10E*(logKapHemff)); //-rho[1][iTau]));
//}
 
             } //wavelength condition
          } // temperature condition

//
// electron (e^-1) scattering (Thomson scattering)

//coefficient per *"hydrogen atom"* (NOT per e^-!!) (neutral or total H??):
    logKapE = logAlphaE + Ne[1][iTau] - logNH[iTau];

//Stimulated emission not relevent 
//Add it in to total - opacity per H particle, so multiply by logNH 
// This is now linear opacity in cm^-1
    //I know, we're adding logNH right back in after subtracting it off, but this is for dlarity and consistency for now... :
           logKapE = logKapE + logNH[iTau];   
               kappa = kappa + Math.exp(logKapE); 
       //System.out.println("E " + log10E*logKapE);
//if (iTau == 36 && iLam == 142){
//           System.out.println("logKapE " + log10E*(logKapE)); //-rho[1][iTau]));
//}

//Metal b-f
//Fig. 8.6 Gray 3rd Ed.
//

//
// This is now linear opacity in cm^-1
// Divide by mass density
// This is now mass extinction in cm^2/g
//
   logKappa[iLam][iTau] = Math.log(kappa) - rho[1][iTau];
// Fudge is in cm^2/g:  Converto to natural log:
   double logEKapFudge = logE10 * logKapFudge;
   logKappa[iLam][iTau] = logKappa[iLam][iTau] + logEKapFudge;
//if (iTau == 36 && iLam == 142){
      //System.out.println(" " + log10E*(logKappa[iLam][iTau]+rho[1][iTau]));
//}

//

        } // close iTau depth loop
//
     } //close iLam wavelength loop 

      return logKappa;

  } //end method kappas2

  public static double[][] kapRos(int numDeps, int numLams, double[] lambdas, double[][] logKappa, double[][] temp){

     double[][] kappaRos = new double[2][numDeps];

     double numerator, denominator, deltaLam, logdBdTau, logNumerator, logDenominator;
     double logTerm, logDeltaLam, logInvKap, logInvKapRos;

     for (int iTau = 0; iTau < numDeps; iTau++){

        numerator = 0.0; //initialize accumulator
        denominator = 0.0;

        for (int iLam = 1; iLam < numLams; iLam++){
          
           deltaLam = lambdas[iLam] - lambdas[iLam-1];  //lambda in cm
           logDeltaLam = Math.log(deltaLam);

           logInvKap = -1.0 * logKappa[iLam][iTau];
           logdBdTau = Planck.dBdT(temp[0][iTau], lambdas[iLam]);
           logTerm = logdBdTau + logDeltaLam;
           denominator = denominator + Math.exp(logTerm); 
           logTerm = logTerm + logInvKap;
           numerator = numerator + Math.exp(logTerm);

        }

        logNumerator = Math.log(numerator);
        logDenominator = Math.log(denominator);
        logInvKapRos = logNumerator - logDenominator; 
        kappaRos[1][iTau] = -1.0 * logInvKapRos; //logarithmic
        kappaRos[0][iTau] = Math.exp(kappaRos[1][iTau]);

     }

     return kappaRos;

  } //end method kapRos  
    
}
