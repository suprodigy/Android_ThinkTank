package com.boostcamp.jr.thinktank.utils;

import android.content.Context;
import android.util.Log;

import com.boostcamp.jr.thinktank.R;
import com.boostcamp.jr.thinktank.manager.KeywordManager;
import com.boostcamp.jr.thinktank.model.KeywordItem;
import com.boostcamp.jr.thinktank.model.KeywordObserver;
import com.boostcamp.jr.thinktank.model.ThinkItem;
import com.boostcamp.jr.thinktank.model.ThinkObserver;
import com.boostcamp.jr.thinktank.network.NaverRestClient;
import com.boostcamp.jr.thinktank.network.ResponseFromNaver;

import java.util.ArrayList;
import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.RealmList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by jr on 2017-02-15.
 */

public class TestUtil {

    public static String[] keywordList = { "놓치다", "대기", "독립", "돌아보다", "또다시", "머릿속", "북쪽",
            "불안하다", "쇠고기", "위반", "주", "카드", "평생", "해당하다", "간부", "관념", "굉장히", "단어",
            "덮다", "도와주다", "도입", "몰다", "배우", "비추다", "신발", "알", "앞서다", "여건", "오래전",
            "자격", "통제", "계단", "김치", "끄덕이다", "낯설다", "높이", "닮다", "마음속", "못지않다" };

    public static void generateThink(Context context) {
        ThinkItem thinkItem = new ThinkItem();

        List<String> keywordStrings = new ArrayList<>();

        int a = (int)(Math.random() * 3) + 1;
        for (int i=0; i<a; i++) {
            int idx = (int)(Math.random() * keywordList.length);
            keywordStrings.add(keywordList[idx]);
        }

        RealmList<KeywordItem> keywords = new RealmList<>();
        for (String keywordName : keywordStrings) {
            KeywordManager.get().createOrUpdateKeyword(keywordName);
            keywords.add(KeywordObserver.get().getKeywordByName(keywordName));
        }

        thinkItem.setContent(context.getString(R.string.korean_lorem_ipsum));
        thinkItem.setKeywords(keywords);
        ThinkObserver.get().insert(thinkItem);
    }

    public static void checkKeyword() {
        OrderedRealmCollection<KeywordItem> list = KeywordObserver.get().selectAll();

        for(KeywordItem item : list) {
            Log.d("ThinkTank", item.getId() + ". " + item.getName() + " : " + "count = " + item.getCount() +
                ", relation = " + toBinary(item.getRelation()));
        }
    }

    private static String toBinary( byte[] bytes ) {
        StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
        for( int i = 0; i < Byte.SIZE * bytes.length; i++ )
            sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        return sb.toString();
    }

    public static void networkTest() {
        String content = "메모";

        NaverRestClient<NaverRestClient.KeywordService> client = new NaverRestClient<>();
        NaverRestClient.KeywordService service = client.getClient(NaverRestClient.KeywordService.class);

        Call<ResponseFromNaver> call = service.getKeywordsFromNaver("search", "blog.json", content);
        call.enqueue(new Callback<ResponseFromNaver>() {
            @Override
            public void onResponse(Call<ResponseFromNaver> call, Response<ResponseFromNaver> response) {

                if (response.isSuccessful()) {
                    ResponseFromNaver responseFromNaver = response.body();
                    Log.d("TEST" , responseFromNaver.getCount() + "");
                    List<ResponseFromNaver.Item> items = responseFromNaver.getItems();
                    for (ResponseFromNaver.Item item : items) {
                        Log.d("TEST", item.getTitle());
                    }
                } else {
                    Log.d("TEST", "호출 실패 :" + response.errorBody());
                }

            }

            @Override
            public void onFailure(Call<ResponseFromNaver> call, Throwable t) {
                Log.d("TEST", "오류 발생");
                t.printStackTrace();
            }
        });
    }

}
