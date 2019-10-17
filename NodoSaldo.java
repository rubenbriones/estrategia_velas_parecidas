import java.util.GregorianCalendar;

/**
 * Esta es una interfaz que tiene que implementar cada Operacion/Apuesta/etc de las que van a componer una clase Saldo.
 */
public interface NodoSaldo<E> extends Comparable<E>{
    public abstract GregorianCalendar getFecha();    
    public abstract double getGan();
    public abstract String toString(); //separando las columnas con ";"
    public abstract int compareTo(E otro);
}
