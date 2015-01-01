package com.rbg.strike;

import java.nio.DoubleBuffer;

public class compWav {
	static WavSample wavs = new WavSample();
	static WavSample wavs2 = new WavSample();
	
	public static void main(String[] args) {
		DoubleBuffer wav = wavs.load("output.wav");
		DoubleBuffer wav2 = wavs2.load("out.wav");
		for (int i=2000;i<3000;i++) {
			if ((wav.get(i)!=wav2.get(i))) {			
				System.out.println(i+" output: "+wav.get(i)+" out: "+wav2.get(i));
			}
		}
	}
}
