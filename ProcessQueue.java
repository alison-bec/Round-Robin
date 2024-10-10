

public class ProcessQueue<E>{
    private Node <E> front;
    private Node <E> rear;

    public ProcessQueue(){
        front = null;
        rear = null;
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
    }

    public E dequeue(){
        if(isEmpty()){
            return null;
        }else{
            E data = front.data;
            front = front.next;
            return data;
        }
    }

    public E peek(){
        if(isEmpty()){
            return null;
        }else{
            return front.data;
        }
    }

    public static void main(String[] args) {
        ProcessQueue <String> queue = new ProcessQueue <String> ();
        queue.enqueue("A");
        queue.enqueue("B");
        queue.enqueue("C");
        queue.enqueue("D");
        queue.enqueue("E");
        System.out.println(queue.dequeue());
        System.out.println(queue.dequeue());
        System.out.println(queue.dequeue());
        System.out.println(queue.dequeue());
        System.out.println(queue.dequeue());
    }
    
}