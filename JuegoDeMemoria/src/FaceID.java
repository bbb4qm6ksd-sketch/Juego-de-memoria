import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.objdetect.FaceDetectorYN;
import org.opencv.objdetect.FaceRecognizerSF;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

/**
 * Face ID para AstroMatch (login y registro con la cámara).
 *
 * Usa OpenCV (detector YuNet + reconocedor SFace). Los dos modelos .onnx se descargan solos
 * la primera vez a la carpeta "modelos_faceid" (unos 38 MB en total).
 *
 * Del rostro NO se guarda ninguna foto: solo 5 vectores de 128 números por usuario, en "faceid.txt".
 *
 * Uso desde el resto del proyecto:
 *   FaceID.instalarEnLogin(vista, this::abrirMenu);   // añade el botón "ENTRAR CON FACE ID" al login
 *   FaceID.ofrecerActivar(ventana, usuario);          // pregunta si quiere registrar su rostro
 *   FaceID.registrar(ventana, usuario);               // registra el rostro directamente
 *   FaceID.identificar(ventana);                      // devuelve el usuario reconocido, o null
 *   FaceID.tieneFaceID(usuario) / FaceID.eliminar(usuario)
 */
public final class FaceID {

    private FaceID() { }

    // ====================== AJUSTES (se pueden tocar) ======================
    private static final Path ARCHIVO = Paths.get("faceid.txt");
    private static final Path CARPETA_MODELOS = Paths.get("modelos_faceid");

    private static final int MUESTRAS = 5;                    // capturas que se guardan al registrar
    private static final long INTERVALO_MUESTRA_MS = 1400;    // pausa entre capturas (para cambiar de pose)
    private static final double UMBRAL = 0.45;                // similitud coseno mínima (mayor = más estricto)
    private static final double MARGEN = 0.05;                // ventaja mínima sobre el 2.º usuario más parecido
    private static final int RACHA_NECESARIA = 3;             // fotogramas seguidos que deben coincidir
    private static final double ANCHO_MIN = 0.20;             // el rostro debe ocupar al menos 20 % del ancho
    private static final long TIEMPO_MAX_MS = 45_000;         // tiempo máximo por intento

    private static final String BASE_1 = "https://media.githubusercontent.com/media/opencv/opencv_zoo/main/models/";
    private static final String BASE_2 = "https://github.com/opencv/opencv_zoo/raw/main/models/";
    // {archivo, subcarpeta en el repositorio, tamaño mínimo esperado en bytes}
    private static final String[][] MODELOS = {
            {"face_detection_yunet_2023mar.onnx", "face_detection_yunet/", "150000"},
            {"face_recognition_sface_2021dec.onnx", "face_recognition_sface/", "30000000"}
    };

    private static final Set<String> RECHAZADOS = new HashSet<>();   // quienes dijeron "ahora no" en esta sesión
    private static boolean nativasCargadas = false;

    // ====================== API PÚBLICA ======================

    /** Añade bajo el botón de iniciar sesión un botón "ENTRAR CON FACE ID". */
    public static void instalarEnLogin(VistaLogin vista, Consumer<String> alIngresar) {
        Container padre = vista.getBtnIngresar().getParent();
        if (padre == null) {
            return;
        }
        int pos = -1;
        for (int i = 0; i < padre.getComponentCount(); i++) {
            if (padre.getComponent(i) == vista.getBtnIngresar()) {
                pos = i;
            }
        }
        if (pos < 0) {
            return;
        }
        VistaTablero.BotonPill boton = new VistaTablero.BotonPill("ENTRAR CON FACE ID", false);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        Component espacio = Box.createVerticalStrut(8);
        ((JComponent) espacio).setAlignmentX(Component.LEFT_ALIGNMENT);
        padre.add(espacio, pos + 1);
        padre.add(boton, pos + 2);
        padre.revalidate();
        padre.repaint();

        boton.addActionListener(e -> {
            String usuario = identificar(vista);
            if (usuario != null) {
                vista.limpiarError();
                alIngresar.accept(usuario);
            }
        });
    }

    /** Pregunta si quiere activar Face ID (si todavía no lo tiene) y, si acepta, lo registra. */
    public static void ofrecerActivar(Window dueno, String usuario) {
        if (usuario == null || tieneFaceID(usuario) || RECHAZADOS.contains(usuario.toLowerCase())) {
            return;
        }
        Object[] opciones = {"Activar Face ID", "Ahora no"};
        int r = JOptionPane.showOptionDialog(dueno,
                "¿Quieres activar Face ID para entrar con tu rostro, sin escribir la contraseña?",
                "AstroMatch - Face ID", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, opciones, opciones[0]);
        if (r == 0) {
            registrar(dueno, usuario);
        } else {
            RECHAZADOS.add(usuario.toLowerCase());
        }
    }

