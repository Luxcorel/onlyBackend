package se.onlyfin.onlyfinbackend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;
import java.util.Objects;

/**
 * This class is responsible for modeling the user table in the database.
 */
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "roles")
    private String roles;

    @Column(name = "is_analyst")
    private boolean isAnalyst;

    @OneToMany(mappedBy = "subscriber")
    @JsonBackReference
    private List<Subscription> subscriptions;

    @OneToMany(mappedBy = "subscribedTo")
    @JsonBackReference
    private List<Subscription> subscribers;

    @Column(name = "about_me", columnDefinition = "TEXT")
    private String aboutMe = "Lorem ipsum";

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public boolean isAnalyst() {
        return isAnalyst;
    }

    public void setAnalyst(boolean analyst) {
        isAnalyst = analyst;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public List<Subscription> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(List<Subscription> subscribers) {
        this.subscribers = subscribers;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }
    
}
