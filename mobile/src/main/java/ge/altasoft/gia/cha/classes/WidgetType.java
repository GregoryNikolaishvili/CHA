package ge.altasoft.gia.cha.classes;

public enum WidgetType {
    LightRelay,
    RoomSensor,
    BoilerPump,
    BoilerSensor,
    OutsideSensor,
    WindSensor,
    PressureSensor,
    RainSensor,
    WindDirSensor,
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
                return OutsideSensor;
            case 5:
                return WindSensor;
            case 6:
                return PressureSensor;
            case 7:
                return RainSensor;
            case 8:
                return WindDirSensor;
            case 9:
                return WaterLevelSensor;
            case  10:
                return WaterLevelPumpRelay;
            default:
                return null;
        }
    }
}
