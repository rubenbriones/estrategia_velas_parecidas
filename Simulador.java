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
public class Simulador{
    //Este ArrayList contendra todas las "Cotizacion" de la accion que estemos analizando.
    public static ArrayList<Cotizacion> array;
    
    public static int indiceIni=0; //es el indice/posicion del ArrayList de las cotizaciones que corresponde con el primer dia que vamos a simular.
    public static int indiceFin=0; //es el indice/posicion del ArrayList de las cotizaciones que corresponde con el ultimo dia que vamos a simular.
    
    //VARIABLES A MODIFICAR EN CADA SIMULACION    
    public static final GregorianCalendar fechaIni = new GregorianCalendar(2007,Calendar.JANUARY,1); //1 de enero del 2007
    public static final GregorianCalendar fechaFin = new GregorianCalendar(2011,Calendar.DECEMBER,31); //31 de diciembre del 2011
    
    public static int numeroDeDiasParaBuscar=1040; //este es el numero de dias en los que vamos a buscar hacia atras coincidencias con el dia actual. 260==1 anyo
    public static double errorTolerado=0.07;   //es el error que toleramos ara decir que un dia es estadisticamente igual a otro.
    public static double limiteMinGanancias=0;   //es el limite minimoq ue tendriamos que tener previsto ganar para el dia siguiente operar.Ponerlo siempre en positivo, que yo luego ya lo cambio para operar el lado corto.
    
    //public static String carpetaMercado = "IBEX35";
    //public static String[] tickersAsimular = {"SAN.MC","BBVA.MC","ITX.MC","TEF.MC","IBE.MC","REP.MC"}; //Se puede meter 100.000€ en la apertura.
    //public static String[] tickersAsimular = {"SAN.MC","BBVA.MC","ITX.MC","TEF.MC","IBE.MC","REP.MC","GAS.MC","ABE.MC","POP.MC","FER.MC"}; //Se puede meter 50.000€ en la apertura.
    //public static String[] tickersAsimular = {"REP.MC"}; //Se puede meter 50.000€ en la apertura.
    //** Variables si uso los datos historicos de VisualChart **//
    public static String carpetaMercado = "IbexVisualChart";
    public static String[] tickersAsimular = {"060 SAN.MC","060 BBVA.MC","060 ITX.MC","060 TEF.MC","060 IBE.MC","060 REP.MC"}; //Se puede meter 100.000€ en la apertura.
    
    //FIN - VARIABLES A MODIFICAR EN CADA SIMULACION
    
