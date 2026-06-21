package com.srcarq.hangman;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

/**
 *
 * @author srcarq
 */
@Component
public class WordFactory {

    private List<String> wordListEasy = List.of(
            "GATO", "PERRO", "CASA", "MESA", "SILLA", "CAMA", "SOL", "LUNA", "FLOR", "AGUA",
            "FUEGO", "TIERRA", "VIENTO", "RIO", "MAR", "PEZ", "PATO", "OJO", "BOCA", "MANO",
            "DEDO", "PIE", "GOMA", "LORO", "LOBO", "CISNE", "VELA", "HOJA", "CAJA", "SOMBRA",
            "LAGO", "ISLA", "JARDIN", "CALLE", "CAMINO", "ARBOL", "NUBE", "ESTRELLA", "PIEDRA", "HOYO",
            "TORO", "VACA", "OVEJA", "BURRO", "RANA", "MOSCA", "ABEJA", "HUEVO", "PAN", "QUESO",
            "LECHE", "MIEL", "SAL", "AZUCAR", "TE", "CAFE", "JUGO", "SEDA", "BARCO", "COCHE",
            "TREN", "BUS", "AVION", "BICI", "BOTE", "LLAVE", "PUERTA", "VASO", "TAZA", "PLATO",
            "TENEDOR", "CUCHILLO", "CUCHARA", "TOALLA", "CEPILLO", "ESPEJO", "RELOJ", "ANILLO", "SOMBRERO", "CAMISA",
            "PANTALON", "ZAPATO", "MEDIAS", "CHAQUETA", "GORRA", "GUANTE", "BUFANDA", "CHALECO", "CINTURON", "BOLSO",
            "CARTA", "LIBRO", "DIARIO", "PLUMA", "LAPIZ", "BOLIGRAFO", "TIJERAS", "PEGAMENTO", "ÑU","PAPEL", "CARPETA"
    );

    private List<String> wordListMedium = List.of(
            "CAMPAÑA", "BOSQUE", "AVISPON", "JARDINERO", "GALLINAS", "ESCALERA", "COMETA", "CIRCUITO", "ALFOMBRA", "ASTILLA",
            "BOTELLON", "CAMAROTE", "DEFENSOR", "ELEFANTE", "FABRICA", "GALAXIA", "HELICES", "ILUSION", "JUGUETES", "FERIA",
            "LIBRERIA", "MAQUINAS", "NACIONAL", "OPERADOR", "PRODUCTO", "QUIMICOS", "RELOJERA", "SALVAJE", "TAMBORES", "UNIVERSO",
            "VACACIONES", "CASTAÑA", "XILOFONO", "YACIMIENTO", "ZAPATERIA", "ACUMULAR", "BRINDAR", "CAPITULO", "DETALLE", "EMBARCAR",
            "FRASEAR", "GESTIONAR", "HILANDAR", "IMPULSAR", "JUBILADO", "KILOMETRO", "LOGISTICA", "MONUMENTO", "NAVEGABLE", "OFERTA",
            "PRINCIPIO", "QUEDARSE", "RESONAR", "SISTEMA", "TRIUNFAR", "USUARIO", "VIGILANCIA", "ADMINISTRADOR", "JUGULAR", "ZOOTECNIA",
            "ACORDES", "BOTONERA", "CIFRARSE", "ANTAÑO", "ENTREGA", "FOTOGRAFO", "GIMNASIO", "HORMIGON", "IDENTIDAD", "JOVENES",
            "CABAÑA", "LECHUGAS", "MOTIVADO", "NEURONA", "OBSERVAR", "PINTORAS", "QUINIELA", "RECICLAR", "SIMETRIA", "TERRENO",
            "ULTRASONIDO", "VALORADO", "REBAÑO", "MEDICO", "ZONIFICA", "AUTOMATA", "BALIZADO", "CLIMATICO", "DIGITAL", "MONTAÑA", "ELECTRICO"
    );

    private List<String> wordListHard = List.of(
            "BICICLETA", "ASTRONOMIA", "CIRCUNFERENCIA", "DEMOCRACIA", "ENCICLOPEDIA", "FOTOGRAFICO", "GRANJEANDO", "HIPOTENUSA", "ICONOGRAFIA", "JURISDICCION",
            "KARATECAS", "LEXICOGRAFIA", "MICROONDAS", "NEOCORTICAL", "OXIMETRIA", "PARALELEPIPEDO", "QUERELLAS", "RECTANGULAR", "SIMBOLOGIA", "TERMOESTATO",
            "ULTRAVIOLETA", "VIBROACUSTICA", "OBSERVATORIO", "XEROGRAFIA", "YACIMIENTO", "ZIGZAGUEADO", "ARTICULACION", "BROMATOLOGIA", "CATALIZADOR", "DIAGNOSTICO",
            "ESTADISTICA", "FOTOMETRIA", "GASTRONOMIA", "HEMODIALISIS", "IDEOLOGICO", "JUDICIAL", "DIPLOMATICO", "LAMBDAFICADO", "MICROSCOPIO", "NUTRICIONAL",
            "ORGANIZADOR", "PAPILOTERO", "QUIMIOTERAPIA", "RECONCILIAR", "SINDICALIZAR", "TERMINOLOGIA", "ULTRASONIDO", "VECINDADURA", "IMPRESORAS", "JUEGATERIAS",
            "KILOTONELADA", "LITERATURAS", "MATERIALISMO", "NECTARINAS", "OPERATORIA", "PRESTIGIOS", "QUANTIFICAR", "RADIOGRAFIA", "SEMIOTICAS", "TRANSFORMADOR",
            "UNIVERSIDAD", "VALORATIVO", "DIDACTICO", "XENOFOBICOS", "CARACTERISTICO", "ZONIFICACION", "ANTROPOMORFO", "BIOLOGICOS", "CLIMATIZADO", "DEMITIFICAR",
            "ELECTRODOS", "FILOSOFICOS", "GEOTERMIA", "HIDROLOGICO", "ILUMINACION", "JARDINERIA", "ÑOÑERIA", "DESPEÑADERO"
    );

    public String selectWordByLevel(int level) {
        List<String> words = switch (level) {
            case 1 -> wordListEasy;
            case 2 -> wordListMedium;
            case 3 -> wordListHard;
            default -> throw new IllegalArgumentException("Nivel no válido: " + level + " (use 1, 2 o 3)");
        };

        int randomNum = ThreadLocalRandom.current().nextInt(words.size());
        return words.get(randomNum);
    }

}
