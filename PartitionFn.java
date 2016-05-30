
package graystar3server;

// Partition functions at two temperatures (5000 K and 10000 K) 
//From Allen's Astrophysical Quantities, 4th Ed. 

// CAUTION: Return Base 10 log_10 of partition fn
//Ionization stages that don't exist (eg. "HIII") are given dummy values of 0.0;

public class PartitionFn{


   public static double[] getPartFn(String species){

// CAUTION: log_10 base 10!!
   double[] log10PartFn = new double[2]; 
   //default initialization
   log10PartFn[0] = 0.0;  //for theta = 5040.0/T = 1.0
   log10PartFn[1] = 0.0;  //for theta = 5040.0/T = 0.5
   

      if ("HI".equals(species)){
         log10PartFn[0] = 0.30; 
         log10PartFn[1] = 0.30;
       }
      if ("HII".equals(species)){
         log10PartFn[0] = 0.0;  //dummy 
         log10PartFn[1] = 0.0;   //dummy
       }
      if ("HIII".equals(species)){
         log10PartFn[0] = 0.0;   //dummy
         log10PartFn[1] = 0.0;   //dummy
       }
      if ("HIV".equals(species)){
         log10PartFn[0] = 0.0;   //dummy
         log10PartFn[1] = 0.0;   //dummy
       }
      if ("HeI".equals(species)){
         log10PartFn[0] = 0.00; 
         log10PartFn[1] = 0.00; 
        }     
      if ("HeII".equals(species)){
         log10PartFn[0] = 0.30; 
         log10PartFn[1] = 0.30;
        }   
      if ("HeIII".equals(species)){
         log10PartFn[0] = 0.0;  //dummy 
         log10PartFn[1] = 0.0;  //dummy 
        }   
      if ("HeIV".equals(species)){
         log10PartFn[0] = 0.0;  //dummy 
         log10PartFn[1] = 0.0;  //dummy 
        }   
      if ("LiI".equals(species)){
         log10PartFn[0] = 0.32; 
         log10PartFn[1] = 0.49;
        }   
      if ("LiII".equals(species)){
         log10PartFn[0] = 0.00; 
         log10PartFn[1] = 0.00; 
        }             
      if ("LiIII".equals(species)){
         log10PartFn[0] = Math.log10(2.0);   
         log10PartFn[1] = Math.log10(2.0); 
        }           
      if ("LiIV".equals(species)){
         log10PartFn[0] = 0.00; 
         log10PartFn[1] = 0.00; 
        }           
      if ("BeI".equals(species)){
         log10PartFn[0] = 0.01; 
         log10PartFn[1] = 0.13; 
        }            
      if ("BeII".equals(species)){
         log10PartFn[0] = 0.30; 
         log10PartFn[1] = 0.30; 
        }            
      if ("BeIII".equals(species)){
         log10PartFn[0] = Math.log10(1.0); 
         log10PartFn[1] = Math.log10(1.0); 
        }         
      if ("BeIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
        }         
      if ("BI".equals(species)){
         log10PartFn[0] = 0.78; 
         log10PartFn[1] =  0.78;
        }         
      if ("BII".equals(species)){
         log10PartFn[0] = 0.00; 
         log10PartFn[1] = 0.00; 
        }         
      if ("BIII".equals(species)){
         log10PartFn[0] = Math.log10(2.0);
         log10PartFn[1] = Math.log10(2.0);
        }        
      if ("BIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
        }        
      if ("CI".equals(species)){
         log10PartFn[0] = 0.97; 
         log10PartFn[1] = 1.0; 
        }       
      if ("CII".equals(species)){
         log10PartFn[0] = 0.78; 
         log10PartFn[1] = 0.78; 
        }      
      if ("CIII".equals(species)){
         log10PartFn[0] = Math.log10(1.0);
         log10PartFn[1] = Math.log10(1.0);
        }      
      if ("CIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] = 0.0;
        }      
      if ("NI".equals(species)){
         log10PartFn[0] = 0.61; 
         log10PartFn[1] = 0.66; 
        }    
      if ("NII".equals(species)){
         log10PartFn[0] = 0.95; 
         log10PartFn[1] = 0.97; 
        }  
      if ("NIII".equals(species)){
         log10PartFn[0] = Math.log10(6.0);
         log10PartFn[1] = Math.log10(6.0);
        }   
      if ("NIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
        }   
      if ("OI".equals(species)){
         log10PartFn[0] = 0.94; 
         log10PartFn[1] = 0.97; 
        }               
      if ("OII".equals(species)){
         log10PartFn[0] = 0.60; 
         log10PartFn[1] = 0.61; 
        }               
      if ("OIII".equals(species)){
         log10PartFn[0] = Math.log10(9.0);
         log10PartFn[1] = Math.log10(9.0);
        }              
      if ("OIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
        }              
      if ("FI".equals(species)){
         log10PartFn[0] = 0.75; 
         log10PartFn[1] =  0.77;
        }             
      if ("FII".equals(species)){
         log10PartFn[0] = 0.92; 
         log10PartFn[1] = 0.94; 
        }             
      if ("FIII".equals(species)){
         log10PartFn[0] = Math.log10(4.0);
         log10PartFn[1] = Math.log10(4.0);
        }           
      if ("FIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
        }           
      if ("NeI".equals(species)){
         log10PartFn[0] = 0.00; 
         log10PartFn[1] = 0.00; 
        }         
      if ("NeII".equals(species)){
         log10PartFn[0] = 0.73; 
         log10PartFn[1] = 0.75; 
        }         
      if ("NeIII".equals(species)){
         log10PartFn[0] = Math.log10(9.0); 
         log10PartFn[1] = Math.log10(9.0); 
        }         
      if ("NeIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
        }         
      if ("NaI".equals(species)){
         log10PartFn[0] = 0.31; 
         log10PartFn[1] = 0.60; 
        }     
      if ("NaII".equals(species)){
         log10PartFn[0] = 0.00; 
         log10PartFn[1] = 0.00; 
        }       
      if ("NaIII".equals(species)){
         log10PartFn[0] = Math.log10(6.0); 
         log10PartFn[1] =  Math.log10(6.0);
        }      
      if ("NaIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
        }      
      if ("MgI".equals(species)){
         log10PartFn[0] = 0.01; 
         log10PartFn[1] = 0.15; 
        }               
      if ("MgII".equals(species)){
         log10PartFn[0] = 0.31; 
         log10PartFn[1] = 0.31; 
        }              
      if ("MgIII".equals(species)){
         log10PartFn[0] = Math.log10(1.0); 
         log10PartFn[1] =  Math.log10(1.0);
        }               
      if ("MgIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
        }               
      if ("AlI".equals(species)){
         log10PartFn[0] = 0.77; 
         log10PartFn[1] = 0.81; 
        }            
      if ("AlII".equals(species)){
         log10PartFn[0] = 0.00; 
         log10PartFn[1] = 0.01; 
        }            
      if ("AlIII".equals(species)){
         log10PartFn[0] = Math.log10(2.0); 
         log10PartFn[1] =  Math.log10(2.0);
        }           
      if ("AlIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
        }           
      if ("SiI".equals(species)){
         log10PartFn[0] = 0.98; 
         log10PartFn[1] = 1.04; 
        }          
      if ("SiII".equals(species)){
         log10PartFn[0] = 0.76; 
         log10PartFn[1] = 0.77; 
        }         
      if ("SiIII".equals(species)){
         log10PartFn[0] = Math.log10(1.0); 
         log10PartFn[1] =  Math.log10(1.0);
        }        
      if ("SiIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
        }        
      if ("PI".equals(species)){
         log10PartFn[0] = 0.65; 
         log10PartFn[1] = 0.79; 
        }       
      if ("PII".equals(species)){
         log10PartFn[0] = 0.91; 
         log10PartFn[1] = 0.94; 
        }     
      if ("PIII".equals(species)){
         log10PartFn[0] = Math.log10(6.0); 
         log10PartFn[1] =  Math.log10(6.0);
        }     
      if ("PIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
        }     
      if ("SI".equals(species)){
         log10PartFn[0] = 0.91; 
         log10PartFn[1] = 0.94; 
        }     
      if ("SII".equals(species)){
         log10PartFn[0] = 0.62; 
         log10PartFn[1] = 0.72; 
        }    
      if ("SIII".equals(species)){
         log10PartFn[0] = Math.log10(9.0); 
         log10PartFn[1] =  Math.log10(9.0);
        }    
      if ("SIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
        }    
      if ("ClI".equals(species)){
         log10PartFn[0] = 0.72; 
         log10PartFn[1] = 0.75; 
        } 
      if ("ClII".equals(species)){
         log10PartFn[0] = 0.89; 
         log10PartFn[1] = 0.92; 
        }                
      if ("ClIII".equals(species)){
         log10PartFn[0] = Math.log10(4.0); 
         log10PartFn[1] =  Math.log10(4.0);
        }                 
      if ("ClIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
        }                 
      if ("ArI".equals(species)){
         log10PartFn[0] = 0.00; 
         log10PartFn[1] = 0.00; 
        }           
      if ("ArII".equals(species)){
         log10PartFn[0] = 0.69; 
         log10PartFn[1] = 0.71; 
        }             
      if ("ArIII".equals(species)){
         log10PartFn[0] = Math.log10(9.0); 
         log10PartFn[1] =  Math.log10(9.0);
        }             
      if ("ArIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
        }             
      if ("KI".equals(species)){
         log10PartFn[0] = 0.34; 
         log10PartFn[1] = 0.60; 
        }
      if ("KII".equals(species)){
         log10PartFn[0] = 0.00; 
         log10PartFn[1] = 0.00; 
        }
      if ("KIII".equals(species)){
         log10PartFn[0] = Math.log10(6.0); 
         log10PartFn[1] =  Math.log10(6.0);
         }         
      if ("KIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
         }         
      if ("CaI".equals(species)){
         log10PartFn[0] = 0.07; 
         log10PartFn[1] = 0.55; 
         }     
      if ("CaII".equals(species)){
         log10PartFn[0] = 0.34; 
         log10PartFn[1] = 0.54; 
         }     
      if ("CaIII".equals(species)){
       log10PartFn[0] =  Math.log10(1.0);
       log10PartFn[1] =  Math.log10(1.0);
         }    
      if ("CaIV".equals(species)){
       log10PartFn[0] = 0.00; 
       log10PartFn[1] = 0.00; 
         }    
      if ("ScI".equals(species)){
         log10PartFn[0] = 1.08; 
         log10PartFn[1] = 1.49; 
         }     
      if ("ScII".equals(species)){
         log10PartFn[0] = 1.36; 
         log10PartFn[1] = 1.52; 
         }     
      if ("ScIII".equals(species)){
         log10PartFn[0] = Math.log10(10.0); 
         log10PartFn[1] =  Math.log10(10.0);
         }  
      if ("ScIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
         }  
      if ("TiI".equals(species)){
         log10PartFn[0] = 1.48; 
         log10PartFn[1] = 1.88; 
         } 
      if ("TiII".equals(species)){
         log10PartFn[0] = 1.75; 
         log10PartFn[1] = 1.92; 
         }                   
      if ("TiIII".equals(species)){
         log10PartFn[0] = Math.log10(21.0); 
         log10PartFn[1] =  Math.log10(21.0);
         }                 
      if ("TiIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
         }                 
      if ("VI".equals(species)){
         log10PartFn[0] = 1.62; 
         log10PartFn[1] = 2.03; 
         }               
      if ("VII".equals(species)){
         log10PartFn[0] = 1.64; 
         log10PartFn[1] = 1.89; 
         }                
      if ("VIII".equals(species)){
         log10PartFn[0] = Math.log10(28.0); 
         log10PartFn[1] =  Math.log10(28.0);
         }               
      if ("VIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
         }               
      if ("CrI".equals(species)){
         log10PartFn[0] = 1.02; 
         log10PartFn[1] = 1.51; 
         }            
      if ("CrII".equals(species)){
         log10PartFn[0] = 0.86; 
         log10PartFn[1] = 1.22; 
         }           
      if ("CrIII".equals(species)){
         log10PartFn[0] = Math.log10(25.0); 
         log10PartFn[1] =  Math.log10(25.0);
         }             
      if ("CrIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
         }             
      if ("MnI".equals(species)){
         log10PartFn[0] = 0.81; 
         log10PartFn[1] = 1.16; 
         }        
      if ("MnII".equals(species)){
         log10PartFn[0] = 0.89; 
         log10PartFn[1] = 1.13; 
         }        
      if ("MnIII".equals(species)){
         log10PartFn[0] = Math.log10(6.0); 
         log10PartFn[1] =  Math.log10(6.0);
         }          
      if ("MnIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
         }          
      if ("FeI".equals(species)){
         log10PartFn[0] = 1.43; 
         log10PartFn[1] = 1.74; 
         }     
      if ("FeII".equals(species)){
         log10PartFn[0] = 1.63; 
         log10PartFn[1] = 1.80; 
         }     
      if ("FeIII".equals(species)){
         log10PartFn[0] = Math.log10(25.0); 
         log10PartFn[1] =  Math.log10(25.0);
         }       
      if ("FeIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
         }       
      if ("CoI".equals(species)){
         log10PartFn[0] = 1.52; 
         log10PartFn[1] = 1.76; 
         }    
      if ("CoII)".equals(species)){
         log10PartFn[0] = 1.46; 
         log10PartFn[1] = 1.66; 
         }    
      if ("CoIII".equals(species)){
         log10PartFn[0] = Math.log10(28.0); 
         log10PartFn[1] =  Math.log10(28.0);
         }   
      if ("CoIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
         }   
      if ("NiI".equals(species)){
         log10PartFn[0] = 1.47; 
         log10PartFn[1] = 1.60; 
         }                 
      if ("NiII".equals(species)){
         log10PartFn[0] = 1.02; 
         log10PartFn[1] = 1.28; 
         }                
      if ("NiIII".equals(species)){
         log10PartFn[0] = Math.log10(21.0); 
         log10PartFn[1] =  Math.log10(21.0);
         }                  
      if ("NiIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
         }                  
      if ("CuI".equals(species)){
         log10PartFn[0] = 0.36; 
         log10PartFn[1] = 0.58; 
         }             
      if ("CuII".equals(species)){
         log10PartFn[0] = 0.01; 
         log10PartFn[1] = 0.18; 
         }             
      if ("CuIII".equals(species)){
         log10PartFn[0] = Math.log10(10.0); 
         log10PartFn[1] =  Math.log10(10.0);
         }               
      if ("CuIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
         }               
      if ("ZnI".equals(species)){
         log10PartFn[0] = 0.00; 
         log10PartFn[1] = 0.03; 
         }          
      if ("ZnII".equals(species)){
        log10PartFn[0] = 0.30; 
        log10PartFn[1] = 0.30; 
         }           
      if ("ZnIII".equals(species)){
         log10PartFn[0] = Math.log10(1.0); 
         log10PartFn[1] =  Math.log10(1.0);
         }          
      if ("ZnIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
         }          
      if ("GaI".equals(species)){
         log10PartFn[0] = 0.73; 
         log10PartFn[1] = 0.77; 
         }       
      if ("GaII".equals(species)){
         log10PartFn[0] = 0.00; 
         log10PartFn[1] = 0.00; 
         }        
      if ("GaIII".equals(species)){
         log10PartFn[0] = Math.log10(2.0); 
         log10PartFn[1] =  Math.log10(2.0);
         }       
      if ("GaIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
         }       
      if ("KrI".equals(species)){
         log10PartFn[0] = 0.00; 
         log10PartFn[1] = 0.00; 
         }
      if ("KrII".equals(species)){
         log10PartFn[0] = 0.62; 
         log10PartFn[1] = 0.66; 
        }     
      if ("KrIII".equals(species)){
         log10PartFn[0] = Math.log10(9.0); 
         log10PartFn[1] =  Math.log10(9.0);
        }      
      if ("KrIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
        }      
      if ("RbI".equals(species)){
         log10PartFn[0] = 0.36; 
         log10PartFn[1] = 0.70; 
        } 
      if ("RbII".equals(species)){
         log10PartFn[0] = 0.00; 
         log10PartFn[1] = 0.00; 
        }                 
      if ("RbIII".equals(species)){
         log10PartFn[0] = Math.log10(6.0); 
         log10PartFn[1] =  Math.log10(6.0);
        }                 
      if ("RbIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
        }                 
      if ("SrI".equals(species)){
         log10PartFn[0] = 0.10; 
         log10PartFn[1] = 0.70; 
        }             
      if ("SrII".equals(species)){
         log10PartFn[0] = 0.34; 
         log10PartFn[1] = 0.53; 
        }             
      if ("SrIII".equals(species)){
         log10PartFn[0] = Math.log10(1.0); 
         log10PartFn[1] =  Math.log10(1.0);
        }              
      if ("SrIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
        }              
      if ("YI".equals(species)){
         log10PartFn[0] = 1.08; 
         log10PartFn[1] = 1.50; 
        }             
      if ("YII".equals(species)){
         log10PartFn[0] = 1.18; 
         log10PartFn[1] = 1.41; 
        }            
      if ("YIII".equals(species)){
         log10PartFn[0] = Math.log10(10.0); 
         log10PartFn[1] =  Math.log10(10.0);
        }           
      if ("YIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
        }           
      if ("ZrI".equals(species)){
         log10PartFn[0] = 1.53; 
         log10PartFn[1] = 1.99; 
        }                 
      if ("ZrII".equals(species)){
         log10PartFn[0] = 1.66; 
         log10PartFn[1] = 1.91; 
        }                    
      if ("ZrIII".equals(species)){
         log10PartFn[0] = Math.log10(21.0); 
         log10PartFn[1] =  Math.log10(21.0);
        }                
      if ("ZrIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
        }                
      if ("NbI".equals(species)){
         log10PartFn[0] = 0.00; 
         log10PartFn[1] = 0.00; 
        }              
      if ("NbII".equals(species)){
         log10PartFn[0] = 0.00; 
         log10PartFn[1] = 0.00; 
        }                
      if ("NbIII".equals(species)){
         log10PartFn[0] = Math.log10(1.0); 
         log10PartFn[1] =  Math.log10(1.0);
        }                
      if ("NbIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
        }                
      if ("CsI".equals(species)){
         log10PartFn[0] = 0.00; 
         log10PartFn[1] = 0.00; 
        }              
      if ("CsII".equals(species)){
         log10PartFn[0] = 0.00; 
         log10PartFn[1] = 0.00; 
        }                
      if ("CsIII".equals(species)){
         log10PartFn[0] = Math.log10(1.0); 
         log10PartFn[1] =  Math.log10(1.0);
        }                
      if ("CsIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
        }                
      if ("BaI".equals(species)){
         log10PartFn[0] = 0.36; 
         log10PartFn[1] = 0.92; 
        }             
      if ("BaII".equals(species)){
         log10PartFn[0] = 0.62; 
         log10PartFn[1] = 0.85; 
        }             
      if ("BaIII".equals(species)){
         log10PartFn[0] = Math.log10(1.0); 
         log10PartFn[1] =  Math.log10(1.0);
        }              
      if ("BaIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
        }              
      if ("LaI".equals(species)){
         log10PartFn[0] = 1.41; 
         log10PartFn[1] = 1.85; 
        }            
      if ("LaII".equals(species)){
         log10PartFn[0] = 1.47; 
         log10PartFn[1] = 1.71; 
        }          
      if ("LaIII".equals(species)){
         log10PartFn[0] =  Math.log10(10.0);
         log10PartFn[1] =  Math.log10(10.0);
        }          
      if ("LaIV".equals(species)){
         log10PartFn[0] = 0.0; 
         log10PartFn[1] =  0.0;
        }          

//
return log10PartFn;

}  //end of method getIonE    


} //end AtomicMass class
