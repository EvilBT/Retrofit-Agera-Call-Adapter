package xyz.zpayh.retrofit2.adapter.agera;

import android.support.annotation.NonNull;

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
 *
 */
public class AgeraCallAdapterFactory extends CallAdapter.Factory{

    public static AgeraCallAdapterFactory create(){
        return new AgeraCallAdapterFactory();
    }

    @Override
    public CallAdapter<?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        Class<?> rawType = getRawType(returnType);

        if (rawType != SupplierResult.class && rawType != Supplier.class){
            return null;
        }

        if ( !(returnType instanceof ParameterizedType)){
            final String className = rawType == SupplierResult.class ? "SupplierResult" : "Supplier";
            final String message = className+" return type must be parameterized"
                    + " as "+className+"<Foo> or "+className+"<? extends Foo>";
            throw new IllegalStateException(message);
        }

        if ( rawType == SupplierResult.class) {
            // SupplierResult
            return getSupplierResultCallAdapter(returnType);
        }

        return getSupplierCallAdapter(returnType);
       /* Type innerType = getParameterUpperBound(0, (ParameterizedType) returnType);

        CallAdapter<Reservoir<?>> callAdapter = getReservoirCallAdapter(returnType);//new ReservoirSimpleCallAdapter(innerType);
        return callAdapter;*/
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
