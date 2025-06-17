package com.fitnessapp.model;

import jakarta.persistence.*;
import lombok.Getter; // Използвайте индивидуални анотации
import lombok.Setter; // вместо @Data
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode; // Изисква се за контролиране на equals/hashCode

import java.util.Set;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.stream.Collectors;


@Entity
@Table(name = "users")
@Getter // Генерира гетъри за всички полета
@Setter // Генерира сетъри за всички полета
@NoArgsConstructor
@AllArgsConstructor
@Builder
// --- ВАЖНА ПРОМЯНА: Контролираме EqualsAndHashCode ---
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Генерира equals/hashCode само за полета с @EqualsAndHashCode.Include
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // Включва 'id' в equals() и hashCode()
    private Integer id;

    @Column(unique = true, nullable = false)
    @EqualsAndHashCode.Include // Можете да включите и имейл, тъй като е уникален и не се променя
    private String email;

    @Column(nullable = false)
    private String password;

    private String fullName;

    // ----- Нови полета от чатбота -----
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
    private Set<String> allergies; // НЕ ВКЛЮЧВАЙТЕ В equals/hashCode

    @ElementCollection
    @CollectionTable(name = "user_dietary_preferences", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "preference")
    private Set<String> otherDietaryPreferences; // НЕ ВКЛЮЧВАЙТЕ В equals/hashCode

    private Integer trainingDaysPerWeek;
    private Integer trainingDurationMinutes;
    @Enumerated(EnumType.STRING)
    private LevelType level;
    @Enumerated(EnumType.STRING)
    private MealFrequencyPreferenceType mealFrequencyPreference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diet_type_id")
    private DietType dietType; // НЕ ВКЛЮЧВАЙТЕ В equals/hashCode, освен ако не е EAGER и не води до циклична зависимост

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_level_id")
    private ActivityLevel activityLevel; // НЕ ВКЛЮЧВАЙТЕ В equals/hashCode

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private Goal goal; // НЕ ВКЛЮЧВАЙТЕ В equals/hashCode

    private Boolean isTemporaryAccount = false;

    // --- КОЛЕКЦИИ: ЗАДЪЛЖИТЕЛНО НЕ ГИ ВКЛЮЧВАЙТЕ В equals/hashCode ---
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<NutritionPlan> nutritionPlans;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TrainingPlan> trainingPlans;

    @ManyToMany(fetch = FetchType.EAGER) // Roles може да е EAGER, но все пак е по-добре да не се включва.
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    // --- Помощни методи за двупосочни връзки (по желание, но силно препоръчително) ---
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

    // Повторете за TrainingPlan
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


    // --- UserDetails имплементации ---
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