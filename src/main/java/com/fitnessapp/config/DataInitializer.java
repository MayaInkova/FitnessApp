package com.fitnessapp.config;

import com.fitnessapp.model.*;
import com.fitnessapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

@Component
public class DataInitializer implements CommandLineRunner {


    private final RecipeRepository recipeRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DietTypeRepository dietTypeRepository;
    private final ActivityLevelRepository activityLevelRepository;
    private final GoalRepository goalRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(RecipeRepository recipeRepository,
                           ExerciseRepository exerciseRepository,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           DietTypeRepository dietTypeRepository,
                           ActivityLevelRepository activityLevelRepository,
                           GoalRepository goalRepository,
                           PasswordEncoder passwordEncoder) {
        this.recipeRepository = recipeRepository;
        this.exerciseRepository = exerciseRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.dietTypeRepository = dietTypeRepository;
        this.activityLevelRepository = activityLevelRepository;
        this.goalRepository = goalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedRoles();
        seedDietTypes();
        seedActivityLevels();
        seedGoals();
        seedRecipes(); // Този метод ще бъде актуализиран
        seedExercises();
        seedTestUsers();
    }



    private void seedRoles() {
        Stream.of("ROLE_USER", "ROLE_ADMIN", "ROLE_MODERATOR", "ROLE_GUEST")
                .filter(r -> !roleRepository.existsByName(r)) // ⚡️ only if missing
                .forEach(r -> roleRepository.save(Role.builder().name(r).build()));
        System.out.println("✔ Ролите синхронизирани");
    }

    private void seedDietTypes() {
        record D(String n, String d) {}
        List<D> list = List.of(
                new D("Протеинова", "Богата на протеини, ниска на въглехидрати."),
                new D("Кето", "Кетогенна с високи мазнини и ниски въглехидрати."),
                new D("Балансирана", "Включва всички хранителни групи."),
                new D("Вегетарианска", "Растителна, без месо и риба."),
                new D("Веган", "Без животински продукти."),
                new D("Палео", "Непреработени храни, без зърнени и бобови.")
        );
        list.forEach(d -> {
            if (!dietTypeRepository.existsByName(d.n)) {
                dietTypeRepository.save(DietType.builder().name(d.n).description(d.d).build());
            }
        });
        System.out.println("✔ Типове диети синхронизирани");
    }

    private void seedActivityLevels() {
        record AL(String n, String d, double m) {}
        List<AL> al = List.of(
                new AL("Заседнал", "Малко или никакво движение", 1.2),
                new AL("Леко активен", "1‑3 тренировки", 1.375),
                new AL("Умерено активен", "3‑5 тренировки", 1.55),
                new AL("Много активен", "6‑7 интензивни тренировки", 1.725),
                new AL("Изключително активен", "Физическа работа / 2× тренировки", 1.9)
        );
        al.forEach(a -> {
            if (!activityLevelRepository.existsByName(a.n)) {
                activityLevelRepository.save(ActivityLevel.builder().name(a.n).description(a.d).multiplier(a.m).build());
            }
        });
        System.out.println("✔ Нивата на активност синхронизирани");
    }

    private void seedGoals() {
        record G(String n, String d, double m) {}
        List<G> gs = List.of(
                new G("Отслабване", "Калориен дефицит", -500.0),
                new G("Наддаване на тегло", "Калориен излишък", 500.0),
                new G("Поддържане на тегло", "Без промяна в калориите", 0.0)
        );
        gs.forEach(g -> {
            if (!goalRepository.existsByName(g.n)) {
                goalRepository.save(Goal.builder().name(g.n).description(g.d).calorieModifier(g.m).build());
            }
        });
        System.out.println("✔ Целите синхронизирани");
    }

    // ВАЖНО: Добавени са "fish" и "pork" към RSeed, за да съответстват на предпочитанията за месо.
    private record RSeed(String n, String d, double kcal, double p, double c, double f,
                         MealType meal, DietType diet, MeatPreferenceType meat,
                         boolean veg, boolean dairy, boolean nuts, boolean fish, boolean pork, String... tags) {
    }


