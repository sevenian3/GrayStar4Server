
package graystar3server;

// Graound state ionization energies in eV 
//From NIST Atomic Spectra Database
//Ionization Energies Data
//Kramida, A., Ralchenko, Yu., Reader, J., and NIST ASD Team (2014). NIST Atomic Spectra Database (ver. 5.2), [Online]. Available: http://physics.nist.gov/asd [2015, November 23]. National Institute of Standards and Technology, Gaithersburg, MD.
//Heaviest element treatable: La


//Ionization stages that don't exist (eg. "HIII") are given extremely large ioization energies (999 ev)

public class IonizationEnergy {


   public static double getIonE(String species){

   double ionE = 8.0; //default initialization

      if ("HI".equals(species)){
         ionE = 13.598434005136;
       }
      if ("HII".equals(species)){
         ionE = 999999.0;
       }
      if ("HIII".equals(species)){
         ionE = 999999.0;
       }
      if ("HIV".equals(species)){
         ionE = 999999.0;
       }
      if ("HeI".equals(species)){
         ionE = 24.587387936;
        }     
      if ("HeII".equals(species)){
         ionE = 54.417763110;
        }   
      if ("HeIII".equals(species)){
         ionE = 999999.0;
        }   
      if ("HeIV".equals(species)){
         ionE = 999999.0;
        }   
      if ("LiI".equals(species)){
         ionE = 5.391714761;
        }   
      if ("LiII".equals(species)){
         ionE = 75.6400937;
        }             
      if ("LiIII".equals(species)){
         ionE = 122.45435380;
        }           
      if ("LiIV".equals(species)){
         ionE = 999999.0;
        }           
      if ("BeI".equals(species)){
         ionE = 9.3226990;
        }            
      if ("BeII".equals(species)){
         ionE = 18.211153;
        }            
      if ("BeIII".equals(species)){
         ionE = 153.8961980;
        }         
      if ("BeIV".equals(species)){
         ionE = 217.7185766;
        }         
      if ("BI".equals(species)){
         ionE = 8.2980190;
        }         
      if ("BII".equals(species)){
         ionE = 25.154830;
        }         
      if ("BIII".equals(species)){
         ionE = 37.930580;
        }        
      if ("BIV".equals(species)){
         ionE = 259.3715;
        }        
      if ("CI".equals(species)){
         ionE = 11.260300;
        }       
      if ("CII".equals(species)){
         ionE = 24.38450;
        }      
      if ("CIII".equals(species)){
         ionE = 47.88778;
        }      
      if ("CIV".equals(species)){
         ionE = 64.49351;
        }      
      if ("NI".equals(species)){
         ionE = 14.534130;
        }    
      if ("NII".equals(species)){
         ionE = 29.601250;
        }  
      if ("NIII".equals(species)){
         ionE = 47.4453;
        }   
      if ("NIV".equals(species)){
         ionE = 77.47350;
        }   
      if ("OI".equals(species)){
         ionE = 13.6180540;
        }               
      if ("OII".equals(species)){
         ionE = 35.121110;
        }               
      if ("OIII".equals(species)){
         ionE = 54.93554;
        }              
      if ("OIV".equals(species)){
         ionE = 77.41350;
        }              
      if ("FI".equals(species)){
         ionE = 17.422820;
        }             
      if ("FII".equals(species)){
         ionE = 34.97081;
        }             
      if ("FIII".equals(species)){
         ionE = 62.70800;
        }           
      if ("FIV".equals(species)){
         ionE = 87.175;
        }           
      if ("NeI".equals(species)){
         ionE = 21.5645400;
        }         
      if ("NeII".equals(species)){
         ionE = 40.962960;
        }         
      if ("NeIII".equals(species)){
         ionE = 63.42331;
        }         
      if ("NeIV".equals(species)){
         ionE = 97.1900;
        }         
      if ("NaI".equals(species)){
         ionE = 5.13907670;
        }     
      if ("NaII".equals(species)){
         ionE = 47.28636;
        }       
      if ("NaIII".equals(species)){
         ionE = 71.6200;
        }      
      if ("NaIV".equals(species)){
         ionE = 98.936;
        }      
      if ("MgI".equals(species)){
         ionE = 7.6462350;
        }               
      if ("MgII".equals(species)){
         ionE = 15.0352670;
        }              
      if ("MgIII".equals(species)){
         ionE = 80.14360;
        }               
      if ("MgIV".equals(species)){
         ionE = 109.2654;
        }               
      if ("AlI".equals(species)){
         ionE = 5.9857684;
        }            
      if ("AlII".equals(species)){
         ionE = 18.828550;
        }            
      if ("AlIII".equals(species)){
         ionE = 28.447640;
        }           
      if ("AlIV".equals(species)){
         ionE = 119.9924;
        }           
      if ("SiI".equals(species)){
         ionE = 8.151683;
        }          
      if ("SiII".equals(species)){
         ionE = 16.345845;
        }         
      if ("SiIII".equals(species)){
         ionE = 33.493000;
        }        
      if ("SiIV".equals(species)){
         ionE = 45.141790;
        }        
      if ("PI".equals(species)){
         ionE = 10.486686;
        }       
      if ("PII".equals(species)){
         ionE = 19.769490;
        }     
      if ("PIII".equals(species)){
         ionE = 30.202640;
        }     
      if ("PIV".equals(species)){
         ionE = 51.44387;
        }     
      if ("SI".equals(species)){
         ionE = 10.36001;
        }     
      if ("SII".equals(species)){
         ionE = 23.33788;
        }    
      if ("SIII".equals(species)){
         ionE = 34.856;
        }    
      if ("SIV".equals(species)){
         ionE = 47.222;
        }    
      if ("ClI".equals(species)){
         ionE = 12.967632;
        } 
      if ("ClII".equals(species)){
         ionE = 23.81364;
        }                
      if ("ClIII".equals(species)){
         ionE = 39.80;
        }                 
      if ("ClIV".equals(species)){
         ionE = 53.24;
        }                 
      if ("ArI".equals(species)){
         ionE = 15.75961120;
        }           
      if ("ArII".equals(species)){
         ionE = 27.62967;
        }             
      if ("ArIII".equals(species)){
         ionE = 40.735;
        }             
      if ("ArIV".equals(species)){
         ionE = 59.58;
        }             
      if ("KI".equals(species)){
         ionE = 4.340663540;
        }
      if ("KII".equals(species)){
         ionE = 31.62500;
        }
      if ("KIII".equals(species)){
         ionE = 45.8031;
         }         
      if ("KIV".equals(species)){
         ionE = 60.917;
         }         
      if ("CaI".equals(species)){
         ionE = 6.11315520;
         }     
      if ("CaII".equals(species)){
         ionE = 11.8717180;
         }     
      if ("CaIII".equals(species)){
       ionE = 50.91315;
         }    
      if ("CaIV".equals(species)){
       ionE = 67.273;
         }    
      if ("ScI".equals(species)){
         ionE = 6.561490;
         }     
      if ("ScII".equals(species)){
         ionE = 12.79977;
         }     
      if ("ScIII".equals(species)){
         ionE = 24.756838;
         }  
      if ("ScIV".equals(species)){
         ionE = 73.48940;
         }  
      if ("TiI".equals(species)){
         ionE = 6.828120;
         } 
      if ("TiII".equals(species)){
         ionE = 13.5755;
         }                   
      if ("TiIII".equals(species)){
         ionE = 27.49171;
         }                 
      if ("TiIV".equals(species)){
         ionE = 43.26717;
         }                 
      if ("VI".equals(species)){
         ionE = 6.746187;
         }               
      if ("VII".equals(species)){
         ionE = 14.6200;
         }                
      if ("VIII".equals(species)){
         ionE = 29.3110;
         }               
      if ("VIV".equals(species)){
         ionE = 46.7090;
         }               
      if ("CrI".equals(species)){
         ionE = 6.766510;
         }            
      if ("CrII".equals(species)){
         ionE = 16.486305;
         }           
      if ("CrIII".equals(species)){
         ionE = 30.960;
         }             
      if ("CrIV".equals(species)){
         ionE = 49.160 ;
         }             
      if ("MnI".equals(species)){
         ionE = 7.4340377;
         }        
      if ("MnII".equals(species)){
         ionE = 15.639990;
         }        
      if ("MnIII".equals(species)){
         ionE = 33.668;
         }          
      if ("MnIV".equals(species)){
         ionE = 51.20;
         }          
      if ("FeI".equals(species)){
         ionE = 7.9024678;
         }     
      if ("FeII".equals(species)){
         ionE = 16.199200;
         }     
      if ("FeIII".equals(species)){
         ionE = 30.651;
         }       
      if ("FeIV".equals(species)){
         ionE = 54.910;
         }       
      if ("CoI".equals(species)){
         ionE = 7.88101;
         }    
      if ("CoII)".equals(species)){
         ionE = 17.0844;
         }    
      if ("CoIII".equals(species)){
         ionE = 33.500;
         }   
      if ("CoIV".equals(species)){
         ionE = 51.27;
         }   
      if ("NiI".equals(species)){
         ionE = 7.639877;
         }                 
      if ("NiII".equals(species)){
         ionE = 18.168837;
         }                
      if ("NiIII".equals(species)){
         ionE = 35.190;
         }                  
      if ("NiIV".equals(species)){
         ionE = 54.90;
         }                  
      if ("CuI".equals(species)){
         ionE = 7.7263800;
         }             
      if ("CuII".equals(species)){
         ionE = 20.292390;
         }             
      if ("CuIII".equals(species)){
         ionE = 36.841;
         }               
      if ("CuIV".equals(species)){
         ionE = 57.380;
         }               
      if ("ZnI".equals(species)){
         ionE = 9.3941970;
         }          
      if ("ZnII".equals(species)){
        ionE = 17.96439;
         }           
      if ("ZnIII".equals(species)){
         ionE = 39.72300;
         }          
      if ("ZnIV".equals(species)){
         ionE = 59.573;
         }          
      if ("GaI".equals(species)){
         ionE = 5.9993018;
         }       
      if ("GaII".equals(species)){
         ionE = 20.51514;
         }        
      if ("GaIII".equals(species)){
         ionE = 30.72600;
         }       
      if ("GaIV".equals(species)){
         ionE = 63.2410;
         }       
      if ("KrI".equals(species)){
         ionE = 13.9996049;
         }
      if ("KrII".equals(species)){
         ionE = 24.35984;
        }     
      if ("KrIII".equals(species)){
         ionE = 35.838;
        }      
      if ("KrIV".equals(species)){
         ionE = 50.85;
        }      
      if ("RbI".equals(species)){
         ionE = 4.1771280;
        } 
      if ("RbII".equals(species)){
         ionE = 27.289540;
        }                 
      if ("RbIII".equals(species)){
         ionE = 39.2470;
        }                 
      if ("RbIV".equals(species)){
         ionE = 52.20;
        }                 
      if ("SrI".equals(species)){
         ionE = 5.69486720;
        }             
      if ("SrII".equals(species)){
         ionE = 11.0302760;
        }             
      if ("SrIII".equals(species)){
         ionE = 42.88353;
        }              
      if ("SrIV".equals(species)){
         ionE = 56.2800;
        }              
      if ("YI".equals(species)){
         ionE = 6.21726;
        }             
      if ("YII".equals(species)){
         ionE = 12.22400;
        }            
      if ("YIII".equals(species)){
         ionE = 20.52441;
        }           
      if ("YIV".equals(species)){
         ionE = 60.6070;
        }           
      if ("ZrI".equals(species)){
         ionE = 6.633900;
        }                 
      if ("ZrII".equals(species)){
         ionE = 13.13;
        }                    
      if ("ZrIII".equals(species)){
         ionE = 23.1700;
        }                
      if ("ZrIV".equals(species)){
         ionE = 34.418360;
        }                
      if ("NbI".equals(species)){
         ionE = 6.758850;
        }              
      if ("NbII".equals(species)){
         ionE = 14.32;
        }                
      if ("NbIII".equals(species)){
         ionE = 25.0;
        }                
      if ("NbIV".equals(species)){
         ionE = 37.611;
        }                
      if ("CsI".equals(species)){
         ionE = 3.893905548;
        }              
      if ("CsII".equals(species)){
         ionE = 23.157450;
        }                
      if ("CsIII".equals(species)){
         ionE = 33.1950;
        }                
      if ("CsIV".equals(species)){
         ionE = 43.0;
        }                
      if ("BaI".equals(species)){
         ionE = 5.2116640;
        }             
      if ("BaII".equals(species)){
         ionE = 10.003826;
        }             
      if ("BaIII".equals(species)){
         ionE = 35.8400;
        }              
      if ("BaIV".equals(species)){
         ionE = 47.03;
        }              
      if ("LaI".equals(species)){
         ionE = 5.57690;
        }            
      if ("LaII".equals(species)){
         ionE = 11.184920;
        }          
      if ("LaIII".equals(species)){
         ionE = 19.17730;
        }          
      if ("LaIV".equals(species)){
         ionE = 49.950;
        }          

//
    return ionE;

  }  //end of method getIonE    


// Molecular dissociation energies in eV 
//From NIST Allen's Astrophysical Quantities, 4th Ed.



