package criptografia.mickey;

public class MICKEY extends MICKEYBase {
    final static int R_Mask0  = 0x1d5363d5; 
    final static int R_Mask1  = 0x415a0aac;
    final static int R_Mask2  = 0x0000d2a8;
    final static int Comp00   = 0x6aa97a30;
    final static int Comp01   = 0x7942a809;
    final static int Comp02   = 0x00003fea;
    final static int Comp10   = 0xdd629e9a;
    final static int Comp11   = 0xe3a21d63;
    final static int Comp12   = 0x00003dd7;
    final static int S_Mask00 = 0x9ffa7faf;
    final static int S_Mask01 = 0xaf4a9381;
    final static int S_Mask02 = 0x00005802;
    final static int S_Mask10 = 0x4c8cb877; 
    final static int S_Mask11 = 0x4911b063; 
    final static int S_Mask12 = 0x0000c52b; 
    
    ///////////////////////////////////////////////////////////////////////////
    
    public MICKEY() {
        super();
        this.key = new int[10];
        this.cached_setupNonce_R = new int[3];
        this.cached_setupNonce_S = new int[3];
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    /*
     * El registro R es el LINEAL, del tipo Galois
     */
    final static void clockR(int input_bit, int control_bit, int[] R, int[] S) {
        int Feedback_bit;
        int Carry0, Carry1;

        Feedback_bit = ((R[2] >>> 15) & 1) ^ input_bit;
        Carry0 = (R[0] >>> 31) & 1;
        Carry1 = (R[1] >>> 31) & 1;
        
        /*
         * Seg�n el bit de control, nos desplazamos normalmente 
         * o con un XOR a si mismo extra
         */
        if (0 != control_bit) {
        	//Esta operaci�n equivale a multiplicar por x+1 o desplazar J veces
            R[0] ^= (R[0] << 1); //Operaci�n de XOR con si mismo desplazado
            R[1] ^= (R[1] << 1) ^ Carry0;
            R[2] ^= (R[2] << 1) ^ Carry1;
        }
        else {
        	//Esta operaci�n equivale a multiplicar por x
            R[0] <<= 1;
            R[1] = (R[1] << 1) ^ Carry0;
            R[2] = (R[2] << 1) ^ Carry1;
        }

        if (0 != Feedback_bit) {
            R[0] ^= R_Mask0;
            R[1] ^= R_Mask1;
            R[2] ^= R_Mask2;
        }
    }    
    /*
     * El registro ese es NO LINEAL y se desplaza de formas distintas seg�n el bit de control
     */
    final static void clockS(int input_bit, int control_bit, int[] R, int[] S) {
        int Feedback_bit;
        int Carry0, Carry1;

        Feedback_bit = ((S[2] >>> 15) & 1) ^ input_bit;
        Carry0 = (S[0] >>> 31) & 1;
        Carry1 = (S[1] >>> 31) & 1;

        S[0] = (S[0] << 1) ^ ((S[0] ^ Comp00) & ((S[0] >>> 1) ^ (S[1] << 31) ^ Comp10) & 0xfffffffe);
        S[1] = (S[1] << 1) ^ ((S[1] ^ Comp01) & ((S[1] >>> 1) ^ (S[2] << 31) ^ Comp11)) ^ Carry0;
        S[2] = (S[2] << 1) ^ ((S[2] ^ Comp02) & ((S[2] >>> 1) ^ Comp12) & 0x7fff) ^ Carry1;

        if (0 != Feedback_bit) {
            if (0 != control_bit) {
                S[0] ^= S_Mask10;
                S[1] ^= S_Mask11;
                S[2] ^= S_Mask12;
            }
            else {
                S[0] ^= S_Mask00;
                S[1] ^= S_Mask01;
                S[2] ^= S_Mask02;
            }
        }
    }
    
    final static int clockKG1(int input_bit, int[] R, int[] S) {
        int Keystream_bit;
        int control_bit_r;
        int control_bit_s;

        Keystream_bit = (R[0] ^ S[0]) & 1;
        control_bit_r = ((S[0] >>> 27) ^ (R[1] >>> 21)) & 1;
        control_bit_s = ((S[1] >>> 21) ^ (R[0] >>> 26)) & 1;

        clockR(((S[1] >>> 8) & 1) ^ input_bit, control_bit_r, R, S);
        clockS(input_bit, control_bit_s, R, S);

        return Keystream_bit;
    }

    ///////////////////////////////////////////////////////////////////////////
    
    public int getKeySize() {
        return 10;  // 80bit
    }
    
    public void processMio(byte[] inBuf, int inOfs, byte[] outBuf, int outOfs, int len) throws Exception{
        int inEnd;
        int reg;
        
        inEnd = inOfs + len;
        
        int[] R = {R0, R1, R2};
        int[] S = {S0, S1, S2};
        
        /*
         * Esto no funciona todav�a!
         */
        while (inOfs < inEnd) {
        	reg = inBuf[inOfs++];
        	for (int s = 7; s >= 0; s--) {
        		reg ^= clockKG1(0, R, S);		
        	}
        	outBuf[outOfs++] = (byte)reg;
        }
    }
    
    public void process(byte[] inBuf, int inOfs, byte[] outBuf, int outOfs, int len) throws Exception {
        int inEnd;
        int reg;
        int Feedback_bit;
        int Carry0, Carry1;
        int control_bit_r;
        int control_bit_s;
        int R0, R1, R2;
        int S0, S1, S2;

        // a CPU with lots of registers helps to speed this one up
        R0 = this.R0 ;
        R1 = this.R1;
        R2 = this.R2;
        S0 = this.S0;
        S1 = this.S1;
        S2 = this.S2;
        
        inEnd = inOfs + len;
        while (inOfs < inEnd) {
            reg = inBuf[inOfs++];
            
            for (int s = 7; s >= 0; s--) {
                
                reg ^= ((R0 ^ S0) & 1) << s;

                control_bit_r = ((S0 >>> 27) ^ (R1 >>> 21)) & 1;
                control_bit_s = ((S1 >>> 21) ^ (R0 >>> 26)) & 1;

                //clockR(0, control_bit_r, R, S);

                Feedback_bit = (R2 >>> 15) & 1;
                Carry0 = (R0 >>> 31) & 1;
                Carry1 = (R1 >>> 31) & 1;

                if (0 != control_bit_r) {
                    R0 ^= (R0 << 1);
                    R1 ^= (R1 << 1) ^ Carry0;
                    R2 ^= (R2 << 1) ^ Carry1;
                }
                else {
                    R0 <<= 1;
                    R1 = (R1 << 1) ^ Carry0;
                    R2 = (R2 << 1) ^ Carry1;
                }
                
                // NOTE: played around with arrays to avoid jumps, didn't help
                if (0 != Feedback_bit) {
                    R0 ^= R_Mask0;
                    R1 ^= R_Mask1;
                    R2 ^= R_Mask2;
                }
                
                //clockS(0, control_bit_s, R, S);
                
                Feedback_bit = (S2 >>> 15) & 1;
                Carry0 = (S0 >>> 31) & 1;
                Carry1 = (S1 >>> 31) & 1;

                S0 = (S0 << 1) ^ ((S0 ^ Comp00) & ((S0 >>> 1) ^ (S1 << 31) ^ Comp10) & 0xfffffffe);
                S1 = (S1 << 1) ^ ((S1 ^ Comp01) & ((S1 >>> 1) ^ (S2 << 31) ^ Comp11)) ^ Carry0;
                S2 = (S2 << 1) ^ ((S2 ^ Comp02) & ((S2 >>> 1) ^ Comp12) & 0x7fff) ^ Carry1;

                if (0 != Feedback_bit) {
                    if (0 != control_bit_s) {
                        S0 ^= S_Mask10;
                        S1 ^= S_Mask11;
                        S2 ^= S_Mask12;
                    }
                    else {
                        S0 ^= S_Mask00;
                        S1 ^= S_Mask01;
                        S2 ^= S_Mask02;
                    }
                }
            }
            
            outBuf[outOfs++] = (byte)reg;
        }
        
        this.R0 = R0;
        this.R1 = R1;
        this.R2 = R2;
        this.S0 = S0;
        this.S1 = S1;
        this.S2 = S2;
    }

    public void setupKey(int mode, byte[] key, int ofs) throws Exception {
        int end = ofs + getKeySize();
        int i = 0;
        
        while (ofs < end) {
            this.key[i++] = key[ofs++] & 0x0ff;
        }
    }

    public void setupNonce(byte[] nonce, int ofs) throws Exception {
        int i;
        int iv_or_key_bit;
        int nsize = this.nsize << 3;    // in bits, bitches!
        int[] key = this.key;
        int[] R = this.cached_setupNonce_R; 
        int[] S = this.cached_setupNonce_S;
        
        for (i = 0; i < 3; i++) {
            R[i] = 0;
            S[i] = 0;
        }
        
        // we could inline clockKG1() as in processBytes(), but it's a question
        // of the environment to have the increase in code size being acceptable

        for (i = 0; i < nsize; i++) {
            iv_or_key_bit = (nonce[(i >>> 3) + ofs] >>> (7 - (i & 7))) & 1;
            clockKG1(iv_or_key_bit, R, S);
        }

        for (i = 0; i < 80; i++) {
            iv_or_key_bit = (key[i >>> 3] >>> (7 - (i & 7))) & 1;
            clockKG1(iv_or_key_bit, R, S);
        }

        for (i = 0; i < 80; i++) {
            clockKG1(0, R, S);
        }
        
        this.R0 = R[0];
        this.R1 = R[1];
        this.R2 = R[2];
        this.S0 = S[0];
        this.S1 = S[1];
        this.S2 = S[2];
    }
    

}
