const API_BASE_URL = 'http://localhost:8080/api';

// API utility
const api = {
  async get(endpoint) {
    try {
      const response = await fetch(`${API_BASE_URL}${endpoint}`);
      if (!response.ok) throw new Error('Failed to fetch');
      return await response.json();
    } catch (error) {
      console.error('API Error:', error);
      return null;
    }
  }
};

// small POST helper used by this file - will use global storage token if available
api.post = async function(endpoint, body) {
  try {
    const headers = { 'Content-Type': 'application/json' };
    try {
      if (window.storage && typeof storage.getToken === 'function' && storage.getToken()) {
        headers['Authorization'] = `Bearer ${storage.getToken()}`;
      }
    } catch (_) {}

    const resp = await fetch(`${API_BASE_URL}${endpoint}`, {
      method: 'POST',
      headers,
      body: JSON.stringify(body)
    });

    if (!resp.ok) throw new Error('POST failed');
    const contentType = resp.headers.get('content-type') || '';
    if (contentType.includes('application/json')) return await resp.json();
    return null;
  } catch (err) {
    console.warn('API POST helper failed:', err);
    return null;
  }
};

// Load dynamic data on page load
document.addEventListener('DOMContentLoaded', async function() {
  // Load all dynamic content
  await Promise.all([
    loadWorkouts(),
    loadTrainers(),
    loadPricingPlans()
  ]);
  
  setupWorkoutFilters();
  setupAnimations();
  setupSmoothScroll();
  // initialize simple tracking UI for meals & progress charts (localStorage-backed fallback)
  initMealsUI();
});

// Load workouts dynamically
async function loadWorkouts() {
  const workouts = await api.get('/workouts/all');
  if (!workouts || workouts.length === 0) return;
  
  const workoutsGrid = document.querySelector('.workouts-grid');
  workoutsGrid.innerHTML = workouts.map(workout => `
    <div class="workout-card" data-category="${workout.category.toLowerCase()}">
      <div class="workout-image">
        <i class="fas ${workout.iconClass}"></i>
        <div class="workout-difficulty">${workout.difficulty}</div>
      </div>
      <div class="workout-info">
        <h3>${workout.name}</h3>
        <p>${workout.description}</p>
        <div class="workout-meta">
          <span><i class="fas fa-clock"></i> ${workout.duration} min</span>
          <span><i class="fas fa-fire"></i> ${workout.calories} cal</span>
        </div>
        <a href="auth.html" class="btn btn-outline">Start Workout</a>
      </div>
    </div>
  `).join('');
}

// Load trainers dynamically
async function loadTrainers() {
  const trainers = await api.get('/trainers/all');
  if (!trainers || trainers.length === 0) return;
  
  const trainersGrid = document.querySelector('.trainers-grid');
  trainersGrid.innerHTML = trainers.map(trainer => {
    const stars = '<i class="fas fa-star"></i>'.repeat(Math.floor(trainer.rating));
    return `
      <div class="trainer-card">
        <div class="trainer-image">
          <i class="fas fa-user-circle"></i>
          ${trainer.certified ? '<div class="trainer-badge">Certified</div>' : ''}
        </div>
        <div class="trainer-info">
          <h3>${trainer.name}</h3>
          <p class="trainer-specialty">${trainer.specialty}</p>
          <p class="trainer-experience">${trainer.experience}</p>
          <div class="trainer-rating">${stars}</div>
          <div class="trainer-price">₹${trainer.pricePerSession} / session</div>
          <div class="trainer-specialties">
            ${trainer.specialties.map(s => `<span class="specialty-tag">${s}</span>`).join('')}
          </div>
          <a href="auth.html" class="btn btn-outline">Book Session</a>
        </div>
      </div>
    `;
  }).join('');
}

// Load pricing plans dynamically
async function loadPricingPlans() {
  const plans = await api.get('/pricing/plans');
  if (!plans || plans.length === 0) return;
  
  const pricingGrid = document.querySelector('.pricing-grid');
  pricingGrid.innerHTML = plans.map(plan => `
    <div class="pricing-card ${plan.featured ? 'featured' : ''}">
      ${plan.featured ? '<div class="popular-badge">Most Popular</div>' : ''}
      <h3>${plan.name}</h3>
      <div class="price">
        <span class="currency">₹</span>
        <span class="amount">${plan.price}</span>
        <span class="period">${plan.period}</span>
      </div>
      <ul class="pricing-features">
        ${plan.features.map(f => `<li><i class="fas fa-check"></i> ${f}</li>`).join('')}
      </ul>
      <a href="auth.html" class="btn ${plan.featured ? 'btn-primary' : 'btn-outline'}">Get Started</a>
    </div>
  `).join('');
}

