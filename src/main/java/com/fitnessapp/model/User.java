package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.stream.Collectors;


@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(unique = true, nullable = false)
    @EqualsAndHashCode.Include
    private String email;

    @Column(nullable = false)
    private String password;
    private String fullName;

    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @Column(name = "reset_password_token_expiry_date")
    private LocalDateTime resetPasswordTokenExpiryDate;

    private Double weight;
    private Double height;
    private Integer age;
    @Enumerated(EnumType.STRING)
    private GenderType gender;
    @Enumerated(EnumType.STRING)
    private MeatPreferenceType meatPreference;
    private Boolean consumesDairy;
    @Enumerated(EnumType.STRING)
    private TrainingType trainingType;

    @ElementCollection
    @CollectionTable(name = "user_allergies", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "allergy")
    private Set<String> allergies;

    @ElementCollection
    @CollectionTable(name = "user_dietary_preferences", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "preference")
    private Set<String> otherDietaryPreferences;

    private Integer trainingDaysPerWeek;
    private Integer trainingDurationMinutes;
    @Enumerated(EnumType.STRING)
    private LevelType level;
    @Enumerated(EnumType.STRING)
    private MealFrequencyPreferenceType mealFrequencyPreference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diet_type_id")
    private DietType dietType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_level_id")
    private ActivityLevel activityLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private Goal goal;

    private Boolean isTemporaryAccount = false;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<NutritionPlan> nutritionPlans;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TrainingPlan> trainingPlans;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    public void addNutritionPlan(NutritionPlan plan) {
        if (this.nutritionPlans == null) {
            this.nutritionPlans = new java.util.HashSet<>();
        }
        this.nutritionPlans.add(plan);
        plan.setUser(this);
    }

    public void removeNutritionPlan(NutritionPlan plan) {
        if (this.nutritionPlans != null) {
            this.nutritionPlans.remove(plan);
            plan.setUser(null);
        }
    }


    public void addTrainingPlan(TrainingPlan plan) {
        if (this.trainingPlans == null) {
            this.trainingPlans = new java.util.HashSet<>();
        }
        this.trainingPlans.add(plan);
        plan.setUser(this);
    }

    public void removeTrainingPlan(TrainingPlan plan) {
        if (this.trainingPlans != null) {
            this.trainingPlans.remove(plan);
            plan.setUser(null);
        }
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}