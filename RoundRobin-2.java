import java.util.Scanner;

class Proceso implements Runnable {
    String id;
    String nombre;
    int tamano;
    int tiempoEjecucion;
    int tiempoRestante;
    int prioridad;
    int tiempoLlegada;

    int tiempoInicio = -1;  
    int tiempoFinalizacion = 0;

    public Proceso(String id, String nombre, int tamano, int tiempoEjecucion, int prioridad, int tiempoLlegada) {
        this.id = id;
        this.nombre = nombre;
        this.tamano = tamano;
        this.tiempoEjecucion = tiempoEjecucion;
        this.tiempoRestante = tiempoEjecucion;
        this.prioridad = prioridad;
        this.tiempoLlegada = tiempoLlegada;
    }

    @Override
    public void run() {}

    @Override
    public String toString() {
        return String.format("| %-5s | %-7s | %-7d | %-12d | %-10d | %-12d |", id, nombre, tamano, tiempoEjecucion, tiempoRestante, tiempoLlegada);
    }
}

class Nodo {
    Proceso proceso;
    Nodo siguiente;

    public Nodo(Proceso proceso) {
        this.proceso = proceso;
        this.siguiente = null;
    }
}

class ListaProcesos {
    private Nodo cabeza = null;
    private Nodo cola = null;
    private String nombreCola;

    public ListaProcesos(String nombreCola) {
        this.nombreCola = nombreCola;
    }

    public Nodo getCabeza() {
        return cabeza;
    }

    public int size() {
        int count = 0;
        Nodo actual = cabeza;
        while (actual != null) {
            count++;
            actual = actual.siguiente;
        }
        return count;
    }

    public void insertar(Proceso proceso) {
        Nodo nuevoNodo = new Nodo(proceso);
        if (cabeza == null) {
            cabeza = nuevoNodo;
            cola = nuevoNodo;
        } else {
            cola.siguiente = nuevoNodo;
            cola = nuevoNodo;
        }
        imprimirCola();
    }

    public Proceso eliminar() {
        if (cabeza == null) {
            return null;
        }
        Proceso proceso = cabeza.proceso;
        cabeza = cabeza.siguiente;
        if (cabeza == null) {
            cola = null;
        }
        imprimirCola();
        return proceso;
    }

    public boolean estaVacia() {
        return cabeza == null;
    }

    public Proceso peek() {
        return cabeza != null ? cabeza.proceso : null;
    }

    public void imprimirCola() {
        System.out.println("\n--- " + nombreCola + " ---");
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.printf("| %-5s | %-7s | %-7s | %-12s | %-10s | %-12s |\n",
                "ID", "Nombre", "Tamaño", "Tiempo Total", "Restante", "Tiempo Llegada");
        System.out.println("------------------------------------------------------------------------------------------");
        
        Nodo actual = cabeza;
        while (actual != null) {
            System.out.println(actual.proceso);
            actual = actual.siguiente;
        }
        System.out.println("------------------------------------------------------------------------------------------");
    }
}

public class RoundRobin {
    private static final int MEMORIA_MAXIMA = 1024;
    private static int memoriaDisponible = MEMORIA_MAXIMA;
    private static int quantum = 2; 
    private static int contadorId = 1; 
    private static int tiempoGlobal = 0;

    private static ListaProcesos colaListos = new ListaProcesos("Cola de Procesos en Espera");
    private static ListaProcesos colaListosEjecucion = new ListaProcesos("Cola de Procesos en RAM");
    private static ListaProcesos colaCompletados = new ListaProcesos("Cola de Procesos Finalizados");

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean salir = false;

        while (!salir) {
            System.out.println("\n----- Menú Round Robin -----");
            System.out.println("1. Correr simulación automática");
            System.out.println("2. Añadir procesos manualmente");
            System.out.println("3. Salir");
            System.out.print("Seleccione una opción: ");

            int opcion = scanner.nextInt();
            switch (opcion) {
                case 1:
                    resetearSimulacion();
                    System.out.println("Quantum seleccionado: " + quantum + " ms");
                    correrSimulacionAutomatica();
                    break;
                case 2:
                    resetearSimulacion();
                    System.out.print("Ingrese el quantum para los procesos: ");
                    quantum = scanner.nextInt();
                    System.out.println("Quantum seleccionado: " + quantum + " ms");
                    agregarProcesosManualmente();
                    ajustarTiempos();
                    mostrarProcesosIniciales();
                    roundRobin();
                    break;
                case 3:
                    salir = true;
                    System.out.println("Saliendo...");
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        }
        scanner.close();
    }

    private static void resetearSimulacion() {
        colaListos = new ListaProcesos("Cola de Procesos en Espera");
        colaListosEjecucion = new ListaProcesos("Cola de Procesos en RAM");
        colaCompletados = new ListaProcesos("Cola de Procesos Finalizados");
        memoriaDisponible = MEMORIA_MAXIMA;
        tiempoGlobal = 0;
        contadorId = 1;
        System.out.println("Simulación reiniciada. Memoria y colas de procesos vaciadas.");
    }

