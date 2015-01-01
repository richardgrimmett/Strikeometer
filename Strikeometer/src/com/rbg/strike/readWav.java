package com.rbg.strike;

import java.nio.DoubleBuffer;

public class readWav {
	static WavSample wavs = new WavSample();
	
	public static void main(String[] args) {
		DoubleBuffer wav = wavs.load("output.wav");
		for (int i=8000;i<9000;i++) {
	////		if ((wav.get(i)>7) && (wav.get(i)<8)) {			
				System.out.println(i+" "+wav.get(i));
	//		}
		}
	}
}
