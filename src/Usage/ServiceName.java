package Usage;

/**
 * ServiceList enum to specify the services.
 */
public enum ServiceName {

    FRONT_END("frontend"),
    EVENT("event"),
    USER("user");

    private final String message;

    /**
     * Constructor of ServiceList.
     *
     * @param message
     */
    ServiceName(String message) {
        this.message = message;
    }

    /**
     * Return the service name.
     *
     * @return String
     */
    @Override
    public String toString() {
        return this.message;
    }
}
