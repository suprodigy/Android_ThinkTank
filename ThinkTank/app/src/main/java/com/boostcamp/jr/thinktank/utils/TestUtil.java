package com.boostcamp.jr.thinktank.utils;

import android.content.Context;

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

    public static String[] keywordList = { "Android", "액티비티", "패키지", "레이아웃", "설정", "HTTP요청", "AsyncTask",
            "JSON", "RecyclerView", "인텐트", "미디어 타입", "생애주기", "로더", "Preference", "리소스", "SQLite",
            "유닛테스트", "ContentProvider", "커서", "서비스", "IntentService", "PendingIntent", "ForegroundService",
            "스케쥴링 잡", "BroadcastReceiver"};

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
        OrderedRealmCollection<KeywordItem> list = KeywordObserver.get().selectAllOrderById();

        for(KeywordItem item : list) {
            MyLog.print(item.getId() + ". " + item.getName() + " : " + "count = " + item.getCount() +
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
                    MyLog.print(responseFromNaver.getCount() + "");
                    List<ResponseFromNaver.Item> items = responseFromNaver.getItems();
                    for (ResponseFromNaver.Item item : items) {
                        MyLog.print(item.getTitle());
                    }
                } else {
                    MyLog.print("호출 실패 :" + response.errorBody());
                }

            }

            @Override
            public void onFailure(Call<ResponseFromNaver> call, Throwable t) {
                MyLog.print("오류 발생");
                t.printStackTrace();
            }
        });
    }

}
