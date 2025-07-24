import java.io.IOException;
import java.io.OutputStream;

/**
 * Créer le : vendredi 18 avril 2025
 * Auteur     : Yoann Meclot (DevMyBits)
 * E-mail      : devmybits@gmail.com
 */
final class AmlPullWriterFactory implements AmlPullWriter
{
    private OutputStream mOutput;
    private AmlElement mRootElement;

    @Override
    public void setOutput(OutputStream output)
    {
        mOutput = output;
    }

    @Override
    public void setElement(Element element)
    {
        mRootElement = (AmlElement)element;
    }

    @Override
    public void write() throws AmlPullWriterException
    {
        if (mRootElement == null) throw new AmlPullWriterException("Root element is null, define root element before call AmlPullWriter.write() method.");
        if (mOutput == null) throw new AmlPullWriterException("Output stream is null, define output stream before call AmlPullWriter.write() method.");
        mRootElement.writeTo(mOutput);
    }

    @Override
    public void close() throws AmlPullWriterException
    {
        try {
            mOutput.close();
        } catch (IOException e) {
            throw new AmlPullWriterException(e);
        }
    }
}
