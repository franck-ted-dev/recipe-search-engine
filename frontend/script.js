function parseIngredients(rawText) {
    return rawText.split(",").map(terme => terme.trim()).filter(Boolean);
}

const form = document.querySelector("form");

form.addEventListener("submit", async function (event) {
    event.preventDefault();

    const requestBody = {
        all: parseIngredients(document.querySelector("#all-ingredients").value),
        any: parseIngredients(document.querySelector("#any-ingredients").value),
        none: parseIngredients(document.querySelector("#none-ingredients").value),
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