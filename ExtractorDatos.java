import java.io.*;
import java.net.*;
import java.util.regex.*;

/**
 * 
 */
public class ExtractorDatos{
    /**
     * Le paso como parametro la URL de yahoo finance de un mercado y me devuelve un array con todos los tickers de las acciones de ese mercado.
     * Ibex35: http://es.finance.yahoo.com/q/cp?s=%5EIBEX
     * Mercado Continuo: http://es.finance.yahoo.com/q/cp?s=IGBM.MA
     */
    public static String[] extraerTickers(String url)throws Exception{
        String[] tickers = null; //inicializamos la variable a devolver a null pues de momento no sabemos cuantos tickers hay en este mercado.
        
        String web = getWeb(url,0);
        
        Pattern numAcciones = Pattern.compile("(\\d*) - (\\d*) de (\\d*)");
        Matcher m = numAcciones.matcher(web);
        
        if(m.find()){
            int accionesTotales = Integer.parseInt(m.group(3));
            tickers = new String[accionesTotales]; //creamos el array a devolver.
            int cont=0; //creamos un contador en el que iremos contando los tickers introducidos en el array anterior.
            
            Pattern ticker = Pattern.compile(">(.{1,10}?)</a></b>"); //(.{1,10}?) --> esto significa que el ticker puede ser como maximo de 10 caracteres.
                       
            //Como solo se muestran 50 acciones por pagina, ponemos un bucle por si hay mas de 50 acciones en ese mercado.
            int pag=0; //guardo una variable para saber que pagina toca por comodidad y no hacer calculos de cocientes y modulos.
            for(int i=0; i<accionesTotales; i+=50, pag++){
                if(i!=0){ //En caso de que haya mas de 50 acciones, hay que acceder al nuevo link donde estan las acciones restantes.
                    url+="&c="+pag;
                    web=getWeb(url,0);                    
                }
                
                //Ahora buscamos todos loc tickers de las acciones mostradas en la pagina/web actual.
                Matcher m2 = ticker.matcher(web);
                while(m2.find()){
                    tickers[cont]=m2.group(1);
                    cont++;
                }
            }
        }
        
        return tickers;
    }
    
    /**
     * Le paso como parametro el ticker de la accion y el anyo a partir del cual quiero descragar sus cotizaciones historicas.
     * Y me descarga un .csv desde el 1 de enero de ese anyo hasta la actualidad en la carpeta que le digamos como parametro,
     * que generalmente sera el nombre del mercado al que pertenece y que estara a su vez dentro de "Cotizaciones Historicas".
     * SI COMO ANYO LE PASAMOS UN 0 DESCARGARA TODOS LOS DATOS QUE TENGA SOBRE ESA ACCION.
     * Los .csv los descarga de yahoo finance, utilizando su API: http://ichart.yahoo.com/table.csv?s=TICKER
     */
    public static void descargarCSV(String ticker, int anyo, String carpeta)throws Exception{
        File dir = new File("Cotizaciones Historicas\\"+carpeta);
        dir.mkdirs();
        
        File file = new File("Cotizaciones Historicas\\"+carpeta+"\\"+ticker+".csv");
        
        String url = "http://ichart.yahoo.com/table.csv?s="+ticker; //dirección url del recurso a descargar
        if(anyo!=0) url+= "&a=0&b=1&c="+anyo; //si el parametro anyo<>0 solo descargamos el historico a partir de ese anyo.
        
        URLConnection conn = new URL(url).openConnection();
        conn.connect();
        
        InputStream in = conn.getInputStream();
        OutputStream out = new FileOutputStream(file);
        
        //Vamos leyendo del InputStream y vamos escribiendo en el OutputStream. Vamos leyendo de a un byte por vez y los escribe en un archivo. El -1 significa que se llego al final.
        int b = 0;
        while(b != -1){
            b = in.read();
            if (b != -1) out.write(b);
        }
        
        //Cerramos los streams:
        out.close();
        in.close();
    }
    
    /**
     * Le pasamos como parametro la direccion de la web de la que queremos sacar su codigo fuente, 
     * y el numero de lineas que queremos ignorar de dicho codigo fuente.
     */
    public static String getWeb(String direccion, int x) throws Exception {
        URL pagina = new URL(direccion);
        BufferedReader in = new BufferedReader(new InputStreamReader(pagina.openStream()));
        
        //Pasamos las X primera lineas, ya que no contienen nada de información
        for(int i = 0; i < x; i++) in.readLine(); 
        
        String entrada;
        String resultado="";
        while ((entrada = in.readLine()) != null)
            resultado+=entrada+"\n";
            
        in.close();
        return resultado;
    }
}
