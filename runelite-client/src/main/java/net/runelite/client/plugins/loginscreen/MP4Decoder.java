/*
 * Copyright (c) 2020, TheStonedTurtle <https://github.com/TheStonedTurtle>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.loginscreen;

import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import com.xuggle.xuggler.Utils;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Decodes an MP4 file frame by frame into BufferedImages using xuggler.
 * Based off the `DecodeAndCaptureFrames` demo released by xuggler
 */
@Slf4j
class MP4Decoder
{
	private final File file;
	private final boolean repeat;

	private IContainer container;
	private IStreamCoder videoCoder;
	private int videoStreamIndex;
	private IVideoResampler resampler;

	@Getter
	private boolean valid = false;

	MP4Decoder(final File file)
	{
		this(file, true);
	}

	MP4Decoder(final File file, final boolean repeat)
	{
		this.file = file;
		this.repeat = repeat;

		container = IContainer.make();
		if (container.open(file.getAbsolutePath(), IContainer.Type.READ, null) < 0)
		{
			log.error("Could not open file: {}", file);
			return;
		}

		findFirstVideoStream();
		if (videoCoder == null)
		{
			log.error("Could not find video stream in container: {}", file);
			return;
		}

		if (videoCoder.open() < 0)
		{
			log.error("Could not open video decoder for container: {}", file);
			return;
		}

		if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24)
		{
			// if this stream is not in BGR24, we're going to need to
			// convert it.  The VideoResampler does that for us.

			resampler = IVideoResampler.make(
				videoCoder.getWidth(), videoCoder.getHeight(), IPixelFormat.Type.BGR24,
				videoCoder.getWidth(), videoCoder.getHeight(), videoCoder.getPixelType());
			if (resampler == null)
			{
				log.error("could not create color space resampler for: {}", file);
				return;
			}
		}

		valid = true;
	}

	private void findFirstVideoStream()
	{
		for (int i = 0; i < container.getNumStreams(); i++)
		{
			final IStream stream = container.getStream(i);
			final IStreamCoder coder = stream.getStreamCoder();
			if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO)
			{
				videoCoder = coder;
				videoStreamIndex = i;
				return;
			}
		}
	}

	@Nullable
	BufferedImage getNextFrame()
	{
		if (!valid)
		{
			log.error("Attempting to get the frame of an invalid MP4Decoder");
			return null;
		}

		IPacket packet = IPacket.make();
		while (container.readNextPacket(packet) >= 0)
		{
			if (packet.getStreamIndex() != videoStreamIndex)
			{
				continue;
			}

			// We allocate a new picture to get the data out of Xuggle
			final IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(),
				videoCoder.getWidth(), videoCoder.getHeight());

			int offset = 0;
			while (offset < packet.getSize())
			{
				// Now, we decode the video, checking for any errors.
				int bytesDecoded = videoCoder.decodeVideo(picture, packet, offset);
				if (bytesDecoded < 0)
				{
					log.error("Error decoding MP4 video: {}", file);
					return null;
				}
				offset += bytesDecoded;

				// Some decoders will consume data in a packet, but will not
				// be able to construct a full video picture yet.  Therefore
				// you should always check if you got a complete picture from
				// the decode.
				if (picture.isComplete())
				{
					IVideoPicture newPic = picture;

					// If the resampler is not null, it means we didn't get the
					// video in BGR24 format and need to convert it into BGR24
					// format.
					if (resampler != null)
					{
						newPic = IVideoPicture.make(resampler.getOutputPixelFormat(),
							picture.getWidth(), picture.getHeight());

						if (resampler.resample(newPic, picture) < 0)
						{
							log.error("Could not resample video from file: ", file);
							return null;
						}
					}

					if (newPic.getPixelType() != IPixelFormat.Type.BGR24)
					{
						log.error("Buffered image in unexpected format, could not decode video file as BGR24: {}" + file);
					}

					// convert the BGR24 to an Java buffered image
					return Utils.videoPictureToImage(newPic);
				}
			}
		}

		// Go back to the first frame
		if (repeat)
		{
			container.seekKeyFrame(videoStreamIndex, 0, IContainer.SEEK_FLAG_FRAME);
		}
		return null;
	}
}
