/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.demos.sandbox;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.threshold.OtsuThreshold;
import org.openimaj.video.xuggle.XuggleVideo;

public class MultiTouchSurface implements Runnable {
	private XuggleVideo stream;
	private Thread monitorThread;
	private OtsuThreshold thresholder;

	MultiTouchSurface(){
		String sourceURL = "http://152.78.64.19:8080/foo";
		stream = new XuggleVideo(sourceURL);
	}

	private void monitor() {
		this.monitorThread = new Thread(this);
		this.thresholder = new OtsuThreshold();
		monitorThread.start();
	}
	
	public static void main(String args[]){
		MultiTouchSurface surface = new MultiTouchSurface();
		surface.monitor();
	}

	@Override
	public void run() {
		for(MBFImage image : this.stream){
			FImage gimage = Transforms.calculateIntensityNTSC(image);
//			gimage.processInline(this.thresholder);
			gimage.threshold(0.7f);
			displayFrame(gimage);
//			trackInputs(image);
//			handleInputs(image);
		}
	}

	private void displayFrame(FImage image) {
		DisplayUtilities.displayName(image, "inputFrame");
	}
}