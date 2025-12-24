# Track Occupancy Android App

An Android application for monitoring auditorium occupancy using camera-based tracking system.

## Features

- **Authentication**: Secure login to access the system
- **City Navigation**: Browse cities with available auditoriums
- **Building Management**: View buildings within selected cities
- **Auditorium Monitoring**: Real-time occupancy data for auditoriums
- **Camera Integration**: View live camera snapshots
- **Network Support**: Works on local network and external SSL connections

## Architecture

The app follows Clean Architecture principles with MVVM pattern:

- **Data Layer**: Repository pattern with API services
- **Domain Layer**: Use cases and business logic
- **UI Layer**: ViewModels and Fragments with data binding

## Tech Stack

- **Language**: Kotlin
- **Architecture**: MVVM with Repository pattern
- **Networking**: Retrofit with OkHttp
- **Dependency Injection**: Hilt
- **UI**: View Binding, Material Design 3
- **Async**: Coroutines and Flow
- **Image Loading**: Glide

## Backend Integration

The app connects to a REST API with the following endpoints:

- `POST /v1/login` - Authentication
- `GET /v1/cities` - List cities
- `GET /v1/cities/{cityId}/buildings` - List buildings
- `GET /v1/cities/{cityId}/buildings/{buildingId}/auditories` - List auditoriums
- `GET /v1/cities/{cityId}/buildings/{buildingId}/auditories/occupancy` - Building occupancy
- `GET /v1/cities/{cityId}/buildings/{buildingId}/auditories/{auditoriumId}/occupancy` - Auditorium occupancy
- `GET /api/snapshot?mac={mac}` - Camera snapshots

## Configuration

### API Endpoints

The app is configured to connect to:
- **Production**: `https://track-occupancy.auditory.ru`
- **Local Network**: `http://192.168.1.100` (update in `BuildConfig`)

### SSL Configuration

For SSL certificate pinning, update the network security configuration in `res/xml/network_security_config.xml`.

## Building the App

### Prerequisites

- Android Studio Arctic Fox or later
- Minimum SDK: API 24 (Android 7.0)
- Target SDK: API 34 (Android 14)

### Build Steps

1. Clone the repository
2. Open in Android Studio
3. Sync project with Gradle files
4. Build → Make Project
5. Run on device or emulator

### Build Variants

- **Debug**: Development build with logging enabled
- **Release**: Production build with optimizations

## Deployment

### Local Network Deployment

1. Ensure backend services are running on local network
2. Update `LOCAL_BASE_URL` in `build.gradle.kts` if IP changes
3. Build and install APK on device connected to same network

### External SSL Deployment

1. Ensure SSL certificate is valid for `track-occupancy.auditory.ru`
2. Configure certificate pinning if required
3. Build release APK
4. Deploy to app stores or distribute APK

## Docker Deployment

The backend services can be deployed using Docker Compose. See the backend README for deployment instructions.

## Security Considerations

- API endpoints use HTTPS in production
- Authentication tokens are stored securely
- Network traffic is logged in debug builds only
- Consider implementing certificate pinning for production

## Contributing

1. Follow Kotlin coding standards
2. Use meaningful commit messages
3. Test on multiple devices/screen sizes
4. Update documentation for API changes

## License

This project is proprietary software for Track Occupancy system.
