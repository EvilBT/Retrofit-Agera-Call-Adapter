package xyz.zpayh.retrofit2.adapter.agera;

import android.support.annotation.NonNull;

import com.google.android.agera.Observable;
import com.google.android.agera.Result;
import com.google.android.agera.Updatable;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.google.android.agera.Preconditions.checkNotNull;


/**
 * 文 件 名: ResponseResultRepository
 * 创 建 人: 陈志鹏
 * 创建日期: 2016/11/12 00:31
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注:
 */

final class ResponseResultRepository<T> implements ResultRepository<Response<T>>{

    @NonNull
    private static final Updatable[] NO_UPDATABLES = new Updatable[0];
    @NonNull
    private final Object token = new Object();
    @NonNull
    private Updatable[] mUpdatables;
    private int mSize;

    @NonNull
    private final Call<T> mCall;
    @NonNull
    private final MainHandler mMainHandler;

    private Result<Response<T>> mResult;

    private boolean doEnqueueing;

    private Call<T> mExecCall;

    ResponseResultRepository(@NonNull Call<T> call) {
        mCall = call;
        mMainHandler = MainHandler.mainHandler();
        mResult = Result.absent();
        mUpdatables = NO_UPDATABLES;
        doEnqueueing = false;
        mExecCall = null;
    }

    @Override
    public void addUpdatable(@NonNull Updatable updatable) {
        checkNotNull(updatable);
        boolean activateNow = false;
        synchronized (token) {
            add(updatable);
            if (mSize == 1) {
                activateNow = true;
            }
        }
        if (activateNow) {
            observableActivated();
        }
        doCall();
    }

    @Override
    public void removeUpdatable(@NonNull Updatable updatable) {
        checkNotNull(updatable);
        boolean deactivateNow = false;
        synchronized (token) {
            remove(updatable);
            if (mSize == 0) {
                deactivateNow = true;
            }
        }
        if (deactivateNow){
            observableDeactivated();
        }
        doCall();
    }

    @NonNull
    @Override
    public Result<Response<T>> get() {
        return mResult;
    }

    private void add(@NonNull Updatable updatable) {
        int indexToAdd = -1;
        for (int index = 0; index < mUpdatables.length; index ++) {
            if (mUpdatables[index] == updatable) {
                throw new IllegalStateException("Updatable already added, cannot add.");
            }
            if (mUpdatables[index] == null) {
                indexToAdd = index;
            }
        }
        if (indexToAdd == -1) {
            indexToAdd = mUpdatables.length;
            mUpdatables = Arrays.copyOf(mUpdatables,
                    indexToAdd < 2 ? 2 : indexToAdd * 2);
        }
        mUpdatables[indexToAdd] = updatable;
        mSize++;
    }

    private void remove(@NonNull final Updatable updatable) {
        for (int index = 0; index < mUpdatables.length; index++) {
            if (mUpdatables[index] == updatable) {
                mUpdatables[index] = null;
                mSize--;
                return;
            }
        }
        throw new IllegalStateException("Updatable not added, cannot remove.");
    }

    private void sendUpdate(){
        for (Updatable updatable : mUpdatables) {
            if(updatable != null){
                updatable.update();
            }
        }
        mExecCall = null;
        doEnqueueing = false;
    }

    private void doCall() {
        if (doEnqueueing && mExecCall != null){
            mExecCall.cancel();
        }

        doEnqueueing = true;
        mExecCall = mCall.clone();
        mExecCall.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                mResult = Result.success(response);
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        sendUpdate();
                    }
                });
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                mResult = Result.failure(t);
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        sendUpdate();
                    }
                });
            }
        });
    }

    /**
     * Called from the worker looper thread when this {@link Observable} is activated by transitioning
     * from having no client {@link Updatable}s to having at least one client {@link Updatable}.
     */
    private void observableActivated() {
    }

    /**
     * Called from the worker looper thread when this {@link Observable} is deactivated by
     * transitioning from having at least one client {@link Updatable} to having no client
     * {@link Updatable}s.
     */
    private void observableDeactivated() {}
}
