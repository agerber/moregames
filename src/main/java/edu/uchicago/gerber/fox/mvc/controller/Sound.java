package edu.uchicago.gerber.fox.mvc.controller;


import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Sound {


	//for individual wav sounds (not looped)
	//http://stackoverflow.com/questions/26305/how-can-i-play-sound-in-java
	public static void playSound(final String strPath) {

		CommandCenter.getInstance().getSoundExecutor().execute(new Runnable() {
			public void run() {
				try {
					Clip clp = AudioSystem.getClip();

					InputStream audioSrc = Sound.class.getResourceAsStream("/fox/sounds/" + strPath);
					InputStream bufferedIn = new BufferedInputStream(audioSrc);
					AudioInputStream aisStream = AudioSystem.getAudioInputStream(bufferedIn);

					clp.open(aisStream);
					clp.start();
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}
		});

	}
	
	
	//for looping wav clips
	//http://stackoverflow.com/questions/4875080/music-loop-in-java
	public static Clip clipForLoopFactory(String fileName){

		Clip clip = null;
		try {
			String relativePath = "/fox/sounds/" + fileName;
			InputStream audioSrc = Sound.class.getResourceAsStream(relativePath);

			if (audioSrc == null)
				throw new IOException("No such sound file exists at " + relativePath);

			InputStream bufferedIn = new BufferedInputStream(audioSrc);
			AudioInputStream aisStream = AudioSystem.getAudioInputStream(bufferedIn);
			clip = AudioSystem.getClip();
		    clip.open( aisStream );

		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			e.printStackTrace();
		}

		return clip;

	}
	
	


}
