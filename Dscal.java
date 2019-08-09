/*# -*- coding: utf-8 -*-
"""
Created on Fri May 17 16:20:21 2019

@author: 
"""
*/

/*
import math

"""
*> \brief \b DSCAL
*
*  =========== DOCUMENTATION ===========
*
* Online html documentation available at
*            http://www.netlib.org/lapack/explore-html/
*
*  Definition:
*  ===========
*
*       SUBROUTINE DSCAL(N,DA,DX,INCX)
*
*       .. Scalar Arguments ..
*       DOUBLE PRECISION DA
*       INTEGER INCX,N
*       ..
*       .. Array Arguments ..
*       DOUBLE PRECISION DX(*)
*       ..
*
*
*> \par Purpose:
*  =============
*>
*> \verbatim
*>
*>    DSCAL scales a vector by a constant.
*>    uses unrolled loops for increment equal to 1.
*> \endverbatim
*
*  Arguments:
*  ==========
*
*> \param[in] N
*> \verbatim
*>          N is INTEGER
*>         number of elements in input vector(s)
*> \endverbatim
*>
*> \param[in] DA
*> \verbatim
*>          DA is DOUBLE PRECISION
*>           On entry, DA specifies the scalar alpha.
*> \endverbatim
*>
*> \param[in,out] DX
*> \verbatim
*>          DX is DOUBLE PRECISION array, dimension ( 1 + ( N - 1)*abs( INCX ) )
*> \endverbatim
*>
*> \param[in] INCX
*> \verbatim
*>          INCX is INTEGER
*>         storage spacing between elements of DX
*> \endverbatim
*
*  Authors:
*  ========
*
*> \author Univ. of Tennessee
*> \author Univ. of California Berkeley
*> \author Univ. of Colorado Denver
*> \author NAG Ltd.
*
*> \date November 2017
*
*> \ingroup double_blas_level1
*
*> \par Further Details:
*  =====================
*>
*> \verbatim
*>
*>     jack dongarra, linpack, 3/11/78.
*>     modified 3/93 to return if incx .le. 0.
*>     modified 12/3/93, array(1) declarations changed to array(*)
*> \endverbatim
*>
*  =====================================================================
"""
*/

package chromastarserver;

public class Dscal{

//#SUBROUTINE dscal(N,DA,DX,INCX)
   public static double[] dscal(int n, double da, double[] dx, int incx){

    //#*
    //#*  -- Reference BLAS level1 routine (version 3.8.0) --
    //#*  -- Reference BLAS is a software package provided by Univ. of
    //#*  Tennessee,    --
    //#*  -- Univ. of California Berkeley, Univ. of Colorado Denver and NAG
    //#*  Ltd..--
    //#*     November 2017
    //#*
    //#*     .. Scalar Arguments ..
    //#DOUBLE PRECISION DA
    //#INTEGER INCX,N
    //#*     ..
    //#*     .. Array Arguments ..
    //#DOUBLE PRECISION DX(*)
    
    int dxSize = 1 + (n-1)*Math.abs(incx);
    //#dxOut = [0.0e0 for i in range(dxSize)]
    //#*     ..
    //#*
    //#*  =====================================================================
    //#*
    //#*     .. Local Scalars ..
    //#INTEGER I,M,MP1,NINCX
    int i = 0;
    int m = 0;
    int mp1 = 0;
    int nincx = 0;
    
    //#print("DSCAL: n ", n, " incx ", incx, " da ", da)
    //#print("dx in ", [dx[kk] for kk in range(n)])
    
    //#*     ..
    //#*     .. Intrinsic Functions ..
    //#INTRINSIC mod
    //#*     ..
    //#IF (n.LE.0 .OR. incx.LE.0) RETURN
    if ( (n > 0)  && (incx > 0) ){
        
        //#IF (incx.EQ.1) THEN
        if (incx == 1){
            //#*
            //#*        code for increment equal to 1
            //#*
            //#*
            //#*        clean-up loop
            //#*
            
            m = n % 5;
            //#IF (m.NE.0) THEN
            if (m != 0){
                
                //#DO i = 1,m
                for (int i99 = 0; i99 < m; i99++){
                    dx[i99] = da*dx[i99];
                    //#print("DSCAL 1: i ", i, " dx ", dx[i])
                }
            
                //#IF (n.LT.5) RETURN
            }
            //#END IF
            //#if ( (m == 0) and (n >= 5) ):
            if ( n >= 5 ){    

                mp1 = m + 1;

                //#DO i = mp1,n,5
//#               print("DSCAL: n ", n, " m ", m, " mp1 ", mp1, " da ", da)
                for (int i99 = mp1-1; i99 < n; i99+=5){
//#                    print("DSCAL 2: i ", i, " dx(i... i+4) ",\
//#                          dx[i], dx[i+1], dx[i+2], dx[i+3], dx[i+4])
                    dx[i99] = da*dx[i99];
                    dx[i99+1] = da*dx[i99+1];
                    dx[i99+2] = da*dx[i99+2];
                    dx[i99+3] = da*dx[i99+3];
                    dx[i99+4] = da*dx[i99+4];
                    //#print("DSCAL 2: i ", i, " dx(i... i+4) ",\
                    //#      dx[i], dx[i+1], dx[i+2], dx[i+3], dx[i+4])
                }
            }        
 
        } else{
            //#*
            //#*        code for increment not equal to 1
            //#*
            nincx = n*incx;
            //#DO i = 1,nincx,incx
            for (int i99 = 0; i99 < nincx; i99+=incx){
                dx[i99] = da*dx[i99];
                //#print("DSCAL 3: i ", i, " dx ", dx[i])
            }
        }
         
    }  
      
    return dx;

   } // end method dscal()
      
} //end class Dscal
