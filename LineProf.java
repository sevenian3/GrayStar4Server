/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package chromastarserver;


/**
 * Line profile, phi_lambda(lambda): Assume Voigt function profile - need H(a,v)
 * Assumes CRD, LTE, ??? Input parameters: lam0 - line center wavelength in nm
 * mass - mass of absorbing particle (amu) logGammaCol - log_10(gamma) - base 10
 * logarithmic collisional (pressure) damping co-efficient (s^-1) epsilon -
 * convective microturbulence- non-thermal broadening parameter (km/s) Also
 * needs atmospheric structure information: numDeps WON'T WORK - need observer's
 * frame fixed lambda at all depths: temp structure for depth-dependent thermal
 * line broadening Teff as typical temp instead of above pressure structure,
 * pGas, if scaling gamma
 */
public class LineProf {

//elta function line profile for initiali check of line strength
    public static double[][] delta(double[][] linePoints, double lam0In, 
            int numDeps, double[][] tauRos, double massIn, double xiTIn, double teff) {


        double lam0 = lam0In; // * 1.0E-7; //nm to cm
        double logLam0 = Math.log(lam0);

        double logE = Math.log10(Math.E); // for debug output

        //System.out.println("LineProf: doppler, logDopp: " + doppler + " " + logE*logDopp);

        //Put input parameters into linear cgs units:

        //System.out.println("LINEGRID: Tau1: " + tau1);
        //logA = 2.0 * logLam0 + logGamma - ln4pi - logC - logDopp;
        //a = Math.exp(logA);
        //System.out.println("LINEGRID: logA: " + logE * logA);
        //Set up a half-profile Delta_lambda grid in Doppler width units 
        //from line centre to wing
        int numPoints = 1;
        //System.out.println("LineProf: numPoints: " + numPoints);

        // Return a 2D numPoints X numDeps array of normalized line profile points (phi)
        double[][] lineProf = new double[1][numDeps];

        double c = Useful.c;
        double logC = Useful.logC();
        double logK = Useful.logK();
        double amu = Useful.amu;
        double ln10 = Math.log(10.0);
        double ln2 = Math.log(2.0);
        double lnSqRtPi = 0.5 * Math.log(Math.PI);
        double logTeff = Math.log(teff);
        double xiT = xiTIn * 1.0E5; //km/s to cm/s
        double logMass = Math.log(massIn * amu);  //amu to g
        // Compute depth-independent Doppler width, Delta_lambda_D:
        double doppler, logDopp;
        double logHelp, help; //scratch
        logHelp = ln2 + logK + logTeff - logMass; // M-B dist, square of v_mode
        help = Math.exp(logHelp) + xiT * xiT; // quadratic sum of thermal v and turbulent v
        logHelp = 0.5 * Math.log(help);
        logDopp = logHelp + logLam0 - logC;
        doppler = Math.exp(logDopp);  // cm


        // Line profiel points in Doppler widths - needed for Voigt function, H(a,v):
        double ii;

//        lineProf[0][0] = 0.0; v[0] = 0.0; //Line centre - cannot do logaritmically!
        double  delta, core, logDelta;
        //int il0 = 36;
        //System.out.println("il0 " + il0 + " temp[il] " + temp[0][il0] + " press[il] " + logE*press[1][il0]);
        for (int id = 0; id < numDeps; id++) {

                //if (il <= numCore) {

                    // - Gaussian ONLY - at line centre Lorentzian will diverge!
                    delta = 1.0;
                    //System.out.println("LINEGRID- CORE: core: " + core);

                //System.out.println("LINEGRID: il, v[il]: " + il + " " + v[il] + " lineProf[0][il]: " + lineProf[0][il]);
                //System.out.println("LINEGRID: il, Voigt, H(): " + il + " " + voigt);
                //Convert from H(a,v) in dimensionless Voigt units to physical phi((Delta lambda) profile:
                logDelta = Math.log(delta) + 2.0 * logLam0 - lnSqRtPi - logDopp - logC;

                lineProf[0][id] = Math.exp(logDelta);
                //if (id == 36) {
                //    System.out.println("il " + il + " linePoints " + 1.0e7 * linePoints[0][il] + " id " + id + " lineProf[il][id] " + lineProf[il][id]);
                //}

                //System.out.println("LineProf: il, id, lineProf[il][id]: " + il + " " + id + " " + lineProf[il][id]);

            // if (id == 20) {
            //     for (int il = 0; il < numPoints; il++) {
            //        System.out.format("Voigt: %20.16f   %20.16f%n", linePoints[1][il], logE * Math.log(lineProf[il][id]));
            //    }
            // }
        } //id loop

        return lineProf;

    } //end method delta()


