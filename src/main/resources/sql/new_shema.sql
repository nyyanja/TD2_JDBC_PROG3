
CREATE TABLE dish (
                      id SERIAL PRIMARY KEY,
                      name VARCHAR(255) NOT NULL,
                      dish_type VARCHAR(50) NOT NULL,
                      price DOUBLE PRECISION
);

CREATE TABLE ingredient (
                            id SERIAL PRIMARY KEY,
                            name VARCHAR(255) NOT NULL,
                            price DOUBLE PRECISION NOT NULL,
                            category VARCHAR(50) NOT NULL
);

CREATE TABLE dish_ingredient (
                                 dish_id INTEGER NOT NULL,
                                 ingredient_id INTEGER NOT NULL,
                                 quantity DOUBLE PRECISION NOT NULL,
                                 unit VARCHAR(20) NOT NULL,
                                 PRIMARY KEY (dish_id, ingredient_id),
                                 FOREIGN KEY (dish_id) REFERENCES dish(id),
                                 FOREIGN KEY (ingredient_id) REFERENCES ingredient(id)
);

CREATE TABLE stock_movement (
                                id SERIAL PRIMARY KEY,
                                ingredient_id INTEGER NOT NULL,
                                quantity DOUBLE PRECISION NOT NULL,
                                unit VARCHAR(10) NOT NULL,
                                movement_date TIMESTAMP NOT NULL,
                                FOREIGN KEY (ingredient_id) REFERENCES ingredient(id)
);
ALTER TABLE ingredient
    ADD COLUMN stock_quantity DOUBLE PRECISION NOT NULL,
ADD COLUMN stock_unit VARCHAR(20) NOT NULL;

