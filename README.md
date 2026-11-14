# Cobranza Vecinal - Sistema de Gestión y Cobranza en Condominios

**Versión:** `v2.2.0` (Segundo Avance - Semana 15)  
**Ciclo Académico:** UTP 2026-II  
**Curso:** Diseño de Patrones de Software / Ingeniería de Software Avanzada  
Plataforma: Java 17 LTS, Spring Boot 3.3.3, JPA / Hibernate 6.5.2, H2 Database Engine, Java Swing GUI, JUnit 5, JaCoCo, Lombok.

---

## 1. Características de la Arquitectura
- **Persistencia JPA Multiesquema**: 25 entidades de dominio mapeadas relacionalmente en 5 esquemas (`financiero`, `inmuebles`, `personas`, `operaciones`, `seguridad`).
- **Capa de Repositorios**: 22 interfaces Spring Data JPA con transaccionalidad ACID y soporte multiesquema.
- **Capa de Servicios Desacoplada (SRP)**: `ICobranzaService`, `IInmuebleService`, `IPersonaService`, `IOperacionService`.
- **Fachada Unificada (GoF Facade)**: `CobranzaFachada` coordina procesos de negocio e interfaces Swing y controladores REST.
- **Control de Acceso Basado en Roles (RBAC)**: Vistas diferenciadas para `ADMINISTRADOR` (`admin/admin123`), `CONSERJE` (`conserje/conserje123`) y `RESIDENTE` (`residente/residente123`).
- **15 Patrones de Diseño GoF (Arquitectura Simétrica 5-5-5)**:
  - *Creacionales (5)*: Singleton, Factory Method, Abstract Factory, Builder, Prototype.
  - *Estructurales (5)*: Adapter, Composite, Decorator, Facade, Proxy.
  - *Comportamiento (5)*: Command (Undo de pagos), Observer, State (ciclo de vida de cuota), Strategy, Template Method.
- **Calidad y Cobertura**: Suite de **77 pruebas automatizadas JUnit 5** aprobadas al 100% (`BUILD SUCCESS`), con reporte de cobertura de código JaCoCo (`target/site/jacoco/index.html`).
- **Diseño Visual Swing**: Tema corporativo Flat Dark, sistema de microinteracciones a 60 FPS (`javax.swing.Timer`), y catálogo interactivo de patrones en la GUI.

---

## 2. Requisitos de Entorno
- **Java**: JDK 17 o superior.
- **IDE Compatible**: Apache NetBeans (12 a 24+), IntelliJ IDEA, Eclipse o Visual Studio Code.
- **Maven**: Incluido mediante el Maven Wrapper (`mvnw` / `mvnw.cmd`). No requiere instalación previa.

---

## 3. Instrucciones de Compilación y Ejecución

### Apertura en Apache NetBeans
1. Abrir **Apache NetBeans**.
2. Ir a **File** > **Open Project...** (`Ctrl + Shift + O`).
3. Seleccionar la carpeta raíz del proyecto.
4. Ejecutar **Clean and Build** (`Shift + F11`).
5. Ejecutar la aplicación con **Run** (`F6`).

### Ejecución Directa por Línea de Comandos
```bash
# Ejecutar suite de pruebas unitarias y generar reporte JaCoCo:
mvn clean test
# (o con Maven Wrapper):
.\mvnw.cmd clean test

# Iniciar la aplicación de escritorio Swing:
mvn spring-boot:run
# (o con Maven Wrapper):
.\mvnw.cmd spring-boot:run

# En Windows también se puede hacer doble clic en:
ejecutar.bat
```

---

## 4. Consola de Base de Datos H2
Con la aplicación en ejecución:
1. Abrir el navegador en: `http://localhost:8080/h2-console`
2. Parámetros de conexión:
   - **Driver Class**: `org.h2.Driver`
   - **JDBC URL**: `jdbc:h2:file:./data/cobranzadb`
   - **User Name**: `sa`
   - **Password**: *(dejar en blanco)*
3. Presionar **Connect**.
