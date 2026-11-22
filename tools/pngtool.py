"""Tiny dependency-free PNG reader/writer used by the asset tooling."""
import struct
import zlib


def read_png(path):
    data = open(path, 'rb').read()
    pos = 8
    width = height = None
    idat = b''
    while pos < len(data):
        length = struct.unpack('>I', data[pos:pos + 4])[0]
        kind = data[pos + 4:pos + 8]
        chunk = data[pos + 8:pos + 8 + length]
        pos += 12 + length
        if kind == b'IHDR':
            width, height, _depth, _ctype = struct.unpack('>IIBB', chunk[:10])
        elif kind == b'IDAT':
            idat += chunk
    raw = zlib.decompress(idat)
    stride = width * 4
    rows = []
    prev = bytearray(stride)
    i = 0
    for _y in range(height):
        filt = raw[i]
        i += 1
        line = bytearray(raw[i:i + stride])
        i += stride
        if filt == 1:
            for x in range(4, stride):
                line[x] = (line[x] + line[x - 4]) & 255
        elif filt == 2:
            for x in range(stride):
                line[x] = (line[x] + prev[x]) & 255
        elif filt == 3:
            for x in range(stride):
                left = line[x - 4] if x >= 4 else 0
                line[x] = (line[x] + ((left + prev[x]) >> 1)) & 255
        elif filt == 4:
            for x in range(stride):
                a = line[x - 4] if x >= 4 else 0
                b = prev[x]
                c = prev[x - 4] if x >= 4 else 0
                pa, pb, pc = abs(b - c), abs(a - c), abs(a + b - 2 * c)
                pred = a if (pa <= pb and pa <= pc) else (b if pb <= pc else c)
                line[x] = (line[x] + pred) & 255
        rows.append(bytes(line))
        prev = line
    pixels = [[tuple(row[x * 4:x * 4 + 4]) for x in range(width)] for row in rows]
    return width, height, pixels


def write_png(path, width, height, pixels):
    raw = b''.join(b'\x00' + b''.join(bytes(px) for px in row) for row in pixels)
    def chunk(kind, payload):
        return (struct.pack('>I', len(payload)) + kind + payload
                + struct.pack('>I', zlib.crc32(kind + payload) & 0xffffffff))
    png = b'\x89PNG\r\n\x1a\n'
    png += chunk(b'IHDR', struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0))
    png += chunk(b'IDAT', zlib.compress(raw, 9))
    png += chunk(b'IEND', b'')
    with open(path, 'wb') as out:
        out.write(png)


def crop(pixels, x0, y0, w, h):
    return [row[x0:x0 + w] for row in pixels[y0:y0 + h]]


def scale(pixels, factor):
    out = []
    for row in pixels:
        big = [px for px in row for _ in range(factor)]
        for _ in range(factor):
            out.append(big)
    return out