    private void seedRecipes() {

        Set<RSeed> uniqueSeeds = new HashSet<>();

        DietType bal   = dietTypeRepository.findByName("Балансирана").orElseThrow(() -> new RuntimeException("DietType 'Балансирана' not found"));
        DietType keto  = dietTypeRepository.findByName("Кето").orElseThrow(() -> new RuntimeException("DietType 'Кето' not found"));
        DietType prot  = dietTypeRepository.findByName("Протеинова").orElseThrow(() -> new RuntimeException("DietType 'Протеинова' not found"));
        DietType veg   = dietTypeRepository.findByName("Вегетарианска").orElseThrow(() -> new RuntimeException("DietType 'Вегетарианска' not found"));
        DietType vegan = dietTypeRepository.findByName("Веган").orElseThrow(() -> new RuntimeException("DietType 'Веган' not found"));
        DietType paleo = dietTypeRepository.findByName("Палео").orElseThrow(() -> new RuntimeException("DietType 'Палео' not found"));


        // ==== Балансирана диета ====
        // --- Закуска (Breakfast) ---
        uniqueSeeds.add(new RSeed("Овесена каша с ябълка и канела",
                "Овесени ядки, ябълка, канела, мляко. Сварете овеса с млякото и добавете ябълка и канела.",
                340, 10, 50, 8, MealType.BREAKFAST, bal, MeatPreferenceType.NONE,
                true, true, false, false, false, "балансирана","закуска","бързо"));
        uniqueSeeds.add(new RSeed("Яйца със спанак и пълнозърнест тост",
                "Запържете яйцата със спанака и сервирайте с препечен пълнозърнест хляб.",
                360, 18, 20, 20, MealType.BREAKFAST, bal, MeatPreferenceType.NONE,
                true, true, false, false, false, "балансирана","високо-протеин"));
        uniqueSeeds.add(new RSeed("Кисело мляко с овес, мед и орехи",
                "Смесете киселото мляко, овесените ядки и меда; поръсете с орехи.",
                310, 14, 35, 10, MealType.BREAKFAST, bal, MeatPreferenceType.NONE,
                true, true, true, false, false, "балансирана","бърза"));
        uniqueSeeds.add(new RSeed("Сандвич с яйце, авокадо и домат",
                "Пълнозърнест хляб, варено яйце, авокадо и резени домат – идеален за път.",
                390, 15, 25, 24, MealType.BREAKFAST, bal, MeatPreferenceType.NONE,
                true, true, false, false, false, "балансирана","to-go", "здравословни мазнини"));
        uniqueSeeds.add(new RSeed("Палачинки с извара и горски плодове",
                "Овесено брашно, извара и яйца. Изпечете палачинките и сервирайте с горски плодове.",
                350, 18, 30, 14, MealType.BREAKFAST, bal, MeatPreferenceType.NONE,
                true, true, false, false, false, "балансирана","фит-десерт"));
        uniqueSeeds.add(new RSeed("Чиа пудинг с кокосово мляко и манго",
                "Накиснете чията за 4-6 ч. в кокосово мляко, добавете пресни парчета манго преди сервиране.",
                320, 9, 25, 15, MealType.BREAKFAST, bal, MeatPreferenceType.NONE,
                true, false, false, false, false, "балансирана","prep-friendly", "веган опция"));
        uniqueSeeds.add(new RSeed("Мюсли с прясно мляко и банан",
                "Смесете мюслито с прясно мляко и нарязан банан, оставете 5 мин да омекне.",
                340, 10, 40, 8, MealType.BREAKFAST, bal, MeatPreferenceType.NONE,
                true, true, false, false, false, "балансирана","classic"));
        uniqueSeeds.add(new RSeed("Бъркани яйца със сирене и чушки",
                "Разбийте яйца със сирене и ситно нарязани чушки. Запържете до готовност.",
                380, 22, 10, 28, MealType.BREAKFAST, bal, MeatPreferenceType.NONE,
                true, true, false, false, false, "балансирана","кето-приятелски"));
        uniqueSeeds.add(new RSeed("Оризовки с авокадо и чиа",
                "Намажете оризовки с авокадо и поръсете с чиа семена.",
                300, 8, 30, 18, MealType.BREAKFAST, bal, MeatPreferenceType.NONE,
                true, false, false, false, false, "балансирана", "веган", "без-глутен"));
        uniqueSeeds.add(new RSeed("Плодова салата с котидж сирене",
                "Микс от сезонни плодове с котидж сирене.",
                290, 15, 35, 10, MealType.BREAKFAST, bal, MeatPreferenceType.NONE,
                true, true, false, false, false, "балансирана", "лека", "свежа"));

        // --- Обяд (Lunch) ---
        uniqueSeeds.add(new RSeed("Салата с киноа, риба тон и зеленчуци",
                "Сварете киноата, охладете и разбъркайте с риба тон, краставици, домати и маслини.",
                420, 28, 40, 14, MealType.LUNCH, bal, MeatPreferenceType.FISH,
                false, false, false, true, false, "балансирана","без-глутен", "бърз обяд"));
        uniqueSeeds.add(new RSeed("Пилешко филе със сладък картоф и броколи",
                "Запечете пилешкото филе и резените сладък картоф във фурна 25 мин при 200 °C. Задушете броколите на пара.",
                480, 38, 35, 16, MealType.LUNCH, bal, MeatPreferenceType.CHICKEN,
                false, false, false, false, false, "балансирана","meal-prep"));
        uniqueSeeds.add(new RSeed("Пъстърва на пара с кафяв ориз и задушени моркови",
                "Гответе пъстървата на пара; поднесете с кафяв ориз и задушени моркови.",
                500, 34, 38, 18, MealType.LUNCH, bal, MeatPreferenceType.FISH,
                false, false, false, true, false, "балансирана","omega-3"));
        uniqueSeeds.add(new RSeed("Свинско месо със зеле и моркови",
                "Задушете свинското месо със зеле и моркови до пълно омекване. Овкусете с червен пипер.",
                510, 36, 20, 22, MealType.LUNCH, bal, MeatPreferenceType.PORK,
                false, false, false, false, true, "балансирана","ниско-въглехидрати"));
        uniqueSeeds.add(new RSeed("Телешко с булгур и домати",
                "Сварете булгура; задушете телешкото с домати и поднесете заедно.",
                530, 40, 30, 20, MealType.LUNCH, bal, MeatPreferenceType.BEEF,
                false, false, false, false, false, "балансирана","желязо"));
        uniqueSeeds.add(new RSeed("Голям омлет със зеленчуци (чушки, лук, гъби)",
                "Яйца с чушки, лук и гъби; изпечете в тефлонов тиган.",
                390, 20, 15, 22, MealType.LUNCH, bal, MeatPreferenceType.NONE,
                true, true, false, false, false, "балансирана","вегетарианска"));
        uniqueSeeds.add(new RSeed("Тофу със зеленчуци и кафяв ориз",
                "Запържете тофуто с броколи, моркови и чушки, сервирайте върху кафяв ориз.",
                440, 24, 35, 14, MealType.LUNCH, bal, MeatPreferenceType.NONE,
                true, false, false, false, false, "балансирана","веган", "бързо"));
        uniqueSeeds.add(new RSeed("Пилешка пържола със зелена салата и дресинг",
                "Изпечете пилешка пържола на тиган, сервирайте със свежа зелена салата и лек дресинг.",
                450, 40, 10, 25, MealType.LUNCH, bal, MeatPreferenceType.CHICKEN,
                false, false, false, false, false, "балансирана","лесно"));
        uniqueSeeds.add(new RSeed("Салата с нахут, авокадо и краставица",
                "Смесете нахут, нарязано авокадо, краставица, червен лук и лимонов дресинг.",
                380, 15, 40, 18, MealType.LUNCH, bal, MeatPreferenceType.NONE,
                true, false, false, false, false, "балансирана", "веган", "без-глутен"));
        uniqueSeeds.add(new RSeed("Морска храна с ориз и зеленчуци",
                "Калмари/скариди запържени с ориз и микс от зеленчуци.",
                500, 30, 45, 15, MealType.LUNCH, bal, MeatPreferenceType.FISH,
                false, false, false, true, false, "балансирана", "морска храна"));
        uniqueSeeds.add(new RSeed("Свинско филе със задушени гъби",
                "Печено свинско филе, сервирано със задушени гъби и пресен магданоз.",
                520, 38, 15, 28, MealType.LUNCH, bal, MeatPreferenceType.PORK,
                false, false, false, false, true, "балансирана", "ниско-въглехидрати"));

        // --- Вечеря (Dinner) ---
        uniqueSeeds.add(new RSeed("Леща с ориз и свежа салата",
                "Сварете лещата и ориза; поднесете с пресна салата от домати и краставици.",
                430, 22, 40, 10, MealType.DINNER, bal, MeatPreferenceType.NONE,
                true, false, false, false, false, "балансирана","фибри"));
        uniqueSeeds.add(new RSeed("Пилешка супа със зеленчуци",
                "Пилешко месо, моркови, картофи, целина; вари се 40 мин и се овкусява с магданоз.",
                400, 30, 20, 12, MealType.DINNER, bal, MeatPreferenceType.CHICKEN,
                false, false, false, false, false, "балансирана","comfort", "лека"));
        uniqueSeeds.add(new RSeed("Рататуй с тиквички, патладжан и домати",
                "Тиквички, патладжан, домати и чушки – печете 35 мин на 180 °C.",
                370, 8, 25, 14, MealType.DINNER, bal, MeatPreferenceType.NONE,
                true, false, false, false, false, "балансирана","ниско-калорични", "веган"));
        uniqueSeeds.add(new RSeed("Тилапия на фурна със задушен зелен фасул",
                "Изпечете тилапията с лимонов сок; сервирайте със задушен зелен фасул.",
                460, 32, 18, 20, MealType.DINNER, bal, MeatPreferenceType.FISH,
                false, false, false, true, false, "балансирана","постен протеин"));
        uniqueSeeds.add(new RSeed("Пълнени чушки с кайма и ориз",
                "Чушки, свинска и телешка кайма и ориз във фурна с доматен сос.",
                490, 28, 30, 18, MealType.DINNER, bal, MeatPreferenceType.NO_PREFERENCE,
                false, false, false, false, false, "балансирана","класика"));
        uniqueSeeds.add(new RSeed("Киноа с печени зеленчуци и гъби",
                "Сварена киноа, тиквички, моркови и гъби, запечени със зехтин и подправки.",
                420, 16, 35, 14, MealType.DINNER, bal, MeatPreferenceType.NONE,
                true, false, false, false, false, "балансирана","суперхрана", "веган"));
        uniqueSeeds.add(new RSeed("Яйца с броколи и пармезан",
                "Запечете яйца, броколи и настърган пармезан в керамичен съд.",
                410, 20, 15, 24, MealType.DINNER, bal, MeatPreferenceType.NONE,
                true, true, false, false, false, "балансирана","кето-приятелски"));
        uniqueSeeds.add(new RSeed("Мусака с телешка кайма",
                "Слоеве от картофи, телешка кайма и бешамелов сос, запечени във фурна.",
                550, 35, 30, 30, MealType.DINNER, bal, MeatPreferenceType.BEEF,
                false, true, false, false, false, "балансирана","традиционна"));
        uniqueSeeds.add(new RSeed("Агнешки котлети с розмарин и чесън",
                "Печени агнешки котлети с пресен розмарин и чесън.",
                560, 40, 10, 35, MealType.DINNER, bal, MeatPreferenceType.LAMB,
                false, false, false, false, false, "балансирана", "празнично"));
        uniqueSeeds.add(new RSeed("Свинско с гъби и ориз",
                "Свинско месо, гъби и бял ориз, задушени в соев сос.",
                530, 35, 30, 25, MealType.DINNER, bal, MeatPreferenceType.PORK,
                false, false, false, false, true, "балансирана", "азиатски вкус"));

        // --- Снак (Snack) ---
        uniqueSeeds.add(new RSeed("Орехи с кисело мляко и мед",
                "Кисело мляко, натрошени орехи и лъжичка мед.",
                280, 14, 10, 20, MealType.SNACK, bal, MeatPreferenceType.NONE,
                true, true, true, false, false, "балансирана","здравословни мазнини"));
        uniqueSeeds.add(new RSeed("Ябълка с фъстъчено масло",
                "Нарежете ябълка на резени и намажете с фъстъчено масло.",
                270, 6, 25, 14, MealType.SNACK, bal, MeatPreferenceType.NONE,
                true, false, true, false, false, "балансирана","on-the-go"));
        uniqueSeeds.add(new RSeed("Смути с кисело мляко, банан и горски плодове",
                "Пасирайте кисело мляко, банан и горски плодове до гладкост.",
                290, 10, 28, 10, MealType.SNACK, bal, MeatPreferenceType.NONE,
                true, true, false, false, false, "балансирана","витамин-буст"));
        uniqueSeeds.add(new RSeed("Домашни барове с мюсли и ядки",
                "Мюсли, мед и ядки – изпечете 15 мин, оставете да стегнат.",
                300, 8, 30, 12, MealType.SNACK, bal, MeatPreferenceType.NONE,
                true, true, true, false, false, "балансирана","prep-bars"));
        uniqueSeeds.add(new RSeed("Фреш от морков, ябълка и лимон",
                "Изцедете сока от морков, ябълка и лимон; сервирайте охладено.",
                150, 2, 28, 1, MealType.SNACK, bal, MeatPreferenceType.NONE,
                true, false, false, false, false, "балансирана","свежо"));
        uniqueSeeds.add(new RSeed("Райска ябълка с кисело мляко",
                "Смесете нарязана райска ябълка с кисело мляко и мед.",
                260, 9, 20, 8, MealType.SNACK, bal, MeatPreferenceType.NONE,
                true, true, false, false, false, "балансирана","сезонно"));
        uniqueSeeds.add(new RSeed("Грис с мляко и сладко",
                "Сварете гриса в прясно мляко, поръсете с канела и добавете лъжичка сладко.",
                310, 7, 35, 9, MealType.SNACK, bal, MeatPreferenceType.NONE,
                true, true, false, false, false, "балансирана","носталгия"));
        uniqueSeeds.add(new RSeed("Протеинов шейк (банан и мляко)",
                "Банан, протеин на прах, мляко. Разбийте в блендер.",
                250, 25, 20, 5, MealType.SNACK, bal, MeatPreferenceType.NONE,
                true, true, false, false, false, "балансирана","бърз протеин"));
        uniqueSeeds.add(new RSeed("Оризовки с хумус",
                "Намажете оризовки с хумус.",
                200, 5, 25, 8, MealType.SNACK, bal, MeatPreferenceType.NONE,
                true, false, false, false, false, "балансирана","веган", "бързо"));
        uniqueSeeds.add(new RSeed("Шепа бадеми и сушени череши",
                "Микс от бадеми и сушени череши.",
                230, 7, 18, 15, MealType.SNACK, bal, MeatPreferenceType.NONE,
                true, false, true, false, false, "балансирана","енергия"));
        uniqueSeeds.add(new RSeed("Варено яйце и зеленчукови пръчици",
                "Едно варено яйце със солети от морков и краставица.",
                180, 10, 10, 10, MealType.SNACK, bal, MeatPreferenceType.NONE,
                true, false, false, false, false, "балансирана", "бързо", "лека"));
        uniqueSeeds.add(new RSeed("Малка шепа кашу и фурми",
                "Кашу и фурми за бърза енергия.",
                250, 6, 30, 12, MealType.SNACK, bal, MeatPreferenceType.NONE,
                true, false, true, false, false, "балансирана", "енергия", "сладко"));


        // ==== Протеинова диета ====
        // --- Закуска (Breakfast) ---
        uniqueSeeds.add(new RSeed("Омлет с извара и чери домати",
                "Яйца, извара, чери домати. Запържете омлета, поднесете с домати.",
                360, 30, 8, 20, MealType.BREAKFAST, prot, MeatPreferenceType.NONE,
                true, true, false, false, false, "протеинова", "закуска"));
        uniqueSeeds.add(new RSeed("Кисело мляко с протеин на прах и чия",
                "Кисело мляко, протеин на прах, чия, стевия. Смесете съставките.",
                320, 25, 15, 15, MealType.BREAKFAST, prot, MeatPreferenceType.NONE,
                true, true, false, false, false, "протеинова", "закуска"));
        uniqueSeeds.add(new RSeed("Яйца по бенедиктински с пушено филе",
                "Яйца, пушено пилешко филе, пълнозърнест хляб. Запечете и сервирайте.",
                410, 30, 20, 24, MealType.BREAKFAST, prot, MeatPreferenceType.CHICKEN,
                false, true, false, false, false, "протеинова", "закуска"));
        uniqueSeeds.add(new RSeed("Протеинови палачинки с извара",
                "Протеин на прах, яйца, извара, банан. Направете палачинки от сместа.",
                370, 28, 18, 14, MealType.BREAKFAST, prot, MeatPreferenceType.NONE,
                true, true, false, false, false, "протеинова", "закуска"));
        uniqueSeeds.add(new RSeed("Бъркани яйца с гъби и кашкавал",
                "Разбийте яйца с гъби и настърган кашкавал. Запържете до готовност.",
                400, 25, 10, 30, MealType.BREAKFAST, prot, MeatPreferenceType.NONE,
                true, true, false, false, false, "протеинова", "кето-приятелски"));
        uniqueSeeds.add(new RSeed("Протеинов овес с фъстъчено масло",
                "Овесени ядки, вода, протеин на прах, фъстъчено масло. Загрявайте до готовност.",
                380, 25, 30, 15, MealType.BREAKFAST, prot, MeatPreferenceType.NONE,
                true, false, true, false, false, "протеинова", "бърза"));

        // --- Обяд (Lunch) ---
        uniqueSeeds.add(new RSeed("Пуешко филе с киноа и аспержи",
                "Пуешко филе, киноа, аспержи. Изпечете пуешкото и сервирайте с киноа и аспержи.",
                520, 42, 35, 18, MealType.LUNCH, prot, MeatPreferenceType.CHICKEN,
                false, false, false, false, false, "протеинова", "обяд"));
        uniqueSeeds.add(new RSeed("Телешки стек със зелен фасул и чесън",
                "Телешки стек, зелен фасул. Изпечете стека и задушете фасула с чесън.",
                540, 45, 10, 28, MealType.LUNCH, prot, MeatPreferenceType.BEEF,
                false, false, false, false, false, "протеинова", "обяд"));
        uniqueSeeds.add(new RSeed("Риба тон (консерва) със зеленчукова салата",
                "Риба тон от консерва, чушки, лук, домати, маслини. Смесете и поднесете.",
                490, 38, 14, 22, MealType.LUNCH, prot, MeatPreferenceType.FISH,
                false, false, false, true, false, "протеинова", "обяд", "бързо"));
        uniqueSeeds.add(new RSeed("Пилешка салата с яйца и авокадо",
                "Пилешко филе, яйца, зелена салата, авокадо. Нарежете и комбинирайте.",
                480, 40, 8, 20, MealType.LUNCH, prot, MeatPreferenceType.CHICKEN,
                false, true, false, false, false, "протеинова", "обяд", "здравословни мазнини"));
        uniqueSeeds.add(new RSeed("Свински котлет с броколи",
                "Изпечете свински котлет и сервирайте с броколи на пара.",
                530, 40, 8, 35, MealType.LUNCH, prot, MeatPreferenceType.PORK,
                false, false, false, false, true, "протеинова", "обяд"));
        uniqueSeeds.add(new RSeed("Туршия от кисело зеле с нарязани телешки гърди",
                "Нарязани телешки гърди, задушени с кисело зеле и червен пипер.",
                550, 45, 20, 25, MealType.LUNCH, prot, MeatPreferenceType.BEEF,
                false, false, false, false, false, "протеинова", "традиционно", "зимно"));
        uniqueSeeds.add(new RSeed("Салата с пилешко филе и спанак",
                "Пилешко филе на грил, пресен спанак, чери домати, балсамов дресинг.",
                460, 40, 10, 20, MealType.LUNCH, prot, MeatPreferenceType.CHICKEN,
                false, false, false, false, false, "протеинова", "лека"));

        // --- Вечеря (Dinner) ---
        uniqueSeeds.add(new RSeed("Сьомга на скара с лимон и копър",
                "Филе от сьомга, подправки, лимон, копър. Изпечете на скара с подправки и лимонов сок.",
                460, 38, 5, 28, MealType.DINNER, prot, MeatPreferenceType.FISH,
                false, false, false, true, false, "протеинова", "вечеря"));
        uniqueSeeds.add(new RSeed("Пилешко филе с гъби и сметанов сос",
                "Пилешко филе, гъби, готварска сметана. Задушете до готовност.",
                490, 36, 6, 24, MealType.DINNER, prot, MeatPreferenceType.CHICKEN,
                false, true, false, false, false, "протеинова", "вечеря"));
        uniqueSeeds.add(new RSeed("Пълнени тиквички с кайма и яйце",
                "Тиквички, кайма (свинска/телешка), яйце, подправки. Напълнете и запечете.",
                520, 42, 12, 26, MealType.DINNER, prot, MeatPreferenceType.NO_PREFERENCE,
                false, false, false, false, false, "протеинова", "вечеря"));
        uniqueSeeds.add(new RSeed("Печена пъстърва с розмарин",
                "Пъстърва, подправки, лимон, розмарин. Изпечете във фурна.",
                470, 35, 4, 22, MealType.DINNER, prot, MeatPreferenceType.FISH,
                false, false, false, true, false, "протеинова", "вечеря"));
        uniqueSeeds.add(new RSeed("Агнешко котлетче с мента",
                "Агнешки котлети, свежа мента, зехтин. Запечете на грил.",
                580, 45, 5, 40, MealType.DINNER, prot, MeatPreferenceType.LAMB,
                false, false, false, false, false, "протеинова", "вечеря"));
        uniqueSeeds.add(new RSeed("Телешко филе със задушен спанак",
                "Телешко филе, задушен спанак с чесън. Пригответе на тиган.",
                540, 48, 8, 35, MealType.DINNER, prot, MeatPreferenceType.BEEF,
                false, false, false, false, false, "протеинова", "бързо"));
        uniqueSeeds.add(new RSeed("Свински медальони с гъби",
                "Свински медальони, запържени с гъби и малко сметана.",
                510, 40, 10, 30, MealType.DINNER, prot, MeatPreferenceType.PORK,
                false, true, false, false, true, "протеинова", "кремообразно"));

        // --- Снак (Snack) ---
        uniqueSeeds.add(new RSeed("Протеинов бар с фурми и ядки",
                "Фурми, фъстъчено масло, протеин на прах, ядки. Смесете и оформете барове.",
                300, 24, 22, 12, MealType.SNACK, prot, MeatPreferenceType.NONE,
                true, false, true, false, false, "протеинова", "снак"));
        uniqueSeeds.add(new RSeed("Кисело мляко с протеин на прах",
                "Кисело мляко, протеин на прах. Разбъркайте добре.",
                280, 22, 10, 10, MealType.SNACK, prot, MeatPreferenceType.NONE,
                true, true, false, false, false, "протеинова", "снак"));
        uniqueSeeds.add(new RSeed("Сурови ядки и сушени плодове",
                "Орехи, бадеми, сушени кайсии. Смесете.",
                320, 12, 20, 20, MealType.SNACK, prot, MeatPreferenceType.NONE,
                true, false, true, false, false, "протеинова", "снак"));
        uniqueSeeds.add(new RSeed("Извара с канела и стевия",
                "Извара, канела, стевия. Разбъркайте.",
                290, 20, 15, 12, MealType.SNACK, prot, MeatPreferenceType.NONE,
                true, true, false, false, false, "протеинова", "снак"));
        uniqueSeeds.add(new RSeed("Сварени яйца (2 бр.)",
                "Просто сварени яйца.",
                160, 12, 1, 12, MealType.SNACK, prot, MeatPreferenceType.NONE,
                true, false, false, false, false, "протеинова", "снак", "бързо"));
        uniqueSeeds.add(new RSeed("Протеинов мус с какао",
                "Протеин на прах, какао, вода/мляко. Разбийте до мус.",
                180, 25, 5, 5, MealType.SNACK, prot, MeatPreferenceType.NONE,
                true, true, false, false, false, "протеинова", "снак"));
        uniqueSeeds.add(new RSeed("Пушено пилешко филе (50гр)",
                "Тънко нарязано пушено пилешко филе.",
                100, 20, 0, 2, MealType.SNACK, prot, MeatPreferenceType.CHICKEN,
                false, false, false, false, false, "протеинова", "бързо"));
        uniqueSeeds.add(new RSeed("Протеинов пудинг без захар",
                "Готов протеинов пудинг от магазина, без добавена захар.",
                150, 18, 10, 5, MealType.SNACK, prot, MeatPreferenceType.NONE,
                true, true, false, false, false, "протеинова", "удобно"));


        // ==== Кето диета ====
        // --- Закуска (Breakfast) ---
        uniqueSeeds.add(new RSeed("Бъркани яйца с бекон и авокадо",
                "Яйца, бекон, авокадо. Запържете и сервирайте.",
                450, 20, 5, 40, MealType.BREAKFAST, keto, MeatPreferenceType.PORK,
                false, false, false, false, true, "кето", "закуска", "високо-мазнини"));
        uniqueSeeds.add(new RSeed("Кето кафе (Bulletproof Coffee)",
                "Кафе, кокосово масло, масло от трева. Пасирайте до пяна.",
                350, 2, 2, 35, MealType.BREAKFAST, keto, MeatPreferenceType.NONE,
                true, true, false, false, false, "кето", "закуска", "бързо"));
        uniqueSeeds.add(new RSeed("Омлет с гъби и сирене",
                "Яйца, гъби, сирене. Запържете омлета.",
                410, 18, 7, 35, MealType.BREAKFAST, keto, MeatPreferenceType.NONE,
                true, true, false, false, false, "кето", "закуска"));
        uniqueSeeds.add(new RSeed("Извара с малини и бадеми",
                "Извара, малко малини, бадеми. Смесете.",
                330, 20, 10, 25, MealType.BREAKFAST, keto, MeatPreferenceType.NONE,
                true, true, true, false, false, "кето", "закуска"));
        uniqueSeeds.add(new RSeed("Кето палачинки от кокосово брашно",
                "Кокосово брашно, яйца, кокосово мляко. Изпечете палачинки.",
                380, 15, 10, 30, MealType.BREAKFAST, keto, MeatPreferenceType.NONE,
                true, false, false, false, false, "кето", "закуска"));
        uniqueSeeds.add(new RSeed("Бъркани яйца с шунка и сирене",
                "Яйца, шунка (телешка/пуешка), сирене. Запържете.",
                420, 25, 5, 35, MealType.BREAKFAST, keto, MeatPreferenceType.NO_PORK,
                false, true, false, false, false, "кето", "закуска"));

        // --- Обяд (Lunch) ---
        uniqueSeeds.add(new RSeed("Сьомга на тиган с аспержи",
                "Сьомга, аспержи, зехтин. Запържете сьомгата.",
                550, 40, 8, 40, MealType.LUNCH, keto, MeatPreferenceType.FISH,
                false, false, false, true, false, "кето", "обяд"));
        uniqueSeeds.add(new RSeed("Салата с пилешко, авокадо и маслини",
                "Пилешко филе, авокадо, маслини, зелена салата, зехтин. Смесете.",
                500, 35, 10, 35, MealType.LUNCH, keto, MeatPreferenceType.CHICKEN,
                false, false, false, false, false, "кето", "обяд", "без-въглехидрати"));
        uniqueSeeds.add(new RSeed("Телешки кюфтета в сметанов сос",
                "Телешка кайма, сметана, подправки. Сгответе кюфтетата.",
                600, 40, 8, 50, MealType.LUNCH, keto, MeatPreferenceType.BEEF,
                false, true, false, false, false, "кето", "обяд"));
        uniqueSeeds.add(new RSeed("Свинско с гъби и кашкавал",
                "Свинско месо, гъби, кашкавал. Задушете до готовност.",
                580, 40, 10, 45, MealType.LUNCH, keto, MeatPreferenceType.PORK,
                false, true, false, false, true, "кето", "обяд"));
        uniqueSeeds.add(new RSeed("Агнешко кебапче с гръцка салата (без домати)",
                "Агнешко кебапче, краставици, маслини, фета сирене (по желание).",
                600, 45, 15, 40, MealType.LUNCH, keto, MeatPreferenceType.LAMB,
                false, true, false, false, false, "кето", "обяд"));
        uniqueSeeds.add(new RSeed("Пъстърва на тиган с масло и лимон",
                "Пъстърва, масло, лимон. Запържете.",
                570, 40, 5, 45, MealType.LUNCH, keto, MeatPreferenceType.FISH,
                false, true, false, true, false, "кето", "обяд"));

        // --- Вечеря (Dinner) ---
        uniqueSeeds.add(new RSeed("Печено пилешко бедро с броколи и масло",
                "Пилешко бедро, броколи, масло. Изпечете във фурна.",
                530, 38, 10, 38, MealType.DINNER, keto, MeatPreferenceType.CHICKEN,
                false, true, false, false, false, "кето", "вечеря"));
        uniqueSeeds.add(new RSeed("Телешки стек с аспержи и масло",
                "Телешки стек, аспержи, масло. Изпечете стека.",
                590, 45, 7, 45, MealType.DINNER, keto, MeatPreferenceType.BEEF,
                false, true, false, false, false, "кето", "вечеря"));
        uniqueSeeds.add(new RSeed("Агнешко с карфиолено пюре",
                "Агнешко месо, карфиол, масло. Задушете агнешкото, направете пюре от карфиол.",
                620, 48, 12, 50, MealType.DINNER, keto, MeatPreferenceType.LAMB,
                false, true, false, false, false, "кето", "вечеря"));
        uniqueSeeds.add(new RSeed("Мазна риба (скумрия) на фурна със зеленчуци",
                "Скумрия, чушки, лук, зехтин. Изпечете във фурна.",
                560, 40, 10, 42, MealType.DINNER, keto, MeatPreferenceType.FISH,
                false, false, false, true, false, "кето", "вечеря", "високо-мазнини"));
        uniqueSeeds.add(new RSeed("Свински врат с броколи и сос Холандез",
                "Свински врат, броколи, сос Холандез.",
                650, 45, 8, 55, MealType.DINNER, keto, MeatPreferenceType.PORK,
                false, true, false, false, true, "кето", "вечеря"));
        uniqueSeeds.add(new RSeed("Телешки джолан с костен мозък",
                "Телешки джолан, костен мозък, подправки. Бавно печене.",
                700, 50, 5, 60, MealType.DINNER, keto, MeatPreferenceType.BEEF,
                false, false, false, false, false, "кето", "вечеря", "традиционно"));
        uniqueSeeds.add(new RSeed("Пилешко на грил с гъби и кашкавал",
                "Пилешко филе на грил, покрито с гъби и кашкавал. Запечете.",
                520, 42, 8, 38, MealType.DINNER, keto, MeatPreferenceType.CHICKEN,
                false, true, false, false, false, "кето", "лесно"));

        // --- Снак (Snack) ---
        uniqueSeeds.add(new RSeed("Шепа бадеми и орехи",
                "Сурови бадеми и орехи.",
                280, 8, 10, 25, MealType.SNACK, keto, MeatPreferenceType.NONE,
                true, false, true, false, false, "кето", "снак"));
        uniqueSeeds.add(new RSeed("Извара с малко сметана",
                "Извара, малко готварска сметана.",
                180, 18, 5, 10, MealType.SNACK, keto, MeatPreferenceType.NONE,
                true, true, false, false, false, "кето", "снак"));
        uniqueSeeds.add(new RSeed("Парче сирене Гауда",
                "Парче сирене Гауда.",
                150, 10, 1, 12, MealType.SNACK, keto, MeatPreferenceType.NONE,
                true, true, false, false, false, "кето", "снак"));
        uniqueSeeds.add(new RSeed("Кето бисквитки",
                "Бисквитки с бадемово брашно и подсладител.",
                190, 5, 8, 15, MealType.SNACK, keto, MeatPreferenceType.NONE,
                true, false, true, false, false, "кето", "снак"));
        uniqueSeeds.add(new RSeed("Олинкър с авокадо",
                "Парчета олинкър с резени авокадо.",
                220, 15, 5, 18, MealType.SNACK, keto, MeatPreferenceType.PORK,
                false, false, false, false, true, "кето", "снак"));
        uniqueSeeds.add(new RSeed("Парче черен шоколад (90% какао)",
                "Парче черен шоколад.",
                170, 3, 10, 15, MealType.SNACK, keto, MeatPreferenceType.NONE,
                true, false, false, false, false, "кето", "снак"));
        uniqueSeeds.add(new RSeed("Кето бомбички с фъстъчено масло",
                "Смес от фъстъчено масло, кокосово масло и подсладител.",
                250, 5, 8, 22, MealType.SNACK, keto, MeatPreferenceType.NONE,
                true, false, true, false, false, "кето", "снак", "десерт"));


        // ==== Вегетарианска диета ====
        // --- Закуска (Breakfast) ---
        uniqueSeeds.add(new RSeed("Омлет със спанак и гъби",
                "Яйца, спанак, гъби. Запържете омлета.",
                350, 18, 15, 25, MealType.BREAKFAST, veg, MeatPreferenceType.NONE,
                true, true, false, false, false, "вегетарианска", "закуска"));
        uniqueSeeds.add(new RSeed("Кисело мляко с гранола и плодове",
                "Кисело мляко, гранола, пресни плодове.",
                320, 12, 40, 10, MealType.BREAKFAST, veg, MeatPreferenceType.NONE,
                true, true, false, false, false, "вегетарианска", "закуска"));
        uniqueSeeds.add(new RSeed("Тост с авокадо и поширано яйце",
                "Пълнозърнест тост, пюре от авокадо, поширано яйце.",
                400, 15, 30, 25, MealType.BREAKFAST, veg, MeatPreferenceType.NONE,
                true, true, false, false, false, "вегетарианска", "закуска"));
        uniqueSeeds.add(new RSeed("Пълнозърнест сандвич с хумус и зеленчуци",
                "Пълнозърнест хляб, хумус, краставици, моркови.",
                310, 10, 40, 12, MealType.BREAKFAST, veg, MeatPreferenceType.NONE,
                true, false, false, false, false, "вегетарианска", "бързо", "веган опция"));
        uniqueSeeds.add(new RSeed("Бъркани яйца със сирене и чушки",
                "Разбийте яйца със сирене и ситно нарязани чушки. Запържете до готовност.",
                380, 22, 10, 28, MealType.BREAKFAST, veg, MeatPreferenceType.NONE,
                true, true, false, false, false, "вегетарианска","кето-приятелски"));

        // --- Обяд (Lunch) ---
        uniqueSeeds.add(new RSeed("Салата Капрезе с киноа",
                "Моцарела, домати, босилек, киноа, зехтин.",
                420, 20, 35, 20, MealType.LUNCH, veg, MeatPreferenceType.NONE,
                true, true, false, false, false, "вегетарианска", "обяд", "без-глутен"));
        uniqueSeeds.add(new RSeed("Супа от леща с крутони",
                "Леща, зеленчуци, подправки. Сервирайте с пълнозърнести крутони.",
                380, 18, 50, 8, MealType.LUNCH, veg, MeatPreferenceType.NONE,
                true, false, false, false, false, "вегетарианска", "обяд", "фибри"));
        uniqueSeeds.add(new RSeed("Бургер от нахут със салата",
                "Котлет от нахут, пълнозърнеста питка, зеленчуци. Сервирайте със зелена салата.",
                450, 25, 45, 15, MealType.LUNCH, veg, MeatPreferenceType.NONE,
                true, true, false, false, false, "вегетарианска", "обяд"));
        uniqueSeeds.add(new RSeed("Паста с броколи и чесън",
                "Пълнозърнеста паста, броколи, чесън, зехтин, пармезан.",
                480, 18, 60, 18, MealType.LUNCH, veg, MeatPreferenceType.NONE,
                true, true, false, false, false, "вегетарианска", "бързо"));
        uniqueSeeds.add(new RSeed("Пълнени картофи с гъби и сирене",
                "Печен картоф, пълнен със запържени гъби и настъргано сирене.",
                460, 15, 50, 20, MealType.LUNCH, veg, MeatPreferenceType.NONE,
                true, true, false, false, false, "вегетарианска", " comfort food"));

        // --- Вечеря (Dinner) ---
        uniqueSeeds.add(new RSeed("Паста с песто и чери домати",
                "Пълнозърнеста паста, песто, чери домати.",
                480, 15, 60, 20, MealType.DINNER, veg, MeatPreferenceType.NONE,
                true, true, true, false, false, "вегетарианска", "вечеря"));
        uniqueSeeds.add(new RSeed("Зеленчуково къри с ориз Басмати",
                "Различни зеленчуци, кокосово мляко, къри паста, ориз басмати.",
                520, 12, 60, 25, MealType.DINNER, veg, MeatPreferenceType.NONE,
                true, false, false, false, false, "вегетарианска", "вечеря", "веган опция"));
        uniqueSeeds.add(new RSeed("Пълнени гъби Портобело",
                "Гъби Портобело, извара, спанак, подправки. Запечете.",
                400, 22, 15, 30, MealType.DINNER, veg, MeatPreferenceType.NONE,
                true, true, false, false, false, "вегетарианска", "вечеря"));
        uniqueSeeds.add(new RSeed("Омлет със сирене и задушени зеленчуци",
                "Голям омлет със сирене, сервиран със задушени моркови и броколи.",
                420, 20, 15, 30, MealType.DINNER, veg, MeatPreferenceType.NONE,
                true, true, false, false, false, "вегетарианска", "бърза"));
        uniqueSeeds.add(new RSeed("Лазаня със спанак и рикота",
                "Лазаня с рикота, спанак и доматен сос.",
                550, 25, 50, 28, MealType.DINNER, veg, MeatPreferenceType.NONE,
                true, true, false, false, false, "вегетарианска", "традиционно"));

        // --- Снак (Snack) ---
        uniqueSeeds.add(new RSeed("Морковени пръчици с хумус",
                "Нарязани моркови, хумус.",
                180, 5, 20, 8, MealType.SNACK, veg, MeatPreferenceType.NONE,
                true, false, false, false, false, "вегетарианска", "снак", "бързо", "веган"));
        uniqueSeeds.add(new RSeed("Плодов сандвич с фъстъчено масло",
                "Две парчета ябълка с фъстъчено масло между тях.",
                260, 6, 25, 14, MealType.SNACK, veg, MeatPreferenceType.NONE,
                true, false, true, false, false, "вегетарианска", "снак"));
        uniqueSeeds.add(new RSeed("Йогурт с горски плодове и семена",
                "Натурален йогурт, горски плодове, ленено семе.",
                270, 12, 25, 10, MealType.SNACK, veg, MeatPreferenceType.NONE,
                true, true, false, false, false, "вегетарианска", "снак"));
        uniqueSeeds.add(new RSeed("Зеленчуков сок (домат, целина, спанак)",
                "Изцеден сок от домати, целина и спанак.",
                120, 4, 20, 1, MealType.SNACK, veg, MeatPreferenceType.NONE,
                true, false, false, false, false, "вегетарианска", "свежо", "детокс"));
        uniqueSeeds.add(new RSeed("Парче пълнозърнест хляб с авокадо",
                "Парче пълнозърнест хляб с намачкано авокадо.",
                220, 6, 25, 12, MealType.SNACK, veg, MeatPreferenceType.NONE,
                true, false, false, false, false, "вегетарианска", "бързо"));


        // ==== Веган диета ====
        // --- Закуска (Breakfast) ---
        uniqueSeeds.add(new RSeed("Веган овесена каша с бадемово мляко и плодове",
                "Овесени ядки, бадемово мляко, горски плодове, кленов сироп.",
                330, 8, 55, 7, MealType.BREAKFAST, vegan, MeatPreferenceType.NONE,
                true, false, false, false, false, "веган", "закуска"));
        uniqueSeeds.add(new RSeed("Тост с авокадо и чери домати",
                "Пълнозърнест тост, авокадо, чери домати, магданоз.",
                380, 7, 28, 25, MealType.BREAKFAST, vegan, MeatPreferenceType.NONE,
                true, false, false, false, false, "веган", "закуска"));
        uniqueSeeds.add(new RSeed("Смути с банан, спанак и растително мляко",
                "Пасирайте банан, спанак и растително мляко до гладкост.",
                300, 10, 40, 10, MealType.BREAKFAST, vegan, MeatPreferenceType.NONE,
                true, false, false, false, false, "веган", "бързо", "зелено смути"));
        uniqueSeeds.add(new RSeed("Оризова каша с кокосово мляко и стафиди",
                "Сварете ориз с кокосово мляко, добавете стафиди.",
                350, 6, 60, 8, MealType.BREAKFAST, vegan, MeatPreferenceType.NONE,
                true, false, false, false, false, "веган", "сладко"));

        // --- Обяд (Lunch) ---
        uniqueSeeds.add(new RSeed("Леща чорба с пресен хляб",
                "Леща, зеленчуци, подправки, сервира се с пресен хляб.",
                400, 20, 55, 7, MealType.LUNCH, vegan, MeatPreferenceType.NONE,
                true, false, false, false, false, "веган", "обяд", "фибри"));
        uniqueSeeds.add(new RSeed("Фасул Яхния с кисели краставички",
                "Бял фасул, домати, лук, моркови, сервира се с кисели краставички.",
                450, 25, 60, 8, MealType.LUNCH, vegan, MeatPreferenceType.NONE,
                true, false, false, false, false, "веган", "обяд", "традиционна"));
        uniqueSeeds.add(new RSeed("Бургер с черен боб и сладки картофи",
                "Котлет от черен боб, пълнозърнеста питка, сервира се с печени сладки картофи.",
                500, 28, 65, 15, MealType.LUNCH, vegan, MeatPreferenceType.NONE,
                true, false, false, false, false, "веган", "обяд"));
        uniqueSeeds.add(new RSeed("Тофу бъркани яйца със спанак и гъби",
                "Разбъркано тофу със спанак и гъби, подправено с куркума за цвят.",
                420, 25, 20, 25, MealType.LUNCH, vegan, MeatPreferenceType.NONE,
                true, false, false, false, false, "веган", "закуска-обяд"));
        uniqueSeeds.add(new RSeed("Салата с нахут, червено цвекло и орехи",
                "Сварен нахут, червено цвекло, пресен магданоз, орехи и лимонов дресинг.",
                400, 18, 40, 18, MealType.LUNCH, vegan, MeatPreferenceType.NONE,
                true, false, true, false, false, "веган", "салата"));

        // --- Вечеря (Dinner) ---
        uniqueSeeds.add(new RSeed("Печени зеленчуци с тофу",
                "Микс от зеленчуци (броколи, чушки, лук), тофу, соев сос, изпечени на фурна.",
                470, 25, 30, 28, MealType.DINNER, vegan, MeatPreferenceType.NONE,
                true, false, false, false, false, "веган", "вечеря"));
        uniqueSeeds.add(new RSeed("Къри с нахут и ориз",
                "Нахут, кокосово мляко, къри паста, зеленчуци, сервира се с ориз.",
                530, 20, 70, 20, MealType.DINNER, vegan, MeatPreferenceType.NONE,
                true, false, false, false, false, "веган", "вечеря"));
        uniqueSeeds.add(new RSeed("Пълнени картофи с веган сирене и гъби",
                "Печени картофи, пълнени с веган сирене и запържени гъби.",
                480, 15, 60, 20, MealType.DINNER, vegan, MeatPreferenceType.NONE,
                true, false, false, false, false, "веган", "comfort food"));
        uniqueSeeds.add(new RSeed("Зеленчуков сувлаки (без месо)",
                "Шишчета от зеленчуци на грил, сервирани с питка и дзадзики от тофу.",
                450, 15, 50, 18, MealType.DINNER, vegan, MeatPreferenceType.NONE,
                true, false, false, false, false, "веган", "гръцки вкус"));

        // --- Снак (Snack) ---
        uniqueSeeds.add(new RSeed("Ябълка с бадемово масло",
                "Ябълка, бадемово масло.",
                280, 5, 25, 18, MealType.SNACK, vegan, MeatPreferenceType.NONE,
                true, false, true, false, false, "веган", "снак"));
        uniqueSeeds.add(new RSeed("Зеленчукови пръчици с гуакамоле",
                "Моркови, краставици, чушки с домашно гуакамоле.",
                220, 4, 15, 18, MealType.SNACK, vegan, MeatPreferenceType.NONE,
                true, false, false, false, false, "веган", "снак"));
        uniqueSeeds.add(new RSeed("Оризовки с авокадо и домати",
                "Оризовки, намазани с авокадо и резени домати.",
                200, 4, 20, 12, MealType.SNACK, vegan, MeatPreferenceType.NONE,
                true, false, false, false, false, "веган", "бързо"));
        uniqueSeeds.add(new RSeed("Веган протеинов бар",
                "Протеинов бар от магазин, подходящ за вегани.",
                250, 15, 25, 10, MealType.SNACK, vegan, MeatPreferenceType.NONE,
                true, false, false, false, false, "веган", "удобно"));


        // ==== Палео диета ====
        // --- Закуска (Breakfast) ---
        uniqueSeeds.add(new RSeed("Палео омлет със спанак и авокадо",
                "Яйца, спанак, кокосово масло, авокадо. Запържете всичко заедно.",
                340, 20, 6, 28, MealType.BREAKFAST, paleo, MeatPreferenceType.NONE,
                true, false, false, false, false, "палео", "закуска"));
        uniqueSeeds.add(new RSeed("Палео палачинки от банан и яйца",
                "Банан, яйца, кокосово масло. Изпечете палачинки.",
                370, 14, 24, 24, MealType.BREAKFAST, paleo, MeatPreferenceType.NONE,
                true, false, false, false, false, "палео", "закуска"));
        uniqueSeeds.add(new RSeed("Салата с авокадо и пушен бекон",
                "Авокадо, пушен бекон, рукола, зехтин. Смесете съставките.",
                410, 16, 8, 36, MealType.BREAKFAST, paleo, MeatPreferenceType.PORK,
                false, false, false, false, true, "палео", "закуска"));
        uniqueSeeds.add(new RSeed("Смути с бадемово мляко, ягоди и семена чиа",
                "Бадемово мляко, ягоди, чиа. Пасирайте всичко.",
                300, 10, 15, 20, MealType.BREAKFAST, paleo, MeatPreferenceType.NONE,
                true, false, false, false, false, "палео", "закуска"));
        uniqueSeeds.add(new RSeed("Телешки колбас без добавки с яйца",
                "Натурален телешки колбас, запържен с две яйца.",
                390, 25, 5, 30, MealType.BREAKFAST, paleo, MeatPreferenceType.BEEF,
                false, false, false, false, false, "палео", "високо-протеин"));

        // --- Обяд (Lunch) ---
        uniqueSeeds.add(new RSeed("Пилешки гърди с тиквички и зехтин",
                "Пилешко филе, тиквички, подправки. Изпечете във фурна.",
                480, 38, 10, 24, MealType.LUNCH, paleo, MeatPreferenceType.CHICKEN,
                false, false, false, false, false, "палео", "обяд"));
        uniqueSeeds.add(new RSeed("Говеждо задушено с броколи",
                "Телешко месо, броколи, зехтин. Задушете заедно.",
                520, 40, 8, 28, MealType.LUNCH, paleo, MeatPreferenceType.BEEF,
                false, false, false, false, false, "палео", "обяд"));
        uniqueSeeds.add(new RSeed("Сьомга на фурна с авокадо",
                "Сьомга, авокадо, лимон. Изпечете и сервирайте.",
                510, 36, 5, 34, MealType.LUNCH, paleo, MeatPreferenceType.FISH,
                false, false, false, true, false, "палео", "обяд"));
        uniqueSeeds.add(new RSeed("Тиквички с яйца и телешка кайма",
                "Тиквички, яйца, телешка кайма. Изпечете всичко заедно.",
                500, 34, 10, 30, MealType.LUNCH, paleo, MeatPreferenceType.BEEF,
                false, false, false, false, false, "палео", "обяд"));
        uniqueSeeds.add(new RSeed("Свинско филе с печени чушки",
                "Свинско филе, изпечено на фурна с различни видове чушки.",
                530, 38, 15, 30, MealType.LUNCH, paleo, MeatPreferenceType.PORK,
                false, false, false, false, true, "палео", "пълноценно"));
        uniqueSeeds.add(new RSeed("Пилешки бутчета с печени кореноплодни",
                "Пилешки бутчета, моркови, пащърнак, целина. Печете до златисто.",
                500, 35, 20, 25, MealType.LUNCH, paleo, MeatPreferenceType.CHICKEN,
                false, false, false, false, false, "палео", "comfort food"));

        // --- Вечеря (Dinner) ---
        uniqueSeeds.add(new RSeed("Риба на скара със зеленчуци микс",
                "Риба, тиквички, домати, чушки. Изпечете на скара.",
                460, 34, 8, 28, MealType.DINNER, paleo, MeatPreferenceType.FISH,
                false, false, false, true, false, "палео", "вечеря"));
        uniqueSeeds.add(new RSeed("Печени пилешки бутчета с розмарин",
                "Пилешки бутчета, подправки, розмарин. Изпечете до златисто.",
                480, 36, 6, 30, MealType.DINNER, paleo, MeatPreferenceType.CHICKEN,
                false, false, false, false, false, "палео", "вечеря"));
        uniqueSeeds.add(new RSeed("Пълнени чушки с телешка кайма",
                "Чушки, телешка кайма, подправки. Напълнете и запечете.",
                490, 32, 6, 26, MealType.DINNER, paleo, MeatPreferenceType.BEEF,
                false, false, false, false, false, "палео", "вечеря"));
        uniqueSeeds.add(new RSeed("Задушен заек с кореноплодни",
                "Заешко месо, моркови, пащърнак, целина. Задушете до готовност.",
                500, 36, 9, 26, MealType.DINNER, paleo, MeatPreferenceType.NO_PREFERENCE,
                false, false, false, false, false, "палео", "вечеря"));
        uniqueSeeds.add(new RSeed("Свинско свинско с праз и моркови",
                "Свинско месо, праз, моркови, задушени до готовност.",
                510, 35, 12, 28, MealType.DINNER, paleo, MeatPreferenceType.PORK,
                false, false, false, false, true, "палео", "лесно"));
        uniqueSeeds.add(new RSeed("Котлети от дивеч (елен/глиган) със сладък картоф",
                "Котлети от дивеч, печени със сладък картоф.",
                580, 45, 20, 30, MealType.DINNER, paleo, MeatPreferenceType.NO_PREFERENCE,
                false, false, false, false, false, "палео", "дивеч", "пълноценно"));

        // --- Снак (Snack) ---
        uniqueSeeds.add(new RSeed("Палео бар с фурми и ядки",
                "Фурми, бадемово масло, кокосови стърготини.",
                290, 6, 25, 18, MealType.SNACK, paleo, MeatPreferenceType.NONE,
                true, false, true, false, false, "палео", "снак"));
        uniqueSeeds.add(new RSeed("Шепа сурови ядки (без фъстъци)",
                "Микс от бадеми, орехи, кашу.",
                270, 8, 15, 22, MealType.SNACK, paleo, MeatPreferenceType.NONE,
                true, false, true, false, false, "палео", "снак"));
        uniqueSeeds.add(new RSeed("Парче сушено месо (без добавки)",
                "Домашно сушено телешко или пилешко месо.",
                150, 20, 2, 5, MealType.SNACK, paleo, MeatPreferenceType.BEEF,
                false, false, false, false, false, "палео", "протеин", "бързо"));
        uniqueSeeds.add(new RSeed("Плодове и семена",
                "Ябълка с тиквени семки.",
                200, 5, 25, 10, MealType.SNACK, paleo, MeatPreferenceType.NONE,
                true, false, false, false, false, "палео", "лесно"));

        // ==== Балансирана диета ====
        // --- Закуска (Breakfast) ---
        uniqueSeeds.add(new RSeed("Овесена каша с ябълка и канела",
                "Овесени ядки, ябълка, канела, мляко. Сварете овеса с млякото и добавете ябълка и канела.",
                340, 10, 50, 8, MealType.BREAKFAST, bal, MeatPreferenceType.NONE,
                true, true, false, false, false, "балансирана", "закуска", "бързо"));
        uniqueSeeds.add(new RSeed("Яйца със спанак и пълнозърнест тост",
                "Запържете яйцата със спанака и сервирайте с препечен пълнозърнест хляб.",
                360, 18, 20, 20, MealType.BREAKFAST, bal, MeatPreferenceType.NONE,
                true, true, false, false, false, "балансирана", "високо-протеин"));
        uniqueSeeds.add(new RSeed("Кисело мляко с овес, мед и орехи",
                "Смесете киселото мляко, овесените ядки и меда; поръсете с орехи.",
                310, 14, 35, 10, MealType.BREAKFAST, bal, MeatPreferenceType.NONE,
                true, true, true, false, false, "балансирана", "бърза"));
        uniqueSeeds.add(new RSeed("Сандвич с яйце, авокадо и домат",
                "Пълнозърнест хляб, варено яйце, авокадо и резени домат – идеален за път.",
                390, 15, 25, 24, MealType.BREAKFAST, bal, MeatPreferenceType.NONE,
                true, true, false, false, false, "балансирана", "to-go", "здравословни мазнини"));
        uniqueSeeds.add(new RSeed("Палачинки с извара и горски плодове",
                "Овесено брашно, извара и яйца. Изпечете палачинките и сервирайте с горски плодове.",
                350, 18, 30, 14, MealType.BREAKFAST, bal, MeatPreferenceType.NONE,
                true, true, false, false, false, "балансирана", "фит-десерт"));
        uniqueSeeds.add(new RSeed("Чиа пудинг с кокосово мляко и манго",
                "Накиснете чията за 4-6 ч. в кокосово мляко, добавете пресни парчета манго преди сервиране.",
                320, 9, 25, 15, MealType.BREAKFAST, bal, MeatPreferenceType.NONE,
                true, false, false, false, false, "балансирана", "prep-friendly", "веган опция"));
        uniqueSeeds.add(new RSeed("Мюсли с прясно мляко и банан",
                "Смесете мюслито с прясно мляко и нарязан банан, оставете 5 мин да омекне.",
                340, 10, 40, 8, MealType.BREAKFAST, bal, MeatPreferenceType.NONE,
                true, true, false, false, false, "балансирана", "classic"));
        uniqueSeeds.add(new RSeed("Бъркани яйца със сирене и чушки",
                "Разбийте яйца със сирене и ситно нарязани чушки. Запържете до готовност.",
                380, 22, 10, 28, MealType.BREAKFAST, bal, MeatPreferenceType.NONE,
                true, true, false, false, false, "балансирана", "кето-приятелски"));
        uniqueSeeds.add(new RSeed("Оризовки с авокадо и чиа",
                "Намажете оризовки с авокадо и поръсете с чиа семена.",
                300, 8, 30, 18, MealType.BREAKFAST, bal, MeatPreferenceType.NONE,
                true, false, false, false, false, "балансирана", "веган", "без-глутен"));
        uniqueSeeds.add(new RSeed("Плодова салата с котидж сирене",
                "Микс от сезонни плодове с котидж сирене.",
                290, 15, 35, 10, MealType.BREAKFAST, bal, MeatPreferenceType.NONE,
                true, true, false, false, false, "балансирана", "лека", "свежа"));

        // --- Обяд (Lunch) ---
        uniqueSeeds.add(new RSeed("Салата с киноа, риба тон и зеленчуци",
                "Сварете киноата, охладете и разбъркайте с риба тон, краставици, домати и маслини.",
                420, 28, 40, 14, MealType.LUNCH, bal, MeatPreferenceType.FISH,
                false, false, false, true, false, "балансирана", "без-глутен", "бърз обяд"));
        uniqueSeeds.add(new RSeed("Пилешко филе със сладък картоф и броколи",
                "Запечете пилешкото филе и резените сладък картоф във фурна 25 мин при 200 °C. Задушете броколите на пара.",
                480, 38, 35, 16, MealType.LUNCH, bal, MeatPreferenceType.CHICKEN,
                false, false, false, false, false, "балансирана", "meal-prep"));
        uniqueSeeds.add(new RSeed("Пъстърва на пара с кафяв ориз и задушени моркови",
                "Гответе пъстървата на пара; поднесете с кафяв ориз и задушени моркови.",
                500, 34, 38, 18, MealType.LUNCH, bal, MeatPreferenceType.FISH,
                false, false, false, true, false, "балансирана", "omega-3"));
        uniqueSeeds.add(new RSeed("Свинско месо със зеле и моркови",
                "Задушете свинското месо със зеле и моркови до пълно омекване. Овкусете с червен пипер.",
                510, 36, 20, 22, MealType.LUNCH, bal, MeatPreferenceType.PORK,
                false, false, false, false, true, "балансирана", "ниско-въглехидрати"));
        uniqueSeeds.add(new RSeed("Телешко с булгур и домати",
                "Сварете булгура; задушете телешкото с домати и поднесете заедно.",
                530, 40, 30, 20, MealType.LUNCH, bal, MeatPreferenceType.BEEF,
                false, false, false, false, false, "балансирана", "желязо"));
        uniqueSeeds.add(new RSeed("Голям омлет със зеленчуци (чушки, лук, гъби)",
                "Яйца с чушки, лук и гъби; изпечете в тефлонов тиган.",
                390, 20, 15, 22, MealType.LUNCH, bal, MeatPreferenceType.NONE,
                true, true, false, false, false, "балансирана", "вегетарианска"));
        uniqueSeeds.add(new RSeed("Тофу със зеленчуци и кафяв ориз",
                "Запържете тофуто с броколи, моркови и чушки, сервирайте върху кафяв ориз.",
                440, 24, 35, 14, MealType.LUNCH, bal, MeatPreferenceType.NONE,
                true, false, false, false, false, "балансирана", "веган", "бързо"));
        uniqueSeeds.add(new RSeed("Пилешка пържола със зелена салата и дресинг",
                "Изпечете пилешка пържола на тиган, сервирайте със свежа зелена салата и лек дресинг.",
                450, 40, 10, 25, MealType.LUNCH, bal, MeatPreferenceType.CHICKEN,
                false, false, false, false, false, "балансирана", "лесно"));
        uniqueSeeds.add(new RSeed("Салата с нахут, авокадо и краставица",
                "Смесете нахут, нарязано авокадо, краставица, червен лук и лимонов дресинг.",
                380, 15, 40, 18, MealType.LUNCH, bal, MeatPreferenceType.NONE,
                true, false, false, false, false, "балансирана", "веган", "без-gлутен"));
        uniqueSeeds.add(new RSeed("Морска храна с ориз и зеленчуци",
                "Калмари/скариди запържени с ориз и микс от зеленчуци.",
                500, 30, 45, 15, MealType.LUNCH, bal, MeatPreferenceType.FISH,
                false, false, false, true, false, "балансирана", "морска храна"));
        uniqueSeeds.add(new RSeed("Свинско филе със задушени гъби",
                "Печено свинско филе, сервирано със задушени гъби и пресен магданоз.",
                520, 38, 15, 28, MealType.LUNCH, bal, MeatPreferenceType.PORK,
                false, false, false, false, true, "балансирана", "ниско-въглехидрати"));

        // --- Вечеря (Dinner) ---
        uniqueSeeds.add(new RSeed("Леща с ориз и свежа салата",
                "Сварете лещата и ориза; поднесете с пресна салата от домати и краставици.",
                430, 22, 40, 10, MealType.DINNER, bal, MeatPreferenceType.NONE,
                true, false, false, false, false, "балансирана", "фибри"));
        uniqueSeeds.add(new RSeed("Пилешка супа със зеленчуци",
                "Пилешко месо, моркови, картофи, целина; вари се 40 мин и се овкусява с магданоз.",
                400, 30, 20, 12, MealType.DINNER, bal, MeatPreferenceType.CHICKEN,
                false, false, false, false, false, "балансирана", "comfort", "лека"));
        uniqueSeeds.add(new RSeed("Рататуй с тиквички, патладжан и домати",
                "Тиквички, патладжан, домати и чушки – печете 35 мин на 180 °C.",
                370, 8, 25, 14, MealType.DINNER, bal, MeatPreferenceType.NONE,
                true, false, false, false, false, "балансирана", "ниско-калорични", "веган"));
        uniqueSeeds.add(new RSeed("Тилапия на фурна със задушен зелен фасул",
                "Изпечете тилапията с лимонов сок; сервирайте със задушен зелен фасул.",
                460, 32, 18, 20, MealType.DINNER, bal, MeatPreferenceType.FISH,
                false, false, false, true, false, "балансирана", "постен протеин"));
        uniqueSeeds.add(new RSeed("Пълнени чушки с кайма и ориз",
                "Чушки, свинска и телешка кайма и ориз във фурна с доматен сос.",
                490, 28, 30, 18, MealType.DINNER, bal, MeatPreferenceType.NO_PREFERENCE,
                false, false, false, false, false, "балансирана", "класика"));
        uniqueSeeds.add(new RSeed("Киноа с печени зеленчуци и гъби",
                "Сварена киноа, тиквички, моркови и гъби, запечени със зехтин и подправки.",
                420, 16, 35, 14, MealType.DINNER, bal, MeatPreferenceType.NONE,
                true, false, false, false, false, "балансирана", "суперхрана", "веган"));
        uniqueSeeds.add(new RSeed("Яйца с броколи и пармезан",
                "Запечете яйца, броколи и настърган пармезан в керамичен съд.",
                410, 20, 15, 24, MealType.DINNER, bal, MeatPreferenceType.NONE,
                true, true, false, false, false, "балансирана", "кето-приятелски"));
        uniqueSeeds.add(new RSeed("Мусака с телешка кайма",
                "Слоеве от картофи, телешка кайма и бешамелов сос, запечени във фурна.",
                550, 35, 30, 30, MealType.DINNER, bal, MeatPreferenceType.BEEF,
                false, true, false, false, false, "балансирана", "традиционна"));
        uniqueSeeds.add(new RSeed("Агнешки котлети с розмарин и чесън",
                "Печени агнешки котлети с пресен розмарин и чесън.",
                560, 40, 10, 35, MealType.DINNER, bal, MeatPreferenceType.LAMB,
                false, false, false, false, false, "балансирана", "празнично"));
        uniqueSeeds.add(new RSeed("Свинско с гъби и ориз",
                "Свинско месо, гъби и бял ориз, задушени в соев сос.",
                530, 35, 30, 25, MealType.DINNER, bal, MeatPreferenceType.PORK,
                false, false, false, false, true, "балансирана", "азиатски вкус"));

        // --- Снак (Snack) ---
        uniqueSeeds.add(new RSeed("Орехи с кисело мляко и мед",
                "Кисело мляко, натрошени орехи и лъжичка мед.",
                280, 14, 10, 20, MealType.SNACK, bal, MeatPreferenceType.NONE,
                true, true, true, false, false, "балансирана", "здравословни мазнини"));
        uniqueSeeds.add(new RSeed("Ябълка с фъстъчено масло",
                "Нарежете ябълка на резени и намажете с фъстъчено масло.",
                270, 6, 25, 14, MealType.SNACK, bal, MeatPreferenceType.NONE,
                true, false, true, false, false, "балансирана", "on-the-go"));
        uniqueSeeds.add(new RSeed("Смути с кисело мляко, банан и горски плодове",
                "Пасирайте кисело мляко, банан и горски плодове до гладкост.",
                290, 10, 28, 10, MealType.SNACK, bal, MeatPreferenceType.NONE,
                true, true, false, false, false, "балансирана", "витамин-буст"));
        uniqueSeeds.add(new RSeed("Домашни барове с мюсли и ядки",
                "Мюсли, мед и ядки – изпечете 15 мин, оставете да стегнат.",
                300, 8, 30, 12, MealType.SNACK, bal, MeatPreferenceType.NONE,
                true, true, true, false, false, "балансирана", "prep-bars"));
        uniqueSeeds.add(new RSeed("Фреш от морков, ябълка и лимон",
                "Изцедете сока от морков, ябълка и лимон; сервирайте охладено.",
                150, 2, 28, 1, MealType.SNACK, bal, MeatPreferenceType.NONE,
                true, false, false, false, false, "балансирана", "свежо"));
        uniqueSeeds.add(new RSeed("Райска ябълка с кисело мляко",
                "Смесете нарязана райска ябълка с кисело мляко и мед.",
                260, 9, 20, 8, MealType.SNACK, bal, MeatPreferenceType.NONE,
                true, true, false, false, false, "балансирана", "сезонно"));
        uniqueSeeds.add(new RSeed("Грис с мляко и сладко",
                "Сварете гриса в прясно мляко, поръсете с канела и добавете лъжичка сладко.",
                310, 7, 35, 9, MealType.SNACK, bal, MeatPreferenceType.NONE,
                true, true, false, false, false, "балансирана", "носталгия"));
        uniqueSeeds.add(new RSeed("Протеинов шейк (банан и мляко)",
                "Банан, протеин на прах, мляко. Разбийте в блендер.",
                250, 25, 20, 5, MealType.SNACK, bal, MeatPreferenceType.NONE,
                true, true, false, false, false, "балансирана", "бърз протеин"));
        uniqueSeeds.add(new RSeed("Оризовки с хумус",
                "Намажете оризовки с хумус.",
                200, 5, 25, 8, MealType.SNACK, bal, MeatPreferenceType.NONE,
                true, false, false, false, false, "балансирана", "веган", "бързо"));
        uniqueSeeds.add(new RSeed("Шепа бадеми и сушени череши",
                "Микс от бадеми и сушени череши.",
                230, 7, 18, 15, MealType.SNACK, bal, MeatPreferenceType.NONE,
                true, false, true, false, false, "балансирана", "енергия"));
        uniqueSeeds.add(new RSeed("Варено яйце и зеленчукови пръчици",
                "Едно варено яйце със солети от морков и краставица.",
                180, 10, 10, 10, MealType.SNACK, bal, MeatPreferenceType.NONE,
                true, false, false, false, false, "балансирана", "бързо", "лека"));
        uniqueSeeds.add(new RSeed("Малка шепа кашу и фурми",
                "Кашу и фурми за бърза енергия.",
                250, 6, 30, 12, MealType.SNACK, bal, MeatPreferenceType.NONE,
                true, false, true, false, false, "балансирана", "енергия", "сладко"));


        // ==== Кето диета ====
        // --- Закуска (Breakfast) ---
        uniqueSeeds.add(new RSeed("Бъркани яйца с бекон и авокадо",
                "Запържете бекона, добавете яйцата и авокадото. Кето закуска.",
                450, 25, 5, 38, MealType.BREAKFAST, keto, MeatPreferenceType.PORK,
                false, false, false, false, true, "кето", "високо-мазнини"));
        uniqueSeeds.add(new RSeed("Омлет със сирене чедър и гъби",
                "Разбийте яйца със сирене чедър и гъби. Изпечете до златисто.",
                400, 20, 8, 32, MealType.BREAKFAST, keto, MeatPreferenceType.NONE,
                true, true, false, false, false, "кето", "вегетарианска"));
        uniqueSeeds.add(new RSeed("Кето смути с авокадо, спанак и кокосово мляко",
                "Пасирайте авокадо, спанак, кокосово мляко и малко протеин на прах.",
                380, 15, 10, 30, MealType.BREAKFAST, keto, MeatPreferenceType.NONE,
                true, false, false, false, false, "кето", "веган", "бързо"));
        uniqueSeeds.add(new RSeed("Кето палачинки с бадемово брашно и маскарпоне",
                "Палачинки от бадемово брашно, яйца и маскарпоне, сервирани с горски плодове.",
                420, 18, 12, 35, MealType.BREAKFAST, keto, MeatPreferenceType.NONE,
                true, true, true, false, false, "кето", "ниско-въглехидрати"));
        uniqueSeeds.add(new RSeed("Яйца по бенедиктински с холандски сос (без хляб)",
                "Поширани яйца, гарнирани с бекон и кето холандски сос.",
                470, 20, 5, 40, MealType.BREAKFAST, keto, MeatPreferenceType.PORK,
                false, true, false, false, true, "кето", "класика"));

        // --- Обяд (Lunch) ---
        uniqueSeeds.add(new RSeed("Сьомга на тиган със спаржа и лимон",
                "Изпечете сьомгата на тиган; сервирайте със задушена спаржа и резен лимон.",
                550, 35, 10, 45, MealType.LUNCH, keto, MeatPreferenceType.FISH,
                false, false, false, true, false, "кето", "омега-3"));
        uniqueSeeds.add(new RSeed("Пилешки бутчета с кожа и салата айсберг",
                "Запечете пилешки бутчета; поднесете със свежа салата айсберг.",
                600, 40, 8, 48, MealType.LUNCH, keto, MeatPreferenceType.CHICKEN,
                false, false, false, false, false, "кето", "високо-мазнини"));
        uniqueSeeds.add(new RSeed("Салата Цезар без крутони с пилешко",
                "Пилешко месо, маруля, пармезан, кето дресинг Цезар (без крутони).",
                480, 38, 10, 32, MealType.LUNCH, keto, MeatPreferenceType.CHICKEN,
                false, true, false, false, false, "кето", "класика"));
        uniqueSeeds.add(new RSeed("Кето пица с карфиолова основа и салам",
                "Основа от карфиол, доматен сос, моцарела и салам пеперони.",
                520, 30, 15, 40, MealType.LUNCH, keto, MeatPreferenceType.NO_PREFERENCE, // Салам може да е свински
                false, true, false, false, true, "кето", "комфорт-храна"));
        uniqueSeeds.add(new RSeed("Телешки кюфтета с доматен сос и моцарела",
                "Кюфтета от телешка кайма, приготвени в доматен сос и запечени с моцарела.",
                580, 45, 12, 40, MealType.LUNCH, keto, MeatPreferenceType.BEEF,
                false, true, false, false, false, "кето", "лесно"));

        // --- Вечеря (Dinner) ---
        uniqueSeeds.add(new RSeed("Агнешки котлети на грил с броколи",
                "Изпечете агнешки котлети на грил; сервирайте с броколи на пара.",
                650, 45, 10, 50, MealType.DINNER, keto, MeatPreferenceType.LAMB,
                false, false, false, false, false, "кето", "празнично"));
        uniqueSeeds.add(new RSeed("Пълнена тиква с телешка кайма и сирене",
                "Тиква, пълнена с телешка кайма, гъби и сирене, изпечена във фурна.",
                620, 42, 20, 45, MealType.DINNER, keto, MeatPreferenceType.BEEF,
                false, true, false, false, false, "кето", "сезонно"));
        uniqueSeeds.add(new RSeed("Кето лазаня с тиквички вместо кори",
                "Слоеве от тиквички, месо и сирене, запечени до златисто.",
                590, 38, 15, 48, MealType.DINNER, keto, MeatPreferenceType.NO_PREFERENCE,
                false, true, false, false, false, "кето", "комфорт-храна"));
        uniqueSeeds.add(new RSeed("Свинска пържола с масло и чесън",
                "Дебела свинска пържола, изпечена на тиган с масло и чесън, сервирана с малко зелена салата.",
                630, 40, 5, 50, MealType.DINNER, keto, MeatPreferenceType.PORK,
                false, true, false, false, true, "кето", "ниско-въглехидрати"));
        uniqueSeeds.add(new RSeed("Кето чили кон карне (без боб)",
                "Мляно телешко месо, домати, чушки, лук и подправки, приготвени без боб.",
                580, 45, 15, 40, MealType.DINNER, keto, MeatPreferenceType.BEEF,
                false, false, false, false, false, "кето", "засищащо"));

        // --- Снак (Snack) ---
        uniqueSeeds.add(new RSeed("Шепа бадеми и парче сирене",
                "Малка шепа бадеми и едно парче твърдо сирене.",
                220, 10, 5, 18, MealType.SNACK, keto, MeatPreferenceType.NONE,
                true, true, true, false, false, "кето", "бързо"));
        uniqueSeeds.add(new RSeed("Кето бомбички с фъстъчено масло",
                "Кето бомбички от фъстъчено масло, кокосово брашно и стевия.",
                280, 8, 8, 25, MealType.SNACK, keto, MeatPreferenceType.NONE,
                true, false, true, false, false, "кето", "десерт"));
        uniqueSeeds.add(new RSeed("Варено яйце с майонеза",
                "Едно варено яйце, намазано с майонеза.",
                150, 8, 1, 12, MealType.SNACK, keto, MeatPreferenceType.NONE,
                true, false, false, false, false, "кето", "бърз протеин"));
        uniqueSeeds.add(new RSeed("Парче авокадо със сол и черен пипер",
                "Нарежете половин авокадо и поръсете със сол и черен пипер.",
                180, 2, 8, 16, MealType.SNACK, keto, MeatPreferenceType.NONE,
                true, false, false, false, false, "кето", "здравословни мазнини"));
        uniqueSeeds.add(new RSeed("Кето бар с ядки и семена",
                "Домашен кето бар от ядки, семена и кокосово масло.",
                250, 10, 10, 20, MealType.SNACK, keto, MeatPreferenceType.NONE,
                true, false, true, false, false, "кето", "енергия"));


        // ==== Протеинова диета ====
        // --- Закуска (Breakfast) ---
        uniqueSeeds.add(new RSeed("Протеинов омлет с пилешко филе и чушки",
                "Яйчен белтък, нарязано пилешко филе и чушки. Запържете до готовност.",
                380, 40, 10, 15, MealType.BREAKFAST, prot, MeatPreferenceType.CHICKEN,
                false, false, false, false, false, "протеинова", "високо-протеин"));
        uniqueSeeds.add(new RSeed("Извара с канела и малко мед",
                "Извара, поръсена с канела и лъжичка мед.",
                280, 30, 20, 8, MealType.BREAKFAST, prot, MeatPreferenceType.NONE,
                true, true, false, false, false, "протеинова", "бърза", "ниско-мазнини"));
        uniqueSeeds.add(new RSeed("Протеинови палачинки с боровинки",
                "Палачинки от протеин на прах, овесено брашно и яйца, с пресни боровинки.",
                350, 35, 25, 10, MealType.BREAKFAST, prot, MeatPreferenceType.NONE,
                true, true, false, false, false, "протеинова", "фит-десерт"));
        uniqueSeeds.add(new RSeed("Скир с протеин на прах и малини",
                "Скир (скир), протеин на прах и пресни малини.",
                320, 40, 15, 5, MealType.BREAKFAST, prot, MeatPreferenceType.NONE,
                true, true, false, false, false, "протеинова", "бързо", "много-протеин"));
        uniqueSeeds.add(new RSeed("Бъркани яйца с шунка",
                "Яйца, бъркани с нарязана шунка.",
                390, 28, 5, 28, MealType.BREAKFAST, prot, MeatPreferenceType.PORK,
                false, false, false, false, true, "протеинова", "класика"));

        // --- Обяд (Lunch) ---
        uniqueSeeds.add(new RSeed("Телешки стек на грил със зелен фасул",
                "Телешки стек, изпечен на грил, сервиран със задушен зелен фасул.",
                500, 50, 10, 28, MealType.LUNCH, prot, MeatPreferenceType.BEEF,
                false, false, false, false, false, "протеинова", "червено месо"));
        uniqueSeeds.add(new RSeed("Пилешки гърди с киноа и задушени моркови",
                "Печени пилешки гърди с киноа и задушени моркови.",
                480, 45, 30, 15, MealType.LUNCH, prot, MeatPreferenceType.CHICKEN,
                false, false, false, false, false, "протеинова", "clean-eating"));
        uniqueSeeds.add(new RSeed("Треска на фурна с картофи и копър",
                "Филе от треска, изпечено на фурна с картофи и свеж копър.",
                450, 40, 30, 15, MealType.LUNCH, prot, MeatPreferenceType.FISH,
                false, false, false, true, false, "протеинова", "постна риба"));
        uniqueSeeds.add(new RSeed("Калмари на тиган със салата от рукола",
                "Запържени калмари на тиган, сервирани със салата от рукола и лимонов дресинг.",
                470, 42, 10, 25, MealType.LUNCH, prot, MeatPreferenceType.FISH,
                false, false, false, true, false, "протеинова", "морска храна"));
        uniqueSeeds.add(new RSeed("Свинско контрафиле на грил със зелена салата",
                "Свинско контрафиле, изпечено на грил, със зелена салата и лек дресинг.",
                520, 48, 10, 30, MealType.LUNCH, prot, MeatPreferenceType.PORK,
                false, false, false, false, true, "протеинова", "ниско-въглехидрати"));

        // --- Вечеря (Dinner) ---
        uniqueSeeds.add(new RSeed("Пуешка кайма с броколи и гъби",
                "Пуешка кайма, задушена с броколи и гъби.",
                420, 40, 15, 20, MealType.DINNER, prot, MeatPreferenceType.CHICKEN,
                false, false, false, false, false, "протеинова", "постен протеин"));
        uniqueSeeds.add(new RSeed("Огретен с извара и яйца",
                "Запечен огретен от извара, яйца и малко сирене.",
                400, 35, 10, 22, MealType.DINNER, prot, MeatPreferenceType.NONE,
                true, true, false, false, false, "протеинова", "вегетарианска"));
        uniqueSeeds.add(new RSeed("Пилешки шишчета със зеленчуци",
                "Пилешки кубчета, нанизани на шиш със зеленчуци (чушки, лук, чери домати), изпечени на грил.",
                450, 40, 20, 18, MealType.DINNER, prot, MeatPreferenceType.CHICKEN,
                false, false, false, false, false, "протеинова", "бързо"));
        uniqueSeeds.add(new RSeed("Протеинова салата с боб, царевица и авокадо",
                "Микс от боб, царевица, нарязано авокадо, червен лук и лимонов дресинг.",
                430, 25, 40, 18, MealType.DINNER, prot, MeatPreferenceType.NONE,
                true, false, false, false, false, "протеинова", "веган", "без-глутен"));
        uniqueSeeds.add(new RSeed("Мини кюфтенца от телешко с карфиолово пюре",
                "Малки кюфтенца от телешка кайма, сервирани с пюре от карфиол.",
                480, 45, 15, 28, MealType.DINNER, prot, MeatPreferenceType.BEEF,
                false, false, false, false, false, "протеинова", "ниско-въглехидрати"));

        // --- Снак (Snack) ---
        uniqueSeeds.add(new RSeed("Протеинов бар",
                "Готов протеинов бар.",
                220, 20, 20, 8, MealType.SNACK, prot, MeatPreferenceType.NONE,
                false, true, true, false, false, "протеинова", "бързо", "on-the-go"));
        uniqueSeeds.add(new RSeed("Протеинов пудинг",
                "Готов протеинов пудинг.",
                180, 20, 15, 5, MealType.SNACK, prot, MeatPreferenceType.NONE,
                true, true, false, false, false, "протеинова", "бързо"));
        uniqueSeeds.add(new RSeed("Котидж сирене с чушка",
                "Котидж сирене, сервирано с нарязана червена чушка.",
                150, 18, 5, 8, MealType.SNACK, prot, MeatPreferenceType.NONE,
                true, true, false, false, false, "протеинова", "бързо", "лека"));
        uniqueSeeds.add(new RSeed("Сурово кашу и стафиди",
                "Малка шепа сурово кашу и стафиди.",
                200, 6, 25, 10, MealType.SNACK, prot, MeatPreferenceType.NONE,
                true, false, true, false, false, "протеинова", "енергия"));
        uniqueSeeds.add(new RSeed("Филийка пълнозърнест хляб с пуешко филе",
                "Една филийка пълнозърнест хляб с две парчета пуешко филе.",
                190, 15, 20, 5, MealType.SNACK, prot, MeatPreferenceType.VEGETARIAN,
                false, false, false, false, false, "протеинова", "бързо"));


        // ==== Вегетарианска диета ====
        // --- Закуска (Breakfast) ---
        uniqueSeeds.add(new RSeed("Яйца на очи с авокадо и чери домати",
                "Две яйца на очи, сервирани с половин авокадо и чери домати.",
                370, 16, 20, 25, MealType.BREAKFAST, veg, MeatPreferenceType.NONE,
                true, false, false, false, false, "вегетарианска", "здравословни мазнини"));
        uniqueSeeds.add(new RSeed("Тост с хумус и краставица",
                "Пълнозърнест тост, намазан с хумус и гарниран с резени краставица.",
                300, 10, 40, 10, MealType.BREAKFAST, veg, MeatPreferenceType.NONE,
                true, false, false, false, false, "вегетарианска", "бързо"));
        uniqueSeeds.add(new RSeed("Гръцко кисело мляко с мед и плодове",
                "Гръцко кисело мляко, мед и микс от сезонни плодове.",
                330, 20, 30, 10, MealType.BREAKFAST, veg, MeatPreferenceType.NONE,
                true, true, false, false, false, "вегетарианска", "високо-протеин"));
        uniqueSeeds.add(new RSeed("Сандвичи с крема сирене и репички",
                "Пълнозърнест хляб, намазан с крема сирене и резени репички.",
                320, 12, 30, 15, MealType.BREAKFAST, veg, MeatPreferenceType.NONE,
                true, true, false, false, false, "вегетарианска", "лека"));

        // --- Обяд (Lunch) ---
        uniqueSeeds.add(new RSeed("Салата Табуле с нахут",
                "Салата от булгур, домати, краставици, магданоз, мента и нахут, овкусена с лимон и зехтин.",
                400, 15, 60, 12, MealType.LUNCH, veg, MeatPreferenceType.NONE,
                true, false, false, false, false, "вегетарианска", "свежа"));
        uniqueSeeds.add(new RSeed("Супа от гъби и сметана",
                "Кремообразна гъбена супа със сметана и пресен магданоз.",
                380, 10, 25, 25, MealType.LUNCH, veg, MeatPreferenceType.NONE,
                true, true, false, false, false, "вегетарианска", "уютно"));
        uniqueSeeds.add(new RSeed("Вегетариански бургер с гъби Портобело",
                "Гъба Портобело, изпечена на грил, сервирана в пълнозърнеста питка със салата.",
                450, 18, 40, 20, MealType.LUNCH, veg, MeatPreferenceType.NONE,
                true, false, false, false, false, "вегетарианска", "бързо хранене"));
        uniqueSeeds.add(new RSeed("Пълнени чушки с ориз и зеленчуци",
                "Чушки, пълнени с ориз, моркови, лук, домати и подправки, запечени във фурна.",
                420, 12, 50, 15, MealType.LUNCH, veg, MeatPreferenceType.NONE,
                true, false, false, false, false, "вегетарианска", "класика"));

        // --- Вечеря (Dinner) ---
        uniqueSeeds.add(new RSeed("Паста с песто и чери домати",
                "Пълнозърнеста паста с домашно песто и пресни чери домати.",
                480, 15, 60, 20, MealType.DINNER, veg, MeatPreferenceType.NONE,
                true, true, true, false, false, "вегетарианска", "бързо"));
        uniqueSeeds.add(new RSeed("Ориз с гъби и сметана",
                "Бял ориз, задушен с гъби, лук и сметана.",
                460, 10, 55, 20, MealType.DINNER, veg, MeatPreferenceType.NONE,
                true, true, false, false, false, "вегетарианска", "уютно"));
        uniqueSeeds.add(new RSeed("Патладжан с доматен сос и моцарела",
                "Резени патладжан, запечени с доматен сос и моцарела.",
                430, 15, 30, 25, MealType.DINNER, veg, MeatPreferenceType.NONE,
                true, true, false, false, false, "вегетарианска", "италианско"));
        uniqueSeeds.add(new RSeed("Зеленчуково къри с кокосово мляко",
                "Разнообразни зеленчуци в кремообразен къри сос с кокосово мляко, сервирани с басмати ориз.",
                500, 15, 55, 25, MealType.DINNER, veg, MeatPreferenceType.NONE,
                true, false, false, false, false, "вегетарианска", "азиатско", "веган опция"));

        // --- Снак (Snack) ---
        uniqueSeeds.add(new RSeed("Ориз с прясно мляко",
                "Сварен ориз, подсладен с малко захар и прясно мляко.",
                280, 8, 40, 10, MealType.SNACK, veg, MeatPreferenceType.NONE,
                true, true, false, false, false, "вегетарианска", "носталгия"));
        uniqueSeeds.add(new RSeed("Плодове и шепа ядки",
                "Смесени сезонни плодове с малка шепа ядки.",
                250, 5, 30, 15, MealType.SNACK, veg, MeatPreferenceType.NONE,
                true, false, true, false, false, "вегетарианска", "бързо", "свежо"));
        uniqueSeeds.add(new RSeed("Зеленчукови пръчици с хумус",
                "Моркови, краставици и целина, нарязани на пръчици, сервирани с хумус.",
                200, 8, 20, 10, MealType.SNACK, veg, MeatPreferenceType.NONE,
                true, false, false, false, false, "вегетарианска", "лека", "свежо"));


        // ==== Веган диета ====
        // --- Закуска (Breakfast) ---
        uniqueSeeds.add(new RSeed("Овесена каша с ябълка и бадемово мляко",
                "Овесени ядки, ябълка, канела, бадемово мляко. Сварете овеса с млякото и добавете ябълка и канела.",
                330, 9, 50, 7, MealType.BREAKFAST, vegan, MeatPreferenceType.NONE,
                true, false, true, false, false, "веган", "закуска", "бързо"));
        uniqueSeeds.add(new RSeed("Тофу с бъркани зеленчуци",
                "Тофу, натрошено и запържено с чушки, лук и спанак. Овкусете с куркума.",
                350, 20, 15, 20, MealType.BREAKFAST, vegan, MeatPreferenceType.NONE,
                true, false, false, false, false, "веган", "високо-протеин"));
        uniqueSeeds.add(new RSeed("Чиа пудинг с ягоди и кленов сироп",
                "Чиа семена, накиснати в растително мляко, с пресни ягоди и кленов сироп.",
                310, 8, 30, 15, MealType.BREAKFAST, vegan, MeatPreferenceType.NONE,
                true, false, false, false, false, "веган", "prep-friendly"));

        // --- Обяд (Lunch) ---
        uniqueSeeds.add(new RSeed("Леща на яхния с моркови и домати",
                "Леща, сварена със ситно нарязани моркови, лук и домати.",
                410, 20, 50, 8, MealType.LUNCH, vegan, MeatPreferenceType.NONE,
                true, false, false, false, false, "веган", "фибри"));
        uniqueSeeds.add(new RSeed("Сандвичи с авокадо, домат и рукола",
                "Пълнозърнест хляб с намачкано авокадо, резени домат и рукола.",
                390, 10, 35, 22, MealType.LUNCH, vegan, MeatPreferenceType.NONE,
                true, false, false, false, false, "веган", "бързо", "здравословни мазнини"));
        uniqueSeeds.add(new RSeed("Веган боул с киноа, черен боб и царевица",
                "Киноа, черен боб, царевица, нарязано авокадо и пикантен дресинг.",
                460, 20, 55, 18, MealType.LUNCH, vegan, MeatPreferenceType.NONE,
                true, false, false, false, false, "веган", "суперхрана", "без-глутен"));

        // --- Вечеря (Dinner) ---
        uniqueSeeds.add(new RSeed("Зеленчуково къри с тофу и ориз басмати",
                "Тофу, зеленчуци и кокосово мляко, приготвени като къри, сервирани с ориз басмати.",
                520, 25, 60, 20, MealType.DINNER, vegan, MeatPreferenceType.NONE,
                true, false, false, false, false, "веган", "азиатско"));
        uniqueSeeds.add(new RSeed("Нахут със спанак и домати",
                "Нахут, задушен със спанак и домати, овкусен с подправки.",
                450, 18, 55, 15, MealType.DINNER, vegan, MeatPreferenceType.NONE,
                true, false, false, false, false, "веган", "бързо"));
        uniqueSeeds.add(new RSeed("Веган лазаня с тиквички и зеленчуци",
                "Лазаня с тиквички вместо кори, доматен сос и различни зеленчуци.",
                480, 15, 40, 25, MealType.DINNER, vegan, MeatPreferenceType.NONE,
                true, false, false, false, false, "веган", "комфорт-храна"));

        // --- Снак (Snack) ---
        uniqueSeeds.add(new RSeed("Ориз с кокосово мляко и стафиди",
                "Сварен ориз с кокосово мляко, подсладен и поръсен със стафиди.",
                290, 6, 45, 10, MealType.SNACK, vegan, MeatPreferenceType.NONE,
                true, false, false, false, false, "веган", "сладко"));
        uniqueSeeds.add(new RSeed("Ябълка с бадемово масло",
                "Нарежете ябълка на резени и намажете с бадемово масло.",
                260, 5, 25, 15, MealType.SNACK, vegan, MeatPreferenceType.NONE,
                true, false, true, false, false, "веган", "on-the-go"));
        uniqueSeeds.add(new RSeed("Смути от банан, спанак и растително мляко",
                "Пасирайте банан, спанак и растително мляко до гладкост.",
                270, 8, 30, 8, MealType.SNACK, vegan, MeatPreferenceType.NONE,
                true, false, false, false, false, "веган", "енергия"));


        // ==== Палео диета ====
        // --- Закуска (Breakfast) ---
        uniqueSeeds.add(new RSeed("Бъркани яйца с гъби и пуешка шунка",
                "Яйца, гъби и пуешка шунка, запържени заедно.",
                420, 28, 10, 30, MealType.BREAKFAST, paleo, MeatPreferenceType.NONE,
                false, false, false, false, false, "палео", "високо-протеин"));
        uniqueSeeds.add(new RSeed("Палео палачинки с кокосово брашно и горски плодове",
                "Палачинки от кокосово брашно, яйца и банан, сервирани с горски плодове.",
                380, 15, 25, 22, MealType.BREAKFAST, paleo, MeatPreferenceType.NONE,
                true, false, false, false, false, "палео", "без-глутен"));
        uniqueSeeds.add(new RSeed("Сьомга на грил с авокадо",
                "Парче сьомга, изпечено на грил, сервирано с резени авокадо.",
                480, 30, 8, 35, MealType.BREAKFAST, paleo, MeatPreferenceType.FISH,
                false, false, false, true, false, "палео", "омега-3"));

        // --- Обяд (Lunch) ---
        uniqueSeeds.add(new RSeed("Телешки стек със сладки картофи и зеленчуци",
                "Телешки стек на грил, сервиран с печени сладки картофи и сезонни зеленчуци.",
                550, 45, 30, 28, MealType.LUNCH, paleo, MeatPreferenceType.BEEF,
                false, false, false, false, false, "палео", "засищащо"));
        uniqueSeeds.add(new RSeed("Пилешка салата с авокадо и ядки",
                "Нарязано печено пилешко, микс салати, авокадо, ядки и зехтинов дресинг.",
                500, 40, 15, 30, MealType.LUNCH, paleo, MeatPreferenceType.CHICKEN,
                false, false, true, false, false, "палео", "бързо"));
        uniqueSeeds.add(new RSeed("Свински котлети с печени ябълки и лук",
                "Свински котлети, изпечени със резени ябълки и лук.",
                580, 40, 25, 35, MealType.LUNCH, paleo, MeatPreferenceType.PORK,
                false, false, false, false, true, "палео", "плодове и месо"));

        // --- Вечеря (Dinner) ---
        uniqueSeeds.add(new RSeed("Агнешко кебапче с печени чушки и патладжан",
                "Агнешки кебапчета, изпечени с чушки и патладжан на грил.",
                600, 50, 20, 35, MealType.DINNER, paleo, MeatPreferenceType.LAMB,
                false, false, false, false, false, "палео", "богато"));
        uniqueSeeds.add(new RSeed("Пуешки кюфтета с доматен сос и тиквички",
                "Кюфтета от пуешка кайма в доматен сос, сервирани със задушени тиквички.",
                480, 40, 20, 25, MealType.DINNER, paleo, MeatPreferenceType.NO_PREFERENCE,
                false, false, false, false, false, "палео", "постен протеин"));
        uniqueSeeds.add(new RSeed("Морски дарове на грил с аспержи",
                "Микс от морски дарове (скариди, калмари) на грил, сервирани с аспержи.",
                520, 35, 15, 28, MealType.DINNER, paleo, MeatPreferenceType.FISH,
                false, false, false, true, false, "палео", "морска храна"));

        // --- Снак (Snack) ---
        uniqueSeeds.add(new RSeed("Варено яйце и парче плод",
                "Едно варено яйце и един среден плод (например, банан или ябълка).",
                200, 10, 20, 10, MealType.SNACK, paleo, MeatPreferenceType.NONE,
                true, false, false, false, false, "палео", "бързо"));
        uniqueSeeds.add(new RSeed("Сурови моркови и малко орехи",
                "Малка шепа сурови моркови и няколко ореха.",
                180, 5, 15, 12, MealType.SNACK, paleo, MeatPreferenceType.NONE,
                true, false, true, false, false, "палео", "хрупкаво"));
        uniqueSeeds.add(new RSeed("Малко сушено месо (джърки)",
                "Малко количество сушено телешко или пуешко месо (без захар).",
                150, 20, 5, 8, MealType.SNACK, paleo, MeatPreferenceType.BEEF,
                false, false, false, false, false, "палео", "протеин"));


        // Записване на уникалните рецепти в базата данни
        uniqueSeeds.forEach(s -> {
            // Използвайте existsByNameAndMealType, за да избегнете дублиране при повторно стартиране
            // Уверете се, че този метод е добавен към вашия RecipeRepository
            if (!recipeRepository.existsByNameAndMealType(s.n, s.meal)) {
                Recipe recipe = Recipe.builder()
                        .name(s.n)
                        .description(s.d)
                        .calories(s.kcal)
                        .protein(s.p)
                        .carbs(s.c)
                        .fat(s.f)
                        .mealType(s.meal)
                        .dietType(s.diet)
                        .meatType(s.meat)
                        .isVegetarian(s.veg)
                        .containsDairy(s.dairy)
                        .containsNuts(s.nuts)
                        .containsFish(s.fish) // Ново поле
                        .containsPork(s.pork) // Ново поле
                        .tags(new HashSet<>(Arrays.asList(s.tags)))
                        .allergens(new HashSet<>()) // Добави конкретни алергени, ако е необходимо
                        .instructions("Пригответе според описанието или намерете пълни инструкции онлайн.") // Примерни инструкции
                        .build();
                recipeRepository.save(recipe);
            }
        });
        System.out.println("✔ Рецептите синхронизирани");
    }



