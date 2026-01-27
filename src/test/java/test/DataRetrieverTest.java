package test;

import com.Nj.code.*;
import com.Nj.code.Order;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataRetrieverTest {

    static DataRetriever dataRetriever;
    static Dish soup;
    static Ingredient fromage;
    static Ingredient oignon;
    static Order savedOrder;

    @BeforeAll
    static void setup() {
        DBConnection dbConnection = new DBConnection();
        dataRetriever = new DataRetriever(dbConnection);
    }

    // =========================
    // TEST 7.a
    // =========================
    @Test
    @org.junit.jupiter.api.Order(1)
    void testFindDishById() {
        Dish dish = dataRetriever.findDishById(1);
        assertNotNull(dish);
        assertTrue(dish.getIngredients().size() > 0);
    }

    // =========================
    // TEST 7.b
    // =========================
    @Test
    @org.junit.jupiter.api.Order(2)
    void testFindDishByIdNotFound() {
        assertThrows(RuntimeException.class,
                () -> dataRetriever.findDishById(999));
    }

    // =========================
    // TEST 7.c
    // =========================
    @Test
    @org.junit.jupiter.api.Order(3)
    void testFindIngredientPagination() {
        List<Ingredient> list = dataRetriever.findIngredient(2, 2);
        assertEquals(2, list.size());
    }

    // =========================
    // TEST 7.d
    // =========================
    @Test
    @org.junit.jupiter.api.Order(4)
    void testFindIngredientEmptyPage() {
        assertTrue(dataRetriever.findIngredient(3, 5).isEmpty());
    }

    // =========================
    // TEST 7.e
    // =========================
    @Test
    @org.junit.jupiter.api.Order(5)
    void testFindDishByIngredientName() {
        List<Dish> dishes = dataRetriever.findDishByIngredientName("eur");
        assertFalse(dishes.isEmpty());
    }

    // =========================
    // TEST 7.f
    // =========================
    @Test
    @org.junit.jupiter.api.Order(6)
    void testFindIngredientsByCriteria() {
        List<Ingredient> ingredients =
                dataRetriever.findIngredientsByCriteria(
                        null, Category.VEGETABLE, null, 1, 10);
        assertFalse(ingredients.isEmpty());
    }

    // =========================
    // TEST 7.i
    // =========================
    @Test
    @org.junit.jupiter.api.Order(7)
    void testCreateIngredients() {
        fromage = new Ingredient(0, "Fromage", 1200.0, Category.DAIRY, 20.0, "KG");
        oignon = new Ingredient(0, "Oignon", 500.0, Category.VEGETABLE, 30.0, "KG");

        dataRetriever.createIngredients(List.of(fromage, oignon));
        assertTrue(fromage.getStockQuantity() > 0);
    }

    // =========================
    // TEST 7.k
    // =========================
    @Test
    @org.junit.jupiter.api.Order(8)
    void testCreateDishConsumesStock() {
        Ingredient ing = dataRetriever.findIngredient(1, 1).get(0);

        soup = new Dish(
                0,
                "Soupe de légumes",
                DishType.START,
                List.of(new DishIngredient(null, ing, 2.0, "KG"))
        );
        soup.setPrice(2500.0);

        Dish saved = dataRetriever.saveDish(soup);
        assertNotNull(saved.getId());
    }

    // =========================
    // TEST 4.a / 4.b
    // =========================
    @Test
    @org.junit.jupiter.api.Order(9)
    void testDishCostAndMargin() {
        Dish dish = dataRetriever.findDishById(soup.getId());
        assertTrue(dish.getDishCost() > 0);
        assertTrue(dish.getGrossMargin() >= 0);
    }

    // =========================
    // TEST STOCK À UNE DATE
    // =========================
    @Test
    @org.junit.jupiter.api.Order(10)
    void testStockValueAtInstant() {
        Instant t = Instant.parse("2024-01-06T12:00:00Z");

        double fromageStock = fromage.getStockValueAt(t);
        double oignonStock = oignon.getStockValueAt(t);

        assertTrue(fromageStock >= 0);
        assertTrue(oignonStock >= 0);
    }

    // =====================================================
    // TEST 8.a – création commande
    // =====================================================
    @Test
    @org.junit.jupiter.api.Order(11)
    void testCreateOrder() {
        Dish dish = dataRetriever.findDishById(soup.getId());

        DishOrder dishOrder = new DishOrder(null, dish, 2);
        Order order = new Order();
        order.setDishOrders(List.of(dishOrder));

        savedOrder = dataRetriever.saveOrder(order);
        assertNotNull(savedOrder.getReference());
    }

    // =========================
    // TEST 8.b
    // =========================
    @Test
    @org.junit.jupiter.api.Order(12)
    void testStockAfterOrder() {
        Dish dish = dataRetriever.findDishById(soup.getId());
        dish.getIngredients().forEach(di ->
                assertTrue(di.getIngredient().getStockQuantity() >= 0)
        );
    }

    // =========================
    // TEST 8.c
    // =========================
    @Test
    @org.junit.jupiter.api.Order(13)
    void testFindOrderByReference() {
        Order found = dataRetriever.findOrderByReference(savedOrder.getReference());
        assertEquals(savedOrder.getReference(), found.getReference());
        assertFalse(found.getDishOrders().isEmpty());
    }

    // =========================
    // TEST 8.d
    // =========================
    @Test
    @org.junit.jupiter.api.Order(14)
    void testOrderStockInsufficient() {
        Dish dish = dataRetriever.findDishById(soup.getId());

        DishOrder tooMuch = new DishOrder(null, dish, 999);
        Order badOrder = new Order();
        badOrder.setDishOrders(List.of(tooMuch));

        assertThrows(RuntimeException.class,
                () -> dataRetriever.saveOrder(badOrder));
    }
}
