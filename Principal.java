import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;

/**
 * 
 */
public class Principal{
    public static void main(String[] args)throws Exception{
        //Ibex35: https://es.finance.yahoo.com/q/cp?s=%5EIBEX
        //Mercado Continuo: https://es.finance.yahoo.com/q/cp?s=IGBM.MA
        //Dax30: https://es.finance.yahoo.com/q/cp?s=%5EGDAXI
        //Cac40: https://es.finance.yahoo.com/q/cp?s=%5EFCHI
        //DowJones30: https://es.finance.yahoo.com/q/cp?s=%5EDJI
        
        String carpetaMercado = "DAX30v2";
        String[] tickers = ExtractorDatos.extraerTickers("https://es.finance.yahoo.com/q/cp?s=%5EGDAXI");
        for(int i=0; i<tickers.length; i++){
            //System.out.println(tickers[i]);
            ExtractorDatos.descargarCSV(tickers[i], 0, carpetaMercado);
        }
        
    }
}
