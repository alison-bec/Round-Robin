import java.util.Iterator;


public class ProcessQueue<E> implements Iterable<E>{
    private Node <E> front;
    private Node <E> rear;
    private int size;

    public ProcessQueue(){
        front = null;
        rear = null;
        size = 0;
    }


    // Iterator to support the for-each loop
    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private Node<E> current = front;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public E next() {
                E data = current.data;
                current = current.next;
                return data;
            }
        };
    }





    public boolean isEmpty(){
        return front == null;
    }

    public void enqueue(E data){
        Node <E> newNode = new Node <E> (data);
        if(isEmpty()){
            front = newNode;
            rear = newNode;
        }else{
            rear.next = newNode;
            rear = newNode;
        }
        size++;
    }

    public int size(){
        return size;
    }

    public boolean add(E data){
        this.enqueue(data);
        return true;
    }

    public E dequeue(){
        if(isEmpty()){
            return null;
        }else{
            E data = front.data;
            front = front.next;
            size--;
            return data;
        }
    }

    public E poll(){
        return this.dequeue();
    }

    public E peek(){
        if(isEmpty()){
            return null;
        }else{
            return front.data;
        }
    }

    //no sabo si funcionan
    public void clear(){
        front = null;
        rear = null;
    }

/* 
    public static void main(String[] args) {
        ProcessQueue<Proceso> cola = new ProcessQueue<>();
        cola.enqueue(new Proceso("P1", "Process1", 100, 10, 5, 0));
        cola.enqueue(new Proceso("P2", "Process2", 150, 15, 10, 1));
        
        // Iterate through the queue and print process details
        for (Proceso p : cola) {
            System.out.printf("| %-5s | %-7s | %-7d | %-12d | %-10d | %-12d |\n",
                              p.id, p.nombre, p.tamano, p.tiempoEjecucion, p.tiempoRestante, p.tiempoLlegada);
        }
    }
*/

    
    
}