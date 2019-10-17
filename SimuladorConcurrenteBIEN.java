import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.io.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 
 */
public class SimuladorConcurrenteBIEN{    
    //VARIABLES A MODIFICAR EN CADA SIMULACION    
    public static final GregorianCalendar fechaIni = new GregorianCalendar(2007,Calendar.JANUARY,1); //1 de enero del 2007
    public static final GregorianCalendar fechaFin = new GregorianCalendar(2010,Calendar.DECEMBER,31); //31 de diciembre del 2009
    
    public static int numeroDeDiasParaBuscar=1170; //este es el numero de dias en los que vamos a buscar hacia atras coincidencias con el dia actual. 260==1 anyo
    public static double errorTolerado=0.07;   //es el error que toleramos ara decir que un dia es estadisticamente igual a otro.
    public static double limiteMinGanancias=0;   //es el limite minimoq ue tendriamos que tener previsto ganar para el dia siguiente operar.Ponerlo siempre en positivo, que yo luego ya lo cambio para operar el lado corto.
    public static int coincidenciasMinimas=0; //es el numero de Cotizaciones "parecidas" que tenemos que haber encontrado para entrar en una operacion.
    
    public static String carpetaMercado = "IbexVisualChart";
    public static String[] tickersAsimular;    
    //FIN - VARIABLES A MODIFICAR EN CADA SIMULACION
    
    public static void main(String[] args)throws Exception{
        File f = new File("Cotizaciones Historicas\\"+carpetaMercado);
        File[] ficheros = f.listFiles();
        String[] aux = new String[ficheros.length];
        int numTickers=0;
        for(int cont=0; cont<ficheros.length; cont++){
            aux[numTickers++]=ficheros[cont].getName();
        }
        tickersAsimular = new String[numTickers];
        for(int cont=0; cont<numTickers; cont++){
            tickersAsimular[cont]=aux[cont];
        }
        
        
        for(numeroDeDiasParaBuscar=260; numeroDeDiasParaBuscar<=1300; numeroDeDiasParaBuscar+=130){ //130==1semestre. 8iteraciones
            System.out.println("===============================");
            System.out.println("NUMERO DE DIAS A BUSCAR: "+numeroDeDiasParaBuscar);
            System.out.println("===============================");
            algoritmo(true, 0.07);
        }
        
        /*for(errorTolerado=0.1; errorTolerado<=0.25; errorTolerado+=0.01){
            System.out.println("===============================");
            System.out.println("ERROR TOLERADO: "+errorTolerado);
            System.out.println("===============================");
            algoritmo(true,0.07);
        }*/
        
        /*for(limiteMinGanancias=0; limiteMinGanancias<=8; limiteMinGanancias+=1){
            System.out.println("===============================");
            System.out.println("LIMITE MINIMO DE GANANCIAS: "+limiteMinGanancias);
            System.out.println("===============================");
            algoritmo(true,0.07);
        }*/
        
        /*for(coincidenciasMinimas=0; coincidenciasMinimas<=5; coincidenciasMinimas+=1){
            System.out.println("===============================");
            System.out.println("COINCIDENCIAS MINIMAS: "+coincidenciasMinimas);
            System.out.println("===============================");
            algoritmo(true,0.07);
        }*/
        
        /*for(errorTolerado=0.05; errorTolerado<=0.4; errorTolerado+=0.05){
            for(limiteMinGanancias=0; limiteMinGanancias<=4; limiteMinGanancias+=0.5){
                System.out.println("===============================");
                System.out.println("ERROR TOLERADO: "+errorTolerado);
                System.out.println("LIMITE MINIMO DE GANANCIAS: "+limiteMinGanancias);
                System.out.println("===============================");
                algoritmo(true, 0.07);
            }
        }*/
        
        /*for(errorTolerado=0.05; errorTolerado<=0.25; errorTolerado+=0.05){ //5 iteraciones
            for(limiteMinGanancias=0.5; limiteMinGanancias<=3; limiteMinGanancias+=0.5){ //6 iteraciones
                for(coincidenciasMinimas=0; coincidenciasMinimas<=4; coincidenciasMinimas+=1){ //5 iteraciones
                    System.out.println("===============================");
                    System.out.println("ERROR TOLERADO: "+errorTolerado);
                    System.out.println("LIMITE MINIMO DE GANANCIAS: "+limiteMinGanancias);
                    System.out.println("COINCIDENCIAS MINIMAS: "+coincidenciasMinimas);
                    System.out.println("===============================");
                    algoritmo(true, 0.07);
                }
            }
        }*/
        
        //algoritmo(false, 0.07);
        
        System.out.println("YA HAN TERMINADO TODOS LOS TRABAJOS/OPTIMIZACIONES/HILOS");
    }
    
