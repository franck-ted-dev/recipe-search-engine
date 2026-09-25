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

    const response = await fetch("http://localhost:8000/search", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestBody),
    });

    const data = await response.json();
    console.log(data);
});