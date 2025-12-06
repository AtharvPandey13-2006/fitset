const API_BASE_URL = 'http://localhost:8080/api';

// Storage utilities
const storage = {
    setToken: (token) => localStorage.setItem('fitset_token', token),
    getToken: () => localStorage.getItem('fitset_token'),
    removeToken: () => localStorage.removeItem('fitset_token'),
    setUser: (user) => localStorage.setItem('fitset_user', JSON.stringify(user)),
    getUser: () => {
        const user = localStorage.getItem('fitset_user');
        return user ? JSON.parse(user) : null;
    },
    removeUser: () => localStorage.removeItem('fitset_user')
};

// API utilities
const api = {
    headers: () => ({
        'Content-Type': 'application/json',
        // Only include Authorization header if we actually have a token
        ...(storage.getToken() ? { 'Authorization': `Bearer ${storage.getToken()}` } : {})
    }),
    
    async call(endpoint, method = 'GET', body = null) {
        const options = {
            method,
            headers: api.headers()
        };
        
        if (body) {
            options.body = JSON.stringify(body);
        }
        
        try {
            const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
            const contentType = response.headers.get('content-type') || '';
            let data = null;
            
            // Handle no-content responses and non-JSON bodies safely
            if (response.status === 204) {
                data = null;
            } else if (contentType.includes('application/json')) {
                try {
                    data = await response.json();
                } catch (_) {
                    data = null; // malformed/empty JSON
                }
            } else {
                // Try to read as text; if it looks like JSON, parse it
                const text = await response.text();
                try {
                    data = text ? JSON.parse(text) : null;
                } catch (_) {
                    data = text; // plain text error or message
                }
            }
            
            if (!response.ok) {
                const message = (data && typeof data === 'object' && data.message)
                    ? data.message
                    : (typeof data === 'string' && data)
                        ? data
                        : `Request failed (${response.status})`;
                throw new Error(message);
            }
            
            return data;
        } catch (error) {
            console.error('API Error:', error);
            // Provide more helpful error messages
            if (error.message.includes('Failed to fetch') || error.message.includes('NetworkError')) {
                throw new Error('Unable to connect to server. Please ensure the backend is running on http://localhost:8080');
            }
            throw error;
        }
    }
};

// Initialize app
document.addEventListener('DOMContentLoaded', () => {
    console.log('App initializing...');
    initAuth();
    initForms();
    
    // Check if user is logged in
    if (storage.getToken()) {
        showDashboard();
        loadUserData();
    } else {
        console.log('No token found, showing auth form');
    }
});

// Auth functions
function initAuth() {
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    
    if (loginForm && registerForm) {
        loginForm.addEventListener('submit', handleLogin);
        registerForm.addEventListener('submit', handleRegister);
        console.log('Auth forms initialized');
    } else {
        console.error('Auth forms not found');
    }
}

function showLogin() {
    document.getElementById('login-form').style.display = 'flex';
    document.getElementById('register-form').style.display = 'none';
    document.querySelectorAll('.tab-btn')[0].classList.add('active');
    document.querySelectorAll('.tab-btn')[1].classList.remove('active');
}

function showRegister() {
    document.getElementById('login-form').style.display = 'none';
    document.getElementById('register-form').style.display = 'flex';
    document.querySelectorAll('.tab-btn')[0].classList.remove('active');
    document.querySelectorAll('.tab-btn')[1].classList.add('active');
}

async function handleLogin(e) {
    e.preventDefault();
    console.log('Login attempt started');
    
    const email = document.getElementById('login-email').value.trim();
    const password = document.getElementById('login-password').value;
    const errorDiv = document.getElementById('login-error');
    
    // Clear previous errors
    errorDiv.textContent = '';
    errorDiv.style.display = 'none';
    
    // Client-side validation
    if (!email) {
        errorDiv.textContent = 'Email is required';
        errorDiv.style.display = 'block';
        return;
    }
    
    if (!password) {
        errorDiv.textContent = 'Password is required';
        errorDiv.style.display = 'block';
        return;
    }
    
    if (!isValidEmail(email)) {
        errorDiv.textContent = 'Please enter a valid email address';
        errorDiv.style.display = 'block';
        return;
    }
    
    try {
        console.log('Sending login request...');
        const data = await api.call('/auth/login', 'POST', { email, password });
        console.log('Login successful:', data);
        
        if (!data || !data.token) {
            throw new Error('Invalid response from server');
        }
        
        storage.setToken(data.token);
        storage.setUser({ id: data.userId, email: data.email, username: data.username });
        
        showDashboard();
        loadUserData();
    } catch (error) {
        console.error('Login error:', error);
        errorDiv.textContent = error.message || 'Login failed. Please check your credentials.';
        errorDiv.style.display = 'block';
    }
}

