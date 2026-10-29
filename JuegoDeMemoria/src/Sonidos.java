import javax.sound.sampled.*;
import javax.swing.AbstractButton;
import javax.swing.SwingUtilities;
import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public final class Sonidos {

    public enum Efecto { CLICK, HOVER, VOLTEAR, ACIERTO, FALLO, VICTORIA, ERROR, TICK, ABRIR }

    private static final float FS = 44100f;
    private static final AudioFormat FORMATO = new AudioFormat(FS, 16, 1, true, false);
    private static final double DURACION_BUCLE = 16.0;   // segundos de la musica antes de repetirse

    private static final Map<Efecto, byte[]> EFECTOS = new EnumMap<>(Efecto.class);
    private static final ExecutorService HILO = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "AstroMatch-Sonidos");
        t.setDaemon(true);
        return t;
    });
    private static final AtomicInteger ACTIVOS = new AtomicInteger();

    private static byte[] datosMusica;
    private static Clip musica;
    private static boolean iniciado = false;
    private static volatile boolean silenciado = false;
    private static volatile boolean musicaActiva = true;
    private static float volumenMusicaDb = -9f;     // la musica suave, por debajo de los efectos
    private static float volumenEfectosDb = -3f;
    private static long ultimoHover = 0;

    private Sonidos() { }
    public static void iniciar() {
        if (iniciado) return;
        iniciado = true;
        HILO.execute(() -> {
            try {
                generarEfectos();
                datosMusica = generarMusica();
                if (!silenciado && musicaActiva) arrancarMusica();
            } catch (Throwable t) {
            }
        });
        instalarSonidoEnBotones();
    }

    public static void reproducir(Efecto efecto) {
        if (silenciado || !iniciado) return;
        HILO.execute(() -> sonar(efecto));
    }

    public static boolean isSilenciado() { return silenciado; }


    public static void setSilenciado(boolean valor) {
        silenciado = valor;
        HILO.execute(() -> {
            if (valor) detenerMusica();
            else if (musicaActiva) arrancarMusica();
        });
    }

    public static void setMusicaActiva(boolean valor) {
        musicaActiva = valor;
        HILO.execute(() -> {
            if (valor && !silenciado) arrancarMusica(); else detenerMusica();
        });
    }


    public static void setVolumenMusica(float db) {
        volumenMusicaDb = db;
        HILO.execute(() -> aplicarGanancia(musica, volumenMusicaDb));
    }

    private static void instalarSonidoEnBotones() {
        Toolkit.getDefaultToolkit().addAWTEventListener(ev -> {
            if (!(ev instanceof MouseEvent)) return;
            MouseEvent me = (MouseEvent) ev;
            if (!(me.getSource() instanceof AbstractButton)) return;
            AbstractButton b = (AbstractButton) me.getSource();
            if (!b.isEnabled()) return;
            boolean esCarta = b instanceof Carta;

            if (me.getID() == MouseEvent.MOUSE_PRESSED && SwingUtilities.isLeftMouseButton(me)) {
                reproducir(esCarta ? Efecto.VOLTEAR : Efecto.CLICK);
            } else if (me.getID() == MouseEvent.MOUSE_ENTERED && !esCarta) {
                long ahora = System.currentTimeMillis();
                if (ahora - ultimoHover > 80) {
                    ultimoHover = ahora;
                    reproducir(Efecto.HOVER);
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);
    }

    private static void sonar(Efecto efecto) {
        byte[] datos = EFECTOS.get(efecto);
        if (datos == null || ACTIVOS.get() > 14) return;
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(FORMATO, datos, 0, datos.length);
            aplicarGanancia(clip, volumenEfectosDb);
            ACTIVOS.incrementAndGet();
            clip.addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP) {
                    clip.close();
                    ACTIVOS.decrementAndGet();
                }
            });
            clip.start();
        } catch (Exception e) {
            // sin audio: ignorar
        }
    }

    private static void arrancarMusica() {
        if (datosMusica == null) return;
        try {
            if (musica != null && musica.isRunning()) return;
            detenerMusica();
            musica = AudioSystem.getClip();
            musica.open(FORMATO, datosMusica, 0, datosMusica.length);
            aplicarGanancia(musica, volumenMusicaDb);
            musica.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            musica = null;
        }
    }

    private static void detenerMusica() {
        if (musica != null) {
            musica.stop();
            musica.close();
            musica = null;
        }
    }

    private static void aplicarGanancia(Clip clip, float db) {
        if (clip == null) return;
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl c = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                c.setValue(Math.max(c.getMinimum(), Math.min(c.getMaximum(), db)));
            }
        } catch (Exception e) {
        }
    }


    private static void generarEfectos() {
        // Clic de boton: campanita corta y limpia
        double[] b = vacio(0.30);
        campana(b, 0, 1318.5, 0.30, 1.0, 0.06, false);
        campana(b, 0, 1976, 0.30, 0.35, 0.05, false);
        EFECTOS.put(Efecto.CLICK, aBytes(b, 0.40, true));

        // Hover: un destello casi imperceptible
        b = vacio(0.12);
        campana(b, 0, 2637, 0.12, 1.0, 0.025, false);
        EFECTOS.put(Efecto.HOVER, aBytes(b, 0.10, true));

        // Voltear carta: "ting" un poco mas grave con un brillo agudo
        b = vacio(0.55);
        campana(b, 0, 987.8, 0.55, 1.0, 0.14, false);
        campana(b, 0.02, 1975.5, 0.50, 0.30, 0.10, false);
        EFECTOS.put(Efecto.VOLTEAR, aBytes(b, 0.45, true));

        // Acierto: arpegio ascendente de campanitas (Mi mayor)
        b = vacio(1.6);
        double[] notas = {659.3, 830.6, 987.8, 1318.5};
        for (int i = 0; i < notas.length; i++) {
            campana(b, i * 0.09, notas[i], 1.2, 0.8, 0.35, false);
        }
        EFECTOS.put(Efecto.ACIERTO, aBytes(b, 0.55, true));

        // Fallo: tono grave suave que baja
        b = vacio(0.55);
        double fase = 0;
        for (int i = 0; i < b.length; i++) {
            double t = i / FS;
            double f = 300 - 110 * Math.min(1, t / 0.45);
            fase += 2 * Math.PI * f / FS;
            double env = Math.min(1, t / 0.01) * Math.exp(-t / 0.18);
            b[i] = env * (Math.sin(fase) + 0.25 * Math.sin(2 * fase));
        }
        EFECTOS.put(Efecto.FALLO, aBytes(b, 0.40, true));

        // Error (login / formulario)
        b = vacio(0.9);
        campana(b, 0, 392, 0.8, 1.0, 0.18, false);
        campana(b, 0.13, 293.7, 0.8, 1.0, 0.22, false);
        EFECTOS.put(Efecto.ERROR, aBytes(b, 0.40, true));

        // Tick del reloj de turno (ultimos segundos)
        b = vacio(0.10);
        campana(b, 0, 1760, 0.10, 1.0, 0.02, false);
        EFECTOS.put(Efecto.TICK, aBytes(b, 0.30, true));

        // Abrir ventana / cambiar de pantalla
        b = vacio(0.9);
        campana(b, 0, 880, 0.8, 0.8, 0.22, false);
        campana(b, 0.10, 1318.5, 0.8, 0.8, 0.26, false);
        EFECTOS.put(Efecto.ABRIR, aBytes(b, 0.40, true));

        // Victoria: arpegio largo de La menor / Do mayor con brillo final
        b = vacio(3.4);
        double[] gan = {440, 523.3, 659.3, 880, 1046.5, 1318.5, 1760};
        for (int i = 0; i < gan.length; i++) {
            campana(b, i * 0.13, gan[i], 2.0, 0.7, 0.6, false);
        }
        campana(b, 0.95, 1318.5, 2.2, 0.8, 0.9, false);
        campana(b, 0.95, 1046.5, 2.2, 0.6, 0.9, false);
        campana(b, 0.95, 659.3, 2.2, 0.6, 0.9, false);
        EFECTOS.put(Efecto.VICTORIA, aBytes(b, 0.60, true));
    }


    private static byte[] generarMusica() {
        double L = DURACION_BUCLE;
        int N = (int) (L * FS);
        double[] pad = new double[N];


        double[] acordeA = {110.0, 164.8, 220.0, 261.6, 329.6, 493.9};   // La2 Mi3 La3 Do4 Mi4 Si4
        double[] acordeB = {87.3, 130.8, 174.6, 220.0, 261.6, 329.6};    // Fa2 Do3 Fa3 La3 Do4 Mi4

        for (int v = 0; v < acordeA.length; v++) {
            double fa = ajustar(acordeA[v], L);
            double fb = ajustar(acordeB[v], L);
            double desfase = v * 1.3;
            for (int i = 0; i < N; i++) {
                double t = i / FS;
                double mezcla = 0.5 - 0.5 * Math.cos(2 * Math.PI * t / L);       // 0 -> 1 -> 0
                double trem = 0.75 + 0.25 * Math.sin(2 * Math.PI * (v % 2 + 1) * t / L + desfase);
                double vozA = Math.sin(2 * Math.PI * fa * t) + Math.sin(2 * Math.PI * (fa + 2 / L) * t);
                double vozB = Math.sin(2 * Math.PI * fb * t) + Math.sin(2 * Math.PI * (fb + 2 / L) * t);
                pad[i] += trem * ((1 - mezcla) * vozA + mezcla * vozB) * 0.5;
            }
        }
        normalizar(pad, 0.55);


        double[] estrellas = new double[N];
        double[] escala = {880, 1046.5, 1318.5, 1568, 1760, 2093};
        Random r = new Random(11);
        for (int k = 0; k < 9; k++) {
            double t0 = (k + r.nextDouble() * 0.7) * L / 9.0;
            double f = escala[r.nextInt(escala.length)];
            campana(estrellas, t0, f, 3.0, 0.5 + 0.5 * r.nextDouble(), 0.7, true);
        }
        // eco circular para dar sensacion de espacio
        double[] conEco = estrellas.clone();
        int d1 = (int) (0.41 * FS), d2 = (int) (0.83 * FS);
        for (int i = 0; i < N; i++) {
            conEco[i] += 0.38 * estrellas[(i - d1 + N) % N] + 0.20 * estrellas[(i - d2 + N) % N];
        }
        normalizar(conEco, 0.35);

        double[] total = new double[N];
        for (int i = 0; i < N; i++) total[i] = pad[i] + conEco[i];
        return aBytes(total, 0.60, false);
    }

    private static double[] vacio(double segundos) { return new double[(int) (segundos * FS)]; }

    private static double ajustar(double f, double L) { return Math.round(f * L) / L; }

    private static void campana(double[] buf, double inicio, double f, double dur,
                                double amp, double tau, boolean circular) {
        double[] ratio = {1.0, 2.76, 5.4};
        double[] peso = {1.0, 0.28, 0.10};
        double[] vida = {1.0, 0.5, 0.25};
        int i0 = (int) (inicio * FS);
        int n = (int) (dur * FS);
        for (int p = 0; p < ratio.length; p++) {
            double w = 2 * Math.PI * f * ratio[p];
            for (int i = 0; i < n; i++) {
                int idx = i0 + i;
                if (idx >= buf.length) {
                    if (!circular) break;
                    idx %= buf.length;
                }
                double t = i / FS;
                double env = Math.min(1.0, t / 0.004) * Math.exp(-t / (tau * vida[p]));
                buf[idx] += amp * peso[p] * env * Math.sin(w * t);
            }
        }
    }

    private static void normalizar(double[] b, double pico) {
        double max = 1e-9;
        for (double v : b) max = Math.max(max, Math.abs(v));
        double k = pico / max;
        for (int i = 0; i < b.length; i++) b[i] *= k;
    }

    private static byte[] aBytes(double[] b, double pico, boolean fundido) {
        normalizar(b, pico);
        byte[] out = new byte[b.length * 2];
        int fade = (int) (0.015 * FS);
        for (int i = 0; i < b.length; i++) {
            double v = b[i];
            if (fundido && i > b.length - fade) v *= (double) (b.length - i) / fade;
            int s = (int) Math.round(Math.max(-1, Math.min(1, v)) * 32767);
            out[2 * i] = (byte) (s & 0xFF);
            out[2 * i + 1] = (byte) ((s >> 8) & 0xFF);
        }
        return out;
    }
}