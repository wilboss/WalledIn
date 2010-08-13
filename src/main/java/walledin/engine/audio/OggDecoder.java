/*  Copyright 2010 Ben Ruijl, Wouter Smeenk

This file is part of Walled In.

Walled In is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3, or (at your option)
any later version.

Walled In is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with Walled In; see the file LICENSE.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

 */
package walledin.engine.audio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.apache.log4j.Logger;
import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;

/**
 * This class decodes an Ogg file. It is heavily based on example code delivered
 * with the JOrbis library.
 */
public class OggDecoder {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(OggDecoder.class);

    private int convsize = 4096 * 4;
    private byte[] convbuffer = new byte[convsize];

    public OggData getData(InputStream input) throws IOException {
        if (input == null) {
            throw new IOException("Failed to read OGG, source does not exist?");
        }
        ByteArrayOutputStream dataout = new ByteArrayOutputStream();

        SyncState oy = new SyncState();
        StreamState os = new StreamState();
        Page og = new Page();
        Packet op = new Packet();

        Info vi = new Info();
        Comment vc = new Comment();
        DspState vd = new DspState();
        Block vb = new Block(vd);

        byte[] buffer;
        int bytes = 0;

        boolean bigEndian = ByteOrder.nativeOrder()
                .equals(ByteOrder.BIG_ENDIAN);

        oy.init();

        while (true) {
            int eos = 0;

            int index = oy.buffer(4096);

            buffer = oy.data;
            try {
                bytes = input.read(buffer, index, 4096);
            } catch (Exception e) {
                LOG.error("Failure reading in vorbis");
                LOG.error(e);
                System.exit(0);
            }
            oy.wrote(bytes);
            if (oy.pageout(og) != 1) {
                if (bytes < 4096)
                    break;

                LOG.error("Input does not appear to be an Ogg bitstream.");
                System.exit(0);
            }

            os.init(og.serialno());

            vi.init();
            vc.init();
            if (os.pagein(og) < 0) {

                LOG.error("Error reading first page of Ogg bitstream data.");
                System.exit(0);
            }

            if (os.packetout(op) != 1) {
                LOG.error("Error reading initial header packet.");
                System.exit(0);
            }

            if (vi.synthesis_headerin(vc, op) < 0) {
                LOG.error("This Ogg bitstream does not contain Vorbis audio data.");
                System.exit(0);
            }

            int i = 0;
            while (i < 2) {
                while (i < 2) {

                    int result = oy.pageout(og);
                    if (result == 0)
                        break;

                    if (result == 1) {
                        os.pagein(og);

                        while (i < 2) {
                            result = os.packetout(op);
                            if (result == 0)
                                break;
                            if (result == -1) {

                                LOG.error("Corrupt secondary header.  Exiting.");
                                System.exit(0);
                            }
                            vi.synthesis_headerin(vc, op);
                            i++;
                        }
                    }
                }

                index = oy.buffer(4096);
                buffer = oy.data;
                try {
                    bytes = input.read(buffer, index, 4096);
                } catch (Exception e) {
                    LOG.error("Failed to read Vorbis: ");
                    LOG.error(e);
                    System.exit(0);
                }
                if (bytes == 0 && i < 2) {
                    LOG.error("End of file before finding all Vorbis headers!");
                    System.exit(0);
                }
                oy.wrote(bytes);
            }

            convsize = 4096 / vi.channels;

            vd.synthesis_init(vi);
            vb.init(vd);

            float[][][] _pcm = new float[1][][];
            int[] _index = new int[vi.channels];

            while (eos == 0) {
                while (eos == 0) {

                    int result = oy.pageout(og);
                    if (result == 0)
                        break;
                    if (result == -1) {
                        LOG.error("Corrupt or missing data in bitstream; continuing...");
                    } else {
                        os.pagein(og);

                        while (true) {
                            result = os.packetout(op);

                            if (result == 0)
                                break;
                            if (result == -1) {

                            } else {

                                int samples;
                                if (vb.synthesis(op) == 0) {
                                    vd.synthesis_blockin(vb);
                                }

                                while ((samples = vd.synthesis_pcmout(_pcm,
                                        _index)) > 0) {
                                    float[][] pcm = _pcm[0];

                                    int bout = (samples < convsize ? samples
                                            : convsize);

                                    for (i = 0; i < vi.channels; i++) {
                                        int ptr = i * 2;

                                        int mono = _index[i];
                                        for (int j = 0; j < bout; j++) {
                                            int val = (int) (pcm[i][mono + j] * 32767.);

                                            if (val > 32767) {
                                                val = 32767;

                                            }
                                            if (val < -32768) {
                                                val = -32768;

                                            }
                                            if (val < 0)
                                                val = val | 0x8000;

                                            if (bigEndian) {
                                                convbuffer[ptr] = (byte) (val >>> 8);
                                                convbuffer[ptr + 1] = (byte) (val);
                                            } else {
                                                convbuffer[ptr] = (byte) (val);
                                                convbuffer[ptr + 1] = (byte) (val >>> 8);
                                            }
                                            ptr += 2 * (vi.channels);
                                        }
                                    }

                                    dataout.write(convbuffer, 0, 2
                                            * vi.channels * bout);

                                    vd.synthesis_read(bout);

                                }
                            }
                        }
                        if (og.eos() != 0)
                            eos = 1;
                    }
                }
                if (eos == 0) {
                    index = oy.buffer(4096);
                    if (index >= 0) {
                        buffer = oy.data;
                        try {
                            bytes = input.read(buffer, index, 4096);
                        } catch (Exception e) {
                            LOG.error("Failure during vorbis decoding");
                            LOG.error(e);
                            System.exit(0);
                        }
                    } else {
                        bytes = 0;
                    }
                    oy.wrote(bytes);
                    if (bytes == 0)
                        eos = 1;
                }
            }

            os.clear();

            vb.clear();
            vd.clear();
            vi.clear();
        }

        oy.clear();

        OggData ogg = new OggData(dataout.toByteArray(),
                vi.rate, vi.channels);

        return ogg;
    }
}