import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Créer le : vendredi 18 avril 2025
 * Auteur     : Yoann Meclot (MSay2)
 * E-mail      : yoannmeclot@hotmail.com
 */
final class AmlElement implements AmlPullWriter.Element, Evaluator.Evaluable<String>
{
    private final String mName;
    private final Evaluator<String, AmlAttribute> mAttributes = new Evaluators<>();
    private final Evaluator<String, AmlElement> mElements = new Evaluators<>();
    private final Evaluator<String, AmlComment> mComments = new Evaluators<>();

    @Override
    public void addAttribute(String name, String value) throws AmlPullWriterException
    {
        mAttributes.push(new AmlAttribute(name, value));
    }

    @Override
    public AmlPullWriter.Attribute getAttribute(int index) throws AmlPullWriterException
    {
        if (index >= mAttributes.length()) throw new AmlPullWriterException("Array index out of bounds. index=" + index + " but size=" + mAttributes.length());
        return mAttributes.get(index);
    }

    @Override
    public void removeAttribute(String name)
    {
        mAttributes.remove(name);
    }

    @Override
    public void addElement(AmlPullWriter.Element element)
    {
        mElements.push((AmlElement)element);
    }

    @Override
    public AmlPullWriter.Element getElement(int index) throws AmlPullWriterException
    {
        if (index >= mElements.length()) throw new AmlPullWriterException("Array index out of bounds. index=" + index + " but size=" + mElements.length());
        return mElements.get(index);
    }

    @Override
    public void removeElement(AmlPullWriter.Element element)
    {
        mElements.remove(element.getName());
    }

    @Override
    public void addComment(String comment) throws AmlPullWriterException
    {
        mComments.push(new AmlComment(comment));
    }

    @Override
    public AmlPullWriter.Comment getComment(int index) throws AmlPullWriterException
    {
        if (index >= mComments.length()) throw new AmlPullWriterException("Array index out of bounds. index=" + index + " but size=" + mComments.length());
        return mComments.get(index);
    }

    @Override
    public String getName()
    {
        return mName;
    }

    @Override
    public int getElementCount()
    {
        return mElements.length();
    }

    @Override
    public int getAttributeCount()
    {
        return mAttributes.length();
    }

    @Override
    public int getCommentCount()
    {
        return mComments.length();
    }

    @Override
    public boolean toEvaluate(String name)
    {
        return mName.equals(name);
    }

    public void writeTo(final OutputStream output) throws AmlPullWriterException
    {
        try {
            for (int i = 0; i < mComments.length(); i++) mComments.get(i).writeTo(output);

            output.write(("{" + mName).getBytes(StandardCharsets.UTF_8));
            for (int i = 0; i < mAttributes.length(); i++) mAttributes.get(i).writeTo(output);

            if (mElements.isEmpty()) output.write('/');

            output.write('}');
            for (int i = 0; i < mElements.length(); i++) mElements.get(i).writeTo(output);

            if (!mElements.isEmpty()) output.write("{/}".getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new AmlPullWriterException(e);
        }
    }

    AmlElement(String name) throws AmlPullWriterException
    {
        if (name == null || name.trim().isEmpty()) throw new AmlPullWriterException("Illegal argument. You don't add element with null name.");
        mName = name;
    }
}
