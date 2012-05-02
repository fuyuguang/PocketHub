/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.util;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static android.text.Spanned.SPAN_MARK_MARK;
import android.text.Editable;
import android.text.Html.ImageGetter;
import android.text.Html.TagHandler;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.text.style.TypefaceSpan;

import org.xml.sax.XMLReader;

/**
 * HTML Utilities
 */
public class HtmlUtils {

    private static final String TAG_ROOT = "githubroot";

    private static final String ROOT_START = '<' + TAG_ROOT + '>';

    private static final String ROOT_END = "</" + TAG_ROOT + '>';

    private static final String TAG_DEL = "del";

    private static final String TAG_UL = "ul";

    private static final String TAG_OL = "ol";

    private static final String TAG_LI = "li";

    private static final String TAG_CODE = "code";

    private static final String TAG_PRE = "pre";

    private static final String TOGGLE_START = "<span class=\"email-hidden-toggle\">";

    private static final String TOGGLE_END = "</span>";

    private static final String REPLY_START = "<div class=\"email-quoted-reply\">";

    private static final String REPLY_END = "</div>";

    private static final String SIGNATURE_START = "<div class=\"email-signature-reply\">";

    private static final String SIGNATURE_END = "</div>";

    private static final String EMAIL_START = "<div class=\"email-fragment\">";

    private static final String EMAIL_END = "</div>";

    private static final String HIDDEN_REPLY_START = "<div class=\"email-hidden-reply\" style=\" display:none\">";

    private static final String HIDDEN_REPLY_END = "</div>";

    private static final String BREAK = "<br>";

    private static final String PARAGRAPH_START = "<p>";

    private static final String PARAGRAPH_END = "</p>";

    private static final String BLOCKQUOTE_START = "<blockquote>";

    private static final String BLOCKQUOTE_END = "</blockquote>";

    private static final String SPACE = "&nbsp;";

    private static final String PRE_START = "<pre>";

    private static final String PRE_END = "</pre>";

    private static final String CODE_START = "<code>";

    private static final String CODE_END = "</code>";

