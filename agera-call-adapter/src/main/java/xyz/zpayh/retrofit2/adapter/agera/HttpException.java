package xyz.zpayh.retrofit2.adapter.agera;

import retrofit2.Response;

/**
 *
 */
public class HttpException extends retrofit2.HttpException {
    public HttpException(Response<?> response) {
        super(response);
    }
}