    public static double[][] gauss(double[][] linePoints, double lam0In, 
            int numDeps, double teff, double[][] tauRos, double[][] temp, 
            double[][] tempSun) {

        double c = Useful.c;
        double logC = Useful.logC();
        //double k = Useful.k;
        double logK = Useful.logK();
        //double e = Useful.e;
        //double mE = Useful.mE;

        double lam0 = lam0In; // * 1.0E-7; //nm to cm
        double logLam0 = Math.log(lam0);

        double ln10 = Math.log(10.0);
        double ln2 = Math.log(2.0);
        double ln4pi = Math.log(4.0 * Math.PI);
        double lnSqRtPi = 0.5 * Math.log(Math.PI);
        double sqPi = Math.sqrt(Math.PI);
        //double ln100 = 2.0*Math.log(10.0);

        double logE = Math.log10(Math.E); // for debug output

        double doppler = linePoints[0][1] / linePoints[1][1];
        double logDopp = Math.log(doppler);
        double tiny = 1.0e-19;  //??


        //System.out.println("LineProf: doppler, logDopp: " + doppler + " " + logE*logDopp);

        //Put input parameters into linear cgs units:

        //System.out.println("LINEGRID: Tau1: " + tau1);
        //logA = 2.0 * logLam0 + logGamma - ln4pi - logC - logDopp;
        //a = Math.exp(logA);
        //System.out.println("LINEGRID: logA: " + logE * logA);
        //Set up a half-profile Delta_lambda grid in Doppler width units 
        //from line centre to wing
        int numPoints = linePoints[0].length;
        //System.out.println("LineProf: numPoints: " + numPoints);

        // Return a 2D numPoints X numDeps array of normalized line profile points (phi)
        double[][] lineProf = new double[numPoints][numDeps];

        // Line profiel points in Doppler widths - needed for Voigt function, H(a,v):
        double[] v = new double[numPoints];
        double logV, ii;

//        lineProf[0][0] = 0.0; v[0] = 0.0; //Line centre - cannot do logaritmically!
        double  gauss, core, logGauss;
        gauss = tiny;  //default initialization
        //int il0 = 36;
        //System.out.println("il0 " + il0 + " temp[il] " + temp[0][il0] + " press[il] " + logE*press[1][il0]);
        for (int id = 0; id < numDeps; id++) {


            for (int il = 0; il < numPoints; il++) {

                v[il] = linePoints[1][il];
                //System.out.println("LineProf: il, v[il]: " + il + " " + v[il]);

                //if (il <= numCore) {
                if (v[il] <= 3.5 && v[il] >= -3.5) {

                    // - Gaussian ONLY - at line centre Lorentzian will diverge!
                    core = Math.exp(-1.0 * (v[il] * v[il]));
                    gauss = core;
                    //System.out.println("LINEGRID- CORE: core: " + core);

                } 

                //System.out.println("LINEGRID: il, v[il]: " + il + " " + v[il] + " lineProf[0][il]: " + lineProf[0][il]);
                //System.out.println("LINEGRID: il, Voigt, H(): " + il + " " + voigt);
                //Convert from H(a,v) in dimensionless Voigt units to physical phi((Delta lambda) profile:
                logGauss = Math.log(gauss) + 2.0 * logLam0 - lnSqRtPi - logDopp - logC;

                lineProf[il][id] = Math.exp(logGauss);
                //if (id == 36) {
                //    System.out.println("il " + il + " linePoints " + 1.0e7 * linePoints[0][il] + " id " + id + " lineProf[il][id] " + lineProf[il][id]);
                //}

                //System.out.println("LineProf: il, id, lineProf[il][id]: " + il + " " + id + " " + lineProf[il][id]);
            } // il lambda loop

            // if (id == 20) {
            //     for (int il = 0; il < numPoints; il++) {
            //        System.out.format("Voigt: %20.16f   %20.16f%n", linePoints[1][il], logE * Math.log(lineProf[il][id]));
            //    }
            // }
        } //id loop


        /* Debug
         // Check that line profile is area-normalized (it is NOT, area = 1.4845992503443734E-19!, but IS constant with depth - !?:
         double delta;
         for (int id = 0; id < numDeps; id++) {
         double sum = 0.0;
         for (int il = 1; il < numPoints2; il++) {
         delta = lineProf2[0][il][id] - lineProf2[0][il - 1][id];
         sum = sum + (lineProf2[1][il][id] * delta);
         }
         System.out.println("LineGrid: id, Profile area = " + id + " " + sum );
         }
         */
        return lineProf;

    } //end method gauss()


