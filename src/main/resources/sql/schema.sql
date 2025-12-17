CREATE TABLE Ingredient(
    id  int PRIMARY KEY,
    name  varchar(100)   NOT NULL,
    price numeric(10, 2) NOT NULL,
    category enum('VEGETABLE', 'ANIMAL','MARINE','DAIRY','OTHER') NOT NULL,
    FOREIGN KEY (Dish_id) REFERENCES Dish(id)
);

CREATE TABLE Dish(
    id  int PRIMARY KEY,
    name  varchar(100)   NOT NULL,
    dish_type enum('START', 'MAIN','DESSERT') NOT NULL
);

