
/*
 *  * To change this license header, choose License Headers in Project Properties.
 *   * To change this template file, choose Tools | Templates
 *    * and open the template in the editor.
 *     */

package chromastarserver;

/* Rayleigh scattering opacity routines taken from Moog (moogjul2014/, MOOGJUL2014.tar)
Chris Sneden (Universtiy of Texas at Austin)  and collaborators
http://www.as.utexas.edu/~chris/moog.html
//From Moog source file Opacscat.f
*/

public class KappasRaylGas{

    public static double[] masterTemp = {130, 500, 3000, 8000, 10000};

/*
c******************************************************************************
c  The subroutines needed to calculate the opacities from scattering by
c  H I, H2, He I, are in this file.  These are from ATLAS9.
c******************************************************************************
*/

      public static double[][] masterRayl(int numDeps, int numLams, double[][] temp, double[] lambdaScale, double[][][] stagePops, String[] gsName, int gsFirstMol, double[][] molPops){

    //System.out.println("masterRayl called...");

//From Moog source file Opacitymetals.f
// From how values such as aC1[] are used in Moog file Opacit.f to compute the total opacity
// and then the optical depth scale, I infer that they are extinction coefficients 
// in cm^-1 
//
// There does not seem to be any correction for stimulated emission 

         double logE = Math.log10(Math.E);

         double[][] masterRScat = new double[numLams][numDeps];

         double[] logUH1 = new double[5];
         double[] logUHe1 = new double[5];

         double logStatWH1 = 0.0;
         double logStatWHe1 = 0.0;

         double theta = 1.0;
         String species = "";
         double[] logGroundPopsH1  = new double[numDeps];
         double[] logGroundPopsHe1  = new double[numDeps];
         double[] logH2Pops  = new double[numDeps];
//
// H I: Z=1 --> iZ=0:
         double[] sigH1 = new double[numDeps];
// He I: Z=2 --> iZ=1:
         double[] sigHe1 = new double[numDeps];

         species = "HI";
         logUH1 = PartitionFn.getPartFn2(species);
         species = "HeI";
         logUHe1 = PartitionFn.getPartFn2(species);

         double[] sigH2 = new double[numDeps];

    //#Find index of H2 in molPops array
    int iH2;
    for (iH2 = 0; iH2 < gsName.length; iH2++){
        if (gsName[iH2].trim() == "H2"){
            break;
        }
    }

    //System.out.println("iH2 "+ iH2+ " iH2-gsFirstMol "+ (iH2-gsFirstMol));

         //System.out.println("iD     PopsH1     PopsHe1");
         for (int iD = 0; iD < numDeps; iD++){

// NEW Interpolation with temperature for new partition function: lburns
            double thisTemp = temp[0][iD];

        logStatWH1 = ToolBox.interpol(KappasRaylGas.masterTemp, logUH1, thisTemp);
        logStatWHe1 = ToolBox.interpol(KappasRaylGas.masterTemp, logUHe1, thisTemp);

/*
            } else if (thisTemp > 130 && thisTemp <= 500){
                logStatWH1 = logUH1[1] * (thisTemp - 130)/(500 - 130)
                           + logUH1[0] * (500 - thisTemp)/(500 - 130);
                logStatWHe1 = logUHe1[1] * (thisTemp - 130)/(500 - 130)
                            + logUHe1[0] * (500 - thisTemp)/(500 - 130);
            } else if (thisTemp > 500 && thisTemp <= 3000){
                logStatWH1 = logUH1[2] * (thisTemp - 500)/(3000 - 500)
                           + logUH1[1] * (3000 - thisTemp)/(3000 - 500);
                logStatWHe1 = logUHe1[2] * (thisTemp - 500)/(3000 - 500)
                            + logUHe1[1] * (3000 - thisTemp)/(3000 - 500);
            } else if (thisTemp > 3000 && thisTemp <= 8000){
                logStatWH1 = logUH1[3] * (thisTemp - 3000)/(8000 - 3000)
                           + logUH1[2] * (8000 - thisTemp)/(8000 - 3000);
                logStatWHe1 = logUHe1[3] * (thisTemp - 3000)/(8000 - 3000)
                            + logUHe1[2] * (8000 - thisTemp)/(8000 - 3000);
            } else if (thisTemp > 8000 && thisTemp < 10000){
                logStatWH1 = logUH1[4] * (thisTemp - 8000)/(10000 - 8000)
                           + logUH1[3] * (10000 - thisTemp)/(10000 - 8000);
                logStatWHe1 = logUHe1[4] * (thisTemp - 8000)/(10000 - 8000)
                            + logUHe1[3] * (10000 - thisTemp)/(10000 - 8000);
            } else {
*/

            if (thisTemp <= 130){
                logStatWH1 = logUH1[0];
                logStatWHe1 = logUHe1[0];
            }
            if (thisTemp >= 10000){
            // for temperatures of greater than or equal to 10000K lburns
                logStatWH1 = logUH1[4];
                logStatWHe1 = logUHe1[4];
            }

            logGroundPopsH1[iD] = stagePops[0][0][iD] - logStatWH1; 
            logGroundPopsHe1[iD] = stagePops[1][0][iD] - logStatWHe1; 
            logH2Pops[iD] = molPops[iH2-gsFirstMol][iD];
            //System.out.println("iD " + iD + " logH2 " + logH2Pops[iD]);

           // if (iD%10 == 1){
           //     System.out.format("%03d, %21.15f, %21.15f %n",
           //          iD, logE*logGroundPopsH1[iD], logE*logGroundPopsHe1[iD]);
           // }

         }   
       
         double kapRScat = 0.0; 
         //System.out.println("iD    iL    lambda    sigH1    sigHe1 ");
         for (int iL = 0; iL < numLams; iL++){
//
            for (int i = 0; i < numDeps; i++){
               sigH1[i] = 0.0;
               sigHe1[i] = 0.0;
               sigH2[i] = 0.0;
            }

            //System.out.println("Calling opacH1 from masterMetal..."); 
            sigH1 = opacHscat(numDeps, temp, lambdaScale[iL], logGroundPopsH1);
            sigHe1 = opacHescat(numDeps, temp, lambdaScale[iL], logGroundPopsHe1);
            sigH2 = opacH2scat(numDeps, temp, lambdaScale[iL], logH2Pops);

            for (int iD = 0; iD < numDeps; iD++){
               kapRScat = sigH1[iD] + sigHe1[iD] + sigH2[iD];
               masterRScat[iL][iD] = Math.log(kapRScat);
               //if ( (iD%10 == 0) && (iL%10 == 0) ) {
               //  System.out.format("%03d, %03d, %21.15f, %21.15f, %21.15f %n",
               //     iD, iL, lambdaScale[iL], Math.log10(sigH1[iD]), Math.log10(sigHe1[iD]));
               //}

            } //iD
 
         } //iL

         return masterRScat;

      } //end method masterRayl


