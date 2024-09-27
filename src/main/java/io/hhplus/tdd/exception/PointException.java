package io.hhplus.tdd.exception;

public class PointException extends RuntimeException {

    public PointException(String message) {
        super(message);
    }

    public PointException(String message, long currentPoint) {
        super(message +  " (잔여 포인트: " + currentPoint + ")" );
    }
}
