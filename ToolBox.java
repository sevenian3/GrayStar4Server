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
public class ToolBox {
    
 /*
 Linear interpolation to a new abscissa - mainly for interpolating flux to a specific lambda
 */   
    /**
     *
     * @param x
     * @param y
     * @param newX
     * @return
     */
    public static double interpol(double[] x, double[] y, double newX) {

        double newY;

        // Bracket newX:
        double x1, x2;
        int p1, p2;
        p1 = 0;
        p2 = 1;
        x1 = x[p1];
        x2 = x[p2];

        for (int i = 1; i < x.length; i++) {
            if (x[i] >= newX) {
                // Found upper bracket
                p2 = i;
                p1 = i - 1;
                x2 = x[p2];
                x1 = x[p1];
                break;
            }
        }

        double step = x2 - x1;

    //Interpolate
        //First order Lagrange formula
        //   newY = y[1][p2] * (newX - x1) / step
        //           + y[1][p1] * (x2 - newX) / step;
        newY = y[p2] * (newX - x1) / step
                + y[p1] * (x2 - newX) / step;

        //System.out.println("Interpol: p1, p2, x1, x2, y1, y2, newX, newY: " + 
        //        p1 + " " + p2 + " " + x1 + " " + x2 + " " + y[1][p1] + " " + y[1][p2] + " " + newX + " " + newY + " ");
        return newY;

    }

/**
 *
 * @author Ian vectorized version of simple linear 1st order interpolation
 * // Caution: Assumes new and old abscissae are in monotonic increasing order
 */

    public static double[] interpolV(double[] y, double[] x, double[] newX) {

        int num = x.length;
        if (num != y.length){
          //System.out.println("Toolbox.interpolV(): Old x and y must be same length");  
        }
        int newNum = newX.length;
        //System.out.println("interpolV: newNum " + newNum + " num " + num); 
        double[] newY = new double[newNum];

//Renormalize ordinates:
    
    int[] iMinAndMax = minMax(y);
    double norm = y[iMinAndMax[1]];
    //System.out.println("norm " + norm);
    double[] yNorm = new double[num]; 
    double[] newYNorm = new double[newNum]; 
    for (int i = 0; i < num; i++){
      yNorm[i] = y[i] / norm; 
    }

// Set any newX elements that are *less than* the first x element to th first 
// x element - "0th order extrapolation"
//
        int start = 0;
         for (int i = 0; i < newNum; i++) {
            if (newX[i] <= x[1]){
              newYNorm[i] = yNorm[0];
              start++;
            }
            if (newX[i] > x[1]){
               break;
            }
         }   
  //System.out.println("start " + start);
  //System.out.println("x[0] " + x[0] + " x[1] " + x[1] + " newX[start] " + newX[start]);
  double jWght, jm1Wght, denom;
  
  if (start < newNum-1){

        int j = 1; //initialize old abscissae index
        //outer loop over new abscissae
        for (int i = start; i < newNum; i++) {

            //System.out.println("i " + i + " j " + j);

// break out if current element newX is *greater* that last x element
            if ( (newX[i] > x[num-1]) || (j > (num-1)) ){
               break; 
            }

            while (x[j] < newX[i]) {
                j++;
            }
            //System.out.println("i " + i + " newX[i] " + newX[i] + " j " + j + " x[j-1] " + x[j-1] + " x[j] " + x[j]);
            //1st order Lagrange method:
            jWght = newX[i] * (1.0 - (x[j-1]/newX[i])); //(newX[i]-x[j-1])
            jm1Wght = x[j] * (1.0 - (newX[i]/x[j])); //(x[j]-newX[i])
            denom = x[j] * (1.0 - (x[j-1]/x[j])); //(x[j]-x[j-1])
            jWght = jWght / denom;
            jm1Wght = jm1Wght / denom;
            //newYNorm[i] = (yNorm[j]*(newX[i]-x[j-1])) + (yNorm[j-1]*(x[j]-newX[i]));
            newYNorm[i] = (yNorm[j]*jWght) + (yNorm[j-1]*jm1Wght);
            //System.out.println("i " + i + " newYNorm[i] " + newYNorm[i] + " j " + j + " yNorm[j-1] " + yNorm[j-1] + " yNorm[j] " + yNorm[j]);
        }

    } //start condition

// Set any newX elements that are *greater than* the first x element to the last 
// x element - "0th order extrapolation"
//
         for (int i = 0; i < newNum; i++) {
            if (newX[i] >= x[num-1]){
              newYNorm[i] = yNorm[num-1];
            }
         }   

//Restore orinate scale
    for (int i = 0; i < newNum; i++){
      newY[i] = newYNorm[i] * norm; 
    }


        return newY;
    }