    private static final TagHandler TAG_HANDLER = new TagHandler() {

        private int indentLevel;

        public void handleTag(final boolean opening, final String tag, final Editable output, final XMLReader xmlReader) {
            if (TAG_DEL.equalsIgnoreCase(tag)) {
                if (opening) {
                    int length = output.length();
                    output.setSpan(new StrikethroughSpan(), length, length, SPAN_MARK_MARK);
                } else {
                    int length = output.length();
                    Object span = getLast(output, StrikethroughSpan.class);
                    int start = output.getSpanStart(span);
                    output.removeSpan(span);
                    if (start != length)
                        output.setSpan(span, start, length, SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                return;
            }

            if (TAG_UL.equalsIgnoreCase(tag) || TAG_OL.equalsIgnoreCase(tag)) {
                if (opening)
                    indentLevel++;
                else
                    indentLevel--;

                if (!opening && indentLevel == 0)
                    output.append('\n');
                return;
            }

            if (TAG_LI.equalsIgnoreCase(tag)) {
                if (opening) {
                    output.append('\n');
                    for (int i = 0; i < indentLevel * 2; i++)
                        output.append(' ');
                    output.append('\u2022').append(' ').append(' ');
                }
                return;
            }

            if (TAG_CODE.equalsIgnoreCase(tag)) {
                if (opening) {
                    int length = output.length();
                    TypefaceSpan span = new TypefaceSpan("monospace");
                    output.setSpan(span, length, length, SPAN_MARK_MARK);
                } else {
                    int length = output.length();
                    Object span = getLast(output, TypefaceSpan.class);
                    int start = output.getSpanStart(span);
                    output.removeSpan(span);
                    if (start != length)
                        output.setSpan(span, start, length, SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                return;
            }

            if (TAG_PRE.equalsIgnoreCase(tag)) {
                output.append('\n');
                return;
            }

            if (TAG_ROOT.equalsIgnoreCase(tag) && !opening) {
                // Remove leading newlines
                while (output.length() > 0 && output.charAt(0) == '\n')
                    output.delete(0, 1);

                // Remove trailing newlines
                int last = output.length() - 1;
                while (last >= 0 && output.charAt(last) == '\n') {
                    output.delete(last, last + 1);
                    last = output.length() - 1;
                }
                return;
            }
        }
    };

    /**
     * Get last span of given kind
     *
     * @param text
     * @param kind
     * @return span
     */
    private static Object getLast(final Spanned text, final Class<?> kind) {
        Object[] spans = text.getSpans(0, text.length(), kind);
        return spans.length > 0 ? spans[spans.length - 1] : null;
    }

    /**
     * Encode HTML
     *
     * @param html
     * @return html
     */
    public static CharSequence encode(String html) {
        return encode(html, null);
    }

    /**
     * Encode HTML
     *
     * @param html
     * @param imageGetter
     * @return html
     */
    public static CharSequence encode(final String html, final ImageGetter imageGetter) {
        if (html == null)
            return "";
        if (html.length() == 0)
            return html;

        return android.text.Html.fromHtml(html, imageGetter, TAG_HANDLER);
    }

    private static StringBuilder strip(final StringBuilder input, final String prefix, final String suffix) {
        int start = input.indexOf(prefix);
        while (start != -1) {
            int end = input.indexOf(suffix, start + prefix.length());
            if (end == -1)
                end = input.length();
            input.delete(start, end + suffix.length());
            start = input.indexOf(prefix, start);
        }
        return input;
    }

    private static StringBuilder replace(final StringBuilder input, final String from, final String to) {
        int start = input.indexOf(from);
        int length = from.length();
        while (start != -1) {
            input.delete(start, start + length);
            input.insert(start, to);
            start = input.indexOf(from, start);
        }
        return input;
    }

    private static StringBuilder replace(final StringBuilder input, final String fromStart, final String fromEnd,
            final String toStart, final String toEnd) {
        int start = input.indexOf(fromStart);
        while (start != -1) {

            input.delete(start, start + fromStart.length());
            input.insert(start, toStart);

            int end = input.indexOf(fromEnd, start + toStart.length());
            if (end != -1) {
                input.delete(end, end + fromEnd.length());
                input.insert(end, toEnd);
            }

            start = input.indexOf(fromStart);
        }
        return input;
    }

    private static StringBuilder formatPres(final StringBuilder input) {
        int start = input.indexOf(PRE_START);
        final int spaceAdvance = SPACE.length() - 1;
        final int breakAdvance = BREAK.length() - 1;
        while (start != -1) {
            int end = input.indexOf(PRE_END, start + PRE_START.length());
            if (end == -1)
                break;

            // Skip over code element
            if (input.indexOf(CODE_START, start) == start)
                start += CODE_START.length();
            if (input.indexOf(CODE_END, start) == end - CODE_END.length())
                end -= CODE_END.length();

            for (int i = start; i < end; i++) {
                switch (input.charAt(i)) {
                case ' ':
                    input.deleteCharAt(i);
                    input.insert(i, SPACE);
                    start += spaceAdvance;
                    end += spaceAdvance;
                    break;
                case '\t':
                    input.deleteCharAt(i);
                    input.insert(i, SPACE);
                    start += spaceAdvance;
                    end += spaceAdvance;
                    for (int j = 0; j < 3; j++) {
                        input.insert(i, SPACE);
                        start += spaceAdvance + 1;
                        end += spaceAdvance + 1;
                    }
                    break;
                case '\n':
                    input.deleteCharAt(i);
                    // Ignore if last character is a newline
                    if (i + 1 < end) {
                        input.insert(i, BREAK);
                        start += breakAdvance;
                        end += breakAdvance;
                    }
                    break;
                }
            }
            start = input.indexOf(PRE_START, end + PRE_END.length());
        }
        return input;
    }

    /**
     * Remove email fragment 'div' tag and replace newlines with 'br' tags
     *
     * @param input
     * @return input
     */
    private static StringBuilder formatEmailFragments(final StringBuilder input) {
        int emailStart = input.indexOf(EMAIL_START);
        int breakAdvance = BREAK.length() - 1;
        while (emailStart != -1) {
            int startLength = EMAIL_START.length();
            int emailEnd = input.indexOf(EMAIL_END, emailStart + startLength);
            if (emailEnd == -1)
                break;

            input.delete(emailEnd, emailEnd + EMAIL_END.length());
            input.delete(emailStart, emailStart + startLength);

            int fullEmail = emailEnd - startLength;
            for (int i = emailStart; i < fullEmail; i++)
                if (input.charAt(i) == '\n') {
                    input.deleteCharAt(i);
                    input.insert(i, BREAK);
                    i += breakAdvance;
                    fullEmail += breakAdvance;
                }

            emailStart = input.indexOf(EMAIL_START, fullEmail);
        }
        return input;
    }

    /**
     * Remove leading and trailing whitespace
     *
     * @param input
     */
    private static StringBuilder trim(final StringBuilder input) {
        int length = input.length();
        int breakLength = BREAK.length();

        while (length > 0) {
            if (input.indexOf(BREAK) == 0)
                input.delete(0, breakLength);
            else if (length >= breakLength && input.lastIndexOf(BREAK) == length - breakLength)
                input.delete(length - breakLength, length);
            else if (Character.isWhitespace(input.charAt(0)))
                input.deleteCharAt(0);
            else if (Character.isWhitespace(input.charAt(length - 1)))
                input.deleteCharAt(length - 1);
            else
                break;
            length = input.length();
        }
        return input;
    }

    /**
     * Format given HTML string so it is ready to be presented in a text view
     *
     * @param html
     * @return formatted HTML
     */
    public static final CharSequence format(final String html) {
        if (html == null)
            return "";
        if (html.length() == 0)
            return "";

        StringBuilder formatted = new StringBuilder(html);

        // Remove e-mail toggle link
        strip(formatted, TOGGLE_START, TOGGLE_END);

        // Remove signature
        strip(formatted, SIGNATURE_START, SIGNATURE_END);

        // Replace div with e-mail content with block quote
        replace(formatted, REPLY_START, REPLY_END, BLOCKQUOTE_START, BLOCKQUOTE_END);

        // Remove hidden div
        strip(formatted, HIDDEN_REPLY_START, HIDDEN_REPLY_END);

        // Replace paragraphs with breaks
        replace(formatted, PARAGRAPH_START, BREAK);
        replace(formatted, PARAGRAPH_END, BREAK);

        formatPres(formatted);

        formatEmailFragments(formatted);

        trim(formatted);

        formatted.insert(0, ROOT_START);
        formatted.append(ROOT_END);

        return formatted;
    }
}