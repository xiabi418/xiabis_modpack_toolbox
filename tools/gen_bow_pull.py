#!/usr/bin/env python3
"""Generates the four bow-draw stages of the omni weapon as 16x16 RGBA PNGs.

The item icon is kept exactly as the base texture, only a bow string is added behind it so the
player can see the draw progress. Pure python (zlib) -- Pillow is not installed in this
environment.
"""
import os
import struct
import zlib

HERE = os.path.dirname(os.path.abspath(__file__))
RES = os.path.join(HERE, '..', 'src', 'main', 'resources', 'assets', 'xiabis_modpack_toolbox')
TEX = os.path.join(RES, 'textures', 'item')
MODELS = os.path.join(RES, 'models', 'item')

ORANGE = (216, 145, 47, 255)
DARK = (122, 74, 24, 255)
STRING = (222, 222, 222, 255)
STRING_DARK = (150, 150, 150, 255)


def read_png(path):
    data = open(path, 'rb').read()
    assert data[:8] == b'\x89PNG\r\n\x1a\n', path
    pos, idat, width, height = 8, b'', 0, 0
    while pos < len(data):
        length = struct.unpack('>I', data[pos:pos + 4])[0]
        kind = data[pos + 4:pos + 8]
        chunk = data[pos + 8:pos + 8 + length]
        pos += 12 + length
        if kind == b'IHDR':
            width, height = struct.unpack('>II', chunk[:8])
        elif kind == b'IDAT':
            idat += chunk
    raw = zlib.decompress(idat)
    bpp, stride = 4, width * 4
    rows, prev, cursor = [], bytearray(stride), 0
    for _ in range(height):
        filt = raw[cursor]
        cursor += 1
        line = bytearray(raw[cursor:cursor + stride])
        cursor += stride
        if filt == 1:
            for x in range(bpp, stride):
                line[x] = (line[x] + line[x - bpp]) & 0xFF
        elif filt == 2:
            for x in range(stride):
                line[x] = (line[x] + prev[x]) & 0xFF
        elif filt == 3:
            for x in range(stride):
                a = line[x - bpp] if x >= bpp else 0
                line[x] = (line[x] + ((a + prev[x]) >> 1)) & 0xFF
        elif filt == 4:
            for x in range(stride):
                a = line[x - bpp] if x >= bpp else 0
                b = prev[x]
                c = prev[x - bpp] if x >= bpp else 0
                p = a + b - c
                pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                pr = a if (pa <= pb and pa <= pc) else (b if pb <= pc else c)
                line[x] = (line[x] + pr) & 0xFF
        rows.append(line)
        prev = line
    return width, height, rows


def write_png(path, width, height, rows):
    raw = b''.join(b'\x00' + bytes(row) for row in rows)
    def chunk(kind, payload):
        return (struct.pack('>I', len(payload)) + kind + payload
                + struct.pack('>I', zlib.crc32(kind + payload) & 0xFFFFFFFF))
    png = b'\x89PNG\r\n\x1a\n'
    png += chunk(b'IHDR', struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0))
    png += chunk(b'IDAT', zlib.compress(raw, 9))
    png += chunk(b'IEND', b'')
    with open(path, 'wb') as handle:
        handle.write(png)


def put(row, x, colour):
    if 0 <= x < len(row) // 4:
        row[x * 4:x * 4 + 4] = bytes(colour)


def draw_line(row0, row1, x0, x1, colour):
    """Simple vertical-ish line between (x0, row0) and (x1, row1)."""
    steps = abs(row1 - row0)
    if steps == 0:
        return
    for i in range(steps + 1):
        x = round(x0 + (x1 - x0) * i / steps)
        yield row0 + i, x


def main():
    width, height, base_rows = read_png(os.path.join(TEX, 'omni_weapon.png'))
    assert (width, height) == (16, 16)

    for stage in range(4):
        rows = [bytearray(row) for row in base_rows]
        # Bow limbs on the left edge, drawn in the item orange.
        for y in range(2, 14):
            limb = ORANGE if 3 <= y <= 12 else DARK
            put(rows[y], 1, limb)
            if y in (2, 13):
                put(rows[y], 2, DARK)
        # String: tips at the limb ends, middle pulled towards the player as the draw advances.
        nock_x = 10 - 2 * stage
        for y, x in draw_line(2, 8, 2, nock_x, STRING):
            put(rows[y], x, STRING)
        for y, x in draw_line(8, 13, nock_x, 2, STRING):
            put(rows[y], x, STRING)
        # Arrow nock sits at the pulled point.
        put(rows[8], nock_x, DARK)
        put(rows[7], nock_x - 1, STRING_DARK)
        put(rows[9], nock_x - 1, STRING_DARK)

        write_png(os.path.join(TEX, f'omni_weapon_pulling_{stage}.png'), width, height, rows)
        print(f'wrote omni_weapon_pulling_{stage}.png')


if __name__ == '__main__':
    main()
