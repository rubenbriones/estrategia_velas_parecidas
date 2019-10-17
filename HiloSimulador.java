import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.Collections;
import java.io.*;

/**
 * 
 */
public class HiloSimulador extends Thread{
    //Este ArrayList contendra todas las "Cotizacion" de la accion que estemos analizando.
    public  ArrayList<Cotizacion> array;
    
    public  int indiceIni=0; //es el indice/posicion del ArrayList de las cotizaciones que corresponde con el primer dia que vamos a simular.
    public  int indiceFin=0; //es el indice/posicion del ArrayList de las cotizaciones que corresponde con el ultimo dia que vamos a simular.
    
    public  GregorianCalendar fechaIni;
    public  GregorianCalendar fechaFin;
    
    public  int numeroDeDiasParaBuscar; //este es el numero de dias en los que vamos a buscar hacia atras coincidencias con el dia actual. 260==1 anyo
    public  double errorTolerado;   //es el error que toleramos ara decir que un dia es estadisticamente igual a otro.
    public  double limiteMinGanancias;   //es el limite minimoq ue tendriamos que tener previsto ganar para el dia siguiente operar.Ponerlo siempre en positivo, que yo luego ya lo cambio para operar el lado corto.
    public  int coincidenciasMinimas; //es el numero de Cotizaciones "parecidas" que tenemos que haber encontrado para entrar en una operacion.
    
    public  double comisiones;
    public  boolean mostrarSoloGlobales;
    
    public  String tickerAccion; //aqui estara el ticker de la accin que estemos analizando
    public  String carpetaMercado; //aqui estara el nombre de la carpeta/mercado a la que pertenece la accion que estamos analizando.
    
    //Este es el objeto/MONITOR en el que se van guardando las estadisticas GLOBALES. Hay que utilizar con metodos synchronized.
    public  EstadisticasGlobales estGlo;
    
    public HiloSimulador(String ticker, String carpeta, GregorianCalendar fecIni, GregorianCalendar fecFin, EstadisticasGlobales estadisticas, 
                         double comision,  boolean soloGlob, int dias, double err, double lim, int coin){
        tickerAccion=ticker;
        carpetaMercado=carpeta;
        
        fechaIni=fecIni;
        fechaFin=fecFin;
        
        estGlo=estadisticas;
        
        comisiones=comision;
        mostrarSoloGlobales=soloGlob;
        
        numeroDeDiasParaBuscar=dias;
        errorTolerado=err;
        limiteMinGanancias=lim;
        coincidenciasMinimas=coin;
    }
    
