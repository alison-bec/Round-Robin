import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

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
    int ultimoTiempoCPU = -1;  
    int contadorReinserciones = 0;  

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

public class RoundRobin {
    private static final int MEMORIA_MAXIMA = 1024;
    private static int memoriaDisponible = MEMORIA_MAXIMA;
    private static int quantum = 4; 
    private static int contadorId = 1; 
    private static int tiempoGlobal = 0;  

    //ahora esta sera mediano plazo
    private static ProcessQueue<Proceso> colaMedianoPlazo = new ProcessQueue<Proceso>();

    //private static Queue<Proceso> colaMedianoPlazo = new LinkedList<>();
    private static Queue<Proceso> colaCortoPlazo = new LinkedList<>();
    private static Queue<Proceso> colaCompletados = new LinkedList<>();
    private static List<Proceso> listaProcesosPendientes = new ArrayList<>();
    private static Queue<Proceso> colaProcesosNuevos = new LinkedList<>();
    private static Queue<Proceso> colaReinsertados = new LinkedList<>(); 

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
        colaMedianoPlazo.clear();
        colaCortoPlazo.clear();
        colaCompletados.clear();
        listaProcesosPendientes.clear();
        colaProcesosNuevos.clear();
        colaReinsertados.clear();
        memoriaDisponible = MEMORIA_MAXIMA;
        tiempoGlobal = 0;
        contadorId = 1;
        System.out.println("Simulación reiniciada. Memoria y colas de procesos vaciadas.");
    }

    private static void correrSimulacionAutomatica() {
        listaProcesosPendientes.add(new Proceso(generarId(), "Proceso_1", 100, 25, 1, 0)); 
        listaProcesosPendientes.add(new Proceso(generarId(), "Proceso_2", 100, 9, 1, 4)); 
        listaProcesosPendientes.add(new Proceso(generarId(), "Proceso_3", 100, 12, 1, 8)); 
        listaProcesosPendientes.add(new Proceso(generarId(), "Proceso_4", 100, 8, 1, 9)); 
    
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
            listaProcesosPendientes.add(proceso);
            System.out.print("¿Desea agregar otro proceso? (s/n): ");
            String respuesta = scanner.next();
            if (respuesta.equalsIgnoreCase("n")) {
                break;
            }
        }
    }

    private static void mostrarProcesosIniciales() {
        System.out.println("\n******** Procesos Iniciales ********");
        imprimirLista(listaProcesosPendientes, "Procesos Pendientes por Llegar");
    }

    private static void roundRobin() {
        while (!listaProcesosPendientes.isEmpty() || !colaMedianoPlazo.isEmpty() || !colaCortoPlazo.isEmpty() || !colaReinsertados.isEmpty()){
            cargarProcesosAlSistema();
            cargarProcesosEnMemoria();

            Proceso procesoActual = colaCortoPlazo.poll();

            if (procesoActual != null) {
                if (procesoActual.tiempoInicio == -1) {  
                    procesoActual.tiempoInicio = tiempoGlobal;
                }

                procesoActual.ultimoTiempoCPU = tiempoGlobal;

                System.out.println("\n--- Proceso en ejecución ---");
                int tiempoEjecucionActual = (procesoActual.tiempoRestante >= quantum) ? quantum : procesoActual.tiempoRestante;

                for (int i = 0; i < tiempoEjecucionActual; i++) {
                    System.out.printf("%s en ejecución %d msg\n", procesoActual.id, procesoActual.tiempoRestante - i);
                }

                tiempoGlobal += tiempoEjecucionActual;
                procesoActual.tiempoRestante -= tiempoEjecucionActual;

                if (procesoActual.tiempoRestante > 0) {
                    procesoActual.contadorReinserciones++;
                    memoriaDisponible += procesoActual.tamano;  
                    colaReinsertados.add(procesoActual);  
                    imprimirColas("Proceso " + procesoActual.id + " reinsertado en la cola de reinsertados. Memoria disponible: " + memoriaDisponible + " KB.");
                } else {
                    procesoActual.tiempoFinalizacion = tiempoGlobal;
                    memoriaDisponible += procesoActual.tamano;
                    colaCompletados.add(procesoActual);
                    imprimirColas("Proceso " + procesoActual.id + " completado. Memoria disponible: " + memoriaDisponible + " KB.");
                }
                cargarProcesosEnMemoria();
            } else {
                tiempoGlobal++;
            }
        }

        System.out.println("\n***** Todos los procesos han sido completados *****\n");
        mostrarTiempos();
    }

    private static void cargarProcesosAlSistema() {
        listaProcesosPendientes.removeIf(proceso -> {
            if (proceso.tiempoLlegada <= tiempoGlobal) {
                colaProcesosNuevos.add(proceso);
                return true;
            }
            return false;
        });

        while (!colaProcesosNuevos.isEmpty()) {
            colaMedianoPlazo.add(colaProcesosNuevos.poll());
        }

        while (!colaReinsertados.isEmpty()) {
            colaMedianoPlazo.add(colaReinsertados.poll());
        }
    }

    private static void cargarProcesosEnMemoria() {
        while (!colaMedianoPlazo.isEmpty()) {
            Proceso proceso = colaMedianoPlazo.peek();
            if (proceso.tamano <= memoriaDisponible) {
                colaMedianoPlazo.poll();
                memoriaDisponible -= proceso.tamano;
                colaCortoPlazo.add(proceso);
                imprimirColas("Proceso subido a corto plazo: " + proceso.id + ". Memoria disponible: " + memoriaDisponible + " KB.");
            } else {
                break;  
            }
        }
    }

    private static void imprimirLista(List<Proceso> lista, String nombreLista) {
        System.out.println("\n" + nombreLista + ":");
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.printf("| %-5s | %-7s | %-7s | %-12s | %-10s | %-12s |\n", 
                          "ID", "Nombre", "Tamaño", "Tiempo Total", "Restante", "Tiempo Llegada");
        System.out.println("------------------------------------------------------------------------------------------");

        for (Proceso p : lista) {
            System.out.printf("| %-5s | %-7s | %-7d | %-12d | %-10d | %-12d |\n",
                              p.id, p.nombre, p.tamano, p.tiempoEjecucion, p.tiempoRestante, p.tiempoLlegada);
        }

        System.out.println("------------------------------------------------------------------------------------------");
    }

    private static void imprimirColas(String mensaje) {
        System.out.println("\n" + mensaje);
        System.out.println("\n--- Cola de Procesos Listos (Mediano Plazo) ---");
        imprimirCola(colaMedianoPlazo, "Cola de procesos listos (espera para subir a memoria)");

        System.out.println("\n--- Cola de Procesos Listos para Ejecución ---");
        imprimirCola(colaCortoPlazo, "Cola de procesos listos para ejecución");
    }

    private static void imprimirCola(Queue<Proceso> cola, String nombreCola) {
        System.out.println("\n" + nombreCola + ":");
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.printf("| %-5s | %-7s | %-7s | %-12s | %-10s | %-12s |\n", 
                          "ID", "Nombre", "Tamaño", "Tiempo Total", "Restante", "Tiempo Llegada");
        System.out.println("------------------------------------------------------------------------------------------");

        for (Proceso p : cola) {
            System.out.printf("| %-5s | %-7s | %-7d | %-12d | %-10d | %-12d |\n",
                              p.id, p.nombre, p.tamano, p.tiempoEjecucion, p.tiempoRestante, p.tiempoLlegada);
        }

        System.out.println("------------------------------------------------------------------------------------------");
    }


    private static void imprimirCola(ProcessQueue<Proceso> cola, String nombreCola) {
        System.out.println("\n" + nombreCola + ":");
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.printf("| %-5s | %-7s | %-7s | %-12s | %-10s | %-12s |\n", 
                          "ID", "Nombre", "Tamaño", "Tiempo Total", "Restante", "Tiempo Llegada");
        System.out.println("------------------------------------------------------------------------------------------");

        for (Proceso p : cola) {
            System.out.printf("| %-5s | %-7s | %-7d | %-12d | %-10d | %-12d |\n",
                              p.id, p.nombre, p.tamano, p.tiempoEjecucion, p.tiempoRestante, p.tiempoLlegada);
        }

        System.out.println("------------------------------------------------------------------------------------------");
    }


    private static void mostrarTiempos() {
        System.out.println("\n********* Tiempos Finales de los Procesos *********");
        System.out.printf("| %-5s | %-12s | %-15s | %-10s | %-10s |\n", "ID", "Tiempo Espera", "Tiempo Ejecución", "Tiempo Respuesta", "Tiempo Llegada");
        int totalEspera = 0, totalEjecucion = 0, totalRespuesta = 0;

        for (Proceso p : colaCompletados) {
            int tiempoPrevio = p.contadorReinserciones * quantum;

            int tiempoRespuesta = p.tiempoInicio - p.tiempoLlegada;
            int tiempoEjecucion = p.tiempoFinalizacion - p.tiempoLlegada;
            int tiempoEspera = p.ultimoTiempoCPU - p.tiempoLlegada - tiempoPrevio;

            totalEspera += tiempoEspera;
            totalEjecucion += tiempoEjecucion;
            totalRespuesta += tiempoRespuesta;

            System.out.printf("| %-5s | %-12d | %-15d | %-10d | %-10d |\n", p.id, tiempoEspera, tiempoEjecucion, tiempoRespuesta, p.tiempoLlegada);
        }

        int n = colaCompletados.size();
        System.out.printf("\nTiempos Promedio: Espera = %.2f, Ejecución = %.2f, Respuesta = %.2f\n",
                          (double) totalEspera / n, (double) totalEjecucion / n, (double) totalRespuesta / n);
    }

    private static String generarId() {
        return "P" + (contadorId++);
    }
}