    /** Abre la cámara y guarda el rostro de ese usuario. true si quedó registrado. */
    public static boolean registrar(Window dueno, String usuario) {
        VentanaCamara v = new VentanaCamara(dueno, VentanaCamara.Modo.REGISTRAR, usuario);
        v.iniciar();
        return v.exito;
    }

    /** Abre la cámara y devuelve el usuario cuyo rostro coincide, o null si no se reconoció / se canceló. */
    public static String identificar(Window dueno) {
        VentanaCamara v = new VentanaCamara(dueno, VentanaCamara.Modo.IDENTIFICAR, null);
        v.iniciar();
        return v.exito ? v.usuarioReconocido : null;
    }

    public static boolean tieneFaceID(String usuario) {
        return usuario != null && cargarTodo(false).containsKey(usuario.toLowerCase());
    }

    /** Borra el rostro guardado de ese usuario. */
    public static synchronized void eliminar(String usuario) {
        try {
            if (!Files.exists(ARCHIVO)) {
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (String linea : Files.readAllLines(ARCHIVO, StandardCharsets.UTF_8)) {
                String nombre = linea.split(";", 2)[0];
                if (!nombre.equalsIgnoreCase(usuario)) {
                    sb.append(linea).append(System.lineSeparator());
                }
            }
            Files.write(ARCHIVO, sb.toString().getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ignorado) {
            // si no se puede escribir, simplemente no se borra
        }
    }

    // ====================== ALMACENAMIENTO (faceid.txt) ======================
    // cada línea: usuario;vector1;vector2;...  (cada vector son 128 floats en Base64)

    private static synchronized void guardar(String usuario, List<float[]> muestras) throws IOException {
        eliminar(usuario);
        StringBuilder sb = new StringBuilder(usuario);
        for (float[] v : muestras) {
            sb.append(';').append(codificar(v));
        }
        sb.append(System.lineSeparator());
        Files.write(ARCHIVO, sb.toString().getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    /** Devuelve usuario(en minúsculas) -> vectores. Con soloCuentasExistentes ignora usuarios ya borrados de cuentas.txt. */
    private static synchronized Map<String, List<float[]>> cargarTodo(boolean soloCuentasExistentes) {
        Map<String, List<float[]>> mapa = new LinkedHashMap<>();
        try {
            if (!Files.exists(ARCHIVO)) {
                return mapa;
            }
            for (String linea : Files.readAllLines(ARCHIVO, StandardCharsets.UTF_8)) {
                String[] partes = linea.split(";");
                if (partes.length < 2) {
                    continue;
                }
                if (soloCuentasExistentes && !Cuentas.existe(partes[0])) {
                    continue;
                }
                List<float[]> vectores = new ArrayList<>();
                for (int i = 1; i < partes.length; i++) {
                    float[] v = decodificar(partes[i]);
                    if (v != null) {
                        vectores.add(v);
                    }
                }
                if (!vectores.isEmpty()) {
                    mapa.put(partes[0].toLowerCase(), vectores);
                }
            }
        } catch (IOException ignorado) {
            // archivo ilegible: se trata como si no hubiera rostros
        }
        return mapa;
    }

    private static String codificar(float[] v) {
        ByteBuffer b = ByteBuffer.allocate(v.length * 4).order(ByteOrder.LITTLE_ENDIAN);
        b.asFloatBuffer().put(v);
        return Base64.getEncoder().encodeToString(b.array());
    }

    private static float[] decodificar(String texto) {
        try {
            byte[] bytes = Base64.getDecoder().decode(texto.trim());
            if (bytes.length == 0 || bytes.length % 4 != 0) {
                return null;
            }
            float[] v = new float[bytes.length / 4];
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(v);
            return v;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // ====================== COMPARACIÓN ======================

    private static double coseno(float[] a, float[] b) {
        if (a.length != b.length) {
            return -1;
        }
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += (double) a[i] * b[i];
            na += (double) a[i] * a[i];
            nb += (double) b[i] * b[i];
        }
        double d = Math.sqrt(na) * Math.sqrt(nb);
        return d == 0 ? -1 : dot / d;
    }

    private static double mejorSimilitud(float[] v, List<float[]> guardados) {
        double mejor = -1;
        for (float[] g : guardados) {
            mejor = Math.max(mejor, coseno(v, g));
        }
        return mejor;
    }

    /** Resultado de comparar un rostro contra todos los usuarios registrados. */
    private static final class Coincidencia {
        String usuario;      // el más parecido (en minúsculas), o null
        double puntaje = -1; // su similitud
        double segundo = -1; // similitud del siguiente usuario
    }

    private static Coincidencia comparar(float[] v, Map<String, List<float[]>> base, String excluir) {
        Coincidencia c = new Coincidencia();
        for (Map.Entry<String, List<float[]>> e : base.entrySet()) {
            if (excluir != null && e.getKey().equalsIgnoreCase(excluir)) {
                continue;
            }
            double s = mejorSimilitud(v, e.getValue());
            if (s > c.puntaje) {
                c.segundo = c.puntaje;
                c.puntaje = s;
                c.usuario = e.getKey();
            } else if (s > c.segundo) {
                c.segundo = s;
            }
        }
        return c;
    }

    // ====================== OPENCV: motor de detección y reconocimiento ======================

    private static synchronized void cargarNativas() {
        if (nativasCargadas) {
            return;
        }
        try {
            // con el jar org.openpnp:opencv las librerías nativas vienen dentro y se cargan así
            Class.forName("nu.pattern.OpenCV").getMethod("loadLocally").invoke(null);
        } catch (ClassNotFoundException e) {
            // con una instalación normal de OpenCV (jar + .dll/.so/.dylib en java.library.path)
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("No se pudieron cargar las librerías nativas de OpenCV ("
                    + (e.getCause() != null ? e.getCause() : e) + ").", e);
        }
        nativasCargadas = true;
    }

    /** Un rostro detectado: fila = x, y, ancho, alto, 5 puntos de referencia (x,y) y la confianza. */
    private static final class Deteccion {
        final float[] fila;
        final int cantidad;   // cuántos rostros salían en el cuadro

        Deteccion(float[] fila, int cantidad) {
            this.fila = fila;
            this.cantidad = cantidad;
        }
    }

    private static final class Motor {
        private final FaceDetectorYN detector;
        private final FaceRecognizerSF reconocedor;
        private int ancho = -1;
        private int alto = -1;

        private Motor(Path detector, Path reconocedor) {
            this.detector = FaceDetectorYN.create(detector.toString(), "", new Size(640, 480), 0.9f, 0.3f, 5000);
            this.reconocedor = FaceRecognizerSF.create(reconocedor.toString(), "");
        }

        private static Motor guardado;   // se reutiliza: cargar los modelos tarda un poco

        static synchronized Motor crear(Consumer<String> estado) throws IOException {
            if (guardado != null) {
                return guardado;
            }
            cargarNativas();
            descargarModelos(CARPETA_MODELOS, estado);
            try {
                guardado = new Motor(CARPETA_MODELOS.resolve(MODELOS[0][0]), CARPETA_MODELOS.resolve(MODELOS[1][0]));
            } catch (RuntimeException primero) {
                // OpenCV en Windows falla con rutas que tienen tildes o ñ: se reintenta desde una ruta simple
                Path alterna = carpetaAlterna();
                Files.createDirectories(alterna);
                for (String[] m : MODELOS) {
                    Files.copy(CARPETA_MODELOS.resolve(m[0]), alterna.resolve(m[0]), StandardCopyOption.REPLACE_EXISTING);
                }
                guardado = new Motor(alterna.resolve(MODELOS[0][0]), alterna.resolve(MODELOS[1][0]));
            }
            return guardado;
        }

        /** El rostro más grande del cuadro, o null si no hay ninguno. */
        Deteccion detectar(Mat cuadro) {
            if (cuadro.cols() != ancho || cuadro.rows() != alto) {
                ancho = cuadro.cols();
                alto = cuadro.rows();
                detector.setInputSize(new Size(ancho, alto));
            }
            Mat caras = new Mat();
            try {
                detector.detect(cuadro, caras);
                int n = caras.rows();
                if (n <= 0) {
                    return null;
                }
                float[] mejor = null;
                for (int i = 0; i < n; i++) {
                    float[] f = new float[15];
                    caras.get(i, 0, f);
                    if (mejor == null || f[2] * f[3] > mejor[2] * mejor[3]) {
                        mejor = f;
                    }
                }
                return new Deteccion(mejor, n);
            } finally {
                caras.release();
            }
        }

        /** Vector de 128 números que identifica ese rostro. */
        float[] vector(Mat cuadro, float[] fila) {
            Mat cara = new Mat(1, 15, CvType.CV_32F);
            Mat alineada = new Mat();
            Mat rasgos = new Mat();
            try {
                cara.put(0, 0, fila);
                reconocedor.alignCrop(cuadro, cara, alineada);
                reconocedor.feature(alineada, rasgos);
                float[] v = new float[(int) rasgos.total()];
                rasgos.get(0, 0, v);
                return v;
            } finally {
                cara.release();
                alineada.release();
                rasgos.release();
            }
        }
    }

    private static Path carpetaAlterna() {
        String publica = System.getenv("PUBLIC");   // C:\Users\Public en Windows
        Path base = publica != null ? Paths.get(publica) : Paths.get(System.getProperty("java.io.tmpdir"));
        return base.resolve("astromatch_faceid");
    }

    private static void descargarModelos(Path carpeta, Consumer<String> estado) throws IOException {
        Files.createDirectories(carpeta);
        for (String[] m : MODELOS) {
            Path destino = carpeta.resolve(m[0]);
            long minimo = Long.parseLong(m[2]);
            if (Files.exists(destino) && Files.size(destino) >= minimo) {
                continue;
            }
            IOException ultimo = null;
            for (String base : new String[]{BASE_1, BASE_2}) {
                try {
                    descargar(base + m[1] + m[0], destino, minimo, estado);
                    ultimo = null;
                    break;
                } catch (IOException e) {
                    ultimo = e;
                }
            }
            if (ultimo != null) {
                throw new IOException("No se pudo descargar el modelo " + m[0] + " (" + ultimo.getMessage()
                        + "). Descárgalo a mano de " + BASE_2 + m[1] + m[0]
                        + " y ponlo en la carpeta " + carpeta.toAbsolutePath() + ".", ultimo);
            }
        }
    }

    private static void descargar(String url, Path destino, long minimo, Consumer<String> estado) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setConnectTimeout(15_000);
        con.setReadTimeout(30_000);
        con.setRequestProperty("User-Agent", "AstroMatch-FaceID");
        int codigo = con.getResponseCode();
        if (codigo != 200) {
            throw new IOException("HTTP " + codigo);
        }
        long total = con.getContentLengthLong();
        Path parcial = destino.resolveSibling(destino.getFileName() + ".part");
        try (InputStream in = con.getInputStream(); OutputStream out = Files.newOutputStream(parcial)) {
            byte[] buf = new byte[64 * 1024];
            long leido = 0;
            int n;
            int ultimoPorcentaje = -1;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
                leido += n;
                int p = total > 0 ? (int) (leido * 100 / total) : (int) (leido / 1_000_000);
                if (p != ultimoPorcentaje) {
                    ultimoPorcentaje = p;
                    estado.accept("Descargando modelo de Face ID (solo la primera vez)… "
                            + (total > 0 ? p + " %" : p + " MB"));
                }
            }
        }
        if (Files.size(parcial) < minimo) {
            Files.deleteIfExists(parcial);
            throw new IOException("descarga incompleta");
        }
        Files.move(parcial, destino, StandardCopyOption.REPLACE_EXISTING);
    }

    private static VideoCapture abrirCamara() {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        // En Windows se prueba DirectShow, luego Media Foundation y por último el modo por defecto
        int[] backends = windows
                ? new int[]{Videoio.CAP_DSHOW, Videoio.CAP_MSMF, Videoio.CAP_ANY}
                : new int[]{Videoio.CAP_ANY};
        for (int indice = 0; indice <= 3; indice++) {   // la webcam no siempre es la 0 (cámaras virtuales, USB, etc.)
            for (int backend : backends) {
                VideoCapture c = new VideoCapture(indice, backend);
                System.out.println("[FaceID] cámara " + indice + " (backend " + backend + "): "
                        + (c.isOpened() ? "abierta" : "no abre"));
                if (c.isOpened()) {
                    configurar(c);
                    if (entregaImagen(c)) {
                        System.out.println("[FaceID] usando cámara " + indice + " (backend " + backend + ")");
                        return c;
                    }
                    System.out.println("[FaceID] cámara " + indice + " abre pero no entrega imagen");
                }
                c.release();
            }
        }
        return null;
    }

    /** Comprueba que la cámara realmente entrega cuadros (las primeras pueden venir vacías mientras se calienta). */
    private static boolean entregaImagen(VideoCapture c) {
        Mat m = new Mat();
        try {
            for (int i = 0; i < 40; i++) {
                if (c.read(m) && !m.empty()) {
                    return true;
                }
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            m.release();
        }
        return false;
    }

    private static VideoCapture configurar(VideoCapture c) {
        c.set(Videoio.CAP_PROP_FRAME_WIDTH, 640);
        c.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);
        return c;
    }

    private static BufferedImage aImagen(Mat m) {
        BufferedImage img = new BufferedImage(m.cols(), m.rows(), BufferedImage.TYPE_3BYTE_BGR);
        byte[] datos = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        m.get(0, 0, datos);
        return img;
    }

    private static String escapar(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ====================== VENTANA DE LA CÁMARA ======================

    private static final class VentanaCamara extends JDialog {

        enum Modo { REGISTRAR, IDENTIFICAR }

        enum Estado { BUSCANDO, OK, ERROR, EXITO }

        private static final String[] PASOS = {
                "Mira al frente",
                "Sigue mirando al frente",
                "Gira un poco la cabeza a la izquierda",
                "Gira un poco la cabeza a la derecha",
                "Mira al frente y sonríe"
        };

        private final Modo modo;
        private final String usuario;
        private final PanelVideo video = new PanelVideo();
        private final Progreso progreso = new Progreso(MUESTRAS);
        private final JLabel lblEstado = new JLabel(" ", SwingConstants.CENTER);
        private final JButton btnCancelar = new VistaTablero.BotonPill("CANCELAR", false);

        private volatile boolean cancelado = false;
        boolean exito = false;
        String usuarioReconocido = null;

        VentanaCamara(Window dueno, Modo modo, String usuario) {
            super(dueno, "AstroMatch - Face ID", ModalityType.APPLICATION_MODAL);
            this.modo = modo;
            this.usuario = usuario;
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setResizable(false);

            Tema fondo = new Tema(90, new Color(0x141F47), Tema.FONDO);
            fondo.setLayout(new BorderLayout(0, 14));
            fondo.setBorder(BorderFactory.createEmptyBorder(26, 34, 26, 34));
            setContentPane(fondo);

            JPanel cabecera = new JPanel();
            cabecera.setOpaque(false);
            cabecera.setLayout(new BoxLayout(cabecera, BoxLayout.Y_AXIS));
            JLabel kicker = etiqueta("FACE ID", fuentes.mono(Font.BOLD, 11), Tema.HIELO);
            JLabel titulo = etiqueta(modo == Modo.REGISTRAR ? "Registra tu rostro" : "Mira a la cámara",
                    fuentes.mono(Font.PLAIN, 28), Tema.TEXTO);
            JLabel subtitulo = etiqueta(modo == Modo.REGISTRAR
                            ? "Tomaremos " + MUESTRAS + " capturas. Sigue las indicaciones."
                            : "Te reconoceremos para iniciar sesión.",
                    fuentes.mono(Font.PLAIN, 13), Tema.SECUNDARIO);
            kicker.setAlignmentX(Component.LEFT_ALIGNMENT);
            titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
            subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
            cabecera.add(kicker);
            cabecera.add(Box.createVerticalStrut(8));
            cabecera.add(titulo);
            cabecera.add(Box.createVerticalStrut(6));
            cabecera.add(subtitulo);
            fondo.add(cabecera, BorderLayout.NORTH);

            fondo.add(video, BorderLayout.CENTER);

            JPanel pie = new JPanel();
            pie.setOpaque(false);
            pie.setLayout(new BoxLayout(pie, BoxLayout.Y_AXIS));
            lblEstado.setFont(Tema.sans(Font.PLAIN, 14));
            lblEstado.setForeground(Tema.TEXTO);
            lblEstado.setAlignmentX(Component.CENTER_ALIGNMENT);
            progreso.setAlignmentX(Component.CENTER_ALIGNMENT);
            progreso.setVisible(modo == Modo.REGISTRAR);
            JLabel privacidad = etiqueta("Tu rostro no se guarda como foto: solo datos numéricos en este equipo.",
                    Tema.sans(Font.PLAIN, 11), Tema.SECUNDARIO);
            privacidad.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnCancelar.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnCancelar.setMaximumSize(new Dimension(220, 46));
            pie.add(lblEstado);
            pie.add(Box.createVerticalStrut(10));
            pie.add(progreso);
            pie.add(Box.createVerticalStrut(10));
            pie.add(privacidad);
            pie.add(Box.createVerticalStrut(14));
            pie.add(btnCancelar);
            fondo.add(pie, BorderLayout.SOUTH);

            btnCancelar.addActionListener(e -> {
                cancelado = true;
                dispose();
            });
            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosing(java.awt.event.WindowEvent e) { cancelado = true; }
                @Override public void windowClosed(java.awt.event.WindowEvent e)  { cancelado = true; }
            });

            video.setPreferredSize(new Dimension(520, 390));
            pack();
            setLocationRelativeTo(dueno);
        }

        /** Arranca la cámara en un hilo aparte y muestra la ventana (bloquea hasta que se cierra). */
        void iniciar() {
            Thread hilo = new Thread(this::trabajar, "FaceID-camara");
            hilo.setDaemon(true);
            hilo.start();
            setVisible(true);
        }

        // ---------- hilo de la cámara ----------

        private void trabajar() {
            VideoCapture camara = null;
            try {
                mensaje("Preparando Face ID…", Estado.BUSCANDO);
                Motor motor = Motor.crear(texto -> mensaje(texto, Estado.BUSCANDO));
                if (cancelado) {
                    return;
                }
                mensaje("Abriendo la cámara…", Estado.BUSCANDO);
                camara = abrirCamara();
                if (cancelado) {
                    return;
                }
                if (camara == null) {
                    fallo("No se pudo abrir la cámara. Cierra Zoom, Teams, el navegador u otras apps que la usen y revisa en Windows: "
                            + "Configuración > Privacidad y seguridad > Cámara > \"Permitir que las aplicaciones de escritorio accedan a la cámara\".");
                    return;
                }
                if (modo == Modo.REGISTRAR) {
                    bucleRegistro(camara, motor);
                } else {
                    bucleIdentificar(camara, motor);
                }
            } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
                e.printStackTrace();
                fallo("Falta la librería OpenCV o no se pudo cargar (" + e.getMessage() + ").");
            } catch (Throwable e) {
                e.printStackTrace();   // el detalle completo sale en la consola de IntelliJ
                fallo(e.getMessage() != null ? e.getMessage() : e.toString());
            } finally {
                if (camara != null) {
                    camara.release();
                }
            }
        }

        private void bucleRegistro(VideoCapture camara, Motor motor) throws InterruptedException {
            List<float[]> muestras = new ArrayList<>();
            Mat cuadro = new Mat();
            Mat espejo = new Mat();
            long inicio = System.currentTimeMillis();
            long ultima = 0;
            try {
                while (!cancelado && muestras.size() < MUESTRAS) {
                    if (System.currentTimeMillis() - inicio > TIEMPO_MAX_MS * 2) {
                        fallo("Se acabó el tiempo. Inténtalo de nuevo con mejor luz.");
                        return;
                    }
                    if (!leer(camara, cuadro, espejo)) {
                        if (System.currentTimeMillis() - inicio > 10_000) {
                            fallo("La cámara no entrega imagen. ¿La está usando otra aplicación?");
                            return;
                        }
                        continue;
                    }
                    Deteccion d = motor.detectar(espejo);
                    String problema = problema(d, espejo);
                    if (problema != null) {
                        ultima = 0;
                        mostrar(espejo, d, Estado.BUSCANDO, problema);
                        continue;
                    }
                    long ahora = System.currentTimeMillis();
                    if (ultima == 0) {
                        ultima = ahora;
                    }
                    mostrar(espejo, d, Estado.OK, PASOS[muestras.size()]);
                    if (ahora - ultima >= INTERVALO_MUESTRA_MS) {
                        muestras.add(motor.vector(espejo, d.fila));
                        ultima = ahora;
                        progreso.fijar(muestras.size());
                    }
                }
                if (cancelado) {
                    return;
                }
                // que ese rostro no esté ya vinculado a otra cuenta (evita confusiones al iniciar sesión)
                Map<String, List<float[]>> base = cargarTodo(true);
                for (float[] v : muestras) {
                    Coincidencia c = comparar(v, base, usuario);
                    if (c.usuario != null && c.puntaje >= UMBRAL) {
                        fallo("Este rostro ya está vinculado a otra cuenta.");
                        return;
                    }
                }
                guardar(usuario, muestras);
                exito = true;
                triunfo("¡Listo! Face ID activado.");
            } catch (IOException e) {
                fallo("No se pudo guardar tu rostro: " + e.getMessage());
            } finally {
                cuadro.release();
                espejo.release();
            }
        }

        private void bucleIdentificar(VideoCapture camara, Motor motor) throws InterruptedException {
            Map<String, List<float[]>> base = cargarTodo(true);
            if (base.isEmpty()) {
                fallo("Todavía no hay rostros registrados. Inicia sesión con tu contraseña y activa Face ID.");
                return;
            }
            Mat cuadro = new Mat();
            Mat espejo = new Mat();
            long inicio = System.currentTimeMillis();
            String candidato = null;
            int racha = 0;
            try {
                while (!cancelado) {
                    if (System.currentTimeMillis() - inicio > TIEMPO_MAX_MS) {
                        fallo("No se pudo reconocer tu rostro. Usa tu contraseña o inténtalo con mejor luz.");
                        return;
                    }
                    if (!leer(camara, cuadro, espejo)) {
                        if (System.currentTimeMillis() - inicio > 10_000) {
                            fallo("La cámara no entrega imagen. ¿La está usando otra aplicación?");
                            return;
                        }
                        continue;
                    }
                    Deteccion d = motor.detectar(espejo);
                    String problema = problema(d, espejo);
                    if (problema != null) {
                        candidato = null;
                        racha = 0;
                        mostrar(espejo, d, Estado.BUSCANDO, problema);
                        continue;
                    }
                    Coincidencia c = comparar(motor.vector(espejo, d.fila), base, null);
                    boolean claro = c.usuario != null && c.puntaje >= UMBRAL && c.puntaje - c.segundo >= MARGEN;
                    if (claro && c.usuario.equals(candidato)) {
                        racha++;
                    } else if (claro) {
                        candidato = c.usuario;
                        racha = 1;
                    } else {
                        candidato = null;
                        racha = 0;
                    }
                    mostrar(espejo, d, Estado.OK, racha > 0 ? "Verificando…" : "Mantén el rostro dentro del óvalo");
                    if (racha >= RACHA_NECESARIA) {
                        usuarioReconocido = nombreReal(candidato);
                        exito = true;
                        mostrar(espejo, d, Estado.EXITO, "");
                        triunfo("¡Hola, " + usuarioReconocido + "!");
                        return;
                    }
                }
            } finally {
                cuadro.release();
                espejo.release();
            }
        }

        /** Recupera el nombre con sus mayúsculas tal como se guardó. */
        private String nombreReal(String enMinusculas) {
            try {
                for (String linea : Files.readAllLines(ARCHIVO, StandardCharsets.UTF_8)) {
                    String nombre = linea.split(";", 2)[0];
                    if (nombre.equalsIgnoreCase(enMinusculas)) {
                        return nombre;
                    }
                }
            } catch (IOException ignorado) {
                // se usa el nombre en minúsculas
            }
            return enMinusculas;
        }

        /** Lee un cuadro de la cámara y lo voltea como un espejo. false si no hubo imagen. */
        private boolean leer(VideoCapture camara, Mat cuadro, Mat espejo) throws InterruptedException {
            if (!camara.read(cuadro) || cuadro.empty()) {
                Thread.sleep(30);
                return false;
            }
            Core.flip(cuadro, espejo, 1);
            return true;
        }

        /** Texto de ayuda si el cuadro no sirve para reconocer, o null si el rostro es válido. */
        private String problema(Deteccion d, Mat cuadro) {
            if (d == null) {
                return "No veo tu rostro. Colócalo dentro del óvalo.";
            }
            if (d.cantidad > 1) {
                return "Solo debe aparecer una persona.";
            }
            if (d.fila[2] < cuadro.cols() * ANCHO_MIN) {
                return "Acércate un poco a la cámara.";
            }
            float cx = d.fila[0] + d.fila[2] / 2f;
            float cy = d.fila[1] + d.fila[3] / 2f;
            if (Math.abs(cx - cuadro.cols() / 2f) > cuadro.cols() * 0.18
                    || Math.abs(cy - cuadro.rows() / 2f) > cuadro.rows() * 0.22) {
                return "Centra tu rostro en el óvalo.";
            }
            return null;
        }

        // ---------- actualizaciones de la interfaz (seguras desde cualquier hilo) ----------

        private void mostrar(Mat espejo, Deteccion d, Estado estado, String texto) {
            video.mostrar(aImagen(espejo), d == null ? null : d.fila, estado);
            mensaje(texto, estado);
        }

        private void mensaje(String texto, Estado estado) {
            String html = "<html><body style='width:420px;text-align:center'>" + escapar(texto) + "</body></html>";
            SwingUtilities.invokeLater(() -> {
                lblEstado.setText(html);
                lblEstado.setForeground(estado == Estado.ERROR ? Tema.ERROR
                        : estado == Estado.EXITO ? Tema.ESTRELLA : Tema.TEXTO);
                if (estado == Estado.ERROR) {   // los errores pueden ser largos: la ventana se ajusta para que se lean completos
                    pack();
                    setLocationRelativeTo(getOwner());
                }
            });
        }

        private void fallo(String texto) {
            if (cancelado) {
                return;
            }
            video.mostrar(null, null, Estado.ERROR);
            mensaje(texto, Estado.ERROR);
            SwingUtilities.invokeLater(() -> {
                btnCancelar.setText("CERRAR");
                Sonidos.reproducir(Sonidos.Efecto.ERROR);
            });
        }

        private void triunfo(String texto) throws InterruptedException {
            video.estadoFinal(Estado.EXITO);
            mensaje(texto, Estado.EXITO);
            SwingUtilities.invokeLater(() -> Sonidos.reproducir(Sonidos.Efecto.ACIERTO));
            Thread.sleep(1100);
            SwingUtilities.invokeLater(this::dispose);
        }
    }

    // ====================== COMPONENTES DE DIBUJO ======================

    private static JLabel etiqueta(String texto, Font fuente, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente);
        l.setForeground(color);
        return l;
    }

