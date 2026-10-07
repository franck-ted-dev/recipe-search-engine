// Choisit le backend selon l'endroit d'où la page est servie.
const LOCAL_HOSTNAMES = ["localhost", "127.0.0.1"];
const API_BASE_URL = LOCAL_HOSTNAMES.includes(window.location.hostname)
    ? "http://localhost:8000"
    : "https://recipe-search-engine-a25m.onrender.com";

const RETRY_DELAY_MS = 3000;
const REQUEST_TIMEOUT_MS = 10000;

const ingredientsList = document.querySelector("#ingredients-list");

const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

// Vrai uniquement si /health répond 200. Une réponse en erreur, un délai dépassé ou une
// panne réseau (service endormi ou en cours de démarrage) veulent tous dire « pas prêt ».
async function isBackendHealthy() {
    try {
        const response = await fetch(`${API_BASE_URL}/health`, {
            signal: AbortSignal.timeout(REQUEST_TIMEOUT_MS),
        });
        return response.ok;
    } catch (error) {
        return false;
    }
}

// Vrai uniquement si la liste a été reçue et ajoutée au <datalist>.
async function loadCanonicalIngredients() {
    try {
        const response = await fetch(`${API_BASE_URL}/canonicalingredients`, {
            signal: AbortSignal.timeout(REQUEST_TIMEOUT_MS),
        });
        if (!response.ok) {
            return false;
        }
        const data = await response.json();

        const sortedIngredients = [...data.ingredients].sort((a, b) => a.localeCompare(b));
        ingredientsList.textContent = "";
        for (const ingredient of sortedIngredients) {
            const option = document.createElement("option");
            option.value = ingredient;
            ingredientsList.appendChild(option);
        }
        return true;
    } catch (error) {
        return false;
    }
}

function unlockSearch() {
    document.querySelector("#startup-status").hidden = true;
    document.querySelectorAll(".add-ingredient, #search-button").forEach((button) => {
        button.disabled = false;
    });
}

// Une seule requête en cours à la fois : on attend la fin de chacune avant de réessayer.
async function startApplication() {
    while (!(await isBackendHealthy())) {
        await wait(RETRY_DELAY_MS);
    }
    while (!(await loadCanonicalIngredients())) {
        await wait(RETRY_DELAY_MS);
    }
    unlockSearch();
}

startApplication().catch(console.error);

function createIngredientField() {
    const row = document.createElement("div");
    row.className = "ingredient-row";

    const input = document.createElement("input");
    input.type = "search";
    input.setAttribute("list", "ingredients-list");
    input.className = "ingredient-input";

    const clearButton = document.createElement("button");
    clearButton.type = "button";
    clearButton.textContent = "Clear";
    clearButton.addEventListener("click", () => {
        input.value = "";
    });

    const removeButton = document.createElement("button");
    removeButton.type = "button";
    removeButton.textContent = "Remove";
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
        const response = await fetch(`${API_BASE_URL}/search`, {
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
    messageError.textContent = "Unable to reach the server. Please try again later.";
    resultsContainer.appendChild(messageError);
}

function renderRecipes(recipes) {
    const resultsContainer = document.querySelector("#results");
    resultsContainer.textContent = "";

    if(recipes.length === 0) {
        const messageNoResults = document.createElement("p");
        messageNoResults.className = "no-results-message";
        messageNoResults.textContent = "No recipe matches this search";
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
        moreInfoButton.textContent = "More details";

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