    public void run(){
        //este ArrayList contendra todas las "Cotizacion" de la accion que estemos analizando.
        array = leerCSVvisualchart(tickerAccion, carpetaMercado);
        
        //Estadisticas de cada Accion: diasEnElMercado, aciertos, ganancias.
        int diasLargos=0, diasCortos=0;
        int acLargos=0, acCortos=0;
        double ganLargos=0, ganCortos=0;
        
        //Creamos un ArrayList en el que guardaremos todas las operaciones realizadas sobre la accion que estemos analizando.
        ArrayList<Operacion> operaciones = new ArrayList<Operacion>();
            
        //Recorremos todas las "Cotizacion" de la accion que estemos analiando. Como las "Cotizacion" estan en orden inverso en el "array,
        //es decir, las ultimas cotizaciones estan en las posiciones 0,1,... y las mas antiguas en las 3569,3568,.. entonces hacemos un recorrido con i--.
        for(int i=array.size()-1; i>0; i--){
            //comprobamos que la fecha de la cotizacion a analizar sea posterior a la fechaIni, si no es asi nos saltamos esta iteracion.
            if(array.get(i).fec.compareTo(fechaIni)<0){ // esto es lo mismo que fec.before(fechaIni) 
                indiceIni=i-1;
                continue;
            }                              
            //comprobamos que la fecha de la cotizacion a analizar sea anterior a la fechaFin, si no es asi salimos del bucle.
            if(array.get(i).fec.compareTo(fechaFin)>0) { // esto es lo mismo que fec.fater(fechaFin)
                indiceFin=i;
                break;
            }
            
            //Creamos un array con la informacion de lo que ha pasado cuando ha habido anteriormente una Cotizacion parecida a la actual.
            //La pos. 0 es el numero Cotizaciones parecidas que ha habido, la pos. 1 es el porcentaje de dias alciatas,
            //y la pos. 2 es la gananciaMedia obtenida al dia siguiente.
            double[] estadisticas = buscarCotizacionesParecidas(i, errorTolerado);
            
            //comprobamos si la gananciaMedia del dia siguiente es superior a nuestro limite en positivo, para asi entrar LARGOS.
            if(estadisticas[2] > limiteMinGanancias && estadisticas[0]>=coincidenciasMinimas){
                diasLargos++;
                if(array.get(i-1).rango-comisiones>0) acLargos++; //comprobamos si hubieramos acertado
                ganLargos+= array.get(i-1).rango - comisiones; //sumamos la ganancia/perdida que hubieramos tenido.
                Operacion op = new Operacion(array.get(i-1).fecha, array.get(i-1).fec, tickerAccion, "LARGO", array.get(i-1).rango-comisiones);
                operaciones.add(op);
                //System.out.println("LARGO;"+tickersAsimular[j]+";"+array.get(i).fecha+";"+(array.get(i-1).rango-comisiones)+";"+estadisticas[0]+";"+estadisticas[1]+";"+estadisticas[2]);
            }
            //comprobamos si la gananciaMedia del dia siguiente es superior (mas negativa) a nuestro limite en negativo, para asi entrar CORTOS.
            else{
                if(estadisticas[2] < -1*limiteMinGanancias && estadisticas[0]>=coincidenciasMinimas){
                    diasCortos++;
                    if(-1*array.get(i-1).rango-comisiones>0) acCortos++; //comprobamos si hubieramos acertado, es decir, si al dia siguiente la accion bajo.
                    ganCortos+= -1*array.get(i-1).rango - comisiones; //sumamos la ganancia/perdida que hubieramos tenido (que es el rango del dia siguiente *-1 ya que entramos cortos).
                    Operacion op = new Operacion(array.get(i-1).fecha, array.get(i-1).fec, tickerAccion, "CORTO", -1*array.get(i-1).rango-comisiones);
                    operaciones.add(op);
                    //System.out.println("CORTO;"+tickersAsimular[j]+";"+array.get(i).fecha+";"+(-1*array.get(i-1).rango-comisiones)+";"+estadisticas[0]+";"+estadisticas[1]+";"+estadisticas[2]);
                }
            }
        }
        
        String impresion="";
        if(!mostrarSoloGlobales){
            //Imprimimos por pantalla las estadisticas rsultantes de la simulacion para la accion simulada.
            impresion+= "Accion: "+tickerAccion+"\n";
            impresion+= "Operaciones LARGOS: "+diasLargos+"\n";
            impresion+= "% Acierto LARGOS: "+(acLargos*100.0/diasLargos)+"\n";
            impresion+= "Ganancias en % LARGOS: "+ganLargos+"\n";
            impresion+= "Ganancias Media en % por Op. LARGOS: "+(ganLargos/diasLargos)+"\n";
            impresion+= "Operaciones CORTOS: "+diasCortos+"\n";
            impresion+= "% Acierto CORTOS: "+(acCortos*100.0/diasCortos)+"\n";
            impresion+= "Ganancias en % CORTOS: "+ganCortos+"\n";
            impresion+= "Ganancias Media en % por Op. CORTOS: "+(ganCortos/diasCortos)+"\n";
            impresion+= "Operaciones: "+(diasLargos+diasCortos)+"\n";
            impresion+= "% Acierto: "+((acLargos+acCortos)*100.0/(diasLargos+diasCortos))+"\n";
            impresion+= "Ganancias en %: "+(ganLargos+ganCortos)+"\n";
            impresion+= "Ganancias Media en % por Op.: "+((ganLargos+ganCortos)/(diasLargos+diasCortos))+"\n";
            
            Saldo<Operacion> sal = new Saldo<Operacion>(operaciones, getYearsDifference(fechaIni, fechaFin));
            impresion+= "Ratio: "+sal.calcularRatio()+"\n";            
            
            impresion+= "----------------------------------------------"+"\n";
            
            estGlo.agregarImpresion(impresion);
        }
        
        
        //Actualizamos las estadisticas Globales. DE MANERA SYNCHRONIZED.
        estGlo.actualizar(diasLargos, diasCortos, acLargos, acCortos, ganLargos, ganCortos, operaciones);  
    }
    
    public  double redondear( double numero, int decimales ) {
        return Math.round(numero*Math.pow(10,decimales))/Math.pow(10,decimales);
    }
    
    /**                                     
     * Este metodo calcula la diferencia en ANYOS entre dos fechas dadas     se le pasa en el primer parametro la fecha anterior     y en el                                
     * segundo parametro la fecha posterior. Lo vamos a utilizar sobre todo para calcular ratios.                                       
     */                                     
    public  final double getYearsDifference(GregorianCalendar date1, GregorianCalendar date2) {                                     
        int m1 = date1.get(Calendar.YEAR) * 12 + (date1.get(Calendar.MONTH)+1);                                       
        int m2 = date2.get(Calendar.YEAR) * 12 + (date2.get(Calendar.MONTH)+1);
        return (m2 - m1) /12.0 ;                                        
    }
    