// Setup workout category filtering
function setupWorkoutFilters() {
  const categoryBtns = document.querySelectorAll('.category-btn');

  categoryBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      // Remove active class from all buttons
      categoryBtns.forEach(b => b.classList.remove('active'));
      // Add active class to clicked button
      btn.classList.add('active');

      const category = btn.getAttribute('data-category');
      const workoutCards = document.querySelectorAll('.workout-card');

      // Filter workout cards
      workoutCards.forEach(card => {
        const cardCategory = card.getAttribute('data-category');
        
        if (category === 'all' || cardCategory === category) {
          card.style.display = 'block';
          setTimeout(() => {
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
          }, 10);
        } else {
          card.style.opacity = '0';
          card.style.transform = 'translateY(20px)';
          setTimeout(() => {
            card.style.display = 'none';
          }, 300);
        }
      });
    });
  });
}

// Smooth scrolling for navigation links
function setupSmoothScroll() {
  document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
      e.preventDefault();
      const target = document.querySelector(this.getAttribute('href'));
      if (target) {
        target.scrollIntoView({
          behavior: 'smooth',
          block: 'start'
        });
      }
    });
  });
}

// Setup animations
function setupAnimations() {
  // Add scroll animation to navbar
  let lastScroll = 0;
  const navbar = document.querySelector('.navbar');

  window.addEventListener('scroll', () => {
    const currentScroll = window.pageYOffset;

    if (currentScroll <= 0) {
      navbar.style.boxShadow = '0 2px 20px rgba(0, 0, 0, 0.6)';
    } else {
      navbar.style.boxShadow = '0 4px 30px rgba(0, 0, 0, 0.8)';
    }

    lastScroll = currentScroll;
  });

  // Add animation on scroll for cards
  const observerOptions = {
    threshold: 0.1,
    rootMargin: '0px 0px -50px 0px'
  };

  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.style.opacity = '1';
        entry.target.style.transform = 'translateY(0)';
      }
    });
  }, observerOptions);

  // Re-observe cards after content loads
  setTimeout(() => {
    document.querySelectorAll('.feature-card, .workout-card, .trainer-card, .tracking-card, .pricing-card').forEach(card => {
      card.style.opacity = '0';
      card.style.transform = 'translateY(30px)';
      card.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
      observer.observe(card);
    });
  }, 100);
}

/* Meals & Progress UI (simple client-side implementation) */
function initMealsUI() {
  try {
    // Check if tracking section exists
    const exerciseContent = document.getElementById('exercise-content');
    const mealsContent = document.getElementById('meals-content');
    
    if (!exerciseContent && !mealsContent) {
      console.log('Tracking cards not found on this page');
      return;
    }

    // Create modals in DOM (if not present)
    if (!document.getElementById('mealModal')) createMealModal();
    if (!document.getElementById('exerciseModal')) createExerciseModal();

    // Load data from storage
    loadMealsFromStorage();
    loadExercisesFromStorage();
    initCharts();
    
    // Attach event listeners to buttons (they may be created dynamically)
    setTimeout(() => {
      const addMealBtn = document.getElementById('add-meal-btn');
      const addExerciseBtn = document.getElementById('add-exercise-btn');
      
      if (addMealBtn) addMealBtn.addEventListener('click', openMealModal);
      if (addExerciseBtn) addExerciseBtn.addEventListener('click', openExerciseModal);
    }, 100);
  } catch (err) {
    console.error('Meals UI init error', err);
  }
}

