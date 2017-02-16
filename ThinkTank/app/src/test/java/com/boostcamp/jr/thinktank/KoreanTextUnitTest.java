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
        String strToExtrtKwrd = "모든 지식의 연장은 의식적인 행동을 무의식으로 바꾸는 것에서 생겨난다.\\n알겠지만, 상상력에는 시간 허비가 필요하다. 길고, 비효율적이며 즐거운 게으름, 꾸물거림, 어정거림. 지식은 지식이다.";

        // init KeywordExtractor
        KeywordExtractor ke = new KeywordExtractor();

        // extract keywords
        KeywordList kl = ke.extractKeyword(strToExtrtKwrd, true);

        // print result
        for( int i = 0; i < kl.size(); i++ ) {
            Keyword kwrd = kl.get(i);
            System.out.println(kwrd.getString() + "\t" + kwrd.getCnt());
        }

    }

}
