package com.rbg.strike;

import java.nio.DoubleBuffer;

public class Param {
	double window_size_seconds=0.1;
	double hop_size_seconds=0.01;
	int freq=44100;
	int nfft=4410;
	int window_size;
	int hop_size;
	int overlap;
	int nhop;
	int nframes;
	int return_length;
	int total_num_samples;
	int this_num_samples;
	int this_start_sample;
	int this_end_sample;
	int max=6000000;
	int nmfpSources;
	double audpow=1;
	String algorithm="basis";
	double refobj = 1;
	double stop_c = .00001;
	int stop_n = 29;
	
	//
	//the following are used for gain processing only
	int threads = 1;
	int sampleOverlap;
	int numHops;
	int numHopsPerShortSection;
	int numHopsPerLongSection;
	int numLongSections;
	int max_hops_per_batch = 1000;
	
	//fft specific processing
	
	public void set(int capacity, int startSample, int bufferSize) {
		this_start_sample=startSample;
		int last_sample=capacity -1;  ///references are from zero
		if ((startSample+bufferSize-1)>last_sample) {
			this_num_samples=last_sample-startSample+1;
		 } else {
			 this_num_samples=bufferSize;
		 }
		total_num_samples=capacity;

		window_size=(int)(window_size_seconds*freq);
        hop_size=(int)(hop_size_seconds*freq);
        overlap=window_size-hop_size;
        nhop=nfft-overlap;
 //       System.out.println("This_num_samples="+this_num_samples);
        nframes=(int)(1+Math.floor((this_num_samples-overlap)/nhop));
        return_length=window_size*nframes;
        
        sampleOverlap=window_size-hop_size;
//        System.out.println("sampleOverlap="+sampleOverlap+" overlap="+overlap);
        numHops=(int)Math.floor((this_num_samples-sampleOverlap)/hop_size);
//        System.out.println("numhops="+numHops+" nhop="+nhop);
	}
	
	public double getWindow_size_seconds() {
		return window_size_seconds;
	}
	public void setWindow_size_seconds(double window_size_seconds) {
		this.window_size_seconds = window_size_seconds;
	}
	public double getHop_size_seconds() {
		return hop_size_seconds;
	}
	public void setHop_size_seconds(double hop_size_seconds) {
		this.hop_size_seconds = hop_size_seconds;
	}
	public int getFreq() {
		return freq;
	}
	public void setFreq(int freq) {
		this.freq = freq;
	}
	public int getNfft() {
		return nfft;
	}
	public void setNfft(int nfft) {
		this.nfft = nfft;
	}
	public int getWindow_size() {
		return window_size;
	}
	public void setWindow_size(int window_size) {
		this.window_size = window_size;
	}
	public int getNframes() {
		return nframes;
	}
	public void setNframes(int nframes) {
		this.nframes = nframes;
	}
	public int getReturn_length() {
		return return_length;
	}
	public void setReturn_length(int return_length) {
		this.return_length = return_length;
	}
	public int getTotal_Num_samples() {
		return total_num_samples;
	}
	public void setTotal_Num_samples(int total_num_samples) {
		this.total_num_samples = total_num_samples;
	}
	public int getMax() {
		return max;
	}
	public void setMax(int max) {
		this.max = max;
	}
	public int getHop_size() {
		return hop_size;
	}
	public void setHop_size(int hop_size) {
		this.hop_size = hop_size;
	}
	public int getOverlap() {
		return overlap;
	}
	public void setOverlap(int overlap) {
		this.overlap = overlap;
	}
	public int getNhop() {
		return nhop;
	}
	public void setNhop(int nhop) {
		this.nhop = nhop;
	}
	public int getNmfpSources() {
		return nmfpSources;
	}
	public void setNmfpSources(int nmfpSources) {
		this.nmfpSources = nmfpSources;
	}
	public double getAudpow() {
		return audpow;
	}
	public void setAudpow(double audpow) {
		this.audpow = audpow;
	}
	public String getAlgorithm() {
		return algorithm;
	}
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public int getThreads() {
		return threads;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public int getSampleOverlap() {
		return sampleOverlap;
	}

	public void setSampleOverlap(int sampleOverlap) {
		this.sampleOverlap = sampleOverlap;
	}

	public int getNumHops() {
		return numHops;
	}

	public void setNumHops(int numHops) {
		this.numHops = numHops;
	}

	public int getNumHopsPerShortSection() {
		return numHopsPerShortSection;
	}

	public void setNumHopsPerShortSection(int numHopsPerShortSection) {
		this.numHopsPerShortSection = numHopsPerShortSection;
	}

	public int getNumHopsPerLongSection() {
		return numHopsPerLongSection;
	}

	public void setNumHopsPerLongSection(int numHopsPerLongSection) {
		this.numHopsPerLongSection = numHopsPerLongSection;
	}

	public int getNumLongSections() {
		return numLongSections;
	}

	public void setNumLongSections(int numLongSections) {
		this.numLongSections = numLongSections;
	}

	public int getMax_hops_per_batch() {
		return max_hops_per_batch;
	}

	public void setMax_hops_per_batch(int max_hops_per_batch) {
		this.max_hops_per_batch = max_hops_per_batch;
	}

	public int getTotal_num_samples() {
		return total_num_samples;
	}

	public void setTotal_num_samples(int total_num_samples) {
		this.total_num_samples = total_num_samples;
	}

	public int getThis_num_samples() {
		return this_num_samples;
	}

	public void setThis_num_samples(int this_num_samples) {
		this.this_num_samples = this_num_samples;
	}

	public int getThis_start_sample() {
		return this_start_sample;
	}

	public void setThis_start_sample(int this_start_sample) {
		this.this_start_sample = this_start_sample;
	}
}
