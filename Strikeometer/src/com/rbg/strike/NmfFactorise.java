package com.rbg.strike;

import org.jblas.DoubleMatrix;

public class NmfFactorise {
	static Param param;
	static DoubleMatrix A;
	static DoubleMatrix W;
	static DoubleMatrix Randnum;
	static rbgRandom rand = rbgRandom.getInstance();
	
	public int index(int row, int column, int collength) {
		return ((column)*(collength))+(row);
	}
	
	public void absColumnMatrix(DoubleMatrix r, DoubleMatrix m1) {
		int i=0;
		r.resize(1, m1.columns);
		for (int d1=0;d1<m1.columns;d1++) {
			double defobj=0;
			for (int d2=0;d2<m1.rows;d2++) {
				int d3=index(d2,d1,m1.rows);
				defobj=defobj+(m1.get(d3)*m1.get(d3));
			}
			r.put(i++,Math.pow(defobj,0.5));
		}	
	}
	
	
	public void logMatrix(DoubleMatrix m) {
		for (int i=0; i<m.length; i++) {
			m.put(i, Math.log(m.get(i)));
		}
	}
	
	public double sumObj(DoubleMatrix m) {
		double obj=0;
		for (int i=0; i<m.length; i++)	obj+=m.get(i);
		return obj;
	}
	
	public double sumColumnMatrix(DoubleMatrix m, int col) {
		double obj=0;
		for (int i=0;i<m.rows;i++) {
			int d1=index(i,col,m.rows);
			obj+=m.get(d1);
		}
		return obj;
	}
	
	public void copyMatrix(DoubleMatrix r, DoubleMatrix m) {
		r.resize(m.rows,  m.columns);
		for (int i=0;i<m.length;i++) r.put(i, m.get(i));
	}
	public void zeroOneMatrix(DoubleMatrix m1, DoubleMatrix m2) {
		for (int i=0; i<m1.length; i++) {
			if (m1.get(i)==0) {
				m1.put(i, 1);
				m2.put(i, 1);
			}
		}
	}
	
	public void show(String lab, DoubleMatrix m, int in) {
		double summed=0;
		for (int i=0; i<m.length; i++) summed+=m.get(i);
		System.out.println(lab+" item "+in+" : "+m.get(in)+" Summed= "+summed+" used="+m.length+" row="+m.rows+" col="+m.columns);
		for (int i=0;i<in;i++) {
			for (int j=0;j<5;j++) {
				System.out.print("  "+m.get(i,j));
			}
			System.out.println("");
		}
	}
	
	public void init(Param p, DoubleMatrix XSig, DoubleMatrix S, DoubleMatrix A) {
		try{

			param=p;
			double refobj=9999999999.9;
			int positive_steps=0;
		
			W = new DoubleMatrix(XSig.rows, XSig.columns);
			W.fill(1.0);
			
			A.resize(XSig.rows, param.getNmfpSources());
			for (int i=0; i<A.length; i++) A.put(i, rbgRandom.nmf2[i]);
			
			//
			//now lets do the nmf
			//
			DoubleMatrix SCopy = new DoubleMatrix(0,0);
					
			double summed=0;
			for (int i=0; i<XSig.length; i++) {
				summed+=Math.pow(XSig.get(i)*W.get(i), 2);
			}
			double scale=Math.sqrt(1000)/Math.sqrt(summed);
	
			XSig.muli(scale);
		
			//see whether train or gain
			if (param.getAlgorithm().equals("gain")) {	//fixed
				SCopy.copy(S);
			} else {
				SCopy.resize(param.getNmfpSources(), param.getNframes());
				for (int i=0; i<param.getNmfpSources()*param.getNframes(); i++) SCopy.put(i,rbgRandom.nmf1[i]);
			}
			
			//X=X.^audpow
			for (int i=0; i<XSig.length; i++) {
				XSig.put(i, Math.pow(XSig.get(i),param.getAudpow()));
			}
	
			//X=X.*W
			XSig.muli(W);
			
			//
			//inner loop
			//
			boolean innerloop=true;
	
			while (innerloop) {
				DoubleMatrix XSig1 = new DoubleMatrix();
				XSig1.copy(XSig);
	
				DoubleMatrix Y = A.mmul(SCopy);
	
				zeroOneMatrix(Y,XSig1);
	
				DoubleMatrix InverseS = SCopy.transpose();
	
				DoubleMatrix general = new DoubleMatrix();
				
				XSig1.divi(Y, general);
	
				DoubleMatrix multi = general.mmul(InverseS);
	
				DoubleMatrix divis = W.mmul(InverseS);
				
				//set all zeros to ones in multi and set divis too
				zeroOneMatrix(multi,divis);
				
				//set all zeros to ones in divis and set multi too
				zeroOneMatrix(divis,multi);
				
				A.muli(multi);
				A.divi(divis);
				
				for (int i=0; i<A.length; i++) {
					if (A.get(i)<1e-16) A.put(i, 0);
				}
				
				DoubleMatrix V = A.mmul(SCopy);
				//set all zeros to ones in V and set XSig too
				zeroOneMatrix(V,XSig1);
				
				//set all zeros to ones in XSig and set V too
				zeroOneMatrix(XSig1,V);
	
				general.copy(XSig1);
				general.divi(V);
	
				logMatrix(general);
				
				DoubleMatrix D = new DoubleMatrix();;
				D.copy(XSig1);
				D.muli(general);
	
				D.subi(XSig1);
				
				D.addi(V);
		
				general.copy(D);
				general.muli(W);
				
				double obj=sumObj(general);
	
				if (refobj<obj*(1+param.stop_c)) {
					positive_steps++;
				}
				refobj=obj;
				if ((positive_steps>param.stop_n)||(obj<0.01)) {
					innerloop=false;
					
					//abscolumnmatrix
					DoubleMatrix norm_c = new DoubleMatrix();
					absColumnMatrix(norm_c,A);

					general=norm_c.repmat(A.rows, 1);
		
					//dividematrix
					DoubleMatrix A1 = new DoubleMatrix();
					A1.copy(A);
					A1.divi(general);
	
					//inversematrix
					general = norm_c.transpose();
					
					//repmatmatrix
					DoubleMatrix general2 = general.repmat(1,SCopy.columns);
	
					SCopy.muli(general2);
					
					A.copy(A1);
					A.divi(scale);
					
				}
	
			}


	} catch (Exception e) {
        e.printStackTrace(); System.exit(1);}
	}
}
