/* =========================================================
   SkillSwap frontend logic.
   Talks to the Java REST API. The Java server also serves this page,
   so we can use the short address "/api".
   ========================================================= */

const API_URL = "/api";

// ---------- 1. PAGE STATE ----------
const state = {
  skills: [],        // all sessions from the API
  users: [],         // all students (for the switcher)
  userId: 1,         // the student currently using the site
  balance: 0,        // that student's credits
  category: "All",   // selected filter
  search: "",        // text typed in the search box
  selected: null     // session chosen in the booking popup
};

// ---------- 2. API HELPERS ----------

// Calls the API, returns JSON, throws an Error with the server's message on failure
async function api(path, options) {
  const res = await fetch(API_URL + path, options);
  let data = {};
  try { data = await res.json(); } catch (e) { /* empty body */ }
  if (!res.ok) throw new Error(data.error || "Something went wrong");
  return data;
}

function postJson(path, body) {
  return api(path, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body)
  });
}

// ---------- 3. SMALL HELPERS ----------

// Stops special characters in data from breaking the page
function escapeHtml(text) {
  const div = document.createElement("div");
  div.textContent = text;
  return div.innerHTML;
}

function typeLabel(type) {
  return type === "GROUP_WORKSHOP" ? "Group workshop" : "1-on-1";
}

function teacherAvatar(skill) {
  const colour = "hsl(" + ((skill.teacherId * 47) % 360) + ", 65%, 50%)";
  return `<div class="teacher-avatar" style="background:${colour}">${escapeHtml(skill.teacher[0])}</div>`;
}

function showToast(message, type) {
  const toast = document.createElement("div");
  toast.className = "toast " + (type || "");
  toast.textContent = message;
  document.getElementById("toast-area").appendChild(toast);
  setTimeout(() => toast.remove(), 3500);
}

function updateBalance(balance) {
  state.balance = balance;
  document.getElementById("credit-balance").textContent = balance;
  const pill = document.querySelector(".credit-pill");
  pill.classList.add("bump");
  setTimeout(() => pill.classList.remove("bump"), 300);
}

function currentUser() {
  return state.users.find(u => u.id === state.userId);
}

// Remember which student was selected (optional convenience)
function loadSavedUser() {
  try { return parseInt(localStorage.getItem("skillswapUser")) || 1; } catch (e) { return 1; }
}
function saveUser(id) {
  try { localStorage.setItem("skillswapUser", String(id)); } catch (e) { /* ignore */ }
}

// ---------- 4. LOADING DATA ----------

async function refreshWallet() {
  const wallet = await api("/wallet/" + state.userId);
  updateBalance(wallet.balance);
}

async function loadSkills() {
  state.skills = await api("/skills");
  renderChips();
  renderSkills();
}

// ---------- 5. DRAWING THE PAGE ----------

function renderUserSelect() {
  const select = document.getElementById("user-select");
  select.innerHTML = state.users.map(u =>
    `<option value="${u.id}" ${u.id === state.userId ? "selected" : ""}>${escapeHtml(u.name)}</option>`
  ).join("");
}

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

function renderSkills() {
  const text = state.search.toLowerCase();
  const visible = state.skills.filter(s =>
    (state.category === "All" || s.category === state.category) &&
    (s.name.toLowerCase().includes(text) || s.teacher.toLowerCase().includes(text))
  );

  document.getElementById("skill-list").innerHTML = visible.map(s => {
    const mine = s.teacherId === state.userId;   // you cannot book your own session
    const rating = s.rating > 0 ? "\u2B50 " + s.rating : "New";
    return `
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
          <div class="teacher-rating">${rating} \u00B7 ${s.duration} min</div>
        </div>
      </div>
      <div class="card-bottom">
        <div class="cost">${s.creditCost} <small>credits</small></div>
        ${mine
          ? `<button class="btn btn-mine" disabled>Your session</button>`
          : `<button class="btn btn-primary" onclick="openModal(${s.id})">Book</button>`}
      </div>
    </article>`;
  }).join("");

  document.getElementById("empty-state").hidden = visible.length > 0;
}

// ---------- 6. BOOKING POPUP ----------