   public static double getDissE(String species){

   double dissE = 8.0; //default initialization

      if (species == "H2"){
         dissE = 4.4781;
       }
      if (species == "H2+"){
         dissE = 2.6507;
       }
      if (species == "C2"){
         dissE = 6.296;
       }
      if (species == "CH"){
         dissE = 3.465;
       }
      if (species == "CO"){
         dissE = 11.092;
       }
      if (species == "CN"){
         dissE = 7.76;
       }
      if (species == "N2"){
         dissE = 9.759;
       }
      if (species == "NH"){
         dissE = 3.47;
       }
      if (species == "NO"){
         dissE = 6.497;
       }
      if (species == "O2"){
         dissE = 5.116;
       }
      if (species == "OH"){
         dissE = 4.392;
       }
      if (species == "MgH"){
         dissE = 1.34;
       }
      if (species == "SiO"){
         dissE = 8.26;
       }
      if (species == "CaH"){
         dissE = 1.70;
       }
      if (species == "CaO"){
         dissE = 4.8;
       }
      if (species == "TiO"){
         dissE = 6.87;
       }
      if (species == "VO"){
         dissE = 6.4;
       }
      if (species == "FeO"){
         dissE = 4.20;
       }

//
  return dissE;

  };  //end of method getDissE    

} //end IonizationEnergy class
