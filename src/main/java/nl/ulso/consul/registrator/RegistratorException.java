package nl.ulso.consul.registrator;

class RegistratorException extends RuntimeException {

    RegistratorException(String message) {
        super(message);
    }

    RegistratorException(String message, Throwable cause) {
        super(message, cause);
    }
}
