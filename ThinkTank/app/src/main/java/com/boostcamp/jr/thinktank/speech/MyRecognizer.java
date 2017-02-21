package com.boostcamp.jr.thinktank.speech;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.WorkerThread;

import com.boostcamp.jr.thinktank.R;
import com.boostcamp.jr.thinktank.utils.MyLog;
import com.naver.speech.clientapi.SpeechConfig;
import com.naver.speech.clientapi.SpeechRecognitionException;
import com.naver.speech.clientapi.SpeechRecognitionListener;
import com.naver.speech.clientapi.SpeechRecognitionResult;
import com.naver.speech.clientapi.SpeechRecognizer;

/**
 * Created by jr on 2017-02-21.
 */

public class MyRecognizer implements SpeechRecognitionListener {

    private Handler mHandler;
    private SpeechRecognizer mRecognizer;

    public MyRecognizer(Context context, Handler handler, String clientId) {
        this.mHandler = handler;
        try {
            mRecognizer = new SpeechRecognizer(context, clientId);
        } catch (SpeechRecognitionException e) {
            e.printStackTrace();
        }
        mRecognizer.setSpeechRecognitionListener(this);
    }

    public SpeechRecognizer getRecognizer() {
        return mRecognizer;
    }

    public void recognize() {
        try {
            mRecognizer.recognize(new SpeechConfig(SpeechConfig.LanguageType.KOREAN,
                    SpeechConfig.EndPointDetectType.AUTO));
        } catch (SpeechRecognitionException e) {
            e.printStackTrace();
        }
    }

    @Override
    @WorkerThread
    public void onInactive() {
        Message msg = Message.obtain(mHandler, R.id.clientInactive);
        msg.sendToTarget();
    }

    @Override
    @WorkerThread
    public void onReady() {
        Message msg = Message.obtain(mHandler, R.id.clientReady);
        msg.sendToTarget();
    }

    @Override
    @WorkerThread
    public void onRecord(short[] speech) {
        Message msg = Message.obtain(mHandler, R.id.audioRecording, speech);
        msg.sendToTarget();
    }

    @Override
    @WorkerThread
    public void onPartialResult(String partialResult) {
        Message msg = Message.obtain(mHandler, R.id.partialResult, partialResult);
        msg.sendToTarget();
    }

    @Override
    @WorkerThread
    public void onEndPointDetected() {
        MyLog.print("Event occurred: EndPointDetected");
    }

    @Override
    @WorkerThread
    public void onResult(SpeechRecognitionResult finalResult) {
        Message msg = Message.obtain(mHandler, R.id.finalResult, finalResult);
        msg.sendToTarget();
    }

    @Override
    @WorkerThread
    public void onError(int errorCode) {
        Message msg = Message.obtain(mHandler, R.id.recognitionError, errorCode);
        msg.sendToTarget();
    }

    @Override
    @WorkerThread
    public void onEndPointDetectTypeSelected(SpeechConfig.EndPointDetectType epdType) {
        Message msg = Message.obtain(mHandler, R.id.endPointDetectTypeSelected, epdType);
        msg.sendToTarget();
    }

}
