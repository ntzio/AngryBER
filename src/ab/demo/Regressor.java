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
import Jama.*;

public class Regressor{
	private Matrix w;
	private Matrix S;
	private Matrix Sinverse;
	private int n;
	private double alpha;
	private double beta;

	// constructor
	public Regressor(int k, double alpha_, double beta_, Settings settings_){
		this.w = new Matrix(k,1);
		this.n = 0;
		
		alpha = alpha_;
		beta = beta_;
		
		new Matrix(k,k);
		this.S = Matrix.identity(k,k).timesEquals(1/alpha);
		new Matrix(k,k);
		this.Sinverse = Matrix.identity(k,k).timesEquals(alpha);
	}

	/*
	 *  function that implemements Bayesian Linear Regression
	 *  35 regressors: type bird-material 7*5
	 *  implementation of equations (3.50) and (3.51): Bishop book
	 */
	public void BayesianRegression(Node target, int t){
		System.out.println("////////////////////////////////////////////////////");
		System.out.println("Update parameters for bayesian linear regression...");
	
		Matrix reward = new Matrix(1,1,(double)t);
		Matrix transPhi = target.PhiX.transpose();
		
		//3.51
		Matrix temp = transPhi.times(target.PhiX);							
		Matrix temp11 = temp.times(beta);									
		Matrix SNinverse = Sinverse.plus(temp11);							
		Matrix SN = SNinverse.inverse();									
		
		// 3.50
		Matrix temp1 = Sinverse.times(w);									
		Matrix temp2 = transPhi.times(reward);								
		Matrix temp22 = temp2.times(beta);									
		Matrix temp3 = temp1.plus(temp22);									
		w = SN.times(temp3);								    
		n++;												
	
		// in each step the posterior converts to prior for the next prediction
		S = SN;
		Sinverse = SNinverse;
		
		System.out.println("End of update for parameters...");
		System.out.println("////////////////////////////////////////////////////");
		
	}
	
	public Matrix GetW() {
		return w;
	}
	
	public Matrix GetV() {
		return S;
	}
	
	public int GetN() {
		return n;
	}
	
	/*
	 * load regressor's parameters
	 */
	public void load_data(Matrix weights_, Matrix covariance_, int n_) {
		w = weights_;
		S = covariance_;
		n = n_;
	}
}