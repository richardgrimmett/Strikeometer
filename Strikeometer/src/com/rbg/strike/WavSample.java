package com.rbg.strike;


import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;

public class WavSample
{
	private WavFile wavFile;
	public long getNumFrames() {
		return wavFile.getNumFrames();
	}
	
   public DoubleBuffer load(String fn)
   {
	   DoubleBuffer wav=null;
	   
      try
      {
         // Open the wav file specified as the first argument
         wavFile = WavFile.openWavFile(new File(fn));

         // Display information about the wav file
         wavFile.display();

         // Get the number of audio channels in the wav file
         int numChannels = wavFile.getNumChannels();

         // Create a buffer of 100 frames
         int[] buffer = new int[100 * numChannels];

         int framesRead;
         double volume=0;
 
         
         wav = ByteBuffer.allocateDirect((int)wavFile.getNumFrames()*8)
     			.order(ByteOrder.nativeOrder()).asDoubleBuffer();
         
         do
         {
            // Read frames into buffer
            framesRead = wavFile.readFrames(buffer, 100);

            for (int s=0 ; s<framesRead * numChannels ; s++)
            {  
            	double sample = buffer[s]/32767.0;
            	wav.put(sample);
            }
         }
         while (framesRead != 0);
         
         // Close the wavFile
         wavFile.close();
         return wav;
         
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return wav;
   }

}