    /**
     * Le pasamos como parametro el ticker de la accion y la carpeta/mercado en la que esta (dentro de "Cotizaciones Historicas"), 
     * y nos devuelve un ArrayList con todas las cotiaciones que tenemos descargadas.
     * SOLO SIRVE PARA LOS ARCHIVOS HISTORICOS DE YAHOO (descragados con este mismo programa)!!
     */
    public static ArrayList<Cotizacion> leerCSV(String ticker, String carpeta){
        try{
            File f = new File("Cotizaciones Historicas\\"+carpeta+"\\"+ticker+".csv");
            Pattern p = Pattern.compile(",|\n"); //uso un pattern pues tengo que anyadir como delimiter el \n, ya que no hay una coma al final de cada linea.
            Scanner s = new Scanner(f).useDelimiter(p);
            s.nextLine(); //nos saltamos la primera linea del .csv en la que esta la leyenda de cada columna.
            
            ArrayList<Cotizacion> res = new ArrayList<Cotizacion>();
            while(s.hasNextLine()){
                String fecha=s.next();
                double open=Double.parseDouble(s.next());
                double high=Double.parseDouble(s.next());
                double low=Double.parseDouble(s.next());
                double close=Double.parseDouble(s.next());
                int volumen=s.nextInt();
                double closeAdj=Double.parseDouble(s.next());
                Cotizacion c = new Cotizacion(fecha, open, high, low, close, volumen, closeAdj);
                res.add(c);
                s.nextLine();
            }

            return res;
        }catch(Exception e){System.err.println("PROBLEMA - No se ha encontrado el archivo .csv de la accion: "+ticker);}

        return null;
    }    
    /**
     * Le pasamos como parametro el ticker de la accion y la carpeta/mercado en la que esta (dentro de "Cotizaciones Historicas"), 
     * y nos devuelve un ArrayList con todas las cotiaciones que tenemos descargadas.
     * SOLO SIRVE PARA LOS ARCHIVOS HISTORICOS DE VISUAL CHART (exportados a mano del visual chart)!!
     */
    public static ArrayList<Cotizacion> leerCSVvisualchart(String ticker, String carpeta){
        try{
            File f = new File("Cotizaciones Historicas\\"+carpeta+"\\"+ticker);
            Pattern p = Pattern.compile(",|\n"); //uso un pattern pues tengo que anyadir como delimiter el \n, ya que no hay una coma al final de cada linea.
            Scanner s = new Scanner(f).useDelimiter(p);
            s.nextLine(); //nos saltamos la primera linea en la que esta la leyenda de cada columna.
            
            ArrayList<Cotizacion> res = new ArrayList<Cotizacion>();
            while(s.hasNextLine()){
                s.next();//nos saltamos el ticker de la accion
                s.next();//nos saltamos la columna PER
                String fecha=s.next();
                s.next(); //nos saltamos la hora
                double open=Double.parseDouble(s.next());
                double high=Double.parseDouble(s.next());
                double low=Double.parseDouble(s.next());
                double close=Double.parseDouble(s.next()); //En visual chart este cierre en realidad es el cierre ajustado.
                int volumen=s.nextInt();
                double closeAdj=close; //en el caso de visual chart el Cierre y el Cierre ajustado son lo mismo (son el Cierer ajustado)
                Cotizacion c = new Cotizacion(fecha, open, high, low, close, volumen, closeAdj);
                res.add(c);
                s.nextLine();
            }
            
            //Como en los archivos historicos de visual chart ordenan de mas antiguo a mas nuevo (al reves que los de yahoo)
            //tenemos que re-ordenar los datos para que tambien este ordenados de mas nuevos a mas antiguos.
            Collections.sort(res);
            
            return res;
        }catch(Exception e){System.err.println("PROBLEMA - No se ha encontrado el archivo .csv de la accion: "+ticker);}

        return null;
    }

