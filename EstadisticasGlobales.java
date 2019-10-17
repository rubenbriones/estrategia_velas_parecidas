import java.util.ArrayList;

/**
 * 
 */
public class EstadisticasGlobales{
    //Estadisticas GLOBALES: diasEnElMercado, aciertos, ganancias.
    int diasLargosT=0, diasCortosT=0;
    int acLargosT=0, acCortosT=0;
    double ganLargosT=0, ganCortosT=0;
    
    //Creamos un ArrayList en el que guardaremos todas las operaciones.
    ArrayList<Operacion> opTotales = new ArrayList<Operacion>();
    
    //Aqui iremos almacenando todas las lineas que habria que haber imprimido por pantalla en la simulacion de cada accion,
    //para imprimirlas luego todas juntas al final, y que no aparezcan desordenadas.
    String impresionPorPantalla="";
        
    public EstadisticasGlobales(){}
    
    public synchronized void actualizar(int diasL, int diasC, int acL, int acC, double ganL, double ganC, ArrayList<Operacion> ops){
        diasLargosT+=diasL;
        diasCortosT+=diasC;
        acLargosT+=acL;
        acCortosT+=acC;
        ganLargosT+=ganL;
        ganCortosT+=ganC;
        opTotales.addAll(ops);
    }
    
    public synchronized void agregarImpresion(String s){
        impresionPorPantalla += s;
    }
}
