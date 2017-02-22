package com.boostcamp.jr.thinktank;

import org.junit.Test;
import org.snu.ids.ha.index.Keyword;
import org.snu.ids.ha.index.KeywordExtractor;
import org.snu.ids.ha.index.KeywordList;

/**
 * Created by jr on 2017-02-16.
 */

public class KoreanTextUnitTest {

    @Test
    public void addition_isCorrect() throws Exception {

        // string to extract keywords
        String strToExtrtKwrd = "모든 지식의 연장은 의식적인 행동을 무의식으로 바꾸는 것에서 생겨난다.";

        // init KeywordExtractor
        KeywordExtractor ke = new KeywordExtractor();
        KeywordList kl = null;

        // extract keywords
        for (int i=0; i<10; i++) {
            kl = ke.extractKeyword(strToExtrtKwrd, true);
        }

        // print result
        for( int i = 0; i < kl.size(); i++ ) {
            Keyword kwrd = kl.get(i);
            System.out.println(kwrd.getString() + "\t" + kwrd.getCnt());
        }

//        // string to analyze
//        String string = "모든 지식의 연장은 의식적인 행동을 무의식으로 바꾸는 것에서 생겨난다.";
//
//        // init MorphemeAnalyzer
//        MorphemeAnalyzer ma = new MorphemeAnalyzer();
//
//        // create logger, null then System.out is set as a default logger
//        ma.createLogger(null);
//
//        // analyze morpheme without any post processing
//        List ret = ma.analyze(string);
//
//        // refine spacing
//        ret = ma.postProcess(ret);
//
//        // leave the best analyzed result
//        ret = ma.leaveJustBest(ret);
//
//        // divide result to setences
//        List stl = ma.divideToSentences(ret);
//
//        // print the result
//        for( int i = 0; i < stl.size(); i++ ) {
//            Sentence st = (Sentence) stl.get(i);
//            System.out.println("===>  " + st.getSentence());
//            for( int j = 0; j < st.size(); j++ ) {
//                System.out.println(st.get(j));
//            }
//        }
//
//        ma.closeLogger();

    }

}