    /**
     * Busca los dias/Cotizacion que se parecen a la Cotizacion que se corresponde con la posicion del "array" que le pasamos como
     * parametro @indiceActual, es decir, que tienen las 3 variables: ["rango", "colAlta", "colBaja"] dentro del errorTolerado 
     * que le pasamos tambien como parametro @error.
     * Busca en las ultimas "numeroDeDiasParaBuscar" barras, es decir, desde la barra de ayer hasta "numeroDeDiasParaBuscar" atras.
     * 
     * Al metodo usa la variable global "numeroDeDiasParaBuscar" que se corresponde con la primera Cotizacion del "array" a partir de la cual vamos 
     * a buscar parecidos con el dia actual, pero al ser global no hace falta pasarla como paramtero. Pasa lo mismo con el "array" de Cotizaciones.
     * 
     * Y devuelve un array de 3 posiciones, en el que:
     *      La posición 0->Numero de dias que ha habido antes parecidos al que pasamos como parametro.
     *      La posición 1->Porcentaje de dias alcistas (esta sobre 1). El porcentaje de dias bajistas seria (1-"este dato").
     *      La posición 2->Ganancia media del dia siguiente a dichos dias parecidos. 
     * 
     * Con esto intentamos predecir cuanto ganaremos de media comprando o vendiendo mañana a la apertura y cerrando la posicion al cierre,
     * y ver si nos merece la pena realizar la operación o no.
     */   
    public  double[] buscarCotizacionesParecidas(int indiceActual, double error){
        //creamos una cotizacion como la actual para tenerla mas a mano y no hacer todo el rato la llamada al: array.get(indiceActual)
        Cotizacion c = array.get(indiceActual); 
        
        //limites entre los que tiene que estar el Rango.
        double limiteSupR = c.rango*(1+error);
        double limiteInfR = c.rango*(1-error);
        
        //limites entre los que tiene que estar la Cola Alta.
        double limiteSupCA = c.colAlta*(1-error); //en estos limites de la cola alta se cambian los signos porque siempre son negativos los numeros.
        double limiteInfCA = c.colAlta*(1+error); //en estos limites de la cola alta se cambian los signos porque siempre son negativos los numeros.
        
        //limites entre los que tiene que estar la Cola Baja.
        double limiteSupCB = c.colBaja*(1+error);
        double limiteInfCB = c.colBaja*(1-error);
        
        int diasParecidos=0;
        int diasAlcistas=0;        
        double ganAcumulada=0;
        
        double[] res = new double[3]; //este es al array que devolveremos que tendra este formato: res = {diasParecidos,diasAlcistas,ganAcumulada}
        //Ponemos este try para que si hay una accion sin historico suficiente para los calculos el programa no pete con un indexOut sino que siga.
        //try{
            //Recorremos todas las cotizaciones desde la primera que simulamos (indiceIni) hasta la anterior a la actual que estamos analizando.
            for(int i=indiceActual+numeroDeDiasParaBuscar; i>indiceActual; i--){
                if(c.rango>0){
                    if(array.get(i).rango>limiteInfR && array.get(i).rango<limiteSupR &&
                    array.get(i).colAlta>limiteInfCA && array.get(i).colAlta<limiteSupCA &&
                    array.get(i).colBaja>limiteInfCB && array.get(i).colBaja<limiteSupCB){       
                        diasParecidos+=1;
                        if(array.get(i-1).rango>0) diasAlcistas++; //array.get(i+1).rango == a variacionDelDiaSiguiente
                        ganAcumulada+=array.get(i-1).rango;
                        //System.out.println("CoincidenciaPara;"+c.fecha+";elDia;"+array.get(i).fecha);
                    }
                }
                //lo unico que cambian son los signos > y < que los pongo al reves ya que el rango es negativo.
                else{
                    if(array.get(i).rango<limiteInfR && array.get(i).rango>limiteSupR &&  
                    array.get(i).colAlta>limiteInfCA && array.get(i).colAlta<limiteSupCA &&
                    array.get(i).colBaja>limiteInfCB && array.get(i).colBaja<limiteSupCB){
                        diasParecidos+=1;
                        if(array.get(i-1).rango>0) diasAlcistas++; //array.get(i+1).rango == a variacionDelDiaSiguiente
                        ganAcumulada+=array.get(i-1).rango;
                        //System.out.println("CoincidenciaPara;"+c.fecha+";elDia;"+array.get(i).fecha);
                    }
                }
            }
            
            res[0]=diasParecidos; res[1]=diasAlcistas; res[2]=ganAcumulada;
        /*}catch(Exception e){
            //En caso de que no haya dias suficientes en el historico para hacer los calculos devolvemos el array "res" con todo = a 0.
            //Para que asi ese dia no se opere, pero si que pase al dia siguiente y se pueda seguir comprobando si para dias posteriores
            //si que hay historico suficiente para a simulacion.
            res[0]=0; res[1]=0; res[2]=0; 
        }*/
        
        return res;
    }
}