function createMealModal() {
  const modal = document.createElement('div');
  modal.id = 'mealModal';
  modal.className = 'simple-modal dark';
  modal.innerHTML = `
    <div class="simple-modal-backdrop" onclick="closeMealModal()"></div>
    <div class="simple-modal-card">
      <h3>Add Meal</h3>
      <form id="meal-form">
        <div class="form-group">
          <label>Meal name</label>
          <input id="meal-name" required />
        </div>
        <div class="form-group">
          <label>Type</label>
          <select id="meal-type"><option>BREAKFAST</option><option>LUNCH</option><option>DINNER</option><option>SNACK</option></select>
        </div>
        <div class="form-row">
          <div class="form-group"><label>Calories</label><input id="meal-calories" type="number" required /></div>
          <div class="form-group"><label>Protein (g)</label><input id="meal-protein" type="number" /></div>
        </div>
        <div class="form-row">
          <div class="form-group"><label>Carbs (g)</label><input id="meal-carbs" type="number" /></div>
          <div class="form-group"><label>Fats (g)</label><input id="meal-fats" type="number" /></div>
        </div>
        <div style="display:flex;gap:10px;justify-content:flex-end;margin-top:12px;">
          <button type="button" class="btn btn-secondary" onclick="closeMealModal()">Cancel</button>
          <button type="submit" class="btn btn-primary">Save Meal</button>
        </div>
      </form>
    </div>
  `;
  document.body.appendChild(modal);

  document.getElementById('meal-form').addEventListener('submit', function (e) {
    e.preventDefault();
    const meal = {
      name: document.getElementById('meal-name').value.trim(),
      type: document.getElementById('meal-type').value,
      calories: parseInt(document.getElementById('meal-calories').value) || 0,
      protein: parseFloat(document.getElementById('meal-protein').value) || 0,
      carbs: parseFloat(document.getElementById('meal-carbs').value) || 0,
      fats: parseFloat(document.getElementById('meal-fats').value) || 0,
      date: new Date().toISOString()
    };

    saveMealClient(meal);
    closeMealModal();
  });
}

function openMealModal() {
  const m = document.getElementById('mealModal');
  if (m) m.style.display = 'flex';
}

function closeMealModal() {
  const m = document.getElementById('mealModal');
  if (m) m.style.display = 'none';
}

// Exercise Modal
function createExerciseModal() {
  const modal = document.createElement('div');
  modal.id = 'exerciseModal';
  modal.className = 'simple-modal dark';
  modal.innerHTML = `
    <div class="simple-modal-backdrop" onclick="closeExerciseModal()"></div>
    <div class="simple-modal-card">
      <h3>Add Exercise</h3>
      <form id="exercise-form">
        <div class="form-group">
          <label>Exercise name</label>
          <input id="exercise-name" required />
        </div>
        <div class="form-group">
          <label>Type</label>
          <select id="exercise-type"><option>CARDIO</option><option>STRENGTH</option><option>FLEXIBILITY</option><option>SPORTS</option></select>
        </div>
        <div class="form-row">
          <div class="form-group"><label>Duration (min)</label><input id="exercise-duration" type="number" required /></div>
          <div class="form-group"><label>Calories</label><input id="exercise-calories" type="number" required /></div>
        </div>
        <div style="display:flex;gap:10px;justify-content:flex-end;margin-top:12px;">
          <button type="button" class="btn btn-secondary" onclick="closeExerciseModal()">Cancel</button>
          <button type="submit" class="btn btn-primary">Save Exercise</button>
        </div>
      </form>
    </div>
  `;
  document.body.appendChild(modal);

  document.getElementById('exercise-form').addEventListener('submit', function (e) {
    e.preventDefault();
    const exercise = {
      name: document.getElementById('exercise-name').value.trim(),
      type: document.getElementById('exercise-type').value,
      duration: parseInt(document.getElementById('exercise-duration').value) || 0,
      calories: parseInt(document.getElementById('exercise-calories').value) || 0,
      completed: false,
      date: new Date().toISOString()
    };

    saveExerciseClient(exercise);
    closeExerciseModal();
  });
}

function openExerciseModal() {
  const m = document.getElementById('exerciseModal');
  if (m) m.style.display = 'flex';
}

function closeExerciseModal() {
  const m = document.getElementById('exerciseModal');
  if (m) m.style.display = 'none';
}

