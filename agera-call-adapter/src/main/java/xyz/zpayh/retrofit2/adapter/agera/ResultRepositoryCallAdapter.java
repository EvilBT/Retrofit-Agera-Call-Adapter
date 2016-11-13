package xyz.zpayh.retrofit2.adapter.agera;

import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.CallAdapter;

/**
 * 文 件 名: ResultRepositoryCallAdapter
 * 创 建 人: 陈志鹏
 * 创建日期: 2016/11/12 00:41
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注:
 */

final class ResultRepositoryCallAdapter implements CallAdapter<ResultRepository<?>>{

    private final Type mType;

    ResultRepositoryCallAdapter(Type type) {
        mType = type;
    }

    @Override
    public Type responseType() {
        return mType;
    }

    @Override
    public <R> ResultRepository<R> adapt(Call<R> call) {
        return new SimpleResultRepository<>(call);
    }
}
