package com.rbg.fft;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.util.StringTokenizer;

import com.rbg.strike.Param;


public class fftEngine {
	public native void fft(DoubleBuffer Wav, int startsample, int winsize, int nframes, int nhops, int nfft, DoubleBuffer realOut);

	static {
		String property = System.getProperty("java.library.path");
		StringTokenizer parser = new StringTokenizer(property, ";");
		while (parser.hasMoreTokens()) {
		    System.err.println(parser.nextToken());
		}
	    System.loadLibrary("rbgfft.dll");
	}
	
	static DoubleBuffer realOut; 
	
	public double getReal(int ctr) {
		return realOut.get(ctr);
	}
	
	public void fft(DoubleBuffer wav, Param param) {
		        
        realOut = ByteBuffer.allocateDirect(param.getMax()*8)
    			.order(ByteOrder.nativeOrder()).asDoubleBuffer();
//        System.out.println("ftEngine: this_start_sample="+param.getThis_start_sample());
//        System.out.println("fftEngine: window_size="+param.getWindow_size());
//        System.out.println("fftEngine: nframes="+param.getNframes());
//        System.out.println("fftEngine: Nhop="+param.getNhop());
//        System.out.println("fftEngine: nfft="+param.getNfft());

	    new fftEngine().fft(wav, param.getThis_start_sample(), param.getWindow_size(), param.getNframes(), param.getNhop(), param.getNfft(), realOut);
	}
}	

	
