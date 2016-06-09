package xyz.zpayh.retrofit2.adapter.agera;

import retrofit2.Response;

/**
 * 文 件 名: HttpException
 * 创 建 人: 陈志鹏
 * 创建日期: 2016/6/9 15:35
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注:
 */
public class HttpException extends Exception {
    private final int code;
    private final String message;
    private final transient Response<?> response;

    public HttpException(Response<?> response) {
        super("HTTP " + response.code() + " " + response.message());
        this.code = response.code();
        this.message = response.message();
        this.response = response;
    }

    /** HTTP status code. */
    public int code() {
        return code;
    }

    /** HTTP status message. */
    public String message() {
        return message;
    }

    /**
     * The full HTTP response. This may be null if the exception was serialized.
     */
    public Response<?> response() {
        return response;
    }
}
