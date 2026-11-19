#!/usr/bin/env python3
"""Generates the placeholder art of the magic potion flask and the flight effect icon.

Pure python (zlib) because Pillow is not installed in this environment.
"""
import os
import struct
import zlib

HERE = os.path.dirname(os.path.abspath(__file__))
RES = os.path.join(HERE, '..', 'src', 'main', 'resources', 'assets', 'xiabis_modpack_toolbox')
TEX_ITEM = os.path.join(RES, 'textures', 'item')
TEX_EFFECT = os.path.join(RES, 'textures', 'mob_effect')
MODELS = os.path.join(RES, 'models', 'item')

GLASS = (206, 219, 231, 255)
GLASS_DARK = (150, 166, 182, 255)
GLASS_LIGHT = (240, 248, 255, 255)
CORK = (122, 84, 48, 255)
CORK_DARK = (84, 56, 30, 255)
LIQUID = (108, 62, 176, 255)
LIQUID_DARK = (74, 40, 124, 255)
LIQUID_LIGHT = (150, 96, 214, 255)
SPARKLE = (150, 242, 200, 255)
WHITE = (255, 255, 255, 255)
SOFT = (214, 236, 255, 255)


def chunk(kind, payload):
    return (struct.pack('>I', len(payload)) + kind + payload
            + struct.pack('>I', zlib.crc32(kind + payload) & 0xFFFFFFFF))


def save_png(path, width, height, pixels):
    raw = bytearray()
    for y in range(height):
        raw.append(0)
        for x in range(width):
            raw.extend(pixels[y][x])
    data = b'\x89PNG\r\n\x1a\n'
    data += chunk(b'IHDR', struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0))
    data += chunk(b'IDAT', zlib.compress(bytes(raw), 9))
    data += chunk(b'IEND', b'')
    os.makedirs(os.path.dirname(path), exist_ok=True)
    open(path, 'wb').write(data)
    print('wrote', os.path.relpath(path, HERE), len(data), 'bytes')


def blank(width, height):
    return [[(0, 0, 0, 0) for _ in range(width)] for _ in range(height)]


def put(px, x, y, color):
    if 0 <= y < len(px) and 0 <= x < len(px[0]):
        px[y][x] = color


def fill(px, x0, x1, y, color):
    for x in range(x0, x1 + 1):
        put(px, x, y, color)


def bottle():
    px = blank(16, 16)
    # cork
    fill(px, 6, 9, 1, CORK)
    fill(px, 6, 9, 2, CORK)
    fill(px, 6, 9, 3, CORK_DARK)
    # neck
    for y in (4, 5):
        fill(px, 6, 9, y, GLASS_DARK)
        fill(px, 7, 8, y, GLASS)
    put(px, 7, 4, GLASS_LIGHT)
    # shoulders
    fill(px, 5, 10, 6, GLASS_DARK)
    fill(px, 6, 9, 6, GLASS)
    # body outline
    for y in range(7, 14):
        put(px, 4, y, GLASS_DARK)
        put(px, 11, y, GLASS_DARK)
        fill(px, 5, 10, y, LIQUID)
    put(px, 5, 7, GLASS)
    put(px, 10, 7, GLASS)
    # bottom
    fill(px, 5, 10, 14, GLASS_DARK)
    fill(px, 6, 9, 14, GLASS)
    # liquid shading + highlights
    for y in range(8, 14):
        put(px, 10, y, LIQUID_DARK)
        put(px, 9, y, LIQUID_DARK)
    for y in range(8, 13):
        put(px, 5, y, LIQUID_LIGHT)
    put(px, 5, 8, GLASS_LIGHT)
    put(px, 6, 9, GLASS_LIGHT)
    # bubbles / sparkles
    put(px, 7, 11, SPARKLE)
    put(px, 6, 12, SPARKLE)
    put(px, 8, 9, SPARKLE)
    put(px, 9, 13, SPARKLE)
    # The shipped texture is hand drawn; only write the generated placeholder when it is absent.
    bottle = os.path.join(TEX_ITEM, 'magic_poison_bottle.png')
    if os.path.exists(bottle):
        print('keep existing', bottle)
    else:
        save_png(bottle, 16, 16, px)


def model():
    path = os.path.join(MODELS, 'magic_poison_bottle.json')
    os.makedirs(os.path.dirname(path), exist_ok=True)
    open(path, 'w', encoding='utf-8').write('''{
  "parent": "item/generated",
  "textures": {
    "layer0": "xiabis_modpack_toolbox:item/magic_poison_bottle"
  }
}
''')
    print('wrote', os.path.relpath(path, HERE))


if __name__ == '__main__':
    bottle()
    model()
