import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Créer le : vendredi 18 avril 2025
 * Auteur     : Yoann Meclot (MSay2)
 * E-mail      : yoannmeclot@hotmail.com
 */
final class AmlAttribute implements AmlPullWriter.Attribute, Evaluator.Evaluable<String>
{
    private final String mName;
    private final String mValue;

    @Override
    public boolean toEvaluate(String name)
    {
        return mName.equals(name);
    }

    @Override
    public String getName()
    {
        return mName;
    }

    @Override
    public String getValue()
    {
        return mValue;
    }

    public void writeTo(OutputStream output) throws IOException
    {
        output.write((" " + mName + "=\"" + mValue + "\"").getBytes(StandardCharsets.UTF_8));
    }

    AmlAttribute(final String name, final String value) throws AmlPullWriterException
    {
        if ((name == null || name.trim().isEmpty()) || value == null) throw new AmlPullWriterException("Illegal argument(s). You don't add attribute with null name or empty name or null value.");

        mName = name;
        mValue = value;
    }
}
