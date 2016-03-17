/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package graystar3server;

 /**
 * linear and logarithmic physical constant and conversion factors cgs units
 */
public class Useful {

    //Fields:
    // Fundamental constants
    public static double c = 2.9979249E+10;        // light speed in vaccuum in cm/s
    public static double sigma = 5.670373E-5;   //Stefan-Boltzmann constant ergs/s/cm^2/K^4  
    public static double k = 1.3806488E-16;          // Boltzmann constant in ergs/K
    public static double h = 6.62606957E-27;         //Planck's constant in ergs sec
    public static double ee = 4.80320425E-10;   //fundamental charge unit in statcoulombs (cgs)
    public static double mE = 9.10938291E-28;  //electron mass (g)
    public static double GConst = 6.674e-8;         //Newton's gravitational constant (cgs)
    //Conversion factors
    public static double amu = 1.66053892E-24;  // atomic mass unit in g
    public static double eV = 1.602176565E-12;  // eV in ergs
    public static double rSun = 6.955e10;   // solar radii to cm
    public static double mSun = 1.9891e33;  // solar masses to g
    public static double lSun = 3.846e33;   // solar bolometric luminosities to ergs/s

    //Methods:
    //Natural logs more useful than base 10 logs - Eg. Formal soln module: 
    // Fundamental constants
    public static double logC() {
        return Math.log(c);
    }

    public static double logSigma() {
        return Math.log(sigma);
    }

    public static double logK() {
        return Math.log(k);
    }

    public static double logH() {
        return Math.log(h);
    }

    public static double logEe() {
        return Math.log(ee);
    } //Named so won't clash with log_10(e)

    public static double logMe() {
        return Math.log(mE);
    }
    
    public static double logGConst() {
        return Math.log(GConst);
    }

    //Conversion factors
    public static double logAmu() {
        return Math.log(amu);
    }

    public static double logEv() {
        return Math.log(eV);
    }
    
    public static double logRSun() {
        return Math.log(rSun);
    }
    
    public static double logMSun() {
        return Math.log(mSun);
    }
    
    public static double logLSun() {
        return Math.log(lSun);
    }
   
    
}
