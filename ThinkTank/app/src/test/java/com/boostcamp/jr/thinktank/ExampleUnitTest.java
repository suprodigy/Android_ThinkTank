package com.boostcamp.jr.thinktank;

import com.twitter.penguin.korean.TwitterKoreanProcessorJava;
import com.twitter.penguin.korean.phrase_extractor.KoreanPhraseExtractor;
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer;

import org.junit.Test;

import java.util.List;

import scala.collection.Seq;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        String text = "<b>메모</b>를 입력하세요";

        // Normalize
        CharSequence normalized = TwitterKoreanProcessorJava.normalize(text);
        System.out.println(normalized);


        // Tokenize
        Seq<KoreanTokenizer.KoreanToken> tokens = TwitterKoreanProcessorJava.tokenize(normalized);
        System.out.println(TwitterKoreanProcessorJava.tokensToJavaStringList(tokens));
        System.out.println(TwitterKoreanProcessorJava.tokensToJavaKoreanTokenList(tokens));


        // Stemming
        Seq<KoreanTokenizer.KoreanToken> stemmed = TwitterKoreanProcessorJava.stem(tokens);
        System.out.println(TwitterKoreanProcessorJava.tokensToJavaStringList(stemmed));
        System.out.println(TwitterKoreanProcessorJava.tokensToJavaKoreanTokenList(stemmed));


        // Phrase extraction
        List<KoreanPhraseExtractor.KoreanPhrase> phrases = TwitterKoreanProcessorJava.extractPhrases(tokens, true, true);
        System.out.println(phrases);
    }
}