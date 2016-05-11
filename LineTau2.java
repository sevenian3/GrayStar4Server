/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package graystar3server;


/**
 * Method 2: Compute monochromatic tau scales by re-scaling from Tau_Ross -
 * PROBLEM: the approximate, scaled solar kappa_Ros values are not consistent
 * with the prescribed Tau_Ros values HOWEVER: this method removes dependence on
 * the flaky depth scale calculation and vulnerability to low rho value at the
 * surface Compute the monochromatic optical depth scale, tau_lambda, at each
 * lambda across the line profile Also computes line centre Continuum
 * monochromatic optical depth scale for continuum rectification And stores it
 * in array element numPoints+1 This may differ from the prescribed Tau_Ross
 * because kappa_Ross was NOT computed consistently with it Inputs: lineGrid
 * -only neded for row 0 - wavelengths logKappaL - monochromatic line extinction
 * co-efficients kappa - we need the background continuum extinction
 * co-efficients, too!
 *
 * PROBLEM: line kappaL values converted to mass extinction by division by rho()
 * are not consistent with fake Kramer's Law based scaling of kappa_Ros with g.
 * Try leaving kappaLs as linear extinctions and converting the scaled kappa_Ros
 * back to linear units with solar rho()
 */
public class LineTau2 {

    /**
     *
     * @param numDeps
     * @param lineMaster
     * @param logKappaL
     * @param kappa
     * @param tauRos
     * @return
     */
    public static double[][] tauLambda(int numDeps, int numMaster, double[][] logKappaL,
            double[][] kappa, double[][] tauRos, int numLams, double[] lambdaScale, double[] masterLams) {

        //No monochromatic optical depth can be less than the Rosseland optical depth,
        // so prevent zero tau_lambda values by setting each tau_lambda(lambda) at the 
        //top of the atmosphere to the tau_Ross value at the top 
        // - prevents trying to take a log of zero!
        double logE = Math.log10(Math.E); // for debug output
        double minTauL = tauRos[0][0];
        double minLogTauL = tauRos[1][0];

        //int numPoints = linePoints[0].length;
        // returns numPoints+1 x numDeps array: the numPoints+1st row holds the line centre continuum tau scale
        double[][] logTauL = new double[numMaster][numDeps];

        double tau1, tau2, delta, tauL,
                integ, logKapRat, logKappaC, lastLogKapRat;

//Interpolate continuum opacity onto onto line-blanketed opacity lambda array:
//
        double[] kappaC = new double[numLams];
        double[] kappaC2 = new double[numMaster];
        double[][] kappa2 = new double[numMaster][numDeps];
        for (int id = 0; id < numDeps; id++) {
           for (int il = 0; il < numLams; il++) {
              kappaC[il] = kappa[il][id];
           }
           kappaC2 = ToolBox.interpolV(kappaC, lambdaScale, masterLams); 
           for (int il = 0; il < numMaster; il++){ 
              kappa2[il][id] = kappaC2[il];
           }
        }

        for (int il = 0; il < numMaster; il++) {

            tau1 = minTauL; //initialize accumulator
            logTauL[il][0] = minLogTauL; // Set upper boundary TauL           

            //System.out.println("LineTau: minTauL: " + minTauL);
            //Trapezoid method: first integrand:
            //total extinction co-efficient
            // Convert kappa_Ros to cm^-1 for consistency with kappaL:
            //logKappaC = kappa[1][0] + rhoSun[1][0]; // + logg;
            //WRONG!  kappa is now wavelength dependent!  
            //logKappaC = kappa[1][0];

            //delta = tauRos[0][1] - tauRos[0][0];
            //logKapRat = logKappaL[il][0] - kappa[1][0];
            lastLogKapRat = logKappaL[il][0] - kappa2[il][0];

            //tau2 = tau1 + ((Math.exp(logKapRat) + 1.0) * delta);
            //opacity being handed in is now total oapcity: line plux continuum:
            //tau2 = tau1 + (Math.exp(logKapRat) * delta);
            //logTauL[il][1] = Math.log(tau2);
            //tau1 = tau2;
            for (int id = 1; id < numDeps; id++) {

                // To test: continue with Euler's method:
                // Convert kappa_Ros to cm^-1 for consistency with kappaL:
                //logKappaC = kappa[1][id] + rhoSun[1][id]; // - logg;
                //logKappaC = kappa[1][id];
                delta = tauRos[0][id] - tauRos[0][id - 1];
                //logKapRat = logKappaL[il][id] - kappa[1][id];
                //logKapRat = logKappaL[il][id] - logKappaC;
                logKapRat = logKappaL[il][id] - kappa2[il][id];


                //tau2 = tau1 + ((Math.exp(logKapRat) + 1.0) * delta);
                //opacity being handed in is now total oppcity: line plux continuum:
                //trapezoid rule:
                integ = 0.5 * (Math.exp(logKapRat) + Math.exp(lastLogKapRat));
                tau2 = tau1 + (integ * delta);

                logTauL[il][id] = Math.log(tau2);
                tau1 = tau2;
                lastLogKapRat = logKapRat;

            //if (id == 12) {
            // System.out.println("il " + il + " id " + id + " logTauL[il][id] " + logE * logTauL[il][id]);
            // System.out.println("tauLambda: il, id, masterLams, logKappaL, logKappa2, logKapRat, logTauL : "
            //     + il + " " + id + " " + masterLams[il] + " " + logE*logKappaL[il][id] + " " + logE*kappa2[il][id] + " " + logE*logKapRat + " " + logE*logTauL[il][id] );
            //}

            } //id loop

        } //il loop

        /* No!
        //This is probably superfluous here, but let's do it this way for consistency with code that was
        // dependent on Method 1:
        //Now compute the monochromatic line centre continuum optical depth scale and store it in an numPoints+1st column of
        // logTauL array:
        for (int id = 0; id < numDeps; id++) {

            logTauL[numPoints - 1][id] = tauRos[1][id];

        }
*/
        return logTauL;

    }
    
    
}
