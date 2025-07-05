# OSM Enhanced Logging System Guide

## Overview

The OSM project now includes a comprehensive logging system that provides detailed exception handling, performance monitoring, and structured logging with pinpoint accuracy for debugging issues.

## Key Features

### 1. **Detailed Exception Logging**
- Pinpoints exact line where exceptions occur
- Provides correlation IDs for request tracking
- Includes full stack trace analysis
- Categorizes exceptions by severity level

### 2. **Performance Monitoring**
- Tracks method execution times
- Identifies slow operations automatically
- Provides performance metrics

### 3. **Structured Logging**
- Method entry/exit logging
- Data access tracking
- Business event logging
- Security event monitoring

### 4. **Context-Aware Logging**
- Correlation IDs for request tracking
- Thread information
- Timestamp precision

## Components

### 1. OSMLogger Class
Main logging utility with comprehensive logging methods.

### 2. ExceptionHandler Class
Centralized exception handling with proper HTTP status codes and error messages.

## Usage Examples

### Basic Logging

```java
import com.xdev.xdevbase.utils.OSMLogger;

// Get logger for your class
Logger logger = OSMLogger.getLogger(YourClass.class);

// Log with different levels
OSMLogger.log(YourClass.class, OSMLogger.LogLevel.INFO, "User logged in: {}", username);
OSMLogger.log(YourClass.class, OSMLogger.LogLevel.WARN, "High memory usage detected");
OSMLogger.log(YourClass.class, OSMLogger.LogLevel.ERROR, "Database connection failed");
```

### Method Entry/Exit Logging

```java
public UserDTO findUserById(UUID id) {
    long startTime = System.currentTimeMillis();
    OSMLogger.logMethodEntry(this.getClass(), "findUserById", id);
    
    try {
        User user = userRepository.findById(id);
        UserDTO result = modelMapper.map(user, UserDTO.class);
        
        OSMLogger.logMethodExit(this.getClass(), "findUserById", result);
        OSMLogger.logPerformance(this.getClass(), "findUserById", startTime, System.currentTimeMillis());
        
        return result;
    } catch (Exception e) {
        OSMLogger.logException(this.getClass(), "Error finding user by ID: " + id, e);
        throw e;
    }
}
```

### Exception Handling

```java
// In Controller
@Override
public ResponseEntity<ApiSingleResponse<User, UserDTO>> findUserById(@PathVariable UUID id) {
    try {
        UserDTO user = userService.findById(id);
        return ResponseEntity.ok(new ApiSingleResponse<>(true, "User found", user));
    } catch (Exception e) {
        return ExceptionHandler.handleSingleException(this.getClass(), "findUserById", e);
    }
}

// In Service
@Override
public UserDTO findUserById(UUID id) {
    try {
        // Your business logic here
        return userRepository.findById(id)
            .map(user -> modelMapper.map(user, UserDTO.class))
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
    } catch (Exception e) {
        OSMLogger.logException(this.getClass(), "Error finding user by ID: " + id, e);
        throw e;
    }
}
```

### Performance Monitoring

```java
public List<UserDTO> findAllUsers() {
    long startTime = System.currentTimeMillis();
    OSMLogger.logMethodEntry(this.getClass(), "findAllUsers");
    
    try {
        List<User> users = userRepository.findAll();
        List<UserDTO> result = users.stream()
            .map(user -> modelMapper.map(user, UserDTO.class))
            .collect(Collectors.toList());
        
        OSMLogger.logMethodExit(this.getClass(), "findAllUsers", "Found " + result.size() + " users");
        OSMLogger.logPerformance(this.getClass(), "findAllUsers", startTime, System.currentTimeMillis());
        
        return result;
    } catch (Exception e) {
        OSMLogger.logException(this.getClass(), "Error finding all users", e);
        throw e;
    }
}
```

### Data Access Logging

```java
// Log database operations
OSMLogger.logDataAccess(this.getClass(), "READ", "User", userId);
OSMLogger.logDataAccess(this.getClass(), "CREATE", "User", newUserId);
OSMLogger.logDataAccess(this.getClass(), "UPDATE", "User", userId);
OSMLogger.logDataAccess(this.getClass(), "DELETE", "User", userId);
```

### Business Event Logging

```java
// Log business events
OSMLogger.logBusinessEvent(this.getClass(), "USER_REGISTERED", "New user registered: " + email);
OSMLogger.logBusinessEvent(this.getClass(), "PAYMENT_PROCESSED", "Payment processed for order: " + orderId);
OSMLogger.logBusinessEvent(this.getClass(), "INVENTORY_UPDATED", "Inventory updated for product: " + productId);
```

### Security Event Logging

```java
// Log security events
OSMLogger.logSecurityEvent(this.getClass(), "LOGIN_ATTEMPT", "Failed login attempt for user: " + username);
OSMLogger.logSecurityEvent(this.getClass(), "PERMISSION_DENIED", "Access denied for resource: " + resource);
OSMLogger.logSecurityEvent(this.getClass(), "SUSPICIOUS_ACTIVITY", "Multiple failed login attempts detected");
```

## Exception Severity Levels

The logging system categorizes exceptions by severity:

- **LOW**: Non-critical issues (e.g., entity not found)
- **MEDIUM**: Standard errors (e.g., validation failures)
- **HIGH**: Security or business logic issues
- **CRITICAL**: System failures (e.g., database connection issues)