    public static double[][] voigt(double[][] linePoints, double lam0In, double logAij, double logGammaCol,
            int numDeps, double teff, double[][] tauRos, double[][] temp, double[][] pGas,
            double[][] tempSun, double[][] pGasSun, double[][] hjertComp) {

        double c = Useful.c;
        double logC = Useful.logC();
        //double k = Useful.k;
        double logK = Useful.logK();
        //double e = Useful.e;
        //double mE = Useful.mE;

        double lam0 = lam0In; // * 1.0E-7; //nm to cm
        double logLam0 = Math.log(lam0);

        double ln10 = Math.log(10.0);
        double ln2 = Math.log(2.0);
        double ln4pi = Math.log(4.0 * Math.PI);
        double lnSqRtPi = 0.5 * Math.log(Math.PI);
        double sqRtPi = Math.sqrt(Math.PI);
        double sqPi = Math.sqrt(Math.PI);
        //double ln100 = 2.0*Math.log(10.0);

        double logE = Math.log10(Math.E); // for debug output

        double doppler = linePoints[0][1] / linePoints[1][1];
        double logDopp = Math.log(doppler);
        //System.out.println("LineProf: doppler, logDopp: " + doppler + " " + logE*logDopp);

        //Put input parameters into linear cgs units:
        //double gammaCol = Math.pow(10.0, logGammaCol);
        // Lorentzian broadening:
        // Assumes Van der Waals dominates radiative damping
        // log_10 Gamma_6 for van der Waals damping around Tau_Cont = 1 in Sun 
        //  - p. 57 of Radiative Transfer in Stellar Atmospheres (Rutten)
        double logGammaSun = 9.0 * ln10; // Convert to base e 
        //double logFudge = Math.log(2.5);  // Van der Waals enhancement factor

        int tau1 = ToolBox.tauPoint(numDeps, tauRos, 1.0);
        //System.out.println("tau1 " + tau1);

        //System.out.println("LINEGRID: Tau1: " + tau1);
        //logA = 2.0 * logLam0 + logGamma - ln4pi - logC - logDopp;
        //a = Math.exp(logA);
        //System.out.println("LINEGRID: logA: " + logE * logA);
        //Set up a half-profile Delta_lambda grid in Doppler width units 
        //from line centre to wing
        int numPoints = linePoints[0].length;
        //System.out.println("LineProf: numPoints: " + numPoints);

        // Return a 2D numPoints X numDeps array of normalized line profile points (phi)
        double[][] lineProf = new double[numPoints][numDeps];

        // Line profiel points in Doppler widths - needed for Voigt function, H(a,v):
        double[] v = new double[numPoints];
        double logV, ii;

//        lineProf[0][0] = 0.0; v[0] = 0.0; //Line centre - cannot do logaritmically!
        double gamma, logGamma, a, logA, voigt, core, wing, logWing, logVoigt;
        double Aij = Math.pow(10.0, logAij);
        int il0 = 36;
// For Hjerting function approximation:
    double vSquare, vFourth, vAbs, a2, a3, a4, Hjert0, Hjert1, Hjert2, Hjert3, Hjert4, hjertFn;
        //System.out.println("il0 " + il0 + " temp[il] " + temp[0][il0] + " press[il] " + logE*press[1][il0]);
        for (int id = 0; id < numDeps; id++) {

            //Formula from p. 56 of Radiative Transfer in Stellar Atmospheres (Rutten),
            // logarithmically with respect to solar value:
            logGamma = pGas[1][id] - pGasSun[1][tau1] + 0.7 * (tempSun[1][tau1] - temp[1][id]) + logGammaSun;
          //if ((id%5 == 1) ){
          //  System.out.println("id " + id + " logGamma " + logGamma);
          //}
            //logGamma = logGamma + logFudge + logGammaCol;
            logGamma = logGamma + logGammaCol;
   //Add radiation (natural) broadning:
           gamma = Math.exp(logGamma) + Aij;
           logGamma = Math.log(gamma);
   //
            //if (id == 12){
            //System.out.println("LineGrid: logGamma: " + id + " " + logE * logGamma + " press[1][id] " + press[1][id] + " pressSun[1][tau1] " 
            // + pressSun[1][tau1] + " temp[1][id] " + temp[1][id] + " tempSun[1][tau1] " + tempSun[1][tau1]); 
            //     }

            //Voigt "a" parameter with line centre wavelength:
            logA = 2.0 * logLam0 + logGamma - ln4pi - logC - logDopp;
            a = Math.exp(logA);
            a2 = Math.exp(2.0*logA);
            a3 = Math.exp(3.0*logA);
            a4 = Math.exp(4.0*logA);

            //    if (id == 12) {
            //System.out.println("LineGrid: lam0: " + lam0 + " logGam " + logE * logGamma + " logA " + logE * logA);
            //     }
            //if (id == 30) {
            //    //System.out.println("il   v[il]   iy   y   logNumerator   logDenominator   logInteg ");
            //    System.out.println("voigt:   v   logVoigt: ");
            //}
            for (int il = 0; il < numPoints; il++) {

                v[il] = linePoints[1][il];
                vAbs = Math.abs(v[il]);
                vSquare = vAbs * vAbs;
                vFourth = vSquare * vSquare;
                //System.out.println("LineProf: il, v[il]: " + il + " " + v[il]);

//Approximate Hjerting fn from tabulated expansion coefficients:
// Interpolate in Hjerting table to exact "v" value for each expanstion coefficient:
// Row 0 of Hjerting component table used for tabulated abscissae, Voigt "v" parameter
            if (vAbs <= 12.0){
              //we are within abscissa domain of table
              Hjert0 = ToolBox.interpol(hjertComp[0], hjertComp[1], vAbs);
              Hjert1 = ToolBox.interpol(hjertComp[0], hjertComp[2], vAbs);
              Hjert2 = ToolBox.interpol(hjertComp[0], hjertComp[3], vAbs);
              Hjert3 = ToolBox.interpol(hjertComp[0], hjertComp[4], vAbs);
              Hjert4 = ToolBox.interpol(hjertComp[0], hjertComp[5], vAbs);
           } else {
              // We use the analytic expansion
              Hjert0 = 0.0;
              Hjert1 = (0.56419 / vSquare) + (0.846 / vFourth);
              Hjert2 = 0.0;
              Hjert3 = -0.56 / vFourth;
              Hjert4 = 0.0;
           }
//Approximate Hjerting fn with power expansion in Voigt "a" parameter
// "Observation & Analysis of Stellar Photospeheres" (D. Gray), 3rd Ed., p. 258:
          hjertFn = Hjert0 + a*Hjert1 + a2*Hjert2 + a3*Hjert3 + a4*Hjert4;
          //if ((id%5 == 1) && (il%2 == 0)){
          //    System.out.println("il " + il + " hjertFn " + hjertFn);
          //}
/* Gaussian + Lorentzian approximation:
                //if (il <= numCore) {
                if (v[il] <= 2.0 && v[il] >= -2.0) {

                    // - Gaussian ONLY - at line centre Lorentzian will diverge!
                    core = Math.exp(-1.0 * (v[il] * v[il]));
                    voigt = core;
                    //System.out.println("LINEGRID- CORE: core: " + core);

                } else {

                    logV = Math.log(Math.abs(v[il]));

                    //Gaussian core:
                    core = Math.exp(-1.0 * (v[il] * v[il]));
               // if (id == 12) {
                //    System.out.println("LINEGRID- WING: core: " + core);
                 //   }
                    //Lorentzian wing:
                    logWing = logA - lnSqRtPi - (2.0 * logV);
                    wing = Math.exp(logWing);

                    voigt = core + wing;
               // if (id == 12) {
                //    System.out.println("LINEGRID- WING: wing: " + wing + " logV " + logV);
                 //     }
                } // end else
*/
                //System.out.println("LINEGRID: il, v[il]: " + il + " " + v[il] + " lineProf[0][il]: " + lineProf[0][il]);
                //System.out.println("LINEGRID: il, Voigt, H(): " + il + " " + voigt);
                //Convert from H(a,v) in dimensionless Voigt units to physical phi((Delta lambda) profile:
                //logVoigt = Math.log(voigt) + 2.0 * logLam0 - lnSqRtPi - logDopp - logC;
                //System.out.println("voigt: Before log... id " + id + " il " + il + " hjertFn " + hjertFn);
                //logVoigt = Math.log(hjertFn) + 2.0 * logLam0 - lnSqRtPi - logDopp - logC;
                voigt = hjertFn * Math.pow(lam0, 2) /sqRtPi / doppler / c; 
                //lineProf[il][id] = Math.exp(logVoigt);
                lineProf[il][id] = voigt;
                if (lineProf[il][id] <= 0.0){
                    lineProf[il][id] = -49.0;
                }
               // if (id == 12) {
                //    System.out.println("il " + il + " linePoints " + 1.0e7 * linePoints[0][il] + " id " + id + " lineProf[il][id] " + lineProf[il][id]);
               // }

                //System.out.println("LineProf: il, id, lineProf[il][id]: " + il + " " + id + " " + lineProf[il][id]);
            } // il lambda loop

            // if (id == 20) {
            //     for (int il = 0; il < numPoints; il++) {
            //        System.out.format("Voigt: %20.16f   %20.16f%n", linePoints[1][il], logE * Math.log(lineProf[il][id]));
            //    }
            // }
        } //id loop


        return lineProf;

    } //end method voigt()