      public static double[] opacHscat(int numDeps, double[][] temp, double lambda, double[] logGroundPops){

      //System.out.println("opacHscat called");

      double[] sigH = new double[numDeps];

//cross-section is zero below threshold, so initialize:
      for (int i = 0; i < numDeps; i++){
         sigH[i] = 0.0;
      }

      double freq = Useful.c / lambda;  

//c******************************************************************************
//c  This routine computes H I Rayleigh scattering opacities.
//c******************************************************************************

//      include 'Atmos.com'
//      include 'Kappa.com'
//      include 'Linex.com'

      double wavetemp = 2.997925e18 / Math.min(freq, 2.463e15);
      double ww = Math.pow(wavetemp, 2);
      double sig = ( 5.799e-13 + (1.422e-6/ww) + (2.784/(ww*ww)) ) / (ww*ww);
      for (int i = 0; i < numDeps; i++){
         sigH[i] = sig * 2.0 * Math.exp(logGroundPops[i]);
      }

      return sigH;

  } //end method opacHscat


      public static double[] opacHescat(int numDeps, double[][] temp, double lambda, double[] logGroundPops){

      //System.out.println("opacHescat called");

      double[] sigHe = new double[numDeps];

//cross-section is zero below threshold, so initialize:
      for (int i = 0; i < numDeps; i++){
         sigHe[i] = 0.0;
      }

      double freq = Useful.c / lambda;  

//c******************************************************************************
//c  This routine computes He I Rayleigh scattering opacities.
//c******************************************************************************

//      include 'Atmos.com'
//      include 'Kappa.com'
//      include 'Linex.com'

      double wavetemp = 2.997925e18 / Math.min(freq, 5.15e15);
      double ww = Math.pow(wavetemp, 2);
      double sig = (5.484e-14/ww/ww) * Math.pow( ( 1.0 + ((2.44e5 + (5.94e10/(ww-2.90e5)))/ww) ), 2 );
      for (int i = 0; i < numDeps; i++){
         sigHe[i] = sig * Math.exp(logGroundPops[i]); 
      }

      return sigHe;

   } //end method opacHescat


      public static double[] opacH2scat(int numDeps, double[][] temp, double lambda, double[] molPops){

      double[] sigH2 = new double[numDeps];

//cross-section is zero below threshold, so initialize:
      for (int i = 0; i < numDeps; i++){
         sigH2[i] = 0.0;
      }

      double freq = Useful.c / lambda;  

//c******************************************************************************
//c  This routine computes H2 I Rayleigh scattering opacities.
//c******************************************************************************

//      include 'Atmos.com'
//      include 'Kappa.com'
//      include 'Linex.com'

      double wavetemp = 2.997925e18 / Math.min(freq, 2.463e15);
      double ww = Math.pow(wavetemp, 2);
      double sig = ( 8.14e-13 + (1.28e-6/ww) + (1.61/(ww*ww)) ) / (ww*ww);
      //System.out.println("freq "+ freq + " wavetemp "+ wavetemp+ " ww "+ ww+ " sig "+ sig);
      for (int i = 0; i < numDeps; i++){
       sigH2[i] = sig * Math.exp(molPops[i]);
       //System.out.println("i " + i + " molPops " + molPops[i] + " sigH2 " + sigH2[i]);
      }

      return sigH2;

      } //end method opacH2scat

} //end class KappasRayl
