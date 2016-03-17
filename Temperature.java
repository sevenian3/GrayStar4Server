/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package graystar3server;


/**
 * Computes the Gray kinetic temperature structure, on the Rosseland optical
 * depth scale T_kin(Tau_Ros) = Teff * (0.75Tau_Ros + Hopf)^0.25
 */
public class Temperature {

    /**
     * 
     * @param numDeps
     * @param teff
     * @param tauRos
     * @return 
     */
    public static double[][] temperature(int numDeps, double teff, double[][] tauRos) {

        //Gray kinetic temeprature structure:
        double[][] temp = new double[2][numDeps];

        double hopf, deltaLogTau, ii;

        for (int i = 0; i < numDeps; i++) {

            // Interpolate approximate Hopf function:
            deltaLogTau = (tauRos[1][i] - tauRos[1][0]) / (tauRos[1][numDeps - 1] - tauRos[1][0]);
            hopf = 0.55 + deltaLogTau * (0.710 - 0.55);

            //temp[1][i] = Math.log(teff) + 
            //             0.25 * Math.log(0.75*tauRos[0][i] + 0.5);
            temp[1][i] = Math.log(teff)
                    + 0.25 * Math.log(0.75 * (tauRos[0][i] + hopf));
            temp[0][i] = Math.exp(temp[1][i]);

        }

        return temp;

    }
    
    
}
