package fr.ausy.traing.petals.services.metiers.livre.sql;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Objects;

public final class SQLUtils {

    private SQLUtils() {
        // classe utilitaire, ne peux être instanciée
    }

    /**
     * @param x
     * @return
     * @{see https://stackoverflow.com/questions/1812891/java-escape-string-to-prevent-sql-injection#answer-44025352}
     */
    public static String escapeString(final String x) {
        return escapeString(x, false);
    }

    /**
     * Échappement des caractères spéciaux SQL.
     * si {@code x} est null retourne null
     *
     * @param x                  chaîne à échapper
     * @param escapeDoubleQuotes chaîne échappée.
     * @return la chaine de caractère échappée
     */
    public static String escapeString(final String x, final boolean escapeDoubleQuotes) {
        if (Objects.isNull(x)) {
            return null;
        }
        final StringBuilder sBuilder = new StringBuilder(x.length() * 11 / 10);

        final int stringLength = x.length();

        for (int i = 0; i < stringLength; ++i) {
            final char c = x.charAt(i);

            switch (c) {
                case 0: /* Must be escaped for 'mysql' */
                    sBuilder.append('\\');
                    sBuilder.append('0');
                    break;

                case '\n': /* Must be escaped for logs */
                    sBuilder.append('\\');
                    sBuilder.append('n');
                    break;

                case '\r':
                    sBuilder.append('\\');
                    sBuilder.append('r');
                    break;

                case '\\':
                    sBuilder.append('\\');
                    sBuilder.append('\\');
                    break;

                case '\'':
                    sBuilder.append("''");
                    break;

                case '"': /* Better safe than sorry */
                    if (escapeDoubleQuotes) {
                        sBuilder.append('\\');
                    }
                    sBuilder.append('"');

                    break;

                case '\032': /* This gives problems on Win32 */
                    sBuilder.append('\\');
                    sBuilder.append('Z');
                    break;

                case '\u00a5':
                case '\u20a9':
                    // escape characters interpreted as backslash by mysql
                    // fall through
                default:
                    sBuilder.append(c);
            }
        }

        return sBuilder.toString();
    }

