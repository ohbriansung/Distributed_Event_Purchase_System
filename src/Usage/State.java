package Usage;

public enum State {

    PRIMARY("primary"),
    SECONDARY("secondary"),
    CANDIDATE("candidate");

    private final String message;

    /**
     * Constructor of States.
     *
     * @param message
     */
    State(String message) {
        this.message = message;
    }

    /**
     * Return the state.
     *
     * @return String
     */
    @Override
    public String toString() {
        return this.message;
    }
}
