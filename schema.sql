
CREATE TABLE Recipe(
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR,
    instructions VARCHAR,
    country VARCHAR,
    imageURL VARCHAR,
    videoURL VARCHAR
);

CREATE TABLE Ingredient(
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR,
    quantity VARCHAR,
    recipe_id INTEGER,
    FOREIGN KEY(recipe_id) REFERENCES Recipe
);