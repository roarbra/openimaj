/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
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
package org.openimaj.tools.twitter.modes.preprocessing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.openimaj.text.nlp.language.LanguageDetector.WeightedLocale;
import org.openimaj.twitter.TwitterStatus;
import org.terrier.terms.EnglishSnowballStemmer;

/**
 * A gateway class which loads and uses the #PorterEnglishStemmingFilter
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class StemmingMode extends TwitterPreprocessingMode<List<String>> {
	
	final static String STEMMED = "stemmed";
	private TwitterPreprocessingMode<Map<String,Object>> langMode;
	private TwitterPreprocessingMode<Map<String,List<String>>> tokMode;
	private EnglishSnowballStemmer stemmer;

	/**
	 * Loads the language detector
	 * @throws IOException 
	 */
	public StemmingMode() throws IOException {
		try {
			langMode = new LanguageDetectionMode();
			tokMode = new TokeniseMode();
			stemmer = new EnglishSnowballStemmer(null);
		} catch (Exception e) {
			throw new IOException("Couldn't create required language detector and tokeniser",e);
		}
	}

	@Override
	public List<String> process(TwitterStatus twitterStatus) {
		List<String> stems = new ArrayList<String>();
		try {
			Map<String,Object> localeMap = TwitterPreprocessingMode.results(twitterStatus,langMode);
			WeightedLocale locale = WeightedLocale.fromMap(localeMap);
			if(locale.getLocale().equals(Locale.ENGLISH)){
				Map<String,List<String>> tokens = TwitterPreprocessingMode.results(twitterStatus,tokMode);
				HashSet<String> protectedToks = new HashSet<String>();
				protectedToks.addAll(tokens.get(TokeniseMode.TOKENS_PROTECTED));
				for (String token : tokens.get(TokeniseMode.TOKENS_ALL)) {
					if(! protectedToks.contains(token)) {
						stems.add(stemmer.stem(token));
					}
					else{
						stems.add(token);
					}
					
				}
			}
		} catch (Exception e) { }
		twitterStatus.addAnalysis(STEMMED, stems);
		return stems;	
		
	}
	@Override
	public String getAnalysisKey() {
		return StemmingMode.STEMMED;
	}

}
