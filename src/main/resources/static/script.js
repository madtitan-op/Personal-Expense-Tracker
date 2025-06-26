// Global variables
let currentUser = null;
let authToken = null;
let expenses = [];
let categories = [];

// API Configuration
const API_BASE_URL = 'http://localhost:8080';

// DOM Elements
const loadingScreen = document.getElementById('loading-screen');
const authSection = document.getElementById('auth-section');
const app = document.getElementById('app');
const loginForm = document.getElementById('login-form');
const registerForm = document.getElementById('register-form');
const logoutBtn = document.getElementById('logout-btn');
const userInfo = document.getElementById('user-info');

// Navigation
const navItems = document.querySelectorAll('.nav-item');
const sections = document.querySelectorAll('.section');

// Modals
const expenseModal = document.getElementById('expense-modal');
const categoryModal = document.getElementById('category-modal');
const confirmModal = document.getElementById('confirm-modal');

// Charts
let categoryBreakdownChart = null;
let monthlyTrendChart = null;
let dashboardCategoryChart = null;

// Initialize application
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
    setupEventListeners();
});

// Initialize application
function initializeApp() {
    // Check if user is already logged in
    const token = localStorage.getItem('authToken');
    const user = localStorage.getItem('currentUser');
    
    if (token && user) {
        authToken = token;
        currentUser = JSON.parse(user);
        showApp();
        // Always hide loading, even if dashboard fails
        loadDashboard().catch(() => {}).finally(() => hideLoading());
    } else {
        hideLoading();
    }
}

// Setup event listeners
function setupEventListeners() {
    // Authentication
    document.getElementById('show-register').addEventListener('click', showRegisterForm);
    document.getElementById('show-login').addEventListener('click', showLoginForm);
    loginForm.addEventListener('submit', handleLogin);
    registerForm.addEventListener('submit', handleRegister);
    logoutBtn.addEventListener('click', handleLogout);

    // Navigation
    navItems.forEach(item => {
        item.addEventListener('click', () => {
            const section = item.dataset.section;
            switchSection(section);
        });
    });

    // Buttons
    document.getElementById('add-expense-btn').addEventListener('click', () => openExpenseModal());
    document.getElementById('add-category-btn').addEventListener('click', () => openCategoryModal());
    document.getElementById('apply-filters').addEventListener('click', applyFilters);
    document.getElementById('clear-filters').addEventListener('click', clearFilters);

    // Forms
    document.getElementById('expense-form').addEventListener('submit', handleExpenseSubmit);
    document.getElementById('category-form').addEventListener('submit', handleCategorySubmit);

    // Modal close events
    document.querySelectorAll('.modal-close, .modal-cancel').forEach(btn => {
        btn.addEventListener('click', closeAllModals);
    });

    // Close modals when clicking outside
    window.addEventListener('click', (e) => {
        if (e.target.classList.contains('modal')) {
            closeAllModals();
        }
    });
}

// Authentication Functions
function showLoginForm(e) {
    e.preventDefault();
    loginForm.classList.remove('hidden');
    registerForm.classList.add('hidden');
}

function showRegisterForm(e) {
    e.preventDefault();
    registerForm.classList.remove('hidden');
    loginForm.classList.add('hidden');
}