async function handleRegister(e) {
    e.preventDefault();
    console.log('Registration attempt started');
    
    const errorDiv = document.getElementById('register-error');
    
    // Clear previous errors
    errorDiv.textContent = '';
    errorDiv.style.display = 'none';
    
    const email = document.getElementById('reg-email').value.trim();
    const username = document.getElementById('reg-username').value.trim();
    const password = document.getElementById('reg-password').value;
    const weight = parseFloat(document.getElementById('reg-weight').value);
    const height = parseFloat(document.getElementById('reg-height').value);
    const age = parseInt(document.getElementById('reg-age').value);
    
    // Client-side validation
    if (!email || !username || !password) {
        errorDiv.textContent = 'Email, username, and password are required';
        errorDiv.style.display = 'block';
        return;
    }
    
    if (!isValidEmail(email)) {
        errorDiv.textContent = 'Please enter a valid email address';
        errorDiv.style.display = 'block';
        return;
    }
    
    if (username.length < 3) {
        errorDiv.textContent = 'Username must be at least 3 characters';
        errorDiv.style.display = 'block';
        return;
    }
    
    if (password.length < 6) {
        errorDiv.textContent = 'Password must be at least 6 characters';
        errorDiv.style.display = 'block';
        return;
    }
    
    if (isNaN(weight) || weight <= 0) {
        errorDiv.textContent = 'Please enter a valid weight';
        errorDiv.style.display = 'block';
        return;
    }
    
    if (isNaN(height) || height <= 0) {
        errorDiv.textContent = 'Please enter a valid height';
        errorDiv.style.display = 'block';
        return;
    }
    
    if (isNaN(age) || age < 10 || age > 120) {
        errorDiv.textContent = 'Please enter a valid age (10-120)';
        errorDiv.style.display = 'block';
        return;
    }
    
    const registerData = {
        email,
        username,
        password,
        weight,
        height,
        age,
        gender: document.getElementById('reg-gender').value,
        fitnessGoal: document.getElementById('reg-goal').value,
        activityLevel: document.getElementById('reg-activity').value
    };
    
    try {
        console.log('Sending registration request...');
        const data = await api.call('/auth/register', 'POST', registerData);
        console.log('Registration successful:', data);
        
        if (!data || !data.token) {
            throw new Error('Invalid response from server');
        }
        
        storage.setToken(data.token);
        storage.setUser({ id: data.userId, email: data.email, username: data.username });
        
        showDashboard();
        loadUserData();
        generateRecommendations();
    } catch (error) {
        console.error('Registration error:', error);
        errorDiv.textContent = error.message || 'Registration failed. Please try again.';
        errorDiv.style.display = 'block';
    }
}

function logout() {
    storage.removeToken();
    storage.removeUser();
    document.getElementById('auth-container').style.display = 'flex';
    document.getElementById('dashboard').style.display = 'none';
    showLogin();
}

function showDashboard() {
    document.getElementById('auth-container').style.display = 'none';
    document.getElementById('dashboard').style.display = 'flex';
}

// Navigation
function showSection(sectionId) {
    // Remove active from all sections and nav links
    document.querySelectorAll('.content-section').forEach(section => {
        section.classList.remove('active');
    });
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
    });
    
    // Add active to selected section
    const sectionEl = document.getElementById(sectionId);
    if (sectionEl) sectionEl.classList.add('active');
    
    // Add active to corresponding nav link by matching the inline onclick attribute
    const link = Array.from(document.querySelectorAll('.nav-link'))
        .find(a => (a.getAttribute('onclick') || '').includes(sectionId));
    if (link) link.classList.add('active');
    
    // Load data for specific sections
    if (sectionId === 'recommendations-section') {
        loadRecommendations();
    } else if (sectionId === 'tracking-section') {
        loadTodayTracking();
    } else if (sectionId === 'profile-section') {
        loadProfile();
    } else if (sectionId === 'progress-section') {
        loadProgress('week');
    }
}

// Load user data
async function loadUserData() {
    try {
        const user = await api.call('/user/profile');
        storage.setUser(user);
        
        document.getElementById('user-name').textContent = user.username || 'User';
        
        // Update dashboard stats
        if (user.bmi) {
            document.getElementById('user-bmi').textContent = user.bmi.toFixed(1);
            const badge = document.getElementById('bmi-category');
            badge.textContent = user.bmiCategory;
            badge.style.background = getBMIColor(user.bmiCategory);
            badge.style.color = 'white';
        }
        
        // Load dashboard statistics
        loadDashboardStats();
        
        // Load today's tracking
        loadTodayTracking();
        loadRecommendations();
    } catch (error) {
        console.error('Failed to load user data:', error);
    }
}

// Load dashboard statistics
async function loadDashboardStats() {
    try {
        const stats = await api.call('/stats/dashboard');
        
        if (!stats || stats.error) {
            console.log('No stats available yet');
            return;
        }
        
        // Update week stats in motivation box or elsewhere
        if (stats.week) {
            updateWeeklyStats(stats.week);
        }
        
        // Update achievements
        if (stats.achievements && stats.achievements.length > 0) {
            displayAchievements(stats.achievements);
        }
        
        // Display motivational message based on progress
        displayMotivationalTip(stats);
        
    } catch (error) {
        console.error('Failed to load dashboard stats:', error);
    }
}

function updateWeeklyStats(weekStats) {
    // You can add these stats to a new section or update existing elements
    // For now, we'll log them - you can customize the UI later
    console.log('Weekly Stats:', weekStats);
}

function displayAchievements(achievements) {
    const motivationBox = document.querySelector('.motivation-box');
    if (!motivationBox) return;
    
    // Add achievements section after the daily tip
    let achievementsHTML = '<div class="achievements-list" style="margin-top: 15px;">';
    achievementsHTML += '<h4 style="margin-bottom: 10px;">🏆 Your Achievements</h4>';
    
    achievements.forEach(achievement => {
        achievementsHTML += `
            <div class="achievement-item" style="display: flex; align-items: center; 
                 padding: 10px; background: var(--light); border-radius: 8px; margin-bottom: 8px;">
                <i class="fas ${achievement.icon}" style="font-size: 24px; margin-right: 12px; 
                   color: var(--accent);"></i>
                <div>
                    <strong>${achievement.title}</strong>
                    <p style="margin: 0; font-size: 0.9em; opacity: 0.8;">${achievement.description}</p>
                </div>
            </div>
        `;
    });
    
    achievementsHTML += '</div>';
    
    // Check if achievements already exist
    const existingAchievements = motivationBox.querySelector('.achievements-list');
    if (existingAchievements) {
        existingAchievements.remove();
    }
    
    motivationBox.insertAdjacentHTML('beforeend', achievementsHTML);
}

