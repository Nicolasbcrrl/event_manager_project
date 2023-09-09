package fi.haagahelia.eventmanager.domain.opinion;

import fi.haagahelia.eventmanager.domain.Activity;
import fi.haagahelia.eventmanager.domain.User;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "eve_opinion")
public class Opinion {

    @EmbeddedId
    private OpinionId id;

    @Column(name = "opi_comment", nullable = false)
    private String comment;

    @Column(name = "opi_rating", nullable = false)
    private int rating;
    /*------------------------------------------------ RELATIONS -----------------------------------------------------*/
    @OneToOne
    @MapsId("opi_act_no")
    @JoinColumn(name = "opi_act_no")
    private Activity activity;

    @OneToOne
    @MapsId("opi_use_no")
    @JoinColumn(name = "opi_use_no")
    private User user;

    //Empty constructor for JPA needs
    public Opinion() {
    }

    public Opinion(OpinionId id,String comment, int rating, Activity activity, User user) {
        this.id = id;
        this.comment = comment;
        this.rating = rating;
        this.activity = activity;
        this.user = user;
        this.id = new OpinionId(activity.getId(), user.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Opinion opinion = (Opinion) o;
        return rating == opinion.rating && Objects.equals(id, opinion.id) && Objects.equals(comment, opinion.comment) && Objects.equals(activity, opinion.activity) && Objects.equals(user, opinion.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, comment, rating, activity, user);
    }

    @Override
    public String toString() {
        return "Opinion{" +
                "id=" + id +
                ", comment='" + comment + '\'' +
                ", rating=" + rating +
                ", activity=" + activity +
                ", user=" + user +
                '}';
    }

    public OpinionId getId() {
        return id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