function openModal(skillId) {
  const skill = state.skills.find(s => s.id === skillId);
  state.selected = skill;
  const left = state.balance - skill.creditCost;
  const notEnough = left < 0;

  document.getElementById("modal-content").innerHTML = `
    <span class="tag ${skill.sessionType === "GROUP_WORKSHOP" ? "group" : ""}">${typeLabel(skill.sessionType)}</span>
    <h3>${escapeHtml(skill.name)}</h3>
    <p class="skill-desc">with ${escapeHtml(skill.teacher)} \u00B7 ${skill.duration} min</p>
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
    const result = await postJson("/sessions", { userId: state.userId, skillId: state.selected.id });
    updateBalance(result.balance);
    closeModal();
    showToast("Booked " + state.selected.name + " \uD83C\uDF89", "success");
  } catch (error) {
    closeModal();
    showToast(error.message, "error");   // e.g. the InsufficientCreditsException message
  }
}

// ---------- 7. TEACH FORM ----------

function openTeachModal() {
  const user = currentUser();
  document.getElementById("modal-content").innerHTML = `
    <h3>Teach a skill</h3>
    <p class="skill-desc">Hosting as <strong>${escapeHtml(user ? user.name : "")}</strong>.
       You earn credits every time a student books.</p>
    <form id="teach-form">
      <label class="form-field">Skill name
        <input name="name" required maxlength="60" placeholder="e.g. Python for Beginners">
      </label>
      <label class="form-field">Category
        <select name="category">
          <option>Programming</option><option>Maths</option><option>Design</option>
          <option>Languages</option><option>Music</option><option>Other</option>
        </select>
      </label>
      <label class="form-field">Description
        <textarea name="description" rows="3" maxlength="200" placeholder="What will students learn?"></textarea>
      </label>
      <label class="form-field">Session type
        <select name="sessionType" id="teach-type">
          <option value="ONE_ON_ONE">1-on-1 (one student)</option>
          <option value="GROUP_WORKSHOP">Group workshop (many students)</option>
        </select>
      </label>
      <label class="form-field">Duration
        <select name="duration" id="teach-duration">
          <option value="30">30 minutes</option>
          <option value="60" selected>60 minutes</option>
          <option value="90">90 minutes</option>
          <option value="120">120 minutes</option>
        </select>
      </label>
      <label class="form-field" id="max-label" hidden>Maximum students
        <input type="number" name="maxParticipants" value="10" min="2" max="50">
      </label>
      <p class="cost-preview" id="cost-preview"></p>
      <div class="modal-actions">
        <button type="button" class="btn btn-ghost" onclick="closeModal()">Cancel</button>
        <button type="submit" class="btn btn-primary">Publish session</button>
      </div>
    </form>
  `;
  document.getElementById("modal").hidden = false;

  document.getElementById("teach-type").addEventListener("change", onTeachFormChange);
  document.getElementById("teach-duration").addEventListener("change", onTeachFormChange);
  document.getElementById("teach-form").addEventListener("submit", submitTeachForm);
  onTeachFormChange();
}

// Show/hide the "max students" box and ask the Java server for the cost
async function onTeachFormChange() {
  const type = document.getElementById("teach-type").value;
  const duration = document.getElementById("teach-duration").value;
  document.getElementById("max-label").hidden = (type !== "GROUP_WORKSHOP");

  const preview = document.getElementById("cost-preview");
  try {
    const result = await api("/cost?type=" + type + "&duration=" + duration);
    preview.textContent = "Each student pays " + result.creditCost + " credits. You earn the same.";
  } catch (error) {
    preview.textContent = "";
  }
}

async function submitTeachForm(event) {
  event.preventDefault();
  const form = event.target;
  const data = new FormData(form);
  const button = form.querySelector("button[type=submit]");
  button.disabled = true;

  try {
    await postJson("/teach", {
      userId: state.userId,
      name: data.get("name"),
      category: data.get("category"),
      description: data.get("description"),
      sessionType: data.get("sessionType"),
      duration: parseInt(data.get("duration")),
      maxParticipants: parseInt(data.get("maxParticipants"))
    });
    closeModal();
    showToast("Your session is live! \uD83D\uDE80", "success");
    state.category = "All";
    state.search = "";
    document.getElementById("skill-search").value = "";
    await loadSkills();
    document.getElementById("skills").scrollIntoView();
  } catch (error) {
    showToast(error.message, "error");
    button.disabled = false;
  }
}

// ---------- 8. START THE APP ----------

async function init() {
  try {
    state.users = await api("/users");
    const saved = loadSavedUser();
    state.userId = state.users.some(u => u.id === saved) ? saved : state.users[0].id;
    renderUserSelect();
    await Promise.all([refreshWallet(), loadSkills()]);
  } catch (error) {
    showToast("Could not load data. Is the Java server running?", "error");
  }
}

// Switching student
document.getElementById("user-select").addEventListener("change", async (e) => {
  state.userId = parseInt(e.target.value);
  saveUser(state.userId);
  await refreshWallet();
  renderSkills();
  showToast("You are now " + currentUser().name);
});

// Search box
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