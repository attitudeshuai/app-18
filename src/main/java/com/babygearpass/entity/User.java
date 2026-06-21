package com.babygearpass.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户实体 - 平台注册用户，可以是母婴用品的赠送方或接收方
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    private String avatar;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "owner")
    private List<GearItem> gearItems;

    @OneToMany(mappedBy = "giver")
    private List<GearHandover> givenHandovers;

    @OneToMany(mappedBy = "receiver")
    private List<GearHandover> receivedHandovers;

    @OneToMany(mappedBy = "user")
    private List<GearStory> stories;

    @OneToMany(mappedBy = "user")
    private List<Wishlist> wishlists;

    @OneToMany(mappedBy = "provider")
    private List<WishlistMatch> providedMatches;

    @OneToMany(mappedBy = "user")
    private List<Notification> notifications;
}
