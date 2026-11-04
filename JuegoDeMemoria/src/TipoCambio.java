import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TipoCambio {

    // aqui se pega el token que da el BCCR
    private static final String TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJCQ0NSLVNEREUiLCJzdWIiOiJnYWJlbW9saWNvcmRvMDNAZ21haWwuY29tIiwiYXVkIjoiU0RERS1TaXRpb0V4dGVybm8iLCJleHAiOjI1MzQwMjMwMDgwMCwibmJmIjoxNzkzMjg0NTE3LCJpYXQiOjE3OTMyODQ1MTcsImp0aSI6ImRhNWY1YmVmLTE4MTItNGI0Ny04OGYyLWE0MGM3NzdhYTNiNCIsImVtYWlsIjoiZ2FiZW1vbGljb3JkbzAzQGdtYWlsLmNvbSJ9.WU9oui89VqdNGhN6r1TQ571bbZyawD6PyvW-YamaH5k";

    private static final String BASE = "https://apim.bccr.fi.cr/SDDE/api/"
            + "Bccr.GE.SDDE.Publico.Indicadores.API/indicadoresEconomicos/";
    private static final String INDICADOR = "317";
    private static final int DIAS_DE_RESPALDO = 5;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final Pattern PATRON_VALOR = Pattern.compile(
            "\"valorDatoPorPeriodo\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");
    private static final HttpClient cliente = HttpClient.newHttpClient();

    // devuelve el tipo de cambio de hoy; si hoy no hay dato (fin de semana, feriado)
    // prueba con los dias anteriores
    public static double obtener() throws IOException, InterruptedException {
        if (TOKEN.startsWith("PEGUE")) {
            throw new IllegalStateException("Falta poner el token en TipoCambio.TOKEN");
        }
        LocalDate dia = LocalDate.now();
        for (int i = 0; i <= DIAS_DE_RESPALDO; i++) {
            Double valor = consultarDia(dia.minusDays(i));
            if (valor != null) {
                return valor;
            }
        }
        throw new IllegalStateException("El BCCR no devolvió datos en los últimos "
                + DIAS_DE_RESPALDO + " días.");
    }

    private static Double consultarDia(LocalDate dia) throws IOException, InterruptedException {
        String fecha = URLEncoder.encode(dia.format(FORMATO_FECHA), StandardCharsets.UTF_8);
        String url = BASE + INDICADOR + "/series?fechaInicio=" + fecha
                + "&fechaFin=" + fecha + "&idioma=ES";

        HttpRequest pedido = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + TOKEN)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> resp = cliente.send(pedido, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new IOException("El BCCR respondió con el código " + resp.statusCode());
        }
        return extraerValor(resp.body());
    }

    // busca el valor numerico en el JSON; null si no viene
    static Double extraerValor(String json) {
        Matcher m = PATRON_VALOR.matcher(json);
        if (m.find()) {
            return Double.parseDouble(m.group(1));
        }
        return null;
    }

    // para probar el token sin abrir el juego
    public static void main(String[] args) {
        try {
            System.out.println("Tipo de cambio: " + obtener());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}

