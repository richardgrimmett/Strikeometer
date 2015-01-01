package com.rbg.strike;

import java.io.BufferedReader;
import java.io.FileReader;

import org.jblas.DoubleMatrix;

public class StrikeToolkit {
	public DoubleMatrix filterMatrix(DoubleMatrix m, int freqlow, int freqhigh, double windowSizeSecs) {
		DoubleMatrix r = new DoubleMatrix((int)((freqhigh*windowSizeSecs)-(freqlow*windowSizeSecs)+1),m.columns);
		int l=0;
   	 	for (int i=0; i<m.columns; i++) {
   	 		for (int j=(int)(freqlow*windowSizeSecs)-1;j<(int)(freqhigh*windowSizeSecs);j++) {
 //  	 			System.out.println("i="+i+" j="+j);
   	 			r.put(l++,m.get(j,i));
   	 		}
   	 	}
   	 	return r;
	}
	
	public DoubleMatrix loadS(String fn, int rows, int cols) {
		DoubleMatrix load = new DoubleMatrix(rows,cols);
		try {
			int j=0;
			for (int i=1; i<(cols+1);i++) {
				String fn1=fn+i+".tra";
				BufferedReader br = new BufferedReader(new FileReader(fn1));

				String sCurrentLine;
				while ((sCurrentLine = br.readLine()) != null) {
						load.put(j++, Double.parseDouble(sCurrentLine));
				}
				br.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(8);
		}	
		return load;
	}
	
	public DoubleMatrix inverseFilterMatrix(DoubleMatrix m, int freqlow, int freqhigh, double windowSizeSecs) {
		DoubleMatrix filter=filterMatrix(m,freqlow,freqhigh,windowSizeSecs);
		DoubleMatrix inverse=filter.transpose();
		return inverse;
	}
	
	public DoubleMatrix getS(String fn, int rows, int cols, int freqlow, int freqhigh, double windowSizeSecs) {
		DoubleMatrix load = new DoubleMatrix();
		DoubleMatrix inverse = new DoubleMatrix();
		try {
			load=loadS(fn,rows,cols);
			inverse=inverseFilterMatrix(load,freqlow,freqhigh,windowSizeSecs);
			load=null;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(8);
		}
		return inverse;
	}
	
	public void show(String lab, DoubleMatrix m, int in) {
		double summed=0;
		for (int i=0; i<m.length; i++) summed+=m.get(i);
		System.out.println(lab+" item "+in+" : "+m.get(in)+" Summed= "+summed+" used="+m.length+" row="+m.rows+" col="+m.columns);
	}
	
}
