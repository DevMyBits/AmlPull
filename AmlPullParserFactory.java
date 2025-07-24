import java.io.IOException;
import java.io.InputStream;

/**
 * Créer le : vendredi 14 février 2025
 * Auteur   : Yoann Meclot (DevMyBits)
 * E-mail   : devmybits@gmail.com
 */
final class AmlPullParserFactory implements AmlPullParser
{
    private static String[] TYPES = {
            "START_DOCUMENT",
            "END_DOCUMENT",
            "START_TAG",
            "END_TAG",
            "COMMENT"
    };
    private static final String UNEXPECTED_EOI = "Unexpected end of input";

    private String[] mAttributes = new String[8];
    private String[] mElementStack = new String[16];
    private String[] mNspStack = new String[8];
    private int[] mNspCounts = new int[4];
    private byte[] mTemp = new byte[16];

    private InputStream mInput;
    private byte[] mBuffer;
    private int mCursor;
    private int mIndex;
    private int mPosition;
    private int mMax;
    private int mEventType;
    private int mAttributeCount;
    private int mLine;
    private int mLineOffset;
    private int mDepth;
    private String mName;
    private String mComment;
    private String mNamespace;
    private String mPrefix;
    private boolean mEndingTag;
    private boolean mProcessNsp;
    private boolean mProcessCmts;

    @Override
    public void setInput(InputStream input) throws AmlPullParserException
    {
        try {
            int available = input.available();
            mBuffer = new byte[(available <= 0 || available > 8192) ? 8192 : available];

            mInput = input;
            mEventType = START_DOCUMENT;
            mLine = 0;
            mLineOffset = 0;
            mDepth = 0;
            mEndingTag = false;
            mNamespace = null;
            mName = null;
            mComment = null;
            mPrefix = null;

            read();
        } catch (IOException e) {
            throw new AmlPullParserException(e);
        }
    }

    @Override
    public void setFeature(String feature, boolean value) throws AmlPullParserException
    {
        if (FEATURE_PROCESS_NAMESPACES.equals(feature)) mProcessNsp = value;
        else if (FEATURE_PROCESS_COMMENTS.equals(feature)) mProcessCmts = value;
        else throw new AmlPullParserException("Unsupported feature " + feature + " ! Provide a valid feature.");
    }

    @Override
    public void clear()
    {
        mBuffer = null;
        mTemp = new byte[16];
    }

    @Override
    public void close()
    {
        try {
            mInput.close();
        } catch (IOException ignored) {}
    }

    @Override
    public int next() throws AmlPullParserException
    {
        if (mEventType == END_TAG) mDepth--;
        if (mEndingTag)
        {
            mEndingTag = false;
            mEventType = END_TAG;

            return mEventType;
        }

        mEventType = readType();
        mPrefix = null;
        mName = null;
        mComment = null;
        mNamespace = null;
        mAttributeCount = -1;

        switch (mEventType)
        {
            case START_TAG:
                readStartTag();
                return START_TAG;
            case END_TAG:
                readEndTag();
                return END_TAG;
            case END_DOCUMENT:
                return END_DOCUMENT;
            case COMMENT:
                readComment();
                return COMMENT;
            default: throw new AmlPullParserException("Unexpected token");
        }
    }

    @Override
    public int getEventType()
    {
        return mEventType;
    }

    @Override
    public int getDepth()
    {
        return mDepth;
    }

    @Override
    public String getName()
    {
        int index = ((mDepth - 1) * 4) + 2;
        if (index < 0) return null;
        return mElementStack[index];
    }

    @Override
    public String getComment()
    {
        return mComment;
    }

    @Override
    public String getNamespace()
    {
        return mNamespace;
    }

    @Override
    public String getNamespace(String prefix) throws AmlPullParserException
    {
        for (int i = ((getNamespaceCount(mDepth) << 1) - 2); i >= 0; i -= 2)
        {
            if (prefix == null)
            {
                if (mNspStack[i] == null) return mNspStack[i + 1];
            }
            else if (prefix.equals(mNspStack[i])) return mNspStack[i + 1];
        }
        return NO_NAMESPACE;
    }

