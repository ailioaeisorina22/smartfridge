const SUPABASE_URL = 'https://shacbhxbpnnxkvdeqild.supabase.co';
const SUPABASE_ANON_KEY = 'sb_publishable_3vu49Bh0iOYsmAp6qEAN9w_yUB2-04r';

const supabaseClient = window.supabase.createClient(SUPABASE_URL, SUPABASE_ANON_KEY);

// State
let session = null;
let isRegistering = false;
let currentRecipeHtml = "";

// Elements
const loadingView = document.getElementById('loading-view');
const authView = document.getElementById('auth-view');
const appView = document.getElementById('app-view');

const authForm = document.getElementById('auth-form');
const emailInput = document.getElementById('email');
const passwordInput = document.getElementById('password');
const authButton = document.getElementById('auth-button');
const toggleAuthText = document.getElementById('toggle-auth');
const authError = document.getElementById('auth-error');

const userEmailSpan = document.getElementById('user-email');
const signOutBtn = document.getElementById('sign-out');

const ingredientsInput = document.getElementById('ingredients');
const prefCheckboxes = document.querySelectorAll('.pref-checkbox');
const btnRecipe = document.getElementById('btn-recipe');
const btnList = document.getElementById('btn-list');
const generationMsg = document.getElementById('generation-msg');
const recipeContainer = document.getElementById('recipe-container');
const recipeContent = document.getElementById('recipe-content');

// Init
async function init() {
    try {
        const { data } = await supabaseClient.auth.getSession();
        session = data?.session || null;
        updateUI();

        supabaseClient.auth.onAuthStateChange((_event, currentSession) => {
            session = currentSession;
            updateUI();
        });
    } catch (err) {
        console.error(err);
        loadingView.style.display = 'none';
        authView.style.display = 'block';
        authError.textContent = "Eroare la inițializare: " + err.message;
        authError.style.display = 'block';
    }
}

function updateUI() {
    loadingView.style.display = 'none';
    if (session) {
        authView.style.display = 'none';
        appView.style.display = 'block';
        userEmailSpan.textContent = session.user.email;
        loadPreferences();
    } else {
        authView.style.display = 'block';
        appView.style.display = 'none';
    }
}

async function loadPreferences() {
    if (!session) return;
    try {
        const res = await fetch('/api/preferences?email=' + encodeURIComponent(session.user.email));
        if (res.ok) {
            const prefs = await res.json();
            prefCheckboxes.forEach(cb => {
                cb.checked = prefs.includes(cb.value);
            });
        }
    } catch (err) {
        console.error("Failed to load preferences", err);
    }
}

// Auth Events
toggleAuthText.addEventListener('click', () => {
    isRegistering = !isRegistering;
    authButton.textContent = isRegistering ? "Creeaza  cont" : "Intra in cont";
    toggleAuthText.textContent = isRegistering ? "Ai deja cont? Autentifica-te" : "Nu ai cont? Creeaza unul nou";
    authError.style.display = 'none';
});

authForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    authButton.innerHTML = '<span class="spinner"></span> Procesare...';
    authButton.disabled = true;
    authError.style.display = 'none';

    const email = emailInput.value;
    const password = passwordInput.value;

    try {
        if (isRegistering) {
            const { error } = await supabaseClient.auth.signUp({ email, password });
            if (error) throw error;
            alert("Cont creat! Acum te poți autentifica.");
            isRegistering = false;
            authButton.textContent = "Intra in cont";
            toggleAuthText.textContent = "Nu ai cont? Creeaza unul nou";
        } else {
            const { error } = await supabaseClient.auth.signInWithPassword({ email, password });
            if (error) {
                if (error.message.includes("Email not confirmed")) {
                    throw new Error("Contul a fost creat, dar trebuie confirmat! Verifică-ți mailul pentru linkul de confirmare.");
                }
                throw error;
            }
        }
    } catch (err) {
        authError.textContent = err.message;
        authError.style.display = 'block';
    } finally {
        authButton.disabled = false;
        authButton.textContent = isRegistering ? "Creeaza cont" : "Intra in cont";
    }
});

signOutBtn.addEventListener('click', async () => {
    await supabaseClient.auth.signOut();
});

// App Events
btnRecipe.addEventListener('click', () => handleAction('recipe'));
btnList.addEventListener('click', () => handleAction('shopping_list'));

ingredientsInput.addEventListener('input', () => {
    const val = ingredientsInput.value.trim();
    btnRecipe.disabled = !val;
    btnList.disabled = !val;
});

prefCheckboxes.forEach(cb => {
    cb.addEventListener('change', async () => {
        if (!session) return;
        const selectedPrefs = Array.from(prefCheckboxes)
            .filter(box => box.checked)
            .map(box => box.value);

        try {
            await fetch('/api/preferences', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    email: session.user.email,
                    preferences: selectedPrefs
                })
            });
        } catch (err) {
            console.error("Failed to save preferences", err);
        }
    });
});

async function handleAction(actionType) {
    const ingredients = ingredientsInput.value.trim();
    if (!ingredients) return;

    let payloadIngredients = ingredients;
    if (actionType === 'shopping_list' && currentRecipeHtml) {
        // Sneak the recipe into the ingredients string so Gemini knows what to build the list for!
        payloadIngredients += ". Foarte important, vreau sa prepar STRICT aceasta reteta: " + currentRecipeHtml;
    }

    generationMsg.style.display = 'none';
    const listMsg = document.getElementById('list-msg');
    if (listMsg) listMsg.style.display = 'none';
    if (actionType === 'recipe') {
        recipeContainer.style.display = 'none';
        btnRecipe.innerHTML = '<span class="spinner"></span> Se genereaza reteta...';
    } else {
        btnList.innerHTML = '<span class="spinner"></span> Se trimite lista pe email...';
    }

    btnRecipe.disabled = true;
    btnList.disabled = true;

    try {
        const res = await fetch("/api/generate-recipe", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                ingredients: payloadIngredients,
                email: session.user.email,
                action: actionType
            }),
        });

        const data = await res.json();
        if (!res.ok) throw new Error(data.error || "Eroare la generare");

        if (actionType === 'recipe') {
            currentRecipeHtml = data.recipe;
            recipeContent.innerHTML = data.recipe;
            recipeContainer.style.display = 'block';
            showMsg("Reteta a fost generata cu succes!", false);
        } else {
            const listMsg = document.getElementById('list-msg');
            listMsg.textContent = data.message;
            listMsg.style.color = '#10b981';
            listMsg.style.display = 'block';
        }
    } catch (err) {
        if (actionType === 'recipe') {
            showMsg("Eroare: " + err.message, true);
        } else {
            const listMsg = document.getElementById('list-msg');
            listMsg.textContent = "Eroare: " + err.message;
            listMsg.style.color = '#ef4444';
            listMsg.style.display = 'block';
        }
    } finally {
        btnRecipe.innerHTML = "Genereaza Reteta";
        btnList.innerHTML = "Trimite ingredientele lipsa pe email 🛒";
        btnRecipe.disabled = false;
        btnList.disabled = false;
    }
}

function showMsg(text, isError) {
    generationMsg.textContent = text;
    generationMsg.style.background = isError ? 'rgba(239, 68, 68, 0.1)' : 'rgba(34, 197, 94, 0.1)';
    generationMsg.style.display = 'block';
}

init();