    /**
 * Return the array index of the wavelength array (lambdas) closest to a desired
 * value of wavelength (lam)
 */
     /**
     * 
     * @param numLams
     * @param lambdas
     * @param lam
     * @return 
     */
    public static int lamPoint(int numLams, double[] lambdas, double lam) {

        int index;

        double[] help = new double[numLams];

        for (int i = 0; i < numLams; i++) {

            help[i] = lambdas[i] - lam;
            help[i] = Math.abs(help[i]);

        }
        index = 0;
        double min = help[index];

        for (int i = 1; i < numLams; i++) {

            if (help[i] < min) {
                min = help[i];
                index = i;
            }

        }

        return index;

    }
    /**
 * Return the minimum and maximum values of an input 1D array CAUTION; Will
 * return the *first* occurence if min and/or max values occur in multiple
 * places iMinMax[0] = first occurence of minimum iMinMax[1] = first occurence
 * of maximum
 */
     /**
     * 
     * @param x
     * @return 
     */
    public static int[] minMax(double[] x) {

        int[] iMinMax = new int[2];

        int num = x.length;
        //System.out.println("MinMax: num: " + num);

        int iMin = 0;
        int iMax = 0;
        double min = x[iMin];
        double max = x[iMax];

        for (int i = 1; i < num; i++) {

            //System.out.println("MinMax: i , current min, x : " + i + " " + min + " " + x[i]);
            if (x[i] < min) {
                //System.out.println("MinMax: new min: if branch triggered" );
                min = x[i];
                iMin = i;
            }
            //System.out.println("MinMax: new min: " + min);

            if (x[i] > max) {
                max = x[i];
                iMax = i;
            }

        }
        //System.out.println("MinMax: " + iMin + " " + iMax);

        iMinMax[0] = iMin;
        iMinMax[1] = iMax;

        return iMinMax;

    }

 /**
 * Version of MinMax.minMax for 2XnumDep & 2XnumLams arrays where row 0 is
 * linear and row 1 is logarithmic
 *
 * Return the minimum and maximum values of an input 1D array CAUTION; Will
 * return the *first* occurence if min and/or max values occur in multiple
 * places iMinMax[0] = first occurence of minimum iMinMax[1] = first occurence
 * of maximum
 */
    /**
     * 
     * @param x
     * @return 
     */
    public static int[] minMax2(double[][] x) {

        int[] iMinMax = new int[2];

        int num = x[0].length;

        int iMin = 0;
        int iMax = 0;

        // Search for minimum and maximum in row 0 - linear values:
        double min = x[0][iMin];
        double max = x[0][iMax];

        for (int i = 1; i < num; i++) {

            if (x[0][i] < min) {
                min = x[0][i];
                iMin = i;
            }

            if (x[0][i] > max) {
                max = x[0][i];
                iMax = i;
            }

        }

        iMinMax[0] = iMin;
        iMinMax[1] = iMax;

        return iMinMax;

    }
   
 /**
 * Return the array index of the optical depth arry (tauRos) closest to a
 * desired value of optical depth (tau) Assumes the use wants to find a *lienar*
 * tau value , NOT logarithmic
 */

    /**
     * 
     * @param numDeps
     * @param tauRos
     * @param tau
     * @return 
     */
    public static int tauPoint(int numDeps, double[][] tauRos, double tau) {

        int index;

        double[] help = new double[numDeps];

        for (int i = 0; i < numDeps; i++) {

            help[i] = tauRos[0][i] - tau;
            help[i] = Math.abs(help[i]);

        }
        index = 0;
        double min = help[index];

        for (int i = 1; i < numDeps; i++) {

            if (help[i] < min) {
                min = help[i];
                index = i;
            }

        }

        return index;

    }
  
       
}