async function saveExerciseClient(exercise) {
  try {
    // store locally first (fast UX)
    const key = 'fs_exercises';
    const arr = JSON.parse(localStorage.getItem(key) || '[]');
    arr.unshift(exercise);
    localStorage.setItem(key, JSON.stringify(arr));

    // Try to persist to backend using helper; if backend accepts partial update, pass full array
    try {
      await api.post('/tracking/update', { exercises: arr });
    } catch (err) {
      // ignore errors - will rely on localStorage fallback
    }

    // Also call legacy saveExercise if present (keeps compatibility)
    if (typeof saveExercise === 'function') {
      try { saveExercise(exercise); } catch (_) { /* ignore */ }
    }

    loadExercisesFromStorage();
    updateChartsFromStorage();
  } catch (err) {
    console.error('Failed to save exercise', err);
  }
}

function loadExercisesFromStorage() {
  const key = 'fs_exercises';
  const arr = JSON.parse(localStorage.getItem(key) || '[]');
  const container = document.getElementById('exercise-content');
  if (!container) return;

  if (arr.length === 0) {
    container.innerHTML = '<p class="empty-text">No exercises logged today.</p><button id="add-exercise-btn" class="btn btn-action">+ Add Exercise</button>';
    document.getElementById('add-exercise-btn').addEventListener('click', openExerciseModal);
    return;
  }

  container.innerHTML = arr.slice(0,5).map(ex => `
    <div class="exercise-item">
      <div>
        <strong>${ex.name}</strong> <small style="color:var(--gray);">(${ex.type})</small>
        <div style="font-size:0.9rem;color:var(--gray);">${ex.duration} min • ${ex.calories} cal</div>
      </div>
    </div>
  `).join('') + '<div style="margin-top:8px;text-align:right;"><button id="add-exercise-btn" class="btn btn-action">+ Add Exercise</button></div>';

  document.getElementById('add-exercise-btn').addEventListener('click', openExerciseModal);
}

async function saveMealClient(meal) {
  try {
    // store locally first (fast UX)
    const key = 'fs_meals';
    const arr = JSON.parse(localStorage.getItem(key) || '[]');
    arr.unshift(meal);
    localStorage.setItem(key, JSON.stringify(arr));

    // Try to persist to backend using helper; pass full meals array
    try {
      await api.post('/tracking/update', { meals: arr });
    } catch (err) {
      // ignore errors - fallback to localStorage
    }

    // Also call legacy saveMeal if present (keeps compatibility)
    if (typeof saveMeal === 'function') {
      try { saveMeal(meal); } catch (_) { /* ignore */ }
    }

    loadMealsFromStorage();
    updateChartsFromStorage();
  } catch (err) {
    console.error('Failed to save meal', err);
  }
}

function loadMealsFromStorage() {
  const key = 'fs_meals';
  const arr = JSON.parse(localStorage.getItem(key) || '[]');
  const container = document.getElementById('meals-content');
  if (!container) return;

  if (arr.length === 0) {
    container.innerHTML = '<p class="empty-text">No meals logged today.</p><button id="add-meal-btn" class="btn btn-action">+ Add Meal</button>';
    document.getElementById('add-meal-btn').addEventListener('click', openMealModal);
    return;
  }

  container.innerHTML = arr.slice(0,5).map(m => `
    <div class="meal-item">
      <div>
        <strong>${m.name}</strong> <small style="color:var(--gray);">(${m.type})</small>
        <div style="font-size:0.9rem;color:var(--gray);">${m.calories} cal • P:${m.protein}g C:${m.carbs}g F:${m.fats}g</div>
      </div>
    </div>
  `).join('') + '<div style="margin-top:8px;text-align:right;"><button id="add-meal-btn" class="btn btn-action">+ Add Meal</button></div>';

  document.getElementById('add-meal-btn').addEventListener('click', openMealModal);
}

// Charts (Chart.js) - render two small doughnuts
let caloriesChart = null;
let exerciseChart = null;
function initCharts() {
  // add Chart.js CDN if not present
  if (!window.Chart) {
    const s = document.createElement('script');
    s.src = 'https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js';
    s.onload = () => createCharts();
    document.head.appendChild(s);
  } else {
    createCharts();
  }
}