    @Override
    public int getNamespaceCount(int depth) throws AmlPullParserException
    {
        if (depth > mDepth) throw new AmlPullParserException("Array index out of bounds. index=" + depth + " but size=" + mNspCounts.length);
        return mNspCounts[depth];
    }

    @Override
    public String getNamespacePrefix(int index)
    {
        return mNspStack[index * 2];
    }

    @Override
    public int getAttributeCount()
    {
        return mAttributeCount;
    }

    @Override
    public String getAttributeNamespace(int index) throws AmlPullParserException
    {
        if (index >= mAttributeCount) throw new AmlPullParserException("Array index out of bounds. index=" + index + " but size=" + mAttributeCount);
        return mAttributes[index * 4];
    }

    @Override
    public String getAttributePrefix(int index) throws AmlPullParserException
    {
        if (index >= mAttributeCount) throw new AmlPullParserException("Array index out of bounds. index=" + index + " but size=" + mAttributeCount);
        return mAttributes[(index * 4) + 1];
    }

    @Override
    public String getAttributeValue(String namespace, String name)
    {
        for (int i = (mAttributeCount * 4) - 4; i >= 0; i -= 4) if ((mAttributes[i + 2].equals(name) && namespace == null) || mAttributes[i].equals(namespace)) return mAttributes[i + 3];
        return null;
    }

    @Override
    public String getAttributeValue(int index) throws AmlPullParserException
    {
        if (index >= mAttributeCount) throw new AmlPullParserException("Array index out of bounds. index=" + index + " but size=" + mAttributeCount);
        return mAttributes[(index * 4) + 3];
    }

    @Override
    public String getAttributeName(int index) throws AmlPullParserException
    {
        if (index >= mAttributeCount) throw new AmlPullParserException("Array index out of bounds. index=" + index + " but size=" + mAttributeCount);
        return mAttributes[(index * 4) + 2];
    }

    private int readType() throws AmlPullParserException
    {
        if (isEnd()) return END_DOCUMENT;
        if (mCursor == '{')
        {
            if (mIndex + 1 >= mMax) throw error("dangling {");
            if (read() == '/') return END_TAG;
            return START_TAG;
        }
        if (mCursor == '<')
        {
            if (!mProcessCmts) throw error("Are you set feature 'FEATURE_PROCESS_COMMENTS' to true ?");
            if (mIndex + 1 >= mMax) throw error("dangling <");
            if (read() == '!') return COMMENT;
        }
        throw error("Illegal type");
    }

    private void readStartTag() throws AmlPullParserException
    {
        skipWhiteSpaces();

        mName = readName();
        mAttributeCount = 0;
        while (true) {
            skipWhiteSpaces();
            if (mCursor == '/')
            {
                mEndingTag = true;
                read();
                skipWhiteSpaces();
                require('}');
                skipWhiteSpaces();
                break;
            }
            else if (mCursor == '}')
            {
                read();
                skipWhiteSpaces();
                break;
            }

            String attribute = readName();
            int i = (mAttributeCount++) * 4;

            mAttributes = updateArray(mAttributes, i + 4);
            mAttributes[i] = "";
            mAttributes[i + 1] = null;
            mAttributes[i + 2] = attribute;

            skipWhiteSpaces();
            if (isEnd()) throw error(UNEXPECTED_EOI);
            if (mCursor != '=') throw expected("attribute value. Are you set value to attribute with '=' character ?");
            else
            {
                read();
                skipWhiteSpaces();
                if (isEnd()) throw error(UNEXPECTED_EOI);

                require('"');
                mAttributes[i + 3] = readValue();
                read();
            }
        }

        int i = (mDepth++) * 4;

        mElementStack = updateArray(mElementStack, i + 4);
        mElementStack[i + 3] = mName;

        if (mDepth >= mNspCounts.length)
        {
            int[] nspCounts = new int[mDepth + 4];
            System.arraycopy(mNspCounts, 0, nspCounts, 0, mNspCounts.length);
            mNspCounts = nspCounts;
        }

        mNspCounts[mDepth] = mNspCounts[mDepth - 1];

        if (mProcessNsp) adjustNsp();
        else mNamespace = "";

        mElementStack[i] = mNamespace;
        mElementStack[i + 1] = mPrefix;
        mElementStack[i + 2] = mName;
    }

