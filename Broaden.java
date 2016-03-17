/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package graystar3server;


import java.text.DecimalFormat;


// **** No!  Do NOT do broadning on server-side!  Class Broaden currently not used (March 2016)


public class Broaden{

    //Do macroturbulent and rotational broadening together because they both require
    //same interpolation onto finer wavelength scals 
    // Input parameters:
    // macroV in km/s
    // equatorial rotational surface velocity in km/s
    // rotation axis inclination wrt to line-of-sight ("i"; i=90 is equator-on)
    // local linear monochromatic limb-darkening coefficient (LDC), "epsilon" 
    //    - computed in class GrayStarServer from monochromatic speecific intensity dist
    //     I_lambda(theta)
    public static double[][] macroRot(double[][] flux, double[] lambda, int numLams, int iStart, int iStop, double macroV, double surfEquRotV) {

/*
//data structure to be returned
   double[][] fluxBroad = new double[3][numLams];
//Default initialization
   for (int i = 0; i < numLams; i++){
      fluxBroad[0][i] = flux[0][i];
      fluxBroad[1][i] = flux[1][i];
   }
*/
//wavelength sampling interval in nm for interpolation 
    double deltaLam = 0.001;  //nm

// Leave convolution margin around spectrum synthesis region:
   iStart = iStart - 1;
   iStop = iStop + 1;
   int numSpecSyn = iStop - iStart + 1;

//Wavelengths bounding specrum synthesis region of SED:

     double lamStart = 1.0e7 * lambda[iStart]; 
     double lamStop = 1.0e7 * lambda[iStop];

     int numFine = (int) Math.floor((lamStop - lamStart) / deltaLam); 

//data structure to be returned
   int numTot = (iStart+1) + numFine + ((numLams-1) - iStop);
   double[][] fluxBroad = new double[3][numTot];
//   System.out.println("macroRot(): numTot " + numTot);
////Default initialization
//   for (int i = 0; i < numTot; i++){
//      fluxBroad[0][i] = flux[0][i];
//      fluxBroad[1][i] = flux[1][i];
//   }

//     System.out.println("numSpecSyn " + numSpecSyn + " numFine " + numFine);
//
//Create uniformly finely sampled wavelenth vector covering specrum synthesis region of SED:
     double[] fineLam = new double[numFine];
     double ii;
     for (int i = 0; i < numFine; i++){
         ii = (float) i;
         fineLam[i] = lamStart + (ii * deltaLam); 
         //System.out.println("i " + i + " fineLam[i] " + fineLam[i]);
         } 

//extract input log flux in spectrum synthesis region:
//  - interpolate in *log* flux
    double[] snipFlux = new double[numSpecSyn];
    double[] snipLam = new double[numSpecSyn];
    for (int i = 0; i < numSpecSyn; i++){
       snipFlux[i] = flux[1][iStart+i]; //log flux
       snipFlux[i] = flux[0][iStart+i]; //linear flux
       snipLam[i] = 1.0e7 * lambda[iStart+i];
       //System.out.println("i " + i + " snipLam[i] " + snipLam[i] + " snipFlux[i] " + Math.log10(snipFlux[i]));
    } 
  
//Interpolate input spectrum sythesis region onto finely sampled wavelength vector:

  double[] fineFlux = ToolBox.interpolV(snipFlux, snipLam, fineLam); 
////Convert back to linear flux:
//  for (int i = 0; i < numFine; i++){
//     fineFlux[i] = Math.exp(fineFlux[i]);
//  }

  if (macroV > 0.0){
//Make an area-normalized Gaussian broadning kernel of FWHM = macroturbulence:

// Find representative wavelength in middle of region to be broadened
       int iMid = (int) Math.floor(numFine / 2);
       double midLam = fineLam[iMid];  //nm
   
       double ckm = 1.0e-5 * Useful.c; //light speed in vacuum in km/s 
  
//Convert macroV to a corrsponding Doppler shift:
      double doppShift = midLam * (macroV / ckm);  //sigma of Gaussian in nm
      double sigma = doppShift / deltaLam; //sigma of Gaussian in pixels 
      //System.out.println("iMid " + iMid + " midLam " + midLam + " macroV " + macroV + " ckm " + ckm + " doppShift " + doppShift + " sigma " + sigma);
   //double fwhm = 2.0 * doppShift; // Is this right?? red shift & blue shift

//Number of wavelength elements for Gaussian
       int numGauss = (int) Math.ceil(5.0 * sigma);
 //ensure odd number of elements in Gausian kernal
       if ((numGauss % 2) == 0){
          numGauss++;
       }
       double[] gauss = new double[numGauss];
       int midPix = (int) Math.floor(numGauss/2);

//Area normalization factors:
       double rootTwoPi = Math.sqrt(2.0 * Math.PI);
       double prefac = 1.0 / (sigma * rootTwoPi); 

       double x, expFac;
//Construct Gaussian in pixel space:
       //double sum = 0.0;  //test
       for (int i = 0; i < numGauss; i++){
          x = (double) (i - midPix);
          expFac = x / sigma; 
          expFac = expFac * expFac; 
          gauss[i] = Math.exp(-0.5 * expFac); 
          gauss[i] = prefac * gauss[i];
          //sum+= gauss[i];   //test
          //System.out.println("i " + i + " gauss[i] " + gauss[i]);  //test
          } 
       //System.out.println("Gaussian area: " + sum); 
//Convolution
//
     //double[] fineFluxConv = convol(fineFlux, gauss);
     double[] fineFluxConv = new double[numFine]; //test only 
     for (int i = 0; i < numFine; i++){    //test only
        //System.out.println("i " + i + " fineLam[i] " + fineLam[i]  + " fineFlux[i]: " + Math.log10(fineFlux[i]));    //test only
        fineFluxConv[i] = fineFlux[i];    //test only
     }                                    //test only

////Convert to log flux for interpolation:
//  for (int i = 0; i < numFine; i++){
//     fineFluxConv[i] = Math.log(fineFluxConv[i]);
//  }

//Interpolate broadened spectrum back onto original sparse lambda grid:
     double[] coarseFlux = ToolBox.interpolV(fineFluxConv, fineLam, snipLam); 

////Convert back to linear flux for interpolation:
//  for (int i = 0; i < numSpecSyn; i++){
//     coarseFlux[i] = Math.exp(coarseFlux[i]);
//     //System.out.println("i " + i + " coarseFlux[i] " + coarseFlux[i]);
//  }
/*
//Put broadened  spectrum synthesis region back into overall SED:
   for (int i = 0; i < numLams; i++){
      if (i <= iStart){
          fluxBroad[0][i] = flux[0][i]; //original value
      }
      if ((i > iStart) && (i < iStop)){
          fluxBroad[0][i] = coarseFlux[(i-iStart)-1];
          //System.out.println("i " + i + " (i-iStart)-1 " + ((i-iStart)-1) + " coarseFlux[(i-iStart)-1] " + Math.log10(coarseFlux[(i-iStart)-1]));
      }
      if (i >= iStop){
          fluxBroad[0][i] = flux[0][i]; //original value
      }
      fluxBroad[1][i] = Math.log(fluxBroad[0][i]);
     //System.out.println("i " + i + " fluxBroad[0][i] " + fluxBroad[0][i] + " fluxBroad[1][i] " + fluxBroad[1][i]);
   }
*/

//Put broadened  spectrum synthesis region back into overall SED:
   for (int i = 0; i < iStart; i++){
          fluxBroad[0][i] = flux[0][i]; //original value
          fluxBroad[1][i] = Math.log(fluxBroad[0][i]);
          fluxBroad[2][i] = lambda[i]; //original value
      }
   for (int i = 0; i < numFine; i++){
          //fluxBroad[0][i] = coarseFlux[(i-iStart)-1];
          fluxBroad[0][iStart+i] = fineFlux[i];
          fluxBroad[1][iStart+i] = Math.log(fluxBroad[0][iStart+i]);
          fluxBroad[2][iStart+i] = 1.0e-7 * fineLam[i];
          //System.out.println("i " + i + " (i-iStart)-1 " + ((i-iStart)-1) + " coarseFlux[(i-iStart)-1] " + Math.log10(coarseFlux[(i-iStart)-1]));
      }
   int count = 0; 
   for (int i = iStop+1; i < numLams; i++){
          fluxBroad[0][iStart+numFine+count] = flux[0][i]; //original value
          fluxBroad[1][iStart+numFine+count] = Math.log(fluxBroad[0][i]);
          fluxBroad[2][iStart+numFine+count] = lambda[i]; //original value
          count++;
      }
     //System.out.println("i " + i + " fluxBroad[0][i] " + fluxBroad[0][i] + " fluxBroad[1][i] " + fluxBroad[1][i]);


} //if macroV > 0.0 condition


if (surfEquRotV > 0.0){

////Interpolate broadened spectrum back onto original sparse lambda grid:
//  double[] coarseFlux = ToolBox.interpolV(fineFluxConv, fineLam, snipLam); 

////Put broadened  spectrum synthesis region back into overall SED:
//   for (int i = 0; i < numLams; i++){
//      if (i <= iStart){
//          fluxBroad[0][i] = flux[0][i]; //original value
//      }
//      if ((i > iStart) && (i < iStop)){
//          fluxBroad[0][i] = coarseFlux[(i-iStart)-1];
//      }
//      if (i >= iStop){
//          fluxBroad[0][i] = flux[0][i]; //original value
//      }
//      fluxBroad[1][i] = Math.log(fluxBroad[0][i]);
   }

        return fluxBroad;

 }  //end method macroRot



//General convolution method
// ***** Function to be convolved and kernel function are expected to *already* be 
// interpolated onto same abssica grid!
//
    public static double[] convol(double[] yFunction, double[] kernel) {

      int ySize = yFunction.length;
      int kernelSize = kernel.length;
      int halfKernelSize = (int) Math.ceil(kernelSize / 2);
  
      double[] yFuncConv = new double[ySize];

//First kernelSize/2 elements of yFunction cannot be convolved
      for (int i = 0; i < halfKernelSize; i++){
         yFuncConv[i] = yFunction[i];
        }
//Convolution:
      int offset = 0; //initialization
      for (int i = halfKernelSize; i < ySize - (halfKernelSize); i++){
         double accum = 0; //accumulator
         for (int j = 0; j < kernelSize; j++){ 
            accum = accum + (kernel[j] * yFunction[j+offset]); 
         }  //inner loop, j	
         yFuncConv[i] = accum;
         offset++;
       } //outer loop, i
//Last kernelSize/2 elements of yFunction cannot be convolved
      for (int i = (ySize - halfKernelSize - 1); i < ySize; i++){
         yFuncConv[i] = yFunction[i];
       }

  return yFuncConv; 

  } //end method convol
   
    
}  //End of class broaden