function displayMotivationalTip(stats) {
    const tips = [
        "Every workout counts! Keep pushing forward! 💪",
        "Consistency is key to success! You're doing great! 🌟",
        "Small progress is still progress! Keep going! 🔥",
        "Your dedication is inspiring! Stay focused! 🎯",
        "Believe in yourself and your journey! ✨",
        "The only bad workout is the one that didn't happen! 🏃",
        "You're stronger than you think! 💯",
        "Progress, not perfection! Keep moving forward! 🚀"
    ];
    
    // Add personalized tip based on stats
    if (stats.week && stats.week.workoutDays >= 5) {
        tips.unshift("Amazing week! You've worked out " + stats.week.workoutDays + " days! 🎉");
    } else if (stats.week && stats.week.workoutDays >= 3) {
        tips.unshift("Great progress! " + stats.week.workoutDays + " workouts this week! Keep it up! 💪");
    } else if (stats.week && stats.week.workoutDays === 0) {
        tips.unshift("Time to get moving! Start with a short workout today! 🏃");
    }
    
    const randomTip = tips[Math.floor(Math.random() * tips.length)];
    const tipElement = document.getElementById('daily-tip');
    if (tipElement) {
        tipElement.textContent = randomTip;
    }
}

function getBMIColor(category) {
    const colors = {
        'Underweight': '#f59e0b',
        'Normal weight': '#10b981',
        'Overweight': '#f59e0b',
        'Obese': '#ef4444'
    };
    return colors[category] || '#64748b';
}

// Recommendations
async function generateRecommendations() {
    console.log('Generating recommendations...');
    
    // Show loading state
    const loadingDiv = document.getElementById('recommendations-loading');
    const contentDiv = document.getElementById('recommendations-content');
    
    if (loadingDiv) {
        loadingDiv.style.display = 'block';
        loadingDiv.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Generating your personalized plan...';
    }
    if (contentDiv) {
        contentDiv.style.display = 'none';
    }
    
    try {
        const recommendations = await api.call('/recommendations/generate', 'POST');
        console.log('Recommendations generated:', recommendations);
        
        if (recommendations) {
            displayRecommendations(recommendations);
            if (loadingDiv) loadingDiv.style.display = 'none';
            if (contentDiv) contentDiv.style.display = 'block';
            
            // Show success message
            alert('🎉 Your personalized plan has been generated!');
        }
    } catch (error) {
        console.error('Failed to generate recommendations:', error);
        
        if (loadingDiv) {
            loadingDiv.innerHTML = `
                <div style="text-align: center; padding: 20px;">
                    <i class="fas fa-exclamation-circle" style="font-size: 3rem; color: var(--danger-color); margin-bottom: 15px;"></i>
                    <p style="margin-bottom: 15px;">${error.message || 'Failed to generate recommendations.'}</p>
                    <p style="font-size: 0.9rem; color: var(--gray); margin-bottom: 20px;">Make sure your profile is complete with weight, height, and age.</p>
                    <button class="btn btn-primary" onclick="generateRecommendations()">
                        <i class="fas fa-redo"></i> Try Again
                    </button>
                </div>
            `;
        }
    }
}

async function loadRecommendations() {
    const loading = document.getElementById('recommendations-loading');
    const content = document.getElementById('recommendations-content');
    
    loading.style.display = 'block';
    content.style.display = 'none';
    
    try {
        const recommendations = await api.call('/recommendations/latest');
        
        if (typeof recommendations === 'string') {
            loading.innerHTML = `
                <p>${recommendations}</p>
                <button class="btn btn-primary" onclick="generateRecommendations()">
                    Generate Recommendations
                </button>
            `;
            return;
        }
        
        displayRecommendations(recommendations);
        loading.style.display = 'none';
        content.style.display = 'block';
    } catch (error) {
        console.error('Failed to load recommendations:', error);
        loading.innerHTML = `
            <p>No recommendations found.</p>
            <button class="btn btn-primary" onclick="generateRecommendations()">
                Generate Recommendations
            </button>
        `;
    }
}

function displayRecommendations(rec) {
    // Diet Plan
    if (rec.dietPlan) {
        document.getElementById('rec-calories').textContent = rec.dietPlan.dailyCalories;
        document.getElementById('rec-protein').textContent = rec.dietPlan.protein + 'g';
        document.getElementById('rec-carbs').textContent = rec.dietPlan.carbs + 'g';
        document.getElementById('rec-fats').textContent = rec.dietPlan.fats + 'g';
        
        // Meal suggestions
        const mealsDiv = document.getElementById('meal-suggestions');
        mealsDiv.innerHTML = rec.dietPlan.meals.map(meal => `
            <div class="meal-item">
                <h5>${meal.name} (${meal.mealType})</h5>
                <p><strong>${meal.calories} calories</strong></p>
                <p>${meal.description}</p>
                <p><strong>Ingredients:</strong> ${meal.ingredients.join(', ')}</p>
            </div>
        `).join('');
        
        // Diet guidelines
        const guidelinesDiv = document.getElementById('diet-guidelines');
        guidelinesDiv.innerHTML = rec.dietPlan.guidelines.map(g => `<li>${g}</li>`).join('');
    }
    
    // Workout Plan
    if (rec.workoutPlan) {
        document.getElementById('rec-weekly-workouts').textContent = rec.workoutPlan.weeklyWorkouts;
        document.getElementById('rec-session-duration').textContent = rec.workoutPlan.sessionDuration;
        
        // Workout schedule
        const scheduleDiv = document.getElementById('workout-schedule');
        scheduleDiv.innerHTML = rec.workoutPlan.sessions.map(session => `
            <div class="workout-day">
                <h5>${session.day} - ${session.focus}</h5>
                ${session.exercises.map(ex => `
                    <div class="exercise-item">
                        <strong>${ex.name}</strong> - 
                        ${ex.sets ? `${ex.sets} sets x ${ex.reps}` : ''}
                        ${ex.duration ? `${ex.duration} min` : ''}
                        <br><small>${ex.description}</small>
                    </div>
                `).join('')}
            </div>
        `).join('');
        
        // Workout guidelines
        const workoutGuidelinesDiv = document.getElementById('workout-guidelines');
        workoutGuidelinesDiv.innerHTML = rec.workoutPlan.guidelines.map(g => `<li>${g}</li>`).join('');
    }
    
    // Tips
    if (rec.tips) {
        const tipsDiv = document.getElementById('tips-list');
        tipsDiv.innerHTML = rec.tips.map(tip => `
            <div class="tip-item">${tip}</div>
        `).join('');
        
        // Display random tip on dashboard
        const randomTip = rec.tips[Math.floor(Math.random() * rec.tips.length)];
        document.getElementById('daily-tip').textContent = randomTip;
    }
}