    private void readEndTag() throws AmlPullParserException
    {
        read();
        require('}');
        skipWhiteSpaces();

        if (mDepth == 0) throw expected("read end tag " + getName() + " with no tags open");
    }

    private void readComment() throws AmlPullParserException
    {
        int count = 0;
        while (true) {
            read();
            if (isEnd()) throw error(UNEXPECTED_EOI);

            mTemp = updateArray(mTemp, count);
            if (mCursor == '!')
            {
                if (read() == '>') break;
                else mTemp[count++] = (byte)'!';
            }
            mTemp[count++] = (byte)mCursor;
        }

        require('>');
        skipWhiteSpaces();

        mComment = new String(mTemp, 0, count);
    }

    private String readValue() throws AmlPullParserException
    {
        int count = 0;

        mTemp = updateArray(mTemp, count);
        mTemp[count++] = (byte) mCursor;
        while (true) {
            read();
            if (isEnd()) throw error(UNEXPECTED_EOI);

            mTemp = updateArray(mTemp, count);
            if (mCursor == '\\')
            {
                read();
                switch (mCursor)
                {
                    case '"':
                    case '/':
                    case '\\':
                        mTemp[count++] = (byte) mCursor;
                        break;
                    case 'b':
                        mTemp[count++] = (byte) '\b';
                        break;
                    case 'f':
                        mTemp[count++] = (byte) '\f';
                        break;
                    case 'n':
                        mTemp[count++] = (byte) '\n';
                        break;
                    case 'r':
                        mTemp[count++] = (byte) '\r';
                        break;
                    case 't':
                        mTemp[count++] = (byte) '\t';
                        break;
                    case 'u':
                        byte[] hex = new byte[4];
                        boolean isHex = true;
                        for (int i = 0; i < 4; i++)
                        {
                            read();
                            if (!isHex()) isHex = false;
                            hex[i] = (byte) mCursor;
                        }
                        if (isHex)
                        {
                            byte b = (byte)Integer.parseInt(new String(hex), 16);
                            mTemp[count++] = b;
                        }
                        else
                        {
                            mTemp = updateArray(mTemp, count + 6);
                            mTemp[count++] = (byte) '\\';
                            mTemp[count++] = (byte) 'u';
                            mTemp[count++] = hex[0];
                            mTemp[count++] = hex[1];
                            mTemp[count++] = hex[2];
                            mTemp[count++] = hex[3];
                        }
                        break;
                    default: throw expected("valid escape sequence");
                }
                read();
            }
            else if (mCursor == '"') break;

            mTemp = updateArray(mTemp, count);
            mTemp[count++] = (byte) mCursor;
        }
        return new String(mTemp, 0, count);
    }

    private String readName() throws AmlPullParserException
    {
        if (isEnd()) throw expected("name");

        int count = 0;

        mTemp = updateArray(mTemp, count);
        mTemp[count++] = (byte) mCursor;
        while (true) {
            read();
            if (isEnd()) throw error(UNEXPECTED_EOI);
            if ((mCursor >= 'a' && mCursor <= 'z') || (mCursor >= 'A' && mCursor <= 'Z') || isDigit() || mCursor == '_' || mCursor == '-' || mCursor == ':' || mCursor == '.' || mCursor >= '·')
            {
                mTemp = updateArray(mTemp, count);
                mTemp[count++] = (byte) mCursor;
                continue;
            }
            return new String(mTemp, 0, count);
        }
    }

    private int read()
    {
        if (!fillBuffer()) return -1;
        if (mCursor == '\n')
        {
            mLineOffset = mPosition;
            mLine++;
        }
        mCursor = mBuffer[mIndex++];
        mPosition++;
        return mCursor;
    }

    private void require(char c) throws AmlPullParserException
    {
        if (c != mCursor) throw expected("'" + c + "' character but '" + ((char)mCursor) + "' character is founded.");
        read();
    }

    private void skipWhiteSpaces()
    {
        while (isWhiteSpace()) read();
    }