    private static void correrSimulacionAutomatica() {
        colaListos.insertar(new Proceso(generarId(), "Proceso_A", 200, 5, 1, 0)); 
        colaListos.insertar(new Proceso(generarId(), "Proceso_B", 300, 4, 1, 1)); 
        colaListos.insertar(new Proceso(generarId(), "Proceso_C", 150, 6, 1, 2)); 
        colaListos.insertar(new Proceso(generarId(), "Proceso_D", 500, 3, 1, 3)); 
        colaListos.insertar(new Proceso(generarId(), "Proceso_E", 100, 8, 1, 4)); 

        System.out.println("Simulación automática creada con procesos de prueba.");
        mostrarProcesosIniciales();
        roundRobin();
    }

    private static void agregarProcesosManualmente() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String id = generarId();
            System.out.print("Ingrese el nombre del proceso: ");
            String nombre = scanner.next();
            System.out.print("Ingrese el tamaño del proceso (KB): ");
            int tamano = scanner.nextInt();
            System.out.print("Ingrese el tiempo de ejecución del proceso: ");
            int tiempoEjecucion = scanner.nextInt();
            System.out.print("Ingrese el tiempo de llegada del proceso: ");
            int tiempoLlegada = scanner.nextInt();

            Proceso proceso = new Proceso(id, nombre, tamano, tiempoEjecucion, 1, tiempoLlegada);
            colaListos.insertar(proceso);
            System.out.print("¿Desea agregar otro proceso? (s/n): ");
            String respuesta = scanner.next();
            if (respuesta.equalsIgnoreCase("n")) {
                break;
            }
        }
    }

    private static void ajustarTiempos() {
        Proceso primerProceso = colaListos.peek();
        if (primerProceso != null) {
            int tiempoInicial = primerProceso.tiempoLlegada;
            Nodo actual = colaListos.getCabeza();
            while (actual != null) {
                actual.proceso.tiempoLlegada -= tiempoInicial;
                actual = actual.siguiente;
            }
            tiempoGlobal = 0;
        }
    }

    private static void mostrarProcesosIniciales() {
        System.out.println("\n******** Procesos Iniciales ********");
        colaListos.imprimirCola();
    }

    private static void roundRobin() {
        cargarProcesosEnMemoria();
        while (!colaListos.estaVacia() || !colaListosEjecucion.estaVacia()) {
            Proceso procesoActual = colaListosEjecucion.eliminar();

            if (procesoActual != null) {
                if (procesoActual.tiempoInicio == -1) {  
                    procesoActual.tiempoInicio = tiempoGlobal;
                }

                System.out.println("\n--- Proceso en ejecución ---");
                int tiempoEjecucionActual = Math.min(quantum, procesoActual.tiempoRestante);
                
                for (int i = tiempoEjecucionActual; i > 0; i--) {
                    System.out.printf("%s en ejecución %d ms\n", procesoActual.id, procesoActual.tiempoRestante);
                    procesoActual.tiempoRestante--;
                    tiempoGlobal++;
                }

                if (procesoActual.tiempoRestante > 0) {
                    colaListos.insertar(procesoActual);
                    memoriaDisponible += procesoActual.tamano;
                } else {
                    procesoActual.tiempoFinalizacion = tiempoGlobal;
                    colaCompletados.insertar(procesoActual);
                    memoriaDisponible += procesoActual.tamano;
                }
                cargarProcesosEnMemoria();
            }
        }

        System.out.println("\n***** Todos los procesos han sido completados *****\n");
        mostrarTiempos();
    }

    private static void cargarProcesosEnMemoria() {
        while (!colaListos.estaVacia()) {
            Proceso proceso = colaListos.peek();
            if (proceso != null && proceso.tamano <= memoriaDisponible) {
                colaListosEjecucion.insertar(proceso);
                memoriaDisponible -= proceso.tamano;
                colaListos.eliminar();
            } else {
                break; 
            }
        }
    }

    private static String generarId() {
        return "P" + (contadorId++);
    }

    private static void mostrarTiempos() {
        System.out.println("\n********* Tiempos Finales de los Procesos *********");
        System.out.printf("| %-5s | %-12s | %-15s | %-10s | %-10s |\n", "ID", "Tiempo Espera", "Tiempo Ejecución", "Tiempo Respuesta", "Tiempo Llegada");
        int totalEspera = 0, totalEjecucion = 0, totalRespuesta = 0;

        Nodo actual = colaCompletados.getCabeza();
        while (actual != null) {
            Proceso p = actual.proceso;
            int tiempoEspera = p.tiempoInicio - p.tiempoLlegada;
            int tiempoEjecucion = p.tiempoFinalizacion - p.tiempoLlegada;
            int tiempoRespuesta = p.tiempoInicio - p.tiempoLlegada;

            totalEspera += tiempoEspera;
            totalEjecucion += tiempoEjecucion;
            totalRespuesta += tiempoRespuesta;

            System.out.printf("| %-5s | %-12d | %-15d | %-10d | %-10d |\n", p.id, tiempoEspera, tiempoEjecucion, tiempoRespuesta, p.tiempoLlegada);
            actual = actual.siguiente;
        }

        int n = colaCompletados.size();
        System.out.printf("\nTiempos Promedio: Espera = %.2f, Ejecución = %.2f, Respuesta = %.2f\n",
                (double) totalEspera / n, (double) totalEjecucion / n, (double) totalRespuesta / n);
    }
}
