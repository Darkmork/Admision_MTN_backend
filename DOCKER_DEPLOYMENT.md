# Docker Sandbox Deployment Guide

This document describes how to deploy the RobotCode backend with Docker sandbox support.

## Docker Sandbox Solution

The implemented Docker sandbox provides:

✅ **Secure Python execution** with resource limits
✅ **Isolated environment** with network disabled
✅ **Memory and CPU constraints** (128MB, 0.5 CPU)
✅ **Timeout protection** (10 seconds)
✅ **Common Python libraries** (numpy, pandas, matplotlib, etc.)
✅ **Automatic fallback** to existing validation when Docker unavailable

## System Requirements

- **Docker** must be installed and running
- **Docker image** `robotcode-python:latest` must be built
- **Java 17** for the Spring Boot application

## Building the Docker Image

```bash
# Navigate to backend directory
cd "RobotCode backend"

# Build the Python sandbox image
docker build -f Dockerfile.python -t robotcode-python:latest .

# Verify the image was created
docker images | grep robotcode-python
```

## Testing the Docker Sandbox

### Basic Test
```bash
docker run --rm robotcode-python:latest python3 -c "print('Hello from Docker sandbox!')"
```

### Resource Limits Test
```bash
docker run --rm --memory=128m --cpus=0.5 --network=none robotcode-python:latest python3 -c "
import os
print(f'Available memory: {os.sysconf(\"SC_PAGE_SIZE\") * os.sysconf(\"SC_PHYS_PAGES\") / 1024 / 1024:.1f} MB')
"
```

### Function Test
```bash
docker run --rm robotcode-python:latest python3 -c "
def verificar_mayor_edad(edad):
    if edad >= 18:
        return 'Es mayor de edad'
    else:
        return 'Es menor de edad'

print(verificar_mayor_edad(20))
print(verificar_mayor_edad(15))
"
```

## API Endpoints

### Check Docker Status
```bash
GET /api/judge/info
```

Returns system information including Docker availability.

### Validate with Docker
```bash
POST /api/judge/validate-docker/{problemId}
Content-Type: application/json

{
    "code": "def verificar_mayor_edad(edad):\n    if edad >= 18:\n        return 'Es mayor de edad'\n    else:\n        return 'Es menor de edad'\n\nedad = int(input())\nprint(verificar_mayor_edad(edad))",
    "language": "python3"
}
```

### Standard Validation (Auto-detects Docker)
```bash
POST /api/judge/validate/{problemId}
Content-Type: application/json

{
    "code": "def verificar_mayor_edad(edad):\n    if edad >= 18:\n        return 'Es mayor de edad'\n    else:\n        return 'Es menor de edad'\n\nedad = int(input())\nprint(verificar_mayor_edad(edad))",
    "language": "python3"
}
```

## Railway Deployment

### Option 1: Build on Railway
If Railway supports Docker-in-Docker, add to `railway.toml`:

```toml
[build]
builder = "dockerfile"
```

Create a main `Dockerfile`:
```dockerfile
FROM openjdk:17-jdk-slim

# Install Docker
RUN apt-get update && apt-get install -y docker.io

# Copy application
COPY . /app
WORKDIR /app

# Build Python image
RUN docker build -f Dockerfile.python -t robotcode-python:latest .

# Run application
CMD ["./mvnw", "spring-boot:run"]
```

### Option 2: Pre-built Image Registry
1. Build and push the Python image to a registry:
```bash
docker tag robotcode-python:latest your-registry/robotcode-python:latest
docker push your-registry/robotcode-python:latest
```

2. Update `DockerExecutorService.java`:
```java
private static final String DOCKER_IMAGE = "your-registry/robotcode-python:latest";
```

### Option 3: Railway-specific Solution
Use Railway's container deployment with Docker-in-Docker support:

```yaml
# railway.yml
services:
  web:
    build:
      dockerfile: Dockerfile
    environment:
      - DOCKER_ENABLED=true
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
```

## Security Considerations

The Docker sandbox implements multiple security layers:

1. **Resource Limits**: 128MB memory, 0.5 CPU
2. **Network Isolation**: `--network=none`
3. **User Restrictions**: `--user=nobody`
4. **Filesystem**: `--read-only` with limited tmpfs
5. **Process Limits**: 10-second timeout
6. **Container Cleanup**: `--rm` for automatic cleanup

## Monitoring

Monitor Docker usage:
```bash
# Check running containers
docker ps

# Check resource usage
docker stats

# Check image size
docker images robotcode-python
```

## Troubleshooting

### Docker Not Available
- Check if Docker daemon is running
- Verify Docker socket permissions
- Ensure image is built: `docker images | grep robotcode-python`

### Out of Memory
- Increase memory limit in `DockerExecutorService.java`
- Monitor with `docker stats`

### Timeout Issues
- Increase timeout in `TIMEOUT_SECONDS` constant
- Check for infinite loops in user code

### Permission Errors
- Ensure Docker socket is accessible
- Check user permissions for Docker commands

## Performance Optimization

1. **Image Caching**: Keep base Python image cached
2. **Container Reuse**: Consider container pooling for high load
3. **Resource Tuning**: Adjust memory/CPU limits based on requirements
4. **Parallel Execution**: Multiple containers can run simultaneously

## Future Enhancements

- **Container Pooling**: Reuse containers for better performance
- **Multi-language Support**: Add support for Java, JavaScript, etc.
- **Advanced Monitoring**: Detailed execution metrics
- **Distributed Execution**: Run containers on multiple nodes
- **Custom Libraries**: Allow problem-specific library installation