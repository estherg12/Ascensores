# Simulador de Ascensores RMI
### Proyecto de Sistemas Distribuidos y Programación Orientada a Objetos
Simulación de la gestión inteligente de ascensores en un edificio usando una arquitectura Cliente-Servidor mediante el paradigma de Invocación de Métodos Remotos (RMI) para coordinar múltiples cabinas y usuarios en tiempo real.
El simulador permite a múltiples clientes solicitar ascensores de forma concurrente. El servidor gestiona una flota dinámica de ascensores y utiliza algoritmos de optimización para asignar la unidad más eficiente.

### Paradigmas y Tecnologías Aplicadas
* Programación Distribuida (RMI): desacoplamiento de la lógica de control (Servidor) y la interacción del usuario (Cliente)
* Programación Concurrente: uso de monitores (synchronized y wait/notify) para gestionar el acceso de múltiples hilos a recursos compartidos
* POO: encapsulamiento de datos y lógica en entidades claras
* Interfaces Gráficas (Swing): paneles de control dinámicos con actualización de datos en tiempo real

## Arquitectura y Clases principales
### Entidades de Datos y Lógica
* Ascensor: cabina física, almacena su posición (planta), estado (ocupado/libre), el ID del cliente actual y marcas de tiempo para algoritmos de inactividad
* InterfazRemota: define el contrato de servicios que el Servidor ofrece al exterior
* ObjetoRemoto: implementa la lógica de concurrencia y algoritmos de selección de ascensor tras una solicitud de un usuario desde una planta concreta:
    - MAS_CERCANO: optimiza el tiempo de respuesta por distancia física
    - MAS_LEJANO: algoritmo de prueba para trayectos largos
    - MAS_INACTIVO: prioriza el uso de ascensores que llevan más tiempo parados
    - MENOS_INACTIVO: prioriza el uso de ascensores que lleve menos tiempo sin parar
 
### Componentes de Interfaz
* Servidor y Control: clase de arranque y JFrame que permite al administrador (Servidor) cambiar el número de ascensores, límites de plantas y algoritmos
* Cliente y VistaCliente: aplicación para el usuario final que permite llamar al ascensor desde una planta (inicialmente 0), seleccionar destinos con botones que se bloquean / desbloquean según el estado del servicio

* PintorCliente: hilo dedicado en el lado del cliente a realizar un polling constante para asegurar que la tabla local refleja fielmente el estado del servidor

## Manual de Usuario
Prerrequisitos: Java JDK 17 o superior + Apache NetBeans (recomendado)
### Instalación y Ejecución:
1. Ejecutar el Servidor: clase Servidor.java para registrar el objeto en el puerto 1099 y abrir el panel de Control
2. Ejecutar Cliente: iniciar 1 o más instancias de Cliente.java, cada instancia simula a 1 persona diferente en el edificio

#### Uso de la Interfaz
* Panel Servidor (Control): usa los Spinners para cambiar el número de ascensores o las dimensiones del edificio (planta mínima y planta máxima). Los cambios se propagarán a todos los clientes conectados de forma reactiva. En la tabla central se muestran los datos de los ascensores, por lo que si se modifica el número de ascensores ofrecidos, se modificarán también las filas de la tabla automáticamente
* Panel Cliente (VistaCliente): pulsa "Subir" o "Bajar" para solicitar un ascensor, una vez asignado (se indica el ID del ascensor asignado), puede seleccionar la planta con el Spinnes justo encima del botón "Ir" y pulsar este botón para moverse con el Ascensor, que viajará (5 segundos por planta) y se liberará automáticamente cuando le haya llevado a su destino

## Calidad de Software: PRUEBAS
### Pruebas Unitarias (JUnit 5)
Se ha garantizado un 100% de cobertura en lógica crítica mediante:
* Caja Blanca: verificación de cada rama de los algoritmos de selección mediante Reflexión de Java
* Clases de Equivalencia: validación de entradas para evitar plantas inexistentes o números negativos de ascensores
* Pruebas de Estrés: simulación de 10 hilos solicitando ascensores simultáneamente para validar la integridad de los contadores y evitar Race Conditions
* Robustez Dinámica: verificación de que los ascensores abortan trayectos de forma seguira si el edificio cambia de tamaño durante el movimiento

### Pruebas de Integración (EasyMock)
Se utilizaron Mock Objects para testear el comportamiento del cliente de forma aislada:
* Simulación de Red: validación del comportamiento del cliente ante una RemoteException (caída del Servidor)
* Integración de UI: comprobación de que la interfaz de usuario reacciona correctamente a los datos simulados del Servidor, habilitando o deshabilitando componentes según los protocolos definidos

### Métricas de Código 📊
El proyecto ha sido analizado con el plugin Source Code Metrics para asegurar mantenibilidad:
* Complejidad Ciclomática (VG): mantenida bajo niveles óptimos mediante la fragmentación de métodos
* Sincronización Thread-Safe: uso de java.awt.EventQueue.invokeLater en las actualizaciones de la GUI para evitar errores de renderizado concurrentes

## Estructura de Carpetas
src/main/java/poo/ascensores/
├── Ascensor.java             # Clase serializable de estado
├── InterfazRemota.java       # Definición de métodos RMI
├── ObjetoRemoto.java         # Lógica de negocio y concurrencia
├── Servidor.java             # Arranque del sistema (Main)
├── Control.java              # GUI del Servidor
├── Cliente.java              # Arranque del usuario (Main)
├── VistaCliente.java         # GUI del Cliente
└── PintorCliente.java        # Hilo de refresco visual
src/test/java/tests/unitarios/
├── ObjetoRemotoTest.java     # Pruebas de caminos básicos (JUnit)
├── ObjetoRemotoEquivalencia.java # Pruebas de límites y borde
└── ObjetoRemotoConcurrenciaTest.java # Pruebas de estrés y hilos
src/test/java/poo/ascensores/
├── PintorClienteTest.java    # Prueba de Integración hilo PintorCliente
└── VistaClienteTest.java     # Prueba de Integración JFrame VistaCliente
