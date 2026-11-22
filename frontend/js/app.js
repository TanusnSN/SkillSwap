/* =========================================================
   SkillSwap frontend logic
   - While the backend is not ready, we use FAKE data.
   - Later set USE_FAKE_DATA = false to call the real Java API.
   ========================================================= */

// ---------- 1. SETTINGS ----------
const USE_FAKE_DATA = false;                       // change to false when backend is ready
const API_URL = "http://localhost:7070/api";      // Javalin server (confirm port with backend teammate)
const CURRENT_USER_ID = 1;                        // later this will come from login

// ---------- 2. FAKE DATA (same shape as the real API JSON) ----------
// sessionType: "ONE_ON_ONE" or "GROUP_WORKSHOP"
// In the real project the Java backend calculates the cost (polymorphism),
// the frontend only displays what the backend sends.
const fakeSkills = [
  { id: 1, name: "Java OOP Basics",       category: "Programming", teacher: "Asha Nair",  rating: 4.9, sessionType: "ONE_ON_ONE",     creditCost: 3, duration: 60, description: "Classes, objects, inheritance and interfaces explained with simple examples." },
  { id: 2, name: "Calculus Crash Course", category: "Maths",       teacher: "Ravi Kumar", rating: 4.8, sessionType: "GROUP_WORKSHOP", creditCost: 1, duration: 90, description: "Limits, derivatives and integrals before your exam. Bring your doubts." },
  { id: 3, name: "UI Design with Figma",  category: "Design",      teacher: "Meera Iyer", rating: 4.7, sessionType: "ONE_ON_ONE",     creditCost: 2, duration: 60, description: "Design clean app screens and learn the basics of colour and layout." },
  { id: 4, name: "Spoken French",         category: "Languages",   teacher: "Karthik S",  rating: 4.6, sessionType: "GROUP_WORKSHOP", creditCost: 1, duration: 60, description: "Everyday conversation practice for complete beginners." },
  { id: 5, name: "Guitar for Beginners",  category: "Music",       teacher: "Sana Ali",   rating: 5.0, sessionType: "ONE_ON_ONE",     creditCost: 2, duration: 45, description: "Learn your first chords and play a full song in one session." },
  { id: 6, name: "SQL & Databases",       category: "Programming", teacher: "Dev Patel",  rating: 4.8, sessionType: "GROUP_WORKSHOP", creditCost: 2, duration: 90, description: "Tables, joins and queries. Perfect before your DBMS lab." },
  { id: 7, name: "Linear Algebra Help",   category: "Maths",       teacher: "Nisha Rao",  rating: 4.5, sessionType: "ONE_ON_ONE",     creditCost: 3, duration: 60, description: "Matrices, vectors and eigenvalues made easy." },
  { id: 8, name: "Poster & Logo Design",  category: "Design",      teacher: "Arjun M",    rating: 4.7, sessionType: "GROUP_WORKSHOP", creditCost: 1, duration: 60, description: "Make eye-catching posters for your college fest." }
];
let fakeWallet = { userId: 1, name: "Yasha", balance: 5 };

// ---------- 3. PAGE STATE ----------
const state = {
  skills: [],          // all skills from API
  balance: 0,          // student's credits
  category: "All",     // selected filter
  search: "",          // text typed in search box
  selected: null       // skill chosen in the popup
};

// ---------- 4. API FUNCTIONS (fake or real) ----------

// GET /api/skills
async function fetchSkills() {
  if (USE_FAKE_DATA) return fakeSkills;
  const res = await fetch(API_URL + "/skills");
  return await res.json();
}

// GET /api/wallet/{userId}
async function fetchWallet() {
  if (USE_FAKE_DATA) return fakeWallet;
  const res = await fetch(API_URL + "/wallet/" + CURRENT_USER_ID);
  return await res.json();
}

// POST /api/sessions   body: { userId, skillId }
// Backend throws InsufficientCreditsException -> we get an error message back
async function bookSession(skill) {
  if (USE_FAKE_DATA) {
    if (fakeWallet.balance < skill.creditCost) {
      throw new Error("Not enough credits. Teach a session to earn more!");
    }
    fakeWallet.balance -= skill.creditCost;
    return fakeWallet;
  }
  const res = await fetch(API_URL + "/sessions", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ userId: CURRENT_USER_ID, skillId: skill.id })
  });
  const data = await res.json();
  if (!res.ok) throw new Error(data.error || "Booking failed");
  return data;   // expected: { balance: 2 }
}

// ---------- 5. SMALL HELPER FUNCTIONS ----------

// Stops special characters in data from breaking the page
function escapeHtml(text) {
  const div = document.createElement("div");
  div.textContent = text;
  return div.innerHTML;
}

// Nice label for the session type
function typeLabel(type) {
  return type === "GROUP_WORKSHOP" ? "Group workshop" : "1-on-1";
}

// Round coloured circle with the teacher's first letter
function teacherAvatar(skill) {
  const colour = "hsl(" + ((skill.id * 47) % 360) + ", 65%, 50%)";
  return `<div class="teacher-avatar" style="background:${colour}">${escapeHtml(skill.teacher[0])}</div>`;
}

