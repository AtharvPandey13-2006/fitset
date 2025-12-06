# ProFit Connect - Fitness Assistant

## 🎯 Project Structure

This project now has a **beautiful landing page** and a **full-featured fitness tracking application**.

### Files Overview:

1. **`index.html`** - Landing page (ProFit Connect homepage)
   - Modern, professional design
   - Showcases workouts, trainers, pricing
   - All CTAs link to `auth.html`

2. **`auth.html`** - Authentication & Dashboard
   - User registration and login
   - Personal fitness dashboard
   - AI-powered recommendations
   - Progress tracking
   - Requires backend connection

3. **`style.css`** - Landing page styles
   - Dark theme design
   - Responsive layout
   - Smooth animations

4. **`css/style.css`** - Dashboard app styles
   - Authentication forms
   - Dashboard components

5. **`js/landing.js`** - Landing page interactions
   - Workout filtering
   - Smooth scrolling
   - Scroll animations

6. **`js/app.js`** - Main application logic
   - API client
   - Authentication
   - Dashboard functionality

## 🚀 Usage

### For Users:
1. **Browse**: Open `index.html` to view the landing page
2. **Sign Up**: Click any "Start Free Trial" or "Get Started" button
3. **Register**: Complete registration form with your details
4. **Track**: Use the dashboard to track workouts, meals, and progress

### For Development:
1. **Backend**: Start Spring Boot backend (`FitSetApplication.java`)
2. **MongoDB**: Fix MongoDB Atlas connection (see below)
3. **Frontend**: Open `index.html` in Live Server or browser

## 🔧 MongoDB Atlas Connection Fix

The backend is timing out because it cannot connect to MongoDB Atlas. To fix:

### Option 1: Whitelist Your IP (Recommended)
1. Go to [MongoDB Atlas Dashboard](https://cloud.mongodb.com/)
2. Navigate to **Network Access** → **IP Whitelist**
3. Click "Add IP Address"
4. Add your current IP or `0.0.0.0/0` for all IPs (testing only)
5. Save and wait 1-2 minutes

### Option 2: Check Connection String
1. Open `backend/src/main/resources/application.properties`
2. Verify MongoDB URI has correct:
   - Username
   - Password (no special characters unescaped)
   - Cluster URL
   - Database name

### Option 3: Test Connection
```bash
# Install MongoDB Compass or mongosh
# Try connecting with your connection string
mongosh "mongodb+srv://yourcluster.mongodb.net/fitset" --username youruser
```

### Option 4: Check Firewall
- Disable Windows Firewall temporarily
- Check antivirus settings
- Try from a different network

## 📁 File Locations

```
frontend/
├── index.html          # Landing page (NEW)
├── auth.html           # Authentication & Dashboard app
├── style.css           # Landing page styles
├── landing.html        # Backup landing page
├── landing.css         # Backup landing styles
├── css/
│   └── style.css       # Dashboard app styles
└── js/
    ├── app.js          # Main app logic
    └── landing.js      # Landing page interactions
```

## 🎨 Features

### Landing Page (`index.html`)
- ✅ Hero section with CTA
- ✅ Feature showcase
- ✅ Workout programs with filtering
- ✅ Expert trainers (Shivam, Bhoomika, Shilpa, Akriti)
- ✅ Progress tracking preview
- ✅ Pricing plans (Basic, Pro, Elite)
- ✅ Contact footer
- ✅ Smooth animations
- ✅ Mobile responsive

### Dashboard App (`auth.html`)
- ✅ User registration with fitness profile
- ✅ Login authentication
- ✅ BMI calculation
- ✅ AI-powered workout recommendations (Gemini API)
- ✅ AI-powered nutrition plans
- ✅ Daily activity tracking
- ✅ Progress analytics
- ✅ Profile management

## 🔗 Flow

1. User visits `index.html` (landing page)
2. User clicks "Start Free Trial" → redirects to `auth.html`
3. User registers/logs in
4. User accesses personalized dashboard
5. Backend generates AI recommendations via Gemini API
6. User tracks daily activities
7. System monitors progress over time

## 🛠️ Next Steps

1. **Fix MongoDB Connection** (critical!)
   - Whitelist IP in Atlas
   - Verify connection string
   - Test with MongoDB Compass

2. **Start Backend**
   - Click play button in Spring Boot Dashboard
   - Backend will run on `http://localhost:8080`

3. **Test Registration**
   - Open `auth.html`
   - Register a new account
   - Verify data is saved to MongoDB

4. **Test AI Features**
   - Generate workout recommendations
   - Check Gemini API integration
   - Verify nutrition plans

## 📞 Support

- **Email**: support@ProFit.com
- **Phone**: 8708656580
- **Available**: India

---

**Built with ❤️ by the ProFit Connect Team**

Transform your life, one workout at a time! 💪
