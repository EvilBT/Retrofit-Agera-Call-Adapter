package xyz.zpayh.retrofit2.adapter.agera;

import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;
import zyx.zpayh.retrofit2.adapter.agera.R;

/**
 * 文 件 名: ResponseResultRepositoryCallAdapter
 * 创 建 人: 陈志鹏
 * 创建日期: 2016/11/12 00:27
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注:
 */
final class ResponseResultRepositoryCallAdapter implements CallAdapter<R,ResultRepository<Response<R>>>{


    private final Type mType;

    public ResponseResultRepositoryCallAdapter(Type type) {
        mType = type;
    }

    @Override
    public Type responseType() {
        return mType;
    }

    @Override
    public ResultRepository<Response<R>> adapt(Call<R> call) {
        return new ResponseResultRepository<>(call);
    }
}
