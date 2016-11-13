package xyz.zpayh.retrofit2.adapter.agera;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;

/**
 * 文 件 名: MainHandler
 * 创 建 人: 陈志鹏
 * 创建日期: 2016/11/12 13:16
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注:
 */

final class MainHandler extends Handler{
    private static final ThreadLocal<WeakReference<MainHandler>> handlers = new ThreadLocal<>();


    @NonNull
    static MainHandler mainHandler() {
        final WeakReference<MainHandler> handlerReference = handlers.get();
        MainHandler handler = handlerReference != null ? handlerReference.get() : null;
        if (handler == null) {
            handler = new MainHandler();
            handlers.set(new WeakReference<>(handler));
        }
        return handler;
    }

    private MainHandler() {
        super(Looper.getMainLooper());
    }
}
