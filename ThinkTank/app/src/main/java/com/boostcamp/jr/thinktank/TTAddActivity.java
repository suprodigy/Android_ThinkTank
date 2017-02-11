package com.boostcamp.jr.thinktank;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.boostcamp.jr.thinktank.manager.KeywordManager;
import com.boostcamp.jr.thinktank.model.KeywordItem;
import com.boostcamp.jr.thinktank.model.KeywordObserver;
import com.boostcamp.jr.thinktank.model.ThinkItem;
import com.boostcamp.jr.thinktank.model.ThinkObserver;

import org.lucasr.dspec.DesignSpec;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import io.realm.RealmList;

public class TTAddActivity extends AppCompatActivity {

    @BindView(R.id.activity_tt_add)
    View mLayout;

    @BindViews({R.id.keyword1, R.id.keyword2, R.id.keyword3})
    List<EditText> mKeywords;

    @BindView(R.id.think_content)
    EditText mContent;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tt_add);
        ButterKnife.bind(this);

        DesignSpec background = DesignSpec.fromResource(mLayout, R.raw.background);
        mLayout.getOverlay().add(background);
    }

    @Override
    protected void onPause() {
        super.onPause();

        RealmList<KeywordItem> keywords = new RealmList<>();
        for(int i=0; i<mKeywords.size(); i++) {
            String keyword = mKeywords.get(i).getText().toString();
            if (keyword.length() != 0) {
                if (keyword.charAt(0) != '#') {
                    KeywordManager.get().createOrUpdateKeyword(keyword);
                    KeywordItem item = KeywordObserver.get().getKeywordByName(keyword);
                    keywords.add(item);
                }
            }
        }

        ThinkObserver.get().insert(new ThinkItem()
                .setContent(mContent.getText().toString())
                .setKeywords(keywords));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
