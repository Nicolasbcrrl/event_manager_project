package fi.haagahelia.eventmanager.domain.opinion;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class OpinionId implements Serializable {
    @Column(name = "opi_act_no")
    private Long activityId;

    @Column(name = "opi_use_no")
    private Long userId;

    //Empty constructor for JPA needs
    public OpinionId() {
    }

    public OpinionId(Long activityId, Long userId) {
        this.activityId = activityId;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpinionId opinionId = (OpinionId) o;
        return Objects.equals(activityId, opinionId.activityId) && Objects.equals(userId, opinionId.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, userId);
    }

    @Override
    public String toString() {
        return "OpinionPK{" +
                "activityId=" + activityId +
                ", userId=" + userId +
                '}';
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
