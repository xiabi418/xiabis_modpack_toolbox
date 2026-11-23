#!/usr/bin/env python3
import json, os, struct, zlib
ROOT=os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src', 'main', 'resources', 'assets', 'xiabis_modpack_toolbox')
PNG=os.path.join(ROOT,'textures','block','compressed_condenser.png')
MODEL=os.path.join(ROOT,'models','block','compressed_condenser.json')
ITEM=os.path.join(ROOT,'models','item','compressed_condenser.json')
def png(w,h,p):
    def chunk(t,d):
        q=t+d; return struct.pack('>I',len(d))+q+struct.pack('>I',zlib.crc32(q)&0xffffffff)
    raw=b''.join(b'\0'+b''.join(bytes(px) for px in row) for row in p)
    head=struct.pack('>IIBBBBB',w,h,8,6,0,0,0)
    return b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',head)+chunk(b'IDAT',zlib.compress(raw,9))+chunk(b'IEND',b'')
def main():
    os.makedirs(os.path.dirname(PNG),exist_ok=True)
    if not os.path.exists(PNG):
        p=[]
        for y in range(16):
            row=[]
            for x in range(16):
                border=x in (0,15) or y in (0,15)
                core=3<=x<=12 and 3<=y<=12
                glow=(x+y)%4==0 and core
                row.append((24,28,34,255) if border else ((48,58,70,255) if core and not glow else (92,190,214,255) if glow else (30,36,44,255)))
            p.append(row)
        with open(PNG,'wb') as f:f.write(png(16,16,p))
    os.makedirs(os.path.dirname(MODEL),exist_ok=True)
    for path,data in [(MODEL,{'parent':'minecraft:block/cube_all','render_type':'minecraft:cutout','textures':{'all':'xiabis_modpack_toolbox:block/compressed_condenser'}}),(ITEM,{'parent':'xiabis_modpack_toolbox:block/compressed_condenser'})]:
        with open(path,'w',encoding='utf-8') as f: json.dump(data,f,indent=2); f.write('\n')
if __name__=='__main__': main()
