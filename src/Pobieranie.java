
import java.io.*;
import java.net.*;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

/**
 * Klasa służąca do pobierania pliku
 * Implementuje interfejs Runnable
 */
public class Pobieranie implements Runnable {

    // rozmiar bufora danych
    private static final int ROZMIAR_BUFORA = 1024;

    // statusy jakie może mieć pobieranie
    public static final int POBIERANIE = 0;
    public static final int PAUZA = 1;
    public static final int ZAKONCZONE = 2;
    public static final int ANULOWANE = 3;
    public static final int BLAD = 4;

    private final URL url;
    private int rozmiarPliku; 
    private int ileSciagnieto;
    private int status;
    
    JProgressBar pasekPostepu;
    Thread t; // wątek odpowiadający na ściąganie
    
    String nazwaKatalogu;

    /**
    * Konstruktor
     * @param url wejściowy adres url
     * @param nazwaKatalogu nazwa katalogu docelowego
     * @param pasekPostepu pasek postępu programu
    */
    public Pobieranie(URL url, String nazwaKatalogu, JProgressBar pasekPostepu) {
        
        this.pasekPostepu = pasekPostepu;
        this.url = url;
        this.nazwaKatalogu = nazwaKatalogu;
        
        this.rozmiarPliku = -1;
        this.ileSciagnieto = 0;
        this.status = POBIERANIE;
        
        
        pobieraj();
    }

    /**
    * Rozpoczyna wątek pobierający dane
    */
    public void pobieraj() {
        
        // tworzy i startuje wątek
        t = new Thread(this);
        t.start();
    }
    /**
    * Pauzuje pobieranie
    */
    public void pauza() {
        
        if (status == POBIERANIE) {
            
            status = PAUZA;
        }
        
    }

    /***
     * Wznawia pobieranie
     */
    public void wznow() {
        if (status == PAUZA) {
            
            status = POBIERANIE;
            pobieraj();
        }
        
    }
    
    /***
     * Anuluje pobieranie
     */
    public void anuluj() {
        
        if (status == POBIERANIE) {
            
            status = ANULOWANE;
        }
        
        ileSciagnieto = 0;
        aktualizujPasekPostepu();
        
    }
    
    /***
     * Ustawia wartość paska postępu na podstawie tego ile danych
     * zostało ściągniętych
     */
    private void aktualizujPasekPostepu() {
 
        pasekPostepu.setValue((int)(100.0 * (double) ileSciagnieto/(double)rozmiarPliku));
    }

    /**
     * Wydziela nazwę pliku z adresu url
     * 
     * @param url wejściowy adres url
     * @return nazwa pliku
     */
    public String pobierzNazwePliku(URL url) {
        String nazwaPliku = url.getFile();
        return nazwaPliku.substring(nazwaPliku.lastIndexOf('/') + 1);
    }

    /***
     * Funkcja robocza interfejsu Runnable
     */
    @Override
    public void run() {
        RandomAccessFile plik = null;
        InputStream strumien = null;

        try {
            
            // otwiera połączenie
            HttpURLConnection conn
                    = (HttpURLConnection) url.openConnection();

            // ustawia żądanie na odpowiedni zakres bajtów
            conn.setRequestProperty("Range",
                    "bytes=" + ileSciagnieto + "-");

            // połączenie
            conn.connect();

            // pobiera kod odpowiedzi (musi być 2xx)
            int kod = conn.getResponseCode();
            
            

            if (kod < 200 && kod > 300) {
                status = BLAD;
            }

            // rozmiar pobieranych danych
            int rozmiar = conn.getContentLength();
           

            if (rozmiar < 1) {
                status = BLAD;
            }
            
            

            if (rozmiarPliku == -1) {
                rozmiarPliku = rozmiar;
            }

            // ustawia zapisywanie w odpowiednim miejscu pliku
            try {
            plik = new RandomAccessFile(nazwaKatalogu + "\\" + pobierzNazwePliku(url), "rw");
            } catch(FileNotFoundException ex) {
               
                JOptionPane.showMessageDialog(null, "Błąd z zapisem pliku do katalogu", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
                    
            }
            plik.seek(ileSciagnieto);

            strumien = conn.getInputStream();

            // pobieranie danych
            while (status == POBIERANIE) {
                
                byte dane[];
                
                if (rozmiarPliku - ileSciagnieto > ROZMIAR_BUFORA) {
                    dane = new byte[ROZMIAR_BUFORA];
                } else {
                    dane = new byte[rozmiarPliku - ileSciagnieto];
                }

                int ilePrzeczytanych = strumien.read(dane);
                if (ilePrzeczytanych == -1) {
                    break;
                }

                plik.write(dane, 0, ilePrzeczytanych);
                ileSciagnieto += ilePrzeczytanych;
                
                aktualizujPasekPostepu();
            }

            if (status == POBIERANIE) {
                status = ZAKONCZONE;
            }
        
            // zamknięcie połączeń i pliku
            
        } catch (IOException e) {
            status = BLAD;
        } finally {
            if (plik != null) {
                try {
                    plik.close();
                } catch (IOException e) {
                }
            }

            if (strumien != null) {
                try {
                    strumien.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
