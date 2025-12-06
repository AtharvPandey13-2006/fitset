# FitSet Backend - Quick Start Guide

## Prerequisites Checklist
- [ ] Java 17 or higher installed
- [ ] Maven installed
- [ ] MongoDB installed and running

## Quick Start Steps

### 1. Start MongoDB
```powershell
# Start MongoDB service
net start MongoDB

# OR if installed via MongoDB Compass, it starts automatically
```

### 2. Build the Backend
```powershell
# Navigate to backend directory
cd backend

# Clean and build
mvn clean install
```

### 3. Run the Application
```powershell
# Run Spring Boot application
mvn spring-boot:run

# OR if you have the JAR built:
java -jar target/fitness-assistant-1.0.0.jar
```

### 4. Verify Backend is Running
- Backend API: http://localhost:8080
- Check health: Open browser and try accessing http://localhost:8080/api/auth/login
- You should see a response (even if it's an error, it means the server is running)

## Default Configuration

- **Server Port**: 8080
- **MongoDB**: mongodb://localhost:27017/fitset
- **Database**: fitset

## Testing the API

You can test using curl or Postman:

```powershell
# Test registration endpoint
curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"test@example.com\",\"username\":\"testuser\",\"password\":\"password123\",\"weight\":70,\"height\":175,\"age\":25,\"gender\":\"MALE\",\"fitnessGoal\":\"MAINTENANCE\",\"activityLevel\":\"MODERATE\"}"
```

## Common Issues

### Issue: Port 8080 already in use
```powershell
# Find process using port 8080
netstat -ano | findstr :8080

# Kill the process (replace PID with actual process ID)
taskkill /PID <PID> /F
```

### Issue: MongoDB connection failed
- Ensure MongoDB is running: `net start MongoDB`
- Check MongoDB Compass or run: `mongo --version`

### Issue: Build failed
```powershell
# Clear Maven cache and rebuild
mvn clean
mvn install -U
```

## API Endpoints Summary

| Endpoint | Method | Description |
|----------|--------|-------------|
| /api/auth/register | POST | Register new user |
| /api/auth/login | POST | Login user |
| /api/user/profile | GET | Get user profile |
| /api/recommendations/generate | POST | Generate recommendations |
| /api/tracking/today | GET | Get today's tracking |

## Next Steps

1. ✅ Start the backend server
2. ✅ Open frontend (index.html)
3. ✅ Register a new account
4. ✅ Start tracking your fitness!

## Development Mode

For development with auto-reload:
```powershell
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=true"
```

## Production Considerations

Before deploying to production:
1. Change JWT secret in application.properties
2. Use environment variables for sensitive data
3. Enable HTTPS
4. Configure MongoDB authentication
5. Set up proper CORS origins
6. Use production-grade logging
