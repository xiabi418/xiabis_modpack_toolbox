#!/usr/bin/env python3
"""Generates the placeholder pixel art for the omni generator block and its two upgrade cards.

The environment has no Pillow, so this file contains a tiny PNG encoder (same approach as the other
generator scripts in this folder).
"""
import json
import os
import struct
import zlib

BASE = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src', 'main', 'resources', 'assets',
                    'xiabis_modpack_toolbox')
TEX_ITEM = os.path.join(BASE, 'textures', 'item')
TEX_BLOCK = os.path.join(BASE, 'textures', 'block')
MODELS_ITEM = os.path.join(BASE, 'models', 'item')
MODELS_BLOCK = os.path.join(BASE, 'models', 'block')
BLOCKSTATES = os.path.join(BASE, 'blockstates')

TRANSPARENT = (0, 0, 0, 0)


def png_bytes(width, height, pixels):
    raw = b''
    for y in range(height):
        raw += b'\x00'
        for x in range(width):
            raw += bytes(pixels[y][x])

    def chunk(tag, data):
        payload = tag + data
        return struct.pack('>I', len(data)) + payload + struct.pack('>I', zlib.crc32(payload) & 0xFFFFFFFF)

    header = struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0)
    return (b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', header)
            + chunk(b'IDAT', zlib.compress(raw, 9)) + chunk(b'IEND', b''))


def write_png(path, rows, palette):
    height = len(rows)
    width = len(rows[0])
    pixels = [[palette.get(char, TRANSPARENT) for char in row] for row in rows]
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'wb') as handle:
        handle.write(png_bytes(width, height, pixels))
    print('wrote', os.path.relpath(path, BASE), os.path.getsize(path), 'bytes')


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as handle:
        json.dump(data, handle, indent=2)
        handle.write('\n')
    print('wrote', os.path.relpath(path, BASE))


# ----------------------------------------------------------------------------- block textures
# 0 dark casing, 1 casing highlight, 2 casing shadow, 3 copper, 4 copper light, 5 copper dark,
# 6 vent dark, 7 glow green, 8 glow dim
BLOCK_PALETTE = {
    '0': (58, 63, 69, 255),
    '1': (78, 84, 91, 255),
    '2': (38, 41, 46, 255),
    '3': (184, 115, 51, 255),
    '4': (214, 145, 78, 255),
    '5': (138, 84, 36, 255),
    '6': (26, 28, 32, 255),
    '7': (57, 211, 83, 255),
    '8': (32, 120, 52, 255),
}

TOP = [
    '2222222222222222',
    '2111111111111112',
    '2100000000000012',
    '2100033333000012',
    '2100344443000012',
    '2100345543000012',
    '2100345543000012',
    '2100033333000012',
    '2100000000000012',
    '2100000000000012',
    '2100600660000012',
    '2100000000000012',
    '2111111111111112',
    '2100000000000012',
    '2111111111111112',
    '2222222222222222',
]

SIDE = [
    '2222222222222222',
    '2111111111111112',
    '2100000000033312',
    '2106600060034512',
    '2106600060033312',
    '2106600060000012',
    '2106600060033312',
    '2106600060034512',
    '2106600060033312',
    '2100000000033312',
    '2100000000034512',
    '2133333333333312',
    '2100000000000012',
    '2111111111111112',
    '2111111111111112',
    '2222222222222222',
]

FRONT = [
    '2222222222222222',
    '2111111111111112',
    '2133333333333312',
    '2134555555554312',
    '2134000000004312',
    '2134077777704312',
    '2134078888704312',
    '2134078008704312',
    '2134078008704312',
    '2134078888704312',
    '2134077777704312',
    '2134000000004312',
    '2134555555554312',
    '2133333333333312',
    '2111111111111112',
    '2222222222222222',
]

# ----------------------------------------------------------------------------- cards
CARD_PALETTE = {
    '0': (26, 32, 28, 255),
    '1': (44, 52, 46, 255),
    '2': (18, 22, 20, 255),
    '3': (196, 155, 72, 255),
    '4': (255, 226, 140, 255),
    '5': (57, 211, 83, 255),
    '6': (32, 120, 52, 255),
    '7': (74, 158, 232, 255),
    '8': (36, 92, 158, 255),
    '9': (231, 214, 106, 255),
}

ENERGY_CARD = [
    '................',
    '..222222222222..',
    '..211111111112..',
    '..213333333312..',
    '..213444444312..',
    '..213455554312..',
    '..213466664312..',
    '..213466664312..',
    '..213455554312..',
    '..213444444312..',
    '..213333333312..',
    '..219999999912..',
    '..219999999912..',
    '..211111111112..',
    '..222222222222..',
    '................',
]

SPEED_CARD = [
    '................',
    '..222222222222..',
    '..211111111112..',
    '..213333333312..',
    '..213444444312..',
    '..213477774312..',
    '..213478874312..',
    '..213788874312..',
    '..213477774312..',
    '..213444444312..',
    '..213333333312..',
    '..219999999912..',
    '..219999999912..',
    '..211111111112..',
    '..222222222222..',
    '................',
]


def keep_existing(path):
    return os.path.exists(path)


def main():
    textures = {
        os.path.join(TEX_BLOCK, 'omni_generator_top.png'): (TOP, BLOCK_PALETTE),
        os.path.join(TEX_BLOCK, 'omni_generator_side.png'): (SIDE, BLOCK_PALETTE),
        os.path.join(TEX_BLOCK, 'omni_generator_front.png'): (FRONT, BLOCK_PALETTE),
        os.path.join(TEX_ITEM, 'energy_card.png'): (ENERGY_CARD, CARD_PALETTE),
        os.path.join(TEX_ITEM, 'speed_card.png'): (SPEED_CARD, CARD_PALETTE),
    }
    for path, (rows, palette) in textures.items():
        if keep_existing(path):
            print('keep existing', os.path.relpath(path, BASE))
            continue
        write_png(path, rows, palette)

    write_json(os.path.join(MODELS_BLOCK, 'omni_generator.json'), {
        'parent': 'minecraft:block/orientable',
        'textures': {
            'top': 'xiabis_modpack_toolbox:block/omni_generator_top',
            'bottom': 'xiabis_modpack_toolbox:block/omni_generator_top',
            'front': 'xiabis_modpack_toolbox:block/omni_generator_front',
            'side': 'xiabis_modpack_toolbox:block/omni_generator_side',
        },
    })

    variants = {}
    for facing, rotation in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
        entry = {'model': 'xiabis_modpack_toolbox:block/omni_generator'}
        if rotation:
            entry['y'] = rotation
        variants['facing=' + facing] = entry
    write_json(os.path.join(BLOCKSTATES, 'omni_generator.json'), {'variants': variants})

    write_json(os.path.join(MODELS_ITEM, 'omni_generator.json'),
               {'parent': 'xiabis_modpack_toolbox:block/omni_generator'})
    for card in ('energy_card', 'speed_card'):
        write_json(os.path.join(MODELS_ITEM, card + '.json'), {
            'parent': 'minecraft:item/generated',
            'textures': {'layer0': 'xiabis_modpack_toolbox:item/' + card},
        })


if __name__ == '__main__':
    main()
