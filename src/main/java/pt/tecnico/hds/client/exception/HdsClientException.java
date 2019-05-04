package pt.tecnico.hds.client.exception;

public abstract class HdsClientException extends Exception {

    private static final long serialVersionUID = 1L;

    public HdsClientException() {
    }

    public HdsClientException(String msg) {
        super(msg);
    }
}