    /** Vista de la cámara con el óvalo guía, la línea de escaneo y las esquinas sobre el rostro. */
    private static final class PanelVideo extends JComponent {
        private volatile BufferedImage imagen;
        private volatile float[] fila;
        private volatile VentanaCamara.Estado estado = VentanaCamara.Estado.BUSCANDO;

        void mostrar(BufferedImage img, float[] cara, VentanaCamara.Estado est) {
            this.imagen = img;
            this.fila = cara;
            this.estado = est;
            repaint();
        }

        void estadoFinal(VentanaCamara.Estado est) {
            this.estado = est;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int w = getWidth();
            int h = getHeight();
            RoundRectangle2D marco = new RoundRectangle2D.Double(0, 0, w - 1, h - 1, 28, 28);
            g.setColor(Tema.SUPERFICIE);
            g.fill(marco);
            g.setClip(marco);

            BufferedImage img = imagen;
            double escala = 1;
            double ox = 0;
            double oy = 0;
            if (img != null) {
                escala = Math.max((double) w / img.getWidth(), (double) h / img.getHeight());
                int iw = (int) Math.round(img.getWidth() * escala);
                int ih = (int) Math.round(img.getHeight() * escala);
                ox = (w - iw) / 2.0;
                oy = (h - ih) / 2.0;
                g.drawImage(img, (int) ox, (int) oy, iw, ih, null);
            }

            VentanaCamara.Estado est = estado;
            Color color = est == VentanaCamara.Estado.ERROR ? Tema.ERROR
                    : est == VentanaCamara.Estado.BUSCANDO ? Tema.HIELO : Tema.ESTRELLA;

            // oscurece todo lo que queda fuera del óvalo
            double ew = w * 0.46;
            double eh = h * 0.80;
            Ellipse2D ovalo = new Ellipse2D.Double((w - ew) / 2, (h - eh) / 2, ew, eh);
            Area fuera = new Area(new Rectangle(0, 0, w, h));
            fuera.subtract(new Area(ovalo));
            g.setColor(new Color(6, 9, 20, 175));
            g.fill(fuera);

            // línea de escaneo
            if (est == VentanaCamara.Estado.BUSCANDO || est == VentanaCamara.Estado.OK) {
                double t = (System.nanoTime() / 1e9) % 2.4 / 2.4;
                double y = ovalo.getY() + t * eh;
                Shape anterior = g.getClip();
                g.clip(ovalo);
                g.setPaint(new GradientPaint(0, (float) y - 26, new Color(169, 188, 255, 0),
                        0, (float) y, new Color(169, 188, 255, 120)));
                g.fill(new Rectangle2D.Double(0, y - 26, w, 26));
                g.setColor(new Color(169, 188, 255, 200));
                g.draw(new Line2D.Double(0, y, w, y));
                g.setClip(anterior);
            }

            // contorno del óvalo
            Stroke trazo = est == VentanaCamara.Estado.BUSCANDO
                    ? new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{9f, 9f}, 0f)
                    : new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            g.setStroke(trazo);
            g.setColor(color);
            g.draw(ovalo);

