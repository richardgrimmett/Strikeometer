package com.rbg.strike;


import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.nio.DoubleBuffer;

import org.jblas.*;

import static org.jblas.DoubleMatrix.*;

import com.rbg.fft.fftEngine;

public class Train
{	
	static rbgRandom rand = rbgRandom.getInstance();

   public static void main(String[] args)
   {
      try
      {
    	 rbgRandom.load();
    	 String tdir="Towers\\Birmingham\\Training";
    	 new File(tdir).mkdirs();
    	 
		 File dir = new File(tdir);
		 File[] files = dir.listFiles(new FilenameFilter() {
		        @Override
		        public boolean accept(File dir, String filename) {
		            return filename.endsWith("tt.wav");
		        }
		 });
		 		 
    	 for (int i=0; i<files.length; i++) {
    	 
    		 Param param = new Param();
			 WavSample wavs = new WavSample();
			 String filename=files[i].getName();
			 DoubleBuffer wav = wavs.load(tdir+"\\"+filename);

			 String bell=filename.substring(0,filename.length()-6);
			 System.out.println("Processing "+filename+" Bell: "+bell);
			 
			 param.setWindow_size_seconds(0.1);
			 param.setHop_size_seconds(0.01);
			 param.setFreq(44100);
			 param.setNfft(4410);
			 param.setNmfpSources(1);

			 param.setAudpow(1.0);
			 param.setAlgorithm("train");
			 param.set(wav.capacity(),0,99999999);

			 fftEngine fft = new fftEngine();
			 fft.fft(wav, param);
				 
			 DoubleMatrix XSig = zeros(param.getWindow_size(), param.getNframes());
			 for (int j=0; j<param.getNframes()*param.getWindow_size(); j++) {
				 XSig.put(j,fft.getReal(j));
			 }

			 NmfFactorise nmf = new NmfFactorise();
			 DoubleMatrix S = new DoubleMatrix();
			 DoubleMatrix A = new DoubleMatrix();
			 nmf.init(param, XSig, S, A);
			 double sumcol=nmf.sumColumnMatrix(A, 0);
			 A.divi(sumcol);
			 
			 PrintWriter writer = new PrintWriter(tdir+"\\"+bell+".tra", "UTF-8");
		     for (int j=0; j<A.length; j++) {
		    	 writer.println(String.valueOf(A.get(j)));
		     }
		     writer.close();
		     
    	 }	
    	 System.out.println("Finished training");
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
}