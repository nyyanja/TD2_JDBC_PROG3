package test;

import com.Nj.code.*;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataRetrieverTest {

    private static DBConnection dbConnection;
    private static DataRetriever dataRetriever;

    @BeforeAll
    static void setup() {
        dbConnection = new DBConnection();
        dataRetriever = new DataRetriever(dbConnection);
    }

    @AfterAll
    static void cleanup() {
        dbConnection.close();
    }

    @Test
    @Order(1)
    void testFindDishByIdExists() {
        Dish dish = dataRetriever.findDishById(1);
        assertNotNull(dish);
        assertEquals("Salade fraîche", dish.getName());
        assertEquals(2, dish.getIngredients().size());
    }

    @Test
    @Order(2)
    void testFindDishByIdNotExists() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> dataRetriever.findDishById(999));
        assertTrue(ex.getMessage().contains("Plat non trouvé"));
    }

    @Test
    @Order(3)
    void testFindIngredientPagination() {
        List<Ingredient> page2 = dataRetriever.findIngredient(2, 2);
        assertNotNull(page2);
    }

    @Test
    @Order(4)
    void testFindIngredientPaginationEmpty() {
        List<Ingredient> emptyPage = dataRetriever.findIngredient(10, 5);
        assertTrue(emptyPage.isEmpty());
    }

    @Test
    @Order(5)
    void testFindDishByIngredientName() {
        List<Dish> dishes = dataRetriever.findDishByIngredientName("Chocolat");
        assertFalse(dishes.isEmpty());
        assertEquals("Gâteaux au chocolat", dishes.get(0).getName());
    }

    @Test
    @Order(6)
    void testFindIngredientsByCriteria() {
        List<Ingredient> veggies = dataRetriever.findIngredientsByCriteria(null, Category.VEGETABLE, null, 1, 10);
        assertFalse(veggies.isEmpty());
        for (Ingredient ing : veggies) {
            assertEquals(Category.VEGETABLE, ing.getCategoryEnum());
        }

        List<Ingredient> emptyList = dataRetriever.findIngredientsByCriteria("NonExistant", null, null, 1, 10);
        assertTrue(emptyList.isEmpty());
    }

    @Test
    @Order(7)
    void testCreateIngredientsAtomicity() {
        // Insertion d'ingrédients uniques
        List<Ingredient> newIngredients = List.of(
                new Ingredient(0, "FromageUnit", 1200.0, Category.DAIRY, null),
                new Ingredient(0, "OignonUnit", 500.0, Category.VEGETABLE, null)
        );
        assertDoesNotThrow(() -> dataRetriever.createIngredients(newIngredients));

        // Insertion avec doublon doit rollback
        List<Ingredient> dupIngredients = List.of(
                new Ingredient(0, "Laitue", 2000.0, Category.VEGETABLE, null),
                new Ingredient(0, "Carotte", 1500.0, Category.VEGETABLE, null)
        );
        RuntimeException ex = assertThrows(RuntimeException.class, () -> dataRetriever.createIngredients(dupIngredients));
        assertTrue(ex.getMessage().contains("déjà existant"));
    }

    @Test
    @Order(8)
    void testSaveDishInsertAndUpdate() {
        Dish newDish = new Dish(0, "Soupe Test", DishType.START, List.of(
                new Ingredient(0, "OignonTest", 100.0, Category.VEGETABLE, null)
        ));
        Dish saved = dataRetriever.saveDish(newDish);
        assertTrue(saved.getId() > 0);

        Dish updateDish = new Dish(saved.getId(), "Soupe Test Modifiée", DishType.START, List.of(
                new Ingredient(0, "OignonTest", 100.0, Category.VEGETABLE, null),
                new Ingredient(0, "CarotteTest", 200.0, Category.VEGETABLE, null)
        ));
        Dish updated = dataRetriever.saveDish(updateDish);
        assertEquals(2, updated.getIngredients().size());
    }
}
