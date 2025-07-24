import java.io.InputStream;

/**
 * <h3>Atao Markup Language</h3> is an specifical textual format for struct data.
 * <br><br>
 * {@code AmlPullParser} is an helper class to parse Atao Markup Language on input stream.
 * <br><br>
 * This is an example to use this parser :
 * <blockquote><pre>
 *     AmlPullParser parser = AmlPullParser.newPullParser();
 *     parser.setInput(new FileInputStream(myFile));
 *
 *     int eventType;
 *     while ((eventType = parser.next()) != AmlPullParser.END_DOCUMENT) {
 *          switch (eventType) {
 *              case AmlPullParser.START_DOCUMENT:
 *                  break;
 *              case AmlPullParser.END_DOCUMENT:
 *                  break;
 *              case AmlPullParser.START_TAG:
 *                  break;
 *              case AmlPullParser.END_TAG:
 *                  break;
 *              case AmlPullParser.COMMENT:
 *                  break;
 *              default:break;
 *          }
 *     }
 * </pre></blockquote>
 * For parse comment, set {@code AmlPullParser.FEATURE_PROCESS_COMMENTS} feature constant :
 * <blockquote><pre>
 *     AmlPullParser parser ...;
 *     parser.setFeature(AmlPullParser.FEATURE_PROCESS_COMMENTS, true);
 * </pre></blockquote>
 * For parse namespace, set {@code AmlPullParser.FEATURE_PROCESS_NAMESPACES} feature constant :
 * <blockquote><pre>
 *     AmlPullParser parser ...;
 *     parser.setFeature(AmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
 * </pre></blockquote>
 * <br>
 * <h3>Supported escaped characters</h3>
 * {@code AmlPullParser} attempt to read escaped characters in attribute value.
 * <br>
 * This list show you the available characters to escaped.
 * <blockquote><pre>
 *     " / \ b f n r t u
 * </pre></blockquote>
 * @since 1.0
 * @Version 1.1
 * @Created  : vendredi 14 février 2025
 * @Author   : Yoann Meclot (DevMyBits)
 * @E-mail   : yoannmeclot@hotmail.com
 * @see AmlPullWriter
 */
public interface AmlPullParser
{
    /**
     * Create instance of AML parser.
     * @return New instance of {@code AmlPullParser}.
     * @since 1.0
     */
    static AmlPullParser newPullParser()
    {
        return new AmlPullParserFactory();
    }

    /**
     * This feature set the parser to evaluate namespaces in AML format.
     */
    String FEATURE_PROCESS_NAMESPACES = "process-namespaces";
    /**
     * This feature set the parser to evaluate comments in AML format.
     */
    String FEATURE_PROCESS_COMMENTS = "process-comments";

    /**
     * Constant value to define default namespace wen namespace not found.
     * @see AmlPullParser#getNamespace()
     * @see AmlPullParser#getNamespace(String)
     */
    String NO_NAMESPACE = "No namespace";

    /**
     * Trigger at the first step before call {@link AmlPullParser#next()} method and after call {@link AmlPullParser#setInput(InputStream)} method.
     */
    int START_DOCUMENT = 0;
    /**
     * Trigger at the end of input (document).
     */
    int END_DOCUMENT = 1;
    /**
     * Trigger at the start tag of AML element.
     * <blockquote><pre>
     *     {my_element [attributes]
     * </pre></blockquote>
     */
    int START_TAG = 2;
    /**
     * Trigger at the end of tag of last AML element opened.
     * <blockquote><pre>
     *     .../}
     *     ...{/}
     * </pre></blockquote>
     */
    int END_TAG = 3;
    /**
     * Trigger at the comment is founded by the parser.
     * <blockquote><pre>
     *     <!This is a comment!>
     * </pre></blockquote>
     * Use {@code AmlPullParser.getComment()} for get current comment.
     */
    int COMMENT = 4;

    /**
     * Define input stream to be parse.
     *
     * @param input The input stream to be parsed.
     * @throws AmlPullParserException If input stream is close and if error is occurred in {@link java.io.IOException}.
     * @since  1.0
     */
    void setInput(InputStream input) throws AmlPullParserException;

    /**
     * Define feature on parser.
     *
     * @param feature The feature type to be set.
     * @param value The value of the feature to be set.
     * @throws AmlPullParserException If the feature provided is unsupported.
     * @since 1.0
     * @see AmlPullParser#FEATURE_PROCESS_COMMENTS
     * @see AmlPullParser#FEATURE_PROCESS_NAMESPACES
     */
    void setFeature(String feature, boolean value) throws AmlPullParserException;

