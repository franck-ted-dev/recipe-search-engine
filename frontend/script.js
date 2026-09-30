const ingredientsList = document.querySelector("#ingredients-list");
let ingredientsPresent = false;

async function loadCanonicalIngredientsIfNeeded() {
    if (ingredientsPresent) {
        return;
    }

    try {
        const response = await fetch("http://localhost:8000/canonicalingredients");
        const data = await response.json();

        const sortedIngredients = [...data.ingredients].sort((a, b) => a.localeCompare(b));
        for (const ingredient of sortedIngredients) {
            const option = document.createElement("option");
            option.value = ingredient;
            ingredientsList.appendChild(option);
        }

        ingredientsPresent = true;
    } catch (error) {
        // La saisie reste possible même si le chargement échoue, simplement sans suggestions.
    }
}

function createIngredientField() {
    const row = document.createElement("div");
    row.className = "ingredient-row";

    const input = document.createElement("input");
    input.type = "search";
    input.setAttribute("list", "ingredients-list");
    input.className = "ingredient-input";
    input.addEventListener("click", loadCanonicalIngredientsIfNeeded);

    const clearButton = document.createElement("button");
    clearButton.type = "button";
    clearButton.textContent = "Vider";
    clearButton.addEventListener("click", () => {
        input.value = "";
    });

    const removeButton = document.createElement("button");
    removeButton.type = "button";
    removeButton.textContent = "Supprimer";
    removeButton.addEventListener("click", () => {
        row.remove();
    });

    row.appendChild(input);
    row.appendChild(clearButton);
    row.appendChild(removeButton);

    return row;
}

document.querySelectorAll(".add-ingredient").forEach(function (button) {
    button.addEventListener("click", function () {
        const category = button.dataset.category;
        const container = document.querySelector(`.ingredient-fields[data-category="${category}"]`);
        container.appendChild(createIngredientField());
    });
});

function collectIngredients(category) {
    const container = document.querySelector(`.ingredient-fields[data-category="${category}"]`);
    const inputs = container.querySelectorAll(".ingredient-input");

    const values = [];
    for (const input of inputs) {
        const value = input.value.trim();
        if (value !== "") {
            values.push(value);
        }
    }
    return values;
}

const form = document.querySelector("form");

form.addEventListener("submit", async function (event) {
    event.preventDefault();

    const requestBody = {
        all: collectIngredients("all"),
        any: collectIngredients("any"),
        none: collectIngredients("none"),
    };

    try {
        const response = await fetch("http://localhost:8000/search", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(requestBody),
        });

        if (!response.ok) {
            renderError();
            return;
        }

        const data = await response.json();
        renderRecipes(data.recipes);
    } catch (error) {
        renderError();
    }
});

function renderError(){
    const resultsContainer = document.querySelector("#results");
    resultsContainer.textContent = "";

    const messageError = document.createElement("p");
    messageError.className = "error-message";
    messageError.textContent = "Impossible de contacter le serveur. Réessayer plus tard";
    resultsContainer.appendChild(messageError);
}

function renderRecipes(recipes) {
    const resultsContainer = document.querySelector("#results");
    resultsContainer.textContent = "";

    if(recipes.length === 0) {
        const messageNoResults = document.createElement("p");
        messageNoResults.className = "no-results-message";
        messageNoResults.textContent = "Aucune recette ne correspond à cette recherche";
        resultsContainer.appendChild(messageNoResults);
        return;
    }

    const dialog = document.querySelector("#recipe-details");
    const recipeCountry = document.querySelector("#dialog-country");
    const recipeIngredients = document.querySelector("#dialog-ingredients");
    const recipeInstructions = document.querySelector("#dialog-instructions");
    const recipeVideo = document.querySelector("#dialog-video");

    for (const recipe of recipes) {
        const card = document.createElement("div");
        card.className = "recipe-card";

        const image = document.createElement("img");
        image.src = recipe.image;
        image.alt = recipe.name;

        const name = document.createElement("p");
        name.textContent = recipe.name;

        const moreInfoButton = document.createElement("button");
        moreInfoButton.textContent = "Plus d'informations";

        moreInfoButton.addEventListener("click", () => {
            recipeCountry.textContent = recipe.country;
            recipeIngredients.textContent = "";
            for (const ingredient of recipe.ingredients) {
                const ingredientItem = document.createElement("li");
                ingredientItem.textContent = `${ingredient.name} - ${ingredient.quantity}`;
                recipeIngredients.appendChild(ingredientItem);
            }
            recipeInstructions.textContent = recipe.instructions;
            recipeVideo.href = recipe.video;
            dialog.showModal();
        });

        card.appendChild(image);
        card.appendChild(name);
        card.appendChild(moreInfoButton);
        resultsContainer.appendChild(card);
    }
}
