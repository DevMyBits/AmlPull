import java.io.OutputStream;

/**
 * <h3>Atao Markup Language</h3> is an specifical textual format for struct data.
 * <br><br>
 * {@code AmlPullWriter} is an helper class to construct Atao Markup Language and to be written on output stream.
 * <br><br>
 * This is an example of AML format represented in string :
 * <blockquote><pre>
 *     {resource/}
 * </pre></blockquote>
 * <blockquote><pre>
 *     {resource}
 *          {attr name="atao:color" format="reference"/}
 *     {/}
 * </pre></blockquote>
 * <blockquote><pre>
 *     {resource type="in" format="stream"/}
 * </pre></blockquote>
 * <blockquote><pre>
 *     {resource type="in" format="stream"}
 *          {attr name="atao:color" format="enum"}
 *              {enum name="black" value="-1"/}
 *              {enum name="white" value="-2"/}
 *          {/}
 *     {/}
 * </pre></blockquote>
 * <blockquote><pre>
 *     <!This is first comment!>
 *     {resource type="in" format="stream"}
 *          <!This is second comment!>
 *          {attr name="atao:text" format="string"/}
 *          <!This is third comment!>
 *          {attr name="atao:size" format="integer"/}
 *     {/}
 * </pre></blockquote>
 * <br>
 * {@code AmlPullWriter} can use '\' character for escaping char in attribute value.
 * <br>
 * <blockquote><pre>
 *     {Message text="Je m\"appelle Yoann."/}
 * </pre></blockquote>
 * See {@code AmlPullParser} comment to see the list of supported escaped characters.
 * @since 1.0
 * @Version 1.1
 * @Created  : vendredi 18 avril 2025
 * @Author   : Yoann Meclot (DevMyBits)
 * @E-mail   : yoannmeclot@hotmail.com
 * @see AmlPullParser
 */
public interface AmlPullWriter
{
    /**
     * Create instance of AML writer.
     *
     * @return New instance of {@code AmlPullWriter}.
     * @since 1.0
     */
    static AmlPullWriter newPullWriter()
    {
        return new AmlPullWriterFactory();
    }

    /**
     * Create instance of element.
     *
     * @param name The name of AML element.
     * @return New instance of {@link AmlPullWriter.Element}.
     * @throws AmlPullWriterException If the name is null.
     * @since 1.0
     */
    static Element newElement(String name) throws AmlPullWriterException
    {
        return new AmlElement(name);
    }

    /**
     * Create instance of comment.
     *
     * @param text The text of comment.
     * @return New instance of {@link AmlPullWriter.Comment}.
     * @throws AmlPullWriterException If the text is null.
     * @since 1.0
     */
    static Comment newComment(String text) throws AmlPullWriterException
    {
        return new AmlComment(text);
    }

    /**
     * Define output stream to be written.
     *
     * @param output The output stream for written.
     * @since 1.0
     */
    void setOutput(OutputStream output);

    /**
     * Define root element of document.
     *
     * @param element The root element.
     * @since 1.0
     */
    void setElement(Element element);

    /**
     * Write root element on output stream.
     *
     * @throws AmlPullWriterException If root element is undefined or output stream is undefined or error is occurred in {@link java.io.IOException}
     */
    void write() throws AmlPullWriterException;

    /**
     * Close the output stream.
     *
     * @throws AmlPullWriterException If error is occurred in {@link java.io.IOException}
     */
    void close() throws AmlPullWriterException;

    /**
     * The {@code Element} class represent an element of AML format.
     * <br>
     * Use it to build an structure of AML document and AML child element.
     * <br><br>
     * Element with no child :
     * <blockquote><pre>
     *     {element_name/}
     * </pre></blockquote>
     *
     * Element with child :
     * <blockquote><pre>
     *     {element_name}{/}
     * </pre></blockquote>
     *
     * @see AmlPullWriter#newElement(String)
     * @since 1.0
     * @Version 1.0
     */
    interface Element
    {
        /**
         * Add new attribute to element.
         *
         * @param name The name (key) of attribute.
         * @param value The value attached by owned key to attribute.
         * @throws AmlPullWriterException If the name and/or value is null.
         * @since 1.0
         */
        void addAttribute(String name, String value) throws AmlPullWriterException;

        /**
         * Get the attribute by index.
         *
         * @param index The index of added attribute.
         * @return The added attribute indexed.
         * @throws AmlPullWriterException If index is out of bounds of attributes.
         * @since 1.0
         */
        Attribute getAttribute(int index) throws AmlPullWriterException;

        /**
         * Remove attribute by the owned name (key).
         *
         * @param name The attached name of attribute to be removed.
         * @since  1.0
         */
        void removeAttribute(String name);

        /**
         * Add new child element.
         *
         * @param element The child element.
         * @since 1.0
         */
        void addElement(Element element);

        /**
         * Get child element by index.
         *
         * @param index The index of added child element.
         * @return The added child element.
         * @throws AmlPullWriterException If index is out of bounds of children element.
         * @since 1.0
         */
        Element getElement(int index) throws AmlPullWriterException;

        /**
         * Remove child element.
         *
         * @param element The child element to be removed.
         * @since 1.0
         */
        void removeElement(Element element);

        /**
         * Add new comment to element.
         *
         * @param comment The comment to be added.
         * @throws AmlPullWriterException If text of comment is null.
         * @since 1.0
         */
        void addComment(String comment) throws AmlPullWriterException;

        /**
         * Get comment by index.
         *
         * @param index The index of added comment.
         * @return The added comment.
         * @throws AmlPullWriterException If index is out of bounds of comments.
         * @since 1.0
         */
        Comment getComment(int index) throws AmlPullWriterException;

        /**
         * Get name of this element.
         *
         * @return The name of this element.
         * @since 1.0
         */
        String getName();

        /**
         * Get added elements count.
         *
         * @return The count of added elements.
         * @since 1.0
         */
        int getElementCount();

        /**
         * Get the added attribute count.
         *
         * @return The count of added attribute.
         * @since 1.0
         */
        int getAttributeCount();

        /**
         * Get the added comment count.
         *
         * @return The count of added comment.
         * @since 1.1
         */
        int getCommentCount();
    }

    /**
     * The {@code Attribute} class represent an attribute with values pair (name and value) in AML format.
     * <br><br>
     * This is an example of attribute represented in string :
     * <blockquote><pre>
     *     [name]="[value]"
     * </pre></blockquote>
     *
     * @since 1.0
     * @Version 1.0
     */
    interface Attribute
    {
        /**
         * Get name of attribute.
         *
         * @return The name of attribute.
         * @since 1.0
         */
        String getName();

        /**
         * Get value of attribute.
         *
         * @return The value of attribute.
         * @since 1.0
         */
        String getValue();
    }

    /**
     * The {@code Comment} class represent an comment in AML format.
     * <br><br>
     * This is an example of comment represented in string :
     * <blockquote><pre>
     *     <!This is a comment!>
     * </pre></blockquote>
     *
     * @see AmlPullWriter#newComment(String)
     * @since 1.0
     * @Version 1.0
     */
    interface Comment
    {
        /**
         * Get text of comment.
         *
         * @return The text of comment.
         * @since 1.0
         */
        String getText();
    }
}

