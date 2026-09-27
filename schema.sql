
CREATE TABLE Recette(
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
    recette_id INTEGER,
    FOREIGN KEY(recette_id) REFERENCES Recette
);