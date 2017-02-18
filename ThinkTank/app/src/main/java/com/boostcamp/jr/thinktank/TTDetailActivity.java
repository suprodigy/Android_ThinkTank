package com.boostcamp.jr.thinktank;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.util.Pair;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.boostcamp.jr.thinktank.image.ImagePagerActivity;
import com.boostcamp.jr.thinktank.image.ImageRepository;
import com.boostcamp.jr.thinktank.manager.KeywordManager;
import com.boostcamp.jr.thinktank.model.KeywordItem;
import com.boostcamp.jr.thinktank.model.KeywordObserver;
import com.boostcamp.jr.thinktank.model.ThinkItem;
import com.boostcamp.jr.thinktank.model.ThinkObserver;
import com.boostcamp.jr.thinktank.utils.KeywordUtil;
import com.boostcamp.jr.thinktank.utils.MyLog;
import com.boostcamp.jr.thinktank.utils.PhotoUtil;
import com.github.clans.fab.FloatingActionButton;

import org.lucasr.dspec.DesignSpec;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.realm.RealmList;
import me.drakeet.materialdialog.MaterialDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

// TODO (1) 키워드 추출 기능 추가 (Retrofit 이용) - pass
// TODO (3) keyword 추가/삭제 UX 수정 - Click/LongClick effect

/**
 *
 * 메모 추가/수정 작업 동시에 처리하게 구현
 * Intent를 통해 넘어온 Extra가 없으면 -> 메모 추가 작업
 * Intent를 통해 넘어온 Extra가 있으면 -> 메모 수정 작업
 *
 */

public class TTDetailActivity extends MyActivity {

    // TTListActivity가 newIntent 메소드를 통해 TTDetailActivity를 부르는 Intent를 얻음
    // EXTRA_POSITION은 Intent Extra의 Key 값
    public static final String EXTRA_ID = "com.boostcamp.jr.thinktank.id";

    private static final int REQUEST_PHOTO = 0;
    private static final int REQUEST_LOAD_IMAGE = 1;

    public static Intent newIntent(Context packageContext, String id) {
        Intent intent = new Intent(packageContext, TTDetailActivity.class);
        intent.putExtra(EXTRA_ID, id);
        return intent;
    }

    // Fields...
    private ThinkItem mThinkItem;
    private boolean mDeleted;
    private boolean mIsAdded;
    private List<Pair<String, Boolean>> mKeywordStrings = new ArrayList<>();
    private File mTemporaryFile;
    private List<File> mImageFiles = new ArrayList<>();
    private ImageAdapter mImageAdapter;

