import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Confeti {

    private static final Color[] COLORES = {
            new Color(255, 87, 87),
            new Color(255, 195, 0),
            new Color(72, 201, 176),
            new Color(93, 173, 226),
            new Color(187, 143, 206),
            Color.WHITE
    };

    private static class Pieza {
        double x, y, velX, velY;
        int tam;
        Color color;
    }

    private final int ancho;
    private final int alto;
    private final int cantidad;
    private final double duracion;   // en segundos
    private final Random random = new Random();
    private final List<Pieza> piezas = new ArrayList<>();

    private double tiempo;
    private boolean activo;

    public Confeti(int ancho, int alto) {
        this(ancho, alto, 150, 4.0);
    }

    public Confeti(int ancho, int alto, int cantidad, double duracion) {
        this.ancho = ancho;
        this.alto = alto;
        this.cantidad = cantidad;
        this.duracion = duracion;
    }

    public void iniciar() {
        tiempo = 0;
        activo = true;
        piezas.clear();
        for (int i = 0; i < cantidad; i++) {
            piezas.add(nuevaPieza());
        }
    }

    private Pieza nuevaPieza() {
        Pieza p = new Pieza();
        p.x = random.nextDouble() * ancho;
        p.y = -random.nextDouble() * alto;          // empiezan arriba de la pantalla
        p.velY = 120 + random.nextDouble() * 180;
        p.velX = -40 + random.nextDouble() * 80;
        p.tam = 5 + random.nextInt(6);
        p.color = COLORES[random.nextInt(COLORES.length)];
        return p;
    }


    public void actualizar(double dt) {
        if (!activo) return;

        tiempo += dt;
        for (Pieza p : piezas) {
            p.x += p.velX * dt;
            p.y += p.velY * dt;
        }

        if (tiempo >= duracion) {
            activo = false;
            piezas.clear();
        }
    }

    public void dibujar(Graphics2D g) {
        if (!activo) return;

        for (Pieza p : piezas) {
            g.setColor(p.color);
            g.fillRect((int) p.x, (int) p.y, p.tam, p.tam);
        }
    }

    public boolean estaActivo() {
        return activo;
    }
}