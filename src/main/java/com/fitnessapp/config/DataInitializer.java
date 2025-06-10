package com.fitnessapp.config;

import com.fitnessapp.model.DietType;
import com.fitnessapp.model.Recipe;
import com.fitnessapp.model.Role;
import com.fitnessapp.model.User;
import com.fitnessapp.repository.DietTypeRepository;
import com.fitnessapp.repository.RecipeRepository;
import com.fitnessapp.repository.RoleRepository;
import com.fitnessapp.repository.UserRepository;
import com.fitnessapp.service.TrainingPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DietTypeRepository dietTypeRepository;
    private final PasswordEncoder passwordEncoder;
    private final TrainingPlanService trainingPlanService;

    @Autowired
    public DataInitializer(RecipeRepository recipeRepository,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           DietTypeRepository dietTypeRepository,
                           PasswordEncoder passwordEncoder,
                           TrainingPlanService trainingPlanService) {
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.dietTypeRepository = dietTypeRepository;
        this.passwordEncoder = passwordEncoder;
        this.trainingPlanService = trainingPlanService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Попълване на роли
        seedRoles(); // <-- Този метод вече ще съдържа ROLE_MODERATOR
        // Попълване на диетични типове
        seedDietTypes();
        // Попълване на упражнения и тренировъчни шаблони
        trainingPlanService.seedDefaultTrainingPlans();
        // Попълване на рецепти
        seedRecipes();
        // Попълване на потребители
        seedUsers();
    }


    private void seedRoles() {
        if (roleRepository.count() == 0) {
            roleRepository.save(Role.builder().name("ROLE_USER").build());
            roleRepository.save(Role.builder().name("ROLE_MODERATOR").build());
            roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
            System.out.println("Ролите са инициализирани успешно.");
        }
    }

    private void seedDietTypes() {
        if (dietTypeRepository.count() == 0) {
            dietTypeRepository.save(DietType.builder().name("Standard").description("Standard diet").build());
            dietTypeRepository.save(DietType.builder().name("Vegan").description("Vegan diet").build());
            dietTypeRepository.save(DietType.builder().name("Vegetarian").description("Vegetarian diet").build());
            dietTypeRepository.save(DietType.builder().name("Keto").description("Ketogenic diet").build());
            dietTypeRepository.save(DietType.builder().name("Paleo").description("Paleolithic diet").build());
        }
    }

    private void seedRecipes() {
        if (recipeRepository.count() == 0) {


            // Рецепти за Закуска (Breakfast)
            recipeRepository.save(Recipe.builder()
                    .name("Овесена каша с плодове")
                    .type("breakfast")
                    .instructions("Сварете овесени ядки с вода или ядково мляко. Добавете плодове.")
                    .imageUrl("https://example.com/oatmeal.jpg")
                    .calories(300).protein(10).fat(5).carbs(50)
                    .description("Здравословна закуска, богата на фибри.")
                    .tags(Set.of("vegan", "vegetarian", "gluten_free", "high_carb", "breakfast"))
                    .build());

            recipeRepository.save(Recipe.builder()
                    .name("Омлет със зеленчуци")
                    .type("breakfast")
                    .instructions("Разбийте яйца, добавете нарязани зеленчуци (пипер, лук, спанак). Изпечете.")
                    .imageUrl("https://example.com/omelette.jpg")
                    .calories(250).protein(18).fat(15).carbs(8)
                    .description("Бърза и протеинова закуска.")
                    .tags(Set.of("vegetarian", "keto", "high_protein", "allergy_eggs", "breakfast"))
                    .build());

            recipeRepository.save(Recipe.builder()
                    .name("Кисело мляко с мед и орехи")
                    .type("breakfast")
                    .instructions("Смесете кисело мляко с мед и поръсете с натрошени орехи.")
                    .imageUrl("https://example.com/yogurt-nuts.jpg")
                    .calories(350).protein(15).fat(20).carbs(30)
                    .description("Вкусна и засищаща закуска.")
                    .tags(Set.of("vegetarian", "dairy", "allergy_nuts", "breakfast"))
                    .build());

            // Рецепти за Междинно хранене (Snack)
            recipeRepository.save(Recipe.builder()
                    .name("Ябълка с фъстъчено масло")
                    .type("snack")
                    .instructions("Нарежете ябълка и намажете с фъстъчено масло.")
                    .imageUrl("https://example.com/apple-pb.jpg")
                    .calories(200).protein(8).fat(12).carbs(20)
                    .description("Бърз и енергиен междинен прием.")
                    .tags(Set.of("vegan", "vegetarian", "gluten_free", "allergy_nuts", "snack"))
                    .build());

            recipeRepository.save(Recipe.builder()
                    .name("Протеинов шейк")
                    .type("snack")
                    .instructions("Смесете протеин на прах с вода/мляко и плодове.")
                    .imageUrl("https://example.com/protein-shake.jpg")
                    .calories(180).protein(25).fat(3).carbs(15)
                    .description("Бърз прием на протеин след тренировка.")
                    .tags(Set.of("vegetarian", "snack")) // Може да е vegan, ако е растителен протеин
                    .build());

            // Рецепти за Обяд (Lunch)
            recipeRepository.save(Recipe.builder()
                    .name("Пилешка салата Цезар")
                    .type("lunch")
                    .instructions("Сгответе пилешко филе, нарежете и смесете със салата Айсберг, крутони, пармезан и дресинг Цезар.")
                    .imageUrl("https://example.com/caesar-salad.jpg")
                    .calories(450).protein(35).fat(25).carbs(20)
                    .description("Класическа салата с пилешко месо.")
                    .tags(Set.of("meat_chicken", "dairy", "lunch"))
                    .build());

            recipeRepository.save(Recipe.builder()
                    .name("Супа от леща (Веган)")
                    .type("lunch")
                    .instructions("Сварете леща със зеленчуци (моркови, лук, целина, картофи) и подправки.")
                    .imageUrl("https://example.com/lentil-soup.jpg")
                    .calories(380).protein(20).fat(8).carbs(60)
                    .description("Засищаща и здравословна супа.")
                    .tags(Set.of("vegan", "vegetarian", "gluten_free", "high_protein", "lunch"))
                    .build());

            recipeRepository.save(Recipe.builder()
                    .name("Сьомга с аспержи")
                    .type("lunch")
                    .instructions("Изпечете филе от сьомга и аспержи. Поднесете с лимон.")
                    .imageUrl("https://example.com/salmon-asparagus.jpg")
                    .calories(500).protein(40).fat(30).carbs(10)
                    .description("Богата на омега-3 мастни киселини.")
                    .tags(Set.of("meat_fish", "keto", "high_protein", "lunch"))
                    .build());

            // Рецепти за Вечеря (Dinner)
            recipeRepository.save(Recipe.builder()
                    .name("Говеждо с броколи")
                    .type("dinner")
                    .instructions("Задушете говеждо месо и броколи в соев сос.")
                    .imageUrl("https://example.com/beef-broccoli.jpg")
                    .calories(600).protein(45).fat(30).carbs(25)
                    .description("Протеиново ястие за вечеря.")
                    .tags(Set.of("meat_beef", "high_protein", "dinner"))
                    .build());

            recipeRepository.save(Recipe.builder()
                    .name("Кюфтета от нахут (Веган)")
                    .type("dinner")
                    .instructions("Смелете нахут, лук, чесън и подправки. Оформете кюфтета и изпечете/изпържете.")
                    .imageUrl("https://example.com/chickpea-patties.jpg")
                    .calories(420).protein(15).fat(18).carbs(50)
                    .description("Алтернатива на месо за вегани.")
                    .tags(Set.of("vegan", "vegetarian", "gluten_free", "dinner"))
                    .build());

            recipeRepository.save(Recipe.builder()
                    .name("Свинско с ориз")
                    .type("dinner")
                    .instructions("Задушете свинско месо и ориз със зеленчуци.")
                    .imageUrl("https://example.com/pork-rice.jpg")
                    .calories(550).protein(35).fat(25).carbs(40)
                    .description("Традиционно ястие.")
                    .tags(Set.of("meat_pork", "dinner"))
                    .build());

            recipeRepository.save(Recipe.builder()
                    .name("Кесо пица с карфиол основа")
                    .type("dinner")
                    .instructions("Пригответе основа от карфиол, покрийте с топинг (сирене, пеперони) и изпечете.")
                    .imageUrl("https://example.com/keto-pizza.jpg")
                    .calories(580).protein(30).fat(45).carbs(15)
                    .description("Вкусна кето пица.")
                    .tags(Set.of("keto", "dairy", "meat_pork", "dinner")) // Примерно с пеперони
                    .build());

            recipeRepository.save(Recipe.builder()
                    .name("Агнешко с картофи")
                    .type("dinner")
                    .instructions("Изпечете агнешко месо с картофи на фурна.")
                    .imageUrl("https://example.com/lamb-potatoes.jpg")
                    .calories(650).protein(40).fat(40).carbs(30)
                    .description("Традиционно агнешко ястие.")
                    .tags(Set.of("meat_lamb", "dinner"))
                    .build());

            // Рецепти с алергени
            recipeRepository.save(Recipe.builder()
                    .name("Паста с песто и ядки")
                    .type("lunch")
                    .instructions("Сварете паста, смесете с песто, направено с босилек, пармезан, зехтин и ядки.")
                    .imageUrl("https://example.com/pasta-pesto-nuts.jpg")
                    .calories(480).protein(15).fat(30).carbs(40)
                    .description("Вкусна паста за любителите на песто.")
                    .tags(Set.of("allergy_gluten", "allergy_nuts", "dairy", "vegetarian", "lunch"))
                    .build());

            recipeRepository.save(Recipe.builder()
                    .name("Сандвич с фъстъчено масло и конфитюр")
                    .type("snack")
                    .instructions("Намажете две филии хляб с фъстъчено масло и конфитюр.")
                    .imageUrl("https://example.com/pb-j-sandwich.jpg")
                    .calories(320).protein(10).fat(15).carbs(35)
                    .description("Бърз и лесен сандвич.")
                    .tags(Set.of("allergy_gluten", "allergy_nuts", "vegetarian", "snack"))
                    .build());

            System.out.println("Рецепти попълнени!");
        }
    }

    private void seedUsers() {
        if (userRepository.count() == 0) { // Изпълни само ако няма потребители
            Role userRole = roleRepository.findByName("ROLE_USER").orElseThrow(() -> new RuntimeException("ROLE_USER not found!"));
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found!"));
            // Добави ROLE_MODERATOR ако си решил да го ползваш
            Role moderatorRole = roleRepository.findByName("ROLE_MODERATOR").orElse(null); // Ако го имаш в seedRoles()

            DietType standardDiet = dietTypeRepository.findByName("Standard").orElse(null);
            DietType veganDiet = dietTypeRepository.findByName("Vegan").orElse(null);
            DietType ketoDiet = dietTypeRepository.findByName("Keto").orElse(null);

            // Потребител 1: Мъж, стандартна диета, активно трениращ
            userRepository.save(User.builder()
                    .fullName("Иван Петров")
                    .email("ivan.petrov@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .age(30).height(180.0).weight(80.0).gender("male")
                    .activityLevel("high") // "low", "medium", "high"
                    .goal("muscle_gain")
                    .meatPreference("chicken") // "chicken", "beef", "fish", "pork", "none" (за вегетарианци/вегани)
                    .consumesDairy(true)
                    .trainingType("тежести")
                    .allergies("не") // празен стринг "" или "не"
                    .roles(List.of(userRole))
                    .dietType(standardDiet)
                    .build());

            // Потребител 2: Жена, веган, умерена активност
            userRepository.save(User.builder()
                    .fullName("Мария Иванова")
                    .email("maria.ivanova@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .age(25).height(165.0).weight(60.0).gender("female")
                    .activityLevel("medium")
                    .goal("weight_loss")
                    .meatPreference("none")
                    .consumesDairy(false)
                    .trainingType("без тежести")
                    .allergies("ядки") // "ядки, глутен"
                    .roles(List.of(userRole))
                    .dietType(veganDiet)
                    .build());

            // Потребител 3: Мъж, кето диета, ниска активност, алергичен към глутен
            userRepository.save(User.builder()
                    .fullName("Георги Димитров")
                    .email("georgi.dimitrov@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .age(40).height(175.0).weight(95.0).gender("male")
                    .activityLevel("low")
                    .goal("weight_loss")
                    .meatPreference("beef")
                    .consumesDairy(true)
                    .trainingType("без тежести")
                    .allergies("глутен")
                    .roles(List.of(userRole))
                    .dietType(ketoDiet)
                    .build());

            // Администраторски потребител
            userRepository.save(User.builder()
                    .fullName("Админ Админов")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("adminpass"))
                    .age(35).height(170.0).weight(70.0).gender("male")
                    .activityLevel("medium")
                    .goal("maintain")
                    .meatPreference("chicken")
                    .consumesDairy(true)
                    .trainingType("тежести")
                    .allergies("не")
                    .roles(List.of(userRole, adminRole))
                    .dietType(standardDiet)
                    .build());

            // Ако искаш да имаш и потребител с роля MODERATOR
            if (moderatorRole != null) {
                userRepository.save(User.builder()
                        .fullName("Модератор Модераторов")
                        .email("moderator@example.com")
                        .password(passwordEncoder.encode("modpass"))
                        .age(28).height(168.0).weight(65.0).gender("female")
                        .activityLevel("medium")
                        .goal("maintain")
                        .meatPreference("none") // Може да е вегетарианец
                        .consumesDairy(true)
                        .trainingType("кардио")
                        .allergies("яйца")
                        .roles(List.of(userRole, moderatorRole)) // USER и MODERATOR
                        .dietType(standardDiet)
                        .build());
            }

            System.out.println("Потребители попълнени!");
        }
    }
}