    /**
     * Clear the internal buffers.
     * @since 1.0
     */
    void clear();

    /**
     * Close the input stream.
     * @since 1.1
     */
    void close();

    /**
     * Go to the next token of AML document.
     *
     * @return Next token type of AML document.
     * @throws AmlPullParserException If serval errors is occurred on parsing AML document.
     * @see AmlPullParser#START_DOCUMENT
     * @see AmlPullParser#END_DOCUMENT
     * @see AmlPullParser#START_TAG
     * @see AmlPullParser#END_TAG
     * @see AmlPullParser#COMMENT
     * @since 1.0
     */
    int next() throws AmlPullParserException;

    /**
     * Get the current token.
     *
     * @return The current token type.
     * @see AmlPullParser#START_DOCUMENT
     * @see AmlPullParser#END_DOCUMENT
     * @see AmlPullParser#START_TAG
     * @see AmlPullParser#END_TAG
     * @see AmlPullParser#COMMENT
     * @since  1.0
     */
    int getEventType();

    /**
     * Get the current depth of the AML element.
     * <blockquote><pre>
     * <!Outside!> ------------------------------------ 0
     * {root} ----------------------------------------- 1
     *      {myElement/} ------------------------------ 2
     *      {myElement/} ------------------------------ 2
     *      {otherElement} ---------------------------- 2
     *          {childElement/} ----------------------- 3
     *      {/} --------------------------------------- 2
     * {/} -------------------------------------------- 1
     * <!Outside!> ------------------------------------ 0
     * </pre></blockquote>
     * @return The current depth.
     * @since 1.0
     */
    int getDepth();

    /**
     * Get name of element.
     *
     * @return The current name of element reached.
     * @since 1.0
     */
    String getName();

    /**
     * Get comment of element.
     *
     * @return The current comment reached.
     * @since 1.0
     */
    String getComment();

    /**
     * Get namespace of element.
     *
     * @return The current namespace reached.
     * @see AmlPullParser#NO_NAMESPACE
     * @since 1.0
     */
    String getNamespace();

    /**
     * Get namespace value of element by it's prefix.
     *
     * @param prefix The prefix namespace to be get value.
     * @return The value of namespace attached by it's prefix.
     * @throws AmlPullParserException If depth of reached element not have namespace.
     * @see AmlPullParser#NO_NAMESPACE
     * @since 1.0
     */
    String getNamespace(String prefix) throws AmlPullParserException;

    /**
     * Get namespace count by the depth of element.
     *
     * @param depth The depth of element to be reached.
     * @return The namespace count of element.
     * @throws AmlPullParserException If depth of reached element not have namespace.
     * @since 1.0
     */
    int getNamespaceCount(int depth) throws AmlPullParserException;

    /**
     * Get namespace prefix by index.
     *
     * @param index The index of namespace.
     * @return The namespace prefix.
     * @since 1.0
     */
    String getNamespacePrefix(int index);

    /**
     * Get attribute count of element.
     *
     * @return The current attribute count of element reached.
     * @since 1.0
     */
    int getAttributeCount();

    /**
     * Get namespace of attribute by index.
     *
     * @param index The index of attribute namespace.
     * @return The attribute namespace.
     * @throws AmlPullParserException If index is out of bounds of attribute count.
     * @since 1.0
     */
    String getAttributeNamespace(int index) throws AmlPullParserException;

    /**
     * Get prefix of attribute by index.
     *
     * @param index The index of attribute prefix.
     * @return The attribute prefix.
     * @throws AmlPullParserException If index is out of bounds of attribute count.
     * @since 1.0
     */
    String getAttributePrefix(int index) throws AmlPullParserException;

    /**
     * Get value of attribute by name or/and namespace.
     * <br>
     * Set {@code namespace} to {@code null} for only use entire name of attribute.
     *
     * @param namespace The namespace of attribute.
     * @param name The name of attribute.
     * @return The value of attribute to be attached by name and/or namespace.
     * @since 1.0
     */
    String getAttributeValue(String namespace, String name);

    /**
     * Get value of attribute by index.
     *
     * @param index The index of attribute value.
     * @return The attribute value.
     * @throws AmlPullParserException If index is out of bounds of attribute count.
     * @since 1.0
     */
    String getAttributeValue(int index) throws AmlPullParserException;

    /**
     * Get name of attribute by index.
     *
     * @param index The index of attribute name.
     * @return The attribute name.
     * @throws AmlPullParserException If index is out of bounds of attribute count.
     * @since 1.0
     */
    String getAttributeName(int index) throws AmlPullParserException;
}