// Daily Tracking
async function loadTodayTracking() {
    document.getElementById('tracking-date').textContent = new Date().toLocaleDateString();
    
    try {
        const tracking = await api.call('/tracking/today');
        displayTracking(tracking);
    } catch (error) {
        console.error('Failed to load tracking:', error);
    }
}

function displayTracking(tracking) {
    // Update dashboard stats
    document.getElementById('calories-today').textContent = tracking.caloriesBurned || 0;
    document.getElementById('exercise-today').textContent = tracking.totalExerciseMinutes || 0;
    
    const waterIntake = tracking.waterIntake || 0;
    const waterGoal = tracking.waterGoal || 8;
    document.getElementById('water-today').textContent = `${Math.round(waterIntake)}/${waterGoal}`;
    
    // Display exercises
    const exerciseList = document.getElementById('exercise-list');
    if (tracking.exercises && tracking.exercises.length > 0) {
        exerciseList.innerHTML = tracking.exercises.map((ex, index) => `
            <div class="exercise-item">
                <div>
                    <strong>${ex.name}</strong> (${ex.type})<br>
                    <small>${ex.duration} min - ${ex.calories} cal</small>
                </div>
                <input type="checkbox" ${ex.completed ? 'checked' : ''} 
                       onchange="toggleExercise(${index})">
            </div>
        `).join('');
        exerciseList.className = 'tracking-list has-items';
    } else {
        exerciseList.innerHTML = '<p class="empty-text">No exercises logged today.</p>';
        exerciseList.className = 'tracking-list';
    }
    
    // Display meals
    const mealList = document.getElementById('meal-list');
    if (tracking.meals && tracking.meals.length > 0) {
        mealList.innerHTML = tracking.meals.map(meal => `
            <div class="meal-item-tracking">
                <div>
                    <strong>${meal.name}</strong> (${meal.type})<br>
                    <small>${meal.calories} cal - P: ${meal.protein}g, C: ${meal.carbs}g, F: ${meal.fats}g</small>
                </div>
            </div>
        `).join('');
        mealList.className = 'tracking-list has-items';
    } else {
        mealList.innerHTML = '<p class="empty-text">No meals logged today.</p>';
        mealList.className = 'tracking-list';
    }
    
    // Display water intake
    displayWaterCups(Math.round(waterIntake), waterGoal);
    
    // Display notes
    if (tracking.notes) {
        document.getElementById('daily-notes').value = tracking.notes;
    }
}

function displayWaterCups(filled, total) {
    const cupsDiv = document.getElementById('water-cups');
    cupsDiv.innerHTML = '';
    
    for (let i = 0; i < total; i++) {
        const cup = document.createElement('div');
        cup.className = 'water-cup';
        if (i < filled) {
            cup.classList.add('filled');
        }
        cup.onclick = () => setWaterIntake(i + 1);
        cupsDiv.appendChild(cup);
    }
    
    document.getElementById('water-count').textContent = filled;
}

async function addWater() {
    try {
        const tracking = await api.call('/tracking/today');
        const newIntake = (tracking.waterIntake || 0) + 1;
        
        await api.call('/tracking/update', 'POST', {
            waterIntake: newIntake
        });
        
        loadTodayTracking();
    } catch (error) {
        console.error('Failed to update water intake:', error);
    }
}

async function setWaterIntake(amount) {
    try {
        await api.call('/tracking/update', 'POST', {
            waterIntake: amount
        });
        
        loadTodayTracking();
    } catch (error) {
        console.error('Failed to update water intake:', error);
    }
}

function addExercise() {
    // Always open the modal (it's defined in this file now)
    openExerciseModal();
}

async function saveExercise(exercise) {
    try {
        const tracking = await api.call('/tracking/today');
        const exercises = tracking.exercises || [];
        exercises.push(exercise);
        
        await api.call('/tracking/update', 'POST', { exercises });
        loadTodayTracking();
    } catch (error) {
        console.error('Failed to save exercise:', error);
    }
}

async function toggleExercise(index) {
    try {
        const tracking = await api.call('/tracking/today');
        tracking.exercises[index].completed = !tracking.exercises[index].completed;
        
        await api.call('/tracking/update', 'POST', {
            exercises: tracking.exercises
        });
        
        loadTodayTracking();
    } catch (error) {
        console.error('Failed to update exercise:', error);
    }
}

function addMeal() {
    // Always open the modal (it's defined in this file now)
    openMealModal();
}

async function saveMeal(meal) {
    try {
        const tracking = await api.call('/tracking/today');
        const meals = tracking.meals || [];
        meals.push(meal);
        
        await api.call('/tracking/update', 'POST', { meals });
        loadTodayTracking();
    } catch (error) {
        console.error('Failed to save meal:', error);
    }
}

async function saveTracking() {
    try {
        const notes = document.getElementById('daily-notes').value;
        
        await api.call('/tracking/update', 'POST', { notes });
        
        alert('Progress saved successfully!');
        // Auto-run AI analysis after saving notes to give immediate feedback
        try {
            await analyzeToday();
        } catch (_) {}
    } catch (error) {
        console.error('Failed to save tracking:', error);
        alert('Failed to save progress');
    }
}