    public static void main(String[] args)throws Exception{     
        /*for(numeroDeDiasParaBuscar=260; numeroDeDiasParaBuscar<=1300; numeroDeDiasParaBuscar+=130){ //130==1semestre. 8iteraciones
            System.out.println("===============================");
            System.out.println("NUMERO DE DIAS A BUSCAR: "+numeroDeDiasParaBuscar);
            System.out.println("===============================");
            algoritmo(true, 0.04);
        }*/
        
        /*for(errorTolerado=0.05; errorTolerado<=1; errorTolerado+=0.05){
            System.out.println("===============================");
            System.out.println("ERROR TOLERADO: "+errorTolerado);
            System.out.println("===============================");
            algoritmo(true,0.04);
        }*/
        
        /*for(limiteMinGanancias=0; limiteMinGanancias<=8; limiteMinGanancias+=1){
            System.out.println("===============================");
            System.out.println("LIMITE MINIMO DE GANANCIAS: "+limiteMinGanancias);
            System.out.println("===============================");
            algoritmo(true,0.04);
        }*/
        
        /*for(errorTolerado=0.04; errorTolerado<=0.1; errorTolerado+=0.01){
            for(limiteMinGanancias=0; limiteMinGanancias<=2.5; limiteMinGanancias+=0.5){
                System.out.println("===============================");
                System.out.println("ERROR TOLERADO: "+errorTolerado);
                System.out.println("LIMITE MINIMO DE GANANCIAS: "+limiteMinGanancias);
                System.out.println("===============================");
                algoritmo(true, 0.04);
            }
        }*/
        
        algoritmo(false, 0.07);
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
    public static void algoritmo(boolean soloGlobales, double comisiones){
        //Estadisticas GLOBALES: diasEnElMercado, aciertos, ganancias.
        int diasLargosT=0, diasCortosT=0;
        int acLargosT=0, acCortosT=0;
        double ganLargosT=0, ganCortosT=0;
            
        //Recorremos todas las acciones a simular.
        for(int j=0; j<tickersAsimular.length; j++){
            //este ArrayList contendra todas las "Cotizacion" de la accion que estemos analizando.
            array = leerCSVvisualchart(tickersAsimular[j], carpetaMercado);
            
            //Estadisticas de cada Accion: diasEnElMercado, aciertos, ganancias.
            int diasLargos=0, diasCortos=0;
            int acLargos=0, acCortos=0;
            double ganLargos=0, ganCortos=0;
            
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
                if(estadisticas[2] > limiteMinGanancias){
                    diasLargos++;
                    if(array.get(i-1).rango>0) acLargos++; //comprobamos si hubieramos acertado
                    ganLargos+= array.get(i-1).rango - comisiones; //sumamos la ganancia/perdida que hubieramos tenido.
                }
                //comprobamos si la gananciaMedia del dia siguiente es superior (mas negativa) a nuestro limite en negativo, para asi entrar CORTOS.
                else{
                    if(estadisticas[2] < -1*limiteMinGanancias){
                        diasCortos++;
                        if(array.get(i-1).rango<0) acCortos++; //comprobamos si hubieramos acertado, es decir, si al dia siguiente la accion bajo (rango<0).
                        ganCortos+= -1*array.get(i-1).rango - comisiones; //sumamos la ganancia/perdida que hubieramos tenido (que es el rango del dia siguiente *-1 ya que entramos cortos).
                    }
                }
            }
            
            if(!soloGlobales){
                //Imprimimos por pantalla las estadisticas rsultantes de la simulacion para la accion simulada.
                System.out.println("Accion: "+tickersAsimular[j]);
                System.out.println("Operaciones LARGOS: "+diasLargos);
                System.out.println("% Acierto LARGOS: "+(acLargos*100.0/diasLargos));
                System.out.println("Ganancias en % LARGOS: "+ganLargos);
                System.out.println("Ganancias Media en % por Op. LARGOS: "+(ganLargos/diasLargos));
                System.out.println("Operaciones CORTOS: "+diasCortos);
                System.out.println("% Acierto CORTOS: "+(acCortos*100.0/diasCortos));
                System.out.println("Ganancias en % CORTOS: "+ganCortos);
                System.out.println("Ganancias Media en % por Op. CORTOS: "+(ganCortos/diasCortos));
                System.out.println("Operaciones: "+(diasLargos+diasCortos));
                System.out.println("% Acierto: "+((acLargos+acCortos)*100.0/(diasLargos+diasCortos)));
                System.out.println("Ganancias en %: "+(ganLargos+ganCortos));
                System.out.println("Ganancias Media en % por Op.: "+((ganLargos+ganCortos)/(diasLargos+diasCortos)));
                System.out.println("----------------------------------------------");
            }
            
            //Actualizamos las estadisticas Globales.
            diasLargosT+=diasLargos;
            diasCortosT+=diasCortos;
            acLargosT+=acLargos;
            acCortosT+=acCortos;
            ganLargosT+=ganLargos;
            ganCortosT+=ganCortos;
        }
        
        //Imprimimos por pantalla las estadisticas rsultantes de la simulacion GLOBAL.
        System.out.println("ESTADISTICAS GLOBALES");
        System.out.println("Acciones: "); for(int i=0; i<tickersAsimular.length; i++) System.out.print(tickersAsimular[i]+", ");
        System.out.println();
        System.out.println("Operaciones LARGOS: "+diasLargosT);
        System.out.println("% Acierto LARGOS: "+(acLargosT*100.0/diasLargosT));
        System.out.println("Ganancias en % LARGOS: "+ganLargosT);
        System.out.println("Ganancias Media en % por Op. LARGOS: "+(ganLargosT/diasLargosT));
        System.out.println("Operaciones CORTOS: "+diasCortosT);
        System.out.println("% Acierto CORTOS: "+(acCortosT*100.0/diasCortosT));
        System.out.println("Ganancias en % CORTOS: "+ganCortosT);
        System.out.println("Ganancias Media en % por Op. CORTOS: "+(ganCortosT/diasCortosT));
        System.out.println("Operaciones: "+(diasLargosT+diasCortosT));
        System.out.println("% Acierto: "+((acLargosT+acCortosT)*100.0/(diasLargosT+diasCortosT)));
        System.out.println("Ganancias en %: "+(ganLargosT+ganCortosT));
        System.out.println("Ganancias Media en % por Op.: "+((ganLargosT+ganCortosT)/(diasLargosT+diasCortosT)));
    }
    
    public static double redondear( double numero, int decimales ) {
        return Math.round(numero*Math.pow(10,decimales))/Math.pow(10,decimales);
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
    public static double[] buscarCotizacionesParecidas(int indiceActual, double error){
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
        
        //Recorremos todas las cotizaciones desde la primera que simulamos (indiceIni) hasta la anterior a la actual que estamos analizando.
        for(int i=indiceActual+numeroDeDiasParaBuscar; i>indiceActual; i--){
            if(c.rango>0){
                if(array.get(i).rango>limiteInfR && array.get(i).rango<limiteSupR &&
                array.get(i).colAlta>limiteInfCA && array.get(i).colAlta<limiteSupCA &&
                array.get(i).colBaja>limiteInfCB && array.get(i).colBaja<limiteSupCB){       
                    diasParecidos++;
                    if(array.get(i-1).rango>0) diasAlcistas++; //array.get(i+1).rango == a variacionDelDiaSiguiente
                    ganAcumulada+=array.get(i-1).rango;             
                }
            }
            //lo unico que cambian son los signos > y < que los pongo al reves ya que el rango es negativo.
            else{
                if(array.get(i).rango<limiteInfR && array.get(i).rango>limiteSupR &&  
                array.get(i).colAlta>limiteInfCA && array.get(i).colAlta<limiteSupCA &&
                array.get(i).colBaja>limiteInfCB && array.get(i).colBaja<limiteSupCB){
                    diasParecidos++;
                    if(array.get(i-1).rango>0) diasAlcistas++; //array.get(i+1).rango == a variacionDelDiaSiguiente
                    ganAcumulada+=array.get(i-1).rango;
                }
            }
        }
        
        double[] res = {diasParecidos,diasAlcistas,ganAcumulada};
        
        return res;
    }
}