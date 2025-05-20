@echo off
SET CONTAINER_NAME=multiplayersudoku
SET IMAGE_NAME=multiplayersudoku
REM Stop any running containers with the name 'multiplayersudoku'
docker ps -q -f name=%CONTAINER_NAME% >nul 2>nul
IF NOT ERRORLEVEL 1 (
    echo Stopping the running '%CONTAINER_NAME%' container...
    docker stop %CONTAINER_NAME%
    docker rm %CONTAINER_NAME%
)

REM Remove any previous images with the name 'multiplayersudoku'
docker images -q %IMAGE_NAME% >nul 2>nul
IF NOT ERRORLEVEL 1 (
    echo Removing the previous '%IMAGE_NAME%' Docker image...
    docker rmi %IMAGE_NAME%
)

echo Building the Docker image...
docker build -t %IMAGE_NAME% .
IF ERRORLEVEL 1 (
    echo Docker build failed. Exiting.
    exit /b 1
)

echo Docker image built successfully.

echo Running the Docker container...
docker run --name %CONTAINER_NAME% -p 8080:8080 -d %IMAGE_NAME%
IF ERRORLEVEL 1 (
    echo Docker container run failed. Exiting.
    exit /b 1
)

echo Docker container is running.

echo Opening http://localhost:8080 in the browser...
start http://localhost:8080