async function handleLogin(e) {
    e.preventDefault();
    
    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;

    try {
        showLoading();
        
        const loginData = { username, password };
        
        const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(loginData)
        });

        if (response.ok) {
            const responseText = await response.text();
            
            // Try to parse as JSON first, then fallback to text
            let token;
            try {
                const jsonResponse = JSON.parse(responseText);
                
                // Check if token is in a specific field
                if (jsonResponse.token) {
                    token = jsonResponse.token;
                } else if (jsonResponse.jwt) {
                    token = jsonResponse.jwt;
                } else if (jsonResponse.accessToken) {
                    token = jsonResponse.accessToken;
                } else {
                    // If it's a simple object with the token as the value
                    const keys = Object.keys(jsonResponse);
                    if (keys.length === 1) {
                        token = jsonResponse[keys[0]];
                    } else {
                        showToast('Invalid response format from server', 'error');
                        return;
                    }
                }
            } catch (parseError) {
                token = responseText.trim();
            }
            
            if (!token) {
                showToast('No token received from server', 'error');
                return;
            }
            
            authToken = token;
            currentUser = { username };
            
            // Store in localStorage
            localStorage.setItem('authToken', token);
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            
            showToast('Login successful!', 'success');
            showApp();
            loadDashboard();
        } else {
            const error = await response.text();
            showToast(error || 'Login failed', 'error');
        }
    } catch (error) {
        showToast('Network error. Please try again.', 'error');
    } finally {
        hideLoading();
    }
}

