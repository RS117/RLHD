/*
 * Copyright (c) 2021, Hooder <ahooder@protonmail.com>
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

// Workaround wrapper for drivers that do not support dynamic indexing,
// particularly Intel drivers on MacOS, where OpenGL 4.1 is the latest version
Material fetchMaterial(int index) {
#if __VERSION__ > 410 // Assume that more recent versions support dynamic indexing
    return material[index];
#else
    switch (index) {
        case 0: return material[0];
        case 1: return material[1];
        case 2: return material[2];
        case 3: return material[3];
        case 4: return material[4];
        case 5: return material[5];
        case 6: return material[6];
        case 7: return material[7];
        case 8: return material[8];
        case 9: return material[9];
        case 10: return material[10];
        case 11: return material[11];
        case 12: return material[12];
        case 13: return material[13];
        case 14: return material[14];
        case 15: return material[15];
        case 16: return material[16];
        case 17: return material[17];
        case 18: return material[18];
        case 19: return material[19];
        case 20: return material[20];
        case 21: return material[21];
        case 22: return material[22];
        case 23: return material[23];
        case 24: return material[24];
        case 25: return material[25];
        case 26: return material[26];
        case 27: return material[27];
        case 28: return material[28];
        case 29: return material[29];
        case 30: return material[30];
        case 31: return material[31];
        case 32: return material[32];
        case 33: return material[33];
        case 34: return material[34];
        case 35: return material[35];
        case 36: return material[36];
        case 37: return material[37];
        case 38: return material[38];
        case 39: return material[39];
        case 40: return material[40];
        case 41: return material[41];
        case 42: return material[42];
        case 43: return material[43];
        case 44: return material[44];
        case 45: return material[45];
        case 46: return material[46];
        case 47: return material[47];
        case 48: return material[48];
        case 49: return material[49];
        case 50: return material[50];
        case 51: return material[51];
        case 52: return material[52];
        case 53: return material[53];
        case 54: return material[54];
        case 55: return material[55];
        case 56: return material[56];
        case 57: return material[57];
        case 58: return material[58];
        case 59: return material[59];
        case 60: return material[60];
        case 61: return material[61];
        case 62: return material[62];
        case 63: return material[63];
        case 64: return material[64];
        case 65: return material[65];
        case 66: return material[66];
        case 67: return material[67];
        case 68: return material[68];
        case 69: return material[69];
        case 70: return material[70];
        case 71: return material[71];
        case 72: return material[72];
        case 73: return material[73];
        case 74: return material[74];
        case 75: return material[75];
        case 76: return material[76];
        case 77: return material[77];
        case 78: return material[78];
        case 79: return material[79];
        case 80: return material[80];
        case 81: return material[81];
        case 82: return material[82];
        case 83: return material[83];
        case 84: return material[84];
        case 85: return material[85];
        case 86: return material[86];
        case 87: return material[87];
        case 88: return material[88];
        case 89: return material[89];
        case 90: return material[90];
        case 91: return material[91];
        case 92: return material[92];
        case 93: return material[93];
        case 94: return material[94];
        case 95: return material[95];
        case 96: return material[96];
        case 97: return material[97];
        case 98: return material[98];
        case 99: return material[99];
        case 100: return material[100];
        case 101: return material[101];
        case 102: return material[102];
        case 103: return material[103];
        case 104: return material[104];
        case 105: return material[105];
        case 106: return material[106];
        case 107: return material[107];
        case 108: return material[108];
        case 109: return material[109];
        case 110: return material[110];
        case 111: return material[111];
        case 112: return material[112];
        case 113: return material[113];
        case 114: return material[114];
        case 115: return material[115];
        case 116: return material[116];
        case 117: return material[117];
        case 118: return material[118];
        case 119: return material[119];
        case 120: return material[120];
        case 121: return material[121];
        case 122: return material[122];
        case 123: return material[123];
        case 124: return material[124];
        case 125: return material[125];
        case 126: return material[126];
        case 127: return material[127];
        case 128: return material[128];
        case 129: return material[129];
        case 130: return material[130];
        case 131: return material[131];
        case 132: return material[132];
        case 133: return material[133];
        case 134: return material[134];
        case 135: return material[135];
        case 136: return material[136];
        case 137: return material[137];
        case 138: return material[138];
        case 139: return material[139];
        case 140: return material[140];
        case 141: return material[141];
        case 142: return material[142];
        case 143: return material[143];
        case 144: return material[144];
        case 145: return material[145];
        case 146: return material[146];
        case 147: return material[147];
        case 148: return material[148];
        case 149: return material[149];
        case 150: return material[150];
        case 151: return material[151];
        case 152: return material[152];
        case 153: return material[153];
        case 154: return material[154];
        case 155: return material[155];
        case 156: return material[156];
        case 157: return material[157];
        case 158: return material[158];
        case 159: return material[159];
        case 160: return material[160];
        case 161: return material[161];
        case 162: return material[162];
        case 163: return material[163];
        case 164: return material[164];
        case 165: return material[165];
        case 166: return material[166];
        case 167: return material[167];
        case 168: return material[168];
        case 169: return material[169];
        case 170: return material[170];
        case 171: return material[171];
        case 172: return material[172];
        case 173: return material[173];
        case 174: return material[174];
        case 175: return material[175];
        case 176: return material[176];
        case 177: return material[177];
        case 178: return material[178];
        case 179: return material[179];
        case 180: return material[180];
        case 181: return material[181];
        case 182: return material[182];
        case 183: return material[183];
        case 184: return material[184];
        case 185: return material[185];
        case 186: return material[186];
        case 187: return material[187];
        case 188: return material[188];
        case 189: return material[189];
        case 190: return material[190];
        case 191: return material[191];
        case 192: return material[192];
        case 193: return material[193];
        case 194: return material[194];
        case 195: return material[195];
        case 196: return material[196];
        case 197: return material[197];
        case 198: return material[198];
        case 199: return material[199];
        default: return material[0];
    }
#endif
}