// AI Analysis for today's workout and meals
async function analyzeToday() {
    const analysisCard = document.getElementById('analysis-card');
    const loading = document.getElementById('analysis-loading');
    const resultDiv = document.getElementById('analysis-result');
    
    if (analysisCard) analysisCard.style.display = 'block';
    if (loading) loading.style.display = 'block';
    if (resultDiv) resultDiv.style.display = 'none';
    
    try {
        const analysis = await api.call('/analysis/today', 'POST');
        renderAnalysis(analysis);
    } catch (error) {
        console.error('Failed to analyze today:', error);
        if (resultDiv) {
            resultDiv.innerHTML = `<p style="color: var(--danger);">${error.message || 'Unable to analyze today. Log some workout or meals first.'}</p>`;
            resultDiv.style.display = 'block';
        }
    } finally {
        if (loading) loading.style.display = 'none';
    }
}

function renderAnalysis(analysis) {
    const resultDiv = document.getElementById('analysis-result');
    if (!resultDiv || !analysis) return;
    
    const scoreColor = (s) => {
        if (s >= 80) return '#10b981';
        if (s >= 60) return '#f59e0b';
        return '#ef4444';
    };
    
    resultDiv.innerHTML = `
        <div style="display:flex; align-items:center; gap:12px; margin-bottom:10px;">
            <div style="background:${scoreColor(analysis.usefulnessScore || 0)}; color:#fff; padding:8px 12px; border-radius:8px; font-weight:700;">
                Usefulness: ${analysis.usefulnessScore ?? 0}/100
            </div>
            <div style="opacity:0.8;">${analysis.usefulnessReason || ''}</div>
        </div>
        <p style="margin:8px 0 16px 0;">${analysis.summary || ''}</p>
        ${listBlock('Expected Results', analysis.expectedResults)}
        ${listBlock('What Went Well', analysis.positives)}
        ${listBlock('Improvements', analysis.improvements)}
        ${listBlock('Next Steps', analysis.nextSteps)}
    `;
    resultDiv.style.display = 'block';
}

function listBlock(title, items) {
    if (!items || !items.length) return '';
    return `
        <div style="margin-top:12px;">
            <h4 style="margin:0 0 6px 0;">${title}</h4>
            <ul style="margin:0; padding-left:18px;">
                ${items.map(i => `<li>${i}</li>`).join('')}
            </ul>
        </div>
    `;
}

// Progress
// Chart instances to destroy before recreating
let exerciseChartInstance = null;
let nutritionChartInstance = null;
let caloriesTrendChartInstance = null;
let activityDistributionChartInstance = null;

async function loadProgress(period) {
    const exerciseSummary = document.getElementById('exercise-summary');
    const nutritionSummary = document.getElementById('nutrition-summary');
    const activityHistory = document.getElementById('activity-history');
    
    // Update active button
    document.querySelectorAll('.progress-filters .btn').forEach(btn => {
        btn.classList.remove('active');
    });
    event?.target?.classList.add('active');
    
    try {
        const allTracking = await api.call('/tracking/all');
        
        if (!allTracking || allTracking.length === 0) {
            exerciseSummary.innerHTML = '<p class="empty-text">No data available yet. Start tracking!</p>';
            nutritionSummary.innerHTML = '<p class="empty-text">No data available yet.</p>';
            activityHistory.innerHTML = '<p class="empty-text">No data available yet.</p>';
            return;
        }
        
        // Filter by period
        let filteredTracking = allTracking;
        if (period === 'week') {
            const weekAgo = new Date();
            weekAgo.setDate(weekAgo.getDate() - 7);
            filteredTracking = allTracking.filter(t => new Date(t.date) >= weekAgo);
        } else if (period === 'month') {
            const monthAgo = new Date();
            monthAgo.setMonth(monthAgo.getMonth() - 1);
            filteredTracking = allTracking.filter(t => new Date(t.date) >= monthAgo);
        }
        
        // Calculate exercise summary
        const totalExerciseMinutes = filteredTracking.reduce((sum, t) => 
            sum + (t.totalExerciseMinutes || 0), 0);
        const totalCaloriesBurned = filteredTracking.reduce((sum, t) => 
            sum + (t.caloriesBurned || 0), 0);
        const daysExercised = filteredTracking.filter(t => 
            t.exercises && t.exercises.length > 0).length;
        
        // Calculate exercise types breakdown
        const exerciseTypes = { CARDIO: 0, STRENGTH: 0, FLEXIBILITY: 0, SPORTS: 0 };
        filteredTracking.forEach(t => {
            if (t.exercises) {
                t.exercises.forEach(ex => {
                    exerciseTypes[ex.type] = (exerciseTypes[ex.type] || 0) + (ex.duration || 0);
                });
            }
        });
        
        // Create Exercise Pie Chart
        createExerciseChart(exerciseTypes);
        
        exerciseSummary.innerHTML = `
            <div class="chart-stat-item">
                <div class="label">Total Time</div>
                <div class="value">${totalExerciseMinutes}<small>min</small></div>
            </div>
            <div class="chart-stat-item">
                <div class="label">Calories Burned</div>
                <div class="value">${totalCaloriesBurned}<small>cal</small></div>
            </div>
            <div class="chart-stat-item">
                <div class="label">Active Days</div>
                <div class="value">${daysExercised}<small>days</small></div>
            </div>
        `;
        
        // Calculate nutrition summary
        const totalCaloriesConsumed = filteredTracking.reduce((sum, t) => 
            sum + (t.totalCaloriesConsumed || 0), 0);
        const avgCaloriesPerDay = filteredTracking.length > 0 ? 
            Math.round(totalCaloriesConsumed / filteredTracking.length) : 0;
        
        // Calculate meal types breakdown
        const mealTypes = { BREAKFAST: 0, LUNCH: 0, DINNER: 0, SNACK: 0 };
        filteredTracking.forEach(t => {
            if (t.meals) {
                t.meals.forEach(meal => {
                    mealTypes[meal.type] = (mealTypes[meal.type] || 0) + (meal.calories || 0);
                });
            }
        });
        
        // Create Nutrition Doughnut Chart
        createNutritionChart(mealTypes);
        
        nutritionSummary.innerHTML = `
            <div class="chart-stat-item">
                <div class="label">Total Calories</div>
                <div class="value">${totalCaloriesConsumed}<small>cal</small></div>
            </div>
            <div class="chart-stat-item">
                <div class="label">Daily Average</div>
                <div class="value">${avgCaloriesPerDay}<small>cal</small></div>
            </div>
        `;
        
        // Create Calories Trend Line Chart
        createCaloriesTrendChart(filteredTracking);
        
        // Create Activity Distribution Bar Chart
        createActivityDistributionChart(filteredTracking);
        
        // Display activity history
        activityHistory.innerHTML = filteredTracking.length > 0 
            ? filteredTracking.slice(0, 10).map(t => `
                <div class="activity-item">
                    <strong><i class="fas fa-calendar-day"></i> ${new Date(t.date).toLocaleDateString('en-US', { 
                        weekday: 'short', month: 'short', day: 'numeric' 
                    })}</strong><br>
                    <div style="margin-top: 8px; display: flex; gap: 20px; flex-wrap: wrap;">
                        <span><i class="fas fa-dumbbell"></i> ${t.totalExerciseMinutes || 0} min</span>
                        <span><i class="fas fa-fire"></i> ${t.caloriesBurned || 0} burned</span>
                        <span><i class="fas fa-utensils"></i> ${t.totalCaloriesConsumed || 0} consumed</span>
                        <span><i class="fas fa-tint"></i> ${Math.round(t.waterIntake || 0)} glasses</span>
                    </div>
                </div>
            `).join('')
            : '<p class="empty-text">No activity recorded yet.</p>';
    } catch (error) {
        console.error('Failed to load progress:', error);
    }
}