async function handleRegister(e) {
    e.preventDefault();
    
    const username = document.getElementById('register-username').value;
    const password = document.getElementById('register-password').value;
    const role = document.getElementById('register-role').value;

    try {
        showLoading();
        const response = await fetch(`${API_BASE_URL}/api/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password, role })
        });

        if (response.ok) {
            showToast('Registration successful! Please login.', 'success');
            showLoginForm(e);
            registerForm.reset();
        } else {
            const error = await response.text();
            showToast(error || 'Registration failed', 'error');
        }
    } catch (error) {
        showToast('Network error. Please try again.', 'error');
    } finally {
        hideLoading();
    }
}

function handleLogout() {
    authToken = null;
    currentUser = null;
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
    showAuth();
    showToast('Logged out successfully', 'info');
}

// Navigation Functions
function switchSection(sectionName) {
    // Update navigation
    navItems.forEach(item => {
        item.classList.remove('active');
        if (item.dataset.section === sectionName) {
            item.classList.add('active');
        }
    });

    // Update sections
    sections.forEach(section => {
        section.classList.remove('active');
        if (section.id === sectionName) {
            section.classList.add('active');
        }
    });

    // Load section data
    switch (sectionName) {
        case 'dashboard':
            loadDashboard();
            break;
        case 'expenses':
            loadExpenses();
            break;
        case 'categories':
            loadCategories();
            break;
        case 'analytics':
            loadAnalytics();
            break;
    }
}

// API Helper Functions
async function apiRequest(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    const config = {
        headers: {
            'Content-Type': 'application/json',
            ...options.headers
        },
        ...options
    };

    if (authToken) {
        config.headers['Authorization'] = `Bearer ${authToken}`;
    }

    try {
        const response = await fetch(url, config);
        
        if (response.status === 401) {
            handleLogout();
            return null;
        }
        
        return response;
    } catch (error) {
        throw error;
    }
}

// Dashboard Functions
async function loadDashboard() {
    try {
        await Promise.all([
            loadExpenses(),
            loadCategories()
        ]);
        
        updateDashboardStats();
        updateCategoryChart();
        updateRecentExpenses();
    } catch (error) {
        showToast('Error loading dashboard', 'error');
    } finally {
        hideLoading();
    }
}

function formatCurrency(amount) {
    return `₹${amount.toFixed(2)}`;
}

function updateDashboardStats() {
    const totalExpenses = expenses.reduce((sum, expense) => sum + expense.amount, 0);
    const currentMonth = new Date().getMonth();
    const currentYear = new Date().getFullYear();
    const monthlyExpenses = expenses.filter(expense => {
        const expenseDate = new Date(expense.date);
        return expenseDate.getMonth() === currentMonth && expenseDate.getFullYear() === currentYear;
    }).reduce((sum, expense) => sum + expense.amount, 0);
    
    const recentExpenses = expenses.slice(0, 5);

    document.getElementById('total-expenses').textContent = formatCurrency(totalExpenses);
    document.getElementById('monthly-expenses').textContent = formatCurrency(monthlyExpenses);
    document.getElementById('total-categories').textContent = categories.length;
    document.getElementById('recent-count').textContent = recentExpenses.length;
}

function updateCategoryChart() {
    const ctx = document.getElementById('dashboard-category-chart').getContext('2d');
    const categoryTotals = {};
    expenses.forEach(expense => {
        const category = categories.find(c => c.id === expense.categoryId);
        if (category) {
            categoryTotals[category.name] = (categoryTotals[category.name] || 0) + expense.amount;
        }
    });
    const labels = Object.keys(categoryTotals);
    const data = Object.values(categoryTotals);
    if (dashboardCategoryChart) {
        dashboardCategoryChart.destroy();
    }
    if (labels.length === 0) {
        // Clear chart area and show message
        document.getElementById('dashboard-category-chart').parentElement.innerHTML += '<p style="margin-top:1em;">No expenses to display</p>';
        return;
    }
    dashboardCategoryChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Total Spent',
                data: data,
                backgroundColor: 'rgba(102, 126, 234, 0.7)',
                borderColor: 'rgba(102, 126, 234, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { display: false },
                title: { display: false },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return `₹${context.parsed.y.toFixed(2)}`;
                        }
                    }
                }
            },
            scales: {
                y: { beginAtZero: true }
            }
        }
    });
}

function updateRecentExpenses() {
    const recentContainer = document.getElementById('recent-expenses');
    const recentExpenses = expenses.slice(0, 5);

    if (recentExpenses.length === 0) {
        recentContainer.innerHTML = '<p>No recent expenses</p>';
        return;
    }

    const recentHTML = recentExpenses.map(expense => {
        const category = categories.find(c => c.id === expense.categoryId);
        return `
            <div class="recent-item">
                <div>
                    <strong>${expense.description}</strong>
                    <br>
                    <small>${category ? category.name : 'Unknown'} • ${new Date(expense.date).toLocaleDateString()}</small>
                </div>
                <span>${formatCurrency(expense.amount)}</span>
            </div>
        `;
    }).join('');
    
    recentContainer.innerHTML = recentHTML;
}

// Expense Functions
async function loadExpenses() {
    try {
        const response = await apiRequest('/api/expenses');
        if (response && response.ok) {
            expenses = await response.json();
            renderExpensesTable();
        }
    } catch (error) {
        showToast('Error loading expenses', 'error');
    }
}

function renderExpensesTable() {
    const tbody = document.getElementById('expenses-tbody');
    
    if (expenses.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align: center;">No expenses found</td></tr>';
        return;
    }

    const rows = expenses.map(expense => {
        const category = categories.find(c => c.id === expense.categoryId);
        return `
            <tr>
                <td>${new Date(expense.date).toLocaleDateString()}</td>
                <td>${expense.description}</td>
                <td>${category ? category.name : 'Unknown'}</td>
                <td>${formatCurrency(expense.amount)}</td>
                <td class="action-buttons">
                    <button class="btn btn-small btn-secondary" onclick="editExpense(${expense.id})">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-small btn-danger" onclick="deleteExpense(${expense.id})">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            </tr>
        `;
    }).join('');
    
    tbody.innerHTML = rows;
}

function openExpenseModal(expense = null) {
    const modal = document.getElementById('expense-modal');
    const title = document.getElementById('expense-modal-title');
    const form = document.getElementById('expense-form');
    
    title.textContent = expense ? 'Edit Expense' : 'Add Expense';
    form.dataset.expenseId = expense ? expense.id : '';
    
    if (expense) {
        document.getElementById('expense-amount').value = expense.amount;
        document.getElementById('expense-description').value = expense.description;
        document.getElementById('expense-date').value = expense.date;
        document.getElementById('expense-category').value = expense.categoryId;
    } else {
        form.reset();
        document.getElementById('expense-date').value = new Date().toISOString().split('T')[0];
    }
    
    populateCategorySelect();
    modal.style.display = 'block';
}

async function handleExpenseSubmit(e) {
    e.preventDefault();
    
    const expenseId = e.target.dataset.expenseId;
    const expenseData = {
        amount: parseFloat(document.getElementById('expense-amount').value),
        description: document.getElementById('expense-description').value,
        date: document.getElementById('expense-date').value,
        categoryId: parseInt(document.getElementById('expense-category').value)
    };

    try {
        showLoading();
        let response;
        
        if (expenseId) {
            // Update existing expense
            response = await apiRequest(`/api/expenses/${expenseId}`, {
                method: 'PUT',
                body: JSON.stringify({ ...expenseData, id: parseInt(expenseId) })
            });
        } else {
            // Create new expense
            response = await apiRequest('/api/expenses', {
                method: 'POST',
                body: JSON.stringify(expenseData)
            });
        }

        if (response && response.ok) {
            showToast(expenseId ? 'Expense updated successfully!' : 'Expense created successfully!', 'success');
            closeAllModals();
            loadExpenses();
            if (document.getElementById('dashboard').classList.contains('active')) {
                loadDashboard();
            }
        } else {
            const error = await response.text();
            showToast(error || 'Operation failed', 'error');
        }
    } catch (error) {
        showToast('Network error. Please try again.', 'error');
    } finally {
        hideLoading();
    }
}

async function editExpense(id) {
    const expense = expenses.find(e => e.id === id);
    if (expense) {
        openExpenseModal(expense);
    }
}

async function deleteExpense(id) {
    showConfirmModal(
        'Are you sure you want to delete this expense?',
        async () => {
            try {
                showLoading();
                const response = await apiRequest(`/api/expenses/${id}`, {
                    method: 'DELETE'
                });

                if (response && response.ok) {
                    showToast('Expense deleted successfully!', 'success');
                    loadExpenses();
                    if (document.getElementById('dashboard').classList.contains('active')) {
                        loadDashboard();
                    }
                } else {
                    const error = await response.text();
                    showToast(error || 'Delete failed', 'error');
                }
            } catch (error) {
                showToast('Network error. Please try again.', 'error');
            } finally {
                hideLoading();
            }
        }
    );
}

// Category Functions
async function loadCategories() {
    try {
        const response = await apiRequest('/api/category/all');
        if (response && response.ok) {
            categories = await response.json();
            renderCategoriesGrid();
            populateCategorySelect();
            populateCategoryFilter();
        }
    } catch (error) {
        showToast('Error loading categories', 'error');
    }
}

function renderCategoriesGrid() {
    const grid = document.getElementById('categories-grid');
    
    if (categories.length === 0) {
        grid.innerHTML = '<p style="grid-column: 1 / -1; text-align: center;">No categories found</p>';
        return;
    }

    const categoryCards = categories.map(category => `
        <div class="category-card">
            <div class="category-header">
                <span class="category-name">${category.name}</span>
                <div class="category-actions">
                    <button class="btn btn-small btn-secondary" onclick="editCategory(${category.id})">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-small btn-danger" onclick="deleteCategory(${category.id})">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>
            <div class="category-total" id="category-total-${category.id}">${formatCurrency(0)}</div>
        </div>
    `).join('');
    
    grid.innerHTML = categoryCards;
    
    // Load category totals
    categories.forEach(category => {
        loadCategoryTotal(category.id);
    });
}

async function loadCategoryTotal(categoryId) {
    try {
        const response = await apiRequest(`/api/expenses/total/category/${categoryId}`);
        if (response && response.ok) {
            const total = await response.json();
            const element = document.getElementById(`category-total-${categoryId}`);
            if (element) {
                element.textContent = formatCurrency(total);
            }
        }
    } catch (error) {
        // Silently handle category total loading errors
    }
}

function populateCategorySelect() {
    const select = document.getElementById('expense-category');
    select.innerHTML = '<option value="">Select Category</option>' +
        categories.map(category => `<option value="${category.id}">${category.name}</option>`).join('');
}

function populateCategoryFilter() {
    const select = document.getElementById('category-filter');
    select.innerHTML = '<option value="">All Categories</option>' +
        categories.map(category => `<option value="${category.id}">${category.name}</option>`).join('');
}

function openCategoryModal(category = null) {
    const modal = document.getElementById('category-modal');
    const title = document.getElementById('category-modal-title');
    const form = document.getElementById('category-form');
    
    title.textContent = category ? 'Edit Category' : 'Add Category';
    form.dataset.categoryId = category ? category.id : '';
    
    if (category) {
        document.getElementById('category-name').value = category.name;
    } else {
        form.reset();
    }
    
    modal.style.display = 'block';
}

async function handleCategorySubmit(e) {
    e.preventDefault();
    
    const categoryId = e.target.dataset.categoryId;
    const categoryData = {
        name: document.getElementById('category-name').value
    };

    try {
        showLoading();
        let response;
        
        if (categoryId) {
            // Update existing category
            response = await apiRequest(`/api/category/${categoryId}`, {
                method: 'PUT',
                body: JSON.stringify({ ...categoryData, id: parseInt(categoryId) })
            });
        } else {
            // Create new category
            response = await apiRequest('/api/category/add', {
                method: 'POST',
                body: JSON.stringify(categoryData)
            });
        }

        if (response && response.ok) {
            showToast(categoryId ? 'Category updated successfully!' : 'Category created successfully!', 'success');
            closeAllModals();
            loadCategories();
        } else {
            const error = await response.text();
            showToast(error || 'Operation failed', 'error');
        }
    } catch (error) {
        showToast('Network error. Please try again.', 'error');
    } finally {
        hideLoading();
    }
}

async function editCategory(id) {
    const category = categories.find(c => c.id === id);
    if (category) {
        openCategoryModal(category);
    }
}

async function deleteCategory(id) {
    showConfirmModal(
        'Are you sure you want to delete this category? This will also delete all associated expenses.',
        async () => {
            try {
                showLoading();
                const response = await apiRequest(`/api/category/${id}`, { method: 'DELETE' });
                if (response && (response.ok || response.status === 204 || response.status === 200)) {
                    // Remove the deleted category from the categories array
                    categories = categories.filter(c => c.id !== id);
                    renderCategoriesGrid();
                    showToast('Category deleted successfully', 'success');
                } else {
                    let errorMsg = 'Failed to delete category';
                    try {
                        const errorText = await response.text();
                        if (errorText) errorMsg = errorText;
                    } catch {}
                    showToast(errorMsg, 'error');
                }
            } catch (error) {
                showToast('Network error. Please try again.', 'error');
            } finally {
                hideLoading();
                closeAllModals();
            }
        }
    );
}

// Filter Functions
async function applyFilters() {
    const categoryId = document.getElementById('category-filter').value;
    const dateFrom = document.getElementById('date-from').value;
    const dateTo = document.getElementById('date-to').value;

    try {
        showLoading();
        let url = '/api/expenses';
        
        if (dateFrom && dateTo) {
            url = `/api/expenses/date-range?start=${dateFrom}&end=${dateTo}`;
        } else if (categoryId) {
            url = `/api/expenses/category/${categoryId}`;
        }

        const response = await apiRequest(url);
        if (response && response.ok) {
            expenses = await response.json();
            renderExpensesTable();
            showToast('Filters applied successfully', 'success');
        }
    } catch (error) {
        showToast('Error applying filters', 'error');
    } finally {
        hideLoading();
    }
}

function clearFilters() {
    document.getElementById('category-filter').value = '';
    document.getElementById('date-from').value = '';
    document.getElementById('date-to').value = '';
    loadExpenses();
    showToast('Filters cleared', 'info');
}

// Analytics Functions
async function loadAnalytics() {
    try {
        await Promise.all([
            loadExpenses(),
            loadCategories()
        ]);
        
        updateCategoryBreakdown();
        updateMonthlyTrend();
    } catch (error) {
        showToast('Error loading analytics', 'error');
    }
}

function updateCategoryBreakdown() {
    const ctx = document.getElementById('category-breakdown-chart').getContext('2d');
    const categoryTotals = {};
    expenses.forEach(expense => {
        const category = categories.find(c => c.id === expense.categoryId);
        if (category) {
            categoryTotals[category.name] = (categoryTotals[category.name] || 0) + expense.amount;
        }
    });
    const labels = Object.keys(categoryTotals);
    const data = Object.values(categoryTotals);
    if (categoryBreakdownChart) {
        categoryBreakdownChart.destroy();
    }
    categoryBreakdownChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Total Spent',
                data: data,
                backgroundColor: 'rgba(102, 126, 234, 0.7)',
                borderColor: 'rgba(102, 126, 234, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { display: false },
                title: { display: false },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return `₹${context.parsed.y.toFixed(2)}`;
                        }
                    }
                }
            },
            scales: {
                y: { beginAtZero: true }
            }
        }
    });
}

function updateMonthlyTrend() {
    const ctx = document.getElementById('monthly-trend-chart').getContext('2d');
    const monthlyData = {};
    expenses.forEach(expense => {
        const date = new Date(expense.date);
        const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
        monthlyData[monthKey] = (monthlyData[monthKey] || 0) + expense.amount;
    });
    const labels = Object.keys(monthlyData).sort();
    const data = labels.map(month => monthlyData[month]);
    if (monthlyTrendChart) {
        monthlyTrendChart.destroy();
    }
    monthlyTrendChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Monthly Expenses',
                data: data,
                fill: false,
                borderColor: 'rgba(118, 75, 162, 1)',
                backgroundColor: 'rgba(118, 75, 162, 0.2)',
                tension: 0.3
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { display: false },
                title: { display: false },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return `₹${context.parsed.y.toFixed(2)}`;
                        }
                    }
                }
            },
            scales: {
                y: { beginAtZero: true }
            }
        }
    });
}

// Utility Functions
function showApp() {
    authSection.classList.add('hidden');
    app.classList.remove('hidden');
    userInfo.textContent = `Welcome, ${currentUser.username}!`;
}

function showAuth() {
    app.classList.add('hidden');
    authSection.classList.remove('hidden');
    loginForm.classList.remove('hidden');
    registerForm.classList.add('hidden');
}

function showLoading() {
    loadingScreen.style.display = 'flex';
}

function hideLoading() {
    loadingScreen.style.display = 'none';
}

function closeAllModals() {
    document.querySelectorAll('.modal').forEach(modal => {
        modal.style.display = 'none';
    });
}

function showConfirmModal(message, onConfirm) {
    const modal = document.getElementById('confirm-modal');
    const messageEl = document.getElementById('confirm-message');
    const confirmBtn = document.getElementById('confirm-action');
    
    messageEl.textContent = message;
    modal.style.display = 'block';
    // Remove any previous handler to prevent stacking
    confirmBtn.onclick = null;
    confirmBtn.onclick = () => {
        onConfirm();
        closeAllModals();
    };
}

function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    const icon = type === 'success' ? 'check-circle' : 
                 type === 'error' ? 'exclamation-circle' : 'info-circle';
    
    toast.innerHTML = `
        <i class="fas fa-${icon}"></i>
        <span>${message}</span>
    `;
    
    container.appendChild(toast);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        if (toast.parentNode) {
            toast.parentNode.removeChild(toast);
        }
    }, 5000);
}

// Handle page visibility change to refresh data when user returns
document.addEventListener('visibilitychange', () => {
    if (!document.hidden && authToken) {
        // Refresh current section data
        const activeSection = document.querySelector('.section.active');
        if (activeSection) {
            switch (activeSection.id) {
                case 'dashboard':
                    loadDashboard();
                    break;
                case 'expenses':
                    loadExpenses();
                    break;
                case 'categories':
                    loadCategories();
                    break;
                case 'analytics':
                    loadAnalytics();
                    break;
            }
        }
    }
});