    /**
     * Échappement de champ de requête SQL
     *
     * @param s
     * @return
     */
    public static String escapeSQL(final String s) {
        final int length = s.length();
        int newLength = length;
        // first check for characters that might
        // be dangerous and calculate a length
        // of the string that has escapes.
        for (int i = 0; i < length; i++) {
            final char c = s.charAt(i);
            switch (c) {
                case '\\':
                case '\"':
                case '\'':
                case '\0':
                    newLength += 1;
                    break;
                default:
                    break;
            }
        }
        if (length == newLength) {
            // nothing to escape in the string
            return s;
        }
        final StringBuilder sb = new StringBuilder(newLength);
        for (int i = 0; i < length; i++) {
            final char c = s.charAt(i);
            switch (c) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\"':
                    sb.append("\\\"");
                    break;
                case '\'':
                    sb.append("\\'");
                    break;
                case '\0':
                    sb.append("\\0");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }


    /**
     * Retourne une chaine de caratères prête à être employée pour une requête SQL (avec les délimiteurs)
     *
     * @param s la chaîne de caractère à traiter
     * @return la chaîne de caratères au format SQL.
     */
    public static String getString(final String s) {
        if (StringUtils.isNotBlank(s)) {
            return "'" + SQLUtils.escapeString(s) + "'";
        }
        return "null";
    }

    /**
     * Retourne une chaine de caractère depuis un {@code XMLGregorianCalendar}
     * Cette méthode permet l'utilisation d'un champ XML contenant une date dans une requête SQL
     *
     * @param s la date à formater pour une requête SQL
     * @return la date au format SQL.
     */
    public static String getStringFromXMLGregorianCalendar(final XMLGregorianCalendar s) {
        if (Objects.nonNull(s) && s.isValid()) {
            return "'" + s + "'";
        }
        return "null";
    }

    /**
     * Permet de palier le fait que le BC-sql retourne une chaine de caratère "NULL" dans le XML plutôt qu'un tag vide (ou pas de tag) <column name="toto"/> par exemple
     * <p>
     * retourne null si la chaine == "NULL"
     *
     * @param value la chaie à tester
     * @return null ou la chaine
     */
    public static String getStringFromColumn(final String value) {
        if (Objects.isNull(value) || "null".equalsIgnoreCase(value)) {
            return null;
        }
        return value;
    }

    /**
     * Création d'un {@code Double} depuis un {@code String}
     * La notation scientifique n'est pas prise en compte, ni l'emploi de suffixe java (l / f /d etc).
     *
     * @param value chaine à convertir en {@code Double}
     * @return {@code Double} correspondant à {@code value} ou 0d si la chaine n'est pas un nombre valide
     */
    public static Double getDoubleFromString(final String value) {
        // utilisation de isParsable plutôt que isCreatable pour accepter un possible zéro à gauche (par exemple 09)
        if (!NumberUtils.isParsable(value)) {
            return 0d;
        }
        return NumberUtils.createDouble(value);
    }

    /**
     * Création d'un {@code Float} depuis un {@code String}.
     * La notation scientifique n'est pas prise en compte, ni l'emploi de suffixe java (l / f /d etc).
     *
     * @param value chaine à convertir en {@code Float}
     * @return {@code Float} correspondant à {@code value} ou 0f si la chaine n'est pas un nombre valide
     */
    public static Float getFloatFromString(final String value) {
        // utilisation de isParsable plutôt que isCreatable pour accepter un possible zéro à gauche (par exemeple 09)
        if (!NumberUtils.isParsable(value)) {
            return 0f;
        }
        return NumberUtils.createFloat(value);
    }

    /**
     * Création d'un {@code Integer} depuis un {@code String}.
     * similaire à getIntegerFromColumn mais sans Levée d'exception.
     *
     * @param value chaine à convertir en {@code Integer}
     * @return {@code Integer} correspondant à {@code value} ou 0f si la chaine n'est pas un nombre valide
     */
    public static Integer getIntegerFromString(final String value) {
        // utilisation de isParsable plutôt que isCreatable pour accepter un possible zéro à gauche (par exemeple 09)
        if (!NumberUtils.isParsable(value)) {
            return 0;
        }
        return NumberUtils.createInteger(value);
    }

    /**
     * Création d'un {@code Long} depuis un {@code String}.
     * La notation scientifique n'est pas prise en compte, ni l'emploi de suffixe java (l / f /d etc).
     *
     * @param value chaine à convertir en {@code Long}
     * @return {@code Long} correspondant à {@code value} ou 0f si la chaine n'est pas un nombre valide
     */
    public static Long getLongFromString(final String value) {
        // utilisation de isParsable plutôt que isCreatable pour accepter un possible zéro à gauche (par exemeple 09)
        if (!NumberUtils.isParsable(value)) {
            return 0L;
        }
        return NumberUtils.createLong(value);
    }

    /**
     * Traitement d'un booleen
     * si le paramètre est null on retourne false, dans le cas contraire le paramètre
     * <p>
     * BC-SQL retourne que des String, le getString sur une colonne de type booleen retourne que la 1ère lettre (f / t)
     *
     * @param s
     * @return
     */
    public static Boolean getBoolean(final String s) {
        if (Objects.nonNull(s)) {
            return "t".equalsIgnoreCase(s);
        }
        return false;
    }

    /**
     * Retourne un entier à partir d'une chaîne de caractères
     * Une entrée incorrecte lève une {@link IllegalArgumentException}
     *
     * @param val Chaine à convertir
     * @return Entier correspondant.
     */
    public static int getIntegerFromColumn(final String val) {
        try {
            return Integer.parseInt(val);
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("La chaîne {" + val + "} n'est pas un nombre entier valide");
        }
    }

}
