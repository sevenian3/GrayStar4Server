/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package graystar3server;

/**
 * Solves the equation of state (EOS) for the mass density (rho) given total
 * pressure from HSE solution, for a mixture of ideal gas particles and photons
 *
 * Need to assume a mean molecular weight structure, mu(Tau)
 *
 */
public class State {

    /**
     *
     * @param numDeps
     * @param temp
     * @param press
     * @return
     */
    public static double[][] massDensity(int numDeps, double[][] temp, double[][] press, double[] mmw, double kappaScale) {

        double logE = Math.log10(Math.E); // for debug output

        //press is a 4 x numDeps array:
        // rows 0 & 1 are linear and log *gas* pressure, respectively
        // rows 2 & 3 are linear and log *radiation* pressure
        // double c = 9.9989E+10; // light speed in vaccuum in cm/s
        // double sigma = 5.670373E-5;   //Stefan-Boltzmann constant ergs/s/cm^2/K^4   
        //Row 0 of mmwNe is Mean molecular weight in amu
        //NO! double[][] mmwNe = mmwNeFn(numDeps, temp, kappaScale);
        double k = Useful.k;
        double logK = Useful.logK();
        double amu = Useful.amu;
        double logAmu = Useful.logAmu();
        double logMuAmu;

        //System.out.println("STATE: logK " + logK + " logMuAmu " + logMuAmu);
        double[][] rho = new double[2][numDeps];

        // Declare scatch variables:
        // double logPrad, pRad, pGas, logPgas;
        for (int i = 0; i < numDeps; i++) {

            logMuAmu = Math.log(mmw[i]) + logAmu;

            // Compute LTE bolometric radiation contribution to total HSE pressure
            //logPrad = radFac + 4.0*temp[1][i] ;
            //pRad = Math.exp(logPrad);
            //pGas = press[0][i] - pRad;
            //logPgas = Math.log(pGas);
            rho[1][i] = press[1][i] - temp[1][i] + (logMuAmu - logK);
            rho[0][i] = Math.exp(rho[1][i]);
            //System.out.println("i " + i + " press[1] " + logE * press[1][i] + " mmw[i] " + mmw[i] + " rho " + logE * rho[1][i]);
            //System.out.println("temp " + temp[0][i] + " rho " + rho[0][i]);
        }

        return rho;

    }

    public static double[] mmwFn(int numDeps, double[][] temp, double kappaScale) {

        // mean molecular weight in amu 
        double[] mmw = new double[numDeps];
        double logMu, logMuN, logMuI, logTempN, logTempI;

        // Carrol & Ostlie 2nd Ed., p. 293: mu_N = 1.3, mu_I = 0.62
        logMuN = Math.log(1.3);
        logMuI = Math.log(0.62);
        logTempN = Math.log(4000.0); // Teff in K for fully neutral gas?
        logTempI = Math.log(10000.0); // Teff in K for *Hydrogen* fully ionized?

        //System.out.println("temp   logNe   mu");
        for (int id = 0; id < numDeps; id++) {

            //Give mu the same temperature dependence as 1/Ne between the fully neutral and fully ionized limits?? - Not yet! 
            if (temp[1][id] < logTempN) {
                mmw[id] = Math.exp(logMuN);
            } else if ((temp[1][id] > logTempN) && (temp[1][id] < logTempI)) {
                logMu = logMuN + ((temp[1][id] - logTempN) / (logTempI - logTempN)) * (logMuI - logMuN);
                //Mean molecular weight in amu
                mmw[id] = Math.exp(logMu);
            } else {
                mmw[id] = Math.exp(logMuI);
            }
        }
        return mmw;
    }

    public static double[][] NeFn(int numDeps, double[][] temp, double[][] NeDfg2, double kappaScale) {

        double[][] Ne = new double[2][numDeps];

        double logE = Math.log10(Math.E); // for debug output

        // Expression for cgs logNe for *hot* *MS* stars from *MKS* logPe expression from D. Turner (private communication):
        // *** We need to do better than this...
        for (int id = 0; id < numDeps; id++) {
            if (temp[0][id] < 7300.0) {
                Ne[0][id] = NeDfg2[0][id] * kappaScale;
                Ne[1][id] = Math.log(Ne[0][id]);
            } else {
                Ne[1][id] = -4.5 - Useful.logK() + 0.5 * temp[1][id] - 6.0 + Math.log(kappaScale); // last term converts m^-3 to cm^-3  
                Ne[0][id] = Math.exp(Ne[1][id]);
                //System.out.format("%12.8f   %12.8f   %12.8f%n", temp[0][id], logE * mmwNe[1][id], mmwNe[0][id]);
            }

            //System.out.format("%12.8f   %12.8f%n", temp[0][id], logE * Ne[1][id]);
        }

        return Ne;

    }
    
    
}
