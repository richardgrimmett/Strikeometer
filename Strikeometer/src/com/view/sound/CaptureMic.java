package com.view.sound;
 
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PipedOutputStream;
 
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.Port;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;

import com.rbg.strike.Param;
import com.rbg.strike.WavFile;
import com.rbg.strike.WavFileException;
 
public class CaptureMic extends JPanel implements ActionListener {
    final int bufSize = 16384;
    static BlockingQueue<Double> queue = new ArrayBlockingQueue<Double>(600000);
    static BlockingQueue<String> controlqueue = new ArrayBlockingQueue<String>(50);
 
    Capture capture = new Capture();
 
    Playback playback = new Playback();
 
    AudioInputStream audioInputStream;
 
    JButton playB, captB;
 
    JTextField textField;
 
    String errStr;
 
    double duration, seconds;
 
    File file;
 
    public CaptureMic() {
        setLayout(new BorderLayout());
        EmptyBorder eb = new EmptyBorder(5, 5, 5, 5);
        SoftBevelBorder sbb = new SoftBevelBorder(SoftBevelBorder.LOWERED);
        setBorder(new EmptyBorder(5, 5, 5, 5));
 
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
 
        JPanel p2 = new JPanel();
        p2.setBorder(sbb);
        p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
 
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBorder(new EmptyBorder(10, 0, 5, 0));
        playB = addButton("Play", buttonsPanel, false);
        captB = addButton("Record", buttonsPanel, true);
        p2.add(buttonsPanel);
 
        p1.add(p2);
        add(p1);
    }
 
    public void open() {
    }
 
    public void close() {
        if (playback.thread != null) {
            playB.doClick(0);
        }
        if (capture.thread != null) {
            captB.doClick(0);
        }
    }
 
