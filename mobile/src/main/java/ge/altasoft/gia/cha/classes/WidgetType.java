package ge.altasoft.gia.cha.classes;

public enum WidgetType {
    LightRelay,
    RoomSensor,
    BoilerPump,
    BoilerSensor,
    OutsideSensor,
    WindSensor,
    PressureSensor,
    RainSensor;

    public static WidgetType fromInt(int value) {
        switch (value) {
            case 0:
                return LightRelay;
            case 1:
                return RoomSensor;
            case 2:
                return BoilerPump;
            case 3:
                return BoilerSensor;
            case 4:
                return OutsideSensor;
            case 5:
                return WindSensor;
            case 6:
                return PressureSensor;
            case 7:
                return RainSensor;

            default:
                return null;
        }
    }
}
