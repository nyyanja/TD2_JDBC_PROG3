

INSERT INTO dish(id, name, dish_type, price) VALUES
                                                 (1, 'Salade fraîche', 'START', 4000.00),
                                                 (2, 'Poulet grillé', 'MAIN', 7500.00),
                                                 (3, 'Riz au légumes', 'MAIN', 3500.00),
                                                 (4, 'Gâteaux au chocolat', 'DESSERT', 5000.00),
                                                 (5, 'Salade de fruits', 'DESSERT', 3000.00);

INSERT INTO ingredient(id, name, price, category) VALUES
                                                      (1, 'Laitue', 800.00, 'VEGETABLE'),
                                                      (2, 'Tomate', 600.00, 'VEGETABLE'),
                                                      (3, 'Poulet', 4500.00, 'ANIMAL'),
                                                      (4, 'Chocolat', 3000.00, 'OTHER'),
                                                      (5, 'Beurre', 2500.00, 'DAIRY'),
                                                      (6, 'Huile', 1500.00, 'OTHER');

INSERT INTO dish_ingredient(dish_id, ingredient_id, quantity, unit) VALUES
                                                                        (1, 1, 1, 'piece'),
                                                                        (1, 2, 0.25, 'KG'),
                                                                        (2, 3, 0.5, 'KG'),
                                                                        (2, 6, 0.15, 'L'),
                                                                        (4, 4, 0.3, 'KG'),
                                                                        (4, 5, 0.2, 'KG');
INSERT INTO stock_movement
(ingredient_id, quantity, unit, movement_type, movement_date)
VALUES
    (1, 10.0, 'KG', 'IN',  '2024-01-01 08:00'),
    (2, 20.0, 'KG', 'IN',  '2024-01-01 08:00'),
    (3, 15.0, 'KG', 'IN',  '2024-01-02 09:00'),
    (4, 8.0,  'KG', 'IN',  '2024-01-02 10:00'),
    (5, 6.0,  'KG', 'IN',  '2024-01-02 10:00'),
    (1, 1.0,  'KG', 'OUT', '2024-01-05 12:00'),
    (2, 0.25, 'KG', 'OUT', '2024-01-05 12:00'),
    (3, 0.5,  'KG', 'OUT', '2024-01-06 10:00');