    private JButton addButton(String name, JPanel p, boolean state) {
        JButton b = new JButton(name);
        b.addActionListener(this);
        b.setEnabled(state);
        p.add(b);
        return b;
    }
 
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj.equals(playB)) {
            if (playB.getText().startsWith("Play")) {
                playback.start();
                captB.setEnabled(false);
                playB.setText("Stop");
            } else {
                playback.stop();
                captB.setEnabled(true);
                playB.setText("Play");
            }
        } else if (obj.equals(captB)) {
            if (captB.getText().startsWith("Record")) {
                capture.start(queue,controlqueue);
                playB.setEnabled(false);
                captB.setText("Stop");
            } else {
                capture.stop();
                playB.setEnabled(true);
            }
 
        }
    }
 
    /**
     * Write data to the OutputChannel.
     */
    public class Playback implements Runnable {
 
        SourceDataLine line;
 
        Thread thread;
 
        public void start() {
            errStr = null;
            thread = new Thread(this);
            thread.setName("Playback");
            thread.start();
        }
 
        public void stop() {
            thread = null;
        }
 
        private void shutDown(String message) {
            if ((errStr = message) != null) {
                System.err.println(errStr);
            }
            if (thread != null) {
                thread = null;
                captB.setEnabled(true);
                playB.setText("Play");
            }
        }
 
        public void run() {
 
            // make sure we have something to play
            if (audioInputStream == null) {
                shutDown("No loaded audio to play back");
                return;
            }
            // reset to the beginnning of the stream
            try {
                audioInputStream.reset();
            } catch (Exception e) {
                shutDown("Unable to reset the stream\n" + e);
                return;
            }
 
            // get an AudioInputStream of the desired format for playback
 
            AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
            float rate = 44100.0f;
            int channels = 2;
            int frameSize = 4;
            int sampleSize = 16;
            boolean bigEndian = true;
 
            AudioFormat format = new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8) * channels, rate, bigEndian);
 
            AudioInputStream playbackInputStream = AudioSystem.getAudioInputStream(format, audioInputStream);
 
            if (playbackInputStream == null) {
                shutDown("Unable to convert stream of format " + audioInputStream + " to format " + format);
                return;
            }
 
            // define the required attributes for our line,
            // and make sure a compatible line is supported.
 
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                shutDown("Line matching " + info + " not supported.");
                return;
            }
 
            // get and open the source data line for playback.
 
            try {
                line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format, bufSize);
            } catch (LineUnavailableException ex) {
                shutDown("Unable to open the line: " + ex);
                return;
            }
 
            // play back the captured audio data
 
            int frameSizeInBytes = format.getFrameSize();
            int bufferLengthInFrames = line.getBufferSize() / 8;
            int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
            byte[] data = new byte[bufferLengthInBytes];
            int numBytesRead = 0;
 
            // start the source data line
            line.start();
 
            while (thread != null) {
                try {
                    if ((numBytesRead = playbackInputStream.read(data)) == -1) {
                        break;
                    }
                    int numBytesRemaining = numBytesRead;
                    while (numBytesRemaining > 0) {
                        numBytesRemaining -= line.write(data, 0, numBytesRemaining);
                    }
                } catch (Exception e) {
                    shutDown("Error during playback: " + e);
                    break;
                }
            }
            // we reached the end of the stream.
            // let the data play out, then
            // stop and close the line.
            if (thread != null) {
                line.drain();
            }
            line.stop();
            line.close();
            line = null;
            shutDown(null);
        }
    } // End class Playback
    
    class Consume implements Runnable {
        
     
        private BlockingQueue<Double> queue;
        private BlockingQueue<String> controlqueue;
        private WavFile wavfile;
        private int fileno=0;
        private double amplitudeFactor=1;
        
        
        public Consume(BlockingQueue<Double> queue, BlockingQueue<String> controlqueue) {
        	System.out.println("Starting queue");
        	this.queue=queue;
        	this.controlqueue=controlqueue;
        }
        
        public void makeWav(int numSamples) {
        	BufferedReader br=null;
        	double[] tmp = new double[2];
        	try {
	        	System.out.println("creating wav file");
	        	fileno++;
	        	wavfile=WavFile.newWavFile(new File("out"+fileno+".wav"), 1, numSamples, 16, 44100);
	        	String sCurrentLine;
				br = new BufferedReader(new FileReader("tmpwav.txt"));
				while ((sCurrentLine = br.readLine()) != null) {
					tmp[0]=amplitudeFactor*Double.parseDouble(sCurrentLine);
					wavfile.writeFrames(tmp, 1);
				}	
				wavfile.close();
        	
				System.out.println("out.wav created");
        	} catch (Exception e) {e.printStackTrace();	}
        }
        
        public void run() {
        	
        	int j=0;
        	try {
        		boolean recording=false;
        		String cmd=null;
        		PrintWriter writer=null;
        		int numSamples=0;
	        	while (1==1) {
	        		while (cmd==null) {
	        			cmd=controlqueue.poll(200,TimeUnit.MILLISECONDS);
	        		}
	        		if (cmd.equals("start")) {
	        			writer = new PrintWriter("tmpwav.txt");
	        			recording=true;
	        		}
	        			
	        		if (cmd.equals("stop")) {
	        			if (numSamples>0) {
	        				writer.close();
	        				makeWav(numSamples);
        					numSamples=0;
        					recording=false;
        				}
	        		}
	        		cmd=null;
	        		while (recording) {
	        			double tmp=queue.take();
	        			if (tmp==999999.0) {
	        				recording=false;
	        			} else {
	        				String rec=tmp+"\n";
	        				writer.write(rec);
	        				numSamples++;
				        	j++;
				        	if (j>44100) {System.out.println("Batch Consumed");j=0;}
	        			}	
	        		}	
	        	
	        	}
        	} catch (Exception e) {e.printStackTrace();}	
        }
    }
 
    /**
     * Reads data from the input channel and writes to the output stream
     */
    class Capture implements Runnable {
 
        TargetDataLine line;
 
        Thread thread;
        
        BlockingQueue<Double> queue;
        BlockingQueue<String> controlqueue;
        boolean recording = false;
    
        public void start(BlockingQueue<Double> queue, BlockingQueue<String> controlqueue) {
        	this.queue=queue;
        	this.controlqueue=controlqueue;
            errStr = null;
            thread = new Thread(this);
            thread.setName("Capture");
            thread.start();
        }
 
        public void stop() {
            thread = null;
        }
 
        private void shutDown(String message) {
            if ((errStr = message) != null && thread != null) {
                thread = null;
                playB.setEnabled(true);
                captB.setText("Record");
                System.err.println(errStr);
            }
        }
        
        private double[] bytesToDoubleArray(byte[] bufferData){
        	double[] val= new double[bufferData.length/2];
        	try {
 	        	for (int i=1;i<val.length;i++) val[i]=0;
	        	int j=0;
	        	int v1,v0;
	        	for (int i=0; i<bufferData.length; i=i+2) {
	        		v1=bufferData[i+1];
	        		v1 = v1 &= 0xFF;
	     			val[j]+=v1 << (0 * 8);
	     			v0=bufferData[i];
	        		val[j]+=v0 << (1*8);
	        		val[j]=val[j]/32767.0;				
	     			j++;
	        	}	
        	} catch (Exception e) {e.printStackTrace();}	
    		return val;
        }
 
        public void run() {
        	//next variables are parameters for the volume controls
            double threshold=0.1;
            int recvolcount=0;
            int secsBefore=3;			//number of seconds that are included at the beginning of the recording before recording is triggered
            int numSamplesBefore = 44100*secsBefore;
            int secsAfter=3;			// number of seconds after volume has dropped that are included in the recording
            int numBatchesAfter = 8*secsAfter;
            int secsRecord=1;   		//duration over which we must have reached threshold before recording is triggered
            int numBatchesRecord=secsRecord*8;
            
            duration = 0;
            audioInputStream = null;
 
            // define the required attributes for our line,
            // and make sure a compatible line is supported.
 
            AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
            float rate = 44100.0f;
            int channels = 1;
            int frameSize = 4;
            int sampleSize = 16;
            boolean bigEndian = true;
 
            AudioFormat format = new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8) * channels, rate, bigEndian);
 
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
 
            if (!AudioSystem.isLineSupported(info)) {
                shutDown("Line matching " + info + " not supported.");
                return;
            } 
            try {
            	final Port.Info myInputType = new Port.Info((Port.class), "Volume", true);
            for (Info mixerInfo : AudioSystem.getMixerInfo()) {
            	System.out.println(mixerInfo.getName()+" "+mixerInfo.getClass());
            	Mixer targetMixer = AudioSystem.getMixer(mixerInfo);
                targetMixer.open();
            		for (Info mifo : AudioSystem.getMixerInfo()) {
            			String port_string = "Port ";
                        if ((port_string + mixerInfo.getName()).equals(mifo.getName())) {
                            System.out.println("Matched Port to Mixer:" + mixerInfo.getName());
                            Mixer portMixer = AudioSystem.getMixer(mifo);
                            
                            portMixer.open();
     //                       Control c=portMixer.getControl(Control.Type.);
                          
       //                     for (Control c : rbg) {
 //                           	System.out.println(c.getType());
        //                    }
                            portMixer.isLineSupported(myInputType);
                            //now check the mixer has the right input type eg LINE_IN
                            if (portMixer.isLineSupported(myInputType)) {
                                //OK we have a supported Port Type for the Mixer
                                //This has all matched (hopefully)
                                //now just get the record line
                                //There should be at least 1 line, usually 32 and possible unlimited
                                // which would be "AudioSystem.Unspecified" if we ask the mixer 
                                //but I haven't checked any of this
                                TargetDataLine td = (TargetDataLine) targetMixer.getLine(info);
                                System.out.println("Got TargetDataLine from :" + targetMixer.getMixerInfo().getName());
                                return;
                            }
                        }    
            		}
           }
            } catch (Exception e ) {}


 
            // get and open the target data line for capture.
 
            try {
                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format, line.getBufferSize());
