package com.rbg.strike;



import java.io.PrintWriter;
import java.nio.DoubleBuffer;
import java.util.concurrent.BlockingQueue;

import org.jblas.*;

import static org.jblas.DoubleMatrix.*;

import com.rbg.fft.fftEngine;

public class Gain1 implements Runnable {
	private BlockingQueue<Param> gainTopQueue;
	DoubleBuffer wav;
	DoubleMatrix S = new DoubleMatrix();
	StrikeToolkit tools = new StrikeToolkit();
	static int freqlow=150;
	static int freqhigh=5000;
	static double windowSizeSecs=0.1;
	
	public Gain1(BlockingQueue<Param> gainTopQueue, DoubleBuffer wav, DoubleMatrix S) {
		this.gainTopQueue=gainTopQueue;
		this.wav=wav;
		this.S=S;
	}
	
    public void run() {
        try {
        	String msg="";
        	while(!msg.equals("exit")){
        		
        		Param param = new Param();
        		param=gainTopQueue.take();

                fftEngine fft = new fftEngine();
				fft.fft(wav, param);
				DoubleMatrix XSig = zeros(param.getWindow_size(), param.getNframes());
				Double summed=0.0;
				for (int j=0; j<param.getNframes()*param.getWindow_size(); j++) {
					XSig.put(j,fft.getReal(j));
					summed+=fft.getReal(j);
				}

				//apply filter
				DoubleMatrix XSig1 = tools.inverseFilterMatrix(XSig, freqlow, freqhigh, windowSizeSecs);
				
				NmfFactorise nmf = new NmfFactorise();
				DoubleMatrix A = new DoubleMatrix();

				nmf.init(param, XSig1, S, A);
				
				PrintWriter writer = new PrintWriter("Towers\\Birmingham\\Gain\\A.gai", "UTF-8");
			    for (int j=0; j<A.length; j++) {
			    	writer.println(String.valueOf(A.get(j)));
			    }
			    writer.close();
				XSig=null;
				fft=null;
				System.out.println("Gain1 ended");            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
