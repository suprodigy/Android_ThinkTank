package com.boostcamp.jr.thinktank;

import com.boostcamp.jr.thinktank.network.NaverRestClient;
import com.boostcamp.jr.thinktank.network.ResponseFromNaver;

import org.junit.Test;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() throws Exception {

        String content = "메모";

        NaverRestClient<NaverRestClient.KeywordService> client = new NaverRestClient<>();
        NaverRestClient.KeywordService service = client.getClient(NaverRestClient.KeywordService.class);

        Call<ResponseFromNaver> call = service.getKeywordsFromNaver("search", "blog.json", content);
        call.enqueue(new Callback<ResponseFromNaver>() {
            @Override
            public void onResponse(Call<ResponseFromNaver> call, Response<ResponseFromNaver> response) {

                if (response.isSuccessful()) {
                    ResponseFromNaver responseFromNaver = response.body();
                    System.out.println(responseFromNaver.getCount() + "");
                    List<ResponseFromNaver.Item> items = responseFromNaver.getItems();
                    for (ResponseFromNaver.Item item : items) {
                        System.out.println(item.getTitle());
                    }
                } else {
                    System.out.println("호출 실패 :" + response.errorBody());
                }

            }

            @Override
            public void onFailure(Call<ResponseFromNaver> call, Throwable t) {
                System.out.println("오류 발생");
                t.printStackTrace();
            }
        });

    }
}