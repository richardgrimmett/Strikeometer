package com.rbg.strike;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;


public class rbgRandom implements Serializable {
    private static final long serialVersionUID = 1L;

	static public double[] nmf1 = new double[24512];
	static public double[] nmf2 = new double[160000];
    
    private rbgRandom() {
        // private constructor
    	for (int i=0;i<10;i++) {

    	}
    	
    }
 
    private static class DemoSingletonHolder {
        public static final rbgRandom INSTANCE = new rbgRandom();
    }
 
    public static rbgRandom getInstance() {
        return DemoSingletonHolder.INSTANCE;
    }
 
    protected Object readResolve() {
        return getInstance();
    }
    
    public static void load() {
		BufferedReader br = null;
		try {
				String sCurrentLine;
				br = new BufferedReader(new FileReader("nmf1.ran"));
				int i=0;
				while ((sCurrentLine = br.readLine()) != null) {
				nmf1[i++]=Double.parseDouble(sCurrentLine);
				
				}
		} catch (IOException e) {
				e.printStackTrace();
		} 
		
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader("nmf2.ran"));
			int i=0;
			while ((sCurrentLine = br.readLine()) != null) {
				nmf2[i++]=Double.parseDouble(sCurrentLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
}