    public static double[][] stark(double[][] linePoints, double lam0In, double logAij, double logGammaCol,
            int numDeps, double teff, double[][] tauRos, double[][] temp, double[][] pGas, double[][] Ne,
            double[][] tempSun, double[][] pGasSun, double[][] hjertComp) {

        double c = Useful.c;
        double logC = Useful.logC();
        //double k = Useful.k;
        double logK = Useful.logK();
        //double e = Useful.e;
        //double mE = Useful.mE;

        double lam0 = lam0In; // * 1.0E-7; //nm to cm
        double logLam0 = Math.log(lam0);
        double logLam0A = Math.log(lam0) + 8.0*Math.log(10.0); //cm to A

        double ln10 = Math.log(10.0);
        double ln2 = Math.log(2.0);
        double ln4pi = Math.log(4.0 * Math.PI);
        double lnSqRtPi = 0.5 * Math.log(Math.PI);
        double sqRtPi = Math.sqrt(Math.PI);
        double sqPi = Math.sqrt(Math.PI);
        //double ln100 = 2.0*Math.log(10.0);

        double logE = Math.log10(Math.E); // for debug output

        double doppler = linePoints[0][1] / linePoints[1][1];
        double logDopp = Math.log(doppler);
        //System.out.println("LineProf: doppler, logDopp: " + doppler + " " + logE*logDopp);

        //Put input parameters into linear cgs units:
        //double gammaCol = Math.pow(10.0, logGammaCol);
        // Lorentzian broadening:
        // Assumes Van der Waals dominates radiative damping
        // log_10 Gamma_6 for van der Waals damping around Tau_Cont = 1 in Sun 
        //  - p. 57 of Radiative Transfer in Stellar Atmospheres (Rutten)
        double logGammaSun = 9.0 * ln10; // Convert to base e 
        //double logFudge = Math.log(2.5);  // Van der Waals enhancement factor

        int tau1 = ToolBox.tauPoint(numDeps, tauRos, 1.0);

        //System.out.println("LINEGRID: Tau1: " + tau1);
        //logA = 2.0 * logLam0 + logGamma - ln4pi - logC - logDopp;
        //a = Math.exp(logA);
        //System.out.println("LINEGRID: logA: " + logE * logA);
        //Set up a half-profile Delta_lambda grid in Doppler width units 
        //from line centre to wing
        int numPoints = linePoints[0].length;
        //System.out.println("LineProf: numPoints: " + numPoints);

        // Return a 2D numPoints X numDeps array of normalized line profile points (phi)
        double[][] lineProf = new double[numPoints][numDeps];

        // Line profiel points in Doppler widths - needed for Voigt function, H(a,v):
        double[] v = new double[numPoints];
        double logV, ii;

//        lineProf[0][0] = 0.0; v[0] = 0.0; //Line centre - cannot do logaritmically!
        double gamma, logGamma, a, logA, voigt, core, wing, logWing, logVoigt;
        double Aij = Math.pow(10.0, logAij);
        int il0 = 36;
// For Hjerting function approximation:
    double vSquare, vFourth, vAbs, a2, a3, a4, Hjert0, Hjert1, Hjert2, Hjert3, Hjert4, hjertFn;

   //Parameters for linear Stark broadening:
   //Assymptotic ("far wing") "K" parameters
   //Stehle & Hutcheon, 1999, A&A Supp Ser, 140, 93 and CDS data table
   //Assume K has something to do with "S" and proceed as in Observation and Analysis of
   // Stellar Photosphere, 3rd Ed. (D. Gray), Eq. 11.50,
   //
   double logTuneStark = Math.log(1.0e9); //convert DeltaI K parameters to deltaS STark profile parameters
   double[] logKStark = new double[5];
   logKStark[0] = Math.log(2.56e-03) + logTuneStark;  //Halpha
   logKStark[1] = Math.log(7.06e-03) + logTuneStark;   //Hbeta
   logKStark[2] = Math.log(1.19e-02) + logTuneStark;  //Hgamma
   logKStark[3] = Math.log(1.94e-02) + logTuneStark;  //Hdelta
   logKStark[4] = Math.log(2.95e-02) + logTuneStark;  //Hepsilon
   double thisLogK = logKStark[4]; //default initialization
   //which Balmer line are we?  crude but effective:
   if (lam0In > 650.0e-7){
      thisLogK = logKStark[0];  //Halpha
      //System.out.println("Halpha");
   }
   if ( (lam0In > 480.0e-7) && (lam0In < 650.0e-7) ){
      //System.out.println("Hbeta");
      thisLogK = logKStark[1];  //Hbeta
   }
   if ( (lam0In > 420.0e-7) && (lam0In < 470.0e-7) ){
      //System.out.println("Hgamma");
      thisLogK = logKStark[2];  //Hgamma
   }
   if ( (lam0In > 400.0e-7) && (lam0In < 450.0e-7) ){
      //System.out.println("Hdelta");
      thisLogK = logKStark[3];  //Hdelta
   }
   if ( (lam0In < 400.0e-7) ){
      //System.out.println("Hepsilon");
      thisLogK = logKStark[4];  //Hepsilon
   }

//
   double F0, logF0, lamOverF0, logLamOverF0; //electrostatic field strength (e.s.u.)
   double deltaAlpha, logDeltaAlpha, logStark, logStarkTerm; //reduced wavelength de-tuning parameter (Angstroms/e.s.u.)
   double logF0Fac = Math.log(1.249e-9);
// log wavelength de-tunings in A:
   double logThisPoint, thisPoint;

        //System.out.println("il0 " + il0 + " temp[il] " + temp[0][il0] + " press[il] " + logE*press[1][il0]);
        for (int id = 0; id < numDeps; id++) {

//linear Stark broadening stuff:
            logF0 = logF0Fac + (0.666667)*Ne[1][id];
            logLamOverF0 = logLam0A - logF0;
            lamOverF0 = Math.exp(logLamOverF0);

 //System.out.println("id " + id + " logF0 " + logE*logF0 + " logLamOverF0 " + logE*logLamOverF0 + " lamOverF0 " + lamOverF0);
            //Formula from p. 56 of Radiative Transfer in Stellar Atmospheres (Rutten),
            // logarithmically with respect to solar value:
            logGamma = pGas[1][id] - pGasSun[1][tau1] + 0.7 * (tempSun[1][tau1] - temp[1][id]) + logGammaSun;
            //logGamma = logGamma + logFudge + logGammaCol;
            logGamma = logGamma + logGammaCol;
   //Add radiation (natural) broadning:
           gamma = Math.exp(logGamma) + Aij;
           logGamma = Math.log(gamma);
   //
            //if (id == 12){
            //System.out.println("LineGrid: logGamma: " + id + " " + logE * logGamma + " press[1][id] " + press[1][id] + " pressSun[1][tau1] " 
            // + pressSun[1][tau1] + " temp[1][id] " + temp[1][id] + " tempSun[1][tau1] " + tempSun[1][tau1]); 
            //     }

            //Voigt "a" parameter with line centre wavelength:
            logA = 2.0 * logLam0 + logGamma - ln4pi - logC - logDopp;
            a = Math.exp(logA);
            a2 = Math.exp(2.0*logA);
            a3 = Math.exp(3.0*logA);
            a4 = Math.exp(4.0*logA);

            //    if (id == 12) {
            //System.out.println("LineGrid: lam0: " + lam0 + " logGam " + logE * logGamma + " logA " + logE * logA);
            //     }
            //if (id == 30) {
            //    //System.out.println("il   v[il]   iy   y   logNumerator   logDenominator   logInteg ");
            //    System.out.println("voigt:   v   logVoigt: ");
            //}
            for (int il = 0; il < numPoints; il++) {

                v[il] = linePoints[1][il];
                vAbs = Math.abs(v[il]);
                vSquare = vAbs * vAbs;
                vFourth = vSquare * vSquare;
                //System.out.println("LineProf: il, v[il]: " + il + " " + v[il]);

//Approximate Hjerting fn from tabulated expansion coefficients:
// Interpolate in Hjerting table to exact "v" value for each expanstion coefficient:
// Row 0 of Hjerting component table used for tabulated abscissae, Voigt "v" parameter
            if (vAbs <= 12.0){
              //we are within abscissa domain of table
              Hjert0 = ToolBox.interpol(hjertComp[0], hjertComp[1], vAbs);
              Hjert1 = ToolBox.interpol(hjertComp[0], hjertComp[2], vAbs);
              Hjert2 = ToolBox.interpol(hjertComp[0], hjertComp[3], vAbs);
              Hjert3 = ToolBox.interpol(hjertComp[0], hjertComp[4], vAbs);
              Hjert4 = ToolBox.interpol(hjertComp[0], hjertComp[5], vAbs);
           } else {
              // We use the analytic expansion
              Hjert0 = 0.0;
              Hjert1 = (0.56419 / vSquare) + (0.846 / vFourth);
              Hjert2 = 0.0;
              Hjert3 = -0.56 / vFourth;
              Hjert4 = 0.0;
           }
//Approximate Hjerting fn with power expansion in Voigt "a" parameter
// "Observation & Analysis of Stellar Photospeheres" (D. Gray), 3rd Ed., p. 258:
          hjertFn = Hjert0 + a*Hjert1 + a2*Hjert2 + a3*Hjert3 + a4*Hjert4;
          logStark = -49.0; //re-initialize

            if (vAbs > 2.0) {

               //System.out.println("Adding in Stark wing");

               thisPoint = 1.0e8 * Math.abs(linePoints[0][il]); //cm to A
               logThisPoint = Math.log(thisPoint);
               logDeltaAlpha = logThisPoint - logF0;
               deltaAlpha = Math.exp(logDeltaAlpha);
               logStarkTerm = ( Math.log(lamOverF0 + deltaAlpha) - logLamOverF0 );
               logStark = thisLogK + 0.5*logStarkTerm - 2.5*logDeltaAlpha;

 //System.out.println("il " + il + " logDeltaAlpha " + logE*logDeltaAlpha + " logStarkTerm " + logE*logStarkTerm  + " logStark " + logE*logStark);
               //console.log("il " + il + " logDeltaAlpha " + logE*logDeltaAlpha + " logStarkTerm " + logE*logStarkTerm  + " logStark " + logE*logStark);

               //System.out.println("id " + id + " il " + il + " v[il] " + v[il] 
               //  + " hjertFn " + hjertFn + " Math.exp(logStark) " + Math.exp(logStark));
               //not here! hjertFn = hjertFn + Math.exp(logStark);
            }

                //System.out.println("LINEGRID: il, v[il]: " + il + " " + v[il] + " lineProf[0][il]: " + lineProf[0][il]);
                //System.out.println("LINEGRID: il, Voigt, H(): " + il + " " + voigt);
                //Convert from H(a,v) in dimensionless Voigt units to physical phi((Delta lambda) profile:
                //logVoigt = Math.log(voigt) + 2.0 * logLam0 - lnSqRtPi - logDopp - logC;
                //System.out.println("stark: Before log... id " + id + " il " + il + " hjertFn " + hjertFn);
                //logVoigt = Math.log(hjertFn) - lnSqRtPi - logDopp;
                voigt = hjertFn / sqRtPi / doppler;                
                logStark = logStark - logF0;
                if (vAbs > 2.0){
                //if (id == 24) {
                //   System.out.println("il " + il + " v[il] " + v[il] + " logVoigt " + logE*logVoigt + " logStark " + logE*logStark);
                //}
                   //voigt = Math.exp(logVoigt) + Math.exp(logStark);
                   voigt = voigt + Math.exp(logStark);
                   //logVoigt = Math.log(voigt);
                }
                //logVoigt = logVoigt + 2.0 * logLam0 - logC;
                voigt = voigt * Math.pow(lam0, 2) / c;
                //lineProf[il][id] = Math.exp(logVoigt);
                lineProf[il][id] = voigt;
                if (lineProf[il][id] <= 0.0){
                    lineProf[il][id] = -49.0;
                }
                //if (id == 24) {
                //    System.out.println("lam0In " + lam0In);
                //    System.out.println("il " + il + " linePoints " + 1.0e7 * linePoints[0][il] + " id " + id + " lineProf[il][id] " + lineProf[il][id]);
                //}

                //System.out.println("LineProf: il, id, lineProf[il][id]: " + il + " " + id + " " + lineProf[il][id]);
            } // il lambda loop

            // if (id == 20) {
            //     for (int il = 0; il < numPoints; il++) {
            //        System.out.format("Voigt: %20.16f   %20.16f%n", linePoints[1][il], logE * Math.log(lineProf[il][id]));
            //    }
            // }
        } //id loop


        return lineProf;

    } //end method stark()


