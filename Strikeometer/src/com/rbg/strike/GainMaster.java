package com.rbg.strike;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.nio.DoubleBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.jblas.*;

import static org.jblas.DoubleMatrix.*;

import com.rbg.fft.fftEngine;

public class GainMaster
{	
	static rbgRandom rand = rbgRandom.getInstance();
	public static ArrayBlockingQueue<Param> gainTopQueue= new ArrayBlockingQueue<Param>(10000);
	static DoubleBuffer wav;
	static String tdir="";
	static String filename="";
	static DoubleMatrix S = new DoubleMatrix();
	static DoubleMatrix S_if  = new DoubleMatrix();
	static WavSample wavs = new WavSample();
	static int freqlow=150;
	static int freqhigh=5000;
	static double windowSizeSecs=0.1;
	
   public static void main(String[] args)
   {
      try
      {
    	 rbgRandom.load();
    	 String tdir="Towers\\Birmingham\\Touch";
    	 String filename="4.wav";
    	 int bufferSize=441000;
    	 int numbells=12;
    	 StrikeToolkit tools = new StrikeToolkit();
    	 
    	 S=tools.getS("Towers\\Birmingham\\Training\\", (int)(bufferSize*windowSizeSecs/10), numbells, freqlow, freqhigh, windowSizeSecs);

      	 new File(tdir).mkdirs();
		 wav = wavs.load(tdir+"\\"+filename); 
		 System.out.println("Processing "+filename);
		 
		 int startSample=0;
		 Thread t = new Thread(new Gain1(gainTopQueue, wav, S));
		 t.start();
		 boolean looping=true;
		 while(looping) {
			 Param param = new Param();
			 param.setWindow_size_seconds(windowSizeSecs);
			 param.setHop_size_seconds(0.01);
			 param.setFreq(44100);
			 param.setNfft(4410);
			 param.setNmfpSources(12);
			 param.setAudpow(1.0);
			 param.setAlgorithm("gain");
			 param.setThreads(1);
	
			 param.set(wav.capacity(),startSample,bufferSize);
			 
			 gainTopQueue.put(param);
			 
			 startSample=startSample+bufferSize;
			 if (startSample>wav.capacity()-1) looping=false;
			 startSample=startSample-3968;
			 param=null;
		 }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
}