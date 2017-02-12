package com.boostcamp.jr.thinktank;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.boostcamp.jr.thinktank.utils.KeywordUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by jr on 2017-02-12.
 */

public class AddKeywordDialog extends DialogFragment {

    public static final String EXTRA_KEYWORD =
            "com.boostcamp.jr.thinktank.keyword";

    @BindView(R.id.keyword_edit_text)
    EditText mKeywordEditText;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_add_keyword, null);

        ButterKnife.bind(this, v);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.dialog_title)
                .setPositiveButton(R.string.dialog_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String keyword = mKeywordEditText.getText().toString();
                                if (keyword.length() != 0) {
                                    keyword = KeywordUtil.removeTag(keyword);
                                    TTDetailActivity activity = (TTDetailActivity) getActivity();
                                    activity.getKeywordFromDialog(keyword);
                                }
                            }
                        })
                .setNegativeButton(R.string.dialog_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                .create();
    }

}