## Log Output Format

### Exception Log Example
```
=== EXCEPTION REPORT ===
Correlation ID: OSM-A1B2C3D4
Timestamp: 2024-01-15 14:30:25.123
Class: com.example.UserService
Message: Error finding user by ID
Exception Type: EntityNotFoundException
Exception Message: User not found with id 123e4567-e89b-12d3-a456-426614174000
Severity: LOW

=== STACK TRACE ANALYSIS ===
Primary Exception Location:
  File: UserService.java
  Class: com.example.UserService
  Method: findById
  Line: 45

Application Code Location:
  File: UserController.java
  Class: com.example.UserController
  Method: findUserById
  Line: 23

Relevant Stack Trace:
  1. com.example.UserService.findById(UserService.java:45)
  2. com.example.UserController.findUserById(UserController.java:23)
  3. org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:118)

=== FULL STACK TRACE ===
[Full stack trace details]

=== CONTEXT INFORMATION ===
Thread: http-nio-8080-exec-1
Thread ID: 123
```

### Performance Log Example
```
⏱️ OPERATION: findUserById took 150ms
💾 DATA ACCESS: READ User with ID: 123e4567-e89b-12d3-a456-426614174000
📋 BUSINESS EVENT: USER_RETRIEVED - Retrieved user with ID: 123e4567-e89b-12d3-a456-426614174000
```

## Best Practices

### 1. **Always Use Try-Catch Blocks**
```java
try {
    // Your business logic
} catch (Exception e) {
    OSMLogger.logException(this.getClass(), "Error description", e);
    throw e; // Re-throw to let ExceptionHandler handle it
}
```

### 2. **Log Method Entry/Exit**
```java
public ReturnType methodName(Parameters params) {
    long startTime = System.currentTimeMillis();
    OSMLogger.logMethodEntry(this.getClass(), "methodName", params);
    
    try {
        // Method logic
        ReturnType result = // ... your logic
        
        OSMLogger.logMethodExit(this.getClass(), "methodName", result);
        OSMLogger.logPerformance(this.getClass(), "methodName", startTime, System.currentTimeMillis());
        
        return result;
    } catch (Exception e) {
        OSMLogger.logException(this.getClass(), "Error in methodName", e);
        throw e;
    }
}
```

### 3. **Use Appropriate Log Levels**
- **TRACE**: Detailed debugging information
- **DEBUG**: General debugging information
- **INFO**: General information about application progress
- **WARN**: Warning messages for potentially harmful situations
- **ERROR**: Error events that might still allow the application to continue
- **FATAL**: Severe error events that will presumably lead to application failure

### 4. **Include Context in Log Messages**
```java
// Good
OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "User {} logged in from IP {}", username, ipAddress);

// Avoid
OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "User logged in");
```

### 5. **Use ExceptionHandler in Controllers**
```java
@Override
public ResponseEntity<ApiResponse<User, UserDTO>> findAll() {
    try {
        List<UserDTO> users = userService.findAll();
        return ResponseEntity.ok(new ApiResponse<>(true, "Users retrieved", users));
    } catch (Exception e) {
        return ExceptionHandler.handleException(this.getClass(), "findAll", e);
    }
}
```

## Configuration

### Logback Configuration
Add to your `logback-spring.xml`:

```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/application.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="STDOUT" />
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

## Migration from Old Logging

### Before (Old Style)
```java
private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

@Override
public UserDTO findById(UUID id) {
    LOGGER.debug("Find By Id Start");
    Optional<User> data = repository.findById(id);
    if (data.isEmpty()) throw new EntityNotFoundException("Entity not found with this id " + id);
    else {
        LOGGER.debug("Find By Id End");
        return modelMapper.map(data.get(), UserDTO.class);
    }
}
```

### After (New Style)
```java
@Override
public UserDTO findById(UUID id) {
    long startTime = System.currentTimeMillis();
    OSMLogger.logMethodEntry(this.getClass(), "findById", id);
    
    try {
        Optional<User> data = repository.findById(id);
        if (data.isEmpty()) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "Entity not found with ID: {}", id);
            throw new EntityNotFoundException("Entity not found with this id " + id);
        } else {
            UserDTO result = modelMapper.map(data.get(), UserDTO.class);
            OSMLogger.logMethodExit(this.getClass(), "findById", result);
            OSMLogger.logPerformance(this.getClass(), "findById", startTime, System.currentTimeMillis());
            OSMLogger.logDataAccess(this.getClass(), "READ", "User", id);
            return result;
        }
    } catch (Exception e) {
        OSMLogger.logException(this.getClass(), "Error finding entity by ID: " + id, e);
        throw e;
    }
}
```

## Benefits

1. **Better Debugging**: Pinpoint exact line where exceptions occur
2. **Performance Monitoring**: Track slow operations automatically
3. **Request Tracking**: Correlation IDs for tracing requests across services
4. **Structured Logging**: Consistent format for all log messages
5. **Security Monitoring**: Track security events and suspicious activities
6. **Business Intelligence**: Log business events for analytics
7. **Error Classification**: Categorize errors by severity for better handling

## Support

For questions or issues with the logging system, please refer to the OSM project documentation or contact the development team. 