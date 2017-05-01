// AngryBer AI Agent // Copyright (c) 2014 by Georgios Papagiannis <gepapagia@gmail.com> and Nikolaos Tziortziotis <ntziorzi@gmail.com>
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package ab.demo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import Jama.Matrix;

public class Features{
	private Matrix mean;
	private Matrix var;
	private int dims;
	private int clusters;
	private int RBFflag;
	private boolean RBFnorm;
	private int typeOfFeatures;
	private boolean dumnyBasis;			// boolean variable to show if we use dumny basis function 
	
	public Features (Settings settings) {
		clusters = settings.get_num_of_clusters();
		dims     = settings.get_num_of_dims();
		RBFflag  = settings.get_rbf_flag();
		RBFnorm  = settings.get_rbf_norm();
		typeOfFeatures = settings.get_type_of_features();
		dumnyBasis = settings.get_dumnyBasis();
		
		// load mean and var files 
		if(RBFflag == 0) {
			mean =   new Matrix(clusters,dims);
			var  = new Matrix(clusters,dims);
				
			try{
				String mnS = new String("mn.txt");
				String StdS = new String("Std.txt");
				
				BufferedReader inmn  = new BufferedReader(new FileReader(mnS));
				BufferedReader inStd = new BufferedReader(new FileReader(StdS));
				
				// read and store mean and var matrices
				mean = Matrix.read(inmn);
				var = Matrix.read(inStd);
					
				inmn.close();
				inStd.close();
			}
			catch(IOException e){
				System.out.println("ERROR while loading RBF Matrices...");
				System.out.println("exception: " + e);
			}	
		}
	}
	
	public Features(Matrix mean_, Matrix var_){
		mean = mean_;
		var = var_;
		dims = mean.getColumnDimension();
		clusters = mean.getRowDimension();
	}

	/*
	 *  function to set feature vector for each Node of our graph-tree
	 *  1. grid model 
	 *  2. rbf model
	 *  3. linear model
	 */
	public void setPhiX(Tree tree){
		int n = tree.LevelSize();
		
		for (int i=n-2; i>=0; i--){		// "Root" must not checked. "Root" cannot be a target
			int m = tree.LevelSize(i);
			for(int j=0; j<m; j++){
					
				if(tree.GetElement(i,j).sceneFeasible && tree.GetElement(i,j).feasible && tree.GetElement(i,j).reachable) {
					
					// features 
					double a = tree.GetMyWeight(i,j);			// area of Node
					double b = 0;
					if(typeOfFeatures == 0) {
						b= tree.GetRelevantHeight(i,j);			// relevant height of Node
					} else {
						b = tree.GetAboveWeight(i,j);			// parents' cumulative weight
					}
					double c = tree.GetNPDistance(i,j);			// distance from nearest Pig or TNT
				
					double [][] x = {{a,b,c}};
					
					if(RBFflag == 1){									// uses the grid method to set 1, the correct position		
						tree.SetPhi(Gridfunc(x), i,j);
					}
					else if(RBFflag == 0){								// RBF method
						tree.SetPhi(RBFfunc(new Matrix(x)), i, j);
					}
					else if(RBFflag == 2)								// linear model 
					{	
						tree.SetPhi(Linearfunc(x), i, j);
					}
				}
			}
		}	
	}
	
	private Matrix Gridfunc(double[][] x) {
		// Grid size at each direction
		int [] kk = {6,8,4}; 											// for different splits of each dimension
		double [] lower_bound = {0,0,0};			
		
		double [] delta0 = {250,250,250,250,1000,Double.POSITIVE_INFINITY};							// 6
		double [] delta1 = {500,500,500,500,1500,1500,5000,Double.POSITIVE_INFINITY};				// 8
		double [] delta2 = {0.25,0.25,0.25,Double.POSITIVE_INFINITY};								// 4	

		int cap=1;
		for(int l=0; l<kk.length; l++){
			cap = cap*kk[l];
		}
	
		Matrix Phi = new Matrix(1,cap);	 
		Phi.set(0,setPhixhlp(x[0], kk, delta0, delta1, delta2, lower_bound),1);
		
		return Phi;		
	}
	
	/*
	 * function to compute Phi, RBFs
	 */
	private Matrix RBFfunc(Matrix x){
		Matrix Phi;
		Matrix diff;
		
		if(dumnyBasis){
			int clusters_ = clusters + 1;
			Phi = new Matrix(1,clusters_);
			Phi.set(0,0,0);
			
			double sum = 0;
			for(int i=1; i<clusters_; i++){
				diff = x.minus(mean.getMatrix(i,i,0,dims-1));		
				double term = 0;
				
				for(int j=0; j<dims; j++){
					term += (diff.get(0,j)*diff.get(0,j))/(var.get(i,j));
				}
				Phi.set(0,i,Math.exp(-0.5*term));	
				sum += Phi.get(0,i);
			}
			
			// normalization
			if(RBFnorm){
				for(int l=1; l<clusters_; l++){
					Phi.set(0, l, Phi.get(0,l)/sum);
				}
			}
			// dumny basis function
			Phi.set(0,0,1);
		}
		else{
			Phi = new Matrix(1,clusters);
			
			Phi.set(0,0,0);
			double sum = 0;
			for(int i=0; i<clusters; i++){
				diff = x.minus(mean.getMatrix(i,i,0,dims-1));		
				double term = 0;
				
				for(int j=0; j<dims; j++){
					term += (diff.get(0,j)*diff.get(0,j))/(var.get(i,j));
				}
				Phi.set(0,i,Math.exp(-0.5*term));	
				sum += Phi.get(0,i);
			}
			
			// normalized
			if(RBFnorm){
				for(int l=0; l<clusters; l++){
					Phi.set(0, l, Phi.get(0,l)/sum);
				}
			}
		}
		
		return Phi;
	}
	
	private Matrix Linearfunc(double[][] x) {
		int k = 7;
		
		Matrix Phi = new Matrix(1,k);
		double a = x[0][0];
		double b = x[0][1];
		double c = x[0][2];
		
		double aa = a*a;
		double bb = b*b;
		double cc = c*c;
	
		Phi.set(0,0,1);		// bias
		Phi.set(0,1,a); 	Phi.set(0,2,b);	 Phi.set(0,3,c);
		Phi.set(0,4,aa); 	Phi.set(0,5,bb); Phi.set(0,6,cc);
		
		return Phi;
	}
	
	/*
	 * input: a vector x
	 * output: the interval in which belongs vector x  
	 * used for grid method
	 * different number of bins in each dimension
	 */
	private int setPhixhlp(double [] x, int [] K, double [] delta0, double [] delta1, double [] delta2, double [] lower_bound){
		int n_dimensions = 3, n=0;
		int d = 1, y = 0;
		
		for (int i=0; i<n_dimensions; i++){
			n = 0;
			double temp = lower_bound[i];
			for(int j=0; j<K[i]; j++){
				
				switch(i){
					case 0:
						if(x[i] > temp + delta0[j]){
							n++;
							temp = temp + delta0[j];
						}
						else{
							break;
						}
						break;
						
					case 1: 
						if(x[i] > temp + delta1[j]){
							n++;
							temp = temp + delta1[j];
						}
						else{
							break;
						}
						break;
					
					case 2: 
						if(x[i] > temp + delta2[j]){
							n++;
							temp = temp + delta2[j];
						}
						else{
							break;
						}
						break;
				}
			}
			y += d * n;
	        d *= K[i];
		}	
		return(y);
	}
}
