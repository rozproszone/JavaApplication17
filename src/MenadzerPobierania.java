
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;

/***
* klasa główna programu będąca GUI
* 
*/
public class MenadzerPobierania extends JFrame {

    private final JTextField nazwaPlikuT;
    private final JButton pauzaB;
    private final JButton wznowB;
    private final JButton anulujB;
    private final JButton pobierzB;
    
    private final JPanel gornyPanel;
    private final JPanel dolnyPanel;
    
    
    private Pobieranie aktywnePobieranie;
    private final JProgressBar pasekPostepu;
    
    private final JTextField katalogT;

    /***
     * Konstruktor
     * dodaje elementy do okna 
     */
    public MenadzerPobierania() {

        setTitle("Menadżer pobierania plików");
        setSize(700, 140);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        // dodanie paska postepu
        pasekPostepu = new JProgressBar(0, 100);
        pasekPostepu.setValue(0);
        pasekPostepu.setStringPainted(true);

        
        gornyPanel = new JPanel();
        // pole do wpisywania url pliku
        nazwaPlikuT = new JTextField(35);
        gornyPanel.add(nazwaPlikuT);
        
        // przycisk pobierania
        pobierzB = new JButton("Pobierz plik");
        pobierzB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pobieraj();
            }
        });
        gornyPanel.add(pobierzB);

        dolnyPanel = new JPanel();
        
        dolnyPanel.add(new JLabel("Katalog:"));
        
        katalogT = new JTextField(30);
        katalogT.setText("C:\\");
        
        dolnyPanel.add(katalogT);
        
        // przycisk pauzy
        pauzaB = new JButton("Pauza");
        pauzaB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                aktywnePobieranie.pauza();
            }
        });
        
        dolnyPanel.add(pauzaB);
        
        
        // przycisk wznawiania
        wznowB = new JButton("Wznów");
        wznowB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                aktywnePobieranie.wznow();
            }
        });
        dolnyPanel.add(wznowB);
        
        // przycisk anulowania
        anulujB = new JButton("Anuluj");
        anulujB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                aktywnePobieranie.anuluj();
            }
        });
        dolnyPanel.add(anulujB);
        
        
        

        // dodanie elementów do okna
        setLayout(new BorderLayout());
        add(gornyPanel, BorderLayout.NORTH);
        add(pasekPostepu, BorderLayout.CENTER);
        add(dolnyPanel, BorderLayout.SOUTH);

        repaint();
        revalidate();
    }

    /***
     * Wywołuje funkcje sprawdzającą poprawność URL i jeśli wszystko jest OK
     * to rozpoczyna pobieranie
     * w przeciwnym razie wyświetla komunikat błędu
     */
    public void pobieraj() {
        
        // sprawdza poprawnośc wpisanego URL
        URL url = sprawdzPoprawnosc(nazwaPlikuT.getText());
        
        String nazwaKatalogu = katalogT.getText();
        
        // jeśli jest poprawny to zaczyna pobieranie
        if (url != null) {
            aktywnePobieranie = new Pobieranie(url, nazwaKatalogu, pasekPostepu);

        // jeśli nie to wyświetla komunikat o błędize    
        } else {
            JOptionPane.showMessageDialog(this, "Niepoprawny url pliku", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    /***
     * Sprawdza poprawność URL, bada czy zaczyna się od prefiksu 'http://' oraz
     * czy da się utworzyć poprawny obiekt URL
     * @param link wejściowy link
     * @return URL albo null w przypadku kiedy URL jest niepoprawny
     */
    public URL sprawdzPoprawnosc(String link) {

        // sprawdza czy link zaczyna się od http://
        if (!link.toLowerCase().startsWith("http://")) {
            return null;
        }

        URL url;
        try {
            url = new URL(link); // sprawdza czy wpisany tekst jest poprawnym adresem URL
        } catch (MalformedURLException e) {
            return null;
        }
        // sprawdza długość pliku
        if (url.getFile().length() < 3) {
            return null;
        }

        return url;
    }

    /***
     * Funkcja main programu
     * @param args
     */
    public static void main(String[] args) {
        new MenadzerPobierania();

    }
}
