import java.io.IOException;

/**
 * Créer le : vendredi 14 février 2025
 * Auteur : Yoann Meclot (MSay2)
 * E-mail : yoannmeclot@hotmail.com
 */
public class AmlPullParserException extends IOException
{
    AmlPullParserException(String message)
    {
        super(message);
    }

    AmlPullParserException(Throwable cause)
    {
        super(cause);
    }

    AmlPullParserException(String message, int line, int column)
    {
        super(message + " at line: " + line + " column: " + column);
    }
}
