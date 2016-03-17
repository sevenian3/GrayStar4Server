/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package graystar3server;

/**
 *
 * @author Ian
 */
public class TauScale {
    
    // CAUTION: Here tau[1][] is log_10!
    /**
     * 
     * @param numDeps
     * @return 
     */
    public static double[][] tauScale(int numDeps, double log10MinDepth, double log10MaxDepth){
        
    //log_10 Rosseland optical depth scale  
        double tauRos[][] = new double[2][numDeps]; 
        
        // Construct the log ROsseland optical depth scale:
        // Try equal spacing in log depth
        
        double ln10 = Math.log(10.0);
        
//        double log10MinDepth = -4.5;
//        double log10MaxDepth = 1.5;
        
        double logMinDepth = log10MinDepth * ln10;
        double logMaxDepth = log10MaxDepth * ln10;
        
        double deltaLogTau = (logMaxDepth - logMinDepth)/(numDeps - 1.0);
        
        double ii;
        for (int i = 0; i < numDeps; i++){
            
            ii = (double)i;
            tauRos[1][i] = logMinDepth + ii*deltaLogTau;
            tauRos[0][i] = Math.exp(tauRos[1][i]);
            //System.out.println("i: " + i + " absTauDiff[1][i] " + tauRos[1][i] + " tauRos[0][i] " + tauRos[0][i]);
        }
        
        return tauRos;
        
    }
        
    
}
