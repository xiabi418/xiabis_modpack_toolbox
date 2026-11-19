#!/usr/bin/env python3
"""Generates the placeholder art of the time acceleration wand.

Pure python (zlib) because Pillow is not installed in this environment.
"""
import os
import struct
import zlib

HERE = os.path.dirname(os.path.abspath(__file__))
RES = os.path.join(HERE, '..', 'src', 'main', 'resources', 'assets', 'xiabis_modpack_toolbox')
TEX_ITEM = os.path.join(RES, 'textures', 'item')
MODELS = os.path.join(RES, 'models', 'item')

PALETTE = {
    'g': (201, 163, 255, 255),   # bright amethyst
    'G': (155, 111, 224, 255),   # mid amethyst
    'D': (109, 70, 176, 255),    # dark amethyst
    'w': (240, 230, 255, 255),   # highlight
    's': (90, 70, 50, 255),      # shaft
    'S': (138, 107, 74, 255),    # shaft highlight
}

ART = [
    "................",
    "...........gD...",
    "..........gGGD..",
    ".........wGGGD..",
    "..........DGGD..",
    "...........DG...",
    "..........ss....",
    ".........sSs....",
    "........sSs.....",
    ".......sSs......",
    "......sSs.......",
    ".....sSs........",
    "....sSs.........",
    "...sSs..........",
    "..sSs...........",
    "................",
]


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


def wand_texture():
    target = os.path.join(TEX_ITEM, 'time_wand.png')
    if os.path.exists(target):
        # Hand drawn art from the user takes precedence over the placeholder.
        print('keep existing', os.path.relpath(target, HERE))
        return
    width, height = 16, 16
    px = blank(width, height)
    for y, row in enumerate(ART):
        for x, char in enumerate(row):
            if char in PALETTE:
                put(px, x, y, PALETTE[char])
    save_png(os.path.join(TEX_ITEM, 'time_wand.png'), width, height, px)


def wand_model():
    payload = {
        'parent': 'minecraft:item/handheld',
        'textures': {'layer0': 'xiabis_modpack_toolbox:item/time_wand'},
    }
    path = os.path.join(MODELS, 'time_wand.json')
    os.makedirs(os.path.dirname(path), exist_ok=True)
    import json
    with open(path, 'w', encoding='utf-8') as handle:
        json.dump(payload, handle, indent=2)
        handle.write('\n')
    print('wrote', os.path.relpath(path, HERE))


if __name__ == '__main__':
    wand_texture()
    wand_model()
