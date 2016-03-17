/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package graystar3server;


import java.text.DecimalFormat;


public class LDC{

// Extract linear monochromatic continuum limb darkening coefficients (LDCs, "epsilon"s) 
// from intensity distribution (needeed for rotational broadening kernel on client side)

// Linear limb darkning law: 
//   Each lambda: I(theta)/I(0) = 1 - epsilon +epsilon*cos(theta)
//   --> Each lambda & theta: "trial" epsilon(theta) = {(I(theta)/I) - 1}/{cos(theta) - 1}
//   --> each lambda: LDC = mean epsilon for all thetas  

    public static double[] ldc(int numLams, double[] lambdaScale, int numThetas, double[][] cosTheta, double[][] contIntens) {

        double ldc[] = new double[numLams];

        double epsilon, meanEpsilon, y;

         for (int iL = 0; iL < numLams; iL++){

            //System.out.println("lambdaScale[iL] " + lambdaScale[iL]);
            meanEpsilon = 0.0; //initialize accumulator

            for (int iT = 1; iT < numThetas; iT++){

               y = contIntens[iL][iT]/contIntens[iL][0];
               epsilon = (y - 1.0) / (cosTheta[1][iT] - 1.0);
               //System.out.println("cosTheta[1][iT] " + cosTheta[1][iT] + " epsilon " + epsilon);
               meanEpsilon += epsilon;  

            } //iT theta loop

            ldc[iL] = meanEpsilon / numThetas;

         } //iL lambda loop  

        return ldc;

 }  //end method ldc 


}  //End of class broaden