    /**
     * Aqui estan todas las instruccines/calculos que hay que hacer para simular el algoritmo. Lo pongo separado del main, para asi en el main
     * poner los bucles de optimizacion de variables solo, y el algoritmo en si lo pongo en este metodo aparte que no hay que modificar.
     * 
     * Solo le voy a pasar una variable booleana para indicar si quiero mostrar las estadicticas de cada Accion o solo las globales.
     * Y otra variable double para indicar las comisiones, de una operacion entera (entrada y salida incluidas), en porcentaje. 
     * Para 100.000€ en ahorro la comision es del 0.04% y para 50.000€ es del 0.07%. HAY QUE TENER EN CUENTA que se pueda meter ese capital
     * en la apertura de las acciones simuladas.
     */
    public static void algoritmo(boolean soloGlobales, double comisiones)throws Exception{
        long tiinicio=System.currentTimeMillis();
        
        //Estadisticas GLOBALES: diasEnElMercado, aciertos, ganancias.
        EstadisticasGlobales est = new EstadisticasGlobales();
        
        ExecutorService exec = Executors.newFixedThreadPool(tickersAsimular.length);        
        //Recorremos todas las acciones a simular.
        for(int j=0; j<tickersAsimular.length; j++){            
            HiloSimuladorBIEN h = new HiloSimuladorBIEN(tickersAsimular[j], carpetaMercado, fechaIni, fechaFin, est, comisiones, soloGlobales,
                                                        numeroDeDiasParaBuscar, errorTolerado, limiteMinGanancias, coincidenciasMinimas);
            exec.execute(h);
        }
        
        exec.shutdown();

        boolean tasksEnded = exec.awaitTermination(5, TimeUnit.MINUTES);
        if(tasksEnded){ //Comprobamos que han acabdo todos los hilos para entonces mostrar las estadisticas por pantalla.
            System.out.print(est.impresionPorPantalla);
        }
        
        Saldo<Operacion> s = new Saldo<Operacion>(est.opTotales, getYearsDifference(fechaIni, fechaFin));
        //s.imprimirTodo();
        //System.out.println(s.drawdown);
        //System.out.println(s.anyos);
        System.out.println("Ratio: "+s.calcularRatio());
        
        //Imprimimos por pantalla las estadisticas rsultantes de la simulacion GLOBAL.
        //         System.out.println("ESTADISTICAS GLOBALES");
        //         System.out.println("Acciones: "); for(int i=0; i<tickersAsimular.length; i++) System.out.print(tickersAsimular[i]+", ");
        //         System.out.println();
        //         System.out.println("Operaciones LARGOS: "+est.diasLargosT);
        //         System.out.println("% Acierto LARGOS: "+(est.acLargosT*100.0/est.diasLargosT));
        System.out.println("Ganancias en % LARGOS: "+est.ganLargosT);
        //         System.out.println("Ganancias Media en % por Op. LARGOS: "+(est.ganLargosT/est.diasLargosT));
        //         System.out.println("Operaciones CORTOS: "+est.diasCortosT);
        //         System.out.println("% Acierto CORTOS: "+(est.acCortosT*100.0/est.diasCortosT));
        System.out.println("Ganancias en % CORTOS: "+est.ganCortosT);
        //         System.out.println("Ganancias Media en % por Op. CORTOS: "+(est.ganCortosT/est.diasCortosT));
        System.out.println("Operaciones: "+(est.diasLargosT+est.diasCortosT));
        //         System.out.println("% Acierto: "+((est.acLargosT+est.acCortosT)*100.0/(est.diasLargosT+est.diasCortosT)));
        System.out.println("Ganancias en %: "+(est.ganLargosT+est.ganCortosT));
        //         System.out.println("Ganancias Media en % por Op.: "+((est.ganLargosT+est.ganCortosT)/(est.diasLargosT+est.diasCortosT)));
        
        
        long tifinal=System.currentTimeMillis();
        System.out.println("CONCURRENTEMENTE el tiempo gastado ha sido: "+(tifinal-tiinicio));
    }
    
    /**                                     
     * Este metodo calcula la diferencia en ANYOS entre dos fechas dadas     se le pasa en el primer parametro la fecha anterior     y en el                                
     * segundo parametro la fecha posterior. Lo vamos a utilizar sobre todo para calcular ratios.                                       
     */                                     
    public static final double getYearsDifference(GregorianCalendar date1, GregorianCalendar date2) {                                     
        int m1 = date1.get(Calendar.YEAR) * 12 + (date1.get(Calendar.MONTH)+1);                                       
        int m2 = date2.get(Calendar.YEAR) * 12 + (date2.get(Calendar.MONTH)+1);
        return (m2 - m1) /12.0 ;                                        
    }
}