// Small popup message at the bottom right
function showToast(message, type) {
  const toast = document.createElement("div");
  toast.className = "toast " + (type || "");
  toast.textContent = message;
  document.getElementById("toast-area").appendChild(toast);
  setTimeout(() => toast.remove(), 3500);
}

// Update the credit number in the navbar with a small bounce
function updateBalance(balance) {
  state.balance = balance;
  document.getElementById("credit-balance").textContent = balance;
  const pill = document.querySelector(".credit-pill");
  pill.classList.add("bump");
  setTimeout(() => pill.classList.remove("bump"), 300);
}

// ---------- 6. DRAWING THE PAGE ----------

// Category buttons: All + every category found in the data
function renderChips() {
  const categories = ["All", ...new Set(state.skills.map(s => s.category))];
  const box = document.getElementById("category-chips");
  box.innerHTML = "";
  categories.forEach(cat => {
    const btn = document.createElement("button");
    btn.className = "chip" + (cat === state.category ? " active" : "");
    btn.textContent = cat;
    btn.onclick = () => { state.category = cat; renderChips(); renderSkills(); };
    box.appendChild(btn);
  });
}

// Skill cards, filtered by category and search text
function renderSkills() {
  const text = state.search.toLowerCase();
  const visible = state.skills.filter(s =>
    (state.category === "All" || s.category === state.category) &&
    (s.name.toLowerCase().includes(text) || s.teacher.toLowerCase().includes(text))
  );

  const grid = document.getElementById("skill-list");
  grid.innerHTML = visible.map(s => `
    <article class="skill-card">
      <div class="card-top">
        <span class="tag">${escapeHtml(s.category)}</span>
        <span class="tag ${s.sessionType === "GROUP_WORKSHOP" ? "group" : ""}">${typeLabel(s.sessionType)}</span>
      </div>
      <h3>${escapeHtml(s.name)}</h3>
      <p class="skill-desc">${escapeHtml(s.description)}</p>
      <div class="teacher">
        ${teacherAvatar(s)}
        <div>
          <div class="teacher-name">${escapeHtml(s.teacher)}</div>
          <div class="teacher-rating">⭐ ${s.rating} · ${s.duration} min</div>
        </div>
      </div>
      <div class="card-bottom">
        <div class="cost">${s.creditCost} <small>credits</small></div>
        <button class="btn btn-primary" onclick="openModal(${s.id})">Book</button>
      </div>
    </article>
  `).join("");

  // Show the "nothing found" message if the list is empty
  document.getElementById("empty-state").hidden = visible.length > 0;
}

// ---------- 7. BOOKING POPUP ----------

function openModal(skillId) {
  const skill = state.skills.find(s => s.id === skillId);
  state.selected = skill;
  const left = state.balance - skill.creditCost;   // credits after booking
  const notEnough = left < 0;

  document.getElementById("modal-content").innerHTML = `
    <span class="tag ${skill.sessionType === "GROUP_WORKSHOP" ? "group" : ""}">${typeLabel(skill.sessionType)}</span>
    <h3>${escapeHtml(skill.name)}</h3>
    <p class="skill-desc">with ${escapeHtml(skill.teacher)} · ${skill.duration} min</p>
    <div class="modal-rows">
      <div class="modal-row"><span>Session cost</span><strong>${skill.creditCost} credits</strong></div>
      <div class="modal-row"><span>Your balance</span><strong>${state.balance} credits</strong></div>
      <div class="modal-row ${notEnough ? "low" : ""}"><span>Balance after</span><strong>${left} credits</strong></div>
    </div>
    <div class="modal-actions">
      <button class="btn btn-ghost" onclick="closeModal()">Cancel</button>
      <button class="btn btn-primary" id="confirm-btn" onclick="confirmBooking()">Confirm booking</button>
    </div>
  `;
  document.getElementById("modal").hidden = false;
}

function closeModal() {
  document.getElementById("modal").hidden = true;
}

async function confirmBooking() {
  const btn = document.getElementById("confirm-btn");
  btn.disabled = true;
  btn.textContent = "Booking...";
  try {
    const result = await bookSession(state.selected);
    updateBalance(result.balance);
    closeModal();
    showToast("Booked " + state.selected.name + " 🎉", "success");
  } catch (error) {
    closeModal();
    showToast(error.message, "error");   // e.g. InsufficientCreditsException message
  }
}

// ---------- 8. START THE APP ----------

async function init() {
  try {
    const [skills, wallet] = await Promise.all([fetchSkills(), fetchWallet()]);
    state.skills = skills;
    updateBalance(wallet.balance);
    document.getElementById("user-avatar").textContent = wallet.name[0];
    renderChips();
    renderSkills();
  } catch (error) {
    showToast("Could not load data. Is the backend running?", "error");
  }
}

// Search box: re-draw cards every time the user types
document.getElementById("skill-search").addEventListener("input", (e) => {
  state.search = e.target.value;
  renderSkills();
});

// Close popup when clicking outside it or pressing Escape
document.getElementById("modal").addEventListener("click", (e) => {
  if (e.target.id === "modal") closeModal();
});
document.addEventListener("keydown", (e) => {
  if (e.key === "Escape") closeModal();
});

init();