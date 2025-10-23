# Next Bus Android App - Complete Documentation

## Overview
Next Bus is an Android application for creating, managing, and viewing local bus schedules with location-based features. Users can add bus routes, track pickup locations on a map, and store bus information locally on their device.

## Architecture

### Technology Stack
- **Language**: Kotlin with Jetpack Compose (UI framework)
- **Database**: Room (SQLite wrapper with ORM)
- **Location Services**: Google Play Services (FusedLocationProviderClient)
- **Maps**: Google Maps SDK for Android
- **MVVM Pattern**: ViewModel + Repository pattern
- **Dependency Injection**: Manual (can be upgraded to Hilt)

### Project Structure

```
app/src/main/java/com/sameerasw/nextbus/
├── MainActivity.kt                    # Main Activity - Entry point
├── data/
│   ├── model/                         # Domain models
│   │   ├── Bus.kt                     # Bus data class
│   │   ├── BusSchedule.kt             # Schedule data class
│   │   └── Location.kt                # Location data class
│   ├── entity/
│   │   └── BusScheduleEntity.kt        # Room entity for database
│   ├── dao/
│   │   └── BusScheduleDao.kt           # Data Access Object for queries
│   ├── database/
│   │   └── AppDatabase.kt              # Room Database configuration
│   └── repository/
│       └── BusScheduleRepository.kt    # Data access layer
├── location/
│   ├── LocationManager.kt              # Location services wrapper
│   └── LocationData.kt                 # Location data class
├── ui/
│   ├── screen/
│   │   ├── BusScheduleListScreen.kt    # Main list of schedules
│   │   ├── CreateScheduleScreen.kt     # Create new schedule sheet
│   │   └── BusScheduleDetailScreen.kt  # Detail view with map
│   ├── viewmodel/
│   │   ├── BusScheduleViewModel.kt     # UI state management
│   │   └── BusScheduleViewModelFactory.kt # ViewModel factory
│   ├── navigation/
│   │   └── AppNavigation.kt            # Navigation logic
│   └── theme/
│       └── Theme files                 # UI theming
```

## Data Model

### BusSchedule (Domain Model)
```kotlin
data class BusSchedule(
    val id: Long = 0,
    val timestamp: Long,              // Departure time (epoch millis)
    val route: String,                // e.g., "Colombo → Kandy"
    val place: String,                // Pickup location name
    val location: Location? = null,   // GPS coordinates & address
    val bus: Bus? = null,             // Bus details
    val seating: String? = null       // "Available", "Almost full", "Full", "Loaded"
)
```

### Bus (Domain Model)
```kotlin
data class Bus(
    val type: String? = null,         // "sltb", "private"
    val tier: String? = null,         // "x1", "x1.5", "x2", "x4"
    val rating: Double? = null,       // 1.0-5.0
    val licensePlate: String? = null
)
```

### Location (Domain Model)
```kotlin
data class Location(
    val lat: Double? = null,
    val lng: Double? = null,
    val address: String? = null
)
```

### BusScheduleEntity (Room Entity - Database)
```kotlin
@Entity(tableName = "bus_schedule")
data class BusScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val route: String,
    val place: String,
    val seating: String? = null,
    val locationLat: Double? = null,
    val locationLng: Double? = null,
    val locationAddress: String? = null,
    val busType: String? = null,
    val busTier: String? = null,
    val busRating: Double? = null,
    val busLicensePlate: String? = null
)
```

## Local Storage (Room Database)

### Database Configuration
- **Database Name**: `nextbus_database`
- **Tables**: `bus_schedule`
- **Version**: 1

### DAO Operations

```kotlin
// Get all schedules (ordered by newest first)
getAllSchedules(): Flow<List<BusScheduleEntity>>

// Get by ID
getScheduleById(id: Long): BusScheduleEntity?

// Search by route
searchByRoute(route: String): Flow<List<BusScheduleEntity>>

// Get schedules in time range
getSchedulesByTimeRange(startTime: Long, endTime: Long): Flow<List<BusScheduleEntity>>

// Create
insertSchedule(schedule: BusScheduleEntity): Long

// Update
updateSchedule(schedule: BusScheduleEntity)

// Delete
deleteSchedule(schedule: BusScheduleEntity)
deleteScheduleById(id: Long)

// Count
getScheduleCount(): Flow<Int>
```

