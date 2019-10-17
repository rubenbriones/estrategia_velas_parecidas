import java.util.GregorianCalendar;

/**
 * 
 */
public class Operacion implements NodoSaldo<Operacion>{
    public String fecha;
    public GregorianCalendar fec;
    public String accion;
    public String lado; //largo o corto
    public double gan;
    
    public Operacion(String fecha, GregorianCalendar fec, String accion, String lado, double gan){
        this.fecha=fecha;
        this.fec=fec;
        this.accion=accion;
        this.lado=lado;
        this.gan=gan;
    }
    
    @Override
    public GregorianCalendar getFecha(){ return fec; }
    @Override
    public double getGan(){ return gan; }
    @Override
    public String toString(){ return fecha+";"+accion+";"+lado+";"+gan; }
    
    /** 
     * Este compareTo es para implementar la interfaz Comparable
     * Lo unico que hace es ver que Operacion ha es anterior a otra en funcion de la fecha.
     * Si la fecha es la misma ordenamos segun la ganancia, esto es para que tanto secuencialmente como concurrentemente nos de siempre
     * el mismo resultado exacto. Si no hicieramos esto el resultado sería muy muy parecido pero no exacto.
     */
    @Override
    public int compareTo(Operacion otro){
        int res = this.getFecha().compareTo(otro.getFecha());
        if(res==0) res = (int)(this.getGan() - otro.getGan());
        return res;
    }
}