function createCharts() {
  const caloriesCtx = document.getElementById('caloriesChart').getContext('2d');
  const exerciseCtx = document.getElementById('exerciseChart').getContext('2d');

  // Goals will be pulled from user profile when available
  const defaultCalories = 2000;
  const defaultExerciseGoal = 45; // minutes

  const calData = [0, defaultCalories];
  const exData = [0, defaultExerciseGoal];

  if (caloriesChart) caloriesChart.destroy();
  if (exerciseChart) exerciseChart.destroy();

  caloriesChart = new Chart(caloriesCtx, {
    type: 'doughnut',
    data: {
      labels: ['Consumed', 'Remaining'],
      datasets: [{ data: calData, backgroundColor: ['#FF6B6B', '#E6EEF9'] }]
    },
    options: { plugins: { legend: { display: false } }, cutout: '70%' }
  });

  exerciseChart = new Chart(exerciseCtx, {
    type: 'doughnut',
    data: {
      labels: ['Minutes', 'Remaining'],
      datasets: [{ data: exData, backgroundColor: ['#4ECDC4', '#E6EEF9'] }]
    },
    options: { plugins: { legend: { display: false } }, cutout: '70%' }
  });

  updateChartsFromStorage();
}

async function updateChartsFromStorage() {
  const meals = JSON.parse(localStorage.getItem('fs_meals') || '[]');
  const exercises = JSON.parse(localStorage.getItem('fs_exercises') || '[]');

  const caloriesConsumed = meals.reduce((s, m) => s + (m.calories || 0), 0);

  const exerciseMinutes = exercises.reduce((s, e) => s + (e.duration || 0), 0);

  // Try to fetch user goals from backend/profile; fallback to defaults
  let caloriesGoal = 2000;
  let exerciseGoal = 45;
  try {
    const profile = await api.get('/user/profile');
    if (profile) {
      if (profile.dailyCalories) caloriesGoal = profile.dailyCalories;
      if (profile.exerciseGoalMinutes) exerciseGoal = profile.exerciseGoalMinutes;
    }
  } catch (err) {
    // ignore - use defaults
  }

  const caloriesRemaining = Math.max(0, caloriesGoal - caloriesConsumed);
  const exerciseRemaining = Math.max(0, exerciseGoal - exerciseMinutes);

  if (caloriesChart) {
    caloriesChart.data.datasets[0].data = [caloriesConsumed, caloriesRemaining];
    caloriesChart.update();
  }

  if (exerciseChart) {
    exerciseChart.data.datasets[0].data = [exerciseMinutes, exerciseRemaining];
    exerciseChart.update();
  }
}

// simple style for modal
// add minimal styles to page dynamically to avoid changing global css file too much
if (!document.getElementById('modal-style')) {
  const style = document.createElement('style');
  style.id = 'modal-style';
  style.innerHTML = `
    .simple-modal{display:none;position:fixed;inset:0;align-items:center;justify-content:center;z-index:9999}
    .simple-modal[style] {display:flex}
    .simple-modal-backdrop{position:absolute;inset:0;background:rgba(0,0,0,0.45)}
    .simple-modal-card{background:white;padding:18px;border-radius:12px;z-index:2;min-width:320px;max-width:520px}
    .simple-modal-card h3{margin:0 0 10px}
    .simple-modal-card .form-group{margin-bottom:10px}
    .simple-modal-card .form-group label{display:block;margin-bottom:5px;font-weight:500;font-size:0.9rem}
    .simple-modal-card .form-row{display:grid;grid-template-columns:1fr 1fr;gap:12px}
    .simple-modal-card input, .simple-modal-card select{width:100%;padding:8px;border:1px solid var(--border);border-radius:8px;box-sizing:border-box}

    /* Dark modal variations */
    .simple-modal.dark .simple-modal-card{background: linear-gradient(180deg, #0f1724 0%, #111827 100%); color: #e6eef9; border-radius:14px; padding:20px; box-shadow: 0 20px 50px rgba(2,6,23,0.7)}
    .simple-modal.dark .simple-modal-card h3{color:#fff}
    .simple-modal.dark .simple-modal-card label{color:#e6eef9}
    .simple-modal.dark .simple-modal-card input, .simple-modal.dark .simple-modal-card select{background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.06); color: #e6eef9}
    .simple-modal.dark .btn-secondary{background: rgba(255,255,255,0.04); color: #e6eef9; border: 1px solid rgba(255,255,255,0.06)}
    .simple-modal.dark .btn-primary{background: linear-gradient(135deg, #FF6B6B, #4ECDC4); color: #fff}
    .simple-modal.dark .simple-modal-backdrop{background: rgba(2,6,23,0.6)}
  `;
  document.head.appendChild(style);
}
