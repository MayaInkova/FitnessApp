package com.fitnessapp.config;

import com.fitnessapp.model.*;
import com.fitnessapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

@Component
public class DataInitializer implements CommandLineRunner {

    /* =========  Repositories  ========= */
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

    /* ==========  Runner  ========== */
    @Override
    public void run(String... args) {
        seedRoles();
        seedDietTypes();
        seedActivityLevels();
        seedGoals();
        seedRecipes();
        seedExercises();
        seedTestUsers();
    }

    /* ================================== */
    /*              MASTER DATA           */
    /* ================================== */

    private void seedRoles() {
        if (roleRepository.count() > 0) return;
        Stream.of("ROLE_USER", "ROLE_ADMIN", "ROLE_MODERATOR", "ROLE_GUEST")
                .forEach(r -> roleRepository.save(Role.builder().name(r).build()));
        System.out.println("✔ Ролите създадени");
    }

    private void seedDietTypes() {
        if (dietTypeRepository.count() > 0) return;
        record D(String n,String d){}
        List<D> list = List.of(
                new D("Протеинова","Богата на протеини, ниска на въглехидрати."),
                new D("Кето","Кетогенна с високи мазнини и ниски въглехидрати."),
                new D("Балансирана","Включва всички хранителни групи."),
                new D("Вегетарианска","Растителна, без месо и риба."),
                new D("Веган","Без животински продукти."),
                new D("Палео","Непреработени храни, без зърнени и бобови.")
        );
        list.forEach(d -> dietTypeRepository.save(DietType.builder().name(d.n).description(d.d).build()));
        System.out.println("✔ Типове диети създадени");
    }

    private void seedActivityLevels() {
        if (activityLevelRepository.count() > 0) return;
        record AL(String n,String d,double m){}
        List<AL> al = List.of(
                new AL("Заседнал","Малко или никакво движение",1.2),
                new AL("Леко активен","1‑3 тренировки",1.375),
                new AL("Умерено активен","3‑5 тренировки",1.55),
                new AL("Много активен","6‑7 интензивни тренировки",1.725),
                new AL("Изключително активен","Физическа работа / 2× тренировки",1.9)
        );
        al.forEach(a -> activityLevelRepository.save(
                ActivityLevel.builder().name(a.n).description(a.d).multiplier(a.m).build()));
        System.out.println("✔ Нивата на активност създадени");
    }

    private void seedGoals() {
        if (goalRepository.count() > 0) return;
        goalRepository.save(Goal.builder().name("Отслабване").description("Калориен дефицит").calorieModifier(-500.0).build());
        goalRepository.save(Goal.builder().name("Наддаване на тегло").description("Калориен излишък").calorieModifier(500.0).build());
        goalRepository.save(Goal.builder().name("Поддържане на тегло").description("Без промяна в калориите").calorieModifier(0.0).build());
        System.out.println("✔ Целите създадени");
    }

    /* ==================================*/
    /*              RECIPES              */
    /* ==================================*/
    private record RSeed(String n,String d,double kcal,double p,double c,double f,
                         MealType meal,DietType diet,MeatPreferenceType meat,
                         boolean veg,boolean dairy,boolean nuts,String... tags){}

