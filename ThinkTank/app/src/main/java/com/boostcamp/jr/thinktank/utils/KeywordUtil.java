package com.boostcamp.jr.thinktank.utils;

import android.content.Context;
import android.util.Pair;
import android.util.SparseArray;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.boostcamp.jr.thinktank.model.KeywordObserver;

import org.snu.ids.ha.index.Keyword;
import org.snu.ids.ha.index.KeywordExtractor;
import org.snu.ids.ha.index.KeywordList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jr on 2017-02-12.
 */

public class KeywordUtil {

    public static final String TAG = "KeywordUtil";

    public static SparseArray<Integer> orderMap = new SparseArray<>();

    public static String removeTag(String keyword) {
        int startIdx = 0;
        try {
            while (keyword.charAt(startIdx) == '#') {
                startIdx++;
            }
            return keyword.substring(startIdx);
        } catch (StringIndexOutOfBoundsException e) {
            return "";
        }
    }

    /**
     * 달팽이 모양 순서대로 배열을 순회하면서, (접근순서, 5 * row + col)을 (key, value)로 맵에 저장
     * 배열을 왼쪽에서 오른쪽으로, 위에서 아래로 순회하면서 해당 요소에 값을 붙인다면 5 * row + col이 됨.
     *
     * @param cnt 접근 순서
     * @return 이에 해당하는 row와 col 값 반환
     */
    //
    public static int getOrderFromCount(int cnt) {

        if (orderMap.size() == 0) {
            int size = 5;

            int i = 0, row = size/2, col = size/2;
            orderMap.append(i, row*5 + col);

            int cntToMove = 1, s = 1;
            while (i < size * size) {
                for (int j=1; j<=cntToMove; j++) {
                    i++;
                    col += s;
                    orderMap.append(i, row*5 + col);
                }

                for (int j=1; j<=cntToMove; j++) {
                    i++;
                    row -= s;
                    orderMap.append(i, row*5 + col);
                }

                cntToMove += 1;
                s *= -1;
            }
        }

        return orderMap.get(cnt);

    }

    /**
     *
     * @param count 현재 size를 구해야 할 keyword의 count
     * @param minMaxCount: BFS의 결과로 나온 keyword의 List에서 count 최소값과 최대값
     * @return min, max에 대해 count의 상대적인 크기를 구함
     *
     * textSize는 15sp~30sp 사이의 값으로 제한
     *
     */
    public static float getTextSize(int count, Pair<Integer, Integer> minMaxCount) {

        int min = minMaxCount.first, max = minMaxCount.second;

        if (min == max) {
            return (30 + 15) / 2;
        } else {

            // y = ax + b (y: textSize, x: keyword.getCount())
            float a = 15 / (max - min);
            float b = 15 - a * min;

            return a * count + b;
        }

    }

    /**
     * twitter-korean-text library를 이용하여, Text에서 명사들만 뽑아냄
     * 물론 명사만 키워드가 될 있는 건 아니지만, 현재는 명사들만 추출해서 Keyword 찾는데 이용
     * Android에서 사용 불가 -> 다른 것으로 수정
     */
//    public static List<String> getNounsFromText(String text) {
//
//        // text에서 오탈자를 수정
//        // ex) 이닼ㅋㅋㅋ -> 이다ㅋㅋㅋ
//        CharSequence nomalized = TwitterKoreanProcessorJava.normalize(text);
//
//        MyLog.print(text.equals("메모를 입력하세요") ? "true" : "false");
//        MyLog.print(nomalized.toString());
//        MyLog.print("size : " + nomalized.length());
//
//        // nomalized된 text를 토큰화 (품사별로 분리)
//        Seq<KoreanTokenizer.KoreanToken> tokens = TwitterKoreanProcessorJava.tokenize(nomalized);
//
//        MyLog.print(tokens.toString());
//
//        // 토근화된 단어를 List 형대로 가지고 있는 tokens에서 명사만 추출
//        List<KoreanPhraseExtractor.KoreanPhrase> phrases =
//                TwitterKoreanProcessorJava.extractPhrases(tokens, true, true);
//
//        MyLog.print(phrases.toString());
//
//        // 명사로 된 단어들만 리스트에 담아서 리턴
//        List<String> ret = new ArrayList<>();
//        for (KoreanPhraseExtractor.KoreanPhrase phrase : phrases) {
//            String noun = phrase.text();
//            if (noun.charAt(0) == '#') {
//                noun = KeywordUtil.removeTag(noun);
//            }
//            ret.add(noun);
//        }
//
//        return ret;
//    }

    public static String getKeywordFromContent(KeywordExtractor ke, String content) {

        // extract keywords
        KeywordList kl = ke.extractKeyword(content, true);

        MyLog.print("2...............................");

        // print result
        for( int i = 0; i < kl.size(); i++ ) {
            Keyword kwrd = kl.get(i);
        }

        MyLog.print("3................................");

        int cnt = 0;
        while(kl.get(cnt).getCnt()
                == (kl.get(cnt+1).getCnt())) {
            cnt++;
        }

        int idx = (int)(Math.random() * cnt);
        MyLog.print("idx : " + idx + ".............................");
        return kl.get(idx).getString();
    }

    public static void addAutoCompleteOnTextView(Context context, AutoCompleteTextView textView) {
        List<String> items = KeywordObserver.get().getAllKeywordNames();

        textView.setAdapter(new ArrayAdapter<String>(
                context,
                android.R.layout.simple_dropdown_item_1line,
                items
        ));
    }

    public static List<Integer> getRandomNumbers() {

        List<Integer> ret = new ArrayList<>();
        boolean[] checked = new boolean[25];

        int cnt = 1;
        ret.add(0);
        checked[0] = true;

        while(cnt < 25) {
            int num = (int)(Math.random()*25);
            if (!checked[num]) {
                ret.add(num);
                cnt++;
                checked[num] = true;
            }
        }

        return ret;

    }

}