### Persistence Example
```kotlin
// Save to database
val newSchedule = BusSchedule(
    timestamp = System.currentTimeMillis(),
    route = "Colombo → Kandy",
    place = "Colombo Fort",
    location = Location(lat = 6.9355, lng = 79.8428, address = "..."),
    bus = Bus(type = "sltb", tier = "x2", rating = 4.5),
    seating = "Available"
)
repository.insertSchedule(newSchedule)

// Query
repository.getAllSchedules().collect { schedules ->
    // Update UI
}

// Delete
repository.deleteSchedule(schedule)
```

## Location Services

### LocationManager
Handles GPS location acquisition and reverse geocoding.

**Key Methods:**
```kotlin
// Get continuous location updates
fun getCurrentLocationFlow(): Flow<LocationData>

// Get address from coordinates
suspend fun getAddressFromCoordinates(latitude: Double, longitude: Double): String?
```

**Permissions Required:**
- `android.permission.ACCESS_FINE_LOCATION` (GPS)
- `android.permission.ACCESS_COARSE_LOCATION` (WiFi/cell)
- `android.permission.INTERNET` (for geocoding)

**Usage:**
```kotlin
locationManager.getCurrentLocationFlow().collect { locationData ->
    println("Lat: ${locationData.latitude}, Lng: ${locationData.longitude}")
    println("Address: ${locationData.address}")
}
```

## UI Screens

### 1. BusScheduleListScreen
- **Purpose**: Displays all saved bus schedules
- **Features**:
  - List sorted by departure time (newest first)
  - Shows route, time, pickup location
  - Bus type and seating status badges
  - Delete button on each card
  - FAB button to create new schedule
  - Empty state message if no schedules

**Key Components:**
- `BusScheduleList()` - Main list UI
- `BusScheduleCard()` - Individual schedule card
- `DeleteConfirmationDialog()` - Confirm before delete

### 2. CreateScheduleSheet (Modal Bottom Sheet)
- **Purpose**: Create new bus schedule
- **Fields**:
  - Time picker (hours/minutes)
  - Route input (required)
  - Pickup location input
  - Bus type dropdown (SLTB/Private/Other)
  - Bus tier/class dropdown (x1, x1.5, x2, x4)
  - Seating status dropdown (Available/Almost full/Full/Loaded)
  - License plate input
  - Rating input

**Features:**
- Location picker button (shows city list)
- Create and Cancel buttons
- Form validation (route required)
- Automatic address suggestion from GPS

### 3. BusScheduleDetailScreen
- **Purpose**: Show detailed information about a schedule
- **Sections**:
  - **Map View**: Shows pickup location with marker
  - **Basic Info**: Time, route, pickup location
  - **Bus Details**: Type, tier, rating, license plate
  - **Location Data**: Latitude, longitude, full address

**Google Maps Integration:**
- Displays map centered on pickup location (zoom 15)
- Custom marker with schedule info
- Interactive map controls

### 4. LocationSearchSheet
- **Purpose**: Search and select pickup location
- **Features**:
  - Searchable list of cities
  - Pre-populated city list (Colombo, Kandy, Galle, etc.)
  - Real-time filtering
  - One-tap selection

## ViewModel & State Management

### BusScheduleViewModel
```kotlin
data class BusScheduleUiState(
    val schedules: List<BusSchedule> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedSchedule: BusSchedule? = null
)

class BusScheduleViewModel(
    private val repository: BusScheduleRepository
) : ViewModel() {
    val uiState: StateFlow<BusScheduleUiState>
    val allSchedules: Flow<List<BusSchedule>>
    
    fun createSchedule(...)
    fun deleteSchedule(schedule: BusSchedule)
    fun selectSchedule(schedule: BusSchedule)
    fun searchByRoute(route: String): Flow<List<BusSchedule>>
}
```

**Features:**
- Observes repository for real-time updates
- Manages loading and error states
- Handles create/delete/search operations
- Survives configuration changes (Activity rotation)

