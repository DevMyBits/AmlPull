import java.io.IOException;

/**
 * Créer le : vendredi 18 avril 2025
 * Auteur : Yoann Meclot (MSay2)
 * E-mail : yoannmeclot@hotmail.com
 */
public class AmlPullWriterException extends IOException
{
    AmlPullWriterException(String message)
    {
        super(message);
    }

    AmlPullWriterException(Throwable cause)
    {
        super(cause);
    }
}