    /* ==================================*/
    /*               EXERCISES           */
    /* ==================================*/
    private record ESeed(String n,String d,Integer sets,Integer reps,Integer dur,
                         ExerciseType type,DifficultyLevel lvl,EquipmentType eq){}

    private void seedExercises() {
        if (exerciseRepository.count() > 0) return;
        List<ESeed> ex = new ArrayList<>();

        // BODYWEIGHT EXERCISES (Total: 15)
        ex.addAll(List.of(
                // BEGINNER
                new ESeed("Клякания","Крака",3,15,8,ExerciseType.BODYWEIGHT,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                new ESeed("Лицеви опори","Гърди",3,12,6,ExerciseType.BODYWEIGHT,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                new ESeed("Планк","Корем",3,1,5,ExerciseType.BODYWEIGHT,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                new ESeed("Напади","Крака",3,12,8,ExerciseType.BODYWEIGHT,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                new ESeed("Глутеус мост","Седалище",3,15,8,ExerciseType.BODYWEIGHT,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                // INTERMEDIATE
                new ESeed("Бърпи","Кардио",3,10,8,ExerciseType.BODYWEIGHT,DifficultyLevel.INTERMEDIATE,EquipmentType.NONE),
                new ESeed("Алпинисти","HIIT",3,20,6,ExerciseType.BODYWEIGHT,DifficultyLevel.INTERMEDIATE,EquipmentType.NONE),
                new ESeed("Кофички","Трицепс",3,10,8,ExerciseType.BODYWEIGHT,DifficultyLevel.INTERMEDIATE,EquipmentType.GYM_EQUIPMENT), // Може и без оборудване
                new ESeed("Диамантени лицеви опори","Трицепс",3,10,6,ExerciseType.BODYWEIGHT,DifficultyLevel.INTERMEDIATE,EquipmentType.NONE),
                new ESeed("Плиометрични лицеви опори","Експлозивна сила",3,8,7,ExerciseType.BODYWEIGHT,DifficultyLevel.INTERMEDIATE,EquipmentType.NONE),
                // ADVANCED
                new ESeed("Повдигане крака от вис","Корем",3,12,6,ExerciseType.BODYWEIGHT,DifficultyLevel.ADVANCED,EquipmentType.GYM_EQUIPMENT),
                new ESeed("Единични клякания (Pistol Squat)","Крака",3,8,8,ExerciseType.BODYWEIGHT,DifficultyLevel.ADVANCED,EquipmentType.NONE),
                new ESeed("V-ups","Корем",3,12,7,ExerciseType.BODYWEIGHT,DifficultyLevel.ADVANCED,EquipmentType.NONE),
                new ESeed("Набирания","Гръб",3,8,10,ExerciseType.BODYWEIGHT,DifficultyLevel.ADVANCED,EquipmentType.GYM_EQUIPMENT),
                new ESeed("Стойка на ръце","Рамена",3,1,5,ExerciseType.BODYWEIGHT,DifficultyLevel.ADVANCED,EquipmentType.NONE)
        ));

        // WEIGHTS EXERCISES (Total: 15)
        ex.addAll(List.of(
                // BEGINNER
                new ESeed("Бицепс сгъване с дъмбели","Бицепс",3,12,6,ExerciseType.WEIGHTS,DifficultyLevel.BEGINNER,EquipmentType.DUMBBELLS),
                new ESeed("Трицепс разгъване над глава","Трицепс",3,12,6,ExerciseType.WEIGHTS,DifficultyLevel.BEGINNER,EquipmentType.DUMBBELLS),
                new ESeed("Флайс с дъмбели","Гърди",3,12,7,ExerciseType.WEIGHTS,DifficultyLevel.BEGINNER,EquipmentType.DUMBBELLS),
                new ESeed("Повдигане на пръсти с дъмбели","Прасци",4,15,5,ExerciseType.WEIGHTS,DifficultyLevel.BEGINNER,EquipmentType.DUMBBELLS),
                new ESeed("Махове с дъмбели встрани","Рамена",3,12,6,ExerciseType.WEIGHTS,DifficultyLevel.BEGINNER,EquipmentType.DUMBBELLS),
                // INTERMEDIATE
                new ESeed("Бенч преса с щанга","Гърди",4,10,10,ExerciseType.WEIGHTS,DifficultyLevel.INTERMEDIATE,EquipmentType.BARBELL),
                new ESeed("Напади с дъмбели","Крака",3,12,8,ExerciseType.WEIGHTS,DifficultyLevel.INTERMEDIATE,EquipmentType.DUMBBELLS),
                new ESeed("Раменна преса с дъмбели","Рамена",3,10,8,ExerciseType.WEIGHTS,DifficultyLevel.INTERMEDIATE,EquipmentType.DUMBBELLS),
                new ESeed("Лег преса","Крака",3,12,8,ExerciseType.WEIGHTS,DifficultyLevel.INTERMEDIATE,EquipmentType.GYM_EQUIPMENT),
                new ESeed("Дърпане на скрипец","Гръб",3,12,9,ExerciseType.WEIGHTS,DifficultyLevel.INTERMEDIATE,EquipmentType.GYM_EQUIPMENT),
                new ESeed("Сгъване за задно бедро","Крака",3,12,7,ExerciseType.WEIGHTS,DifficultyLevel.INTERMEDIATE,EquipmentType.GYM_EQUIPMENT),
                new ESeed("Разгъване за предно бедро","Крака",3,12,7,ExerciseType.WEIGHTS,DifficultyLevel.INTERMEDIATE,EquipmentType.GYM_EQUIPMENT),
                // ADVANCED
                new ESeed("Мъртва тяга","Комплекс",4,8,12,ExerciseType.WEIGHTS,DifficultyLevel.ADVANCED,EquipmentType.BARBELL),
                new ESeed("Клек с щанга","Крака",4,10,10,ExerciseType.WEIGHTS,DifficultyLevel.ADVANCED,EquipmentType.BARBELL),
                new ESeed("Гребане с щанга","Гръб",3,10,10,ExerciseType.WEIGHTS,DifficultyLevel.ADVANCED,EquipmentType.BARBELL)
        ));

        // CARDIO EXERCISES (Total: 5)
        ex.addAll(List.of(
                new ESeed("Бягане пътека","Кардио",null,null,30,ExerciseType.CARDIO,DifficultyLevel.BEGINNER,EquipmentType.GYM_EQUIPMENT),
                new ESeed("Колоездене стационарно","Кардио",null,null,45,ExerciseType.CARDIO,DifficultyLevel.INTERMEDIATE,EquipmentType.GYM_EQUIPMENT),
                new ESeed("Скачане въже","HIIT",null,null,15,ExerciseType.CARDIO,DifficultyLevel.INTERMEDIATE,EquipmentType.NONE),
                new ESeed("Плуване","Цяло тяло",null,null,40,ExerciseType.CARDIO,DifficultyLevel.ADVANCED,EquipmentType.NONE),
                new ESeed("Елиптикъл","Кардио",null,null,35,ExerciseType.CARDIO,DifficultyLevel.BEGINNER,EquipmentType.GYM_EQUIPMENT)
        ));

        // OTHER EXERCISES (Total: 5)
        ex.addAll(List.of(
                new ESeed("Йога - Поздрав към слънцето","Гъвкавост и баланс",null,null,20,ExerciseType.OTHER,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                new ESeed("Пилатес - Стомах","Ядро и контрол",null,null,30,ExerciseType.OTHER,DifficultyLevel.INTERMEDIATE,EquipmentType.NONE),
                new ESeed("Стречинг - Цяло тяло","Възстановяване",null,null,15,ExerciseType.OTHER,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                new ESeed("Тай Чи","Баланс и концентрация",null,null,25,ExerciseType.OTHER,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                new ESeed("Бокс на чувал","Кардио и сила",null,null,20,ExerciseType.OTHER,DifficultyLevel.INTERMEDIATE,EquipmentType.GYM_EQUIPMENT)
        ));
        ex.addAll(List.of(
                // BEGINNER (7)
                new ESeed("Клякания","Крака",3,15,8,ExerciseType.BODYWEIGHT,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                new ESeed("Лицеви опори","Гърди",3,12,6,ExerciseType.BODYWEIGHT,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                new ESeed("Планк","Корем",3,1,5,ExerciseType.BODYWEIGHT,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                new ESeed("Напади","Крака",3,12,8,ExerciseType.BODYWEIGHT,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                new ESeed("Глутеус мост","Седалище",3,15,8,ExerciseType.BODYWEIGHT,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                new ESeed("Повдигане на прасци","Прасци",3,20,4,ExerciseType.BODYWEIGHT,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                new ESeed("Супермен","Гръб и ядро",3,12,6,ExerciseType.BODYWEIGHT,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                // INTERMEDIATE (7)
                new ESeed("Бърпи","Кардио",3,10,8,ExerciseType.BODYWEIGHT,DifficultyLevel.INTERMEDIATE,EquipmentType.NONE),
                new ESeed("Алпинисти","HIIT",3,20,6,ExerciseType.BODYWEIGHT,DifficultyLevel.INTERMEDIATE,EquipmentType.NONE),
                new ESeed("Кофички","Трицепс/Гърди",3,10,8,ExerciseType.BODYWEIGHT,DifficultyLevel.INTERMEDIATE,EquipmentType.GYM_EQUIPMENT), // Може и без оборудване
                new ESeed("Диамантени лицеви опори","Трицепс",3,10,6,ExerciseType.BODYWEIGHT,DifficultyLevel.INTERMEDIATE,EquipmentType.NONE),
                new ESeed("Плиометрични лицеви опори","Експлозивна сила",3,8,7,ExerciseType.BODYWEIGHT,DifficultyLevel.INTERMEDIATE,EquipmentType.NONE),
                new ESeed("Руски туистове","Корем",3,15,6,ExerciseType.BODYWEIGHT,DifficultyLevel.INTERMEDIATE,EquipmentType.NONE),
                new ESeed("Страничен планк","Корем",3,1,5,ExerciseType.BODYWEIGHT,DifficultyLevel.INTERMEDIATE,EquipmentType.NONE),
                // ADVANCED (6)
                new ESeed("Повдигане крака от вис","Корем",3,12,6,ExerciseType.BODYWEIGHT,DifficultyLevel.ADVANCED,EquipmentType.GYM_EQUIPMENT),
                new ESeed("Единични клякания (Pistol Squat)","Крака",3,8,8,ExerciseType.BODYWEIGHT,DifficultyLevel.ADVANCED,EquipmentType.NONE),
                new ESeed("V-ups","Корем",3,12,7,ExerciseType.BODYWEIGHT,DifficultyLevel.ADVANCED,EquipmentType.NONE),
                new ESeed("Набирания","Гръб",3,8,10,ExerciseType.BODYWEIGHT,DifficultyLevel.ADVANCED,EquipmentType.GYM_EQUIPMENT),
                new ESeed("Стойка на ръце (опити)","Рамена",3,1,5,ExerciseType.BODYWEIGHT,DifficultyLevel.ADVANCED,EquipmentType.NONE),
                new ESeed("Избутвания от стена (Handstand Push-ups)","Рамена",3,6,8,ExerciseType.BODYWEIGHT,DifficultyLevel.ADVANCED,EquipmentType.NONE)
        ));

        // WEIGHTS EXERCISES (Total: 20)
        ex.addAll(List.of(
                // BEGINNER (7)
                new ESeed("Бицепс сгъване с дъмбели","Бицепс",3,12,6,ExerciseType.WEIGHTS,DifficultyLevel.BEGINNER,EquipmentType.DUMBBELLS),
                new ESeed("Трицепс разгъване над глава","Трицепс",3,12,6,ExerciseType.WEIGHTS,DifficultyLevel.BEGINNER,EquipmentType.DUMBBELLS),
                new ESeed("Флайс с дъмбели","Гърди",3,12,7,ExerciseType.WEIGHTS,DifficultyLevel.BEGINNER,EquipmentType.DUMBBELLS),
                new ESeed("Повдигане на пръсти с дъмбели","Прасци",4,15,5,ExerciseType.WEIGHTS,DifficultyLevel.BEGINNER,EquipmentType.DUMBBELLS),
                new ESeed("Махове с дъмбели встрани","Рамена",3,12,6,ExerciseType.WEIGHTS,DifficultyLevel.BEGINNER,EquipmentType.DUMBBELLS),
                new ESeed("Дърпане на долен скрипец","Гръб",3,12,8,ExerciseType.WEIGHTS,DifficultyLevel.BEGINNER,EquipmentType.GYM_EQUIPMENT),
                new ESeed("Раменна преса с дъмбели (седеж)","Рамена",3,10,8,ExerciseType.WEIGHTS,DifficultyLevel.BEGINNER,EquipmentType.DUMBBELLS),
                // INTERMEDIATE (7)
                new ESeed("Бенч преса с щанга","Гърди",4,10,10,ExerciseType.WEIGHTS,DifficultyLevel.INTERMEDIATE,EquipmentType.BARBELL),
                new ESeed("Напади с дъмбели","Крака",3,12,8,ExerciseType.WEIGHTS,DifficultyLevel.INTERMEDIATE,EquipmentType.DUMBBELLS),
                new ESeed("Раменна преса с щанга (прав)","Рамена",3,10,9,ExerciseType.WEIGHTS,DifficultyLevel.INTERMEDIATE,EquipmentType.BARBELL),
                new ESeed("Лег преса","Крака",3,12,8,ExerciseType.WEIGHTS,DifficultyLevel.INTERMEDIATE,EquipmentType.GYM_EQUIPMENT),
                new ESeed("Дърпане на горен скрипец","Гръб",3,12,9,ExerciseType.WEIGHTS,DifficultyLevel.INTERMEDIATE,EquipmentType.GYM_EQUIPMENT),
                new ESeed("Сгъване за задно бедро (машина)","Крака",3,12,7,ExerciseType.WEIGHTS,DifficultyLevel.INTERMEDIATE,EquipmentType.GYM_EQUIPMENT),
                new ESeed("Разгъване за предно бедро (машина)","Крака",3,12,7,ExerciseType.WEIGHTS,DifficultyLevel.INTERMEDIATE,EquipmentType.GYM_EQUIPMENT),
                // ADVANCED (6)
                new ESeed("Мъртва тяга","Комплекс",4,8,12,ExerciseType.WEIGHTS,DifficultyLevel.ADVANCED,EquipmentType.BARBELL),
                new ESeed("Клек с щанга","Крака",4,10,10,ExerciseType.WEIGHTS,DifficultyLevel.ADVANCED,EquipmentType.BARBELL),
                new ESeed("Гребане с щанга","Гръб",3,10,10,ExerciseType.WEIGHTS,DifficultyLevel.ADVANCED,EquipmentType.BARBELL),
                new ESeed("Преден клек","Крака",4,8,12,ExerciseType.WEIGHTS,DifficultyLevel.ADVANCED,EquipmentType.BARBELL),
                new ESeed("Военна преса","Рамена",4,8,10,ExerciseType.WEIGHTS,DifficultyLevel.ADVANCED,EquipmentType.BARBELL),
                new ESeed("Обръщане и изтласкване (Clean & Jerk)","Цяло тяло",3,5,15,ExerciseType.WEIGHTS,DifficultyLevel.ADVANCED,EquipmentType.BARBELL)
        ));

        // CARDIO EXERCISES (Total: 10)
        ex.addAll(List.of(
                // BEGINNER (3)
                new ESeed("Бягане пътека (бавно темпо)","Кардио",null,null,30,ExerciseType.CARDIO,DifficultyLevel.BEGINNER,EquipmentType.GYM_EQUIPMENT),
                new ESeed("Елиптикъл","Кардио",null,null,35,ExerciseType.CARDIO,DifficultyLevel.BEGINNER,EquipmentType.GYM_EQUIPMENT),
                new ESeed("Бързо ходене","Кардио",null,null,40,ExerciseType.CARDIO,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                // INTERMEDIATE (4)
                new ESeed("Колоездене стационарно","Кардио",null,null,45,ExerciseType.CARDIO,DifficultyLevel.INTERMEDIATE,EquipmentType.GYM_EQUIPMENT),
                new ESeed("Скачане въже (HIIT)","HIIT",null,null,15,ExerciseType.CARDIO,DifficultyLevel.INTERMEDIATE,EquipmentType.NONE),
                new ESeed("Бокс сянка","Кардио",null,null,20,ExerciseType.CARDIO,DifficultyLevel.INTERMEDIATE,EquipmentType.NONE),
                new ESeed("Гребане (машина)","Кардио",null,null,30,ExerciseType.CARDIO,DifficultyLevel.INTERMEDIATE,EquipmentType.GYM_EQUIPMENT),
                // ADVANCED (3)
                new ESeed("Плуване","Цяло тяло",null,null,40,ExerciseType.CARDIO,DifficultyLevel.ADVANCED,EquipmentType.NONE),
                new ESeed("Спринтове","HIIT",null,null,20,ExerciseType.CARDIO,DifficultyLevel.ADVANCED,EquipmentType.NONE),
                new ESeed("Каране на ски/сноуборд (симулатор)","Кардио",null,null,30,ExerciseType.CARDIO,DifficultyLevel.ADVANCED,EquipmentType.GYM_EQUIPMENT)
        ));

        // OTHER EXERCISES (Total: 10)
        ex.addAll(List.of(
                // BEGINNER (5)
                new ESeed("Йога - Поздрав към слънцето","Гъвкавост и баланс",null,null,20,ExerciseType.OTHER,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                new ESeed("Стречинг - Цяло тяло","Възстановяване",null,null,15,ExerciseType.OTHER,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                new ESeed("Тай Чи","Баланс и концентрация",null,null,25,ExerciseType.OTHER,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                new ESeed("Леки разтягания сутрин","Разтягане",null,null,10,ExerciseType.OTHER,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                new ESeed("Фоум ролър масаж","Възстановяване",null,null,15,ExerciseType.OTHER,DifficultyLevel.BEGINNER,EquipmentType.GYM_EQUIPMENT),
                // INTERMEDIATE (3)
                new ESeed("Пилатес - Стомах","Ядро и контрол",null,null,30,ExerciseType.OTHER,DifficultyLevel.INTERMEDIATE,EquipmentType.NONE),
                new ESeed("Бокс на чувал","Кардио и сила",null,null,20,ExerciseType.OTHER,DifficultyLevel.INTERMEDIATE,EquipmentType.GYM_EQUIPMENT),
                new ESeed("Функционални движения","Мобилност",null,null,25,ExerciseType.OTHER,DifficultyLevel.INTERMEDIATE,EquipmentType.NONE),
                // ADVANCED (2)
                new ESeed("Йога - Инверсии","Баланс и сила",null,null,30,ExerciseType.OTHER,DifficultyLevel.ADVANCED,EquipmentType.NONE),
                new ESeed("TRX - Основни движения","Цяло тяло с окачване",3,10,20,ExerciseType.OTHER,DifficultyLevel.ADVANCED,EquipmentType.GYM_EQUIPMENT)
        ));
        ex.addAll(List.of(
                // BEGINNER (7)
                new ESeed("Клякания","Крака",3,15,8,ExerciseType.BODYWEIGHT,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                new ESeed("Лицеви опори","Гърди",3,12,6,ExerciseType.BODYWEIGHT,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                new ESeed("Планк","Корем",3,1,5,ExerciseType.BODYWEIGHT,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                new ESeed("Напади","Крака",3,12,8,ExerciseType.BODYWEIGHT,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                new ESeed("Глутеус мост","Седалище",3,15,8,ExerciseType.BODYWEIGHT,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                new ESeed("Повдигане на прасци","Прасци",3,20,4,ExerciseType.BODYWEIGHT,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                new ESeed("Супермен","Гръб и ядро",3,12,6,ExerciseType.BODYWEIGHT,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                // INTERMEDIATE (7)
                new ESeed("Бърпи","Кардио",3,10,8,ExerciseType.BODYWEIGHT,DifficultyLevel.INTERMEDIATE,EquipmentType.NONE),
                new ESeed("Алпинисти","HIIT",3,20,6,ExerciseType.BODYWEIGHT,DifficultyLevel.INTERMEDIATE,EquipmentType.NONE),
                new ESeed("Кофички","Трицепс/Гърди",3,10,8,ExerciseType.BODYWEIGHT,DifficultyLevel.INTERMEDIATE,EquipmentType.GYM_EQUIPMENT),
                new ESeed("Диамантени лицеви опори","Трицепс",3,10,6,ExerciseType.BODYWEIGHT,DifficultyLevel.INTERMEDIATE,EquipmentType.NONE),
                new ESeed("Плиометрични лицеви опори","Експлозивна сила",3,8,7,ExerciseType.BODYWEIGHT,DifficultyLevel.INTERMEDIATE,EquipmentType.NONE),
                new ESeed("Руски туистове","Корем",3,15,6,ExerciseType.BODYWEIGHT,DifficultyLevel.INTERMEDIATE,EquipmentType.NONE),
                new ESeed("Страничен планк","Корем",3,1,5,ExerciseType.BODYWEIGHT,DifficultyLevel.INTERMEDIATE,EquipmentType.NONE),
                // ADVANCED (6)
                new ESeed("Повдигане крака от вис","Корем",3,12,6,ExerciseType.BODYWEIGHT,DifficultyLevel.ADVANCED,EquipmentType.GYM_EQUIPMENT),
                new ESeed("Единични клякания (Pistol Squat)","Крака",3,8,8,ExerciseType.BODYWEIGHT,DifficultyLevel.ADVANCED,EquipmentType.NONE),
                new ESeed("V-ups","Корем",3,12,7,ExerciseType.BODYWEIGHT,DifficultyLevel.ADVANCED,EquipmentType.NONE),
                new ESeed("Набирания","Гръб",3,8,10,ExerciseType.BODYWEIGHT,DifficultyLevel.ADVANCED,EquipmentType.GYM_EQUIPMENT),
                new ESeed("Стойка на ръце (опити)","Рамена",3,1,5,ExerciseType.BODYWEIGHT,DifficultyLevel.ADVANCED,EquipmentType.NONE),
                new ESeed("Избутвания от стена (Handstand Push-ups)","Рамена",3,6,8,ExerciseType.BODYWEIGHT,DifficultyLevel.ADVANCED,EquipmentType.NONE)
        ));

        // WEIGHTS EXERCISES (Total: 15 existing + 5 new = 20 unique after deduplication)
        ex.addAll(List.of(
                // BEGINNER (7)
                new ESeed("Бицепс сгъване с дъмбели","Бицепс",3,12,6,ExerciseType.WEIGHTS,DifficultyLevel.BEGINNER,EquipmentType.DUMBBELLS),
                new ESeed("Трицепс разгъване над глава","Трицепс",3,12,6,ExerciseType.WEIGHTS,DifficultyLevel.BEGINNER,EquipmentType.DUMBBELLS),
                new ESeed("Флайс с дъмбели","Гърди",3,12,7,ExerciseType.WEIGHTS,DifficultyLevel.BEGINNER,EquipmentType.DUMBBELLS),
                new ESeed("Повдигане на пръсти с дъмбели","Прасци",4,15,5,ExerciseType.WEIGHTS,DifficultyLevel.BEGINNER,EquipmentType.DUMBBELLS),
                new ESeed("Махове с дъмбели встрани","Рамена",3,12,6,ExerciseType.WEIGHTS,DifficultyLevel.BEGINNER,EquipmentType.DUMBBELLS),
                new ESeed("Дърпане на долен скрипец","Гръб",3,12,8,ExerciseType.WEIGHTS,DifficultyLevel.BEGINNER,EquipmentType.GYM_EQUIPMENT),
                new ESeed("Раменна преса с дъмбели (седеж)","Рамена",3,10,8,ExerciseType.WEIGHTS,DifficultyLevel.BEGINNER,EquipmentType.DUMBBELLS),
                // INTERMEDIATE (7)
                new ESeed("Бенч преса с щанга","Гърди",4,10,10,ExerciseType.WEIGHTS,DifficultyLevel.INTERMEDIATE,EquipmentType.BARBELL),
                new ESeed("Напади с дъмбели","Крака",3,12,8,ExerciseType.WEIGHTS,DifficultyLevel.INTERMEDIATE,EquipmentType.DUMBBELLS),
                new ESeed("Раменна преса с щанга (прав)","Рамена",3,10,9,ExerciseType.WEIGHTS,DifficultyLevel.INTERMEDIATE,EquipmentType.BARBELL),
                new ESeed("Лег преса","Крака",3,12,8,ExerciseType.WEIGHTS,DifficultyLevel.INTERMEDIATE,EquipmentType.GYM_EQUIPMENT),
                new ESeed("Дърпане на горен скрипец","Гръб",3,12,9,ExerciseType.WEIGHTS,DifficultyLevel.INTERMEDIATE,EquipmentType.GYM_EQUIPMENT),
                new ESeed("Сгъване за задно бедро (машина)","Крака",3,12,7,ExerciseType.WEIGHTS,DifficultyLevel.INTERMEDIATE,EquipmentType.GYM_EQUIPMENT),
                new ESeed("Разгъване за предно бедро (машина)","Крака",3,12,7,ExerciseType.WEIGHTS,DifficultyLevel.INTERMEDIATE,EquipmentType.GYM_EQUIPMENT),
                // ADVANCED (6)
                new ESeed("Мъртва тяга","Комплекс",4,8,12,ExerciseType.WEIGHTS,DifficultyLevel.ADVANCED,EquipmentType.BARBELL),
                new ESeed("Клек с щанга","Крака",4,10,10,ExerciseType.WEIGHTS,DifficultyLevel.ADVANCED,EquipmentType.BARBELL),
                new ESeed("Гребане с щанга","Гръб",3,10,10,ExerciseType.WEIGHTS,DifficultyLevel.ADVANCED,EquipmentType.BARBELL),
                new ESeed("Преден клек","Крака",4,8,12,ExerciseType.WEIGHTS,DifficultyLevel.ADVANCED,EquipmentType.BARBELL),
                new ESeed("Военна преса","Рамена",4,8,10,ExerciseType.WEIGHTS,DifficultyLevel.ADVANCED,EquipmentType.BARBELL),
                new ESeed("Обръщане и изтласкване (Clean & Jerk)","Цяло тяло",3,5,15,ExerciseType.WEIGHTS,DifficultyLevel.ADVANCED,EquipmentType.BARBELL)
        ));

        // CARDIO EXERCISES (Total: 5 existing + 5 new = 10 unique after deduplication)
        ex.addAll(List.of(
                // BEGINNER (3)
                new ESeed("Бягане пътека (бавно темпо)","Кардио",null,null,30,ExerciseType.CARDIO,DifficultyLevel.BEGINNER,EquipmentType.GYM_EQUIPMENT),
                new ESeed("Елиптикъл","Кардио",null,null,35,ExerciseType.CARDIO,DifficultyLevel.BEGINNER,EquipmentType.GYM_EQUIPMENT),
                new ESeed("Бързо ходене","Кардио",null,null,40,ExerciseType.CARDIO,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                // INTERMEDIATE (4)
                new ESeed("Колоездене стационарно","Кардио",null,null,45,ExerciseType.CARDIO,DifficultyLevel.INTERMEDIATE,EquipmentType.GYM_EQUIPMENT),
                new ESeed("Скачане въже (HIIT)","HIIT",null,null,15,ExerciseType.CARDIO,DifficultyLevel.INTERMEDIATE,EquipmentType.NONE),
                new ESeed("Бокс сянка","Кардио",null,null,20,ExerciseType.CARDIO,DifficultyLevel.INTERMEDIATE,EquipmentType.NONE),
                new ESeed("Гребане (машина)","Кардио",null,null,30,ExerciseType.CARDIO,DifficultyLevel.INTERMEDIATE,EquipmentType.GYM_EQUIPMENT),
                // ADVANCED (3)
                new ESeed("Плуване","Цяло тяло",null,null,40,ExerciseType.CARDIO,DifficultyLevel.ADVANCED,EquipmentType.NONE),
                new ESeed("Спринтове","HIIT",null,null,20,ExerciseType.CARDIO,DifficultyLevel.ADVANCED,EquipmentType.NONE),
                new ESeed("Каране на ски/сноуборд (симулатор)","Кардио",null,null,30,ExerciseType.CARDIO,DifficultyLevel.ADVANCED,EquipmentType.GYM_EQUIPMENT)
        ));

        // OTHER EXERCISES (Total: 5 existing + 5 new = 10 unique after deduplication)
        ex.addAll(List.of(
                // BEGINNER (5)
                new ESeed("Йога - Поздрав към слънцето","Гъвкавост и баланс",null,null,20,ExerciseType.OTHER,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                new ESeed("Стречинг - Цяло тяло","Възстановяване",null,null,15,ExerciseType.OTHER,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                new ESeed("Тай Чи","Баланс и концентрация",null,null,25,ExerciseType.OTHER,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                new ESeed("Леки разтягания сутрин","Разтягане",null,null,10,ExerciseType.OTHER,DifficultyLevel.BEGINNER,EquipmentType.NONE),
                new ESeed("Фоум ролър масаж","Възстановяване",null,null,15,ExerciseType.OTHER,DifficultyLevel.BEGINNER,EquipmentType.GYM_EQUIPMENT),
                // INTERMEDIATE (3)
                new ESeed("Пилатес - Стомах","Ядро и контрол",null,null,30,ExerciseType.OTHER,DifficultyLevel.INTERMEDIATE,EquipmentType.NONE),
                new ESeed("Бокс на чувал","Кардио и сила",null,null,20,ExerciseType.OTHER,DifficultyLevel.INTERMEDIATE,EquipmentType.GYM_EQUIPMENT),
                new ESeed("Функционални движения","Мобилност",null,null,25,ExerciseType.OTHER,DifficultyLevel.INTERMEDIATE,EquipmentType.NONE),
                // ADVANCED (2)
                new ESeed("Йога - Инверсии","Баланс и сила",null,null,30,ExerciseType.OTHER,DifficultyLevel.ADVANCED,EquipmentType.NONE),
                new ESeed("TRX - Основни движения","Цяло тяло с окачване",3,10,20,ExerciseType.OTHER,DifficultyLevel.ADVANCED,EquipmentType.GYM_EQUIPMENT)
        ));


        ex.forEach(e -> {
            Exercise.ExerciseBuilder b = Exercise.builder()
                    .name(e.n).description(e.d)
                    .type(e.type).difficultyLevel(e.lvl).equipment(e.eq);
            if(e.sets!=null) b.sets(e.sets);
            if(e.reps!=null) b.reps(e.reps);
            if(e.dur!=null)  b.durationMinutes(e.dur);
            exerciseRepository.save(b.build());
        });
        System.out.println("✔ Упражнения добавени: " + ex.size());
    }

    /* ==================================*/
    /*               USERS               */
    /* =================================*/
    private void seedTestUsers() {
        if (userRepository.count() > 0) return;
        Role roleUser  = roleRepository.findByName("ROLE_USER").orElseThrow();
        Role roleAdmin = roleRepository.findByName("ROLE_ADMIN").orElseThrow();

        ActivityLevel active = activityLevelRepository.findByName("Умерено активен").orElseThrow();
        Goal goalLoss  = goalRepository.findByName("Отслабване").orElseThrow();
        Goal goalMaintain = goalRepository.findByName("Поддържане на тегло").orElseThrow();

        createUser("test@example.com","Тест Потребител",GenderType.MALE,30,80.0,180.0,
                active,goalLoss,TrainingType.WEIGHTS,LevelType.INTERMEDIATE,
                4,60,Set.of(roleUser));

        createUser("admin@example.com","Админ",GenderType.FEMALE,35,65.0,170.0,
                active,goalMaintain,TrainingType.CARDIO,LevelType.BEGINNER,
                3,45,Set.of(roleAdmin));

        System.out.println("✔ Тестови потребители създадени");
    }

    private void createUser(String email,String name,GenderType g,int age,double weight,double height,
                            ActivityLevel act,Goal goal,TrainingType tr,LevelType lvl,
                            int days,int dur,Set<Role> roles){
        if(userRepository.findByEmail(email).isPresent()) return;
        User u = User.builder()
                .email(email)
                .password(passwordEncoder.encode("password123"))
                .fullName(name)
                .gender(g)
                .age(age)
                .weight(weight)
                .height(height)
                .activityLevel(act)
                .goal(goal)
                .trainingType(tr)
                .level(lvl)
                .trainingDaysPerWeek(days)
                .trainingDurationMinutes(dur)
                .roles(roles)
                .build();
        userRepository.save(u);
    }

    private void createUserIfMissing(String email, String name, GenderType g, int age, double weight, double height,
                                     ActivityLevel act, Goal goal, TrainingType tr, LevelType lvl,
                                     int days, int dur, Set<Role> roles) {
        if (userRepository.existsByEmail(email)) return;
        User u = User.builder()
                .email(email)
                .password(passwordEncoder.encode("password123"))
                .fullName(name)
                .gender(g)
                .age(age)
                .weight(weight)
                .height(height)
                .activityLevel(act)
                .goal(goal)
                .trainingType(tr)
                .level(lvl)
                .trainingDaysPerWeek(days)
                .trainingDurationMinutes(dur)
                .roles(roles)
                .build();
        userRepository.save(u);
    }
}