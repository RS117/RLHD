package rs117.hd.environments;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

@Slf4j
public enum DayLight {

    DAY(LocalTime.of(7, 0), true),
    NIGHT(LocalTime.of(17, 0), false);

    private LocalTime startTime;
    private boolean shadowsEnabled;

    /**
     * This is a day(12-hour) based change.
     */
    private static final int START_PITCH = 175;
    private static final int END_PITCH = 360;

    /**
     * This is a season(month) based changed.
     * December/June = -90 degrees
     * March/September = -45/-135 degrees
     */
    private static final int START_YAW = -45;
    private static final int END_YAW = -135;

    /**
     * TODO: Calculate this value based on distance between sunrise/sunset.
     */
    private static final float CYCLE_LENGTH = 12;

    DayLight(LocalTime startTime, boolean shadowsEnabled) {
        this.startTime = startTime;
        this.shadowsEnabled = shadowsEnabled;
    }

    public boolean isShadowsEnabled() {
        return shadowsEnabled;
    }

    public static DayLight getTimeOfDay(LocalTime currentTime) {
        return currentTime.isAfter(DAY.startTime) && currentTime.isBefore(NIGHT.startTime) ? DAY : NIGHT;
    }

    private float percentageOfDaylight(LocalTime currentTime) {
        return Math.abs(Duration.between(currentTime, startTime).toHours()) / CYCLE_LENGTH;
    }

    private float percentageOfSeason(LocalDate currentDate) {
        float month = currentDate.getMonthValue() + (currentDate.getDayOfMonth() / currentDate.lengthOfMonth());
        float normalizedMonth = month <= 6 ? month : month - 7;
        return normalizedMonth / 6;
    }

    public float getCurrentPitch(LocalTime currentTime) {
        return percentageOfDaylight(currentTime) * (END_PITCH - START_PITCH) + START_PITCH;
    }

    public float getCurrentYaw(LocalDate currentDate) {
        return percentageOfSeason(currentDate) * (END_YAW - START_YAW) + START_YAW;
    }

}
