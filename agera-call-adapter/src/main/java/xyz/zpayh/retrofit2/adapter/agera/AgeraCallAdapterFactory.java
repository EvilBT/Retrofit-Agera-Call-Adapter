package xyz.zpayh.retrofit2.adapter.agera;

import android.support.annotation.NonNull;

import com.google.android.agera.Repository;
import com.google.android.agera.Result;
import com.google.android.agera.Supplier;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * 文 件 名: AgeraCallAdapterFactory
 * 创 建 人: 陈志鹏
 * 创建日期: 2016/11/11 22:26
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注:
 */
public class AgeraCallAdapterFactory extends CallAdapter.Factory{

    public static AgeraCallAdapterFactory create(){
        return new AgeraCallAdapterFactory();
    }

    @Override
    public CallAdapter<?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        Class<?> rawType = getRawType(returnType);

        if (rawType != SupplierResult.class
                && rawType != Supplier.class
                && rawType != Repository.class
                && rawType != ResultRepository.class){
            return null;
        }

        if ( !(returnType instanceof ParameterizedType)){
            final String className =
                    rawType == SupplierResult.class ? "SupplierResult" :
                    rawType == Supplier.class ? "Supplier" :
                    rawType == Repository.class ? "Repository" :
                    "ResultRepository";
            final String message = className+" return type must be parameterized"
                    + " as "+className+"<Foo> or "+className+"<? extends Foo>";
            throw new IllegalStateException(message);
        }

        if ( rawType == SupplierResult.class) {
            // SupplierResult
            return getSupplierResultCallAdapter(returnType);
        }

        if (rawType == Supplier.class) {
            return getSupplierCallAdapter(returnType);
        }

        if (rawType == ResultRepository.class){
            return getResultRepositoryCallAdapter(returnType);
        }

        return getRepositoryCallAdapter(returnType);
    }

    private CallAdapter<?> getRepositoryCallAdapter(Type returnType) {
        Type repositoryType = getParameterUpperBound(0, (ParameterizedType) returnType);
        Class<?> rowRepositoryType = getRawType(repositoryType);
        if (rowRepositoryType != Result.class) {
            return null;
        }

        if (!(repositoryType instanceof ParameterizedType)){
            throw new IllegalStateException("Result must be parameterized"
                    + " as Result<Foo> or Result<? extends Foo>");
        }

        return getResultRepositoryCallAdapter(repositoryType);
    }

    private CallAdapter<?> getResultRepositoryCallAdapter(Type returnType) {
        Type resultRepositoryType = getParameterUpperBound(0, (ParameterizedType) returnType);
        Class<?> rowResultRepositoryType = getRawType(resultRepositoryType);

        if (rowResultRepositoryType == Response.class){
            if (!(resultRepositoryType instanceof ParameterizedType)){
                throw new IllegalStateException("Response must be parameterized"
                        + " as Response<Foo> or Response<? extends Foo>");
            }
            Type responseType = getParameterUpperBound(0, (ParameterizedType) resultRepositoryType);
            return new ResponseResultRepositoryCallAdapter(responseType);
        }

        return new ResultRepositoryCallAdapter(resultRepositoryType);
    }

    private CallAdapter<?> getSupplierCallAdapter(Type returnType) {
        Type supplierType = getParameterUpperBound(0, (ParameterizedType) returnType);
        Class<?> rowSupplierType = getRawType(supplierType);
        if (rowSupplierType != Result.class){
            return null;
        }

        if (!(supplierType instanceof ParameterizedType)){
            throw new IllegalStateException("Response must be parameterized"
                    + " as Result<Foo> or Result<? extends Foo>");
        }
       // Type innerType = getParameterUpperBound(0, (ParameterizedType) supplierType);
        return getSupplierResultCallAdapter(supplierType);
    }

    private CallAdapter<?> getSupplierResultCallAdapter(Type returnType) {
        Type supplierResultType = getParameterUpperBound(0, (ParameterizedType) returnType);
        Class<?> rowSupplierResultType = getRawType(supplierResultType);
        if (rowSupplierResultType == Response.class){
            if (!(supplierResultType instanceof ParameterizedType)){
                throw new IllegalStateException("Response must be parameterized"
                        + " as Response<Foo> or Response<? extends Foo>");
            }
            Type responseType = getParameterUpperBound(0, (ParameterizedType) supplierResultType);
            return new SupplierResultResponseCallAdapter(responseType);
        }
        return new SupplierResultSimpleCallAdapter(supplierResultType);
    }

    static final class SupplierResultResponseCallAdapter implements CallAdapter<SupplierResult<?>>{

        private final Type mType;

        public SupplierResultResponseCallAdapter(Type type) {
            mType = type;
        }

        @Override
        public Type responseType() {
            return mType;
        }

        @Override
        public <R> SupplierResult<Response<R>> adapt(Call<R> call) {
            return new ResponseSupplierResult<>(call);
        }
    }

    static final class SupplierResultSimpleCallAdapter implements CallAdapter<SupplierResult<?>>{
        private final Type mType;

        public SupplierResultSimpleCallAdapter(Type type) {
            mType = type;
        }

        @Override
        public Type responseType() {
            return mType;
        }

        @Override
        public <R> SupplierResult<R> adapt(Call<R> call) {
            return new SimpleSupplierResult<>(call);
        }
    }

    static final class SimpleSupplierResult<T> implements SupplierResult<T>{
        private final Call<T> mCall;

        public SimpleSupplierResult(Call<T> call) {
            mCall = call;
        }

        @NonNull
        @Override
        public Result<T> get() {

            try {
                Response<T> response = mCall.execute();
                if (response.isSuccessful()){
                    return Result.success(response.body());
                }
                return Result.failure(new HttpException(response));
            } catch (IOException e) {
                //e.printStackTrace();
                return Result.failure(e);
            }
        }
    }

    static final class ResponseSupplierResult<T> implements SupplierResult<Response<T>>{
        private final Call<T> mCall;

        public ResponseSupplierResult(Call<T> call) {
            mCall = call;
        }

        @NonNull
        @Override
        public Result<Response<T>> get() {
            try {
                return Result.success(mCall.execute());
            } catch (IOException e) {
                //e.printStackTrace();
                return Result.failure(e);
            }
        }
    }
}