function createExerciseChart(exerciseTypes) {
    const ctx = document.getElementById('exerciseChart');
    if (!ctx) return;
    
    // Destroy previous chart
    if (exerciseChartInstance) {
        exerciseChartInstance.destroy();
    }
    
    const data = Object.entries(exerciseTypes).filter(([_, value]) => value > 0);
    
    if (data.length === 0) {
        ctx.parentElement.innerHTML = '<canvas id="exerciseChart"></canvas><p class="empty-text">No exercise data</p>';
        return;
    }
    
    exerciseChartInstance = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: data.map(([type]) => type.charAt(0) + type.slice(1).toLowerCase()),
            datasets: [{
                data: data.map(([_, value]) => value),
                backgroundColor: [
                    'rgba(255, 99, 132, 0.8)',
                    'rgba(54, 162, 235, 0.8)',
                    'rgba(255, 206, 86, 0.8)',
                    'rgba(75, 192, 192, 0.8)'
                ],
                borderColor: [
                    'rgba(255, 99, 132, 1)',
                    'rgba(54, 162, 235, 1)',
                    'rgba(255, 206, 86, 1)',
                    'rgba(75, 192, 192, 1)'
                ],
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        padding: 15,
                        font: { size: 12 }
                    }
                },
                tooltip: {
                    callbacks: {
                        label: (context) => `${context.label}: ${context.parsed} minutes`
                    }
                }
            }
        }
    });
}

function createNutritionChart(mealTypes) {
    const ctx = document.getElementById('nutritionChart');
    if (!ctx) return;
    
    // Destroy previous chart
    if (nutritionChartInstance) {
        nutritionChartInstance.destroy();
    }
    
    const data = Object.entries(mealTypes).filter(([_, value]) => value > 0);
    
    if (data.length === 0) {
        ctx.parentElement.innerHTML = '<canvas id="nutritionChart"></canvas><p class="empty-text">No nutrition data</p>';
        return;
    }
    
    nutritionChartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: data.map(([type]) => type.charAt(0) + type.slice(1).toLowerCase()),
            datasets: [{
                data: data.map(([_, value]) => value),
                backgroundColor: [
                    'rgba(255, 159, 64, 0.8)',
                    'rgba(153, 102, 255, 0.8)',
                    'rgba(255, 99, 132, 0.8)',
                    'rgba(54, 162, 235, 0.8)'
                ],
                borderColor: [
                    'rgba(255, 159, 64, 1)',
                    'rgba(153, 102, 255, 1)',
                    'rgba(255, 99, 132, 1)',
                    'rgba(54, 162, 235, 1)'
                ],
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        padding: 15,
                        font: { size: 12 }
                    }
                },
                tooltip: {
                    callbacks: {
                        label: (context) => `${context.label}: ${context.parsed} cal`
                    }
                }
            }
        }
    });
}

function createCaloriesTrendChart(trackingData) {
    const ctx = document.getElementById('caloriesTrendChart');
    if (!ctx) return;
    
    // Destroy previous chart
    if (caloriesTrendChartInstance) {
        caloriesTrendChartInstance.destroy();
    }
    
    const sortedData = trackingData.sort((a, b) => new Date(a.date) - new Date(b.date));
    
    caloriesTrendChartInstance = new Chart(ctx, {
        type: 'line',
        data: {
            labels: sortedData.map(t => new Date(t.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })),
            datasets: [
                {
                    label: 'Consumed',
                    data: sortedData.map(t => t.totalCaloriesConsumed || 0),
                    borderColor: 'rgba(255, 99, 132, 1)',
                    backgroundColor: 'rgba(255, 99, 132, 0.2)',
                    borderWidth: 2,
                    tension: 0.4,
                    fill: true
                },
                {
                    label: 'Burned',
                    data: sortedData.map(t => t.caloriesBurned || 0),
                    borderColor: 'rgba(54, 162, 235, 1)',
                    backgroundColor: 'rgba(54, 162, 235, 0.2)',
                    borderWidth: 2,
                    tension: 0.4,
                    fill: true
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    position: 'top',
                    labels: {
                        padding: 15,
                        font: { size: 12 }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Calories'
                    }
                }
            }
        }
    });
}

