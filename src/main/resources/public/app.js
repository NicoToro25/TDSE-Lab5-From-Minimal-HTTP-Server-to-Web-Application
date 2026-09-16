const loadingEl = document.getElementById("loading");
const resultEl = document.getElementById("result");
const errorEl = document.getElementById("error");

function showLoading() {
    loadingEl.style.display = "block";
    resultEl.textContent = "";
    errorEl.textContent = "";
}

function showResult(message) {
    loadingEl.style.display = "none";
    resultEl.textContent = "";
    errorEl.textContent = message;
}

async function callService(url, onSuccess) {
    showLoading();
    try {
        const response = await fetch(url);

        // Verificar el status antes de intentar parsear el body
        if (!response.ok) {
            // El servidor respondió con un status de error HTTP válido (400, 404).
            const errorBody = await response.json().catch(() => null);
            const message = errorBody && errorBody.error
                ? errorBody.error
                : `Request failed with status ${response}`;
            showError(message);
            return;
        }

        const data = await response.json();
        onSuccess(data);
    } catch (networkError) {
        // Esto captura fallos de RED (servidor caido, DNS, CORS, etc),
        // no errores HTTP como los que se hicieron arriba :)
        showError("Network error: could not reach the server.");
    }
}

document.getElementById("greeting-form").addEventListener("submit", function (event) {
    event.preventDefault(); // Evita el reload de la pagina
    const name = document.getElementById("greeting-name").value;
    const url = "/greeting?name=" + encodeURIComponent(name);

    callService(url, (data) => {
        showResult(data.message);
    });
});

document.getElementById("square-form").addEventListener("submit", function (event) {
    event.preventDefault(); // Evita el reload de la pagina
    const value = document.getElementById("square-value").value;
    const url = "/square?value=" + encodeURIComponent(value);

    callService(url, (data) => {
        showResult(`${data.input}² = ${data.square}`);
    });
});

document.getElementById("time-form").addEventListener("submit", function (event) {
    event.preventDefault();

    callService("/time", (data) => {
        showResult(`Server time: ${data.serverTime}`);
    });
});
