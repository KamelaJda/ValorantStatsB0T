package me.kamelajda.valorantstats.utils.exception;

public class ResponseCodeException extends Exception {

    public ResponseCodeException(int code) {
        super("Invalid response code: " + code);
    }

}
