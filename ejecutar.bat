@echo off
title Cobranza Vecinal - Spring Boot
echo ========================================================
echo   Iniciando Cobranza Vecinal (Sistema de Cobranza)
echo ========================================================
echo.
cd /d "%~dp0"

:: Deteccion automatica y robusta de JAVA_HOME para ejecucion directa
if not defined JAVA_HOME (
    if exist "C:\Program Files\Eclipse Adoptium\jdk-25.0.4.7-hotspot" (
        set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.4.7-hotspot"
    ) else if exist "C:\Program Files\Apache NetBeans\jdk" (
        set "JAVA_HOME=C:\Program Files\Apache NetBeans\jdk"
    ) else if exist "C:\Program Files\Java\jdk-21" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-21"
    ) else if exist "C:\Program Files\Java\jdk-17" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-17"
    )
)

if defined JAVA_HOME (
    echo [OK] JAVA_HOME detectado: %JAVA_HOME%
) else (
    echo [AVISO] JAVA_HOME no esta configurado en variables de entorno.
)

if exist mvnw.cmd (
    call mvnw.cmd spring-boot:run -o -Dspring-boot.run.jvmArguments="-XX:TieredStopAtLevel=1 -Xverify:none -Dspring.main.lazy-initialization=true -Xms128m -Xmx512m"
) else (
    call mvn spring-boot:run -o -Dspring-boot.run.jvmArguments="-XX:TieredStopAtLevel=1 -Xverify:none -Dspring.main.lazy-initialization=true -Xms128m -Xmx512m"
)
pause
