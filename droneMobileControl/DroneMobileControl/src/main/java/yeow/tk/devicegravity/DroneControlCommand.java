package yeow.tk.devicegravity;

/**
 *
 * FORWARD(32)
 * BACKWARD(16)
 * LEFT(128)
 * RIGHT(64)
 * UP(4)
 * DOWN(8)
 * ROTATE_LEFT(2)
 * ROTATE_RIGHT(1)
 * HOVER(0)
 * LAND(256)
 * TAKEOFF(512);
 */
public enum DroneControlCommand {
    FORWARD(32), //
    BACKWARD(16), //
    LEFT(128),  //
    RIGHT(64), //
    UP(4),  //
    DOWN(8), //
    ROTATE_LEFT(2),  //
    ROTATE_RIGHT(1), //
    HOVER(0), //
    LAND(256), //
    TAKEOFF(512);

    private final int value;

    DroneControlCommand(int value) {
        this.value = value;
    }

    /**
     * Get the value from the integer
     * @Pram value
     * */
    public int getIntValue() {
        return value;
    }

    /**
     * Creates a DroneControlCommand from the Integer
     * throws a Illigal Argument Exception, if the Integer is unknown
     * @param value
     * @return
     */
    public static DroneControlCommand createFromIntValue(int value) {
        if (value == BACKWARD.getIntValue())
            return DroneControlCommand.BACKWARD;
        else if (value == FORWARD.getIntValue())
            return DroneControlCommand.FORWARD;
        else if (value == LEFT.getIntValue())
            return DroneControlCommand.LEFT;
        else if (value == RIGHT.getIntValue())
            return DroneControlCommand.RIGHT;
        else if (value == UP.getIntValue())
            return DroneControlCommand.UP;
        else if (value == DOWN.getIntValue())
            return DroneControlCommand.DOWN;
        else if (value == ROTATE_LEFT.getIntValue())
            return DroneControlCommand.ROTATE_LEFT;
        else if (value == ROTATE_RIGHT.getIntValue())
            return DroneControlCommand.ROTATE_RIGHT;
        else if (value == HOVER.getIntValue())
            return DroneControlCommand.HOVER;
        else if (value == LAND.getIntValue())
            return DroneControlCommand.LAND;
        else if (value == TAKEOFF.getIntValue())
            return DroneControlCommand.TAKEOFF;
        else  throw new IllegalArgumentException("No enum literal exists for int value: " + value);
    }
}