            // esquinas sobre el rostro detectado
            float[] f = fila;
            if (f != null && img != null && est != VentanaCamara.Estado.ERROR) {
                int x = (int) (ox + f[0] * escala);
                int y = (int) (oy + f[1] * escala);
                int cw = (int) (f[2] * escala);
                int ch = (int) (f[3] * escala);
                int L = Math.max(14, Math.min(cw, ch) / 4);
                g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(color);
                esquina(g, x, y, L, L);
                esquina(g, x + cw, y, -L, L);
                esquina(g, x, y + ch, L, -L);
                esquina(g, x + cw, y + ch, -L, -L);
            }

            // palomita de éxito
            if (est == VentanaCamara.Estado.EXITO) {
                g.setColor(new Color(6, 9, 20, 150));
                g.fill(new Ellipse2D.Double(w / 2.0 - 46, h / 2.0 - 46, 92, 92));
                g.setColor(Tema.ESTRELLA);
                g.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Path2D palomita = new Path2D.Double();
                palomita.moveTo(w / 2.0 - 22, h / 2.0 + 2);
                palomita.lineTo(w / 2.0 - 6, h / 2.0 + 18);
                palomita.lineTo(w / 2.0 + 24, h / 2.0 - 16);
                g.draw(palomita);
            }

            g.setClip(null);
            g.setStroke(new BasicStroke(1.2f));
            g.setColor(Tema.LINEA);
            g.draw(marco);
            g.dispose();
        }

        private static void esquina(Graphics2D g, int x, int y, int dx, int dy) {
            g.draw(new Line2D.Double(x, y, x + dx, y));
            g.draw(new Line2D.Double(x, y, x, y + dy));
        }
    }

    /** Puntos de progreso del registro (uno por captura). */
    private static final class Progreso extends JComponent {
        private final int total;
        private volatile int hechas = 0;

        Progreso(int total) {
            this.total = total;
            setOpaque(false);
        }

        void fijar(int n) {
            hechas = n;
            repaint();
        }

        @Override public Dimension getPreferredSize() { return new Dimension(total * 26, 16); }
        @Override public Dimension getMaximumSize()   { return getPreferredSize(); }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 0; i < total; i++) {
                Ellipse2D punto = new Ellipse2D.Double(i * 26 + 4, 2, 12, 12);
                if (i < hechas) {
                    g.setColor(Tema.ESTRELLA);
                    g.fill(punto);
                } else {
                    g.setColor(Tema.LINEA);
                    g.setStroke(new BasicStroke(1.6f));
                    g.draw(punto);
                }
            }
            g.dispose();
        }
    }
}