    private String[] updateArray(String[] array, int required)
    {
        if (array.length >= required) return array;

        String[] attributes = new String[required + 16];
        System.arraycopy(array, 0, attributes, 0, array.length);
        return attributes;
    }

    private byte[] updateArray(byte[] array, int count)
    {
        if (count < array.length) return array;

        byte[] bigger = new byte[array.length + 16];
        System.arraycopy(array, 0, bigger, 0, array.length);
        return bigger;
    }

    private boolean isEnd()
    {
        return mCursor == -1;
    }

    private boolean isWhiteSpace()
    {
        return mCursor == ' ' || mCursor == '\n' || mCursor == '\r' || mCursor == '\t';
    }

    private boolean isDigit()
    {
        return mCursor >= '0' && mCursor <= '9';
    }

    private boolean isHex()
    {
        return isDigit() || mCursor >= 'a' && mCursor <= 'f' || mCursor >= 'A' && mCursor <= 'F';
    }

    private boolean fillBuffer()
    {
        if (mIndex < mMax) return true; // Unecessary to fill buffer, but notify this like an filled buffer for read more bytes of array buffered !!
        try {
            int count;
            if ((count = mInput.read(mBuffer)) < 0)
            {
                mCursor = -1;
                mIndex = -1;
                mMax = 0;
                return false;
            }
            mIndex = 0;
            mMax = count;
            return true;
        } catch (IOException e) {
            mCursor = -1;
            mIndex = -1;
            mMax = 0;
            return false;
        }
    }

    private void adjustNsp() throws AmlPullParserException
    {
        boolean any = false;
        for (int i = 0; i < (mAttributeCount << 2); i += 4)
        {
            String attrName = mAttributes[i + 2];
            int cut = attrName.indexOf(':');

            String prefix;
            if (cut != -1)
            {
                prefix = attrName.substring(0, cut);
                attrName = attrName.substring(cut + 1);
            }
            else if (attrName.equals("amlns"))
            {
                prefix = attrName;
                attrName = null;
            }
            else continue;

            if (!prefix.equals("amlns"))
                any = true;
            else
            {
                int e = (mNspCounts[mDepth]++) << 1;

                mNspStack = updateArray(mNspStack, e + 2);
                mNspStack[e] = attrName;
                mNspStack[e + 1] = mAttributes[i + 3];

                if (attrName != null && mAttributes[i + 3].isEmpty()) throw error("Illegal empty namespace");

                System.arraycopy(mAttributes, i + 4, mAttributes, i, ((--mAttributeCount) << 2) - i);
                i -= 4;
            }
        }
        if (any)
        {
            for (int i = (mAttributeCount << 2) - 4; i >= 0; i -= 4)
            {
                String attrName = mAttributes[i + 2];
                int cut = attrName.indexOf(':');
                if (cut == 0) throw error("Illegal attribute name '" + attrName + "'");
                else if (cut != -1)
                {
                    String attrPrefix = attrName.substring(0, cut);
                    attrName = attrName.substring(cut + 1);


                    String attrNs = getNamespace(attrPrefix);
                    if (attrNs == null) throw error("Undefined prefix: '" + attrPrefix + "'");

                    mAttributes[i] = attrNs;
                    mAttributes[i + 1] = attrPrefix;
                    mAttributes[i + 2] = attrName;
                }
            }
        }

        int cut = mName.indexOf(':');
        if (cut == 0) throw error("Illegal tag name: " + mName);
        if (cut != -1)
        {
            mPrefix = mName.substring(0, cut);
            mName = mName.substring(cut + 1);
        }

        mNamespace = getNamespace(mPrefix);
        if (mNamespace == null)
        {
            if (mPrefix != null) throw error("Undefined prefix: '" + mPrefix + "'");
            mNamespace = NO_NAMESPACE;
        }
    }

    private AmlPullParserException expected(String message) throws AmlPullParserException
    {
        if (isEnd()) throw error(message);
        return error("Expected " + message);
    }

    private AmlPullParserException error(String message)
    {
        return new AmlPullParserException(message, mLine, (mPosition - mLineOffset) - 1);
    }

    AmlPullParserFactory() {}
}

