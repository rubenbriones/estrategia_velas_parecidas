import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * 
 */
public class Cotizacion implements Comparable<Cotizacion>{
    public String fecha;
    public GregorianCalendar fec;
    public double open;
    public double high;
    public double low;
    public double close;
    public int volumen;
    public double closeAdj; //este es el cierre ajustado (close adjusted).

    //Los 3 datos siguientes estan en porcentaje
    public double rango; //este dato vendria a ser lo mismo que la subidaDeEsteDia, es decir, si compramos a la apertura y vendemos al cierre.
    public double colAlta; //este dato siempre sera negativo
    public double colBaja; //este dato siempre sera positivo

    public Cotizacion(String fecha, double open, double high, double low, double close, int volumen, double closeAdj){
        this.fecha = fecha;
        try{ //este parseador es para los historicos de yahoo
            SimpleDateFormat formateador = new SimpleDateFormat("yyyy-MM-dd");
            fec = new GregorianCalendar();
            fec.setTime(formateador.parse(fecha));
        }catch(ParseException e){
            try{ //este parseados es para los hitoricos sacados de visual chart
                SimpleDateFormat formateador = new SimpleDateFormat("yyyyMMdd");
                fec = new GregorianCalendar();
                fec.setTime(formateador.parse(fecha));
            }catch(ParseException e2){
                System.out.println(e.toString());System.err.println("No se ha podido parsear bien la fecha: "+fecha);System.exit(0);
            }
        }
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volumen = volumen;
        this.closeAdj = closeAdj;

        double max = close, min=open; //esto esta pensado si la barra es verde. (close>open)
        if(close<open){max=open; min=close;} //si la barra es roja, cambiamos los valores

        this.rango = ((close-open)/open)*100.0;
        this.colAlta = ((max-high)/high)*100.0;
        this.colBaja = ((min-low)/low)*100.0;
    }
    
    /**
     * Sobreescribimos el metodo compareTo para poder ordenar los ArrayList de tipo Cotizacion.
     * El orden sera de mas nuevos a mas antiguos (tal y como se ordenan las cotizaciones en los datos historicos de yahoo,
     * y al reves de como estan ordenados en visual chart).
     */
    public int compareTo(Cotizacion otro){
        return -this.fec.compareTo(otro.fec); //el orden es inverso al normal, por eso ponemos el signo menos delante.
    }
}