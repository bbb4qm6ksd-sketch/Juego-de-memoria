
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Confeti {

    // Colores que se asignan aleatoriamente a las piezas de confeti.
    private static final Color[] COLORES = {
            new Color(255, 87, 87),
            new Color(255, 195, 0),
            new Color(72, 201, 176),
            new Color(93, 173, 226),
            new Color(187, 143, 206),
            Color.WHITE
    };

    // Representa una pieza individual con posición, velocidad, tamaño y color.
    private static class Pieza {
        double x, y, velX, velY;
        int tam;
        Color color;
    }

    // Dimensiones del área donde se mostrará la animación.
    private final int ancho;
    private final int alto;
    // Número de piezas y duración total de la animación.
    private final int cantidad;
    private final double duracion;   // en segundos
    // Generador para variar la posición, velocidad, tamaño y color de cada pieza.
    private final Random random = new Random();
    // Lista de piezas que actualmente forman parte de la animación.
    private final List<Pieza> piezas = new ArrayList<>();

    // Tiempo transcurrido y estado de ejecución de la animación.
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

    // Reinicia el tiempo y crea todas las piezas para comenzar la animación.
    public void iniciar() {
        tiempo = 0;
        activo = true;
        piezas.clear();
        for (int i = 0; i < cantidad; i++) {
            piezas.add(nuevaPieza());
        }
    }

    // Crea una pieza con características aleatorias y la devuelve.
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


    // Avanza la posición de las piezas según el tiempo transcurrido (dt).
    public void actualizar(double dt) {
        // Si la animación no está en marcha, no hay nada que actualizar.
        if (!activo) return;

        tiempo += dt;
        for (Pieza p : piezas) {
            p.x += p.velX * dt;
            p.y += p.velY * dt;
        }

        // Al cumplirse la duración, se detiene la animación y se eliminan las piezas.
        if (tiempo >= duracion) {
            activo = false;
            piezas.clear();
        }
    }

    // Dibuja cada pieza como un pequeño rectángulo usando su color actual.
    public void dibujar(Graphics2D g) {
        if (!activo) return;

        for (Pieza p : piezas) {
            g.setColor(p.color);
            g.fillRect((int) p.x, (int) p.y, p.tam, p.tam);
        }
    }

    // Indica si la animación sigue activa.
    public boolean estaActivo() {
        return activo;
    }
}