## Setup Instructions

### 1. Google Maps API Key
Add your Google Maps API key to `AndroidManifest.xml`:
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_API_KEY_HERE" />
```

### 2. Permissions (AndroidManifest.xml)
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
```

### 3. Runtime Permissions
The app requests location permissions at runtime when:
- User opens the app for the first time
- User denies permission and tries to use location features

### 4. Build Configuration
```gradle
// gradle.properties - Java 17 compatible
org.gradle.java.home=/path/to/java17

// build.gradle.kts
android {
    compileSdk = 36
    defaultConfig {
        minSdk = 33
        targetSdk = 36
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
```

## Dependencies

```toml
# Room Database
androidx-room-runtime = "2.6.1"
androidx-room-compiler = "2.6.1"
androidx-room-ktx = "2.6.1"

# Play Services Location
play-services-location = "21.1.0"

# Maps
play-services-maps = "18.2.0"
maps-compose = "4.3.1"

# Navigation & ViewModel
androidx-navigation-compose = "2.7.7"
androidx-lifecycle-viewmodel-compose = "2.7.0"

# Jetpack Compose
androidx-compose-bom = "2024.09.00"
androidx-material3 = "latest"
```

## Building & Running

### Prerequisites
- Android Studio Ladybug+ 
- Java 17+
- Android SDK 33+
- Google Maps API Key

### Build Commands
```bash
# Clean build
./gradlew clean build

# Run on device
./gradlew installDebug

# Run tests
./gradlew test

# Build release APK
./gradlew build -PENV=prod
```

### Troubleshooting

**Issue**: "Unresolved reference" errors during build
**Solution**: 
- Ensure Java 17 is set in `gradle.properties`: `org.gradle.java.home=/path/to/java17`
- Run `./gradlew clean build --no-build-cache`

**Issue**: "Google Maps API key invalid"
**Solution**:
- Check API key in `AndroidManifest.xml`
- Ensure Maps SDK for Android is enabled in Google Cloud Console
- Verify signing certificate fingerprint is registered

**Issue**: Location permission denied
**Solution**:
- Grant permission in device Settings → Apps → Next Bus → Permissions
- App will request permission on first launch

## Future Enhancements

1. **Cloud Sync**: Sync schedules with Firebase/backend
2. **Real-time Updates**: Integrate real bus tracking APIs
3. **Notifications**: Alert when bus arrives nearby
4. **User Accounts**: Multi-device sync via user login
5. **Share Schedule**: QR code or link sharing
6. **Rating System**: Rate bus experiences, aggregate data
7. **Favorites**: Save frequently used routes
8. **Offline Maps**: Download offline map tiles
9. **Multiple Languages**: Localization support
10. **Dark Mode**: Full dark theme support

## Testing

### Unit Tests
```kotlin
// Test repository operations
@Test
fun insertSchedule_savesToDatabase() {
    val schedule = BusSchedule(...)
    repository.insertSchedule(schedule)
    // Assert schedule is saved
}

// Test ViewModel
@Test
fun createSchedule_updatesUiState() {
    viewModel.createSchedule(...)
    // Assert schedules list updated
}
```

### UI Tests
```kotlin
// Test compose screens
@Test
fun busScheduleListScreen_displaysSchedules() {
    composeTestRule.setContent {
        BusScheduleListScreen(viewModel, {}, {})
    }
    // Assert schedules are displayed
}
```

## Performance Considerations

1. **Database Indexing**: Schedule queries indexed by timestamp
2. **Pagination**: Load schedules in batches for large datasets
3. **Image Caching**: Cache bus/location images locally
4. **Location Updates**: Throttle location updates (5000ms interval minimum)
5. **Memory Management**: Use Flow for reactive, efficient updates

## Security

1. **API Keys**: Store Google Maps key securely (not in code)
2. **Permissions**: Only request necessary permissions
3. **Local Data**: All data stored locally on device (SQLite encrypted at OS level)
4. **Location Privacy**: Clear location data on app uninstall
5. **HTTPS**: Any backend communication uses TLS

## License
MIT License - See LICENSE file

## Support
For issues or questions, contact: support@nextbus.app

