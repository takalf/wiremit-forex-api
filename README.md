# Wiremit Forex API

A comprehensive Forex trading API built with Spring Boot, providing real-time exchange rates, currency pair management, and secure authentication.

## Features

- 🔐 **JWT Authentication** - Secure user registration and login
- 💱 **Real-time Forex Rates** - Live currency exchange rates
- 📊 **Currency Pair Management** - CRUD operations for currency pairs
- 📚 **API Documentation** - Interactive Swagger UI
- 🛡️ **Security** - Role-based access control

## Tech Stack

- **Java 17**
- **Spring Boot 3.x**
- **Spring Security** with JWT
- **MySQL** database
- **Maven** build tool
- **Swagger/OpenAPI** documentation

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- MySQL 8.0+

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/wiremit-forex.git
   cd wiremit-forex
   ```

2. **Setup MySQL database**
   ```sql
   CREATE DATABASE forex;
   CREATE USER 'root'@'localhost' IDENTIFIED BY 'your_password';
   GRANT ALL PRIVILEGES ON forex.* TO 'root'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. **Configure environment variables**
   Create a `.env` file or set environment variables with your actual values:
   ```bash
   DB_PASSWORD=your_actual_password
   JWT_SECRET=your_jwt_secret_key_here
   EXCHANGERATE_API_KEY=your_exchangerate_api_key
   FIXER_API_KEY=your_fixer_api_key
   OPENEXCHANGERATES_API_KEY=your_openexchangerates_api_key
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

5. **Access API Documentation**
    - Swagger UI: http://localhost:8005/swagger-ui.html

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Refresh JWT token
- `POST /api/auth/change-password` - Change password (authenticated)
- `GET /api/auth/account-status` - Get account status (authenticated)

### Currency Pairs
- `GET /api/v1/currency-pairs` - Get all currency pairs
- `GET /api/v1/currency-pairs/active` - Get active currency pairs
- `GET /api/v1/currency-pairs/{id}` - Get currency pair by ID
- `POST /api/v1/currency-pairs` - Create currency pair
- `PUT /api/v1/currency-pairs/{id}` - Update currency pair
- `PATCH /api/v1/currency-pairs/{id}/activate` - Activate currency pair
- `PATCH /api/v1/currency-pairs/{id}/deactivate` - Deactivate currency pair
- `DELETE /api/v1/currency-pairs/{id}` - Delete currency pair

### Forex Rates
- `GET /api/v1/forex-rates/latest` - Get all latest rates (authenticated)
- `GET /api/v1/forex-rates/latest/{pairCode}` - Get latest rate by pair code
- `GET /api/v1/forex-rates/latest/{base}/{target}` - Get latest rate by currencies
- `POST /api/v1/forex-rates/latest/batch` - Get multiple rates
- `GET /api/v1/forex-rates/history/{pairCode}` - Get rate history

## Configuration

### Environment Variables
Create a `.env` file in the root directory or set these environment variables:
```bash
# Database
DB_URL=jdbc:mysql://localhost:3306/forex?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your_jwt_secret_key_here
JWT_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=86400000

# External API Keys
EXCHANGERATE_API_KEY=your_exchangerate_api_key
FIXER_API_KEY=your_fixer_api_key
OPENEXCHANGERATES_API_KEY=your_openexchangerates_api_key

# Server
SERVER_PORT=8005
```

### Application Properties
The application uses environment variables for sensitive configuration. Key settings include:
- Database connection details
- JWT secret and expiration times
- External API keys for forex rate providers
- Server port configuration

## Development

### Building for Production
```bash
# Build JAR
mvn clean package

# Run with production profile
java -jar target/wiremit-forex-*.jar --spring.profiles.active=prod
```

## API Documentation

Complete API documentation is available through Swagger UI at:
- http://localhost:8005/swagger-ui.html

The interactive documentation provides:
- Detailed endpoint descriptions
- Request/response schemas
- Try-it-out functionality
- Authentication examples

## Error Handling

The API uses JWT (JSON Web Tokens) for authentication. To access protected endpoints:

1. Register a new account via `/api/auth/register`
2. Login via `/api/auth/login` to receive a JWT token
3. Include the token in the Authorization header: `Bearer <your-jwt-token>`

## External API Integration

The application integrates with multiple foreign exchange rate providers:

- **ExchangeRate-API** - Primary exchange rate provider
- **Fixer.io** - Alternative exchange rate source
- **Open Exchange Rates** - Additional rate data source

To use these services, you'll need to obtain API keys from each provider and set them in your environment variables.

The API returns standard HTTP status codes and structured error responses:
```json
{
  "timestamp": "2023-12-07T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid currency pair format",
  "path": "/api/v1/currency-pairs"
}
```