    // MaterialDialog library 사용을 위해 정의한 객체
    private MaterialDialog mDialog;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.toolbar_title)
    TextView mTitle;

    @BindView(R.id.activity_tt_detail)
    View mLayout;

    @BindView(R.id.layout_for_image)
    RecyclerView mLayoutForImage;

    @BindView(R.id.think_keyword)
    TextView mKeywordTextView;

    @BindView(R.id.think_content)
    EditText mContentEditText;

    @BindView(R.id.take_photo_button)
    FloatingActionButton mTakePhotoButton;

    @BindView(R.id.share_button)
    FloatingActionButton mShareButton;

    @BindView(R.id.delete_button)
    FloatingActionButton mDeleteButton;


    @OnClick(R.id.think_keyword)
    void onKeywordViewClicked() {
        if (mKeywordStrings.size() == 3) {
            Toast.makeText(this, R.string.cannot_add_keyword, Toast.LENGTH_SHORT).show();
        } else {
            showAddTagDialog();
        }
    }

    /**
     * 태그를 추가하기 위한 대화상자를 띄움.
     * 대화 상자에서 확인 버튼을 눌렀을 때, Empty String이 입력되지 않았으면 getKeywordFromDialog() 호출
     */

    private void showAddTagDialog() {
        View v = getLayoutInflater().inflate(R.layout.dialog_add_keyword, null);

        final EditText keywordEditText = (EditText) v.findViewById(R.id.keyword_edit_text);

        mDialog = new MaterialDialog(this)
                .setView(v)
                .setPositiveButton(R.string.add_dialog_ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String keyword = keywordEditText.getText().toString();
                        if (keyword.length() != 0) {
                            keyword = KeywordUtil.removeTag(keyword);
                            getKeywordFromDialog(keyword);
                            mDialog.dismiss();
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                    }
                });

        mDialog.show();
    }

    @OnClick(R.id.extract_keyword)
    void onExtractButtonClicked() {
//        if (mThinkItem.getContent().length() != 0) {
//            new GetKeywordTask().execute("TEst");
//        }
    }

    @OnClick(R.id.take_photo_button)
    void onTakePhotoButtonClicked() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String fileName = mThinkItem.getId() + "_" + mImageFiles.size() + ".jpg";
        mTemporaryFile = PhotoUtil.getPhotoFile(this, fileName);

        if (mTemporaryFile == null) {
            Toast.makeText(this, getString(R.string.cannot_take_photo), Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = Uri.fromFile(mTemporaryFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, REQUEST_PHOTO);
    }

    @OnClick(R.id.get_image_from_gallery)
    void onImageButtonClicked() {
        Intent i = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUEST_LOAD_IMAGE);
    }

    @OnClick(R.id.share_button)
    void onShareButtonClicked() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, mThinkItem.getContent());
        i.putExtra(Intent.EXTRA_SUBJECT, mKeywordTextView.getText().toString());
        i = Intent.createChooser(i, getString(R.string.share_think));
        startActivity(i);
    }

    @OnClick(R.id.delete_button)
    void onDeleteButtonClicked() {
        ThinkObserver.get().delete(this, mThinkItem);
        mDeleted = true;
        finish();
    }

    @OnLongClick(R.id.think_keyword)
    boolean onKeywordViewLongClicked() {
        showDeleteKeywordDialog();
        return true;
    }

    private void showDeleteKeywordDialog() {
        View v = getLayoutInflater().inflate(R.layout.dialog_delete_keyword, null);

        View layoutIfKeywordsExist = v.findViewById(R.id.layout_if_keywords_exist);
        View layoutIfKeywordsNotExist = v.findViewById(R.id.layout_if_keywords_not_exist);

        final List<CheckBox> checkBoxes = new ArrayList<>();

        if (mKeywordStrings.isEmpty()) {
            layoutIfKeywordsExist.setVisibility(View.INVISIBLE);
            layoutIfKeywordsNotExist.setVisibility(View.VISIBLE);
        } else {
            LinearLayout layoutForCheckbox = (LinearLayout) v.findViewById(R.id.layout_for_checkbox);

            for (Pair<String, Boolean> pair : mKeywordStrings) {
                String keyword = pair.first;
                CheckBox checkBox = new CheckBox(this);
                checkBox.setPadding(8, 32, 8, 32);
                checkBox.setText("#" + keyword);
                checkBox.setTextColor(getResources().getColor(R.color.blue));
                CalligraphyUtils.applyFontToTextView(this, checkBox, "fonts/NanumPen.ttf");
                checkBoxes.add(checkBox);
                layoutForCheckbox.addView(checkBox);
            }
        }

        mDialog = new MaterialDialog(this)
                .setView(v)
                .setNegativeButton(R.string.dialog_cancel, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                    }
                });

        if (!mKeywordStrings.isEmpty()) {
            mDialog.setPositiveButton(R.string.delete_dialog_ok, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onKeywordDeleted(checkBoxes);
                    mDialog.dismiss();
                }
            });
        }

        mDialog.show();
    }

    private void onKeywordDeleted(List<CheckBox> checkBoxes) {
        for (int i=0; i<checkBoxes.size(); i++) {
            CheckBox checkBox = checkBoxes.get(i);
            if (checkBox.isChecked()) {
                String keywordName = checkBox.getText().toString();
                keywordName = KeywordUtil.removeTag(keywordName);

                if (mKeywordStrings.get(i).second) {
                    KeywordObserver keywordObserver = KeywordObserver.get();
                    KeywordItem item = keywordObserver
                            .getCopiedObject(keywordObserver.getKeywordByName(keywordName));
                    item.setCount(item.getCount() - 1);
                    keywordObserver.update(item);
                }
            }
        }
        for (int i=checkBoxes.size()-1; i>=0; i--) {
            CheckBox checkBox = checkBoxes.get(i);
            if (checkBox.isChecked()) {
                mKeywordStrings.remove(i);
            }
        }
        setKeywordTextView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tt_detail);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            MyLog.print("No action bar in TTDetailActivity");
            e.printStackTrace();
        }

        DesignSpec background = DesignSpec.fromResource(mLayout, R.raw.background);
        mLayout.setBackground(background);

        init();
        setEventListener();
    }

    private void init() {
        mDeleted = false;
        String id = getIntent().getStringExtra(EXTRA_ID);

        initImageButton();

        if (id == null) {
            setIfItemNotAdded();
        } else {
            setIfItemAdded(id);
        }

        initImageAdapter();
    }

    private void initImageAdapter() {
        String pathsString = mThinkItem.getImagePaths();

        if (pathsString == null || pathsString.length() == 0) {

            mImageAdapter = new ImageAdapter(null);

        } else {

            MyLog.print(pathsString);
            String[] pathStrings = pathsString.split(",");
            for (String pathString : pathStrings) {
                File photoFile = new File(pathString);
                mImageFiles.add(photoFile);
            }

            mImageAdapter = new ImageAdapter(mImageFiles);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mLayoutForImage.setLayoutManager(layoutManager);
        mLayoutForImage.setAdapter(mImageAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mLayoutForImage.getContext(),
                layoutManager.getOrientation());
        mLayoutForImage.addItemDecoration(dividerItemDecoration);
        setSwipeEvent();
    }

    private void setSwipeEvent() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.UP | ItemTouchHelper.DOWN) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                File photoFile = mImageFiles.get(position);
                removeFile(photoFile);
            }
        }).attachToRecyclerView(mLayoutForImage);
    }

    private void removeFile(File photoFile) {
        if (PhotoUtil.isMyImage(getApplicationContext(), photoFile)) {
            photoFile.delete();
        }
        mImageFiles.remove(photoFile);
        mImageAdapter.swapFiles(mImageFiles);
        mImageAdapter.notifyDataSetChanged();
    }

    private void initImageButton() {
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        PackageManager packageManager = getPackageManager();
        boolean canTakePhoto = captureImage.resolveActivity(packageManager) != null;
        mTakePhotoButton.setEnabled(canTakePhoto);
    }

    private void setIfItemNotAdded() {
        mIsAdded = false;
        mThinkItem = new ThinkItem();
        mShareButton.setEnabled(false);
        mDeleteButton.setEnabled(false);
    }

    private void setIfItemAdded(String id) {
        mIsAdded = true;
        ThinkObserver observer = ThinkObserver.get();
        ThinkItem passedItem = observer.selectItemThatHasId(id);
        mThinkItem = observer.getCopiedObject(passedItem);

        if (mThinkItem.getImagePaths() != null) {
            MyLog.print(mThinkItem.getImagePaths());
        }

        setView();
    }

    private void setView() {
        RealmList<KeywordItem> keywordsInItem = mThinkItem.getKeywords();
        for(KeywordItem keyword : keywordsInItem) {
            String keywordName = keyword.getName();
            Pair<String, Boolean> temp = new Pair<>(keywordName, true);
            mKeywordStrings.add(temp);
        }
        setKeywordTextView();
        mContentEditText.setText(mThinkItem.getContent());
    }

    private void setEventListener() {

        mContentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mThinkItem.setContent(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mDeleted) {
            return;
        }

        String content = mThinkItem.getContent();
        if (content == null || content.length() == 0
                || mKeywordStrings.size() == 0) {
            return;
        }

        RealmList<KeywordItem> keywords = new RealmList<>();
        List<Pair<String, Boolean>> newKeywordStrings = new ArrayList<>();
        for (Pair<String, Boolean> keyword : mKeywordStrings) {
            if (!keyword.second) {
                KeywordManager.get().createOrUpdateKeyword(keyword.first);
            }
            keywords.add(KeywordObserver.get().getKeywordByName(keyword.first));
            newKeywordStrings.add(new Pair<>(keyword.first, true));
        }
        mKeywordStrings = newKeywordStrings;
        mThinkItem.setKeywords(keywords);

        String[] pathStrings = new String[mImageFiles.size()];

        MyLog.print(mImageFiles.size() + "!!!!!");

        for (int i = 0; i < mImageFiles.size(); i++) {
            pathStrings[i] = mImageFiles.get(i).getPath();
            MyLog.print(mImageFiles.get(i).getPath());
        }

        String imageNames = TextUtils.join(",", pathStrings);

        MyLog.print(imageNames + "!!!!!!");

        mThinkItem.setImagePaths(imageNames);

        ThinkObserver thinkObserver = ThinkObserver.get();

        mIsAdded = (thinkObserver.selectItemThatHasId(mThinkItem.getId()) != null);

        if (!mIsAdded) {
            thinkObserver.insert(mThinkItem);
            MyLog.print("inserted..........");
        } else {
            mThinkItem.setDateUpdated(new Date());
            thinkObserver.update(mThinkItem);
            MyLog.print("updated...........");
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_PHOTO) {
            if (mTemporaryFile != null && mTemporaryFile.exists()) {
                mImageFiles.add(mTemporaryFile);
                mImageAdapter.swapFiles(mImageFiles);
            }
        } else if (requestCode == REQUEST_LOAD_IMAGE) {

            if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(
                            selectedImage, filePathColumn, null, null, null
                    );
                    cursor.moveToFirst();

                    int columnIdx = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIdx);
                    cursor.close();

                    File file = new File(filePath);

                    MyLog.print(filePath);

                    mImageFiles.add(file);
                    mImageAdapter.swapFiles(mImageFiles);
                } else {
                    Toast.makeText(this, getString(R.string.no_permission), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * "#" 제거 후 String이 비어있지 않으면 mKeywordStrings List에 추가 후,
     * textView reset을 위해 setKeywordTextView() 호출
     */

    public void getKeywordFromDialog(String keyword) {
        if (keyword.length() != 0) {
            mKeywordStrings.add(new Pair<>(keyword, false));
            setKeywordTextView();
        }
    }

    /**
     * 멤버 변수인 mKeywordStrings가 keyword로 추가된 String을 유지하고 있고,
     * "#" 추가 후 textView에 set!
     * (키워드가 없으면 안내 문자열로 set! - R.string.no_keyword)
     */

    private void setKeywordTextView() {
        String keywordString = "";
        for (Pair<String, Boolean> keyword : mKeywordStrings) {
            keywordString += "#" + keyword.first + " ";
        }

        if (keywordString.equals("")) {
            keywordString = getString(R.string.no_keyword);
        }

        mKeywordTextView.setText(keywordString);
    }

//    private class GetKeywordTask extends AsyncTask<String, Void, String> {
//
//        Context mContext;
//
//        @Override
//        protected void onPreExecute() {
//            mContext = getApplicationContext();
//            Toast.makeText(mContext, getString(R.string.no_keyword), Toast.LENGTH_SHORT).show();
//            Toast.makeText(mContext,
//                    getString(R.string.explain_get_keyword_automatically), Toast.LENGTH_SHORT).show();
//        }
//
//        @Override
//        protected String doInBackground(String... params) {
//            String content = params[0];
//
//            Log.d(TAG, "키워드를 추출합니다...");
//            String content = "단어";
//
//            String keywordExtracted = KeywordUtil.getKeywordFromContent(content);
//
//            for(String noun : mNouns) {
//                NaverRestClient<KeywordService> client = new NaverRestClient<>();
//                KeywordService service = client.getClient(KeywordService.class);
//
//                Call<ResponseFromNaver> call = service.getKeywordsFromNaver("search", "blog.json", noun);
//                call.enqueue(new Callback<ResponseFromNaver>() {
//                    @Override
//                    public void onResponse(Call<ResponseFromNaver> call, Response<ResponseFromNaver> response) {
//
//                        if (response.isSuccessful()) {
//                            ResponseFromNaver responseFromNaver = response.body();
//                            List<ResponseFromNaver.Item> items = responseFromNaver.getItems();
//                            for(ResponseFromNaver.Item item : items) {
//                                List<String> nounsFromTitle = KeywordUtil.getNounsFromText(item.getTitle());
//                                for (String nounFromTitle : nounsFromTitle) {
//                                    if (!mNouns.contains(nounFromTitle)) {
//                                        mNouns.add(nounFromTitle);
//                                    }
//                                }
//                            }
//                        } else {
//                            Log.d(TAG, "호출 실패 : " + response.errorBody());
//                        }
//
//                    }
//
//                    @Override
//                    public void onFailure(Call<ResponseFromNaver> call, Throwable t) {
//                        Log.d(TAG, "오류 발생");
//                        t.printStackTrace();
//                    }
//                });
//            }
//
//            Log.d(TAG, "keywordExtracted : " + keywordExtracted);
//            return keywordExtracted;
//        }
//
//        @Override
//        protected void onPostExecute(String keywordName) {
//            Toast.makeText(mContext,
//                    getString(R.string.after_get_keyword, keywordName), Toast.LENGTH_LONG).show();
//            getKeywordFromDialog(keywordName);
//        }
//
//    }

    public class ImageHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private File mFile;

        @BindView(R.id.photo_image_view)
        ImageView mPhotoImageView;

        public ImageHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        public void bindImage(File photoFile) {
            mFile = photoFile;

            ViewTreeObserver observer = mPhotoImageView.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    updatePhotoView(mPhotoImageView.getWidth(), mPhotoImageView.getHeight());
                }
            });

            Bitmap bitmap = PhotoUtil.getScaledBitmap(photoFile.getPath(),
                    mPhotoImageView.getWidth(),
                    mPhotoImageView.getHeight());

            mPhotoImageView.setImageBitmap(bitmap);
        }

        private void updatePhotoView(int destWidth, int destHeight) {
            if (mFile == null || !mFile.exists()) {
                removeFile(mFile);
            } else {
                Bitmap bitmap = PhotoUtil.getScaledBitmap(mFile.getPath(), destWidth, destHeight);
                mPhotoImageView.setImageBitmap(bitmap);
            }
        }

        @Override
        public void onClick(View v) {
            ImageRepository.get().setFiles(mImageFiles);
            Intent intent = ImagePagerActivity.newIntent(getApplicationContext(), getAdapterPosition());
            startActivity(intent);
        }
    }


    private class ImageAdapter extends RecyclerView.Adapter<ImageHolder> {

        List<File> mFiles;

        public ImageAdapter(List<File> files) {
            mFiles = files;
        }

        @Override
        public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.image_list_item, null, false);
            return new ImageHolder(view);
        }

        @Override
        public void onBindViewHolder(ImageHolder holder, int position) {
            File file = mFiles.get(position);
            holder.bindImage(file);
        }

        @Override
        public int getItemCount() {
            return (mFiles == null) ? 0 : mFiles.size();
        }

        public void swapFiles(List<File> newFiles) {
            mFiles = newFiles;
            notifyDataSetChanged();
        }

    }

}
