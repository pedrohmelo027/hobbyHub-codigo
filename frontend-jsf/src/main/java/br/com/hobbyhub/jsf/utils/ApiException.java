package br.com.hobbyhub.jsf.utils;

public class ApiException extends RuntimeException {

    private final int status;

    public ApiException(String mensagem) {
        this(400, mensagem);
    }

    public ApiException(int status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public ApiException(String mensagem, Throwable causa) {
        super(mensagem, causa);
        this.status = 503;
    }

    public int getStatus() {
        return status;
    }
}
