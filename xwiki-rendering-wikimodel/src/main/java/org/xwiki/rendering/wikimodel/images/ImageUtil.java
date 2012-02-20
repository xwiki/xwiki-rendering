/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.rendering.wikimodel.images;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

/**
 * Contains utility methods dealing with images like image size definition or
 * creation of thumbnails (reduced copies of images).
 *
 * @version $Id$
 * @since 4.0M1
 */
public class ImageUtil
{
    /**
     * Creates a smaller version of an image or returns the original image if it
     * was in the specified boundaries. The returned image keeps the ratio of
     * the original image.
     *
     * @param image the image to re-size
     * @param thumbWidth the maximal width of the image
     * @param thumbHeight the maximal height of the image
     * @return a new re-sized image or the original image if it was in the
     *         specified boundaries
     */
    private static BufferedImage createThumb(
        BufferedImage image,
        int thumbWidth,
        int thumbHeight)
    {
        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);
        int[] size = getNewSize(
            imageWidth,
            imageHeight,
            thumbWidth,
            thumbHeight);
        if (size[0] == imageWidth && size[1] == imageHeight) {
            return image;
        }

        BufferedImage thumbImage = new BufferedImage(
            size[0],
            size[1],
            BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = thumbImage.createGraphics();
        graphics2D.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);
        return thumbImage;
    }

    /**
     * Create a reduced image (thumb) of an image from the given input stream.
     * Possible output formats are "jpg" or "png" (no "gif"; it is possible to
     * read "gif" files, but not to write). The resulting thumb is written to
     * the output stream. The both streams will be explicitly closed by this
     * method. This method keeps the ratio width/height of the initial image. If
     * both dimensions of the initial image is less than the specified
     * boundaries it is not re-scaled; it is written to the output stream as is.
     *
     * @param input the input stream containing the image to reduce
     * @param output the output stream where the resulting reduced image is
     * written
     * @param thumbWidth the maximal width of the reduced image
     * @param thumbHeight the maximal height of the reduced image
     * @param format the output format of the reduced image; it can be "jpg",
     * "png", ...
     */
    public static void createThumb(
        InputStream input,
        OutputStream output,
        int thumbWidth,
        int thumbHeight,
        String format) throws IOException
    {
        try {
            try {
                ImageInputStream imageInput = ImageIO
                    .createImageInputStream(input);
                BufferedImage image = ImageIO.read(imageInput);
                BufferedImage thumbImage = createThumb(
                    image,
                    thumbWidth,
                    thumbHeight);
                ImageIO.write(thumbImage, format, output);
            } finally {
                output.close();
            }
        } finally {
            input.close();
        }
    }

    /**
     * Returns the size (width and height) of an image from the given input
     * stream. This method closes the given stream.
     *
     * @param input the input stream with an image
     * @return the size (width and height) of an image from the given input
     *         stream
     */
    public static int[] getImageSize(InputStream input) throws IOException
    {
        try {
            ImageInputStream imageInput = ImageIO.createImageInputStream(input);
            BufferedImage image = ImageIO.read(imageInput);
            return new int[]{image.getWidth(), image.getHeight()};
        } finally {
            input.close();
        }
    }

    /**
     * Returns the possible size of an image from the given input stream; the
     * returned size does not overcome the specified maximal borders but keeps
     * the ratio of the image. This method closes the given stream.
     *
     * @param input the input stream with an image
     * @param maxWidth the maximal width
     * @param maxHeight the maximal height
     * @return the possible size of an image from the given input stream; the
     *         returned size does not overcome the specified maximal borders but
     *         keeps the ratio of the image
     */
    public static int[] getImageSize(
        InputStream input,
        int maxWidth,
        int maxHeight) throws IOException
    {
        int[] size = getImageSize(input);
        return getNewSize(size[0], size[1], maxWidth, maxHeight);
    }

    /**
     * Calculates new size of an image with the specified max borders keeping
     * the ratio between height and width of the image.
     *
     * @param width the initial width of an image
     * @param height the initial height of an image
     * @param maxWidth the maximal width of an image
     * @param maxHeight the maximal height of an image
     * @return a new size of an image where the height and width don't overcome
     *         the specified borders; the size keeps the initial image ratio
     *         between width and height
     */
    public static int[] getNewSize(
        int width,
        int height,
        int maxWidth,
        int maxHeight)
    {
        if (width <= maxWidth && height <= maxHeight) {
            return new int[]{width, height};
        }
        double thumbRatio = (double) maxWidth / (double) maxHeight;
        double imageRatio = (double) width / (double) height;
        if (thumbRatio < imageRatio) {
            maxHeight = (int) (maxWidth / imageRatio);
        } else {
            maxWidth = (int) (maxHeight * imageRatio);
        }
        return new int[]{maxWidth, maxHeight};
    }
}
