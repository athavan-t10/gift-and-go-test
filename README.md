# Gift and Go: File Processing Service

REST API that processes pipe-delimited entry files and returns JSON outcome files.

## Requirements
- Java 21
- Spring Boot 3.2.7
- Maven

## Run
```bash
mvn spring-boot:run
```

## Configuration
`application.yml`:
```yaml
features:
  skip-validation=false  # Enable validation (default)
  skip-validation=true   # Skip validation
```

## Test Endpoint

**curl:**
```bash
curl -X POST http://localhost:8080/file/upload \
  -F "file=@EntryFile.txt" \
  -o OutcomeFile.json
```

**Postman:** POST to `http://localhost:8080/file/upload` with form-data key `file` (File type)

## Input Format
```
UUID|ID|Name|Likes|Transport|AvgSpeed|TopSpeed
```

## Output Format
```json
[{"name":"John Smith","transport":"Rides A Bike","topSpeed":12.1}]
```

## Design
- Feature flag controls file and data validation
- Controller: HTTP handling
- Service: business logic