function createActivityDistributionChart(trackingData) {
    const ctx = document.getElementById('activityDistributionChart');
    if (!ctx) return;
    
    // Destroy previous chart
    if (activityDistributionChartInstance) {
        activityDistributionChartInstance.destroy();
    }
    
    const sortedData = trackingData.sort((a, b) => new Date(a.date) - new Date(b.date)).slice(-7);
    
    activityDistributionChartInstance = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: sortedData.map(t => new Date(t.date).toLocaleDateString('en-US', { weekday: 'short' })),
            datasets: [
                {
                    label: 'Exercise (min)',
                    data: sortedData.map(t => t.totalExerciseMinutes || 0),
                    backgroundColor: 'rgba(75, 192, 192, 0.8)',
                    borderColor: 'rgba(75, 192, 192, 1)',
                    borderWidth: 2
                },
                {
                    label: 'Water (glasses)',
                    data: sortedData.map(t => Math.round(t.waterIntake || 0)),
                    backgroundColor: 'rgba(54, 162, 235, 0.8)',
                    borderColor: 'rgba(54, 162, 235, 1)',
                    borderWidth: 2
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    position: 'top',
                    labels: {
                        padding: 15,
                        font: { size: 12 }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}

// Profile
function initForms() {
    const profileForm = document.getElementById('profile-form');
    profileForm.addEventListener('submit', handleProfileUpdate);
}

async function loadProfile() {
    try {
        const user = await api.call('/user/profile');
        
        document.getElementById('profile-username').value = user.username || '';
        document.getElementById('profile-email').value = user.email || '';
        document.getElementById('profile-weight').value = user.weight || '';
        document.getElementById('profile-height').value = user.height || '';
        document.getElementById('profile-age').value = user.age || '';
        document.getElementById('profile-gender').value = user.gender || 'MALE';
        document.getElementById('profile-goal').value = user.fitnessGoal || 'MAINTENANCE';
        document.getElementById('profile-activity').value = user.activityLevel || 'MODERATE';
    } catch (error) {
        console.error('Failed to load profile:', error);
    }
}

async function handleProfileUpdate(e) {
    e.preventDefault();
    
    const updatedProfile = {
        username: document.getElementById('profile-username').value,
        weight: parseFloat(document.getElementById('profile-weight').value),
        height: parseFloat(document.getElementById('profile-height').value),
        age: parseInt(document.getElementById('profile-age').value),
        gender: document.getElementById('profile-gender').value,
        fitnessGoal: document.getElementById('profile-goal').value,
        activityLevel: document.getElementById('profile-activity').value
    };
    
    const messageDiv = document.getElementById('profile-message');
    
    try {
        await api.call('/user/profile', 'PUT', updatedProfile);
        
        messageDiv.textContent = 'Profile updated successfully!';
        messageDiv.style.display = 'block';
        
        setTimeout(() => {
            messageDiv.style.display = 'none';
        }, 3000);
        
        loadUserData();
    } catch (error) {
        console.error('Failed to update profile:', error);
        alert('Failed to update profile');
    }
}

// Utility Functions
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

// Modal Functions for Exercise and Meal Entry
function createExerciseModal() {
    if (document.getElementById('exerciseModal')) return;
    
    const modal = document.createElement('div');
    modal.id = 'exerciseModal';
    modal.className = 'simple-modal dark';
    modal.innerHTML = `
        <div class="simple-modal-backdrop" onclick="closeExerciseModal()"></div>
        <div class="simple-modal-card">
            <h3>Add Exercise</h3>
            <form id="exercise-form-modal">
                <div class="form-group">
                    <label>Exercise name</label>
                    <input id="exercise-name-modal" required />
                </div>
                <div class="form-group">
                    <label>Type</label>
                    <select id="exercise-type-modal">
                        <option>CARDIO</option>
                        <option>STRENGTH</option>
                        <option>FLEXIBILITY</option>
                        <option>SPORTS</option>
                    </select>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label>Duration (min)</label>
                        <input id="exercise-duration-modal" type="number" required />
                    </div>
                    <div class="form-group">
                        <label>Calories</label>
                        <input id="exercise-calories-modal" type="number" required />
                    </div>
                </div>
                <div style="display:flex;gap:10px;justify-content:flex-end;margin-top:12px;">
                    <button type="button" class="btn btn-secondary" onclick="closeExerciseModal()">Cancel</button>
                    <button type="submit" class="btn btn-primary">Save Exercise</button>
                </div>
            </form>
        </div>
    `;
    document.body.appendChild(modal);
    
    document.getElementById('exercise-form-modal').addEventListener('submit', async function(e) {
        e.preventDefault();
        const exercise = {
            name: document.getElementById('exercise-name-modal').value.trim(),
            type: document.getElementById('exercise-type-modal').value,
            duration: parseInt(document.getElementById('exercise-duration-modal').value) || 0,
            calories: parseInt(document.getElementById('exercise-calories-modal').value) || 0,
            completed: false
        };
        
        await saveExercise(exercise);
        closeExerciseModal();
        
        // Clear form
        document.getElementById('exercise-form-modal').reset();
    });
}

function openExerciseModal() {
    createExerciseModal();
    const modal = document.getElementById('exerciseModal');
    if (modal) modal.style.display = 'flex';
}

function closeExerciseModal() {
    const modal = document.getElementById('exerciseModal');
    if (modal) modal.style.display = 'none';
}

function createMealModal() {
    if (document.getElementById('mealModal')) return;
    
    const modal = document.createElement('div');
    modal.id = 'mealModal';
    modal.className = 'simple-modal dark';
    modal.innerHTML = `
        <div class="simple-modal-backdrop" onclick="closeMealModal()"></div>
        <div class="simple-modal-card">
            <h3>Add Meal</h3>
            <form id="meal-form-modal">
                <div class="form-group">
                    <label>Meal name</label>
                    <input id="meal-name-modal" required />
                </div>
                <div class="form-group">
                    <label>Type</label>
                    <select id="meal-type-modal">
                        <option>BREAKFAST</option>
                        <option>LUNCH</option>
                        <option>DINNER</option>
                        <option>SNACK</option>
                    </select>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label>Calories</label>
                        <input id="meal-calories-modal" type="number" required />
                    </div>
                    <div class="form-group">
                        <label>Protein (g)</label>
                        <input id="meal-protein-modal" type="number" />
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label>Carbs (g)</label>
                        <input id="meal-carbs-modal" type="number" />
                    </div>
                    <div class="form-group">
                        <label>Fats (g)</label>
                        <input id="meal-fats-modal" type="number" />
                    </div>
                </div>
                <div style="display:flex;gap:10px;justify-content:flex-end;margin-top:12px;">
                    <button type="button" class="btn btn-secondary" onclick="closeMealModal()">Cancel</button>
                    <button type="submit" class="btn btn-primary">Save Meal</button>
                </div>
            </form>
        </div>
    `;
    document.body.appendChild(modal);
    
    document.getElementById('meal-form-modal').addEventListener('submit', async function(e) {
        e.preventDefault();
        const meal = {
            name: document.getElementById('meal-name-modal').value.trim(),
            type: document.getElementById('meal-type-modal').value,
            calories: parseInt(document.getElementById('meal-calories-modal').value) || 0,
            protein: parseFloat(document.getElementById('meal-protein-modal').value) || 0,
            carbs: parseFloat(document.getElementById('meal-carbs-modal').value) || 0,
            fats: parseFloat(document.getElementById('meal-fats-modal').value) || 0
        };
        
        await saveMeal(meal);
        closeMealModal();
        
        // Clear form
        document.getElementById('meal-form-modal').reset();
    });
}

function openMealModal() {
    createMealModal();
    const modal = document.getElementById('mealModal');
    if (modal) modal.style.display = 'flex';
}

function closeMealModal() {
    const modal = document.getElementById('mealModal');
    if (modal) modal.style.display = 'none';
}

// Add modal styles if not already present
if (!document.getElementById('modal-style-app')) {
    const style = document.createElement('style');
    style.id = 'modal-style-app';
    style.innerHTML = `
        .simple-modal{display:none;position:fixed;inset:0;align-items:center;justify-content:center;z-index:9999}
        .simple-modal[style*="flex"] {display:flex !important}
        .simple-modal-backdrop{position:absolute;inset:0;background:rgba(0,0,0,0.45)}
        .simple-modal-card{background:white;padding:18px;border-radius:12px;z-index:2;min-width:320px;max-width:520px}
        .simple-modal-card h3{margin:0 0 10px}
        .simple-modal-card .form-group{margin-bottom:10px}
        .simple-modal-card .form-group label{display:block;margin-bottom:5px;font-weight:500;font-size:0.9rem}
        .simple-modal-card .form-row{display:grid;grid-template-columns:1fr 1fr;gap:12px}
        .simple-modal-card input, .simple-modal-card select{width:100%;padding:8px;border:1px solid #ddd;border-radius:8px;box-sizing:border-box}
        
        .simple-modal.dark .simple-modal-card{background: linear-gradient(180deg, #0f1724 0%, #111827 100%); color: #e6eef9; border-radius:14px; padding:20px; box-shadow: 0 20px 50px rgba(2,6,23,0.7)}
        .simple-modal.dark .simple-modal-card h3{color:#fff;margin-bottom:18px}
        .simple-modal.dark .simple-modal-card label{color:#e6eef9;font-size:0.95rem;margin-bottom:6px}
        .simple-modal.dark .simple-modal-card input{background: rgba(255,255,255,0.08); border: 1px solid rgba(255,255,255,0.12); color: #fff;padding:10px;font-size:1rem}
        .simple-modal.dark .simple-modal-card input:focus{border-color: #4ECDC4;background: rgba(255,255,255,0.12);outline:none}
        .simple-modal.dark .simple-modal-card select{
            background: #2a2a3e !important;
            border: 2px solid #444 !important;
            color: #ffffff !important;
            padding: 10px !important;
            font-size: 1rem !important;
            cursor: pointer !important;
            appearance: none !important;
            background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%23ffffff' d='M6 9L1 4h10z'/%3E%3C/svg%3E") !important;
            background-repeat: no-repeat !important;
            background-position: right 12px center !important;
            background-size: 12px !important;
            padding-right: 35px !important;
        }
        .simple-modal.dark .simple-modal-card select:hover{border-color: #4ECDC4 !important;background-color: #323248 !important}
        .simple-modal.dark .simple-modal-card select:focus{border-color: #4ECDC4 !important;outline:none;box-shadow: 0 0 0 3px rgba(78,205,196,0.2) !important}
        .simple-modal.dark .simple-modal-card select option{background: #2a2a3e !important;color: #ffffff !important;padding:12px !important}
        .simple-modal.dark .btn-secondary{background: rgba(255,255,255,0.08); color: #e6eef9; border: 1px solid rgba(255,255,255,0.12);padding:10px 20px;font-weight:600}
        .simple-modal.dark .btn-secondary:hover{background: rgba(255,255,255,0.12);border-color: rgba(255,255,255,0.2)}
        .simple-modal.dark .btn-primary{background: linear-gradient(135deg, #FF6B6B, #4ECDC4); color: #fff; border: none;padding:10px 20px;font-weight:600}
        .simple-modal.dark .btn-primary:hover{transform:translateY(-2px);box-shadow: 0 4px 12px rgba(78,205,196,0.4)}
        .simple-modal.dark .simple-modal-backdrop{background: rgba(2,6,23,0.7)}
    `;
    document.head.appendChild(style);
}
