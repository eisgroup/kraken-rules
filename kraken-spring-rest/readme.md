# Kraken rest API

## Endpoints

### Rule
url: `host:8888/rule`

request body: (dimensions optional)
```json
{
    "name": "R0000",
    "dimensions": {
    	"packageCd": "1"
    }
}
```

### Entry Point
url: `host:8888/ep`

request body: (dimensions optional) 
```json
{
    "name": "Init",
    "dimensions": {
    	"packageCd": "1"
    }
}
```

### Context Definition
url: `host:8888/context`

request body:
```json
{
    "name": "PolicyCtx"
}
```


### Entry Point Bundle
url: `host:8888/bundle`

request body: (dimensions optional)
```json
{
    "name": "PolicyCtx",
    "dimensions": {
    	"packageCd": "1"
    }
}
```

## Build app 
- build: `mvn clean install`
- build example for jenkins: 
`mvn clean install -Dbuild.number="123" -Dbuild.date="06-06-1993" -Dbuild.startTime="00:00" -Dbuild.revision="646dr8"`
- run jar: `java -jar target/kraken-spring-rest-1.0.2-SNAPSHOT.jar`
- start locally: `mvn spring-boot:run`
- start locally with jenkins build properties example: 
`mvn spring-boot:run -Dbuild.number="123" -Dbuild.date="06-06-1993" -Dbuild.startTime="00:00" -Dbuild.revision="646dr8"`
