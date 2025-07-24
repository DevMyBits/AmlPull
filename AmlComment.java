import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Créer le : vendredi 18 avril 2025
 * Auteur     : Yoann Meclot (DevMyBits)
 * E-mail      : devmybits@gmail.com
 */
final class AmlComment implements AmlPullWriter.Comment, Evaluator.Evaluable<String>
{
    private final String mText;

    @Override
    public boolean toEvaluate(String text)
    {
        return mText.equals(text);
    }

    @Override
    public String getText()
    {
        return mText;
    }

    public void writeTo(OutputStream output) throws IOException
    {
        output.write(("<!" + mText + "!>").getBytes(StandardCharsets.UTF_8));
    }

    AmlComment(String text) throws AmlPullWriterException
    {
        if (text == null) throw new AmlPullWriterException("Illegal argument. You don't add comment with null text.");
        mText = text;
    }
}
