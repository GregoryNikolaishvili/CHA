package ge.altasoft.gia.cha.classes;

public enum WidgetType {
    LightRelay,
    RoomSensor,
    BoilerPump,
    BoilerSensor,
    WindSensor,
    WindDirSensor,
    RainSensor,
    PressureSensor,
    WaterLevelSensor,
    WaterLevelPumpRelay;

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
                return WindSensor;
            case 5:
                return WindDirSensor;
            case 6:
                return RainSensor;
            case 7:
                return PressureSensor;
            case 8:
                return WaterLevelSensor;
            case  9:
                return WaterLevelPumpRelay;
            default:
                return null;
        }
    }
}