//FloatControl control = (FloatControl)line.getControl(FloatControl.Type.);
//System.out.println("Matsre Gain ="+control.getValue());
//control.setValue(limit(control,level));             
            } catch (LineUnavailableException ex) {
                shutDown("Unable to open the line: " + ex);
                return;
            } catch (SecurityException ex) {
                shutDown(ex.toString());
                //JavaSound.showInfoDialog();
                return;
            } catch (Exception ex) {
                shutDown(ex.toString());
                return;
            }
 
            // play back the captured audio data
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int frameSizeInBytes = format.getFrameSize();
            int bufferLengthInFrames = line.getBufferSize() / 8;
            int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
            byte[] data = new byte[bufferLengthInBytes];
            int numBytesRead;
 
            
            Thread t = new Thread(new Consume(queue, controlqueue));
            t.start();

            
            line.start();
            

            try {
            	PrintWriter writer = new PrintWriter("vol.txt");
	            while (thread != null) {
	                if ((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) {
	                    break;
	                }
	                
	               	double[] samples = new double[numBytesRead/2];
	               	samples=bytesToDoubleArray(data);
	               	double volume=0;
	              	for (int i=0;i<samples.length;i++) {
	              		volume+=samples[i]*samples[i];
	              		//store the sample
	              		try {
		              		queue.put(samples[i]);
		              		if ((queue.size()>441000) && (recording)) {queue.take(); /*System.out.println("losing data");*/}
		              		if ((queue.size()>numSamplesBefore)  &&  (!recording)) {queue.take(); /*System.out.println("recycling");*/ }
	              		} catch (Exception e) {e.printStackTrace();System.exit(1);}	
	               	}
	              	volume=volume/samples.length;
	              	volume=Math.pow(volume,0.5);
//	              	System.out.println("Volume="+volume);
	              	String rec=volume+"\r\n";
	              	writer.write(rec);
	              	
	              	if (Math.abs(volume)>threshold) {
	              		if (recvolcount<numBatchesAfter) recvolcount++;
	              	} else {
	              		if (recvolcount>0) recvolcount--;
//	              		System.out.println("count="+recvolcount);
	              	}
	              	
	              	if ((recvolcount>numBatchesRecord) && (!recording)) {
	              		try {
	              			System.out.println("instruction to record");
	              			controlqueue.put("start");
	              			recording=true;
	              			recvolcount=numBatchesAfter;
	              		} catch (Exception e ) {e.printStackTrace();}
	              	}
	               	if ((recvolcount<1) && (recording)) {
	              		try {
	              			System.out.println("instruction to stop");
	              			controlqueue.put("stop");
		              		queue.put(999999.0);
		              		recording=false;
	              		} catch (Exception e) {e.printStackTrace();}	
	              	}
	              	System.out.println("recvolcount="+recvolcount);
	     //         	out.write(data, 0, numBytesRead);
	            }
	            writer.close();
	            
            } catch (Exception e) {}
            // we reached the end of the stream.
            // stop and close the line.

            line.stop();
            line.close();
            line = null;
 
            // stop and close the output stream
            try {
                out.flush();
                out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
 
            // load bytes into the audio input stream for playback
 
            byte audioBytes[] = out.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
            audioInputStream = new AudioInputStream(bais, format, audioBytes.length / frameSizeInBytes);
 
            long milliseconds = (long) ((audioInputStream.getFrameLength() * 1000) / format.getFrameRate());
            duration = milliseconds / 1000.0;
 
            try {
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, new File("output.wav"));
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
 
        }
    } // End class Capture
 
    public static void main(String s[]) {
        CaptureMic ssc = new CaptureMic();
        ssc.open();
        JFrame f = new JFrame("Capture/Playback");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add("Center", ssc);
        f.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = 360;
        int h = 170;
        f.setLocation(screenSize.width / 2 - w / 2, screenSize.height / 2 - h / 2);
        f.setSize(w, h);
        f.show();
    }
}