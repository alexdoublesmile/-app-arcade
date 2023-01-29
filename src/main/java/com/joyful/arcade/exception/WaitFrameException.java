package com.joyful.arcade.exception;

public class WaitFrameException extends RuntimeException {
    public WaitFrameException(InterruptedException ex) {
        super("Waiting for frame time was interrupted", ex);
    }
}
