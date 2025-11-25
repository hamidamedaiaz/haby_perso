# 🗺️ Heavy Client - Java SOAP Routing Application

A Java Swing application that displays interactive routes on OpenStreetMap using SOAP web services.

## ✨ Features

- **Interactive Map Visualization** - OpenStreetMap integration with JXMapViewer2
- **SOAP Web Service Client** - Consumes routing services via JAX-WS
- **Route Display** - Visual representation of calculated itineraries
- **Waypoint Markers** - Start and end point indicators
- **Auto-Zoom** - Automatic map fitting to display entire route
- **Real-time Notifications** - ActiveMQ message broker integration
- **Local Tile Caching** - Offline map support

## 🎯 Demo

![Application Screenshot](docs/screenshot.png)

The application displays:

- OpenStreetMap background tiles (HTTPS)
- Route line with black outline and blue inner line
- Red circular waypoint markers at start/end
- Real-time notification panel at bottom

## 🏗️ Architecture

```
heavyClient/
├── src/main/java/com/heavyclient/
│   ├── Main.java                    # Application entry point
│   ├── HeavyClientUI.java           # Main UI with map display
│   ├── AMQNotificationListener.java # ActiveMQ message consumer
│   └── utils/
│       ├── RouteUtils.java          # GPS position extraction
│       ├── RoutePainter.java        # Route line renderer
│       └── CustomWaypointRenderer.java # Waypoint marker renderer
├── target/generated-sources/wsimport/
│   └── com/soap/generated/          # JAX-WS generated SOAP client
└── pom.xml                          # Maven configuration
```

## 🚀 Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Internet connection (for tile downloads)
- SOAP Routing Service running on `http://localhost:8734/RoutingServiceSOAP`

### Build

```bash
git clone <repository-url>
cd heavyClient
mvn clean package
```

### Run

```bash
java -jar target/heavyclient-with-dependencies.jar
```

Or with Maven:

```bash
mvn exec:java -Dexec.mainClass="com.heavyclient.Main"
```

## 📦 Dependencies

| Library          | Version | Purpose                     |
|------------------|---------|-----------------------------|
| JXMapViewer2     | 2.8     | OpenStreetMap visualization |
| JAX-WS RT        | 4.0.2   | SOAP web service client     |
| Jakarta XML Bind | 4.0.0   | XML/Java binding            |
| ActiveMQ         | 5.18.3  | Message broker integration  |
| Log4j            | 2.22.0  | Logging framework           |

## ⚙️ Configuration

### Map Tiles

The application uses **HTTPS** for OpenStreetMap tiles (HTTP is deprecated):

```java
https://tile.openstreetmap.org/{zoom}/{x}/{y}.png
```

**Cache location:** `~/.heavyclient-cache/`

### SOAP Service

Configure the WSDL URL in `pom.xml`:

```xml
<wsdlUrl>http://localhost:8734/RoutingServiceSOAP?wsdl</wsdlUrl>
```

### Rate Limiting

To comply with OSM tile usage policy:

- **Thread Pool Size:** 2
- **Request Delay:** 100ms between requests
- **Timeouts:** 15s connect, 20s read

## 🎨 Customization

### Change Route Color

```java
// In HeavyClientUI.drawRoute()
routePainter.setColor(Color.GREEN);
```

### Adjust Waypoint Size

```java
renderer.setSize(20); // Default: 16
```

### Modify Zoom Level

```java
mapViewer.setZoom(10); // Default: 8
```

## 📖 API Usage

### Display a Route

```java
HeavyClientUI ui = new HeavyClientUI();
List<GeoPosition> route = Arrays.asList(
        new GeoPosition(45.758, 4.835),  // Lyon
        new GeoPosition(48.8566, 2.3522) // Paris
);
ui.drawRoute(route);
```

### Add Notification

```java
ui.addNotification("Route calculated: 450km");
```

## 🔧 Troubleshooting

### Map Tiles Not Loading

**Symptoms:** Gray tiles instead of map
**Solutions:**

1. Check internet connection
2. Verify HTTPS access to `tile.openstreetmap.org`
3. Check cache directory: `~/.heavyclient-cache/`
4. Review logs for "Failed to load" messages

### SOAP Service Connection Failed

**Symptoms:** `Connection refused` error
**Solutions:**

1. Verify routing service is running
2. Check URL: `http://localhost:8734/RoutingServiceSOAP?wsdl`
3. Review firewall settings

### Compilation Errors

```bash
# Regenerate SOAP client
mvn clean generate-sources

# Reload Maven project in IDE
```

## 🧪 Testing

### Test Map Display

```bash
mvn test -Dtest=MapTest
```

### Test SOAP Client

```bash
curl http://localhost:8734/RoutingServiceSOAP?wsdl
```

## 📝 License

This project is an educational demonstration of:

- Java Swing GUI development
- SOAP web service consumption
- OpenStreetMap integration
- Message broker usage

## 🤝 Contributing

This is an educational project. For improvements:

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Submit a pull request

## 📧 Contact

For issues or questions, please open an issue in the repository.

## 🙏 Acknowledgments

- **JXMapViewer2** - Martin Steiger and contributors
- **OpenStreetMap** - Map data © OpenStreetMap contributors
- **Apache ActiveMQ** - Message broker
- **Jakarta EE** - SOAP and XML binding

---

**Built with ❤️ using Java, Maven, and OpenStreetMap**