    // Make line source function:
    // Equivalenth two-level atom (ETLA) approx
    //CAUTION: input lambda in nm
    public static double[] lineSource(int numDeps, double[][] tau, double[][] temp, double lambda) {

        double[] lineSource = new double[numDeps];

        //thermal photon creation/destruction probability
        double epsilon = 0.01; //should decrease with depth??

        //This is an artifact of jayBinner's original purpose:
        double grayLevel = 1.0;

        //int iLam0 = numLams / 2; //+/- 1 deltaLambda
        //double lam0 = linePoints[0][iLam0];  //line centre lambda in cm - not needed:
        //double lamStart = lambda - 0.1; // nm
        //double lamStop = lambda + 0.1; // nm
        //double lamRange = (lamStop - lamStart); // * 1.0e-7; // line width in cm
        //System.out.println("lamStart " + lamStart + " lamStop " + lamStop + " lamRange " + lamRange);
        double[] jayLambda = new double[numDeps];
        double[][] BLambda = new double[2][numDeps];
        double linSrc;

        // Dress up Blambda to look like what jayBinner expects:
        for (int i = 0; i < numDeps; i++) {
            //Planck.planck return log(B_lambda):
            BLambda[0][i] = Math.exp(Planck.planck(temp[0][i], lambda));
            BLambda[1][i] = 1.0;  //supposed to be dB/dT, but not needed. 
        }

        //CAUTION: planckBin Row 0 is linear lambda-integrated B_lambda; Row 1 is same for dB_lambda/dT
        //planckBin = MulGrayTCorr.planckBinner(numDeps, temp, lamStart, lamStop);
        jayLambda = MulGrayTCorr.jayBinner(numDeps, tau, temp, BLambda, grayLevel);
        //To begin with, coherent scattering - we're not computing line profile-weighted average Js and Bs
        for (int i = 0; i < numDeps; i++) {

            //planckBin[0][i] = planckBin[0][i] / lamRange;  //line average
            //jayBin[i] = jayBin[i];  
            linSrc = (1.0 - epsilon) * jayLambda[i] + epsilon * BLambda[0][i];
            lineSource[i] = Math.log(linSrc);
        }

        return lineSource;
    }
    
    
}
