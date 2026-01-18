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
    public static void setup() {
        dbConnection = new DBConnection();
        dataRetriever = new DataRetriever(dbConnection);
    }

    @Test
    @Order(1)
    void testFindDishById() {
        Dish dish = dataRetriever.findDishById(1);
        assertNotNull(dish);
        assertEquals("Salade fraîche", dish.getName());
        assertTrue(dish.getIngredients().size() > 0);
    }

    @Test
    @Order(2)
    void testFindDishByIdNotFound() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dataRetriever.findDishById(999));
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
    void testFindIngredientEmptyPage() {
        List<Ingredient> emptyPage = dataRetriever.findIngredient(10, 5);
        assertTrue(emptyPage.isEmpty());
    }

    @Test
    @Order(5)
    void testFindDishByIngredientName() {
        List<Dish> dishes = dataRetriever.findDishByIngredientName("eur");
        assertNotNull(dishes);
        assertTrue(dishes.size() > 0);
    }

    @Test
    @Order(6)
    void testFindIngredientsByCriteriaCategory() {
        List<Ingredient> vegs = dataRetriever.findIngredientsByCriteria(
                null, Category.VEGETABLE, null, 1, 10);
        assertNotNull(vegs);
        assertFalse(vegs.isEmpty());
        assertTrue(vegs.stream().allMatch(i -> i.getCategory() == Category.VEGETABLE));
    }

    @Test
    @Order(7)
    void testFindIngredientsByCriteriaEmpty() {
        List<Ingredient> empty = dataRetriever.findIngredientsByCriteria("cho", null, "Sal", 1, 10);
        assertTrue(empty.isEmpty());
    }

    @Test
    @Order(8)
    void testCreateIngredients() {
        Ingredient fromage = new Ingredient(0, "Fromage", 1200.0, Category.DAIRY);
        Ingredient oignon = new Ingredient(0, "Oignon", 500.0, Category.VEGETABLE);
        List<Ingredient> created = dataRetriever.createIngredients(List.of(fromage, oignon));
        assertEquals(2, created.size());
    }

    @Test
    @Order(9)
    void testCreateAndSaveDish() {
        Ingredient ing1 = dataRetriever.findIngredient(1, 1).get(0);
        Dish soup = new Dish(
                0,
                "Soupe de légumes",
                DishType.START,
                List.of(new DishIngredient(null, ing1, 2.0, "KG"))
        );
        soup.setPrice(2500.0);
        Dish saved = dataRetriever.saveDish(soup);
        assertNotNull(saved.getId());
        assertEquals(1, saved.getIngredients().size());
    }

    @Test
    @Order(10)
    void testUpdateDish() {
        Ingredient ing1 = dataRetriever.findIngredient(1, 1).get(0);
        Ingredient ing2 = dataRetriever.findIngredient(2, 1).get(0);
        Dish updateDish = new Dish(
                1,
                "Salade fraîche",
                DishType.START,
                List.of(
                        new DishIngredient(null, ing1, 1.0, "KG"),
                        new DishIngredient(null, ing2, 1.0, "KG")
                )
        );
        updateDish.setPrice(4000.0);
        Dish updated = dataRetriever.saveDish(updateDish);
        assertEquals(2, updated.getIngredients().size());
    }

    @Test
    @Order(11)
    void testDishCostAndGrossMargin() {
        Dish dish = dataRetriever.findDishById(1);
        double cost = dish.getDishCost();
        assertTrue(cost > 0);

        double grossMargin = dish.getGrossMargin();
        assertEquals(dish.getPrice() - cost, grossMargin);
    }
}