    private void seedRecipes() {
        if (recipeRepository.count() > 0) return;

        // === Fetch diet references ===
        DietType bal   = dietTypeRepository.findByName("Балансирана").orElseThrow();
        DietType keto  = dietTypeRepository.findByName("Кето").orElseThrow();
        DietType prot  = dietTypeRepository.findByName("Протеинова").orElseThrow();
        DietType veg   = dietTypeRepository.findByName("Вегетарианска").orElseThrow();
        DietType vegan = dietTypeRepository.findByName("Веган").orElseThrow();
        DietType paleo = dietTypeRepository.findByName("Палео").orElseThrow();

        List<RSeed> seeds = new ArrayList<>();

        seeds.addAll(List.of(
                new RSeed("Овесена каша с ябълка", "Овесени ядки, ябълка, канела, мляко. Сварете овеса с млякото и добавете ябълка и канела.", 340, 10, 50, 8, MealType.BREAKFAST, bal, MeatPreferenceType.NONE, true, true, false, "балансирана"),
                new RSeed("Яйца със спанак и пълнозърнест хляб", "Яйца, спанак, пълнозърнест хляб. Запържете яйцата със спанака и сервирайте с хляб.", 360, 18, 20, 20, MealType.BREAKFAST, bal, MeatPreferenceType.NONE, true, true, false, "балансирана"),
                new RSeed("Кисело мляко с овес и мед", "Кисело мляко, овес, мед, орехи. Смесете съставките.", 310, 14, 35, 10, MealType.BREAKFAST, bal, MeatPreferenceType.NONE, true, true, true, "балансирана"),
                new RSeed("Сандвич с яйце и домат", "Пълнозърнест хляб, варено яйце, домат. Нарежете и сервирайте.", 330, 15, 25, 12, MealType.BREAKFAST, bal, MeatPreferenceType.NONE, true, true, false, "балансирана"),
                new RSeed("Палачинки с извара", "Овесено брашно, извара, яйца. Изпечете в тиган.", 350, 18, 30, 14, MealType.BREAKFAST, bal, MeatPreferenceType.NONE, true, true, false, "балансирана"),
                new RSeed("Чиа пудинг с плодове", "Чиа, мляко, сезонни плодове. Накиснете чията и добавете плодове.", 320, 9, 25, 15, MealType.BREAKFAST, bal, MeatPreferenceType.NONE, true, true, false, "балансирана"),
                new RSeed("Мюсли с мляко и банан", "Мюсли, прясно мляко, банан. Смесете и охладете.", 340, 10, 40, 8, MealType.BREAKFAST, bal, MeatPreferenceType.NONE, true, true, false, "балансирана"),

                new RSeed("Салата с киноа и риба тон", "Киноа, риба тон, зеленчуци. Сварете киноата и смесете с рибата.", 420, 28, 40, 14, MealType.LUNCH, bal, MeatPreferenceType.FISH, false, false, false, "балансирана"),
                new RSeed("Пилешко със сладки картофи", "Пилешко филе, сладък картоф. Изпечете заедно.", 480, 38, 35, 16, MealType.LUNCH, bal, MeatPreferenceType.CHICKEN, false, false, false, "балансирана"),
                new RSeed("Пъстърва с ориз и зеленчуци", "Пъстърва, ориз, зеленчуци. Гответе на пара.", 500, 34, 38, 18, MealType.LUNCH, bal, MeatPreferenceType.FISH, false, false, false, "балансирана"),
                new RSeed("Свинско със зеле и моркови", "Свинско месо, зеле, моркови. Задушете до готовност.", 510, 36, 20, 22, MealType.LUNCH, bal, MeatPreferenceType.PORK, false, false, false, "балансирана"),
                new RSeed("Телешко с булгур", "Телешко месо, булгур, домат. Гответе до омекване.", 530, 40, 30, 20, MealType.LUNCH, bal, MeatPreferenceType.BEEF, false, false, false, "балансирана"),
                new RSeed("Омлет със зеленчуци", "Яйца, чушки, лук, гъби. Запържете.", 390, 20, 15, 22, MealType.LUNCH, bal, MeatPreferenceType.NONE, true, true, false, "балансирана"),
                new RSeed("Тофу с кафяв ориз", "Тофу, кафяв ориз, броколи. Запържете леко.", 440, 24, 35, 14, MealType.LUNCH, bal, MeatPreferenceType.NONE, true, false, false, "балансирана"),

                new RSeed("Леща с ориз и салата", "Леща, ориз, салата. Сварете и поднесете със свежа салата.", 430, 22, 40, 10, MealType.DINNER, bal, MeatPreferenceType.NONE, true, false, false, "балансирана"),
                new RSeed("Пилешка супа", "Пилешко, моркови, картоф, подправки. Сварете всичко.", 400, 30, 20, 12, MealType.DINNER, bal, MeatPreferenceType.CHICKEN, false, false, false, "балансирана"),
                new RSeed("Рататуй", "Тиквички, патладжан, домати. Задушете зеленчуците.", 370, 8, 25, 14, MealType.DINNER, bal, MeatPreferenceType.NONE, true, false, false, "балансирана"),
                new RSeed("Тилапия със зелен фасул", "Тилапия, зелен фасул, лимон. Изпечете рибата и сервирайте.", 460, 32, 18, 20, MealType.DINNER, bal, MeatPreferenceType.FISH, false, false, false, "балансирана"),
                new RSeed("Пълнени чушки с кайма и ориз", "Чушки, кайма, ориз, доматен сос. Изпечете във фурна.", 490, 28, 30, 18, MealType.DINNER, bal, MeatPreferenceType.BEEF, false, false, false, "балансирана"),
                new RSeed("Киноа с печени зеленчуци", "Киноа, тиквички, моркови. Сварете киноата и запечете зеленчуците.", 420, 16, 35, 14, MealType.DINNER, bal, MeatPreferenceType.NONE, true, false, false, "балансирана"),
                new RSeed("Яйца с броколи и сирене", "Яйца, броколи, сирене. Запечете всичко заедно.", 410, 20, 15, 24, MealType.DINNER, bal, MeatPreferenceType.NONE, true, true, false, "балансирана"),

                new RSeed("Орехи с кисело мляко", "Кисело мляко, орехи. Смесете и охладете.", 280, 14, 10, 20, MealType.SNACK, bal, MeatPreferenceType.NONE, true, true, true, "балансирана"),
                new RSeed("Ябълка с фъстъчено масло", "Ябълка, фъстъчено масло. Нарежете и намажете.", 270, 6, 25, 14, MealType.SNACK, bal, MeatPreferenceType.NONE, true, false, true, "балансирана"),
                new RSeed("Смути с кисело мляко и плодове", "Кисело мляко, банан, горски плодове. Пасирайте всичко.", 290, 10, 28, 10, MealType.SNACK, bal, MeatPreferenceType.NONE, true, true, false, "балансирана"),
                new RSeed("Домашни барове с мюсли", "Мюсли, мед, ядки. Смесете и запечете.", 300, 8, 30, 12, MealType.SNACK, bal, MeatPreferenceType.NONE, true, true, true, "балансирана"),
                new RSeed("Фреш от морков и ябълка", "Морков, ябълка, лимон. Изцедете и охладете.", 150, 2, 28, 1, MealType.SNACK, bal, MeatPreferenceType.NONE, true, false, false, "балансирана"),
                new RSeed("Райска ябълка с кисело мляко", "Кисело мляко, райска ябълка, мед. Смесете и охладете.", 260, 9, 20, 8, MealType.SNACK, bal, MeatPreferenceType.NONE, true, true, false, "балансирана"),
                new RSeed("Грис с мляко", "Грис, прясно мляко, канела. Сварете и сервирайте.", 310, 7, 35, 9, MealType.SNACK, bal, MeatPreferenceType.NONE, true, true, false, "балансирана")
        ));

        seeds.addAll(List.of(
                // --- BREAKFAST ---
                new RSeed("Авокадо с яйце", "Необходими продукти: авокадо, яйца.\nПриготвяне: разрежете авокадото, запечете яйце в средата.", 380, 18, 5, 30, MealType.BREAKFAST, keto, MeatPreferenceType.NONE, true, true, false, "кето", "закуска"),
                new RSeed("Кето палачинки с бадемово брашно", "Необходими продукти: яйца, бадемово брашно, сметана.\nПриготвяне: смесете съставките и изпържете палачинки.", 320, 16, 4, 26, MealType.BREAKFAST, keto, MeatPreferenceType.NONE, true, true, true, "кето", "закуска"),
                new RSeed("Чиа пудинг с кокосово мляко", "Необходими продукти: чиа, кокосово мляко, стевия.\nПриготвяне: оставете чиа семената да набъбнат в млякото за 4 часа.", 290, 10, 8, 24, MealType.BREAKFAST, keto, MeatPreferenceType.NONE, true, false, false, "кето", "закуска"),
                new RSeed("Омлет с шунка и сирене", "Необходими продукти: яйца, шунка, кашкавал.\nПриготвяне: запържете шунката и яйцата, добавете кашкавала.", 400, 22, 3, 34, MealType.BREAKFAST, keto, MeatPreferenceType.PORK, false, true, false, "кето", "закуска"),
                new RSeed("Яйца с бекон и спанак", "Необходими продукти: яйца, бекон, спанак.\nПриготвяне: изпържете бекона и яйцата със спанак.", 410, 28, 4, 35, MealType.BREAKFAST, keto, MeatPreferenceType.PORK, false, true, false, "кето", "закуска"),
                new RSeed("Кокосови палачинки", "Необходими продукти: кокосово брашно, яйца, ванилия.\nПриготвяне: разбийте и изпържете.", 340, 14, 6, 28, MealType.BREAKFAST, keto, MeatPreferenceType.NONE, true, true, false, "кето", "закуска"),
                new RSeed("Скумрия със зеленчуци", "Необходими продукти: скумрия, тиквички, яйца.\nПриготвяне: задушете всичко заедно.", 430, 26, 4, 30, MealType.BREAKFAST, keto, MeatPreferenceType.FISH, false, false, false, "кето", "закуска"),

                // --- LUNCH ---
                new RSeed("Пилешки бутчета с карфиол", "Необходими продукти: пилешки бутчета, карфиол, масло.\nПриготвяне: изпечете до златисто в тава.", 510, 36, 6, 38, MealType.LUNCH, keto, MeatPreferenceType.CHICKEN, false, true, false, "кето", "обяд"),
                new RSeed("Кето бургер без хляб", "Необходими продукти: телешка кюфте, салата, домат, сирене.\nПриготвяне: сглобете бургер между листа салата.", 580, 40, 4, 45, MealType.LUNCH, keto, MeatPreferenceType.BEEF, false, true, false, "кето", "обяд"),
                new RSeed("Пълнени гъби с кайма", "Необходими продукти: големи гъби, телешка кайма, кашкавал.\nПриготвяне: запълнете гъбите и запечете.", 490, 35, 5, 39, MealType.LUNCH, keto, MeatPreferenceType.BEEF, false, true, false, "кето", "обяд"),
                new RSeed("Салата с авокадо и яйце", "Необходими продукти: авокадо, варено яйце, зехтин.\nПриготвяне: нарежете и овкусете.", 440, 20, 4, 38, MealType.LUNCH, keto, MeatPreferenceType.NONE, true, true, false, "кето", "обяд"),
                new RSeed("Свинско със зелен фасул", "Необходими продукти: свинско, зелен фасул, чесън.\nПриготвяне: запържете леко.", 520, 38, 8, 36, MealType.LUNCH, keto, MeatPreferenceType.PORK, false, true, false, "кето", "обяд"),
                new RSeed("Риба тон с авокадо", "Необходими продукти: риба тон, авокадо, зелена салата.\nПриготвяне: смесете всичко.", 460, 34, 6, 32, MealType.LUNCH, keto, MeatPreferenceType.FISH, false, true, false, "кето", "обяд"),
                new RSeed("Патладжан с кайма", "Необходими продукти: патладжан, телешка кайма, домати.\nПриготвяне: запечете във фурна.", 500, 36, 10, 33, MealType.LUNCH, keto, MeatPreferenceType.BEEF, false, true, false, "кето", "обяд"),

                // --- DINNER ---
                new RSeed("Свинско със зеле", "Необходими продукти: свинско месо, зеле, подправки.\nПриготвяне: задушете месото със зелето.", 600, 45, 10, 40, MealType.DINNER, keto, MeatPreferenceType.PORK, false, false, false, "кето", "вечеря"),
                new RSeed("Скумрия на скара с броколи", "Необходими продукти: скумрия, броколи, зехтин.\nПриготвяне: изпечете рибата и сервирайте със задушени броколи.", 520, 38, 4, 36, MealType.DINNER, keto, MeatPreferenceType.FISH, false, false, false, "кето", "вечеря"),
                new RSeed("Мусака с карфиол", "Необходими продукти: карфиол, кайма, яйца, сметана.\nПриготвяне: подредете и запечете като мусака.", 540, 33, 7, 42, MealType.DINNER, keto, MeatPreferenceType.BEEF, false, true, false, "кето", "вечеря"),
                new RSeed("Яйца по бенедиктински с шунка", "Необходими продукти: яйца, шунка, холандез сос.\nПриготвяне: приготвят се яйца по пош и се заливат със сос.", 470, 28, 6, 37, MealType.DINNER, keto, MeatPreferenceType.PORK, false, true, false, "кето", "вечеря"),
                new RSeed("Задушена риба с тиквички", "Необходими продукти: бяла риба, тиквички, масло.\nПриготвяне: задушете леко.", 510, 34, 6, 35, MealType.DINNER, keto, MeatPreferenceType.FISH, false, false, false, "кето", "вечеря"),
                new RSeed("Кюфтета с гъбен сос", "Необходими продукти: кайма, гъби, сметана.\nПриготвяне: запържете и добавете сос.", 560, 39, 6, 41, MealType.DINNER, keto, MeatPreferenceType.BEEF, false, true, false, "кето", "вечеря"),
                new RSeed("Печена сьомга със спанак", "Необходими продукти: филе от сьомга, спанак, лимон.\nПриготвяне: изпечете рибата и поднесете със задушен спанак.", 480, 36, 4, 38, MealType.DINNER, keto, MeatPreferenceType.FISH, false, false, false, "кето", "вечеря"),

                // --- SNACK ---
                new RSeed("Маслини и кашкавал", "Необходими продукти: зелени маслини, твърд кашкавал.\nПриготвяне: поднесете нарязани.", 260, 14, 2, 22, MealType.SNACK, keto, MeatPreferenceType.NONE, true, true, false, "кето", "снак"),
                new RSeed("Селъри с фъстъчено масло", "Необходими продукти: стъбла селъри, фъстъчено масло.\nПриготвяне: напълнете селърито с фъстъчено масло.", 220, 8, 5, 18, MealType.SNACK, keto, MeatPreferenceType.NONE, true, false, true, "кето", "снак"),
                new RSeed("Сушени меса и сирена", "Необходими продукти: прошуто, салам, сирена.\nПриготвяне: подредете като плато.", 330, 20, 1, 28, MealType.SNACK, keto, MeatPreferenceType.NO_FISH, false, true, false, "кето", "снак"),
                new RSeed("Кето ядки микс", "Необходими продукти: бадеми, орехи, лешници.\nПриготвяне: смесете ядките в купа.", 290, 10, 6, 26, MealType.SNACK, keto, MeatPreferenceType.NONE, true, false, true, "кето", "снак"),
                new RSeed("Кашкавал с орехи", "Необходими продукти: кашкавал, орехи.\nПриготвяне: нарежете и комбинирайте.", 270, 12, 3, 23, MealType.SNACK, keto, MeatPreferenceType.NONE, true, true, true, "кето", "снак"),
                new RSeed("Салата с риба тон", "Необходими продукти: риба тон, маслини, зелена салата.\nПриготвяне: смесете съставките.", 310, 20, 4, 20, MealType.SNACK, keto, MeatPreferenceType.FISH, false, true, false, "кето", "снак"),
                new RSeed("Кокосов крем с чия", "Необходими продукти: кокосово мляко, чия, ванилия.\nПриготвяне: оставете да стегне.", 280, 9, 6, 22, MealType.SNACK, keto, MeatPreferenceType.NONE, true, false, false, "кето", "снак")
        ));

        seeds.addAll(List.of(
                new RSeed("Омлет с извара", "Необходими продукти: яйца, извара, чери домати.\nПриготвяне: запържете омлета, поднесете с домати.", 360, 30, 8, 20, MealType.BREAKFAST, prot, MeatPreferenceType.NONE, true, true, false, "протеинова", "закуска"),
                new RSeed("Кисело мляко с орехи", "Необходими продукти: кисело мляко, орехи, мед.\nПриготвяне: смесете съставките.", 320, 18, 10, 15, MealType.BREAKFAST, prot, MeatPreferenceType.NONE, true, true, true, "протеинова", "закуска"),
                new RSeed("Яйца по бенедиктински", "Необходими продукти: яйца, шунка, пълнозърнест хляб.\nПриготвяне: запечете и сервирайте с холандез сос.", 410, 22, 20, 24, MealType.BREAKFAST, prot, MeatPreferenceType.PORK, false, true, false, "протеинова", "закуска"),
                new RSeed("Протеинови палачинки", "Необходими продукти: протеин, яйца, банан.\nПриготвяне: направете палачинки от сместа.", 370, 28, 18, 14, MealType.BREAKFAST, prot, MeatPreferenceType.NONE, true, true, false, "протеинова", "закуска"),

                new RSeed("Пуешко с киноа", "Необходими продукти: пуешко филе, киноа, броколи.\nПриготвяне: изпечете пуешкото и сервирайте с киноа и броколи.", 520, 42, 35, 18, MealType.LUNCH, prot, MeatPreferenceType.CHICKEN, false, false, false, "протеинова", "обяд"),
                new RSeed("Телешки стек със зелен фасул", "Необходими продукти: телешки стек, зелен фасул.\nПриготвяне: изпечете стека и задушете фасула.", 540, 45, 10, 28, MealType.LUNCH, prot, MeatPreferenceType.BEEF, false, false, false, "протеинова", "обяд"),
                new RSeed("Риба тон със зеленчуци", "Необходими продукти: риба тон, чушки, лук, домати.\nПриготвяне: запечете и поднесете.", 490, 38, 14, 22, MealType.LUNCH, prot, MeatPreferenceType.FISH, false, false, false, "протеинова", "обяд"),
                new RSeed("Пилешка салата с яйца", "Необходими продукти: пилешко филе, яйца, зелена салата.\nПриготвяне: нарежете и комбинирайте.", 480, 40, 8, 20, MealType.LUNCH, prot, MeatPreferenceType.CHICKEN, false, true, false, "протеинова", "обяд"),

                new RSeed("Сьомга на скара", "Необходими продукти: филе от сьомга, подправки, лимон.\nПриготвяне: изпечете на скара с подправки и лимонов сок.", 460, 38, 5, 28, MealType.DINNER, prot, MeatPreferenceType.FISH, false, false, false, "протеинова", "вечеря"),
                new RSeed("Пилешко с гъби", "Необходими продукти: пилешко, гъби, сметана.\nПриготвяне: задушете до готовност.", 490, 36, 6, 24, MealType.DINNER, prot, MeatPreferenceType.CHICKEN, false, true, false, "протеинова", "вечеря"),
                new RSeed("Пълнени тиквички с месо", "Необходими продукти: тиквички, кайма, подправки.\nПриготвяне: напълнете и запечете.", 520, 42, 12, 26, MealType.DINNER, prot, MeatPreferenceType.NO_PREFERENCE, false, false, false, "протеинова", "вечеря"),
                new RSeed("Печена пъстърва", "Необходими продукти: пъстърва, подправки, лимон.\nПриготвяне: изпечете във фурна.", 470, 35, 4, 22, MealType.DINNER, prot, MeatPreferenceType.FISH, false, false, false, "протеинова", "вечеря"),

                new RSeed("Протеинов бар с фурми", "Необходими продукти: фурми, фъстъчено масло, протеин.\nПриготвяне: смесете и оформете барове.", 300, 24, 22, 12, MealType.SNACK, prot, MeatPreferenceType.NONE, true, false, true, "протеинова", "снак"),
                new RSeed("Кисело мляко с протеин", "Необходими продукти: кисело мляко, протеин на прах.\nПриготвяне: разбъркайте добре.", 280, 22, 10, 10, MealType.SNACK, prot, MeatPreferenceType.NONE, true, true, false, "протеинова", "снак"),
                new RSeed("Сурови ядки и сушени плодове", "Необходими продукти: орехи, бадеми, сушени кайсии.\nПриготвяне: смесете.", 320, 12, 20, 20, MealType.SNACK, prot, MeatPreferenceType.NONE, true, false, true, "протеинова", "снак"),
                new RSeed("Извара с плодове", "Необходими продукти: извара, ябълка, канела.\nПриготвяне: нарежете и разбъркайте.", 290, 20, 15, 12, MealType.SNACK, prot, MeatPreferenceType.NONE, true, true, false, "протеинова", "снак")
        ));

        seeds.addAll(List.of(
                new RSeed("Смути от боровинки и овес", "Бадемово мляко, овес, боровинки. Пасирайте съставките.", 310, 9, 38, 10, MealType.BREAKFAST, vegan, MeatPreferenceType.NONE, true, false, false),
                new RSeed("Тост с авокадо и чери домати", "Пълнозърнест хляб, авокадо, чери домати. Намачкайте авокадото, нарежете доматите, сервирайте.", 350, 7, 30, 22, MealType.BREAKFAST, vegan, MeatPreferenceType.NONE, true, false, false),
                new RSeed("Киноа с ядки и банан", "Сварена киноа, бадеми, банан. Смесете и гарнирайте.", 400, 10, 40, 18, MealType.BREAKFAST, vegan, MeatPreferenceType.NONE, true, false, true),
                new RSeed("Чиа пудинг с манго", "Чиа, бадемово мляко, манго. Накиснете чията, добавете манго.", 320, 8, 28, 16, MealType.BREAKFAST, vegan, MeatPreferenceType.NONE, true, false, false),
                new RSeed("Овес с какао и круша", "Овес, какао, круша, бадемово мляко. Сварете и разбъркайте.", 330, 7, 35, 12, MealType.BREAKFAST, vegan, MeatPreferenceType.NONE, true, false, false),
                new RSeed("Смути с спанак и ябълка", "Спанак, ябълка, ленено семе, вода. Пасирайте всичко.", 280, 6, 25, 10, MealType.BREAKFAST, vegan, MeatPreferenceType.NONE, true, false, false),
                new RSeed("Палачинки от овес и банан", "Овесено брашно, банан, вода. Изпечете в тиган.", 360, 9, 40, 14, MealType.BREAKFAST, vegan, MeatPreferenceType.NONE, true, false, false),

                new RSeed("Ориз със зеленчуци", "Кафяв ориз, чушки, моркови, грах. Сварете ориза, добавете зеленчуци.", 420, 10, 60, 8, MealType.LUNCH, vegan, MeatPreferenceType.NONE, true, false, false),
                new RSeed("Леща яхния", "Леща, лук, морков, домат. Сварете до готовност.", 450, 18, 40, 12, MealType.LUNCH, vegan, MeatPreferenceType.NONE, true, false, false),
                new RSeed("Салата с киноа", "Киноа, домати, краставица, зехтин. Смесете всичко.", 400, 12, 35, 14, MealType.LUNCH, vegan, MeatPreferenceType.NONE, true, false, false),
                new RSeed("Бургер с нахут", "Питка, нахут кюфте, зеленчуци. Запечете и сглобете.", 480, 20, 45, 16, MealType.LUNCH, vegan, MeatPreferenceType.NONE, true, false, false),
                new RSeed("Печен батат с тахан", "Сладък картоф, тахан, семена. Изпечете и полейте.", 430, 9, 50, 15, MealType.LUNCH, vegan, MeatPreferenceType.NONE, true, false, true),
                new RSeed("Тофу с зеленчуци", "Тофу, броколи, моркови, соев сос. Запържете леко.", 460, 22, 30, 18, MealType.LUNCH, vegan, MeatPreferenceType.NONE, true, false, false),
                new RSeed("Киноа с авокадо и боб", "Киноа, черен боб, авокадо, чушка. Смесете и овкусете.", 440, 16, 40, 20, MealType.LUNCH, vegan, MeatPreferenceType.NONE, true, false, false),

                new RSeed("Къри с нахут", "Нахут, кокосово мляко, къри, домати. Гответе заедно.", 450, 14, 45, 18, MealType.DINNER, vegan, MeatPreferenceType.NONE, true, false, false),
                new RSeed("Тофу със спанак", "Тофу, спанак, чесън, зехтин. Запържете леко.", 400, 20, 20, 18, MealType.DINNER, vegan, MeatPreferenceType.NONE, true, false, false),
                new RSeed("Фалафел с таханов сос", "Нахут, подправки, тахан. Изпечете и сервирайте със сос.", 480, 18, 35, 22, MealType.DINNER, vegan, MeatPreferenceType.NONE, true, false, true),
                new RSeed("Печен зеленчуков микс", "Тиквички, патладжан, моркови. Изпечете във фурна.", 390, 6, 30, 14, MealType.DINNER, vegan, MeatPreferenceType.NONE, true, false, false),
                new RSeed("Гъби с ориз", "Гъби, кафяв ориз, лук, подправки. Сварете и задушете.", 420, 10, 50, 10, MealType.DINNER, vegan, MeatPreferenceType.NONE, true, false, false),
                new RSeed("Авокадо с киноа и царевица", "Авокадо, царевица, киноа, чушка. Смесете и овкусете.", 430, 12, 40, 18, MealType.DINNER, vegan, MeatPreferenceType.NONE, true, false, false),
                new RSeed("Тиквено ризото", "Ориз, тиква, зеленчуков бульон. Гответе до кремообразност.", 410, 8, 50, 12, MealType.DINNER, vegan, MeatPreferenceType.NONE, true, false, false),

                new RSeed("Ядки с плодове", "Смесени ядки, нарязани плодове. Смесете и сервирайте.", 300, 8, 22, 20, MealType.SNACK, vegan, MeatPreferenceType.NONE, true, false, true),
                new RSeed("Хумус с моркови", "Хумус, нарязани моркови. Потопете и хапвайте.", 280, 7, 20, 14, MealType.SNACK, vegan, MeatPreferenceType.NONE, true, false, true),
                new RSeed("Фурми с тахан", "Фурми, тахан. Напълнете фурмите с тахан.", 260, 4, 35, 10, MealType.SNACK, vegan, MeatPreferenceType.NONE, true, false, true),
                new RSeed("Оризовки с фъстъчено масло", "Оризовки, фъстъчено масло. Намажете и хапвайте.", 310, 6, 30, 16, MealType.SNACK, vegan, MeatPreferenceType.NONE, true, false, true),
                new RSeed("Банан с бадемово масло", "Банан, бадемово масло. Нарежете и намажете.", 290, 5, 28, 14, MealType.SNACK, vegan, MeatPreferenceType.NONE, true, false, true),
                new RSeed("Смути с ягоди и чия", "Ягоди, чия, вода. Пасирайте всичко.", 270, 6, 25, 10, MealType.SNACK, vegan, MeatPreferenceType.NONE, true, false, false),
                new RSeed("Гранола бар домашен", "Овес, ядки, сушени плодове. Смесете и запечете.", 320, 8, 30, 14, MealType.SNACK, vegan, MeatPreferenceType.NONE, true, false, true)
        ));

        seeds.addAll(List.of(
                // BREAKFAST
                new RSeed("Палео омлет със спанак", "Яйца, спанак, кокосово масло. Запържете всичко заедно.", 340, 20, 6, 28, MealType.BREAKFAST, paleo, MeatPreferenceType.NONE, true, false, false, "палео", "закуска"),
                new RSeed("Палачинки от банан и яйца", "Банан, яйца, кокосово масло. Изпечете палачинки.", 370, 14, 24, 24, MealType.BREAKFAST, paleo, MeatPreferenceType.NONE, true, false, false, "палео", "закуска"),
                new RSeed("Салата с авокадо и бекон", "Авокадо, бекон, рукола, зехтин. Смесете съставките.", 410, 16, 8, 36, MealType.BREAKFAST, paleo, MeatPreferenceType.PORK, false, false, false, "палео", "закуска"),
                new RSeed("Смути с бадемово мляко и ягоди", "Бадемово мляко, ягоди, чия. Пасирайте всичко.", 300, 10, 15, 20, MealType.BREAKFAST, paleo, MeatPreferenceType.NONE, true, false, false, "палео", "закуска"),
                new RSeed("Яйца с авокадо и домати", "Яйца, авокадо, домати. Сварете яйцата, нарежете и поднесете.", 360, 18, 10, 28, MealType.BREAKFAST, paleo, MeatPreferenceType.NONE, true, false, false, "палео", "закуска"),
                new RSeed("Печени зеленчуци с яйца", "Тиквички, патладжан, яйца. Изпечете заедно.", 330, 12, 8, 22, MealType.BREAKFAST, paleo, MeatPreferenceType.NONE, true, false, false, "палео", "закуска"),
                new RSeed("Скариди с яйце и рукола", "Скариди, яйце, рукола. Изпечете и поднесете.", 380, 22, 4, 30, MealType.BREAKFAST, paleo, MeatPreferenceType.FISH, false, false, false, "палео", "закуска"),

                // LUNCH
                new RSeed("Пилешки гърди с тиквички", "Пилешко филе, тиквички, подправки. Изпечете във фурна.", 480, 38, 10, 24, MealType.LUNCH, paleo, MeatPreferenceType.CHICKEN, false, false, false, "палео", "обяд"),
                new RSeed("Говеждо с броколи", "Телешко месо, броколи, зехтин. Задушете заедно.", 520, 40, 8, 28, MealType.LUNCH, paleo, MeatPreferenceType.BEEF, false, false, false, "палео", "обяд"),
                new RSeed("Сьомга с авокадо", "Сьомга, авокадо, лимон. Изпечете и сервирайте.", 510, 36, 5, 34, MealType.LUNCH, paleo, MeatPreferenceType.FISH, false, false, false, "палео", "обяд"),
                new RSeed("Тиквички с яйца и месо", "Тиквички, яйца, кайма. Изпечете всичко заедно.", 500, 34, 10, 30, MealType.LUNCH, paleo, MeatPreferenceType.NO_PREFERENCE, false, false, false, "палео", "обяд"),
                new RSeed("Кюфтета с доматен сос", "Кайма, домати, подправки. Запечете с доматен сос.", 490, 32, 6, 26, MealType.LUNCH, paleo, MeatPreferenceType.BEEF, false, false, false, "палео", "обяд"),
                new RSeed("Салата с пиле и маслини", "Пиле, маслини, домати, краставици. Смесете всичко.", 460, 30, 12, 22, MealType.LUNCH, paleo, MeatPreferenceType.CHICKEN, false, false, false, "палео", "обяд"),
                new RSeed("Задушен заек с зеленчуци", "Заешко месо, моркови, лук. Задушете до готовност.", 500, 36, 9, 26, MealType.LUNCH, paleo, MeatPreferenceType.NO_PREFERENCE, false, false, false, "палео", "обяд"),

                // DINNER
                new RSeed("Риба на скара със зеленчуци", "Риба, тиквички, домати. Изпечете на скара.", 460, 34, 8, 28, MealType.DINNER, paleo, MeatPreferenceType.FISH, false, false, false, "палео", "вечеря"),
                new RSeed("Печени пилешки бутчета", "Пилешки бутчета, подправки. Изпечете до златисто.", 480, 36, 6, 30, MealType.DINNER, paleo, MeatPreferenceType.CHICKEN, false, false, false, "палео", "вечеря"),
                new RSeed("Пълнени чушки с месо", "Чушки, кайма, подправки. Напълнете и запечете.", 470, 32, 10, 24, MealType.DINNER, paleo, MeatPreferenceType.NO_PREFERENCE, false, false, false, "палео", "вечеря"),
                new RSeed("Яйца с гъби и лук", "Яйца, гъби, лук. Запържете всичко заедно.", 390, 20, 6, 22, MealType.DINNER, paleo, MeatPreferenceType.NONE, true, false, false, "палео", "вечеря"),
                new RSeed("Телешко със спанак", "Телешко месо, спанак. Задушете заедно.", 500, 40, 5, 28, MealType.DINNER, paleo, MeatPreferenceType.BEEF, false, false, false, "палео", "вечеря"),
                new RSeed("Печена скумрия с лимон", "Скумрия, лимон, подправки. Изпечете рибата.", 480, 34, 4, 32, MealType.DINNER, paleo, MeatPreferenceType.FISH, false, false, false, "палео", "вечеря"),
                new RSeed("Зеленчукова яхния с пиле", "Пилешко, моркови, чушки. Задушете в тенджера.", 460, 30, 10, 22, MealType.DINNER, paleo, MeatPreferenceType.CHICKEN, false, false, false, "палео", "вечеря"),

                // SNACK
                new RSeed("Ядки микс", "Бадеми, орехи, лешници. Смесете.", 280, 10, 8, 22, MealType.SNACK, paleo, MeatPreferenceType.NONE, true, false, true, "палео", "снак"),
                new RSeed("Ябълка с бадемово масло", "Ябълка, бадемово масло. Нарежете и намажете.", 260, 6, 22, 16, MealType.SNACK, paleo, MeatPreferenceType.NONE, true, false, true, "палео", "снак"),
                new RSeed("Сушено месо", "Сушено телешко месо. Поднесете като снак.", 300, 25, 2, 18, MealType.SNACK, paleo, MeatPreferenceType.BEEF, false, false, false, "палео", "снак"),
                new RSeed("Авокадо с лимон", "Авокадо, лимон. Нарежете и полейте със сок.", 270, 4, 6, 24, MealType.SNACK, paleo, MeatPreferenceType.NONE, true, false, false, "палео", "снак"),
                new RSeed("Яйце със спанак", "Сварено яйце, пресен спанак. Поднесете охладено.", 240, 12, 4, 16, MealType.SNACK, paleo, MeatPreferenceType.NONE, true, false, false, "палео", "снак"),
                new RSeed("Смути с кокосово мляко", "Кокосово мляко, горски плодове. Пасирайте.", 290, 8, 14, 20, MealType.SNACK, paleo, MeatPreferenceType.NONE, true, false, false, "палео", "снак"),
                new RSeed("Моркови с гуакамоле", "Моркови, гуакамоле. Потопете и хапвайте.", 250, 4, 10, 18, MealType.SNACK, paleo, MeatPreferenceType.NONE, true, false, false, "палео", "снак")
        ));
        seeds.addAll(List.of(
                // === VEGETARIAN (7-Day Plan) ===
                // --- BREAKFAST ---
                new RSeed("Овесена каша с плодове", "Овес, мляко, сезонни плодове. Сварете и поднесете.", 320, 10, 40, 8, MealType.BREAKFAST, veg, MeatPreferenceType.NONE, true, true, false, "вегетарианска", "закуска"),
                new RSeed("Палачинки с извара", "Извара, яйца, овесено брашно. Смесете и изпечете.", 340, 18, 30, 14, MealType.BREAKFAST, veg, MeatPreferenceType.NONE, true, true, false, "вегетарианска", "закуска"),
                new RSeed("Кисело мляко с мед и орехи", "Кисело мляко, мед, орехи. Смесете всичко.", 310, 14, 25, 12, MealType.BREAKFAST, veg, MeatPreferenceType.NONE, true, true, true, "вегетарианска", "закуска"),
                new RSeed("Тост с авокадо и яйце", "Пълнозърнест хляб, авокадо, варено яйце. Нарежете и подредете.", 350, 12, 28, 16, MealType.BREAKFAST, veg, MeatPreferenceType.NONE, true, true, false, "вегетарианска", "закуска"),
                new RSeed("Смути с банан и спанак", "Банан, спанак, мляко. Пасирайте съставките.", 300, 8, 30, 10, MealType.BREAKFAST, veg, MeatPreferenceType.NONE, true, true, false, "вегетарианска", "закуска"),
                new RSeed("Мюсли с мляко и ябълка", "Мюсли, мляко, нарязана ябълка. Смесете.", 330, 10, 38, 9, MealType.BREAKFAST, veg, MeatPreferenceType.NONE, true, true, false, "вегетарианска", "закуска"),
                new RSeed("Извара с ягоди", "Извара, ягоди, ванилия. Смесете леко.", 290, 16, 18, 10, MealType.BREAKFAST, veg, MeatPreferenceType.NONE, true, true, false, "вегетарианска", "закуска"),

                // --- LUNCH ---
                new RSeed("Зеленчукова лазаня", "Тиквички, патладжан, домати, сирене. Подредете и изпечете.", 450, 20, 30, 20, MealType.LUNCH, veg, MeatPreferenceType.NONE, true, true, false, "вегетарианска", "обяд"),
                new RSeed("Салата с яйце и сирене", "Зелена салата, варено яйце, сирене. Смесете всичко.", 400, 18, 15, 28, MealType.LUNCH, veg, MeatPreferenceType.NONE, true, true, false, "вегетарианска", "обяд"),
                new RSeed("Ризото с гъби", "Ориз, гъби, масло. Задушете и разбъркайте.", 470, 14, 40, 18, MealType.LUNCH, veg, MeatPreferenceType.NONE, true, true, false, "вегетарианска", "обяд"),
                new RSeed("Омлет със сирене и зеленчуци", "Яйца, сирене, чушки, лук. Запържете леко.", 430, 20, 12, 25, MealType.LUNCH, veg, MeatPreferenceType.NONE, true, true, false, "вегетарианска", "обяд"),
                new RSeed("Пълнени чушки с ориз и сирене", "Чушки, ориз, сирене. Напълнете и изпечете.", 480, 15, 38, 20, MealType.LUNCH, veg, MeatPreferenceType.NONE, true, true, false, "вегетарианска", "обяд"),
                new RSeed("Картофена яхния", "Картофи, лук, морков, подправки. Задушете до готовност.", 440, 10, 35, 16, MealType.LUNCH, veg, MeatPreferenceType.NONE, true, true, false, "вегетарианска", "обяд"),
                new RSeed("Киноа с печени зеленчуци", "Киноа, моркови, тиквички, подправки. Изпечете и поднесете.", 420, 12, 30, 18, MealType.LUNCH, veg, MeatPreferenceType.NONE, true, true, false, "вегетарианска", "обяд"),

                // --- DINNER ---
                new RSeed("Печени зеленчуци с яйца", "Тиквички, патладжан, яйца. Изпечете и добавете яйцата.", 410, 18, 20, 22, MealType.DINNER, veg, MeatPreferenceType.NONE, true, true, false, "вегетарианска", "вечеря"),
                new RSeed("Крем супа от броколи", "Броколи, картоф, сметана. Сварете и пасирайте.", 390, 10, 28, 15, MealType.DINNER, veg, MeatPreferenceType.NONE, true, true, false, "вегетарианска", "вечеря"),
                new RSeed("Тофу с тиквички", "Тофу, тиквички, чесън. Запържете леко.", 420, 22, 12, 18, MealType.DINNER, veg, MeatPreferenceType.NONE, true, false, false, "вегетарианска", "вечеря"),
                new RSeed("Печени картофи със сирене", "Картофи, сирене, подправки. Изпечете и поднесете.", 430, 14, 36, 18, MealType.DINNER, veg, MeatPreferenceType.NONE, true, true, false, "вегетарианска", "вечеря"),
                new RSeed("Фритата със зеленчуци", "Яйца, чушки, броколи. Изпечете във фурна.", 400, 20, 18, 22, MealType.DINNER, veg, MeatPreferenceType.NONE, true, true, false, "вегетарианска", "вечеря"),
                new RSeed("Гъби със спанак и сметана", "Гъби, спанак, сметана. Задушете всичко.", 450, 18, 14, 24, MealType.DINNER, veg, MeatPreferenceType.NONE, true, true, false, "вегетарианска", "вечеря"),
                new RSeed("Ориз със зеленчуци", "Кафяв ориз, грах, моркови. Сварете и поднесете.", 440, 12, 42, 10, MealType.DINNER, veg, MeatPreferenceType.NONE, true, true, false, "вегетарианска", "вечеря"),

                // --- SNACK ---
                new RSeed("Орехи с кисело мляко", "Кисело мляко, орехи. Смесете и охладете.", 280, 14, 10, 20, MealType.SNACK, veg, MeatPreferenceType.NONE, true, true, true, "вегетарианска", "снак"),
                new RSeed("Ябълка с фъстъчено масло", "Ябълка, фъстъчено масло. Нарежете и намажете.", 270, 6, 25, 14, MealType.SNACK, veg, MeatPreferenceType.NONE, true, false, true, "вегетарианска", "снак"),
                new RSeed("Домашни мюсли барчета", "Мюсли, мед, ядки. Смесете и запечете.", 300, 8, 30, 12, MealType.SNACK, veg, MeatPreferenceType.NONE, true, true, true, "вегетарианска", "снак"),
                new RSeed("Смути с кисело мляко и плодове", "Кисело мляко, банан, ягоди. Пасирайте всичко.", 290, 10, 28, 10, MealType.SNACK, veg, MeatPreferenceType.NONE, true, true, false, "вегетарианска", "снак"),
                new RSeed("Грис с мляко и мед", "Грис, мляко, мед. Сварете и поднесете.", 310, 7, 35, 9, MealType.SNACK, veg, MeatPreferenceType.NONE, true, true, false, "вегетарианска", "снак"),
                new RSeed("Печени ябълки с канела", "Ябълки, канела, мед. Запечете леко.", 260, 4, 30, 8, MealType.SNACK, veg, MeatPreferenceType.NONE, true, true, true, "вегетарианска", "снак"),
                new RSeed("Извара с праскова", "Извара, праскова, ванилия. Смесете леко.", 290, 16, 20, 10, MealType.SNACK, veg, MeatPreferenceType.NONE, true, true, false, "вегетарианска", "снак")
        ));

        seeds.forEach(s -> {
            Recipe recipe = Recipe.builder()
                    .name(s.n).description(s.d)
                    .calories(s.kcal).protein(s.p).carbs(s.c).fat(s.f)
                    .mealType(s.meal).dietType(s.diet).meatType(s.meat)
                    .isVegetarian(s.veg).containsDairy(s.dairy).containsNuts(s.nuts)
                    .tags(new HashSet<>(Arrays.asList(s.tags)))
                    .build();
            recipeRepository.save(recipe);
        });
        System.out.println("✔ Рецепти добавени: " + seeds.size());
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
}
