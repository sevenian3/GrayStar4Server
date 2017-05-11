/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package chromastarserver;

/**
 *
 * @author Ian
 */
public class LamGrid {
 
    /**
     * 
     * @param numLams
     * @param lamSetup
     * @return 
     */
    public static double[] lamgrid(int numLams, double[] lamSetup){
    
     
    double[] lambdaScale = new double[numLams];
    double logLambda;
    
    // Space lambdas logarithmically:
    double logLam1 = Math.log10(lamSetup[0]);
    double logLam2 = Math.log10(lamSetup[1]);
    double delta = ( logLam2 - logLam1 ) / numLams;
    
    double ii;
    for ( int i = 0; i < numLams; i++){
        
        ii = (double) i;
        logLambda = logLam1 + ( ii * delta );
        lambdaScale[i] = Math.pow(10.0, logLambda);
        
        //System.out.println("il " + i + " lambda: " + lambdaScale[i]); //debug
        
    }
        
    return lambdaScale;
    
    }
        
    
}
