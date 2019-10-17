import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * 
 */
public class Saldo<E extends NodoSaldo<E>>{
    public ArrayList<E> array;
    public int num; //es el numero de elementos, viene a ser lo mismo que array.size().
    
    public double[] saldos; //empezamos en 0, y vamos poniendo el saldo/acumulado.
    
    public double anyos; //cuantos anyos hay entre el primer NodoSaldo(apuesta/operacion/etc) y el ultimo.
    public double ganTotal;
    public double drawdown;
        
    public Saldo(ArrayList<E> a){
        array=a;
        Collections.sort(array);
        num=array.size();
        
        saldos = new double[num+1];
        saldos[0]=0; //empezamos el acumulado en 0.
        for(int i=1; i<saldos.length; i++){
            saldos[i]=saldos[i-1]+array.get(i-1).getGan();
        }
        ganTotal=saldos[num];
        
        anyos=getYearsDifference(array.get(0).getFecha(), array.get(num-1).getFecha());
        
        drawdown=calcularDrawdown();
    }
    
    /**
     * Este constuctor es igual al anterior solo que ya le paso yo los anyos simulados, pues el ratio sino va a haber veces que salga muy
     * alto pero porque se hacen pocas operaciones/apuestas y el getYearsDifference de esta clase me va a dar muy bajo.
     */
    public Saldo(ArrayList<E> a, double anyosSimulados){
        array=a;
        Collections.sort(array);
        num=array.size();
        
        saldos = new double[num+1];
        saldos[0]=0; //empezamos el acumulado en 0.
        for(int i=1; i<saldos.length; i++){
            saldos[i]=saldos[i-1]+array.get(i-1).getGan();
        }
        ganTotal=saldos[num];
        
        anyos=anyosSimulados;
        
        drawdown=calcularDrawdown();
    }
    
    public void imprimirTodo(){
        for(int i=0; i<array.size(); i++){
            System.out.println(array.get(i).toString());
        }
    }
    
    public double calcularDrawdown(){        
        double maxRelativo=0, minRelativo=0, down=0;
        for(int j=0; j<=num; j++){
            if(saldos[j]>maxRelativo) {
                maxRelativo = saldos[j]; 
                minRelativo=maxRelativo;
            }
            else if(saldos[j]<minRelativo) minRelativo = saldos[j];

            if(minRelativo-maxRelativo < down) down=minRelativo-maxRelativo;
        }
        return down;
    }
    
    public double calcularRatio(){
        return (ganTotal/anyos)/(-drawdown);
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
